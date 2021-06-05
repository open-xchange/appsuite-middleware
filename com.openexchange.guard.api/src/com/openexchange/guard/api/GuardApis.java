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

package com.openexchange.guard.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
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
            extractedCookies.add(new Cookie(cookie.getName(), cookie.getValue()));
        }
        return extractedCookies;
    }

}
