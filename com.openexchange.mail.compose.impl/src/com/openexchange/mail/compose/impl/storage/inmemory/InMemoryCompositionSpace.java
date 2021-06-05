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

package com.openexchange.mail.compose.impl.storage.inmemory;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import com.openexchange.java.BufferingQueue;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.compose.ClientToken;
import com.openexchange.mail.compose.CompositionSpace;
import com.openexchange.mail.compose.CompositionSpaceId;
import com.openexchange.mail.compose.CompositionSpaceServiceFactory;
import com.openexchange.mail.compose.MessageDescription;

/**
 * {@link InMemoryCompositionSpace}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class InMemoryCompositionSpace implements CompositionSpace {

    private final CompositionSpaceId id;
    private final AtomicLong lastModifiedStamp;
    private final InMemoryMessage message;
    private final int userId;
    private final int contextId;
    private final ClientToken clientToken;

    /**
     * Initializes a new {@link InMemoryCompositionSpace}.
     */
    public InMemoryCompositionSpace(UUID id, MessageDescription initialMessageDesc, BufferingQueue<InMemoryMessage> bufferingQueue, int userId, int contextId, ClientToken clientToken) {
        super();
        this.id = new CompositionSpaceId(CompositionSpaceServiceFactory.DEFAULT_SERVICE_ID, id);
        this.userId = userId;
        this.contextId = contextId;

        lastModifiedStamp = new AtomicLong(System.currentTimeMillis());
        message = new InMemoryMessage(id, initialMessageDesc, bufferingQueue, userId, contextId);
        this.clientToken = clientToken;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    @Override
    public CompositionSpaceId getId() {
        return id;
    }

    @Override
    public Optional<MailPath> getMailPath() {
        return Optional.empty();
    }

    @Override
    public long getLastModified() {
        return lastModifiedStamp.get();
    }

    @Override
    public InMemoryMessage getMessage() {
        return message;
    }

    @Override
    public ClientToken getClientToken() {
        return clientToken;
    }

    /**
     * Atomically updates the last-modified time stamp.
     *
     * @param clientStamp The option client-side stamp
     * @return <code>true</code> if successfully updates; otherwise <code>false</code> on update conflict
     */
    public boolean updateLastModifiedStamp(Date clientStamp) {
        if (null == clientStamp) {
            lastModifiedStamp.set(System.currentTimeMillis());
            return true;
        }

        long stamp;
        do {
            stamp = lastModifiedStamp.get();
            if (stamp != clientStamp.getTime()) {
                // Current stamp is not equal to client-side stamp
                return false;
            }
        } while (!lastModifiedStamp.compareAndSet(stamp, System.currentTimeMillis()));
        return true;
    }

}
