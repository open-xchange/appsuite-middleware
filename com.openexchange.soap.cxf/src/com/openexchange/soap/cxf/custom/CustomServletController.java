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
