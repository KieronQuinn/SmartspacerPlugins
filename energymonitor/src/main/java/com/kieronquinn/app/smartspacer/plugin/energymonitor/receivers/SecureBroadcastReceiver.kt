package com.kieronquinn.app.smartspacer.plugin.energymonitor.receivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import androidx.annotation.CallSuper
import androidx.annotation.RestrictTo
import com.kieronquinn.app.smartspacer.plugin.energymonitor.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.energymonitor.receivers.SecureBroadcastReceiver.Companion.putExtra
import java.util.UUID

/**
 *  [BroadcastReceiver] that checks the package who created the intent by requiring a pending
 *  intent to be attached with the creator package matching [BuildConfig.APPLICATION_ID]. If it
 *  does not match, an exception will be thrown. Use [putExtra] to add the required extra to an
 *  intent.
 */
abstract class SecureBroadcastReceiver: BroadcastReceiver() {

    companion object {
        private const val EXTRA_VERIFICATION_PENDING_INTENT = "verification_pending_intent"

        fun putExtra(context: Context, intent: Intent): Intent {
            return intent.apply {
                putExtra(
                    EXTRA_VERIFICATION_PENDING_INTENT,
                    PendingIntent.getActivity(
                        context, UUID.randomUUID().hashCode(), Intent(), PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }
        }
    }

    @CallSuper
    override fun onReceive(context: Context, intent: Intent?) {
        val callingPackage = intent
            ?.getParcelableExtraCompat(EXTRA_VERIFICATION_PENDING_INTENT, PendingIntent::class.java)
            ?.creatorPackage
        if(callingPackage != BuildConfig.APPLICATION_ID){
            throw SecurityException("Invalid calling package $callingPackage")
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    @Suppress("DEPRECATION")
    private fun <T: Parcelable> Intent.getParcelableExtraCompat(key: String, type: Class<T>): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, type)
        } else {
            getParcelableExtra(key)
        }
    }

}