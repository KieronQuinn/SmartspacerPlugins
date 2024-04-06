package com.kieronquinn.app.smartspacer.plugin.controls.requirements

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.service.controls.templates.ControlTemplate
import android.service.controls.templates.RangeTemplate
import android.service.controls.templates.TemperatureControlTemplate
import android.service.controls.templates.ToggleRangeTemplate
import android.service.controls.templates.ToggleTemplate
import androidx.annotation.StringRes
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlMode
import com.kieronquinn.app.smartspacer.plugin.controls.model.LoadingConfig
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository.ControlState
import com.kieronquinn.app.smartspacer.plugin.controls.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity
import com.kieronquinn.app.smartspacer.sdk.model.Backup
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerRequirementProvider
import org.koin.android.ext.android.inject

class ControlsRequirement: SmartspacerRequirementProvider() {

    private val dataRepository by inject<DataRepository>()
    private val controlsRepository by inject<ControlsRepository>()
    private val gson by inject<Gson>()

    override fun isRequirementMet(smartspacerId: String): Boolean {
        val requirementData = dataRepository
            .getRequirementData(smartspacerId, RequirementData::class.java) ?: return false
        val control = controlsRepository.getControl(
            requirementData.componentName ?: return false,
            requirementData.controlId ?: return false,
            smartspacerId
        ) ?: return false
        return requirementData.meetsRequirement(control)
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            resources.getString(R.string.requirement_label),
            getDescription(smartspacerId),
            Icon.createWithResource(provideContext(), R.drawable.ic_controls),
            configActivity = BaseConfigurationActivity.createIntent(
                provideContext(),
                NavGraphMapping.CONFIGURATION_REQUIREMENT
            ),
            setupActivity = BaseConfigurationActivity.createIntent(
                provideContext(),
                NavGraphMapping.CONFIGURATION_REQUIREMENT
            )
        )
    }

    private fun getDescription(smartspacerId: String?): String {
        val data = smartspacerId?.let { getRequirementData(it) }
        val controlName = data?.controlName
        val controlApp = data?.controlApp
        return if(controlName != null && controlApp != null) {
            resources.getString(R.string.requirement_description, controlName, controlApp)
        }else{
            resources.getString(R.string.requirement_description_unset)
        }
    }

    override fun createBackup(smartspacerId: String): Backup {
        val requirementData = dataRepository
            .getRequirementData(smartspacerId, RequirementData::class.java)
        val json = gson.toJson(requirementData)
        return Backup(json, getDescription(smartspacerId))
    }

    override fun restoreBackup(smartspacerId: String, backup: Backup): Boolean {
        val data = try {
            gson.fromJson(backup.data, RequirementData::class.java)
        }catch (e: Exception) {
            null
        } ?: return false
        dataRepository.updateRequirementData(
            smartspacerId,
            RequirementData::class.java,
            RequirementData.TYPE,
            ::onRestored
        ) {
            data
        }
        return false //Still check for permissions
    }

    private fun onRestored(context: Context, smartspacerId: String) {
        notifyChange(smartspacerId)
    }

    private fun getRequirementData(smartspacerId: String): RequirementData {
        return dataRepository.getRequirementData(smartspacerId, RequirementData::class.java)
            ?: RequirementData(smartspacerId)
    }

    data class RequirementData(
        @SerializedName("smartspacer_id")
        val smartspacerId: String,
        @SerializedName("control_component_name")
        val controlComponentName: String? = null,
        @SerializedName("control_app")
        val controlApp: String? = null,
        @SerializedName("control_id")
        val controlId: String? = null,
        @SerializedName("control_name")
        val controlName: String? = null,
        @SerializedName("loading_config")
        val loadingConfig: LoadingConfig? = null,
        @SerializedName("requirement_type")
        val requirementType: RequirementType? = null,
        @SerializedName("boolean")
        val boolean: Boolean? = null,
        @SerializedName("mode")
        val mode: ControlMode? = null,
        @SerializedName("float")
        val float: Float? = null,
        @SerializedName("float_type")
        val floatType: RequirementValueType? = null
    ) {

        fun equalsIgnoringFloat(other: Any?): Boolean {
            if(other !is RequirementData) return false
            if(other.controlComponentName != controlComponentName) return false
            if(other.controlApp != controlApp) return false
            if(other.controlId != controlId) return false
            if(other.controlName != controlName) return false
            if(other.loadingConfig != loadingConfig) return false
            if(other.requirementType != requirementType) return false
            if(other.boolean != boolean) return false
            if(other.mode != mode) return false
            return other.floatType == floatType
        }

        val loadConfig
            get() = loadingConfig ?: LoadingConfig.CACHED

        val componentName
            get() = controlComponentName?.let { ComponentName.unflattenFromString(it) }

        val controlRequirementType
            get() = requirementType ?: RequirementType.AVAILABLE

        fun meetsRequirement(controlState: ControlState): Boolean {
            return when(controlRequirementType) {
                RequirementType.AVAILABLE -> controlState.isAvailable()
                RequirementType.BOOLEAN -> {
                    controlState.isBooleanMet(boolean ?: false)
                }
                RequirementType.MODE -> {
                    controlState.isModeMet(mode ?: ControlMode.MODE_OFF)
                }
                RequirementType.FLOAT -> {
                    controlState.isFloatMet(
                        float,
                        floatType ?: RequirementValueType.EQUALS
                    )
                }
            }
        }

        private fun ControlState.isAvailable(): Boolean {
            if(this !is ControlState.Control) return false
            return control.controlTemplate != ControlTemplate.getErrorTemplate()
        }

        private fun ControlState.isBooleanMet(comparison: Boolean): Boolean {
            if(this !is ControlState.Control) return false
            return when(val template = control.controlTemplate) {
                is ToggleTemplate -> {
                   template.isChecked == comparison
                }
                is ToggleRangeTemplate -> {
                    template.isChecked == comparison
                }
                else -> false
            }
        }

        private fun ControlState.isModeMet(comparison: ControlMode): Boolean {
            if(this !is ControlState.Control) return false
            return when(val template = control.controlTemplate) {
                is TemperatureControlTemplate -> {
                    template.currentMode == comparison.mode
                }
                else -> false
            }
        }

        private fun ControlState.isFloatMet(comparison: Float?, type: RequirementValueType): Boolean {
            if(this !is ControlState.Control) return false
            return when(val template = control.controlTemplate) {
                is RangeTemplate -> {
                    template.isFloatMet(comparison ?: template.currentValue, type)
                }
                is ToggleRangeTemplate -> {
                    template.range.isFloatMet(comparison ?: template.range.currentValue, type)
                }
                else -> false
            }
        }

        private fun RangeTemplate.isFloatMet(comparison: Float, type: RequirementValueType): Boolean {
            return when(type) {
                RequirementValueType.EQUALS -> comparison == currentValue
                RequirementValueType.GREATER_THAN -> comparison > currentValue
                RequirementValueType.GREATER_THAN_OR_EQUAL_TO -> comparison >= currentValue
                RequirementValueType.LESS_THAN -> comparison < currentValue
                RequirementValueType.LESS_THAN_OR_EQUAL_TO -> comparison <= currentValue
            }
        }

        enum class RequirementType(@StringRes val label: Int) {
            AVAILABLE(R.string.requirement_type_available),
            BOOLEAN(R.string.requirement_type_boolean),
            MODE(R.string.requirement_type_mode),
            FLOAT(R.string.requirement_type_float)
        }

        enum class RequirementValueType(@StringRes val label: Int) {
            EQUALS(R.string.requirement_type_float_type_equals),
            LESS_THAN(R.string.requirement_type_float_type_less_than),
            LESS_THAN_OR_EQUAL_TO(R.string.requirement_type_float_type_less_than_equal_to),
            GREATER_THAN(R.string.requirement_type_float_type_greater_than),
            GREATER_THAN_OR_EQUAL_TO(R.string.requirement_type_float_type_greater_than_or_equal_to),
        }

        companion object {
            const val TYPE = "control"
        }
    }

}