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

package com.openexchange.folderstorage.osgi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;


/**
 * {@link FolderStorageServices}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class FolderStorageServices implements ServiceTrackerCustomizer<Object, Object> {

    private static final FolderStorageServices INSTANCE = new FolderStorageServices();

    /**
     * Gets the service if available.
     *
     * @return The service or <code>null</code>.
     */
    public static <T> T getService(Class<T> clazz) {
        return (T) INSTANCE.services.get(clazz);
    }

    /**
     * Gets the service.
     *
     * @return The service.
     * @throws OXException {@link ServiceExceptionCode#SERVICE_UNAVAILABLE} if service is unavailable.
     */
    public static <T> T requireService(Class<T> clazz) throws OXException {
        T service = (T) INSTANCE.services.get(clazz);
        if (service == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(clazz.getName());
        }

        return service;
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    private final ConcurrentMap<Class<?>, Object> services = new ConcurrentHashMap<>();
    private final AtomicReference<BundleContext> contextRef = new AtomicReference<BundleContext>();
    private final AtomicReference<Class<?>[]> serviceClassesRef = new AtomicReference<Class<?>[]>();

    private FolderStorageServices() {
        super();
    }

    static FolderStorageServices init(BundleContext context, Class<?>[] services) {
        INSTANCE.contextRef.set(context);
        INSTANCE.serviceClassesRef.set(services);
        return INSTANCE;
    }

    @Override
    public Object addingService(ServiceReference<Object> reference) {
        BundleContext context = contextRef.get();
        if (context == null) {
            return null;
        }

        Object service = context.getService(reference);
        if (service == null) {
            context.ungetService(reference);
            return null;
        }

        Class<?>[] serviceClasses = serviceClassesRef.get();
        for (Class<?> clazz : serviceClasses) {
            if (clazz.isAssignableFrom(service.getClass())) {
                services.put(clazz, service);
                return service;
            }
        }

        // Service is not of interest; discard it
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<Object> reference, Object service) {
        // Nothing
    }

    @Override
    public void removedService(ServiceReference<Object> reference, Object service) {
        Class<?>[] serviceClasses = serviceClassesRef.get();
        for (Class<?> clazz : serviceClasses) {
            if (clazz.isAssignableFrom(service.getClass())) {
                services.remove(clazz, service);
                break;
            }
        }

        BundleContext context = contextRef.get();
        if (context != null) {
            context.ungetService(reference);
        }
    }

}
