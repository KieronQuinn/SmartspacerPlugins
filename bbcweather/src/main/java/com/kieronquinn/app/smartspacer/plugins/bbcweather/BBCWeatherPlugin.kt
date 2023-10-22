package com.kieronquinn.app.smartspacer.plugins.bbcweather

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.GsonBuilder
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.utils.gson.BitmapTypeAdapter
import com.kieronquinn.app.smartspacer.plugins.bbcweather.repositories.BBCWeatherRepository
import com.kieronquinn.app.smartspacer.plugins.bbcweather.repositories.BBCWeatherRepositoryImpl
import org.koin.dsl.module

class BBCWeatherPlugin: SmartspacerPlugin() {

    companion object {
        const val PACKAGE_NAME = "bbc.mobile.weather"
    }

    override fun getModule(context: Context) = module {
        single<BBCWeatherRepository> { BBCWeatherRepositoryImpl(get(), get()) }
    }

    override fun GsonBuilder.configure() = apply {
        registerTypeAdapter(Bitmap::class.java, BitmapTypeAdapter())
    }

}