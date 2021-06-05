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

package com.openexchange.sessionstorage.hazelcast.serialization;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link PortableMultipleSessionRemoteLookUp}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PortableMultipleSessionRemoteLookUp extends AbstractCustomPortable implements Callable<PortableSessionCollection> {

    private static final AtomicReference<SessiondService> SERVICE_REFERENCE = new AtomicReference<SessiondService>();

    /**
     * Sets the service reference
     *
     * @param service The service reference or <code>null</code>
     */
    public static void setSessiondServiceReference(SessiondService service) {
        SERVICE_REFERENCE.set(service);
    }

    private static final AtomicReference<ObfuscatorService> OBFUSCATOR_REFERENCE = new AtomicReference<ObfuscatorService>();

    /**
     * Sets the service reference
     *
     * @param service The service reference or <code>null</code>
     */
    public static void setObfuscatorServiceReference(ObfuscatorService service) {
        OBFUSCATOR_REFERENCE.set(service);
    }

    // ---------------------------------------------------------------------------------------------------------------------

    /** The unique portable class ID of the {@link PortableMultipleSessionRemoteLookUp} */
    public static final int CLASS_ID = 402;

    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_CTX_ID = "contextId";
    private static final String FIELD_WITH_LOCAL_LAST_ACTIVE = "withLocalLastActive";

    private int userId;
    private int contextId;
    private boolean withLocalLastActive;

    /**
     * Initializes a new {@link PortableMultipleSessionRemoteLookUp}.
     */
    public PortableMultipleSessionRemoteLookUp() {
        super();
    }

    /**
     * Initializes a new {@link PortableMultipleSessionRemoteLookUp}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public PortableMultipleSessionRemoteLookUp(int userId, int contextId) {
        this(userId, contextId, false);
    }

    /**
     * Initializes a new {@link PortableMultipleSessionRemoteLookUp}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param withLocalLastActive <code>true</code> to include the local last-active time stamp for a session; otherwise <code>false</code>
     */
    public PortableMultipleSessionRemoteLookUp(int userId, int contextId, boolean withLocalLastActive) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.withLocalLastActive = withLocalLastActive;
    }

    @Override
    public PortableSessionCollection call() throws Exception {
        SessiondService service = SERVICE_REFERENCE.get();
        if (null == service) {
            return new PortableSessionCollection(new PortableSession[0]);
        }

        Collection<Session> sessions = service.getSessions(userId, contextId);
        if (null == sessions || sessions.isEmpty()) {
            return new PortableSessionCollection(new PortableSession[0]);
        }

        // Obfuscate password
        PortableSession[] portableSessions = new PortableSession[sessions.size()];
        int i = 0;
        for (Session session : sessions) {
            PortableSession portableSession = new PortableSession(session);
            if (withLocalLastActive) {
                Object oLocalLastActive = session.getParameter(Session.PARAM_LOCAL_LAST_ACTIVE);
                if (null != oLocalLastActive) {
                    portableSession.setLocalLastActive(((Long) oLocalLastActive).longValue());
                }
            }
            portableSession.setPassword(OBFUSCATOR_REFERENCE.get().obfuscate(portableSession.getPassword()));
            portableSessions[i++] = portableSession;
        }

        return new PortableSessionCollection(portableSessions);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeInt(FIELD_CTX_ID, contextId);
        writer.writeInt(FIELD_USER_ID, userId);
        writer.writeBoolean(FIELD_WITH_LOCAL_LAST_ACTIVE, withLocalLastActive);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.contextId = reader.readInt(FIELD_CTX_ID);
        this.userId = reader.readInt(FIELD_USER_ID);
        this.withLocalLastActive = reader.readBoolean(FIELD_WITH_LOCAL_LAST_ACTIVE);
    }

}
