package com.franieldancis.lifecycleawaretimer.di

import android.app.Application
import com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer

class TimerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initiate library
        LifecycleAwareTimer.init(this)
    }
}