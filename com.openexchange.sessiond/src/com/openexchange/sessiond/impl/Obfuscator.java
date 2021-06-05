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

package com.openexchange.sessiond.impl;

import static com.openexchange.java.Strings.isEmpty;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.crypto.CryptoService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.session.ObfuscatorService;
import com.openexchange.session.Session;
import com.openexchange.sessiond.osgi.Services;
import com.openexchange.sessionstorage.StoredSession;

/**
 * {@link Obfuscator}
 *
 * Utility class to wrap/unwrap sessions before/after putting/getting them from the session storage.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Obfuscator implements ObfuscatorService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SessionImpl.class);

    private static boolean useDirectByteBuffer() {
        return false;
    }

    static ByteBuffer obtainDirectByteBuffer(int size) {
        return ByteBuffer.allocateDirect(size);
    }

    static void releaseDirectByteBuffer(ByteBuffer directByteBuffer) {
        directByteBuffer.clear();
        destroyBuffer(directByteBuffer);
    }

    static void destroyBuffer(Buffer buffer) {
        if (buffer.isDirect()) {
            try {
                if (!buffer.getClass().getName().equals("java.nio.DirectByteBuffer")) {
                    Field attField = buffer.getClass().getDeclaredField("att");
                    attField.setAccessible(true);
                    buffer = (Buffer) attField.get(buffer);
                }

                Method cleanerMethod = buffer.getClass().getMethod("cleaner");
                cleanerMethod.setAccessible(true);
                Object cleaner = cleanerMethod.invoke(buffer);
                Method cleanMethod = cleaner.getClass().getMethod("clean");
                cleanMethod.setAccessible(true);
                cleanMethod.invoke(cleaner);
            } catch (Exception e) {
                LOG.error("Could not destroy direct buffer {}", buffer, e);
            }
        }
    }

    private static byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000');// clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0);// clear sensitive data
        return bytes;
    }

    private static char[] toChars(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        CharBuffer charBuffer = Charsets.UTF_8.decode(byteBuffer);
        char[] chars = Arrays.copyOfRange(charBuffer.array(), charBuffer.position(), charBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0);// clear sensitive data
        Arrays.fill(charBuffer.array(), '\u0000');// clear sensitive data
        return chars;
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    private final String obfuscationKey;
    private final AtomicReference<ByteBuffer> obfuscationKeyRef;
    private final int size;

    /**
     * Initializes a new {@link Obfuscator}.
     *
     * @param obfuscationKey The key used to (un)obfuscate session passwords
     */
    public Obfuscator(char[] obfuscationKey) {
        super();
        if (useDirectByteBuffer()) {
            byte[] bytes = toBytes(obfuscationKey);
            ByteBuffer byteBuffer = obtainDirectByteBuffer(bytes.length);
            byteBuffer.mark();
            byteBuffer.put(bytes, 0, bytes.length);
            byteBuffer.reset();
            this.obfuscationKeyRef = new AtomicReference<ByteBuffer>(byteBuffer);
            this.size = bytes.length;
            this.obfuscationKey = null;
        } else {
            this.obfuscationKeyRef = null;
            this.size = 0;
            this.obfuscationKey = new String(obfuscationKey);
        }
    }

    private char[] getCharsFromBuffer() {
        ByteBuffer byteBuffer = obfuscationKeyRef.get();
        byte[] bytes = new byte[size];
        byteBuffer.get(bytes, 0, size);
        byteBuffer.reset();
        char[] key = toChars(bytes);
        return key;
    }

    /**
     * Destroys this obfuscator.
     */
    public void destroy() {
        if (null != obfuscationKeyRef) {
            ByteBuffer directByteBuffer = obfuscationKeyRef.getAndSet(null);
            if (null != directByteBuffer) {
                releaseDirectByteBuffer(directByteBuffer);
            }
        }
    }

    /**
     * Wraps a session before putting it to the storage.
     *
     * @param session The session
     * @return the wrapped session
     */
    public Session wrap(SessionImpl session) {
        if (null == session) {
            return null;
        }

        Map<String, Object> parameters = new HashMap<String, Object>(2);
        for (String name : session.getParameterNames()) {
            parameters.put(name, session.getParameter(name));
        }

        // Instantiate & return appropriate stored session
        return new StoredSession(session.getSessionID(), session.getLoginName(), obfuscate(session.getPassword()), session.getContextId(), session.getUserId(), session.getSecret(), session.getLogin(), session.getRandomToken(), session.getLocalIp(), session.getAuthId(), session.getHash(), session.getClient(), session.isStaySignedIn(), session.getOrigin(), parameters);
    }

    /**
     * Unwraps a session after getting it from the storage.
     *
     * @param session The session
     * @return The unwrapped session
     */
    public SessionImpl unwrap(Session session) {
        if (null == session) {
            return null;
        }

        // Instantiate session
        SessionImpl sessionImpl = new SessionImpl(session.getUserId(), session.getLoginName(), unobfuscate(session.getPassword()), session.getContextId(), session.getSessionID(), session.getSecret(), session.getRandomToken(), session.getLocalIp(), session.getLogin(), session.getAuthId(), session.getHash(), session.getClient(), false, session.isStaySignedIn(), session.getOrigin());
        for (String name : session.getParameterNames()) {
            Object value = session.getParameter(name);
            sessionImpl.setParameter(name, value);
            LOG.debug("Restored remote parameter '{}' with value '{}' for session {} ({}@{})", name, value, session.getSessionID(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
        }

        // Return
        return sessionImpl;
    }

    @Override
    public String obfuscate(final String string) {
        if (isEmpty(string)) {
            return string;
        }

        String obfuscationKey = this.obfuscationKey;
        if (null != obfuscationKey) {
            try {
                return Services.getService(CryptoService.class).encrypt(string, obfuscationKey);
            } catch (OXException e) {
                LOG.error("Could not obfuscate string", e);
                return string;
            }
        }

        char[] key = getCharsFromBuffer();
        try {
            return Services.getService(CryptoService.class).encrypt(string, new String(key));
        } catch (OXException e) {
            LOG.error("Could not obfuscate string", e);
            return string;
        } finally {
            for (int i = key.length; i-- > 0;) {
                key[i] = '\u0000';
            }
        }
    }

    @Override
    public String unobfuscate(final String string) {
        if (isEmpty(string)) {
            return string;
        }

        String obfuscationKey = this.obfuscationKey;
        if (null != obfuscationKey) {
            try {
                return Services.getService(CryptoService.class).decrypt(string, obfuscationKey);
            } catch (OXException e) {
                LOG.error("Could not unobfuscate string", e);
                return string;
            }
        }

        char[] key = getCharsFromBuffer();
        try {
            return Services.getService(CryptoService.class).decrypt(string, new String(key));
        } catch (OXException e) {
            LOG.error("Could not unobfuscate string", e);
            return string;
        } finally {
            for (int i = key.length; i-- > 0;) {
                key[i] = '\u0000';
            }
        }
    }

}
