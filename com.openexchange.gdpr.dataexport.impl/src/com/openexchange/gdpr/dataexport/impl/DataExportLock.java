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

package com.openexchange.gdpr.dataexport.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.impl.osgi.Services;
import com.openexchange.java.Strings;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link DataExportLock} - The helper class to acquire/release data export lock that is acquired by both - clean-up task and job processor.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class DataExportLock {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DataExportLock.class);

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

    /** The special identifier for clean-up lock in <code>"reason_text"</code> table */
    private static final int LOCK_ID = 1496146671;

    /** The lock identifier for write lock */
    private static final String LOCK_ID_WRITE = "LOCKED";

    /** The delimiter character separating a token's lock identifier and time stamp; e.g. <code>"LOCKED-1616084059000"</code> */
    private static final char DELIMITER = '-';

    /** The refresh interval: one minute */
    private static final long REFRESH_MINUTES = 1;

    /** The idle duration */
    private static final Duration IDLE_DURATION = Duration.ofMinutes(5);

    /**
     * Tries to acquires the clean-up lock.
     *
     * @param writeLock Whether write lock or read lock should be acquired
     * @return The lock acquisition result
     * @throws OXException If lock acquisition fails
     */
    public LockAcquisition acquireCleanUpTaskLock(boolean writeLock) throws OXException {
        return acquireCleanUpTaskLock(writeLock, Services.requireService(DatabaseService.class));
    }

    /**
     * Tries to acquires the clean-up lock.
     *
     * @param writeLock Whether write lock or read lock should be acquired
     * @param databaseService The database service to use
     * @return The lock acquisition result
     * @throws OXException If lock acquisition fails
     */
    public LockAcquisition acquireCleanUpTaskLock(boolean writeLock, DatabaseService databaseService) throws OXException {
        TimerService timerService = Services.requireService(TimerService.class);
        boolean modified = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection writeCon = databaseService.getWritable();
        try {
            stmt = writeCon.prepareStatement("SELECT text FROM reason_text WHERE id=?");
            stmt.setInt(1, LOCK_ID);
            rs = stmt.executeQuery();
            String prevToken = rs.next() ? rs.getString(1) : null;
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            // Check existent lock entry
            if (prevToken == null || isExpired(prevToken)) {
                // No lock entry existing or expired
                if (prevToken != null) {
                    // Expired...
                    stmt = writeCon.prepareStatement("DELETE FROM reason_text WHERE id=?");
                    stmt.setInt(1, LOCK_ID);
                    modified = stmt.executeUpdate() > 0;
                    Databases.closeSQLStuff(stmt);
                    stmt = null;
                }

                // Try to INSERT.
                String lock = writeLock ? LOCK_ID_WRITE : "1";
                String currentToken = generateLockToken(lock);
                stmt = writeCon.prepareStatement("INSERT INTO reason_text (id, text) VALUES (?, ?)");
                stmt.setInt(1, LOCK_ID);
                stmt.setString(2, currentToken);
                try {
                    modified |= stmt.executeUpdate() > 0;
                    if (modified == false) {
                        // Lock could NOT be acquired
                        Databases.closeSQLStuff(stmt);
                        stmt = null;
                        databaseService.backWritableAfterReading(writeCon);
                        writeCon = null;
                        return acquireCleanUpTaskLock(writeLock, databaseService);
                    }
                } catch (SQLException e) {
                    if (Databases.isPrimaryKeyConflictInMySQL(e)) {
                        // Lock could NOT be acquired
                        Databases.closeSQLStuff(stmt);
                        stmt = null;
                        databaseService.backWritableAfterReading(writeCon);
                        writeCon = null;
                        return acquireCleanUpTaskLock(writeLock, databaseService);
                    }
                    throw e;
                }
                // Lock could be acquired
                return forAcquiredLock(writeLock, timerService, LOG);
            }

            // Lock entry does exist and not expired.
            if (writeLock) {
                // Write lock shall be acquired
                return NOT_ACQUIRED;
            }

            // Determine lock identifier
            String prevLockId = extractLockIdFrom(prevToken);
            if (LOCK_ID_WRITE.equals(prevLockId)) {
                // Write lock currently held by another process
                return NOT_ACQUIRED;
            }

            // Try to atomically increment read lock counter
            int previousCount = Strings.parseUnsignedInt(prevLockId);
            if (previousCount < 0) {
                throw new IllegalStateException("Illegal clean-up lock token: " + prevToken);
            }

            String lock = Integer.toString(previousCount + 1);
            String currentToken = generateLockToken(lock);
            stmt = writeCon.prepareStatement("UPDATE reason_text SET text=? WHERE id=? AND text=?");
            stmt.setString(1, currentToken);
            stmt.setInt(2, LOCK_ID);
            stmt.setString(3, prevToken);
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
            return forAcquiredLock(writeLock, timerService, LOG);
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
     * Annotates given lock identifier with current time stamp.
     * <p>
     * <pre>
     *   LOCKED --&gt; LOCKED-1616084059000
     * </pre>
     *
     * @param lock The identifier
     * @return The token for given lock identifier
     */
    static String generateLockToken(String lock) {
        return new StringBuilder(lock).append(DELIMITER).append(System.currentTimeMillis()).toString();
    }

    /**
     * Extracts the lock identifier from given token.
     * <p>
     * <pre>
     *   LOCKED-1616084059000 --&gt; LOCKED
     * </pre>
     *
     * @param token The token; e.g. <code>"LOCKED-1616084059000"</code>
     * @return The lock identifier
     */
    static String extractLockIdFrom(String token) {
        int pos = token.indexOf(DELIMITER);
        return pos <= 0 ? token : token.substring(0, pos);
    }

    /**
     * Checks if given token is considered as expired.
     *
     * @param token The token to check; e.g. <code>"LOCKED-1616084059000"</code>
     * @return <code>true</code> if expired; otherwise <code>false</code>
     * @throws NumberFormatException If token is of invalid format
     */
    private static boolean isExpired(String token) {
        Duration duration = determineDurationFor(token);
        return duration == null || duration.compareTo(IDLE_DURATION) > 0;
    }

    /**
     * Determines the duration for given token.
     * <p>
     * <pre>
     *   LOCKED-1616084059000 --&gt; 1616084059000
     * </pre>
     *
     * @param token The token; e.g. <code>"LOCKED-1616084059000"</code>
     * @return The token's duration or <code>null</code> if time stamp information is absent
     * @throws NumberFormatException If token is of invalid format
     */
    private static Duration determineDurationFor(String token) {
        int pos = token.indexOf(DELIMITER);
        if (pos <= 0) {
            return null;
        }
        long timeStamp = Long.parseLong(token.substring(pos + 1));
        return Duration.ofMillis(System.currentTimeMillis() - timeStamp);
    }

    /**
     * Tries to releases the clean-up lock.
     *
     * @param lockAcquisition The previously acquired instance
     * @return <code>true</code> if lock could be successfully released; otherwise <code>false</code>
     * @throws OXException If releasing lock fails
     */
    public boolean releaseCleanUpTaskLock(LockAcquisition lockAcquisition) throws OXException {
        if (lockAcquisition == null || lockAcquisition.isNotAcquired()) {
            return false;
        }

        // Close lock acquisition
        lockAcquisition.close();

        // Drop lock
        return doReleaseCleanUpTaskLock(lockAcquisition.isWriteLock(), Services.requireService(DatabaseService.class));
    }

    /**
     * Tries to releases the clean-up lock.
     *
     * @param lockAcquisition The previously acquired instance
     * @param databaseService The database service to use
     * @return <code>true</code> if lock could be successfully released; otherwise <code>false</code>
     * @throws OXException If releasing lock fails
     */
    public boolean releaseCleanUpTaskLock(LockAcquisition lockAcquisition, DatabaseService databaseService) throws OXException {
        if (lockAcquisition == null || lockAcquisition.isNotAcquired()) {
            return false;
        }

        // Close lock acquisition
        lockAcquisition.close();

        // Drop lock
        return doReleaseCleanUpTaskLock(lockAcquisition.isWriteLock(), databaseService);
    }

    /**
     * Tries to releases the clean-up lock.
     *
     * @param writeLock Whether write lock or read lock should be released
     * @param databaseService The database service to use
     * @return <code>true</code> if lock could be successfully released; otherwise <code>false</code>
     * @throws OXException If releasing lock fails
     */
    private boolean doReleaseCleanUpTaskLock(boolean writeLock, DatabaseService databaseService) throws OXException {
        boolean modified = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection writeCon = databaseService.getWritable();
        try {
            // Read existent token
            stmt = writeCon.prepareStatement("SELECT text FROM reason_text WHERE id=?");
            stmt.setInt(1, LOCK_ID);
            rs = stmt.executeQuery();
            String prevToken = rs.next() ? rs.getString(1) : null;
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            if (prevToken == null) {
                // Nothing to release
                return false;
            }

            // Write lock should be released
            if (writeLock) {
                if (prevToken.startsWith(LOCK_ID_WRITE) == false) {
                    throw new IllegalStateException("Unexpected clean-up lock token: " + prevToken);
                }

                stmt = writeCon.prepareStatement("DELETE FROM reason_text WHERE id=? AND text=?");
                stmt.setInt(1, LOCK_ID);
                stmt.setString(2, prevToken);
                modified = stmt.executeUpdate() > 0;
                return modified;
            }

            // Read lock should be released. Try to atomically decrement read lock counter
            String prevLock = extractLockIdFrom(prevToken);
            int previousCount = Strings.parseUnsignedInt(prevLock);
            if (previousCount < 0) {
                throw new IllegalStateException("Illegal clean-up lock token: " + prevToken);
            }

            if (previousCount == 1) {
                stmt = writeCon.prepareStatement("DELETE FROM reason_text WHERE id=? AND text=?");
                stmt.setInt(1, LOCK_ID);
                stmt.setString(2, prevToken);
            } else {
                stmt = writeCon.prepareStatement("UPDATE reason_text SET text=? WHERE id=? AND text=?");
                stmt.setString(1, generateLockToken(Integer.toString(previousCount - 1)));
                stmt.setInt(2, LOCK_ID);
                stmt.setString(3, prevToken);
            }
            modified = stmt.executeUpdate() > 0;
            if (modified == false) {
                // Lock could NOT be released
                Databases.closeSQLStuff(stmt);
                stmt = null;
                databaseService.backWritableAfterReading(writeCon);
                writeCon = null;
                return doReleaseCleanUpTaskLock(writeLock, databaseService);
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

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the <code>LockAcquisition</code> instance for an acquired lock.
     *
     * @param writeLock Whether write or read lock
     * @param timerService The timer service to use
     * @param logger The logger to use
     * @return The appropriate <code>LockAcquisition</code> instance
     */
    private static LockAcquisition forAcquiredLock(boolean writeLock, TimerService timerService, org.slf4j.Logger logger) {
        ScheduledTimerTask timerTask = null;
        try {
            LockToucher task = new LockToucher(writeLock, logger);
            timerTask = timerService.scheduleWithFixedDelay(task, REFRESH_MINUTES, REFRESH_MINUTES, TimeUnit.MINUTES);
            LockAcquisition retval = new LockAcquisition(true, writeLock, timerTask, timerService, task);
            timerTask = null;
            return retval;
        } finally {
            if (timerTask != null) {
                timerTask.cancel();
                timerService.purge();
            }
        }
    }

    /** The constant signaling that lock has not been acquired */
    private static final LockAcquisition NOT_ACQUIRED = new LockAcquisition(false, false, null, null, null);

    /**
     * Represents a lock acquisition for data export lock.
     */
    public static final class LockAcquisition {

        private final boolean acquired;
        private final boolean writeLock;
        private final TimerService timerService;
        private final LockToucher task;
        private ScheduledTimerTask timerTask; // Guarded by synchronized

        /**
         * Initializes a new {@link LockAcquisition}.
         *
         * @param acquired The flag signaling whether lock has been acquired or not
         * @param writeLock Whether acquired lock is a write or a read lock
         * @param timerTask The associated timer task responsible for updating lock's keep-alive time stamp
         * @param timerService The timer service to use
         * @param task The toucher task
         */
        LockAcquisition(boolean acquired, boolean writeLock, ScheduledTimerTask timerTask, TimerService timerService, LockToucher task) {
            super();
            this.acquired = acquired;
            this.writeLock = writeLock;
            this.timerTask = timerTask;
            this.timerService = timerService;
            this.task = task;
        }

        /**
         * Gets the flag signaling whether lock has been acquired or not.
         *
         * @return <code>true</code> if <b>not</b> acquired; otherwise <code>false</code> if acquired
         */
        public boolean isNotAcquired() {
            return acquired == false;
        }

        /**
         * Gets the flag signaling whether lock has been acquired or not.
         *
         * @return <code>true</code> if acquired; otherwise <code>false</code>
         */
        public boolean isAcquired() {
            return acquired;
        }

        /**
         * Checks whether acquired lock is a write or a read lock.
         *
         * @return <code>true</code> if acquired lock is a write; otherwise <code>false</code> for a read lock
         */
        public boolean isWriteLock() {
            return writeLock;
        }

        /**
         * Closes this instance.
         */
        void close() {
            if (task != null) {
                synchronized (task) {
                    task.markClosed();
                    stopTimerTask();
                }
            }
        }

        private void stopTimerTask() {
            ScheduledTimerTask timerTask = this.timerTask;
            if (timerTask != null) {
                this.timerTask = null;
                timerTask.cancel();
                timerService.purge();
            }
        }
    }

    // ---------------------------------------------------- End of class LockAcquisition ---------------------------------------------------

    /**
     * Touches a lock's keep-alive time stamp.
     */
    private static class LockToucher implements Runnable {

        private final boolean writeLock;
        private final org.slf4j.Logger logger;
        private boolean closed;

        /**
         * Initializes a new {@link LockToucher}.
         *
         * @param writeLock Whether write or read lock is supposed to be updated
         * @param logger The logger to use
         */
        LockToucher(boolean writeLock, org.slf4j.Logger logger) {
            super();
            this.writeLock = writeLock;
            this.logger = logger;
            closed = false;
        }

        /**
         * Marks this instance as closed.
         */
        synchronized void markClosed() {
            this.closed = true;
        }

        @Override
        public synchronized void run() {
            if (!closed) {
                try {
                    DatabaseService databaseService = Services.requireService(DatabaseService.class);
                    doTouchLock(databaseService);
                } catch (Exception e) {
                    logger.warn("Failed to update keep-alive time stamp of clean-up lock for data export files", e);
                }
            }
        }

        private void doTouchLock(DatabaseService databaseService) throws OXException, SQLException {
            boolean modified = false;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            Connection writeCon = databaseService.getWritable();
            try {
                stmt = writeCon.prepareStatement("SELECT text FROM reason_text WHERE id=?");
                stmt.setInt(1, LOCK_ID);
                rs = stmt.executeQuery();
                String prevToken = rs.next() ? rs.getString(1) : null;
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                if (prevToken == null) {
                    // No such token
                    return;
                }

                String prevLock = null;
                if (writeLock) {
                    if (!prevToken.startsWith(LOCK_ID_WRITE + DELIMITER)) {
                        // Token does not start with this toucher's expected lock identifier
                        return;
                    }
                } else {
                    prevLock = extractLockIdFrom(prevToken);
                    int previousCount = Strings.parseUnsignedInt(prevLock);
                    if (previousCount < 0) {
                        // Token does not start with this toucher's expected lock identifier
                        return;
                    }
                }

                stmt = writeCon.prepareStatement("UPDATE reason_text SET text=? WHERE id=? AND text=?");
                stmt.setString(1, generateLockToken(prevLock == null ? extractLockIdFrom(prevToken) : prevLock));
                stmt.setInt(2, LOCK_ID);
                stmt.setString(3, prevToken);
                modified = stmt.executeUpdate() > 0;
                if (modified) {
                    logger.info("Successfully updated keep-alive time stamp of clean-up lock for data export files");
                }
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
    } // End of class LockToucher

}
