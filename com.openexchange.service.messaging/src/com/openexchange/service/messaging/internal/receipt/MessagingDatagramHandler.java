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

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.service.messaging.Message;
import com.openexchange.service.messaging.MessagingServiceExceptionCode;
import com.openexchange.service.messaging.internal.Constants;
import com.openexchange.service.messaging.internal.MessageHandlerTracker;
import com.openexchange.service.messaging.internal.MessageHandlerWrapper;
import com.openexchange.threadpool.RefusedExecutionBehavior;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

/**
 * {@link MessagingDatagramHandler} - A handler for received packages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessagingDatagramHandler {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(MessagingDatagramHandler.class));

    private final ConcurrentMap<Key, MessagingContiguousMessage> truncated;

    private final ConcurrentMap<Key, LockAndCondition> locks;

    private final MessageHandlerTracker handlers;

    /**
     * Initializes a new {@link MessagingDatagramHandler}.
     */
    public MessagingDatagramHandler(final MessageHandlerTracker handlers) {
        super();
        this.handlers = handlers;
        truncated = new ConcurrentHashMap<Key, MessagingContiguousMessage>();
        locks = new ConcurrentHashMap<Key, LockAndCondition>();
    }

    /**
     * Shuts-down this socket handler.
     */
    public void shutDownNow() {
        truncated.clear();
        locks.clear();
    }

    private static final RefusedExecutionBehavior<Void> BEHAVIOR = CallerRunsBehavior.getInstance();

    /**
     * Handles given datagram in a separate task.
     *
     * @param datagramPacket The received datagram packet
     */
    public void handle(final DatagramPacket datagramPacket) {
        final ThreadPoolService threadPool = ThreadPools.getThreadPool();
        if (null == threadPool) {
            new MSSocketHandlerTask(datagramPacket, handlers, null, truncated, locks, LOG).call();
        } else {
            threadPool.submit(
                ThreadPools.task(new MSSocketHandlerTask(datagramPacket, handlers, threadPool, truncated, locks, LOG)),
                BEHAVIOR);
        }
    }

    private static final class MSSocketHandlerTask implements Callable<Void> {

        private final DatagramPacket datagramPacket;

        private final ConcurrentMap<Key, MessagingContiguousMessage> tTruncated;

        private final ConcurrentMap<Key, LockAndCondition> tLocks;

        private final MessageHandlerTracker handlers;

        private final ThreadPoolService threadPool;

        private final Log logger;

        /**
         * Initializes a new {@link MSSocketHandlerTask}.
         *
         * @param datagramPacket The received datagram packet
         */
        public MSSocketHandlerTask(final DatagramPacket datagramPacket, final MessageHandlerTracker handlers, final ThreadPoolService threadPool, final ConcurrentMap<Key, MessagingContiguousMessage> truncated, final ConcurrentMap<Key, LockAndCondition> locks, final Log logger) {
            super();
            this.threadPool = threadPool;
            this.handlers = handlers;
            this.datagramPacket = datagramPacket;
            this.tLocks = locks;
            this.tTruncated = truncated;
            this.logger = logger;
        }

        @Override
        public Void call() {
            try {
                /*
                 * Parse socket package
                 */
                final Message message = parse2Message();
                if (null != message) {
                    /*
                     * Delegate to listeners interested in message's topic
                     */
                    final String topic = message.getTopic();
                    final Set<MessageHandlerWrapper> eventHandlers = handlers.getHandlers(topic);
                    /*
                     * If there are no handlers, then we are done
                     */
                    if (eventHandlers.isEmpty()) {
                        return null;
                    }
                    if (null == threadPool) {
                        for (final MessageHandlerWrapper handler : eventHandlers) {
                            handler.handleMessage(message);
                        }
                    } else {
                        for (final MessageHandlerWrapper handler : eventHandlers) {
                            threadPool.submit(ThreadPools.task(new Runnable() {

                                @Override
                                public void run() {
                                    handler.handleMessage(message);
                                }
                            }, "MessagingListener-"), CallerRunsBehavior.getInstance());
                        }
                    }
                }
            } catch (final OXException e) {
                logger.error(e.getMessage(), e);
            }
            return null;
        }

        /**
         * Parses given datagram to a message.
         *
         * @return The parsed message or <code>null</code> if a contiguous package was passed
         * @throws OXException If parsing fails
         */
        private Message parse2Message() throws OXException {
            try {
                final MessagingParsedMessage pm;
                {
                    final byte[] b = new byte[datagramPacket.getLength()];
                    System.arraycopy(datagramPacket.getData(), 0, b, 0, b.length);
                    /*
                     * Get parsed message
                     */
                    pm = new MessagingMessageParser(b).parse();
                }
                if (!pm.isContiguous()) {
                    if (Constants.PREFIX_CODE_START == pm.getPrefixCode()) {
                        /*
                         * Already all data received with first datagram
                         */
                        return new MessagingContiguousMessage(pm.getTopic(), pm.getChunk()).toMessage();
                    }
                    /*
                     * Last chunk received
                     */
                    final Key key = new Key(new InetSocketAddress(datagramPacket.getAddress(), datagramPacket.getPort()), pm.getUuid());
                    final MessagingContiguousMessage prev = tTruncated.remove(key);
                    tLocks.remove(key);
                    if (null == prev) {
                        throw MessagingServiceExceptionCode.MISSING_PREV_PACKAGE.create();
                    }
                    prev.add(pm.getChunkNumber(), pm.getChunk());
                    return prev.toMessage();
                }
                /*
                 * Contiguous flag is set
                 */
                final UUID uuid = pm.getUuid();
                final Key key = new Key(new InetSocketAddress(datagramPacket.getAddress(), datagramPacket.getPort()), uuid);
                if (Constants.PREFIX_CODE_START == pm.getPrefixCode()) {
                    /*
                     * Must be first
                     */
                    final MessagingContiguousMessage first = new MessagingContiguousMessage(pm.getTopic(), pm.getChunk());
                    if (null != tTruncated.putIfAbsent(key, first)) {
                        throw MessagingServiceExceptionCode.CONFLICTING_TRUNCATED_PACKAGES.create();
                    }
                    final LockAndCondition lac = getLock(key);
                    final Lock lock = lac.lock;
                    lock.lock();
                    try {
                        lac.condition.signalAll();
                    } finally {
                        lock.unlock();
                    }
                    return null;
                }
                /*
                 * A subsequent data package
                 */
                MessagingContiguousMessage prev = tTruncated.get(key);
                if (null == prev) {
                    final LockAndCondition lac = getLock(key);
                    final Lock lock = lac.lock;
                    lock.lock();
                    try {
                        do {
                            try {
                                lac.condition.await();
                            } catch (final InterruptedException e) {
                                /*
                                 * Propagate to non-interrupted thread
                                 */
                                lac.condition.signalAll();
                                // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                                Thread.currentThread().interrupt();
                                throw MessagingServiceExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
                            }
                        } while (null == (prev = tTruncated.get(key)));
                    } finally {
                        lock.unlock();
                    }
                }
                prev.add(pm.getChunkNumber(), pm.getChunk());
                return null;
            } catch (final RuntimeException e) {
                throw MessagingServiceExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }

        private LockAndCondition getLock(final Key key) {
            LockAndCondition lock = tLocks.get(key);
            if (null == lock) {
                final LockAndCondition newLock = new LockAndCondition();
                lock = tLocks.putIfAbsent(key, newLock);
                if (null == lock) {
                    lock = newLock;
                }
            }
            return lock;
        }

    }

    /**
     * Tiny helper class to link a {@link Condition} to its {@link Lock}.
     */
    private static final class LockAndCondition {

        public final Lock lock;

        public final Condition condition;

        public LockAndCondition() {
            super();
            lock = new ReentrantLock();
            condition = lock.newCondition();
        }

    }

    private static final class Key {

        private final InetSocketAddress socketAddress;

        private final UUID uuid;

        private final int hash;

        /**
         * Initializes a new {@link Key}.
         *
         * @param socketAddress
         * @param uuid
         */
        public Key(final InetSocketAddress socketAddress, final UUID uuid) {
            super();
            this.socketAddress = socketAddress;
            this.uuid = uuid;
            final int prime = 31;
            int result = 1;
            result = prime * result + ((socketAddress == null) ? 0 : socketAddress.hashCode());
            result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
            if (socketAddress == null) {
                if (other.socketAddress != null) {
                    return false;
                }
            } else if (!socketAddress.equals(other.socketAddress)) {
                return false;
            }
            if (uuid == null) {
                if (other.uuid != null) {
                    return false;
                }
            } else if (!uuid.equals(other.uuid)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return new StringBuilder(32).append("{ socketAddress=").append(socketAddress).append(", uuid=").append(uuid.toString()).append(
                '}').toString();
        }

    }

}
