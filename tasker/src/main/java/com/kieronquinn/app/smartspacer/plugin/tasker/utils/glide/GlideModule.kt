package com.kieronquinn.app.smartspacer.plugin.tasker.utils.glide

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.signature.ObjectKey
import com.kieronquinn.app.smartspacer.plugin.shared.utils.glide.prepend
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.glide.PackageIcon
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

@GlideModule
class GlideModule(context: Context): AppGlideModule(), KoinComponent {

    private val loadScope = MainScope()

    private val iconPadding = context.resources.getDimensionPixelSize(R.dimen.icon_padding)

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(
            context,
            IIcon::class.java,
            Drawable::class.java,
            null,
            this::loadIIcon,
            this::getKeyForIIcon
        )
        registry.prepend(
            context,
            PackageIcon::class.java,
            Drawable::class.java,
            context.packageManager,
            this::loadPackageIcon,
            this::getKeyForPackageIcon
        )
    }

    private fun getKeyForIIcon(icon: IIcon): ObjectKey {
        return ObjectKey(icon.formattedName)
    }

    private fun loadIIcon(
        context: Context,
        icon: IIcon,
        dummy: Any?,
        callback: DataFetcher.DataCallback<in Drawable>
    ) {
        loadScope.launch(Dispatchers.IO) {
            val drawable = IconicsDrawable(context, icon).apply {
                paddingPx = iconPadding
            }
            callback.onDataReady(drawable)
        }
    }

    private fun getKeyForPackageIcon(icon: PackageIcon): ObjectKey {
        return ObjectKey(icon.packageName)
    }

    private fun loadPackageIcon(
        context: Context,
        icon: PackageIcon,
        packageManager: PackageManager,
        callback: DataFetcher.DataCallback<in Drawable>
    ) {
        loadScope.launch(Dispatchers.IO) {
            try {
                callback.onDataReady(packageManager.getApplicationIcon(icon.packageName))
            } catch (e: PackageManager.NameNotFoundException) {
                callback.onLoadFailed(e)
            }
        }
    }

}