package com.openexchange.realtime.hazelcast.group.helper;

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

    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    @Override
    public boolean isShutdown() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isTerminated() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T> Future<T> submit(Task<T> task) {
        System.out.println("Submitting task: "+ task);
        return executor.submit(task);
    }

    @Override
    public <T> Future<T> submit(Task<T> task, RefusedExecutionBehavior<T> refusedExecutionBehavior) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Task<T>> tasks) throws InterruptedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Task<T>> tasks, long timeout) throws InterruptedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> CompletionFuture<T> invoke(Collection<? extends Task<T>> tasks) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> CompletionFuture<T> invoke(Task<T>[] tasks) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> CompletionFuture<T> invoke(Collection<? extends Task<T>> tasks, RefusedExecutionBehavior<T> refusedExecutionBehavior) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ExecutorService getExecutor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ExecutorService getFixedExecutor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ExecutorService getFixedExecutor(int size) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getPoolSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getActiveCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getLargestPoolSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getTaskCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getCompletedTaskCount() {
        // TODO Auto-generated method stub
        return 0;
    }

}
