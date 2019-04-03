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

package com.openexchange.ratelimit.rdb.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.ratelimit.RateLimiter;

/**
 * {@link RateLimiterImpl} is a {@link RateLimiter} which uses the database to do a cluster wide rate limiting.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class RateLimiterImpl implements RateLimiter {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RateLimiterImpl.class);
    }

    private final long timeframe;
    private final long amount;
    private final int userId;
    private final Context ctx;
    private final String id;
    private final DBProvider dbProvider;

    /**
     * Initializes a new {@link RateLimiterImpl}.
     */
    public RateLimiterImpl(String id, int user, Context ctx, long amount, long timeframe, DBProvider provider) {
        this.id = id;
        this.amount = amount;
        this.timeframe = timeframe;
        this.userId = user;
        this.ctx = ctx;
        this.dbProvider = provider;
    }

    @Override
    public boolean acquire() {
       return acquire(1);
    }

    @Override
    public boolean acquire(long permits) {
        Connection writeCon = null;
        boolean readOnly = true;
        int rollback = 0;
        try {
            writeCon = dbProvider.getWriteConnection(ctx);
            Databases.startTransaction(writeCon);
            rollback = 1;
            readOnly = deleteOldPermits(writeCon) <= 0;
            final boolean result = insertPermit(writeCon, permits);
            writeCon.commit();
            rollback = 2;
            readOnly = readOnly && !result;
            return result;
        } catch (final OXException e) {
            LoggerHolder.LOG.error("Unable to aquire permits.", e);
        } catch (final SQLException e) {
            LoggerHolder.LOG.error("Unable to aquire permits.", e);
        } finally {
            if (writeCon != null) {
                if (rollback > 0) {
                    Databases.autocommit(writeCon);
                    if (rollback == 1) {
                        Databases.rollback(writeCon);
                    }
                }
                if (readOnly) {
                    dbProvider.releaseWriteConnectionAfterReading(ctx, writeCon);
                } else {
                    dbProvider.releaseWriteConnection(ctx, writeCon);
                }
            }
        }
        return false;
    }

    @Override
    public boolean exceeded() {
        // First, check if exceeded using just read connection
        // Most checks will be fine with just read access
        Connection readCon = null;
        try {
            readCon = dbProvider.getReadConnection(ctx);
            if (getCount(readCon) < amount) {
                return false;
            }
        } catch (final OXException e) {
            LoggerHolder.LOG.error("Unable to check exceeded attempts", e);
        } finally {
            if (readCon != null) {
                dbProvider.releaseReadConnection(ctx, readCon);
            }
        }

        // If exceeded, do cleanup and check again
        Connection writeCon = null;
        boolean readOnly = true;
        try {
            writeCon = dbProvider.getWriteConnection(ctx);
            readOnly = deleteOldPermits(writeCon) <= 0;
            return (getCount(writeCon) >= amount);
        } catch (final OXException e) {
            LoggerHolder.LOG.error("Unable to check exceeded attempts", e);
            return false;
        } finally {
            if (writeCon != null) {
                if (readOnly) {
                    dbProvider.releaseWriteConnectionAfterReading(ctx, writeCon);
                } else {
                    dbProvider.releaseWriteConnection(ctx, writeCon);
                }
            }
        }
    }

    @Override
    public void reset() {
        Connection readCon = null;
        // First, check if there is any work to do.  Use read databases
        try {
            readCon = dbProvider.getReadConnection(ctx);
            if (getCount(readCon) <= 0) {  // Nothing to clean
                return;
            }
        }  catch (final OXException e) {
            LoggerHolder.LOG.error("Unable to check exceeded attempts", e);
        } finally {
            if (readCon != null) {
                dbProvider.releaseReadConnection(ctx, readCon);
            }
        }
        // Need cleanup
        try {
            cleanUp();
        } catch (final OXException e) {
            LoggerHolder.LOG.error("Unable to reset rateLimiter", e);
        }
    }

    private static final String SQL_DELTE_OLD = "DELETE FROM ratelimit WHERE cid=? AND userId=? AND id=? AND timestamp < ?";
    private static final String SQL_DELETE = "DELETE FROM ratelimit WHERE cid=? AND userId=? AND id=?";
    private static final String SQL_INSERT = "INSERT INTO ratelimit SELECT ?,?,?,?,? FROM dual WHERE ? >= (SELECT COALESCE(sum(permits), 0) FROM ratelimit WHERE cid=? AND userId=? AND id=?);";
    private static final String SQL_COUNT = "SELECT COALESCE(sum(permits), 0) FROM ratelimit WHERE cid=? AND userId=? AND id=?";

    /**
     * Inserts the given amount of permits if possible
     *
     * @param writeCon The writable connection
     * @param permits The number of permits
     * @return <code>true</code> if the insert was successful, <code>false</code> otherwise
     */
    private boolean insertPermit(Connection writeCon, long permits) {
        try (PreparedStatement stmt = writeCon.prepareStatement(SQL_INSERT)) {
            return executeStmt(stmt, permits);
        } catch (final SQLException e) {
            if (Databases.isPrimaryKeyConflictInMySQL(e)) {
                // Duplicate primary key. Try again
                try (PreparedStatement stmt = writeCon.prepareStatement(SQL_INSERT)) {
                    return executeStmt(stmt, permits);
                } catch (final SQLException e2) {
                    // ignore
                }
            }
            LoggerHolder.LOG.error("Unable to insert permit.", e);
        }
        return false;
    }

    /**
     * Remove all permits for the user/id
     * 
     * @throws OXException
     */
    private void cleanUp() throws OXException {
        final Connection con = dbProvider.getWriteConnection(ctx);
        int rowsUpdated = 0;
        try (PreparedStatement stmt = con.prepareStatement(SQL_DELETE)) {
            int index = 1;
            stmt.setInt(index++, ctx.getContextId());
            stmt.setInt(index++, userId);
            stmt.setString(index++, id);
            rowsUpdated = stmt.executeUpdate();
        } catch (final SQLException e) {
            LoggerHolder.LOG.error("Error deleting rateLimit permits.", e);
        } finally {
            if(rowsUpdated > 0) {
                dbProvider.releaseWriteConnection(ctx, con);
            } else {
                dbProvider.releaseWriteConnectionAfterReading(ctx, con);
            }
        }
    }

    private boolean executeStmt(PreparedStatement stmt, long permits) throws SQLException {
        int index = 1;
        stmt.setInt(index++, ctx.getContextId());
        stmt.setInt(index++, userId);
        stmt.setString(index++, id);
        stmt.setLong(index++, System.currentTimeMillis());
        stmt.setLong(index++, permits);
        stmt.setLong(index++, amount - permits);
        stmt.setInt(index++, ctx.getContextId());
        stmt.setInt(index++, userId);
        stmt.setString(index++, id);
        return 1 == stmt.executeUpdate();
    }


    /**
     * Deletes old permits
     *
     * @param con The write connection
     * @return the number of deleted entries
     */
    private int deleteOldPermits(Connection con) {
        final long start = System.currentTimeMillis() - timeframe;
        try (PreparedStatement stmt = con.prepareStatement(SQL_DELTE_OLD)) {
            int index = 1;
            stmt.setInt(index++, ctx.getContextId());
            stmt.setInt(index++, userId);
            stmt.setString(index++, id);
            stmt.setLong(index, start);
            return stmt.executeUpdate();
        } catch (final SQLException e) {
            LoggerHolder.LOG.error("Unable to delete old permits.", e);
            return -1;
        }
    }

    /**
     * Get a count of the permits already in the database
     * @param con
     * @return Count of permits registered
     */
    private int getCount(Connection con) {
        try (PreparedStatement stmt = con.prepareStatement(SQL_COUNT)) {
            int index = 1;
            stmt.setInt(index++, ctx.getContextId());
            stmt.setInt(index++, userId);
            stmt.setString(index++, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return -1;  // Shouldn't happen
        } catch (final SQLException e) {
            LoggerHolder.LOG.error("Unable to get count of permits.", e);
            return -1;
        }
    }

}
