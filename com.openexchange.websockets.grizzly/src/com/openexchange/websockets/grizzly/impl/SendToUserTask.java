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

package com.openexchange.websockets.grizzly.impl;

import static com.openexchange.java.Autoboxing.I;
import org.slf4j.Logger;
import com.openexchange.threadpool.AbstractTask;

/**
 * {@link SendToUserTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class SendToUserTask extends AbstractTask<Void> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SendToUserTask.class);

    private final String message;
    private final String sourceToken;
    private final String pathFilter;
    private final boolean remote;
    private final int userId;
    private final int contextId;
    private final DefaultGrizzlyWebSocketApplication application;

    /**
     * Initializes a new {@link SendToUserTask}.
     *
     * @param message The text message to send
     * @param sourceToken The push token of the client triggering the update, or <code>null</code> if not available
     * @param pathFilter The optional path to filter by (e.g. <code>"/websockets/push"</code>)
     * @param remote Whether the text message was remotely received; otherwise <code>false</code> for local origin
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param application The running application
     */
    public SendToUserTask(String message, String sourceToken, String pathFilter, boolean remote, int userId, int contextId, DefaultGrizzlyWebSocketApplication application) {
        super();
        this.message = message;
        this.sourceToken = sourceToken;
        this.pathFilter = pathFilter;
        this.remote = remote;
        this.userId = userId;
        this.contextId = contextId;
        this.application = application;
    }

    @Override
    public Void call() throws Exception {
        try {
            application.sendToUser(message, sourceToken, pathFilter, remote, userId, contextId);
        } catch (Exception e) {
            LOG.error("Failed to send message to user {} in context {}", I(userId), I(contextId), e);
        }
        return null;
    }

}
