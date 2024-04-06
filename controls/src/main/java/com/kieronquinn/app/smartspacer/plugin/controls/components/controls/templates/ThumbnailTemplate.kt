package com.kieronquinn.app.smartspacer.plugin.controls.components.controls.templates

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.service.controls.Control
import android.service.controls.templates.ThumbnailTemplate
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlExtraData
import com.kieronquinn.app.smartspacer.plugin.controls.targets.ControlsTarget
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Duration
import android.graphics.drawable.Icon as AndroidIcon

/**
 *  Usually shows an image, only supported as a Target in Smartspacer. Otherwise, behaves as basic.
 */
@RequiresApi(Build.VERSION_CODES.S)
object ThumbnailTemplate: ControlsTemplate<ThumbnailTemplate>() {

    private const val THUMBNAIL_WIDTH = 768
    private const val THUMBNAIL_HEIGHT = 432

    override fun getTarget(
        template: ThumbnailTemplate,
        context: Context,
        componentName: ComponentName,
        control: Control,
        targetData: ControlsTarget.TargetData
    ): SmartspaceTarget? {
        val thumbnailImage = template.thumbnail.loadThumbnail(context)
        return if(thumbnailImage != null) {
            val controlExtraData = ControlExtraData(
                targetData.doesRequireUnlock(control),
                targetData.modeSetMode,
                targetData.floatSetFloat,
                targetData.shouldHideDetails
            )
            TargetTemplate.Image(
                context,
                getId(control, targetData.hashCode()),
                componentName,
                SmartspaceTarget.FEATURE_UNDEFINED,
                Text(targetData.customTitle ?: control.title),
                Text(
                    targetData.customSubtitle
                        ?: getContent(context, template, control, controlExtraData)
                ),
                targetData.getIcon(ControlsTarget.TargetData.IconConfig.Control(control, componentName), context),
                Icon(AndroidIcon.createWithBitmap(thumbnailImage)),
                getTapAction(
                    context,
                    targetData.controlTapAction,
                    controlExtraData,
                    control,
                    componentName,
                    targetData.smartspacerId
                )
            ).create()
        }else{
            super.getTarget(template, context, componentName, control, targetData)
        }
    }

    private fun AndroidIcon.loadThumbnail(context: Context): Bitmap? = runBlocking {
        withTimeoutOrNull(Duration.ofSeconds(2).toMillis()) {
            loadDrawable(context)?.toBitmap(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
        }
    }

}