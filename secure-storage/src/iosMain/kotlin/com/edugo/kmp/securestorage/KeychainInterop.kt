/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.edugo.kmp.securestorage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import platform.CoreFoundation.CFDictionaryCreate
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.create
import platform.posix.memcpy

/**
 * Helpers de interop Kotlin/Native ↔ Core Foundation / Foundation para las llamadas a Keychain.
 *
 * Las funciones `SecItem*` consumen `CFDictionaryRef`. La query se construye con [CFDictionaryCreate]
 * nativo a partir de punteros `CFTypeRef` (ver [toCFDictionary]).
 */

/**
 * Convierte un mapa Kotlin a `CFDictionaryRef` vía [CFDictionaryCreate] nativo.
 *
 * **Por qué NO `NSDictionary` + toll-free bridging (drift F6c.2):** armar la query metiendo las
 * constantes `kSec*` (que son `CFStringRef` crudos) dentro de un `NSDictionary` (API Foundation) y
 * puenteándolo con `CFBridgingRetain` REVIENTA en el runtime de iOS: `SecItemCopyMatching`/`SecItemAdd`
 * devuelven `errSecParam (-50)` y `securityd` loguea "Unsupported CFType". El bug solo aparece al
 * EJECUTAR en un binario iOS real (el test JVM nunca toca este actual), por eso pasó desapercibido
 * hasta F6c.2. Verificado: el fallo persiste aun cambiando `kCFBooleanTrue` por `NSNumber`, así que
 * el culpable es el round-trip por Foundation, no un valor concreto.
 *
 * [CFDictionaryCreate] recibe los mismos `CFTypeRef` directamente, sin el bridging que los corrompe.
 * Cada clave/valor se normaliza a `CFTypeRef` con [toCFTypeRef] (las constantes `kSec*`/`kCFBoolean*`
 * ya lo son; `String`→`CFString`, `NSData`→`CFData` vía `CFBridgingRetain`). Con
 * `kCFTypeDictionary*CallBacks` el diccionario RETIENE cada elemento, así que las referencias
 * puenteadas temporales se sueltan al salir del `memScoped` (el diccionario conserva las suyas).
 */
internal fun Map<Any?, Any?>.toCFDictionary(): CFDictionaryRef? = memScoped {
    val count = size
    val keysArray = allocArrayOf(map { it.key.toCFTypeRef() })
    val valuesArray = allocArrayOf(map { it.value.toCFTypeRef() })
    CFDictionaryCreate(
        kCFAllocatorDefault,
        keysArray,
        valuesArray,
        count.convert(),
        kCFTypeDictionaryKeyCallBacks.ptr,
        kCFTypeDictionaryValueCallBacks.ptr,
    )
}

/**
 * Normaliza un valor de la query a `CFTypeRef` (`COpaquePointer`) apto para [CFDictionaryCreate].
 *
 * Las constantes `kSec*`/`kCFBoolean*` ya llegan como `CPointer` (Kotlin/Native las expone así): se
 * pasan tal cual. `String` y `NSData` se puentean a `CFString`/`CFData` con `CFBridgingRetain`. El
 * diccionario retiene cada elemento (callbacks de tipo CF), por lo que no liberamos aquí.
 */
private fun Any?.toCFTypeRef(): COpaquePointer? = when (this) {
    is String -> CFBridgingRetain(this as NSString)
    is NSData -> CFBridgingRetain(this)
    is COpaquePointer -> this
    else -> CFBridgingRetain(this)
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
