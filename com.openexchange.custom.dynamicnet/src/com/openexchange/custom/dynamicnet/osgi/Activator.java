
package com.openexchange.custom.dynamicnet.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.context.ContextService;
import com.openexchange.custom.dynamicnet.impl.DynamicNetAuthentication;
import com.openexchange.custom.dynamicnet.impl.DynamicNetHostnameService;
import com.openexchange.custom.dynamicnet.impl.DynamicNetIFrameServlet;
import com.openexchange.groupware.notify.hostname.HostnameService;

public class Activator implements BundleActivator {

    private static transient final Log LOG = LogFactory.getLog(Activator.class);

    /**
     * Reference to the service registration.
     */
    private ServiceRegistration<AuthenticationService> registration;

    private ServiceRegistration<HostnameService> registration_hostname;

    private ServiceTracker<HttpService, HttpService> st;

    private ServiceTracker<ContextService,ContextService> st_hostname;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final BundleContext context) throws Exception {

        LOG.info("Starting authentication service: dynamic-net");
        registration = context.registerService(AuthenticationService.class,new DynamicNetAuthentication(), null);
        LOG.info("Authentication service dynamic-net started!");

        LOG.info("Starting http service: dynamic-net");
        st = new HTTPServletRegistration(context,"/ajax/iframe_info",new DynamicNetIFrameServlet());

        LOG.info("Starting context service: dynamic-net");
        st_hostname = new ServiceTracker<ContextService,ContextService>(context,ContextService.class,new ContextRegisterer(context));

        LOG.info("Starting hostname service: dynamic-net");
        registration_hostname = context.registerService(HostnameService.class,new DynamicNetHostnameService(), null);
        LOG.info("Hostname service dynamic-net started!");

        st.open();
        st_hostname.open();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        LOG.info("Stopping http service: dynamic-net");
        st.close();

        LOG.info("Stopping context service: dynamic-net");
        st_hostname.close();

        LOG.info("Stopping authentication service: dynamic-net!");
        registration.unregister();
        LOG.info("Authentication service dynamic-net stopped!");

        LOG.info("Stopping hostname service: dynamic-net!");
        registration_hostname.unregister();
        LOG.info("Hostname service dynamic-net stopped!");

    }
}
