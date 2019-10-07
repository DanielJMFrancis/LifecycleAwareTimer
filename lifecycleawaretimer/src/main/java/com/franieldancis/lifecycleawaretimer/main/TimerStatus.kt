package com.franieldancis.lifecycleawaretimer.main

import android.content.SharedPreferences
import com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer.Companion.PREFS_DAYS_KEY
import com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer.Companion.PREFS_HOURS_KEY
import com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer.Companion.PREFS_MINUTES_KEY
import com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer.Companion.PREFS_SECONDS_KEY
import javax.inject.Inject

class TimerStatus @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    internal fun isTimerOut(): Boolean {
        sharedPreferences.run {
            val days = getLong(PREFS_DAYS_KEY, 0)
            val hours = getLong(PREFS_HOURS_KEY, 0)
            val minutes = getLong(PREFS_MINUTES_KEY, 0)
            val seconds = getLong(PREFS_SECONDS_KEY, 0)

            return days <= 0 && hours <= 0 && minutes <= 0 && seconds <= 0
        }
    }

    internal fun setTimerMinutes(minutes: Long) {
        sharedPreferences.edit()
            .putLong(PREFS_MINUTES_KEY, minutes)
            .apply()
    }

    internal fun setTimerSeconds(seconds: Long) {
        sharedPreferences.edit()
            .putLong(PREFS_SECONDS_KEY, seconds)
            .apply()
    }

    // TODO: Add (public) setters for days and hours
}