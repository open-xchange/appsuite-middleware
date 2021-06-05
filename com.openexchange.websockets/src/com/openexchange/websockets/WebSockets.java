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
     * Gets a value indicating whether a push source token matches a specific web socket.
     * 
     * @param webSocket The web socket to check
     * @param sourceToken The source push token to check, or <code>null</code> if not specified
     * @return <code>true</code> if both match, <code>false</code>, otherwise
     */
    public static boolean matchesToken(WebSocket webSocket, String sourceToken) {
        return null != sourceToken && null != webSocket && null != webSocket.getConnectionId() && sourceToken.equals(webSocket.getConnectionId().getId());
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
