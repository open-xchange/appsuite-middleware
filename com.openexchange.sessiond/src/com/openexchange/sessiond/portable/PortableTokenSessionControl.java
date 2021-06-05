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

package com.openexchange.sessiond.portable;

import java.io.IOException;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.sessionstorage.hazelcast.serialization.PortableSession;

/**
 * {@link PortableTokenSessionControl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PortableTokenSessionControl implements CustomPortable {

    private static final String PARAMETER_SESSION = "session";
    private static final String PARAMETER_CLIENT_TOKEN = "clientToken";
    private static final String PARAMETER_SERVER_TOKEN = "serverToken";
    private static final String PARAMETER_CREATION_STAMP = "creationStamp";

    /** The unique portable class ID of the {@link PortableTokenSessionControl} */
    public static final int CLASS_ID = 18;

    /** The class version for {@link PortableTokenSessionControl} */
    public static final int CLASS_VERSION = PortableSession.CLASS_VERSION;

    /** The class definition for PortableTokenSessionControl */
    public static ClassDefinition CLASS_DEFINITION = new ClassDefinitionBuilder(FACTORY_ID, CLASS_ID, CLASS_VERSION)
        .addPortableField(PARAMETER_SESSION, PortableSession.CLASS_DEFINITION)
        .addUTFField(PARAMETER_CLIENT_TOKEN)
        .addUTFField(PARAMETER_SERVER_TOKEN)
        .addLongField(PARAMETER_CREATION_STAMP)
        .build();

    // --------------------------------------------------------------------------------------------------------------------

    private PortableSession session;
    private String clientToken;
    private String serverToken;
    private long creationStamp;

    /**
     * Initializes a new {@link PortableTokenSessionControl}.
     */
    public PortableTokenSessionControl() {
        super();
    }

    /**
     * Initializes a new {@link PortableTokenSessionControl}.
     *
     * @param session The portable session
     * @param clientToken The client token
     * @param serverToken The server token
     * @param creationStamp
     */
    public PortableTokenSessionControl(PortableSession session, String clientToken, String serverToken, long creationStamp) {
        super();
        this.session = session;
        this.clientToken = clientToken;
        this.serverToken = serverToken;
        this.creationStamp = creationStamp;
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
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writePortable(PARAMETER_SESSION, session);
        writer.writeUTF(PARAMETER_CLIENT_TOKEN, clientToken);
        writer.writeUTF(PARAMETER_SERVER_TOKEN, serverToken);
        writer.writeLong(PARAMETER_CREATION_STAMP, creationStamp);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        session = reader.readPortable(PARAMETER_SESSION);
        clientToken = reader.readUTF(PARAMETER_CLIENT_TOKEN);
        serverToken = reader.readUTF(PARAMETER_SERVER_TOKEN);
        creationStamp = reader.readLong(PARAMETER_CREATION_STAMP);
    }

    /**
     * Gets the creation time in milliseconds
     *
     * @return The creation time in milliseconds
     */
    public long getCreationStamp() {
        return creationStamp;
    }

    /**
     * Gets the portable session
     *
     * @return The portable session
     */
    public PortableSession getSession() {
        return session;
    }

    /**
     * Gets the client token
     *
     * @return The client token
     */
    public String getClientToken() {
        return clientToken;
    }

    /**
     * Gets the server token
     *
     * @return The server token
     */
    public String getServerToken() {
        return serverToken;
    }

}
