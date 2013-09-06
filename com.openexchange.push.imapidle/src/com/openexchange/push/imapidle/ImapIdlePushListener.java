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

package com.openexchange.push.imapidle;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.mail.Folder;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPCapabilities;
import com.openexchange.imap.IMAPFolderStorage;
import com.openexchange.imap.IMAPProvider;
import com.openexchange.mail.Protocol;
import com.openexchange.mail.api.IMailFolderStorageDelegator;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.push.PushClientWhitelist;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushUtility;
import com.openexchange.push.imapidle.services.ImapIdleServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.AbstractSessionMatcher;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link ImapIdlePushListener} - The IMAP IDLE {@link PushListener}.
 */
public final class ImapIdlePushListener implements PushListener, Runnable {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(ImapIdlePushListener.class));

    /**
     * @author choeger
     *
     */
    public static enum PushMode {
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

    private static volatile Boolean DEBUG_ENABLED;

    /**
     * @param debugEnabled the debugEnabled to set
     */
    public static final void setDebugEnabled(final boolean debugEnabled) {
        DEBUG_ENABLED = Boolean.valueOf(debugEnabled);
        ImapIdlePushListenerRegistry.setDebugEnabled(debugEnabled);
    }

    private static boolean isDebugEnabled() {
        final Boolean debug = DEBUG_ENABLED;
        return null == debug ? LOG.isDebugEnabled() : debug.booleanValue();
    }

    private static volatile PushMode pushMode;

    /**
     * @param pushmode the pushmode to set
     */
    public static final void setPushmode(final PushMode pushmode) {
        pushMode = pushmode;
    }

    /**
     * A placeholder constant for account ID.
     */
    private static final int ACCOUNT_ID = 0;

    private static volatile String folder;

    private static volatile int errordelay;

    /**
     * Gets the account ID constant.
     *
     * @return The account ID constant
     */
    public static int getAccountId() {
        return ACCOUNT_ID;
    }

    /**
     * Sets static folder full name.
     *
     * @param folder The folder full name
     */
    public static void setFolder(final String folder) {
        ImapIdlePushListener.folder = folder;
    }

    /**
     * Gets static folder full name.
     *
     * @return The folder full name
     */
    public static String getFolder() {
        return folder;
    }

    /**
     * Initializes a new {@link ImapIdlePushListener}.
     *
     * @param session The user session
     * @return A new {@link ImapIdlePushListener}.
     */
    public static ImapIdlePushListener newInstance(final Session session) {
        return new ImapIdlePushListener(session);
    }

    /**
     * Initializes a new {@link ImapIdlePushListener}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return A new {@link ImapIdlePushListener}.
     */
    public static ImapIdlePushListener newInstance(final int userId, final int contextId) {
        return new ImapIdlePushListener(userId, contextId);
    }

    /*
     * Member section
     */

    private final AtomicBoolean running;

    private final AtomicReference<Session> sessionRef;

    private final ConcurrentMap<String, String> invalidSessionIds;

    private final int userId;

    private final int contextId;

    private volatile Future<Object> imapIdleFuture;

    private MailService mailService;

    private volatile boolean shutdown;

    /**
     * Initializes a new {@link ImapIdlePushListener}.
     */
    private ImapIdlePushListener(final Session session) {
        super();
        running = new AtomicBoolean();
        sessionRef = new AtomicReference<Session>(session);
        invalidSessionIds = new ConcurrentHashMap<String, String>(2);
        userId = session.getUserId();
        contextId = session.getContextId();
        mailService = null;
        errordelay = 1000;
        shutdown = false;
    }

    /**
     * Initializes a new {@link ImapIdlePushListener}.
     */
    private ImapIdlePushListener(final int userId, final int contextId) {
        super();
        running = new AtomicBoolean();
        sessionRef = new AtomicReference<Session>();
        invalidSessionIds = new ConcurrentHashMap<String, String>(2);
        this.userId = userId;
        this.contextId = contextId;
        mailService = null;
        errordelay = 1000;
        shutdown = false;
    }

    /**
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
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
        final StringBuilder sb = new StringBuilder(128).append("user=").append(userId).append(", context=").append(contextId);
        sb.append(", imapIdleFuture=").append(imapIdleFuture);
        return sb.toString();
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public Session getSession() {
        Session session = sessionRef.get();
        if (null == session) {
            final SessiondService service = ImapIdleServiceRegistry.getServiceRegistry().getService(SessiondService.class);
            if (invalidSessionIds.isEmpty()) {
                session = service.findFirstMatchingSessionForUser(userId, contextId, new AbstractSessionMatcher() {

                    @Override
                    public boolean accepts(final Session tmp) {
                        return PushUtility.allowedClient(tmp.getClient());
                    }

                });
            } else {
                final Collection<Session> sessions = service.getSessions(userId, contextId);
                for (final Session s : sessions) {
                    if (!invalidSessionIds.containsKey(s.getSessionID()) && PushUtility.allowedClient(s.getClient())) {
                        session = s;
                    }
                }
                invalidSessionIds.clear();
            }
            if (!sessionRef.compareAndSet(null, session)) {
                session = sessionRef.get();
            }
        }
        return session;
    }

    private void dropSessionRef(final boolean failedAuth) {
        Session session;
        do {
            session = sessionRef.get();
            if (null == session) {
                return;
            }
        } while (!sessionRef.compareAndSet(session, null));
        if (failedAuth) {
            final String sessionID = session.getSessionID();
            invalidSessionIds.put(sessionID, sessionID);
        }
    }

    /**
     * Opens this listener
     *
     * @throws OXException If listener cannot be opened
     */
    public void open() throws OXException {
        shutdown = false;
        final ThreadPoolService threadPoolService;
        {
            threadPoolService = ThreadPools.getThreadPool();
            mailService = ImapIdleServiceRegistry.getServiceRegistry().getService(MailService.class, true);
            final Session session = getSession();
            if (null == session) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Cannot find an appropriate session with a client identifier matching pattern(s): " + PushClientWhitelist.getInstance().getPatterns());
            }
            /*
             * Get access
             */
            MailAccess<?, ?> access = null;
            try {
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
                if (null != access) {
                    access.close(true);
                }
            }
        }
        imapIdleFuture = threadPoolService.submit(ThreadPools.task(this, getClass().getName()));
    }

    /**
     * Closes this listener.
     */
    public void close() {
        if (shutdown) {
            return;
        }
        shutdown = true;
        if (isDebugEnabled()) {
            final Session session = getSession();
            LOG.info("stopping IDLE for Context: " + contextId + ", Login: " + (null == session ? "unknown" : session.getLoginName()), new Throwable("Closing IMAP IDLE push listener"));
        }
        if (null != imapIdleFuture) {
            imapIdleFuture.cancel(true);
            imapIdleFuture = null;
        }
    }

    @Override
    public void run() {
        /*
         * Periodically invoke #checkNewMail() unless not shut-down
         */
        try {
            Run: while (!shutdown) {
                try {
                    while (checkNewMail()) {
                        // Nothing...
                    }
                } catch (final MissingSessionException e) {
                    LOG.info(e.getMessage());
                    /*
                     * Bind ImapIdlePushListener to another session
                     */
                    final SessiondService sessiondService = ImapIdleServiceRegistry.getServiceRegistry().getService(SessiondService.class);
                    final Session session = sessiondService.findFirstMatchingSessionForUser(userId, contextId, new AbstractSessionMatcher() {

                        @Override
                        public boolean accepts(final Session tmp) {
                            return PushUtility.allowedClient(tmp.getClient());
                        }

                    });
                    if (null == session) {
                        final String message =
                            "IDLE: Found no other valid & active session for user " + userId + " in context " + contextId + ". Therefore shutting down associated IMAP IDLE push listener.";
                        if (isDebugEnabled()) {
                            LOG.info(message, new Throwable());
                        } else {
                            LOG.info(message);
                        }
                        return;
                    }
                    LOG.info("IDLE: Found another valid & active session for user " + userId + " in context " + contextId + ". Reactivating IMAP IDLE push listener.");
                    sessionRef.set(session);
                    continue Run;
                }
                if (shutdown) {
                    if (LOG.isDebugEnabled()) {
                        LOG.info("IDLE: Listener has been shut down for user " + userId + " in context " + contextId);
                    }
                    return;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.info("IDLE: Orderly left checkNewMail() method for user " + userId + " in context " + contextId);
                }
            }
        } catch (final Exception e) {
            LOG.error("IDLE: Unexpectedly left run() method for user " + userId + " in context " + contextId, e);
        } finally {
            if (ImapIdlePushListenerRegistry.getInstance().purgeUserPushListener(this)) {
                try {
                    ImapIdlePushListenerRegistry.getInstance().removePushListener(contextId, userId);
                } catch (final Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Check for new mails
     *
     * @throws OXException If check for new mails fails
     * @throws MissingSessionException If session is <code>null</code>
     */
    public boolean checkNewMail() throws OXException {
        if (shutdown) {
            if (isDebugEnabled()) {
                LOG.info("IDLE: Listener was requested to shut-down for associated user " + userId + " in context " + contextId + ". Abort...", new Throwable());
            } else {
                LOG.info("IDLE: Listener was requested to shut-down for associated user " + userId + " in context " + contextId + ". Abort...");
            }
            return false;
        }
        if (!running.compareAndSet(false, true)) {
            /*
             * Still in process...
             */
            if (isDebugEnabled()) {
                LOG.info(new StringBuilder(64).append("Listener still in process for user ").append(userId).append(" in context ").append(
                    contextId).append(". Return immediately.").toString());
            }
            return true;
        }
        final int errDelay = errordelay;
        MailAccess<?, ?> mailAccess = null;
        try {
            final Session session = getSession();
            if (null == session) {
                // No active session found for associated user. Abort...
                throw new MissingSessionException("IDLE: No active session found for associated user " + userId + " in context " + contextId + ". Abort...");
            }
            mailAccess = mailService.getMailAccess(session, ACCOUNT_ID);
            mailAccess.connect(false);
            final IMAPFolderStorage istore;
            {
                Object fstore = mailAccess.getFolderStorage();
                if (!(fstore instanceof IMAPFolderStorage)) {
                    if (!(fstore instanceof IMailFolderStorageDelegator)) {
                        throw PushExceptionCodes.UNEXPECTED_ERROR.create("Unknown MAL implementation");
                    }
                    fstore = ((IMailFolderStorageDelegator) fstore).getDelegateFolderStorage();
                    if (!(fstore instanceof IMAPFolderStorage)) {
                        throw PushExceptionCodes.UNEXPECTED_ERROR.create("Unknown MAL implementation");
                    }
                }
                istore = (IMAPFolderStorage) fstore;
            }
            final IMAPStore imapStore = istore.getImapStore();
            final IMAPFolder inbox = (IMAPFolder) imapStore.getFolder(folder);
            try {
                inbox.open(Folder.READ_WRITE);
                if (isDebugEnabled()) {
                    LOG.info("starting IDLE for Context: " + session.getContextId() + ", Login: " + session.getLoginName() + ", Session: " + session.getSessionID());
                }
                int deletedCount = 0;
                int totalCount = 0;
                int unreadCount = 0;
                final PushMode pushMode = ImapIdlePushListener.pushMode;
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
                        if (isDebugEnabled()) {
                            final int nmails = inbox.getNewMessageCount();
                            LOG.info("IDLE: " + nmails + " new mail(s) for Context: " + session.getContextId() + ", Login: " + session.getLoginName());
                        }
                        notifyNewMail();
                    }
                    break;
                case ALWAYS:
                default:
                    if (inbox.getNewMessageCount() > 0) {
                        if (isDebugEnabled()) {
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
                        if (isDebugEnabled()) {
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
            if ("PUSH".equals(e.getPrefix())) {
                throw e;
            }
            // throw new PushException(e);
            if ("ACC".equalsIgnoreCase(e.getPrefix()) && MailAccountExceptionCodes.NOT_FOUND.getNumber() == e.getCode()) {
                /*
                 * Missing mail account; drop listener
                 */
                LOG.debug("Missing (default) mail account for user " + userId + ". Stopping obsolete IMAP-IDLE listener.");
                return false;
            }
            dropSessionRef("MSG".equals(e.getPrefix()) && 1001 == e.getCode());
            LOG.info("Interrupted while IDLE'ing: " + e.getMessage() + ", sleeping for " + errDelay + "ms", e);
            if (isDebugEnabled()) {
                LOG.error(e.getMessage(), e);
            }
            try {
                Thread.sleep(errDelay);
            } catch (final InterruptedException e1) {
                Thread.currentThread().interrupt();
                if (isDebugEnabled()) {
                    LOG.error("ERROR in IDLE'ing: " + e1.getMessage(), e1);
                }
            }
        } catch (final MessagingException e) {
            dropSessionRef(e instanceof javax.mail.AuthenticationFailedException);
            LOG.info("Interrupted while IDLE'ing: " + e.getMessage() + ", sleeping for " + errDelay + "ms", e);
            if (isDebugEnabled()) {
                LOG.error(e.getMessage(), e);
            }
            try {
                Thread.sleep(errDelay);
            } catch (final InterruptedException e1) {
                Thread.currentThread().interrupt();
                if (isDebugEnabled()) {
                    LOG.error("ERROR in IDLE'ing: " + e1.getMessage(), e1);
                }
            }
        } catch (final MissingSessionException e) {
            throw e;
        } catch (final RuntimeException e) {
            dropSessionRef(false);
            LOG.info("Interrupted while IDLE'ing: " + e.getMessage() + ", sleeping for " + errDelay + "ms", e);
            if (isDebugEnabled()) {
                LOG.error(e.getMessage(), e);
            }
            try {
                Thread.sleep(errDelay);
            } catch (final InterruptedException e1) {
                Thread.currentThread().interrupt();
                if (isDebugEnabled()) {
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
        PushUtility.triggerOSGiEvent(MailFolderUtility.prepareFullname(ACCOUNT_ID, folder), getSession());
    }

    private static final class MissingSessionException extends RuntimeException {

        private static final long serialVersionUID = -6008627356112015806L;

        /**
         * Initializes a new {@link MissingSessionException}.
         *
         * @param message The message
         */
        public MissingSessionException(final String message) {
            super(message);
        }

    }

}
