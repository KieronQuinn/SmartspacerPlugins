package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string

import android.os.Bundle
import android.os.Parcelable
import android.text.InputType
import android.text.format.DateFormat
import android.view.View
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BaseBottomSheetFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.onApplyInsets
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.onChanged
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.databinding.FragmentStringInputBottomSheetBinding
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.DateTimeFormatter
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.DateTimeFormatter.Companion.format
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.Instant
import java.time.ZoneId
import com.kieronquinn.app.shared.R as SharedR

class StringInputFragment: BaseBottomSheetFragment<FragmentStringInputBottomSheetBinding>(FragmentStringInputBottomSheetBinding::inflate) {

    companion object {
        private const val KEY_RESULT = "result"

        fun Fragment.setupStringResultListener(key: String, callback: (result: String) -> Unit) {
            setFragmentResultListener(key) { requestKey, bundle ->
                if(requestKey != key) return@setFragmentResultListener
                val result = bundle.getString(KEY_RESULT, "")
                callback.invoke(result)
            }
        }
    }

    private val viewModel by viewModel<StringInputViewModel>()
    private val args by navArgs<StringInputFragmentArgs>()
    
    private val config by lazy {
        args.config as Config
    }

    private val dateTimeFormatter by lazy {
        DateTimeFormatter(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupConfig()
        setupInput()
        setupError()
        setupPositive()
        setupNegative()
        setupNeutral()
        setupInsets()
    }

    private fun setupMonet() {
        val accent = monet.getAccentColor(requireContext())
        binding.stringInputInput.applyMonet()
        binding.stringInputEdit.applyMonet()
        binding.stringInputPositive.setTextColor(accent)
        binding.stringInputNegative.setTextColor(accent)
        binding.stringInputNeutral.setTextColor(accent)
    }

    private fun setupInput() = with(binding.stringInputEdit) {
        setText(viewModel.input)
        updateHelperText(viewModel.input)
        whenResumed {
            onChanged().collect {
                viewModel.setInput(it?.toString()?.trim() ?: "")
                updateHelperText(it?.toString()?.trim() ?: "")
            }
        }
    }

    private fun setupError() = with(binding.stringInputInput) {
        whenResumed {
            viewModel.error.collect { errorRes ->
                isErrorEnabled = errorRes != null
                error = errorRes?.let { getString(it) }
            }
        }
    }

    private fun setupPositive() = with(binding.stringInputPositive) {
        whenResumed {
            onClicked().collect {
                if(viewModel.validate(config.inputValidation)) {
                    dismissWithResult(viewModel.input)
                }
            }
        }
    }

    private fun setupNegative() = with(binding.stringInputNegative) {
        whenResumed {
            onClicked().collect {
                viewModel.dismiss()
            }
        }
    }

    private fun setupNeutral() = with(binding.stringInputNeutral) {
        whenResumed {
            onClicked().collect {
                onNeutralClicked()
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

    private fun dismissWithResult(result: String) {
        setFragmentResult(config.key, bundleOf(KEY_RESULT to result))
        viewModel.dismiss()
    }

    private fun updateHelperText(input: String) = with(binding.stringInputInput) {
        val newHelperText = input.getHelperText()
        isHelperTextEnabled = newHelperText != null
        helperText = newHelperText
    }

    private fun String.getHelperText(): String? {
        return when(config.inputValidation) {
            InputValidation.TIME -> {
                val newTime = toLongOrNull()?.let { Instant.ofEpochMilli(it) } ?: return null
                newTime.format(dateTimeFormatter)
            }
            else -> null
        }
    }

    private fun onNeutralClicked() {
        when(config.neutralAction) {
            NeutralAction.DATE_TIME_PICKER -> {
                showDateTimePicker { binding.stringInputEdit.setText(it.toString()) }
            }
            NeutralAction.REFRESH_PERIOD -> {
                dismissWithResult("")
            }
            null -> {
                //No-op
            }
        }
    }

    private fun showDateTimePicker(result: (Long) -> Unit) {
        showDatePicker {
            showTimePicker(it, result)
        }
    }

    private fun showDatePicker(action: (Long) -> Unit) {
        val current = viewModel.input.toLongOrNull() ?: System.currentTimeMillis()
        MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(config.title))
            .setSelection(current)
            .setPositiveButtonText(android.R.string.ok)
            .setNegativeButtonText(android.R.string.cancel)
            .build().apply {
                addOnPositiveButtonClickListener { action(it) }
            }.show(childFragmentManager, "date_picker")
    }

    private fun showTimePicker(date: Long, action: (Long) -> Unit) {
        val current = viewModel.input.toLongOrNull() ?: System.currentTimeMillis()
        val time = Instant.ofEpochMilli(current).atZone(ZoneId.systemDefault())
        val newDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault())
        val timeFormat = if(DateFormat.is24HourFormat(requireContext())){
            TimeFormat.CLOCK_24H
        }else{
            TimeFormat.CLOCK_12H
        }
        MaterialTimePicker.Builder()
            .setTitleText(getString(config.title))
            .setHour(time.hour)
            .setMinute(time.minute)
            .setTimeFormat(timeFormat)
            .setPositiveButtonText(android.R.string.ok)
            .setNegativeButtonText(android.R.string.cancel)
            .build().apply {
                addOnPositiveButtonClickListener {
                    val newTime = newDate.withHour(hour).withMinute(minute).toInstant()
                        .toEpochMilli()
                    action(newTime)
                }
            }.show(childFragmentManager, "time_picker")
    }

    private fun setupConfig() = with(config) {
        viewModel.setInitialInput(initialContent)
        binding.stringInputTitle.text = getString(title)
        binding.stringInputContent.text = getText(content)
        binding.stringInputEdit.hint = getString(hint)
        binding.stringInputEdit.inputType = inputType
        binding.stringInputInput.suffixText = suffix?.let { getString(it) }
        binding.stringInputNeutral.isVisible = neutralAction != null
        binding.stringInputNeutral.text = neutralAction?.let { getString(it.content) }
    }

    @Parcelize
    data class Config(
        val initialContent: String,
        val key: String,
        @StringRes
        val title: Int,
        @StringRes
        val content: Int,
        @StringRes
        val hint: Int,
        val suffix: Int? = null,
        val inputValidation: InputValidation? = null,
        val inputType: Int = InputType.TYPE_CLASS_TEXT,
        val neutralAction: NeutralAction? = null
    ) : Parcelable

    enum class InputValidation(@StringRes val error: Int) {
        NOT_EMPTY(R.string.input_string_error_empty),
        URL(R.string.input_string_error_url),
        ASPECT_RATIO(R.string.input_string_error_aspect_ratio),
        FRAME_DURATION(R.string.input_string_error_frame_duration),
        WIDTH(R.string.input_string_error_width),
        HEIGHT(R.string.input_string_error_height),
        TIME(R.string.input_string_error_time),
        TASKER_VARIABLE(R.string.input_string_error_variable),
        TEMPERATURE(R.string.input_string_error_temperature),
        REFRESH_PERIOD(R.string.input_string_error_refresh_period),
        TAP_ACTION_ID(R.string.input_string_error_tap_action_id)
    }

    enum class NeutralAction(@StringRes val content: Int) {
        DATE_TIME_PICKER(R.string.input_string_neutral_action_date_time_picker),
        REFRESH_PERIOD(R.string.configuration_target_refresh_period_content_unset)
    }

}