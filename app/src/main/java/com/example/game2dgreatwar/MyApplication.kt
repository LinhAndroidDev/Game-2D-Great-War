package com.example.game2dgreatwar

import android.app.Application
import android.os.StrictMode

class MyApplication : Application() {
    var screenWidth = 0
    var screenHeight = 0

    override fun onCreate() {
        super.onCreate()
        setUpScreenSize()

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    private fun setUpScreenSize() {
        resources.displayMetrics.run {
            screenWidth = this.widthPixels
            screenHeight = this.heightPixels
        }
    }
}