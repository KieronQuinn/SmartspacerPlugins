package com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.screens.configuration.sunrise

import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.R
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.screens.configuration.ConfigurationFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class SunriseConfigurationFragment: ConfigurationFragment() {

    override val viewModel by viewModel<SunriseConfigurationViewModel>()

    override fun getTitle(): CharSequence {
        return getString(R.string.complication_sunrise_label)
    }

}