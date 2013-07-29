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

package com.openexchange.realtime.client.impl.connection;

import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.realtime.client.RTException;

public class ResendTask extends TimerTask implements Future<MessageState> {

        private static final Logger LOG = LoggerFactory.getLogger(ResendTask.class);

        private static final int RESEND_LIMIT = 100;

        private final long seq;

        private final JSONObject message;

        private final AbstractRTConnection connection;

        private final Lock lock;

        private final Condition isDone;

        private MessageState state;

        private boolean isCancelled;

        private int resendCount;

        public ResendTask(AbstractRTConnection connection, long seq, JSONObject message) {
            super();
            this.connection = connection;
            this.seq = seq;
            this.message = message;
            lock = new ReentrantLock();
            isDone = lock.newCondition();
            state = MessageState.PENDING;
            isCancelled = false;
            resendCount = 0;
        }

        @Override
        public void run() {
            try {
                if (resendCount == RESEND_LIMIT) {
                    LOG.error("Could not send message " + seq + " after " + RESEND_LIMIT + " tries.");
                    lock.lock();
                    try {
                        cancel();
                        state = MessageState.CANCELLED;
                    } finally {
                        lock.unlock();
                    }
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Trying to send message {}. Resend count: {}", message.toString(), resendCount);
                }
                connection.doSend(message);
                resendCount++;
            } catch (RTException e) {
                LOG.warn("Error while sending message " + seq + ". Resend count is: " + resendCount, e);
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            lock.lock();
            try {
                boolean retval = cancel();
                state = MessageState.CANCELLED;
                return retval;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean cancel() {
            lock.lock();
            try {
                if (!isCancelled) {
                    isCancelled = true;
                    state = MessageState.ACK_RECEIVED;
                    isDone.signal();
                    return true;
                }

                return false;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return isCancelled;
        }

        @Override
        public MessageState get() throws InterruptedException, ExecutionException {
            lock.lock();
            try {
                while (!isCancelled) {
                    isDone.await();
                }
                return state;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public MessageState get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            lock.lock();
            try {
                while (!isCancelled) {
                    if (!isDone.await(timeout, unit)) {
                        throw new TimeoutException();
                    }
                }
                return state;
            } finally {
                lock.unlock();
            }
        }

    }