/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.osgi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.osgi.console.ServiceStateLookup;

/**
 * This abstract service tracker can wait for some service and register another one if all become available.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class DependentServiceRegisterer<S> implements ServiceTrackerCustomizer<Object,Object> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DependentServiceRegisterer.class);

    private final Lock lock = new ReentrantLock();
    private final BundleContext context;
    private final Class<S> serviceType;
    private final Class<? extends S> serviceClass;
    private final Dictionary<String, ?> properties;
    private final Class<?>[] neededServices;
    private final Object[] foundServices;

    protected S registeredService;
    private ServiceRegistration<?> registration;

    /**
     * Initializes a new {@link DependentServiceRegisterer}.
     *
     * @param context The bundle context
     * @param serviceType The type of the service
     * @param serviceClass The class of the concrete service instance
     * @param properties The accompanying service properties
     * @param neededServices The needed services
     */
    public DependentServiceRegisterer(BundleContext context, Class<S> serviceType, Class<? extends S> serviceClass, Dictionary<String, ?> properties, Class<?>... neededServices) {
        super();
        this.context = context;
        this.serviceType = serviceType;
        this.serviceClass = serviceClass;
        this.properties = properties;
        this.neededServices = neededServices;
        this.foundServices = new Object[neededServices.length];
        setState();
    }

    public Filter getFilter() throws InvalidSyntaxException {
        return Tools.generateServiceFilter(context, neededServices);
    }

    @Override
    public Object addingService(ServiceReference<Object> reference) {
        final Object obj = context.getService(reference);
        boolean needsRegistration = true;
        lock.lock();
        try {
            for (int i = 0; i < neededServices.length; i++) {
                if (neededServices[i].isAssignableFrom(obj.getClass())) {
                    foundServices[i] = obj;
                }
                needsRegistration &= null != foundServices[i];
            }
            needsRegistration &= null == registration;
            if (needsRegistration) {
                register();
            }
            setState();
        } finally {
            lock.unlock();
        }
        return obj;
    }

    /**
     * You can overwrite this method to do something else to start up your code with the needed services. If you want to register a servlet,
     * it would be wise to have HttpService as the first service in the needed services list, so you can easily access it.
     */
    protected void register() {
        try {
            Constructor<? extends S> constructor = serviceClass.getConstructor(neededServices);
            S registeredService = constructor.newInstance(foundServices);
            this.registeredService = registeredService;
            LOG.trace("Registering service {}", serviceClass.getName());
            registration = context.registerService(serviceType, registeredService, properties);
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.error("Can not register {}", serviceClass.getName(), t);
        }
    }

    @Override
    public void modifiedService(ServiceReference<Object> arg0, Object arg1) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<Object> reference, Object service) {
        ServiceRegistration<?> unregister = null;
        lock.lock();
        try {
            boolean someServiceMissing = false;
            for (int i = 0; i < neededServices.length; i++) {
                if (neededServices[i].isAssignableFrom(service.getClass())) {
                    foundServices[i] = null;
                }
                someServiceMissing |= null == foundServices[i];
            }
            if (null != registration && someServiceMissing) {
                unregister = registration;
                registration = null;
            }
            if (null != unregister) {
                unregister(unregister, service);
            }
            setState();
        } finally {
            lock.unlock();
        }
        context.ungetService(reference);
    }

    /**
     * You can overwrite this method to do something else to shut down your code if one of the needed services is gone. If you overwrote
     * {@link #register()} and you did not register an OSGi service, just ignore the unregister parameter. If you registered a servlet and
     * the HttpService is gone, it is not available anymore in neededServices but from the given service parameter.
     * @param unregister OSGi service registration that needs to be unregistered.
     * @param service OSGi service that is taken down.
     */
    protected void unregister(ServiceRegistration<?> unregister, Object service) {
        LOG.trace("Unregistering service {}", serviceClass.getName());
        unregister.unregister();
        try {
            Method method = serviceClass.getMethod("shutDown", new Class<?>[0]);
            method.invoke(registeredService, new Object[0]);
        } catch (SecurityException e) {
            // Service does not have a shutDown() method.
        } catch (NoSuchMethodException e) {
            // Service does not have a shutDown() method.
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.error("Can not shut down {}", serviceClass.getName(), t);
        }
    }

    private void setState() {
        ServiceStateLookup lookup = DeferredActivator.getLookup();
        List<String> missing = new ArrayList<String>();
        List<String> present = new ArrayList<String>();
        for (int i = 0; i < neededServices.length; i++) {
            String serviceName = neededServices[i].getName();
            if (null == foundServices[i]) {
                missing.add(serviceName);
            } else {
                present.add(serviceName);
            }
        }
        lookup.setState(serviceClass.getName(), missing, present);
    }
}
