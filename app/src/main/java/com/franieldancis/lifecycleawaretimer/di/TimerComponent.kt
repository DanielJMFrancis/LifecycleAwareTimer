//package com.franieldancis.lifecycleawaretimer.di
//
//import android.content.Context
//import com.franieldancis.lifecycleawaretimer.di.modules.TimerModule
//import com.franieldancis.lifecycleawaretimer.di.modules.UiModule
//import dagger.BindsInstance
//import dagger.Component
//import dagger.android.AndroidInjectionModule
//import dagger.android.AndroidInjector
//import javax.inject.Singleton
//
//@Component(
//    modules = [TimerModule::class,
//        UiModule::class,
//        AndroidInjectionModule::class]
//)
//@Singleton
//interface TimerComponent : AndroidInjector<TimerApplication> {
//    @Component.Factory
//    interface Factory {
//        fun create(
//            @BindsInstance applicationContext: Context
//        ): TimerComponent
//    }
//}
