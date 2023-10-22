package com.kieronquinn.app.smartspacer.plugin.tasker.model

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.widget.ImageView
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.Bitmap_createBlankBitmap
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.UiSurface_validSurfaces
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.resolveAppWidget
import com.kieronquinn.app.smartspacer.sdk.annotations.LimitedNativeSupport
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.UiSurface
import kotlinx.parcelize.Parcelize
import com.kieronquinn.app.smartspacer.sdk.model.expanded.ExpandedState as SmartspacerExpandedState
import com.kieronquinn.app.smartspacer.sdk.model.expanded.ExpandedState.AppShortcuts as SmartspacerAppShortcuts
import com.kieronquinn.app.smartspacer.sdk.model.expanded.ExpandedState.Shortcuts as SmartspacerShortcuts
import com.kieronquinn.app.smartspacer.sdk.model.expanded.ExpandedState.Shortcuts.Shortcut as SmartspacerShortcut
import com.kieronquinn.app.smartspacer.sdk.model.expanded.ExpandedState.Widget as SmartspacerWidget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.CarouselTemplateData.CarouselItem as SmartspacerCarouselItem
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text as SmartspacerText
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate.Basic as TargetTemplateBasic
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate.Button as TargetTemplateButton
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate.Carousel as TargetTemplateCarousel
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate.Doorbell as TargetTemplateDoorbell
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate.DoorbellState as SmartspacerDoorbellState
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate.HeadToHead as TargetTemplateHeadToHead
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate.Image as TargetTemplateImage
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate.Images as TargetTemplateImages
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate.ListItems as TargetTemplateListItems
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate.LoyaltyCard as TargetTemplateLoyaltyCard

sealed class TargetTemplate(
    @Transient
    @SerializedName(NAME_TITLE)
    open val title: Text,
    @Transient
    @SerializedName(NAME_SUBTITLE)
    open val subtitle: Text,
    @Transient
    @SerializedName(NAME_ICON)
    open val icon: Icon?,
    @Transient
    @SerializedName(NAME_FEATURE_TYPE)
    open val _featureType: String,
    @Transient
    @SerializedName(NAME_ON_CLICK)
    open val onClick: TapAction,
    @Transient
    @SerializedName(NAME_TARGET_EXTRAS)
    open val targetExtras: TargetExtras,
    @SerializedName(NAME_TARGET_TYPE)
    val templateType: TemplateType
): Manipulative<TargetTemplate> {

    open val featureType
        get() = _featureType.toIntOrNull() ?: SmartspaceTarget.FEATURE_UNDEFINED

    open val supportsExtraBasedOptions
        get() = true

    companion object {
        const val NAME_TARGET_TYPE = "target_type"
        private const val NAME_FEATURE_TYPE = "feature_type"
        private const val NAME_ON_CLICK = "on_click"
        private const val NAME_TITLE = "title"
        private const val NAME_SUBTITLE = "subtitle"
        private const val NAME_ICON = "icon"
        private const val NAME_TARGET_EXTRAS = "target_extras"
        private const val NAME_SUB_COMPLICATION = "sub_complication"
    }

    enum class TemplateType {
        BASIC,
        HEAD_TO_HEAD,
        BUTTON,
        LIST_ITEMS,
        LOYALTY_CARD,
        IMAGE,
        DOORBELL,
        IMAGES,
        CAROUSEL
    }

    interface SubComplicationSupportingTarget {
        val subComplication: ComplicationTemplate?

        fun copyWithSubComplication(
            subComplication: ComplicationTemplate?
        ): SubComplicationSupportingTarget
    }

    fun toTarget(
        context: Context,
        componentName: ComponentName,
        id: String
    ): SmartspaceTarget {
        var target = toSmartspaceTarget(context, componentName, id)
        if(targetExtras.hideIfNoComplications) {
            target = target.copy(
                headerAction = target.headerAction?.copy(subtitle = ""),
                baseAction = target.baseAction?.copy(subtitle = ""),
                templateData = target.templateData?.copy(
                    subtitleItem = null,
                    subtitleSupplementalItem = null
                )
            )
        }
        return target
    }

    protected abstract fun toSmartspaceTarget(
        context: Context,
        componentName: ComponentName,
        id: String
    ): SmartspaceTarget

    fun copy(
        title: Text = this.title,
        subtitle: Text = this.subtitle,
        icon: Icon? = this.icon,
        targetExtras: TargetExtras = this.targetExtras,
        onClick: TapAction = this.onClick
    ): TargetTemplate {
        return when(this) {
            is Basic -> copy(title, subtitle, icon, _featureType, onClick, targetExtras)
            is Button -> copy(title, subtitle, icon, onClick, targetExtras, _featureType = _featureType)
            is Carousel -> copy(title, subtitle, icon, onClick, targetExtras, _featureType = _featureType)
            is Doorbell -> copy(title, subtitle, icon, onClick, targetExtras, _featureType)
            is HeadToHead -> copy(title, subtitle, icon, onClick, targetExtras, _featureType = _featureType)
            is Image -> copy(title, subtitle, icon, onClick, targetExtras, _featureType)
            is Images -> copy(title, subtitle, icon, onClick, targetExtras, _featureType = _featureType)
            is ListItems -> copy(title, subtitle, icon, onClick, targetExtras, _featureType = _featureType)
            is LoyaltyCard -> copy(title, subtitle, icon, onClick, targetExtras, _featureType =  _featureType)
        }
    }

    data class Basic(
        @SerializedName(NAME_TITLE)
        override val title: Text,
        @SerializedName(NAME_SUBTITLE)
        override val subtitle: Text,
        @SerializedName(NAME_ICON)
        override val icon: Icon?,
        @SerializedName(NAME_FEATURE_TYPE)
        override val _featureType: String,
        @SerializedName(NAME_ON_CLICK)
        override val onClick: TapAction,
        @SerializedName(NAME_TARGET_EXTRAS)
        override val targetExtras: TargetExtras,
        @SerializedName(NAME_SUB_COMPLICATION)
        override val subComplication: ComplicationTemplate? = null
    ): TargetTemplate(
        title,
        subtitle,
        icon,
        _featureType,
        onClick,
        targetExtras,
        TemplateType.BASIC
    ), SubComplicationSupportingTarget {

        override fun toSmartspaceTarget(
            context: Context,
            componentName: ComponentName,
            id: String
        ): SmartspaceTarget {
            return TargetTemplateBasic(
                id = id,
                componentName = componentName,
                featureType = featureType,
                title = title.toText(),
                subtitle = subtitle.toText(),
                icon = icon?.toIcon(context),
                onClick = onClick.toTapAction(context),
                subComplication = subComplication?.toComplication(context, id)
            ).create().apply {
                targetExtras.applyToTarget(context, this)
            }
        }

        override fun getVariables(): Array<String> {
            return arrayOf(
                *title.getVariables(),
                *subtitle.getVariables(),
                *(icon?.getVariables() ?: emptyArray()),
                *_featureType.getVariables(),
                *onClick.getVariables(),
                *targetExtras.getVariables(),
                *subComplication?.getVariables() ?: emptyArray()
            )
        }

        override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): Basic {
            return copy(
                title = title.copyWithManipulations(context, replacements),
                subtitle = subtitle.copyWithManipulations(context, replacements),
                icon = icon?.copyWithManipulations(context, replacements),
                _featureType = _featureType.replace(replacements),
                onClick = onClick.copyWithManipulations(context, replacements),
                targetExtras = targetExtras.copyWithManipulations(context, replacements),
                subComplication = subComplication?.copyWithManipulations(context, replacements)
            )
        }

        override fun copyWithSubComplication(
            subComplication: ComplicationTemplate?
        ): SubComplicationSupportingTarget {
            return copy(subComplication = subComplication)
        }

    }

    data class HeadToHead(
        @SerializedName(NAME_TITLE)
        override val title: Text,
        @SerializedName(NAME_SUBTITLE)
        override val subtitle: Text,
        @SerializedName(NAME_ICON)
        override val icon: Icon?,
        @SerializedName(NAME_ON_CLICK)
        override val onClick: TapAction,
        @SerializedName(NAME_TARGET_EXTRAS)
        override val targetExtras: TargetExtras,
        @SerializedName("head_to_head_title")
        val headToHeadTitle: Text,
        @SerializedName("head_to_head_first_competitor_icon")
        val headToHeadFirstCompetitorIcon: Icon,
        @SerializedName("head_to_head_first_competitor_text")
        val headToHeadFirstCompetitorText: Text,
        @SerializedName("head_to_head_second_competitor_icon")
        val headToHeadSecondCompetitorIcon: Icon,
        @SerializedName("head_to_head_second_competitor_text")
        val headToHeadSecondCompetitorText: Text,
        @SerializedName(NAME_FEATURE_TYPE)
        override val _featureType: String = SmartspaceTarget.FEATURE_SPORTS.toString()
    ): TargetTemplate(
        title,
        subtitle,
        icon,
        _featureType,
        onClick,
        targetExtras,
        TemplateType.HEAD_TO_HEAD
    ) {

        override fun toSmartspaceTarget(
            context: Context,
            componentName: ComponentName,
            id: String
        ): SmartspaceTarget {
            return TargetTemplateHeadToHead(
                context = context,
                id = id,
                componentName = componentName,
                title = title.toText(),
                subtitle = subtitle.toText(),
                icon = icon?.toIcon(context),
                onClick = onClick.toTapAction(context),
                headToHeadTitle = headToHeadTitle.toText(),
                headToHeadFirstCompetitorIcon = headToHeadFirstCompetitorIcon.toIcon(context),
                headToHeadFirstCompetitorText = headToHeadFirstCompetitorText.toText(),
                headToHeadSecondCompetitorIcon = headToHeadSecondCompetitorIcon.toIcon(context),
                headToHeadSecondCompetitorText = headToHeadSecondCompetitorText.toText()
            ).create().apply {
                targetExtras.applyToTarget(context, this)
            }
        }

        override fun getVariables(): Array<String> {
            return arrayOf(
                *title.getVariables(),
                *subtitle.getVariables(),
                *(icon?.getVariables() ?: emptyArray()),
                *_featureType.getVariables(),
                *onClick.getVariables(),
                *targetExtras.getVariables(),
                *headToHeadTitle.getVariables(),
                *headToHeadFirstCompetitorIcon.getVariables(),
                *headToHeadFirstCompetitorText.getVariables(),
                *headToHeadSecondCompetitorIcon.getVariables(),
                *headToHeadSecondCompetitorText.getVariables()
            )
        }

        override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): HeadToHead {
            return copy(
                title = title.copyWithManipulations(context, replacements),
                subtitle = subtitle.copyWithManipulations(context, replacements),
                icon = icon?.copyWithManipulations(context, replacements),
                onClick = onClick.copyWithManipulations(context, replacements),
                targetExtras = targetExtras.copyWithManipulations(context, replacements),
                headToHeadTitle = headToHeadTitle.copyWithManipulations(context, replacements),
                headToHeadFirstCompetitorIcon = headToHeadFirstCompetitorIcon
                    .copyWithManipulations(context, replacements),
                headToHeadFirstCompetitorText = headToHeadFirstCompetitorText
                    .copyWithManipulations(context, replacements),
                headToHeadSecondCompetitorIcon = headToHeadSecondCompetitorIcon
                    .copyWithManipulations(context, replacements),
                headToHeadSecondCompetitorText = headToHeadSecondCompetitorText
                    .copyWithManipulations(context, replacements)
            )
        }

    }

    data class Button(
        @SerializedName(NAME_TITLE)
        override val title: Text,
        @SerializedName(NAME_SUBTITLE)
        override val subtitle: Text,
        @SerializedName(NAME_ICON)
        override val icon: Icon?,
        @SerializedName(NAME_ON_CLICK)
        override val onClick: TapAction,
        @SerializedName(NAME_TARGET_EXTRAS)
        override val targetExtras: TargetExtras,
        @SerializedName("button_icon")
        val buttonIcon: Icon,
        @SerializedName("button_text")
        val buttonText: Text,
        @SerializedName(NAME_FEATURE_TYPE)
        override val _featureType: String = SmartspaceTarget.FEATURE_LOYALTY_CARD.toString()
    ): TargetTemplate(
        title,
        subtitle,
        icon,
        _featureType,
        onClick,
        targetExtras,
        TemplateType.BUTTON
    ) {

        override fun toSmartspaceTarget(
            context: Context,
            componentName: ComponentName,
            id: String
        ): SmartspaceTarget {
            return TargetTemplateButton(
                context = context,
                id = id,
                componentName = componentName,
                title = title.toText(),
                subtitle = subtitle.toText(),
                icon = icon?.toIcon(context),
                onClick = onClick.toTapAction(context),
                buttonIcon = buttonIcon.toIcon(context),
                buttonText = buttonText.toText()
            ).create().apply {
                targetExtras.applyToTarget(context, this)
            }
        }

        override fun getVariables(): Array<String> {
            return arrayOf(
                *title.getVariables(),
                *subtitle.getVariables(),
                *(icon?.getVariables() ?: emptyArray()),
                *_featureType.getVariables(),
                *onClick.getVariables(),
                *targetExtras.getVariables(),
                *buttonIcon.getVariables(),
                *buttonText.getVariables()
            )
        }

        override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): Button {
            return copy(
                title = title.copyWithManipulations(context, replacements),
                subtitle = subtitle.copyWithManipulations(context, replacements),
                icon = icon?.copyWithManipulations(context, replacements),
                onClick = onClick.copyWithManipulations(context, replacements),
                targetExtras = targetExtras.copyWithManipulations(context, replacements),
                buttonIcon = buttonIcon.copyWithManipulations(context, replacements),
                buttonText = buttonText.copyWithManipulations(context, replacements)
            )
        }

    }

    data class ListItems(
        @SerializedName(NAME_TITLE)
        override val title: Text,
        @SerializedName(NAME_SUBTITLE)
        override val subtitle: Text,
        @SerializedName(NAME_ICON)
        override val icon: Icon?,
        @SerializedName(NAME_ON_CLICK)
        override val onClick: TapAction,
        @SerializedName(NAME_TARGET_EXTRAS)
        override val targetExtras: TargetExtras,
        @SerializedName("list_items")
        val listItems: List<Text>,
        @SerializedName("list_icon")
        val listIcon: Icon,
        @SerializedName("empty_list_message")
        val emptyListMessage: String,
        @SerializedName(NAME_FEATURE_TYPE)
        override val _featureType: String = SmartspaceTarget.FEATURE_SHOPPING_LIST.toString()
    ): TargetTemplate(
        title,
        subtitle,
        icon,
        _featureType,
        onClick,
        targetExtras,
        TemplateType.LIST_ITEMS
    ) {

        override fun toSmartspaceTarget(
            context: Context,
            componentName: ComponentName,
            id: String
        ): SmartspaceTarget {
            return TargetTemplateListItems(
                id = id,
                componentName = componentName,
                context = context,
                title = title.toText(),
                subtitle = subtitle.toText(),
                icon = icon?.toIcon(context),
                listIcon = listIcon.toIcon(context),
                listItems = listItems.map { it.toText() },
                emptyListMessage = SmartspacerText(emptyListMessage),
                onClick = onClick.toTapAction(context)
            ).create().apply { 
                targetExtras.applyToTarget(context, this)
            }
        }

        override fun getVariables(): Array<String> {
            return arrayOf(
                *title.getVariables(),
                *subtitle.getVariables(),
                *(icon?.getVariables() ?: emptyArray()),
                *_featureType.getVariables(),
                *onClick.getVariables(),
                *targetExtras.getVariables(),
                *listItems.getVariables(),
                *listIcon.getVariables()
            )
        }

        override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): ListItems {
            return copy(
                title = title.copyWithManipulations(context, replacements),
                subtitle = subtitle.copyWithManipulations(context, replacements),
                icon = icon?.copyWithManipulations(context, replacements),
                onClick = onClick.copyWithManipulations(context, replacements),
                targetExtras = targetExtras.copyWithManipulations(context, replacements),
                listItems = listItems.map { it.copyWithManipulations(context, replacements) },
                listIcon = listIcon.copyWithManipulations(context, replacements),
                emptyListMessage = emptyListMessage.replace(replacements)
            )
        }

    }

    data class LoyaltyCard(
        @SerializedName(NAME_TITLE)
        override val title: Text,
        @SerializedName(NAME_SUBTITLE)
        override val subtitle: Text,
        @SerializedName(NAME_ICON)
        override val icon: Icon?,
        @SerializedName(NAME_ON_CLICK)
        override val onClick: TapAction,
        @SerializedName(NAME_TARGET_EXTRAS)
        override val targetExtras: TargetExtras,
        @SerializedName("card_icon")
        val cardIcon: Icon,
        @SerializedName("card_prompt")
        val cardPrompt: Text,
        @SerializedName("image_scale_type")
        val imageScaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_CENTER,
        @SerializedName("image_width")
        val _imageWidth: String? = null,
        @SerializedName("image_height")
        val _imageHeight: String? = null,
        @SerializedName(NAME_FEATURE_TYPE)
        override val _featureType: String = SmartspaceTarget.FEATURE_LOYALTY_CARD.toString()
    ): TargetTemplate(
        title,
        subtitle,
        icon,
        _featureType,
        onClick,
        targetExtras,
        TemplateType.LOYALTY_CARD
    ) {

        val imageWidth
            get() = _imageWidth?.toIntOrNull()

        val imageHeight
            get() = _imageHeight?.toIntOrNull()

        override fun toSmartspaceTarget(
            context: Context,
            componentName: ComponentName,
            id: String
        ): SmartspaceTarget {
            return TargetTemplateLoyaltyCard(
                context = context,
                id = id,
                componentName = componentName,
                title = title.toText(),
                subtitle = subtitle.toText(),
                icon = icon?.toIcon(context),
                cardIcon = cardIcon.toIcon(context),
                cardPrompt = cardPrompt.toText(),
                imageScaleType = imageScaleType,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                onClick = onClick.toTapAction(context)
            ).create().apply { 
                targetExtras.applyToTarget(context, this)
            }
        }

        override fun getVariables(): Array<String> {
            return arrayOf(
                *title.getVariables(),
                *subtitle.getVariables(),
                *(icon?.getVariables() ?: emptyArray()),
                *_featureType.getVariables(),
                *onClick.getVariables(),
                *targetExtras.getVariables(),
                *cardIcon.getVariables(),
                *cardPrompt.getVariables(),
                *_imageWidth?.getVariables() ?: emptyArray(),
                *_imageHeight?.getVariables() ?: emptyArray()
            )
        }

        override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): LoyaltyCard {
            return copy(
                title = title.copyWithManipulations(context, replacements),
                subtitle = subtitle.copyWithManipulations(context, replacements),
                icon = icon?.copyWithManipulations(context, replacements),
                onClick = onClick.copyWithManipulations(context, replacements),
                targetExtras = targetExtras.copyWithManipulations(context, replacements),
                cardIcon = cardIcon.copyWithManipulations(context, replacements),
                cardPrompt = cardPrompt.copyWithManipulations(context, replacements),
                _imageWidth = _imageWidth?.replace(replacements),
                _imageHeight = _imageHeight?.replace(replacements)
            )
        }

    }

    data class Image(
        @SerializedName(NAME_TITLE)
        override val title: Text,
        @SerializedName(NAME_SUBTITLE)
        override val subtitle: Text,
        @SerializedName(NAME_ICON)
        override val icon: Icon?,
        @SerializedName(NAME_ON_CLICK)
        override val onClick: TapAction,
        @SerializedName(NAME_TARGET_EXTRAS)
        override val targetExtras: TargetExtras,
        @SerializedName(NAME_FEATURE_TYPE)
        override val _featureType: String,
        @SerializedName("image")
        val image: Icon
    ): TargetTemplate(
        title,
        subtitle,
        icon,
        _featureType,
        onClick,
        targetExtras,
        TemplateType.IMAGE
    ) {

        override val featureType: Int
            get() = _featureType.toIntOrNull() ?: SmartspaceTarget.FEATURE_COMMUTE_TIME

        override fun toSmartspaceTarget(
            context: Context,
            componentName: ComponentName,
            id: String
        ): SmartspaceTarget {
            return TargetTemplateImage(
                context = context,
                id = id,
                componentName = componentName,
                featureType = featureType,
                title = title.toText(),
                subtitle = subtitle.toText(),
                icon = icon?.toIcon(context),
                image = image.toIcon(context),
                onClick = onClick.toTapAction(context)
            ).create().apply { 
                targetExtras.applyToTarget(context, this)
            }
        }

        override fun getVariables(): Array<String> {
            return arrayOf(
                *title.getVariables(),
                *subtitle.getVariables(),
                *(icon?.getVariables() ?: emptyArray()),
                *_featureType.getVariables(),
                *onClick.getVariables(),
                *targetExtras.getVariables(),
                *image.getVariables()
            )
        }

        override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): Image {
            return copy(
                title = title.copyWithManipulations(context, replacements),
                subtitle = subtitle.copyWithManipulations(context, replacements),
                icon = icon?.copyWithManipulations(context, replacements),
                _featureType = _featureType.replace(replacements),
                onClick = onClick.copyWithManipulations(context, replacements),
                targetExtras = targetExtras.copyWithManipulations(context, replacements),
                image = image.copyWithManipulations(context, replacements)
            )
        }

    }

    data class Doorbell(
        @SerializedName(NAME_TITLE)
        override val title: Text,
        @SerializedName(NAME_SUBTITLE)
        override val subtitle: Text,
        @SerializedName(NAME_ICON)
        override val icon: Icon?,
        @SerializedName(NAME_ON_CLICK)
        override val onClick: TapAction,
        @SerializedName(NAME_TARGET_EXTRAS)
        override val targetExtras: TargetExtras,
        @SerializedName(NAME_FEATURE_TYPE)
        override val _featureType: String,
        @SerializedName("doorbell_state")
        val doorbellState: DoorbellState
    ): TargetTemplate(
        title,
        subtitle,
        icon,
        _featureType,
        onClick,
        targetExtras,
        TemplateType.DOORBELL
    ) {

        override val featureType: Int
            get() = _featureType.toIntOrNull() ?: SmartspaceTarget.FEATURE_DOORBELL

        override fun toSmartspaceTarget(
            context: Context,
            componentName: ComponentName,
            id: String
        ): SmartspaceTarget {
            return TargetTemplateDoorbell(
                id = id,
                componentName = componentName,
                featureType = featureType,
                title = title.toText(),
                subtitle = subtitle.toText(),
                icon = icon?.toIcon(context),
                doorbellState = doorbellState.toDoorbellState(context),
                onClick = onClick.toTapAction(context)
            ).create().apply {
                targetExtras.applyToTarget(context, this)
            }
        }

        override fun getVariables(): Array<String> {
            return arrayOf(
                *title.getVariables(),
                *subtitle.getVariables(),
                *(icon?.getVariables() ?: emptyArray()),
                *_featureType.getVariables(),
                *onClick.getVariables(),
                *targetExtras.getVariables(),
                *doorbellState.getVariables()
            )
        }

        override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): TargetTemplate {
            return copy(
                title = title.copyWithManipulations(context, replacements),
                subtitle = subtitle.copyWithManipulations(context, replacements),
                icon = icon?.copyWithManipulations(context, replacements),
                onClick = onClick.copyWithManipulations(context, replacements),
                targetExtras = targetExtras.copyWithManipulations(context, replacements),
                doorbellState = doorbellState.copyWithManipulations(context, replacements),
                _featureType = _featureType.replace(replacements)
            )
        }

        sealed class DoorbellState(
            @SerializedName(NAME_TYPE)
            val type: DoorbellStateType
        ): Manipulative<DoorbellState>, Parcelable {

            companion object {
                const val NAME_TYPE = "type"
            }
            
            abstract fun toDoorbellState(context: Context): SmartspacerDoorbellState

            override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): DoorbellState {
                return this
            }

            @Parcelize
            data class LoadingIndeterminate(
                @SerializedName("width")
                val _width: String?,
                @SerializedName("height")
                val _height: String?,
                @SerializedName("ratio_width")
                val _ratioWidth: String? = null,
                @SerializedName("ratio_height")
                val _ratioHeight: String? = null
            ): DoorbellState(DoorbellStateType.LOADING_INDETERMINATE) {

                val width
                    get() = _width?.toIntOrNull()

                val height
                    get() = _height?.toIntOrNull()

                val ratioWidth
                    get() = _ratioWidth?.toIntOrNull() ?: 1

                val ratioHeight
                    get() = _ratioHeight?.toIntOrNull() ?: 1

                override fun toDoorbellState(context: Context): SmartspacerDoorbellState {
                    return SmartspacerDoorbellState.LoadingIndeterminate(
                        width, height, ratioWidth, ratioHeight
                    )
                }

                override fun getVariables(): Array<String> {
                    return arrayOf(
                        *_width?.getVariables() ?: emptyArray(),
                        *_height?.getVariables() ?: emptyArray(),
                        *_ratioWidth?.getVariables() ?: emptyArray(),
                        *_ratioHeight?.getVariables() ?: emptyArray(),
                    )
                }

                override suspend fun copyWithManipulations(
                    context: Context, replacements: Map<String, String>
                ): LoadingIndeterminate {
                    return copy(
                        _width = _width?.replace(replacements),
                        _height = _height?.replace(replacements),
                        _ratioWidth = _ratioWidth?.replace(replacements),
                        _ratioHeight = _ratioHeight?.replace(replacements)
                    )
                }

            }

            @Parcelize
            data class Loading(
                @SerializedName("icon")
                val icon: Icon,
                @SerializedName("width")
                val _width: String? = null,
                @SerializedName("height")
                val _height: String? = null,
                @SerializedName("show_progress_bar")
                val showProgressBar: Boolean = false,
                @SerializedName("ratio_width")
                val _ratioWidth: String? = null,
                @SerializedName("ratio_height")
                val _ratioHeight: String? = null
            ): DoorbellState(DoorbellStateType.LOADING) {

                val width
                    get() = _width?.toIntOrNull()

                val height
                    get() = _height?.toIntOrNull()

                val ratioWidth
                    get() = _ratioWidth?.toIntOrNull() ?: 1

                val ratioHeight
                    get() = _ratioHeight?.toIntOrNull() ?: 1

                override fun toDoorbellState(context: Context): SmartspacerDoorbellState {
                    return SmartspacerDoorbellState.Loading(
                        icon = icon.loadBitmap(context) ?: Bitmap_createBlankBitmap(),
                        tint = icon.shouldTint,
                        width = width,
                        height = height,
                        showProgressBar = showProgressBar,
                        ratioWidth = ratioWidth,
                        ratioHeight = ratioHeight
                    )
                }

                override fun getVariables(): Array<String> {
                    return arrayOf(
                        *icon.getVariables(),
                        *_width?.getVariables() ?: emptyArray(),
                        *_height?.getVariables() ?: emptyArray(),
                        *_ratioWidth?.getVariables() ?: emptyArray(),
                        *_ratioHeight?.getVariables() ?: emptyArray(),
                    )
                }

                override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): Loading {
                    return copy(
                        icon = icon.copyWithManipulations(context, replacements),
                        _width = _width?.replace(replacements),
                        _height = _height?.replace(replacements),
                        _ratioWidth = _ratioWidth?.replace(replacements),
                        _ratioHeight = _ratioHeight?.replace(replacements)
                    )
                }

            }

            @Parcelize
            data class Videocam(
                @SerializedName("width")
                val _width: String?,
                @SerializedName("height")
                val _height: String?,
                @SerializedName("ratio_width")
                val _ratioWidth: String? = null,
                @SerializedName("ratio_height")
                val _ratioHeight: String? = null
            ): DoorbellState(DoorbellStateType.VIDEOCAM) {

                val width
                    get() = _width?.toIntOrNull()

                val height
                    get() = _height?.toIntOrNull()

                val ratioWidth
                    get() = _ratioWidth?.toIntOrNull() ?: 1

                val ratioHeight
                    get() = _ratioHeight?.toIntOrNull() ?: 1

                override fun toDoorbellState(context: Context): SmartspacerDoorbellState {
                    return SmartspacerDoorbellState.Videocam(
                        width = width,
                        height = height,
                        ratioWidth = ratioWidth,
                        ratioHeight = ratioHeight
                    )
                }

                override fun getVariables(): Array<String> {
                    return arrayOf(
                        *_width?.getVariables() ?: emptyArray(),
                        *_height?.getVariables() ?: emptyArray(),
                        *_ratioWidth?.getVariables() ?: emptyArray(),
                        *_ratioHeight?.getVariables() ?: emptyArray(),
                    )
                }

                override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): Videocam {
                    return copy(
                        _width = _width?.replace(replacements),
                        _height = _height?.replace(replacements),
                        _ratioWidth = _ratioWidth?.replace(replacements),
                        _ratioHeight = _ratioHeight?.replace(replacements)
                    )
                }

            }

            @Parcelize
            data class VideocamOff(
                @SerializedName("width")
                val _width: String?,
                @SerializedName("height")
                val _height: String?,
                @SerializedName("ratio_width")
                val _ratioWidth: String? = null,
                @SerializedName("ratio_height")
                val _ratioHeight: String? = null
            ): DoorbellState(DoorbellStateType.VIDEOCAM_OFF) {

                val width
                    get() = _width?.toIntOrNull()

                val height
                    get() = _height?.toIntOrNull()

                val ratioWidth
                    get() = _ratioWidth?.toIntOrNull() ?: 1

                val ratioHeight
                    get() = _ratioHeight?.toIntOrNull() ?: 1

                override fun toDoorbellState(context: Context): SmartspacerDoorbellState {
                    return SmartspacerDoorbellState.VideocamOff(
                        width = width,
                        height = height,
                        ratioWidth = ratioWidth,
                        ratioHeight = ratioHeight
                    )
                }

                override fun getVariables(): Array<String> {
                    return arrayOf(
                        *_width?.getVariables() ?: emptyArray(),
                        *_height?.getVariables() ?: emptyArray(),
                        *_ratioWidth?.getVariables() ?: emptyArray(),
                        *_ratioHeight?.getVariables() ?: emptyArray(),
                    )
                }

                override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): VideocamOff {
                    return copy(
                        _width = _width?.replace(replacements),
                        _height = _height?.replace(replacements),
                        _ratioWidth = _ratioWidth?.replace(replacements),
                        _ratioHeight = _ratioHeight?.replace(replacements)
                    )
                }

            }

            @Parcelize
            data class ImageBitmap(
                @SerializedName("bitmap")
                val bitmap: Icon,
                @SerializedName("image_scale_type")
                val imageScaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_CENTER,
                @SerializedName("image_width")
                val _imageWidth: String? = null,
                @SerializedName("image_height")
                val _imageHeight: String? = null
            ): DoorbellState(DoorbellStateType.IMAGE_BITMAP) {

                val imageWidth
                    get() = _imageWidth?.toIntOrNull()

                val imageHeight
                    get() = _imageHeight?.toIntOrNull()

                override fun toDoorbellState(context: Context): SmartspacerDoorbellState {
                    return SmartspacerDoorbellState.ImageBitmap(
                        bitmap = bitmap.loadBitmap(context) ?: Bitmap_createBlankBitmap(),
                        imageScaleType = imageScaleType,
                        imageWidth = imageWidth,
                        imageHeight = imageHeight
                    )
                }

                override fun getVariables(): Array<String> {
                    return arrayOf(
                        *bitmap.getVariables(),
                        *_imageWidth?.getVariables() ?: emptyArray(),
                        *_imageHeight?.getVariables() ?: emptyArray()
                    )
                }

                override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): DoorbellState {
                    return copy(
                        bitmap = bitmap.copyWithManipulations(context, replacements),
                        _imageWidth = _imageWidth?.replace(replacements),
                        _imageHeight = _imageHeight?.replace(replacements)
                    )
                }

            }

            @Parcelize
            data class ImageUri(
                @SerializedName("frame_duration_ms")
                val _frameDurationMs: String,
                @SerializedName("image_uris")
                val imageUris: List<Icon>
            ): DoorbellState(DoorbellStateType.IMAGE_URI) {

                val frameDurationMs
                    get() = _frameDurationMs.toIntOrNull() ?: 1000

                override fun toDoorbellState(context: Context): SmartspacerDoorbellState {
                    return SmartspacerDoorbellState.ImageUri(
                        frameDurationMs = frameDurationMs,
                        imageUris = imageUris.filterIsInstance<Icon.Bitmap>().mapNotNull {
                            Uri.parse(it.uri)
                        }
                    )
                }

                override fun getVariables(): Array<String> {
                    return arrayOf(
                        *_frameDurationMs.getVariables(),
                        *imageUris.getVariables()
                    )
                }

                override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): DoorbellState {
                    return copy(
                        _frameDurationMs = _frameDurationMs.replace(replacements),
                        imageUris = imageUris.map {
                            it.copyWithManipulations(context, replacements)
                        }
                    )
                }

            }

            enum class DoorbellStateType {
                LOADING_INDETERMINATE,
                LOADING,
                VIDEOCAM,
                VIDEOCAM_OFF,
                IMAGE_BITMAP,
                IMAGE_URI
            }

        }

    }

    data class Images(
        @SerializedName(NAME_TITLE)
        override val title: Text,
        @SerializedName(NAME_SUBTITLE)
        override val subtitle: Text,
        @SerializedName(NAME_ICON)
        override val icon: Icon?,
        @SerializedName(NAME_ON_CLICK)
        override val onClick: TapAction,
        @SerializedName(NAME_TARGET_EXTRAS)
        override val targetExtras: TargetExtras,
        @SerializedName("images")
        val images: List<Icon>,
        @SerializedName("image_click_intent")
        val imageClickIntent: TapAction? = null,
        @SerializedName("frame_duration_ms")
        val _frameDurationMs: String? = null,
        @SerializedName("image_dimension_ratio")
        val imageDimensionRatio: String? = null,
        @SerializedName(NAME_FEATURE_TYPE)
        override val _featureType: String = SmartspaceTarget.FEATURE_UNDEFINED.toString()
    ): TargetTemplate(
        title,
        subtitle,
        icon,
        _featureType,
        onClick,
        targetExtras,
        TemplateType.IMAGES
    ) {

        val frameDurationMs
            get() = _frameDurationMs?.toIntOrNull() ?: 1000

        override val supportsExtraBasedOptions
            get() = false

        override fun toSmartspaceTarget(
            context: Context,
            componentName: ComponentName,
            id: String
        ): SmartspaceTarget {
            return TargetTemplateImages(
                id = id,
                componentName = componentName,
                context = context,
                title = title.toText(),
                subtitle = subtitle.toText(),
                icon = icon?.toIcon(context),
                images = images.map { it.toIcon(context) },
                onClick = onClick.toTapAction(context),
                imageClickIntent = imageClickIntent?.toTapAction(context, true)?.intent,
                frameDurationMs = frameDurationMs,
                imageDimensionRatio = imageDimensionRatio
            ).create().apply {
                targetExtras.applyToTarget(context, this, false)
            }
        }

        override fun getVariables(): Array<String> {
            return arrayOf(
                *title.getVariables(),
                *subtitle.getVariables(),
                *(icon?.getVariables() ?: emptyArray()),
                *_featureType.getVariables(),
                *onClick.getVariables(),
                *targetExtras.getVariables(),
                *images.getVariables(),
                *imageClickIntent?.getVariables() ?: emptyArray(),
                *_frameDurationMs?.getVariables() ?: emptyArray(),
                *imageDimensionRatio?.getVariables() ?: emptyArray()
            )
        }

        override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): Images {
            return copy(
                title = title.copyWithManipulations(context, replacements),
                subtitle = subtitle.copyWithManipulations(context, replacements),
                icon = icon?.copyWithManipulations(context, replacements),
                onClick = onClick.copyWithManipulations(context, replacements),
                targetExtras = targetExtras.copyWithManipulations(context, replacements),
                images = images.map { it.copyWithManipulations(context, replacements) },
                imageClickIntent = imageClickIntent?.copyWithManipulations(context, replacements),
                _frameDurationMs = _frameDurationMs?.replace(replacements),
                imageDimensionRatio = imageDimensionRatio?.replace(replacements)
            )
        }

    }

    data class Carousel(
        @SerializedName(NAME_TITLE)
        override val title: Text,
        @SerializedName(NAME_SUBTITLE)
        override val subtitle: Text,
        @SerializedName(NAME_ICON)
        override val icon: Icon?,
        @SerializedName(NAME_ON_CLICK)
        override val onClick: TapAction,
        @SerializedName(NAME_TARGET_EXTRAS)
        override val targetExtras: TargetExtras,
        @SerializedName("carousel_item")
        val carouselItems: List<CarouselItem>,
        @SerializedName("on_carousel_click")
        val onCarouselClick: TapAction?,
        @SerializedName(NAME_SUB_COMPLICATION)
        override val subComplication: ComplicationTemplate?,
        @SerializedName(NAME_FEATURE_TYPE)
        override val _featureType: String = SmartspaceTarget.FEATURE_UNDEFINED.toString()
    ): TargetTemplate(
        title,
        subtitle,
        icon,
        _featureType,
        onClick,
        targetExtras,
        TemplateType.CAROUSEL
    ), SubComplicationSupportingTarget {

        override val supportsExtraBasedOptions
            get() = false

        override fun toSmartspaceTarget(
            context: Context,
            componentName: ComponentName,
            id: String
        ): SmartspaceTarget {
            return TargetTemplateCarousel(
                id = id,
                componentName = componentName,
                title = title.toText(),
                subtitle = subtitle.toText(),
                icon = icon?.toIcon(context),
                items = carouselItems.map { it.toCarouselItem(context) },
                onClick = onClick.toTapAction(context),
                onCarouselClick = onCarouselClick?.toTapAction(context),
                subComplication = subComplication?.toComplication(context, id)
            ).create().apply {
                targetExtras.applyToTarget(context, this, false)
            }
        }

        override fun getVariables(): Array<String> {
            return arrayOf(
                *title.getVariables(),
                *subtitle.getVariables(),
                *(icon?.getVariables() ?: emptyArray()),
                *_featureType.getVariables(),
                *onClick.getVariables(),
                *targetExtras.getVariables(),
                *subComplication?.getVariables() ?: emptyArray()
            )
        }

        override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): Carousel {
            return copy(
                title = title.copyWithManipulations(context, replacements),
                subtitle = subtitle.copyWithManipulations(context, replacements),
                icon = icon?.copyWithManipulations(context, replacements),
                onClick = onClick.copyWithManipulations(context, replacements),
                targetExtras = targetExtras.copyWithManipulations(context, replacements),
                subComplication = subComplication?.copyWithManipulations(context, replacements)
            )
        }

        override fun copyWithSubComplication(
            subComplication: ComplicationTemplate?
        ): SubComplicationSupportingTarget {
            return copy(subComplication = subComplication)
        }

        @Parcelize
        data class CarouselItem(
            @SerializedName("upper_text")
            val upperText: Text,
            @SerializedName("lower_text")
            val lowerText: Text,
            @SerializedName("image")
            val image: Icon,
            @SerializedName("tap_action")
            val tapAction: TapAction
        ): Manipulative<CarouselItem>, Parcelable {

            fun toCarouselItem(context: Context): SmartspacerCarouselItem {
                return SmartspacerCarouselItem(
                    upperText = upperText.toText(),
                    lowerText = lowerText.toText(),
                    image = image.toIcon(context),
                    tapAction = tapAction.toTapAction(context)
                )
            }

            override fun getVariables(): Array<String> {
                return arrayOf(
                    *upperText.getVariables(),
                    *lowerText.getVariables(),
                    *image.getVariables(),
                    *tapAction.getVariables(),
                )
            }

            override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): CarouselItem {
                return copy(
                    upperText = upperText.copyWithManipulations(context, replacements),
                    lowerText = lowerText.copyWithManipulations(context, replacements),
                    image = image.copyWithManipulations(context, replacements),
                    tapAction = tapAction.copyWithManipulations(context, replacements)
                )
            }

        }

    }

    data class TargetExtras(
        @SerializedName("expiry_time_millis")
        val _expiryTimeMillis: String? = null,
        @SerializedName("is_sensitive")
        val isSensitive: Boolean = false,
        @SerializedName("source_notification_key")
        val sourceNotificationKey: String? = null,
        @SerializedName("expanded_state")
        val expandedState: ExpandedState? = null,
        @SerializedName("can_be_dismissed")
        val canBeDismissed: Boolean = true,
        @SerializedName("can_take_two_complications")
        val canTakeTwoComplications: Boolean = false,
        @SerializedName("hide_if_no_complications")
        val hideIfNoComplications: Boolean = false,
        @SerializedName("limit_to_surfaces")
        val limitToSurfaces: Set<UiSurface> = UiSurface_validSurfaces().toSet(),
        @SerializedName("about_intent")
        val aboutIntent: TapAction? = null,
        @SerializedName("feedback_intent")
        val feedbackIntent: TapAction? = null,
        @SerializedName("hide_title_on_aod")
        val hideTitleOnAod: Boolean = false,
        @SerializedName("hide_subtitle_on_aod")
        val hideSubtitleOnAod: Boolean = false
    ): Manipulative<TargetExtras> {

        val expiryTimeMillis
            get() = _expiryTimeMillis?.toLongOrNull()

        @OptIn(LimitedNativeSupport::class)
        fun applyToTarget(
            context: Context,
            target: SmartspaceTarget,
            applyBundleBasedExtras: Boolean = true
        ) {
            target.expiryTimeMillis = expiryTimeMillis ?: 0L
            target.isSensitive = isSensitive
            target.sourceNotificationKey = sourceNotificationKey
            target.expandedState = expandedState?.toExpandedState(context)
            target.canBeDismissed = canBeDismissed
            target.canTakeTwoComplications = canTakeTwoComplications
            target.hideIfNoComplications = hideIfNoComplications
            target.limitToSurfaces = limitToSurfaces
            if(applyBundleBasedExtras) {
                target.aboutIntent = aboutIntent?.toTapAction(context, true)?.intent
                target.feedbackIntent = feedbackIntent?.toTapAction(context, true)?.intent
                target.hideTitleOnAod = hideTitleOnAod
                target.hideSubtitleOnAod = hideSubtitleOnAod
            }
        }

        override fun getVariables(): Array<String> {
            return arrayOf(
                *_expiryTimeMillis?.getVariables() ?: emptyArray(),
                *sourceNotificationKey?.getVariables() ?: emptyArray(),
                *expandedState?.getVariables() ?: emptyArray(),
                *aboutIntent?.getVariables() ?: emptyArray(),
                *feedbackIntent?.getVariables() ?: emptyArray()
            )
        }

        override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): TargetExtras {
            return copy(
                _expiryTimeMillis = _expiryTimeMillis?.replace(replacements),
                sourceNotificationKey = sourceNotificationKey?.replace(replacements),
                expandedState = expandedState?.copyWithManipulations(context, replacements),
                aboutIntent = aboutIntent?.copyWithManipulations(context, replacements),
                feedbackIntent = feedbackIntent?.copyWithManipulations(context, replacements)
            )
        }

        @Parcelize
        data class ExpandedState(
            @SerializedName("widget")
            val widget: Widget? = null,
            @SerializedName("shortcuts")
            val shortcuts: Shortcuts? = null,
            @SerializedName("app_shortcuts")
            val appShortcuts: AppShortcuts? = null
        ): Manipulative<ExpandedState>, Parcelable {

            fun toExpandedState(context: Context): SmartspacerExpandedState {
                return SmartspacerExpandedState(
                    widget = widget?.toWidget(context),
                    shortcuts = shortcuts?.toShortcuts(context),
                    appShortcuts = appShortcuts?.toAppShortcuts()
                )
            }

            override fun getVariables(): Array<String> {
                return arrayOf(
                    *widget?.getVariables() ?: emptyArray(),
                    *shortcuts?.getVariables() ?: emptyArray(),
                    *appShortcuts?.getVariables() ?: emptyArray()
                )
            }

            override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): ExpandedState {
                return copy(
                    widget = widget?.copyWithManipulations(context, replacements),
                    shortcuts = shortcuts?.copyWithManipulations(context, replacements),
                    appShortcuts = appShortcuts?.copyWithManipulations(context, replacements)
                )
            }

            @Parcelize
            data class Widget(
                @SerializedName("component_name")
                val _componentName: String? = null,
                @SerializedName("label")
                val label: String? = null,
                @SerializedName("id")
                val id: String? = null,
                @SerializedName("show_when_locked")
                val showWhenLocked: Boolean = true,
                @SerializedName("skip_configure")
                val skipConfigure: Boolean = false,
                @SerializedName("width")
                val _width: String? = null,
                @SerializedName("height")
                val _height: String? = null
            ): Manipulative<Widget>, Parcelable {

                val componentName: ComponentName?
                    get() {
                        return try {
                            ComponentName.unflattenFromString(_componentName ?: return null)
                        }catch (e: Exception){
                            null
                        }
                    }

                val width: Int
                    get() {
                        if(id == null) return 0
                        return _width?.toIntOrNull() ?: 0
                    }

                val height: Int
                    get() {
                        if(id == null) return 0
                        return _height?.toIntOrNull() ?: 0
                    }

                fun toWidget(context: Context): SmartspacerWidget? {
                    val providerInfo = componentName?.let {
                        context.resolveAppWidget(it)
                    }?.also {
                        if(skipConfigure){
                            it.configure = null
                        }
                    } ?: return null
                    return SmartspacerWidget(
                        info = providerInfo,
                        id = id,
                        showWhenLocked = showWhenLocked,
                        width = width,
                        height = height
                    )
                }

                override fun getVariables(): Array<String> {
                    return arrayOf(
                        *_componentName?.getVariables() ?: emptyArray(),
                        *id?.getVariables() ?: emptyArray(),
                        *_width?.getVariables() ?: emptyArray(),
                        *_height?.getVariables() ?: emptyArray()
                    )
                }

                override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): Widget {
                    return copy(
                        _componentName = _componentName?.replace(replacements),
                        id = id?.replace(replacements),
                        _width = _width?.replace(replacements),
                        _height = _height?.replace(replacements)
                    )
                }

            }

            @Parcelize
            data class Shortcuts(
                @SerializedName("shortcuts")
                val shortcuts: List<Shortcut>
            ): Manipulative<Shortcuts>, Parcelable {

                fun toShortcuts(context: Context): SmartspacerShortcuts {
                    return SmartspacerShortcuts(
                        shortcuts = shortcuts.map { it.toShortcut(context) }
                    )
                }

                override fun getVariables(): Array<String> {
                    return arrayOf(
                        *shortcuts.getVariables()
                    )
                }

                override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): Shortcuts {
                    return copy(
                        shortcuts = shortcuts.map { it.copyWithManipulations(context, replacements) }
                    )
                }

                @Parcelize
                data class Shortcut(
                    @SerializedName("label")
                    val label: Text,
                    @SerializedName("icon")
                    val icon: Icon,
                    @SerializedName("tap_action")
                    val tapAction: TapAction,
                    @SerializedName("show_when_locked")
                    val showWhenLocked: Boolean = true
                ): Manipulative<Shortcut>, Parcelable {

                    fun toShortcut(context: Context): SmartspacerShortcut {
                        val action = tapAction.toTapAction(context)
                        return SmartspacerShortcut(
                            label = label.toText().text,
                            icon = icon?.toIcon(context),
                            pendingIntent = action?.pendingIntent,
                            intent = action?.intent,
                            showWhenLocked = showWhenLocked
                        )
                    }

                    override fun getVariables(): Array<String> {
                        return arrayOf(
                            *label.getVariables(),
                            *icon.getVariables(),
                            *tapAction.getVariables()
                        )
                    }

                    override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): Shortcut {
                        return copy(
                            label = label.copyWithManipulations(context, replacements),
                            icon = icon.copyWithManipulations(context, replacements),
                            tapAction = tapAction.copyWithManipulations(context, replacements)
                        )
                    }

                }

            }

            @Parcelize
            data class AppShortcuts(
                @SerializedName("package_name")
                val packageName: String?,
                @SerializedName("label")
                val label: String?,
                @SerializedName("app_shortcut_count")
                val appShortcutCount: Int = 5,
                @SerializedName("show_when_locked")
                val showWhenLocked: Boolean = true
            ): Manipulative<AppShortcuts>, Parcelable {

                fun toAppShortcuts(): SmartspacerAppShortcuts {
                    return SmartspacerAppShortcuts(
                        packageNames = setOfNotNull(packageName),
                        appShortcutCount = appShortcutCount,
                        showWhenLocked = showWhenLocked
                    )
                }

                override fun getVariables(): Array<String> {
                    return packageName?.getVariables() ?: emptyArray()
                }

                override suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): AppShortcuts {
                    return copy(
                        packageName = packageName?.replace(replacements)
                    )
                }

            }

        }

    }

}