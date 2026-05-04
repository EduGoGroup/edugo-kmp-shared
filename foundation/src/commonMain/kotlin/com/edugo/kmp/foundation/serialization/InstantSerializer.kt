package com.edugo.kmp.foundation.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/**
 * Serializador ISO-8601 para [kotlin.time.Instant].
 *
 * Serializa/deserializa instantes de tiempo en formato ISO-8601
 * (e.g., "2024-01-15T10:30:00Z").
 *
 * Compatible con kotlinx-datetime 0.7.x donde `Instant` migró
 * de `kotlinx.datetime.Instant` a `kotlin.time.Instant`.
 *
 * ## Uso con @Serializable
 * ```kotlin
 * @Serializable
 * data class Event(
 *     @Serializable(with = InstantSerializer::class)
 *     val timestamp: Instant
 * )
 * ```
 *
 * ## Uso con @file:UseSerializers
 * ```kotlin
 * @file:UseSerializers(InstantSerializer::class)
 * ```
 */
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("kotlin.time.Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}

