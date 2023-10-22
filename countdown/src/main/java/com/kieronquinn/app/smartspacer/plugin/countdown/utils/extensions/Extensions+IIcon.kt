package com.kieronquinn.app.smartspacer.plugin.countdown.utils.extensions

import com.mikepenz.iconics.typeface.IIcon

val IIcon.niceName: String
    get() = name.removePrefix(typeface.mappingPrefix)
        .replace("_", " ")
        .capitalizeWords()
        .trim()