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

package com.openexchange.jslob.storage.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobExceptionCodes;
import com.openexchange.jslob.storage.JSlobId;
import com.openexchange.jslob.storage.JSlobStorage;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DBJSlobStorage}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DBJSlobStorage implements JSlobStorage {

    private static final String ID = "io.ox.wd.jslob.storage.db";

    private final ServiceLookup services;

    private final Lock rlock;

    private final Lock wlock;

    private final Condition wlockCondition;

    /**
     * Initializes a new {@link DBJSlobStorage}.
     */
    public DBJSlobStorage(final ServiceLookup services) {
        super();
        this.services = services;
        final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        rlock = rwLock.readLock();
        wlock = rwLock.writeLock();
        wlockCondition = wlock.newCondition();
    }

    @Override
    public String getIdentifier() {
        return ID;
    }

    private static final String SQL_DELETE_ALL_USER = "DELETE FROM jsonStorage WHERE cid = ? AND user = ?";

    /**
     * Drops all JSlob entries associated with specified user.
     * 
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If deleting all user entries fails
     */
    public void dropAllUserJSlobs(final int userId, final int contextId) throws OXException {
        wlock.lock();
        try {
            final DatabaseService databaseService = getDatabaseService();
            final Connection con = databaseService.getWritable(contextId);
            boolean committed = false;
            PreparedStatement stmt = null;
            try {
                /*
                 * Now delete
                 */
                con.setAutoCommit(false); // BEGIN
                stmt = con.prepareStatement(SQL_DELETE_ALL_USER);
                stmt.setLong(1, contextId);
                stmt.setLong(2, userId);
                stmt.executeUpdate();
                con.commit(); // COMMIT
                committed = true;
            } catch (final SQLException e) {
                throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                if (!committed) {
                    Databases.rollback(con);
                }
                Databases.closeSQLStuff(stmt);
                Databases.autocommit(con);
                databaseService.backWritable(contextId, con);
            }
        } finally {
            wlock.unlock();
        }
    }

    private static final String SQL_UPDATE_LOCK =
        "UPDATE jsonStorage SET locked = ? WHERE cid = ? AND user = ? AND serviceId = ? AND id = ? AND locked = ?";

    @Override
    public boolean lock(final JSlobId id) throws OXException {
        wlock.lock();
        try {
            /*
             * Check for existing lock
             */
            final DatabaseService databaseService = getDatabaseService();
            final int contextId = id.getContext();
            final Connection con = databaseService.getWritable(contextId);
            boolean committed = false;
            PreparedStatement stmt = null;
            final ResultSet result = null;
            try {
                con.setAutoCommit(false); // BEGIN
                final boolean locked = checkLocked(id, true, con);
                if (locked) {
                    /*
                     * Already locked
                     */
                    con.commit();
                    return false;
                }
                /*
                 * Lock
                 */
                stmt = con.prepareStatement(SQL_UPDATE_LOCK);
                stmt.setInt(1, 1);
                stmt.setLong(2, contextId);
                stmt.setLong(3, id.getUser());
                stmt.setString(4, id.getServiceId());
                stmt.setString(5, id.getId());
                stmt.setInt(6, 0);
                final int res = stmt.executeUpdate();
                con.commit(); // COMMIT
                committed = true;
                if (res == 0) {
                    /*
                     * Could not be locked
                     */
                    return false;
                }
                /*
                 * Marked as locked.
                 */
                return true;
            } catch (final SQLException e) {
                throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                if (!committed) {
                    Databases.rollback(con);
                }
                Databases.closeSQLStuff(result, stmt);
                Databases.autocommit(con);
                databaseService.backWritable(contextId, con);
            }
        } finally {
            wlock.unlock();
        }
    }

    @Override
    public void unlock(final JSlobId id) throws OXException {
        wlock.lock();
        try {
            /*
             * Check for existing lock
             */
            final DatabaseService databaseService = getDatabaseService();
            final int contextId = id.getContext();
            final Connection con = databaseService.getWritable(contextId);
            boolean committed = false;
            PreparedStatement stmt = null;
            final ResultSet result = null;
            try {
                con.setAutoCommit(false); // BEGIN
                final boolean locked = checkLocked(id, true, con);
                if (!locked) {
                    /*
                     * Already unlocked
                     */
                    con.commit();
                    return;
                }
                /*
                 * Unlock
                 */
                stmt = con.prepareStatement(SQL_UPDATE_LOCK);
                stmt.setInt(1, 0);
                stmt.setLong(2, contextId);
                stmt.setLong(3, id.getUser());
                stmt.setString(4, id.getServiceId());
                stmt.setString(5, id.getId());
                stmt.setInt(6, 1);
                final int res = stmt.executeUpdate();
                con.commit(); // COMMIT
                committed = true;
                if (res == 0) {
                    /*
                     * Could not be locked
                     */
                    throw DBJSlobStorageExceptionCode.UNLOCK_FAILED.create(id.getComponents());
                }
            } catch (final SQLException e) {
                throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }  finally {
                if (!committed) {
                    Databases.rollback(con);
                }
                Databases.closeSQLStuff(result, stmt);
                Databases.autocommit(con);
                databaseService.backWritable(contextId, con);
            }
        } finally {
            wlock.unlock();
        }
    }

    private static final String SQL_LOCKED_TRANSACTIONAL =
        "SELECT locked FROM jsonStorage WHERE cid = ? AND user = ? AND serviceId = ? AND id = ? FOR UPDATE";

    private static final String SQL_LOCKED = "SELECT locked FROM jsonStorage WHERE cid = ? AND user = ? AND serviceId = ? AND id = ?";

    private boolean checkLocked(final JSlobId id, final boolean transactional, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(transactional ? SQL_LOCKED_TRANSACTIONAL : SQL_LOCKED);
            stmt.setLong(1, id.getContext());
            stmt.setLong(2, id.getUser());
            stmt.setString(3, id.getServiceId());
            stmt.setString(4, id.getId());
            result = stmt.executeQuery();
            if (!result.next()) {
                throw DBJSlobStorageExceptionCode.NO_ENTRY.create(id.getComponents());
            }
            return (result.getInt(1) > 0);
        } catch (final SQLException e) {
            throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(result, stmt);
        }
    }

    private static final String SQL_SELECT = "SELECT data FROM jsonStorage WHERE cid = ? AND user = ? AND serviceId = ? AND id = ?";

    @Override
    public JSlob load(final JSlobId id) throws OXException {
        rlock.lock();
        try {
            final DatabaseService databaseService = getDatabaseService();
            final int contextId = id.getContext();
            final Connection con = databaseService.getReadOnly(contextId);
            try {
                final JSlob jslob = load(id, contextId, con);
                if (null == jslob) {
                    throw JSlobExceptionCodes.NOT_FOUND_EXT.create(id.getServiceId(), Integer.valueOf(id.getUser()), Integer.valueOf(contextId));
                }
                return jslob;
            } finally {
                databaseService.backReadOnly(contextId, con);
            }
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public JSlob opt(final JSlobId id) throws OXException {
        rlock.lock();
        try {
            final DatabaseService databaseService = getDatabaseService();
            final int contextId = id.getContext();
            final Connection con = databaseService.getReadOnly(contextId);
            try {
                return load(id, contextId, con);
            } finally {
                databaseService.backReadOnly(contextId, con);
            }
        } finally {
            rlock.unlock();
        }
    }

    private JSlob load(final JSlobId id, final int contextId, final Connection con) throws OXException {
        if (false && checkLocked(id, false, con)) {
            throw DBJSlobStorageExceptionCode.ALREADY_LOCKED.create(id.getComponents());
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT);
            stmt.setLong(1, contextId);
            stmt.setLong(2, id.getUser());
            stmt.setString(3, id.getServiceId());
            stmt.setString(4, id.getId());
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return new JSlob(new JSONObject(rs.getString(1)));
        } catch (final SQLException e) {
            throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final JSONException e) {
            throw JSlobExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private static final String SQL_DELETE = "DELETE FROM jsonStorage WHERE cid = ? AND user = ? AND serviceId = ? AND id = ?";

    @Override
    public JSlob remove(final JSlobId id) throws OXException {
        wlock.lock();
        try {
            final DatabaseService databaseService = getDatabaseService();
            final int contextId = id.getContext();
            final Connection con = databaseService.getWritable(contextId);
            boolean committed = false;
            if (false && checkLocked(id, false, con)) {
                throw DBJSlobStorageExceptionCode.ALREADY_LOCKED.create(id.getComponents());
            }
            PreparedStatement stmt = null;
            try {
                /*
                 * Load
                 */
                final JSlob toDelete = load(id, contextId, con);
                if (null == toDelete) {
                    throw JSlobExceptionCodes.NOT_FOUND_EXT.create(id.getServiceId(), Integer.valueOf(id.getUser()), Integer.valueOf(contextId));
                }
                /*
                 * Now delete
                 */
                con.setAutoCommit(false);
                stmt = con.prepareStatement(SQL_DELETE);
                stmt.setLong(1, contextId);
                stmt.setLong(2, id.getUser());
                stmt.setString(3, id.getServiceId());
                stmt.setString(4, id.getId());
                final int rows = stmt.executeUpdate();
                if (rows < 1) {
                    throw JSlobExceptionCodes.NOT_FOUND_EXT.create(id.getServiceId(), Integer.valueOf(id.getUser()), Integer.valueOf(contextId));
                } else if (rows > 1) {
                    throw JSlobExceptionCodes.CONFLICT.create(id.getServiceId(), Integer.valueOf(id.getUser()), Integer.valueOf(contextId));
                }
                /*
                 * Return
                 */
                con.commit();
                committed = true;
                return toDelete;
            } catch (final SQLException e) {
                throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                if (!committed) {
                    Databases.rollback(con);
                }
                Databases.closeSQLStuff(stmt);
                Databases.autocommit(con);
                databaseService.backWritable(contextId, con);
            }
        } finally {
            wlock.unlock();
        }
    }

    private static final String SQL_INSERT = "INSERT INTO jsonStorage (cid, user, serviceId, id, data) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE = "UPDATE jsonStorage SET data = ? WHERE cid = ? AND user = ? AND serviceId = ? AND id = ?";

    @Override
    public void store(final JSlobId id, final JSlob jslob) throws OXException {
        wlock.lock();
        try {
            final DatabaseService databaseService = getDatabaseService();
            final int contextId = id.getContext();
            final Connection con = databaseService.getWritable(contextId);
            boolean committed = false;
            PreparedStatement stmt = null;
            try {
                /*
                 * Load
                 */
                con.setAutoCommit(false);
                final JSlob present = load(id, contextId, con);
                if (null == present) {
                    /*
                     * Insert
                     */
                    stmt = con.prepareStatement(SQL_INSERT);
                    stmt.setLong(1, contextId);
                    stmt.setLong(2, id.getUser());
                    stmt.setString(3, id.getServiceId());
                    stmt.setString(4, id.getId());
                    stmt.setString(5, jslob.getJsonObject().toString());
                    stmt.executeUpdate();
                } else {
                    /*
                     * Update
                     */
                    if (false && checkLocked(id, false, con)) {
                        throw DBJSlobStorageExceptionCode.ALREADY_LOCKED.create(id.getComponents());
                    }
                    stmt = con.prepareStatement(SQL_UPDATE);
                    stmt.setString(1, jslob.getJsonObject().toString());
                    stmt.setLong(2, contextId);
                    stmt.setLong(3, id.getUser());
                    stmt.setString(4, id.getServiceId());
                    stmt.setString(5, id.getId());
                    stmt.executeUpdate();
                }
                con.commit();
                committed = true;
            } catch (final SQLException e) {
                throw JSlobExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                if (!committed) {
                    Databases.rollback(con);
                }
                Databases.closeSQLStuff(stmt);
                Databases.autocommit(con);
                databaseService.backWritable(contextId, con);
            }
        } finally {
            wlock.unlock();
        }
    }

    private DatabaseService getDatabaseService() throws OXException {
        return services.getService(DatabaseService.class);
    }

}
