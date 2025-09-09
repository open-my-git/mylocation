package akhoi.libs.mlct

import akhoi.libs.mlct.tools.KeyValuePrefsReader
import akhoi.libs.mlct.tracking.TrackingStateReaderFactory
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Module
interface MainModule {
    companion object {
        @Provides
        fun trackingStateProperties(context: Context): KeyValuePrefsReader =
            TrackingStateReaderFactory.getStateReader(context)
    }
}

@Component(modules = [MainModule::class])
interface MainComponent {
    fun mainViewModel(): MainViewModel

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): MainComponent
    }
}
