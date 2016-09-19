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

package com.openexchange.folderstorage.virtual.sql;

import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.event.impl.EventClient;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.mail.MailFolderType;
import com.openexchange.folderstorage.virtual.osgi.Services;
import com.openexchange.java.util.Tools;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.event.EventPool;
import com.openexchange.mail.event.PooledEvent;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link Delete} - SQL for deleting a virtual folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Delete {

    /**
     * Initializes a new {@link Delete}.
     */
    private Delete() {
        super();
    }

    /**
     * (Hard-) Deletes all folder entries for specified tree.
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param session The associated session
     * @return <code>true</code> if one or more folder were deleted; otherwise <code>false</code>
     * @throws OXException If delete fails
     */
    public static boolean deleteTree(int cid, int tree, int user, Session session) throws OXException {
        DatabaseService databaseService = Services.getService(DatabaseService.class);
        // Get a connection
        Connection con = databaseService.getWritable(cid);
        boolean modified = false;
        boolean rollback = false;
        try {
            List<String> folderIds = getFoldersForTree(cid, tree, user, con);
            if (folderIds.isEmpty()) {
                return false;
            }

            con.setAutoCommit(false); // BEGIN
            rollback = true;
            for (String folderId : folderIds) {
                deleteFolder(cid, tree, user, folderId, false, true, session, con);
            }
            modified = true;
            con.commit(); // COMMIT
            rollback = false;
            return true;
        } catch (SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                DBUtils.rollback(con); // ROLLBACK
            }
            DBUtils.autocommit(con);
            if (modified) {
                databaseService.backWritable(cid, con);
            } else {
                databaseService.backWritableAfterReading(cid, con);
            }
        }
    }

    private static List<String> getFoldersForTree(int cid, int tree, int user, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT folderId FROM virtualTree WHERE cid = ? AND tree = ? AND user = ?");
            stmt.setInt(1, cid);
            stmt.setInt(2, tree);
            stmt.setInt(3, user);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyList();
            }

            List<String> folderIds = new LinkedList<String>();
            do {
                folderIds.add(rs.getString(1));
            } while (rs.next());
            return folderIds;
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Deletes specified folder.
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param folderId The folder identifier
     * @param backup <code>true</code> to backup folder data prior to deletion; otherwise <code>false</code>
     * @throws OXException If delete fails
     */
    public static void deleteFolder(final int cid, final int tree, final int user, final String folderId, final boolean backup, final Session session) throws OXException {
        DatabaseService databaseService = Services.getService(DatabaseService.class);
        // Get a connection
        Connection con = databaseService.getWritable(cid);
        boolean rollback = false;
        try {
            con.setAutoCommit(false); // BEGIN
            rollback = true;

            deleteFolder(cid, tree, user, folderId, backup, session, con);

            con.commit(); // COMMIT
            rollback = false;
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                DBUtils.rollback(con); // ROLLBACK
            }
            DBUtils.autocommit(con);
            databaseService.backWritable(cid, con);
        }
    }

    /**
     * Deletes specified folder with specified connection.
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param folderId The folder identifier
     * @param backup <code>true</code> to backup folder data prior to deletion; otherwise <code>false</code>
     * @param con The connection to use
     * @throws OXException If delete fails
     */
    public static void deleteFolder(final int cid, final int tree, final int user, final String folderId, final boolean backup, final Session session, final Connection con) throws OXException {
        deleteFolder(cid, tree, user, folderId, backup, false, session, con);
    }

    private static void deleteFolder(int cid, int tree, int user, String folderId, boolean backup, boolean force, Session session, Connection con) throws OXException {
        if (null == con) {
            deleteFolder(cid, tree, user, folderId, backup, session);
            return;
        }
        PreparedStatement stmt = null;
        if (false == force) {
            // Check for default folder first
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement("SELECT shadow FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?");
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, folderId);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    return;
                }
                final String shadow = rs.getString(1);
                if ("default".equals(shadow)) {
                    throw FolderExceptionErrorMessage.FOLDER_NOT_DELETEABLE.create(folderId, Integer.valueOf(user), Integer.valueOf(cid));
                }
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }
        }
        if (backup) {
            /*
             * Backup folder data
             */
            try {
                stmt = con.prepareStatement("INSERT INTO virtualBackupTree SELECT * FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?");
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, folderId);
                stmt.executeUpdate();
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
            /*
             * Backup permission data
             */
            try {
                stmt = con.prepareStatement("INSERT INTO virtualBackupPermission SELECT * FROM virtualPermission WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?");
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, folderId);
                stmt.executeUpdate();
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
            /*
             * Backup subscribe data
             */
            try {
                stmt = con.prepareStatement("INSERT INTO virtualBackupSubscription SELECT * FROM virtualSubscription WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?");
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, folderId);
                stmt.executeUpdate();
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
        }
        /*
         * Delete subscribe data
         */
        try {
            stmt = con.prepareStatement("DELETE FROM virtualSubscription WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, tree);
            stmt.setInt(pos++, user);
            stmt.setString(pos, folderId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        /*
         * Delete permission data
         */
        try {
            stmt = con.prepareStatement("DELETE FROM virtualPermission WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, tree);
            stmt.setInt(pos++, user);
            stmt.setString(pos, folderId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        /*
         * Delete folder data
         */
        try {
            stmt = con.prepareStatement("DELETE FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, tree);
            stmt.setInt(pos++, user);
            stmt.setString(pos, folderId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        /*
         * Post event
         */
        if (null != session) {
            final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
            final boolean postEvent = null == service ? true : service.getBoolProperty("com.openexchange.folderstorage.postEASFolderEvents", true);
            if (postEvent) {
                try {
                    if (MailFolderType.getInstance().servesFolderId(folderId)) {
                        final FullnameArgument argument = prepareMailFolderParam(folderId);
                        postEvent(argument.getAccountId(), argument.getFullname(), false, true, false, session);
                    } else {
                        final ServerSession serverSession = ServerSessionAdapter.valueOf(session);
                        new EventClient(serverSession).delete(new OXFolderAccess(con, serverSession.getContext()).getFolderObject(unsignedInt(folderId)));
                    }
                } catch (final Exception e) {
                    // Ignore
                    org.slf4j.LoggerFactory.getLogger(Delete.class).error("", e);
                }
            }
        }
    }

    private static void postEvent(final int accountId, final String fullname, final boolean contentRelated, final boolean immediateDelivery, final boolean async, final Session session) {
        if (MailAccount.DEFAULT_ID != accountId) {
            /*
             * TODO: No event for non-primary account?
             */
            return;
        }
        EventPool.getInstance().put(new PooledEvent(session.getContextId(), session.getUserId(), accountId, prepareFullname(accountId, fullname), contentRelated, immediateDelivery, true, session).setAsync(async));
    }

    private static int unsignedInt(final String sInteger) {
        return Tools.getUnsignedInteger(sInteger);
    }

}
