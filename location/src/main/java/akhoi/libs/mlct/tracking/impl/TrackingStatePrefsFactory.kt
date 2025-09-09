package akhoi.libs.mlct.tracking.impl

import akhoi.libs.mlct.tools.FileNameProperties
import akhoi.libs.mlct.tools.FileWatcher
import akhoi.libs.mlct.tools.KeyValuePreferences
import android.content.Context

internal object TrackingStatePrefsFactory {
    fun getStatePreferences(context: Context): KeyValuePreferences =
        FileNameProperties(DataFiles.getRootDir(context))
}