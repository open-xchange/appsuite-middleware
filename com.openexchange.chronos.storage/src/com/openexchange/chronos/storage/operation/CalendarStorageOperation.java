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

package com.openexchange.chronos.storage.operation;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarStorageOperation}
 *
 * @param <T> The return type of the operation
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class CalendarStorageOperation<T> {

    /** A named logger instance */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarStorageOperation.class);

    /** The default maximum number of retry attempts */
    protected static final int DEFAULT_RETRIES = 3;

    /** The base number of milliseconds to wait until retrying */
    private static final int RETRY_BASE_DELAY = 500;

    /** The random number generator */
    private static final Random RANDOM = new Random();

    protected final int contextId;
    private final DatabaseService dbService;
    private final Connection foreignConnection;
    private final int maxRetries;

    private int retryCount;

    /**
     * Initializes a new {@link CalendarStorageOperation}.
     *
     * @param dbService A reference to the database service
     * @param contextId The context identifier
     */
    protected CalendarStorageOperation(DatabaseService dbService, int contextId) {
        this(dbService, contextId, DEFAULT_RETRIES, null);
    }

    /**
     * Initializes a new {@link CalendarStorageOperation}.
     *
     * @param dbService A reference to the database service
     * @param contextId The context identifier
     * @param maxRetries The maximum number of retry attempts when encountering recoverable storage errors, or <code>0</code> for no retries
     * @param optConnection An optional <i>outer</i> database connection to use
     */
    protected CalendarStorageOperation(DatabaseService dbService, int contextId, int maxRetries, Connection optConnection) {
        super();
        this.dbService = dbService;
        this.contextId = contextId;
        this.maxRetries = maxRetries;
        this.foreignConnection = optConnection;
    }

    /**
     * Initializes the calendar storage.
     *
     * @param dbProvider The initialized database provider to use
     * @param txPolicy The transaction policy to use
     * @return The initialized storage
     */
    protected abstract CalendarStorage initStorage(DBProvider dbProvider, DBTransactionPolicy txPolicy) throws OXException;

    /**
     * Performs the operation using the initialized calendar storage.
     * <p/>
     * During <i>write</i>-operations, may throw {@link CalendarExceptionCodes#DB_NOT_MODIFIED} to indicate that no data was actually
     * updated by the operation, and the connection can be returned to the pool <i>after reading</i>, hence bypass the replication
     * monitor. In this case, the operation always returns <code>null</code>.
     *
     * @param session The calendar session
     * @param storage The initialized calendar storage to use
     * @return The result
     */
    protected abstract T call(CalendarStorage storage) throws OXException;

    /**
     * Invoked after a connection was acquired and backed.
     * <p/>
     * Does nothing by default; override of applicable.
     *
     * @param connection The acquired connection, or <code>null</code> if a previously acquired connection is backed to the pool
     */
    protected void onConnection(Connection connection) {
        //
    }

    /**
     * Gets a value indicating whether the storage operation may be tried again for an occurred exception or not.
     * <p/>
     * Tries again for {@link Category#CATEGORY_TRY_AGAIN} exceptions by default; override of applicable.
     *
     * @param e The exception to check
     * @return <code>true</code> if the operation may be tried again, <code>false</code>, otherwise
     */
    protected boolean mayTryAgain(OXException e) {
        return null != e && Category.CATEGORY_TRY_AGAIN.equals(e.getCategory());
    }

    /**
     * Executes the read-only storage operation.
     *
     * @return The result
     */
    public T executeQuery() throws OXException {
        if (null != foreignConnection) {
            return execute(new SimpleDBProvider(foreignConnection, null), DBTransactionPolicy.NO_TRANSACTIONS);
        }
        return doExecuteQuery();
    }

    /**
     * Executes the read/write storage operation in a transaction.
     *
     * @return The result
     */
    public T executeUpdate() throws OXException {
        if (null != foreignConnection) {
            return execute(new SimpleDBProvider(foreignConnection, foreignConnection), DBTransactionPolicy.NO_TRANSACTIONS);
        }
        while (true) {
            try {
                return doExecuteUpdate();
            } catch (OXException e) {
                if (retryCount > maxRetries || false == mayTryAgain(e)) {
                    throw e;
                }
                retryCount++;
                int delay = RETRY_BASE_DELAY * retryCount + RANDOM.nextInt(RETRY_BASE_DELAY);
                LOG.info("Error performing storage operation (\"{}\"), trying again in {}ms ({}/{})...", e.getMessage(), I(delay), I(retryCount), I(maxRetries));
                LockSupport.parkNanos(delay * 1000000L);
            }
        }
    }

    private T doExecuteQuery() throws OXException {
        Connection readConnection = null;
        try {
            readConnection = dbService.getReadOnly(contextId);
            onConnection(readConnection);
            return execute(new SimpleDBProvider(readConnection, null), DBTransactionPolicy.NO_TRANSACTIONS);
        } finally {
            onConnection(null);
            if (null != readConnection) {
                dbService.backReadOnly(contextId, readConnection);
            }
        }
    }

    private T doExecuteUpdate() throws OXException {
        boolean committed = false;
        Connection writeConnection = null;
        try {
            writeConnection = dbService.getWritable(contextId);
            writeConnection.setAutoCommit(false);
            onConnection(writeConnection);
            T result = execute(new SimpleDBProvider(writeConnection, writeConnection), DBTransactionPolicy.NO_TRANSACTIONS);
            writeConnection.commit();
            committed = true;
            return result;
        } catch (OXException e) {
            if (CalendarExceptionCodes.DB_NOT_MODIFIED.equals(e)) {
                LOG.debug("No data modified, going to back writable connection after reading.", e);
                return null;
            }
            throw e;
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            onConnection(null);
            if (null != writeConnection) {
                if (false == committed) {
                    rollback(writeConnection);
                    autocommit(writeConnection);
                    dbService.backWritableAfterReading(contextId, writeConnection);
                } else {
                    autocommit(writeConnection);
                    dbService.backWritable(contextId, writeConnection);
                }
            }
        }
    }

    private T execute(DBProvider dbProvider, DBTransactionPolicy txPolicy) throws OXException {
        CalendarStorage storage = initStorage(dbProvider, txPolicy);
        long start = System.currentTimeMillis();
        try {
            return call(storage);
        } finally {
            LOG.trace("Calendar storage operation finished after {}ms.", L(System.currentTimeMillis() - start));
        }
    }

    /**
     * Collects and flushes all warnings from the supplied calendar storage.
     *
     * @param storage The calendar storage to collect the warnings from
     * @return The warnings, or an empty list if there were none
     */
    protected static List<OXException> collectWarnings(CalendarStorage storage) {
        Map<String, List<OXException>> warningsPerEventId = storage.getAndFlushWarnings();
        if (null == warningsPerEventId || warningsPerEventId.isEmpty()) {
            return Collections.emptyList();
        }
        List<OXException> warnings = new ArrayList<OXException>();
        for (List<OXException> value : warningsPerEventId.values()) {
            warnings.addAll(value);
        }
        return warnings;
    }

    private static void rollback(Connection connection) {
        if (null != connection) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                LOG.error("", e);
            }
        }
    }

    private static void autocommit(Connection connection) {
        if (null != connection) {
            try {
                if (false == connection.isClosed()) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                LOG.error("", e);
            }
        }
    }

}
