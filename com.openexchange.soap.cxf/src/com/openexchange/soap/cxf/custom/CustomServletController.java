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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.soap.cxf.custom;

import java.net.URI;
import java.util.Optional;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.servlet.BaseUrlHelper;
import org.apache.cxf.transport.servlet.ServletController;

/**
 * {@link CustomServletController} is a custom {@link ServletController} which creates the base url based on the X-Forwarded-Proto header if present.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.5
 */
public class CustomServletController extends ServletController {

    private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
    private static final String X_FORWARDED_PORT = "X-Forwarded-Port";
    private static final String HTTPS = "https";

    /**
     * Initializes a new {@link CustomServletController}.
     *
     * @param destinationRegistry
     * @param config
     * @param serviceListGenerator
     */
    public CustomServletController(DestinationRegistry destinationRegistry, ServletConfig config, HttpServlet serviceListGenerator) {
        super(destinationRegistry, config, serviceListGenerator);
    }

    @Override
    protected String getBaseURL(HttpServletRequest request) {
        if (forcedBaseAddress != null) {
            return forcedBaseAddress;
        }

        String proto = request.getHeader(X_FORWARDED_PROTO);
        if (proto != null && proto.equalsIgnoreCase(HTTPS)) {
            Optional<String> optPort = Optional.ofNullable(request.getHeader(X_FORWARDED_PORT));
            return getBaseUrl(request, HTTPS, optPort.orElse("443"));
        }
        return BaseUrlHelper.getBaseURL(request);
    }

    /**
     * Similar to {@link BaseUrlHelper#getBaseURL(HttpServletRequest)} but uses the given url scheme and port
     *
     * @param request The {@link HttpServletRequest}
     * @param scheme The scheme to use
     * @param port The port to use
     * @return The base url
     */
    private String getBaseUrl(HttpServletRequest request, String scheme, String port) {
        StringBuilder sb = new StringBuilder();
        URI uri = URI.create(request.getRequestURL().toString());
        sb.append(scheme).append("://").append(uri.getHost()).append(":").append(port);
        String contextPath = request.getContextPath();
        if (contextPath != null) {
            sb.append(contextPath);
        }
        String servletPath = request.getServletPath();
        if (servletPath != null) {
            sb.append(servletPath);
        }

        return sb.toString();
    }

}
