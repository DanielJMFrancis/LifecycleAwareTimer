package com.franieldancis.lifecycleawaretimer.main

import android.content.Context
import android.content.SharedPreferences
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.*
import java.util.concurrent.TimeUnit

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
class LifecycleAwareTimer internal constructor(
    val lifecycle: Lifecycle,
    val prefsKey: String = ""
) : LifecycleObserver {
    // Millisecond emissions
    private val _milliseconds = MutableLiveData<Long>()
    val milliseconds = _milliseconds as LiveData<Long>

    // Emit true when timer has run out
    private val _onFinish = MutableLiveData<Unit>()
    val onFinish = _onFinish as LiveData<Unit>

    // CountDownTimer to track time going down
    private lateinit var countdownTimer: CountDownTimer

    init {
        // Add this as an observer to its lifecycle
        lifecycle.addObserver(this)
    }

    // region Lifecycle events
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        checkInitialized()

        // Fetch millisecond values from sharedPreferences
        val millisecondsValue =
            sharedPreferences.getLong(PREFS_MILLISECONDS_KEY + prefsKey, DEFAULT_NUM_MILLISECONDS_TIMER)

        // Special case: emit "zero" value to LiveData when preference data is zero
        if (millisecondsValue <= 0L) {
            _milliseconds.postValue(0)
            return
        }

        // Create and start CountDownTimer only when milliseconds are greater than 0
        countdownTimer = createCountDownTimer(millisecondsValue)
        countdownTimer.start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        checkInitialized()

        // Update timer's stored current milliseconds
        milliseconds.value?.let {
                currentMillis -> timerStatus.setTimerMilliseconds(currentMillis, prefsKey, true)
        } ?: Log.e(javaClass.name, "Error saving current milliseconds to preferences - milliseconds value is null")

        //  Cancel timer if it has been initialized
        if (::countdownTimer.isInitialized) countdownTimer.cancel()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        // Remove this as an observer from its lifecycle
        lifecycle.removeObserver(this)
    }
    // endregion

    // region Time setter
    /**
     * Set the time on an actively running timer, adding up all time values passed in to the function.
     * @param milliseconds
     * @param seconds
     * @param minutes
     * @param hours
     * @param days
     * */
    fun setTimeLeftOnActiveTimer(
        milliseconds: Long = 0,
        seconds: Long = 0,
        minutes: Long = 0,
        hours: Long = 0,
        days: Long = 0
    ) {
        checkInitialized()

        timerStatus.setTimerMilliseconds(milliseconds
            .plus(TimeUnit.SECONDS.toMillis(seconds))
            .plus(TimeUnit.MINUTES.toMillis(minutes))
            .plus(TimeUnit.HOURS.toMillis(hours))
            .plus(TimeUnit.DAYS.toMillis(days)),
            prefsKey = prefsKey,
            forceSet = true)

        // Cancel restart the CountDownTimer
        cancelAndRestartTimer()
    }

    /**
     * Manual call to save the timer's current milliseconds
     * (in any case your lifecycle owner's onPause would not be called before you'd like to access
     * the timer elsewhere).
     * */
    fun saveCurrentTime() {
        checkInitialized()

        // Update timer's stored current milliseconds
        milliseconds.value?.let {
                currentMillis -> timerStatus.setTimerMilliseconds(currentMillis, prefsKey, true)
        } ?: Log.e(javaClass.name, "Error saving current milliseconds to preferences - milliseconds value is null")
    }
    // endregion

    private fun createCountDownTimer(overallMilliseconds: Long): CountDownTimer {
        return object :
            CountDownTimer(overallMilliseconds, MILLISECONDS_IN_SECOND) {
            override fun onFinish() {
                // Update time stored
                sharedPreferences.edit()
                    .putLong(PREFS_MILLISECONDS_KEY + prefsKey, 0)
                    .apply()

                // Post zero to milliseconds
                _milliseconds.postValue(0)

                // Emit that timer has run out
                _onFinish.postValue(Unit)
            }

            override fun onTick(millisUntilFinished: Long) {
                // Post the seconds value
                _milliseconds.postValue(millisUntilFinished)
            }
        }
    }

    private fun cancelAndRestartTimer() {
        // Cancel current CountDownTimer
        if (::countdownTimer.isInitialized) {
            countdownTimer.cancel()
        }

        // Go through onResume flow (to restart timer)
        onResume()
    }

    companion object {
        // SharedPreference dependency
        private lateinit var sharedPreferences: SharedPreferences
        private lateinit var timerStatus: TimerStatus

        // Library initialization flag
        private var isInitialized = false

        /**
         * Function to initialize the library.
         * @param applicationContext - your application's context
         * */
        fun init(applicationContext: Context) {
            // Set up SharedPreferences
            sharedPreferences = applicationContext.getSharedPreferences(
                SHARED_PREFERENCES_TIMER,
                Context.MODE_PRIVATE
            )

            // Set up TimerStatus
            timerStatus = TimerStatus(sharedPreferences)

            // Update library initialization flag
            isInitialized = true
        }

        /**
         * Get an instance of LifecycleAwareTimer.
         * @see com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer
         * @param lifecycle - the lifecycle object (e.g. from Activity or Fragment) the timer will observe
         * @param prefsKey - the key to save time preferences under (in order to track times for different keys)
         * */
        fun getInstance(lifecycle: Lifecycle, prefsKey: String = ""): LifecycleAwareTimer {
            return LifecycleAwareTimer(lifecycle, prefsKey)
        }

        /**
         * @return If the timer is out of time
         * @param prefsKey - The preference identifier for the timer to be checked
         * */
        fun isTimerOutOfTime(prefsKey: String = ""): Boolean {
            checkInitialized()

            return timerStatus.isTimerOut(prefsKey)
        }

        /**
         * Set the time on a timer, adding up all time values passed in to the function.
         * @param prefsKey - string identifier for the timer
         * @param milliseconds
         * @param seconds
         * @param minutes
         * @param hours
         * @param days
         * @param forceSet - flag to forcefully set the value - true to set regardless of if the preference already exists, false to only set it when it doesn't already exist.
         * @implNote - Don't use this method to set the time on an active timer
         * */
        fun setTimeLeft(
            prefsKey: String = "",
            milliseconds: Long = 0,
            seconds: Long = 0,
            minutes: Long = 0,
            hours: Long = 0,
            days: Long = 0,
            forceSet: Boolean = false
        ) {
            checkInitialized()

            timerStatus.setTimerMilliseconds(milliseconds
                .plus(TimeUnit.SECONDS.toMillis(seconds))
                .plus(TimeUnit.MINUTES.toMillis(minutes))
                .plus(TimeUnit.HOURS.toMillis(hours))
                .plus(TimeUnit.DAYS.toMillis(days)),
                prefsKey = prefsKey,
                forceSet = forceSet)
        }

        /**
         * @return the time left on the current timer
         * @param timeUnit - the {@link java.util.concurrent.TimeUnit} to return. Supports time units from milliseconds to days.
         * @param prefsKey - string identifier for the timer
         * */
        fun getTimeLeft(
            timeUnit: TimeUnit,
            prefsKey: String = ""
        ): Long {
            checkInitialized()

            return timerStatus.getCurrentTime(timeUnit, prefsKey)
        }

        /**
         * Private function to check if the library has been initialized.
         * @throws UninitializedLibraryException when the library has not been initialized
         * */
        private fun checkInitialized() {
            if (!isInitialized)
                throw UninitializedLibraryException("Library has not been initialized. Call LifecycleAwareTimer.init(applicationContext) before using the library.")
        }

        // region SharedPreference Keys
        /**
         * File name for the library's SharedPreferences
         * @see android.content.SharedPreferences
         * */
        private const val SHARED_PREFERENCES_TIMER = "franiel_dancis_shared_prefs"

        /**
         * SharedPreferences key for the overall number of milliseconds left
         * */
        internal const val PREFS_MILLISECONDS_KEY = "LIFECYCLE_TIMER_MILLISECONDS"
        // endregion

        // region Default initial timer values
        /**
         * Default number of seconds (not including minutes) on timer if seconds preference is not set
         * */
        internal const val DEFAULT_NUM_MILLISECONDS_TIMER = 0L
        // endregion

        // region time constants
        private const val MILLISECONDS_IN_SECOND = 1_000L
        // endregion
    }

    // Exception to denote the library has not been initialized
    private class UninitializedLibraryException(string: String? = null) : Exception(string)
}