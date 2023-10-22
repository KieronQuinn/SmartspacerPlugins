package com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions

fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") {
        it.replaceFirstChar { char -> char.uppercase() }
    }
}

fun String.takeIfNotBlank(): String? {
    return takeIf { isNotBlank() }
}

fun String.char(): Char {
    return toCharArray().first()
}

private val regexVariableWhole = "%[A-Za-z0-9]+".toRegex()

fun String.isVariable(): Boolean {
    return regexVariableWhole.matches(this)
}

fun String.removeAllPrefixes(prefix: String): String {
    var result = this
    while(result.startsWith(prefix)) {
        result = removePrefix(prefix)
    }
    return result
}

fun String.prependPrefixIfRequired(prefix: String): String {
    return if(!startsWith(prefix)) {
        "$prefix$this"
    }else this
}