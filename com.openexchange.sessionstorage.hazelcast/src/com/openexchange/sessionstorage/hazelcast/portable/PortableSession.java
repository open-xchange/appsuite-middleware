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

    public static final String PARAMETER_LOGIN_NAME = "loginName";
    public static final String PARAMETER_PASSWORD = "password";
    public static final String PARAMETER_CONTEXT_ID = "contextId";
    public static final String PARAMETER_USER_ID = "userId";
    public static final String PARAMETER_SESSION_ID = "sessionId";
    public static final String PARAMETER_SECRET = "secret";
    public static final String PARAMETER_LOGIN = "login";
    public static final String PARAMETER_RANDOM_TOKEN = "randomToken";
    public static final String PARAMETER_LOCAL_IP = "localIp";
    public static final String PARAMETER_AUTH_ID = "authId";
    public static final String PARAMETER_HASH = "hash";
    public static final String PARAMETER_CLIENT = "client";
    public static final String PARAMETER_USER_LOGIN = "userLogin";
    public static final String PARAMETER_ALT_ID = "altId";

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
        writer.writeUTF(PARAMETER_LOGIN_NAME, loginName);
        writer.writeUTF(PARAMETER_PASSWORD, password);
        writer.writeInt(PARAMETER_CONTEXT_ID, contextId);
        writer.writeInt(PARAMETER_USER_ID, userId);
        writer.writeUTF(PARAMETER_SESSION_ID, sessionId);
        writer.writeUTF(PARAMETER_SECRET, secret);
        writer.writeUTF(PARAMETER_LOGIN, login);
        writer.writeUTF(PARAMETER_RANDOM_TOKEN, randomToken);
        writer.writeUTF(PARAMETER_LOCAL_IP, localIp);
        writer.writeUTF(PARAMETER_AUTH_ID, authId);
        writer.writeUTF(PARAMETER_HASH, hash);
        writer.writeUTF(PARAMETER_CLIENT, client);
        writer.writeUTF(PARAMETER_USER_LOGIN, userLogin);
        /*
         * special handling for parameters map
         */
        Object altId = parameters.get(PARAM_ALTERNATIVE_ID);
        writer.writeUTF(PARAMETER_ALT_ID, null != altId && String.class.isInstance(altId) ? (String)altId : null);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        /*
         * basic properties
         */
        loginName = reader.readUTF(PARAMETER_LOGIN_NAME);
        password = reader.readUTF(PARAMETER_PASSWORD);
        contextId = reader.readInt(PARAMETER_CONTEXT_ID);
        userId = reader.readInt(PARAMETER_USER_ID);
        sessionId = reader.readUTF(PARAMETER_SESSION_ID);
        secret = reader.readUTF(PARAMETER_SECRET);
        login = reader.readUTF(PARAMETER_LOGIN);
        randomToken = reader.readUTF(PARAMETER_RANDOM_TOKEN);
        localIp = reader.readUTF(PARAMETER_LOCAL_IP);
        authId = reader.readUTF(PARAMETER_AUTH_ID);
        hash = reader.readUTF(PARAMETER_HASH);
        client = reader.readUTF(PARAMETER_CLIENT);
        userLogin = reader.readUTF(PARAMETER_USER_LOGIN);
        /*
         * special handling for parameters map
         */
        String altId = reader.readUTF(PARAMETER_ALT_ID);
        if (null != altId) {
            parameters.put(PARAM_ALTERNATIVE_ID, altId);
        }
    }

}
