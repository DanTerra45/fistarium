package wiki.tk.fistarium.features.news.domain

data class NewsArticle(
    val id: String = "",
    val title: String = "",
    val summary: String = "",
    val content: String = "", // Can be markdown or plain text
    val date: Any? = null, // Changed to Any? to handle both String and Number from Firestore
    val version: String? = null, // e.g. "1.05"
    val imageUrl: String? = null,
    val type: NewsType = NewsType.UPDATE,
    val game: String? = null,
    val translations: Map<String, NewsTranslation> = emptyMap()
) {
    fun getTimestamp(): Long {
        return when (date) {
            is Number -> date.toLong()
            is String -> date.toLongOrNull() ?: System.currentTimeMillis()
            else -> System.currentTimeMillis()
        }
    }

    fun getLocalizedTitle(languageCode: String): String {
        return (translations[languageCode]?.title ?: title).replace("\\n", "\n")
    }

    fun getLocalizedSummary(languageCode: String): String {
        return (translations[languageCode]?.summary ?: summary).replace("\\n", "\n")
    }

    fun getLocalizedContent(languageCode: String): String {
        return (translations[languageCode]?.content ?: content).replace("\\n", "\n")
    }
}

data class NewsTranslation(
    val title: String = "",
    val summary: String = "",
    val content: String = ""
)

enum class NewsType {
    UPDATE, // Patch notes
    ANNOUNCEMENT, // General news
    COMMUNITY // Community spotlight
}
