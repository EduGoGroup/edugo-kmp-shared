package com.edugo.kmp.design.tokens

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.fail

/**
 * Test de contrato: enumera los icon-names que el seed envia desde
 * la Fase 3a y valida que [IconCatalog] los resuelve a un ImageVector
 * no nulo.
 *
 * Si el seed introduce un icon-name nuevo que no esta registrado,
 * este test falla con un mensaje accionable.
 *
 * No bloquea el build de la app; CI lo corre como gate de seed-vs-cliente.
 */
class IconCatalogContractTest {

    private val seedIconNames: Set<String> = setOf(
        "save",
        "trash",
        "plus",
        "pencil",
        "list",
        "help_outline",
        "check_circle",
        "archive",
        "more_vert",
        // Aliases comunes para test cross-naming
        "delete",
        "edit",
        "add",
        "group_add",
    )

    @Test
    fun allSeedIconsResolveToImageVector() {
        val missing = seedIconNames.filter { IconCatalog.lookup(it) == null }
        if (missing.isNotEmpty()) {
            fail(
                "Icon-names declarados en el seed pero NO registrados en IconCatalog: " +
                    "$missing. Agregar entradas en IconCatalog.kt antes de mergear el seed.",
            )
        }
    }

    @Test
    fun lookupIsCaseInsensitive() {
        assertNotNull(IconCatalog.lookup("SAVE"))
        assertNotNull(IconCatalog.lookup("Save"))
        assertNotNull(IconCatalog.lookup("save"))
    }
}
