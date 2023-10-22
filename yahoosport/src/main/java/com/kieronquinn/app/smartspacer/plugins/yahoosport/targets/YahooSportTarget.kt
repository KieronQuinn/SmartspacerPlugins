package com.kieronquinn.app.smartspacer.plugins.yahoosport.targets

import android.content.ComponentName
import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.plugins.yahoosport.R
import com.kieronquinn.app.smartspacer.plugins.yahoosport.receivers.YahooSportTargetClickReceiver.Companion.createPendingIntent
import com.kieronquinn.app.smartspacer.plugins.yahoosport.repositories.GameRepository
import com.kieronquinn.app.smartspacer.plugins.yahoosport.repositories.GameRepository.Game
import com.kieronquinn.app.smartspacer.plugins.yahoosport.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugins.yahoosport.widgets.YahooSportWidget
import com.kieronquinn.app.smartspacer.sdk.model.Backup
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon

class YahooSportTarget: SmartspacerTargetProvider() {

    private val gameRepository by inject<GameRepository>()
    private val dataRepository by inject<DataRepository>()
    private val gson by inject<Gson>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val game = gameRepository.getGame(smartspacerId) ?: return emptyList()
        val targetData = getTargetData(smartspacerId)
        if(targetData.dismissedGames.contains(game.getGameHash().toString())) return emptyList()
        return listOf(game.toTarget(smartspacerId))
    }

    private fun Game.toTarget(smartspacerId: String): SmartspaceTarget {
        return TargetTemplate.HeadToHead(
            provideContext(),
            "yahoo_sport_${smartspacerId}_at_${System.currentTimeMillis()}",
            ComponentName(provideContext(), this::class.java),
            Text(team1Name),
            Text(resources.getString(R.string.target_subtitle_template, team2Name)),
            Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_yahoo_sport)),
            TapAction(pendingIntent = createPendingIntent(provideContext(), smartspacerId)),
            Text(resources.getString(R.string.target_title_template, date, period).formatTitle()),
            Icon(AndroidIcon.createWithBitmap(team1Icon), shouldTint = false),
            Text(team1Score),
            Icon(AndroidIcon.createWithBitmap(team2Icon), shouldTint = false),
            Text(team2Score)
        ).create()
    }

    private fun String.formatTitle(): CharSequence {
        return SpannableStringBuilder(this).apply {
            setSpan(
                RelativeSizeSpan(0.75f), 0, length, Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
    }

    override fun getConfig(smartspacerId: String?): Config {
        val targetData = smartspacerId?.let { getTargetData(it) }
        val description = targetData?.teamName?.let {
            resources.getString(R.string.target_description_set, it)
        } ?: resources.getString(R.string.target_description)
        return Config(
            resources.getString(R.string.target_label),
            description,
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_yahoo_sport),
            compatibilityState = getCompatibilityState(),
            widgetProvider = YahooSportWidget.AUTHORITY,
            allowAddingMoreThanOnce = true,
            configActivity = createIntent(provideContext(), NavGraphMapping.CONFIGURATION)
        )
    }

    override fun createBackup(smartspacerId: String): Backup {
        val targetData = getTargetData(smartspacerId)
        val description = targetData.teamName?.let {
            resources.getString(R.string.target_description_set, it)
        } ?: resources.getString(R.string.target_description)
        val data = gson.toJson(getTargetData(smartspacerId))
        return Backup(data, description)
    }

    override fun restoreBackup(smartspacerId: String, backup: Backup): Boolean {
        val data = try {
            gson.fromJson(backup.data, TargetData::class.java)
        }catch (e: Exception) {
            return false
        }
        dataRepository.updateTargetData(
            smartspacerId,
            TargetData::class.java,
            TargetData.TYPE,
            ::onRestored
        ) {
            data
        }
        return true
    }

    private fun onRestored(context: Context, smartspacerId: String) {
        notifyChange(smartspacerId)
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        val game = gameRepository.getGame(smartspacerId) ?: run {
            notifyChange(smartspacerId)
            return true
        }
        dataRepository.updateTargetData(
            smartspacerId,
            TargetData::class.java,
            TargetData.TYPE,
            ::onChanged
        ) {
            val data = it ?: TargetData()
            data.copy(dismissedGames = data.dismissedGames.plus(game.getGameHash().toString()))
        }
        return true
    }

    private fun onChanged(context: Context, smartspacerId: String) {
        notifyChange(smartspacerId)
    }

    private fun getTargetData(smartspacerId: String): TargetData {
        return dataRepository.getTargetData(smartspacerId, TargetData::class.java) ?: TargetData()
    }

    override fun onProviderRemoved(smartspacerId: String) {
        gameRepository.setGame(smartspacerId, null)
    }

    private fun getCompatibilityState(): CompatibilityState {
        return if(YahooSportWidget.getProvider(provideContext()) == null) {
            CompatibilityState.Incompatible(resources.getString(R.string.target_incompatible))
        }else CompatibilityState.Compatible
    }

    data class TargetData(
        @SerializedName("team_name")
        val teamName: String? = null,
        @SerializedName("dismissed_games")
        val dismissedGames: Set<String> = emptySet()
    ) {

        companion object {
            const val TYPE = "sport"
        }

    }

}