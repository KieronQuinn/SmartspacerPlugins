package com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.title

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import com.kieronquinn.app.smartspacer.plugin.controls.databinding.FragmentCustomTitleBottomSheetBinding
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BaseBottomSheetFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.onApplyInsets
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.onChanged
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class CustomTitleBottomSheetFragment: BaseBottomSheetFragment<FragmentCustomTitleBottomSheetBinding>(FragmentCustomTitleBottomSheetBinding::inflate) {

    companion object {
        private const val REQUEST_KEY = "custom_title"
        private const val KEY_RESULT = "result"

        fun Fragment.setupCustomTitleResultListener(callback: (result: String?) -> Unit) {
            setFragmentResultListener(REQUEST_KEY) { requestKey, bundle ->
                if(requestKey != REQUEST_KEY) return@setFragmentResultListener
                val result = bundle.getString(KEY_RESULT, "").takeIf { it.isNotBlank() }
                callback.invoke(result)
            }
        }
    }

    private val viewModel by viewModel<CustomTitleViewModel>()
    private val args by navArgs<CustomTitleBottomSheetFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setInitialInput(args.current)
        setupMonet()
        setupInput()
        setupPositive()
        setupNegative()
        setupNeutral()
        setupInsets()
    }

    private fun setupMonet() {
        val accent = monet.getAccentColor(requireContext())
        binding.customTitleInput.applyMonet()
        binding.customTitleEdit.applyMonet()
        binding.customTitlePositive.setTextColor(accent)
        binding.customTitleNegative.setTextColor(accent)
        binding.customTitleNeutral.setTextColor(accent)
    }

    private fun setupInput() = with(binding.customTitleEdit) {
        setText(viewModel.input)
        whenResumed {
            onChanged().collect {
                viewModel.setInput(it?.toString()?.trim() ?: "")
            }
        }
    }

    private fun setupPositive() = with(binding.customTitlePositive) {
        whenResumed {
            onClicked().collect {
                dismissWithResult(viewModel.input)
            }
        }
    }

    private fun setupNegative() = with(binding.customTitleNegative) {
        whenResumed {
            onClicked().collect {
                viewModel.dismiss()
            }
        }
    }

    private fun setupNeutral() = with(binding.customTitleNeutral) {
        whenResumed {
            onClicked().collect {
                dismissWithResult(null)
            }
        }
    }

    private fun setupInsets() = with(binding.root) {
        val padding = resources.getDimension(SharedR.dimen.margin_16).toInt()
        onApplyInsets { _, insets ->
            val bottomInset = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            ).bottom
            updatePadding(bottom = bottomInset + padding)
        }
    }

    private fun dismissWithResult(result: String?) {
        setFragmentResult(REQUEST_KEY, bundleOf(KEY_RESULT to result))
        viewModel.dismiss()
    }

}