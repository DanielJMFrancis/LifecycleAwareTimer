package com.franieldancis.lifecycleawaretimer.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.franieldancis.lifecycleawaretimer.R
import com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val timer: LifecycleAwareTimer by lazy {
        LifecycleAwareTimer.getInstance(this.lifecycle)
    }

    private val timer2: LifecycleAwareTimer by lazy {
        LifecycleAwareTimer.getInstance(this.lifecycle, EXAMPLE_TIMER_KEY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Set initial time for both timers
        LifecycleAwareTimer.run {
            // Timer 1 - no preference key associated with it
            setDays(1)
            setHours(1)
            setMinutes(1)
            setSeconds(5)

            // Timer 2 - preference key associated
            setDays(2, timer2.prefsKey)
            setHours(2, timer2.prefsKey)
            setMinutes(2, timer2.prefsKey)
            setSeconds(30, timer2.prefsKey)
        }

        timer.run {
            days.observe(this@MainActivity, Observer {
                timerDays.text = String.format(ZERO_PAD_FORMAT_PATTERN, it)
            })
            hours.observe(this@MainActivity, Observer {
                timerHours.text = String.format(ZERO_PAD_FORMAT_PATTERN, it)
            })
            minutes.observe(this@MainActivity, Observer {
                timerMinutes.text = String.format(ZERO_PAD_FORMAT_PATTERN, it)
            })

            seconds.observe(this@MainActivity, Observer {
                timerSeconds.text = String.format(ZERO_PAD_FORMAT_PATTERN, it)
            })
        }

        timer2.run {
            days.observe(this@MainActivity, Observer {
                timerDays2.text = String.format(ZERO_PAD_FORMAT_PATTERN, it)
            })
            hours.observe(this@MainActivity, Observer {
                timerHours2.text = String.format(ZERO_PAD_FORMAT_PATTERN, it)
            })
            minutes.observe(this@MainActivity, Observer {
                timerMinutes2.text = String.format(ZERO_PAD_FORMAT_PATTERN, it)
            })

            seconds.observe(this@MainActivity, Observer {
                timerSeconds2.text = String.format(ZERO_PAD_FORMAT_PATTERN, it)
            })
        }

    }

    @Suppress("UNUSED_PARAMETER")
    fun resetTimer(view: View) {
        timer.run {
            setActiveDays(1)
            setActiveHours(1)
            setActiveMinutes(1)
            setActiveSeconds(5)
        }
        timer2.run {
            setActiveDays(2)
            setActiveHours(2)
            setActiveMinutes(2)
            setActiveSeconds(30)
        }
    }

    companion object {
        /**
         * Format string for zero-padding seconds and minutes
         * */
        private const val ZERO_PAD_FORMAT_PATTERN = "%02d"

        /**
         * Example timer key value
         * */
        const val EXAMPLE_TIMER_KEY = "EXAMPLE_TIMER_KEY"
    }

}
