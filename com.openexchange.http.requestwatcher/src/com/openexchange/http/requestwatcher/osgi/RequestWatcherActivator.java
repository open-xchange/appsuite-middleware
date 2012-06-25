
package com.openexchange.http.requestwatcher.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.http.requestwatcher.internal.RequestWatcherServiceImpl;
import com.openexchange.http.requestwatcher.osgi.services.RequestWatcherService;
import com.openexchange.log.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.timer.TimerService;

public class RequestWatcherActivator extends HousekeepingActivator {

    private static final org.apache.commons.logging.Log LOG = Log.valueOf(LogFactory.getLog(RequestWatcherActivator.class));
    private RequestWatcherServiceImpl requestWatcher;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ConfigurationService.class, TimerService.class };
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.osgi.HousekeepingActivator#handleAvailability(java.lang.Class)
     */
    @Override
    protected void handleAvailability(Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Service " + clazz.getName() + " is available again.");
        }
        Object service = getService(clazz);
        RequestWatcherServiceRegistry.getInstance().addService(clazz, service);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.osgi.HousekeepingActivator#handleUnavailability(java.lang.Class)
     */
    @Override
    protected void handleUnavailability(Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Service " + clazz.getName() + " is no longer available.");
        }
        RequestWatcherServiceRegistry.getInstance().removeService(clazz);
    }

    @Override
    protected void startBundle() throws OXException {
            if (LOG.isInfoEnabled()) {
                LOG.info("Starting RequestWatcher.");
            }
            
            RequestWatcherServiceRegistry requestWatcherServiceRegistry = RequestWatcherServiceRegistry.getInstance();
            
            /*
             * initialize the registry, handleUn/Availability keeps track of services.
             * Otherwise use trackService(ConfigurationService.class) and openTrackers() to let the superclass handle the services.
             */
            initializeServiceRegistry(requestWatcherServiceRegistry);

            requestWatcher = new RequestWatcherServiceImpl();
            registerService(RequestWatcherService.class, requestWatcher);
    }

    @Override
    protected void stopBundle() throws Exception {
        //Stop the Watcher
        requestWatcher.stopWatching();
         
        /*
         * Clear the registry from the services we are tracking.
         * Otherwise use super.stopBundle(); if we let the superclass handle the services.
         */
        RequestWatcherServiceRegistry.getInstance().clearRegistry();
        
        if (LOG.isInfoEnabled()) {
            LOG.info("Unregistering RequestWatcherService");
        }
        unregisterServices();
    }

    /**
     * Initialize the package wide service registry with the services we declared as needed.
     * @param serviceRegistry the registry to fill
     */
    private void initializeServiceRegistry(final RequestWatcherServiceRegistry serviceRegistry) {
        serviceRegistry.clearRegistry();
        Class<?>[] serviceClasses = getNeededServices();
        for (Class<?> serviceClass : serviceClasses) {
            Object service = getService(serviceClass);
            if (service != null) {
                serviceRegistry.addService(serviceClass, service);
            }
        }
    }
    
}

