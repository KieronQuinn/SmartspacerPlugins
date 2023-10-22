package com.kieronquinn.app.smartspacer.plugin.youtube.ui.screens.subscriptions

import android.graphics.Paint
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItemType
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.app.smartspacer.plugin.youtube.databinding.ItemSubscriptionsFooterBinding
import com.kieronquinn.app.smartspacer.plugin.youtube.ui.screens.subscriptions.SubscriptionsConfigurationViewModel.SubscriptionsConfigurationSettingsItem
import com.kieronquinn.app.smartspacer.plugin.youtube.ui.screens.subscriptions.SubscriptionsConfigurationViewModel.SubscriptionsConfigurationSettingsItem.ItemType

class SubscriptionsConfigurationAdapter(
    recyclerView: LifecycleAwareRecyclerView,
    override var items: List<BaseSettingsItem>
): BaseSettingsAdapter(recyclerView, items) {

    override fun getItemType(viewType: Int): BaseSettingsItemType {
        return BaseSettingsItemType.findIndex<ItemType>(viewType) ?: super.getItemType(viewType)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        itemType: BaseSettingsItemType
    ): ViewHolder {
        return when(itemType){
            ItemType.FOOTER -> SubscriptionsConfigurationViewHolder.Footer(
                ItemSubscriptionsFooterBinding.inflate(layoutInflater, parent, false)
            )
            else -> super.onCreateViewHolder(parent, itemType)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(holder){
            is SubscriptionsConfigurationViewHolder.Footer -> {
                val item = items[position] as SubscriptionsConfigurationSettingsItem.Footer
                holder.setup(item)
            }
            else -> super.onBindViewHolder(holder, position)
        }
    }

    private fun SubscriptionsConfigurationViewHolder.Footer.setup(
        item: SubscriptionsConfigurationSettingsItem.Footer
    ) = with(binding.subscriptionsFooterLink) {
        val accent = monet.getAccentColor(context)
        val primary = monet.getPrimaryColor(context)
        background.setTint(primary)
        setTextColor(accent)
        paintFlags = paintFlags or Paint.ANTI_ALIAS_FLAG or Paint.UNDERLINE_TEXT_FLAG
        whenResumed {
            onClicked().collect {
                item.onLinkClicked()
            }
        }
    }

    sealed class SubscriptionsConfigurationViewHolder(
        override val binding: ViewBinding
    ): ViewHolder(binding) {
        data class Footer(override val binding: ItemSubscriptionsFooterBinding):
            SubscriptionsConfigurationViewHolder(binding)
    }

}