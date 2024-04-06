package com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings

import android.content.res.ColorStateList
import android.graphics.drawable.Animatable
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.kieronquinn.app.shared.databinding.ItemSettingsCardBinding
import com.kieronquinn.app.shared.databinding.ItemSettingsFooterBinding
import com.kieronquinn.app.shared.databinding.ItemSettingsHeaderBinding
import com.kieronquinn.app.shared.databinding.ItemSettingsRadioCardBinding
import com.kieronquinn.app.shared.databinding.ItemSettingsSliderItemBinding
import com.kieronquinn.app.shared.databinding.ItemSettingsSwitchBinding
import com.kieronquinn.app.shared.databinding.ItemSettingsSwitchItemBinding
import com.kieronquinn.app.shared.databinding.ItemSettingsTextItemBinding
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItemType
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.GenericSettingsItemType
import com.kieronquinn.app.smartspacer.plugin.shared.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getAttrColor
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.isDarkMode
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onChanged
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onLongClicked
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.flow.distinctUntilChanged

abstract class BaseSettingsAdapter(
    recyclerView: LifecycleAwareRecyclerView,
    open var items: List<BaseSettingsItem>
): LifecycleAwareRecyclerView.Adapter<BaseSettingsAdapter.ViewHolder>(recyclerView) {

    protected val layoutInflater: LayoutInflater = LayoutInflater.from(recyclerView.context)
    protected val glide = Glide.with(recyclerView.context)

    protected val monet by lazy {
        MonetCompat.getInstance()
    }

    init {
        this.setHasStableIds(true)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    final override fun getItemId(position: Int): Long {
        return items[position].getItemId()
    }

    final override fun getItemViewType(position: Int): Int {
        return items[position].itemType.itemIndex
    }

    @CallSuper
    open fun onCreateViewHolder(parent: ViewGroup, itemType: BaseSettingsItemType): ViewHolder {
        itemType as GenericSettingsItemType
        return when(itemType){
            GenericSettingsItemType.HEADER -> GenericViewHolder.Header(
                ItemSettingsHeaderBinding.inflate(layoutInflater, parent, false)
            )
            GenericSettingsItemType.SWITCH -> GenericViewHolder.Switch(
                ItemSettingsSwitchBinding.inflate(layoutInflater, parent, false)
            )
            GenericSettingsItemType.SETTING -> GenericViewHolder.Setting(
                ItemSettingsTextItemBinding.inflate(layoutInflater, parent, false)
            )
            GenericSettingsItemType.SWITCH_SETTING -> GenericViewHolder.SwitchSetting(
                ItemSettingsSwitchItemBinding.inflate(layoutInflater, parent, false)
            )
            GenericSettingsItemType.SLIDER -> GenericViewHolder.Slider(
                ItemSettingsSliderItemBinding.inflate(layoutInflater, parent, false)
            )
            GenericSettingsItemType.DROPDOWN -> GenericViewHolder.Dropdown(
                ItemSettingsTextItemBinding.inflate(layoutInflater, parent, false)
            )
            GenericSettingsItemType.CARD -> GenericViewHolder.Card(
                ItemSettingsCardBinding.inflate(layoutInflater, parent, false)
            )
            GenericSettingsItemType.FOOTER -> GenericViewHolder.Footer(
                ItemSettingsFooterBinding.inflate(layoutInflater, parent, false)
            )
            GenericSettingsItemType.RADIO_CARD -> GenericViewHolder.RadioCard(
                ItemSettingsRadioCardBinding.inflate(layoutInflater, parent, false)
            )
        }
    }

    @CallSuper
    open fun getItemType(viewType: Int): BaseSettingsItemType {
        return BaseSettingsItemType.findIndex<GenericSettingsItemType>(viewType)
            ?: throw RuntimeException("Failed to find item with index $viewType")
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return onCreateViewHolder(
            parent,
            getItemType(viewType)
        )
    }

    @CallSuper
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        when(holder){
            is GenericViewHolder.Header -> holder.setup(item as GenericSettingsItem.Header)
            is GenericViewHolder.Switch -> holder.setup(item as GenericSettingsItem.Switch)
            is GenericViewHolder.Setting -> holder.setup(item as GenericSettingsItem.Setting)
            is GenericViewHolder.SwitchSetting -> holder.setup(item as GenericSettingsItem.SwitchSetting)
            is GenericViewHolder.Slider -> holder.setup(item as GenericSettingsItem.Slider)
            is GenericViewHolder.Dropdown -> holder.setup(item as GenericSettingsItem.Dropdown<*>)
            is GenericViewHolder.Card -> holder.setup(item as GenericSettingsItem.Card)
            is GenericViewHolder.Footer -> holder.setup(item as GenericSettingsItem.Footer)
            is GenericViewHolder.RadioCard -> holder.setup(item as GenericSettingsItem.RadioCard)
        }
    }

    /**
     *  Updates the list via [DiffUtil], and scrolls to the top if the size has changed
     */
    fun update(
        newList: List<BaseSettingsItem>,
        recyclerView: LifecycleAwareRecyclerView? = null,
        forceScroll: Boolean = false
    ) {
        items = newList
        notifyDataSetChanged()
        val hasChangedSize = items.size != newList.size
        items = newList
        if(hasChangedSize || forceScroll) {
            recyclerView?.scrollToPosition(0)
        }
    }

    private fun GenericViewHolder.Header.setup(item: GenericSettingsItem.Header) = with(binding) {
        itemSettingsHeaderTitle.text = item.text
        itemSettingsHeaderTitle.setTextColor(monet.getAccentColor(root.context))
    }

    private fun GenericViewHolder.Switch.setup(item: GenericSettingsItem.Switch) = with(binding) {
        itemSettingsSwitchSwitch.isChecked = item.enabled
        itemSettingsSwitchSwitch.text = item.text
        whenResumed {
            binding.itemSettingsSwitchSwitch.onClicked().collect {
                item.onChanged(!itemSettingsSwitchSwitch.isChecked)
            }
        }
    }

    private fun GenericViewHolder.Setting.setup(item: GenericSettingsItem.Setting) = with(binding) {
        root.alpha = if(item.isEnabled) 1f else 0.5f
        root.isEnabled = item.isEnabled
        itemSettingsTextTitle.text = item.title
        itemSettingsTextContent.text = item.subtitle
        itemSettingsTextContent.isVisible = item.subtitle.isNotEmpty()
        glide.load(item.icon)
            .placeholder(itemSettingsTextIcon.drawable)
            .into(itemSettingsTextIcon)
        if(item.icon is Animatable) {
            item.icon.start()
        }
        itemSettingsTextSpace.isVisible = item.icon == null
        itemSettingsTextIcon.isVisible = item.icon != null
        itemSettingsTextIcon.imageTintList = if(item.tintIcon){
            ColorStateList.valueOf(root.context.getAttrColor(android.R.attr.colorControlNormal))
        }else null
        if(item.onLongClick != null){
            whenResumed {
                root.onLongClicked().collect {
                    item.onLongClick.invoke()
                }
            }
        }else{
            root.setOnLongClickListener(null)
        }
        whenResumed {
            root.onClicked().collect {
                item.onClick()
            }
        }
    }

    private fun GenericViewHolder.SwitchSetting.setup(item: GenericSettingsItem.SwitchSetting) = with(binding) {
        root.alpha = if(item.enabled) 1f else 0.5f
        root.isEnabled = item.enabled
        itemSettingsSwitchTitle.text = item.title
        itemSettingsSwitchContent.text = item.subtitle
        itemSettingsSwitchContent.isVisible = item.subtitle.isNotEmpty()
        glide.load(item.icon)
            .placeholder(itemSettingsSwitchIcon.drawable)
            .into(itemSettingsSwitchIcon)
        if(item.icon is Animatable) {
            item.icon.start()
        }
        itemSettingsSwitchSpace.isVisible = item.icon == null
        itemSettingsSwitchIcon.isVisible = item.icon != null
        itemSettingsSwitchSwitch.isChecked = item.checked
        itemSettingsSwitchSwitch.isEnabled = item.enabled
        itemSettingsSwitchSwitch.applyMonet()
        whenResumed {
            binding.itemSettingsSwitchSwitch.onChanged().collect {
                if(!item.enabled) return@collect
                item.onChanged(it)
            }
        }
        binding.root.setOnClickListener {
            if(!item.enabled) return@setOnClickListener
            binding.itemSettingsSwitchSwitch.toggle()
        }
    }

    private fun GenericViewHolder.Slider.setup(item: GenericSettingsItem.Slider) = with(binding) {
        itemSettingsSliderTitle.text = item.title
        itemSettingsSliderContent.text = item.subtitle
        itemSettingsSliderContent.isVisible = item.subtitle.isNotEmpty()
        glide.load(item.icon)
            .placeholder(itemSettingsSliderIcon.drawable)
            .into(itemSettingsSliderIcon)
        itemSettingsSliderSpace.isVisible = item.icon == null
        itemSettingsSliderIcon.isVisible = item.icon != null
        itemSettingsSliderSlider.applyMonet()
        itemSettingsSliderSlider.valueFrom = item.minValue
        itemSettingsSliderSlider.valueTo = item.maxValue
        itemSettingsSliderSlider.stepSize = item.step
        itemSettingsSliderSlider.value = item.startValue
        itemSettingsSliderSlider.setLabelFormatter(item.labelFormatter)
        itemSettingsSliderSlider.labelBehavior = item.labelBehavior
        Log.d("CR69", "Setup")
        whenResumed {
            binding.itemSettingsSliderSlider.clearOnChangeListeners()
            binding.itemSettingsSliderSlider.onChanged().distinctUntilChanged().collect {
                Log.d("CR69", "onChanged to $it")
                item.onChanged(it)
            }
        }
    }

    private fun <T> GenericViewHolder.Dropdown.setup(
        item: GenericSettingsItem.Dropdown<T>
    ) = with(binding) {
        itemSettingsTextTitle.text = item.title
        itemSettingsTextContent.text = item.subtitle
        itemSettingsTextIcon.isVisible = item.icon != null
        itemSettingsTextSpace.isVisible = item.icon == null
        itemSettingsTextContent.isVisible = item.subtitle.isNotEmpty()
        glide.load(item.icon)
            .placeholder(itemSettingsTextIcon.drawable)
            .into(itemSettingsTextIcon)
        whenResumed {
            root.onClicked().collect {
                it.showDropdown(item)
            }
        }
    }

    private fun GenericViewHolder.Card.setup(item: GenericSettingsItem.Card) = with(binding) {
        itemSettingsCardContent.text = item.content
        glide.load(item.icon)
            .placeholder(itemSettingsCardIcon.drawable)
            .into(itemSettingsCardIcon)
        val background = monet.getPrimaryColor(root.context, !root.context.isDarkMode)
        root.backgroundTintList = ColorStateList.valueOf(background)
        root.isEnabled = item.onClick != null
        whenResumed {
            root.onClicked().collect {
                item.onClick?.invoke()
            }
        }
    }

    private fun GenericViewHolder.Footer.setup(item: GenericSettingsItem.Footer) = with(binding) {
        glide.load(item.icon).into(itemSettingsFooterIcon)
        itemSettingsFooterContent.text = item.text
    }

    private fun GenericViewHolder.RadioCard.setup(item: GenericSettingsItem.RadioCard) = with(binding) {
        settingsRadioCardTitle.text = item.title
        settingsRadioCardContent.text = item.content
        settingsRadioCardRadio.isChecked = item.isChecked
        settingsRadioCardRadio.applyMonet()
        val background = monet.getPrimaryColor(root.context, !root.context.isDarkMode)
        root.backgroundTintList = ColorStateList.valueOf(background)
        whenResumed {
            settingsRadioCardRadio.onClicked().collect {
                item.onClick()
            }
        }
        whenResumed {
            root.onClicked().collect {
                item.onClick()
            }
        }
    }

    private fun <T> View.showDropdown(
        item: GenericSettingsItem.Dropdown<T>
    ) {
        val popup = PopupMenu(context, this)
        item.options.forEachIndexed { index, option ->
            popup.menu.add(Menu.NONE, index, Menu.NONE, item.adapter(option))
        }
        popup.setOnMenuItemClickListener {
            item.onSet(item.options[it.itemId])
            popup.dismiss()
            true
        }
        popup.show()
    }

    sealed class GenericViewHolder(override val binding: ViewBinding): ViewHolder(binding) {
        data class Header(override val binding: ItemSettingsHeaderBinding): GenericViewHolder(binding)
        data class Switch(override val binding: ItemSettingsSwitchBinding): GenericViewHolder(binding)
        data class Setting(override val binding: ItemSettingsTextItemBinding): GenericViewHolder(binding)
        data class SwitchSetting(override val binding: ItemSettingsSwitchItemBinding): GenericViewHolder(binding)
        data class Slider(override val binding: ItemSettingsSliderItemBinding): GenericViewHolder(binding)
        data class Dropdown(override val binding: ItemSettingsTextItemBinding): GenericViewHolder(binding)
        data class Card(override val binding: ItemSettingsCardBinding): GenericViewHolder(binding)
        data class Footer(override val binding: ItemSettingsFooterBinding): GenericViewHolder(binding)
        data class RadioCard(override val binding: ItemSettingsRadioCardBinding): GenericViewHolder(binding)
    }

    abstract class ViewHolder(open val binding: ViewBinding): LifecycleAwareRecyclerView.ViewHolder(binding.root)

}