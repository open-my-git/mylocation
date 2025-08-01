package akhoi.libs.mlct.tracking.impl

import akhoi.libs.mlct.location.model.Location
import akhoi.libs.mlct.tools.FileNameProperties
import akhoi.libs.mlct.tools.flowTimer
import akhoi.libs.mlct.tracking.TrackingManager
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
import javax.inject.Inject
import kotlin.collections.set

internal class TrackingManagerImpl @Inject constructor(private val context: Context) : TrackingManager {
    private val rootDir by lazy { File("${context.filesDir}/$ROOT_DIR") }

    private val properties by lazy { FileNameProperties(rootDir, "properties") }

    private val locationFile: File = File("$rootDir/locations")
    private var locationOutputStream: FileOutputStream? = null

    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val status: @TrackingStatus Int
        get() = properties[KEY_STATUS] ?: TrackingStatus.STOPPED

    override val startTimeElapsed: Long?
        get() = properties[KEY_START_TIME_EL]

    override val startTimeCalendar: Long?
        get() = properties[KEY_START_TIME_CA]

    override val currentSpeed: Float
        get() = properties[KEY_SPEED] ?: 0.0f

    override var routeDistance: Double
        get() = properties[KEY_ROUTE_DISTANCE] ?: 0.0
        set(value) { properties[KEY_ROUTE_DISTANCE] = value }

    override var lastActiveTime: Long?
        get() = properties[KEY_LAST_ACTIVE_TIME]
        set(value) { properties[KEY_LAST_ACTIVE_TIME] = value }

    override var pausedDuration: Long
        get() = properties[KEY_PAUSED_DURATION] ?: 0L
        set(value) { properties[KEY_PAUSED_DURATION] = value }

    private val locationSerializers = mutableMapOf<Byte, LocationSerializer>()

    private val distanceCalculator = DistanceCalculator()

    private var tickerJob: Job? = null

    override fun start() {
        properties.put(KEY_START_TIME_CA, System.currentTimeMillis())
        val startTime = SystemClock.elapsedRealtime()
        properties.put(KEY_START_TIME_EL, startTime)
        properties.put(KEY_DATA_VERSION, DATA_VERSION)

        locationSerializers.clear()
        locationSerializers[DATA_V1] = TrackingLocationSerializerV1(startTime)

        properties[KEY_STATUS] = TrackingStatus.RESUMED
        openLocationFile()
        updateLatestActiveTime()
        startTicker()
    }

    override fun pause() {
        properties[KEY_STATUS] = TrackingStatus.PAUSED
        updateLatestActiveTime()
        closeLocationFile()
        distanceCalculator.clearLastComputedLocation()
        tickerJob?.cancel()
    }

    override fun resume() {
        openLocationFile()
        addPausedDuration()
        properties[KEY_STATUS] = TrackingStatus.RESUMED
        startTicker()
    }

    private fun addPausedDuration() {
        pausedDuration += SystemClock.elapsedRealtime() - (lastActiveTime ?: 0L)
        updateLatestActiveTime()
    }

    override fun stop() {
        properties[KEY_STATUS] = TrackingStatus.STOPPED
        updateLatestActiveTime()
        tickerJob?.cancel()
    }

    private fun updateLatestActiveTime() {
        lastActiveTime = SystemClock.elapsedRealtime()
    }

    override suspend fun recordLocations(locations: List<Location>) {
        if (locations.isEmpty()) {
            return
        }

        storeLocationData(locations)

        properties.put(KEY_SPEED, locations.last().speed)

        addRouteDistance(locations)
    }

    private fun addRouteDistance(locations: List<Location>) {
        val distanceToAdd = distanceCalculator.calculateDistanceTo(locations)
        routeDistance += distanceToAdd
    }

    private fun openLocationFile() {
        locationOutputStream?.close()
        locationOutputStream = FileOutputStream(locationFile, true)
    }

    private fun closeLocationFile() {
        locationOutputStream?.close()
    }

    private suspend fun storeLocationData(locations: List<Location>) {
        val dataVersion = properties.get<Byte>(KEY_DATA_VERSION) ?: DATA_VERSION
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
        private const val TAG = "TrackingManager"

        private const val DATA_V1: Byte = 1
        private const val DATA_VERSION: Byte = DATA_V1

        private const val ROOT_DIR = "akhoi.libs.mlct.tracking_state"

        private const val TICKER_PERIOD = 1000L

        private const val KEY_START_TIME_CA = "KEY_CREATED_TIME"
        private const val KEY_START_TIME_EL = "KEY_START_TIME"
        private const val KEY_DATA_VERSION = "KEY_DATA_VERSION"
        private const val KEY_SPEED = "KEY_SPEED"
        private const val KEY_ROUTE_DISTANCE = "KEY_ROUTE_DISTANCE"
        private const val KEY_STATUS = "KEY_STATUS"
        private const val KEY_PAUSED_DURATION = "KEY_PAUSE_DURATION"
        private const val KEY_LAST_ACTIVE_TIME = "KEY_LAST_ACTIVE_TIME"
    }
}