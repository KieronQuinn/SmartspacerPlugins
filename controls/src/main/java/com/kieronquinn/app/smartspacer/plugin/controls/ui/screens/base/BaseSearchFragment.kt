package com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.base

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.kieronquinn.app.shared.R
import com.kieronquinn.app.smartspacer.plugin.controls.databinding.FragmentSettingsSearchBinding
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BoundFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.applyBottomNavigationInset
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.onChanged
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.onEditorActionSent
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.hideIme
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.launch

abstract class BaseSearchFragment: BoundFragment<FragmentSettingsSearchBinding>(FragmentSettingsSearchBinding::inflate) {

    open val disableNestedScrolling = false

    abstract val adapter: RecyclerView.Adapter<*>
    abstract val viewModel: BaseSearchViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLoading()
        setupSearch()
        setupSearchClear()
        setupMonet()
        whenResumed {
            setupRecyclerView()
        }
    }

    open suspend fun RecyclerView.createLayoutManager(): LayoutManager {
        return LinearLayoutManager(context)
    }

    private suspend fun setupRecyclerView() = with(binding.settingsBaseRecyclerView) {
        layoutManager = createLayoutManager()
        adapter = this@BaseSearchFragment.adapter
        applyBottomNavigationInset(resources.getDimension(R.dimen.margin_16))
        if(disableNestedScrolling){
            isNestedScrollingEnabled = false
        }
    }

    private fun setupMonet() {
        binding.includeSearch.searchBox.applyMonet()
        binding.includeSearch.searchBox.backgroundTintList = ColorStateList.valueOf(
            monet.getBackgroundColorSecondary(requireContext()) ?: monet.getBackgroundColor(
                requireContext()
            )
        )
    }

    private fun setupLoading() = with(binding.settingsBaseLoadingProgress) {
        applyMonet()
    }

    private fun setupSearch() {
        setSearchText(viewModel.getSearchTerm())
        whenResumed {
            binding.includeSearch.searchBox.onEditorActionSent().collect {
                binding.includeSearch.searchBox.hideIme()
            }
        }
        whenResumed {
            binding.includeSearch.searchBox.onChanged().collect {
                viewModel.setSearchTerm(it?.toString() ?: "")
            }
        }
    }

    private fun setupSearchClear() = whenResumed {
        launch {
            viewModel.showSearchClear.collect {
                binding.includeSearch.searchClear.isVisible = it
            }
        }
        launch {
            binding.includeSearch.searchClear.onClicked().collect {
                setSearchText("")
            }
        }
    }

    private fun setSearchText(text: CharSequence) {
        binding.includeSearch.searchBox.run {
            this.text?.let {
                it.clear()
                it.append(text)
            } ?: setText(text)
        }
    }

}