package com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import com.kieronquinn.app.smartspacer.sdk.utils.getParcelableCompat
import java.io.Serializable

val PendingIntent_MUTABLE_FLAGS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
} else {
    PendingIntent.FLAG_UPDATE_CURRENT
}

@Suppress("DEPRECATION")
fun <T: Serializable> Intent.getSerializableExtraCompat(key: String, type: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra(key, type)
    } else {
        getSerializableExtra(key) as? T
    }
}

@Suppress("DEPRECATION")
fun <T: Parcelable> Intent.getParcelableArrayListCompat(key: String, clazz: Class<T>): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayListExtra(key, clazz)
    } else {
        getParcelableArrayListExtra(key)
    }
}

@Suppress("DEPRECATION")
fun <T: Parcelable> Intent.getParcelableExtraCompat(key: String, type: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, type)
    } else {
        getParcelableExtra(key)
    }
}

private const val INTENT_KEY_SECURITY_TAG = "security_tag"
private const val PENDING_INTENT_REQUEST_CODE = 999

fun Intent.applySecurity(context: Context) {
    val securityTag = PendingIntent.getActivity(
        context,
        PENDING_INTENT_REQUEST_CODE,
        Intent(),
        PendingIntent.FLAG_IMMUTABLE
    )
    putExtra(INTENT_KEY_SECURITY_TAG, securityTag)
}

fun Intent.verifySecurity(context: Context) {
    extras?.getParcelableCompat(INTENT_KEY_SECURITY_TAG, PendingIntent::class.java)?.let {
        if(it.creatorPackage == context.packageName) return
    }
    throw SecurityException("Unauthorised access")
}