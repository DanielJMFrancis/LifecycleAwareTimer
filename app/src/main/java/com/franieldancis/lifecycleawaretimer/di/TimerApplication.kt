package com.franieldancis.lifecycleawaretimer.di

import android.app.Application
import com.franieldancis.testlib.main.LifecycleAwareTimer

class TimerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        LifecycleAwareTimer.init(this)
    }
}