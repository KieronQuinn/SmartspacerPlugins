package com.kieronquinn.app.smartspacer.plugin.aftership.utils.room

import com.kieronquinn.app.smartspacer.plugin.aftership.model.database.Package.Tracking
import com.kieronquinn.app.smartspacer.plugin.shared.utils.room.GsonConverter

object TrackingConverter: GsonConverter<Tracking>(typeToken())