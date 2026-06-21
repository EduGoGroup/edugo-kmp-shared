package com.edugo.kmp.securestorage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Pruebas de contrato de [SecureKeyStore] ejecutables sin dispositivo (`:secure-storage:desktopTest`).
 *
 * El round-trip **real** (escribir/leer en Keystore/Keychain) exige un device y se valida en el bucle
 * interactivo (F5.5/F5.6), no aquí. En el target `desktop` la factory devuelve el stub no-soportado,
 * así que estas pruebas fijan dos cosas estables y verificables en CI:
 *  - la factory `createSecureKeyStore()` resuelve un `actual` en cada plataforma (incluido desktop);
 *  - en plataformas sin almacén seguro real, las operaciones fallan con [SecureStorageException] en
 *    lugar de degradar silenciosamente a almacenamiento en claro.
 */
class SecureKeyStoreContractTest {

    @Test
    fun default_service_name_is_stable() {
        // Cambiar este valor "pierde" los secretos ya custodiados; el test lo blinda como contrato.
        assertEquals("com.edugo.securestorage", DEFAULT_SECURE_STORAGE_SERVICE)
    }

    @Test
    fun factory_resolves_an_actual_on_this_platform() {
        // No debe lanzar al construir: la factory existe y resuelve el actual de la plataforma de test.
        val store = createSecureKeyStore("com.edugo.securestorage.test")
        // En desktop, cualquier operación debe fallar ruidosamente (sin custodia real → no silenciar).
        assertFailsWith<SecureStorageException> {
            store.putBytes("k", byteArrayOf(1, 2, 3))
        }
    }
}
