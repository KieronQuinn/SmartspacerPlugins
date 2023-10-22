package com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions

import com.kieronquinn.app.smartspacer.plugin.googlewallet.model.extras.TransitCardExtrasProto.TransitCardExtras.TransitLeg
import java.time.ZonedDateTime

fun TransitLeg.getDeparture(): ZonedDateTime? {
    if(hasActualDeparture()){
        return actualDeparture.toZonedDateTimeOrNull()
    }
    if(hasScheduledDeparture()){
        return scheduledDeparture.toZonedDateTime()
    }
    return null
}

fun TransitLeg.getArrival(): ZonedDateTime? {
    if(hasActualArrival()){
        return actualArrival.toZonedDateTimeOrNull()
    }
    if(hasScheduledArrival()){
        return scheduledArrival.toZonedDateTime()
    }
    return null
}