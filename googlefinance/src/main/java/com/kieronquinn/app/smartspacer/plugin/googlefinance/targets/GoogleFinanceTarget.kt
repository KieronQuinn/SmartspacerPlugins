package com.kieronquinn.app.smartspacer.plugin.googlefinance.targets

import android.app.PendingIntent
import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.googlefinance.GoogleFinancePlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.googlefinance.R
import com.kieronquinn.app.smartspacer.plugin.googlefinance.model.FinancialWidget
import com.kieronquinn.app.smartspacer.plugin.googlefinance.repositories.GoogleFinanceRepository
import com.kieronquinn.app.smartspacer.plugin.googlefinance.targets.GoogleFinanceTarget.TargetData.MinimumTrendDirection
import com.kieronquinn.app.smartspacer.plugin.googlefinance.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugin.googlefinance.utils.extensions.parseTrend
import com.kieronquinn.app.smartspacer.plugin.googlefinance.widgets.GoogleFinanceWidget
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.PendingIntent_MUTABLE_FLAGS
import com.kieronquinn.app.smartspacer.sdk.model.Backup
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.expanded.ExpandedState
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import org.koin.android.ext.android.inject
import java.util.LinkedList
import kotlin.math.abs
import android.graphics.drawable.Icon as AndroidIcon

class GoogleFinanceTarget: SmartspacerTargetProvider() {

    private val googleFinanceRepository by inject<GoogleFinanceRepository>()
    private val dataRepository by inject<DataRepository>()
    private val gson by inject<Gson>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val financialWidget = googleFinanceRepository.getFinancialWidget(smartspacerId)
            ?: return emptyList()
        val targetData = getTargetData(smartspacerId)
        if(financialWidget.getHash() == targetData.dismissedAt) return emptyList()
        if(!financialWidget.trend.meetsRequirements(targetData)) return emptyList()
        return listOf(financialWidget.toTarget(smartspacerId, targetData))
    }

    private fun String.meetsRequirements(targetData: TargetData): Boolean {
        val minimumTrend = targetData.minimumTrend ?: return true
        val trend = parseTrend() ?: return true
        return when(targetData.minimumTrendDirection) {
            MinimumTrendDirection.UP -> trend >= minimumTrend
            MinimumTrendDirection.DOWN -> -minimumTrend >= trend
            MinimumTrendDirection.BOTH -> abs(trend) >= minimumTrend
        }
    }

    private fun FinancialWidget.toTarget(
        smartspacerId: String,
        targetData: TargetData
    ): SmartspaceTarget {
        return TargetTemplate.Image(
            provideContext(),
            "finance_${smartspacerId}_at_${System.currentTimeMillis()}",
            ComponentName(provideContext(), GoogleFinanceTarget::class.java),
            SmartspaceTarget.FEATURE_COMMUTE_TIME,
            Text(name),
            Text(resources.getString(
                R.string.target_subtitle,
                pricePrefix,
                priceFirst,
                priceSecond,
                trend
            )),
            Icon(AndroidIcon.createWithBitmap(direction)),
            Icon(AndroidIcon.createWithBitmap(chart), shouldTint = false),
            getTapAction()
        ).create().apply {
            expandedState = ExpandedState(
                remoteViews = createRemoteViews(targetData)?.let { ExpandedState.RemoteViews(it) }
            )
        }
    }

    private fun FinancialWidget.getTapAction(): TapAction {
        return TapAction(intent = name.getTapIntent())
    }

    private fun FinancialWidget.FinancialItem.getClickPendingIntent(): PendingIntent {
        val intent = name.getTapIntent()
        return PendingIntent.getActivity(
            provideContext(),
            name.hashCode(),
            intent,
            PendingIntent_MUTABLE_FLAGS
        )
    }

    private fun String.getTapIntent(): Intent {
        val searchTerm = if(!contains("/")) {
            "\$$this"
        }else this
        return Intent(Intent.ACTION_WEB_SEARCH).apply {
            `package` = PACKAGE_NAME
            putExtra(SearchManager.QUERY, searchTerm)
        }
    }

    private fun FinancialWidget.createRemoteViews(
        targetData: TargetData
    ): RemoteViews? = with(provideContext()) {
        val filteredItems = if(targetData.minimumTrend != null && targetData.filterExpanded) {
            financialItems.filter { it.trend.meetsRequirements(targetData) }
        }else financialItems
        if(filteredItems.isEmpty()) return null
        val items = LinkedList(filteredItems)
        val root = RemoteViews(packageName, R.layout.expanded_state_financial)
        while(items.peek() != null) {
            val firstItem = items.pop()
            val secondItem = if(items.peek() != null) {
                items.pop()
            }else null
            val row = RemoteViews(packageName, R.layout.row_expanded_state_financial)
            val itemOne = RemoteViews(packageName, R.layout.item_expanded_state_financial)
            itemOne.applyFinancialItem(firstItem)
            row.addView(R.id.row_expanded_state_financial_item_1, itemOne)
            if(secondItem != null) {
                val itemTwo = RemoteViews(packageName, R.layout.item_expanded_state_financial)
                itemTwo.applyFinancialItem(secondItem)
                row.addView(R.id.row_expanded_state_financial_item_2, itemTwo)
                row.setViewVisibility(R.id.row_expanded_state_financial_item_2, View.VISIBLE)
                row.setViewVisibility(R.id.row_expanded_state_financial_space_1, View.GONE)
                row.setViewVisibility(R.id.row_expanded_state_financial_space_2, View.GONE)
            }else{
                row.setViewVisibility(R.id.row_expanded_state_financial_item_2, View.GONE)
                row.setViewVisibility(R.id.row_expanded_state_financial_space_1, View.VISIBLE)
                row.setViewVisibility(R.id.row_expanded_state_financial_space_2, View.VISIBLE)
            }
            root.addView(R.id.expanded_state_financial_container, row)
        }
        root
    }

    private fun RemoteViews.applyFinancialItem(financialItem: FinancialWidget.FinancialItem) {
        setOnClickPendingIntent(R.id.item_root, financialItem.getClickPendingIntent())
        setTextViewText(R.id.price_first, financialItem.priceFirst)
        setTextViewText(R.id.price_second, financialItem.priceSecond)
        setTextViewText(R.id.name, financialItem.name)
        setTextViewText(R.id.trend, financialItem.trend)
        setImageViewBitmap(R.id.direction, financialItem.direction)
    }

    private fun getTargetData(smartspacerId: String): TargetData {
        return dataRepository.getTargetData(smartspacerId, TargetData::class.java) ?: TargetData()
    }

    override fun onProviderRemoved(smartspacerId: String) {
        super.onProviderRemoved(smartspacerId)
        googleFinanceRepository.deleteFinancialWidget(smartspacerId)
        dataRepository.deleteTargetData(smartspacerId)
    }

    override fun createBackup(smartspacerId: String): Backup {
        val targetData = getTargetData(smartspacerId)
        val financialWidget = googleFinanceRepository.getFinancialWidget(smartspacerId)
        val description = if(financialWidget != null) {
            resources.getString(R.string.target_description_set, financialWidget.name)
        }else{
            resources.getString(R.string.target_description)
        }
        return Backup(gson.toJson(targetData), description)
    }

    override fun restoreBackup(smartspacerId: String, backup: Backup): Boolean {
        val targetData = try {
            gson.fromJson(backup.data, TargetData::class.java)
        }catch (e: Exception) {
            null
        } ?: return false
        dataRepository.updateTargetData(
            smartspacerId,
            TargetData::class.java,
            TargetData.TYPE,
            ::onUpdated
        ) {
            targetData
        }
        return true
    }

    override fun getConfig(smartspacerId: String?): Config {
        val financialWidget = smartspacerId?.let { googleFinanceRepository.getFinancialWidget(it) }
        val description = if(financialWidget != null) {
            resources.getString(R.string.target_description_set, financialWidget.name)
        }else{
            resources.getString(R.string.target_description)
        }
        return Config(
            resources.getString(R.string.target_label),
            description,
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_target_icon),
            widgetProvider = GoogleFinanceWidget.AUTHORITY,
            configActivity = createIntent(provideContext(), NavGraphMapping.TARGET_GOOGLE_FINANCE),
            compatibilityState = getCompatibilityState()
        )
    }

    private fun getCompatibilityState(): CompatibilityState {
        return if(GoogleFinanceWidget.getProvider(provideContext()) == null) {
            CompatibilityState.Incompatible(resources.getString(R.string.target_incompatible))
        }else CompatibilityState.Compatible
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        val financialItem = googleFinanceRepository.getFinancialWidget(smartspacerId)
            ?: return false
        dataRepository.updateTargetData(
            smartspacerId,
            TargetData::class.java,
            TargetData.TYPE,
            ::onUpdated
        ) {
            val data = it ?: TargetData()
            data.copy(dismissedAt = financialItem.getHash())
        }
        return true
    }

    private fun onUpdated(context: Context, smartspacerId: String) {
        notifyChange(smartspacerId)
    }

    data class TargetData(
        @SerializedName("minimum_trend")
        val minimumTrend: Double? = null,
        @SerializedName("minimum_trend_direction")
        val minimumTrendDirection: MinimumTrendDirection = MinimumTrendDirection.UP,
        @SerializedName("filter_expanded")
        val filterExpanded: Boolean = false,
        @SerializedName("dismissed_at")
        val dismissedAt: Int? = null
    ) {

        companion object {
            const val TYPE = "finance"
        }

        enum class MinimumTrendDirection(val label: Int) {
            UP(R.string.target_configuration_minimum_trend_direction_up),
            DOWN(R.string.target_configuration_minimum_trend_direction_down),
            BOTH(R.string.target_configuration_minimum_trend_direction_both)
        }

    }

}