package com.franieldancis.lifecycleawaretimer.main

import android.content.SharedPreferences
import com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer.Companion.PREFS_DAYS_KEY
import com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer.Companion.PREFS_HOURS_KEY
import com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer.Companion.PREFS_MINUTES_KEY
import com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer.Companion.PREFS_SECONDS_KEY
import javax.inject.Inject

internal class TimerStatus @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    internal fun isTimerOut(prefsKey: String? = null): Boolean {
        sharedPreferences.run {
            val days = getLong(PREFS_DAYS_KEY + prefsKey, 0)
            val hours = getLong(PREFS_HOURS_KEY + prefsKey, 0)
            val minutes = getLong(PREFS_MINUTES_KEY + prefsKey, 0)
            val seconds = getLong(PREFS_SECONDS_KEY + prefsKey, 0)

            return days <= 0 && hours <= 0 && minutes <= 0 && seconds <= 0
        }
    }

    internal fun setTimerDays(days: Long, prefsKey: String? = null) {
        sharedPreferences.edit()
            .putLong(PREFS_DAYS_KEY + prefsKey, days)
            .apply()
    }

    internal fun setTimerHours(hours: Long, prefsKey: String? = null) {
        sharedPreferences.edit()
            .putLong(PREFS_HOURS_KEY + prefsKey, hours)
            .apply()
    }

    internal fun setTimerMinutes(minutes: Long, prefsKey: String? = null) {
        sharedPreferences.edit()
            .putLong(PREFS_MINUTES_KEY + prefsKey, minutes)
            .apply()
    }

    internal fun setTimerSeconds(seconds: Long, prefsKey: String? = null) {
        sharedPreferences.edit()
            .putLong(PREFS_SECONDS_KEY + prefsKey, seconds)
            .apply()
    }
}