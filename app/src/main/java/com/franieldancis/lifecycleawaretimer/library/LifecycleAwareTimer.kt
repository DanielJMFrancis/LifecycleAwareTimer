//package com.franieldancis.lifecycleawaretimer.library
//
//import android.content.SharedPreferences
//import android.os.CountDownTimer
//import androidx.lifecycle.*
//
//class LifecycleAwareTimer @AssistedInject constructor(
//    @Assisted val lifecycle: Lifecycle,
//    val sharedPreferences: SharedPreferences
//) : LifecycleObserver {
//
//    // Seconds emissions
//    private val _seconds = MutableLiveData<Long>()
//    val seconds = _seconds as LiveData<Long>
//
//    // Minutes emissions
//    private val _minutes = MutableLiveData<Long>()
//    val minutes = _minutes as LiveData<Long>
//
//    private var countdownTimer = createCountDownTimer(300000)
//
//    init {
//        lifecycle.addObserver(this)
//    }
//
//    @AssistedInject.Factory
//    interface Factory {
//        fun create(lifecycle: Lifecycle): LifecycleAwareTimer
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
//    fun onResume() {
//        val minutesValue = sharedPreferences.getLong("minutes", 5)
//        val secondsValue = sharedPreferences.getLong("seconds", 0)
//        countdownTimer = createCountDownTimer(minutesValue * 60 + secondsValue)
//        countdownTimer.start()
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
//    fun onPause() {
//        sharedPreferences.edit()
//            .putLong("minutes", _minutes.value ?: 0)
//            .putLong("seconds", _seconds.value ?: 0)
//            .apply()
//        countdownTimer.cancel()
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//    fun onDestroy() {
//        lifecycle.removeObserver(this)
//    }
//
//    private fun createCountDownTimer(overallSeconds: Long): CountDownTimer {
//        return object : CountDownTimer(overallSeconds * 1000, 1000) {
//            override fun onFinish() {
//                // TODO: Update shared preferences minutes and seconds to zero
//            }
//
//            override fun onTick(millisUntilFinished: Long) {
//                val overallSecondsLeft = millisUntilFinished / 1000
//                val secondsLeft = overallSecondsLeft % 60
//                val minutesLeft = overallSecondsLeft / 60
//                _seconds.postValue(secondsLeft)
//                if (_minutes.value != minutesLeft) {
//                    _minutes.postValue(minutesLeft)
//                }
//            }
//        }
//    }
//}