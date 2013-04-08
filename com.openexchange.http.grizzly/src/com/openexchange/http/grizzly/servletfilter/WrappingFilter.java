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
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.http.grizzly.GrizzlyConfig;
import com.openexchange.http.grizzly.http.servlet.HttpServletRequestWrapper;
import com.openexchange.http.grizzly.http.servlet.HttpServletResponseWrapper;
import com.openexchange.http.grizzly.util.IPTools;
import com.openexchange.log.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Props;
import static com.openexchange.http.grizzly.http.servlet.HttpServletRequestWrapper.HTTP_SCHEME;
import static com.openexchange.http.grizzly.http.servlet.HttpServletRequestWrapper.HTTPS_SCHEME;

/**
 * {@link WrappingFilter} - Wrap the Request in {@link HttpServletResponseWrapper} and the Response in {@link HttpServletResponseWrapper}
 * and creates a new HttpSession and do various tasks to achieve feature parity with the ajp based implementation.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class WrappingFilter implements Filter {

    private static final org.apache.commons.logging.Log LOG = Log.valueOf(LogFactory.getLog(WrappingFilter.class));

    IPTools remoteIPFinder;

    private String forHeader;

    private List<String> knownProxies;

    private String protocolHeader;

    private String httpsProtoValue;

    private int httpPort;

    private int httpsPort;

    private boolean isConsiderXForwards = false;

    private String echoHeader;

    @Override
    public void init(FilterConfig filterConfig) {
        GrizzlyConfig config = GrizzlyConfig.getInstance();
        this.forHeader = config.getForHeader();
        this.knownProxies = config.getKnownProxies();
        this.protocolHeader = config.getProtocolHeader();
        this.httpsProtoValue = config.getHttpsProtoValue();
        this.httpPort = config.getHttpProtoPort();
        this.httpsPort = config.getHttpsProtoPort();
        this.isConsiderXForwards = config.isConsiderXForwards();
        this.echoHeader = config.getEchoHeader();
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

        // Inspect X-Forwarded headers and create HttpServletRequestWrapper accordingly
        if (isConsiderXForwards) {
            String forHeaderValue = httpServletRequest.getHeader(forHeader);
            String remoteIP = IPTools.getRemoteIP(forHeaderValue, knownProxies);
            String protocol = httpServletRequest.getHeader(protocolHeader);

            if(!isValidProtocol(protocol)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Could not detect a valid protocol header value in " + protocol + ", falling back to default");
                }
                 protocol = httpServletRequest.getScheme();
            }
            
            if (remoteIP.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    forHeaderValue = forHeaderValue == null ? "" : forHeaderValue;
                    LOG.debug("Could not detect a valid remote ip in " + forHeader + ": [" + forHeaderValue + "], falling back to default");
                }
                remoteIP = httpServletRequest.getRemoteAddr();
            }
            
            httpServletRequestWrapper = new HttpServletRequestWrapper(protocol, remoteIP, httpServletRequest.getServerPort(), httpServletRequest);
            
        } else {
            httpServletRequestWrapper = new HttpServletRequestWrapper(httpServletRequest);
        }
        httpServletResponseWrapper = new HttpServletResponseWrapper(httpServletResponse);

        // Create a Session if needed
        httpServletRequest.getSession(true);

        // Set LogProperties
        if (LogProperties.isEnabled()) {
            Props logProperties = LogProperties.getLogProperties();

            // Servlet related properties
            logProperties.put(LogProperties.Name.GRIZZLY_REQUEST_URI, httpServletRequest.getRequestURI());
            logProperties.put(LogProperties.Name.GRIZZLY_SERVLET_PATH, httpServletRequest.getServletPath());
            logProperties.put(LogProperties.Name.GRIZZLY_PATH_INFO, httpServletRequest.getPathInfo());

            // Remote infos
            logProperties.put(LogProperties.Name.GRIZZLY_REMOTE_PORT, httpServletRequestWrapper.getRemotePort());
            logProperties.put(LogProperties.Name.GRIZZLY_REQUEST_IP, httpServletRequestWrapper.getRemoteAddr());

            // Names, addresses
            logProperties.put(LogProperties.Name.GRIZZLY_THREAD_NAME, Thread.currentThread().getName());
            logProperties.put(LogProperties.Name.GRIZZLY_SERVER_NAME, httpServletRequest.getServerName());
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
