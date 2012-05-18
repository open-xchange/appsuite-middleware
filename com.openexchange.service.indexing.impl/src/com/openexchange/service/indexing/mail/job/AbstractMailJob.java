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

package com.openexchange.service.indexing.mail.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexDocument.Type;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.smal.SmalAccessService;
import com.openexchange.service.indexing.StandardIndexingJob;
import com.openexchange.service.indexing.impl.Services;
import com.openexchange.service.indexing.mail.Constants;
import com.openexchange.service.indexing.mail.MailJobInfo;
import com.openexchange.service.indexing.mail.StorageAccess;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link AbstractMailJob} - The abstract mail job.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractMailJob extends StandardIndexingJob {

    private static final long serialVersionUID = 8617301658519763880L;

    /**
     * The insert type.
     */
    public static enum InsertType {
        /**
         * Mail headers.
         */
        ENVELOPE,
        /**
         * Header & body of the mail
         */
        BODY,
        /**
         * Whole mail including its attachments (default)
         */
        ATTACHMENTS;
    }

    /**
     * The mail index document type.
     */
    protected static final Type MAIL = IndexDocument.Type.MAIL;

    /**
     * The job's information.
     */
    protected final MailJobInfo info;

    /**
     * Convenience member for <code>info.contextId</code>
     */
    protected final int contextId;

    /**
     * Convenience member for <code>info.userId</code>
     */
    protected final int userId;

    /**
     * Convenience member for <code>info.accountId</code>
     */
    protected final int accountId;

    /**
     * The storage access.
     */
    protected final StorageAccess storageAccess;

    /**
     * Initializes a new {@link AbstractMailJob}.
     */
    protected AbstractMailJob(final MailJobInfo info) {
        super();
        this.info = info;
        accountId = info.accountId;
        userId = info.userId;
        contextId = info.contextId;        
        storageAccess = new StorageAccess(info);
    }

    @Override
    public Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, MailService.class, IndexFacadeService.class, SmalAccessService.class };
    }

    @Override
    public final void performJob() throws OXException, InterruptedException {        
        try {
            performMailJob();
        } finally {
            storageAccess.close();
        }
    }

    /**
     * Performs this job's task.
     * 
     * @throws OXException If performing job fails for any reason
     * @throws InterruptedException If job has been interrupted
     */
    protected abstract void performMailJob() throws OXException, InterruptedException;

    /**
     * Converts <code>MailMessage</code> collection to an <code>IndexDocument</code> list.
     * 
     * @param mails The <code>MailMessage</code> collection
     * @return The <code>IndexDocument</code> list
     */
    protected static List<IndexDocument<MailMessage>> toDocuments(final Collection<MailMessage> mails) {
        if (null == mails) {
            return Collections.emptyList();
        }
        final List<IndexDocument<MailMessage>> documents = new ArrayList<IndexDocument<MailMessage>>(mails.size());
        for (final MailMessage mail : mails) {
            documents.add(new StandardIndexDocument<MailMessage>(mail, MAIL));
        }
        return documents;
    }

    /**
     * Checks if a sync should be performed for specified full name with default span of 1 hour.
     * 
     * @param fullName The full name
     * @param now The current time milliseconds
     * @return <code>true</code> if a sync should be performed for passed full name; otherwise <code>false</code>
     * @throws OXException If an error occurs
     */
    protected boolean shouldSync(final String fullName, final long now) throws OXException {
        return shouldSync(fullName, now, Constants.HOUR_MILLIS);
    }

    /**
     * Checks if a sync should be performed because given span is exceeded for specified full name.
     * 
     * @param fullName The full name
     * @param now The current time milliseconds
     * @param span The max. allowed span; if exceeded the folder is considered to be synchronized
     * @return <code>true</code> if a sync should be performed for passed full name; otherwise <code>false</code>
     * @throws OXException If an error occurs
     */
    protected boolean shouldSync(final String fullName, final long now, final long span) throws OXException {
        final DatabaseService databaseService = Services.getService(DatabaseService.class);
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
            if ((now - stamp) > span) {
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

    /**
     * Updates the time stamp and unsets the sync flag.
     * 
     * @param fullName The folder full name
     * @param stamp The time stamp
     * @return <code>true</code> if operation was successful; otherwise <code>false</code>
     * @throws OXException If an error occurs
     */
    protected boolean setTimestampAndUnsetSyncFlag(final String fullName, final long stamp) throws OXException {
        final DatabaseService databaseService = Services.getService(DatabaseService.class);
        if (null == databaseService) {
            return false;
        }
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            stmt =
                con.prepareStatement("UPDATE mailSync SET sync = 0, timestamp = ? WHERE cid = ? AND user = ? AND accountId = ? AND fullName = ? AND sync = 1");
            int pos = 1;
            stmt.setLong(pos++, stamp);
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

    /**
     * Updates the time stamp.
     * 
     * @param fullName The folder full name
     * @param stamp The time stamp
     * @return <code>true</code> if operation was successful; otherwise <code>false</code>
     * @throws OXException If an error occurs
     */
    protected boolean setTimestamp(final String fullName, final long stamp) throws OXException {
        final DatabaseService databaseService = Services.getService(DatabaseService.class);
        if (null == databaseService) {
            return false;
        }
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            stmt =
                con.prepareStatement("UPDATE mailSync SET timestamp = ? WHERE cid = ? AND user = ? AND accountId = ? AND fullName = ? AND sync = 1");
            int pos = 1;
            stmt.setLong(pos++, stamp);
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

    /**
     * Unsets the sync flag.
     * 
     * @param fullName The folder full name
     * @return <code>true</code> if operation was successful; otherwise <code>false</code>
     * @throws OXException If an error occurs
     */
    protected boolean unsetSyncFlag(final String fullName) throws OXException {
        final DatabaseService databaseService = Services.getService(DatabaseService.class);
        if (null == databaseService) {
            return false;
        }
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        try {
            stmt =
                con.prepareStatement("UPDATE mailSync SET sync = 0 WHERE cid = ? AND user = ? AND accountId = ? AND fullName = ? AND sync = 1");
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

    /**
     * Drops the entry associated with specified full name.
     * 
     * @param fullName The full name
     * @throws OXException If removal fails
     */
    protected void dropFolderEntry(final String fullName) throws OXException {
        final DatabaseService databaseService = Services.getService(DatabaseService.class);
        if (null == databaseService) {
            return;
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
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            databaseService.backWritable(contextId, con);
        }
    }

    /**
     * Checks if this call succeeds in setting the sync flag.
     * 
     * @param fullName The folder full name
     * @param now
     * @return <code>true</code> if operation was successful; otherwise <code>false</code>
     * @throws OXException If an error occurs
     */
    protected boolean wasAbleToSetSyncFlag(final String fullName, final long now) throws OXException {
        final DatabaseService databaseService = Services.getService(DatabaseService.class);
        if (null == databaseService) {
            return false;
        }
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT sync FROM mailSync WHERE cid = ? AND user = ? AND accountId = ? AND fullName = ?");
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
                stmt.setInt(pos, 1);
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
            /*
             * Try to set sync flag
             */
            DBUtils.closeSQLStuff(rs, stmt);
            stmt =
                con.prepareStatement("UPDATE mailSync SET sync = 1 WHERE cid = ? AND user = ? AND accountId = ? AND fullName = ? AND sync = 0");
            pos = 1;
            stmt.setLong(pos++, contextId);
            stmt.setLong(pos++, userId);
            stmt.setLong(pos++, accountId);
            stmt.setString(pos, fullName);
            return stmt.executeUpdate() > 0;
        } catch (final SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            databaseService.backWritable(contextId, con);
        }
    }

}
