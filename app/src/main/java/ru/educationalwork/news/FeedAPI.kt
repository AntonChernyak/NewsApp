package ru.educationalwork.news

class FeedAPI (
    val items: ArrayList<FeedItemAPI>
)

class FeedItemAPI(
    val title: String,
    val link: String,
    val enclosure: EnclosureAPI,
    val description: String
)

class EnclosureAPI(val link: String = "")
