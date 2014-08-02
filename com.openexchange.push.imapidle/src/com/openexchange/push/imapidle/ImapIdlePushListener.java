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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.push.imapidle;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.mail.Folder;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPCapabilities;
import com.openexchange.imap.IMAPFolderStorage;
import com.openexchange.imap.IMAPProvider;
import com.openexchange.mail.Protocol;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageDelegator;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushUtility;
import com.openexchange.push.imapidle.locking.ImapIdleClusterLock;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link ImapIdlePushListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public final class ImapIdlePushListener implements PushListener, Runnable {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ImapIdlePushListener.class);

    /**
     * A simple task that actually performs the {@link IMAPFolder#idle() IMAP IDLE call}.
     */
    private static final class ImapIdleTask extends AbstractTask<Void> {

        private final IMAPFolder imapFolder;

        ImapIdleTask(IMAPFolder imapFolder) {
            super();
            this.imapFolder = imapFolder;
        }

        @Override
        public Void call() throws MessagingException {
            imapFolder.idle(true);
            return null;
        }
    }

    /**
     * The push mode; either <code>"newmail"</code> or <code>"always"</code>.
     */
    public static enum PushMode {

        /**
         * Only propagate a push event if at least one new message has arrived in mailbox
         */
        NEWMAIL("newmail"),
        /**
         * Propagate push event on any change to mailbox
         */
        ALWAYS("always");

        private final String identifier;

        private PushMode(final String text) {
            this.identifier = text;
        }

        /**
         * Gets the push mode identifier
         *
         * @return The identifier
         */
        public String getIdentifier() {
            return identifier;
        }

        /**
         * Gets the push mode by specified identifier.
         *
         * @param id The identifier
         * @return The push mode or <code>null</code>
         */
        public static PushMode fromIdentifier(String id) {
            if (id != null) {
                for (final PushMode m : PushMode.values()) {
                    if (id.equalsIgnoreCase(m.identifier)) {
                        return m;
                    }
                }
            }
            return null;
        }
    }

    // ------------------------------------------------------------------------------------------------------- //

    private final ServiceLookup services;
    private final Session session;
    private ScheduledTimerTask timerTask;
    private final int accountId;
    private final String fullName;
    private final long delay;
    private final PushMode pushMode;
    private final AtomicBoolean canceled;
    private volatile IMAPFolder imapFolderInUse;

    /**
     * Initializes a new {@link ImapIdlePushListener}.
     */
    public ImapIdlePushListener(String fullName, int accountId, PushMode pushMode, long delay, Session session, ServiceLookup services) {
        super();
        canceled = new AtomicBoolean();
        this.fullName = fullName;
        this.accountId = accountId;
        this.session = session;
        this.delay = delay <= 0 ? 5000L : delay;
        this.services = services;
        this.pushMode = pushMode;
    }

    @Override
    public void notifyNewMail() throws OXException {
        PushUtility.triggerOSGiEvent(MailFolderUtility.prepareFullname(accountId, fullName), session);
    }

    @Override
    public void run() {
        if (canceled.get()) {
            return;
        }

        String sContextId = Integer.toString(session.getContextId());
        String sUserId = Integer.toString(session.getUserId());

        try {
            boolean error = true;
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
            try {
                MailService mailService = services.getOptionalService(MailService.class);
                if (null == mailService) {
                    // Currently no MailService available
                    error = false;
                    return;
                }

                mailAccess = mailService.getMailAccess(session, accountId);
                mailAccess.connect(false);

                boolean notified = false;

                IMAPStore imapStore = getImapFolderStorageFrom(mailAccess).getImapStore();
                final IMAPFolder imapFolder = (IMAPFolder) imapStore.getFolder(fullName);
                this.imapFolderInUse = imapFolder;
                try {
                    imapFolder.open(Folder.READ_WRITE);
                    LOGGER.debug("Starting IMAP-IDLE run for user {} in context {}.", sUserId, sContextId);

                    // Acquire folder counts
                    int deletedCount = 0;
                    int totalCount = 0;
                    final PushMode pushMode = this.pushMode;
                    if (PushMode.ALWAYS == pushMode) {
                        // Operations may be expensive, so only do them in always mode.
                        deletedCount = imapFolder.getDeletedMessageCount();
                        totalCount = imapFolder.getMessageCount();
                    }

                    // Check if canceled meanwhile
                    if (canceled.get()) {
                        error = false;
                        return;
                    }

                    // Refresh lock prior to entering IMAP-IDLE
                    ImapIdlePushManagerService.getInstance().refreshLock(session);

                    // Do the IMAP IDLE connect
                    mailAccess.setWaiting(true);
                    try {
                        if (false == doImapIdleTimeoutAware(imapFolder)) {
                            // Timeout elapsed
                            error = false;
                            return;
                        }
                    } finally {
                        mailAccess.setWaiting(false);
                    }

                    // Check if canceled meanwhile
                    if (canceled.get()) {
                        error = false;
                        return;
                    }

                    // Do the push dependent on mode
                    switch (pushMode) {
                    case NEWMAIL:
                        {
                            int newMessageCount = imapFolder.getNewMessageCount();
                            if (newMessageCount > 0) {
                                LOGGER.debug("IMAP-IDLE result for user {} in context {}: Doing push due to {} new mail(s)", sUserId, sContextId, Integer.toString(newMessageCount));
                                notifyNewMail();
                                notified = true;
                            }
                        }
                        break;
                    case ALWAYS:
                        // Fall-through
                    default:
                        // Check new message counter
                        {
                            int newMessageCount = imapFolder.getNewMessageCount();
                            if (newMessageCount > 0) {
                                LOGGER.debug("IMAP-IDLE result for user {} in context {}: Doing push due to {} new mail(s)", sUserId, sContextId, Integer.toString(newMessageCount));
                                notifyNewMail();
                                notified = true;
                                break;
                            }
                        }

                        // Compare deleted message counters
                        {
                            int newDeletedCount = imapFolder.getDeletedMessageCount();
                            if (imapFolder.getDeletedMessageCount() != deletedCount) {
                                LOGGER.debug("IMAP-IDLE result for user {} in context {}: Doing push due to differing message counts. Current deleted count {} vs. old deleted count {}", Integer.toString(newDeletedCount), Integer.toString(deletedCount));
                                notifyNewMail();
                                notified = true;
                                break;
                            }
                        }

                        // Compare total message counters
                        {
                            int newTotalCount = imapFolder.getMessageCount();
                            if (newTotalCount != totalCount) {
                                LOGGER.debug("IMAP-IDLE result for user {} in context {}: Doing push due to differing message counts. Current total count {} vs. old total count {}", Integer.toString(newTotalCount), Integer.toString(totalCount));
                                notifyNewMail();
                                notified = true;
                                break;
                            }
                        }
                    }

                    if (notified) {
                        LOGGER.debug("Performed IMAP-IDLE run having new messages for user {} in context {}. ", sUserId, sContextId);
                    } else {
                        LOGGER.debug("Performed IMAP-IDLE run with no result for user {} in context {}. ", sUserId, sContextId);
                    }
                } finally {
                    this.imapFolderInUse = null;
                    try {
                        imapFolder.close(false);
                    } catch (final Exception e) {
                        // Ignore
                    }
                }
            } catch (OXException e) {
                launderOXException(e);
            } catch (javax.mail.AuthenticationFailedException e) {
                // Definitely cancel...
                throw e;
            } catch (javax.mail.NoSuchProviderException e) {
                // Definitely cancel...
                throw e;
            } catch (javax.mail.MethodNotSupportedException e) {
                // Definitely cancel...
                throw e;
            } catch (javax.mail.MessagingException e) {
                LOGGER.debug("Awaiting next IMAP-IDLE run for user {} in context {}.", sUserId, sContextId);
                // Try again
            } finally {
                closeMailAccess(mailAccess);
                mailAccess = null;

                if (false == error) {
                    // Perform next run
                    LOGGER.debug("Awaiting next IMAP-IDLE run for user {} in context {}.", sUserId, sContextId);
                }
            }
        } catch (InterruptedException e) {
            // Thread interrupted - keep interrupted flag
            Thread.currentThread().interrupt();
            LOGGER.debug("Thread interrupted during IMAP-IDLE run for user {} in context {}. Therefore going to cancel associated listener permanently.", sUserId, sContextId, e);
            cancel(true);
        } catch (Exception e) {
            // Any aborting error
            LOGGER.warn("Severe error during IMAP-IDLE run for user {} in context {}. Therefore going to cancel associated listener permanently.", sUserId, sContextId, e);
            cancel(true);
        }
    }

    /**
     * Actually enters the IMAP-IDLE to IMAP server with respect to frequent cluster lock <i><tt>touch</tt></i>ing.
     * <p>
     * IMAP-IDLE is performed until either
     * <ol>
     * <li>A notification is yielded by IMAP server (new messages, whatever...)</li>
     * <li>The timeout elapses leading to a forced abortion of IMAP-IDLE</li>
     * </ol>
     * <p>
     * For the first condition <code>true</code> is returned; otherwise <code>false</code> for the second case.
     *
     * @param imapFolder The associated mailbox for which to enter the IMAP-IDLE command
     * @return <code>true</code> in case an IMAP server notification terminated the IMAP-IDLE; otherwise <code>false</code> if timeout elapsed
     * @throws InterruptedException If idle'ing thread has been interrupted
     * @throws MessagingException If IMAP-IDLE fails for any reason
     */
    private boolean doImapIdleTimeoutAware(final IMAPFolder imapFolder) throws InterruptedException, MessagingException {
        try {
            Future<Void> f = ThreadPools.getThreadPool().submit(new ImapIdleTask(imapFolder), CallerRunsBehavior.<Void> getInstance());
            f.get(ImapIdleClusterLock.TIMEOUT_MILLIS - 60000, TimeUnit.MILLISECONDS);
            return true;
        } catch (TimeoutException e) {
            // Next run...
            return false;
        } catch (ExecutionException e) {
            throw ThreadPools.launderThrowable(e, MessagingException.class);
        }
    }

    /**
     * Starts this listener.
     *
     * @throws OXException If start-up fails
     */
    public synchronized void start() throws OXException {
        TimerService timerService = services.getOptionalService(TimerService.class);
        if (null == timerService) {
            throw ServiceExceptionCode.absentService(TimerService.class);
        }

        // Check primary mail account's nature
        MailAccess<?, ?> access = null;
        try {
            MailService mailService = services.getOptionalService(MailService.class);
            if (null == mailService) {
                throw ServiceExceptionCode.absentService(MailService.class);
            }
            access = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            /*
             * Check protocol
             */
            final Protocol protocol = access.getProvider().getProtocol();
            if (null == protocol || (!Protocol.ALL.equals(protocol.getName()) && !IMAPProvider.PROTOCOL_IMAP.equals(protocol))) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Primary mail account is not IMAP, but " + (null == protocol ? "is missing." : protocol.getName()));
            }
            /*
             * Check for IDLE capability
             */
            access.connect(false);
            final IMAPCapabilities capabilities = (IMAPCapabilities) access.getMailConfig().getCapabilities();
            if (!capabilities.hasIdle()) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Primary IMAP account does not support \"IDLE\" capability!");
            }
        } finally {
            if (null != access) {
                access.close(false);
            }
        }

        timerTask = timerService.scheduleAtFixedRate(this, delay, delay);
    }

    /**
     * Cancels this IMAP IDLE listener.
     *
     * @return <code>true</code> if reconnected; otherwise <code>false</code> if terminated
     */
    public synchronized boolean cancel(boolean tryToReconnect) {
        boolean reconnected = false;
        try {
            // Mark as canceled
            canceled.set(true);

            // Close IMAP resources, too
            final IMAPFolder imapFolderInUse = this.imapFolderInUse;
            if (null != imapFolderInUse) {
                this.imapFolderInUse = null;
                try {
                    imapFolderInUse.close(false);
                } catch (final Exception e) {
                    // Ignore
                }
            }

            // Cancel time task
            ScheduledTimerTask timerTask = this.timerTask;
            if (null != timerTask) {
                this.timerTask = null;
                timerTask.cancel();
            }
        } finally {
            ImapIdlePushManagerService instance = ImapIdlePushManagerService.getInstance();
            if (null != instance) {
                ImapIdlePushListener anotherListener = tryToReconnect ? instance.injectAnotherListenerFor(session) : null;
                if (null == anotherListener) {
                    // No other listener available
                    // Give up lock and return
                    try {
                        instance.releaseLock(session);
                    } catch (Exception e) {
                        LOGGER.warn("Failed to release lock for user {} in context {}.", session.getUserId(), session.getContextId(), e);
                    }
                } else {
                    try {
                        anotherListener.start();
                        reconnected = true;
                    } catch (Exception e) {
                        LOGGER.warn("Failed to start new listener for user {} in context {}.", session.getUserId(), session.getContextId(), e);
                        // Give up lock and return
                        try {
                            instance.releaseLock(session);
                        } catch (Exception x) {
                            LOGGER.warn("Failed to release DB lock for user {} in context {}.", session.getUserId(), session.getContextId(), x);
                        }
                    }
                }
            }
        }
        return reconnected;
    }

    private void launderOXException(OXException e) throws OXException {
        if (PushExceptionCodes.PREFIX.equals(e.getPrefix())) {
            throw e;
        }
        if (MailAccountExceptionCodes.NOT_FOUND.equals(e)) {
            /*
             * Missing mail account; drop listener
             */
            LOGGER.debug("Missing (default) mail account for user {} in context {}. Stopping obsolete IMAP-IDLE listener.", session.getUserId(), session.getContextId());
            throw e;
        }
        if ("DBP".equals(e.getPrefix())) {
            throw e;
        }
    }

    private static void closeMailAccess(final MailAccess<?, ?> mailAccess) {
        if (null != mailAccess) {
            try {
                mailAccess.close(false);
            } catch (final Exception x) {
                // Ignore
            }
        }
    }

    private static IMAPFolderStorage getImapFolderStorageFrom(final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) throws OXException {
        IMailFolderStorage fstore = mailAccess.getFolderStorage();
        if (!(fstore instanceof IMAPFolderStorage)) {
            if (!(fstore instanceof IMailFolderStorageDelegator)) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Unknown MAL implementation");
            }
            fstore = ((IMailFolderStorageDelegator) fstore).getDelegateFolderStorage();
            if (!(fstore instanceof IMAPFolderStorage)) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Unknown MAL implementation");
            }
        }
        return (IMAPFolderStorage) fstore;
    }

}
