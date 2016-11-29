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
 * Portions Copyright 2012 OPEN-XCHANGE, licensed under GPL Version 2.
 */

package com.openexchange.http.grizzly.service.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.MimeType;
import org.glassfish.grizzly.servlet.HttpServletRequestImpl;
import org.glassfish.grizzly.servlet.HttpServletResponseImpl;
import org.osgi.service.http.HttpContext;

/**
 * OSGi Resource {@link HttpHandler}.
 * <p/>
 * OSGi Resource registration integration.
 *
 * @author Hubert Iwaniuk
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class OSGiResourceHandler extends HttpHandler implements OSGiHandler {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OSGiResourceHandler.class);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final String alias;
    private final String prefix;
    private final HttpContext httpContext;
    private final OSGiServletContext servletContext;

    /**
     * Default constructor.
     *
     * @param alias       Registered under this alias.
     * @param prefix      Internal prefix.
     * @param httpContext Backing {@link org.osgi.service.http.HttpContext}.
     * @param logger      Logger utility.
     */
    public OSGiResourceHandler(String alias,
                               String prefix,
                               HttpContext httpContext,
                               OSGiServletContext servletContext) {
        super();
        //noinspection AccessingNonPublicFieldOfAnotherObject
//        super.commitErrorResponse = false;
        this.alias = alias;
        this.prefix = prefix;
        this.httpContext = httpContext;
        this.servletContext = servletContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void service(Request request, Response response) throws Exception {
        String requestURI = request.getDecodedRequestURI();
        LOG.debug("OSGiResourceHandler requestURI: {}", requestURI);
        String path = requestURI.replaceFirst(alias, prefix);
        try {
            // authentication
            if (!authenticate(request, response, servletContext)) {
                LOG.debug("OSGiResourceHandler Request not authenticated ({}).", requestURI);
                return;
            }
        } catch (IOException e) {
            LOG.warn("Error while authenticating request: {}", request, e);
        }

        // find resource
        URL resource = httpContext.getResource(path);
        if (resource == null) {
            LOG.debug("OSGiResourceHandler \'{}\' Haven't found '{}'.", alias, path);
            response.setStatus(404);
            return;
        } else {
            response.setStatus(200);
        }

        // MIME handling
        String mime = httpContext.getMimeType(path);
        if (mime == null) {
            mime = MimeType.getByFilename(path);
        }
        if (mime != null) {
            response.setContentType(mime);
        }

        try {
            final URLConnection urlConnection = resource.openConnection();
            final int length = urlConnection.getContentLength();
            final InputStream is = urlConnection.getInputStream();
            final OutputStream os = response.getOutputStream();

            byte buff[] = new byte[1024*8];
            int read, total = 0;
            while ((read = is.read(buff)) > 0) {
                total += read;
                os.write(buff, 0, read);
            }
            os.flush();
            response.finish();
            if (total != length) {
                LOG.warn("Was supposed to send {}, but sent {}", length, total);
            }
        } catch (IOException e) {
            LOG.warn("", e);
        }
    }

    /**
     * Checks authentication.
     * <p/>
     * Calls {@link HttpContext#handleSecurity} to authenticate.
     *
     * @param request  Request to authenticate.
     * @param response Response to populate if authentication not performed but needed.
     * @param servletContext Context needed for proper HttpServletRequest creation.
     * @return <code>true</code> if authenticated and can proceed with processing, else <code>false</code>.
     * @throws IOException Propagate exception thrown by {@link HttpContext#handleSecurity}.
     */
    private boolean authenticate(Request request, Response response,
            OSGiServletContext servletContext) throws IOException {

        HttpServletRequestImpl servletRequest =
                new OSGiHttpServletRequest(servletContext);
        HttpServletResponseImpl servletResponse = HttpServletResponseImpl.create();

        servletResponse.initialize(response, servletRequest);
        servletRequest.initialize(request, servletResponse, servletContext);

        return httpContext.handleSecurity(servletRequest, servletResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReentrantReadWriteLock.ReadLock getProcessingLock() {
        return lock.readLock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReentrantReadWriteLock.WriteLock getRemovalLock() {
        return lock.writeLock();
    }

    private static class OSGiHttpServletRequest extends HttpServletRequestImpl {

        public OSGiHttpServletRequest(
                OSGiServletContext context) throws IOException {
            super();
            setContextImpl(context);
        }
    }
}
