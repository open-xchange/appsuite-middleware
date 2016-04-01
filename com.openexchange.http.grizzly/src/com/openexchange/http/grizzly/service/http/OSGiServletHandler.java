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
 * Copyright (c) 2009-2011 Oracle and/or its affiliates. All rights reserved.
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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.localization.LogMessages;
import org.glassfish.grizzly.servlet.FilterChainInvoker;
import org.glassfish.grizzly.servlet.FilterConfigImpl;
import org.glassfish.grizzly.servlet.ServletConfigImpl;
import org.glassfish.grizzly.servlet.ServletHandler;
import org.glassfish.grizzly.servlet.WebappContext;
import org.osgi.service.http.HttpContext;
import com.openexchange.java.Charsets;
import com.openexchange.log.LogProperties;
import com.openexchange.tools.exceptions.ExceptionUtils;
import com.openexchange.tools.servlet.UploadServletException;

/**
 * OSGi customized {@link ServletHandler}.
 *
 * @author Hubert Iwaniuk
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class OSGiServletHandler extends ServletHandler implements OSGiHandler {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OSGiServletHandler.class);

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private HttpContext httpContext;

    private String servletPath;

    private volatile CopyOnWriteArrayList<Filter> servletFilters;

    private FilterChainFactory filterChainFactory;

    /**
     * Initializes a new {@link OSGiServletHandler}.
     *
     * @param servlet
     * @param httpContext
     * @param servletInitParams
     */
    public OSGiServletHandler(final Servlet servlet, final HttpContext httpContext, final HashMap<String, String> servletInitParams) {
        super(createServletConfig(new OSGiServletContext(httpContext), servletInitParams));
        // noinspection AccessingNonPublicFieldOfAnotherObject
        super.servletInstance = servlet;
        this.httpContext = httpContext;
        servletFilters = new CopyOnWriteArrayList<Filter>();
        this.filterChainFactory = new FilterChainFactory(servlet, (OSGiServletContext) getServletCtx());
    }

    private OSGiServletHandler(final ServletConfigImpl servletConfig) {
        super(servletConfig);

    }

    public OSGiServletHandler newServletHandler(Servlet servlet) {
        OSGiServletHandler servletHandler = new OSGiServletHandler(getServletConfig());

        servletHandler.setServletInstance(servlet);
        servletHandler.setServletPath(getServletPath());
        // noinspection AccessingNonPublicFieldOfAnotherObject
        servletHandler.httpContext = httpContext;
        return servletHandler;
    }

    /**
     * Starts {@link Servlet} instance of this {@link OSGiServletHandler}.
     *
     * @throws ServletException If {@link Servlet} startup failed.
     */
    public void startServlet() throws ServletException {
        configureServletEnv();
        // setResourcesContextPath(getContextPath() + getServletPath());
        // always load servlet
        if (null == servletInstance) {
            loadServlet();
        } else {
            servletInstance.init(getServletConfig());
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

    protected void setServletPath(final String path) {
        this.servletPath = path;
    }

    protected String getServletPath() {
        return servletPath;
    }

    public HttpContext getHttpContext() {
        return httpContext;
    }

    // ------------------------------------------------------- Protected Methods

    @Override
    protected FilterChainInvoker getFilterChain(Request request) {
        return filterChainFactory.createFilterChain(servletFilters);
    }

    /**
     * Append a filter to the chain of Filters this ServletHandler manages.
     *
     * @param filter Instance of the Filter to add
     * @param name Name of this Filter to add
     * @param initParams Initparameters needed to create a FilterConfig
     */
    protected void addFilter(final Filter filter, final String name, final Map<String, String> initParams) {
        try {
            filter.init(createFilterConfig(getServletCtx(), name, initParams));
            servletFilters.add(filter);
        } catch (Exception e) {
            LOG.error(e.toString(), e);
        }
    }

    /**
     * Updates the chain of Filters this ServletHandler manages.
     *
     * @param filtersToRemove The filters to remove
     * @param filtersToRemove The filters to add
     */
    protected void updateFilters(final Collection<Filter> filtersToRemove, final Collection<Filter> filtersToAdd) {
        try {
            List<Filter> list = new LinkedList<Filter>(servletFilters);
            list.removeAll(filtersToRemove);

            for (Filter filter : filtersToAdd) {
                filter.init(createFilterConfig(getServletCtx(), filter.getClass().getName(), null));
                list.add(filter);
            }

            servletFilters = new CopyOnWriteArrayList<Filter>(list);
        } catch (Exception e) {
            LOG.error(e.toString(), e);
        }
    }

    /**
     * Removes a filter from the chain of Filters this ServletHandler manages.
     *
     * @param filter Instance of the Filter to remove
     */
    protected void removeFilter(final Filter filter) {
        try {
            servletFilters.remove(filter);
        } catch (Exception e) {
            LOG.error(e.toString(), e);
        }
    }

    // @Override
    // protected void setPathData(Request from, HttpServletRequestImpl to) {
    // to.setServletPath(getServletPath());
    // }

    // --------------------------------------------------------- Private Methods

    private static FilterConfig createFilterConfig(final WebappContext ctx, final String name, final Map<String, String> initParams) {
        final OSGiFilterConfig config = new OSGiFilterConfig(ctx);
        config.setFilterName(name);
        config.setInitParameters(initParams);
        return config;

    }

    private static ServletConfigImpl createServletConfig(final OSGiServletContext ctx, final Map<String, String> params) {

        final OSGiServletConfig config = new OSGiServletConfig(ctx);
        config.setInitParameters(params);
        return config;
    }

    // ---------------------------------------------------------- Nested Classes

    private static final class OSGiFilterConfig extends FilterConfigImpl {

        public OSGiFilterConfig(WebappContext servletContext) {
            super(servletContext);
        }

        @Override
        protected void setFilterName(String filterName) {
            super.setFilterName(filterName);
        }

        @Override
        protected void setInitParameters(Map initParameters) {
            super.setInitParameters(initParameters);
        }
    }

    private static final class OSGiServletConfig extends ServletConfigImpl {

        protected OSGiServletConfig(WebappContext servletContextImpl) {
            super(servletContextImpl);
        }

        @Override
        protected void setInitParameters(Map<String, String> parameters) {
            super.setInitParameters(parameters);
        }
    }

    private static final class FilterChainFactory {

        private final Servlet servlet;

        private final OSGiServletContext servletContext;

        /**
         * Initializes a new {@link FilterChainFactory}.
         *
         * @param servlet
         * @param servletContext
         */
        public FilterChainFactory(Servlet servlet, OSGiServletContext servletContext) {
            super();
            this.servlet = servlet;
            this.servletContext = servletContext;
        }

        /**
         * Construct and return a FilterChain implementation that will wrap the execution of the specified servlet instance. If we should
         * not execute a filter chain at all, return <code>null</code>.
         *
         * @param request The servlet request we are processing
         * @param servlet The servlet instance to be wrapped
         */
        public FilterChainImpl createFilterChain(List<Filter> servletFilters) {

            if (servletFilters.isEmpty()) {
                return null;
            }
            FilterChainImpl filterChain = new FilterChainImpl(servlet, servletContext);
            for (Filter servletFilter : servletFilters) {
                filterChain.addFilter(servletFilter);
            }
            return filterChain;
        }
    }

    private static final class FilterChainImpl implements FilterChain, FilterChainInvoker {

        private static final java.util.logging.Logger LOGGER = Grizzly.logger(FilterChainImpl.class);

        /**
         * The servlet instance to be executed by this chain.
         */
        private final Servlet servlet;

        private final OSGiServletContext ctx;

        private final Object lock = new Object();

        private int n;

        private Filter[] filters = new Filter[0];

        /**
         * The int which is used to maintain the current position in the filter chain.
         */
        private int pos;

        public FilterChainImpl(final Servlet servlet, final OSGiServletContext ctx) {

            this.servlet = servlet;
            this.ctx = ctx;
        }

        // ---------------------------------------------------- FilterChain Methods

        @Override
        public void invokeFilterChain(final ServletRequest request, final ServletResponse response) throws IOException, ServletException {

            ServletRequestEvent event = new ServletRequestEvent(ctx, request);
            try {
                requestInitialized(event);
                pos = 0;
                doFilter(request, response);
            } finally {
                requestDestroyed(event);
            }

        }

        /**
         * Invoke the next filter in this chain, passing the specified request and response. If there are no more filters in this chain,
         * invoke the <code>service()</code> method of the servlet itself.
         *
         * @param request The servlet request we are processing
         * @param response The servlet response we are creating
         * @throws java.io.IOException if an input/output error occurs
         * @throws javax.servlet.ServletException if a servlet exception occurs
         */
        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            if (pos < n) {
                Filter filter = filters[pos++];
                if (filter != null) {
                    try {
                        filter.doFilter(request, response, this);
                    } catch (Throwable throwable) {
                        handleThrowable(throwable, request, response);
                    }
                    return;
                }
            }

            try {
                if (servlet != null) {
                    servlet.service(request, response);
                }
            } catch (final UploadServletException e) {
                /*
                 * Log ServletException's own root cause separately
                 */
                final Throwable rootCause = e.getRootCause();
                if (null != rootCause) {
                    LOG.error("", rootCause);
                }
                /*
                 * Now log actual UploadServletException...
                 */
                LOG.error("", e);
                /*
                 * ... and write bytes
                 */
                if (response instanceof HttpServletResponse) {
                    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                    try {
                        handleUploadServletException(e, httpServletResponse);
                    } catch (final Exception ie) {
                        handleThrowable(e, request, response);
                    }
                }
            } catch (IOException ioe) {
                if (response instanceof HttpServletResponse) {
                    // 500 - Internal Server Error
                    ((HttpServletResponse) response).setStatus(500);
                }
            } catch (Throwable throwable) {
                handleThrowable(throwable, request, response);
            }

        }

        private void handleUploadServletException(final UploadServletException e, final HttpServletResponse httpServletResponse) throws IOException {
            try {
                final PrintWriter writer = httpServletResponse.getWriter();
                writer.write(e.getData());
                writer.flush();
            } catch (final IllegalStateException ise) {
                // OutputStream already selected
                ServletOutputStream out = httpServletResponse.getOutputStream();
                out.write(e.getData().getBytes(Charsets.UTF_8));
                out.flush();
            }
        }

        /**
         * Let the ExceptionUtils handle the Throwable for us and set a proper HttpStatus on the response.
         *
         * @param throwable The Throwable that needs to be handled.
         * @param request The request that couldn't be serviced because of throwable
         * @param response The associated Response
         */
        private void handleThrowable(Throwable throwable, ServletRequest request, ServletResponse response) {
            com.openexchange.exception.ExceptionUtils.handleThrowable(throwable);

            StringBuilder logBuilder = new StringBuilder(128).append("Error processing request:").append(System.getProperty("line.separator"));
            logBuilder.append(LogProperties.getAndPrettyPrint(LogProperties.Name.SESSION_SESSION));

            if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                appendHttpServletRequestInfo(logBuilder, (HttpServletRequest) request);
                // 500 - Internal Server Error
                ((HttpServletResponse) response).setStatus(500);
            } else {
                appendServletRequestInfo(logBuilder, request);
            }
            LOG.error(logBuilder.toString(), throwable);
        }

        // ------------------------------------------------------- Protected Methods

        protected void addFilter(final Filter filter) {
            if (filter == null) {
                throw new IllegalArgumentException("Obligatory parameter is null: filter");
            }
            synchronized (lock) {
                // Don't ad Filters twice
                boolean isAlreadyAdded = false;
                for (Filter currentFilter : filters) {
                    if (currentFilter != null && currentFilter.getClass().equals(filter.getClass())) {
                        isAlreadyAdded = true;
                        LOG.error("Tried to add Filter {} multiple times.", filter);
                        break;
                    }
                }
                if (!isAlreadyAdded) {
                    if (n == filters.length) {
                        Filter[] newFilters = new Filter[n + 4];
                        System.arraycopy(filters, 0, newFilters, 0, n);
                        filters = newFilters;
                    }

                    filters[n++] = filter;
                }
            }
        }

        // --------------------------------------------------------- Private Methods
        /**
         * Add ServletName and Parameters of the request to the log string allocator.
         *
         * @param logBuilder The existing StringBuilder user for building the log message
         * @param request The Request that couldn't be executed successfully.
         */
        private void appendServletRequestInfo(StringBuilder logBuilder, ServletRequest request) {
            logBuilder.append("servlet name=''");
            logBuilder.append(servlet.getServletConfig().getServletName());
            logBuilder.append("servlet parameters=''");
            @SuppressWarnings("unchecked") Enumeration<String> parameterNames = request.getParameterNames();
            boolean firstParam = true;
            while (parameterNames.hasMoreElements()) {
                if (firstParam) {
                    String name = parameterNames.nextElement();
                    String value = request.getParameter(name);
                    logBuilder.append(name);
                    logBuilder.append("=");
                    logBuilder.append(value);
                    firstParam = false;
                } else {
                    logBuilder.append("&");
                    String name = parameterNames.nextElement();
                    String value = request.getParameter(name);
                    logBuilder.append(name);
                    logBuilder.append("=");
                    logBuilder.append(value);
                }
            }
        }

        /**
         * Add Uri and QueryString of the httpServletRequest to the log string allocator
         *
         * @param logBuilder The existing StringBuilder user for building the log message
         * @param httpServletRequest The HttpServletRequest that couldn't be executed successfully
         */
        private void appendHttpServletRequestInfo(StringBuilder logBuilder, HttpServletRequest httpServletRequest) {
            logBuilder.append("request-URI=''");
            logBuilder.append(httpServletRequest.getRequestURI());
            logBuilder.append("'', query-string=''");
            logBuilder.append(httpServletRequest.getQueryString());
            logBuilder.append("''");
        }

        private void requestDestroyed(ServletRequestEvent event) {
            // TODO don't create the event unless necessary
            final EventListener[] listeners = ctx.getEventListeners();
            for (int i = 0, len = listeners.length; i < len; i++) {
                if (listeners[i] instanceof ServletRequestListener) {
                    try {
                        ((ServletRequestListener) listeners[i]).requestDestroyed(event);
                    } catch (Throwable t) {
                        ExceptionUtils.handleThrowable(t);
                        if (LOGGER.isLoggable(Level.WARNING)) {
                            LOGGER.log(Level.WARNING, LogMessages.WARNING_GRIZZLY_HTTP_SERVLET_CONTAINER_OBJECT_DESTROYED_ERROR(
                                "requestDestroyed",
                                "ServletRequestListener",
                                listeners[i].getClass().getName()), t);
                        }
                    }
                }
            }

        }

        private void requestInitialized(ServletRequestEvent event) {
            final EventListener[] listeners = ctx.getEventListeners();
            for (int i = 0, len = listeners.length; i < len; i++) {
                if (listeners[i] instanceof ServletRequestListener) {
                    try {
                        ((ServletRequestListener) listeners[i]).requestDestroyed(event);
                    } catch (Throwable t) {
                        ExceptionUtils.handleThrowable(t);
                        if (LOGGER.isLoggable(Level.WARNING)) {
                            LOGGER.log(Level.WARNING, LogMessages.WARNING_GRIZZLY_HTTP_SERVLET_CONTAINER_OBJECT_INITIALIZED_ERROR(
                                "requestDestroyed",
                                "ServletRequestListener",
                                listeners[i].getClass().getName()), t);
                        }
                    }
                }
            }
        }

    } // END FilterChainImpl

}
