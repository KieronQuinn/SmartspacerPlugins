package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.app.smartspacer.plugin.tasker.databinding.ItemAppPickerBinding
import com.kieronquinn.app.smartspacer.plugin.tasker.model.glide.PackageIcon
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.PackageRepository.ListAppsApp
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.app.AppPickerAdapter.ViewHolder

class AppPickerAdapter(
    recyclerView: LifecycleAwareRecyclerView,
    var items: List<ListAppsApp>,
    private val onItemClicked: (ListAppsApp) -> Unit
): LifecycleAwareRecyclerView.Adapter<ViewHolder>(recyclerView) {

    init {
        setHasStableIds(true)
    }

    private val layoutInflater = LayoutInflater.from(recyclerView.context)
    private val glide = Glide.with(recyclerView.context)

    override fun getItemId(position: Int): Long {
        return items[position].packageName.hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAppPickerBinding.inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder.binding) {
        val item = items[position]
        itemAppPickerAppLabel.text = item.label
        itemAppPickerAppPackage.text = item.packageName
        itemAppPickerAppPackage.isVisible = item.showPackageName
        itemAppPickerAppIcon
        glide.load(PackageIcon(item.packageName))
            .placeholder(itemAppPickerAppIcon.drawable)
            .into(itemAppPickerAppIcon)
        holder.whenResumed {
            root.onClicked().collect {
                onItemClicked(item)
            }
        }
        Unit
    }

    data class ViewHolder(val binding: ItemAppPickerBinding):
        LifecycleAwareRecyclerView.ViewHolder(binding.root)

}