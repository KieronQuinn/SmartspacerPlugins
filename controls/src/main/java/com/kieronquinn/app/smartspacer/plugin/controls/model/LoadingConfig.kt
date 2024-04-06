package com.kieronquinn.app.smartspacer.plugin.controls.model

import androidx.annotation.StringRes
import com.kieronquinn.app.smartspacer.plugin.controls.R

enum class LoadingConfig(@StringRes val label: Int, @StringRes val labelAlt: Int) {
    LOADING(
        R.string.configuration_loading_config_loading,
        R.string.configuration_loading_config_loading_alt
    ),
    CACHED(
        R.string.configuration_loading_config_cached,
        R.string.configuration_loading_config_cached_alt
    ),
    HIDDEN(
        R.string.configuration_loading_config_hidden,
        R.string.configuration_loading_config_hidden
    )
}