package com.github.premnirmal.textpad

import android.content.Context

object Injector {

    lateinit var appComponent: AppComponent

    fun initialize(context: Context) {
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(context))
            .build()
    }
}