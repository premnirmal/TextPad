package com.github.premnirmal.textpad

import android.app.Application
import dagger.hilt.EntryPoints
import kotlin.jvm.java

object Injector {

    lateinit var appComponent: AppComponent

    fun initialize(app: Application) {
        appComponent = EntryPoints.get(app, AppComponent::class.java)
    }
}