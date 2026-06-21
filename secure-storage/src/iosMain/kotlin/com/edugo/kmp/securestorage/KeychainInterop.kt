/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.edugo.kmp.securestorage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.CoreFoundation.CFDictionaryRef
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSDictionary
import platform.Foundation.create
import platform.Foundation.dictionaryWithObjects
import platform.posix.memcpy

/**
 * Helpers de interop Kotlin/Native ↔ Core Foundation / Foundation para las llamadas a Keychain.
 *
 * Las funciones `SecItem*` consumen `CFDictionaryRef`. En K/N construimos un `NSDictionary` (toll-free
 * bridged con `CFDictionary`) y lo pasamos a Core Foundation con [CFBridgingRetain], que devuelve un
 * `CFTypeRef` **retenido (+1)**. Esa retención extra se libera sola cuando el `SecItem*` termina
 * (las funciones de Keychain copian/retienen lo que necesitan de la query), por lo que para un
 * diccionario efímero de una sola llamada no hace falta un `CFRelease` explícito.
 */

/**
 * Convierte un mapa Kotlin a `CFDictionaryRef` vía `NSDictionary` + toll-free bridging.
 *
 * Construye el `NSDictionary` con `dictionaryWithObjects:forKeys:` (orden values/keys) para no depender
 * de varargs de pares, y lo puentea a Core Foundation. Los valores/claves deben ser tipos compatibles
 * con CF (constantes `kSec*`, `NSData`, `NSString`/`String`, `CFBoolean`).
 */
@Suppress("UNCHECKED_CAST")
internal fun Map<Any?, Any?>.toCFDictionary(): CFDictionaryRef? {
    val nsDictionary = NSDictionary.dictionaryWithObjects(
        objects = this.values.toList(),
        // dictionaryWithObjects:forKeys: exige List<Any>; nuestras claves (constantes kSec*) nunca son
        // null, así que el cast es seguro aunque el mapa declare Any? por comodidad en los call sites.
        forKeys = this.keys.toList() as List<Any>,
    )
    // CFBridgingRetain devuelve CFTypeRef genérico; el NSDictionary es toll-free bridged a CFDictionary.
    return CFBridgingRetain(nsDictionary) as CFDictionaryRef?
}

/**
 * Copia el contenido de este [ByteArray] en un `NSData` recién creado.
 *
 * `usePinned` fija el array en memoria mientras `NSData.create` copia los bytes; tras la copia el
 * `NSData` es independiente del array original. Un array vacío produce un `NSData` vacío.
 */
internal fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    return usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.convert())
    }
}

/**
 * Copia los bytes de este `NSData` a un [ByteArray] nuevo.
 *
 * Reserva un array del tamaño de `length` y usa `memcpy` desde el puntero `bytes` del `NSData`. Un
 * `NSData` vacío produce un array vacío.
 */
internal fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    val result = ByteArray(size)
    result.usePinned { pinned ->
        memcpy(pinned.addressOf(0), this.bytes, length)
    }
    return result
}
