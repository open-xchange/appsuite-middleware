
package com.openexchange.publish.microformats.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.microformats.DummyContactMFPublisher;

public class Activator implements BundleActivator {

    private ServiceRegistration registerService;

    public void start(BundleContext context) throws Exception {
        registerService = context.registerService(PublicationService.class.getName(), new DummyContactMFPublisher(), null);
        
    }

    public void stop(BundleContext context) throws Exception {
        registerService.unregister();
    }



    
}
