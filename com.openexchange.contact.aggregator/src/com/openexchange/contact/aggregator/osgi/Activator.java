package com.openexchange.contact.aggregator.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.server.osgiservice.HousekeepingActivator;

public class Activator extends HousekeepingActivator {

    private static final Class[] NEEDED = {MessagingServiceRegistry.class};
    
    /* (non-Javadoc)
     * @see com.openexchange.server.osgiservice.DeferredActivator#getNeededServices()
     */
    @Override
    protected Class<?>[] getNeededServices() {        
        return NEEDED;
    }

    /* (non-Javadoc)
     * @see com.openexchange.server.osgiservice.DeferredActivator#startBundle()
     */
    @Override
    protected void startBundle() throws Exception {
        // TODO Auto-generated method stub
        
    }

	

}
