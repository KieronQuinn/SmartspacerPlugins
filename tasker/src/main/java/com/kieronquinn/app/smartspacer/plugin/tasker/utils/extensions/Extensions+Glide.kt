package com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions

import android.util.Log
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.model.GlideUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun RequestManager.downloadToFile(url: String, authentication: String?): File? {
    return withContext(Dispatchers.IO){
        val glideUrl = GlideUrl(url).apply {
            if(authentication != null){
                headers["Authorization"] = authentication
            }
        }
        try {
            asFile().load(glideUrl).submit().get()
        }catch (e: Exception){
            Log.e("ICON", "Error downloading icon", e)
            null
        }
    }
}