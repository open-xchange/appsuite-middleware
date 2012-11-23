
package com.openexchange.test.osgi.internal;

import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.test.osgi.OSGiTest;

public class Activator extends HousekeepingActivator {

    static OSGiTest test = null;
    
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { OSGiTest.class };
    }

    
    public Activator() {
      super();
      String timeoutValueString = System.getProperty("com.openexchange.test.osgi.timeout.value", "120000");
      Long timeoutValue = Long.valueOf(timeoutValueString).longValue();
      
      CheckThread myCheckThread = new CheckThread(timeoutValue);
      myCheckThread.start();
      
    }
    
    
    @Override
    protected void startBundle() throws Exception {
        test = getService(OSGiTest.class);
        OSGiTestRunner testRunner = new OSGiTestRunner(test);
        testRunner.start();
    }

}
class CheckThread extends Thread {
    
    Long timeoutValue = 0L;
    
    public CheckThread(Long timeoutValue) {
        this.timeoutValue = timeoutValue;
    }
    
    
    public void run() {
        Long endTime = System.currentTimeMillis() + timeoutValue;
        System.out.println("mythread started with timeout of " + timeoutValue);
        do {
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                
            }
        } while (Activator.test == null && System.currentTimeMillis() < endTime) ;
        
        if (Activator.test == null) {
            System.out.println("No test classes found after timeout of " + timeoutValue + " milliseconds.");
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                
            }
            
            System.exit(0);
        }
    }
  
  }