package com.kieronquinn.app.smartspacer.plugin.googlekeep.targets

import android.app.PendingIntent
import android.content.ComponentName
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.googlekeep.R
import com.kieronquinn.app.smartspacer.plugin.googlekeep.model.Note
import com.kieronquinn.app.smartspacer.plugin.googlekeep.receivers.GoogleKeepTargetClickReceiver
import com.kieronquinn.app.smartspacer.plugin.googlekeep.repositories.KeepRepository
import com.kieronquinn.app.smartspacer.plugin.googlekeep.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugin.googlekeep.widgets.GoogleKeepWidget
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.Bitmap_createBlankBitmap
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.PendingIntent_MUTABLE_FLAGS
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getDisplayPortraitWidth
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.takeEllipsised
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.expanded.ExpandedState
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon

class GoogleKeepTarget: SmartspacerTargetProvider() {

    companion object {
        private const val LIST_ITEM_MAX_LENGTH = 15
    }

    private val keepRepository by inject<KeepRepository>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val targetData = getTargetData(smartspacerId) ?: return emptyList()
        return listOfNotNull(targetData.toTarget(smartspacerId))
    }

    override fun getConfig(smartspacerId: String?): Config {
        val targetData = smartspacerId?.let { getTargetData(it) }
        val description = if(targetData?.note != null){
            resources.getString(R.string.target_description, targetData.note.title)
        }else{
            resources.getString(R.string.target_description_unset)
        }
        return Config(
            resources.getString(R.string.target_label),
            description,
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_keep),
            widgetProvider = GoogleKeepWidget.AUTHORITY,
            configActivity = createIntent(provideContext(), NavGraphMapping.TARGET_KEEP),
            allowAddingMoreThanOnce = true,
            compatibilityState = getCompatibilityState()
        )
    }

    private fun getCompatibilityState(): CompatibilityState {
        return if(GoogleKeepWidget.getProvider(provideContext()) == null) {
            CompatibilityState.Incompatible(resources.getString(R.string.target_incompatible))
        }else CompatibilityState.Compatible
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        return false
    }

    override fun onProviderRemoved(smartspacerId: String) {
        super.onProviderRemoved(smartspacerId)
        keepRepository.deleteTargetData(smartspacerId)
    }

    private fun getTargetData(smartspacerId: String): TargetData? {
        return keepRepository.getTargetData(smartspacerId)
    }

    private fun TargetData.toTarget(smartspacerId: String): SmartspaceTarget? {
        return when(note) {
            null -> return null
            is Note.RegularNote -> note.toTarget(smartspacerId)
            is Note.ListNote -> note.toTarget(smartspacerId, this)
        }?.apply {
            canBeDismissed = false
        }
    }

    private fun Note.RegularNote.toTarget(smartspacerId: String): SmartspaceTarget {
        return TargetTemplate.Basic(
            getId(smartspacerId),
            ComponentName(provideContext(), GoogleKeepTarget::class.java),
            SmartspaceTarget.FEATURE_UNDEFINED,
            Text(title.ifBlank { resources.getString(R.string.target_untitled) }),
            Text(content.trimToSingleLine().ifBlank { resources.getString(R.string.target_label) }),
            Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_keep)),
            getTapAction(smartspacerId)
        ).create().apply {
            expandedState = getExpandedState(smartspacerId)
        }
    }

    private fun Note.ListNote.toTarget(
        smartspacerId: String,
        targetData: TargetData
    ): SmartspaceTarget? {
        val size = items.size
        val items = items.filter(targetData).take(3)
        if(items.isEmpty() && targetData.hideIfEmpty) return null
        return TargetTemplate.ListItems(
            getId(smartspacerId),
            ComponentName(provideContext(), GoogleKeepTarget::class.java),
            provideContext(),
            Text(title.ifBlank { resources.getString(R.string.target_untitled) }),
            Text(resources.getQuantityString(R.plurals.target_list_content, size, size)),
            Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_keep)),
            items.map {
                Text(it.getText().takeEllipsised(LIST_ITEM_MAX_LENGTH))
            }.ifEmpty {
                listOf(Text(resources.getString(R.string.target_list_empty)))
            },
            Icon(AndroidIcon.createWithBitmap(Bitmap_createBlankBitmap())),
            Text(resources.getString(R.string.target_list_empty)),
            getTapAction(smartspacerId)
        ).create().apply {
            expandedState = getExpandedState(smartspacerId)
        }
    }

    private fun Note.getId(smartspacerId: String): String {
        return "${smartspacerId}_${type.name}_$appWidgetId"
    }

    private fun getExpandedState(smartspacerId: String): ExpandedState? {
        return ExpandedState(
            widget = ExpandedState.Widget(
                GoogleKeepWidget.getProvider(provideContext()) ?: return null,
                smartspacerId,
                width = provideContext().getDisplayPortraitWidth()
            )
        )
    }

    private fun List<Note.ListNote.Item>.filter(targetData: TargetData): List<Note.ListNote.Item> {
        if(targetData.showIndented) return this
        return filterNot { it.isIndented }
    }

    private fun Note.ListNote.Item.getText(): String {
        return if(isIndented){
            "\t$content"
        }else content
    }

    private fun Note.getTapAction(smartspacerId: String): TapAction {
        val intent = GoogleKeepTargetClickReceiver.createIntent(provideContext(), smartspacerId)
        val pendingIntent = PendingIntent.getBroadcast(
            provideContext(),
            appWidgetId,
            intent,
            PendingIntent_MUTABLE_FLAGS
        )
        return TapAction(pendingIntent = pendingIntent)
    }

    private fun String.trimToSingleLine(): String {
        return lines().firstOrNull() ?: ""
    }

    data class TargetData(
        @SerializedName("note")
        val note: Note? = null,
        @SerializedName("show_indented")
        val showIndented: Boolean = true,
        @SerializedName("hide_if_empty")
        val hideIfEmpty: Boolean = false
    ) {
        companion object {
            const val TYPE = "note"
        }
    }

}