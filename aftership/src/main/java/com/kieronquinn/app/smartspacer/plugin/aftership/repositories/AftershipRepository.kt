package com.kieronquinn.app.smartspacer.plugin.aftership.repositories

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import androidx.core.graphics.scale
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.kieronquinn.app.shared.R
import com.kieronquinn.app.shared.maps.generateGoogleMap
import com.kieronquinn.app.smartspacer.plugin.aftership.AftershipPlugin
import com.kieronquinn.app.smartspacer.plugin.aftership.model.BitmapWrapper
import com.kieronquinn.app.smartspacer.plugin.aftership.model.WidgetListItem
import com.kieronquinn.app.smartspacer.plugin.aftership.model.database.Package
import com.kieronquinn.app.smartspacer.plugin.aftership.model.database.Package.Status
import com.kieronquinn.app.smartspacer.plugin.aftership.targets.AftershipTarget
import com.kieronquinn.app.smartspacer.plugin.aftership.utils.extensions.makeSquare
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.firstNotNull
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getApplicationInfo
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.getSerializableCompat
import dalvik.system.PathClassLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.Serializable

interface AftershipRepository {

    fun loadAdapterItems(items: List<WidgetListItem>)
    fun updateAdapterItems()
    fun getActivePackages(): List<Package>
    fun dismissPackage(id: String)
    fun clearDismissedPackages()
    fun getTrackingUrl(pkg: Package): String?

}

class AftershipRepositoryImpl(
    private val context: Context,
    private val trackingRepository: TrackingRepository,
    private val databaseRepository: DatabaseRepository
): AftershipRepository {

    companion object {
        private const val EXTRA_WIDGET_LIST_ITEM = "extra_widget_list_item"
        private const val URL_PREFIX = "https://m.aftership.com/"
        private const val URL_TRACKING_PREFIX = "https://track.aftership.com/"
        private val REGEX_ID = ".*feedId=(.*), feedKindEnum.*".toRegex()
        private val REGEX_TRACKING_NUM = ".*trackingNum=(.*), checkPointStatus.*".toRegex()
        private val REGEX_CATE_TYPE = ".*cateType=(.*), brand.*".toRegex()
        private val REGEX_COURIER_ENTITY_SLUG = ".*slug=(.*), name.*".toRegex()
        private const val MAP_WIDTH = 768
        private const val MAP_HEIGHT = 432
        private const val MARKER_SIZE = 48
    }

    private val scope = MainScope()
    private val cacheDir = context.cacheDir

    private val packages = databaseRepository
        .getPackages()
        .debounce(250L)
        .onEach {
            onPackagesChanged()
        }
        .stateIn(scope, SharingStarted.Eagerly, null)

    private val mapPadding = context.resources.getDimensionPixelSize(R.dimen.margin_16).let {
        Rect(it, 0, 0, 0)
    }

    override fun loadAdapterItems(items: List<WidgetListItem>) {
        scope.launch(Dispatchers.IO) {
            val classLoader = context.getClassLoaderForPackage(AftershipPlugin.PACKAGE_NAME)
                ?: return@launch
            val currentPackages = packages.firstNotNull()
            val packages = items.mapNotNull {
                it.loadPackage(classLoader, currentPackages)
            }
            val removed = currentPackages.filterNot {
                packages.any { p -> p.id == it.id }
            }
            removed.forEach {
                databaseRepository.deletePackage(it)
            }
            packages.forEach {
                databaseRepository.addPackage(it)
            }
        }
    }

    override fun updateAdapterItems() {
        scope.launch {
            val packages = packages.firstNotNull().map {
                it.update()
            }
            packages.forEach {
                databaseRepository.addPackage(it)
            }
        }
    }

    private suspend fun WidgetListItem.loadPackage(
        classLoader: ClassLoader,
        currentPackages: List<Package>
    ): Package? {
        bundle.classLoader = classLoader
        val item = bundle.getSerializableCompat(
            EXTRA_WIDGET_LIST_ITEM, Serializable::class.java
        ) ?: return null
        val id = item.toString().getFirst(REGEX_ID) ?: return null
        val current = currentPackages.firstOrNull { it.id == id }
        val trackingNum = item.toString().getFirst(REGEX_TRACKING_NUM)
        val cateType = item.toString().getFirst(REGEX_CATE_TYPE)?.let { cate ->
            Status.values().firstOrNull { it.name == cate }
        } ?: return null
        if(cateType == Status.DELIVERED && current?.status == Status.DELIVERED){
            //Package was already delivered and we've updated to handle that, return the current
            return current
        }
        val slug = item.findCourierEntity()?.toString()
            ?.getFirst(REGEX_COURIER_ENTITY_SLUG)
        val url = if(slug != null && trackingNum != null) {
            "$URL_PREFIX$slug/$trackingNum"
        }else null
        val tracking = if(url != null){
            trackingRepository.getTrackingInfo(url)
        }else null
        val icon = BitmapWrapper.create(getImagePath("${id}_icon.png"), icon)
        val image = image?.makeSquare()?.let {
            BitmapWrapper.create(getImagePath("${id}_image.png"), it)
        }
        val map = tracking?.let {
            generateGoogleMap(it, this.icon)
        }?.let {
            BitmapWrapper.create(getImagePath("${id}_map.png"), it)
        }
        return Package(
            id,
            0,
            title,
            courier,
            state,
            icon,
            image ?: current?.image, //Don't remove the image if it has been lost
            map,
            cateType,
            url,
            tracking,
            current?.dismissedAt
        ).withChangedAt().let {
            //If a new package is already delivered, automatically dismiss it
            if(current == null && cateType == Status.DELIVERED) {
                it.copy(dismissedAt = it.changedAt)
            }else it
        }
    }

    private suspend fun Package.update(): Package {
        //No more updates will be found
        if(status == Status.DELIVERED) return this
        val tracking = if(trackingUrl != null){
            trackingRepository.getTrackingInfo(trackingUrl)
        }else null
        val map = tracking?.let {
            generateGoogleMap(it, this.icon.bitmap ?: return this)
        }?.let {
            BitmapWrapper.create(getImagePath("${id}_map.png"), it)
        }
        return copy(tracking = tracking, map = map)
    }

    private fun Package.withChangedAt(): Package {
        return copy(changedAt = getNormalisedHashCode())
    }

    private suspend fun generateGoogleMap(
        tracking: Package.Tracking,
        icon: Bitmap
    ): Bitmap? {
        val latLng = if(tracking.latitude != null && tracking.longitude != null){
            LatLng(tracking.latitude, tracking.longitude)
        } else return null
        val marker = icon.scale(MARKER_SIZE, MARKER_SIZE)
        return context.generateGoogleMap(MAP_WIDTH, MAP_HEIGHT, mapPadding, 7f) {
            listOf(
                MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(marker))
            )
        }
    }

    private fun Context.getClassLoaderForPackage(packageName: String): ClassLoader? {
        val sourceDir = try {
            packageManager.getApplicationInfo(packageName).sourceDir
        }catch (e: PackageManager.NameNotFoundException){
            return null
        }
        return PathClassLoader(sourceDir, ClassLoader.getSystemClassLoader())
    }

    private fun getImagePath(name: String): String {
        return File(cacheDir, name).absolutePath
    }

    private fun Any.findCourierEntity(): Any? {
        return this::class.java.fields.firstOrNull { f ->
            f.get(this)?.toString()?.startsWith("CourierEntity(") == true
        }?.get(this)
    }

    private fun String.getFirst(regex: Regex): String? {
        return regex.find(this)?.groupValues?.getOrNull(1)
    }

    private fun onPackagesChanged() {
        SmartspacerTargetProvider.notifyChange(context, AftershipTarget::class.java)
    }

    override fun getActivePackages(): List<Package> {
        return runBlocking {
            packages.firstNotNull().filter {
                it.dismissedAt == null || it.changedAt != it.dismissedAt
            }
        }
    }

    override fun dismissPackage(id: String) {
        scope.launch(Dispatchers.IO) {
            val pkg = packages.firstNotNull().firstOrNull { it.id == id } ?: return@launch
            databaseRepository.addPackage(
                pkg.copy(dismissedAt = pkg.changedAt)
            )
        }
    }

    override fun clearDismissedPackages() {
        scope.launch(Dispatchers.IO) {
            packages.firstNotNull().filterNot{ it.status == Status.DELIVERED }.forEach {
                databaseRepository.addPackage(it.copy(dismissedAt = null))
            }
        }
    }

    override fun getTrackingUrl(pkg: Package): String? {
        return pkg.trackingUrl?.replace(URL_PREFIX, URL_TRACKING_PREFIX)
    }

}