package akhoi.libs.mlct.tracking.impl

import akhoi.libs.mlct.location.model.Location
import akhoi.libs.mlct.tools.bcc.ByteConcat
import akhoi.libs.mlct.tools.bcc.ByteConcatReader
import akhoi.libs.mlct.tools.bcc.ResizeDouble.compressDouble
import akhoi.libs.mlct.tools.bcc.ResizeDouble.expandDouble

internal interface LocationSerializer {
    fun serialize(locations: List<Location>): ByteArray
    fun deserialize(bytes: ByteArray): List<Location>
}

internal class TrackingLocationSerializerV1(private val startTime: Long) : LocationSerializer {
    override fun serialize(locations: List<Location>): ByteArray {
        val byteConcat = ByteConcat(TOTAL_BYTES * locations.size)
        locations.forEach { (elapsedTime, latitude, longitude, altitude, speed) ->
            val timeSinceStarted = elapsedTime - startTime
            byteConcat.appendLong(timeSinceStarted, TIME_FIELDSIZE)
            val compactLat = compressDouble(latitude, LAT_EXP_SIZE, LAT_SIG_SIZE)
            byteConcat.appendLong(compactLat, LAT_FIELDSIZE)
            val compactLong = compressDouble(longitude, LONG_EXP_SIZE, LONG_SIG_SIZE)
            byteConcat.appendLong(compactLong, LONG_FIELDSIZE)
            val compactAlt = compressDouble(altitude, ALT_EXP_SIZE, ALT_SIG_SIZE)
            byteConcat.appendLong(compactAlt, ALT_FIELDSIZE)
            val rawSpeed = java.lang.Float.floatToRawIntBits(speed)
            byteConcat.appendInt(rawSpeed, SPEED_FIELDSIZE)
        }
        return byteConcat.getContent()
    }

    override fun deserialize(bytes: ByteArray): List<Location> {
        val reader = ByteConcatReader(bytes)
        val count = bytes.size / TOTAL_BYTES
        val locations = mutableListOf<Location>()
        repeat(count) {
            val elapsedTime = reader.readLong(TIME_FIELDSIZE) + startTime
            val latitude = reader.readLong(LAT_FIELDSIZE)
            val longitude = reader.readLong(LONG_FIELDSIZE)
            val altitude = reader.readLong(ALT_FIELDSIZE)
            val speed = reader.readInt(SPEED_FIELDSIZE)
            locations.add(
                Location(
                    elapsedTime,
                    expandDouble(latitude, LAT_EXP_SIZE, LAT_SIG_SIZE),
                    expandDouble(longitude, LONG_EXP_SIZE, LONG_SIG_SIZE),
                    expandDouble(altitude, ALT_EXP_SIZE, ALT_SIG_SIZE),
                    java.lang.Float.intBitsToFloat(speed)
                )
            )
        }
        return locations
    }

    companion object {
        private const val TIME_FIELDSIZE = 27

        private const val LAT_FIELDSIZE = 36
        private const val LAT_EXP_SIZE = 4
        private const val LAT_SIG_SIZE = 31

        private const val LONG_FIELDSIZE = 36
        private const val LONG_EXP_SIZE = 4
        private const val LONG_SIG_SIZE = 31

        private const val ALT_FIELDSIZE = 36
        private const val ALT_EXP_SIZE = 6
        private const val ALT_SIG_SIZE = 29

        private const val SPEED_FIELDSIZE = 32

        private const val TOTAL_BYTES = (TIME_FIELDSIZE + LAT_FIELDSIZE + LONG_FIELDSIZE + ALT_FIELDSIZE + SPEED_FIELDSIZE) / 8
    }
}