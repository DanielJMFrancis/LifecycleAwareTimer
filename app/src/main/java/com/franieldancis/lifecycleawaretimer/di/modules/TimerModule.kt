//package com.franieldancis.lifecycleawaretimer.di.modules
//
//import android.content.Context
//import android.content.Context.MODE_PRIVATE
//import android.content.SharedPreferences
//import com.squareup.inject.assisted.dagger2.AssistedModule
//import dagger.Module
//import dagger.Provides
//
//@AssistedModule
//@Module(includes = [AssistedInject_TimerModule::class])
//object TimerModule {
//    @JvmStatic @Provides
//    fun provideSharedPreferences(context: Context): SharedPreferences {
//        return context.getSharedPreferences(SHARED_PREFERENCES_TIMER, MODE_PRIVATE)
//    }
//
//    private const val SHARED_PREFERENCES_TIMER = "franiel_dancis_shared_prefs"
//}