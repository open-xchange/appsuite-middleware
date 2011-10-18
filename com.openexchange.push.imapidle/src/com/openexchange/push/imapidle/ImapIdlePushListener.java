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

package com.openexchange.push.imapidle;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPCapabilities;
import com.openexchange.imap.IMAPFolderStorage;
import com.openexchange.imap.IMAPProvider;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushUtility;
import com.openexchange.push.imapidle.services.ImapIdleServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link ImapIdlePushListener} - The IMAP IDLE {@link PushListener}.
 */
public final class ImapIdlePushListener implements PushListener {

    /**
     * @author choeger
     *
     */
    public enum PushMode {
        NEWMAIL("newmail"),
        ALWAYS("always");

        private final String text;

        private PushMode(final String text) {
            this.text = text;
        }

        public final String getText() {
            return text;
        }

        public static final PushMode fromString(final String text) {
            if( text != null ) {
                for(final PushMode m : PushMode.values()) {
                    if(text.equals(m.text)) {
                        return m;
                    }
                }
            }
            return null;
        }
    }

    /**
     * @param debugEnabled the debugEnabled to set
     */
    public static final void setDebugEnabled(final boolean debugEnabled) {
        DEBUG_ENABLED = debugEnabled;
    }

    private static PushMode pushMode;

    /**
     * @param pushmode the pushmode to set
     */
    public static final void setPushmode(final PushMode pushmode) {
        pushMode = pushmode;
    }


    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(ImapIdlePushListener.class));

    private static boolean DEBUG_ENABLED = LOG.isDebugEnabled();

    /**
     * A placeholder constant for account ID.
     */
    private static final int ACCOUNT_ID = 0;

    private static volatile String folder;

    private static int errordelay;

    /**
     * Gets the account ID constant.
     *
     * @return The account ID constant
     */
    public static int getAccountId() {
        return ACCOUNT_ID;
    }

    /**
     * Sets static folder fullname.
     *
     * @param folder The folder fullname
     */
    public static void setFolder(final String folder) {
        ImapIdlePushListener.folder = folder;
    }

    /**
     * Gets static folder fullname.
     *
     * @return The folder fullname
     */
    public static String getFolder() {
        return folder;
    }

    /**
     * Initializes a new {@link ImapIdlePushListener}.
     *
     * @param session The needed session to obtain and connect mail access instance
     * @param startTimerTask <code>true</code> to start a timer task for this listener
     * @return A new {@link ImapIdlePushListener}.
     */
    public static ImapIdlePushListener newInstance(final Session session) {
        return new ImapIdlePushListener(session);
    }

    /*
     * Member section
     */

    private final AtomicBoolean running;

    private final Session session;

    private final int userId;

    private final int contextId;

    private volatile Future<Object> imapIdleFuture;

    private MailAccess<?, ?> mailAccess;

    private MailService mailService;

    private boolean shutdown;

    /**
     * Initializes a new {@link ImapIdlePushListener}.
     *
     * @param session The needed session to obtain and connect mail access instance
     */
    private ImapIdlePushListener(final Session session) {
        super();
        running = new AtomicBoolean();
        this.session = session;
        userId = session.getUserId();
        contextId = session.getContextId();
        mailAccess = null;
        mailService = null;
        errordelay = 1000;
        shutdown = false;
    }

    /**
     * @return the errordelay
     */
    public static final int getErrordelay() {
        return errordelay;
    }

    /**
     * @param errordelay the errordelay to set
     */
    public static final void setErrordelay(final int errordelay) {
        ImapIdlePushListener.errordelay = errordelay;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128).append("session-ID=").append(session.getSessionID());
        sb.append(", user=").append(userId).append(", context=").append(contextId);
        sb.append(", imapIdleFuture=").append(imapIdleFuture);
        return sb.toString();
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Opens this listener
     *
     * @throws OXException If listener cannot be opened
     */
    public void open() throws OXException {
        final ThreadPoolService threadPoolService;
        {
            threadPoolService = ImapIdleServiceRegistry.getServiceRegistry().getService(ThreadPoolService.class, true);
            mailService = ImapIdleServiceRegistry.getServiceRegistry().getService(MailService.class, true);
            /*
             * Get access
             */
            final MailAccess<?, ?> access = mailService.getMailAccess(session, MailAccount.DEFAULT_ID);
            /*
             * Check protocol
             */
            if (!IMAPProvider.PROTOCOL_IMAP.equals(access.getProvider().getProtocol())) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Primary mail account is not IMAP!");
            }
            /*
             * Check for IDLE capability
             */
            access.connect(false);
            try {
                final IMAPCapabilities capabilities = (IMAPCapabilities) access.getMailConfig().getCapabilities();
                if (!capabilities.hasIdle()) {
                    throw PushExceptionCodes.UNEXPECTED_ERROR.create("Primary IMAP account does not support \"IDLE\" capability!");
                }
                /*-
                 * No more needed because watcher recognizes IDLE state if properly set via MailAccess.setWaiting(boolean).
                 *
                 *
                final IMailProperties imcf = IMAPAccess.getInstance(session).getMailConfig().getMailProperties();
                if( imcf.isWatcherEnabled() ) {
                    LOG.error("com.openexchange.mail.watcherEnabled is enabled, please disable it!");
                    throw PushExceptionCodes.UNEXPECTED_ERROR.create("com.openexchange.mail.watcherEnabled is enabled, please disable it!");
                }
                 */
            } finally {
                access.close(true);
            }
        }
        imapIdleFuture = threadPoolService.submit(ThreadPools.task(new ImapIdlePushListenerTask(this)));
    }

    /**
     * Closes this listener.
     */
    public void close() {
        if (DEBUG_ENABLED) {
            LOG.info("stopping IDLE for Context: " + session.getContextId() + ", Login: " + session.getLoginName());
        }
        shutdown = true;
        if (null != imapIdleFuture) {
            imapIdleFuture.cancel(true);
            imapIdleFuture = null;
        }
    }

    /**
     * Check for new mails
     *
     * @throws OXException If check for new mails fails
     */
    public boolean checkNewMail() throws OXException {
        if (shutdown) {
            return false;
        }
        if (!running.compareAndSet(false, true)) {
            /*
             * Still in process...
             */
            if (DEBUG_ENABLED) {
                LOG.info(new StringBuilder(64).append("Listener still in process for user ").append(userId).append(" in context ").append(
                    contextId).append(". Return immediately.").toString());
            }
            return true;
        }
        try {
            mailAccess = mailService.getMailAccess(session, ACCOUNT_ID);
            mailAccess.connect(false);
            final IMAPFolderStorage istore;
            {
                final Object fstore = mailAccess.getFolderStorage();
                if (!(fstore instanceof IMAPFolderStorage)) {
                    throw PushExceptionCodes.UNEXPECTED_ERROR.create("Unknown MAL implementation");
                }
                istore = (IMAPFolderStorage) fstore;
            }
            final IMAPStore imapStore = istore.getImapStore();
            final IMAPFolder inbox = (IMAPFolder) imapStore.getFolder(folder);
            try {
                inbox.open(IMAPFolder.READ_WRITE);
                if (DEBUG_ENABLED) {
                    LOG.info("starting IDLE for Context: " + session.getContextId() + ", Login: " + session.getLoginName() + ", Session: " + session.getSessionID());
                }
                int deletedCount = 0;
                int totalCount = 0;
                int unreadCount = 0;
                if (PushMode.ALWAYS == pushMode) {
                    // Operations may be expensive, so only do them in always mode.
                    deletedCount = inbox.getDeletedMessageCount();
                    totalCount = inbox.getMessageCount();
                    unreadCount = inbox.getUnreadMessageCount();
                }
                mailAccess.setWaiting(true);
                try {
                    inbox.idle(true);
                } finally {
                    mailAccess.setWaiting(false);
                }
                switch (pushMode) {
                case NEWMAIL:
                    if (inbox.getNewMessageCount() > 0) {
                        if (DEBUG_ENABLED) {
                            final int nmails = inbox.getNewMessageCount();
                            LOG.info("IDLE: " + nmails + " new mail(s) for Context: " + session.getContextId() + ", Login: " + session.getLoginName());
                        }
                        notifyNewMail();
                    }
                    break;
                case ALWAYS:
                default:
                    if (inbox.getNewMessageCount() > 0) {
                        if (DEBUG_ENABLED) {
                            final int nmails = inbox.getNewMessageCount();
                            LOG.info("IDLE: " + nmails + " new mail(s) for Context: " + session.getContextId() + ", Login: " + session.getLoginName());
                        }
                        notifyNewMail();
                        break;
                    }
                    final int newDeletedCount = inbox.getDeletedMessageCount();
                    final int newTotalCount = inbox.getMessageCount();
                    final int newUnreadCount = inbox.getUnreadMessageCount();
                    if (!(newDeletedCount == deletedCount && newTotalCount == totalCount && newUnreadCount == unreadCount)) {
                        if (DEBUG_ENABLED) {
                            final StringBuilder sb = new StringBuilder("IDLE: Mail event for Context: ");
                            sb.append(session.getContextId());
                            sb.append(", Login: ");
                            sb.append(session.getLoginName());
                            sb.append(", Total: ");
                            sb.append(totalCount);
                            sb.append(',');
                            sb.append(newTotalCount);
                            sb.append(", Unread: ");
                            sb.append(unreadCount);
                            sb.append(',');
                            sb.append(newUnreadCount);
                            sb.append(", Deleted: ");
                            sb.append(deletedCount);
                            sb.append(',');
                            sb.append(newDeletedCount);
                            LOG.info(sb.toString());
                        }
                        notifyNewMail();
                    }
                    break;
                }
                /*
                 * NOTE: we cannot throw Exceptions because that would stop the IDLE'ing when e.g. IMAP server is down/busy for a moment or
                 * if e.g. cyrus client timeout happens (idling for too long)
                 */
            } finally {
                inbox.close(false);
            }
        } catch (final OXException e) {
            // throw new PushException(e);
            if ("ACC".equalsIgnoreCase(e.getPrefix()) && MailAccountExceptionCodes.NOT_FOUND.getNumber() == e.getCode()) {
                /*
                 * Missing mail account; drop listener
                 */
                LOG.info("Missing (default) mail account for user " + userId + ". Stopping obsolete IMAP-IDLE listener.");
                return false;
            }
            LOG.info("Interrupted while IDLE'ing: " + e.getMessage() + ", sleeping for " + errordelay + "ms", e);
            if (DEBUG_ENABLED) {
                LOG.error(e);
            }
            try {
                Thread.sleep(errordelay);
            } catch (final InterruptedException e1) {
                if (DEBUG_ENABLED) {
                    LOG.error("ERROR in IDLE'ing: " + e1.getMessage(), e1);
                }
            }
        } catch (final MessagingException e) {
            LOG.info("Interrupted while IDLE'ing: " + e.getMessage() + ", sleeping for " + errordelay + "ms", e);
            if (DEBUG_ENABLED) {
                LOG.error(e);
            }
            try {
                Thread.sleep(errordelay);
            } catch (final InterruptedException e1) {
                if (DEBUG_ENABLED) {
                    LOG.error("ERROR in IDLE'ing: " + e1.getMessage(), e1);
                }
            }
        } finally {
            if (null != mailAccess) {
                mailAccess.close(false);
                mailAccess = null;
            }
            running.set(false);
        }
        return true;
    }

    @Override
    public void notifyNewMail() throws OXException {
        PushUtility.triggerOSGiEvent(MailFolderUtility.prepareFullname(ACCOUNT_ID, folder), session);
    }

}
