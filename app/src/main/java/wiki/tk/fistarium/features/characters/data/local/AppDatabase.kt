package wiki.tk.fistarium.features.characters.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import wiki.tk.fistarium.features.characters.data.local.dao.CharacterDao
import wiki.tk.fistarium.features.characters.data.local.entity.CharacterEntity

@Database(
    entities = [CharacterEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE characters ADD COLUMN imageUrlsJson TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE characters ADD COLUMN fightingStyle TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE characters ADD COLUMN country TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE characters ADD COLUMN difficulty TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE characters ADD COLUMN moveListJson TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE characters ADD COLUMN combosJson TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE characters ADD COLUMN frameDataJson TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE characters ADD COLUMN translationsJson TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE characters ADD COLUMN createdBy TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE characters ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE characters ADD COLUMN updatedBy TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE characters ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE characters ADD COLUMN isOfficial INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE characters ADD COLUMN version INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE characters ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}