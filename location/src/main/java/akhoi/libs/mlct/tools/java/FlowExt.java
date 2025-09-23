package akhoi.libs.mlct.tools.java;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Job;

public final class FlowExt {
    private FlowExt() {
    }

    public static Job flowTimer(
            CoroutineScope scope,
            long initialDelay,
            long period,
            Function2<? super CoroutineScope, ? super Continuation<? super Unit>, ? extends Object> action
    ) {
        return akhoi.libs.mlct.tools.FlowExtKt.flowTimer(scope, initialDelay, period, action);
    }
}
