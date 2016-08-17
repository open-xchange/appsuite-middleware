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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

import static com.openexchange.push.imapidle.ImapIdlePushManagerService.isTransient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;
import javax.mail.internet.InternetAddress;
import org.slf4j.Logger;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.imap.IMAPCapabilities;
import com.openexchange.imap.IMAPFolderStorage;
import com.openexchange.imap.IMAPProvider;
import com.openexchange.java.Strings;
import com.openexchange.mail.Protocol;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageDelegator;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.IDMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeMailExceptionCode;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.pns.DefaultPushNotification;
import com.openexchange.pns.KnownTopic;
import com.openexchange.pns.PushNotification;
import com.openexchange.pns.PushNotificationField;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.push.Container;
import com.openexchange.push.PushEventConstants;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushUtility;
import com.openexchange.push.imapidle.control.ImapIdleControl;
import com.openexchange.push.imapidle.locking.ImapIdleClusterLock;
import com.openexchange.push.imapidle.locking.SessionInfo;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.IMAPProtocol;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

/**
 * {@link ImapIdlePushListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public final class ImapIdlePushListener implements PushListener, Runnable {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ImapIdlePushListener.class);

    /** The timeout threshold; cluster lock timeout minus one minute */
    private static final long TIMEOUT_THRESHOLD_MILLIS = ImapIdleClusterLock.TIMEOUT_MILLIS - 60000L;

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
    private final ImapIdleControl control;
    private final Session session;
    private ScheduledTimerTask timerTask;
    private final int accountId;
    private final String fullName;
    private final long delayNanos;
    private final PushMode pushMode;
    private final AtomicBoolean canceled;
    private final boolean permanent;
    private final boolean supportsPermanentListeners;
    private volatile IMAPFolder imapFolderInUse;
    private volatile Map<String, Object> additionalProps;
    private volatile long lastLockRefreshNanos;
    private volatile boolean interrupted;
    private volatile boolean idling;

    /**
     * Initializes a new {@link ImapIdlePushListener}.
     */
    public ImapIdlePushListener(String fullName, int accountId, PushMode pushMode, long delay, Session session, boolean permanent, boolean supportsPermanentListeners, ImapIdleControl control, ServiceLookup services) {
        super();
        canceled = new AtomicBoolean();
        this.permanent = permanent;
        this.supportsPermanentListeners = supportsPermanentListeners;
        this.fullName = fullName;
        this.accountId = accountId;
        this.session = session;
        this.delayNanos = TimeUnit.MILLISECONDS.toNanos(delay <= 0 ? 5000L : delay);
        this.services = services;
        this.control = control;
        this.pushMode = pushMode;
        additionalProps = null;
        lastLockRefreshNanos = System.nanoTime();
    }

    private boolean isUserValid() {
        try {
            ContextService contextService = services.getService(ContextService.class);
            Context context = contextService.loadContext(session.getContextId());
            if (!context.isEnabled()) {
                return false;
            }

            UserService userService = services.getService(UserService.class);
            User user = userService.getUser(session.getUserId(), context);
            return user.isMailEnabled();
        } catch (OXException e) {
            return false;
        }
    }

    /**
     * Gets the context identifier.
     *
     * @return The context identifier
     */
    public int getContextId() {
        return session.getContextId();
    }

    /**
     * Gets the user identifier.
     *
     * @return The user identifier
     */
    public int getUserId() {
        return session.getUserId();
    }

    /**
     * Marks this IMAP-IDLE listener as interrupted while IDL'ing.
     */
    public void markInterrupted() {
        this.interrupted = true;
    }

    /**
     * Gets the permanent flag
     *
     * @return The permanent flag
     */
    public boolean isPermanent() {
        return permanent;
    }

    /**
     * Checks if this listener is currently idl'ing.
     *
     * @return <code>true</code> if idl'ing; otherwise <code>false</code>
     */
    public boolean isIdling() {
        return idling;
    }

    @Override
    public void notifyNewMail() throws OXException {
        Map<String, Object> props = this.additionalProps;

        PushNotificationService pushNotificationService = services.getOptionalService(PushNotificationService.class);
        if (null != pushNotificationService) {
            Collection<PushNotification> notifications = createNotification(props);
            if (null != notifications) {
                for (PushNotification notification : notifications) {
                    pushNotificationService.handle(notification);
                }
            }
        }

        if (null == props) {
            props = new LinkedHashMap<>(2);
        }
        props.put(PushEventConstants.PROPERTY_NO_FORWARD, Boolean.TRUE); // Do not redistribute through com.openexchange.pns.impl.event.PushEventHandler!
        PushUtility.triggerOSGiEvent(MailFolderUtility.prepareFullname(accountId, fullName), session, props, true, true);
    }

    private Collection<PushNotification> createNotification(Map<String, Object> props) {
        int userId = session.getUserId();
        int contextId = session.getContextId();

        if (null == props) {
            Map<String, Object> messageData = new LinkedHashMap<>(2);
            messageData.put(PushNotificationField.FOLDER.getId(), MailFolderUtility.prepareFullname(accountId, fullName));
            return Collections.<PushNotification> singleton(DefaultPushNotification.builder()
                .contextId(contextId)
                .userId(userId)
                .topic(KnownTopic.MAIL_NEW.getName())
                .messageData(messageData)
                .build());
        }

        Container<MailMessage> container = (Container<MailMessage>) props.get(PushEventConstants.PROPERTY_CONTAINER);
        if (null == container) {
            Map<String, Object> messageData = new LinkedHashMap<>(2);
            messageData.put(PushNotificationField.FOLDER.getId(), MailFolderUtility.prepareFullname(accountId, fullName));
            String ids = (String) props.get(PushEventConstants.PROPERTY_IDS);
            if (false == Strings.isEmpty(ids)) {
                messageData.put(PushNotificationField.ID.getId(), Strings.splitByComma(ids));
            }
            return Collections.<PushNotification> singleton(DefaultPushNotification.builder()
                .contextId(contextId)
                .userId(userId)
                .topic(KnownTopic.MAIL_NEW.getName())
                .messageData(messageData)
                .build());

        }

        // Iterate container content
        List<PushNotification> notifications = new ArrayList<>(container.size());
        for (MailMessage mailMessage : container) {
            Map<String, Object> messageData = new LinkedHashMap<>(2);
            messageData.put(PushNotificationField.FOLDER.getId(), MailFolderUtility.prepareFullname(accountId, fullName));
            messageData.put(PushNotificationField.ID.getId(), mailMessage.getMailId());
            {
                InternetAddress[] from = mailMessage.getFrom();
                if (null != from && from.length > 0) {
                    messageData.put(PushNotificationField.MAIL_SENDER.getId(), from[0].getAddress());
                }
            }
            {
                String subject = mailMessage.getSubject();
                if (null != subject) {
                    messageData.put(PushNotificationField.MAIL_SUBJECT.getId(), subject);
                }
            }
            {
                int unread = mailMessage.getUnreadMessages();
                if (unread >= 0) {
                    messageData.put(PushNotificationField.MAIL_UNREAD.getId(), Integer.valueOf(unread));
                }
            }

            notifications.add(DefaultPushNotification.builder()
                .contextId(contextId)
                .userId(userId)
                .topic(KnownTopic.MAIL_NEW.getName())
                .messageData(messageData)
                .build());
        }
        return notifications;
    }

    @Override
    public void run() {
        if (canceled.get()) {
            return;
        }

        if (!isUserValid()) {
            cancel(false);
            return;
        }

        String sContextId = Integer.toString(session.getContextId());
        String sUserId = Integer.toString(session.getUserId());

        // Set thread name
        ThreadRenamer renamer = getThreadRenamer();
        if (null != renamer) {
            renamer.renamePrefix("ImapIdle");
        }
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
                Map<String, Object> props = new HashMap<String, Object>(3);
                this.additionalProps = props;
                long uidNext = -1;
                try {
                    // The next expected UID
                    uidNext = getUIDNext(imapFolder);

                    imapFolder.open(Folder.READ_WRITE);
                    LOGGER.debug("Starting IMAP-IDLE run for user {} in context {}.", sUserId, sContextId);

                    // Acquire folder counts
                    int totalCount = imapFolder.getMessageCount();
                    int deletedCount = 0;
                    final PushMode pushMode = this.pushMode;
                    if (PushMode.ALWAYS == pushMode) {
                        // Operations may be expensive, so only do them in always mode.
                        deletedCount = imapFolder.getDeletedMessageCount();
                    }

                    // Check if canceled meanwhile
                    if (canceled.get()) {
                        error = false;
                        return;
                    }

                    // Refresh lock prior to entering IMAP-IDLE
                    if (doRefreshLock()) {
                        ImapIdlePushManagerService.getInstance().refreshLock(new SessionInfo(session, permanent, permanent ? false : isTransient(session, services)));
                    }

                    // Are there already new messages?
                    {
                        int newMessageCount = imapFolder.getNewMessageCount();
                        if (newMessageCount > 0) {
                            LOGGER.debug("IMAP-IDLE result for user {} in context {}: Doing push due to {} new mail(s)", sUserId, sContextId, Integer.toString(newMessageCount));
                            notifyNewMail();
                            notified = true;
                        }
                    }

                    // Do the IMAP IDLE connect
                    {
                        long st = System.nanoTime();

                        mailAccess.setWaiting(true);
                        try {
                            if (false == doImapIdle(imapFolder)) {
                                // Timeout elapsed
                                error = false;
                                return;
                            }
                        } finally {
                            mailAccess.setWaiting(false);
                        }

                        long parkNanos = delayNanos - (System.nanoTime() - st);
                        if (parkNanos > 0L) {
                            LockSupport.parkNanos(parkNanos);
                        }
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
                                setEventProperties(uidNext, totalCount, props, imapFolder);
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
                                setEventProperties(uidNext, totalCount, props, imapFolder);
                                notifyNewMail();
                                notified = true;
                                break;
                            }
                        }

                        // Compare deleted message counters
                        {
                            int newDeletedCount = imapFolder.getDeletedMessageCount();
                            if (imapFolder.getDeletedMessageCount() != deletedCount) {
                                LOGGER.debug("IMAP-IDLE result for user {} in context {}: Doing push due to differing message counts. Current deleted count {} vs. old deleted count {}", sUserId, sContextId, Integer.toString(newDeletedCount), Integer.toString(deletedCount));
                                setEventProperties(uidNext, totalCount, props, imapFolder);
                                notifyNewMail();
                                notified = true;
                                break;
                            }
                        }

                        // Compare total message counters
                        {
                            int newTotalCount = imapFolder.getRealMessageCount();
                            if (newTotalCount != totalCount) {
                                LOGGER.debug("IMAP-IDLE result for user {} in context {}: Doing push due to differing message counts. Current total count {} vs. old total count {}", sUserId, sContextId, Integer.toString(newTotalCount), Integer.toString(totalCount));
                                setEventProperties(uidNext, totalCount, props, imapFolder);
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
                    this.additionalProps = null;
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
        } catch (Exception e) {
            // Any aborting error
            LOGGER.warn("Severe error during IMAP-IDLE run for user {} in context {}. Therefore going to cancel associated listener permanently.", sUserId, sContextId, e);
            cancel(true);
        } finally {
            if (null != renamer) {
                renamer.restoreName();
            }
        }
    }

    /**
     * Checks whether held cluster lock needs to be refreshed.
     *
     * @return <code>true</code> if refresh is needed; otherwise <code>false</code>
     */
    private boolean doRefreshLock() {
        long last = lastLockRefreshNanos;
        long nanos = System.nanoTime();
        if (nanos - last > TimeUnit.MILLISECONDS.toNanos(TIMEOUT_THRESHOLD_MILLIS)) {
            lastLockRefreshNanos = nanos;
            return true;
        }
        return false;
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
     * @throws MessagingException If IMAP-IDLE fails for any reason
     */
    private boolean doImapIdle(IMAPFolder imapFolder) throws MessagingException {
        interrupted = false;

        control.add(this, imapFolder, TIMEOUT_THRESHOLD_MILLIS);
        try {
            // Enter IMAP-IDLE
            idling = true;
            try {
                imapFolder.idle(true);
            } finally {
                idling = false;
            }

            if (interrupted) {
                // Consciously interrupted by ImapIdleControlTask
                return false;
            }
            return true;
        } catch (MessagingException e) {
            if (interrupted) {
                // Consciously interrupted by ImapIdleControlTask
                return false;
            }
            throw e;
        } catch (RuntimeException e) {
            if (interrupted) {
                // Consciously interrupted by ImapIdleControlTask
                return false;
            }
            throw e;
        } finally {
            control.remove(this);
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
        } catch (OXException e) {
            if (!e.equalsCode(7, "CTX")) {
                throw e;
            }
            // Updating database...
        } finally {
            if (null != access) {
                access.close(false);
            }
        }

        long delay = TimeUnit.NANOSECONDS.toMillis(delayNanos);
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

            // Cancel timer task
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
                        instance.releaseLock(new SessionInfo(session, permanent, permanent ? false : isTransient(session, services)));
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
                            instance.releaseLock(new SessionInfo(session, permanent, permanent ? false : isTransient(session, services)));
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
        if (MimeMailExceptionCode.LOGIN_FAILED.equals(e)) {
            Throwable cause = null == e.getCause() ? e : e.getCause();
            throw PushExceptionCodes.AUTHENTICATION_ERROR.create(cause, new Object[0]);
        }
    }

    private long getUIDNext(IMAPFolder imapFolder) throws MessagingException {
        try {
            return imapFolder.getUIDNext();
        } catch (javax.mail.StoreClosedException e) {
            // Re-throw...
            throw e;
        } catch (MessagingException e) {
            LOGGER.warn("Could not determine UIDNEXT. Assuming -1 instead.", e);
            return -1L;
        }
    }

    private long getUIDNextCustom(IMAPFolder imapFolder) {
        try {
            final String fullName = imapFolder.getFullName();
            return ((Long) imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

                @Override
                public Object doCommand(IMAPProtocol p) throws ProtocolException {
                    String[] item = { "UIDNEXT" };
                    return Long.valueOf(p.status(fullName, item).uidnext);
                }
            })).longValue();
        } catch (MessagingException e) {
            LOGGER.warn("Could not determine UIDNEXT. Assuming -1 instead.", e);
            return -1L;
        }
    }

    private void setEventProperties(long uidNext, int totalCount, Map<String, Object> props, IMAPFolder imapFolder) {
        if (false == supportsPermanentListeners) {
            return;
        }

        // Has new messages?
        if (uidNext > 0 && totalCount >= 0) {
            try {
                int newTotalCount = imapFolder.getRealMessageCount();
                if (newTotalCount > 0 && newTotalCount > totalCount) {
                    long newUidNext = getUIDNextCustom(imapFolder);
                    if (newUidNext > 0 && uidNext != newUidNext) {
                        StringBuilder buf = new StringBuilder(64);
                        TLongList uids = new TLongArrayList();

                        buf.append(uidNext);
                        uids.add(uidNext);
                        for (long uid = uidNext + 1; uid < newUidNext; uid++) {
                            buf.append(',').append(uid);
                            uids.add(uid);
                        }
                        props.put(PushEventConstants.PROPERTY_IDS, buf.toString());

                        Container<MailMessage> container = fetchMessageInfoFor(uids.toArray(), imapFolder);
                        if (null != container) {
                            props.put(PushEventConstants.PROPERTY_CONTAINER, container);
                        }
                    }
                }
            } catch (MessagingException e) {
                LOGGER.warn("Could not determine new message count.", e);
            }
        }

        // Has deleted messages?
        if (totalCount > 0) {
            try {
                int newTotalCount = imapFolder.getRealMessageCount();
                if (newTotalCount >= 0 && newTotalCount < totalCount) {
                    props.put(PushEventConstants.PROPERTY_DELETED, Boolean.TRUE);
                }
            } catch (MessagingException e) {
                LOGGER.warn("Could not determine new message count.", e);
            }
        }
    }

    private static final FetchProfile FETCH_PROFILE_MSG_INFO = new FetchProfile() {
        {
            add(FetchProfile.Item.CONTENT_INFO);
            add(FetchProfile.Item.ENVELOPE);
            add(FetchProfile.Item.FLAGS);
            add(FetchProfile.Item.SIZE);
            add(UIDFolder.FetchProfileItem.UID);
        }
    };

    private Container<MailMessage> fetchMessageInfoFor(long[] uids, IMAPFolder imapFolder) throws MessagingException {
        try {
            int unread = imapFolder.getUnreadMessageCount();
            Message[] messages = imapFolder.getMessagesByUID(uids);
            imapFolder.fetch(messages, FETCH_PROFILE_MSG_INFO);

            String fullName = imapFolder.getFullName();
            Map<Long, MailMessage> map = new HashMap<Long, MailMessage>(messages.length);
            for (Message message : messages) {
                try {
                    IMAPMessage im = (IMAPMessage) message;
                    MailMessage mailMessage = convertMessage(im, fullName, unread);
                    map.put(Long.valueOf(im.getUID()), mailMessage);
                } catch (Exception e) {
                    LOGGER.warn("Could not handle message.", e);
                }
            }

            Container<MailMessage> container = new Container<MailMessage>();
            for (long uid : uids) {
                MailMessage mailMessage = map.get(Long.valueOf(uid));
                if (null != mailMessage) {
                    container.add(mailMessage);
                }
            }
            return container;
        } catch (javax.mail.StoreClosedException e) {
            // Re-throw...
            throw e;
        } catch (MessagingException e) {
            LOGGER.warn("Could not fetch message info.", e);
            return null;
        }
    }

    private MailMessage convertMessage(IMAPMessage im, String fullName, int unread) throws MessagingException, OXException {
        MailMessage mailMessage = new IDMailMessage(Long.toString(im.getUID()), fullName);
        mailMessage.addFrom(MimeMessageConverter.getAddressHeader(MessageHeaders.HDR_FROM, im));
        mailMessage.addTo(MimeMessageConverter.getAddressHeader(MessageHeaders.HDR_TO, im));
        mailMessage.addCc(MimeMessageConverter.getAddressHeader(MessageHeaders.HDR_CC, im));

        final Flags msgFlags = im.getFlags();
        int flags = 0;
        if (msgFlags.contains(Flags.Flag.ANSWERED)) {
            flags |= MailMessage.FLAG_ANSWERED;
        }
        if (msgFlags.contains(Flags.Flag.DELETED)) {
            flags |= MailMessage.FLAG_DELETED;
        }
        if (msgFlags.contains(Flags.Flag.DRAFT)) {
            flags |= MailMessage.FLAG_DRAFT;
        }
        if (msgFlags.contains(Flags.Flag.FLAGGED)) {
            flags |= MailMessage.FLAG_FLAGGED;
        }
        if (msgFlags.contains(Flags.Flag.RECENT)) {
            flags |= MailMessage.FLAG_RECENT;
        }
        if (msgFlags.contains(Flags.Flag.SEEN)) {
            flags |= MailMessage.FLAG_SEEN;
        }
        if (msgFlags.contains(Flags.Flag.USER)) {
            flags |= MailMessage.FLAG_USER;
        }

        mailMessage.setFlags(flags);

        mailMessage.addBcc(MimeMessageConverter.getAddressHeader(MessageHeaders.HDR_BCC, im));
        {
            String[] tmp = im.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
            if ((tmp != null) && (tmp.length > 0)) {
                mailMessage.setContentType(MimeMessageUtility.decodeMultiEncodedHeader(tmp[0]));
            } else {
                mailMessage.setContentType(MimeTypes.MIME_DEFAULT);
            }
        }
        mailMessage.setSentDate(MimeMessageConverter.getSentDate(im));
        try {
            mailMessage.setSize(im.getSize());
        } catch (final Exception e) {
            // Size unavailable
            mailMessage.setSize(-1);
        }
        mailMessage.setSubject(MimeMessageConverter.getSubject(im));
        mailMessage.setUnreadMessages(unread);
        return mailMessage;
    }

    private ThreadRenamer getThreadRenamer() {
        Thread t = Thread.currentThread();
        return (t instanceof ThreadRenamer) ? (ThreadRenamer)t : null;
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
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Unknown MAL implementation: " + fstore.getClass().getName());
            }
            fstore = ((IMailFolderStorageDelegator) fstore).getDelegateFolderStorage();
            if (!(fstore instanceof IMAPFolderStorage)) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create("Unknown MAL implementation: " + fstore.getClass().getName());
            }
        }
        return (IMAPFolderStorage) fstore;
    }

}
