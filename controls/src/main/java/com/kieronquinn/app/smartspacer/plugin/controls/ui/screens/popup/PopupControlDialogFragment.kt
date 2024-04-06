package com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.popup

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.controls.Control
import android.text.InputType
import android.view.View
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.controls.databinding.DialogInputBinding
import com.kieronquinn.app.smartspacer.plugin.controls.databinding.FragmentPopupControlBinding
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlExtraData
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlTapAction
import com.kieronquinn.app.smartspacer.plugin.controls.model.glide.PackageIcon
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository.ControlState
import com.kieronquinn.app.smartspacer.plugin.controls.ui.activities.PopupControlDialogActivity
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.popup.PopupControlDialogViewModel.ControlsIntent
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.popup.PopupControlDialogViewModel.InteractionRequired
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.popup.PopupControlDialogViewModel.State
import com.kieronquinn.app.smartspacer.plugin.controls.ui.views.ControlView
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.locked
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BoundFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getParcelableExtraCompat
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.app.smartspacer.plugin.shared.utils.whenResumed
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants.EXTRA_SMARTSPACER_ID
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import org.koin.androidx.viewmodel.ext.android.viewModel

class PopupControlDialogFragment: BoundFragment<FragmentPopupControlBinding>(FragmentPopupControlBinding::inflate),
    ControlView.ControlListener {

    companion object {
        private const val EXTRA_COMPONENT_NAME = "component_name"
        private const val EXTRA_CONTROL_ID = "control_id"
        private const val EXTRA_REQUIRES_UNLOCK = "requires_unlock"

        fun createLaunchIntent(
            context: Context,
            controlId: String,
            componentName: ComponentName,
            smartspacerId: String,
            requiresLock: Boolean
        ): Intent {
            return Intent(context, PopupControlDialogActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(EXTRA_SMARTSPACER_ID, smartspacerId)
                putExtra(EXTRA_COMPONENT_NAME, componentName)
                putExtra(EXTRA_CONTROL_ID, controlId)
                putExtra(EXTRA_REQUIRES_UNLOCK, requiresLock)
            }
        }
    }

    private val viewModel by viewModel<PopupControlDialogViewModel>()

    private val glide by lazy {
        Glide.with(requireContext())
    }

    private val smartspacerId by lazy {
        requireActivity().intent.getStringExtra(EXTRA_SMARTSPACER_ID)!!
    }

    private val controlId by lazy {
        requireActivity().intent.getStringExtra(EXTRA_CONTROL_ID)!!
    }

    private val requiresUnlock by lazy {
        requireActivity().intent.getBooleanExtra(EXTRA_REQUIRES_UNLOCK, false)
    }

    private val componentName by lazy {
        requireActivity().intent
            .getParcelableExtraCompat(EXTRA_COMPONENT_NAME, ComponentName::class.java)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClose()
        setupTapOutside()
        setupState()
        setupUserInteraction()
        setupFab()
        setupScreenOffClose()
        viewModel.setup(smartspacerId, controlId, componentName)
        binding.popupControlView.root.setListener(this)
    }

    override fun onDestroyView() {
        binding.popupControlView.root.setListener(null)
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        whenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: State) {
        when(state){
            is State.Loading -> {
                //No-op, should be almost instant
            }
            is State.Loaded -> {
                handleHeader(state.componentName, state.appName)
                handleControlState(state.controlState)
                handleFab(state.controlsIntent)
            }
        }
    }

    private fun setupUserInteraction() {
        whenResumed {
            viewModel.interactionRequiredBus.collect {
                when(it) {
                    is InteractionRequired.Prompt -> {
                        showConfirmationDialog(it.control, it.controlAction, it.controlExtraData)
                    }
                    is InteractionRequired.Password -> {
                        showPasscodeDialog(
                            it.control,
                            it.controlAction,
                            it.controlExtraData,
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        )
                    }
                    is InteractionRequired.PIN -> {
                        showPasscodeDialog(
                            it.control,
                            it.controlAction,
                            it.controlExtraData,
                            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
                        )
                    }
                }
            }
        }
    }

    private fun setupClose() = with(binding.popupControlClose) {
        setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun setupTapOutside() = with(binding.root){
        setOnLongClickListener {
            requireActivity().finish()
            true
        }
    }

    private fun handleHeader(componentName: ComponentName, appName: CharSequence) = with(binding) {
        glide.load(PackageIcon(componentName.packageName)).into(popupControlIcon)
        popupControlTitle.text = appName
    }

    private fun handleControlState(controlState: ControlState) {
        binding.popupControlView.root.setControlState(controlState)
    }

    private fun handleFab(controlsIntent: ControlsIntent) {
        binding.popupControlOpenControls.isVisible = controlsIntent !is ControlsIntent.None
    }

    private fun setupFab() = with(binding.popupControlOpenControls) {
        whenResumed {
            onClicked().collect {
                viewModel.onFabClicked()
            }
        }
    }

    private fun setupScreenOffClose() {
        if(!requiresUnlock) return
        whenResumed {
            requireContext().locked().collect {
                if(it) requireActivity().finish()
            }
        }
    }

    override fun onSetValue(newValue: Float) {
        viewModel.onControlSetValue(newValue)
    }

    override fun onToggle() {
        viewModel.onControlToggle()
    }

    override fun onLongPress() {
        viewModel.onControlLongPress()
    }

    private fun showConfirmationDialog(
        control: Control,
        tapAction: ControlTapAction,
        extraData: ControlExtraData
    ) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setMessage(getString(R.string.controls_confirmation_message, control.title))
            setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                viewModel.onPromptResult(tapAction, extraData.copy(passcode = "ok"))
            }
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }.show()
    }

    private fun showPasscodeDialog(
        control: Control,
        tapAction: ControlTapAction,
        extraData: ControlExtraData,
        inputType: Int
    ) {
        val view = DialogInputBinding.inflate(layoutInflater)
        view.dialogInputEdit.applyMonet()
        view.dialogInputEdit.inputType = inputType
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(getString(R.string.controls_pin_verify, control.title))
            setView(view.root)
            setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                viewModel.onPromptResult(
                    tapAction,
                    extraData.copy(passcode = view.dialogInputEdit.text?.toString() ?: "")
                )
            }
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }.show()
    }

}