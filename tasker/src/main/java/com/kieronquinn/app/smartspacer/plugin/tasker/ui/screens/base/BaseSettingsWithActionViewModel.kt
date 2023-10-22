package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.base

import android.graphics.drawable.Drawable
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItemType

interface BaseSettingsWithActionViewModel {

    sealed class SettingsWithActionItem(val type: ItemType): BaseSettingsItem(type) {

        data class SettingWithAction(
            val title: CharSequence,
            val subtitle: CharSequence,
            val icon: Drawable?,
            val actionIcon: Drawable?,
            val tintIcon: Boolean = true,
            val isEnabled: Boolean = true,
            val onLongClick: (() -> Unit)? = null,
            val onActionClicked: (() -> Unit)? = null,
            val onClick: () -> Unit
        ): SettingsWithActionItem(ItemType.SETTING_WITH_ACTION)

        enum class ItemType: BaseSettingsItemType {
            SETTING_WITH_ACTION
        }
    }
    
}