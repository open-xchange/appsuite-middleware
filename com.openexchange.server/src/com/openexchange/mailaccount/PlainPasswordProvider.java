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
    private final char[] plainPassword;

    /**
     * Initializes a new {@link PlainPasswordProvider}.
     */
    public PlainPasswordProvider(String plainPassword, boolean useDirectByteBuffer) {
        super();
        if (useDirectByteBuffer) {
            byte[] bytes = toBytes(plainPassword.toCharArray());
            ByteBuffer byteBuffer = obtainDirectByteBuffer(bytes.length);
            byteBuffer.mark();
            byteBuffer.put(bytes, 0, bytes.length);
            byteBuffer.reset();
            plainPasswordRef = new AtomicReference<ByteBuffer>(byteBuffer);
            size = bytes.length;
            this.plainPassword = null;
        } else {
            this.plainPassword = plainPassword.toCharArray();
            plainPasswordRef = null;
            size = 0;
        }
    }

    /**
     * Gets the plain password
     *
     * @return The plain password
     */
    public Password getPassword() {
        char[] plainPw = getChars();
        return null == plainPw ? null : new Password(plainPw, Password.Type.PLAIN);
    }

    private char[] getChars() {
        ByteBuffer byteBuffer = null == plainPasswordRef ? null : plainPasswordRef.get();
        if (null == byteBuffer) {
            return clone(this.plainPassword);
        }

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

    private static char[] clone(char[] data) {
        if (data == null) {
            return null;
        }

        char[] copy = new char[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        return copy;
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

}
