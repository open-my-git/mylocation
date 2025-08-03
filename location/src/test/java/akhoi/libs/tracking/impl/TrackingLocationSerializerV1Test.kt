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
                altitude = 90.12,
                speed = 3.9f
            )
        )
        val actual = serializer.serialize(locations)
        val expected = byteArrayFromInts(
            0x59, 0x43, 0x90, 0x0A,
            0x8A, 0xE1, 0x47, 0xAE,
            0xCC, 0x63, 0xD7, 0x0A,
            0x29, 0x5A, 0x1E, 0xB8,
            0x50, 0x80, 0xf3, 0x33,
            0x34
        )
        assertContentEquals(expected, actual)
    }

    @Test
    fun testSerialize_multipleLocations() {
        val serializer = TrackingLocationSerializerV1(0)
        val locations = listOf(
            Location(
                elapsedTime = 86400000L,
                latitude = 180.0,
                longitude = 90.0,
                altitude = 100000.0,
                speed = 100.123f,
            ),
            Location(
                elapsedTime = 0L,
                latitude = -180.0,
                longitude = -90.0,
                altitude = 0.0,
                speed = 1f
            ),
        )
        val actual = serializer.serialize(locations)
        val expected = byteArrayFromInts(
            0xA4, 0xCB, 0x80, 0x0E,
            0x68, 0x00, 0x00, 0x00,
            0xD6, 0x80, 0x00, 0x00,
            0x0B, 0xE1, 0xA8, 0x00,
            0x00, 0x85, 0x90, 0x7d,
            0xF4, 0x00, 0x00, 0x00,
            0x3C, 0xD0, 0x00, 0x00,
            0x03, 0xAD, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0xfe, 0x00,
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
            0x50, 0x80, 0xf3, 0x33,
            0x34
        )
        val actual = deserializer.deserialize(content)
        val expected = listOf(
            Location(
                elapsedTime = 46800000L,
                latitude = 12.339999999850988,
                longitude = 56.77999998629093,
                altitude = 90.11999988555908,
                speed = 3.9f
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
            0x00, 0x85, 0x90, 0x7d,
            0xF4, 0x00, 0x00, 0x00,
            0x3C, 0xD0, 0x00, 0x00,
            0x03, 0xAD, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0xfe, 0x00,
            0x00, 0x00
        )
        val deserializer = TrackingLocationSerializerV1(0)
        val actual = deserializer.deserialize(bytes)
        val expected = listOf(
            Location(
                elapsedTime = 86400000L,
                latitude = 180.0,
                longitude = 90.0,
                altitude = 100000.0,
                speed = 100.123f
            ),
            Location(
                elapsedTime = 0L,
                latitude = -180.0,
                longitude = -90.0,
                altitude = 0.0,
                speed = 1f
            ),
        )
        assertContentEquals(expected, actual)
    }

    private fun byteArrayFromInts(vararg ints: Int) = ints.map { it.toByte() }.toByteArray()
}