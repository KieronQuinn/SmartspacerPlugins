package com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.packages.options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepository
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import kotlinx.coroutines.launch

abstract class PackageOptionsBottomSheetViewModel: ViewModel() {

    abstract fun onDismissClicked(delivery: AmazonDelivery.Delivery)
    abstract fun onUndismissClicked(delivery: AmazonDelivery.Delivery)
    abstract fun onUnlinkClicked(delivery: AmazonDelivery.Delivery)

}

class PackageOptionsBottomSheetViewModelImpl(
    private val amazonRepository: AmazonRepository,
    private val navigation: ContainerNavigation
): PackageOptionsBottomSheetViewModel() {

    override fun onDismissClicked(delivery: AmazonDelivery.Delivery) {
        viewModelScope.launch {
            amazonRepository.dismissDelivery(delivery.orderId)
            navigation.navigateBack()
        }
    }

    override fun onUndismissClicked(delivery: AmazonDelivery.Delivery) {
        viewModelScope.launch {
            amazonRepository.unDismissDelivery(delivery.orderId)
            navigation.navigateBack()
        }
    }

    override fun onUnlinkClicked(delivery: AmazonDelivery.Delivery) {
        viewModelScope.launch {
            amazonRepository.clearOrderDetails(delivery.orderId)
            navigation.navigateBack()
        }
    }

}