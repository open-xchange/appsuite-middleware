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

package com.openexchange.sessiond.impl;

import static com.openexchange.java.Strings.isEmpty;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
     * @param remoteParameterNames The names of such parameters that are supposed to be taken over from session to stored session representation
     * @return the wrapped session
     */
    public Session wrap(SessionImpl session, List<String> remoteParameterNames) {
        if (null == session) {
            return null;
        }

        // Initialize its parameters
        Map<String, Object> parameters = new HashMap<String, Object>(2);
        for (String name : session.getParameterNames()) {
            parameters.put(name, session.getParameter(name));
        }

        // Maintain remote parameters
        if (null != remoteParameterNames) {
            for (String parameterName : remoteParameterNames) {
                Object value = session.getParameter(parameterName);
                if (null != value) {
                    parameters.put(parameterName, value);
                }
            }
        }

        // Instantiate & return appropriate stored session
        return new StoredSession(session.getSessionID(), session.getLoginName(), obfuscate(session.getPassword()), session.getContextId(), session.getUserId(), session.getSecret(), session.getLogin(), session.getRandomToken(), session.getLocalIp(), session.getAuthId(), session.getHash(), session.getClient(), parameters);
    }

    /**
     * Unwraps a session after getting it from the storage.
     *
     * @param session The session
     * @param remoteParameterNames The names of such parameters that are supposed to be taken over from session to stored session representation
     * @return The unwrapped session
     */
    public SessionImpl unwrap(Session session, List<String> remoteParameterNames) {
        if (null == session) {
            return null;
        }

        // Instantiate session
        SessionImpl sessionImpl = new SessionImpl(session.getUserId(), session.getLoginName(), unobfuscate(session.getPassword()), session.getContextId(), session.getSessionID(), session.getSecret(), session.getRandomToken(), session.getLocalIp(), session.getLogin(), session.getAuthId(), session.getHash(), session.getClient(), false);
        for (String name : session.getParameterNames()) {
            sessionImpl.setParameter(name, session.getParameter(name));
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
            } catch (final OXException e) {
                LOG.error("Could not obfuscate string", e);
                return string;
            }
        }

        char[] key = getCharsFromBuffer();
        try {
            return Services.getService(CryptoService.class).encrypt(string, new String(key));
        } catch (final OXException e) {
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
            } catch (final OXException e) {
                LOG.error("Could not obfuscate string", e);
                return string;
            }
        }

        char[] key = getCharsFromBuffer();
        try {
            return Services.getService(CryptoService.class).decrypt(string, new String(key));
        } catch (final OXException e) {
            LOG.error("Could not unobfuscate string", e);
            return string;
        } finally {
            for (int i = key.length; i-- > 0;) {
                key[i] = '\u0000';
            }
        }
    }

}
