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

package com.openexchange.service.messaging.internal.delivery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.service.messaging.Message;
import com.openexchange.service.messaging.MessagingServiceExceptionCode;
import com.openexchange.service.messaging.internal.Constants;
import com.openexchange.service.messaging.internal.MessagingRemoteServerProvider;
import com.openexchange.threadpool.RefusedExecutionBehavior;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

/**
 * {@link MessagingDatagramPoster}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessagingDatagramPoster {

    /**
     * The logger instance for this class.
     */
    static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MessagingDatagramPoster.class));

    private static final int MAX_INT_VALUE = 0xFFFF;

    private final DatagramSocket datagramSocket;

    private final InetAddress ownAddress;

    /**
     * Initializes a new {@link MessagingDatagramPoster}.
     *
     * @throws OXException If initialization fails
     */
    public MessagingDatagramPoster(final DatagramSocket datagramSocket) throws OXException {
        super();
        this.datagramSocket = datagramSocket;
        try {
            ownAddress = InetAddress.getLocalHost();
        } catch (final UnknownHostException e) {
            throw MessagingServiceExceptionCode.UNKNOWN_HOST.create(e, "localhost");
        }
    }

    private static final RefusedExecutionBehavior<Void> BEHAVIOR = CallerRunsBehavior.getInstance();

    /**
     * Posts the passed message.
     *
     * @throws OXException If posting fails
     */
    public void post(final Message message) throws OXException {
        final List<InetSocketAddress> servers = MessagingRemoteServerProvider.getInstance().getRemoteMessagingServers();
        final List<byte[]> chunks = createChunks(message);
        for (final InetSocketAddress server : servers) {
            /*
             * Don't post to local host
             */
            if (!ownAddress.equals(server.getAddress())) {
                final ThreadPoolService pool = ThreadPools.getThreadPool();
                if (null == pool) {
                    new PosterCallable(chunks, server, datagramSocket).call();
                } else {
                    pool.submit(
                        ThreadPools.task(new PosterCallable(chunks, server, datagramSocket), "MessagingWorker-"),
                        BEHAVIOR);
                }
            }
        }
    }

    private static final class PosterCallable implements Callable<Void> {

        private final List<byte[]> chunks;

        private final InetSocketAddress server;

        private final DatagramSocket datagramSocket;

        public PosterCallable(final List<byte[]> chunks, final InetSocketAddress server, final DatagramSocket datagramSocket) {
            super();
            this.chunks = chunks;
            this.server = server;
            this.datagramSocket = datagramSocket;
        }

        @Override
        public Void call() throws OXException {
            try {
                for (final byte[] chunk : chunks) {
                    datagramSocket.send(new DatagramPacket(chunk, chunk.length, server));
                }
                return null;
            } catch (final IOException e) {
                LOG.error("Posting message failed.", e);
                throw MessagingServiceExceptionCode.IO_ERROR.create(e, e.getMessage());
            } catch (final RuntimeException e) {
                LOG.error("Posting message failed.", e);
                throw MessagingServiceExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }

    } // End of PosterCallable class

    private static List<byte[]> createChunks(final Message message) throws OXException {
        try {
            /*
             * Get the payload to write
             */
            final byte[] payload;
            {
                final ByteArrayOutputStream baos = new com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream(
                    Constants.PACKAGE_LENGTH << 1);
                final ObjectOutputStream oos = new ObjectOutputStream(baos);
                try {
                    final Map<String, Serializable> p = new HashMap<String, Serializable>();
                    for (final Entry<String, Serializable> entry : message.getProperties().entrySet()) {
                        p.put(entry.getKey(), entry.getValue());
                    }
                    oos.writeObject(p);
                    oos.flush();
                } finally {
                    try {
                        oos.close();
                    } catch (final IOException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                payload = baos.toByteArray();
            }
            final int payloadLen = payload.length;
            /*
             * The UUID
             */
            final byte[] uuid = UUIDs.toByteArray(UUID.randomUUID());
            /*-
             * Divide into appropriate chunks
             * <magic><uuid><prefix-code><continuation-flag>
             */
            final byte[] topic = writeString(message.getTopic());
            {
                final int firstPackageConsumed = getFirstPackageConsumed(topic.length);
                if (payloadLen <= (Constants.PACKAGE_LENGTH - firstPackageConsumed)) {
                    /*
                     * Everything fits into one single datagram package
                     */
                    final byte[] full = new byte[firstPackageConsumed + payloadLen];
                    int off = 0;
                    off = appendMagicBytes(off, full);
                    off = appendUUIDBytes(uuid, off, full);
                    full[off++] = Constants.PREFIX_CODE_START;
                    full[off++] = 0; // No continuation flag
                    System.arraycopy(topic, 0, full, off, topic.length);
                    off += topic.length;
                    System.arraycopy(payload, 0, full, off, payloadLen);
                    return Collections.singletonList(full);
                }
            }
            /*
             * Split
             */
            final List<byte[]> dgrams = new ArrayList<byte[]>(8);
            int payloadOff;
            {
                int off = 0;
                final byte[] firstChunk = new byte[Constants.PACKAGE_LENGTH];
                off = appendMagicBytes(off, firstChunk);
                off = appendUUIDBytes(uuid, off, firstChunk);
                firstChunk[off++] = Constants.PREFIX_CODE_START;
                firstChunk[off++] = 1; // Continuation flag
                System.arraycopy(topic, 0, firstChunk, off, topic.length);
                off += topic.length;
                /*
                 * Number of bytes remaining in current chunk for payload data
                 */
                final int rem = Constants.PACKAGE_LENGTH - off;
                System.arraycopy(payload, 0, firstChunk, off, rem);
                dgrams.add(firstChunk);
                payloadOff = rem;
            }
            final int capacity = Constants.PACKAGE_LENGTH - (Constants.GEN_PREFIX_LENGTH + 2);
            int counter = 2;
            while ((payloadLen - payloadOff) > capacity) {
                final byte[] chunk = new byte[Constants.PACKAGE_LENGTH];
                int off = 0;
                off = appendMagicBytes(off, chunk);
                off = appendUUIDBytes(uuid, off, chunk);
                chunk[off++] = Constants.PREFIX_CODE_DATA;
                chunk[off++] = 1; // Continuation flag
                off = appendCounterBytes(counter++, off, chunk);
                final int rem = Constants.PACKAGE_LENGTH - off;
                System.arraycopy(payload, 0, chunk, off, rem);
                dgrams.add(chunk);
                payloadOff += rem;
            }
            /*
             * Last one
             */
            final byte[] lastChunk = new byte[Constants.PACKAGE_LENGTH];
            int off = 0;
            off = appendMagicBytes(off, lastChunk);
            off = appendUUIDBytes(uuid, off, lastChunk);
            lastChunk[off++] = Constants.PREFIX_CODE_DATA;
            lastChunk[off++] = 0; // No continuation flag
            off = appendCounterBytes(counter, off, lastChunk);
            final int rem = Constants.PACKAGE_LENGTH - off;
            System.arraycopy(payload, 0, lastChunk, off, rem);
            dgrams.add(lastChunk);
            return dgrams;
        } catch (final IOException e) {
            throw MessagingServiceExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private static int appendMagicBytes(final int offset, final byte[] dest) {
        final int[] magic = Constants.MAGIC;
        int off = offset;
        for (int i = 0; i < magic.length; i++) {
            dest[off++] = (byte) magic[i];
        }
        return off;
    }

    private static int appendUUIDBytes(final byte[] uuid, final int offset, final byte[] dest) {
        System.arraycopy(uuid, 0, dest, offset, uuid.length);
        return offset + uuid.length;
    }

    private static int appendCounterBytes(final int count, final int offset, final byte[] dest) throws OXException {
        final byte[] cbytes = writeInt(count);
        System.arraycopy(cbytes, 0, dest, offset, cbytes.length);
        return offset + cbytes.length;
    }

    /**
     * Calculates the number of consumed bytes of the first package without payload data.
     *
     * <pre>
     * &lt;magic&gt;&lt;uuid&gt;&lt;prefix-code&gt;&lt;continuation-flag&gt;&lt;topic&gt;
     * </pre>
     *
     * @param topicByteCount The number of bytes needed for topic string
     * @return The number of consumed bytes
     */
    private static int getFirstPackageConsumed(final int topicByteCount) {
        return Constants.GEN_PREFIX_LENGTH + topicByteCount;
    }

    private static byte[] writeString(final String string) throws OXException {
        final int len = string.length();
        if (len <= 0) {
            /*
             * Write empty string
             */
            return new byte[] { (byte) (Constants.REQUEST_TERMINATOR), (byte) (Constants.REQUEST_TERMINATOR) };
        }
        if (len > MAX_INT_VALUE) {
            throw MessagingServiceExceptionCode.INT_TOO_BIG.create(Integer.valueOf(len));
        }
        final byte[] bytes = new byte[len + 2 + 1];
        int pos = 0;
        bytes[pos++] = (byte) (len >> 8); // high
        bytes[pos++] = (byte) (len & (255)); // low
        /*
         * Write string content and terminating '0'
         */
        final char[] chars = string.toCharArray();
        for (int i = 0; i < len; i++) {
            bytes[pos++] = (byte) chars[i];
        }
        /*
         * Terminating zero
         */
        bytes[pos] = 0;
        return bytes;
    }

    private static byte[] writeInt(final int i) throws OXException {
        if (i > MAX_INT_VALUE) {
            throw MessagingServiceExceptionCode.INT_TOO_BIG.create(Integer.valueOf(i));
        }
        return new byte[] { (byte) (i >> 8), (byte) (i & (255)) };
    }

}
