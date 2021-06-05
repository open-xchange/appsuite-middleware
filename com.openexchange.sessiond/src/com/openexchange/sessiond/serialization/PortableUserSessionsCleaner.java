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

package com.openexchange.sessiond.serialization;

import java.io.IOException;
import java.util.concurrent.Callable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionHandler;

/**
 * {@link PortableUserSessionsCleaner}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public class PortableUserSessionsCleaner extends AbstractCustomPortable implements Callable<Integer> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PortableUserSessionsCleaner.class);

    private static final String FIELD_CONTEXT_ID = "contextId";
    private static final String FIELD_USER_ID = "userId";

    // ----------------------------------------------------------------------------------------------------------

    private int contextId;
    private int userId;

    /**
     * Initializes a new {@link PortableUserSessionsCleaner}.
     */
    public PortableUserSessionsCleaner() {
        super();
    }

    /**
     * Initializes a new {@link PortableUserSessionsCleaner}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public PortableUserSessionsCleaner(int userId, int contextId) {
        this();
        this.userId = userId;
        this.contextId = contextId;
    }

    @Override
    public Integer call() throws Exception {
        try {
            Session[] removedSessions = SessionHandler.removeUserSessions(userId, contextId);
            return Integer.valueOf(null == removedSessions ? 0 : removedSessions.length);
        } catch (Exception exception) {
            LOG.error("Unable to remove sessions for user {} in context {}.", Integer.valueOf(userId), Integer.valueOf(contextId));
            throw exception;
        }
    }

    @Override
    public int getClassId() {
        return PORTABLE_USER_SESSIONS_CLEANER_CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeInt(FIELD_CONTEXT_ID, contextId);
        writer.writeInt(FIELD_USER_ID, userId);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.contextId = reader.readInt(FIELD_CONTEXT_ID);
        this.userId = reader.readInt(FIELD_USER_ID);
    }

    @Override
    public String toString() {
        return "PortableUserSessionsCleaner for user " + userId + " in context " + contextId;
    }

}
