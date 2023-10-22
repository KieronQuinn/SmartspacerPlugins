package com.kieronquinn.app.smartspacer.plugin.shared.repositories

interface NavGraphRepository {

    fun getNavGraph(className: String): NavGraphMapping?

    interface NavGraphMapping {
        val className: String
        val graph: Int
    }

}