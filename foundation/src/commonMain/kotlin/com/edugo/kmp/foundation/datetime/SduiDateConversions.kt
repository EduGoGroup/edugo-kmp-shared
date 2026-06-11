package com.edugo.kmp.foundation.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Conversiones de fecha/hora compartidas entre la UI dinámica (SDUI) y las
 * pantallas nativas que usan el `DatePicker`/`TimePicker` de Material 3.
 *
 * Existen dos contratos de transporte, deliberadamente separados (estándar de
 * fechas UTC del ecosistema):
 *
 *  - **Sólo-fecha** (`"date"`): cadena `YYYY-MM-DD` SIN zona. El día-calendario es
 *    el dato; no hay instante ni corrimiento de zona. El `DatePicker` de Material 3
 *    devuelve la medianoche **UTC** del día visual, así que se convierte con
 *    [TimeZone.UTC] (NUNCA con la zona del sistema) para no perder/ganar un día.
 *  - **Fecha+hora** (`"datetime"`): instante completo ISO-8601 con `Z` (UTC). Se
 *    renderiza y edita en zona LOCAL, pero se transporta como instante exacto.
 *
 * Funciones puras (sin dependencia de Compose) para que las consuman tanto los
 * controles SDUI como las pantallas nativas (principio shared-over-inline).
 */

/** Hora por defecto para un control fecha+hora recién creado (08:00 local). */
val DEFAULT_DATETIME_LOCAL_TIME: LocalTime = LocalTime(hour = 8, minute = 0)

// --- Sólo-fecha: `YYYY-MM-DD` <-> epoch millis (medianoche UTC del DatePicker) ---

/**
 * `YYYY-MM-DD` → epoch millis de la medianoche UTC de ese día.
 * Tolera un instante ISO completo por compatibilidad: extrae su fecha en UTC
 * (sin corrimiento de zona). Devuelve `null` si no es parseable.
 */
fun dateOnlyToUtcMillis(value: String): Long? {
    val trimmed = value.takeIf { it.isNotBlank() } ?: return null
    parseDateOnly(trimmed)?.let { return it.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds() }
    return null
}

/** epoch millis (medianoche UTC del DatePicker) → `YYYY-MM-DD`. */
fun utcMillisToDateOnly(millis: Long): String =
    Instant.fromEpochMilliseconds(millis)
        .toLocalDateTime(TimeZone.UTC)
        .date
        .toString()

/**
 * Texto entrante de un control sólo-fecha → `LocalDate`, SIN corrimiento de zona.
 * Acepta `YYYY-MM-DD` directo o un instante ISO (del que extrae la fecha en UTC,
 * por compatibilidad con datos viejos). `null` si no es parseable.
 */
fun parseDateOnly(value: String): LocalDate? {
    val trimmed = value.takeIf { it.isNotBlank() } ?: return null
    runCatching { return LocalDate.parse(trimmed) }
    runCatching { return Instant.parse(trimmed).toLocalDateTime(TimeZone.UTC).date }
    return null
}

// --- Fecha+hora: instante ISO-Z <-> componentes LOCAL para el picker ---

/**
 * Texto entrante de un control fecha+hora → `Instant`. Acepta un instante ISO-Z
 * directo o, por compatibilidad, una fecha `YYYY-MM-DD` (anclada a
 * [DEFAULT_DATETIME_LOCAL_TIME] en zona local). `null` si está vacío/no parseable.
 */
fun parseDateTime(value: String): Instant? {
    val trimmed = value.takeIf { it.isNotBlank() } ?: return null
    runCatching { return Instant.parse(trimmed) }
    runCatching {
        return LocalDate.parse(trimmed)
            .atTimeInZone(DEFAULT_DATETIME_LOCAL_TIME, TimeZone.currentSystemDefault())
    }
    return null
}

/**
 * Instante → fecha+hora LOCAL para poblar los pickers (zona del sistema).
 */
fun Instant.toLocalDateTimeInSystemZone(): LocalDateTime =
    toLocalDateTime(TimeZone.currentSystemDefault())

/**
 * Compone fecha local (la elegida en el DatePicker, que viene como millis de
 * medianoche UTC) + hora local (la elegida en el TimePicker) en un `Instant`
 * exacto, interpretando los componentes en zona LOCAL. El resultado se serializa
 * con `Instant.toString()` (ISO-8601 con `Z`).
 */
fun localDateAndTimeToInstant(date: LocalDate, time: LocalTime): Instant =
    date.atTimeInZone(time, TimeZone.currentSystemDefault())

private fun LocalDate.atTimeInZone(time: LocalTime, zone: TimeZone): Instant =
    LocalDateTime(year, monthNumber, dayOfMonth, time.hour, time.minute, time.second)
        .toInstant(zone)
