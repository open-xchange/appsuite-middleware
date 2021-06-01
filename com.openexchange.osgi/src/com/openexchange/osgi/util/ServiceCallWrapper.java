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

package com.openexchange.osgi.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;


/**
 * The {@link ServiceCallWrapper} may be used in static helper classes to execute calls to
 * services that are not available yet in the current scope.
 *
 * This wrapper is especially meant to be used in cases where formerly service implementations
 * have been used directly instead of their service equivalent. Such usage is discouraged in
 * most cases and should be avoided. See below example how such legacy code can be refactored:<br>
 * <br>
 * Before:
 * <pre>
 * User user = UserStorage.getInstance().getUser(userId, contextId);
 * </pre>
 *
 * After:
 * <pre>
 * try {
 *     User user = ServiceCallWrapper.doServiceCall(CurrentClazz.class, UserService.class,
 *          new ServiceUser<UserService, User>() {
 *              public User perform(UserService service) throws Exception {
 *                  return service.getUser(context, userId);
 *              }
 *          });
 * } catch (ServiceException e) {
 *     if (e.isServiceUnavailable()) {
 *         handleServiceUnavailable();
 *     } else {
 *         throw e.toOXException();
 *     }
 * }
 * </pre>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class ServiceCallWrapper {

    static final ConcurrentMap<ServiceKey<?>, ServiceValue<?>> SERVICE_CACHE = new ConcurrentHashMap<>(16, 0.9F, 1);

    public static final AtomicReference<BundleContextProvider> BC_PROVIDER_REF = new AtomicReference<BundleContextProvider>(new BundleContextProvider());

    private static <S> String generateServiceFilter(Class<S> serviceClass) {
        return new StringBuilder(48).append("(").append(Constants.OBJECTCLASS).append('=').append(serviceClass.getName()).append(")").toString();
    }

    private static <S> ServiceValue<S> getService(Class<?> caller, Class<S> serviceClass, boolean required) throws ServiceException {
        final ServiceKey<S> serviceKey = new ServiceKey<>(caller, serviceClass);

        // Check cache
        {
            ServiceValue<S> serviceValue = (ServiceValue<S>) SERVICE_CACHE.get(serviceKey);
            if (null != serviceValue) {
                return serviceValue;
            }
        }

        BundleContextProvider bundleContextProvider = BC_PROVIDER_REF.get();
        if (null == bundleContextProvider) {
            if (required) {
                throw new ServiceException("Service '" + serviceClass.getName() + "' is not available!", serviceClass);
            }
            return null;
        }

        BundleContext bundleContext = bundleContextProvider.getBundleContext(caller, serviceClass);
        ServiceReference<S> serviceReference = bundleContext.getServiceReference(serviceClass);
        if (serviceReference == null) {
            if (required) {
                throw new ServiceException("Service '" + serviceClass.getName() + "' is not available!", serviceClass);
            }
            return null;
        }

        boolean ungetServiceHere = false;
        try {
            S service = bundleContext.getService(serviceReference);
            ungetServiceHere = true;
            if (service == null) {
                if (required) {
                    throw new ServiceException("Service '" + serviceClass.getName() + "' is not available!", serviceClass);
                }
                return null;
            }

            // Try to put into cache
            try {
                ServiceListener serviceListener = new ServiceListener() {

                    @Override
                    public void serviceChanged(ServiceEvent event) {
                        if (event.getType() == ServiceEvent.UNREGISTERING) {
                            ServiceValue<?> value = SERVICE_CACHE.remove(serviceKey);
                            if (null != value) {
                                value.ungetService();
                            }
                        }
                    }
                };
                bundleContext.addServiceListener(serviceListener, generateServiceFilter(serviceClass));

                ServiceValue<S> serviceValue = new ServiceValue<S>(service, serviceReference, bundleContext, false);
                SERVICE_CACHE.put(serviceKey, serviceValue);
                ungetServiceHere = false;
                return serviceValue;
            } catch (Exception e) {
                // Put into cache failed
            }

            ServiceValue<S> serviceValue = new ServiceValue<S>(service, serviceReference, bundleContext, true);
            ungetServiceHere = false;
            return serviceValue;
        } catch (RuntimeException e) {
            throw new ServiceException(e, serviceClass);
        } finally {
            if (ungetServiceHere) {
                bundleContext.ungetService(serviceReference);
            }
        }
    }

    /**
     * Performs a call to a specified service. The service is requested from the OSGi service registry and passed
     * to the call()-method of a given {@link ServiceUser}.
     *
     * @param caller The calling class. Will be used to determine the {@link BundleContext} for getting the service.
     *  Must not be <code>null</code>.
     * @param serviceClass The class of the required service. The service registry will be asked for a service according
     *  to this class.
     * @param serviceUser The {@link ServiceUser} that is called with the requested service.
     * @return The return value of {@link ServiceUser#call(Object)}.
     * @throws ServiceException if the service was not available or an error occurred during {@link ServiceUser#call(Object)}.
     */
    public static <S, T> T doServiceCall(Class<?> caller, Class<S> serviceClass, ServiceUser<S, T> serviceUser) throws ServiceException {
        ServiceValue<S> serviceValue = getService(caller, serviceClass, true);
        try {
            return serviceUser.call(serviceValue.service);
        } catch (Exception e) {
            throw new ServiceException(e, serviceClass);
        } finally {
            serviceValue.close();
        }
    }

    /**
     * Performs a call to a specified service. The service is requested from the OSGi service registry and passed
     * to the call()-method of a given {@link ServiceUser}. If the requested service is not available, the given
     * default value is returned.
     *
     * @param caller The calling class. Will be used to determine the {@link BundleContext} for getting the service.
     *  Must not be <code>null</code>.
     * @param serviceClass The class of the required service. The service registry will be asked for a service according
     *  to this class.
     * @param serviceUser The {@link ServiceUser} that is called with the requested service.
     * @return The return value of {@link ServiceUser#call(Object)}.
     * @throws ServiceException if an error occurred during {@link ServiceUser#call(Object)}.
     */
    public static <S, T> T tryServiceCall(Class<?> caller, Class<S> serviceClass, ServiceUser<S, T> serviceUser, T defaultValue) throws ServiceException {
        ServiceValue<S> serviceValue = getService(caller, serviceClass, false);
        if (null == serviceValue) {
            return defaultValue;
        }

        try {
            return serviceUser.call(serviceValue.service);
        } catch (Exception e) {
            throw new ServiceException(e, serviceClass);
        } finally {
            serviceValue.close();
        }
    }

    /**
     * Performs the call to the associated service.
     *
     * @param <S> The type of the service; e.g. <code>UserService</code>
     * @param <T> The type of the return value; e.g. <code>User</code>
     */
    public static interface ServiceUser<S, T> {

        /**
         * Performs the call to the associated service.
         *
         * @param service The service instance
         * @return The resulting return value
         * @throws Exception If invocation fails for any reason
         */
        T call(S service) throws Exception;
    }

    /**
     * This exception is thrown by the {@link ServiceCallWrapper}. It indicates either the absence
     * of the needed service or an exception thrown by the service call itself.
     *
     * In the first case {@link ServiceException#isServiceUnavailable()} returns <code>true</code>.
     * A more detailed message about why the service couldn't be obtained can then be got via
     * {@link ServiceException#getMessage()}. In the second case {@link ServiceException#isServiceUnavailable()}
     * returns <code>false</code> and the causing exception can be obtained via {@link ServiceException#getCause()}.
     *
     * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
     */
    public static class ServiceException extends Exception {

        private static final long serialVersionUID = 4795091520600135899L;

        private final Class<?> serviceClass;

        private final boolean isServiceUnavailable;

        ServiceException(String message, Class<?> serviceClass) {
            super(message);
            this.serviceClass = serviceClass;
            isServiceUnavailable = true;
        }

        ServiceException(Throwable cause, Class<?> serviceClass) {
            super(cause);
            this.serviceClass = serviceClass;
            isServiceUnavailable = false;
        }

        /**
         * @return <code>true</code> if the cause for this exception is a missing service, otherwise <code>false</code>.
         */
        public boolean isServiceUnavailable() {
            return isServiceUnavailable;
        }

        /**
         * Throws an {@link OXException} according to the actual cause of this exception.
         * <ul>
         * <li>If the service was not available, {@link ServiceExceptionCode#SERVICE_UNAVAILABLE} is thrown.</li>
         * <li>If the underlying exception is an {@link OXException}, it is simply re-thrown.</li>
         * <li>A generic {@link OXException} is thrown with the cause set to the cause of this exception.</li>
         * </ul>
         */
        public OXException toOXException() {
            if (isServiceUnavailable) {
                return ServiceExceptionCode.SERVICE_UNAVAILABLE.create(serviceClass.getName());
            }

            Throwable cause = getCause();
            if (cause instanceof OXException) {
                return (OXException) cause;
            }

            return new OXException(cause);
        }

        /**
         * Throws a runtime exception that either denotes the unavailability of the requested service or
         * encapsulates the root cause of this exception.
         */
        public RuntimeException toRuntimeException() {
            if (isServiceUnavailable) {
                return new RuntimeException("The required service " + serviceClass.getName() + " is temporary not available. Please try again later.");
            }

            return new RuntimeException(getCause());
        }

    }

    public static class BundleContextProvider {

        /**
         * Returns the {@link BundleContext} to retrieve the needed service. If possible the returned context
         * should always belong to the bundle of the calling class.
         *
         * @param caller The calling class
         * @param clazz The class of the needed service
         * @return The bundle context, never <code>null</code>
         * @throws ServiceException if no bundle context could be determined
         */
        public BundleContext getBundleContext(Class<?> caller, Class<?> clazz) throws ServiceException {
            Bundle bundle = FrameworkUtil.getBundle(caller);
            if (bundle == null) {
                throw new ServiceException("Class '" + caller.getName() + "' was loaded outside from OSGi!", clazz);
            }

            BundleContext bundleContext = bundle.getBundleContext();
            if (bundleContext == null) {
                throw new ServiceException("No valid bundle context exists for bundle '" + bundle.getSymbolicName() + "'!", clazz);
            }

            return bundleContext;
        }
    }

    private static final class ServiceKey<S> {

        final Class<?> caller;
        final Class<S> serviceClass;
        private final int hash;

        ServiceKey(Class<?> caller, Class<S> serviceClass) {
            super();
            this.caller = caller;
            this.serviceClass = serviceClass;

            int prime = 31;
            int result = 1;
            result = prime * result + ((caller == null) ? 0 : caller.hashCode());
            result = prime * result + ((serviceClass == null) ? 0 : serviceClass.hashCode());
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ServiceKey<?> other = (ServiceKey<?>) obj;
            if (caller == null) {
                if (other.caller != null) {
                    return false;
                }
            } else if (!caller.equals(other.caller)) {
                return false;
            }
            if (serviceClass == null) {
                if (other.serviceClass != null) {
                    return false;
                }
            } else if (!serviceClass.equals(other.serviceClass)) {
                return false;
            }
            return true;
        }
    }

    private static final class ServiceValue<S> implements AutoCloseable {

        private final BundleContext bundleContext;
        private final ServiceReference<S> serviceReference;
        private final boolean unget;
        final S service;

        ServiceValue(S service, ServiceReference<S> serviceReference, BundleContext bundleContext, boolean unget) {
            super();
            this.bundleContext = bundleContext;
            this.service = service;
            this.serviceReference = serviceReference;
            this.unget = unget;
        }

        void ungetService() {
            bundleContext.ungetService(serviceReference);
        }

        @Override
        public void close() {
            if (unget) {
                ungetService();
            }
        }
    }

}
