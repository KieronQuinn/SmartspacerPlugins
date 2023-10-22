package com.kieronquinn.app.smartspacer.plugins.yahoosport.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugins.yahoosport.BuildConfig
import com.kieronquinn.app.smartspacer.plugins.yahoosport.YahooSportPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugins.yahoosport.repositories.GameRepository
import com.kieronquinn.app.smartspacer.plugins.yahoosport.repositories.GameRepository.Game
import com.kieronquinn.app.smartspacer.plugins.yahoosport.utils.extensions.addPadding
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import org.koin.android.ext.android.inject

class YahooSportWidget: SmartspacerWidgetProvider() {

    companion object {
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.widgets.yahoosport"

        private val COMPONENT_WIDGET = ComponentName(
            PACKAGE_NAME,
            "com.protrade.sportacular.widget.SingleScoreWidgetProvider"
        )

        private const val IDENTIFIER_CLICKABLE = "$PACKAGE_NAME:id/widgetParent"
        private const val IDENTIFIER_TEAM1_IMAGE = "$PACKAGE_NAME:id/widgetTeam1Image"
        private const val IDENTIFIER_TEAM2_IMAGE = "$PACKAGE_NAME:id/widgetTeam2Image"
        private const val IDENTIFIER_TEAM1_NAME = "$PACKAGE_NAME:id/widgetTeam1Name"
        private const val IDENTIFIER_TEAM2_NAME = "$PACKAGE_NAME:id/widgetTeam2Name"
        private const val IDENTIFIER_TEAM1_SCORE = "$PACKAGE_NAME:id/widgetTeam1Score"
        private const val IDENTIFIER_TEAM2_SCORE = "$PACKAGE_NAME:id/widgetTeam2Score"
        private const val IDENTIFIER_DATE = "$PACKAGE_NAME:id/widgetDate"
        private const val IDENTIFIER_PERIOD = "$PACKAGE_NAME:id/widgetPeriod"
        private const val ICON_PADDING = 0.25f

        fun getProvider(context: Context): AppWidgetProviderInfo? {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            return appWidgetManager.installedProviders.firstOrNull {
                it.provider == COMPONENT_WIDGET
            }
        }

        fun clickWidget(context: Context, smartspacerId: String) {
            clickView(context, smartspacerId, IDENTIFIER_CLICKABLE)
        }
    }

    private val gameRepository by inject<GameRepository>()

    override fun onWidgetChanged(smartspacerId: String, remoteViews: RemoteViews?) {
        val views = remoteViews?.load() ?: return
        val team1Score = views.findViewByIdentifier<TextView>(IDENTIFIER_TEAM1_SCORE)
        if(team1Score == null || !team1Score.isVisible) {
            //Game has not started yet, don't show
            gameRepository.setGame(smartspacerId, null)
            return
        }
        val team2Score = views.findViewByIdentifier<TextView>(IDENTIFIER_TEAM2_SCORE) ?: return
        val team1Image = views.findViewByIdentifier<ImageView>(IDENTIFIER_TEAM1_IMAGE) ?: return
        val team2Image = views.findViewByIdentifier<ImageView>(IDENTIFIER_TEAM2_IMAGE) ?: return
        val team1Name = views.findViewByIdentifier<TextView>(IDENTIFIER_TEAM1_NAME) ?: return
        val team2Name = views.findViewByIdentifier<TextView>(IDENTIFIER_TEAM2_NAME) ?: return
        val date = views.findViewByIdentifier<TextView>(IDENTIFIER_DATE) ?: return
        val period = views.findViewByIdentifier<TextView>(IDENTIFIER_PERIOD) ?: return
        gameRepository.setGame(
            smartspacerId,
            Game(
                team1Name.text.toString(),
                team2Name.text.toString(),
                team1Image.drawable.toBitmap().addPadding(ICON_PADDING),
                team2Image.drawable.toBitmap().addPadding(ICON_PADDING),
                team1Score.text.toString(),
                team2Score.text.toString(),
                date.text.toString(),
                period.text.toString()
            )
        )
        gameRepository.setTeamName(smartspacerId, team1Name.text.toString())
    }

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        return getProvider(provideContext())
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config()
    }

}