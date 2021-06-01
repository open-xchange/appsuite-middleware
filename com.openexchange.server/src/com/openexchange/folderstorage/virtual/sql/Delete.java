/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
        int rollback = 0;
        try {
            List<String> folderIds = getFoldersForTree(cid, tree, user, con);
            if (folderIds.isEmpty()) {
                return false;
            }

            con.setAutoCommit(false); // BEGIN
            rollback = 1;
            for (String folderId : folderIds) {
                deleteFolder(cid, tree, user, folderId, false, true, session, con);
            }
            modified = true;
            con.commit(); // COMMIT
            rollback = 2;
            return true;
        } catch (SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    Databases.rollback(con); // ROLLBACK
                }
                Databases.autocommit(con);
            }
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
        } catch (SQLException e) {
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
        int rollback = 0;
        try {
            con.setAutoCommit(false); // BEGIN
            rollback = 1;

            deleteFolder(cid, tree, user, folderId, backup, session, con);

            con.commit(); // COMMIT
            rollback = 2;
        } catch (SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    Databases.rollback(con); // ROLLBACK
                }
                Databases.autocommit(con);
            }
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
            } catch (SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
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
            } catch (SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(stmt);
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
            } catch (SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(stmt);
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
            } catch (SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(stmt);
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
        } catch (SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
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
        } catch (SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
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
        } catch (SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
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
                } catch (Exception e) {
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
