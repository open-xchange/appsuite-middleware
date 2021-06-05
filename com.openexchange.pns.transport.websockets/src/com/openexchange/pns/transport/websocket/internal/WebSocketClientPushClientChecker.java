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

package com.openexchange.pns.transport.websocket.internal;

import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.pns.KnownTopic;
import com.openexchange.pns.transport.websocket.WebSocketClient;
import com.openexchange.pns.transport.websocket.WebSocketToClientResolver;
import com.openexchange.push.PushClientChecker;
import com.openexchange.session.Session;


/**
 * {@link WebSocketClientPushClientChecker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketClientPushClientChecker implements PushClientChecker {

    private final WebSocketToClientResolverRegistry resolvers;

    /**
     * Initializes a new {@link WebSocketClientPushClientChecker}.
     */
    public WebSocketClientPushClientChecker(WebSocketToClientResolverRegistry resolvers) {
        super();
        this.resolvers = resolvers;

    }

    @Override
    public boolean isAllowed(String clientId, Session session) throws OXException {
        if (null == session || Strings.isEmpty(clientId)) {
            // Unable to check
            return false;
        }

        String newMailTopic = KnownTopic.MAIL_NEW.getName();
        for (WebSocketToClientResolver resolver : resolvers) {
            WebSocketClient webSocketClient = resolver.getSupportedClients().get(clientId);
            if (null != webSocketClient) {
                return webSocketClient.isInterestedIn(newMailTopic);
            }
        }

        return false;
    }

}
