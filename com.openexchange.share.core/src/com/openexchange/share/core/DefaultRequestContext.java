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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.share.core;

import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.osgi.util.ServiceCallWrapper;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceException;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceUser;
import com.openexchange.share.RequestContext;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link DefaultRequestContext} - Default implementation of {@link RequestContext} based
 * on plain fields.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class DefaultRequestContext implements RequestContext {

    private final String protocol;

    private final String hostname;

    private final String servletPrefix;

    /**
     * Initializes a new {@link DefaultRequestContext}. Note that there are convenience methods
     * for creating new request context instances: {@link #newInstance(AJAXRequestData)}
     * and {@link #newInstance(HttpServletRequest, int, int)}.
     *
     * @param protocol The protocol to use (e.g. <code>http</code>). You probably want to pass
     *        <code>com.openexchange.tools.servlet.http.Tools.getProtocol()</code> here.
     * @param hostname The hostname to use if no HostnameService is available. You probably want to pass
     *        the result of a HostnameService call or as a fallback <code>HttpServletRequest.getServerName()</code> here.
     * @param servletPrefix The servlet prefix for AJAX action factories. Leading and trailing slashes are added if missing.
     */
    public DefaultRequestContext(String protocol, String hostname, String servletPrefix) {
        super();
        this.hostname = hostname;
        this.protocol = protocol;
        this.servletPrefix = normalizePrefix(servletPrefix);
    }

    /**
     * Creates a new {@link DefaultRequestContext} based on the given servlet request and an optional
     * session object.
     *
     * @param servletRequest The servlet request
     * @param contextId The context id or <code>-1</code> if none is available
     * @param userId The user id or <code>-1</code> if none is available
     * @return The request context
     */
    public static DefaultRequestContext newInstance(HttpServletRequest servletRequest, int contextId, int userId) {
        String protocol = determineProtocol(servletRequest);
        String hostname = determineHostname(servletRequest, contextId, userId);
        String servletPrefix = determineServletPrefix();
        return new DefaultRequestContext(protocol, hostname, servletPrefix);
    }

    /**
     * Creates a new {@link DefaultRequestContext} based on the given AJAX request data.
     *
     * @param requestData The request data
     * @return The request context
     */
    public static DefaultRequestContext newInstance(AJAXRequestData requestData) {
        String protocol = determineProtocol(requestData);
        String hostname = requestData.getHostname();
        String servletPrefix = determineServletPrefix();
        return new DefaultRequestContext(protocol, hostname, servletPrefix);
    }

    private static String determineProtocol(HttpServletRequest servletRequest) {
        return Tools.considerSecure(servletRequest) ? "https" : "http";
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public String getServletPrefix() {
        return servletPrefix;
    }

    private static String normalizePrefix(String prefix) {
        prefix = prefix.trim();
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        if (!prefix.endsWith(prefix)) {
            prefix = prefix + "/";
        }

        return prefix;
    }

    private static String determineServletPrefix() {
        try {
            return ServiceCallWrapper.tryServiceCall(DefaultRequestContext.class, DispatcherPrefixService.class, new ServiceUser<DispatcherPrefixService, String>() {
                @Override
                public String call(DispatcherPrefixService service) throws Exception {
                    return service.getPrefix();
                }
            }, DispatcherPrefixService.DEFAULT_PREFIX);
        } catch (ServiceException e) {
            return DispatcherPrefixService.DEFAULT_PREFIX;
        }
    }

    private static String determineHostname(HttpServletRequest servletRequest, final int contextId, final int userId) {
        String hostname = null;
        try {
            hostname = ServiceCallWrapper.tryServiceCall(DefaultRequestContext.class, HostnameService.class, new ServiceUser<HostnameService, String>() {
                @Override
                public String call(HostnameService service) throws Exception {
                    return service.getHostname(userId, contextId);
                }
            }, null);
        } catch (ServiceException e) {
            // ignore
        }

        if (hostname == null) {
            hostname = servletRequest.getServerName();
        }

        return hostname;

    }

    private static String determineProtocol(AJAXRequestData requestData) {
        return requestData.isSecure() ? "https" : "http";
    }

}
