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

package com.openexchange.osgi.util;

import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
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

    static final AtomicReference<BundleContextProvider> BC_PROVIDER_REF = new AtomicReference<BundleContextProvider>(new BundleContextProvider());

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
        BundleContextProvider bundleContextProvider = BC_PROVIDER_REF.get();
        if (null == bundleContextProvider) {
            throw new ServiceException("Service '" + serviceClass.getName() + "' is not available!", serviceClass);
        }
        BundleContext bundleContext = bundleContextProvider.getBundleContext(caller, serviceClass);
        ServiceReference<S> serviceReference = bundleContext.getServiceReference(serviceClass);
        if (serviceReference == null) {
            throw new ServiceException("Service '" + serviceClass.getName() + "' is not available!", serviceClass);
        }

        try {
            S service = bundleContext.getService(serviceReference);
            if (service == null) {
                throw new ServiceException("Service '" + serviceClass.getName() + "' is not available!", serviceClass);
            }

            return serviceUser.call(service);
        } catch (Exception e) {
            throw new ServiceException(e, serviceClass);
        } finally {
            bundleContext.ungetService(serviceReference);
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
        BundleContextProvider bundleContextProvider = BC_PROVIDER_REF.get();
        if (null == bundleContextProvider) {
            return defaultValue;
        }
        BundleContext bundleContext = bundleContextProvider.getBundleContext(caller, serviceClass);
        ServiceReference<S> serviceReference = bundleContext.getServiceReference(serviceClass);
        if (serviceReference == null) {
            return defaultValue;
        }

        try {
            S service = bundleContext.getService(serviceReference);
            if (service == null) {
                return defaultValue;
            }

            return serviceUser.call(service);
        } catch (Exception e) {
            throw new ServiceException(e, serviceClass);
        } finally {
            bundleContext.ungetService(serviceReference);
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

    static class BundleContextProvider {

        /**
         * Returns the {@link BundleContext} to retrieve the needed service. If possible the returned context
         * should always belong to the bundle of the calling class.
         *
         * @param caller The calling class
         * @param clazz The class of the needed service
         * @return The bundle context, never <code>null</code>
         * @throws ServiceException if no bundle context could be determined
         */
        BundleContext getBundleContext(Class<?> caller, Class<?> clazz) throws ServiceException {
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

}
