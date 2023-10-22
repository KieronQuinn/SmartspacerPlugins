package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.app

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BoundFragment
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesOverflow
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesTitle
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.onApplyInsets
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.onChanged
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.onEditorActionSent
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.hideIme
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.databinding.FragmentAppPickerBinding
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.PackageRepository.ListAppsApp
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.app.AppPickerViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.Companion.setupStringResultListener
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import com.kieronquinn.app.shared.R as SharedR

class AppPickerFragment: BoundFragment<FragmentAppPickerBinding>(FragmentAppPickerBinding::inflate), BackAvailable, ProvidesTitle, ProvidesOverflow {

    companion object {
        private const val KEY_PACKAGE_NAME = "package_name"
        private const val KEY_LABEL = "label"
        const val REQUEST_KEY_VARIABLE = "variable"

        fun Fragment.setupAppResultListener(
            key: String,
            callback: (packageName: String, label: CharSequence) -> Unit
        ) {
            setFragmentResultListener(key) { requestKey, bundle ->
                if(requestKey != key) return@setFragmentResultListener
                val packageName = bundle.getString(KEY_PACKAGE_NAME)
                    ?: return@setFragmentResultListener
                val label = bundle.getCharSequence(KEY_LABEL)
                    ?: return@setFragmentResultListener
                callback.invoke(packageName, label)
            }
        }
    }

    private val viewModel by viewModel<AppPickerViewModel> {
        parametersOf(config.includeNotLaunchable)
    }

    private val args by navArgs<AppPickerFragmentArgs>()

    private val config by lazy {
        args.config as Config
    }

    private val adapter by lazy {
        AppPickerAdapter(
            binding.appPickerRecyclerView,
            emptyList(),
            ::onAppSelected
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSearch()
        setupSearchClear()
        setupState()
        setupMonet()
        setupListener()
        setupRecyclerView()
    }

    override fun inflateMenu(menuInflater: MenuInflater, menu: Menu) {
        if(config.showVariable) {
            menuInflater.inflate(R.menu.menu_variable, menu)
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.menu_variable -> viewModel.onVariableClicked(config.current)
        }
        return true
    }

    override fun getTitle(): CharSequence {
        return getString(config.title)
    }

    private fun setupListener() {
        setupStringResultListener(REQUEST_KEY_VARIABLE) {
            whenResumed {
                dismiss(it, it)
            }
        }
    }

    private fun setupRecyclerView() = with(binding.appPickerRecyclerView) {
        adapter = this@AppPickerFragment.adapter
        layoutManager = LinearLayoutManager(context)
        val bottomPadding = resources.getDimension(SharedR.dimen.margin_16).toInt()
        onApplyInsets { view, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            view.updatePadding(bottom = bottomPadding + bottomInset)
        }
    }

    private fun setupMonet() {
        binding.appPickerLoading.loadingProgress.applyMonet()
        binding.appPickerSearch.searchBox.applyMonet()
        binding.appPickerSearch.searchBox.backgroundTintList =
            ColorStateList.valueOf(monet.getBackgroundColorSecondary(requireContext())
                ?: monet.getBackgroundColor(requireContext()))
    }

    private fun setupSearch() {
        setSearchText(viewModel.getSearchTerm())
        whenResumed {
            binding.appPickerSearch.searchBox.onEditorActionSent().collect {
                binding.appPickerSearch.searchBox.hideIme()
            }
        }
        whenResumed {
            binding.appPickerSearch.searchBox.onChanged().collect {
                viewModel.setSearchTerm(it?.toString() ?: "")
            }
        }
    }

    private fun setupSearchClear() = whenResumed {
        launch {
            viewModel.showSearchClear.collect {
                binding.appPickerSearch.searchClear.isVisible = it
            }
        }
        launch {
            binding.appPickerSearch.searchClear.onClicked().collect {
                setSearchText("")
            }
        }
    }

    private fun setSearchText(text: CharSequence) {
        binding.appPickerSearch.searchBox.run {
            this.text?.let {
                it.clear()
                it.append(text)
            } ?: setText(text)
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
                binding.appPickerLoading.root.isVisible = true
                binding.appPickerRecyclerView.isVisible = false
                binding.appPickerSearch.root.isVisible = false
            }
            is State.Loaded -> {
                binding.appPickerLoading.root.isVisible = false
                binding.appPickerRecyclerView.isVisible = true
                binding.appPickerSearch.root.isVisible = true
                adapter.items = state.apps
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun onAppSelected(app: ListAppsApp) {
        dismiss(app.packageName, app.label)
    }

    private fun dismiss(packageName: String, label: CharSequence) {
        setFragmentResult(config.key, bundleOf(
            KEY_PACKAGE_NAME to packageName,
            KEY_LABEL to label
        ))
        viewModel.dismiss()
    }

    @Parcelize
    data class Config(
        val key: String,
        val current: String?,
        @StringRes
        val title: Int,
        val includeNotLaunchable: Boolean = false,
        val showVariable: Boolean = true
    ): Parcelable
    
}