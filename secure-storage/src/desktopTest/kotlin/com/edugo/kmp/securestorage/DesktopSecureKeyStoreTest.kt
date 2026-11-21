package com.edugo.kmp.securestorage

import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Contrato del `actual` de escritorio (JVM) de [SecureKeyStore].
 *
 * Desktop no tiene almacén seguro respaldado por el SO: la factory resuelve el stub sin configuración
 * previa y cualquier operación falla con [SecureStorageException] en lugar de guardar en claro.
 *
 * Vive en `desktopTest` y no en `commonTest` porque en Android la factory exige un `Context`
 * (`SecureStorageInitializer.install`) y el Keystore, que la JVM host de `testAndroidHostTest` no tiene,
 * y en iOS la operación usa el Keychain real: la aserción solo es cierta para el stub de escritorio.
 */
class DesktopSecureKeyStoreTest {

    @Test
    fun factory_resolves_an_actual_on_this_platform() {
        // No debe lanzar al construir: la factory existe y resuelve el actual de escritorio.
        val store = createSecureKeyStore("com.edugo.securestorage.test")
        // Sin custodia real, cualquier operación debe fallar ruidosamente (no silenciar).
        assertFailsWith<SecureStorageException> {
            store.putBytes("k", byteArrayOf(1, 2, 3))
        }
    }
}
