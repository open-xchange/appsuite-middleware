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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.java.Charsets;
import com.openexchange.java.StringAppender;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.SessionStorageConfiguration;
import com.openexchange.sessionstorage.StoredSession;

/**
 * {@link PortableSession} - The portable representation for {@link Session} type.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PortableSession extends StoredSession implements CustomPortable {

    private static final long serialVersionUID = -2346327568417617677L;

    /**
     * BitSet of www-form-url safe characters.
     */
    protected static final BitSet BIT_SET_PARAMS;

    // Static initializer for BIT_SET_PARAMS
    static {
        final BitSet bitSet = new BitSet(256);

        // Exclude ':' and '%' from printable ASCII characters
        for (int i = 32; i < 37; i++) {
            bitSet.set(i);
        }
        for (int i = 38; i < 58; i++) {
            bitSet.set(i);
        }
        for (int i = 59; i < 127; i++) {
            bitSet.set(i);
        }

        BIT_SET_PARAMS = bitSet;
    }

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
    public static final String PARAMETER_REMOTE_PARAMETER_NAMES = "remoteParameterNames";
    public static final String PARAMETER_REMOTE_PARAMETER_VALUES = "remoteParameterValues";
    public static final String PARAMETER_SERIALIZABLE_PARAMETER_NAMES = "remoteSerializableParameterNames";

    // Must not contain a colon in any name!
    private static final String[] PORTABLE_PARAMETERS = new String[] { "kerberosSubject", "kerberosPrincipal" };

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
        {
            Object altId = parameters.get(PARAM_ALTERNATIVE_ID);
            writer.writeUTF(PARAMETER_ALT_ID, null == altId ? null : altId.toString());
        }
        {
            List<String> remoteParameterNames = new LinkedList<String>();
            remoteParameterNames.addAll(SessionStorageConfiguration.getInstance().getRemoteParameterNames());
            remoteParameterNames.addAll(Arrays.asList(PORTABLE_PARAMETERS));
            if (remoteParameterNames.isEmpty()) {
                writer.writeUTF(PARAMETER_REMOTE_PARAMETER_NAMES, null);
                writer.writeUTF(PARAMETER_REMOTE_PARAMETER_VALUES, null);
            } else {
                StringAppender names = new StringAppender(':');
                StringAppender values = new StringAppender(':');
                StringAppender serializableNames = new StringAppender(':');
                for (String parameterName : remoteParameterNames) {
                    Object value = parameters.get(parameterName);
                    if (null != value) {
                        if (isSerializablePojo(value)) {
                            String sValue = value.toString();
                            names.append(parameterName);
                            values.append(getSafeValue(sValue));
                        } else if (value instanceof Serializable) {
                            serializableNames.append(parameterName);
                            byte[] bytes = SerializationUtils.serialize((Serializable) value);
                            writer.writeByteArray(parameterName, bytes);
                        } else {
                            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PortableSession.class);
                            logger.warn("Denied remote parameter for name {}. Seems to be no ordinary Java object.", value.getClass().getName());
                        }
                    }
                }
                if (0 == names.length()) {
                    writer.writeUTF(PARAMETER_REMOTE_PARAMETER_NAMES, null);
                    writer.writeUTF(PARAMETER_REMOTE_PARAMETER_VALUES, null);
                } else {
                    writer.writeUTF(PARAMETER_REMOTE_PARAMETER_NAMES, names.toString());
                    writer.writeUTF(PARAMETER_REMOTE_PARAMETER_VALUES, values.toString());
                }
                if (0 == serializableNames.length()) {
                    writer.writeUTF(PARAMETER_SERIALIZABLE_PARAMETER_NAMES, null);
                } else {
                    writer.writeUTF(PARAMETER_SERIALIZABLE_PARAMETER_NAMES, serializableNames.toString());
                }
            }
        }
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
            String sNames = reader.readUTF(PARAMETER_REMOTE_PARAMETER_NAMES);
            if (null != sNames) {
                List<String> names = parseColonString(sNames);
                List<String> values = parseColonString(reader.readUTF(PARAMETER_REMOTE_PARAMETER_VALUES)); // Expect them, too
                for (int i = 0, size = names.size(); i < size; i++) {
                    try {
                        Object value = parseToSerializablePojo(decodeSafeValue(values.get(i)));
                        parameters.put(names.get(i), value);
                    } catch (DecoderException e) {
                        // Ignore
                    }
                }
            }
        }
        {
            String serializableNames = reader.readUTF(PARAMETER_SERIALIZABLE_PARAMETER_NAMES);
            if (null != serializableNames) {
                List<String> names = parseColonString(serializableNames);
                for (String name : names) {
                    ByteArrayInputStream bais = null;
                    ObjectInputStream ois = null;
                    try {
                        byte[] bytes = reader.readByteArray(name);
                        bais = new ByteArrayInputStream(bytes);
                        ois = new ObjectInputStream(bais);
                        Object value = ois.readObject();
                        parameters.put(name, value);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SerializationException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        if (null != ois) {
                            ois.close();
                        }
                        if (null != bais) {
                            bais.close();
                        }
                    }
                }
            }
        }
    }

    private static List<String> parseColonString(String str) {
        List<String> retval = new LinkedList<String>();
        int length = str.length();
        {
            int prev = 0;
            int pos;
            while (prev < length && (pos = str.indexOf(':', prev)) >= 0) {
                if (pos > 0) {
                    retval.add(str.substring(prev, pos));
                }
                prev = pos + 1;
            }
            if (prev < length) {
                retval.add(str.substring(prev));
            }
        }
        return retval;
    }

    private static String getSafeValue(String sValue) {
        return sValue.indexOf(':') < 0 ? sValue : Charsets.toAsciiString(URLCodec.encodeUrl(BIT_SET_PARAMS, sValue.getBytes(Charsets.UTF_8)));
    }

    private static String decodeSafeValue(String value) throws DecoderException {
        return value.indexOf('%') < 0 ? value : new String(URLCodec.decodeUrl(Charsets.toAsciiBytes(value)), Charsets.UTF_8);
    }

    private static final String POJO_PACKAGE = "java.lang.";

    private static boolean isSerializablePojo(Object obj) {
        return null == obj ? false : ((obj instanceof Serializable) && obj.getClass().getName().startsWith(POJO_PACKAGE));
    }

    private static Object parseToSerializablePojo(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }

        try {
            int i = Integer.parseInt(value, 10);
            return Integer.valueOf(i);
        } catch (Exception e) {
            // Ignore...
        }

        try {
            long l = Long.parseLong(value, 10);
            return Long.valueOf(l);
        } catch (Exception e) {
            // Ignore...
        }

        /*-
         *
        try {
            float f = Float.parseFloat(value);
            return new Float(f);
        } catch (Exception e) {
            // Ignore...
        }

        try {
            double d = Double.parseDouble(value);
            return new Double(d);
        } catch (Exception e) {
            // Ignore...
        }
        *
        */

        return value;
    }

}
