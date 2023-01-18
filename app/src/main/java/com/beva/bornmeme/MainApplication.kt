package com.beva.bornmeme

import android.app.Application
import android.content.Context
import kotlin.properties.Delegates

class MainApplication : Application() {

    companion object {
        var instance: MainApplication by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

}