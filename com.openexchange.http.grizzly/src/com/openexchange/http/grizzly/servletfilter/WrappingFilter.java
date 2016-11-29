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

package com.openexchange.http.grizzly.servletfilter;

import static com.openexchange.http.grizzly.http.servlet.HttpServletRequestWrapper.HTTPS_SCHEME;
import static com.openexchange.http.grizzly.http.servlet.HttpServletRequestWrapper.HTTP_SCHEME;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import com.openexchange.http.grizzly.GrizzlyConfig;
import com.openexchange.http.grizzly.http.servlet.HttpServletRequestWrapper;
import com.openexchange.http.grizzly.http.servlet.HttpServletResponseWrapper;
import com.openexchange.http.grizzly.util.IPTools;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogProperties;

/**
 * {@link WrappingFilter} - Wrap the Request in {@link HttpServletResponseWrapper} and the Response in {@link HttpServletResponseWrapper}
 * and creates a new HttpSession.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class WrappingFilter implements Filter {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(WrappingFilter.class);

    private static final Pattern PATTERN_CRLF = Pattern.compile("\r?\n|\r|(?:%0[aA])?%0[dD]|%0[aA]");

    private static String dropLinebreaks(String s) {
        // Strip possible "\r?\n" and/or "%0A?%0D"
        return PATTERN_CRLF.matcher(s).replaceAll("");
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private final String forHeader;
    private final List<String> knownProxies;
    private final String protocolHeader;
    private final boolean isConsiderXForwards;
    private final String echoHeaderName;
    private final boolean considerEchoHeader;
    private final String contentSecurityPolicy;

    /**
     * Initializes a new {@link WrappingFilter}.
     */
    public WrappingFilter(GrizzlyConfig config) {
        super();
        this.forHeader = config.getForHeader();
        this.knownProxies = config.getKnownProxies();
        this.protocolHeader = config.getProtocolHeader();
        this.isConsiderXForwards = config.isConsiderXForwards();
        this.echoHeaderName = config.getEchoHeader();
        this.considerEchoHeader = !Strings.isEmpty(echoHeaderName);
        this.contentSecurityPolicy = config.getContentSecurityPolicy();
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Inspect echoHeader and when present copy it to Response
        String echoHeaderValue = considerEchoHeader ? httpRequest.getHeader(echoHeaderName) : null;
        if (echoHeaderValue != null) {
            echoHeaderValue = dropLinebreaks(echoHeaderValue);
            httpResponse.setHeader(echoHeaderName, echoHeaderValue);
        }

        // Set Content-Security-Policy header
        {
            String contentSecurityPolicy = this.contentSecurityPolicy;
            if (!Strings.isEmpty(contentSecurityPolicy)) {
                httpResponse.setHeader("Content-Security-Policy", contentSecurityPolicy);
                httpResponse.setHeader("X-WebKit-CSP", contentSecurityPolicy);
                httpResponse.setHeader("X-Content-Security-Policy", contentSecurityPolicy);
            }
        }

        // Inspect X-Forwarded headers and create HttpServletRequestWrapper accordingly
        HttpServletRequestWrapper httpRequestWrapper = buildHttpServletRequestWrapper(httpRequest);

        // Build the HttpServletResponseWrapper instance
        HttpServletResponseWrapper httpResponseWrapper = new HttpServletResponseWrapper(httpResponse, echoHeaderName, echoHeaderValue);

        // Set LogProperties
        {

            // Servlet related properties
            LogProperties.put(LogProperties.Name.GRIZZLY_REQUEST_URI, httpRequest.getRequestURI());
            LogProperties.put(LogProperties.Name.GRIZZLY_SERVLET_PATH, httpRequest.getServletPath());
            LogProperties.put(LogProperties.Name.GRIZZLY_PATH_INFO, httpRequest.getPathInfo());
            {
                String queryString = httpRequest.getQueryString();
                LogProperties.put(LogProperties.Name.GRIZZLY_QUERY_STRING, null == queryString ? "<none>" : LogProperties.getSanitizedQueryString(queryString));
            }
            LogProperties.put(LogProperties.Name.GRIZZLY_METHOD, httpRequest.getMethod());

            // Remote infos
            LogProperties.put(LogProperties.Name.GRIZZLY_REMOTE_PORT, Integer.toString(httpRequestWrapper.getRemotePort()));
            LogProperties.put(LogProperties.Name.GRIZZLY_REMOTE_ADDRESS, httpRequestWrapper.getRemoteAddr());

            // Names, addresses
            final Thread currentThread = Thread.currentThread();
            LogProperties.put(LogProperties.Name.GRIZZLY_THREAD_NAME, currentThread.getName());
            LogProperties.put(LogProperties.Name.THREAD_ID, Long.toString(currentThread.getId()));
            LogProperties.put(LogProperties.Name.GRIZZLY_SERVER_NAME, httpRequest.getServerName());
            {
                String userAgent = httpRequest.getHeader("User-Agent");
                LogProperties.put(LogProperties.Name.GRIZZLY_USER_AGENT, null == userAgent ? "<unknown>" : userAgent);
            }

            // Tracking identifier
            String trackingId = request.getParameter("trackingId");
            if (trackingId == null) {
                trackingId = UUIDs.getUnformattedString(UUID.randomUUID());
            }
            LogProperties.putProperty(LogProperties.Name.REQUEST_TRACKING_ID, trackingId);
        }

        chain.doFilter(httpRequestWrapper, httpResponseWrapper);
    }

    private HttpServletRequestWrapper buildHttpServletRequestWrapper(HttpServletRequest httpRequest) {
        if (!isConsiderXForwards) {
            return new HttpServletRequestWrapper(httpRequest);
        }

        String forHeaderValue = httpRequest.getHeader(forHeader);
        String remoteIP = IPTools.getRemoteIP(forHeaderValue, knownProxies);
        String protocol = httpRequest.getHeader(protocolHeader);

        if (!isValidProtocol(protocol)) {
            LOG.debug("Could not detect a valid protocol header value in {}, falling back to default", protocol);
            protocol = httpRequest.getScheme();
        }

        if (remoteIP.isEmpty()) {
            LOG.debug("Could not detect a valid remote IP address in {}: [{}], falling back to default", forHeader, forHeaderValue == null ? "" : forHeaderValue);
            remoteIP = httpRequest.getRemoteAddr();
        }

        return new HttpServletRequestWrapper(protocol, remoteIP, httpRequest.getServerPort(), httpRequest);
    }

    private boolean isValidProtocol(String protocolHeaderValue) {
        return HTTP_SCHEME.equals(protocolHeaderValue) || HTTPS_SCHEME.equals(protocolHeaderValue);
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }

}
