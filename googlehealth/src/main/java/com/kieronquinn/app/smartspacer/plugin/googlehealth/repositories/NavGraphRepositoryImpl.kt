package com.kieronquinn.app.smartspacer.plugin.googlehealth.repositories

import com.kieronquinn.app.smartspacer.plugin.googlehealth.ui.activities.ConfigurationActivity
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository

class NavGraphRepositoryImpl: NavGraphRepository {

    override fun getNavGraph(className: String): NavGraphRepository.NavGraphMapping? {
        return ConfigurationActivity.NavGraphMapping.entries.firstOrNull {
            it.className == className
        }
    }
    
}