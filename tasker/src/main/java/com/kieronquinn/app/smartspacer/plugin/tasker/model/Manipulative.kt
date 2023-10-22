package com.kieronquinn.app.smartspacer.plugin.tasker.model

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Manipulative.Companion.REGEX_VARIABLE
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.prependPrefixIfRequired
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.removeAllPrefixes

/**
 *  Defines a model which can have text dynamically manipulated, following the [REGEX_VARIABLE]
 *  format, which is `%variable`. Calling [copyWithManipulations] with a map of `%variable` to
 *  `replacement` will replace text in this and all (configured) children of the model.
 *
 *  Fields which can have replacements but are normally not a String should be setup as a `_` field,
 *  as a String, perform replacements as normal, and then have a `get()` of the correctly typed
 *  field without the underscore prefix, handling invalid values with a default value as required.
 *
 *  Since [copyWithManipulations] is a suspend function, this can also be used to apply runtime
 *  manipulations such as downloading remote resources and converting the type to use a local copy.
 */
interface Manipulative<T> {

    companion object {
        val REGEX_VARIABLE = "(%[A-Za-z0-9]+)?".toRegex()

        fun String.containsTaskerVariable(): Boolean {
            return getVariablesFromString().isNotEmpty()
        }

        fun String.getVariablesFromString(): Array<String> {
            return REGEX_VARIABLE.findAll(this).mapNotNull {
                it.groupValues.getOrNull(1)
            }.filterNot { it.isBlank() }.toList().toTypedArray()
        }

        fun String.replaceWithReplacements(replacements: Map<String, String>): String {
            return replace(REGEX_VARIABLE) { replacements.getOrDefault(it.value, it.value) }
        }
    }

    suspend fun copyWithManipulations(context: Context, replacements: Map<String, String>): T
    fun getVariables(): Array<String>

    fun String.getVariables(): Array<String> {
        return getVariablesFromString().stripPercentagePrefix()
    }

    fun String.replace(replacements: Map<String, String>): String {
        return replaceWithReplacements(replacements.appendPercentagePrefix())
    }

    private fun Array<String>.stripPercentagePrefix(): Array<String> {
        return map {
            it.removeAllPrefixes("%")
        }.toTypedArray()
    }

    private fun Map<String, String>.appendPercentagePrefix(): Map<String, String> {
        return mapKeys { it.key.prependPrefixIfRequired("%") }
    }

}

fun List<Manipulative<*>>.getVariables(): Array<String> {
    return flatMap {
        it.getVariables().toList()
    }.toTypedArray()
}