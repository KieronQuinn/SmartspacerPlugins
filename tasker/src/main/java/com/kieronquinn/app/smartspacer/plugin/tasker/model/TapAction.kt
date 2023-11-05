package com.kieronquinn.app.smartspacer.plugin.tasker.model

import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.PendingIntent_MUTABLE_FLAGS
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getPackageLabel
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.receivers.TapActionReceiver
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities.TapActionProxyActivity
import kotlinx.parcelize.Parcelize
import android.content.Intent as AndroidIntent
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction as SmartspacerTapAction

sealed class TapAction(
    @SerializedName(NAME_TYPE)
    val type: TapActionType,
    @Transient
    @SerializedName(NAME_RUN_WHILE_LOCKED)
    open val runWhileLocked: Boolean = false
): Manipulative<TapAction>, Parcelable {

    companion object {
        const val NAME_TYPE = "type"
        const val NAME_RUN_WHILE_LOCKED = "run_while_locked"
    }

    abstract fun toTapAction(context: Context, forceIntent: Boolean = false): SmartspacerTapAction?
    abstract fun describe(context: Context): String

    @Parcelize
    data class Url(
        @SerializedName("url")
        val url: String,
        @SerializedName(NAME_RUN_WHILE_LOCKED)
        override val runWhileLocked: Boolean = false
    ): TapAction(TapActionType.URL, runWhileLocked) {

        override fun toTapAction(context: Context, forceIntent: Boolean): SmartspacerTapAction? {
            val uri = try {
                Uri.parse(url)
            }catch (e: Exception) {
                return null
            }
            val intent = AndroidIntent(AndroidIntent.ACTION_VIEW).apply {
                addFlags(AndroidIntent.FLAG_ACTIVITY_NEW_TASK)
                data = uri
            }
            return SmartspacerTapAction(intent = intent, shouldShowOnLockScreen = runWhileLocked)
        }

        override fun describe(context: Context): String {
            return context.getString(R.string.tap_action_open_url_description, url)
        }

        override fun getVariables(): Array<String> {
            return arrayOf(*url.getVariables())
        }

        override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): Url {
            return copy(url = url.replace(replacements))
        }

    }

    @Parcelize
    data class LaunchApp(
        @SerializedName("package_name")
        val packageName: String,
        @SerializedName("app_name")
        val appName: String,
        @SerializedName(NAME_RUN_WHILE_LOCKED)
        override val runWhileLocked: Boolean = false
    ): TapAction(TapActionType.LAUNCH_APP, runWhileLocked) {

        companion object {
            fun fromPackageName(context: Context, packageName: String): LaunchApp {
                val appName = context.packageManager.getPackageLabel(packageName)?.toString()
                return LaunchApp(packageName, appName ?: packageName)
            }
        }

        override fun toTapAction(context: Context, forceIntent: Boolean): SmartspacerTapAction {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            return SmartspacerTapAction(intent = intent, shouldShowOnLockScreen = runWhileLocked)
        }

        override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): LaunchApp {
            return copy(
                packageName = packageName.replace(replacements)
            )
        }

        override fun getVariables(): Array<String> {
            return packageName.getVariables()
        }

        override fun describe(context: Context): String {
            return context.getString(R.string.tap_action_launch_app_description, appName)
        }

    }

    @Parcelize
    data class TaskerEvent(
        @SerializedName("id")
        val id: String,
        @SerializedName(NAME_RUN_WHILE_LOCKED)
        override val runWhileLocked: Boolean = false
    ): TapAction(TapActionType.TASKER_EVENT, runWhileLocked) {

        override fun toTapAction(context: Context, forceIntent: Boolean): SmartspacerTapAction {
            return if (forceIntent) {
                //Used when the action only accepts an intent (eg. Feedback, About intents)
                val intent = TapActionProxyActivity.createIntent(context, id)
                SmartspacerTapAction(intent = intent)
            } else {
                val intent = TapActionReceiver.createIntent(context, id)
                val pendingIntent = PendingIntent.getBroadcast(
                    context, id.hashCode(), intent, PendingIntent_MUTABLE_FLAGS
                )
                SmartspacerTapAction(
                    pendingIntent = pendingIntent,
                    shouldShowOnLockScreen = runWhileLocked
                )
            }
        }

        override suspend fun copyWithManipulations(
            context: Context,
            replacements: Map<String, String>
        ): TapAction {
            return copy(id = id.replace(replacements))
        }

        override fun getVariables(): Array<String> {
            return id.getVariables()
        }

        override fun describe(context: Context): String {
            return context.getString(R.string.tap_action_trigger_tasker_event_description, id)
        }

    }

    enum class TapActionType {
        URL,
        LAUNCH_APP,
        TASKER_EVENT
    }

}
