package akhoi.libs.mlct.tracking

import akhoi.libs.mlct.DaggerMyLocationComponent
import akhoi.libs.mlct.location.LocationSource
import akhoi.libs.mlct.location.model.Location
import akhoi.libs.mlct.location.model.LocationRequest
import akhoi.libs.mlct.tracking.impl.TrackingStatus
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TrackingService : Service() {

    @Inject
    internal lateinit var locationDataSource: LocationSource

    @Inject
    internal lateinit var trackingManager: TrackingManager

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("TrackingService", "exceptionHandler", exception)
    }

    private val mainScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate + exceptionHandler)

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private var locationUpdateJob: Job? = null
    private var locationFilterSpeed: Double = Double.MAX_VALUE

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        DaggerMyLocationComponent.factory().create(this).inject(this)
        super.onCreate()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() = mainScope.launch {
        val locationRequest = LocationRequest(
            interval = 3000,
            minInterval = 1000,
            maxInterval = 6000,
            distance = 10f
        )
        locationUpdateJob?.cancel()
        locationUpdateJob = locationDataSource.getLocationUpdates(locationRequest)
            .map { it.filter { location -> location.speed <= locationFilterSpeed } }
            .onEach(::onLocationUpdate)
            .launchIn(mainScope)
    }

    private suspend fun onLocationUpdate(
        locations: List<Location>,
    ) = withContext(ioDispatcher) {
        Log.d("TrackingService", "onLocationUpdate: ${locations.size}")
        trackingManager.recordLocations(locations)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            mainScope.launch {
                // service restarts after the process was killed
                if (trackingManager.getStatus() == TrackingStatus.RESUMED) {
                    onActionResume(null)
                }
            }
        } else {
            when (intent.action) {
                ACTION_START -> onActionStart(intent)
                ACTION_STOP -> onActionStop()
                ACTION_RESUME -> onActionResume(intent)
                ACTION_PAUSE -> onActionPause()
            }
        }

        return START_STICKY
    }

    private fun onActionStart(intent: Intent) {
        Log.d(TAG, "onActionStart")
        startForegroundWithNotification(intent)
        requestLocationUpdates()
        acquireWakeLock()
        trackingManager.start()
    }

    private fun onActionPause() {
        locationUpdateJob?.cancel()
        trackingManager.pause()
    }

    private fun onActionResume(intent: Intent?) {
        Log.d(TAG, "onActionResume isServiceRestart=${intent == null}")

        // todo: if the intent is null, does the notification still show?
        if (intent != null) {
            startForegroundWithNotification(intent)
        }
        requestLocationUpdates()

        trackingManager.resume()
    }

    private fun startForegroundWithNotification(intent: Intent) {
        val pendingIntent = intent.getParcelableExtra<PendingIntent>(EXT_PENDING_INTENT)
        val channelId = intent.getStringExtra(EXT_NOTIF_CHANNEL_ID)
        val notifId = intent.getIntExtra(EXT_NOTIF_ID, 0)
        val contentTitle = intent.getCharSequenceExtra(EXT_CONTENT_TITLE)
        val smallIcon = intent.getIntExtra(EXT_SMALL_ICON, 0)
        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle(contentTitle)
            .apply {
                if (smallIcon != 0) { setSmallIcon(smallIcon) }
            }
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            startForeground(notifId, notification)
        } else {
            startForeground(
                notifId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        }
        Log.d(TAG, "startForegroundWithNotification")
    }

    private fun onActionStop() {
        Log.d(TAG, "onActionStop")

        locationUpdateJob?.cancel()
        trackingManager.stop()

        releaseWakeLock()
        stopSelf()
    }

    private fun acquireWakeLock() {
        wakeLock =
            (getSystemService(POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RouteTrackingService::lock").apply {
                    acquire()
                }
            }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    class Launcher(
        private val contentIntent: Intent,
        private val channelId: String,
        private val notificationId: Int,
    ) {
        var intentFlag: Int = 0
        var requestCode: Int = 0
        var contentTitle: CharSequence = ""
        var contentText: CharSequence = ""
        @DrawableRes
        var smallIcon: Int = 0

        fun createStartIntent(context: Context): Intent {
            val intent = Intent(context, TrackingService::class.java)
            intent.action = ACTION_START

            val pendingIntent =
                PendingIntent.getActivity(context, requestCode, contentIntent, intentFlag)
            intent.putExtra(EXT_PENDING_INTENT, pendingIntent)

            intent.putExtra(EXT_NOTIF_CHANNEL_ID, channelId)
            intent.putExtra(EXT_NOTIF_ID, notificationId)

            intent.putExtra(EXT_CONTENT_TITLE, contentTitle)
            intent.putExtra(EXT_CONTENT_TEXT, contentText)

            if (smallIcon != 0) {
                intent.putExtra(EXT_SMALL_ICON, smallIcon)
            }

            return intent
        }

        companion object {
            fun createStopIntent(context: Context): Intent {
                val intent = Intent(context, TrackingService::class.java)
                intent.action = ACTION_STOP
                return intent
            }

            fun createPauseIntent(context: Context) =
                Intent(context, TrackingService::class.java).apply {
                    action = ACTION_PAUSE
                }

            fun createResumeIntent(context: Context) =
                Intent(context, TrackingService::class.java).apply {
                    action = ACTION_RESUME
                }
        }
    }

    companion object {
        private const val TAG = "TrackingService"

        private const val ACTION_START = "ACTION_START"
        private const val ACTION_STOP = "ACTION_STOP"
        private const val ACTION_RESUME = "ACTION_RESUME"
        private const val ACTION_PAUSE = "ACTION_PAUSE"

        private const val EXT_PENDING_INTENT = "EXT_PENDING_INTENT"
        private const val EXT_NOTIF_CHANNEL_ID = "EXT_NOTIF_CHANNEL_ID"
        private const val EXT_NOTIF_ID = "EXT_NOTIF_ID"
        private const val EXT_CONTENT_TITLE = "EXT_CONTENT_TITLE"
        private const val EXT_CONTENT_TEXT = "EXT_CONTENT_TEXT"
        private const val EXT_SMALL_ICON = "EXT_SMALL_ICON"
        private const val EXT_LARGE_ICON = "EXT_LARGE_ICON"

        fun startIntent(
            context: Context,
        ): Intent {
            val intent = Intent(context, TrackingService::class.java)
            intent.action = ACTION_START
            return intent
        }



        @Suppress("DEPRECATION")
        fun isTrackingServiceRunning(context: Context): Boolean {
            val activityMan = ContextCompat.getSystemService(context, ActivityManager::class.java)
            val infos = activityMan?.getRunningServices(Int.MAX_VALUE)
            return infos?.any {
                TrackingService::class.java.name == it.service.className
            } == true
        }
    }
}
