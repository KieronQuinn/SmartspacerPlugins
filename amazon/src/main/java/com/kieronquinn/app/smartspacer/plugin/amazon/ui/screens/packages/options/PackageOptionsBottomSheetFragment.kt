package com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.packages.options

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.smartspacer.plugin.amazon.R
import com.kieronquinn.app.smartspacer.plugin.amazon.databinding.FragmentPackageOptionsBottomSheetBinding
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BaseBottomSheetFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.onApplyInsets
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class PackageOptionsBottomSheetFragment: BaseBottomSheetFragment<FragmentPackageOptionsBottomSheetBinding>(FragmentPackageOptionsBottomSheetBinding::inflate) {

    private val args by navArgs<PackageOptionsBottomSheetFragmentArgs>()
    private val viewModel by viewModel<PackageOptionsBottomSheetViewModel>()

    private val adapter by lazy {
        Adapter()
    }

    private val delivery by lazy {
        args.delivery as AmazonDelivery.Delivery
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInsets()
        setupRecyclerView()
        setupClose()
        adapter.update(getItems(), binding.packageOptionsRecyclerview)
    }

    private fun setupRecyclerView() = with(binding.packageOptionsRecyclerview) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@PackageOptionsBottomSheetFragment.adapter
    }

    private fun setupClose() = with(binding.packageOptionsPositive) {
        val accent = monet.getAccentColor(requireContext())
        setTextColor(accent)
        whenResumed {
            onClicked().collect {
                dismiss()
            }
        }
    }

    private fun setupInsets() = with(binding.root) {
        val padding = resources.getDimensionPixelSize(SharedR.dimen.margin_16)
        onApplyInsets { view, insets ->
            view.updatePadding(
                bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom + padding
            )
        }
    }

    private fun getItems(): List<BaseSettingsItem> {
        return listOfNotNull(
            Setting(
                getString(R.string.target_configuration_settings_bottom_sheet_dismiss),
                getString(R.string.target_configuration_settings_bottom_sheet_dismiss_content),
                ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_close)
            ){
                viewModel.onDismissClicked(delivery)
            }.takeIf { !delivery.isDismissed() },
            Setting(
                getString(R.string.target_configuration_settings_bottom_sheet_undismiss),
                getString(R.string.target_configuration_settings_bottom_sheet_undismiss_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_undismiss)
            ){
                viewModel.onUndismissClicked(delivery)
            }.takeIf { delivery.isDismissed() },
            Setting(
                getString(R.string.target_configuration_settings_bottom_sheet_unlink),
                getString(R.string.target_configuration_settings_bottom_sheet_unlink_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_unlink)
            ){
                viewModel.onUnlinkClicked(delivery)
            }.takeIf { delivery.trackingId != null }
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.packageOptionsRecyclerview, emptyList())

}