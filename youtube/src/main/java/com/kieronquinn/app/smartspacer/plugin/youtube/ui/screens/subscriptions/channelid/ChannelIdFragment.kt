package com.kieronquinn.app.smartspacer.plugin.youtube.ui.screens.subscriptions.channelid

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BaseBottomSheetFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.onApplyInsets
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.onChanged
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.app.smartspacer.plugin.youtube.databinding.FragmentChannelIdBottomSheetBinding
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class ChannelIdFragment: BaseBottomSheetFragment<FragmentChannelIdBottomSheetBinding>(FragmentChannelIdBottomSheetBinding::inflate) {

    companion object {
        private const val REQUEST_KEY = "channel_id"
        private const val KEY_RESULT = "result"

        fun Fragment.setupChannelIdResultListener(callback: (result: String) -> Unit) {
            setFragmentResultListener(REQUEST_KEY) { requestKey, bundle ->
                if(requestKey != REQUEST_KEY) return@setFragmentResultListener
                val result = bundle.getString(KEY_RESULT, "")
                callback.invoke(result)
            }
        }
    }

    private val viewModel by viewModel<ChannelIdViewModel>()
    private val args by navArgs<ChannelIdFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setInitialInput(args.current)
        setupMonet()
        setupInput()
        setupPositive()
        setupNegative()
        setupInsets()
    }

    private fun setupMonet() {
        val accent = monet.getAccentColor(requireContext())
        binding.channelIdInput.applyMonet()
        binding.channelIdEdit.applyMonet()
        binding.channelIdPositive.setTextColor(accent)
        binding.channelIdNegative.setTextColor(accent)
    }

    private fun setupInput() = with(binding.channelIdEdit) {
        setText(viewModel.input)
        whenResumed {
            onChanged().collect {
                viewModel.setInput(it?.toString()?.trim() ?: "")
            }
        }
    }

    private fun setupPositive() = with(binding.channelIdPositive) {
        whenResumed {
            onClicked().collect {
                dismissWithResult(viewModel.input)
            }
        }
    }

    private fun setupNegative() = with(binding.channelIdNegative) {
        whenResumed {
            onClicked().collect {
                viewModel.dismiss()
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