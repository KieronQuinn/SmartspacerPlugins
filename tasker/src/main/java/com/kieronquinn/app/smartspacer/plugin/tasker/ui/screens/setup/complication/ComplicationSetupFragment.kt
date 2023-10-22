package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.setup.complication

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Card
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.base.BaseSettingsFabFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.setup.complication.ComplicationSetupViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.Companion.setupStringResultListener
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants.EXTRA_SMARTSPACER_ID
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class ComplicationSetupFragment: BaseSettingsFabFragment(), BackAvailable {

    companion object {
        private const val REQUEST_KEY_NAME = "name"

        internal val CONFIG_NAME = StringInputFragment.Config(
            "",
            REQUEST_KEY_NAME,
            R.string.complication_setup_name_title,
            R.string.complication_setup_name_content,
            R.string.complication_setup_name_title,
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        )
    }

    private val viewModel by viewModel<ComplicationSetupViewModel>()

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFab()
        setupState()
        setupResult()
        setupError()
        val smartspacerId = requireActivity().intent.getStringExtra(EXTRA_SMARTSPACER_ID)!!
        viewModel.setup(smartspacerId)
    }

    private fun setupFab() = with(binding.settingsBaseFab) {
        text = getString(R.string.save)
        icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_save)
        whenResumed {
            onClicked().collect {
                if(viewModel.onSaveClicked(requireContext())){
                    requireActivity().setResult(Activity.RESULT_OK)
                    requireActivity().finish()
                }
            }
        }
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        whenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun setupResult() {
        setupStringResultListener(REQUEST_KEY_NAME) {
            viewModel.onNameResult(it)
        }
    }

    private fun setupError() {
        whenResumed {
            viewModel.errorBus.collect {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleState(state: State) {
        when(state) {
            is State.Loading -> {
                binding.settingsBaseLoading.isVisible = true
                binding.settingsBaseRecyclerView.isVisible = false
                binding.settingsBaseFab.isVisible = false
            }
            is State.Loaded -> {
                binding.settingsBaseLoading.isVisible = false
                binding.settingsBaseRecyclerView.isVisible = true
                binding.settingsBaseFab.isVisible = true
                adapter.update(state.loadItems(), binding.settingsBaseRecyclerView)
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        val name = if(name.isNullOrEmpty()) {
            getString(R.string.complication_setup_name_content)
        }else name
        return listOf(
            Card(
                ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                getString(R.string.complication_setup_header)
            ),
            Setting(
                getString(R.string.complication_setup_name_title),
                name,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_tasker),
                onClick = viewModel::onNameClicked
            ),
            Setting(
                getString(R.string.complication_setup_name_id_title),
                smartspacerId,
                ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_smartspacer),
                isEnabled = false
            ){}
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}