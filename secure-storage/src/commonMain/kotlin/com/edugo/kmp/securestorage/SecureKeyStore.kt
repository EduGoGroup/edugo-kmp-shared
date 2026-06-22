/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

package com.edugo.kmp.securestorage

/**
 * Almacén seguro de secretos respaldado por el hardware/SO de cada plataforma.
 *
 * Su único cometido es **custodiar bytes sensibles que nunca deben tocar disco en claro**: en la
 * mensajería EduGo, la clave privada del dispositivo (`Kd_priv`, 32 B) y la DEK de 256 bits. Esos
 * secretos son la base de la seguridad zero-knowledge del Plan 025 y **no pueden** vivir en
 * `SharedPreferences`/`UserDefaults` sin cifrar.
 *
 * Respaldo por plataforma:
 * - **Android:** `EncryptedSharedPreferences` cifrado con una `MasterKey` AES256-GCM que reside en el
 *   Android Keystore (hardware-backed cuando el SoC lo soporta).
 * - **iOS:** Keychain Services (`kSecClassGenericPassword`) con accesibilidad
 *   `kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly` — el secreto **no migra** a backups de iCloud
 *   ni a otros dispositivos.
 * - **Desktop/Web:** ver el `actual` de cada plataforma; estos targets **no** ofrecen custodia segura
 *   real y no son objetivo de F5 (ver los stubs por plataforma).
 *
 * **Capa primitiva — sin `Result<T>`:** igual que el módulo `:crypto`, este es un puerto de bajo nivel
 * sobre el almacén del SO. Los fallos del almacén (Keystore corrupto, Keychain con `errSec*`) se
 * propagan como [SecureStorageException]. La capa de negocio que lo consuma (p.ej. el repositorio de
 * mensajería) es la responsable de envolver el resultado en `Result<T>` según las convenciones del
 * proyecto; no propagar esta excepción más allá de esa frontera.
 *
 * **Construcción:** se obtiene vía [createSecureKeyStore]. En Android requiere un `Context` que la app
 * inyecta una sola vez al arrancar con [SecureStorageInitializer.install]; en el resto de plataformas
 * la factory no necesita configuración previa. Ver KDoc de [createSecureKeyStore].
 *
 * **Hilo:** las operaciones son bloqueantes y delegan en E/S del SO. Invócalas fuera del hilo
 * principal (en EduGo, vía `AppDispatchers` del módulo `:core` desde la capa que lo use).
 */
expect class SecureKeyStore {

    /**
     * Guarda [value] bajo [key], sobrescribiendo cualquier valor previo de forma atómica.
     *
     * @param key identificador lógico del secreto (p.ej. `"messaging.kd_priv"`).
     * @param value bytes a custodiar (no se copian defensivamente; no mutar el array tras la llamada).
     * @throws SecureStorageException si el almacén del SO rechaza la escritura.
     */
    fun putBytes(key: String, value: ByteArray)

    /**
     * Lee los bytes asociados a [key].
     *
     * @return los bytes custodiados, o `null` si no existe ninguna entrada para [key].
     * @throws SecureStorageException si el almacén existe pero la lectura/descifrado falla.
     */
    fun getBytes(key: String): ByteArray?

    /**
     * Elimina la entrada [key]. No-op si la clave no existía.
     *
     * @throws SecureStorageException si el almacén del SO rechaza el borrado.
     */
    fun remove(key: String)

    /**
     * Indica si existe una entrada para [key] sin materializar su valor.
     */
    fun contains(key: String): Boolean
}

/**
 * Construye el [SecureKeyStore] de la plataforma actual.
 *
 * - **Android:** lee el `Context` previamente registrado con [SecureStorageInitializer.install]. Si no
 *   se instaló, lanza [SecureStorageException]; la app debe llamar a `install(applicationContext)` una
 *   vez al arrancar (típicamente en `Application.onCreate`) antes de instanciar el store. Se inyecta el
 *   `Context` por esta vía (y no por el constructor) para mantener la firma `expect`/`actual` común a
 *   todas las plataformas.
 * - **iOS / Desktop / Web:** no requiere configuración previa.
 *
 * El [serviceName] actúa como espacio de nombres del almacén (en Android, nombre del fichero
 * `EncryptedSharedPreferences`; en iOS, el atributo `kSecAttrService` del item de Keychain). Usar el
 * mismo valor en todas las instancias para que compartan el conjunto de secretos.
 */
expect fun createSecureKeyStore(serviceName: String = DEFAULT_SECURE_STORAGE_SERVICE): SecureKeyStore

/**
 * Espacio de nombres por defecto del almacén seguro EduGo. Mantener estable: cambiarlo "pierde" los
 * secretos ya custodiados bajo el nombre anterior.
 */
const val DEFAULT_SECURE_STORAGE_SERVICE: String = "com.edugo.securestorage"
