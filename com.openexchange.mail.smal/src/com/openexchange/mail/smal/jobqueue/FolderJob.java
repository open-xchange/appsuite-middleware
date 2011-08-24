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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.jobqueue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.smal.SMALMailAccess;
import com.openexchange.mail.smal.SMALServiceLookup;
import com.openexchange.mail.smal.adapter.IndexAdapter;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link FolderJob}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderJob extends AbstractMailSyncJob {

    private static final long serialVersionUID = -7195124742370755327L;

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(FolderJob.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private final String fullName;

    private final String identifier;

    private final boolean checkShouldSync;

    private volatile boolean error;

    private volatile long span;

    /**
     * Initializes a new {@link FolderJob}.
     *
     * @param fullName The folder full name
     * @param accountId The account ID
     * @param userId The user ID
     * @param contextId The context ID
     */
    public FolderJob(final String fullName, final int accountId, final int userId, final int contextId) {
        this(fullName, accountId, userId, contextId, true);
    }

    /**
     * Initializes a new {@link FolderJob}.
     *
     * @param fullName The folder full name
     * @param accountId The account ID
     * @param userId The user ID
     * @param contextId The context ID
     * @param checkShouldSync <code>true</code> to check if a sync for denoted folder should be performed; otherwise <code>false</code>
     */
    public FolderJob(final String fullName, final int accountId, final int userId, final int contextId, final boolean checkShouldSync) {
        super(accountId, userId, contextId);
        this.checkShouldSync = checkShouldSync;
        this.fullName = fullName;
        identifier =
            new StringBuilder(FolderJob.class.getSimpleName()).append('@').append(contextId).append('@').append(userId).append('@').append(
                accountId).append('@').append(fullName).toString();
        span = Constants.DEFAULT_MILLIS;
    }

    /**
     * Sets the span
     *
     * @param span The span to set
     * @return This folder job with specified span applied
     */
    public FolderJob setSpan(final long span) {
        this.span = span;
        return this;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int getRanking() {
        return 0;
    }

    private static final MailField[] FIELDS = new MailField[] { MailField.ID, MailField.FLAGS };

    @Override
    public void perform() {
        if (error) {
            cancel();
            return;
        }
        try {
            final long now = System.currentTimeMillis();
            try {
                if ((checkShouldSync && !shouldSync(fullName, now, span)) || !wasAbleToSetSyncFlag(fullName)) {
                    return;
                }
            } catch (final OXException e) {
                LOG.error("Couldn't look-up database.", e);
            }
            /*
             * Sync mails with index...
             */
            final long st = DEBUG ? System.currentTimeMillis() : 0L;
            boolean unset = true;
            try {
                final IndexAdapter indexAdapter = getAdapter();
                final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess =
                    SMALMailAccess.getUnwrappedInstance(userId, contextId, accountId);
                final Session session = mailAccess.getSession();
                /*
                 * Get the mails from mail storage
                 */
                final MailMessage[] mails;
                mailAccess.connect(true);
                try {
                    /*
                     * At first check existence of denoted folder
                     */
                    if (!mailAccess.getFolderStorage().exists(fullName)) {
                        /*
                         * Drop entry from database and return
                         */
                        deleteDBEntry();
                        unset = false;
                        return;
                    }
                    /*
                     * Fetch mails
                     */
                    mails =
                        mailAccess.getMessageStorage().searchMessages(
                            fullName,
                            IndexRange.NULL,
                            MailSortField.RECEIVED_DATE,
                            OrderDirection.ASC,
                            null,
                            FIELDS);
                } finally {
                    mailAccess.close(true);
                }
                final Map<String, MailMessage> storagedMap;
                if (0 == mails.length) {
                    storagedMap = Collections.emptyMap();
                } else {
                    storagedMap = new HashMap<String, MailMessage>(mails.length);
                    for (final MailMessage mailMessage : mails) {
                        storagedMap.put(mailMessage.getMailId(), mailMessage);
                    }
                }
                /*
                 * Get the mails from index
                 */
                final List<MailMessage> indexedMails = indexAdapter.getMessages(null, fullName, null, null, FIELDS, accountId, session);
                final Map<String, MailMessage> indexedMap;
                if (indexedMails.isEmpty()) {
                    indexedMap = Collections.emptyMap();
                } else {
                    indexedMap = new HashMap<String, MailMessage>(indexedMails.size());
                    for (final MailMessage mailMessage : indexedMails) {
                        indexedMap.put(mailMessage.getMailId(), mailMessage);
                    }
                }
                /*
                 * New ones
                 */
                Set<String> newIds = new HashSet<String>(storagedMap.keySet());
                newIds.removeAll(indexedMap.keySet());
                /*
                 * Removed ones
                 */
                Set<String> deletedIds = new HashSet<String>(indexedMap.keySet());
                deletedIds.removeAll(storagedMap.keySet());
                /*
                 * Changed ones
                 */
                Set<String> changedIds = new HashSet<String>(indexedMap.keySet());
                List<MailMessage> changedMails = new ArrayList<MailMessage>(changedIds.size());
                changedIds.removeAll(deletedIds);
                for (final Iterator<String> iterator = changedIds.iterator(); iterator.hasNext();) {
                    final String mailId = iterator.next();
                    final MailMessage storageMail = storagedMap.get(mailId);
                    if (storageMail.getFlags() == indexedMap.get(mailId).getFlags()) {
                        iterator.remove();
                    } else {
                        storageMail.setAccountId(accountId);
                        storageMail.setFolder(fullName);
                        storageMail.setMailId(mailId);
                        changedMails.add(storageMail);
                    }
                }
                changedIds = null;
                /*
                 * Delete
                 */
                indexAdapter.deleteMessages(deletedIds, fullName, accountId, session);
                deletedIds = null;
                /*
                 * Change flags
                 */
                indexAdapter.change(changedMails, session);
                changedMails = null;
                /*
                 * Add
                 */
                final int blockSize;
                final int size = newIds.size();
                {
                    final int configuredBlockSize = Constants.CHUNK_SIZE;
                    blockSize = configuredBlockSize > size ? size : configuredBlockSize;
                }
                final String[] ids = newIds.toArray(new String[newIds.size()]);
                newIds = null;
                int start = 0;
                while (start < size) {
                    final int num = add2Index(ids, start, blockSize, fullName, indexAdapter);
                    start += num;
                    if (DEBUG) {
                        final long dur = System.currentTimeMillis() - st;
                        LOG.debug("Folder job \"" + identifier + "\" inserted " + start + " of " + size + " messages in " + dur + "msec.");
                    }
                }
                setTimestampAndUnsetSyncFlag(fullName, System.currentTimeMillis());
                unset = false;
            } finally {
                if (unset) {
                    // Unset sync flag
                    unsetSyncFlag(fullName);
                }
                if (DEBUG) {
                    final long dur = System.currentTimeMillis() - st;
                    LOG.debug("Folder job \"" + identifier + "\" took " + dur + "msec.");
                }
            }
        } catch (final Exception e) {
            error = true;
            cancel();
            LOG.error("Folder job \"" + identifier + "\" failed.", e);
        }
    }

    private int add2Index(final String[] ids, final int offset, final int len, final String fullName, final IndexAdapter indexAdapter) throws OXException {
        final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess =
            SMALMailAccess.getUnwrappedInstance(userId, contextId, accountId);
        final Session session = mailAccess.getSession();
        mailAccess.connect(true);
        try {
            final int retval; // The number of mails added to index
            final int end; // The end position (exclusive)
            {
                final int remaining = ids.length - offset;
                if (remaining >= len) {
                    end = offset + len;
                    retval = len;
                } else {
                    end = ids.length;
                    retval = remaining;
                }
            }
            final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            final List<MailMessage> toAdd = new ArrayList<MailMessage>(retval);
            for (int i = offset; i < end; i++) {
                final MailMessage mail = messageStorage.getMessage(fullName, ids[i], false);
                mail.setAccountId(accountId);
                toAdd.add(mail);
            }
            indexAdapter.add(toAdd, session);
            return retval;
        } finally {
            mailAccess.close(true);
        }
    }

    private boolean deleteDBEntry() throws OXException {
        final DatabaseService databaseService = SMALServiceLookup.getServiceStatic(DatabaseService.class);
        if (null == databaseService) {
            return false;
        }
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM mailSync WHERE cid = ? AND user = ? AND accountId = ? AND fullName = ?");
            int pos = 1;
            stmt.setLong(pos++, contextId);
            stmt.setLong(pos++, userId);
            stmt.setLong(pos++, accountId);
            stmt.setString(pos, fullName);
            return stmt.executeUpdate() > 0;
        } catch (final SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            databaseService.backWritable(contextId, con);
        }
    }

}
