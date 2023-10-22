package com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.screens.configuration.sunset

import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.R
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.screens.configuration.ConfigurationFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class SunsetConfigurationFragment: ConfigurationFragment() {

    override val viewModel by viewModel<SunsetConfigurationViewModel>()

    override fun getTitle(): CharSequence {
        return getString(R.string.complication_sunset_label)
    }

}