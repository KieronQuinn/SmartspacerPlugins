package com.kieronquinn.app.smartspacer.plugin.googlehome.repositories

import com.kieronquinn.app.smartspacer.plugin.googlehome.ui.activities.ConfigurationActivity
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository

class NavGraphRepositoryImpl: NavGraphRepository {

    override fun getNavGraph(className: String): NavGraphRepository.NavGraphMapping? {
        return ConfigurationActivity.NavGraphMapping.entries.firstOrNull {
            it.className == className
        }
    }
    
}