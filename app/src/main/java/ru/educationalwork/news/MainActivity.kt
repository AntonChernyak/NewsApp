package ru.educationalwork.news

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var request: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Realm.init(this)
        // RxJava. Observable - самый используемый класс, у котого Generic - тип возвращаемого значения
        val o =
            createRequest("https://api.rss2json.com/v1/api.json?rss_url=https%3A%2F%2F3dnews.ru%2Fnews%2Frss%2F")
                // rss даёт результат в виде xml. Необходимо преобразовать в объект json с помощью map --> библиотека GSON
                // it - строка, которую переводим. Feed - наш класс, с теми же полями, что и по ссылке
                // не забыть запросить разрешение INTERNET в манифесте
                .map { Gson().fromJson(it, FeedAPI::class.java) }
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

        // Запускаем код. ({} - результат, {} - функция, которая вызывается при ошибке (нет сети например))
        request = o.subscribe({
            val feed = FeedRealm(it.items.mapTo(RealmList<FeedItemRealm>(), {feed -> FeedItemRealm(feed.title, feed.link, feed.enclosure.link, feed.description) }))

            // Записываем в БД Realm
            Realm.getDefaultInstance().executeTransaction { realm ->
                // Если в БД были старые данные, то стираем их, чтобы не хранить мусор
                // Для начала найдём FeedList
                val oldList = realm.where(FeedRealm::class.java).findAll()
                //Есть он есть, то удаляем элементы
                if (oldList.size > 0) oldList.map { oldItem -> oldItem.deleteFromRealm() }
                // Теперь записываем элементы
                realm.copyToRealm(feed)
            }

            showRecView()
        }, {
            // Если нет сети, то он покажет последние данные из БД
            showRecView()
        })


    }

    fun showRecView() {
        // Запрашиваем данные из БД
       Realm.getDefaultInstance().executeTransaction {realm ->
            val feed = realm.where(FeedRealm::class.java).findAll()
            // Т.к. максимум в БД 1 элемент, т.к. старые feed мы стираем, то можем обратиться сразу к 1-му эл-ту
           if (feed.size > 0){
               // Наполняем адаптер
               recyclerView.adapter = RecyclerAdapter(feed[0]!!.items)
               // Задаём вид
               recyclerView.layoutManager = LinearLayoutManager(this)
           }
       }
    }

    override fun onDestroy() {
        // предотвращает утечку памяти (как в AsyncTask). Обрывает цепочку, удаляется ссылка на subscribe и ссылка на activity
        // всё чистит сборщик мусора
        request?.dispose()
        super.onDestroy()
    }
}

