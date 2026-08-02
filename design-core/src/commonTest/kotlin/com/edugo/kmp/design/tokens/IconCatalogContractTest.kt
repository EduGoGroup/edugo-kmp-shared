package com.edugo.kmp.design.tokens

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Test de contrato: valida que [IconCatalog] resuelve a un ImageVector no nulo
 * todos los icon-names que el seed declara.
 *
 * La lista NO se mantiene a mano: sale de [SeedIconNames], archivo generado por
 * `make regen-seed-icons` a partir del seed real de `edugo-infrastructure`.
 * Antes de eso la lista se escribia a mano y quedo desincronizada del seed — el
 * gate daba verde mientras iconos como `message-circle` caian al fallback en la
 * app (plan 052, tarea 6.4 / QA-24).
 *
 * Cuando alguien agrega un icono al seed, el flujo es: regenerar, ver fallar
 * este test, registrar el icono en [IconCatalog].
 *
 * No bloquea el build de la app; CI lo corre como gate de seed-vs-cliente.
 */
class IconCatalogContractTest {
    @Test
    fun allSeedIconsResolveToImageVector() {
        val missing = SeedIconNames.ALL.filter { IconCatalog.lookup(it) == null }
        if (missing.isNotEmpty()) {
            fail(
                "Icon-names declarados en el seed pero NO registrados en IconCatalog: " +
                    "$missing. Agregar entradas en IconCatalog.kt antes de mergear el seed.",
            )
        }
    }

    /**
     * Guarda contra un generador roto: si el script apunta a una ruta vacia y
     * emite una lista sin nombres, el test de arriba pasaria por vacuidad.
     */
    @Test
    fun seedIconListIsNotEmpty() {
        assertTrue(
            SeedIconNames.ALL.isNotEmpty(),
            "SeedIconNames.ALL esta vacia: regenerar con `make regen-seed-icons`.",
        )
    }

    @Test
    fun lookupIsCaseInsensitive() {
        assertNotNull(IconCatalog.lookup("SAVE"))
        assertNotNull(IconCatalog.lookup("Save"))
        assertNotNull(IconCatalog.lookup("save"))
    }
}
