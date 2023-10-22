package com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.extensions

import com.google.internal.tapandpay.v1.CommonProto.DateTime
import com.google.protobuf.Duration
import com.google.type.Date
import com.google.type.TimeOfDay
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.Duration as JavaDuration

fun DateTime.toZonedDateTime(): ZonedDateTime {
    return toZonedDateTimeOrNull()!!
}

fun DateTime.toZonedDateTimeOrNull(): ZonedDateTime? {
    val date = this.localDate
    val time = this.localTime
    val localDate = LocalDate.of(
        date.year.orNull() ?: return null,
        date.month.orNull() ?: return null,
        date.day.orNull() ?: return null
    )
    val localTime = LocalTime.of(time.hours, time.minutes, time.seconds, time.nanos)
    val zoneOffset = if(hasOffsetFromUtc()){
        ZoneOffset.ofTotalSeconds(offsetFromUtc.seconds.toInt())
    }else{
        ZoneOffset.from(ZonedDateTime.now())
    }
    return localDate.atTime(localTime).atZone(zoneOffset)
}

fun DateTime.matches(other: DateTime): Boolean {
    return toZonedDateTime() == other.toZonedDateTime()
}

fun ZonedDateTime.toDateTime(): DateTime {
    return DateTime.newBuilder().apply {
        localTime = TimeOfDay.newBuilder().apply {
            hours = hour
            minutes = minute
            seconds = second
            nanos = nano
        }.build()
        localDate = Date.newBuilder().apply {
            year = this@toDateTime.year
            month = this@toDateTime.monthValue
            day = this@toDateTime.dayOfMonth
        }.build()
        offsetFromUtc = Duration.newBuilder().apply {
            seconds = offset.totalSeconds.toLong()
        }.build()
    }.build()
}

fun Duration.toDuration(): JavaDuration {
    return JavaDuration.ofSeconds(seconds).plusNanos(nanos.toLong())
}

fun ZonedDateTime.atUtc(): ZonedDateTime {
    return withZoneSameInstant(ZoneId.of("UTC"))
}

private fun Int.orNull() = takeIf { it != 0 }