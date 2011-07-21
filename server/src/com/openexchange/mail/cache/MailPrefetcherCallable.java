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

package com.openexchange.mail.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import org.json.JSONObject;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link MailPrefetcherCallable} - The mail prefetcher {@link Callable callable}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailPrefetcherCallable implements Callable<Object> {

    static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(MailPrefetcherCallable.class)));

    static final boolean DEBUG = LOG.isDebugEnabled();

    final Session session;

    final int accountId;

    final String[] mailIds;

    final String fullname;

    final boolean overwrite;

    final ThreadPoolService threadPool;

    /**
     * Initializes a new {@link MailPrefetcherCallable}.
     * 
     * @param session The session providing needed user information
     * @param accountId he account ID
     * @param fullname The folder's fullname
     * @param mailIds The mail IDs
     * @param overwrite <code>true</code> to allow messages being overridden; otherwise <code>false</code>
     * @param threadPool The (optional) thread pool service reference; may be <code>null</code>
     */
    public MailPrefetcherCallable(final Session session, final int accountId, final String fullname, final String[] mailIds, final boolean overwrite, final ThreadPoolService threadPool) {
        super();
        this.session = session;
        this.accountId = accountId;
        this.fullname = fullname;
        this.mailIds = mailIds;
        this.overwrite = overwrite;
        this.threadPool = threadPool;
    }

    public Object call() throws Exception {
        final JSONMessageCache cache = JSONMessageCache.getInstance();
        if (null == cache) {
            return null;
        }
        try {

            final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, accountId);
            mailAccess.connect(false);
            try {
                final Set<String> validIDs;
                {
                    /*
                     * Valid IDs
                     */
                    validIDs = new HashSet<String>(Arrays.asList(mailIds));
                    /*
                     * Check for unseen-only
                     */
                    if (JSONMessageCacheConfiguration.getInstance().isUnseenOnly()) {
                        final MailMessage[] unreadMails =
                            mailAccess.getMessageStorage().getUnreadMessages(
                                fullname,
                                MailSortField.RECEIVED_DATE,
                                OrderDirection.ASC,
                                new MailField[] { MailField.ID },
                                mailIds.length);
                        final Set<String> unreadIDs = new HashSet<String>(unreadMails.length);
                        for (final MailMessage unreadMail : unreadMails) {
                            unreadIDs.add(unreadMail.getMailId());
                        }
                        /*
                         * Retain unread only
                         */
                        validIDs.retainAll(unreadIDs);
                    }
                }
                /*
                 * Create an array of futures as place holders for resulting JSON object
                 */
                final List<SetableFutureTask<JSONObject>> futures = new ArrayList<SetableFutureTask<JSONObject>>(validIDs.size());
                for (final String mailId : validIDs) {
                    if (overwrite || !cache.containsKey(accountId, fullname, mailId, session)) {
                        final SetableFutureTask<JSONObject> f =
                            new SetableFutureTask<JSONObject>(new MessageLoadCallable(mailId, fullname, accountId, session, LOG), mailId);
                        futures.add(f);
                        cache.put(accountId, fullname, mailId, f, session);
                    }
                }
                if (!futures.isEmpty()) {
                    /*
                     * Obtain mail access, fetch messages and set result value in future
                     */
                    final long start = DEBUG ? System.currentTimeMillis() : 0L;
                    final int size = futures.size();
                    final BlockingQueue<MailMessage> q = new ArrayBlockingQueue<MailMessage>(size);
                    /*
                     * Produce in a separate thread (if possible)
                     */
                    {
                        final Callable<Object> producerCallable = new Callable<Object>() {

                            public Object call() throws Exception {
                                for (final SetableFutureTask<JSONObject> f : futures) {
                                    try {
                                        final MailMessage mailMessage =
                                            mailAccess.getMessageStorage().getMessage(fullname, f.mailId, false);
                                        if (null == mailMessage) {
                                            f.setException(new MailException(MailException.Code.MAIL_NOT_FOUND, f.mailId, fullname));
                                        } else {
                                            q.offer(mailMessage);
                                        }
                                    } catch (final Exception e) {
                                        // LOG1.error(e.getMessage(), e);
                                        f.setException(e);
                                    }
                                }
                                return null;
                            }
                        };
                        if (null == threadPool) {
                            // Run in this thread
                            producerCallable.call();
                        } else {
                            threadPool.submit(ThreadPools.task(producerCallable));
                        }
                    }
                    /*
                     * Consume
                     */
                    final List<String> markUnseen = new ArrayList<String>(size);
                    for (final SetableFutureTask<JSONObject> f : futures) {
                        if (!f.hasException() && !f.hasValue()) {
                            final MailMessage m = q.take();
                            try {
                                if (!m.isSeen()) {
                                    // Mail is unseen
                                    markUnseen.add(m.getMailId());
                                }
                                f.set(MessageWriter.writeRawMailMessage(accountId, m));
                            } catch (final Exception e) {
                                // LOG1.error(e.getMessage(), e);
                                f.setException(e);
                            }
                        }
                    }
                    if (!markUnseen.isEmpty()) {
                        /*
                         * Explicitly mark as unseen since generating raw JSON mail representation touches mail's content
                         */
                        mailAccess.getMessageStorage().updateMessageFlags(
                            fullname,
                            markUnseen.toArray(new String[markUnseen.size()]),
                            MailMessage.FLAG_SEEN,
                            false);
                    }
                    if (DEBUG) {
                        final long dur = System.currentTimeMillis() - start;
                        final StringBuilder sb = new StringBuilder(128);
                        sb.append("Put ").append(size).append(" messages from folder ").append(fullname);
                        sb.append(" in account ").append(accountId).append(" for user ").append(session.getUserId());
                        sb.append(" in context ").append(session.getContextId()).append(" into JSON message cache in ");
                        sb.append(dur).append("msec.");
                        LOG.debug(sb.toString());
                    }
                    /*-
                     * 
                    for (int i = 0; i < mailIds.length; i++) {
                        final SetableFutureTask<JSONObject> future = futures[i];
                        if (null != future) {
                            try {
                                final MailMessage mm = mailAccess.getMessageStorage().getMessage(fullname, mailIds[i], false);
                                future.set(MessageWriter.writeRawMailMessage(accountId, mm, session));
                                if (DEBUG) {
                                    final StringBuilder sb = new StringBuilder(128);
                                    sb.append("Put message ").append(mailIds[i]).append(" from folder ");
                                    sb.append(fullname).append(" from account ").append(accountId).append(" into JSON message cache.");
                                    LOG1.debug(sb.toString());
                                }

                                System.out.println("Put message " + mailIds[i] + " from folder " + fullname + " from account " + accountId + " into JSON message cache!");

                            } catch (final MailException e) {
                                LOG.error(e.getMessage(), e);
                                future.setException(e);
                            }
                        }
                    }
                     */
                }
            } finally {
                mailAccess.close(true);
            }
            return null;
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    private static final class MessageLoadCallable implements Callable<JSONObject> {

        private final Session session;

        private final int accountId;

        private final String mailId;

        private final String fullname;

        private final org.apache.commons.logging.Log logger;

        MessageLoadCallable(final String mailId, final String fullname, final int accountId, final Session session, final org.apache.commons.logging.Log logger) {
            super();
            this.session = session;
            this.accountId = accountId;
            this.mailId = mailId;
            this.fullname = fullname;
            this.logger = logger;
        }

        public JSONObject call() throws Exception {
            try {
                final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, accountId);
                mailAccess.connect(false);
                try {
                    final MailMessage mm = mailAccess.getMessageStorage().getMessage(fullname, mailId, false);
                    if (null == mm) {
                        throw new MailException(MailException.Code.MAIL_NOT_FOUND, mailId, fullname);
                    }
                    final boolean unseen = !mm.isSeen();
                    final JSONObject rawMailMessage = MessageWriter.writeRawMailMessage(accountId, mm);
                    if (unseen) {
                        /*
                         * Explicitly mark as unseen since generating raw JSON mail representation touches mail's content
                         */
                        mailAccess.getMessageStorage().updateMessageFlags(
                            fullname,
                            new String[] { mm.getMailId() },
                            MailMessage.FLAG_SEEN,
                            false);
                    }
                    return rawMailMessage;
                } finally {
                    mailAccess.close(true);
                }
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
                throw e;
            }
        }

    } // End of MessageLoadCallable

    private static final class SetableFutureTask<V> extends FutureTask<V> {

        final String mailId;

        volatile boolean exceptionSet;

        volatile boolean valueSet;

        SetableFutureTask(final Callable<V> callable, final String mailId) {
            super(callable);
            this.mailId = mailId;
        }

        /**
         * Sets the result of this Future to the given value unless this future has already been set or has been canceled. Otherwise it is a
         * no-op.
         * 
         * @param v The value
         */
        @Override
        public void set(final V v) {
            super.set(v);
            valueSet = true;
        }

        /**
         * Causes this future to report an <tt>ExecutionException</tt> with the given throwable as its cause, unless this Future has already
         * been set or has been canceled. Otherwise it is a no-op.
         * 
         * @param t The cause of failure.
         */
        @Override
        public void setException(final Throwable t) {
            super.setException(t);
            exceptionSet = true;
        }

        /**
         * Checks whether an exception instance was previously set by {@link #setException(Throwable)}.
         * 
         * @return <code>true</code> if an exception instance was set; otherwise <code>false</code>
         */
        public boolean hasException() {
            return exceptionSet;
        }

        /**
         * Checks whether a value was previously set by {@link #set(Object)}.
         * 
         * @return <code>true</code> if a value was set; otherwise <code>false</code>
         */
        public boolean hasValue() {
            return valueSet;
        }

    } // End of SetableFutureTask

}
