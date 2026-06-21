/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

package com.edugo.kmp.securestorage

/**
 * Fallo del almacén seguro del SO (Android Keystore / iOS Keychain): clave maestra inaccesible,
 * almacén corrupto, `errSec*` de Keychain, etc.
 *
 * Es la traducción al ecosistema EduGo de los errores nativos de bajo nivel. Como [SecureKeyStore] es
 * una primitiva, la capa de negocio que la consuma debe envolverla en `Result<T>` y **no** dejar que
 * esta excepción cruce esa frontera (mismo criterio que `CryptoOpenException` en `:crypto`).
 */
class SecureStorageException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
