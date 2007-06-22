
package com.openexchange.admin.tools.monitoring;

/**
 *
 * @author cutmasta
 */
public class MonitoringInfos {
    
    
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
    
    public static long getNumberOfCreateResourceCalled(){
        return numberOfCreateResourceCalled;
    }
    
    public static long getNumberOfCreateContextCalled(){
        return numberOfCreateContextCalled;
    }
    
    public static long getNumberOfCreateUserCalled(){
        return numberOfCreateUserCalled;
    }
    
    public static long getNumberOfCreateGroupCalled(){
        return numberOfCreateGroupCalled;
    }
    
}
