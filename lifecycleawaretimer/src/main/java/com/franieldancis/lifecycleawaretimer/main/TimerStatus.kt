package com.franieldancis.lifecycleawaretimer.main

import android.content.SharedPreferences
import com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer.Companion.PREFS_DAYS_KEY
import com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer.Companion.PREFS_HOURS_KEY
import com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer.Companion.PREFS_MINUTES_KEY
import com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer.Companion.PREFS_SECONDS_KEY
import javax.inject.Inject

/**
 * Class for getting and setting the status of a LifecycleAwareTimer's time values.
 * @see com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer
 * */
internal class TimerStatus @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    internal fun isTimerOut(prefsKey: String = ""): Boolean {
        sharedPreferences.run {
            val days = getLong(PREFS_DAYS_KEY + prefsKey, 0)
            val hours = getLong(PREFS_HOURS_KEY + prefsKey, 0)
            val minutes = getLong(PREFS_MINUTES_KEY + prefsKey, 0)
            val seconds = getLong(PREFS_SECONDS_KEY + prefsKey, 0)

            return days <= 0 && hours <= 0 && minutes <= 0 && seconds <= 0
        }
    }

    internal fun setTimerDays(days: Long, prefsKey: String = "", forceSet: Boolean = false) {
        // If preference already exists
        val containsPref = sharedPreferences.contains(PREFS_DAYS_KEY + prefsKey)

        if (forceSet || !containsPref) {
            sharedPreferences.edit()
                .putLong(PREFS_DAYS_KEY + prefsKey, days)
                .apply()
        }
    }

    internal fun setTimerHours(hours: Long, prefsKey: String = "", forceSet: Boolean = false) {
        // If preference already exists
        val containsPref = sharedPreferences.contains(PREFS_HOURS_KEY + prefsKey)

        if (forceSet || !containsPref) {
            sharedPreferences.edit()
                .putLong(PREFS_HOURS_KEY + prefsKey, hours)
                .apply()
        }
    }

    internal fun setTimerMinutes(minutes: Long, prefsKey: String = "", forceSet: Boolean = false) {
        // If preference already exists
        val containsPref = sharedPreferences.contains(PREFS_MINUTES_KEY + prefsKey)

        if (forceSet || !containsPref) {
            sharedPreferences.edit()
                .putLong(PREFS_MINUTES_KEY + prefsKey, minutes)
                .apply()
        }
    }

    internal fun setTimerSeconds(seconds: Long, prefsKey: String = "", forceSet: Boolean = false) {
        // If preference already exists
        val containsPref = sharedPreferences.contains(PREFS_SECONDS_KEY + prefsKey)

        if (forceSet || !containsPref) {
            sharedPreferences.edit()
                // Offset value of seconds being set by one to prevent timer going down immediately after new seconds value is set
                .putLong(PREFS_SECONDS_KEY + prefsKey, seconds + 1)
                .apply()
        }
    }
}