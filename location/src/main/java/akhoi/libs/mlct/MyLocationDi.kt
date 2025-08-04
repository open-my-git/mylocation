package akhoi.libs.mlct

import akhoi.libs.mlct.location.LocationSource
import akhoi.libs.mlct.location.impl.LocationSourceImpl
import akhoi.libs.mlct.tools.KeyValuePreferences
import akhoi.libs.mlct.tracking.TrackingManager
import akhoi.libs.mlct.tracking.TrackingService
import akhoi.libs.mlct.tracking.TrackingStateReaderFactory
import akhoi.libs.mlct.tracking.impl.TrackingManagerImpl
import akhoi.libs.mlct.tracking.impl.TrackingStatePrefsFactory
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
internal interface LocationModule {
    @Binds
    fun locationSource(impl: LocationSourceImpl): LocationSource

    companion object {
        @Provides
        fun locationClient(context: Context): FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)
    }
}

@Module
internal interface TrackingModule {
    companion object {
        const val TRACKING_STATE_PREFS = "TRACKING_STATE_PREFS"
        @Named(TRACKING_STATE_PREFS)
        @Provides
        fun trackingStateProperties(context: Context): KeyValuePreferences =
            TrackingStatePrefsFactory.getStatePreferences(context)

        @Provides
        fun trackingManager(
            context: Context,
            @Named(TRACKING_STATE_PREFS)
            stateProperties: KeyValuePreferences
        ): TrackingManager = TrackingManagerImpl(context, stateProperties)
    }
}

@Component(modules = [LocationModule::class, TrackingModule::class])
internal interface MyLocationComponent {
    fun inject(trackingService: TrackingService)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance appContext: Context): MyLocationComponent
    }
}