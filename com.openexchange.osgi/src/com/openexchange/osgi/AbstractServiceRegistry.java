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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link AbstractServiceRegistry} can be used to create a singleton inside a bundle for holding concrete service implementations.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class AbstractServiceRegistry {

    private final Map<Class<?>, Object> services;

    /**
     * Initializes a new {@link AbstractServiceRegistry}.
     */
    protected AbstractServiceRegistry() {
        super();
        services = new ConcurrentHashMap<Class<?>, Object>();
    }

    /**
     * Clears the whole registry
     */
    public void clearRegistry() {
        services.clear();
    }

    /**
     * Removes a service bound to given class from this service registry.
     *
     * @param clazz The service's class
     */
    public <S extends Object> S removeService(final Class<S> clazz) {
        return clazz.cast(services.remove(clazz));
    }

    /**
     * Adds a service bound to given class to this service registry.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: Any existing service associated with specified service's class is replaced.
     * </div>
     *
     * @param clazz The service's class
     * @param service The service itself
     */
    public <S extends Object> void addService(final Class<? extends S> clazz, final S service) {
        services.put(clazz, service);
    }

    /**
     * Gets the service defined by given class.
     *
     * @param <S> The type of service's class
     * @param clazz The service's class
     * @return The service if found; otherwise <code>null</code>
     */
    public <S extends Object> S getService(final Class<? extends S> clazz) {
        final Object service = services.get(clazz);
        if (null == service) {
            /*
             * Service is not present
             */
            return null;
        }
        return clazz.cast(service);
    }

    /**
     * Gets the service defined by given class.
     *
     * @param <S> The type of service's class
     * @param clazz The service's class
     * @param errorOnAbsence <code>true</code> to throw an error on service absence; otherwise <code>false</code>
     * @return The service if found; otherwise <code>null</code> if <code>errorOnAbsence</code> is <code>false</code>
     * @throws OXException If service is unavailable and <code>errorOnAbsence</code> is <code>true</code>
     */
    public <S extends Object> S getService(final Class<? extends S> clazz, final boolean errorOnAbsence) throws OXException {
        final Object service = services.get(clazz);
        if (null == service) {
            /*
             * Service is not present
             */
            if (errorOnAbsence) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(clazz.getName());
            }
            return null;
        }
        return clazz.cast(service);
    }

    /**
     * Gets the number of services currently held by this service registration
     *
     * @return The number of services currently held by this service registration
     */
    public int size() {
        return services.size();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256);
        sb.append(this.getClass().getName() + " registry:\n");
        if (services.isEmpty()) {
            sb.append("<empty>");
        } else {
            final Iterator<Map.Entry<Class<?>, Object>> iter = services.entrySet().iterator();
            while (true) {
                final Map.Entry<Class<?>, Object> e = iter.next();
                sb.append(e.getKey().getName()).append(": ").append(e.getValue().toString());
                if (iter.hasNext()) {
                    sb.append('\n');
                } else {
                    break;
                }
            }
        }
        return sb.toString();
    }
}
