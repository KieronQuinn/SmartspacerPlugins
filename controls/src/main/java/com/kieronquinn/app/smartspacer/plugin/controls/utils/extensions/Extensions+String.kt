package com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions

fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") {
        it.replaceFirstChar { char -> char.uppercase() }
    }
}