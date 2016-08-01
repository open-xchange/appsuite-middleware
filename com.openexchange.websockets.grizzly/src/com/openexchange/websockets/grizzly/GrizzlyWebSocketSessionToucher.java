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

package com.openexchange.websockets.grizzly;

import java.util.Map;
import org.glassfish.grizzly.websockets.WebSocket;
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
public class GrizzlyWebSocketSessionToucher implements Runnable {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GrizzlyWebSocketSessionToucher.class);

    private final GrizzlyWebSocketApplication app;

    /**
     * Initializes a new {@link GrizzlyWebSocketSessionToucher}.
     *
     * @param app The Web Socket application
     */
    public GrizzlyWebSocketSessionToucher(GrizzlyWebSocketApplication app) {
        super();
        this.app = app;
    }

    @Override
    public void run() {
        // Acquire needed service
        SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();
        if (false == SessiondServiceExtended.class.isInstance(sessiondService)) {
            LOG.warn("", ServiceExceptionCode.absentService(SessiondServiceExtended.class));
            return;
        }

        // Get a list of currently active sessions bound to a Web Socket
        Map<String, WebSocket> sessions = app.getActiveSessions();

        // Touch them
        for (Map.Entry<String, WebSocket> sessionEntry : sessions.entrySet()) {
            Session session = sessiondService.getSession(sessionEntry.getKey());
            if (null == session) {
                // No such session
                app.close(sessionEntry.getValue());
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
            LOG.warn("Unable to determine \"com.openexchange.sessiond.sessionDefaultLifeTime\", falling back to {}.", defaultValue);
            value = defaultValue;
        } else {
            value = configService.getIntProperty("com.openexchange.sessiond.sessionDefaultLifeTime", defaultValue);
        }
        return value;
    }

}
