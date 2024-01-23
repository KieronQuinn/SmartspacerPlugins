package com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.kieronquinn.app.shared.R
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BoundFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.applyBottomNavigationInset
import com.kieronquinn.monetcompat.extensions.views.applyMonet

abstract class BaseFragment<V: ViewBinding>(private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> V): BoundFragment<V>(inflate) {

    open val additionalPadding = 0f
    open val disableNestedScrolling = false

    abstract val adapter: BaseSettingsAdapter

    abstract val recyclerView: LifecycleAwareRecyclerView
    abstract val loadingView: LinearProgressIndicator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupLoading()
    }

    private fun setupRecyclerView() = with(recyclerView) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@BaseFragment.adapter
        applyBottomNavigationInset(resources.getDimension(R.dimen.margin_16))
        if(additionalPadding != 0f){
            updatePadding(top = additionalPadding.toInt())
        }
        if(disableNestedScrolling){
            isNestedScrollingEnabled = false
        }
    }

    private fun setupLoading() = with(loadingView) {
        applyMonet()
    }

}