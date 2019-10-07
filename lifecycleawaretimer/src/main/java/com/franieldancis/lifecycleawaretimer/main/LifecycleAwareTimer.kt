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

class LifecycleAwareTimer @AssistedInject constructor(
    @Assisted val lifecycle: Lifecycle,
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
    private val _hasTimerRunOut = MutableLiveData<Boolean>()
    val hasTimerRunOut = _hasTimerRunOut as LiveData<Boolean>

    // CountDownTimer to track time going down
    private lateinit var countdownTimer: CountDownTimer

    init {
        // Add this as an observer to its lifecycle
        lifecycle.addObserver(this)
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(lifecycle: Lifecycle): LifecycleAwareTimer
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        // Fetch minute and second values from SharedPrefs
        val daysValue = sharedPreferences.getLong(PREFS_DAYS_KEY, DEFAULT_NUM_DAYS_TIMER)
        val hoursValue = sharedPreferences.getLong(PREFS_HOURS_KEY, DEFAULT_NUM_HOURS_TIMER)
        val minutesValue = sharedPreferences.getLong(PREFS_MINUTES_KEY, DEFAULT_NUM_MINUTES_TIMER)
        val secondsValue = sharedPreferences.getLong(PREFS_SECONDS_KEY, DEFAULT_NUM_SECONDS_TIMER)

        // Convert days, hours, and minutes to seconds
        val daysInSeconds = daysValue * SECONDS_IN_DAY
        val hoursInSeconds = hoursValue * SECONDS_IN_HOUR
        val minutesInSeconds = minutesValue * SECONDS_IN_MINUTE

        // Create and start CountDownTimer with length of combined number of minutes and seconds
        countdownTimer = createCountDownTimer(daysInSeconds + hoursInSeconds + minutesInSeconds + secondsValue)
        countdownTimer.start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        // Update SharedPreferences with current values
        sharedPreferences.edit()
            .putLong(PREFS_DAYS_KEY, _days.value ?: 0)
            .putLong(PREFS_HOURS_KEY, _hours.value ?: 0)
            .putLong(PREFS_MINUTES_KEY, _minutes.value ?: 0)
            .putLong(PREFS_SECONDS_KEY, _seconds.value?.plus(1) ?: 0)
            .apply()
        countdownTimer.cancel()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        // Remove this as an observer from its lifecycle
        lifecycle.removeObserver(this)
    }

    // TODO: Add (public) setters for days, hours, & minutes
    fun setMinutes(numberOfMinutesToAdd: Long): Boolean {
        return try {
            timerStatus.setTimerMinutes(numberOfMinutesToAdd)

            // Cancel current CountDownTimer
            countdownTimer.cancel()

            // Go through onResume flow (to restart timer)
            onResume()

            true
        } catch (exception: Exception) {
            Log.e(this.javaClass.name, exception.toString())

            false
        }
    }

    private fun createCountDownTimer(overallSeconds: Long): CountDownTimer {
        return object : CountDownTimer(overallSeconds * MILLISECONDS_IN_SECOND, MILLISECONDS_IN_SECOND) {
            override fun onFinish() {
                // Update time stored
                sharedPreferences.edit()
                    .putLong(PREFS_DAYS_KEY, 0)
                    .putLong(PREFS_HOURS_KEY, 0)
                    .putLong(PREFS_MINUTES_KEY, 0)
                    .putLong(PREFS_SECONDS_KEY, 0)
                    .apply()

                // Emit that timer has run out
                _hasTimerRunOut.postValue(true)
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
                }
                if (_hours.value != hoursLeft) {
                    _hours.postValue(hoursLeft)
                }
                if (_days.value != daysLeft) {
                    _days.postValue(daysLeft)
                }
            }
        }
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
         * */
        fun getInstance(lifecycle: Lifecycle): LifecycleAwareTimer {
            return component.timerFactory.get().create(lifecycle)
        }

        /**
         * @return If the timer is out of time
         * */
        fun isTimerOutOfTime(): Boolean {
            return component.timerStatus.get().isTimerOut()
        }

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
        internal const val DEFAULT_NUM_MINUTES_TIMER = 5L

        /**
         * Default number of seconds (not including minutes) on timer if seconds preference is not set
         * */
        internal const val DEFAULT_NUM_SECONDS_TIMER = 10L

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