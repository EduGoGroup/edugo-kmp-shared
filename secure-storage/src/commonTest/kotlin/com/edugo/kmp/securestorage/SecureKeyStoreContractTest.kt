package com.edugo.kmp.securestorage

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pruebas de contrato de [SecureKeyStore] comunes a todas las plataformas.
 *
 * El round-trip **real** (escribir/leer en Keystore/Keychain) exige un device y se valida en el bucle
 * interactivo (F5.5/F5.6), no aquí. El comportamiento del stub de escritorio (fallar ruidosamente en
 * vez de degradar a almacenamiento en claro) se prueba en `desktopTest`
 * (`DesktopSecureKeyStoreTest`), porque es semántica de ese `actual`, no de todas las plataformas.
 */
class SecureKeyStoreContractTest {

    @Test
    fun default_service_name_is_stable() {
        // Cambiar este valor "pierde" los secretos ya custodiados; el test lo blinda como contrato.
        assertEquals("com.edugo.securestorage", DEFAULT_SECURE_STORAGE_SERVICE)
    }
}
