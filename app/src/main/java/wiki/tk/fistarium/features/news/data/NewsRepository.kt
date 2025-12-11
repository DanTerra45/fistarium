package wiki.tk.fistarium.features.news.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import wiki.tk.fistarium.features.news.domain.NewsArticle
import wiki.tk.fistarium.features.news.domain.NewsType

interface NewsRepository {
    suspend fun getNewsArticles(): Result<List<NewsArticle>>
    suspend fun getLatestPatchNote(): Result<NewsArticle?>
}

class NewsRepositoryImpl(
    private val firestore: FirebaseFirestore
) : NewsRepository {

    override suspend fun getNewsArticles(): Result<List<NewsArticle>> {
        return try {
            val snapshot = firestore.collection("news")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val articles = snapshot.documents.mapNotNull { doc ->
                doc.toObject(NewsArticle::class.java)?.copy(id = doc.id)
            }
            Result.success(articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLatestPatchNote(): Result<NewsArticle?> {
        return try {
            val snapshot = firestore.collection("news")
                .whereEqualTo("type", NewsType.UPDATE.name)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            
            val article = snapshot.documents.firstOrNull()?.toObject(NewsArticle::class.java)?.copy(id = snapshot.documents.first().id)
            Result.success(article)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
