package com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.packages

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.smartspacer.plugin.amazon.databinding.ItemPackageBinding
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.packages.PackagesAdapter.PackagesViewHolder.Package
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.packages.PackagesViewModel.PackagesSettingsItem
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.packages.PackagesViewModel.PackagesSettingsItem.ItemType
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItemType
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onLongClicked
import com.kieronquinn.monetcompat.extensions.views.applyMonet

class PackagesAdapter(
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
            ItemType.PACKAGE -> Package(
                ItemPackageBinding.inflate(layoutInflater, parent, false)
            )
            else -> super.onCreateViewHolder(parent, itemType)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(holder){
            is Package -> {
                val item = items[position] as PackagesSettingsItem.Package
                holder.setup(item)
            }
            else -> super.onBindViewHolder(holder, position)
        }
    }

    private fun Package.setup(item: PackagesSettingsItem.Package) = with(binding) {
        packageLink.applyMonet()
        packageTitle.text = item.delivery.name
        packageStatus.text = item.delivery.message
        packageLink.isVisible = item.delivery.requiresLinkingDelivery()
        packageTrackingIndicator.isVisible = item.isTracking
        if(item.delivery.imageBitmap != null) {
            glide.load(item.delivery.imageBitmap)
                .placeholder(packageImage.drawable)
                .into(packageImage)
        }else{
            packageImage.setImageDrawable(null)
        }
        whenResumed {
            packageClickable.onClicked().collect {
                item.onClicked(item.delivery)
            }
        }
        whenResumed {
            packageClickable.onLongClicked().collect {
                item.onLongClicked(item.delivery)
            }
        }
        whenResumed {
            packageLink.onClicked().collect {
                item.onLinkClicked(item.delivery)
            }
        }
    }

    sealed class PackagesViewHolder(
        override val binding: ViewBinding
    ): ViewHolder(binding) {
        data class Package(override val binding: ItemPackageBinding): PackagesViewHolder(binding)
    }

}