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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;

/**
 * {@link RetryingTransactionClosure}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class RetryingTransactionClosure<V> {

    /**
     * Executes given closure; retrying if appropriate.
     *
     * @param closure The closure to execute
     * @param retries The number of retry attempts
     * @param con The connection to use
     * @return The return value
     * @throws IllegalArgumentException If number of retries is less than/equal to <code>0</code> (zero) or closure/connection is <code>null</code>
     * @throws SQLException If an SQL error occurs
     * @throws OXException Of an Open-Xchange error occurs
     */
    public static <V> V execute(SQLClosure<V> closure, int retries, Connection con) throws SQLException, OXException {
        return new RetryingTransactionClosure<V>(retries, closure).execute(con);
    }

    // ------------------------------------------------------------------------------------------------------------------------

    private final TransactionRollbackCondition condition;
    private final SQLClosure<V> closure;

    /**
     * Initializes a new {@link RetryingTransactionClosure}.
     */
    private RetryingTransactionClosure(int retries, SQLClosure<V> closure) {
        super();
        if (retries <= 0) {
            throw new IllegalArgumentException("Number of retries needs to be greater than 0 (zero)");
        }
        if (closure == null) {
            throw new IllegalArgumentException("Closure must not be null.");
        }
        this.closure = closure;
        condition = new TransactionRollbackCondition(retries);
    }

    /**
     * Executes this closure while opening a transaction using specified connection; retrying if appropriate.
     *
     * @param con The connection to use
     * @return The return value
     * @throws IllegalArgumentException If connection is <code>null</code>
     * @throws SQLException If an SQL error occurs
     * @throws OXException Of an Open-Xchange error occurs
     */
    public V execute(Connection con) throws SQLException, OXException {
        if (con == null) {
            throw new IllegalArgumentException("Connection must not be null.");
        }

        if (con.getAutoCommit()) {
            // Start new transaction
            do {
                int rollback = 0;
                try {
                    con.setAutoCommit(false);
                    rollback = 1;

                    V retval = closure.execute(con);

                    con.commit();
                    rollback = 2;
                    return retval;
                } catch (SQLException e) {
                    if (!condition.isFailedTransactionRollback(e)) {
                        throw e;
                    }
                } finally {
                    if (rollback > 0) {
                        if (rollback == 1) {
                            Databases.rollback(con);
                        }
                        Databases.autocommit(con);
                    }
                }
            } while (retryUpdate(condition));
        } else {
            // Transaction already started
            do {
                Savepoint savepoint = null;
                int rollback = 0;
                try {
                    savepoint = con.setSavepoint(UUIDs.getUnformattedStringFromRandom());
                    rollback = 1;

                    V retval = closure.execute(con);

                    Databases.releaseSavepoint(savepoint, con);
                    rollback = 2;
                    return retval;
                } catch (SQLException e) {
                    if (!condition.isFailedTransactionRollback(e)) {
                        throw e;
                    }
                } finally {
                    if (rollback == 1) {
                        Databases.rollback(savepoint, true, con);
                    }
                }
            } while (retryUpdate(condition));
        }
        return null;
    }

    private static boolean retryUpdate(TransactionRollbackCondition condition) throws SQLException {
        boolean retry = condition.checkRetry();
        if (retry) {
            // Wait with exponential back-off
            int retryCount = condition.getCount();
            long nanosToWait = TimeUnit.NANOSECONDS.convert((retryCount * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
        }
        return retry;
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    /**
     * Checks for retry condition for a failed transaction roll-back.
     */
    public static final class TransactionRollbackCondition {

        private final int max;

        private int count;

        private SQLException transactionRollbackException;

        /**
         * Initializes a new {@link TransactionRollbackCondition}.
         *
         * @param max The max. retry count
         */
        public TransactionRollbackCondition(final int max) {
            super();
            count = 0;
            this.max = max;
        }

        /**
         * Check for a failed transaction roll-back.
         *
         * @param e The SQL exception to check for a failed transaction roll-back
         * @return <code>true</code> a failed transaction roll-back; otherwise <code>false</code>
         */
        public boolean isFailedTransactionRollback(final SQLException e) {
            if (Databases.isTransactionRollbackException(e)) {
                transactionRollbackException = e;
                return true;
            }
            return false;
        }

        /**
         * Check for a failed transaction roll-back.
         *
         * @param e The exception to check for a failed transaction roll-back
         * @return <code>true</code> a failed transaction roll-back; otherwise <code>false</code>
         */
        public boolean isFailedTransactionRollback(final Exception e) {
            final SQLException sqle = Databases.extractSqlException(e);
            if (null != sqle && Databases.isTransactionRollbackException(sqle)) {
                transactionRollbackException = sqle;
                return true;
            }
            return false;
        }

        /**
         * Gets the recently checked <tt>SQLException</tt> reference that indicates a failed transaction roll-back.
         *
         * @return The recently checked <tt>SQLException</tt> reference
         */
        public SQLException getTransactionRollbackException() {
            return transactionRollbackException;
        }

        /**
         * Resets the reference that indicates a failed transaction roll-back.
         */
        public void resetTransactionRollbackException() {
            transactionRollbackException = null;
        }

        /**
         * Check for retry condition.
         * <p>
         * <b>Note</b>: {@link #isFailedTransactionRollback(SQLException)} is expected to be called prior to invoking this method.
         * <p>
         * If check returns <code>true</code>, <tt>SQLException</tt> reference is set to <code>null</code>.
         *
         * @return <code>true</code> if retry condition is met; otherwise <code>false</code>
         * @throws SQLException If retry-count is exceeded and previously checked <tt>SQLException</tt> indicated a failed transaction roll-back
         */
        public boolean checkRetry() throws SQLException {
            if (null == transactionRollbackException) {
                return false;
            }
            if (++count <= max) {
                transactionRollbackException = null;
                return true;
            }
            throw transactionRollbackException;
        }

        /**
         * Gets the number of retries
         *
         * @return The retry count
         */
        public int getCount() {
            return count;
        }
    }

}
