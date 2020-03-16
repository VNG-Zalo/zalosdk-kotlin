package me.zalo.startuphelper;

import android.os.Build;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TaskExecutor
 * Created by khanhtm on 3/14/18.
 */

class TaskExecutor {
    private static ThreadPoolExecutor mExecutor;

    static {
        mExecutor = new ThreadPoolExecutor(1, 1,
                30L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(100));
        mExecutor.setThreadFactory(new MyThreadFactory("TaskExecutor"));
        if (Build.VERSION.SDK_INT >= 9) {
            mExecutor.allowCoreThreadTimeOut(true);
        }
    }

    public static void queueRunnable(Runnable runnable) {
        mExecutor.execute(runnable);
    }

    static class MyThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public MyThreadFactory(String poolName) {
            namePrefix = "pool-" + poolName + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            t.setPriority(Thread.MAX_PRIORITY);
            return t;
        }
    }
}
