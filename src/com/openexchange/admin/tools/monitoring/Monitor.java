package com.openexchange.admin.tools.monitoring;

/**
 * 
 * @author cutmasta
 */
public class Monitor implements MonitorMBean {

    private static long numberOfCreateResourceCalled = 0;

    private static long numberOfCreateContextCalled = 0;

    private static long numberOfCreateUserCalled = 0;

    private static long numberOfCreateGroupCalled = 0;

    public synchronized static void incrementNumberOfCreateResourceCalled() {
        numberOfCreateResourceCalled++;
    }

    public synchronized static void incrementNumberOfCreateContextCalled() {
        numberOfCreateContextCalled++;
    }

    public synchronized static void incrementNumberOfCreateUserCalled() {
        numberOfCreateUserCalled++;
    }

    public synchronized static void incrementNumberOfCreateGroupCalled() {
        numberOfCreateGroupCalled++;
    }

    public long getNumberOfCreateResourceCalled() {
        return numberOfCreateResourceCalled;
    }

    public long getNumberOfCreateContextCalled() {
        return numberOfCreateContextCalled;
    }

    public long getNumberOfCreateUserCalled() {
        return numberOfCreateUserCalled;
    }

    public long getNumberOfCreateGroupCalled() {
        return numberOfCreateGroupCalled;
    }

}
