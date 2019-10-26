package dniel.forwardauth.infrastructure.siren.internal.util

import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal fun Instant.toZonedDateTime(): ZonedDateTime =
    ZonedDateTime.ofInstant(this, ZoneOffset.UTC)

internal fun String.toZonedDateTime(): ZonedDateTime =
    try {
        ZonedDateTime.parse(this, DateTimeFormatter.ISO_ZONED_DATE_TIME)
    } catch (e: Exception) {
        throw IllegalArgumentException(this)
    }

internal fun ZonedDateTime.toFormattedString() = format(DateTimeFormatter.ISO_INSTANT)
