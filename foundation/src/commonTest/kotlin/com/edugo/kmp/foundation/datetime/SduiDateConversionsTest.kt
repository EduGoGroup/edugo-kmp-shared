package com.edugo.kmp.foundation.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class SduiDateConversionsTest {

    // --- Sólo-fecha: UTC puro, sin corrimiento de día ---

    @Test
    fun dateOnly_roundTrip_doesNotShiftDay() {
        // El DatePicker entrega la medianoche UTC del día visual. La ida y vuelta
        // se hace SIEMPRE en UTC, así que el día-calendario se preserva sin
        // importar la zona del host (donde corre el test podría ser UTC-4, etc.).
        val input = "2026-05-15"
        val millis = dateOnlyToUtcMillis(input)
        assertEquals("2026-05-15", millis?.let { utcMillisToDateOnly(it) })
    }

    @Test
    fun dateOnly_millisIsUtcMidnight() {
        // 2026-05-15T00:00:00Z = 1778198400000 ms. Verifica que NO se aplica la
        // zona local (que movería el instante y, al volver, podría correr el día).
        val expected = Instant.parse("2026-05-15T00:00:00Z").toEpochMilliseconds()
        assertEquals(expected, dateOnlyToUtcMillis("2026-05-15"))
    }

    @Test
    fun dateOnly_parsesIsoInstantWithoutShift() {
        // Compat: un instante ISO entrante se reduce a su fecha en UTC.
        assertEquals(LocalDate(2026, 1, 10), parseDateOnly("2026-01-10T23:30:00Z"))
        assertEquals(LocalDate(2026, 5, 15), parseDateOnly("2026-05-15"))
    }

    @Test
    fun dateOnly_blankIsNull() {
        assertNull(dateOnlyToUtcMillis(""))
        assertNull(parseDateOnly("   "))
    }

    // --- Fecha+hora: instante exacto preservado, default 08:00 ---

    @Test
    fun dateTime_roundTrip_preservesInstant() {
        // ISO-Z -> componentes locales -> instante. El instante exacto se preserva
        // en cualquier zona del host (la zona afecta la presentación, no el dato).
        val original = Instant.parse("2026-01-10T09:00:00Z")
        val local = original.toLocalDateTimeInSystemZone()
        val rebuilt = localDateAndTimeToInstant(local.date, LocalTime(local.hour, local.minute))
        assertEquals(original, rebuilt)
    }

    @Test
    fun dateTime_default_is_0800_local() {
        // Valor nuevo (date-only entrante por compat): ancla a 08:00 local. Al
        // volver a local, la hora debe ser 08:00 (sin importar la zona).
        assertEquals(LocalTime(8, 0), DEFAULT_DATETIME_LOCAL_TIME)
        val instant = parseDateTime("2026-01-10")
        val local = instant?.toLocalDateTime(TimeZone.currentSystemDefault())
        assertEquals(8, local?.hour)
        assertEquals(0, local?.minute)
    }

    @Test
    fun dateTime_parsesIsoZ() {
        assertEquals(Instant.parse("2026-01-10T09:00:00Z"), parseDateTime("2026-01-10T09:00:00Z"))
    }

    @Test
    fun dateTime_blankIsNull() {
        assertNull(parseDateTime(""))
        assertNull(parseDateTime("   "))
    }
}
