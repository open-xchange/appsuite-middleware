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

package com.openexchange.push.malpoll;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.pns.DefaultPushNotification;
import com.openexchange.pns.KnownTopic;
import com.openexchange.pns.PushNotification;
import com.openexchange.pns.PushNotificationField;
import com.openexchange.pns.PushNotificationService;
import com.openexchange.push.PushEventConstants;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushUtility;
import com.openexchange.push.malpoll.services.MALPollServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link MALPollPushListener} - The MAL poll {@link PushListener}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MALPollPushListener implements PushListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MALPollPushListener.class);

    private static final MailField[] FIELDS = new MailField[] { MailField.ID };

    /**
     * A placeholder constant for account ID.
     */
    private static final int ACCOUNT_ID = 0;

    private static volatile String folder;

    private static volatile long periodMillis;

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
        MALPollPushListener.folder = folder;
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
     * Sets static period milliseconds.
     *
     * @param periodMillis The period milliseconds
     */
    public static void setPeriodMillis(final long periodMillis) {
        MALPollPushListener.periodMillis = periodMillis;
    }

    /**
     * Initializes a new {@link MALPollPushListener}.
     *
     * @param session The needed session to obtain and connect mail access instance
     * @param startTimerTask <code>true</code> to start a timer task for this listener
     * @return A new {@link MALPollPushListener}.
     */
    public static MALPollPushListener newInstance(final Session session, final boolean startTimerTask) {
        return new MALPollPushListener(session, startTimerTask);
    }

    /*
     * Member section
     */

    private final AtomicBoolean running;

    private final Session session;

    private final int userId;

    private final int contextId;

    private final boolean ignoreOnGlobal;

    private volatile ScheduledTimerTask timerTask;

    private volatile boolean started;

    /**
     * Initializes a new {@link MALPollPushListener}.
     *
     * @param session The needed session to obtain and connect mail access instance
     * @param ignoreOnGlobal <code>true</code> to ignore during global run
     */
    private MALPollPushListener(final Session session, final boolean ignoreOnGlobal) {
        super();
        running = new AtomicBoolean();
        this.session = session;
        this.ignoreOnGlobal = ignoreOnGlobal;
        userId = session.getUserId();
        contextId = session.getContextId();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128).append("session-ID=").append(session.getSessionID());
        sb.append(", user=").append(userId).append(", context=").append(contextId);
        sb.append(", startTimerTask=").append(!ignoreOnGlobal);
        return sb.toString();
    }

    /**
     * Opens this listener (if {@link #isIgnoreOnGlobal()} returns <code>false</code>).
     *
     * @throws OXException If listener cannot be opened
     */
    public void open() throws OXException {
        if (ignoreOnGlobal) {
            /*
             * This listener gets its own timer task and is not considered during global run
             */
            final TimerService timerService = MALPollServiceRegistry.getServiceRegistry().getService(TimerService.class, true);
            timerTask = timerService.scheduleWithFixedDelay(new MALPollPushListenerRunnable(this), 1000, periodMillis);
        }
    }

    /**
     * Closes this listener.
     */
    public void close() {
        final ScheduledTimerTask scheduledTimerTask = timerTask;
        if (null != scheduledTimerTask) {
            /*
             * Release all timer task resources
             */
            scheduledTimerTask.cancel();
            timerTask = null;
            /*
             * ... and purge from timer service
             */
            final TimerService timerService = MALPollServiceRegistry.getServiceRegistry().getService(TimerService.class);
            if (null != timerService) {
                timerService.purge();
            }
        }
    }

    /**
     * Checks whether to ignore this listener on global run.
     *
     * @return <code>true</code> to ignore this listener on global run; otherwise <code>false</code>
     */
    public boolean isIgnoreOnGlobal() {
        return ignoreOnGlobal;
    }

    /**
     * Check for new mails
     *
     * @throws OXException If check for new mails fails
     */
    public void checkNewMail() throws OXException {
        if (!running.compareAndSet(false, true)) {
            /*
             * Still in process...
             */
            LOG.debug("Listener still in process for user {} in context {}. Return immediately.", userId, contextId);
            return;
        }
        try {
            ContextService contextService = MALPollServiceRegistry.getServiceRegistry().getService(ContextService.class, true);
            Context context = contextService.getContext(contextId);
            if (context.isReadOnly()) {
                return;
            }

            MailService mailService = MALPollServiceRegistry.getServiceRegistry().getService(MailService.class, true);
            if (started) {
                subsequentRun(mailService);
            } else {
                firstRun(mailService);
                started = true;
            }
        } finally {
            running.set(false);
        }
    }

    private void firstRun(final MailService mailService) throws OXException, OXException {
        /*
         * First run
         */
        String fullname = folder;

        UUID hash;
        boolean loadDBIDs;
        do {
            hash = MALPollDBUtility.getHash(contextId, userId, ACCOUNT_ID, fullname);
            loadDBIDs = true;
            if (null == hash) {
                // Insert new hash
                hash = MALPollDBUtility.insertHash(contextId, userId, ACCOUNT_ID, fullname);
                if (null != hash) {
                    loadDBIDs = false;
                }
            }
        } while (null == hash);

        // Synchronize
        synchronizeIDs(mailService, hash, loadDBIDs);
    }

    private void subsequentRun(final MailService mailService) throws OXException, OXException {
        /*
         * Subsequent run
         */
        final UUID hash = MALPollDBUtility.getHash(contextId, userId, ACCOUNT_ID, folder);
        if (null == hash) {
            return;
        }
        synchronizeIDs(mailService, hash, true);
    }

    private void synchronizeIDs(final MailService mailService, final UUID hash, final boolean loadDBIDs) throws OXException, OXException {
        final Set<String> newIds;
        final Set<String> delIds;
        {
            final Set<String> fetchedUids;
            try {
                fetchedUids = gatherUIDs(mailService);
            } catch (final OXException e) {
                if (MailExceptionCode.ACCOUNT_DOES_NOT_EXIST.equals(e)) {
                    LOG.debug("", e);
                    /*
                     * Nothing to synchronize
                     */
                    return;
                }
                throw e;
            }
            final Set<String> dbUids;
            if (loadDBIDs) {
                dbUids = MALPollDBUtility.getMailIDs(hash, contextId);
            } else {
                dbUids = Collections.emptySet();
            }
            /*
             * Check for new mails
             */
            if (fetchedUids.isEmpty()) {
                newIds = new HashSet<String>(dbUids);
            } else {
                newIds = new HashSet<String>(fetchedUids);
                newIds.removeAll(dbUids);
            }
            /*
             * Check for deleted mails
             */
            if (dbUids.isEmpty()) {
                delIds = Collections.emptySet();
            } else {
                delIds = new HashSet<String>(dbUids);
                delIds.removeAll(fetchedUids);
            }
        }
        /*
         * Notify (if necessary) and update DB
         */
        if (!newIds.isEmpty()) {
            /*
             * New IDs available, so notify & update DB
             */
            notifyNewMail();
            MALPollDBUtility.replaceMailIDs(hash, newIds, delIds, contextId);
        } else if (!delIds.isEmpty()) {
            /*
             * Deleted IDs detected, so update DB
             */
            MALPollDBUtility.replaceMailIDs(hash, newIds, delIds, contextId);
        }
    }

    private Set<String> gatherUIDs(final MailService mailService) throws OXException {
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, ACCOUNT_ID);
            mailAccess.connect();
            String fullname = folder;

            /*-
             *
            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
            if (folderStorage instanceof com.openexchange.mail.api.IMailFolderStorageStatusSupport) {
                com.openexchange.mail.api.IMailFolderStorageStatusSupport statusSupport = (com.openexchange.mail.api.IMailFolderStorageStatusSupport) folderStorage;
                MailFolderStatus folderStatus = statusSupport.getFolderStatus(fullname);
                folderStatus.getNextId()
            }
             *
             */

            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            MailMessage[] messages = messageStorage.searchMessages(fullname, null, MailSortField.RECEIVED_DATE, OrderDirection.ASC, null, FIELDS);
            Set<String> uidSet = new HashSet<String>(messages.length);
            for (MailMessage mailMessage : messages) {
                uidSet.add(mailMessage.getMailId());
            }
            return uidSet;
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }

    @Override
    public void notifyNewMail() throws OXException {
        String folderId = MailFolderUtility.prepareFullname(ACCOUNT_ID, folder);

        MALPollServiceRegistry serviceRegistry = MALPollServiceRegistry.getServiceRegistry();
        PushNotificationService pushNotificationService = serviceRegistry.getService(PushNotificationService.class);
        if (null != pushNotificationService) {
            PushNotification notification = createNotification(folderId);
            if (null != notification) {
                pushNotificationService.handle(notification);
            }
        }

        Map<String, Object> props = new LinkedHashMap<>(2);
        props.put(PushEventConstants.PROPERTY_NO_FORWARD, Boolean.TRUE); // Do not redistribute through com.openexchange.pns.impl.event.PushEventHandler!
        PushUtility.triggerOSGiEvent(folderId, session, props, true, false);
    }

    private PushNotification createNotification(String folderId) {
        int userId = session.getUserId();
        int contextId = session.getContextId();

        Map<String, Object> messageData = new LinkedHashMap<>(2);
        messageData.put(PushNotificationField.FOLDER.getId(), folderId);
        return DefaultPushNotification.builder()
            .contextId(contextId)
            .userId(userId)
            .topic(KnownTopic.MAIL_NEW.getName())
            .messageData(messageData)
            .build();
    }

    /**
     * Checks if this listener has been started.
     *
     * @return <code>true</code> if this listener has been started; otherwise <code>false</code>
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Gets the session.
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

}
