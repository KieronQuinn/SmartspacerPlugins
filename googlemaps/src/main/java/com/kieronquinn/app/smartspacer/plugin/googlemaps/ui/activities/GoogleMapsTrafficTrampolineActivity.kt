package com.kieronquinn.app.smartspacer.plugin.googlemaps.ui.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.GoogleMapsRepository
import com.kieronquinn.app.smartspacer.plugin.googlemaps.targets.GoogleMapsTrafficTarget
import com.kieronquinn.app.smartspacer.plugin.googlemaps.widgets.GoogleMapsTrafficWidget
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.allowBackground
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.applySecurity
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.packageHasPermission
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.verifySecurity
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import org.koin.android.ext.android.inject

class GoogleMapsTrafficTrampolineActivity: AppCompatActivity() {

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, GoogleMapsTrafficTrampolineActivity::class.java).apply {
                applySecurity(context)
            }
        }
    }

    private val googleMapsRepository by inject<GoogleMapsRepository>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.verifySecurity(this)
        val preferred = googleMapsRepository.getClickIntent()
        val hasPermission = packageManager.packageHasPermission(
            GoogleMapsTrafficWidget.PACKAGE_NAME,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        when {
            !hasPermission && preferred != null -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    preferred.send(ActivityOptionsCompat.makeBasic().allowBackground().toBundle())
                }else{
                    preferred.send()
                }
            }
            hasPermission -> {
                startActivity(Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    `package` = GoogleMapsTrafficWidget.PACKAGE_NAME
                })
            }
            else -> {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${GoogleMapsTrafficWidget.PACKAGE_NAME}")
                })
            }
        }
        SmartspacerTargetProvider.notifyChange(this, GoogleMapsTrafficTarget::class.java)
        finish()
    }

}