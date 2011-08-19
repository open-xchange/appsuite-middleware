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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
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
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.smal.SMALMailAccess;
import com.openexchange.mail.smal.SMALServiceLookup;
import com.openexchange.mail.smal.adapter.IndexAdapter;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link MailAccountJob}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountJob extends Job {

    private static final long serialVersionUID = -854493208476191708L;

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(MailAccountJob.class));

    private final int contextId;

    private final int userId;

    private final int accountId;

    private final String identifier;

    /**
     * Initializes a new {@link MailAccountJob}.
     * 
     * @param accountId
     * @param userId
     * @param contextId
     */
    public MailAccountJob(final int accountId, final int userId, final int contextId) {
        super();
        this.accountId = accountId;
        this.userId = userId;
        this.contextId = contextId;
        identifier =
            new StringBuilder(MailAccountJob.class.getSimpleName()).append('@').append(contextId).append('@').append(userId).append('@').append(
                accountId).toString();
    }

    private List<String> init() throws OXException {
        final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess =
            SMALMailAccess.getDelegateInstance(userId, contextId, accountId);
        mailAccess.connect(true);
        try {
            final List<String> fullNames = new LinkedList<String>();
            handleSubfolders(MailFolder.DEFAULT_FOLDER_ID, mailAccess.getFolderStorage(), fullNames);
            return fullNames;
        } finally {
            mailAccess.close(true);
        }
    }

    private void handleSubfolders(final String fullName, final IMailFolderStorage folderStorage, final List<String> fullNames) throws OXException {
        for (final MailFolder mailFolder : folderStorage.getSubfolders(fullName, true)) {
            final String subFullName = mailFolder.getFullname();
            fullNames.add(subFullName);
            handleSubfolders(subFullName, folderStorage, fullNames);
        }
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int getRanking() {
        return 1;
    }

    private static final MailField[] FIELDS = new MailField[] { MailField.ID };

    private static final int CHUNK = 250;

    @Override
    public void perform() {
        if (error) {
            cancel();
            return;
        }
        try {
            if (!initialized) {
                init();
            }
            /*
             * Process full names in queue
             */
            final long now = System.currentTimeMillis();
            while (!folders.isEmpty()) {
                final String fullName = folders.poll();
                if (null != fullName) {
                    try {
                        if (shouldSync(fullName, now)) {
                            return;
                        }
                    } catch (final OXException e) {
                        LOG.error("Couldn't look-up database.", e);
                    }

                    final IndexAdapter indexAdapter = getAdapter();
                    final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess =
                        SMALMailAccess.getDelegateInstance(userId, contextId, accountId);
                    final Session session = mailAccess.getSession();
                    if (indexAdapter.containsFolder(fullName, accountId, session)) {
                        continue;
                    }
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
                            final int configuredBlockSize = CHUNK;
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
                    /*
                     * Re-enqueue for next run
                     */
                    final BlockingQueue<Job> queue = getQueue();
                    if (null == queue || !queue.offer(this)) {
                        LOG.error("Re-enqueueing mail account job failed.");
                        cancel();
                    }
                    return;
                }
            }
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
        final MailMessage[] toAdd = new MailMessage[retval];
        for (int i = offset, j = 0; i < end; i++) {
            final MailMessage mail = messageStorage.getMessage(fullName, mails[i].getMailId(), false);
            mail.setAccountId(accountId);
            toAdd[j++] = mail;
        }
        indexAdapter.add(toAdd, session);
        return retval;
    }

    private boolean shouldSync(final String fullName, final long now) throws OXException {
        final DatabaseService databaseService = SMALServiceLookup.getServiceStatic(DatabaseService.class);
        if (null == databaseService) {
            return false;
        }
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt =
                con.prepareStatement("SELECT timestamp, sync FROM mailSync WHERE cid = ? AND user = ? AND accountId = ? AND fullName = ?");
            int pos = 1;
            stmt.setLong(pos++, contextId);
            stmt.setLong(pos++, userId);
            stmt.setLong(pos++, accountId);
            stmt.setString(pos, fullName);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                DBUtils.closeSQLStuff(rs, stmt);
                stmt = con.prepareStatement("INSERT INTO mailSync (cid, user, accountId, fullName, timestamp, sync) VALUES (?,?,?,?,?,?)");
                pos = 1;
                stmt.setLong(pos++, contextId);
                stmt.setLong(pos++, userId);
                stmt.setLong(pos++, accountId);
                stmt.setString(pos++, fullName);
                stmt.setLong(pos++, now);
                stmt.setInt(pos, 0);
                try {
                    stmt.executeUpdate();
                    return true;
                } catch (final Exception e) {
                    /*
                     * Another INSERTed in the meantime
                     */
                    return false;
                }
            }
            final long stamp = rs.getLong(1);
            if ((now - stamp) > Constants.HOUR_MILLIS) {
                /*
                 * Ensure sync flag is NOT set
                 */
                if (rs.getInt(2) > 0) {
                    DBUtils.closeSQLStuff(rs, stmt);
                    stmt =
                        con.prepareStatement("UPDATE mailSync SET sync = ? WHERE cid = ? AND user = ? AND accountId = ? AND fullName = ?");
                    pos = 1;
                    stmt.setInt(pos++, 0);
                    stmt.setLong(pos++, contextId);
                    stmt.setLong(pos++, userId);
                    stmt.setLong(pos++, accountId);
                    stmt.setString(pos, fullName);
                    stmt.executeUpdate();
                }
                return true;
            }
            return false;
        } catch (final SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            databaseService.backWritable(contextId, con);
        }

    }

}
