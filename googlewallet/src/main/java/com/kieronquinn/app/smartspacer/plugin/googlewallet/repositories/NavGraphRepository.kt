package com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories

import com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.activities.ConfigurationActivity
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository

class NavGraphRepositoryImpl: NavGraphRepository {

    override fun getNavGraph(className: String): NavGraphRepository.NavGraphMapping? {
        return ConfigurationActivity.NavGraphMapping.values().firstOrNull {
            it.className == className
        }
    }

}