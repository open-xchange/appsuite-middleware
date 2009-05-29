package com.openexchange.subscribe.xing.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.xing.XingSubscribeService;
import com.openexchange.subscribe.xing.XingSubscriptionErrorMessage;

public class Activator implements BundleActivator {

	private ServiceRegistration registration;
    private ComponentRegistration componentRegistration;

    public void start(BundleContext context) throws Exception {
        SubscribeService service = new XingSubscribeService();
	    registration = context.registerService(SubscribeService.class.getName(), service, null);
	    componentRegistration = new ComponentRegistration(context, "XING", "com.openexchange.subscribe.xing", XingSubscriptionErrorMessage.EXCEPTIONS);
    }

	public void stop(BundleContext context) throws Exception {
	    registration.unregister();
	    componentRegistration.unregister();
	}

}
