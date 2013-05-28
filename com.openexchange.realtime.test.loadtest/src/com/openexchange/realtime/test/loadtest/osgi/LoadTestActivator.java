
package com.openexchange.realtime.test.loadtest.osgi;

import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.Component;
import com.openexchange.realtime.test.loadtest.LoadTestComponent;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class LoadTestActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return null;
    }

    @Override
    protected void startBundle() throws Exception {
        // Register a component for creating a chat room
        this.registerService(Component.class, new LoadTestComponent(this));
    }
}
