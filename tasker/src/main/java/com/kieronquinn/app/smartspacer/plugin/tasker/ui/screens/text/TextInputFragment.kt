package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.text

import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils.TruncateAt
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesBack
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesTitle
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Text
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.Companion.setupStringResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.text.TextInputViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.label
import com.kieronquinn.app.smartspacer.sdk.utils.getParcelableCompat
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class TextInputFragment: BaseSettingsFragment(), BackAvailable, ProvidesTitle, ProvidesBack {

    companion object {
        private const val KEY_RESULT = "result"
        const val REQUEST_KEY_TEXT = "text"

        fun Fragment.setupTextResultListener(key: String, callback: (result: Text) -> Unit) {
            setFragmentResultListener(key) { requestKey, bundle ->
                if(requestKey != key) return@setFragmentResultListener
                val result = bundle.getParcelableCompat(KEY_RESULT, Text::class.java)
                    ?: return@setFragmentResultListener
                callback.invoke(result)
            }
        }
    }

    private val viewModel by viewModel<TextInputViewModel>()
    private val args by navArgs<TextInputFragmentArgs>()

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
        setupListeners()
        viewModel.setup(args.config.input)
    }

    override fun getTitle(): CharSequence {
        return getString(args.config.title)
    }

    override fun onBackPressed(): Boolean {
        val state = viewModel.state.value as? State.Loaded ?: run {
            viewModel.dismiss()
            return true
        }
        setFragmentResult(args.config.key, bundleOf(KEY_RESULT to state.text))
        viewModel.dismiss()
        return true
    }

    private fun setupListeners() {
        setupStringResultListener(REQUEST_KEY_TEXT) {
            viewModel.onTextChanged(it)
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

    private fun handleState(state: State) = with(binding) {
        when(state) {
            is State.Loading -> {
                settingsBaseLoading.isVisible = true
                settingsBaseRecyclerView.isVisible = false
            }
            is State.Loaded -> {
                settingsBaseLoading.isVisible = false
                settingsBaseRecyclerView.isVisible = true
                adapter.update(state.loadItems(), settingsBaseRecyclerView)
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        return listOf(
            Setting(
                getString(R.string.configuration_text_text_title),
                text.text,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_text_content),
                onClick = viewModel::onTextClicked
            ),
            Dropdown(
                getString(R.string.configuration_text_truncate_at_title),
                getString(
                    R.string.configuration_text_truncate_at_content,
                    getString(text.truncateAtType.label() ?: 0)
                ),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_text_truncate_at),
                text.truncateAtType,
                viewModel::onTruncateChanged,
                TruncateAt.values().filterNot { it.label() == null }.toList()
            ) {
                it.label() ?: 0
            }
        )
    }

    @Parcelize
    data class Config(
        @StringRes
        val title: Int,
        val key: String,
        val input: Text
    ): Parcelable

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}