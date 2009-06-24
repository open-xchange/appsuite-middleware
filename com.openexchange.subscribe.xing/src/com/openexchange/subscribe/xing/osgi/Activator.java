
package com.openexchange.subscribe.xing.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.xing.XingContactParser;
import com.openexchange.subscribe.xing.XingSubscribeService;
import com.openexchange.subscribe.xing.XingSubscriptionErrorMessage;

public class Activator implements BundleActivator {

    private ComponentRegistration componentRegistration;

    private ServiceRegistration serviceRegistration;

    public void start(BundleContext context) throws Exception {
        componentRegistration = new ComponentRegistration(
            context,
            "XING",
            "com.openexchange.subscribe.xing",
            XingSubscriptionErrorMessage.EXCEPTIONS);

            XingContactParser contactParser = new XingContactParser();
            XingSubscribeService subscribeService = new XingSubscribeService();
            subscribeService.setXingContactParser(contactParser);

            serviceRegistration = context.registerService(SubscribeService.class.getName(), subscribeService, null);
    }

    public void stop(BundleContext context) throws Exception {
        serviceRegistration.unregister();
        componentRegistration.unregister();
    }

 

}
