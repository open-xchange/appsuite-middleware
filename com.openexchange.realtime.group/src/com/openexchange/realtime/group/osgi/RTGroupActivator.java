package com.openexchange.realtime.group.osgi;

import org.osgi.framework.BundleActivator;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.group.GroupDispatcher;


public class RTGroupActivator extends HousekeepingActivator implements BundleActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{MessageDispatcher.class};
    }

    @Override
    protected void startBundle() throws Exception {
        GroupDispatcher.services = this;
    }

}
