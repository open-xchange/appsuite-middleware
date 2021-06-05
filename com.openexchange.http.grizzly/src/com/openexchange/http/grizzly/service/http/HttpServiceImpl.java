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
 * Copyright (c) 2009-2013 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Dictionary;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;

/**
 * Grizzly OSGi HttpService implementation.
 *
 * @author Hubert Iwaniuk
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since Jan 20, 2009
 */
public class HttpServiceImpl implements HttpServiceExtension {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HttpServiceImpl.class);

    private final Bundle bundle;
    private final OSGiMainHandler mainHttpHandler;

    // ------------------------------------------------------------ Constructors


    /**
     * {@link HttpService} constructor.
     *
     * @param mainHttpHandler The bundle-associated HTTP handler
     * @param bundle {@link org.osgi.framework.Bundle} that got this instance of {@link org.osgi.service.http.HttpService}.
     */
    public HttpServiceImpl(OSGiMainHandler mainHttpHandler, Bundle bundle) {
        super();
        this.bundle = bundle;
        this.mainHttpHandler = mainHttpHandler;
    }

    // ------------------------------------------------ Methods from HttpService


    /**
     * {@inheritDoc}
     */
    @Override
    public HttpContext createDefaultHttpContext() {
        return new HttpContextImpl(bundle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerServlet(
            final String alias, final Servlet servlet, final Dictionary<?, ?> initparams, HttpContext httpContext)
            throws ServletException, NamespaceException {

        // LOG.info("Registering servlet: {}, under: {}, with: {} and context: {}", servlet, alias, initparams, httpContext);
        LOG.info("Registering servlet: {}, under: {} and context: {}", servlet, alias, httpContext);

        mainHttpHandler.registerServletHandler(alias, servlet, initparams, httpContext, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerResources(final String alias, String prefix, HttpContext httpContext)
            throws NamespaceException {

        LOG.info("Registering resource: alias: {}, prefix: {} and context: {}", alias, prefix, httpContext);

        mainHttpHandler.registerResourceHandler(alias, httpContext, prefix, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregister(final String alias) {
        LOG.info("Unregistering alias: {}", alias);
        mainHttpHandler.unregisterAlias(alias);
    }


    // --------------------------------------- Methods from HttpServiceExtension


    /**
     * {@inheritDoc}
     */
    @Override
    public void registerFilter(Filter filter, String urlPattern, Dictionary<?, ?> initParams, HttpContext context)
    throws ServletException {
        LOG.info("Registering servlet: {}, under url-pattern: {}, with: {} and context: {}", filter, urlPattern, initParams, context);
        mainHttpHandler.registerFilter(filter, urlPattern, initParams, context, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterFilter(Filter filter) {
        LOG.info("Unregister filter: {}", filter);
        mainHttpHandler.unregisterFilter(filter);
    }

}
