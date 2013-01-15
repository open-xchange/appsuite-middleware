
package com.openexchange.test.osgi.internal;

import java.util.List;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.console.ServiceState;
import com.openexchange.osgi.console.ServiceStateLookup;
import com.openexchange.test.osgi.OSGiTest;

public class Activator extends HousekeepingActivator {

    static OSGiTest test = null;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { OSGiTest.class,ServiceStateLookup.class };
    }


    public Activator() {
      super();
      String timeoutValueString = System.getProperty("com.openexchange.test.osgi.timeout.value", "120000");
      Long timeoutValue = Long.valueOf(timeoutValueString).longValue();

      CheckThread myCheckThread = new CheckThread(timeoutValue,this);
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
    Activator myActivator;

    public CheckThread(Long timeoutValue,Activator myActivator) {
        this.timeoutValue = timeoutValue;
        this.myActivator = myActivator;
    }


    @Override
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
            final ServiceStateLookup myServiceStateLookup = myActivator.getService(ServiceStateLookup.class);
            if (myServiceStateLookup != null) {
                for (final String name : myServiceStateLookup.getNames()) {
                    final ServiceState state = myServiceStateLookup.determineState(name);
                    final List<String> services = state.getMissingServices();
                    if (!services.isEmpty()) {
                        System.out.println("=====[" + name + " Missing Services ]=====");
                        for (final String string : services) {
                            System.out.println("\t" + string);
                        }

                    }
                }

            }

            System.exit(0);
        }
    }

  }