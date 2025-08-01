package akhoi.libs.mlct.tracking.impl;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@IntDef({ TrackingStatus.STOPPED, TrackingStatus.RESUMED, TrackingStatus.PAUSED })
@Retention(SOURCE)
@Target({ ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.TYPE_USE })
public @interface TrackingStatus {
    int STOPPED = 0;
    int RESUMED = 1;
    int PAUSED = 2;
}
