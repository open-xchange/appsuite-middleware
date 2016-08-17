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
