package kr.stonecold.myheritage.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        IndividualEntity::class,
        FamilyEntity::class,
        FactEntity::class,
        NoteEntity::class,
        ResourceEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class MyHeritageDatabase : RoomDatabase() {

    abstract fun individualDao(): IndividualDao
    abstract fun familyDao(): FamilyDao
    abstract fun factDao(): FactDao
    abstract fun noteDao(): NoteDao
    abstract fun resourceDao(): ResourceDao

    companion object {
        @Volatile
        private var INSTANCE: MyHeritageDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE facts ADD COLUMN show_in_list INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getDatabase(context: Context): MyHeritageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyHeritageDatabase::class.java,
                    "myHeritage.db"
                )
                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun resetDatabase() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}
