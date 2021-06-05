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
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.session.Session;

/**
 * {@link PortableSessionCollection} - A collection of portable representations for {@link Session} type.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PortableSessionCollection implements CustomPortable, VersionedPortable {

    /** The unique portable class ID of the {@link PortableSessionCollection} */
    public static final int CLASS_ID = 403;

    /** The class version for {@link PortableSessionCollection} */
    public static final int CLASS_VERSION = PortableSession.CLASS_VERSION;

    /** The class definition for PortableCacheEvent */
    public static ClassDefinition CLASS_DEFINITION = new ClassDefinitionBuilder(FACTORY_ID, CLASS_ID, CLASS_VERSION)
        .addPortableArrayField("sessions", PortableSession.CLASS_DEFINITION)
        .build();

    // -------------------------------------------------------------------------------------------------

    private PortableSession[] sessions;

    /**
     * Initializes a new {@link PortableSessionCollection}.
     */
    public PortableSessionCollection() {
        super();
        sessions = new PortableSession[0];
    }

    /**
     * Initializes a new {@link PortableSessionCollection}.
     *
     * @param session The underlying session
     */
    public PortableSessionCollection(PortableSession[] sessions) {
        super();
        this.sessions = sessions;
    }

    /**
     * Gets the sessions
     *
     * @return The sessions
     */
    public PortableSession[] getSessions() {
        return sessions;
    }

    @Override
    public int getFactoryId() {
        return FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public int getClassVersion() {
        return CLASS_VERSION;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writePortableArray("sessions", sessions);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        Portable[] portables = reader.readPortableArray("sessions");
        sessions = new PortableSession[portables.length];
        for (int i = 0; i < portables.length; i++) {
            sessions[i] = (PortableSession) portables[i];
        }
    }

}
