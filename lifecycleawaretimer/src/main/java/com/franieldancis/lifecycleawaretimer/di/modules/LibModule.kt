package com.franieldancis.lifecycleawaretimer.di.modules

import android.content.Context
import android.content.SharedPreferences
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module
import dagger.Provides

@AssistedModule
@Module(includes = [AssistedInject_LibModule::class])
object LibModule {
    @JvmStatic @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(SHARED_PREFERENCES_TIMER, Context.MODE_PRIVATE)
    }

    private const val SHARED_PREFERENCES_TIMER = "franiel_dancis_shared_prefs"
}