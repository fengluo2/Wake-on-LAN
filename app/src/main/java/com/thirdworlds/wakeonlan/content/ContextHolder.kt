package com.thirdworlds.wakeonlan.content

import android.content.Context

object ContextHolder {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun get(): Context = appContext
}