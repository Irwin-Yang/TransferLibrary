package com.irwin.app;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Irwin on 2015/11/24.
 */
public class ThreadExecutor {

    public static ThreadExecutor INSTANCE;

    private static ScheduledExecutorService SCHEDULE_EXECUTOR;

    private static ExecutorService EXECUTOR;

    private static Handler HANDLER;


    public static ThreadExecutor setup(int size) {
        if (INSTANCE == null) {
            INSTANCE = new ThreadExecutor(size);
        }
        return INSTANCE;
    }

    /**
     * Return Instance. make sure you have setup the ThreadExecutor
     * by call {@link #setup(int)}
     *
     * @return
     */
    public static ThreadExecutor getInstance() {
        return INSTANCE;
    }

    private ThreadExecutor(int size) {
        EXECUTOR = size > 0 ? Executors.newFixedThreadPool(size) : Executors.newCachedThreadPool();
        HANDLER = new Handler(Looper.getMainLooper());
    }

    public void execute(Runnable runnable) {
        EXECUTOR.execute(runnable);
    }

    /**
     * Execute a delayed runnable.
     *
     * @param runnable
     * @param delayMillis Delay time in millisecond.
     */
    public synchronized void executeDelay(Runnable runnable, long delayMillis) {
        if (SCHEDULE_EXECUTOR == null) {
            SCHEDULE_EXECUTOR = Executors.newScheduledThreadPool(1);
        }
        SCHEDULE_EXECUTOR.schedule(runnable, delayMillis, TimeUnit.MILLISECONDS);
    }

    public void executeOnUI(Runnable runnable) {
        HANDLER.post(runnable);
    }

    /**
     * Execute a delayed runnable on ui thread.
     *
     * @param runnable
     * @param delayMillis Delay time in millisecond.
     */
    public void executeOnUIDelay(Runnable runnable, long delayMillis) {
        HANDLER.postDelayed(runnable, delayMillis);
    }

}
