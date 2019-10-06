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
    // Seconds emissions
    private val _seconds = MutableLiveData<Long>()
    val seconds = _seconds as LiveData<Long>

    // Minutes emissions
    private val _minutes = MutableLiveData<Long>()
    val minutes = _minutes as LiveData<Long>

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
        val minutesValue = sharedPreferences.getLong(PREFS_MINUTES_KEY, DEFAULT_NUM_MINUTES_TIMER)
        val secondsValue = sharedPreferences.getLong(PREFS_SECONDS_KEY, DEFAULT_NUM_SECONDS_TIMER)

        // Convert minutes to seconds
        val minutesInSeconds = minutesValue * 60

        // Create and start CountDownTimer with length of combined number of minutes and seconds
        countdownTimer = createCountDownTimer(minutesInSeconds + secondsValue)
        countdownTimer.start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        // Update SharedPreferences with current values
        sharedPreferences.edit()
            .putLong(PREFS_MINUTES_KEY, _minutes.value ?: 0)
            .putLong(PREFS_SECONDS_KEY, _seconds.value ?: 0)
            .apply()
        countdownTimer.cancel()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        // Remove this as an observer from its lifecycle
        lifecycle.removeObserver(this)
    }

    fun setMinutes(numberOfMinutesToAdd: Long): Boolean {
        try {
            timerStatus.setTimerMinutes(numberOfMinutesToAdd)

            // Cancel current CountDownTimer
            countdownTimer.cancel()

            // Go through onResume flow (to restart timer)
            onResume()

            return true
        } catch (exception: Exception) {
            Log.e(this.javaClass.name, exception.toString())

            return false
        }
    }

    private fun createCountDownTimer(overallSeconds: Long): CountDownTimer {
        return object : CountDownTimer(overallSeconds * 1000, 1000) {
            override fun onFinish() {
                // Update time stored
                sharedPreferences.edit()
                    .putLong(PREFS_MINUTES_KEY, 0)
                    .putLong(PREFS_SECONDS_KEY, 0)
                    .apply()

                // Emit that timer has run out
                _hasTimerRunOut.postValue(true)
            }

            override fun onTick(millisUntilFinished: Long) {
                // Calculate overall seconds left
                val overallSecondsLeft = millisUntilFinished / 1000

                // Convert to seconds left in minute
                val secondsLeft = overallSecondsLeft % 60

                // Convert to minutes left
                val minutesLeft = overallSecondsLeft / 60

                // Post the seconds value
                _seconds.postValue(secondsLeft)

                // Post the minutes value if it's changed from the current minutes value
                if (_minutes.value != minutesLeft) {
                    _minutes.postValue(minutesLeft)
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
         * SharedPreferences key for the number of minutes left
         * */
        internal const val PREFS_MINUTES_KEY = "LIFECYCLE_TIMER_MINUTES"

        /**
         * SharedPreferences key for the number of seconds left
         * */
        internal const val PREFS_SECONDS_KEY = "LIFECYCLE_TIMER_SECONDS"

        /**
         * Default number of minutes on timer if minutes preference is not set.
         * */
        internal const val DEFAULT_NUM_MINUTES_TIMER = 5L

        /**
         * Default number of seconds (not including minutes) on timer if seconds preference is not set
         * */
        internal const val DEFAULT_NUM_SECONDS_TIMER = 0L
    }
}