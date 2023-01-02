package com.beva.bornmeme

import android.app.Application
import android.content.Context
import kotlin.properties.Delegates

class MainApplication : Application() {

    //    init {
//        instance = this
//    }
//
//    companion object {
//        private var instance: MainApplication? = null
//
//        fun applicationContext() : Context? {
//            return instance?.applicationContext
//        }
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        // initialize for any
//
//        // Use ApplicationContext.
//        // example: SharedPreferences etc...
//        val context: Context? = MainApplication.applicationContext()
//    }
    companion object {
        var instance: MainApplication by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

}