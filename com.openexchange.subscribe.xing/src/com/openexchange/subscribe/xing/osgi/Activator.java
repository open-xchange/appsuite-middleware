
package com.openexchange.subscribe.xing.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.ServiceDependentRegistration;
import com.openexchange.server.osgiservice.Whiteboard;
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
