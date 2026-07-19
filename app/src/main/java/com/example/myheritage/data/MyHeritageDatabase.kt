package com.example.myheritage.data

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
                    "myHeritage.db" // 기존 DB 명칭 그대로 사용
                )
                // 기존 SQLite DB 구조와 호환시키기 위해 fallbackToDestructiveMigration을 쓰지 않고
                // 마이그레이션이 정의되지 않았을 때의 처리를 하거나, 수동 마이그레이션을 구성해야 함.
                // 여기서는 우선 호환 가능하도록 구성.
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
