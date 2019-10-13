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
        LifecycleAwareTimer.getInstance(this.lifecycle, "test key")
    }

    private val timer2: LifecycleAwareTimer by lazy {
        LifecycleAwareTimer.getInstance(this.lifecycle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

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
    fun testFunction(view: View) {
        timer.setMinutes(0)
        timer2.run {
            setMinutes(0)
            setSeconds(0)
        }
    }

    companion object {
        /**
         * Format string for zero-padding seconds and minutes
         * */
        private const val ZERO_PAD_FORMAT_PATTERN = "%02d"
    }

}
