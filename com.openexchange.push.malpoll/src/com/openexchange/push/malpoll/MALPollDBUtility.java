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

package com.openexchange.push.malpoll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.malpoll.services.MALPollServiceRegistry;

/**
 * {@link MALPollDBUtility} - DB utilities for MAL poll bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MALPollDBUtility {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MALPollDBUtility.class);

    private static final int CHUNK_SIZE = 100;

    /**
     * Initializes a new {@link MALPollDBUtility}.
     */
    private MALPollDBUtility() {
        super();
    }

    /**
     * Inserts the mail IDs associated with specified hash.
     *
     * @param hash The hash
     * @param mailIds The new mail IDs
     * @param cid The context ID
     * @return The mail IDs associated with specified hash
     * @throws OXException If a database resource could not be acquired
     */
    public static void insertMailIDs(final UUID hash, final Set<String> mailIds, final int cid) throws OXException {
        final DatabaseService databaseService = getDBService();
        final Connection writableConnection = getReadWriteConnection(cid, databaseService, false);
        try {
            final StringBuilder sb = new StringBuilder(CHUNK_SIZE * 16).append(SQL_INSERT_PREFIX);
            insert0(cid, hash, mailIds, CHUNK_SIZE, sb, writableConnection);
            writableConnection.commit(); // COMMIT
        } catch (Exception e) {
            rollback(writableConnection);
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            autocommit(writableConnection);
            databaseService.backWritable(cid, writableConnection);
        }
    }

    // private static final String SQL_INSERT = "INSERT INTO malPollUid (hash, uid) VALUES (?, ?)";

    private static final String SQL_INSERT_PREFIX = "INSERT INTO malPollUid (cid, hash, uid) VALUES ";

    private static final String SQL_INSERT_VALUES = "(?, UNHEX(REPLACE(?,'-','')), ?)";

    private static void insert0(final int cid, final UUID hash, final Set<String> mailIds, final int chunkSize, final StringBuilder sb, final Connection writableConnection) throws SQLException {
        if (mailIds.isEmpty()) {
            return;
        }
        final String uuidStr = hash.toString();
        final int isize = mailIds.size() + 1;
        final Iterator<String> iter = mailIds.iterator();
        for (int k = 1; k < isize;) {
            PreparedStatement stmt = null;
            try {
                int j = k;
                k += chunkSize;
                final int limit = Math.min(k, isize);
                /*
                 * Compose statement string
                 */
                {
                    sb.setLength(SQL_INSERT_PREFIX.length());
                    final String values = SQL_INSERT_VALUES; // INSERT INTO malPollUid (hash, uid) VALUES (?, ?)
                    sb.append(values);
                    final String delim = ", ";
                    for (int i = j + 1; i < limit; i++) {
                        sb.append(delim).append(values); // , VALUES (?, UNHEX(REPLACE(?,'-','')), ?)
                    }
                    stmt = writableConnection.prepareStatement(sb.toString());
                }
                /*
                 * Fill values
                 */
                int pos = 1;
                for (; j < limit; j++) {
                    stmt.setInt(pos++, cid);
                    stmt.setString(pos++, uuidStr);
                    stmt.setString(pos++, iter.next());
                }
                stmt.executeUpdate();
            } finally {
                MALPollDBUtility.closeSQLStuff(stmt);
            }
        }
    }

    private static final String SQL_DELETE = "DELETE FROM malPollUid WHERE cid = ? AND hash = UNHEX(REPLACE(?,'-','')) AND uid = ?";

    private static void deletet0(final int cid, final UUID hash, final Set<String> mailIds, final int chunkSize, final Connection writableConnection) throws SQLException {
        if (mailIds.isEmpty()) {
            return;
        }
        final String uuidStr = hash.toString();
        final int isize = mailIds.size() + 1;
        final Iterator<String> iter = mailIds.iterator();
        for (int k = 1; k < isize;) {
            final PreparedStatement stmt = writableConnection.prepareStatement(SQL_DELETE);
            try {
                int j = k;
                k += chunkSize;
                final int limit = Math.min(k, isize);
                int pos;
                for (; j < limit; j++) {
                    pos = 1;
                    stmt.setInt(pos++, cid);
                    stmt.setString(pos++, uuidStr);
                    stmt.setString(pos, iter.next());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } finally {
                MALPollDBUtility.closeSQLStuff(stmt);
            }
        }
    }

    /**
     * Replaces the mail IDs associated with specified hash.
     *
     * @param hash The hash
     * @param newIds The new mail IDs
     * @param delIds The deleted mail IDs
     * @param cid The context ID
     * @throws OXException If a database resource could not be acquired
     */
    public static void replaceMailIDs(final UUID hash, final Set<String> newIds, final Set<String> delIds, final int cid) throws OXException {
        final DatabaseService databaseService = getDBService();
        final Connection writableConnection = getReadWriteConnection(cid, databaseService, false);
        try {
            deletet0(cid, hash, delIds, CHUNK_SIZE, writableConnection);
            final StringBuilder sb = new StringBuilder(CHUNK_SIZE * 16).append(SQL_INSERT_PREFIX);
            insert0(cid, hash, newIds, CHUNK_SIZE, sb, writableConnection);

            writableConnection.commit(); // COMMIT
        } catch (Exception e) {
            rollback(writableConnection);
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            autocommit(writableConnection);
            databaseService.backWritable(cid, writableConnection);
        }
    }

    /**
     * Drops the mail IDs associated with specified hash and the hash itself, too.
     *
     * @param cid The context ID
     * @param user The user ID
     * @throws OXException If a database resource could not be acquired
     */
    public static void dropMailIDs(final int cid, final int user) throws OXException {
        final DatabaseService databaseService = getDBService();
        final Connection con = databaseService.getForUpdateTask(cid);
        try {
            con.setAutoCommit(false);
            final List<UUID> uuids = getUserUUIDs(cid, con, user);
            deleteEntries(cid, con, uuids);
            deleteUserData(cid, con, user);
            con.commit(); // COMMIT
        } catch (Exception e) {
            rollback(con);
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            autocommit(con);
            databaseService.backForUpdateTask(cid, con);
        }
    }

    private static List<UUID> getUserUUIDs(final int cid, final Connection con, final int user) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Select all UUIDs belonging to given user
            stmt = con.prepareStatement("SELECT hash FROM malPollHash WHERE cid=? AND user=?");
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            rs = stmt.executeQuery();
            final List<UUID> uuids = new ArrayList<UUID>();
            while (rs.next()) {
                uuids.add(toUUID(rs.getBytes(1)));
            }
            return uuids;
        } finally {
            MALPollDBUtility.closeSQLStuff(rs);
            MALPollDBUtility.closeSQLStuff(stmt);
        }
    }

    private static void deleteEntries(final int cid, final Connection con, final List<UUID> uuids) throws SQLException {
        if (uuids.isEmpty()) {
            return;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM malPollUid WHERE cid = ? AND hash = UNHEX(REPLACE(?,'-',''))");
            for (final UUID uuid : uuids) {
                stmt.setInt(1, cid);
                stmt.setString(2, uuid.toString());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } finally {
            MALPollDBUtility.closeSQLStuff(stmt);
        }
    }

    /**
     * Drops the user data.
     * <p>
     * Prefer {@link #dropMailIDs(int, int)} instead.
     *
     * @param cid The context ID
     * @param user The user ID
     * @throws OXException If a database resource could not be acquired
     */
    public static void deleteUserData(final int cid, final int user) throws OXException {
        final DatabaseService databaseService = getDBService();
        final Connection con = databaseService.getForUpdateTask(cid);
        boolean rollback = true;
        try {
            con.setAutoCommit(false);
            deleteUserData(cid, con, user);
            con.commit(); // COMMIT
            rollback = false;
        } catch (Exception e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            databaseService.backForUpdateTask(cid, con);
        }
    }

    private static void deleteUserData(final int cid, final Connection con, final int user) throws SQLException {
        PreparedStatement stmt = null;
        try {
            /*
             * Delete all user data
             */
            stmt = con.prepareStatement("DELETE FROM malPollHash WHERE cid = ? AND user = ?");
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            stmt.executeUpdate();
        } finally {
            MALPollDBUtility.closeSQLStuff(stmt);
        }
    }

    /**
     * Drops the context data.
     *
     * @param cid The context ID
     * @throws OXException If a database resource could not be acquired
     */
    public static void deleteContextData(final int cid) throws OXException {
        final DatabaseService databaseService = getDBService();
        final Connection con = databaseService.getForUpdateTask(cid);
        boolean rollback = true;
        try {
            con.setAutoCommit(false);
            deleteContextData(cid, con);
            con.commit(); // COMMIT
            rollback = false;
        } catch (Exception e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            databaseService.backForUpdateTask(cid, con);
        }
    }

    private static void deleteContextData(final int cid, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            /*
             * Delete all context data
             */
            stmt = con.prepareStatement("DELETE FROM malPollHash WHERE cid = ?");
            stmt.setInt(1, cid);
            stmt.executeUpdate();
        } finally {
            MALPollDBUtility.closeSQLStuff(stmt);
        }
    }

    /**
     * Gets all mail IDs associated with specified hash.
     *
     * @param hash The hash
     * @param cid The context ID
     * @return The mail IDs associated with specified hash
     * @throws OXException If a database resource could not be acquired
     */
    public static Set<String> getMailIDs(final UUID hash, final int cid) throws OXException {
        final DatabaseService databaseService = getDBService();
        final Connection readableConnection = getReadOnlyConnection(cid, databaseService);
        try {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = readableConnection.prepareStatement("SELECT uid FROM malPollUid WHERE cid = ? AND hash = UNHEX(REPLACE(?,'-',''))");
                stmt.setInt(1, cid);
                stmt.setString(2, hash.toString());
                rs = stmt.executeQuery();
                final Set<String> ids = new HashSet<String>();
                while (rs.next()) {
                    ids.add(rs.getString(1));
                }
                return ids;
            } catch (SQLException e) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                MALPollDBUtility.closeSQLStuff(rs);
                MALPollDBUtility.closeSQLStuff(stmt);
            }

        } finally {
            databaseService.backReadOnly(cid, readableConnection);
        }
    }

    /**
     * Gets the hash for specified keys.
     *
     * @param cid The context ID
     * @param user The user ID
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @return The read hash or <code>null</code> if none present
     * @throws OXException If a database resource could not be acquired
     */
    public static UUID getHash(final int cid, final int user, final int accountId, final String fullname) throws OXException {
        final DatabaseService databaseService = getDBService();
        final Connection readableConnection = getReadOnlyConnection(cid, databaseService);
        try {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt =
                    readableConnection.prepareStatement("SELECT hash FROM malPollHash WHERE cid = ? AND user = ? AND id = ? AND fullname = ?");
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos, fullname);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    return null;
                }
                return toUUID(rs.getBytes(1));
            } catch (SQLException e) {
                LOG.error("", e);
                return null;
            } finally {
                MALPollDBUtility.closeSQLStuff(rs);
                MALPollDBUtility.closeSQLStuff(stmt);
            }
        } finally {
            databaseService.backReadOnly(cid, readableConnection);
        }
    }

    /**
     * Generates and inserts a new hash for specified keys.
     *
     * @param cid The context ID
     * @param user The user ID
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @return The generated hash or <code>null</code> on failure
     * @throws OXException If a database resource could not be acquired
     */
    public static UUID insertHash(final int cid, final int user, final int accountId, final String fullname) throws OXException {
        final DatabaseService databaseService = getDBService();
        final Connection writableConnection = getReadWriteConnection(cid, databaseService);
        try {
            PreparedStatement stmt = null;
            try {
                stmt =
                    writableConnection.prepareStatement("INSERT INTO malPollHash (cid, user, id, fullname, hash) VALUES (?, ?, ?, ?, UNHEX(REPLACE(?,'-','')))");
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, fullname);
                final UUID hash = UUID.randomUUID();
                stmt.setString(pos, hash.toString());
                try {
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    if (Databases.isPrimaryKeyConflictInMySQL(e)) {
                        return null;
                    }
                    throw e;
                }
                return hash;
            } catch (SQLException e) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                MALPollDBUtility.closeSQLStuff(stmt);
            }
        } finally {
            databaseService.backWritable(cid, writableConnection);
        }
    }

    private static final int UUID_BYTE_LENGTH = 16;

    /**
     * Generates a new {@link UUID} instance from specified byte array.
     *
     * @param bytes The byte array
     * @return A new {@link UUID} instance
     * @throws IllegalArgumentException If passed byte array is <code>null</code> or its length is not 16
     */
    private static UUID toUUID(final byte[] bytes) {
        if (null == bytes) {
            throw new IllegalArgumentException("Byte array is null.");
        }
        if (bytes.length != UUID_BYTE_LENGTH) {
            throw new IllegalArgumentException("UUID must be contructed using a byte array with length 16.");
        }
        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (bytes[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (bytes[i] & 0xff);
        }
        return new UUID(msb, lsb);
    }

    /**
     * Gets the {@link DatabaseService database service} from service registry.
     *
     * @return The database service
     * @throws OXException If database service is not available
     */
    private static DatabaseService getDBService() throws OXException {
        return MALPollServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
    }

    private static Connection getReadWriteConnection(final int cid, final DatabaseService databaseService) throws OXException {
        return getReadWriteConnection(cid, databaseService, true);
    }

    private static Connection getReadWriteConnection(final int cid, final DatabaseService databaseService, final boolean autoCommit) throws OXException {
        try {
            if (autoCommit) {
                return databaseService.getWritable(cid);
            }
            final Connection writableConnection = databaseService.getWritable(cid);
            writableConnection.setAutoCommit(false); // BEGIN
            return writableConnection;
        } catch (SQLException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static Connection getReadOnlyConnection(final int cid, final DatabaseService databaseService) throws OXException {
        return databaseService.getReadOnly(cid);
    }

    /**
     * Rolls-back specified connection.
     *
     * @param con The connection to roll back.
     */
    private static void rollback(final Connection con) {
        if (null == con) {
            return;
        }
        try {
            con.rollback();
        } catch (SQLException e) {
            LOG.error("", e);
        }
    }

    /**
     * Convenience method to set the auto-commit of a connection to <code>true</code>.
     *
     * @param con The connection that should go into auto-commit mode.
     */
    private static void autocommit(final Connection con) {
        if (null == con) {
            return;
        }
        try {
            con.setAutoCommit(true);
        } catch (SQLException e) {
            LOG.error("", e);
        }
    }

    /**
     * Closes the {@link ResultSet}.
     *
     * @param result <code>null</code> or a {@link ResultSet} to close.
     */
    private static void closeSQLStuff(final ResultSet result) {
        if (result != null) {
            try {
                result.close();
            } catch (SQLException e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * Closes the {@link Statement}.
     *
     * @param stmt <code>null</code> or a {@link Statement} to close.
     */
    private static void closeSQLStuff(final Statement stmt) {
        if (null != stmt) {
            try {
                stmt.close();
            } catch (SQLException e) {
                LOG.error("", e);
            }
        }
    }

}
