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

package com.openexchange.service.messaging.internal.receipt;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.openexchange.service.messaging.Message;
import com.openexchange.exception.OXException;
import com.openexchange.service.messaging.MessagingServiceExceptionCode;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link MessagingContiguousMessage} - A contiguous message.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessagingContiguousMessage {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MessagingContiguousMessage.class));

    private final String topic;

    private final List<byte[]> chunks;

    private int size;

    /**
     * Initializes a new {@link MessagingContiguousMessage}.
     *
     * @param topic The message topic
     * @param firstChunk The first data chunk
     */
    public MessagingContiguousMessage(final String topic, final byte[] firstChunk) {
        super();
        this.topic = topic;
        chunks = new ArrayList<byte[]>(2);
        chunks.add(firstChunk);
        size = firstChunk.length;
    }

    /**
     * Atomically adds given bytes to this contiguous message.
     *
     * @param cnum The chunk number
     * @param chunk The chunk to add
     */
    public void add(final int cnum, final byte[] chunk) {
        synchronized (chunks) {
            final int off = cnum - 1;
            if ((off < 0) || (off > chunks.size())) {
                chunks.add(chunk);
            } else {
                chunks.add(off, chunk);
            }
            size += chunk.length;
        }
    }

    /**
     * Converts this contiguous message into a {@link Message} instance.
     * <p>
     * This method may only be invoked if contiguous message has completed gathering data.
     *
     * @return The resulting {@link Message} instance
     * @throws OXException If conversion fails
     */
    public Message toMessage() throws OXException {
        synchronized (chunks) {
            final ObjectInputStream ois;
            try {
                final ByteBuffer bb = new ByteBuffer(size);
                for (final byte[] chunk : chunks) {
                    bb.append(chunk);
                }
                ois = new ObjectInputStream(new UnsynchronizedByteArrayInputStream(bb.getBytes()));
            } catch (final IOException e) {
                throw MessagingServiceExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
            try {
                @SuppressWarnings("unchecked") final Map<String, Serializable> properties = (Map<String, Serializable>) ois.readObject();
                return new Message(topic, properties);
            } catch (final IOException e) {
                throw MessagingServiceExceptionCode.IO_ERROR.create(e, e.getMessage());
            } catch (final ClassNotFoundException e) {
                throw MessagingServiceExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                try {
                    ois.close();
                } catch (final IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    private static final class ByteBuffer {

        private byte[] bytes;

        private int off;

        public ByteBuffer(final int capacity) {
            super();
            this.bytes = new byte[capacity];
            off = 0;
        }

        public void append(final byte[] toAppend) {
            final int remaining = bytes.length - off;
            if (toAppend.length > remaining) {
                final byte[] tmp = bytes;
                bytes = new byte[tmp.length + toAppend.length - remaining];
                System.arraycopy(tmp, 0, bytes, 0, tmp.length);
                System.arraycopy(toAppend, 0, bytes, off, remaining);
                System.arraycopy(toAppend, remaining, bytes, tmp.length, toAppend.length - remaining);
                off = bytes.length;
            } else {
                System.arraycopy(toAppend, 0, bytes, off, toAppend.length);
                off += toAppend.length;
            }
        }

        public byte[] getBytes() {
            final int remaining = bytes.length - off;
            if (remaining > 0) {
                final byte[] ret = new byte[off];
                System.arraycopy(bytes, 0, ret, 0, off);
                return ret;
            }
            return bytes;
        }

    }
}
