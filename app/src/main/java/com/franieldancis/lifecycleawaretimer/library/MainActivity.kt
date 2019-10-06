package com.franieldancis.lifecycleawaretimer.library

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        timer.run {
            minutes.observe(this@MainActivity, Observer {
                timerMinutes.text = String.format(ZERO_PAD_FORMAT_PATTERN, it)
            })

            seconds.observe(this@MainActivity, Observer {
                timerSeconds.text = String.format(ZERO_PAD_FORMAT_PATTERN, it)
            })
        }

    }

    fun testFunction(view: View) {
        timer.setMinutes(5)
    }

    companion object {
        /**
         * Format string for zero-padding seconds and minutes
         * */
        private const val ZERO_PAD_FORMAT_PATTERN = "%02d"
    }

}
