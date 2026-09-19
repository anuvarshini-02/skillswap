package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.SkillSwapRepository

class SkillSwapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.getDatabase(this)
        SkillSwapRepository.initialize(this)
    }
}
