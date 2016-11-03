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

package com.openexchange.websockets;

import com.openexchange.java.Strings;

/**
 * {@link WebSockets} - A utility class for Web Sockets.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSockets {

    /**
     * Initializes a new {@link WebSockets}.
     */
    private WebSockets() {
        super();
    }

    /**
     * Checks if specified path filter matches given socket.
     *
     * <pre>
     * Examples:
     *   Filter "/websockets/push"
     *    matches:
     *     "/websockets/push"
     *    does not match:
     *     "/websockets/push/foo"
     *     "/websockets/pushother"
     *     "/websockets/"
     *
     *   Filter "/websockets/push/*"
     *    matches:
     *     "/websockets/push"
     *     "/websockets/push/foo"
     *    does not match:
     *     "/websockets/pushother"
     *     "/websockets/"
     *
     *   Filter "*"
     *    matches all
     * </pre>
     *
     * @param pathFilter The filter expression
     * @param socket The socket that might match
     * @return <code>true</code> if socket matches; otherwise <code>false</code>
     */
    public static boolean matches(String pathFilter, WebSocket socket) {
        return null == socket ? false : matches(pathFilter, socket.getPath());
    }

    /**
     * Checks if specified path filter matches given socket path.
     *
     * <pre>
     * Examples:
     *   Filter "/websockets/push"
     *    matches:
     *     "/websockets/push"
     *    does not match:
     *     "/websockets/push/foo"
     *     "/websockets/pushother"
     *     "/websockets/"
     *
     *   Filter "/websockets/push/*"
     *    matches:
     *     "/websockets/push"
     *     "/websockets/push/foo"
     *    does not match:
     *     "/websockets/pushother"
     *     "/websockets/"
     *
     *   Filter "*"
     *    matches all
     * </pre>
     *
     * @param pathFilter The filter expression
     * @param path The socket path that might match
     * @return <code>true</code> if socket matches; otherwise <code>false</code>
     */
    public static boolean matches(String pathFilter, String path) {
        if (null == pathFilter) {
            // No filter at all
            return true;
        }

        if (Strings.isEmpty(path)) {
            // Socket has no path
            return false;
        }

        if ("*".equals(pathFilter)) {
            // Matches all paths
            return true;
        }

        if (!pathFilter.endsWith("/*")) {
            // Check for exact match
            return path.equals(pathFilter);
        }

        // Prefix look-up...
        String prefix = pathFilter.substring(0, pathFilter.length() - 2);
        if (!path.startsWith(prefix)) {
            return false;
        }

        if (path.length() == prefix.length()) {
            // Exact match
            return true;
        }

        // Filter "websockets/push" should match "websockets/push/foo", but not "websockets/pushother"
        return path.charAt(prefix.length()) == '/';
    }

    /**
     * Validates the path filter expression.
     *
     * @param pathFilter The path filter expression to validate.
     * @return <code>true</code> if the path filter expression is valid; otherwise <code>false</code>
     */
    public static boolean validatePath(String pathFilter) {
        if (null == pathFilter) {
            // A null value is accepted... Simply no filter
            return true;
        }

        if (Strings.isEmpty(pathFilter)) {
            return false;
        }

        // Special "match all"?
        if ("*".equals(pathFilter)) {
            return true;
        }

        if (pathFilter.endsWith("/*")) {
            pathFilter = pathFilter.substring(0, pathFilter.length() - 2);
        }

        int length = pathFilter.length();
        if (length == 0) {
            return false;
        }

        if ('/' != pathFilter.charAt(0)) {
            // Does not start with a slash character
            return false;
        }

        for (int i = 0; i < length; i++) {
            char ch = pathFilter.charAt(i);
            if (ch == '/') {
                // Can't end with a '/' but anywhere else is okay
                if ((i == length - 1)) {
                    return false;
                }
                // Can't have "//"
                if (i > 0 && pathFilter.charAt(i - 1) == '/') {
                    return false;
                }
                continue;
            }
            if (('A' <= ch) && (ch <= 'Z')) {
                continue;
            }
            if (('a' <= ch) && (ch <= 'z')) {
                continue;
            }
            if (('0' <= ch) && (ch <= '9')) {
                continue;
            }
            if ((ch == '_') || (ch == '-') || (ch == '.')) {
                continue;
            }
            return false;
        }

        return true;
    }

}
