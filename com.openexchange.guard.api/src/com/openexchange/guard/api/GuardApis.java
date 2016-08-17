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

package com.openexchange.guard.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.session.Session;

/**
 * {@link GuardApis} - Utility class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v2.0.0
 */
public class GuardApis {

    /**
     * Initializes a new {@link GuardApis}.
     */
    private GuardApis() {
        super();
    }

    /**
     * Gets a map for specified arguments.
     *
     * @param args The arguments
     * @return The resulting map
     */
    public static Map<String, String> mapFor(String... args) {
        if (null == args) {
            return null;
        }
        int length = args.length;
        if (0 == length || (length % 2) != 0) {
            return null;
        }
        Map<String, String> map = new LinkedHashMap<String, String>(length >> 1);
        for (int i = 0; i < length; i+=2) {
            map.put(args[i], args[i+1]);
        }
        return map;
    }

    /**
     * Extracts the user-authenticating cookies from given HTTP request using associated session
     *
     * @param request The HTTP request
     * @param session The associated session
     * @return The extracted cookies or an empty list
     */
    public static List<Cookie> extractCookiesFrom(HttpServletRequest request, Session session) {
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (null == cookies) {
            return Collections.emptyList();
        }

        List<Cookie> extractedCookies = new LinkedList<Cookie>();
        for (javax.servlet.http.Cookie cookie : cookies) {
            String name = cookie.getName();
            if (name.startsWith(LoginServlet.SECRET_PREFIX)) {
                String value = cookie.getValue();
                if (null != value && value.equals(session.getSecret())) {
                    extractedCookies.add(new Cookie(name, cookie.getValue()));
                }
            } else if (name.startsWith(LoginServlet.PUBLIC_SESSION_PREFIX)) {
                String value = cookie.getValue();
                if (null != value && value.equals(session.getParameter(Session.PARAM_ALTERNATIVE_ID))) {
                    extractedCookies.add(new Cookie(name, cookie.getValue()));
                }
            } else if ("JSESSIONID".equals(name)) {
                String value = cookie.getValue();
                if (null != value) {
                    extractedCookies.add(new Cookie(name, value));
                }
            }
        }
        return extractedCookies;
    }

}
