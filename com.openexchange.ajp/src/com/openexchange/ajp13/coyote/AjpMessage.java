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

package com.openexchange.ajp13.coyote;

import java.io.IOException;
import javax.mail.MessagingException;
import com.openexchange.ajp13.coyote.util.ByteChunk;
import com.openexchange.ajp13.coyote.util.CharChunk;
import com.openexchange.ajp13.coyote.util.MessageBytes;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.log.Log;
import com.openexchange.tools.codec.QuotedPrintable;

/**
 * {@link AjpMessage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AjpMessage {

    private static final org.apache.commons.logging.Log log = Log.valueOf(com.openexchange.log.LogFactory.getLog(AjpMessage.class));

    // ------------------------------------------------------------ Constructor

    public AjpMessage(final int packetSize) {
        buf = new byte[packetSize];
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * Fixed size buffer.
     */
    protected byte buf[] = null;

    /**
     * The current read or write position in the buffer.
     */
    protected int pos;

    /**
     * This actually means different things depending on whether the packet is read or write. For read, it's the length of the payload
     * (excluding the header). For write, it's the length of the packet as a whole (counting the header). Oh, well.
     */
    protected int len;

    // --------------------------------------------------------- Public Methods

    /**
     * Prepare this packet for accumulating a message from the container to the web server. Set the write position to just after the header
     * (but leave the length unwritten, because it is as yet unknown).
     */
    public void reset() {
        len = 4;
        pos = 4;
    }

    /**
     * For a packet to be sent to the web server, finish the process of accumulating data and write the length of the data payload into the
     * header.
     */
    public void end() {
        len = pos;
        final int dLen = len - 4;

        buf[0] = (byte) 0x41;
        buf[1] = (byte) 0x42;
        buf[2] = (byte) ((dLen >>> 8) & 0xFF);
        buf[3] = (byte) (dLen & 0xFF);
    }

    /**
     * Return the underlying byte buffer.
     */
    public byte[] getBuffer() {
        return buf;
    }

    /**
     * Return the current message length. For read, it's the length of the payload (excluding the header). For write, it's the length of the
     * packet as a whole (counting the header).
     */
    public int getLen() {
        return len;
    }

    /**
     * Add a short integer (2 bytes) to the message.
     */
    public void appendInt(final int val) {
        buf[pos++] = (byte) ((val >>> 8) & 0xFF);
        buf[pos++] = (byte) (val & 0xFF);
    }

    /**
     * Append a byte (1 byte) to the message.
     */
    public void appendByte(final int val) {
        buf[pos++] = (byte) val;
    }

    /**
     * Append an int (4 bytes) to the message.
     */
    public void appendLongInt(final int val) {
        buf[pos++] = (byte) ((val >>> 24) & 0xFF);
        buf[pos++] = (byte) ((val >>> 16) & 0xFF);
        buf[pos++] = (byte) ((val >>> 8) & 0xFF);
        buf[pos++] = (byte) (val & 0xFF);
    }

    /**
     * Write a MessageBytes out at the current write position. A null MessageBytes is encoded as a string with length 0.
     */
    public void appendBytes(final MessageBytes mb) {
        if (mb == null) {
            log.error("ajpmessage.null", new NullPointerException());
            appendInt(0);
            appendByte(0);
            return;
        }
        if (mb.getType() == MessageBytes.T_BYTES) {
            final ByteChunk bc = mb.getByteChunk();
            appendByteChunk(bc);
        } else if (mb.getType() == MessageBytes.T_CHARS) {
            final CharChunk cc = mb.getCharChunk();
            appendCharChunk(cc);
        } else {
            appendString(mb.toString());
        }
    }

    /**
     * Write a ByteChunk out at the current write position. A null ByteChunk is encoded as a string with length 0.
     */
    public void appendByteChunk(final ByteChunk bc) {
        if (bc == null) {
            log.error("ajpmessage.null", new NullPointerException());
            appendInt(0);
            appendByte(0);
            return;
        }
        appendBytes(bc.getBytes(), bc.getStart(), bc.getLength());
    }

    /**
     * Write a CharChunk out at the current write position. A null CharChunk is encoded as a string with length 0.
     */
    public void appendCharChunk(final CharChunk cc) {
        if (cc == null) {
            log.error("ajpmessage.null", new NullPointerException());
            appendInt(0);
            appendByte(0);
            return;
        }
        final int start = cc.getStart();
        final int end = cc.getEnd();
        appendInt(end - start);
        final char[] cbuf = cc.getBuffer();
        for (int i = start; i < end; i++) {
            char c = cbuf[i];
            // Note: This is clearly incorrect for many strings,
            // but is the only consistent approach within the current
            // servlet framework. It must suffice until servlet output
            // streams properly encode their output.
            if ((c <= 31) && (c != 9)) {
                c = ' ';
            } else if (c == 127) {
                c = ' ';
            }
            appendByte(c);
        }
        appendByte(0);
    }

    /**
     * Write a String out at the current write position. Strings are encoded with the length in two bytes first, then the string, and then a
     * terminating \0 (which is <B>not</B> included in the encoded length). The terminator is for the convenience of the C code, where it
     * saves a round of copying. A null string is encoded as a string with length 0.
     */
    public void appendString(final String str) {
        if (str == null) {
            log.error("ajpmessage.null", new NullPointerException());
            appendInt(0);
            appendByte(0);
            return;
        }
        final int len = str.length();
        appendInt(len);
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            // Note: This is clearly incorrect for many strings,
            // but is the only consistent approach within the current
            // servlet framework. It must suffice until servlet output
            // streams properly encode their output.
            if ((c <= 31) && (c != 9)) {
                c = ' ';
            } else if (c == 127) {
                c = ' ';
            }
            appendByte(c);
        }
        appendByte(0);
    }

    /**
     * Copy a chunk of bytes into the packet, starting at the current write position. The chunk of bytes is encoded with the length in two
     * bytes first, then the data itself, and finally a terminating \0 (which is <B>not</B> included in the encoded length).
     *
     * @param b The array from which to copy bytes.
     * @param off The offset into the array at which to start copying
     * @param numBytes The number of bytes to copy.
     */
    public void appendBytes(final byte[] b, final int off, final int numBytes) {
        if (pos + numBytes + 3 > buf.length) {
            log.error("ajpmessage.overflow: numBytes=" + numBytes + ", pos=" + pos, new ArrayIndexOutOfBoundsException());
            if (log.isDebugEnabled()) {
                dump("Overflow/coBytes");
            }
            return;
        }
        appendInt(numBytes);
        System.arraycopy(b, off, buf, pos, numBytes);
        pos += numBytes;
        appendByte(0);
    }

    private static final int ASCII_LIMIT = 127;

    private static final String DEFAULT_ENCODING = ServerConfig.getProperty(Property.DefaultEncoding);

    /**
     * Reads a string from packet and advance the read position past it.
     *
     * @return The read string
     */
    public String getString(final StringBuilder builder) {
        final int strLength = getInt();
        if ((strLength == 0xFFFF) || (strLength == -1)) {
            /*
             * Special byte 0xFF indicates absence of current string value.
             */
            return "";
        }
        boolean encoded = false;
        final StringBuilder sb;
        if (null == builder) {
            sb = new StringBuilder(strLength);
        } else {
            sb = builder;
            sb.setLength(0);
        }
        for (int strIndex = 0; strIndex < strLength; strIndex++) {
            final int b = buf[pos++] & 0xFF;
            if (b > ASCII_LIMIT) { // non-ascii character
                encoded = true;
                sb.append('=').append(Integer.toHexString(b));
            } else {
                sb.append((char) b);
            }
        }
        pos++; // Skip the terminating \0
        if (encoded) {
            try {
                return QuotedPrintable.decodeString(sb.toString(), DEFAULT_ENCODING == null ? "UTF-8" : DEFAULT_ENCODING);
            } catch (final IOException e) {
                log.warn(e.getMessage(), e);
                return sb.toString();
            } catch (final MessagingException e) {
                log.warn(e.getMessage(), e);
                return sb.toString();
            }
        }
        return sb.toString();
    }

    /**
     * Read an integer from packet, and advance the read position past it. Integers are encoded as two unsigned bytes with the high-order
     * byte first, and, as far as I can tell, in little-endian order within each byte.
     */
    public int getInt() {
        return ((buf[pos++] & 0xFF) << 8) + (buf[pos++] & 0xFF);
    }

    public int peekInt() {
        return ((buf[pos] & 0xFF) << 8) + (buf[pos + 1] & 0xFF);
    }

    public byte getByte() {
        return buf[pos++];
    }

    public byte peekByte() {
        return buf[pos];
    }

    public void getBytes(final MessageBytes mb) {
        final int length = getInt();
        if ((length == 0xFFFF) || (length == -1)) {
            mb.recycle();
            return;
        }
        mb.setBytes(buf, pos, length);
        pos += length;
        pos++; // Skip the terminating \0
    }

    /**
     * Copy a chunk of bytes from the packet into an array and advance the read position past the chunk. See appendBytes() for details on
     * the encoding.
     *
     * @return The number of bytes copied.
     */
    public int getBytes(final byte[] dest) {
        final int length = getInt();
        if (pos + length > buf.length) {
            log.error("ajpmessage.read: length=" + length);
            return 0;
        }

        if ((length == 0xFFFF) || (length == -1)) {
            return 0;
        }

        System.arraycopy(buf, pos, dest, 0, length);
        pos += length;
        pos++; // Skip terminating \0
        return length;
    }

    /**
     * Read a 32 bits integer from packet, and advance the read position past it. Integers are encoded as four unsigned bytes with the
     * high-order byte first, and, as far as I can tell, in little-endian order within each byte.
     */
    public int getLongInt() {
        int b1 = buf[pos++] & 0xFF; // No swap, Java order
        b1 <<= 8;
        b1 |= (buf[pos++] & 0xFF);
        b1 <<= 8;
        b1 |= (buf[pos++] & 0xFF);
        b1 <<= 8;
        b1 |= (buf[pos++] & 0xFF);
        return b1;
    }

    public int getHeaderLength() {
        return 4;
    }

    public int getPacketSize() {
        return buf.length;
    }

    public int processHeader() {
        pos = 0;
        final int mark = getInt();
        len = getInt();
        // Verify message signature
        if ((mark != 0x1234) && (mark != 0x4142)) {
            log.error("ajpmessage.invalid: mark=" + mark);
            if (log.isDebugEnabled()) {
                dump("In: ");
            }
            return -1;
        }
        if (log.isDebugEnabled()) {
            log.debug("Received AJP message of length " + len + ". First payload byte is: " + buf[0]);
        }
        return len;
    }

    /**
     * Dump the contents of the message, prefixed with the given String.
     */
    public void dump(final String msg) {
        if (!log.isDebugEnabled()) {
            return;
        }
        final StringBuilder temp = new StringBuilder(8192).append(msg).append('\n');
        int max = pos;
        if (len + 4 > pos) {
            max = len + 4;
        }
        if (max > 1000) {
            max = 1000;
        }

        for (int j = 0; j < max; j += 16) {
            hexLine(buf, j, len, temp);
            temp.append('\n');
        }
        log.debug(temp.toString());
    }

    // ------------------------------------------------------ Protected Methods

    protected static void hexLine(final byte buf[], final int start, final int len, final StringBuilder sb) {
        for (int i = start; i < start + 16; i++) {
            if (i < len + 4) {
                sb.append(hex(buf[i]) + " ");
            } else {
                sb.append("   ");
            }
        }
        sb.append(" | ");
        for (int i = start; i < start + 16 && i < len + 4; i++) {
            if (!Character.isISOControl((char) buf[i])) {
                sb.append(new Character((char) buf[i]));
            } else {
                sb.append('.');
            }
        }
    }

    protected static String hex(final int x) {
        String hex = Integer.toHexString(x);
        if (hex.length() == 1) {
            hex = "0" + hex;
        }
        return hex.substring(hex.length() - 2);
    }

}
