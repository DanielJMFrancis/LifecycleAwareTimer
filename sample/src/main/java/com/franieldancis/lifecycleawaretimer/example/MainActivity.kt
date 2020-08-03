package com.franieldancis.lifecycleawaretimer.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.franieldancis.lifecycleawaretimer.R
import com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

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
            setTimeLeft(seconds = 5)

            // Timer 2 - preference key associated
            setTimeLeft(days = 2, hours = 2, minutes = 2, seconds = 30, prefsKey = timer2.prefsKey)
        }

        timer.run {
            // Observe time ticking
            milliseconds.observe(this@MainActivity, Observer { millisLeft ->
                val days = TimeUnit.MILLISECONDS.toDays(millisLeft)
                val hours = TimeUnit.MILLISECONDS.toHours(millisLeft)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisLeft)

                // Round to closest second
                val seconds = (millisLeft / 1000.0).roundToInt()

                // Convert and display the time left on the timer
                timerDays.text = String.format(ZERO_PAD_FORMAT_PATTERN, days)
                timerHours.text = String.format(ZERO_PAD_FORMAT_PATTERN, hours % 24)
                timerMinutes.text = String.format(ZERO_PAD_FORMAT_PATTERN, minutes % 60)
                timerSeconds.text = String.format(ZERO_PAD_FORMAT_PATTERN, seconds % 60)
            })

            // Observe timer finishing
            onFinish.observe(this@MainActivity, Observer {
                Toast.makeText(this@MainActivity, "Timer 1 has finished", Toast.LENGTH_SHORT).show()
            })
        }

        timer2.run {
            // Observe time ticking
            milliseconds.observe(this@MainActivity, Observer { millisLeft ->
                val days = TimeUnit.MILLISECONDS.toDays(millisLeft)
                val hours = TimeUnit.MILLISECONDS.toHours(millisLeft)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisLeft)

                // Round to closest second
                val seconds = (millisLeft / 1000.0).roundToInt()

                // Convert and display the time left on the timer
                timerDays2.text = String.format(ZERO_PAD_FORMAT_PATTERN, days)
                timerHours2.text = String.format(ZERO_PAD_FORMAT_PATTERN, hours % 24)
                timerMinutes2.text = String.format(ZERO_PAD_FORMAT_PATTERN, minutes % 60)
                timerSeconds2.text = String.format(ZERO_PAD_FORMAT_PATTERN, seconds % 60)
            })
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun resetTimer(view: View) {
        timer.run {
            setTimeLeftOnActiveTimer(seconds = 5)
        }
        timer2.run {
            setTimeLeftOnActiveTimer(days = 2, hours = 2, minutes = 2, seconds = 30)
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
