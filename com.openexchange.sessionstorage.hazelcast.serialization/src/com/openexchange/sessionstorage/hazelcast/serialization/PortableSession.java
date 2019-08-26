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

package com.openexchange.sessionstorage.hazelcast.serialization;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.java.Strings;
import com.openexchange.session.Origin;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.SessionStorageConfiguration;
import com.openexchange.sessionstorage.StoredSession;

/**
 * {@link PortableSession} - The portable representation for {@link Session} type.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PortableSession extends StoredSession implements CustomPortable, VersionedPortable {

    private static final long serialVersionUID = -2346327568417617677L;

    /** Simple class to delay initialization until needed */
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PortableSession.class);

    /** The unique portable class ID of the {@link PortableSession} */
    public static final int CLASS_ID = 1;

    /**
     * The class version for {@link PortableSession}
     * <p>
     * This number should be incremented whenever fields are added;
     * see <a href="http://docs.hazelcast.org/docs/latest-development/manual/html/Serialization/Implementing_Portable_Serialization/Versioning_for_Portable_Serialization.html">here</a> for reference.
     */
    public static final int CLASS_VERSION = 4;

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
    public static final String PARAMETER_USER_AGENT = "userAgent";
    public static final String PARAMETER_LOGIN_TIME = "loginTime";
    public static final String PARAMETER_LOCAL_LAST_ACTIVE = "localLastActive";
    public static final String PARAMETER_REMOTE_PARAMETERS = "remoteParameters";
    public static final String PARAMETER_ORIGIN = "origin";

    /** The class definition for PortableSession */
    public static ClassDefinition CLASS_DEFINITION = new ClassDefinitionBuilder(FACTORY_ID, CLASS_ID, CLASS_VERSION)
        .addUTFField(PARAMETER_LOGIN_NAME)
        .addUTFField(PARAMETER_PASSWORD)
        .addIntField(PARAMETER_CONTEXT_ID)
        .addIntField(PARAMETER_USER_ID)
        .addUTFField(PARAMETER_SESSION_ID)
        .addUTFField(PARAMETER_SECRET)
        .addUTFField(PARAMETER_LOGIN)
        .addUTFField(PARAMETER_RANDOM_TOKEN)
        .addUTFField(PARAMETER_LOCAL_IP)
        .addUTFField(PARAMETER_AUTH_ID)
        .addUTFField(PARAMETER_HASH)
        .addUTFField(PARAMETER_CLIENT)
        .addUTFField(PARAMETER_USER_LOGIN)
        .addUTFField(PARAMETER_ALT_ID)
        .addUTFField(PARAMETER_USER_AGENT)
        .addLongField(PARAMETER_LOGIN_TIME)
        .addLongField(PARAMETER_LOCAL_LAST_ACTIVE)
        .addUTFField(PARAMETER_REMOTE_PARAMETERS)
        .addUTFField(PARAMETER_ORIGIN)
        .build();

    // -------------------------------------------------------------------------------------------------

    private Set<String> remoteParameterNames;
    private Long localLastActive;

    /**
     * Initializes a new {@link PortableSession}.
     */
    public PortableSession() {
        super();
        localLastActive = null;
        remoteParameterNames = Collections.emptySet();
    }

    /**
     * Initializes a new {@link PortableSession}.
     *
     * @param session The underlying session
     */
    public PortableSession(Session session) {
        super(session);
        localLastActive = null;
        Collection<String> configuredRemoteParameterNames = SessionStorageConfiguration.getInstance().getRemoteParameterNames(userId, contextId);
        Set<String> remoteParameterNames = new LinkedHashSet<>(configuredRemoteParameterNames.size() + 2); // Keep order

        // Add static remote parameters
        remoteParameterNames.add(PARAM_OAUTH_ACCESS_TOKEN);
        remoteParameterNames.add(PARAM_OAUTH_REFRESH_TOKEN);
        remoteParameterNames.add(PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE);

        // Add configured remote parameters
        remoteParameterNames.addAll(configuredRemoteParameterNames);
        this.remoteParameterNames = remoteParameterNames;
    }

    /**
     * Sets the local last-active time stamp.
     *
     * @param localLastActive The local last-active time stamp to set
     */
    public void setLocalLastActive(long localLastActive) {
        this.localLastActive = Long.valueOf(localLastActive);
    }

    /**
     * Gets the local last-active time stamp.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: This method might return <code>null</code>. Beware of auto-unboxing!
     * </div>
     *
     * @return The local last-active time stamp or <code>null</code> if not available
     */
    public Long getLocalLastActive() {
        return localLastActive;
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
        {
            Object altId = parameters.get(PARAM_ALTERNATIVE_ID);
            writer.writeUTF(PARAMETER_ALT_ID, null == altId ? null : altId.toString());
        }
        {
            Object userAgent = parameters.get(PARAM_USER_AGENT);
            writer.writeUTF(PARAMETER_USER_AGENT, null == userAgent ? null : userAgent.toString());
        }
        {
            Object loginTime = parameters.get(PARAM_LOGIN_TIME);
            writer.writeLong(PARAMETER_LOGIN_TIME, null != loginTime ? ((Long) loginTime).longValue() : -1L);
        }
        {
            Long localLastActive = this.localLastActive;
            writer.writeLong(PARAMETER_LOCAL_LAST_ACTIVE, null != localLastActive ? localLastActive.longValue() : -1L);
        }
        {
            Set<String> remoteParameterNames = this.remoteParameterNames;
            JSONObject jRemoteParameters = null;
            for (String parameterName : remoteParameterNames) {
                Object value = parameters.get(parameterName);
                if (isSerializablePojo(value)) {
                    if (jRemoteParameters == null) {
                        jRemoteParameters = new JSONObject(remoteParameterNames.size());
                    }
                    if (value instanceof Boolean) {
                        // Boolean
                        jRemoteParameters.putSafe(parameterName, value);
                    } else if (value instanceof Integer) {
                        // Integer
                        jRemoteParameters.putSafe(parameterName, value);
                    } else if (value instanceof Long) {
                        // Long
                        jRemoteParameters.putSafe(parameterName, value);
                    } else {
                        // Enforce String representation
                        jRemoteParameters.putSafe(parameterName, value.toString());
                    }
                    LOG.debug("Put remote parameter '{}' with value '{}' into portable session {} ({}@{})", parameterName, value, sessionId, Integer.valueOf(userId), Integer.valueOf(contextId));
                } else {
                    if (value == null) {
                        LOG.debug("No value available for remote parameter name '{}' in session {}.", parameterName, sessionId);
                    } else {
                        LOG.warn("Denied remote parameter name '{}' in session {}. Seems to be no ordinary Java object: {}", parameterName, sessionId, value.getClass().getName());
                    }
                }
            }
            if (null == jRemoteParameters) {
                writer.writeUTF(PARAMETER_REMOTE_PARAMETERS, null);
            } else {
                writer.writeUTF(PARAMETER_REMOTE_PARAMETERS, jRemoteParameters.toString(true));
            }
        }
        writer.writeUTF(PARAMETER_ORIGIN, null == origin ? "" : origin.name());
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
        {
            String altId = reader.readUTF(PARAMETER_ALT_ID);
            if (null != altId) {
                parameters.put(PARAM_ALTERNATIVE_ID, altId);
            }
        }
        {
            String userAgent = reader.readUTF(PARAMETER_USER_AGENT);
            if (null != userAgent) {
                parameters.put(PARAM_USER_AGENT, userAgent);
            }
        }
        {
            long loginTime = reader.readLong(PARAMETER_LOGIN_TIME);
            if (loginTime > 0) {
                parameters.put(PARAM_LOGIN_TIME, Long.valueOf(loginTime));
            }
        }
        {
            long localLastActive = reader.readLong(PARAMETER_LOCAL_LAST_ACTIVE);
            this.localLastActive = localLastActive > 0 ? Long.valueOf(localLastActive) : null;
        }
        {
            String sRemoteParameters = reader.readUTF(PARAMETER_REMOTE_PARAMETERS);
            if (null != sRemoteParameters) {
                try {
                    JSONObject jRemoteParameters = new JSONObject(sRemoteParameters);
                    Set<String> remoteParameterNames = new LinkedHashSet<>(jRemoteParameters.length()); // Keep order
                    for (Map.Entry<String,Object> entry : jRemoteParameters.entrySet()) {
                        String name = entry.getKey();
                        parameters.put(name, entry.getValue());
                        remoteParameterNames.add(name);
                    }
                    this.remoteParameterNames = remoteParameterNames;
                } catch (JSONException je) {
                    LOG.warn("Failed to decode remote parameters from session {}.", sessionId, je);
                }
            }
        }
        {
            String sOrigin = reader.readUTF(PARAMETER_ORIGIN);
            origin = Strings.isEmpty(sOrigin) ? null : Origin.originFor(sOrigin);
        }
    }

    private static final String POJO_PACKAGE = "java.lang.";

    private static boolean isSerializablePojo(Object obj) {
        return null == obj ? false : ((obj instanceof Serializable) && obj.getClass().getName().startsWith(POJO_PACKAGE));
    }

}
