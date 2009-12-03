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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.DatabaseService;
import com.openexchange.push.PushException;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.malpoll.services.MALPollServiceRegistry;
import com.openexchange.server.ServiceException;

/**
 * {@link MALPollDBUtility} - DB utilities for MAL poll bundle.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MALPollDBUtility {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MALPollDBUtility.class);

    private static final int CHUNK_SIZE = 100;

    /**
     * Initializes a new {@link MALPollDBUtility}.
     */
    private MALPollDBUtility() {
        super();
    }

    /**
     * Rolls-back specified connection.
     * 
     * @param con The connection to roll back.
     */
    public static void rollback(final Connection con) {
        if (null == con) {
            return;
        }
        try {
            con.rollback();
        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Convenience method to set the auto-commit of a connection to <code>true</code>.
     * 
     * @param con The connection that should go into auto-commit mode.
     */
    public static void autocommit(final Connection con) {
        if (null == con) {
            return;
        }
        try {
            con.setAutoCommit(true);
        } catch (final SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Closes the {@link ResultSet}.
     * 
     * @param result <code>null</code> or a {@link ResultSet} to close.
     */
    public static void closeSQLStuff(final ResultSet result) {
        if (result != null) {
            try {
                result.close();
            } catch (final SQLException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Closes the {@link Statement}.
     * 
     * @param stmt <code>null</code> or a {@link Statement} to close.
     */
    public static void closeSQLStuff(final Statement stmt) {
        if (null != stmt) {
            try {
                stmt.close();
            } catch (final SQLException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Inserts the mail IDs associated with specified hash.
     * 
     * @param hash The hash
     * @param mailIds The new mail IDs
     * @param cid The context ID
     * @return The mail IDs associated with specified hash
     * @throws PushException If a database resource could not be acquired
     */
    public static void insertMailIDs(final String hash, final Set<String> mailIds, final int cid) throws PushException {
        final DatabaseService databaseService;
        try {
            databaseService = MALPollServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
        } catch (final ServiceException e) {
            throw new PushException(e);
        }
        final Connection writableConnection;
        try {
            writableConnection = databaseService.getWritable(cid);
            writableConnection.setAutoCommit(false); // BEGIN
        } catch (final DBPoolingException e) {
            throw new PushException(e);
        } catch (final SQLException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        try {
            insert0(hash, mailIds, CHUNK_SIZE, writableConnection);

            writableConnection.commit(); // COMMIT
        } catch (final SQLException e) {
            rollback(writableConnection);
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            rollback(writableConnection);
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            autocommit(writableConnection);
            databaseService.backWritable(cid, writableConnection);
        }
    }

    private static final String SQL_INSERT = "INSERT INTO malPollUid (hash, uid) VALUES (?, ?)";

    private static void insert0(final String hash, final Set<String> mailIds, final int chunkSize, final Connection writableConnection) throws SQLException {
        if (mailIds.isEmpty()) {
            return;
        }
        final int isize = mailIds.size() + 1;
        final Iterator<String> iter = mailIds.iterator();
        for (int k = 1; k < isize;) {
            final PreparedStatement stmt = writableConnection.prepareStatement(SQL_INSERT);
            try {
                int j = k;
                k += chunkSize;
                final int limit = Math.min(k, isize);
                for (; j < limit; j++) {
                    stmt.setString(1, hash);
                    stmt.setString(2, iter.next());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } finally {
                MALPollDBUtility.closeSQLStuff(stmt);
            }
        }
    }

    private static final String SQL_DELETE = "DELETE FROM malPollUid WHERE hash = ? AND uid = ?";

    private static void deletet0(final String hash, final Set<String> mailIds, final int chunkSize, final Connection writableConnection) throws SQLException {
        if (mailIds.isEmpty()) {
            return;
        }
        final int isize = mailIds.size() + 1;
        final Iterator<String> iter = mailIds.iterator();
        for (int k = 1; k < isize;) {
            final PreparedStatement stmt = writableConnection.prepareStatement(SQL_DELETE);
            try {
                int j = k;
                k += chunkSize;
                final int limit = Math.min(k, isize);
                for (; j < limit; j++) {
                    stmt.setString(1, hash);
                    stmt.setString(2, iter.next());
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
     * @throws PushException If a database resource could not be acquired
     */
    public static void replaceMailIDs(final String hash, final Set<String> newIds, final Set<String> delIds, final int cid) throws PushException {
        final DatabaseService databaseService;
        try {
            databaseService = MALPollServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
        } catch (final ServiceException e) {
            throw new PushException(e);
        }
        final Connection writableConnection;
        try {
            writableConnection = databaseService.getWritable(cid);
            writableConnection.setAutoCommit(false); // BEGIN
        } catch (final DBPoolingException e) {
            throw new PushException(e);
        } catch (final SQLException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        try {
            deletet0(hash, delIds, CHUNK_SIZE, writableConnection);

            insert0(hash, newIds, CHUNK_SIZE, writableConnection);

            writableConnection.commit(); // COMMIT
        } catch (final SQLException e) {
            rollback(writableConnection);
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
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
     * @param accountId The account ID
     * @param fullname The folder fullname
     * @throws PushException If a database resource could not be acquired
     */
    public static void dropMailIDs(final int cid, final int user, final int accountId, final String fullname) throws PushException {
        /*
         * Get hash
         */
        final String hash = getHash(cid, user, accountId, fullname);
        if (null == hash) {
            /*
             * No hash available
             */
            return;
        }
        /*
         * Drop everything related to hash
         */
        final DatabaseService databaseService;
        try {
            databaseService = MALPollServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
        } catch (final ServiceException e) {
            throw new PushException(e);
        }
        final Connection writableConnection;
        try {
            writableConnection = databaseService.getForUpdateTask(cid);
            writableConnection.setAutoCommit(false); // BEGIN
        } catch (final DBPoolingException e) {
            throw new PushException(e);
        } catch (final SQLException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        try {
            PreparedStatement stmt = null;
            try {
                stmt = writableConnection.prepareStatement("DELETE FROM malPollUid WHERE hash = ?");
                stmt.setString(1, hash);
                stmt.executeUpdate();
                MALPollDBUtility.closeSQLStuff(stmt);

                stmt = writableConnection.prepareStatement("DELETE FROM malPollHash WHERE hash = ?");
                stmt.setString(1, hash);
                stmt.executeUpdate();
                writableConnection.commit(); // COMMIT
            } catch (final SQLException e) {
                rollback(writableConnection);
                throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (final Exception e) {
                rollback(writableConnection);
                throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                MALPollDBUtility.closeSQLStuff(stmt);
            }
        } finally {
            autocommit(writableConnection);
            databaseService.backWritable(cid, writableConnection);
        }
    }

    /**
     * Gets all mail IDs associated with specified hash.
     * 
     * @param hash The hash
     * @param cid The context ID
     * @return The mail IDs associated with specified hash
     * @throws PushException If a database resource could not be acquired
     */
    public static Set<String> getMailIDs(final String hash, final int cid) throws PushException {
        final DatabaseService databaseService;
        try {
            databaseService = MALPollServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
        } catch (final ServiceException e) {
            throw new PushException(e);
        }
        final Connection readableConnection;
        try {
            readableConnection = databaseService.getReadOnly(cid);
        } catch (final DBPoolingException e) {
            throw new PushException(e);
        }
        try {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = readableConnection.prepareStatement("SELECT uid FROM malPollUid WHERE hash = ?");
                stmt.setString(1, hash);
                rs = stmt.executeQuery();
                final Set<String> ids = new HashSet<String>();
                while (rs.next()) {
                    ids.add(rs.getString(1));
                }
                return ids;
            } catch (final SQLException e) {
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
     * @throws PushException If a database resource could not be acquired
     */
    public static String getHash(final int cid, final int user, final int accountId, final String fullname) throws PushException {
        final DatabaseService databaseService;
        try {
            databaseService = MALPollServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
        } catch (final ServiceException e) {
            throw new PushException(e);
        }
        final Connection readableConnection;
        try {
            readableConnection = databaseService.getReadOnly(cid);
        } catch (final DBPoolingException e) {
            throw new PushException(e);
        }
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
                stmt.setString(pos++, fullname);
                rs = stmt.executeQuery();
                return rs.next() ? rs.getString(1) : null;
            } catch (final SQLException e) {
                LOG.error(e.getMessage(), e);
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
     * @throws PushException If a database resource could not be acquired
     */
    public static String insertHash(final int cid, final int user, final int accountId, final String fullname) throws PushException {
        final DatabaseService databaseService;
        try {
            databaseService = MALPollServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
        } catch (final ServiceException e) {
            throw new PushException(e);
        }
        final Connection writableConnection;
        try {
            writableConnection = databaseService.getWritable(cid);
        } catch (final DBPoolingException e) {
            throw new PushException(e);
        }
        try {
            PreparedStatement stmt = null;
            try {
                stmt =
                    writableConnection.prepareStatement("INSERT INTO malPollHash (cid, user, id, fullname, hash) VALUES (?, ?, ?, ?, ?)");
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, fullname);
                final String hash = randomUUID();
                stmt.setString(pos++, hash);
                stmt.executeUpdate();
                return hash;
            } catch (final SQLException e) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                MALPollDBUtility.closeSQLStuff(stmt);
            }
        } finally {
            databaseService.backWritable(cid, writableConnection);
        }
    }

    /**
     * Generates a UUID using {@link UUID#randomUUID()} and removes all dashes; e.g.:<br>
     * <i>a5aa65cb-6c7e-4089-9ce2-b107d21b9d15</i> would be <i>a5aa65cb6c7e40899ce2b107d21b9d15</i>
     * 
     * @return A UUID string
     */
    private static String randomUUID() {
        final StringBuilder s = new StringBuilder(36).append(UUID.randomUUID());
        s.deleteCharAt(23);
        s.deleteCharAt(18);
        s.deleteCharAt(13);
        s.deleteCharAt(8);
        return s.toString();
    }

}
