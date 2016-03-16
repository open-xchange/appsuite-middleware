/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */


package com.openexchange.utils.osgi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
import org.slf4j.Marker;
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
public abstract class OSGiAbstractor implements ServiceLookup, BundleActivator{

    private class Entry<T> extends SimpleEntry<T> {

        private final AvailabilityActivationClosure<T> closure;

        private final boolean required;

        public Entry(final Class<T> clazz, final boolean required, final AvailabilityActivationClosure<T> closure) {
            super(clazz);
            this.required = required;
            this.closure = closure;
        }

        public Entry(final ServiceClassEntry<T> entry, final boolean required, final AvailabilityActivationClosure<T> closure) {
            super(entry.getClazz(), entry.getService());
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

    private class Logger implements org.slf4j.Logger {

        private final org.slf4j.Logger delegate;

        private final String prefixString;

        public Logger(final org.slf4j.Logger log, final Class<?> clazz) {
            this.delegate = log;
            this.prefixString = "Logged for " + clazz.getCanonicalName() + ": ";
        }

        @Override
        public void debug(String message) {
            this.delegate.debug(prefixString + message);
        }

        @Override
        public void debug(String message, Throwable t) {
            this.delegate.debug(prefixString + message, t);
        }

        @Override
        public void error(String message) {
            this.delegate.error(prefixString + message);
        }

        @Override
        public void error(String message, Throwable t) {
            this.delegate.error(prefixString + message, t);
        }

        @Override
        public void info(String message) {
            this.delegate.info(prefixString + message);
        }

        @Override
        public void info(String message, Throwable t) {
            this.delegate.info(prefixString + message, t);
        }

        @Override
        public boolean isDebugEnabled() {
            return this.delegate.isDebugEnabled();
        }

        @Override
        public boolean isErrorEnabled() {
            return this.delegate.isErrorEnabled();
        }

        @Override
        public boolean isInfoEnabled() {
            return this.delegate.isInfoEnabled();
        }

        @Override
        public boolean isTraceEnabled() {
            return this.delegate.isTraceEnabled();
        }

        @Override
        public boolean isWarnEnabled() {
            return this.delegate.isWarnEnabled();
        }

        @Override
        public void trace(String message) {
            this.delegate.trace(prefixString + message);
        }

        @Override
        public void trace(String message, Throwable t) {
            this.delegate.trace(prefixString + message, t);
        }

        @Override
        public void warn(String message) {
            this.delegate.warn(prefixString + message);
        }

        @Override
        public void warn(String message, Throwable t) {
            this.delegate.warn(prefixString + message, t);
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public void trace(String format, Object arg) {
            delegate.trace(prefixString + format, arg);
        }

        @Override
        public void trace(String format, Object arg1, Object arg2) {
            delegate.trace(prefixString + format, arg1, arg2);
        }

        @Override
        public void trace(String format, Object... arguments) {
            delegate.trace(prefixString + format, arguments);
        }

        @Override
        public boolean isTraceEnabled(Marker marker) {
            return delegate.isTraceEnabled(marker);
        }

        @Override
        public void trace(Marker marker, String msg) {
            delegate.trace(marker, prefixString + msg);
        }

        @Override
        public void trace(Marker marker, String format, Object arg) {
            delegate.trace(marker, prefixString + format, arg);
        }

        @Override
        public void trace(Marker marker, String format, Object arg1, Object arg2) {
            delegate.trace(marker, prefixString + format, arg1, arg2);
        }

        @Override
        public void trace(Marker marker, String format, Object... argArray) {
            delegate.trace(marker, prefixString + format, argArray);
        }

        @Override
        public void trace(Marker marker, String msg, Throwable t) {
            delegate.trace(marker, prefixString + msg, t);
        }

        @Override
        public void debug(String format, Object arg) {
            delegate.debug(prefixString + format, arg);
        }

        @Override
        public void debug(String format, Object arg1, Object arg2) {
            delegate.debug(prefixString + format, arg1, arg2);
        }

        @Override
        public void debug(String format, Object... arguments) {
            delegate.debug(prefixString + format, arguments);
        }

        @Override
        public boolean isDebugEnabled(Marker marker) {
            return delegate.isDebugEnabled(marker);
        }

        @Override
        public void debug(Marker marker, String msg) {
            delegate.debug(marker, prefixString + msg);
        }

        @Override
        public void debug(Marker marker, String format, Object arg) {
            delegate.debug(marker, prefixString + format, arg);
        }

        @Override
        public void debug(Marker marker, String format, Object arg1, Object arg2) {
            delegate.debug(marker, prefixString + format, arg1, arg2);
        }

        @Override
        public void debug(Marker marker, String format, Object... arguments) {
            delegate.debug(marker, prefixString + format, arguments);
        }

        @Override
        public void debug(Marker marker, String msg, Throwable t) {
            delegate.debug(marker, prefixString + msg, t);
        }

        @Override
        public void info(String format, Object arg) {
            delegate.info(prefixString + format, arg);
        }

        @Override
        public void info(String format, Object arg1, Object arg2) {
            delegate.info(prefixString + format, arg1, arg2);
        }

        @Override
        public void info(String format, Object... arguments) {
            delegate.info(prefixString + format, arguments);
        }

        @Override
        public boolean isInfoEnabled(Marker marker) {
            return delegate.isInfoEnabled(marker);
        }

        @Override
        public void info(Marker marker, String msg) {
            delegate.info(marker, prefixString + msg);
        }

        @Override
        public void info(Marker marker, String format, Object arg) {
            delegate.info(marker, prefixString + format, arg);
        }

        @Override
        public void info(Marker marker, String format, Object arg1, Object arg2) {
            delegate.info(marker, prefixString + format, arg1, arg2);
        }

        @Override
        public void info(Marker marker, String format, Object... arguments) {
            delegate.info(marker, prefixString + format, arguments);
        }

        @Override
        public void info(Marker marker, String msg, Throwable t) {
            delegate.info(marker, prefixString + msg, t);
        }

        @Override
        public void warn(String format, Object arg) {
            delegate.warn(prefixString + format, arg);
        }

        @Override
        public void warn(String format, Object... arguments) {
            delegate.warn(prefixString + format, arguments);
        }

        @Override
        public void warn(String format, Object arg1, Object arg2) {
            delegate.warn(prefixString + format, arg1, arg2);
        }

        @Override
        public boolean isWarnEnabled(Marker marker) {
            return delegate.isWarnEnabled(marker);
        }

        @Override
        public void warn(Marker marker, String msg) {
            delegate.warn(marker, prefixString + msg);
        }

        @Override
        public void warn(Marker marker, String format, Object arg) {
            delegate.warn(marker, prefixString + format, arg);
        }

        @Override
        public void warn(Marker marker, String format, Object arg1, Object arg2) {
            delegate.warn(marker, prefixString + format, arg1, arg2);
        }

        @Override
        public void warn(Marker marker, String format, Object... arguments) {
            delegate.warn(marker, prefixString + format, arguments);
        }

        @Override
        public void warn(Marker marker, String msg, Throwable t) {
            delegate.warn(marker, prefixString + msg, t);
        }

        @Override
        public void error(String format, Object arg) {
            delegate.error(prefixString + format, arg);
        }

        @Override
        public void error(String format, Object arg1, Object arg2) {
            delegate.error(prefixString + format, arg1, arg2);
        }

        @Override
        public void error(String format, Object... arguments) {
            delegate.error(prefixString + format, arguments);
        }

        @Override
        public boolean isErrorEnabled(Marker marker) {
            return delegate.isErrorEnabled(marker);
        }

        @Override
        public void error(Marker marker, String msg) {
            delegate.error(marker, prefixString + msg);
        }

        @Override
        public void error(Marker marker, String format, Object arg) {
            delegate.error(marker, prefixString + format, arg);
        }

        @Override
        public void error(Marker marker, String format, Object arg1, Object arg2) {
            delegate.error(marker, prefixString + format, arg1, arg2);
        }

        @Override
        public void error(Marker marker, String format, Object... arguments) {
            delegate.error(marker, prefixString + format, arguments);
        }

        @Override
        public void error(Marker marker, String msg, Throwable t) {
            delegate.error(marker, prefixString + msg, t);
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
            if (null != dependingServices) {
                this.dependingServices = new SimpleEntry<?>[dependingServices.length];
                for (int i = 0; i < dependingServices.length; i++) {
                    this.dependingServices[i] = new SimpleEntry(dependingServices[i]);
                }
            } else {
                this.dependingServices = null;
            }
            this.properties = properties;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public ServiceEntry(final Class<?> clazz, final AbstractInitializer service, final Dictionary<String, ?> dictionary, final ServiceClassEntry<?>[] dependingServices, final PropertyInterface[] properties) {
            super();
            this.clazz = clazz;
            this.service = service;
            this.dictionary = dictionary;
            if (null != dependingServices) {
                this.dependingServices = new SimpleEntry<?>[dependingServices.length];
                for (int i = 0; i < dependingServices.length; i++) {
                    this.dependingServices[i] = new SimpleEntry(dependingServices[i].getClazz(), dependingServices[i].getService());
                }
            } else {
                this.dependingServices = null;
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

        private final Class<?> service;

        public SimpleEntry(final Class<T> clazz) {
            this.clazz = clazz;
            this.service = clazz;
        }

        public SimpleEntry(final Class<T> clazz, final Class<?> service) {
            super();
            this.clazz = clazz;
            this.service = service;
        }

        public Class<T> getClazz() {
            return clazz;
        }

        public Class<?> getService() {
            return service;
        }
    }

    static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OSGiAbstractor.class);

    static ServiceRegistry registry;

    private final static List<ServiceTracker<Object, Object>> serviceTrackers = new ArrayList<ServiceTracker<Object,Object>>();

    private static AtomicBoolean started = new AtomicBoolean(false);

    /*
     * Keeps all the needed services
     */
    final List<Entry<?>> bundleMap = new ArrayList<Entry<?>>();

    private int requiredService = 0;

    int registeredServiceImplementations = 0;

    protected BundleContext m_context;

    /*
     * Keeps all the service implementation which should be registered
     */
    private final List<ServiceEntry> registrations = new ArrayList<ServiceEntry>();

    private final AtomicBoolean shutdownActivated = new AtomicBoolean(false);

    public OSGiAbstractor() {
        // Change LOG to the correct class for distinguishing in the logs
        LOG = new Logger(LOG, this.getClass());
    }

    /* (non-Javadoc)
     * @see com.openexchange.server.ServiceLookup#getOptionalService(java.lang.Class)
     */
    @Override
    public final <S> S getOptionalService(Class<? extends S> clazz) {
        return getService(clazz);
    }

    /* (non-Javadoc)
     * @see com.openexchange.server.ServiceLookup#getService(java.lang.Class)
     */
    @Override
    public final <S> S getService(final Class<? extends S> clazz) {
        return getServiceStatic(clazz);
    }

    /**
     * Method implementing a proper way to shutdown a bundle
     */
    public void shutdownBundle() {
        if (shutdownActivated.compareAndSet(false, true)) {
            final Bundle bundle = this.m_context.getBundle();
            final String bundleName = bundle.getSymbolicName();
            LOG.error("Adding listener for shutting down bundle: {}", bundleName);
            final BundleListener listener = new BundleListener() {

                @Override
                public void bundleChanged(BundleEvent event) {
                    if (BundleEvent.STARTED == event.getType() && m_context.getBundle().equals(event.getBundle())) {
                        try {
                            m_context.removeBundleListener(this);
                            // This bundle is fully started, stop it now...
                            m_context.getBundle().stop();
                        } catch (final BundleException e) {
                            // Just log...
                            LOG.error("Error while shutting down \"{}\" bundle", bundleName, e);
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
                    LOG.error("Error while shutting down \"{}\" bundle", bundleName, e);
                }
            }
            // Need to check if the context is still there, because it might be that the bundles stop method was called before and the context is null
            if (null != this.m_context) {
                m_context.addBundleListener(listener);
            }
        }
    }

    @Override
    public final void start(BundleContext context) throws Exception {
        this.m_context = context;
        startBundle(context);
        start();
    }

    @Override
    public final void stop(BundleContext context) throws Exception {
        stopBundle(context);
        stop();
        this.m_context = null;
    }

    private boolean checkConfigService(SimpleEntry<?>[] dependingServices) {
        for (final SimpleEntry<?> entry : dependingServices) {
            if (ConfigurationService.class.equals(entry.getClazz())) {
                return true;
            }
        }
        return false;
    }

    void checkStarted() {
        if (((null != registry && registry.size() >= requiredService) || null == registry) && null != registrations && registrations.size() == registeredServiceImplementations) {
            started.set(true);
        } else {
            started.set(false);
        }
    }

    private Filter createFilter(final List<? extends SimpleEntry<?>> list) throws Exception {
        if (null == list || list.isEmpty()) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        if (list.size() > 1) {
            // Prepend or condition
            sb.append("(|");
        }
        for (final SimpleEntry<?> entry : list) {
            sb.append("(");
            sb.append(Constants.OBJECTCLASS);
            sb.append('=');
            sb.append(entry.getService().getName());
            sb.append(")");
        }
        if (list.size() > 1) {
            // Closing brackets if or condition must be closed
            sb.append(")");
        }
        final Filter filter;
        try {
            filter = m_context.createFilter(sb.toString());
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
            filterServices = new ArrayList<OSGiAbstractor.SimpleEntry<?>>(1);
            filterServices.add(new SimpleEntry<ConfigurationService>(ConfigurationService.class));
            /*-
             * Previous implementation was:
             *
             *   ownDependingServices = Arrays.asList(dependingServices);
             *
             * But 'dependingServices' is known to be null here, thus a NullPointerException will occur;
             * see see java.util.Arrays.ArrayList.ArrayList(E[]) for proof
             *
             * Therefore changed to:
             */
            ownDependingServices = Collections.emptyList();
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
        if (filter != null) {
            ServiceTracker<Object, Object> serviceTracker = new ServiceTracker<Object, Object>(m_context, filter, new ServiceTrackerCustomizer<Object, Object>() {

                private final Lock lock = new ReentrantLock();

                private final Object[] objects = new Object[ownDependingServices.size()];

                private ServiceRegistration<?> registration;

                @Override
                public Object addingService(final ServiceReference<Object> reference) {
                    final Object addedService = m_context.getService(reference);
                    final boolean needsRegistration;
                    lock.lock();
                    try {
                        if (null != propertyInterfaces && 0 != propertyInterfaces.length) {
                            if (ConfigurationService.class.isInstance(addedService)) {
                                // Check properties
                                try {
                                    PropertyHandler.check((ConfigurationService) addedService, propertyInterfaces,
                                        m_context.getBundle().getSymbolicName() + " bundle");
                                } catch (final OXException e) {
                                    LOG.error("Error while checking Properties", e);
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
                        try {
                            service.setObjects(objects);
                            registration = m_context.registerService(className, service, dictionary);
                            registeredServiceImplementations++;
                            checkStarted();
                            LOG.info("Registered {} service.", className);
                        } catch (final OXException e) {
                            LOG.error("Error while setting required services in \"{}\"", service.getClass().getCanonicalName(), e);
                            shutdownBundle();
                        } catch (final RuntimeException e) {
                            LOG.error("Error while setting required services in \"{}\"", service.getClass().getCanonicalName(), e);
                            shutdownBundle();
                        }
                    }
                    return addedService;
                }

                @Override
                public void modifiedService(final ServiceReference<Object> arg0, final Object arg1) {
                    // Nothing to do here
                }

                @Override
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
                            registeredServiceImplementations--;
                            checkStarted();
                        }
                    } finally {
                        lock.unlock();
                    }
                    if (null != unregister) {
                        LOG.info("Unregistering {} service.", className);
                        unregister.unregister();
                    }
                    m_context.ungetService(reference);
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
    }

    private void registerServices() throws Exception {
        for (final ServiceEntry entry : registrations) {
            if (null != entry.getDependingServices() || (null != entry.getProperties() && 0 != entry.getProperties().length)) {
                createRegisterServiceTracker(entry.getClassName(), entry.getService(), entry.getDictionary(), entry.getDependingServices(), entry.getProperties());
            } else {
                m_context.registerService(entry.getClassName(), entry.getService(), entry.getDictionary());
                registeredServiceImplementations++;
            }
        }
    }

    private void start() throws Exception {
        try {
            if (bundleMap.size() != 0) {
                // First fire up the registry with the correct capacity...
                registry = new ServiceRegistry(bundleMap.size());
                final Filter filter = createFilter(bundleMap);
                ServiceTracker<Object, Object> serviceTracker = new ServiceTracker<Object, Object>(m_context, filter, new ServiceTrackerCustomizer<Object, Object>() {

                    @Override
                    @SuppressWarnings("unchecked")
                    public Object addingService(final ServiceReference<Object> reference) {
                        try {
                            final Object addedService = m_context.getService(reference);
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
                                    checkStarted();
                                }
                            }
                            return addedService;
                        } catch (final RuntimeException e) {
                            LOG.error("A runtime exception occurred while addingService", e);
                            shutdownBundle();
                            throw e;
                        }
                    }

                    @Override
                    public void modifiedService(final ServiceReference<Object> arg0, final Object arg1) {
                        // Nothing to do here
                    }

                    @Override
                    public void removedService(final ServiceReference<Object> arg0, final Object arg1) {
                        try {
                            for (final Entry<?> entry : bundleMap) {
                                if (entry.getClazz().isInstance(arg1)) {
                                    if (entry.isRequired()) {
                                        final Bundle bundle = m_context.getBundle();
                                        LOG.error("The required service \"{}\" was removed from OSGi system, shutting down {}", entry.getClazz().getName(), bundle.getSymbolicName());
                                        shutdownBundle();
                                    }
                                    registry.removeService(entry.getClazz());
                                }
                            }
                        } catch (final RuntimeException e) {
                            LOG.error("A runtime exception occurred while removedService", e);
                            shutdownBundle();
                            throw e;
                        }
                    }
                });
                serviceTracker.open();
                serviceTrackers.add(serviceTracker);
            }
            registerServices();
        } catch (final Exception e) {
            LOG.error("", e);
        }
    }

    /**
     * Stops the OSGi abstraction service. This method must be called so that all resources needed for OSGi service tracking etc. will be
     * cleaned
     */
    private void stop() {
        started.set(false);
        for (final ServiceTracker<Object, Object> serviceTracker : serviceTrackers) {
            serviceTracker.close();
        }
        serviceTrackers.clear();

        ServiceRegistry tmp = registry;
        if (null != tmp) {
            tmp.clearRegistry();
            registry = null;
        }
    }

    /**
     * A convenience method which calls {@link #addServiceImplementation(Class, AbstractInitializer, Dictionary, Class[], PropertyInterface[])}
     */
    protected final void addServiceImplementation(final Class<?> clazz, final AbstractInitializer implementation) {
        addServiceImplementation(clazz, implementation, null, (Class<?>[])null, null);
    }

    /**
     * A convenience method which calls {@link #addServiceImplementation(Class, AbstractInitializer, Dictionary, Class[], PropertyInterface[])}
     */
    protected final void addServiceImplementation(final Class<?> clazz, final AbstractInitializer implementation, final Class<?>[] dependingServices) {
        addServiceImplementation(clazz, implementation, null, dependingServices, null);
    }

    /**
     * A convenience method which calls {@link #addServiceImplementation(Class, AbstractInitializer, Dictionary, Class[], PropertyInterface[])}
     */
    protected final void addServiceImplementation(final Class<?> clazz, final AbstractInitializer implementation, final Class<?>[] dependingServices, final PropertyInterface[] properties) {
        addServiceImplementation(clazz, implementation, null, dependingServices, properties);
    }

    /**
     * A convenience method which calls {@link #addServiceImplementation(Class, AbstractInitializer, Dictionary, ServiceClassEntry[], PropertyInterface[])}
     */
    protected final void addServiceImplementation(final Class<?> clazz, final AbstractInitializer implementation, final ServiceClassEntry<?>[] dependingServices, final PropertyInterface[] properties) {
        addServiceImplementation(clazz, implementation, null, dependingServices, properties);
    }

    /**
     * A convenience method which calls {@link #addServiceImplementation(Class, AbstractInitializer, Dictionary, Class[], PropertyInterface[])}
     */
    protected final void addServiceImplementation(final Class<?> clazz, final AbstractInitializer implementation, final Dictionary<String, ?> dictionary) {
        addServiceImplementation(clazz, implementation, dictionary, (Class<?>[])null, null);
    }

    /**
     * Adds a service implementation to the framework
     *
     * @param clazz The class or interface the service you want to add is implementing
     * @param implementation An instance of your implementing class, note that this class must implement the interface {@link AbstractInitializer}
     * @param dictionary A dictionary which can contain object, which will be attached to the service in OSGi
     * @param dependingServices A number of services (class names) which must be present so that you service implementation is activated,
     *            the order you give the classes here will be the same like in the {@link AbstractInitializer#setObjects(Object[])} you have
     *            to implement
     * @param properties Properties as an array of type {@link PropertyInterface} if properties should be checked
     * @return
     */
    protected final void addServiceImplementation(final Class<?> clazz, final AbstractInitializer implementation, final Dictionary<String, ?> dictionary, final Class<?>[] dependingServices, PropertyInterface[] properties) {
        registrations.add(new ServiceEntry(clazz, implementation, dictionary, dependingServices, properties));
    }

    /**
     * Adds a service implementation to the framework. This variant of the method is used if you depend on services which are registered with a different name and thus need to be looked up through a different OSGi service class filter
     *
     * @param clazz The class or interface the service you want to add is implementing
     * @param implementation An instance of your implementing class, note that this class must implement the interface {@link AbstractInitializer}
     * @param dictionary A dictionary which can contain object, which will be attached to the service in OSGi
     * @param dependingServices A number of @link {@link ServiceClassEntry} objects which must be present so that you service implementation is activated,
     *            the order you give the classes here will be the same like in the {@link AbstractInitializer#setObjects(Object[])} you have
     *            to implement.
     * @param properties Properties as an array of type {@link PropertyInterface} if properties should be checked
     * @return
     */
    protected final void addServiceImplementation(final Class<?> clazz, final AbstractInitializer implementation, final Dictionary<String, ?> dictionary, final ServiceClassEntry<?>[] dependingServices, PropertyInterface[] properties) {
        registrations.add(new ServiceEntry(clazz, implementation, dictionary, dependingServices, properties));
    }

    /**
     * A convenience method which calls {@link #addServiceToRegistry(Class, boolean, AvailabilityActivationClosure)}
     */
    protected final <T> void addServiceToRegistry(final Class<T> service, final boolean required) {
        addServiceToRegistry(service, required, null);
    }

    /**
     * Adds a service to the registry of this bundle. The service can be fetched later by calling one of the methods provided by the
     * {@link ServiceLookup} interface. Or by calling {@link #getServiceStatic(Class)} if you don't have the instance of this class
     * available
     *
     * @param service the name of the service class you want to add
     * @param required if this service is required for your bundle or not, if it's required your bundle will shutdown if the service is not
     *            available any more
     * @param closure an object of type {@link AvailabilityActivationClosure}. With that object you can define if something should be done
     *            if a specific service gets available
     */
    protected final <T> void addServiceToRegistry(final Class<T> service, final boolean required, final AvailabilityActivationClosure<T> closure) {
        if (required) {
            requiredService++;
        }
        bundleMap.add(new Entry<T>(service, required, closure));
    }

    /**
     * Adds a service to the registry of this bundle. The service can be fetched later by calling one of the methods provided by the
     * {@link ServiceLookup} interface. Or by calling {@link #getServiceStatic(Class)} if you don't have the instance of this class
     * available
     *
     * @param serviceAndClass the name of the service class the OSGi filter should use and the real service class
     * @param required if this service is required for your bundle or not, if it's required your bundle will shutdown if the service is not
     *            available any more
     * @param closure an object of type {@link AvailabilityActivationClosure}. With that object you can define if something should be done
     *            if a specific service gets available
     */
    protected final <T> void addServiceToRegistry(final ServiceClassEntry<T> serviceAndClass, final boolean required, final AvailabilityActivationClosure<T> closure) {
        if (required) {
            requiredService++;
        }
        bundleMap.add(new Entry<T>(serviceAndClass, required, closure));
    }

    protected abstract void startBundle(final BundleContext context) throws Exception;

    protected abstract void stopBundle(final BundleContext context) throws Exception;

    /**
     * @param clazz
     * @return
     */
    public final static <S> S getServiceStatic(final Class<? extends S> clazz) {
        if (started.get()) {
            return registry.getService(clazz);
        } else {
            throw new RuntimeException("The bundle is not fully started");
        }
    }

    /**
     * If this bundle is already correctly started (all Required Services are available)
     *
     * @return
     */
    public final static boolean isStarted() {
        return started.get();
    }

}
