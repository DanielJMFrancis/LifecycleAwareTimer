package com.franieldancis.lifecycleawaretimer.di

import android.content.Context
import com.franieldancis.lifecycleawaretimer.di.modules.LibModule
import com.franieldancis.lifecycleawaretimer.main.LifecycleAwareTimer
import com.franieldancis.lifecycleawaretimer.main.TimerStatus
import dagger.BindsInstance
import dagger.Component
import javax.inject.Provider
import javax.inject.Singleton

@Component(
    modules = [LibModule::class]
)
@Singleton
internal interface LibComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance applicationContext: Context
        ): LibComponent
    }

    val timerFactory: Provider<LifecycleAwareTimer.Factory>
    val timerStatus: Provider<TimerStatus>
}