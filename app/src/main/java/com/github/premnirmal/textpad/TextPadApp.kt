package com.github.premnirmal.textpad

import android.app.Application

class TextPadApp: Application() {

    override fun onCreate() {
        super.onCreate()
        Injector.initialize(this)
    }
}