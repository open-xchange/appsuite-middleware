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
