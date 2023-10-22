package com.kieronquinn.app.smartspacer.plugin.tasker.repositories

import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getInstalledApplications
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.queryIntentActivitiesCompat
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.PackageRepository.ListAppsApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface PackageRepository {

    suspend fun getInstalledApps(includeNotLaunchable: Boolean = false): List<ListAppsApp>

    data class ListAppsApp(
        val packageName: String,
        val label: CharSequence,
        val showPackageName: Boolean = false
    )

}

class PackageRepositoryImpl(private val context: Context): PackageRepository {

    override suspend fun getInstalledApps(includeNotLaunchable: Boolean): List<ListAppsApp> {
        return withContext(Dispatchers.IO) {
            if(includeNotLaunchable){
                getAllApps()
            }else{
                getLaunchableApps()
            }
        }
    }

    private fun getLaunchableApps(): List<ListAppsApp> {
        val packageManager = context.packageManager
        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return packageManager.queryIntentActivitiesCompat(launchIntent, 0).mapNotNull {
            val label = it.loadLabel(packageManager)?.trim() ?: return@mapNotNull null
            Pair(it.activityInfo.packageName, label)
        }.sortApps()
    }

    private fun getAllApps(): List<ListAppsApp> {
        val packageManager = context.packageManager
        return packageManager.getInstalledApplications().map {
            val label = it.loadLabel(packageManager).trim()
            Pair(it.packageName, label)
        }.sortApps()
    }

    private fun List<Pair<String, CharSequence>>.sortApps(): List<ListAppsApp> {
        return map {
            ListAppsApp(it.first, it.second)
        }.sortedBy {
            it.label.toString().lowercase()
        }.let { apps ->
            //Show the package name if there's multiple
            apps.map { app ->
                if(apps.containsIdenticalLabel(app.label)) {
                    app.copy(showPackageName = true)
                }else app
            }
        }
    }

    /**
     *  If there's multiple apps with the same label (ignoring whitespace and case), we want to
     *  show the package name to differentiate between them, so count labels which match this
     *  similarity and return if there's more than one (ie. there are multiple similar apps)
     */
    private fun List<ListAppsApp>.containsIdenticalLabel(label: CharSequence): Boolean {
        val formattedLabel = label.toString().lowercase().trim()
        val count = count {
            it.label.toString().lowercase().trim() == formattedLabel
        }
        return count > 1
    }

}