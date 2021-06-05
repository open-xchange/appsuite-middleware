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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 *
 * Portions Copyright 2016-2020 OX Software GmbH, licensed under GPL Version 2.
 */

package com.openexchange.http.grizzly.service.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.servlet.Filter;
import javax.servlet.ServletException;
import org.glassfish.grizzly.http.server.HttpServer;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;

/**
 * Grizzly OSGi {@link HttpService} {@link ServiceFactory}.
 *
 * @author Hubert Iwaniuk
 * @since Jan 20, 2009
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class HttpServiceFactory implements ServiceFactory<HttpService> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HttpServiceFactory.class);

    private final OSGiMainHandler mainHttpHandler;
    private final List<FilterAndPath> initialFilters;
    private final Map<Bundle, OSGiMainHandler> httpHandlers;
    private final boolean supportHierachicalLookupOnNotFound;
    private final int sessionTimeoutInSeconds;

    public HttpServiceFactory(HttpServer httpServer, List<FilterAndPath> initialFilters, boolean supportHierachicalLookupOnNotFound, int sessionTimeoutInSeconds, Bundle bundle) {
        super();
        this.supportHierachicalLookupOnNotFound = supportHierachicalLookupOnNotFound;
        this.sessionTimeoutInSeconds = sessionTimeoutInSeconds;
        Map<Bundle, OSGiMainHandler> httpHandlers = new HashMap<>(128, 0.9F);
        this.httpHandlers = httpHandlers;
        List<FilterAndPath> filters = new CopyOnWriteArrayList<>(initialFilters);
        this.initialFilters = filters;
        mainHttpHandler = new OSGiMainHandler(filters, supportHierachicalLookupOnNotFound, sessionTimeoutInSeconds, bundle);
        httpServer.getServerConfiguration().addHttpHandler(mainHttpHandler, "/");
        httpHandlers.put(bundle, mainHttpHandler);
    }

    /**
     * Adds specified filter to all HTTP contexts.
     *
     * @param filter The filter to add
     * @param path The filter path
     * @throws ServletException If registering specified filter fails
     */
    public synchronized void addServletFilter(Filter filter, String path) throws ServletException {
        // Add 'initialFilters' to also apply filter to subsequently created OSGiMainHandler instances
        initialFilters.add(new FilterAndPath(filter, path));

        // Add filter to existing OSGiMainHandler instances
        for (OSGiMainHandler httpHandler : httpHandlers.values()) {
            httpHandler.addServletFilter(filter, path);
        }
    }

    /**
     * Removes specified filter from all HTTP contexts.
     *
     * @param filter The filter to remove
     */
    public synchronized void removeServletFilter(Filter filter) {
        // Remove filter from existing OSGiMainHandler instances
        for (OSGiMainHandler httpHandler : httpHandlers.values()) {
            httpHandler.removeServletFilter(filter);
        }

        // Also drop from 'initialFilters' to avoid subsequently created OSGiMainHandler instances still use it
        initialFilters.remove(new FilterAndPath(filter, null));
    }

    @Override
    public synchronized HttpService getService(final Bundle bundle, final ServiceRegistration<HttpService> serviceRegistration) {
        LOG.debug("Bundle: {}, is getting HttpService with serviceRegistration: {}", bundle, serviceRegistration);

        OSGiMainHandler bundleHttpHandler = new OSGiMainHandler(initialFilters, supportHierachicalLookupOnNotFound, sessionTimeoutInSeconds, bundle);
        httpHandlers.put(bundle, bundleHttpHandler);
        return new HttpServiceImpl(bundleHttpHandler, bundle);
    }

    @Override
    public synchronized void ungetService(final Bundle bundle, final ServiceRegistration<HttpService> serviceRegistration, final HttpService httpServiceObj) {
        LOG.debug("Bundle: {}, is ungetting HttpService with serviceRegistration: {}", bundle, serviceRegistration);
        mainHttpHandler.uregisterAllLocal();
        httpHandlers.remove(bundle);
    }

    /**
     * Clean up.
     */
    public synchronized void stop() {
        LOG.info("Stoping main handler");
        mainHttpHandler.unregisterAll();
        httpHandlers.clear();
    }

}
