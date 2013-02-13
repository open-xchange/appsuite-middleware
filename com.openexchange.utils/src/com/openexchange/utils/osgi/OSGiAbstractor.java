
package com.openexchange.utils.osgi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.server.ServiceLookup;
import com.openexchange.utils.propertyhandling.PropertyHandler;
import com.openexchange.utils.propertyhandling.PropertyInterface;


/**
 * This class aims to help keep away the OSGi complexity from bundle implementing parties
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class OSGiAbstractor implements ServiceLookup {

    private class Logger implements Log {

        private final Log delegate;

        private final String prefixString;

        public Logger(final Log log, final Class<?> clazz) {
            this.delegate = log;
            this.prefixString = "Logged for " + clazz.getCanonicalName() + ": ";
        }

        public boolean isDebugEnabled() {
            return this.delegate.isDebugEnabled();
        }

        public boolean isErrorEnabled() {
            return this.delegate.isErrorEnabled();
        }

        public boolean isFatalEnabled() {
            return this.delegate.isFatalEnabled();
        }

        public boolean isInfoEnabled() {
            return this.delegate.isInfoEnabled();
        }

        public boolean isTraceEnabled() {
            return this.delegate.isTraceEnabled();
        }

        public boolean isWarnEnabled() {
            return this.delegate.isWarnEnabled();
        }

        public void trace(Object message) {
            this.delegate.trace(prefixString + message);
        }

        public void trace(Object message, Throwable t) {
            this.delegate.trace(prefixString + message, t);
        }

        public void debug(Object message) {
            this.delegate.debug(prefixString + message);
        }

        public void debug(Object message, Throwable t) {
            this.delegate.debug(prefixString + message, t);
        }

        public void info(Object message) {
            this.delegate.info(prefixString + message);
        }

        public void info(Object message, Throwable t) {
            this.delegate.info(prefixString + message, t);
        }

        public void warn(Object message) {
            this.delegate.warn(prefixString + message);
        }

        public void warn(Object message, Throwable t) {
            this.delegate.warn(prefixString + message, t);
        }

        public void error(Object message) {
            this.delegate.error(prefixString + message);
        }

        public void error(Object message, Throwable t) {
            this.delegate.error(prefixString + message, t);
        }

        public void fatal(Object message) {
            this.delegate.fatal(prefixString + message);
        }

        public void fatal(Object message, Throwable t) {
            this.delegate.fatal(prefixString + message, t);
        }

    }

    private class Entry<T> extends SimpleEntry<T> {

        private AvailabilityActivationClosure<T> closure;

        private final boolean required;

        public Entry(final Class<T> clazz, final boolean required) {
            super(clazz);
            this.required = required;
        }

        public Entry(final Class<T> clazz, final boolean required, final AvailabilityActivationClosure<T> closure) {
            super(clazz);
            this.required = required;
            this.closure = closure;
        }

        public AvailabilityActivationClosure<T> getClosure() {
            return closure;
        }

        public boolean isRequired() {
            return required;
        }

    }
    
    private class ServiceEntry {

        private final Class<?> clazz;

        private final SimpleEntry<?>[] dependingServices;

        private final Dictionary<String, ?> dictionary;

        private final PropertyInterface[] properties;

        private final AbstractInitializer service;

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public ServiceEntry(final Class<?> clazz, final AbstractInitializer service, final Dictionary<String, ?> dictionary, final Class<?>[] dependingServices, final PropertyInterface[] properties) {
            super();
            this.clazz = clazz;
            this.service = service;
            this.dictionary = dictionary;
            this.dependingServices = new SimpleEntry<?>[dependingServices.length];
            for (int i = 0; i < dependingServices.length; i++) {
                this.dependingServices[i] = new SimpleEntry(dependingServices[i]);
            }
            this.properties = properties;
        }

        public String getClassName() {
            return clazz.getName();
        }

        public SimpleEntry<?>[] getDependingServices() {
            return dependingServices;
        }

        public Dictionary<String, ?> getDictionary() {
            return dictionary;
        }

        public PropertyInterface[] getProperties() {
            return properties;
        }

        public AbstractInitializer getService() {
            return service;
        }

    }
    
    private class SimpleEntry<T> {

        private final Class<T> clazz;

        public SimpleEntry(final Class<T> clazz) {
            this.clazz = clazz;
        }

        public Class<T> getClazz() {
            return clazz;
        }
    }

    private static Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(OSGiAbstractor.class));

    private static ServiceRegistry registry;

    private final static List<ServiceTracker<Object, Object>> serviceTrackers = new ArrayList<ServiceTracker<Object,Object>>();

    private static AtomicBoolean started = new AtomicBoolean(false);

    /**
     * @param clazz
     * @return
     */
    public static <S> S getServiceStatic(final Class<? extends S> clazz) {
        if (started.get()) {
            return registry.getService(clazz);
        } else {
            return null;
        }
    }
    
    private final List<Entry<?>> bundleMap = new ArrayList<Entry<?>>();

    private final BundleContext context;

    private final List<ServiceEntry> registrations = new ArrayList<ServiceEntry>();

    private final AtomicBoolean shutdownActivated = new AtomicBoolean(false);

    /**
     * Initializes the helper class with a {@link BundleContext} object
     * @param context The {@link BundleContext} given in the {@link BundleActivator}
     * @param clazz TODO
     */
    public OSGiAbstractor(final BundleContext context, final Class<?> clazz) {
        if (null == clazz) {
            throw new RuntimeException("No class given to OSGiAbstractor constructor");
        }
        if (null == context) {
            throw new RuntimeException("No BundleContext given to OSGiAbstractor constructor");
        }
        
        // Change LOG to the correct class for distinguishing in the logs
        LOG = new Logger(LOG, clazz);
        this.context = context;
    }

    public <T> boolean add(final Class<T> clazz, final boolean required) {
        return bundleMap.add(new Entry<T>(clazz, required));
    }

    public <T> boolean add(final Class<T> clazz, final boolean required, final AvailabilityActivationClosure<T> closure) {
        return bundleMap.add(new Entry<T>(clazz, required, closure));
    }

    /**
     * A convenience method which calls {@link #addService(Class, AbstractInitializer, Dictionary, Class[], PropertyInterface[])} with no depending services and
     * no {@link Dictionary} object
     */
    public boolean addService(final Class<?> clazz, final AbstractInitializer service) {
        return addService(clazz, service, null, null, null);
    }

    /**
     * A convenience method which calls {@link #addService(Class, AbstractInitializer, Dictionary, Class[], PropertyInterface[])} with no
     * {@link Dictionary} and no {@link PropertyInterface} object
     */
    public boolean addService(final Class<?> clazz, final AbstractInitializer service, final Class<?>[] dependingServices) {
        return addService(clazz, service, null, dependingServices, null);
    }

    /**
     * A convenience method which calls {@link #addService(Class, AbstractInitializer, Dictionary, Class[], PropertyInterface[])} with no
     * {@link Dictionary} object
     */
    public boolean addService(final Class<?> clazz, final AbstractInitializer service, final Class<?>[] dependingServices, final PropertyInterface[] properties) {
        return addService(clazz, service, null, dependingServices, properties);
    }

    /**
     * A convenience method which calls {@link #addService(Class, AbstractInitializer, Dictionary, Class[], PropertyInterface[])} with no depending services
     */
    public boolean addService(final Class<?> clazz, final AbstractInitializer service, final Dictionary<String, ?> dictionary) {
        return addService(clazz, service, dictionary, null, null);
    }

    /**
     * Adds a service to the framework
     * 
     * @param clazz The class or interface the service you want to add is implementing
     * @param service An instance of your implementing class, note that this class must implement the interface {@link AbstractInitializer}
     * @param dictionary A dictionary which can contain object, which will be attached to the service in OSGi
     * @param dependingServices A number of services (class names) which must be present so that you service implementation is activated,
     *            the order you give the classes here will be the same like in the {@link AbstractInitializer#setObjects(Object[])} you have
     *            to implement
     * @param properties Properties as an array of type {@link PropertyInterface} if properties should be checked
     * @return
     */
    public boolean addService(final Class<?> clazz, final AbstractInitializer service, final Dictionary<String, ?> dictionary, final Class<?>[] dependingServices, PropertyInterface[] properties) {
        return registrations.add(new ServiceEntry(clazz, service, dictionary, dependingServices, properties));
    }

    /* (non-Javadoc)
     * @see com.openexchange.server.ServiceLookup#getOptionalService(java.lang.Class)
     */
    public <S> S getOptionalService(Class<? extends S> clazz) {
        return getService(clazz);
    }

    /* (non-Javadoc)
     * @see com.openexchange.server.ServiceLookup#getService(java.lang.Class)
     */
    public <S> S getService(final Class<? extends S> clazz) {
        if (started.get()) {
            return registry.getService(clazz);
        } else {
            return null;
        }
    }

    /**
     * Method implementing a proper way to shutdown a bundle
     */
    public void shutdownBundle() {
        if (shutdownActivated.compareAndSet(false, true)) {
            final Bundle bundle = this.context.getBundle();
            final String bundleName = bundle.getSymbolicName();
            LOG.error("Adding listener for shutting down bundle: " + bundleName);
            final BundleListener listener = new BundleListener() {
                
                @Override
                public void bundleChanged(BundleEvent event) {
                    if (BundleEvent.STARTED == event.getType() && context.getBundle().equals(event.getBundle())) {
                        try {
                            context.removeBundleListener(this);
                            // This bundle is fully started, stop it now...
                            context.getBundle().stop();
                        } catch (final BundleException e) {
                            // Just log...
                            LOG.error("Error while shutting down \"" + bundleName + "\" bundle: " + e.getMessage(), e);
                        }
                    }
                    
                }
            };
            // Check if the bundle is not already activated before adding the listener
            if (Bundle.ACTIVE == bundle.getState()) {
                try {
                    bundle.stop();
                } catch (final BundleException e) {
                    // Just log...
                    LOG.error("Error while shutting down \"" + bundleName + "\" bundle: " + e.getMessage(), e);
                }
            } 
            context.addBundleListener(listener);
        }
    }

    public void start() throws Exception {
        try {
            if (bundleMap.size() != 0) {
                // First fire up the registry with the correct capacity...
                registry = new ServiceRegistry(bundleMap.size());
                final Filter filter = createFilter(bundleMap);
                ServiceTracker<Object, Object> serviceTracker = new ServiceTracker<Object, Object>(context, filter, new ServiceTrackerCustomizer<Object, Object>() {

                    @SuppressWarnings("unchecked")
                    public Object addingService(final ServiceReference<Object> reference) {
                        final Object addedService = context.getService(reference);
                        for (final Entry<?> entry : bundleMap) {
                            final Class<?> clazz = entry.getClazz();
                            if (clazz.isInstance(addedService)) {
                                final Object cast = clazz.cast(addedService);
                                @SuppressWarnings("rawtypes")
                                final AvailabilityActivationClosure closure = entry.getClosure();
                                if (null != closure) {
                                    closure.serviceAvailable(cast);
                                }
                                registry.addService(clazz, cast);
                            }
                        }
                        return addedService;
                    }

                    public void modifiedService(final ServiceReference<Object> arg0, final Object arg1) {
                        // Nothing to do here
                    }

                    public void removedService(final ServiceReference<Object> arg0, final Object arg1) {
                        for (final Entry<?> entry : bundleMap) {
                            if (entry.getClazz().isInstance(arg1)) {
                                if (entry.isRequired()) {
                                    final Bundle bundle = context.getBundle();
                                    LOG.error("The required service \"" + entry.getClazz().getName() +"\" was removed from OSGi system, shutting down " + bundle.getSymbolicName());
                                    shutdownBundle();
                                }
                                registry.removeService(entry.getClazz());
                            }
                        }
                    }
                });
                serviceTracker.open();
                serviceTrackers.add(serviceTracker);
            }
            registerServices();
            started.set(true);
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Stops the OSGi abstraction service. This method must be called so that all resources needed for OSGi service tracking etc. will be
     * cleaned
     */
    public void stop() {
        started.set(false);
        for (final ServiceTracker<Object, Object> serviceTracker : serviceTrackers) {
            serviceTracker.close();
        }
        serviceTrackers.clear();
        registry = null;
    }

    private boolean checkConfigService(SimpleEntry<?>[] dependingServices) {
        for (final SimpleEntry<?> entry : dependingServices) {
            if (ConfigurationService.class.equals(entry.getClazz())) {
                return true;
            }
        }
        return false;
    }

    private Filter createFilter(final List<? extends SimpleEntry<?>> list) throws Exception {
        final StringBuilder sb = new StringBuilder();
        if (list.size() > 1) {
            // Prepend or condition
            sb.append("(|");
        }
        for (final SimpleEntry<?> entry : list) {
            sb.append("(");
            sb.append(Constants.OBJECTCLASS);
            sb.append('=');
            sb.append(entry.getClazz().getName());
            sb.append(")");
        }
        if (list.size() > 1) {
            // Closing brackets if or condition must be closed
            sb.append(")");
        }
        final Filter filter;
        try {
            filter = context.createFilter(sb.toString());
        } catch (final InvalidSyntaxException e) {
            throw new Exception(e);
        }
        return filter;
    }

    private void createRegisterServiceTracker(final String className, final AbstractInitializer service, final Dictionary<String, ?> dictionary, final SimpleEntry<?>[] dependingServices, final PropertyInterface[] propertyInterfaces) throws Exception {
        final List<SimpleEntry<?>> filterServices;
        final List<SimpleEntry<?>> ownDependingServices;
        if (null == dependingServices) {
            // only property check
            filterServices = new ArrayList<OSGiAbstractor.SimpleEntry<?>>();
            filterServices.add(new SimpleEntry<ConfigurationService>(ConfigurationService.class));
            ownDependingServices = Arrays.asList(dependingServices);
        } else {
            if (null != propertyInterfaces && 0 != propertyInterfaces.length) {
                if (checkConfigService(dependingServices)) {
                    filterServices = Arrays.asList(dependingServices);
                    ownDependingServices = filterServices;
                } else {
                    // Add config service
                    filterServices = new ArrayList<OSGiAbstractor.SimpleEntry<?>>(Arrays.asList(dependingServices));
                    filterServices.add(new SimpleEntry<ConfigurationService>(ConfigurationService.class));
                    ownDependingServices = Arrays.asList(dependingServices);
                }
            } else {
                ownDependingServices = Arrays.asList(dependingServices);
                filterServices = ownDependingServices;
            }
        }
        final Filter filter = createFilter(ownDependingServices);
        ServiceTracker<Object, Object> serviceTracker = new ServiceTracker<Object, Object>(context, filter, new ServiceTrackerCustomizer<Object, Object>() {

            private final Lock lock = new ReentrantLock();

            private final Object[] objects = new Object[ownDependingServices.size()];

            private ServiceRegistration<?> registration;

            public Object addingService(final ServiceReference<Object> reference) {
                final Object addedService = context.getService(reference);
                final boolean needsRegistration;
                lock.lock();
                try {
                    if (null != propertyInterfaces && 0 != propertyInterfaces.length) {
                        if (ConfigurationService.class.isInstance(addedService)) {
                            // Check properties
                            try {
                                PropertyHandler.check((ConfigurationService) addedService, propertyInterfaces,
                                    context.getBundle().getSymbolicName() + " bundle");
                            } catch (final OXException e) {
                                LOG.error("Error while checking Properties: " + e.getMessage(), e);
                                shutdownBundle();
                                return addedService;
                            }
                        }
                    }

                    for (int i = 0; i < ownDependingServices.size(); i++) {
                        final SimpleEntry<?> entry = ownDependingServices.get(i);
                        final Class<?> clazz = entry.getClazz();
                        if (clazz.isInstance(addedService)) {
                            objects[i] = clazz.cast(addedService);
                        }
                    }

                    final int found = countObjects();
                    needsRegistration = found == objects.length && null == registration;
                } finally {
                    lock.unlock();
                }
                if (needsRegistration) {
                    LOG.info("Registering " + className + " service.");
                    try {
                        service.setObjects(objects);
                        registration = context.registerService(className, service, null);
                    } catch (final OXException e) {
                        LOG.error("Error while setting required services in \"" + service.getClass().getCanonicalName() + "\": " + e.getMessage(), e);
                        shutdownBundle();
                    }
                }
                return addedService;
            }

            public void modifiedService(final ServiceReference<Object> arg0, final Object arg1) {
                // Nothing to do here
            }

            public void removedService(final ServiceReference<Object> reference, final Object obj) {
                ServiceRegistration<?> unregister = null;
                lock.lock();
                try {
                    for (int i = 0; i < ownDependingServices.size(); i++) {
                        final SimpleEntry<?> entry = ownDependingServices.get(i);
                        if (entry.getClazz().isInstance(obj)) {
                            objects[i] = null;
                        }
                    }

                    final int found = countObjects();
                    if (registration != null && found != objects.length) {
                        unregister = registration;
                        registration = null;
                    }
                } finally {
                    lock.unlock();
                }
                if (null != unregister) {
                    LOG.info("Unregistering " + className + " service.");
                    unregister.unregister();
                }
                context.ungetService(reference);
            }

            private int countObjects() {
                int found = 0;
                for (final Object object : objects) {
                    if (null != object) {
                        found++;
                    }
                }
                return found;
            }
        });
        serviceTracker.open();
        serviceTrackers.add(serviceTracker);
    }

    private void registerServices() throws Exception {
        for (final ServiceEntry entry : registrations) {
            if (null != entry.getDependingServices() || (null != entry.getProperties() && 0 != entry.getProperties().length)) {
                createRegisterServiceTracker(entry.getClassName(), entry.getService(), entry.getDictionary(), entry.getDependingServices(), entry.getProperties());
            } else {
                context.registerService(entry.getClassName(), entry.getService(), entry.getDictionary());
            }
        }
    }

}
