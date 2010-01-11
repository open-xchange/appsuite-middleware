package com.openexchange.messaging.registry.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.openexchange.messaging.registry.MessagingServiceRegistry;

public class Activator implements BundleActivator {

	private OSGIMessagingServiceRegistry registry;

    public void start(BundleContext context) throws Exception {
	    registry = new OSGIMessagingServiceRegistry(context);
	    registry.start();
	    context.registerService(MessagingServiceRegistry.class.getName(), registry, null);
	}

	
    public void stop(BundleContext context) throws Exception {
        registry.stop();
    }

}
