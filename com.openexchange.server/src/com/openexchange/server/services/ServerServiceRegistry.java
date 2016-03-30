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
        final StringBuilder sb = new StringBuilder(256);
        sb.append("Server service registry:\n");
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
