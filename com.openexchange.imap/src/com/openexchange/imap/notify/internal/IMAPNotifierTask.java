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

package com.openexchange.imap.notify.internal;

import static com.openexchange.mail.dataobjects.MailFolder.DEFAULT_FOLDER_ID;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.UIDFolder.FetchProfileItem;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.imap.IMAPFolderStorage;
import com.openexchange.imap.config.IMAPProperties;
import com.openexchange.imap.services.Services;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.BASE64MailboxEncoder;

/**
 * {@link IMAPNotifierTask}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPNotifierTask {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IMAPNotifierTask.class);

    private final int accountId;

    private final int user;

    private final int context;

    private final ConcurrentMap<String, String> fullNames;

    private volatile ScheduledTimerTask scheduledTask;

    /**
     * Initializes a new {@link IMAPNotifierTask}.
     *
     * @param accountId The account identifier
     * @param session The session providing user information
     */
    public IMAPNotifierTask(final int accountId, final Session session) {
        super();
        this.accountId = accountId;
        this.user = session.getUserId();
        this.context = session.getContextId();
        fullNames = new NonBlockingHashMap<String, String>(2);
    }

    /**
     * Adds specified full name(s).
     *
     * @param fullName The first full name
     * @param other Optional further full names
     * @return This notifier task with specified full name added for chained invocations
     */
    public IMAPNotifierTask addFullName(final String fullName, final String... other) {
        fullNames.put(BASE64MailboxEncoder.encode(fullName).toUpperCase(Locale.US), fullName);
        if (null != other && other.length > 0) {
            for (int i = 0; i < other.length; i++) {
                final String fn = other[i];
                fullNames.put(BASE64MailboxEncoder.encode(fn).toUpperCase(Locale.US), fn);
            }
        }
        return this;
    }

    /**
     * Adds specified full names.
     *
     * @param fullNames The full names
     * @return This notifier task with specified full names added for chained invocations
     */
    public IMAPNotifierTask addFullNames(final String[] fullNames) {
        if (null != fullNames && fullNames.length > 0) {
            for (int i = 0; i < fullNames.length; i++) {
                final String fn = fullNames[i];
                this.fullNames.put(BASE64MailboxEncoder.encode(fn).toUpperCase(Locale.US), fn);
            }
        }
        return this;
    }

    /**
     * Adds specified full name if not already present.
     *
     * @param fullName The full name
     * @return <code>true</code> if full name is added; otherwise <code>false</code>
     */
    public boolean addFullNameIfAbsent(final String fullName) {
        return (null == fullNames.putIfAbsent(BASE64MailboxEncoder.encode(fullName).toUpperCase(Locale.US), fullName));
    }

    /**
     * Removes specified full name.
     *
     * @param fullName The full name
     * @return The possibly removed full name or <code>null</code> if none was removed
     */
    public String removeFullName(final String fullName) {
        return fullNames.remove(BASE64MailboxEncoder.encode(fullName).toUpperCase(Locale.US));
    }

    /**
     * Shuts down this notifier task.
     */
    public void shutDown() {
        final ScheduledTimerTask scheduledTask = this.scheduledTask;
        if (null != scheduledTask) {
            scheduledTask.cancel(false);
            this.scheduledTask = null;
            /*
             * Purge from timer service
             */
            final TimerService timerService = Services.getService(TimerService.class);
            if (null != timerService) {
                timerService.purge();
            }
        }
    }

    /**
     * Starts up this notifier task.
     *
     * @return <code>true</code> if this task has been successfully started; otherwise <code>false</code>
     */
    public boolean startUp() {
        final TimerService timerService = Services.getService(TimerService.class);
        if (null == timerService) {
            return false;
        }

        final int freqMillis = IMAPProperties.getInstance().getNotifyFrequencySeconds() * 1000;
        scheduledTask = timerService.scheduleWithFixedDelay(new IMAPNotifierTaskRunnable(accountId, user, context, fullNames), freqMillis, freqMillis);
        return true;
    }

    /**
     * The fetch profile carrying only UID fetch item.
     */
    protected static final FetchProfile UID_FETCH_PROFILE = new FetchProfile() {

        {
            add(FetchProfileItem.UID);
        }
    };

    private static final class IMAPNotifierTaskRunnable implements Runnable {

        private final ConcurrentMap<String, String> fullNames;

        private final int accountId;

        private final int user;

        private final int context;

        protected IMAPNotifierTaskRunnable(final int accountId, final int user, final int context, final ConcurrentMap<String, String> fullNames) {
            super();
            this.accountId = accountId;
            this.user = user;
            this.context = context;
            this.fullNames = fullNames;
        }

        @Override
        public void run() {
            try {
                final Iterator<String> iter = fullNames.values().iterator();
                /*
                 * No full names present, yet
                 */
                if (!iter.hasNext()) {
                    return;
                }
                /*
                 * Get connected IMAP access
                 */
                MailAccess<?, ?> access = null;
                try {
                    access = MailAccess.getInstance(user, context, accountId);
                    access.connect(false);
                    final IMAPFolderStorage imapFolderStorage =  (IMAPFolderStorage) access.getFolderStorage();
                    final IMAPStore imapStore = imapFolderStorage.getImapStore();
                    final Session session = imapFolderStorage.getSession();
                    /*
                     * Open folders to trigger recent listener if any recent message are available
                     */
                    do {
                        final IMAPFolder imapFolder = getIMAPFolder(iter.next(), imapStore, session);
                        /*
                         * Open in read-write mode to clear \Recent flag(s) to not notify multiple times for the same recent messages
                         */
                        imapFolder.open(Folder.READ_WRITE);
                        imapFolder.close(true);
                    } while (iter.hasNext());
                } finally {
                    if (null != access) {
                        access.close(true);
                    }
                }
            } catch (final Exception e) {
                LOG.warn("Failed IMAP notifier task run.", e);
            }
        }

        private IMAPFolder getIMAPFolder(final String fullName, final IMAPStore imapStore, final Session session) throws MessagingException {
            final IMAPFolder ret =
                DEFAULT_FOLDER_ID.equals(fullName) ? (IMAPFolder) imapStore.getDefaultFolder() : (IMAPFolder) imapStore.getFolder(fullName);
            IMAPNotifierMessageRecentListener.addNotifierFor(ret, fullName, accountId, session, true);
            return ret;
        }

    }

}
