package com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories

import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugins.pokemongo.ui.activities.ConfigurationActivity

class NavGraphRepositoryImpl: NavGraphRepository {

    override fun getNavGraph(className: String): NavGraphRepository.NavGraphMapping? {
        return ConfigurationActivity.NavGraphMapping.values().firstOrNull {
            it.className == className
        }
    }
    
}