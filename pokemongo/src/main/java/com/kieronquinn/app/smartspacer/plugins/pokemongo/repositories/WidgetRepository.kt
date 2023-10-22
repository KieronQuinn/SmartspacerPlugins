package com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.shared.utils.gson.BitmapTypeAdapter
import com.kieronquinn.app.smartspacer.plugins.pokemongo.PokemonGoPlugin.Variant
import com.kieronquinn.app.smartspacer.plugins.pokemongo.R
import com.kieronquinn.app.smartspacer.plugins.pokemongo.complications.BuddyComplicationPlay
import com.kieronquinn.app.smartspacer.plugins.pokemongo.complications.BuddyComplicationSamsung
import com.kieronquinn.app.smartspacer.plugins.pokemongo.complications.EggComplicationPlay
import com.kieronquinn.app.smartspacer.plugins.pokemongo.complications.EggComplicationSamsung
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository.EggConfiguration
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository.WidgetConfiguration
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository.WidgetType
import com.kieronquinn.app.smartspacer.plugins.pokemongo.utils.makeSquare
import com.kieronquinn.app.smartspacer.plugins.pokemongo.utils.resizeTo
import com.kieronquinn.app.smartspacer.plugins.pokemongo.utils.trim
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import java.io.File

interface WidgetRepository {

    fun getEggConfiguration(variant: Variant): EggConfiguration?
    fun getBuddyConfiguration(variant: Variant): WidgetConfiguration?

    fun writeEggConfiguration(
        variant: Variant,
        eggConfiguration: EggConfiguration?
    )

    fun writeBuddyConfiguration(
        variant: Variant,
        widgetConfiguration: WidgetConfiguration?
    )

    fun getComplicationClass(
        variant: Variant,
        widgetType: WidgetType
    ): Class<out SmartspacerComplicationProvider>

    data class EggConfiguration(
        @SerializedName("eggs")
        val eggs: List<WidgetConfiguration>
    )

    data class WidgetConfiguration(
        @SerializedName("text")
        val text: String,
        @SerializedName("icon")
        val icon: Bitmap?
    )

    enum class WidgetType(
        val filename: String,
        val dl: String,
        @StringRes
        val configurationTitle: Int,
        @StringRes
        val configurationStaticIconContent: Int,
        @DrawableRes
        val staticIcon: Int
    ) {
        EGG(
            "egg",
            "notification://dl_action=OPEN_POKEMON_INVENTORY,dl_tab=EGGS",
            R.string.complication_egg_label_short,
            R.string.configuration_egg_use_static_content,
            R.drawable.ic_complication_egg
        ),
        BUDDY(
            "buddy",
            "notification://dl_action=OPEN_BUDDY",
            R.string.complication_buddy_label_short,
            R.string.configuration_buddy_use_static_content,
            R.drawable.ic_complication_buddy
        )
    }

}

class WidgetRepositoryImpl(private val context: Context): WidgetRepository {

    companion object {
        private const val ICON_SIZE = 48
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(Bitmap::class.java, BitmapTypeAdapter())
        .create()

    private val cacheDir = context.cacheDir

    override fun getBuddyConfiguration(variant: Variant): WidgetConfiguration? {
        return getJsonFile(variant, WidgetType.BUDDY).readWidgetConfiguration()
    }

    override fun getEggConfiguration(variant: Variant): EggConfiguration? {
        return getJsonFile(variant, WidgetType.EGG).readEggConfiguration()
    }

    override fun writeBuddyConfiguration(
        variant: Variant,
        widgetConfiguration: WidgetConfiguration?
    ) {
        getJsonFile(variant, WidgetType.BUDDY).apply {
            if(widgetConfiguration == null){
                delete()
            }else{
                val config = widgetConfiguration.copy(
                    icon = widgetConfiguration.icon?.trim()?.makeSquare()?.resizeTo(ICON_SIZE)
                )
                writeText(gson.toJson(config))
            }
        }
        val complication = getComplicationClass(variant, WidgetType.BUDDY)
        SmartspacerComplicationProvider.notifyChange(context, complication)
    }

    override fun writeEggConfiguration(variant: Variant, eggConfiguration: EggConfiguration?) {
        getJsonFile(variant, WidgetType.EGG).apply {
            if(eggConfiguration == null){
                delete()
            }else{
                val eggs = eggConfiguration.eggs.map {
                    it.copy(icon = it.icon?.trim()?.makeSquare()?.resizeTo(ICON_SIZE))
                }
                writeText(gson.toJson(eggConfiguration.copy(eggs = eggs)))
            }
        }
        val complication = getComplicationClass(variant, WidgetType.EGG)
        SmartspacerComplicationProvider.notifyChange(context, complication)
    }

    override fun getComplicationClass(
        variant: Variant,
        widgetType: WidgetType
    ): Class<out SmartspacerComplicationProvider> {
        return when(widgetType) {
            WidgetType.EGG -> {
                when(variant) {
                    Variant.PLAY -> EggComplicationPlay::class.java
                    Variant.SAMSUNG -> EggComplicationSamsung::class.java
                }
            }
            WidgetType.BUDDY -> {
                when(variant) {
                    Variant.PLAY -> BuddyComplicationPlay::class.java
                    Variant.SAMSUNG -> BuddyComplicationSamsung::class.java
                }
            }
        }
    }

    private fun getJsonFile(variant: Variant, widgetType: WidgetType): File {
        return File(cacheDir, "${variant.packageName}_${widgetType.filename}.json")
    }

    private fun File.readEggConfiguration(): EggConfiguration? {
        if(!exists()) return null
        return try {
            gson.fromJson(readText(), EggConfiguration::class.java)
        }catch (e: Exception){
            null
        }
    }

    private fun File.readWidgetConfiguration(): WidgetConfiguration? {
        if(!exists()) return null
        return try {
            gson.fromJson(readText(), WidgetConfiguration::class.java)
        }catch (e: Exception){
            null
        }
    }

}