package com.example.landnv4

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.room.Room
import com.example.landnv4.data.db.AppDatabase
import com.example.landnv4.data.inputs.AppInputsStore

class App : Application(), DefaultLifecycleObserver {
    companion object {
        lateinit var db: AppDatabase
            private set
    }

    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        db = AppDatabase.getInstance(this)

    }

    override fun onStop(owner: LifecycleOwner) {
        // App went to background (effectively "closed" for our purposes)
        AppInputsStore.setClosedAt(this, System.currentTimeMillis())
    }
}
