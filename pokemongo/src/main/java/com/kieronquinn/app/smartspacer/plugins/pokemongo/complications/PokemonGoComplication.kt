package com.kieronquinn.app.smartspacer.plugins.pokemongo.complications

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.countOf
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.isPackageInstalled
import com.kieronquinn.app.smartspacer.plugins.pokemongo.PokemonGoPlugin.Variant
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository.WidgetConfiguration
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository.WidgetType
import com.kieronquinn.app.smartspacer.plugins.pokemongo.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugins.pokemongo.ui.screens.configuration.ConfigurationFragment
import com.kieronquinn.app.smartspacer.sdk.model.Backup
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon

abstract class PokemonGoComplication: SmartspacerComplicationProvider() {

    abstract val widgetType: WidgetType
    abstract val variant: Variant
    abstract val titleRes: Int
    abstract val descriptionRes: Int
    abstract val iconRes: Int
    abstract val incompatibleRes: Int
    abstract val widgetProvider: String

    protected val widgetRepository by inject<WidgetRepository>()
    private val dataRepository by inject<DataRepository>()
    private val gson by inject<Gson>()

    abstract fun getWidgets(): List<WidgetConfiguration>

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val configs = getWidgets()
        val settings = getSettings(smartspacerId)
        return configs.mapIndexed { index, config ->
            val icon = if(config.icon != null && !settings.useStaticIcon) {
                Icon(AndroidIcon.createWithBitmap(config.icon), shouldTint = false)
            } else {
                Icon(AndroidIcon.createWithResource(provideContext(), iconRes))
            }
            ComplicationTemplate.Basic(
                id = getId(smartspacerId, index, settings.useStaticIcon),
                content = Text(config.text.removeDuplicateUnit()),
                icon = icon,
                onClick = TapAction(intent = getLaunchIntent())
            ).create()
        }
    }

    private fun getId(smartspacerId: String, index: Int, useStaticIcon: Boolean): String {
        val packageName = variant.packageName
        val filename = widgetType.filename
        return "pogo_${packageName}_${filename}_${index}_${useStaticIcon}_$smartspacerId"
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            resources.getString(titleRes),
            resources.getString(descriptionRes),
            AndroidIcon.createWithResource(provideContext(), iconRes),
            compatibilityState = getCompatibilityState(),
            configActivity = getConfigurationIntent(),
            widgetProvider = widgetProvider,
            allowAddingMoreThanOnce = true
        )
    }

    override fun createBackup(smartspacerId: String): Backup {
        val settings = gson.toJson(getSettings(smartspacerId))
        return Backup(settings, resources.getString(descriptionRes))
    }

    override fun restoreBackup(smartspacerId: String, backup: Backup): Boolean {
        val settings = try {
            gson.fromJson(backup.data, ComplicationData::class.java)
        }catch (e: Exception) {
            return false
        }
        dataRepository.updateComplicationData(
            smartspacerId,
            ComplicationData::class.java,
            ComplicationData.TYPE,
            ::onChanged
        ) {
            ComplicationData(settings.useStaticIcon)
        }
        return true
    }

    private fun onChanged(context: Context, smartspacerId: String) {
        notifyChange(context, this::class.java, smartspacerId)
    }

    private fun getSettings(smartspacerId: String): ComplicationData {
        return dataRepository.getComplicationData(smartspacerId, ComplicationData::class.java)
            ?: ComplicationData()
    }

    private fun getCompatibilityState(): CompatibilityState {
        return if(!isInstalled()){
            CompatibilityState.Incompatible(provideContext().getString(incompatibleRes))
        }else CompatibilityState.Compatible
    }

    private fun isInstalled(): Boolean {
        return provideContext().packageManager.isPackageInstalled(variant.packageName)
    }

    private fun getConfigurationIntent(): Intent {
        return createIntent(provideContext(), NavGraphMapping.POKEMON_GO).apply {
            ConfigurationFragment.setup(this, widgetType, variant)
        }
    }

    private fun getLaunchIntent(): Intent? {
        return provideContext().packageManager.getLaunchIntentForPackage(
            variant.packageName
        )?.apply {
            putExtra("dl", widgetType.dl)
        }
    }

    private fun String.removeDuplicateUnit(): String {
        return if(countOf("km") == 2) {
            replaceFirst(" km", "")
        }else this
    }

    data class ComplicationData(
        @SerializedName("use_static_icon")
        val useStaticIcon: Boolean = false
    ) {
        companion object {
            const val TYPE = "pokemon_go"
        }
    }

}