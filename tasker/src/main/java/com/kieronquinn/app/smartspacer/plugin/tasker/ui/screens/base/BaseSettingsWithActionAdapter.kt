package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.base

import android.content.res.ColorStateList
import android.graphics.drawable.Animatable
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItemType
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getAttrColor
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onLongClicked
import com.kieronquinn.app.smartspacer.plugin.tasker.databinding.ItemSettingsTextItemWithActionBinding
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.base.BaseSettingsWithActionViewModel.SettingsWithActionItem

abstract class BaseSettingsWithActionAdapter(
    recyclerView: LifecycleAwareRecyclerView,
    override var items: List<BaseSettingsItem>
): BaseSettingsAdapter(recyclerView, items) {

    override fun getItemType(viewType: Int): BaseSettingsItemType {
        return BaseSettingsItemType.findIndex<SettingsWithActionItem.ItemType>(viewType)
            ?: super.getItemType(viewType)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        itemType: BaseSettingsItemType
    ): ViewHolder {
        return when(itemType){
            SettingsWithActionItem.ItemType.SETTING_WITH_ACTION -> SettingsWithActionViewHolder.SettingWithAction(
                ItemSettingsTextItemWithActionBinding.inflate(layoutInflater, parent, false)
            )
            else -> super.onCreateViewHolder(parent, itemType)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(holder){
            is SettingsWithActionViewHolder.SettingWithAction -> {
                val item = items[position] as SettingsWithActionItem.SettingWithAction
                holder.setup(item)
            }
            else -> super.onBindViewHolder(holder, position)
        }
    }

    private fun SettingsWithActionViewHolder.SettingWithAction.setup(
        item: SettingsWithActionItem.SettingWithAction
    ) = with(binding) {
        root.alpha = if(item.isEnabled) 1f else 0.5f
        root.isEnabled = item.isEnabled
        itemSettingsTextTitle.text = item.title
        itemSettingsTextContent.text = item.subtitle
        itemSettingsTextContent.isVisible = item.subtitle.isNotEmpty()
        glide.load(item.icon)
            .placeholder(itemSettingsTextIcon.drawable)
            .into(itemSettingsTextIcon)
        glide.load(item.actionIcon)
            .placeholder(itemSettingsTextAction.drawable)
            .into(itemSettingsTextAction)
        if(item.icon is Animatable) {
            item.icon.start()
        }
        if(item.actionIcon is Animatable) {
            item.actionIcon.start()
        }
        itemSettingsTextSpace.isVisible = item.icon == null
        itemSettingsTextIcon.isVisible = item.icon != null
        itemSettingsTextIcon.imageTintList = if(item.tintIcon){
            ColorStateList.valueOf(root.context.getAttrColor(android.R.attr.colorControlNormal))
        }else null
        if(item.onLongClick != null){
            whenResumed {
                root.onLongClicked().collect {
                    item.onLongClick.invoke()
                }
            }
        }else{
            root.setOnLongClickListener(null)
        }
        whenResumed {
            root.onClicked().collect {
                item.onClick()
            }
        }
        whenResumed {
            itemSettingsTextAction.onClicked().collect {
                item.onActionClicked?.invoke()
            }
        }
    }

    sealed class SettingsWithActionViewHolder(override val binding: ViewBinding): ViewHolder(binding) {
        data class SettingWithAction(override val binding: ItemSettingsTextItemWithActionBinding):
            SettingsWithActionViewHolder(binding)
    }

}