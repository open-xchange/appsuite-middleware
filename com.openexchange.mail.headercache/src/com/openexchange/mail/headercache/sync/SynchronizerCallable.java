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

package com.openexchange.mail.headercache.sync;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.headercache.HeaderCacheStaticProperties;
import com.openexchange.mail.headercache.database.DatabaseAccess;
import com.openexchange.mail.headercache.properties.HeaderCacheProperties;
import com.openexchange.mail.headercache.properties.RdbHeaderCacheProperties;
import com.openexchange.session.Session;

/**
 * {@link SynchronizerCallable} - The synchronizer callable.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SynchronizerCallable implements Callable<Object> {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(SynchronizerCallable.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static final long ZERO = 0L;

    /**
     * To create appropriate String array on toArray() invocations.
     */
    private static final String[] STR_ARR = new String[0];

    private static final MailField[] FIELDS_LOAD = {
        MailField.ID, MailField.SIZE, MailField.FLAGS, MailField.RECEIVED_DATE, MailField.HEADERS };

    /**
     * The mail access.
     */
    private final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess;

    /**
     * The folder fullname.
     */
    private final String folder;

    /**
     * The session.
     */
    private final Session session;

    /**
     * Whether to enforce synchronization or not.
     */
    private final boolean enforce;

    /**
     * Initializes a new {@link SynchronizerCallable}.
     */
    public SynchronizerCallable(final String folder, final Session session, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, final boolean enforce) {
        super();
        this.mailAccess = mailAccess;
        this.folder = folder;
        this.session = session;
        this.enforce = enforce;
    }

    public Object call() throws OXException {
        try {
            final long s = DEBUG ? System.currentTimeMillis() : ZERO;
            /*
             * Some useful variables
             */
            final int accountId = mailAccess.getAccountId();
            final int user = session.getUserId();
            final int contextId = session.getContextId();
            /*
             * Check refresh-rate if not enforced
             */
            final HeaderCacheProperties props = new RdbHeaderCacheProperties(accountId, user, contextId);
            final String propertyLastAccessed = getPropertyLastAccessed();
            if (!enforce) {
                final long refreshRate;
                {
                    final String propRefreshRate = props.getProperty(HeaderCacheProperties.PROP_REFRESH_RATE_MILLIS);
                    refreshRate =
                        null == propRefreshRate ? HeaderCacheStaticProperties.getInstance().getDefaultRefreshRate() : Long.parseLong(propRefreshRate);
                }
                if (refreshRate > 0) {
                    final String propLastAccessed = props.getProperty(propertyLastAccessed);
                    if ((null != propLastAccessed) && (System.currentTimeMillis() - Long.parseLong(propLastAccessed) < refreshRate)) {
                        /*
                         * Refresh rate not elapsed, yet.
                         */
                        if (DEBUG) {
                            LOG.debug(new StringBuilder(64).append(": Synchronizing folder \"").append(folder).append("\" of account ").append(
                                accountId).append(" aborted.").append(" Refresh-rate not elapsed, yet.").toString());
                        }
                        return null;
                    }
                }
            }
            /*
             * Create database access
             */
            final DatabaseAccess databaseAccess = DatabaseAccess.newInstance(folder, accountId, user, contextId);
            /*
             * Storage data
             */
            final Set<SyncData> storageData = databaseAccess.loadSyncData();
            /*
             * Current data
             */
            final Set<SyncData> currentData = loadCurrentData();
            /*
             * Sets for IDs
             */
            final Set<String> storageIDs = SyncData.getIDs(storageData);
            final Set<String> currentIDs = SyncData.getIDs(currentData);
            /*
             * Deleted ones
             */
            {
                final Set<String> deleted = new HashSet<String>(storageIDs);
                /*
                 * Detect deleted UIDs
                 */
                deleted.removeAll(currentIDs);
                if (!deleted.isEmpty()) {
                    /*
                     * Remove deleted ones from storage
                     */
                    databaseAccess.deleteSyncData(deleted);
                }
            }
            /*
             * New and modified
             */
            {
                /*
                 * Detect new UIDs
                 */
                final Set<String> newUIDs = new HashSet<String>(currentIDs);
                newUIDs.removeAll(storageIDs);
                if (!newUIDs.isEmpty()) {
                    databaseAccess.insertSyncData(Arrays.asList(mailAccess.getMessageStorage().getMessages(
                        folder,
                        newUIDs.toArray(STR_ARR),
                        FIELDS_LOAD)));
                }
                /*
                 * Detect IDs of changed messages
                 */
                final Set<String> existing = new HashSet<String>(storageIDs);
                existing.retainAll(currentIDs);
                if (!existing.isEmpty()) {
                    final Set<SyncData> changedData = new HashSet<SyncData>(SyncData.filterByUIDs(existing, currentData));
                    changedData.removeAll(storageData);
                    if (!changedData.isEmpty()) {
                        /*
                         * Write changes to storage
                         */
                        databaseAccess.updateSyncData(changedData);
                    }
                }
            }
            /*
             * Log
             */
            if (DEBUG) {
                final long dur = System.currentTimeMillis() - s;
                LOG.debug(new StringBuilder(64).append(": Synchronizing folder \"").append(folder).append("\" of account ").append(
                    accountId).append(" took ").append(dur).append("msec").toString());
            }
            /*
             * Update properties
             */
            props.addProperty(propertyLastAccessed, String.valueOf(System.currentTimeMillis()));
            /*
             * Return dummy null
             */
            return null;
        } catch (final OXException e) {
            throw e;
        } catch (final Exception e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    } // End of call() method

    /**
     * Fields to request ID and flags.
     */
    private static final MailField[] FIELDS_SYNC = { MailField.ID, MailField.FLAGS };

    private Set<SyncData> loadCurrentData() throws OXException {
        /*
         * Get all messages: id + flags
         */
        final MailMessage[] mails =
            mailAccess.getMessageStorage().searchMessages(folder, null, MailSortField.RECEIVED_DATE, OrderDirection.ASC, null, FIELDS_SYNC);
        final Set<SyncData> currentData = new HashSet<SyncData>(mails.length);
        for (final MailMessage mail : mails) {
            currentData.add(SyncData.newInstance(mail.getMailId(), mail.getFlags(), Arrays.asList(mail.getUserFlags())));
        }
        return currentData;
    }

    private String getPropertyLastAccessed() {
        return new StringBuilder(64).append(HeaderCacheProperties.PROP_LAST_ACCESSED).append('.').append(folder).toString();
    }

}
