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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mailaccount;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.java.Charsets;

/**
 * {@link PlainPasswordProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class PlainPasswordProvider implements AutoCloseable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PlainPasswordProvider.class);

    // --------------------------------------------------------------------------------------------------------

    private final AtomicReference<ByteBuffer> plainPasswordRef;
    private final int size;

    /**
     * Initializes a new {@link PlainPasswordProvider}.
     */
    public PlainPasswordProvider(String plainPassword) {
        super();
        byte[] bytes = toBytes(plainPassword.toCharArray());
        ByteBuffer byteBuffer = obtainDirectByteBuffer(bytes.length);
        byteBuffer.mark();
        byteBuffer.put(bytes, 0, bytes.length);
        byteBuffer.reset();
        plainPasswordRef = new AtomicReference<ByteBuffer>(byteBuffer);
        size = bytes.length;
    }

    /**
     * Gets the plain password
     *
     * @return The plain password
     */
    public Password getPassword() {
        char[] plainPw = getCharsFromBuffer();
        return new Password(plainPw, Password.Type.PLAIN);
    }

    private char[] getCharsFromBuffer() {
        ByteBuffer byteBuffer = plainPasswordRef.get();
        byte[] bytes = new byte[size];
        byteBuffer.get(bytes, 0, size);
        byteBuffer.reset();
        char[] key = toChars(bytes);
        return key;
    }

    @Override
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }

    /**
     * Destroys this password instance.
     */
    public void destroy() {
        if (null != plainPasswordRef) {
            ByteBuffer directByteBuffer = plainPasswordRef.getAndSet(null);
            if (null != directByteBuffer) {
                releaseDirectByteBuffer(directByteBuffer);
            }
        }
    }

    @Override
    public void close() throws Exception {
        destroy();
    }

    // -------------------------------------------- Helpers ------------------------------------------------------

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

}
