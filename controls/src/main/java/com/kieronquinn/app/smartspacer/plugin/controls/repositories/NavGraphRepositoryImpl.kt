package com.kieronquinn.app.smartspacer.plugin.controls.repositories

import com.kieronquinn.app.smartspacer.plugin.controls.ui.activities.ConfigurationActivity
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository

class NavGraphRepositoryImpl: NavGraphRepository {

    override fun getNavGraph(className: String): NavGraphRepository.NavGraphMapping? {
        return ConfigurationActivity.NavGraphMapping.values().firstOrNull {
            it.className == className
        }
    }
    
}