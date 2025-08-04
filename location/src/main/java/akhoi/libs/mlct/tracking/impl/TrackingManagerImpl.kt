package akhoi.libs.mlct.tracking.impl

import akhoi.libs.mlct.location.model.Location
import akhoi.libs.mlct.tools.KeyValuePreferences
import akhoi.libs.mlct.tools.flowTimer
import akhoi.libs.mlct.tools.get
import akhoi.libs.mlct.tracking.TrackingManager
import akhoi.libs.mlct.tracking.TrackingStateKeys.KEY_CA_START_TIME
import akhoi.libs.mlct.tracking.TrackingStateKeys.KEY_DATA_VERSION
import akhoi.libs.mlct.tracking.TrackingStateKeys.KEY_EL_START_TIME
import akhoi.libs.mlct.tracking.TrackingStateKeys.KEY_LAST_ACTIVE_TIME
import akhoi.libs.mlct.tracking.TrackingStateKeys.KEY_PAUSED_DURATION
import akhoi.libs.mlct.tracking.TrackingStateKeys.KEY_ROUTE_DISTANCE
import akhoi.libs.mlct.tracking.TrackingStateKeys.KEY_SPEED
import akhoi.libs.mlct.tracking.TrackingStateKeys.KEY_STATUS
import android.content.Context
import android.os.SystemClock
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import java.io.File
import java.io.FileOutputStream

internal class TrackingManagerImpl(
    context: Context,
    private val stateProperties: KeyValuePreferences
) : TrackingManager {
    private val rootDir by lazy { File("${context.filesDir}/${DataFiles.DIR_ROOT}/$DATA_DIR") }

    private val locationFile: File = File("$rootDir/locations")
    private var locationOutputStream: FileOutputStream? = null

    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val locationSerializers = mutableMapOf<Byte, LocationSerializer>()

    private val distanceCalculator = DistanceCalculator()

    private var tickerJob: Job? = null

    override fun start() {
        stateProperties[KEY_CA_START_TIME] = System.currentTimeMillis()
        val startTime = SystemClock.elapsedRealtime()
        stateProperties[KEY_EL_START_TIME] = startTime
        stateProperties[KEY_DATA_VERSION] = DATA_VERSION

        locationSerializers.clear()
        locationSerializers[DATA_V1] = TrackingLocationSerializerV1(startTime)

        stateProperties[KEY_STATUS] = TrackingStatus.RESUMED
        openLocationFile()
        updateLatestActiveTime()
        startTicker()
    }

    override fun pause() {
        stateProperties[KEY_STATUS] = TrackingStatus.PAUSED
        updateLatestActiveTime()
        closeLocationFile()
        distanceCalculator.clearLastComputedLocation()
        tickerJob?.cancel()
    }

    override fun resume() {
        openLocationFile()
        addPausedDuration()
        stateProperties[KEY_STATUS] = TrackingStatus.RESUMED
        startTicker()
    }

    private fun addPausedDuration() {
        val pausedDuration = stateProperties[KEY_PAUSED_DURATION] ?: 0L
        val lastActiveTime = stateProperties[KEY_LAST_ACTIVE_TIME] ?: 0L
        stateProperties[KEY_PAUSED_DURATION] =
            pausedDuration + SystemClock.elapsedRealtime() - lastActiveTime
        updateLatestActiveTime()
    }

    override fun stop() {
        stateProperties[KEY_STATUS] = TrackingStatus.STOPPED
        updateLatestActiveTime()
        tickerJob?.cancel()
    }

    private fun updateLatestActiveTime() {
        stateProperties[KEY_LAST_ACTIVE_TIME] = SystemClock.elapsedRealtime()
    }

    override suspend fun recordLocations(locations: List<Location>) {
        if (locations.isEmpty()) {
            return
        }

        storeLocationData(locations)

        stateProperties[KEY_SPEED] = locations.last().speed

        addRouteDistance(locations)
    }

    private fun addRouteDistance(locations: List<Location>) {
        val distanceToAdd = distanceCalculator.calculateDistanceTo(locations)
        val routeDistance = stateProperties[KEY_ROUTE_DISTANCE] ?: 0L
        stateProperties[KEY_ROUTE_DISTANCE] = routeDistance + distanceToAdd
    }

    private fun openLocationFile() {
        locationOutputStream?.close()
        locationOutputStream = FileOutputStream(locationFile, true)
    }

    private fun closeLocationFile() {
        locationOutputStream?.close()
    }

    private fun storeLocationData(locations: List<Location>) {
        val dataVersion = stateProperties[KEY_DATA_VERSION] ?: DATA_VERSION
        val byteArray = locationSerializers[dataVersion]?.serialize(locations) ?: ByteArray(0)
        locationOutputStream?.write(byteArray)
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = mainScope.flowTimer(TICKER_PERIOD, TICKER_PERIOD) {
            updateLatestActiveTime()
        }
    }

    private inner class DistanceCalculator {
        private var lastComputedLocation: Location? = null

        fun calculateDistanceTo(locations: List<Location>): Double {
            val joinedLocations = mutableListOf<Location>()

            // add last location of the previous batch to compute distance to the first location of this batch
            lastComputedLocation?.let(joinedLocations::add)
            joinedLocations.addAll(locations)

            val locationUpdateDistance = SphericalUtil.computeLength(
                joinedLocations.map { LatLng(it.latitude, it.longitude) }
            )

            // remember the last location of this batch for later compute
            lastComputedLocation = locations.lastOrNull()

            return locationUpdateDistance
        }

        fun clearLastComputedLocation() {
            lastComputedLocation = null
        }
    }

    companion object {
        private const val DATA_DIR = "manager"

        private const val DATA_V1: Byte = 1
        private const val DATA_VERSION: Byte = DATA_V1

        private const val TICKER_PERIOD = 1000L

    }
}