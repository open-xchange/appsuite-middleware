package com.openexchange.mobileconfig.osgi;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.mobileconfig.MobileConfigServlet;
import com.openexchange.mobileconfig.services.MobileConfigServiceRegistry;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.server.osgiservice.ServiceRegistry;
import com.openexchange.tools.service.ServletRegistration;

public class Activator extends DeferredActivator {

    private static final Log LOG = LogFactory.getLog(Activator.class);

    private static final Class<?>[] NEEDED_SERVICES = {};
    private static final String ALIAS = "/servlet/mobileconfig";

    private List<ServiceTracker> serviceTrackerList;

    private ServletRegistration registration;

    public Activator() {
        serviceTrackerList = new ArrayList<ServiceTracker>();
    }
    
    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        MobileConfigServiceRegistry.getServiceRegistry().addService(clazz, getService(clazz));

        register();
    }

    @Override
    protected void handleUnavailability(Class<?> clazz) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("Absent service: " + clazz.getName());
        }
        MobileConfigServiceRegistry.getServiceRegistry().removeService(clazz);

        unregister();
    }

    @Override
    protected void startBundle() throws Exception {
        {
            final ServiceRegistry registry = MobileConfigServiceRegistry.getServiceRegistry();
            registry.clearRegistry();
            final Class<?>[] classes = getNeededServices();
            for (int i = 0; i < classes.length; i++) {
                final Object service = getService(classes[i]);
                if (null != service) {
                    registry.addService(classes[i], service);
                }
            }
        }
        serviceTrackerList.add(new ServiceTracker(context, HostnameService.class.getName(), new HostnameInstallationServiceListener(context)));
        
        // Open service trackers
        for (final ServiceTracker tracker : serviceTrackerList) {
            tracker.open();
        }

        register();
    }

    @Override
    protected void stopBundle() throws Exception {
        unregister();

        /*
         * Close service trackers
         */
        for (final ServiceTracker tracker : serviceTrackerList) {
            tracker.close();
        }
        serviceTrackerList.clear();

        MobileConfigServiceRegistry.getServiceRegistry().clearRegistry();
    }

    private void register() {
        if(registration != null) {
            return;
        }

        registration = new ServletRegistration(context, new MobileConfigServlet(), ALIAS);
        LOG.info("MobileConfig servlet registered");
    }

    public void unregister() {
        if (registration == null) {
            return;
        }
        registration.remove();
        registration = null;
    }


}
