package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.base

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.shared.R
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BoundFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.applyBottomNavigationInset
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.onApplyInsets
import com.kieronquinn.app.smartspacer.plugin.tasker.databinding.FragmentSettingsFabBinding
import com.kieronquinn.monetcompat.extensions.views.applyMonet

abstract class BaseSettingsFabFragment: BoundFragment<FragmentSettingsFabBinding>(FragmentSettingsFabBinding::inflate) {

    open val additionalPadding = 0f
    open val disableNestedScrolling = false

    abstract val adapter: BaseSettingsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupLoading()
        setupFab()
    }

    private fun setupRecyclerView() = with(binding.settingsBaseRecyclerView) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@BaseSettingsFabFragment.adapter
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

    private fun setupFab() = with(binding.settingsBaseFab) {
        backgroundTintList = ColorStateList.valueOf(monet.getPrimaryColor(requireContext()))
        val margin = resources.getDimensionPixelSize(R.dimen.margin_16)
        onApplyInsets { view, insets ->
            view.updateLayoutParams<ConstraintLayout.LayoutParams> {
                updateMargins(bottom = insets.getInsets(Type.systemBars()).bottom + margin)
            }
        }
    }

}