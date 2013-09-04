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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajp13.coyote.util;

import java.io.IOException;

/**
 * {@link ByteChunk}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ByteChunk {

    /**
     * Input interface, used when the buffer is empty. Same as java.nio.channel.ReadableByteChannel
     */
    public static interface ByteInputChannel {

        /**
         * Read new bytes ( usually the internal conversion buffer ). The implementation is allowed to ignore the parameters, and mutate the
         * chunk if it wishes to implement its own buffering.
         */
        public int realReadBytes(byte cbuf[], int off, int len) throws IOException;
    }

    /**
     * Same as java.nio.channel.WrittableByteChannel.
     */
    public static interface ByteOutputChannel {

        /**
         * Send the bytes ( usually the internal conversion buffer ). Expect 8k output if the buffer is full.
         */
        public void realWriteBytes(byte cbuf[], int off, int len) throws IOException;
    }

    // --------------------

    /**
     * Default encoding used to convert to strings. It should be UTF8, as most standards seem to converge, but the servlet API requires
     * 8859_1, and this object is used mostly for servlets.
     */
    public static final String DEFAULT_CHARACTER_ENCODING = "ISO-8859-1";

    // byte[]
    private byte[] buff;

    private int start = 0;

    private int end;

    private String enc;

    private boolean isSet = false; // XXX

    // How much can it grow, when data is added
    private int limit = -1;

    private ByteInputChannel in = null;

    private ByteOutputChannel out = null;

    private boolean isOutput = false;

    private boolean optimizedWrite = true;

    /**
     * Creates a new, uninitialized ByteChunk object.
     */
    public ByteChunk() {
        super();
    }

    public ByteChunk(final int initial) {
        this();
        allocate(initial, -1);
    }

    // --------------------
    public ByteChunk getClone() {
        try {
            return (ByteChunk) this.clone();
        } catch (final Exception ex) {
            return null;
        }
    }

    public boolean isNull() {
        return !isSet; // buff==null;
    }

    /**
     * Resets the message buff to an uninitialized state.
     */
    public void recycle() {
        // buff = null;
        enc = null;
        start = 0;
        end = 0;
        isSet = false;
    }

    public void reset() {
        buff = null;
    }

    // -------------------- Setup --------------------

    public void allocate(final int initial, final int limit) {
        isOutput = true;
        if (buff == null || buff.length < initial) {
            buff = new byte[initial];
        }
        this.limit = limit;
        start = 0;
        end = 0;
        isSet = true;
    }

    /**
     * Sets the message bytes to the specified subarray of bytes.
     *
     * @param b the ascii bytes
     * @param off the start offset of the bytes
     * @param len the length of the bytes
     */
    public void setBytes(final byte[] b, final int off, final int len) {
        buff = b;
        start = off;
        end = start + len;
        isSet = true;
    }

    public void setOptimizedWrite(final boolean optimizedWrite) {
        this.optimizedWrite = optimizedWrite;
    }

    public void setEncoding(final String enc) {
        this.enc = enc;
    }

    public String getEncoding() {
        if (enc == null) {
            enc = DEFAULT_CHARACTER_ENCODING;
        }
        return enc;
    }

    /**
     * Returns the message bytes.
     */
    public byte[] getBytes() {
        return getBuffer();
    }

    /**
     * Returns the message bytes.
     */
    public byte[] getBuffer() {
        return buff;
    }

    /**
     * Returns the start offset of the bytes. For output this is the end of the buffer.
     */
    public int getStart() {
        return start;
    }

    public int getOffset() {
        return start;
    }

    public void setOffset(final int off) {
        if (end < off) {
            end = off;
        }
        start = off;
    }

    /**
     * Returns the length of the bytes. XXX need to clean this up
     */
    public int getLength() {
        return end - start;
    }

    /**
     * Maximum amount of data in this buffer. If -1 or not set, the buffer will grow undefinitely. Can be smaller than the current buffer
     * size ( which will not shrink ). When the limit is reached, the buffer will be flushed ( if out is set ) or throw exception.
     */
    public void setLimit(final int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

    /**
     * When the buffer is empty, read the data from the input channel.
     */
    public void setByteInputChannel(final ByteInputChannel in) {
        this.in = in;
    }

    /**
     * When the buffer is full, write the data to the output channel. Also used when large amount of data is appended. If not set, the
     * buffer will grow to the limit.
     */
    public void setByteOutputChannel(final ByteOutputChannel out) {
        this.out = out;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(final int i) {
        end = i;
    }

    // -------------------- Adding data to the buffer --------------------
    /**
     * Append a char, by casting it to byte. This IS NOT intended for unicode.
     *
     * @param c
     * @throws IOException
     */
    public void append(final char c) throws IOException {
        append((byte) c);
    }

    public void append(final byte b) throws IOException {
        makeSpace(1);

        // couldn't make space
        if (limit > 0 && end >= limit) {
            flushBuffer();
        }
        buff[end++] = b;
    }

    public void append(final ByteChunk src) throws IOException {
        append(src.getBytes(), src.getStart(), src.getLength());
    }

    /**
     * Add data to the buffer
     */
    public void append(final byte src[], final int off, final int len) throws IOException {
        // will grow, up to limit
        makeSpace(len);

        // if we don't have limit: makeSpace can grow as it wants
        if (limit < 0) {
            // assert: makeSpace made enough space
            System.arraycopy(src, off, buff, end, len);
            end += len;
            return;
        }

        // Optimize on a common case.
        // If the buffer is empty and the source is going to fill up all the
        // space in buffer, may as well write it directly to the output,
        // and avoid an extra copy
        if (out != null && optimizedWrite && len == limit && end == start) {
            out.realWriteBytes(src, off, len);
            return;
        }
        // if we have limit and we're below
        if (len <= limit - end) {
            // makeSpace will grow the buffer to the limit,
            // so we have space
            System.arraycopy(src, off, buff, end, len);
            end += len;
            return;
        }

        // need more space than we can afford, need to flush
        // buffer

        // the buffer is already at ( or bigger than ) limit

        // We chunk the data into slices fitting in the buffer limit, although
        // if the data is written directly if it doesn't fit

        final int avail = limit - end;
        System.arraycopy(src, off, buff, end, avail);
        end += avail;

        flushBuffer();

        int remain = len - avail;

        while (remain > (limit - end)) {
            out.realWriteBytes(src, (off + len) - remain, limit - end);
            remain = remain - (limit - end);
        }

        System.arraycopy(src, (off + len) - remain, buff, end, remain);
        end += remain;

    }

    /**
     * Append bytes.
     */
    public void appendBytes(final byte src[], final int off, final int len) {
        // will grow, up to limit
        makeSpace(len);

        System.arraycopy(src, off, buff, end, len);
        end += len;
    }

    // -------------------- Removing data from the buffer --------------------

    public int substract() throws IOException {

        if ((end - start) == 0) {
            if (in == null) {
                return -1;
            }
            final int n = in.realReadBytes(buff, 0, buff.length);
            if (n < 0) {
                return -1;
            }
        }

        return (buff[start++] & 0xFF);

    }

    public int substract(final ByteChunk src) throws IOException {

        if ((end - start) == 0) {
            if (in == null) {
                return -1;
            }
            final int n = in.realReadBytes(buff, 0, buff.length);
            if (n < 0) {
                return -1;
            }
        }

        final int len = getLength();
        src.append(buff, start, len);
        start = end;
        return len;

    }

    public int substract(final byte src[], final int off, final int len) throws IOException {

        if ((end - start) == 0) {
            if (in == null) {
                return -1;
            }
            final int n = in.realReadBytes( buff, 0, buff.length );
            if (n < 0) {
                return -1;
            }
        }

        int n = len;
        final int length = getLength();
        if (len > length) {
            n = length;
        }
        System.arraycopy(buff, start, src, off, n);
        start += n;
        return n;

    }

    /**
     * Send the buffer to the sink. Called by append() when the limit is reached. You can also call it explicitely to force the data to be
     * written.
     *
     * @throws IOException
     */
    public void flushBuffer() throws IOException {
        // assert out!=null
        if (out == null) {
            throw new IOException("Buffer overflow, no sink " + limit + " " + buff.length);
        }
        out.realWriteBytes(buff, start, end - start);
        end = start;
    }

    /**
     * Make space for len chars. If len is small, allocate a reserve space too. Never grow bigger than limit.
     */
    private void makeSpace(final int count) {
        byte[] tmp = null;

        int newSize;
        int desiredSize = end + count;

        // Can't grow above the limit
        if (limit > 0 && desiredSize > limit) {
            desiredSize = limit;
        }

        if (buff == null) {
            if (desiredSize < 256) {
                desiredSize = 256; // take a minimum
            }
            buff = new byte[desiredSize];
        }

        // limit < buf.length ( the buffer is already big )
        // or we already have space XXX
        if (desiredSize <= buff.length) {
            return;
        }
        // grow in larger chunks
        if (desiredSize < 2 * buff.length) {
            newSize = buff.length * 2;
            if (limit > 0 && newSize > limit) {
                newSize = limit;
            }
            tmp = new byte[newSize];
        } else {
            newSize = buff.length * 2 + count;
            if (limit > 0 && newSize > limit) {
                newSize = limit;
            }
            tmp = new byte[newSize];
        }

        System.arraycopy(buff, start, tmp, 0, end - start);
        buff = tmp;
        tmp = null;
        end = end - start;
        start = 0;
    }

    // -------------------- Conversion and getters --------------------

    @Override
    public String toString() {
        if (null == buff) {
            return null;
        } else if (end - start == 0) {
            return "";
        }
        return StringCache.toString(this);
    }

    public String toStringInternal() {
        String strValue = null;
        try {
            if (enc == null) {
                enc = DEFAULT_CHARACTER_ENCODING;
            }
            strValue = new String(buff, start, end - start, enc);
            /*
             * Does not improve the speed too much on most systems, it's safer to use the "clasical" new String(). Most overhead is in
             * creating char[] and copying, the internal implementation of new String() is very close to what we do. The decoder is nice for
             * large buffers and if we don't go to String ( so we can take advantage of reduced GC) // Method is commented out, in: return
             * B2CConverter.decodeString( enc );
             */
        } catch (final java.io.UnsupportedEncodingException e) {
            // Use the platform encoding in that case; the usage of a bad
            // encoding will have been logged elsewhere already
            strValue = new String(buff, start, end - start);
        }
        return strValue;
    }

    public int getInt() {
        return Ascii.parseInt(buff, start, end - start);
    }

    public long getLong() {
        return Ascii.parseLong(buff, start, end - start);
    }

    // -------------------- equals --------------------

    /**
     * Compares the message bytes to the specified String object.
     *
     * @param s the String to compare
     * @return true if the comparison succeeded, false otherwise
     */
    public boolean equals(final String s) {
        // XXX ENCODING - this only works if encoding is UTF8-compat
        // ( ok for tomcat, where we compare ascii - header names, etc )!!!

        final byte[] b = buff;
        final int blen = end - start;
        if (b == null || blen != s.length()) {
            return false;
        }
        int boff = start;
        for (int i = 0; i < blen; i++) {
            if (b[boff++] != s.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the message bytes to the specified String object.
     *
     * @param s the String to compare
     * @return true if the comparison succeeded, false otherwise
     */
    public boolean equalsIgnoreCase(final String s) {
        final byte[] b = buff;
        final int blen = end - start;
        if (b == null || blen != s.length()) {
            return false;
        }
        int boff = start;
        for (int i = 0; i < blen; i++) {
            if (Ascii.toLower(b[boff++]) != Ascii.toLower(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(final ByteChunk bb) {
        return equals(bb.getBytes(), bb.getStart(), bb.getLength());
    }

    public boolean equals(final byte b2[], int off2, final int len2) {
        final byte b1[] = buff;
        if (b1 == null && b2 == null) {
            return true;
        }

        int len = end - start;
        if (len2 != len || b1 == null || b2 == null) {
            return false;
        }

        int off1 = start;

        while (len-- > 0) {
            if (b1[off1++] != b2[off2++]) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(final char c2[], int off2, final int len2) {
        // XXX works only for enc compatible with ASCII/UTF !!!
        final byte b1[] = buff;
        if (c2 == null && b1 == null) {
            return true;
        }

        if (b1 == null || c2 == null || end - start != len2) {
            return false;
        }
        int off1 = start;
        int len = end - start;

        while (len-- > 0) {
            if ((char) b1[off1++] != c2[off2++]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the message bytes starts with the specified string.
     *
     * @param s the string
     */
    public boolean startsWith(final String s) {
        // Works only if enc==UTF
        final byte[] b = buff;
        final int blen = s.length();
        if (b == null || blen > end - start) {
            return false;
        }
        int boff = start;
        for (int i = 0; i < blen; i++) {
            if (b[boff++] != s.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /* Returns true if the message bytes start with the specified byte array */
    public boolean startsWith(final byte[] b2) {
        final byte[] b1 = buff;
        if (b1 == null && b2 == null) {
            return true;
        }

        final int len = end - start;
        if (b1 == null || b2 == null || b2.length > len) {
            return false;
        }
        for (int i = start, j = 0; i < end && j < b2.length;) {
            if (b1[i++] != b2[j++]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the message bytes starts with the specified string.
     *
     * @param s the string
     * @param pos The position
     */
    public boolean startsWithIgnoreCase(final String s, final int pos) {
        final byte[] b = buff;
        final int len = s.length();
        if (b == null || len + pos > end - start) {
            return false;
        }
        int off = start + pos;
        for (int i = 0; i < len; i++) {
            if (Ascii.toLower(b[off++]) != Ascii.toLower(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public int indexOf(final String src, final int srcOff, final int srcLen, final int myOff) {
        final char first = src.charAt(srcOff);

        // Look for first char
        final int srcEnd = srcOff + srcLen;

        for (int i = myOff + start; i <= (end - srcLen); i++) {
            if (buff[i] != first) {
                continue;
            }
            // found first char, now look for a match
            int myPos = i + 1;
            for (int srcPos = srcOff + 1; srcPos < srcEnd;) {
                if (buff[myPos++] != src.charAt(srcPos++)) {
                    break;
                }
                if (srcPos == srcEnd) {
                    return i - start; // found it
                }
            }
        }
        return -1;
    }

    // -------------------- Hash code --------------------

    // normal hash.
    public int hash() {
        return hashBytes(buff, start, end - start);
    }

    // hash ignoring case
    public int hashIgnoreCase() {
        return hashBytesIC(buff, start, end - start);
    }

    private static int hashBytes(final byte buff[], final int start, final int bytesLen) {
        final int max = start + bytesLen;
        final byte bb[] = buff;
        int code = 0;
        for (int i = start; i < max; i++) {
            code = code * 37 + bb[i];
        }
        return code;
    }

    private static int hashBytesIC(final byte bytes[], final int start, final int bytesLen) {
        final int max = start + bytesLen;
        final byte bb[] = bytes;
        int code = 0;
        for (int i = start; i < max; i++) {
            code = code * 37 + Ascii.toLower(bb[i]);
        }
        return code;
    }

    /**
     * Returns true if the message bytes starts with the specified string.
     *
     * @param c the character
     * @param starting The start position
     */
    public int indexOf(final char c, final int starting) {
        final int ret = indexOf(buff, start + starting, end, c);
        return (ret >= start) ? ret - start : -1;
    }

    public static int indexOf(final byte bytes[], int off, final int end, final char qq) {
        // Works only for UTF
        while (off < end) {
            final byte b = bytes[off];
            if (b == qq) {
                return off;
            }
            off++;
        }
        return -1;
    }

    /**
     * Find a character, no side effects.
     *
     * @return index of char if found, -1 if not
     */
    public static int findChar(final byte buf[], final int start, final int end, final char c) {
        final byte b = (byte) c;
        int offset = start;
        while (offset < end) {
            if (buf[offset] == b) {
                return offset;
            }
            offset++;
        }
        return -1;
    }

    /**
     * Find a character, no side effects.
     *
     * @return index of char if found, -1 if not
     */
    public static int findChars(final byte buf[], final int start, final int end, final byte c[]) {
        final int clen = c.length;
        int offset = start;
        while (offset < end) {
            for (int i = 0; i < clen; i++) {
                if (buf[offset] == c[i]) {
                    return offset;
                }
            }
            offset++;
        }
        return -1;
    }

    /**
     * Find the first character != c
     *
     * @return index of char if found, -1 if not
     */
    public static int findNotChars(final byte buf[], final int start, final int end, final byte c[]) {
        final int clen = c.length;
        int offset = start;
        boolean found;

        while (offset < end) {
            found = true;
            for (int i = 0; i < clen; i++) {
                if (buf[offset] == c[i]) {
                    found = false;
                    break;
                }
            }
            if (found) { // buf[offset] != c[0..len]
                return offset;
            }
            offset++;
        }
        return -1;
    }

    /**
     * Convert specified String to a byte array. This ONLY WORKS for ascii, UTF chars will be truncated.
     *
     * @param value to convert to byte array
     * @return the byte array value
     */
    public static final byte[] convertToBytes(final String value) {
        final byte[] result = new byte[value.length()];
        for (int i = 0; i < value.length(); i++) {
            result[i] = (byte) value.charAt(i);
        }
        return result;
    }

    /**
     * Gets a {@link ByteChunk} carrying specified single byte.
     *
     * @param b The byte
     * @return The {@link ByteChunk} instance
     */
    public static ByteChunk valueOf(final int b) {
        final ByteChunk bc = new ByteChunk(1);
        bc.buff[bc.end++] = (byte) b;
        return bc;
    }

}
