package com.example.landnv4

import android.app.Application
import androidx.room.Room
import com.example.landnv4.data.db.AppDatabase

class App : Application() {
    companion object {
        lateinit var db: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDatabase::class.java, "landnv4.db")
            .build()
    }
}
