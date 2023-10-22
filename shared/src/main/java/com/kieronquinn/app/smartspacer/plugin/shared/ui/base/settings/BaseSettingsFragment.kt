package com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.shared.R
import com.kieronquinn.app.shared.databinding.FragmentSettingsBaseBinding
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BoundFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.applyBottomNavigationInset
import com.kieronquinn.monetcompat.extensions.views.applyMonet

abstract class BaseSettingsFragment: BoundFragment<FragmentSettingsBaseBinding>(FragmentSettingsBaseBinding::inflate) {

    open val additionalPadding = 0f
    open val disableNestedScrolling = false

    abstract val adapter: BaseSettingsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupLoading()
    }

    private fun setupRecyclerView() = with(binding.settingsBaseRecyclerView) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@BaseSettingsFragment.adapter
        applyBottomNavigationInset(resources.getDimension(R.dimen.margin_16))
        if(additionalPadding != 0f){
            updatePadding(top = additionalPadding.toInt())
        }
        if(disableNestedScrolling){
            isNestedScrollingEnabled = false
        }
    }

    private fun setupLoading() = with(binding.settingsBaseLoadingProgress) {
        applyMonet()
    }

}