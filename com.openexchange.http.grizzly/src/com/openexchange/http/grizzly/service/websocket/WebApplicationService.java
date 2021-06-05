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

package com.openexchange.http.grizzly.service.websocket;

import java.util.Dictionary;
import org.glassfish.grizzly.websockets.WebSocketApplication;

/**
 * {@link WebApplicationService} - The service to register/unregister Web Socket Applications.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.0
 */
public interface WebApplicationService {

    /**
     * Registers a <code>WebSocketApplication</code> to a specific context path and URL pattern.
     * <p>
     * If you wish to associate this application with the root context, use an empty string for the <code>contextPath</code> argument.
     *
     * <pre>
     * Examples:
     * // WS application will be invoked:
     * // ws://localhost:8080/echo
     * // WS application will not be invoked:
     * // ws://localhost:8080/foo/echo
     * // ws://localhost:8080/echo/some/path
     * register(&quot;&quot;, &quot;/echo&quot;, webSocketApplication);
     *
     * // WS application will be invoked:
     * // ws://localhost:8080/echo
     * // ws://localhost:8080/echo/some/path
     * // WS application will not be invoked:
     * // ws://localhost:8080/foo/echo
     * register(&quot;&quot;, &quot;/echo/*&quot;, webSocketApplication);
     *
     * // WS application will be invoked:
     * // ws://localhost:8080/context/echo
     *
     * // WS application will not be invoked:
     * // ws://localhost:8080/echo
     * // ws://localhost:8080/context/some/path
     * register(&quot;/context&quot;, &quot;/echo&quot;, webSocketApplication);
     * </pre>
     *
     * @param contextPath The context path (per servlet rules)
     * @param urlPattern The URL pattern (per servlet rules)
     * @param app The Web Socket application
     * @throws java.lang.IllegalArgumentException if any of the arguments are invalid
     */
    void registerWebSocketApplication(String contextPath, String urlPattern, WebSocketApplication app, Dictionary<String, Object> initParams);

    /**
     * Unregisters a previous registration done by <code>registerWebApplication</code> method.
     *
     * @param app The application to unregister
     * @throws java.lang.IllegalArgumentException If there is no such registration
     */
    void unregisterWebSocketApplication(WebSocketApplication app);

}
