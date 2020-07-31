package ru.educationalwork.news

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import io.realm.RealmList


class RecyclerAdapter(val items: RealmList<FeedItemRealm>): RecyclerView.Adapter<RecHolder>() {
    // Создаём Holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecHolder {
        // Получаем inflater. У того родителя, который вызовет адаптер точно будет контекст
        val inflater = LayoutInflater.from(parent.context)
        // Создаём View. 1 - что надуваем, 2 - куда надуваем, 3 - переиспоользовать один и тот же элемент?
        val view = inflater.inflate(R.layout.list_item, parent, false)
        return RecHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // Заполняем холдер данными. Нам передали holder и сказали записать в него элемент с индексом position
    // Т.к. view обернуто внутри holder'a, то мы внутри и должны ставить к view данные --- метод bind()
    override fun onBindViewHolder(holder: RecHolder, position: Int) {
        val item = items[position]
        if (item != null) {
            holder.bind(item)
        }
    }
}

// Holder --- класс-контейнер, в который мы заворачиваем UI
class RecHolder(view: View):RecyclerView.ViewHolder(view){

    // itemView (поле внутри ViewHolder'a) == view (Поле нашего Holder'a)
    fun bind(item: FeedItemRealm) {
        val vTitle = itemView.findViewById<TextView>(R.id.item_title)
        val vDesc = itemView.findViewById<TextView>(R.id.item_description)
        val vThumb = itemView.findViewById<ImageView>(R.id.item_thumb)
        vTitle.text = item.title
        vDesc.text = item.description

        // Picasso автоматически подгружает изображения и даёт нам их хеширование
       // Picasso.with(vThumb.context).load(item.enclosure).into(vThumb)
       Picasso.with(vThumb.context).load(item.enclosure).into(vThumb)

        itemView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link))
            vThumb.context.startActivity(intent)
        }

    }
}