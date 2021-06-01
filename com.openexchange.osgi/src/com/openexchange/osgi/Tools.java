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

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Stack;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link Tools} - Provides utility methods for OSGi programming.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Tools {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Tools.class);

    /**
     * Generates an OR filter matching the services given in the classes varargs.
     *
     * @param context The bundle context to use for creating the filter from comiled expression
     * @param classes The classes of the services for which to yield the filter
     * @throws InvalidSyntaxException if the syntax of the generated filter is not correct.
     */
    public static final Filter generateServiceFilter(final BundleContext context, final Class<?>... classes) throws InvalidSyntaxException {
        if (null == classes) {
            throw new IllegalArgumentException("classes is null.");
        }

        if (0 == classes.length) {
            throw new IllegalArgumentException("classes is empty.");
        }

        if (classes.length == 1) {
            StringBuilder sb = new StringBuilder(64).append("(");
            sb.append(Constants.OBJECTCLASS);
            sb.append('=');
            sb.append(classes[0].getName());
            sb.append(")");
            return context.createFilter(sb.toString());
        }

        StringBuilder sb = new StringBuilder(16 << classes.length).append("(|(");
        for (Class<?> clazz : classes) {
            sb.append(Constants.OBJECTCLASS);
            sb.append('=');
            sb.append(clazz.getName());
            sb.append(")(");
        }
        sb.setCharAt(sb.length() - 1, ')');
        return context.createFilter(sb.toString());
    }

    public static final void open(Collection<ServiceTracker<?,?>> trackers) {
        for (ServiceTracker<?,?> tracker : trackers) {
            tracker.open();
        }
    }

    public static final void close(Stack<ServiceTracker<?,?>> trackers) {
        while (!trackers.isEmpty()) {
            trackers.pop().close();
        }
    }

    /**
     * Obtains a service from the given {@link ServiceLookup} and returns it. If the
     * service is not available, {@link ServiceExceptionCode#SERVICE_UNAVAILABLE} is thrown.
     * @param serviceClass The service class to obtain
     * @param serviceLookup The service lookup to obtain the service from
     * @return The service
     * @throws OXException if the service is not available
     */
    public static <T> T requireService(Class<T> serviceClass, ServiceLookup serviceLookup) throws OXException {
        T service = serviceLookup.getService(serviceClass);
        if (service == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(serviceClass.getName());
        }

        return service;
    }

    /**
     * Safely ungets the service associated with specified service reference using given bundle context.
     *
     * @param reference The service reference to unget
     * @param context The bundle context to use
     */
    public static <S> void ungetServiceSafe(ServiceReference<S> reference, BundleContext context) {
        if (null != reference && null != context) {
            try {
                context.ungetService(reference);
            } catch (Exception e) {
                LOG.debug("Failed to unget service.", e);
            }
        }
    }

    /**
     * Checks if specified bundle is a fragment bundle by testing presence of special <code>"Fragment-Host"</code> header (specified in
     * <code>MANIFEST.MF</code> file) in given bundle.
     * <p>
     * Example:
     * <pre>
     *     Manifest-Version: 1.0
     *     Bundle-ManifestVersion: 2
     *     Bundle-SymbolicName: com.openexchange.logback.classic.extensions
     *     Fragment-Host: ch.qos.logback.classic
     *     ...
     * </pre>
     *
     * @param bundle The bundle to check
     * @return <code>true</code> if specified bundle is a fragment bundle; else <code>false</code>
     */
    public static boolean isFragment(final Bundle bundle) {
        return (null != bundle.getHeaders().get(Constants.FRAGMENT_HOST));
    }

    /**
     * Checks if specified bundle is <b>not</b> a fragment bundle.
     *
     * @param bundle The bundle to check
     * @return <code>true</code> if specified bundle is <b>not</b> a fragment bundle; else <code>false</code>
     */
    public static boolean isNoFragment(final Bundle bundle) {
        return isFragment(bundle) == false;
    }

    /**
     * Checks if specified bundle is <b>not</b> a fragment bundle <small><b>AND</b></small> its state is <code>ACTIVE</code>.
     *
     * @param bundle The bundle to check
     * @return <code>true</code> if specified bundle is <b>not</b> a fragment bundle <small><b>AND</b></small> its state is <code>ACTIVE</code>; else <code>false</code>
     */
    public static boolean isNoFragmentAndActive(final Bundle bundle) {
        return (isNoFragment(bundle) && (Bundle.ACTIVE == bundle.getState()));
    }

    /**
     * Creates a new dictionary containing the specified ranking number to be used on service registration.
     * <p>
     * The service ranking is used by the Framework to determine the <i>natural
     * order</i> of services, see {@link ServiceReference#compareTo(Object)},
     * and the <i>default</i> service to be returned from a call to the
     * {@link BundleContext#getServiceReference(Class)} or
     * {@link BundleContext#getServiceReference(String)} method.
     * <p>
     * The default ranking is zero (0). A service with a ranking of
     * {@code Integer.MAX_VALUE} is very likely to be returned as the default
     * service, whereas a service with a ranking of {@code Integer.MIN_VALUE} is
     * very unlikely to be returned.
     *
     * @param ranking The ranking
     * @return The newly created directory containing the specified ranking number
     */
    public static Dictionary<String, Object> withRanking(int ranking) {
        return withRanking(Integer.valueOf(ranking));
    }

    /**
     * Creates a new dictionary containing the specified ranking number to be used on service registration.
     * <p>
     * The service ranking is used by the Framework to determine the <i>natural
     * order</i> of services, see {@link ServiceReference#compareTo(Object)},
     * and the <i>default</i> service to be returned from a call to the
     * {@link BundleContext#getServiceReference(Class)} or
     * {@link BundleContext#getServiceReference(String)} method.
     * <p>
     * The default ranking is zero (0). A service with a ranking of
     * {@code Integer.MAX_VALUE} is very likely to be returned as the default
     * service, whereas a service with a ranking of {@code Integer.MIN_VALUE} is
     * very unlikely to be returned.
     *
     * @param ranking The ranking
     * @return The newly created directory containing the specified ranking number
     */
    public static Dictionary<String, Object> withRanking(Integer ranking) {
        if (ranking == null) {
            throw new IllegalArgumentException("Ranking must not be null");
        }

        Dictionary<String, Object> properties = new Hashtable<>(2);
        properties.put(Constants.SERVICE_RANKING, ranking);
        return properties;
    }

    private Tools() {
        super();
    }
}
