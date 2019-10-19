package com.franieldancis.lifecycleawaretimer.main

import android.content.SharedPreferences
import com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer.Companion.PREFS_MILLISECONDS_KEY
import java.util.concurrent.TimeUnit

/**
 * Class for getting and setting the status of a LifecycleAwareTimer's time values.
 * @see com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer
 * */
internal class TimerStatus constructor(
    private val sharedPreferences: SharedPreferences
) {
    internal fun isTimerOut(prefsKey: String = ""): Boolean {
        sharedPreferences.run {
            val milliseconds = getLong(PREFS_MILLISECONDS_KEY + prefsKey, 0)
            return milliseconds <= 0
        }
    }

    internal fun setTimerMilliseconds(milliseconds: Long, prefsKey: String = "", forceSet: Boolean = false) {
        // If preference already exists
        val containsPref = sharedPreferences.contains(PREFS_MILLISECONDS_KEY + prefsKey)

        if (forceSet || !containsPref) {
            sharedPreferences.edit()
                .putLong(PREFS_MILLISECONDS_KEY + prefsKey, milliseconds)
                .apply()
        }
    }

    internal fun getCurrentTime(timeUnit: TimeUnit, prefsKey: String = ""): Long {
        val milliseconds: Long = sharedPreferences.getLong(PREFS_MILLISECONDS_KEY + prefsKey, 0)
        return when (timeUnit) {
            TimeUnit.MILLISECONDS -> milliseconds
            TimeUnit.SECONDS -> TimeUnit.MILLISECONDS.toSeconds(milliseconds)
            TimeUnit.MINUTES -> TimeUnit.MILLISECONDS.toMinutes(milliseconds)
            TimeUnit.HOURS -> TimeUnit.MILLISECONDS.toHours(milliseconds)
            TimeUnit.DAYS -> TimeUnit.MILLISECONDS.toDays(milliseconds)
            else -> throw UnsupportedOperationException("TimeUnit $timeUnit not supported")
        }
    }
}