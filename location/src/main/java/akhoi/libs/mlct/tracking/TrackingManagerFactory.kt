package akhoi.libs.mlct.tracking

import akhoi.libs.mlct.tracking.impl.TrackingManagerImpl
import android.content.Context

object TrackingManagerFactory {
    fun createInstance(context: Context): TrackingManager =
        TrackingManagerImpl(context)
}