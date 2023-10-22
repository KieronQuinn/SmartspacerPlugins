package com.kieronquinn.app.smartspacer.plugins.pokemongo.providers

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository.EggConfiguration
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository.WidgetConfiguration
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import org.koin.android.ext.android.inject

abstract class EggWidgetProvider: BaseWidgetProvider() {

    companion object {
        private const val CLASS_WIDGET =
            "com.nianticproject.holoholo.libholoholo.appwidget.EggsWidget"
        private const val IDENTIFIER_NO_EGGS = ":id/no_eggs_view"
        private const val IDENTIFIER_SINGLE_EGG = ":id/single_egg_view"
        private const val IDENTIFIER_MULTIPLE_EGG = ":id/multiple_eggs_view"
        private const val IDENTIFIER_PROGRESS = ":id/egg_hatching_progress"
        private const val IDENTIFIER_PROGRESS_0 = ":id/incubator_progress_0"
        private const val IDENTIFIER_PROGRESS_1 = ":id/incubator_progress_1"
        private const val IDENTIFIER_PROGRESS_2 = ":id/incubator_progress_2"
        private const val IDENTIFIER_PROGRESS_3 = ":id/incubator_progress_3"
        private const val IDENTIFIER_IMAGE = ":id/incubator_egg_image"
        private const val IDENTIFIER_IMAGE_0 = ":id/incubator_egg_image_0"
        private const val IDENTIFIER_IMAGE_1 = ":id/incubator_egg_image_1"
        private const val IDENTIFIER_IMAGE_2 = ":id/incubator_egg_image_2"
        private const val IDENTIFIER_IMAGE_3 = ":id/incubator_egg_image_3"
        private const val IDENTIFIER_HATCHING = ":id/egg_hatching_text"
        private const val IDENTIFIER_HATCHING_0 = ":id/egg_hatching_text_0"
        private const val IDENTIFIER_HATCHING_1 = ":id/egg_hatching_text_1"
        private const val IDENTIFIER_HATCHING_2 = ":id/egg_hatching_text_2"
        private const val IDENTIFIER_HATCHING_3 = ":id/egg_hatching_text_3"
    }

    private val widgetRepository by inject<WidgetRepository>()

    private val appWidgetManager by lazy {
        provideContext().getSystemService(Context.APPWIDGET_SERVICE) as AppWidgetManager
    }

    private val providerInfo by lazy {
        appWidgetManager.installedProviders.firstOrNull {
            it.provider.packageName == variant.packageName &&
                    it.provider.className == CLASS_WIDGET
        }
    }

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        return providerInfo
    }

    override fun onWidgetChanged(smartspacerId: String, remoteViews: RemoteViews?) {
        val views = remoteViews?.load() ?: return
        val eggs = when {
            views.isVisible(IDENTIFIER_NO_EGGS) -> emptyList()
            views.isVisible(IDENTIFIER_SINGLE_EGG) -> {
                listOfNotNull(views.getProgressLarge(
                    IDENTIFIER_PROGRESS, IDENTIFIER_IMAGE, IDENTIFIER_HATCHING
                ))
            }
            views.isVisible(IDENTIFIER_MULTIPLE_EGG) -> {
                listOfNotNull(
                    views.getProgress(
                        IDENTIFIER_PROGRESS_0, IDENTIFIER_IMAGE_0, IDENTIFIER_HATCHING_0
                    ),
                    views.getProgress(
                        IDENTIFIER_PROGRESS_1, IDENTIFIER_IMAGE_1, IDENTIFIER_HATCHING_1
                    ),
                    views.getProgress(
                        IDENTIFIER_PROGRESS_2, IDENTIFIER_IMAGE_2, IDENTIFIER_HATCHING_2
                    ),
                    views.getProgress(
                        IDENTIFIER_PROGRESS_3, IDENTIFIER_IMAGE_3, IDENTIFIER_HATCHING_3
                    )
                )
            }
            else -> emptyList()
        }
        widgetRepository.writeEggConfiguration(variant, EggConfiguration(eggs))
    }

    private fun View.getProgress(
        progress: String, image: String, hatching: String
    ): WidgetConfiguration? {
        val container = findViewByIdentifier<LinearLayout>(getIdentifier(progress)) ?: return null
        if(!container.isVisible) return null
        val hatchingText = findViewByIdentifier<TextView>(getIdentifier(hatching))
        val text = if(hatchingText?.isVisible == true){
            hatchingText.text.toString()
        }else{
            container.child()?.child()?.getProgressText()
        } ?: return null
        val egg = findViewByIdentifier<ImageView>(getIdentifier(image))?.getImageAsBitmap()
        return WidgetConfiguration(text, egg)
    }

    private fun View.getProgressLarge(
        progress: String, image: String, hatching: String
    ): WidgetConfiguration? {
        val container = findViewByIdentifier<LinearLayout>(getIdentifier(progress)) ?: return null
        val hatchingText = findViewByIdentifier<TextView>(hatching)
        val text = if(hatchingText?.isVisible == true){
            hatchingText.text.toString()
        }else {
            container.child()?.getProgressText()
        } ?: return null
        val egg = findViewByIdentifier<ImageView>(getIdentifier(image))?.getImageAsBitmap()
        return WidgetConfiguration(text, egg)
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config()
    }

}