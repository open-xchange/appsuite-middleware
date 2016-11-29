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

import org.glassfish.grizzly.servlet.FilterRegistration;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.log.LogProperties;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import org.glassfish.grizzly.http.server.ErrorPageGenerator;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.util.MappingData;
import org.glassfish.grizzly.http.util.HttpStatus;

/**
 * OSGi Main HttpHandler.
 * <p/>
 * Dispatching HttpHandler.
 * Grizzly integration.
 * <p/>
 * Responsibilities:
 * <ul>
 * <li>Manages registration data.</li>
 * <li>Dispatching {@link HttpHandler#service(Request, Response)} method call to registered
 * {@link HttpHandler}s.</li>
 * </ul>
 *
 * @author Hubert Iwaniuk
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class OSGiMainHandler extends HttpHandler implements OSGiHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OSGiMainHandler.class);

    private static final AtomicBoolean SHUTDOWN_REQUESTED = new AtomicBoolean(false);

    /**
     * Sets the "shut-down requested" marker.
     */
    public static void markShutdownRequested() {
        SHUTDOWN_REQUESTED.set(true);
    }

    /**
     * Unsets the "shut-down requested" marker.
     */
    public static void unmarkShutdownRequested() {
        SHUTDOWN_REQUESTED.set(false);
    }

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Bundle bundle;
    private final List<Filter> initialFilters;
    private final OSGiCleanMapper mapper;
    private final HttpStatus shutDownStatus;
    private final ErrorPageGenerator errorPageGenerator;

    /**
     * Constructor.
     *
     * @param initialFilters The initial Servlet filter to apply
     * @param bundle Bundle that we create if for, for local data reference.
     */
    public OSGiMainHandler(List<Filter> initialFilters, Bundle bundle) {
        super();
        this.initialFilters = initialFilters;
        this.bundle = bundle;
        this.mapper = new OSGiCleanMapper();
        this.shutDownStatus = HttpStatus.newHttpStatus(HttpStatus.SERVICE_UNAVAILABLE_503.getStatusCode(), "Server shutting down...");
        errorPageGenerator = new ErrorPageGeneratorImpl();
    }

    /**
     * Service method dispatching to registered handlers.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public void service(Request request, Response response) throws Exception {
        boolean invoked = false;
        String alias = request.getDecodedRequestURI();
        String originalAlias = alias;
        LOG.debug("Serviceing URI: {}", alias);
        // first lookup needs to be done for full match.
        boolean cutOff = false;
        while (true) {
            LOG.debug("CutOff: {}, alias: {}", cutOff, alias);
            alias = OSGiCleanMapper.map(alias, cutOff);
            if (alias == null) {
                if (cutOff) {
                    // not found
                    break;
                }
                // switching to reducing mapping mode (removing after last '/' and searching)
                LOG.debug("Switching to reducing mapping mode.");
                cutOff = true;
                alias = originalAlias;
            } else {
                if (SHUTDOWN_REQUESTED.get()) {
                    // 503 - Service Unavailable
                    response.setStatus(shutDownStatus);
                    return;
                }

                HttpHandler httpHandler = OSGiCleanMapper.getHttpHandler(alias);

                ReadLock processingLock = ((OSGiHandler) httpHandler).getProcessingLock();
                processingLock.lock();
                try {
                    updateMappingInfo(request, alias, originalAlias);

                    httpHandler.service(request, response);
                } catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    StringBuilder logBuilder = new StringBuilder(128).append("Error processing request:\n");
                    logBuilder.append(LogProperties.getAndPrettyPrint(LogProperties.Name.SESSION_SESSION));
                    appendRequestInfo(logBuilder, request);
                    LOG.error(logBuilder.toString(), t);
                    // 500 - Internal Server Error
                    response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                } finally {
                    processingLock.unlock();
                }
                invoked = true;
                if (response.getStatus() != 404) {
                    break;
                } else if ("/".equals(alias)) {
                    // 404 in "/", cutoff algo will not escape this one.
                    break;
                } else if (!cutOff){
                    // not found and haven't run in cutoff mode
                    cutOff = true;
                }
            }
        }
        if (!invoked) {
            response.setStatus(HttpStatus.NOT_FOUND_404);
            try {
                writeErrorPage(request, response);
            } catch (Exception e) {
                LOG.warn("Failed to commit 404 status.", e);
            }
        }
    }

    /**
     * Appends request information.
     *
     * @param builder The builder to append to
     */
    private void appendRequestInfo(final StringBuilder builder, Request request) {
        builder.append("request-URI=''");
        builder.append(request.getRequestURI());
        builder.append("'', query-string=''");
        builder.append(request.getQueryString());
        builder.append("''");
    }

    /**
     * Registers {@link services.http.OSGiServletHandler} in OSGi Http Service.
     * <p/>
     * Keeps truck of all registrations, takes care of thread safety.
     *
     * @param alias       Alias to register, if wrong value than throws {@link org.osgi.service.http.NamespaceException}.
     * @param servlet     Servlet to register under alias, if fails to {@link javax.servlet.Servlet#init(javax.servlet.ServletConfig)}
     *                    throws {@link javax.servlet.ServletException}.
     * @param initparams  Initial parameters to populate {@link javax.servlet.ServletContext} with.
     * @param context     OSGi {@link org.osgi.service.http.HttpContext}, provides mime handling, security and bundle specific resource access.
     * @param httpService Used to {@link HttpService#createDefaultHttpContext()} if needed.
     * @throws org.osgi.service.http.NamespaceException
     *                                        If alias was invalid or already registered.
     * @throws javax.servlet.ServletException If {@link javax.servlet.Servlet#init(javax.servlet.ServletConfig)} fails.
     */
    public void registerServletHandler(final String alias,
                                       final Servlet servlet,
                                       final Dictionary initparams,
                                       HttpContext context,
                                       final HttpService httpService)
            throws NamespaceException, ServletException {

        ReentrantLock lock = OSGiCleanMapper.getLock();
        lock.lock();
        try {
            // TODO: clean up OX servlet structure so we can apply alias and servlet validation

            validateAlias4RegOk(alias);

            /*
             * Currently only checks if servlet is already registered. This prevents the same servlet with different aliases. Disabled until
             * we don't have to register the DispatcherServlet multiple times under different aliases.
             */
            // validateServlet4RegOk(servlet, alias);

            /*
             * A context provides methods to getResources, Mimetypes and handle security. It's implemented by users of the httpservice.
             * Servlets with the same HttpContext share the same ServletContext. If the reference is null we use a default context.
             */
            if (context == null) {
                LOG.debug("No HttpContext provided, creating default");
                context = httpService.createDefaultHttpContext();
            }

            OSGiServletHandler servletHandler =
                    findOrCreateOSGiServletHandler(servlet, context, initparams);
            servletHandler.setServletPath(alias);
            addInitialServletFilters(context);

            /*
             * Servlet would be started several times if registered with multiple aliases. Starting means: 1. Set ContextPath 2. Instantiate
             * Servlet if null 3. Call init(config) on the Servlet.
             */
            LOG.debug("Initializing Servlet been registered");
            servletHandler.startServlet(); // this might throw ServletException, throw it to offending bundle.

            // Add the servletPath and the OSGiServletHandler to the backing map.
            mapper.addHttpHandler(alias, servletHandler);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Add our default set of Filters to the ServletHandler.
     *
     * @param context The associated HTTP context
     * @throws ServletException
     */
    private void addInitialServletFilters(HttpContext context) throws ServletException {
        for (Filter initialFilter : initialFilters) {
            registerFilter(initialFilter, "/*", null, context, null);
        }
    }

    /**
     *
     * @param filter
     * @param urlPattern
     * @param initparams
     * @param context
     * @param httpService
     * @throws NamespaceException
     * @throws ServletException
     */
    public void registerFilter(final Filter filter,
                               final String urlPattern,
                               final Dictionary initparams,
                               HttpContext context,
                               final HttpService httpService)
            throws ServletException {

        ReentrantLock lock = OSGiCleanMapper.getLock();
        lock.lock();
        try {


            if (context == null) {
                LOG.debug("No HttpContext provided, creating default");
                context = httpService.createDefaultHttpContext();
            }

            OSGiServletContext servletContext =
                    mapper.getServletContext(context);
            if (servletContext == null) {
                mapper.addContext(context, null);
                servletContext = mapper.getServletContext(context);
            }

            FilterRegistration registration =
                    servletContext.addFilter(Integer.toString(filter.hashCode()), filter);
            registration.addMappingForUrlPatterns(null, urlPattern);

            filter.init(new OSGiFilterConfig(servletContext));

        } finally {
            lock.unlock();
        }
    }

    /**
     * Registers {@link OSGiResourceHandler} in OSGi Http Service.
     * <p/>
     * Keeps truck of all registrations, takes care of thread safety.
     *
     * @param alias          Alias to register, if wrong value than throws {@link NamespaceException}.
     * @param context        OSGi {@link HttpContext}, provides mime handling, security and bundle specific resource access.
     * @param internalPrefix Prefix to map request for this alias to.
     * @param httpService Used to {@link HttpService#createDefaultHttpContext()} if needed.
     * @throws NamespaceException If alias was invalid or already registered.
     */
    public void registerResourceHandler(String alias, HttpContext context, String internalPrefix,
                                        HttpService httpService)
            throws NamespaceException {

        ReentrantLock lock = OSGiCleanMapper.getLock();
        lock.lock();
        try {
            validateAlias4RegOk(alias);

            if (context == null) {
                LOG.debug("No HttpContext provided, creating default");
                context = httpService.createDefaultHttpContext();
            }
            if (internalPrefix == null) {
                internalPrefix = "";
            }
            OSGiServletContext servletContext = mapper.getServletContext(context);

            mapper.addHttpHandler(alias,
                                  new OSGiResourceHandler(alias,
                                                          internalPrefix,
                                                          context,
                                                          servletContext));
        } finally {
            lock.unlock();
        }
    }

    /**
     * Unregisters previously registered alias.
     * <p/>
     * Keeps truck of all registrations, takes care of thread safety.
     *
     * @param alias       Alias to unregister, if not owning alias {@link IllegalArgumentException} is thrown.
     * @throws IllegalArgumentException If alias was not registered by calling bundle.
     */
    public void unregisterAlias(String alias) {

        ReentrantLock lock = OSGiCleanMapper.getLock();
        lock.lock();
        try {
            if (mapper.isLocalyRegisteredAlias(alias)) {
                mapper.doUnregister(alias, true);
            } else {
                LOG.warn("Bundle: {} tried to unregister not owned alias '{}{}", bundle, alias, '\'');
                throw new IllegalArgumentException(new StringBuilder(64).append("Alias '").append(alias).append(
                    "' was not registered by you.").toString());
            }
        } finally {
            lock.unlock();
        }
    }

    public void unregisterFilter(final Filter filter) {
        ReentrantLock lock = OSGiCleanMapper.getLock();
        lock.lock();
        try {
            for (OSGiServletContext servletContext : mapper.httpContextToServletContextMap.values()) {
                servletContext.unregisterFilter(filter);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Unregisters all <code>alias</code>es registered by owning bundle.
     */
    public void uregisterAllLocal() {
        LOG.info("Unregistering all aliases registered by owning bundle");

        ReentrantLock lock = OSGiCleanMapper.getLock();
        lock.lock();
        try {
            for (String alias : mapper.getLocalAliases()) {
                LOG.debug("Unregistering '{}'", alias);
                // remember not to call Servlet.destroy() owning bundle might be stopped already.
                mapper.doUnregister(alias, false);
                for (OSGiServletContext servletContext : mapper.httpContextToServletContextMap.values()) {
                    servletContext.unregisterAllFilters();
                }
                mapper.httpContextToServletContextMap.clear();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Part of Shutdown sequence.
     * Unregister and clean up.
     */
    public void unregisterAll() {
        LOG.info("Unregistering all registered aliases");

        ReentrantLock lock = OSGiCleanMapper.getLock();
        lock.lock();
        try {
            Set<String> aliases = OSGiCleanMapper.getAllAliases();
            while (!aliases.isEmpty()) {
                String alias = ((TreeSet<String>) aliases).first();
                LOG.debug("Unregistering '{}'", alias);
                // remember not to call Servlet.destroy() owning bundle might be stopped already.
                mapper.doUnregister(alias, false);
            }
        } finally {
            lock.unlock();
        }
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

    /**
     * Chek if <code>alias</code> has been already registered.
     *
     * @param alias Alias to check.
     * @throws NamespaceException If <code>alias</code> has been registered.
     */
    private void validateAlias4RegOk(String alias) throws NamespaceException {
        if (null == alias) {
            String msg = "Alias must not be null";
            LOG.warn(msg);
            throw new NamespaceException(msg);
        }
        if (!alias.startsWith("/")) {
            // have to start with "/"
            String msg = new StringBuilder(64).append("Invalid alias '").append(alias).append("', have to start with '/'.").toString();
            LOG.warn(msg);
            throw new NamespaceException(msg);
        }
        if (alias.length() > 1 && alias.endsWith("/")) {
            // if longer than "/", should not end with "/"
            String msg = new StringBuilder(64).append("Alias '").append(alias).append("' can't end with '/' with exception to alias '/'.").toString();
            LOG.warn(msg);
            throw new NamespaceException(msg);
        }
        if (alias.length() > 1 && alias.endsWith("*")) {
            // if longer than "/", wildcards/mappings aren't supported
            String msg = new StringBuilder(64).append("Alias '").append(alias).append(
                "' can't end with '*'. Wildcards/mappings aren't supported.").toString();
            LOG.warn(msg);
            throw new NamespaceException(msg);
        }
        if (OSGiCleanMapper.containsAlias(alias)) {
            String msg = "Alias: '" + alias + "', already registered";
            LOG.warn(msg);
            throw new NamespaceException(msg);
        }
    }

    /**
     * Check if <code>servlet</code> has been already registered.
     * <p/>
     * An instance of {@link Servlet} can be registered only once, so in case of servlet been registered before will throw
     * {@link ServletException} as specified in OSGi HttpService Spec.
     *
     * @param servlet {@link Servlet} to check if can be registered.
     * @throws ServletException Iff <code>servlet</code> has been registered before.
     */
    private void validateServlet4RegOk(Servlet servlet) throws ServletException {
        if (OSGiCleanMapper.containsServlet(servlet)) {
            String msg = "Servlet: '" + servlet + "', already registered.";
            LOG.warn(msg);
            throw new ServletException(msg);
        }
    }

    /**
     * Looks up {@link OSGiServletHandler}.
     * <p/>
     * If is already registered for <code>httpContext</code> then create new instance based on already registered. Else
     * Create new one.
     * <p/>
     *
     * @param servlet     {@link Servlet} been registered.
     * @param httpContext {@link HttpContext} used for registration.
     * @param initparams  Init parameters that will be visible in {@link javax.servlet.ServletContext}.
     * @return Found or created {@link OSGiServletHandler}.
     */
    private OSGiServletHandler findOrCreateOSGiServletHandler(
            Servlet servlet, HttpContext httpContext, Dictionary initparams) {
        OSGiServletHandler osgiServletHandler;

        List<OSGiServletHandler> servletHandlers =
                mapper.getContext(httpContext);
        if (servletHandlers != null) {
            LOG.debug("Reusing ServletHandler");
            // new servlet handler for same configuration, different servlet and alias
            osgiServletHandler = servletHandlers.get(0).newServletHandler(servlet);
            servletHandlers.add(osgiServletHandler);
        } else {
            LOG.debug("Creating new ServletHandler");
            HashMap<String, String> params;
            if (initparams != null) {
                params = new HashMap<String, String>(initparams.size());
                Enumeration names = initparams.keys();
                while (names.hasMoreElements()) {
                    String name = (String) names.nextElement();
                    params.put(name, (String) initparams.get(name));
                }
            } else {
                params = new HashMap<String, String>(0);
            }

            servletHandlers = new ArrayList<OSGiServletHandler>(1);
            mapper.addContext(httpContext,
                    mapper.getServletContext(httpContext),
                    servletHandlers);

            final OSGiServletContext servletContext =
                    mapper.getServletContext(httpContext);

            assert servletContext != null;

            osgiServletHandler =
                    new OSGiServletHandler(servlet,
                                           httpContext,
                                           servletContext,
                                           params);
            servletHandlers.add(osgiServletHandler);
            osgiServletHandler.setFilterChainFactory(servletContext.getFilterChainFactory());
        }

        return osgiServletHandler;
    }

    private void updateMappingInfo(final Request request,
            final String alias, final String originalAlias) {

        final MappingData mappingData = request.obtainMappingData();
        mappingData.contextPath.setString("");
        if (alias.equals("/")) {
            mappingData.wrapperPath.setString("");
        } else {
            mappingData.wrapperPath.setString(alias);
        }

        if (alias.length() != originalAlias.length()) {
            String pathInfo = originalAlias.substring(alias.length());
            if (pathInfo.charAt(0) != '/') {
                pathInfo = "/" + pathInfo;
            }

            mappingData.pathInfo.setString(pathInfo);
        }

        updatePaths(request, mappingData);
    }

    @Override
    protected ErrorPageGenerator getErrorPageGenerator(Request request) {
        return errorPageGenerator;
    }

    private void writeErrorPage(Request req, Response res) throws Exception {
        res.setStatus(HttpStatus.NOT_FOUND_404);
        final ByteBuffer bb = getErrorPage(404, "Not found", "Resource does not exist.");
        res.setContentLength(bb.limit());
        res.setContentType("text/html");
        org.glassfish.grizzly.http.io.OutputBuffer out = res.getOutputBuffer();
        out.prepareCharacterEncoder();
        out.write(bb.array(), bb.arrayOffset() + bb.position(), bb.remaining());
        out.close();
    }

    private static final CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();

    /**
     * Generate an error page based on our servlet defaults.
     *
     * @return A {@link ByteBuffer} containing the HTTP response.
     */
    public synchronized static ByteBuffer getErrorPage(int code,
        String message, String description) throws IOException {
        String body = com.openexchange.tools.servlet.http.Tools.getErrorPage(
            code, message, description);
        CharBuffer reponseBuffer = CharBuffer.allocate(4096);
        reponseBuffer.clear();
        reponseBuffer.put(body);
        reponseBuffer.flip();
        return encoder.encode(reponseBuffer);

    }

    // -----------------------------------------------------------------------------------------

    private static final class ErrorPageGeneratorImpl implements ErrorPageGenerator {


        /**
         * Initializes a new {@link OSGiMainHandler.ErrorPageGeneratorImpl}.
         */
        ErrorPageGeneratorImpl() {
            super();
        }

        @Override
        public String generate(Request request, int status, String reasonPhrase, String description, Throwable exception) {
            return com.openexchange.tools.servlet.http.Tools.getErrorPage(status, reasonPhrase, description);
        }
    }

}
