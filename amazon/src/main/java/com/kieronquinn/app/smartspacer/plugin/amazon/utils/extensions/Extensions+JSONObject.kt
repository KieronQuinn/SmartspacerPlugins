package com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions

import org.json.JSONException
import org.json.JSONObject

fun JSONObject.getStringOrNull(key: String): String? {
    return try {
        getString(key)
    }catch (e: JSONException) {
        null
    }
}