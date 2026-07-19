package kr.stonecold.myheritage

import android.app.Application
import kr.stonecold.myheritage.data.DataRepository
import kr.stonecold.myheritage.data.DefaultDataRepository

class MyHeritageApplication : Application() {
    lateinit var repository: DataRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = DefaultDataRepository(this)
    }
}
