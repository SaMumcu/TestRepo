package com.gradientinsight.gallery01;

import android.util.Log;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleWorker extends Thread {

    public static final String TAG = "SimpleWorker";
    private AtomicBoolean alive = new AtomicBoolean(true);
    private ConcurrentLinkedDeque<Runnable> tasksQueue = new ConcurrentLinkedDeque<>();

    public SimpleWorker() {
        super(TAG);
        start();
    }

    @Override
    public void run() {
        while (alive.get()) {
            Runnable task = tasksQueue.poll();
            if (task != null) {
                task.run();
            }
        }
        Log.d(TAG, " terminated.");
    }

    public SimpleWorker execute(Runnable task) {
        tasksQueue.add(task);
        return this;
    }

    public void quit() {
        alive.set(false);
    }
}
