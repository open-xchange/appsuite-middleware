
package com.openexchange.test.osgi.internal;

import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.test.osgi.OSGiTest;

public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { OSGiTest.class };
    }

    @Override
    protected void startBundle() throws Exception {
        OSGiTestRunner testRunner = new OSGiTestRunner(getService(OSGiTest.class));
        testRunner.start();
    }

}
