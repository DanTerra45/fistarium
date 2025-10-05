package wiki.tk.fistarium.features.characters.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import wiki.tk.fistarium.features.characters.data.local.dao.CharacterDao
import wiki.tk.fistarium.features.characters.data.local.entity.CharacterEntity

@Database(
    entities = [CharacterEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
}