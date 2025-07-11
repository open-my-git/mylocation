package akhoi.libs.location

import akhoi.libs.location.impl.LocationSourceImpl
import android.content.Context

object LocationSourceFactory {
    fun createLocationSource(context: Context): LocationSource = LocationSourceImpl(context)
}