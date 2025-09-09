package akhoi.libs.mlct

import akhoi.libs.mlct.tools.KeyValuePrefsReader
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val trackingState: KeyValuePrefsReader
) {
    init {
    }
}