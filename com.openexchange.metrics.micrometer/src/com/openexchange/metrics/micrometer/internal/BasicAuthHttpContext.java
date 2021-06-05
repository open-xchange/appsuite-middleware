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

package com.openexchange.metrics.micrometer.internal;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.osgi.service.http.HttpContext;
import com.openexchange.java.Strings;

/**
 * {@link BasicAuthHttpContext}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
@SuppressWarnings("unused")
public class BasicAuthHttpContext implements HttpContext {

    private static final String AUTH_HEADER = "Authorization";
    private final String password;
    private final String login;

    /**
     * Initializes a new {@link BasicAuthHttpContext}.
     *
     * @param login The user name
     * @param password The password
     */
    public BasicAuthHttpContext(String login, String password) {
        super();
        this.login = login;
        this.password = password;
    }

    @Override
    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if ((!request.getScheme().toLowerCase().equals("https")) && !request.getRemoteAddr().equals(request.getLocalAddr())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        if (request.getHeader(AUTH_HEADER) == null) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"Access to the prometheus metrics\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        if (isAuthenticated(request)) {
            return true;
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    /**
     * Checks if the given request is authenticated
     *
     * @param request The request
     * @return <code>true</code> if its authenticated, <code>false</code> otherwise
     */
    protected boolean isAuthenticated(HttpServletRequest request) {
        try {
            String authzHeader = request.getHeader(AUTH_HEADER);
            String loginAndPassword = new String(Base64.getDecoder().decode(authzHeader.substring(6)), StandardCharsets.ISO_8859_1);
            String[] creds = loginAndPassword.split(":", 2);
            if (creds.length != 2 || Strings.isEmpty(creds[0]) || Strings.isEmpty(creds[1])) {
                return false;
            }
            return creds[0].equals(this.login) && creds[1].equals(this.password);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public URL getResource(String name) {
        return null;
    }

    @Override
    public String getMimeType(String name) {
        return null;
    }

}
