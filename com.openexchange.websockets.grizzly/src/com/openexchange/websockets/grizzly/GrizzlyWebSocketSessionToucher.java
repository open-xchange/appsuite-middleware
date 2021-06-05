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

package com.openexchange.websockets.grizzly;

import static com.openexchange.java.Autoboxing.I;
import java.util.List;
import java.util.Map;
import com.openexchange.config.ConfigurationService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SessiondServiceExtended;

/**
 * {@link GrizzlyWebSocketSessionToucher}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class GrizzlyWebSocketSessionToucher<S extends SessionBoundWebSocket> implements Runnable {

    private final AbstractGrizzlyWebSocketApplication<S> app;

    /**
     * Initializes a new {@link GrizzlyWebSocketSessionToucher}.
     *
     * @param app The Web Socket application
     */
    public GrizzlyWebSocketSessionToucher(AbstractGrizzlyWebSocketApplication<S> app) {
        super();
        this.app = app;
    }

    @Override
    public void run() {
        // Acquire needed service
        SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();
        if (null == sessiondService) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GrizzlyWebSocketSessionToucher.class);
            logger.warn("", ServiceExceptionCode.absentService(SessiondServiceExtended.class));
            return;
        }

        // Get a list of currently active sessions bound to a Web Socket
        Map<String, List<S>> sessions = app.getActiveSessions();

        // Touch them
        for (Map.Entry<String, List<S>> sessionEntry : sessions.entrySet()) {
            Session session = sessiondService.getSession(sessionEntry.getKey());
            if (null == session) {
                // No such session
                for (SessionBoundWebSocket socket : sessionEntry.getValue()) {
                    app.close(socket, session);
                }
            }
        }
    }

    /**
     * Gets the required touch period for sessions in the distributed storage based on the configured session default lifetime.
     *
     * @param configService A reference to the configuration service
     * @return The touch period in milliseconds
     */
    public static int getTouchPeriod(ConfigurationService configService) {
        int defaultValue = 60 * 60 * 1000;
        int value;
        if (null == configService) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GrizzlyWebSocketSessionToucher.class);
            logger.warn("Unable to determine \"com.openexchange.sessiond.sessionDefaultLifeTime\", falling back to {}.", I(defaultValue));
            value = defaultValue;
        } else {
            value = configService.getIntProperty("com.openexchange.sessiond.sessionDefaultLifeTime", defaultValue);
        }
        return value;
    }

}
