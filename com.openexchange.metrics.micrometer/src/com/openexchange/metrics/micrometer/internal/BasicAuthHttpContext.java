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

package com.openexchange.metrics.micrometer.internal;

import java.io.IOException;
import java.net.URL;
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
        if (!request.getScheme().toLowerCase().equals("https")) {
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
            String loginAndPassword = new String(Base64.getDecoder().decode(authzHeader.substring(6)));

            String[] creds = Strings.splitByColon(loginAndPassword);
            if (creds.length != 2 || Strings.isEmpty(creds[0]) || Strings.isEmpty(creds[1])) {
                return false;
            }
            return creds[0].equals(this.login) && creds[1].equals(this.password);
        } catch (@SuppressWarnings("unused") IllegalArgumentException e) {
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
