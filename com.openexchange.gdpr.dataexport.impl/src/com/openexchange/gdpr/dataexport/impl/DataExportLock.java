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

package com.openexchange.gdpr.dataexport.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.impl.osgi.Services;
import com.openexchange.java.Strings;

/**
 * {@link DataExportLock} - The helper class to acquire/release data export lock that is acquired by both - clean-up task and job processor.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class DataExportLock {

    private static final DataExportLock INSTANCE = new DataExportLock();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static DataExportLock getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link DataExportLock}.
     */
    private DataExportLock() {
        super();
    }

    private static final int LOCK_ID = 1496146671;

    private static final String TOKEN_WRITE_LOCK = "LOCKED";

    /**
     * Tries to acquires the clean-up lock.
     *
     * @param writeLock Whether write lock or read lock should be acquired
     * @return <code>true</code> if lock could be successfully acquired; otherwise <code>false</code>
     * @throws OXException If lock acquisition fails
     */
    public boolean acquireCleanUpTaskLock(boolean writeLock) throws OXException {
        return acquireCleanUpTaskLock(writeLock, Services.requireService(DatabaseService.class));
    }

    /**
     * Tries to acquires the clean-up lock.
     *
     * @param writeLock Whether write lock or read lock should be acquired
     * @param databaseService The database service to use
     * @return <code>true</code> if lock could be successfully acquired; otherwise <code>false</code>
     * @throws OXException If lock acquisition fails
     */
    public boolean acquireCleanUpTaskLock(boolean writeLock, DatabaseService databaseService) throws OXException {
        boolean modified = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection writeCon = databaseService.getWritable();
        try {
            stmt = writeCon.prepareStatement("SELECT text FROM reason_text WHERE id=?");
            stmt.setInt(1, LOCK_ID);
            rs = stmt.executeQuery();
            String previousToken = rs.next() ? rs.getString(1) : null;
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            String currentToken;
            if (previousToken == null) {
                // No lock entry existing. Try to INSERT.
                currentToken = writeLock ? TOKEN_WRITE_LOCK : "1";
                stmt = writeCon.prepareStatement("INSERT INTO reason_text (id, text) VALUES (?, ?)");
                stmt.setInt(1, LOCK_ID);
                stmt.setString(2, currentToken);
                try {
                    modified = stmt.executeUpdate() > 0;
                } catch (SQLException e) {
                    if (Databases.isPrimaryKeyConflictInMySQL(e)) {
                        Databases.closeSQLStuff(stmt);
                        stmt = null;
                        databaseService.backWritableAfterReading(writeCon);
                        writeCon = null;
                        return acquireCleanUpTaskLock(writeLock, databaseService);
                    }
                    throw e;
                }
                // Lock could be acquired
                return true;
            }

            // Lock entry does exist. Check for exclusive write lock.
            if (writeLock || TOKEN_WRITE_LOCK.equals(previousToken)) {
                // Either write lock shall be acquired or write lock currently held by another process
                return false;
            }

            // Try to atomically increment read lock counter
            int previousCount = Strings.parseUnsignedInt(previousToken);
            if (previousCount < 0) {
                throw new IllegalStateException("Illegal clean-up lock token: " + previousToken);
            }

            currentToken = Integer.toString(previousCount + 1);
            stmt = writeCon.prepareStatement("UPDATE reason_text SET text=? WHERE id=? AND text=?");
            stmt.setString(1, currentToken);
            stmt.setInt(2, LOCK_ID);
            stmt.setString(3, previousToken);
            modified = stmt.executeUpdate() > 0;
            if (modified == false) {
                // Lock could NOT be acquired
                Databases.closeSQLStuff(stmt);
                stmt = null;
                databaseService.backWritableAfterReading(writeCon);
                writeCon = null;
                return acquireCleanUpTaskLock(writeLock, databaseService);
            }
            // Lock could be acquired
            return true;
        } catch (SQLException e) {
            throw DataExportExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (writeCon != null) {
                if (modified) {
                    databaseService.backWritable(writeCon);
                } else {
                    databaseService.backWritableAfterReading(writeCon);
                }
            }
        }
    }

    /**
     * Tries to releases the clean-up lock.
     *
     * @param writeLock Whether write lock or read lock should be released
     * @return <code>true</code> if lock could be successfully released; otherwise <code>false</code>
     * @throws OXException If releasing lock fails
     */
    public boolean releaseCleanUpTaskLock(boolean writeLock) throws OXException {
        return releaseCleanUpTaskLock(writeLock, Services.requireService(DatabaseService.class));
    }

    /**
     * Tries to releases the clean-up lock.
     *
     * @param writeLock Whether write lock or read lock should be released
     * @param databaseService The database service to use
     * @return <code>true</code> if lock could be successfully released; otherwise <code>false</code>
     * @throws OXException If releasing lock fails
     */
    public boolean releaseCleanUpTaskLock(boolean writeLock, DatabaseService databaseService) throws OXException {
        boolean modified = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection writeCon = databaseService.getWritable();
        try {
            if (writeLock) {
                // Write lock should be released
                stmt = writeCon.prepareStatement("DELETE FROM reason_text WHERE id=? AND text=?");
                stmt.setInt(1, LOCK_ID);
                stmt.setString(2, TOKEN_WRITE_LOCK);
                modified = stmt.executeUpdate() > 0;
                return modified;
            }

            // Read lock should be released
            stmt = writeCon.prepareStatement("SELECT text FROM reason_text WHERE id=?");
            stmt.setInt(1, LOCK_ID);
            rs = stmt.executeQuery();
            String previousToken = rs.next() ? rs.getString(1) : null;
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            if (previousToken == null) {
                // Nothing to release
                return false;
            }

            // Try to atomically decrement read lock counter
            int previousCount = Strings.parseUnsignedInt(previousToken);
            if (previousCount < 0) {
                throw new IllegalStateException("Illegal clean-up lock token: " + previousToken);
            }

            if (previousCount == 1) {
                stmt = writeCon.prepareStatement("DELETE FROM reason_text WHERE id=? AND text=?");
                stmt.setInt(1, LOCK_ID);
                stmt.setString(2, previousToken);
            } else {
                stmt = writeCon.prepareStatement("UPDATE reason_text SET text=? WHERE id=? AND text=?");
                stmt.setString(1, Integer.toString(previousCount - 1));
                stmt.setInt(2, LOCK_ID);
                stmt.setString(3, previousToken);
            }
            modified = stmt.executeUpdate() > 0;
            if (modified == false) {
                // Lock could NOT be released
                Databases.closeSQLStuff(stmt);
                stmt = null;
                databaseService.backWritableAfterReading(writeCon);
                writeCon = null;
                return releaseCleanUpTaskLock(writeLock, databaseService);
            }
            return true;
        } catch (SQLException e) {
            throw DataExportExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (writeCon != null) {
                if (modified) {
                    databaseService.backWritable(writeCon);
                } else {
                    databaseService.backWritableAfterReading(writeCon);
                }
            }
        }
    }

}
