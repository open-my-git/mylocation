package akhoi.libs.mlct.tracking.impl

import android.content.Context
import java.io.File

internal object DataFiles {
    val namespace = "akhoi.libs.mlct"

    val DIR_ROOT = "tracking"
    val FILE_TRACKING_STATE = "state"
    val FILE_TRACKING_LOCATION = "locations"

    fun getRootDir(context: Context): File =
        File("${context.filesDir}/$namespace/$DIR_ROOT")
}