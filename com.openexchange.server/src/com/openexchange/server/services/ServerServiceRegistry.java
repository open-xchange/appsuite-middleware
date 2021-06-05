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

package com.openexchange.server.services;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link ServerServiceRegistry} - A registry for services needed by server
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ServerServiceRegistry {

    private static final ServerServiceRegistry REGISTRY = new ServerServiceRegistry();

    /**
     * Gets the server's service registry
     *
     * @return The server's service registry
     */
    public static ServerServiceRegistry getInstance() {
        return REGISTRY;
    }

    /**
     * Gets the service defined by given class
     *
     * @param <S> The type of service's class
     * @param clazz The service's class
     * @return The service if found; otherwise <code>null</code>
     */
    public static <S extends Object> S getServize(final Class<? extends S> clazz) {
        return getInstance().getService(clazz);
    }

    /**
     * Gets the service defined by given class
     *
     * @param <S> The type of service's class
     * @param clazz The service's class
     * @param failOnError <code>true</code> to throw an appropriate {@link OXException} if service is missing; otherwise
     *            <code>false</code>
     * @return The service if found; otherwise <code>null</code> or an appropriate {@link OXException} is thrown dependent on
     *         <code>failOnError</code> parameter.
     * @throws OXException If <code>failOnError</code> parameter is set to <code>true</code> and service is missing
     */
    public static <S extends Object> S getServize(final Class<? extends S> clazz, final boolean failOnError) throws OXException {
        return getInstance().getService(clazz, failOnError);
    }

    // ------------------------------------------------------------------------------------------------ //

    private final Map<Class<?>, Object> services;

    /**
     * Initializes a new {@link ServerServiceRegistry}
     */
    private ServerServiceRegistry() {
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
     * Removes a service bound to given class from this service registry
     *
     * @param clazz The service's class
     */
    public void removeService(final Class<?> clazz) {
        services.remove(clazz);
    }

    /**
     * Adds a service bound to given class to this service registry
     *
     * @param clazz The service's class
     * @param service The service itself
     */
    public <S extends Object> void addService(final Class<? extends S> clazz, final S service) {
        services.put(clazz, service);
    }

    /**
     * Adds a service bound to its class to this service registry
     *
     * @param service The service
     */
    public <S extends Object> void addService(final S service) {
        services.put(service.getClass(), service);
    }

    /**
     * Gets the service defined by given class
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
     * Gets the service defined by given class
     *
     * @param <S> The type of service's class
     * @param clazz The service's class
     * @param failOnError <code>true</code> to throw an appropriate {@link OXException} if service is missing; otherwise
     *            <code>false</code>
     * @return The service if found; otherwise <code>null</code> or an appropriate {@link OXException} is thrown dependent on
     *         <code>failOnError</code> parameter.
     * @throws OXException If <code>failOnError</code> parameter is set to <code>true</code> and service is missing
     */
    public <S extends Object> S getService(final Class<? extends S> clazz, final boolean failOnError) throws OXException {
        final Object service = services.get(clazz);
        if (null == service) {
            /*
             * Service is not present
             */
            if (failOnError) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create( clazz.getName());
            }
            return null;
        }
        return clazz.cast(service);
    }

    @Override
    public String toString() {
        Iterator<Map.Entry<Class<?>, Object>> iter = services.entrySet().iterator();
        if (false == iter.hasNext()) {
            return "Server service registry:\n<empty>";
        }

        StringBuilder sb = new StringBuilder(512);
        sb.append("Server service registry:");
        do {
            Map.Entry<Class<?>, Object> e = iter.next();
            sb.append('\n').append(e.getKey().getName()).append(": ").append(e.getValue().toString());
        } while (iter.hasNext());
        return sb.toString();
    }

}
