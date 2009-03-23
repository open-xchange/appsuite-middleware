
package com.openexchange.subscribe.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.SubscribeServiceImpl;
import com.openexchange.subscribe.SubscriptionHandler;
import com.openexchange.subscribe.ExternalSubscriptionHandler;
import com.openexchange.subscribe.ExternalSubscriptionService;
import com.openexchange.subscribe.ExternalSubscriptionServiceImpl;
import com.openexchange.subscribe.parser.MicroformatContactParser;
import com.openexchange.subscribe.parser.ExternalSubscriptionHandlerImpl;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Activator implements BundleActivator {

    private ServiceRegistration registryPublish;

    private ServiceRegistration handlerPublish;

    private ServiceRegistration xingHandlerPublish;

    private ServiceRegistration xingRegistryPublish;

    public void start(BundleContext context) throws Exception {
        registryPublish = context.registerService(SubscribeService.class.getName(), new SubscribeServiceImpl(), null);
        handlerPublish = context.registerService(SubscriptionHandler.class.getName(), new MicroformatContactParser(), null);
        
        xingHandlerPublish = context.registerService(ExternalSubscriptionHandler.class.getName(), new ExternalSubscriptionHandlerImpl(), null);
        xingRegistryPublish = context.registerService(ExternalSubscriptionService.class.getName(), new ExternalSubscriptionServiceImpl(), null);
        
    }

    public void stop(BundleContext context) throws Exception {
        registryPublish.unregister();
        handlerPublish.unregister();
        xingHandlerPublish.unregister();
        xingRegistryPublish.unregister();
    }

}
