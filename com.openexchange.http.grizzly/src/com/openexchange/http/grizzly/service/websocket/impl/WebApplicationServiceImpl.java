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

package com.openexchange.http.grizzly.service.websocket.impl;

import java.util.Dictionary;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import com.openexchange.http.grizzly.service.websocket.WebApplicationService;

/**
 * {@link WebApplicationServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.0
 */
public final class WebApplicationServiceImpl implements WebApplicationService {

    /**
     * Initializes a new {@link WebApplicationServiceImpl}.
     */
    public WebApplicationServiceImpl() {
        super();
    }

    @Override
    public void registerWebSocketApplication(final String contextPath, final String urlPattern, final WebSocketApplication app, final Dictionary<String, Object> initParams) {
        WebSocketEngine.getEngine().register(contextPath, urlPattern, app);
    }

    @Override
    public void unregisterWebSocketApplication(final WebSocketApplication app) {
        WebSocketEngine.getEngine().unregister(app);
    }

}
