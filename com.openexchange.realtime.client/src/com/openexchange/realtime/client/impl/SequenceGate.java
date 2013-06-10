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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.realtime.client.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.json.JSONValue;

/**
 * A {@link SequenceGate} assures an ordered delivery of incoming messages.
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SequenceGate {

    static final int BUFFER_SIZE = 20;

    private final BlockingQueue<EnqueuedMessage> toTake;

    private final List<EnqueuedMessage> buffer;

    private final Lock lock;

    private final Condition queueNonEmpty;

    private long threshold;

    /**
     * Initializes a new {@link SequenceGate}.
     */
    public SequenceGate() {
        super();
        toTake = new PriorityBlockingQueue<EnqueuedMessage>();
        buffer = new ArrayList<EnqueuedMessage>();
        lock = new ReentrantLock();
        queueNonEmpty = lock.newCondition();
        threshold = 0L;
    }

    /**
     * Enqueue an incoming message. Messages will be preserved until they form a valid sequence. Afterwards they can be removed and
     * delivered. For removal see {@link #take()}.
     * 
     * @param message The message.
     * @param seq The messages sequence number.
     * @return Whether the message was enqueued or not. If <code>false</code> the message was not enqueued because of a full buffer. This
     *         may happen if one message is missing in the sequence but lots of messages with higher sequences arrive before the message is
     *         resent by the server. This may also happen if the buffer is not emptied via {@link #take()}.
     */
    public boolean enqueue(JSONValue message, long seq) {
        lock.lock();
        try {
            if (seq == threshold) {
                if (toTake.size() > 2 * BUFFER_SIZE) {
                    return false;
                }

                long highestSeq = seq;
                toTake.put(new EnqueuedMessage(seq, message));
                Iterator<EnqueuedMessage> it = buffer.iterator();
                while (it.hasNext()) {
                    EnqueuedMessage enqueued = it.next();
                    long currentSeq = enqueued.getSeq();
                    if (currentSeq > highestSeq) {
                        highestSeq = currentSeq;
                    }

                    toTake.put(enqueued);
                    it.remove();
                }

                threshold = ++highestSeq;
                queueNonEmpty.signal();
            } else if (seq > threshold) {
                if (buffer.size() == BUFFER_SIZE) {
                    return false;
                }

                EnqueuedMessage enqueued = new EnqueuedMessage(seq, message);
                if (!buffer.contains(enqueued)) {
                    buffer.add(enqueued);
                }
            }
        } catch (InterruptedException e) {
            // TODO: log
            return false;
        } finally {
            lock.unlock();
        }

        return true;
    }

    /**
     * This call blocks until a valid sequence of messages is available in the buffer. The returned messages will be removed from the
     * buffer. This method should be called in a loop to receive incoming messages as fast as possible and to keep the buffer small.
     * 
     * @return A list with formerly buffered messages. The list is ordered ascending by sequence numbers.
     */
    public List<JSONValue> take() throws InterruptedException {
        lock.lock();
        try {
            while (toTake.isEmpty()) {
                queueNonEmpty.await();
            }

            List<EnqueuedMessage> enqueuedMessages = new ArrayList<EnqueuedMessage>(toTake.size());
            List<JSONValue> messages = new ArrayList<JSONValue>(toTake.size());
            toTake.drainTo(enqueuedMessages);
            for (EnqueuedMessage enqueued : enqueuedMessages) {
                messages.add(enqueued.getMessage());
            }

            return messages;
        } finally {
            lock.unlock();
        }
    }

    private static class EnqueuedMessage implements Comparable<EnqueuedMessage> {

        private final long seq;

        private final JSONValue message;

        /**
         * Initializes a new {@link EnqueuedMessage}.
         * 
         * @param seq
         * @param message
         */
        public EnqueuedMessage(long seq, JSONValue message) {
            super();
            this.seq = seq;
            this.message = message;
        }

        /**
         * Gets the seq
         * 
         * @return The seq
         */
        public long getSeq() {
            return seq;
        }

        /**
         * Gets the message
         * 
         * @return The message
         */
        public JSONValue getMessage() {
            return message;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(EnqueuedMessage o) {
            long seq2 = o.getSeq();
            return (int) (seq - seq2);
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (seq ^ (seq >>> 32));
            return result;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            EnqueuedMessage other = (EnqueuedMessage) obj;
            if (seq != other.seq)
                return false;
            return true;
        }

    }

}
