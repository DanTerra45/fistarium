package wiki.tk.fistarium.domain.model

data class Character(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String? = null,
    val stats: Map<String, Int> = emptyMap() // e.g., "health" to 100, "attack" to 80, etc.
)