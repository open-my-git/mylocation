package akhoi.libs.tracking.impl

import akhoi.libs.mlct.location.model.Location
import akhoi.libs.mlct.tracking.impl.TrackingLocationSerializerV1
import org.junit.Test
import kotlin.math.round
import kotlin.test.assertContentEquals

class TrackingLocationSerializerV1Test {

    @Test
    fun testSerialize_singleLocation() {
        val serializer = TrackingLocationSerializerV1(0)
        val locations = listOf(
            Location(
                elapsedTime = 46800000L,
                latitude = 12.34,
                longitude = 56.78,
                altitude = 90.12
            )
        )
        val actual = serializer.serialize(locations)
        val expected = byteArrayFromInts(
            0x59, 0x43, 0x90, 0x0A,
            0x8A, 0xE1, 0x47, 0xAE,
            0xCC, 0x63, 0xD7, 0x0A,
            0x29, 0x5A, 0x1E, 0xB8,
            0x50
        )
        assertContentEquals(expected, actual)
    }

    @Test
    fun testSerialize_mixedLocations() {
        val serializer = TrackingLocationSerializerV1(0)
        val locations = listOf(
            Location(
                elapsedTime = 86400000L,
                latitude = 180.0,
                longitude = 90.0,
                altitude = 100000.0
            ),
            Location(
                elapsedTime = 0L,
                latitude = -180.0,
                longitude = -90.0,
                altitude = 0.0
            ),
        )
        val actual = serializer.serialize(locations)
        val expected = byteArrayFromInts(
            0xA4, 0xCB, 0x80, 0x0E,
            0x68, 0x00, 0x00, 0x00,
            0xD6, 0x80, 0x00, 0x00,
            0x0B, 0xE1, 0xA8, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x3C, 0xD0, 0x00, 0x00,
            0x03, 0xAD, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00
        )
        assertContentEquals(expected, actual)
    }

    @Test
    fun testDeserialize_singleLocation() {
        val deserializer = TrackingLocationSerializerV1(0)
        val content = byteArrayFromInts(
            0x59, 0x43, 0x90, 0x0A,
            0x8A, 0xE1, 0x47, 0xAE,
            0xCC, 0x63, 0xD7, 0x0A,
            0x29, 0x5A, 0x1E, 0xB8,
            0x50
        )
        val actual = deserializer.deserialize(content).map {
            it.copy(
                latitude = round(it.latitude * 100) / 100,
                longitude = round(it.longitude * 100) / 100,
                altitude = round(it.altitude * 100) / 100
            )
        }
        val expected = listOf(
            Location(
                elapsedTime = 46800000L,
                latitude = 12.34,
                longitude = 56.78,
                altitude = 90.12
            )
        )
        assertContentEquals(expected, actual)
    }

    @Test
    fun testDeserialize_mixedLocations() {
        val bytes = byteArrayFromInts(
            0xA4, 0xCB, 0x80, 0x0E,
            0x68, 0x00, 0x00, 0x00,
            0xD6, 0x80, 0x00, 0x00,
            0x0B, 0xE1, 0xA8, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x3C, 0xD0, 0x00, 0x00,
            0x03, 0xAD, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00
        )
        val deserializer = TrackingLocationSerializerV1(0)
        val actual = deserializer.deserialize(bytes).map {
            it.copy(
                latitude = round(it.latitude * 10) / 10,
                longitude = round(it.longitude * 10) / 10,
                altitude = round(it.altitude * 10) / 10,
            )
        }
        val expected = listOf(
            Location(
                elapsedTime = 86400000L,
                latitude = 180.0,
                longitude = 90.0,
                altitude = 100000.0
            ),
            Location(
                elapsedTime = 0L,
                latitude = -180.0,
                longitude = -90.0,
                altitude = 0.0
            ),
        )
        assertContentEquals(expected, actual)
    }

    private fun byteArrayFromInts(vararg ints: Int) = ints.map { it.toByte() }.toByteArray()
}