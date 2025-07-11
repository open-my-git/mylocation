package akhoi.libs.tracking.impl

import akhoi.libs.location.model.Location
import akhoi.libs.tools.MetadataCacheProperties
import akhoi.libs.tools.TruncateConcatByteArray
import akhoi.libs.tracking.ActivityTrackingDataStore
import android.content.Context
import android.os.SystemClock
import java.io.File
import java.util.UUID

internal class ActivityTrackingDataStoreImpl(
    private val context: Context,
) : ActivityTrackingDataStore {

    private lateinit var rootDir: File
    private lateinit var metaProperties: MetadataCacheProperties

    override fun initialize() {
        rootDir = File("${context.filesDir}/$FILEPATH_ROOT")
        rootDir.mkdirs()
        metaProperties = MetadataCacheProperties(rootDir, "activity_properties")
        metaProperties.initialize()
    }

    override suspend fun startTracking(): String {
        val currentId = metaProperties.get<String>(PROPKEY_ACTIVITY_ID)
        if (currentId != null) {
            return currentId
        }
        val activityId = UUID.randomUUID().toString()

        metaProperties.put(PROPKEY_ACTIVITY_ID, activityId)
        metaProperties.put(PROPKEY_ACTIVITY_START_ELAPSED_TIME, SystemClock.elapsedRealtime())
        metaProperties.put(PROPKEY_ACTIVITY_START_TIME, System.currentTimeMillis())

        val versionFile = File("$rootDir/$FILEPATH_DATA_VERSION")
        if (!versionFile.exists()) {
            versionFile.writeBytes(ByteArray(1) { DATA_VERSION } )
        }

        return activityId
    }

    override suspend fun getStartTime(): Long {
        return metaProperties.get<Long>(PROPKEY_ACTIVITY_START_TIME) ?: 0
    }

    override suspend fun addTrackingLocations(locations: List<Location>) {
        val locationFile = File("$rootDir/$FILEPATH_LOCATION")
        val byteConcat: TruncateConcatByteArray = TruncateConcatByteArray(locations.size)
        val startTime = getStartTime()
        // byteConcat.appendLong(DATA_VERSION, 4)
        locations.forEach { (time, latitude, longitude, altitude, _) ->
            val timeSinceActivityStarted = time - startTime
            byteConcat.appendLong(timeSinceActivityStarted, 27)
            byteConcat.appendDouble(latitude, 39)
            byteConcat.appendDouble(longitude, 39)
            byteConcat.appendDouble(altitude, 39)
        }
    }

    override suspend fun clearAll() {
        TODO("Not yet implemented")
    }

    override suspend fun getLocations(skip: Int): List<Location> {
        TODO("Not yet implemented")
    }

    override suspend fun getLastLocationTime(): Long {
        TODO("Not yet implemented")
    }

    private abstract class LocationSerializer(val size: Int) {
        abstract val byteConcat: TruncateConcatByteArray
        abstract fun append(location: Location): ByteArray
        abstract fun deserialize(byteArray: ByteArray): Location
    }

    private class LocationSerializerV1() : LocationSerializer(18) {
        override val byteConcat: TruncateConcatByteArray = TruncateConcatByteArray(18)
        override fun append(location: Location): ByteArray {
            TODO("Not yet implemented")
        }

        override fun deserialize(byteArray: ByteArray): Location {
            TODO("Not yet implemented")
        }

    }

    companion object {
        private const val DATA_VERSION: Byte = 1
        private const val FILEPATH_ROOT = "akhoi.libs.tracking.data_store"
        private const val FILEPATH_LOCATION = "locations"
        private const val FILEPATH_DATA_VERSION = "version"

        private const val PROPKEY_ACTIVITY_START_ELAPSED_TIME = "PROPKEY_ACTIVITY_START_ELAPSED_TIME"
        private const val PROPKEY_ACTIVITY_START_TIME = "PROPKEY_ACTIVITY_START_TIME"
        private const val PROPKEY_ACTIVITY_ID = "PROPKEY_ACTIVITY_ID"
    }
}
