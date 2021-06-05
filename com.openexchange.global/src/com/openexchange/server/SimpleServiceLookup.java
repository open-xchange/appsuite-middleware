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

package com.openexchange.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * {@link SimpleServiceLookup} - A simple service look-up.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class SimpleServiceLookup implements ServiceLookup {

    private final ConcurrentMap<Class<?>, Object> services;

    /**
     * Initializes a new {@link SimpleServiceLookup}.
     */
    public SimpleServiceLookup() {
        super();
        services = new ConcurrentHashMap<Class<?>, Object>();
    }

    @Override
    public <S> S getService(Class<? extends S> clazz) {
        S service = getOptionalService(clazz);
        if (null == service) {
            throw new IllegalStateException("Missing service " + clazz.getName());
        }
        return service;
    }

    @Override
    public <S> S getOptionalService(Class<? extends S> clazz) {
        @SuppressWarnings("unchecked") S service = (S) services.get(clazz);
        return service;
    }

    /**
     * Adds specified service to this service look-up (replacing any possibly existing association).
     *
     * @param clazz The service's class
     * @param service The service implementation
     */
    public <S> void add(Class<? extends S> clazz, S service) {
        services.put(clazz, service);
    }

    /**
     * Removes specified service
     *
     * @param clazz The service's class
     * @return The removed service implementation or <code>null</code> if there was no such service
     */
    public <S> S remove(Class<? extends S> clazz) {
        @SuppressWarnings("unchecked") S removedService = (S) services.remove(clazz);
        return removedService;
    }



}
