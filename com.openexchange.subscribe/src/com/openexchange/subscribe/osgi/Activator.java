
package com.openexchange.subscribe.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.SubscribeServiceImpl;
import com.openexchange.subscribe.SubscriptionHandler;
import com.openexchange.subscribe.parser.MicroformatContactParser;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Activator implements BundleActivator {

    private ServiceRegistration registryPublish;
    private ServiceRegistration handlerPublish;

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        registryPublish = context.registerService(SubscribeService.class.getName(), new SubscribeServiceImpl(), null);
        handlerPublish =  context.registerService(SubscriptionHandler.class.getName(), new MicroformatContactParser(), null);
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        registryPublish.unregister();
        handlerPublish.unregister();
    }

}
