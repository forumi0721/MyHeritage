package kr.stonecold.myheritage.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        IndividualEntity::class,
        FamilyEntity::class,
        FactEntity::class,
        NoteEntity::class,
        ResourceEntity::class
    ],
    version = 3, // 기존 DB 버전이 3이었으므로 동일하게 3으로 매칭
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

        fun getDatabase(context: Context): MyHeritageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyHeritageDatabase::class.java,
                    "myHeritage.db"
                )
                .fallbackToDestructiveMigrationOnDowngrade()
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
