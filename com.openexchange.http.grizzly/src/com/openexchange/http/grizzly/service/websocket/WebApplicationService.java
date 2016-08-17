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
