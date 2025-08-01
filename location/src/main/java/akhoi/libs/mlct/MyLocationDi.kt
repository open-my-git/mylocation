package akhoi.libs.mlct

import akhoi.libs.mlct.location.LocationSource
import akhoi.libs.mlct.location.impl.LocationSourceImpl
import akhoi.libs.mlct.tracking.TrackingManager
import akhoi.libs.mlct.tracking.TrackingService
import akhoi.libs.mlct.tracking.impl.TrackingManagerImpl
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

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
    @Binds
    fun trackingManager(impl: TrackingManagerImpl): TrackingManager
}

@Component(modules = [LocationModule::class, TrackingModule::class])
internal interface MyLocationComponent {
    fun inject(trackingService: TrackingService)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance appContext: Context): MyLocationComponent
    }
}