package com.kieronquinn.app.smartspacer.plugin.shared.utils

import android.view.SurfaceControl
import android.view.SurfaceControlHidden
import dev.rikka.tools.refine.Refine

fun SurfaceControl.Transaction.setBackgroundBlurRadius(
    surfaceControl: SurfaceControl, radius: Int
): SurfaceControlHidden.Transaction {
    return Refine.unsafeCast<SurfaceControlHidden.Transaction>(this)
        .setBackgroundBlurRadius(surfaceControl, radius) as SurfaceControlHidden.Transaction
}