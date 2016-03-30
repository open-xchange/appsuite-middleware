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

package com.openexchange.sessiond.portable;

import java.io.IOException;
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

    /** The unique portable class ID of the {@link PortableTokenSessionControl} */
    public static final int CLASS_ID = 18;

    public static final String PARAMETER_SESSION = "session";
    public static final String PARAMETER_CLIENT_TOKEN = "clientToken";
    public static final String PARAMETER_SERVER_TOKEN = "serverToken";
    public static final String PARAMETER_CREATION_STAMP = "creationStamp";

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
