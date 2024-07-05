package com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.popup

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.google.internal.tapandpay.v1.valuables.CommonProto.BarcodeType
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.kieronquinn.app.smartspacer.plugin.googlewallet.R
import com.kieronquinn.app.smartspacer.plugin.googlewallet.databinding.FragmentPopupWalletBinding
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository.Valuable
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.activities.PopupWalletDialogActivity
import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.popup.PopupWalletDialogViewModel.State
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.toBitmap
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions.toColour
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BoundFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.awaitPost
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.isColorDark
import com.kieronquinn.app.smartspacer.plugin.shared.utils.whenResumed
import org.koin.androidx.viewmodel.ext.android.viewModel

class PopupWalletDialogFragment: BoundFragment<FragmentPopupWalletBinding>(FragmentPopupWalletBinding::inflate) {

    companion object {
        private const val EXTRA_VALUABLE_ID = "valuable_id"
        private const val EXTRA_LOCK_ORIENTATION = "lock_orientation"

        fun createLaunchIntent(context: Context, valuableId: String, lockOrientation: Boolean): Intent {
            return Intent(context, PopupWalletDialogActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(EXTRA_VALUABLE_ID, valuableId)
                putExtra(EXTRA_LOCK_ORIENTATION, lockOrientation)
            }
        }
    }

    private val viewModel by viewModel<PopupWalletDialogViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClose()
        setupTapOutside()
        setupState()
        viewModel.setupWithId(requireActivity().intent.getStringExtra(EXTRA_VALUABLE_ID)!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(requireActivity().intent.getBooleanExtra(EXTRA_LOCK_ORIENTATION, false)) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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

    private fun handleState(state: State) {
        when(state){
            is State.Loading -> {
                //No-op, should be almost instant
            }
            is State.Loaded -> {
                val card = state.valuable
                setupContainer(card)
                setupTitle(card)
                setupLogo(card)
                setupLabel(card)
                setupCard(card)
                setupCode(card)
                setupCardText(card)
                setupFab(card)
            }
            is State.Error -> {
                requireActivity().finish()
            }
        }
    }

    private fun setupClose() = with(binding.popupWalletClose) {
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

    private fun setupContainer(card: Valuable) = with(binding.popupWalletCodeContainer) {
        updateLayoutParams<ConstraintLayout.LayoutParams> {
            if(card.isCodeSquare()){
                val qrSize = resources.getDimension(R.dimen.popup_wallet_dialog_qr_size).toInt()
                width = qrSize
                height = qrSize
            }else{
                height = resources.getDimension(R.dimen.popup_wallet_dialog_barcode_height).toInt()
            }
        }
    }

    private fun setupTitle(card: Valuable) = with(binding.popupWalletTitle) {
        text = card.getGroupingInfo()?.groupingTitle
        setTextColor(card.getTextColor())
    }

    private fun setupLogo(card: Valuable) = with(binding.popupWalletLogo) {
        card.image?.toBitmap()?.let {
            setImageBitmap(it)
        } ?: run {
            setupLogoLetter(card)
        }
        (foreground as? GradientDrawable)?.setStroke(
            resources.getDimension(R.dimen.popup_wallet_dialog_logo_outline).toInt(), card.getTextColor()
        )
    }

    private fun setupLogoLetter(card: Valuable) = with(binding.popupWalletLogoLetter) {
        var label = card.getIssuerInfo()?.issuerName
        if(label.isNullOrEmpty()){
            label = card.getGroupingInfo()?.groupingTitle
        }
        text = label?.substring(0, 1)
        setTextColor(card.getTextColor())
    }

    private fun setupLabel(card: Valuable) = with(binding.popupWalletLabel) {
        text = card.getGroupingInfo()?.groupingSubtitle
        setTextColor(card.getTextColor())
    }

    private fun setupCard(card: Valuable) = with(binding.popupWalletCard) {
        backgroundTintList = card.getGroupingInfo()?.backgroundColor?.toColour()
            ?.let { ColorStateList.valueOf(it) }
    }

    private fun setupCardText(card: Valuable) = with(binding.popupWalletCardText) {
        text = card.getRedemptionInfo()?.barcode?.displayText
        setTextColor(card.getTextColor())
    }

    private fun setupFab(card: Valuable) = with(binding.popupWalletOpenInPay) {
        backgroundTintList = card.getGroupingInfo()?.backgroundColor?.toColour()
            ?.let { ColorStateList.valueOf(it) }
        setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(
                    "https://pay.google.com/gp/v/valuable/${card.id}?vs=gp_lp"
                )
            })
        }
        setTextColor(card.getTextColor())
        iconTint = ColorStateList.valueOf(card.getTextColor())
    }

    private fun setupCode(card: Valuable) = with(binding.popupWalletCodeImage) {
        whenResumed {
            awaitPost()
            generateCode(card, measuredWidth, measuredHeight)?.let {
                setImageBitmap(it)
            } ?: run {
                binding.popupWalletCodeContainer.isVisible = false
            }
        }
    }

    private fun generateCode(card: Valuable, width: Int, height: Int): Bitmap? {
        val barcode = card.getRedemptionInfo()?.barcode ?: return null
        runCatching {
            val writer = MultiFormatWriter()
            val bitMatrix = writer.encode(
                barcode.encodedValue, barcode.type.toBarcodeFormat(), width, height
            )
            val barcodeEncoder = BarcodeEncoder()
            return barcodeEncoder.createBitmap(bitMatrix)
        }
        return null
    }

    private fun Valuable.isBackgroundDark(): Boolean {
        return getGroupingInfo()?.backgroundColor?.toColour()?.isColorDark() ?: false
    }

    private fun Valuable.isCodeSquare(): Boolean {
        return getRedemptionInfo()?.barcode?.type?.let {
            it == BarcodeType.BARCODE_TYPE_AZTEC || it == BarcodeType.BARCODE_TYPE_QR_CODE
        } ?: false
    }

    private fun Valuable.getTextColor(): Int {
        return if(isBackgroundDark()) Color.WHITE else Color.BLACK
    }

    private fun BarcodeType.toBarcodeFormat(): BarcodeFormat? {
        return when(this) {
            BarcodeType.BARCODE_TYPE_UNSPECIFIED -> null
            BarcodeType.BARCODE_TYPE_AZTEC -> BarcodeFormat.AZTEC
            BarcodeType.BARCODE_TYPE_CODE_39 -> BarcodeFormat.CODE_39
            BarcodeType.BARCODE_TYPE_CODABAR -> BarcodeFormat.CODABAR
            BarcodeType.BARCODE_TYPE_DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
            BarcodeType.BARCODE_TYPE_CODE_128 -> BarcodeFormat.CODE_128
            BarcodeType.BARCODE_TYPE_EAN_8 -> BarcodeFormat.EAN_8
            BarcodeType.BARCODE_TYPE_EAN_13 -> BarcodeFormat.EAN_13
            BarcodeType.BARCODE_TYPE_ITF_14 -> BarcodeFormat.ITF
            BarcodeType.BARCODE_TYPE_PDF_417 -> BarcodeFormat.PDF_417
            BarcodeType.BARCODE_TYPE_QR_CODE -> BarcodeFormat.QR_CODE
            BarcodeType.BARCODE_TYPE_UPC_A -> BarcodeFormat.UPC_A
            BarcodeType.BARCODE_TYPE_UPC_E -> BarcodeFormat.UPC_E
            BarcodeType.BARCODE_TYPE_TEXT_ONLY -> null
            BarcodeType.BARCODE_TYPE_UNRECOGNIZED -> null
            else -> null
        }
    }

}