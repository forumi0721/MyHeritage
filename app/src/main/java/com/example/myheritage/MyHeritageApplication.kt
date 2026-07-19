package com.example.myheritage

import android.app.Application
import com.example.myheritage.data.DataRepository
import com.example.myheritage.data.DefaultDataRepository

class MyHeritageApplication : Application() {
    lateinit var repository: DataRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = DefaultDataRepository(this)
    }
}
