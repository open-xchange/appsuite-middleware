package com.openexchange.subscribe.xing.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.xing.XingSubscribeService;

public class Activator implements BundleActivator {

	private ServiceRegistration registration;

    public void start(BundleContext context) throws Exception {
        SubscribeService service = new XingSubscribeService();
	    registration = context.registerService(SubscribeService.class.getName(), service, null);
	}

	public void stop(BundleContext context) throws Exception {
	    registration.unregister();
	}

}
