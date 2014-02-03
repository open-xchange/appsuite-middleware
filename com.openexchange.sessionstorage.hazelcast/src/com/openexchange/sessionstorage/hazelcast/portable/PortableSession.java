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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.sessionstorage.hazelcast.portable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.StoredSession;

/**
 * {@link PortableSession}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PortableSession extends StoredSession implements CustomPortable {

    /** The unique portable class ID of the {@link PortableSession} */
    public static final int CLASS_ID = 1;

    private static final long serialVersionUID = -2346327568417617677L;

    /**
     * Initializes a new {@link PortableSession}.
     */
    public PortableSession() {
        super();
    }

    /**
     * Initializes a new {@link PortableSession}.
     *
     * @param session The underlying session
     */
    public PortableSession(final Session session) {
        super(session);
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
        /*
         * basic properties
         */
        writer.writeUTF("loginName", loginName);
        writer.writeUTF("password", password);
        writer.writeInt("contextId", contextId);
        writer.writeInt("userId", userId);
        writer.writeUTF("sessionId", sessionId);
        writer.writeUTF("secret", secret);
        writer.writeUTF("login", login);
        writer.writeUTF("randomToken", randomToken);
        writer.writeUTF("localIp", localIp);
        writer.writeUTF("authId", authId);
        writer.writeUTF("hash", hash);
        writer.writeUTF("client", client);
        writer.writeUTF("userLogin", userLogin);
        /*
         * special handling for parameters map
         */
        Object alternativeID = parameters.get(PARAM_ALTERNATIVE_ID);
        writer.writeUTF("alternativeID", null != alternativeID && String.class.isInstance(alternativeID) ? (String)alternativeID : null);
        Object capabilitiesValue = parameters.get(PARAM_CAPABILITIES);
        if (null != capabilitiesValue && java.util.Collection.class.isInstance(capabilitiesValue)) {
            Collection<?> capabilities = (Collection<?>)capabilitiesValue;
            int size = capabilities.size();
            writer.writeInt("capabilitiesSize", size);
            int i = 0;
            for (Object capability : capabilities) {
                writer.writeUTF("capability" + String.valueOf(++i),
                    null != capability && String.class.isInstance(capability) ? (String)capability : null);
            }
        } else {
            writer.writeInt("capabilitiesSize", 0);
        }
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        /*
         * basic properties
         */
        loginName = reader.readUTF("loginName");
        password = reader.readUTF("password");
        contextId = reader.readInt("contextId");
        userId = reader.readInt("userId");
        sessionId = reader.readUTF("sessionId");
        secret = reader.readUTF("secret");
        login = reader.readUTF("login");
        randomToken = reader.readUTF("randomToken");
        localIp = reader.readUTF("localIp");
        authId = reader.readUTF("authId");
        hash = reader.readUTF("hash");
        client = reader.readUTF("client");
        userLogin = reader.readUTF("userLogin");
        /*
         * special handling for parameters map
         */
        String alternativeID = reader.readUTF("alternativeID");
        if (null != alternativeID) {
            parameters.put(PARAM_ALTERNATIVE_ID, alternativeID);
        }
        int capabilitiesSize = reader.readInt("capabilitiesSize");
        if (0 < capabilitiesSize) {
            List<String> capabilities = new ArrayList<String>();
            for (int i = 0; i < capabilitiesSize; i++) {
                capabilities.add(reader.readUTF("capabilitiy" + String.valueOf(i)));
            }
            parameters.put(PARAM_CAPABILITIES, capabilities);
        }
    }

}
