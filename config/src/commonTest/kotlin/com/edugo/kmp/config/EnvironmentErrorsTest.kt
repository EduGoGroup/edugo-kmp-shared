package com.edugo.kmp.config

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests del contrato de mensajes de error que cada detector usa cuando la
 * variable de entorno falta o llega con un valor inválido. Sirven como
 * regresión: si alguien suaviza el comportamiento (devolver default, swallow
 * exception, etc.) estos tests fallarán.
 */
class EnvironmentErrorsTest {

    @Test
    fun missingErrorMencionaPlataformaYContrato() {
        val ex = assertFailsWith<IllegalStateException> {
            environmentMissingError("Android")
        }
        val msg = ex.message ?: ""

        assertTrue(msg.contains(ENV_VAR_NAME), "El mensaje debe mencionar APP_ENVIRONMENT")
        assertTrue(msg.contains("Android"), "El mensaje debe identificar la plataforma")
        // El hint debe listar al menos un valor válido para guiar al dev.
        Environment.entries.forEach { value ->
            assertTrue(msg.contains(value.name), "El mensaje debe listar ${value.name} entre los valores válidos")
        }
    }

    @Test
    fun missingErrorIncluyeHintParaCadaPlataforma() {
        val ex = assertFailsWith<IllegalStateException> {
            environmentMissingError("Web")
        }
        val msg = ex.message ?: ""
        // El hint estandarizado de STANDARD.md §5 cubre las cuatro plataformas
        // sin importar cuál disparó el error — así un dev que ve el error en
        // logs sabe la receta para todas.
        assertTrue(msg.contains("Desktop"), "Hint debe incluir Desktop")
        assertTrue(msg.contains("Android"), "Hint debe incluir Android")
        assertTrue(msg.contains("iOS"), "Hint debe incluir iOS")
        assertTrue(msg.contains("Web"), "Hint debe incluir Web")
    }

    @Test
    fun invalidErrorEcoaElValorRecibido() {
        val ex = assertFailsWith<IllegalStateException> {
            environmentInvalidError("iOS", "STAGGING")
        }
        val msg = ex.message ?: ""
        assertTrue(msg.contains("STAGGING"), "El mensaje debe ecoar el valor inválido para diagnóstico")
        assertTrue(msg.contains("iOS"), "El mensaje debe identificar la plataforma")
        assertTrue(msg.contains(ENV_VAR_NAME), "El mensaje debe mencionar APP_ENVIRONMENT")
    }

    @Test
    fun invalidErrorListaValoresValidos() {
        val ex = assertFailsWith<IllegalStateException> {
            environmentInvalidError("Desktop", "")
        }
        val msg = ex.message ?: ""
        Environment.entries.forEach { value ->
            assertTrue(msg.contains(value.name), "El mensaje debe listar ${value.name} entre los valores válidos")
        }
    }
}
