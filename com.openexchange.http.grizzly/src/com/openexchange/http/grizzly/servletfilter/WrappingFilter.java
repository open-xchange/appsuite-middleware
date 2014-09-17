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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.http.grizzly.GrizzlyConfig;
import com.openexchange.http.grizzly.http.servlet.HttpServletRequestWrapper;
import com.openexchange.http.grizzly.http.servlet.HttpServletResponseWrapper;
import com.openexchange.http.grizzly.osgi.Services;
import com.openexchange.http.grizzly.util.IPTools;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogProperties;

/**
 * {@link WrappingFilter} - Wrap the Request in {@link HttpServletResponseWrapper} and the Response in {@link HttpServletResponseWrapper}
 * and creates a new HttpSession and do various tasks to achieve feature parity with the ajp based implementation.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class WrappingFilter implements Filter {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(WrappingFilter.class);

    IPTools remoteIPFinder;
    private String forHeader;
    private List<String> knownProxies;
    private String protocolHeader;
    private String httpsProtoValue;
    private int httpPort;
    private int httpsPort;
    private boolean isConsiderXForwards = false;
    private String echoHeader;
    private String contentSecurityPolicy = null;
    private String dispatcherPrefix = DispatcherPrefixService.DEFAULT_PREFIX;

    @Override
    public void init(FilterConfig filterConfig) {
        final GrizzlyConfig config = GrizzlyConfig.getInstance();
        this.forHeader = config.getForHeader();
        this.knownProxies = config.getKnownProxies();
        this.protocolHeader = config.getProtocolHeader();
        this.httpsProtoValue = config.getHttpsProtoValue();
        this.httpPort = config.getHttpProtoPort();
        this.httpsPort = config.getHttpsProtoPort();
        this.isConsiderXForwards = config.isConsiderXForwards();
        this.echoHeader = config.getEchoHeader();
        this.contentSecurityPolicy = config.getContentSecurityPolicy();

        // register listener for changed known proxies configuration
        config.addPropertyListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                knownProxies = config.getKnownProxies();
            }
        });

        String prefix = DispatcherPrefixService.DEFAULT_PREFIX;
        DispatcherPrefixService service = Services.optService(DispatcherPrefixService.class);
        if (null != service) {
            prefix = service.getPrefix();
        }
        this.dispatcherPrefix = prefix;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpServletRequestWrapper httpServletRequestWrapper = null;
        HttpServletResponseWrapper httpServletResponseWrapper = null;

        // Inspect echoHeader and when present copy it to Response
        String echoHeaderValue = httpServletRequest.getHeader(echoHeader);
        if (echoHeaderValue != null) {
            httpServletResponse.setHeader(echoHeader, echoHeaderValue);
        }

        // Set Content-Security-Policy header
        {
            final String contentSecurityPolicy = this.contentSecurityPolicy;
            if (!Strings.isEmpty(contentSecurityPolicy)) {
                httpServletResponse.setHeader("Content-Security-Policy", contentSecurityPolicy);
                httpServletResponse.setHeader("X-WebKit-CSP", contentSecurityPolicy);
                httpServletResponse.setHeader("X-Content-Security-Policy", contentSecurityPolicy);
            }
        }

        // Inspect X-Forwarded headers and create HttpServletRequestWrapper accordingly
        if (isConsiderXForwards) {
            String forHeaderValue = httpServletRequest.getHeader(forHeader);
            String remoteIP = IPTools.getRemoteIP(forHeaderValue, knownProxies);
            String protocol = httpServletRequest.getHeader(protocolHeader);

            if(!isValidProtocol(protocol)) {
                LOG.debug("Could not detect a valid protocol header value in {}, falling back to default", protocol);
                 protocol = httpServletRequest.getScheme();
            }

            if (remoteIP.isEmpty()) {
                LOG.debug("Could not detect a valid remote ip in {}: [{}], falling back to default", forHeader, forHeaderValue == null ? "" : forHeaderValue);
                remoteIP = httpServletRequest.getRemoteAddr();
            }

            httpServletRequestWrapper = new HttpServletRequestWrapper(protocol, remoteIP, httpServletRequest.getServerPort(), httpServletRequest);

        } else {
            httpServletRequestWrapper = new HttpServletRequestWrapper(httpServletRequest);
        }
        httpServletResponseWrapper = new HttpServletResponseWrapper(httpServletResponse);

        // Set LogProperties
        {

            // Servlet related properties
            LogProperties.put(LogProperties.Name.GRIZZLY_REQUEST_URI, httpServletRequest.getRequestURI());
            LogProperties.put(LogProperties.Name.GRIZZLY_SERVLET_PATH, httpServletRequest.getServletPath());
            LogProperties.put(LogProperties.Name.GRIZZLY_PATH_INFO, httpServletRequest.getPathInfo());

            // Remote infos
            LogProperties.put(LogProperties.Name.GRIZZLY_REMOTE_PORT, Integer.toString(httpServletRequestWrapper.getRemotePort()));
            LogProperties.put(LogProperties.Name.GRIZZLY_REMOTE_ADDRESS, httpServletRequestWrapper.getRemoteAddr());
            LogProperties.put(LogProperties.Name.GRIZZLY_REQUEST_IP, httpServletRequestWrapper.getRemoteAddr());

            // Names, addresses
            final Thread currentThread = Thread.currentThread();
            LogProperties.put(LogProperties.Name.GRIZZLY_THREAD_NAME, currentThread.getName());
            LogProperties.put(LogProperties.Name.THREAD_ID, Long.toString(currentThread.getId()));
            LogProperties.put(LogProperties.Name.GRIZZLY_SERVER_NAME, httpServletRequest.getServerName());
            final String userAgent = httpServletRequest.getHeader("User-Agent");
            LogProperties.put(LogProperties.Name.GRIZZLY_USER_AGENT, null == userAgent ? "<unknown>" : userAgent);

            // AJAX module/action
            {
                String action = request.getParameter("action");
                if (null != action) {
                    LogProperties.put(LogProperties.Name.AJAX_ACTION, action);
                }

                String requestURI = httpServletRequest.getRequestURI();
                if (requestURI.startsWith(dispatcherPrefix) && requestURI.length() > dispatcherPrefix.length()) {
                    String pathInfo = requestURI;
                    int lastIndex = pathInfo.lastIndexOf(';');
                    if (lastIndex > 0) {
                        pathInfo = pathInfo.substring(0, lastIndex);
                    }
                    String module = pathInfo.substring(dispatcherPrefix.length());
                    int mlen = module.length() - 1;
                    if (mlen > 0 && '/' == module.charAt(mlen)) {
                        module = module.substring(0, mlen);
                    }
                    LogProperties.put(LogProperties.Name.AJAX_MODULE, module);
                }
            }

            // Tracking identifier
            String trackingId = request.getParameter("trackingId");
            if (trackingId == null) {
                trackingId = UUIDs.getUnformattedString(UUID.randomUUID());
            }
            LogProperties.putProperty(LogProperties.Name.REQUEST_TRACKING_ID, trackingId);
        }

        chain.doFilter(httpServletRequestWrapper, httpServletResponseWrapper);
    }

    private boolean isValidProtocol(String protocolHeaderValue) {
        return HTTP_SCHEME.equals(protocolHeaderValue) || HTTPS_SCHEME.equals(protocolHeaderValue);
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }

}
