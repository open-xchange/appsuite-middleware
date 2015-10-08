package com.openexchange.realtime;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.openexchange.threadpool.CompletionFuture;
import com.openexchange.threadpool.RefusedExecutionBehavior;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;


public class DumbThreadPool implements ThreadPoolService {

    private static ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public <T> Future<T> submit(Task<T> task) {
        return executor.submit(task);
    }

    @Override
    public <T> Future<T> submit(Task<T> task, RefusedExecutionBehavior<T> refusedExecutionBehavior) {
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Task<T>> tasks) throws InterruptedException {
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Task<T>> tasks, long timeout) throws InterruptedException {
        return null;
    }

    @Override
    public <T> CompletionFuture<T> invoke(Collection<? extends Task<T>> tasks) {
        return null;
    }

    @Override
    public <T> CompletionFuture<T> invoke(Task<T>[] tasks) {
        return null;
    }

    @Override
    public <T> CompletionFuture<T> invoke(Collection<? extends Task<T>> tasks, RefusedExecutionBehavior<T> refusedExecutionBehavior) {
        return null;
    }

    @Override
    public ExecutorService getExecutor() {
        return executor;
    }

    @Override
    public ExecutorService getFixedExecutor() {
        return null;
    }

    @Override
    public ExecutorService getFixedExecutor(int size) {
        return null;
    }

    @Override
    public int getPoolSize() {
        return 0;
    }

    @Override
    public int getActiveCount() {
        return 0;
    }

    @Override
    public int getLargestPoolSize() {
        return 0;
    }

    @Override
    public long getTaskCount() {
        return 0;
    }

    @Override
    public long getCompletedTaskCount() {
        return 0;
    }

}
