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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.push.PushEventConstants;
import com.openexchange.push.PushException;
import com.openexchange.push.PushListener;
import com.openexchange.push.malpoll.services.MALPollServiceRegistry;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link MALPollPushListener} - The MAL poll {@link PushListener}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MALPollPushListener implements PushListener {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MALPollPushListener.class);

    private static final boolean DEBUG_ENABLED = LOG.isDebugEnabled();

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
        this.userId = session.getUserId();
        this.contextId = session.getContextId();
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
     * @throws PushException If listener cannot be opened
     */
    public void open() throws PushException {
        if (ignoreOnGlobal) {
            /*
             * This listener gets its own timer task and is not considered during global run
             */
            final TimerService timerService;
            try {
                timerService = MALPollServiceRegistry.getServiceRegistry().getService(TimerService.class, true);
            } catch (final ServiceException e) {
                throw new PushException(e);
            }
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
     * @throws PushException If check for new mails fails
     */
    public void checkNewMail() throws PushException {
        if (!running.compareAndSet(false, true)) {
            /*
             * Still in process...
             */
            if (DEBUG_ENABLED) {
                LOG.debug(new StringBuilder(64).append("Listener still in process for user ").append(userId).append(" in context ").append(
                    contextId).append(". Return immediately.").toString());
            }
            return;
        }
        try {
            final MailService mailService = MALPollServiceRegistry.getServiceRegistry().getService(MailService.class, true);
            if (started) {
                subsequentRun(mailService);
            } else {
                firstRun(mailService);
                started = true;
            }
        } catch (final ServiceException e) {
            throw new PushException(e);
        } catch (final MailException e) {
            throw new PushException(e);
        } finally {
            running.set(false);
        }
    }

    private void firstRun(final MailService mailService) throws PushException, MailException {
        final long s = DEBUG_ENABLED ? System.currentTimeMillis() : 0L;
        /*
         * First run
         */
        final String fullname = folder;
        MALPollDBUtility.dropMailIDs(contextId, userId, ACCOUNT_ID, fullname);
        final Set<String> uids = gatherUIDs(mailService);
        /*
         * Insert
         */
        final String insertedHash = MALPollDBUtility.insertHash(contextId, userId, ACCOUNT_ID, fullname);
        MALPollDBUtility.insertMailIDs(insertedHash, uids, contextId);
        if (DEBUG_ENABLED) {
            final long d = System.currentTimeMillis() - s;
            LOG.debug("First run took " + d + "msec");
        }
    }

    private void subsequentRun(final MailService mailService) throws PushException, MailException {
        final long s = DEBUG_ENABLED ? System.currentTimeMillis() : 0L;
        /*
         * Subsequent run
         */
        final String hash = MALPollDBUtility.getHash(contextId, userId, ACCOUNT_ID, folder);
        if (null == hash) {
            return;
        }
        final Set<String> newIds;
        final Set<String> delIds;
        {
            final Set<String> fetchedUids = gatherUIDs(mailService);
            final Set<String> dbUids = MALPollDBUtility.getMailIDs(hash, contextId);
            /*
             * Check for new mails
             */
            newIds = new HashSet<String>(fetchedUids);
            newIds.removeAll(dbUids);
            /*
             * Check for deleted mails
             */
            delIds = new HashSet<String>(dbUids);
            delIds.removeAll(fetchedUids);
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
        if (DEBUG_ENABLED) {
            final long d = System.currentTimeMillis() - s;
            LOG.debug("Subsequent run took " + d + "msec");
        }
    }

    private Set<String> gatherUIDs(final MailService mailService) throws MailException {
        final MailAccess<?, ?> mailAccess = mailService.getMailAccess(session, ACCOUNT_ID);
        mailAccess.connect();
        try {
            final String fullname = folder;
            final Set<String> uidSet = new HashSet<String>(mailAccess.getFolderStorage().getFolder(fullname).getMessageCount());
            final MailMessage[] messages =
                mailAccess.getMessageStorage().searchMessages(fullname, null, MailSortField.RECEIVED_DATE, OrderDirection.ASC, null, FIELDS);
            for (final MailMessage mailMessage : messages) {
                uidSet.add(mailMessage.getMailId());
            }
            return uidSet;
        } finally {
            mailAccess.close(true);
        }
    }

    public void notifyNewMail() throws PushException {
        final EventAdmin eventAdmin;
        try {
            eventAdmin = MALPollServiceRegistry.getServiceRegistry().getService(EventAdmin.class, true);
        } catch (final ServiceException e) {
            throw new PushException(e);
        }
        /*
         * Create event's properties
         */
        final Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(PushEventConstants.PROPERTY_CONTEXT, Integer.valueOf(contextId));
        properties.put(PushEventConstants.PROPERTY_USER, Integer.valueOf(userId));
        properties.put(PushEventConstants.PROPERTY_SESSION, session);
        properties.put(PushEventConstants.PROPERTY_FOLDER, MailFolderUtility.prepareFullname(ACCOUNT_ID, folder));
        /*
         * Create event with push topic
         */
        final Event event = new Event(PushEventConstants.TOPIC, properties);
        /*
         * Finally post it
         */
        eventAdmin.postEvent(event);
        if (DEBUG_ENABLED) {
            LOG.debug(new StringBuilder(64).append("Notified new mails in folder \"").append(folder).append("\" for user ").append(userId).append(
                " in context ").append(contextId).toString());
        }
    }

    /**
     * Checks if this listener has been started.
     * 
     * @return <code>true</code> if this listener has been started; otherwise <code>false</code>
     */
    public boolean isStarted() {
        return started;
    }

}
