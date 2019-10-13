package com.franieldancis.lifecycleawaretimer.main

import android.content.Context
import android.content.SharedPreferences
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.*
import com.franieldancis.lifecycleawaretimer.di.DaggerLibComponent
import com.franieldancis.lifecycleawaretimer.di.LibComponent
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

/**
 * Implementation of a android.os.CountDownTimer that only runs when its androidx.lifecycle.Lifecycle is resumed.
 *
 * @implNote - Initiate the library using the companion object's init() function with your application's context.
 * @constructor - Constructor is made private to prevent direct instantiation. Use getInstance() function to obtain
 * instance of LifecycleAwareTimer.
 *
 * @see com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer.Companion.init
 * @see com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer.Companion.getInstance
 * */
class LifecycleAwareTimer @AssistedInject internal constructor(
    @Assisted val lifecycle: Lifecycle,
    @Assisted val prefsKey: String = "",
    private val sharedPreferences: SharedPreferences,
    private val timerStatus: TimerStatus
) : LifecycleObserver {
    // Second emissions
    private val _seconds = MutableLiveData<Long>()
    val seconds = _seconds as LiveData<Long>

    // Minute emissions
    private val _minutes = MutableLiveData<Long>()
    val minutes = _minutes as LiveData<Long>

    // Hour emissions
    private val _hours = MutableLiveData<Long>()
    val hours = _hours as LiveData<Long>

    // Day emissions
    private val _days = MutableLiveData<Long>()
    val days = _days as LiveData<Long>

    // Emit true when timer has run out
    private val _hasTimerRunOut = MutableLiveData<Unit>()
    val hasTimerRunOut = _hasTimerRunOut as LiveData<Unit>

    // CountDownTimer to track time going down
    private lateinit var countdownTimer: CountDownTimer

    init {
        // Add this as an observer to its lifecycle
        lifecycle.addObserver(this)
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(lifecycle: Lifecycle, prefsKey: String? = null): LifecycleAwareTimer
    }

    // region Lifecycle events
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        // Fetch minute and second values from SharedPrefs
        val daysValue = sharedPreferences.getLong(PREFS_DAYS_KEY + prefsKey, DEFAULT_NUM_DAYS_TIMER)
        val hoursValue = sharedPreferences.getLong(PREFS_HOURS_KEY + prefsKey, DEFAULT_NUM_HOURS_TIMER)
        val minutesValue = sharedPreferences.getLong(PREFS_MINUTES_KEY + prefsKey, DEFAULT_NUM_MINUTES_TIMER)
        val secondsValue = sharedPreferences.getLong(PREFS_SECONDS_KEY + prefsKey, DEFAULT_NUM_SECONDS_TIMER)

        // Convert days, hours, and minutes to seconds
        val daysInSeconds = daysValue * SECONDS_IN_DAY
        val hoursInSeconds = hoursValue * SECONDS_IN_HOUR
        val minutesInSeconds = minutesValue * SECONDS_IN_MINUTE

        // Special case: emit "zero" values to LiveData when preference data is zero
        if (daysValue == 0L) _days.postValue(0)
        if (hoursValue == 0L) _hours.postValue(0)
        if (minutesValue == 0L) _minutes.postValue(0)
        if (secondsValue == 0L) _seconds.postValue(0)

        // Create and start CountDownTimer with length of combined number of minutes and seconds
        countdownTimer = createCountDownTimer(daysInSeconds + hoursInSeconds + minutesInSeconds + secondsValue)
        countdownTimer.start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        // Update SharedPreferences with current values
        sharedPreferences.edit()
            .putLong(PREFS_DAYS_KEY + prefsKey, _days.value ?: 0)
            .putLong(PREFS_HOURS_KEY + prefsKey, _hours.value ?: 0)
            .putLong(PREFS_MINUTES_KEY + prefsKey, _minutes.value ?: 0)
            .putLong(PREFS_SECONDS_KEY + prefsKey, _seconds.value?.plus(1) ?: 0) // Add 1 second to the value saved to offset time lost in pausing the timer
            .apply()
        countdownTimer.cancel()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        // Remove this as an observer from its lifecycle
        lifecycle.removeObserver(this)
    }
    // endregion

    // region Time setters
    /**
     * Set the number of days on the timer. @return true if set successfully.
     * @param numberOfDays - the number of days to set left on the timer
     * */
    fun setActiveDays(numberOfDays: Long): Boolean {
        return try {
            // Update days
            timerStatus.setTimerDays(numberOfDays, prefsKey, true)

            // Cancel and restart CountDownTimer
            cancelAndRestartTimer()

            true
        } catch (exception: Exception) {
            Log.e(this.javaClass.name, exception.toString())

            false
        }
    }

    /**
     * Set the number of hours left on the timer. @return true if set successfully.
     * @param numberOfHours - the number of hours to set left on the timer
     * */
    fun setActiveHours(numberOfHours: Long): Boolean {
        return try {
            // Set hours
            timerStatus.setTimerHours(numberOfHours, prefsKey, true)

            // Cancel and restart CountDownTimer
            cancelAndRestartTimer()

            true
        } catch (exception: Exception) {
            Log.e(this.javaClass.name, exception.toString())

            false
        }
    }

    /**
     * Set the number of minutes left on the timer. @return true if set successfully.
     * @param numberOfMinutes - the number of minutes to set left on the timer
     * */
    fun setActiveMinutes(numberOfMinutes: Long): Boolean {
        return try {
            // Set minutes
            timerStatus.setTimerMinutes(numberOfMinutes, prefsKey, true)

            // Cancel and restart CountDownTimer
            cancelAndRestartTimer()

            true
        } catch (exception: Exception) {
            Log.e(this.javaClass.name, exception.toString())

            false
        }
    }

    /**
     * Set the number of seconds left on the timer. @return true if set successfully.
     * @param numberOfSeconds - the number of seconds to set left on the timer
     * */
    fun setActiveSeconds(numberOfSeconds: Long): Boolean {
        return try {
            // Set seconds
            timerStatus.setTimerSeconds(numberOfSeconds, prefsKey, true)

            // Cancel and restart CountDownTimer
            cancelAndRestartTimer()

            true
        } catch (exception: Exception) {
            Log.e(this.javaClass.name, exception.toString())

            false
        }
    }
    // endregion

    private fun createCountDownTimer(overallSeconds: Long): CountDownTimer {
        return object : CountDownTimer(overallSeconds * MILLISECONDS_IN_SECOND, MILLISECONDS_IN_SECOND) {
            override fun onFinish() {
                // Update time stored
                sharedPreferences.edit()
                    .putLong(PREFS_DAYS_KEY + prefsKey, 0)
                    .putLong(PREFS_HOURS_KEY + prefsKey, 0)
                    .putLong(PREFS_MINUTES_KEY + prefsKey, 0)
                    .putLong(PREFS_SECONDS_KEY + prefsKey, 0)
                    .apply()

                // Emit that timer has run out
                _hasTimerRunOut.postValue(Unit)
            }

            override fun onTick(millisUntilFinished: Long) {
                // Calculate overall seconds left
                val overallSecondsLeft = millisUntilFinished / MILLISECONDS_IN_SECOND

                // Convert to seconds left in minute
                val secondsLeft = overallSecondsLeft % SECONDS_IN_MINUTE

                // Convert to minutes left
                val minutesLeft = overallSecondsLeft / SECONDS_IN_MINUTE % MINUTES_IN_HOUR

                // Convert to hours left
                val hoursLeft = overallSecondsLeft / SECONDS_IN_HOUR % HOURS_IN_DAY

                // Convert to days left
                val daysLeft = overallSecondsLeft / SECONDS_IN_DAY

                // Post the seconds value
                _seconds.postValue(secondsLeft)

                // Post values if they've changed from their current values
                if (_minutes.value != minutesLeft) {
                    _minutes.postValue(minutesLeft)

                    // Update minutes preference
                    timerStatus.setTimerMinutes(minutesLeft, prefsKey, true)
                }
                if (_hours.value != hoursLeft) {
                    _hours.postValue(hoursLeft)

                    // Update hours preference
                    timerStatus.setTimerHours(hoursLeft, prefsKey, true)
                }
                if (_days.value != daysLeft) {
                    _days.postValue(daysLeft)

                    // Update days preference
                    timerStatus.setTimerDays(daysLeft, prefsKey, true)
                }
            }
        }
    }

    private fun cancelAndRestartTimer() {
        // Cancel current CountDownTimer
        countdownTimer.cancel()

        // Go through onResume flow (to restart timer)
        onResume()
    }

    companion object {
        // Library component to be instantiated
        private lateinit var component: LibComponent

        /**
         * Function to initialize the library.
         * @param applicationContext - your application's context
         * */
        fun init(applicationContext: Context) {
            component = DaggerLibComponent.factory()
                .create(applicationContext)
        }

        /**
         * Get an instance of LifecycleAwareTimer.
         * @see com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer
         * @param lifecycle - the lifecycle object (e.g. from Activity or Fragment) the timer will observe
         * @param prefsKey - the key to save time preferences under (in order to track times for different keys)
         * */
        fun getInstance(lifecycle: Lifecycle, prefsKey: String = ""): LifecycleAwareTimer {
            return component.timerFactory.get().create(lifecycle, prefsKey)
        }

        /**
         * @return If the timer is out of time
         * @param prefsKey - The preference identifier for the timer to be checked
         * */
        fun isTimerOutOfTime(prefsKey: String = ""): Boolean {
            return component.timerStatus.get().isTimerOut(prefsKey)
        }

        /**
         * Set the number of days for a timer.
         * @param numDays - number of days to set left on the timer
         * @param prefsKey - the SharedPreference key to identify which timer it is for
         * @param forceSet - flag to forcefully set the value - true to set regardless of if the preference already exists, false to only set it when it doesn't already exist.
         * @implNote - Don't use this method to set the time on an active timer
         * */
        fun setDays(numDays: Long, prefsKey: String = "", forceSet: Boolean = false) {
            component.timerStatus.get().setTimerDays(numDays, prefsKey, forceSet)
        }

        /**
         * Set the number of hours for a timer.
         * @param numHours - number of hours to set left on the timer
         * @param prefsKey - the SharedPreference key to identify which timer it is for
         * @param forceSet - flag to forcefully set the value - true to set regardless of if the preference already exists, false to only set it when it doesn't already exist.
         * @implNote - Don't use this method to set the time on an active timer
         * */
        fun setHours(numHours: Long, prefsKey: String = "", forceSet: Boolean = false) {
            component.timerStatus.get().setTimerHours(numHours, prefsKey, forceSet)
        }

        /**
         * Set the number of minutes for a timer.
         * @param numMinutes - number of minutes to set left on the timer
         * @param prefsKey - the SharedPreference key to identify which timer it is for
         * @param forceSet - flag to forcefully set the value - true to set regardless of if the preference already exists, false to only set it when it doesn't already exist.
         * @implNote - Don't use this method to set the time on an active timer
         * */
        fun setMinutes(numMinutes: Long, prefsKey: String = "", forceSet: Boolean = false) {
            component.timerStatus.get().setTimerMinutes(numMinutes, prefsKey, forceSet)
        }

        /**
         * Set the number of seconds for a timer.
         * @param numSeconds - number of seconds to set left on the timer
         * @param prefsKey - the SharedPreference key to identify which timer it is for
         * @param forceSet - flag to forcefully set the value - true to set regardless of if the preference already exists, false to only set it when it doesn't already exist.
         * @implNote - Don't use this method to set the time on an active timer
         * */
        fun setSeconds(numSeconds: Long, prefsKey: String = "", forceSet: Boolean = false) {
            component.timerStatus.get().setTimerSeconds(numSeconds, prefsKey, forceSet)
        }

        // region SharedPreference Keys
        /**
         * SharedPreferences key for the number of days left
         * */
        internal const val PREFS_DAYS_KEY = "LIFECYCLE_TIMER_DAYS"

        /**
         * SharedPreferences key for the number of hours left
         * */
        internal const val PREFS_HOURS_KEY = "LIFECYCLE_TIMER_HOURS"

        /**
         * SharedPreferences key for the number of minutes left
         * */
        internal const val PREFS_MINUTES_KEY = "LIFECYCLE_TIMER_MINUTES"

        /**
         * SharedPreferences key for the number of seconds left
         * */
        internal const val PREFS_SECONDS_KEY = "LIFECYCLE_TIMER_SECONDS"
        // endregion

        // region Default initial timer values
        /**
         * Default number of days on timer if days preference is not set.
         * */
        internal const val DEFAULT_NUM_DAYS_TIMER = 0L

        /**
         * Default number of hours on timer if hours preference is not set
         * */
        internal const val DEFAULT_NUM_HOURS_TIMER = 0L

        /**
         * Default number of minutes on timer if minutes preference is not set.
         * */
        internal const val DEFAULT_NUM_MINUTES_TIMER = 0L

        /**
         * Default number of seconds (not including minutes) on timer if seconds preference is not set
         * */
        internal const val DEFAULT_NUM_SECONDS_TIMER = 0L
        // endregion

        // region time constants
        private const val SECONDS_IN_DAY = 864_000L
        private const val SECONDS_IN_HOUR = 3_600L
        private const val SECONDS_IN_MINUTE = 60L
        private const val MINUTES_IN_HOUR = 60L
        private const val HOURS_IN_DAY = 24L
        private const val MILLISECONDS_IN_SECOND = 1_000L
        // endregion
    }
}