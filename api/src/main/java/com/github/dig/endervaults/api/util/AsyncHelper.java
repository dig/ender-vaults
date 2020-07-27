package com.github.dig.endervaults.api.util;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@UtilityClass
public class AsyncHelper {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    public static ExecutorService executor() {
        return executorService;
    }

    public static ScheduledExecutorService scheduledExecutor() {
        return scheduledExecutorService;
    }
}
