package akhoi.libs.mlct.tracking

import akhoi.libs.mlct.tools.KeyValuePrefsReader
import akhoi.libs.mlct.tracking.impl.TrackingStatePrefsFactory
import android.content.Context

object TrackingStateReaderFactory {
    fun getStateReader(context: Context): KeyValuePrefsReader =
        TrackingStatePrefsFactory.getStatePreferences(context)
}
