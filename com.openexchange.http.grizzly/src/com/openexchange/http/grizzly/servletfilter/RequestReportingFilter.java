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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.http.grizzly.servletfilter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.http.grizzly.GrizzlyExceptionCode;
import com.openexchange.http.grizzly.osgi.Services;
import com.openexchange.http.requestwatcher.osgi.services.RequestRegistryEntry;
import com.openexchange.http.requestwatcher.osgi.services.RequestWatcherService;
import com.openexchange.java.StringAllocator;

/**
 * {@link RequestReportingFilter} - Add incoming requests to the RequestWatcherService so we can track and interrupt long running requests.
 * EAS Requests aren't tracked as they are long running requests simulating server side message pushing.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RequestReportingFilter implements Filter {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(RequestReportingFilter.class);

    private static final boolean DEBUG = LOG.isDebugEnabled();

    // properties of long running eas requests
    private static final String EAS_URI = "/Microsoft-Server-ActiveSync";

    private static final String EAS_CMD = "Cmd";

    private static final String EAS_PING = "Ping";

    private static final String DRIVE_URI = "/ajax/drive";

    private final boolean isFilterEnabled;

    /**
     * Initializes a new {@link RequestReportingFilter}.
     *
     * @throws OXException If initialization fails
     */
    public RequestReportingFilter() throws OXException {
        final ConfigurationService configService = Services.optService(ConfigurationService.class);
        if (configService == null) {
            throw GrizzlyExceptionCode.NEEDED_SERVICE_MISSING.create(ConfigurationService.class.getSimpleName());
        }
        this.isFilterEnabled = configService.getBoolProperty("com.openexchange.server.requestwatcher.isEnabled", true);
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // nothing to do here
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (isFilterEnabled) {
            final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            if (isLongRunning(httpServletRequest)) { // don't track long running requests
                chain.doFilter(request, response);
            } else {
                final RequestWatcherService requestWatcherService = Services.optService(RequestWatcherService.class);
                // Request watcher is enabled but service is missing, bundle not started etc ..
                if (requestWatcherService == null) {
                    LOG.warn("RequestWatcherService is not available. Unable to watch this request.");
                    chain.doFilter(httpServletRequest, httpServletResponse);
                } else {
                    final RequestRegistryEntry requestRegistryEntry = requestWatcherService.registerRequest(httpServletRequest, httpServletResponse, Thread.currentThread());
                    try {
                        // proceed processing
                        chain.doFilter(request, response);

                        // debug duration
                        if (DEBUG) {
                            LOG.debug(new StringAllocator("Request took ").append(requestRegistryEntry.getAge()).append("ms ").append(" for URL: ").append(
                                httpServletRequest.getRequestURL()).toString());
                        }
                    } finally {
                        // remove request from registry after processing finished
                        requestWatcherService.unregisterRequest(requestRegistryEntry);
                    }
                }
            }
        } else { // filter isn't enabled
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // nothing to do here
    }

    private boolean isLongRunning(final HttpServletRequest httpServletRequest) {
        return  isDriveRequest(httpServletRequest) || isEASRequest(httpServletRequest);
    }

    private final boolean isDriveRequest(final HttpServletRequest httpServletRequest) {
        final boolean isDriveRequest = DRIVE_URI.equals(httpServletRequest.getRequestURI());
        return isDriveRequest;
    }

    private final boolean isEASRequest(final HttpServletRequest httpServletRequest) {
        final boolean isEASRequest = EAS_URI.equals(httpServletRequest.getRequestURI()) && EAS_PING.equals(httpServletRequest.getParameter(EAS_CMD));
        return isEASRequest;
    }

}
