package com.kieronquinn.app.smartspacer.plugin.notifications.ui.screens.configuration

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.notifications.R
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.FacebookComplication
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.InstagramComplication
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.ThreadsComplication
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.TwitterComplication
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.WhatsAppComplication
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesTitle
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getSerializableExtraCompat
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import com.kieronquinn.app.shared.R as SharedR

class ConfigurationFragment: BaseSettingsFragment(), BackAvailable, ProvidesTitle {

    companion object {
        private const val EXTRA_CONFIG = "config"

        fun setConfig(intent: Intent, config: Config) {
            intent.putExtra(EXTRA_CONFIG, config)
        }
    }

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    private val config by lazy {
        requireActivity().intent.getSerializableExtraCompat(EXTRA_CONFIG, Config::class.java)!!
    }

    private val viewModel by viewModel<ConfigurationViewModel> {
        parametersOf(config.packageName)
    }

    override fun getTitle(): CharSequence {
        return getString(config.title)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.settingsBaseRecyclerView.isVisible = true
        binding.settingsBaseLoading.isVisible = false
    }

    private fun loadItems(): List<BaseSettingsItem> {
        return listOf(
            GenericSettingsItem.Card(
                ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                getString(R.string.config_info, getString(config.title))
            ),
            GenericSettingsItem.Setting(
                getString(R.string.config_clear_title),
                getString(R.string.config_clear_description, getString(config.title)),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_config_clear),
                onClick = ::onClearClicked
            )
        )
    }

    private fun onClearClicked() {
        viewModel.onClearClicked()
        Toast.makeText(requireContext(), R.string.config_clear_toast, Toast.LENGTH_LONG).show()
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, loadItems())

    enum class Config(@StringRes val title: Int, val packageName: String) {
        FACEBOOK(R.string.complication_facebook_label_short, FacebookComplication.PACKAGE_NAME),
        TWITTER(R.string.complication_twitter_label_short, TwitterComplication.PACKAGE_NAME),
        WHATSAPP(R.string.complication_whatsapp_label_short, WhatsAppComplication.PACKAGE_NAME),
        INSTAGRAM(R.string.complication_instagram_label_short, InstagramComplication.PACKAGE_NAME),
        THREADS(R.string.complication_threads_label_short, ThreadsComplication.PACKAGE_NAME)
    }

}