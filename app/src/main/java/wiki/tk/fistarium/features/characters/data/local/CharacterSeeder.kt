package wiki.tk.fistarium.features.characters.data.local

import wiki.tk.fistarium.features.characters.domain.*

class CharacterSeeder {

    fun getInitialCharacters(): List<Character> {
        val currentTime = System.currentTimeMillis()
        
        return listOf(
            Character(
                id = "kazuya",
                name = "Kazuya Mishima",
                description = "The ruthless leader of the Mishima Zaibatsu, known for his devil gene. After falling into a volcano and being revived, Kazuya seeks revenge and world domination.",
                imageUrl = "https://www.tekken-official.jp/8/assets/img/character/kazuya/main_ss.jpg",
                imageUrls = emptyList(),
                stats = mapOf("power" to 85, "speed" to 75, "technique" to 90, "reach" to 70),
                fightingStyle = "Mishima Style Fighting Karate",
                country = "Japan",
                difficulty = "Hard",
                moveList = listOf(
                    Move("ewgf", "Electric Wind God Fist", "f,n,d,df+2", "28", "Mid", "Just-frame execution required"),
                    Move("hellsweep", "Hell Sweep", "f,n,d,df+4,4", "12+12", "Low", "Strong low option"),
                    Move("twin_pistons", "Twin Pistons", "1,1,2", "10+10+20", "High", "10-frame punisher")
                ),
                combos = listOf(
                    Combo("basic_launcher", "Basic Launcher Combo", "df+2, f,n,d,df+1, S!, dash, b+2,1 -> B!, dash, 1,2,2", "~65", "Medium", "Launcher", null, null)
                ),
                frameData = mapOf(
                    "jab" to FrameDataEntry(10, 0, 8, 8, "Standard jab"),
                    "df+2" to FrameDataEntry(15, -12, null, null, "Launcher on counter hit")
                ),
                translations = emptyMap(),
                createdBy = null,
                createdAt = currentTime,
                updatedBy = null,
                updatedAt = currentTime,
                isOfficial = true,
                version = 1,
                isFavorite = false
            ),
            Character(
                id = "jin",
                name = "Jin Kazama",
                description = "Son of Kazuya and Jun, wielder of the Devil Gene. Jin seeks to end the Mishima bloodline curse and destroy the devil gene forever.",
                imageUrl = "https://www.tekken-official.jp/8/assets/img/character/jin/main_ss.jpg",
                imageUrls = emptyList(),
                stats = mapOf("power" to 90, "speed" to 80, "technique" to 85, "reach" to 75),
                fightingStyle = "Traditional Karate",
                country = "Japan",
                difficulty = "Medium",
                moveList = listOf(
                    Move("ewhf", "Electric Wind Hook Fist", "f,n,d,df+1", "25", "Mid", "Mishima just-frame"),
                    Move("parry", "Parry", "b+1+3 or b+2+4", "0", "Special", "Auto counter on success"),
                    Move("zen_stance", "Zen Stance", "1+2", "0", "Special", "Stance transition")
                ),
                combos = listOf(
                    Combo("zen_combo", "Zen Stance Combo", "1+2, 1, S!, dash, 1,3~3, f+4", "~60", "Medium", "Stance", null, null)
                ),
                frameData = emptyMap(),
                translations = emptyMap(),
                createdBy = null,
                createdAt = currentTime,
                updatedBy = null,
                updatedAt = currentTime,
                isOfficial = true,
                version = 1,
                isFavorite = false
            ),
            Character(
                id = "king",
                name = "King",
                description = "The noble wrestler masked as a jaguar, fighting for justice and to support orphans. Master of professional wrestling and grappling techniques.",
                imageUrl = "https://www.tekken-official.jp/8/assets/img/character/king/main_ss.jpg",
                imageUrls = emptyList(),
                stats = mapOf("power" to 80, "speed" to 70, "technique" to 95, "reach" to 75),
                fightingStyle = "Pro Wrestling",
                country = "Mexico",
                difficulty = "Medium",
                moveList = listOf(
                    Move("giant_swing", "Giant Swing", "f,hcf+1", "45", "Throw", "Iconic throw move"),
                    Move("ali_kicks", "Ali Kicks", "3~3,4,3,4,3,4", "Varies", "High/Mid", "Requires rhythm"),
                    Move("shining_wizard", "Shining Wizard", "FC df+4,3,4", "30", "Low/Mid", "Can wallsplat")
                ),
                combos = listOf(
                    Combo("chain_throw", "Chain Throw", "From Giant Swing -> chain grabs", "~80", "Hard", "Throw", null, "Requires multiple inputs")
                ),
                frameData = emptyMap(),
                translations = emptyMap(),
                createdBy = null,
                createdAt = currentTime,
                updatedBy = null,
                updatedAt = currentTime,
                isOfficial = true,
                version = 1,
                isFavorite = false
            ),
            Character(
                id = "nina",
                name = "Nina Williams",
                description = "Cold-blooded Irish assassin and master of Aikido. Known for her deadly efficiency and complex relationships with her sister Anna.",
                imageUrl = "https://www.tekken-official.jp/8/assets/img/character/nina/main_ss.jpg",
                imageUrls = emptyList(),
                stats = mapOf("power" to 75, "speed" to 90, "technique" to 85, "reach" to 70),
                fightingStyle = "Aikido & Koppojutsu",
                country = "Ireland",
                difficulty = "Hard",
                moveList = emptyList(),
                combos = emptyList(),
                frameData = emptyMap(),
                translations = emptyMap(),
                createdBy = null,
                createdAt = currentTime,
                updatedBy = null,
                updatedAt = currentTime,
                isOfficial = true,
                version = 1,
                isFavorite = false
            ),
            Character(
                id = "paul",
                name = "Paul Phoenix",
                description = "Hot-blooded American martial artist with a signature flat-top hairdo. Dreams of becoming the toughest fighter in the universe.",
                imageUrl = "https://www.tekken-official.jp/8/assets/img/character/paul/main_ss.jpg",
                imageUrls = emptyList(),
                stats = mapOf("power" to 95, "speed" to 70, "technique" to 75, "reach" to 75),
                fightingStyle = "Judo",
                country = "USA",
                difficulty = "Easy",
                moveList = listOf(
                    Move("deathfist", "Deathfist", "qcf+2", "55", "Mid", "Iconic move, very high damage"),
                    Move("demo_man", "Demolition Man", "qcb+4,2", "20+27", "Mid", "Wall bounce on hit")
                ),
                combos = emptyList(),
                frameData = emptyMap(),
                translations = emptyMap(),
                createdBy = null,
                createdAt = currentTime,
                updatedBy = null,
                updatedAt = currentTime,
                isOfficial = true,
                version = 1,
                isFavorite = false
            )
        )
    }
}