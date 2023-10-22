package com.kieronquinn.app.smartspacer.plugin.tasker.providers

import android.net.Uri
import android.os.ParcelFileDescriptor
import com.kieronquinn.app.smartspacer.sdk.provider.BaseProvider
import java.io.InputStream

/**
 *  Helper class to implement a ContentProvider which can provide Smartspace Targets with image
 *  streams from your app. Implementations of this class should be exported, since [openFile]
 *  verifies the call comes from Smartspacer.
 */
abstract class SmartspacerFileProvider: BaseProvider() {

    /**
     *  Open your [InputStream] for a given [Uri]. You can pass parameters in the [Uri] as required,
     *  and return `null` if the Uri is somehow invalid.
     *
     *  Your InputStream will be closed automatically.
     */
    abstract fun getInputStream(uri: Uri): InputStream?

    final override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        verifySecurity()
        val inputStream = getInputStream(uri) ?: return null
        val pipe = ParcelFileDescriptor.createPipe()
        val outputStream = ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])
        inputStream.copyTo(outputStream)
        return pipe[0]
    }

    final override fun getType(uri: Uri): String {
        return "application/octet-stream"
    }

}