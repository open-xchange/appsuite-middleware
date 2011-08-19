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
import java.util.List;
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

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(FolderJob.class));

    private final String fullName;
    
    private final String identifier;

    private volatile boolean error;

    /**
     * Initializes a new {@link FolderJob}.
     * 
     * @param accountId
     * @param userId
     * @param contextId
     */
    public FolderJob(final String fullName, final int accountId, final int userId, final int contextId) {
        super(accountId, userId, contextId);
        this.fullName = fullName;
        identifier =
            new StringBuilder(MailAccountJob.class.getSimpleName()).append('@').append(contextId).append('@').append(userId).append('@').append(
                accountId).append('@').append(fullName).toString();
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
                if (!shouldSync(fullName, now) || !wasAbleToSetSyncFlag(fullName)) {
                    return;
                }
            } catch (final OXException e) {
                LOG.error("Couldn't look-up database.", e);
            }
            /*
             * Sync mails with index...
             */
            final IndexAdapter indexAdapter = getAdapter();
            final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess =
                SMALMailAccess.getDelegateInstance(userId, contextId, accountId);
            final Session session = mailAccess.getSession();

            mailAccess.connect(true);
            try {
                final IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
                final MailMessage[] mails =
                    messageStorage.searchMessages(
                        fullName,
                        IndexRange.NULL,
                        MailSortField.RECEIVED_DATE,
                        OrderDirection.ASC,
                        null,
                        FIELDS);
                final int blockSize;
                {
                    final int configuredBlockSize = Constants.CHUNK_SIZE;
                    blockSize = configuredBlockSize > mails.length ? mails.length : configuredBlockSize;
                }
                int start = 0;
                while (start < mails.length) {
                    final int num = add2Index(mails, start, blockSize, fullName, session, messageStorage, indexAdapter);
                    start += num;
                }
            } finally {
                mailAccess.close(true);
            }
            LOG.info("Put mails from folder " + fullName + " from account " + accountId + " into index for login " + mailAccess.getMailConfig().getLogin());
        } catch (final Exception e) {
            error = true;
            cancel();
            LOG.error("Mail account job failed.", e);
        }
    }

    private int add2Index(final MailMessage[] mails, final int offset, final int len, final String fullName, final Session session, final IMailMessageStorage messageStorage, final IndexAdapter indexAdapter) throws OXException {
        final int retval; // The number of mails added to index
        final int end; // The ending sequence number (exclusive)
        {
            final int remaining = mails.length - offset;
            if (remaining >= len) {
                end = offset + len;
                retval = len;
            } else {
                end = mails.length;
                retval = remaining;
            }
        }
        final List<MailMessage> indexedMails = indexAdapter.getMessages(fullName, null, null, FIELDS, accountId, session);
        
        
        
        final MailMessage[] toAdd = new MailMessage[retval];
        for (int i = offset, j = 0; i < end; i++) {
            final MailMessage mail = messageStorage.getMessage(fullName, mails[i].getMailId(), false);
            mail.setAccountId(accountId);
            toAdd[j++] = mail;
        }
        indexAdapter.add(toAdd, session);
        return retval;
    }

    private boolean wasAbleToSetSyncFlag(final String fullName) throws OXException {
        final DatabaseService databaseService = SMALServiceLookup.getServiceStatic(DatabaseService.class);
        if (null == databaseService) {
            return false;
        }
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE mailSync SET sync = 1 WHERE cid = ? AND user = ? AND accountId = ? AND fullName = ? AND sync = 0");
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
