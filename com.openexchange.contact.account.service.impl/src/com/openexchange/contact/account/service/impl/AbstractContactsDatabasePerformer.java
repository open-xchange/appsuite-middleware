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

package com.openexchange.contact.account.service.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.provider.ContactsProviderExceptionCodes;
import com.openexchange.contact.storage.ContactStorages;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.Databases.ConnectionStatus;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.policy.retry.ExponentialBackOffRetryPolicy;
import com.openexchange.policy.retry.RetryPolicy;

/**
 * {@link AbstractContactsDatabasePerformer}
 *
 * @param <T> The return type of the operation
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
abstract class AbstractContactsDatabasePerformer<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractContactsDatabasePerformer.class);

    /** The default maximum number of retry attempts */
    protected static final int DEFAULT_RETRIES = 3;

    private final Connection foreignConnection;
    private final RetryPolicy retryPolicy;
    private final DatabaseService databaseService;
    final int contextId;

    /**
     * Initialises a new {@link AbstractContactsDatabasePerformer}.
     * 
     * @param dbService A reference to the database service
     * @param contextId The context identifier
     */
    AbstractContactsDatabasePerformer(DatabaseService databaseService, int contextId) {
        this(databaseService, contextId, null);
    }

    /**
     * Initialises a new {@link AbstractContactsDatabasePerformer}.
     *
     * @param dbService A reference to the database service
     * @param contextId The context identifier
     * @param optConnection An optional <i>outer</i> database connection to use
     */
    AbstractContactsDatabasePerformer(DatabaseService databaseService, int contextId, Connection optConnection) {
        super();
        this.databaseService = databaseService;
        this.contextId = contextId;
        this.foreignConnection = optConnection;
        this.retryPolicy = new ExponentialBackOffRetryPolicy(DEFAULT_RETRIES);
    }

    /**
     * Performs the operation using the initialised contacts storage.
     *
     * @param storage The initialised contacts storage to use
     * @return The result
     */
    abstract T perform(ContactStorages storage) throws OXException;

    /**
     * Initialises the contacts storage.
     *
     * @param dbProvider The initialised database provider to use
     * @param txPolicy The transaction policy to use
     * @return The initialised storage
     */
    abstract ContactStorages initStorage(DBProvider dbProvider, DBTransactionPolicy txPolicy) throws OXException;

    /**
     * Invoked after a connection was acquired and backed.
     * <p/>
     * Does nothing by default; override if applicable.
     *
     * @param connection The acquired connection, or <code>null</code> if a previously acquired connection is backed to the pool
     */
    protected void onConnection(Connection connection) {
        //
    }

    /**
     * Executes the read-only storage operation.
     *
     * @return The result
     */
    T executeQuery() throws OXException {
        if (null != foreignConnection) {
            return execute(new SimpleDBProvider(foreignConnection, null), DBTransactionPolicy.NO_TRANSACTIONS);
        }
        return doExecuteQuery();
    }

    /**
     * Executes the read/write storage operation
     *
     * @return The result
     */
    T executeUpdate() throws OXException {
        if (null != foreignConnection) {
            return execute(new SimpleDBProvider(foreignConnection, foreignConnection), DBTransactionPolicy.NO_TRANSACTIONS);
        }
        OXException toThrow;
        do {
            try {
                return doExecuteUpdate();
            } catch (OXException e) {
                LOG.info("Error performing storage operation ('{}'), will try again in a bit ({}/{})...", e.getMessage(), I(retryPolicy.retryCount()), I(retryPolicy.getMaxTries()));
                toThrow = e;
            }
        } while (retryPolicy.isRetryAllowed() || mayTryAgain(toThrow));
        throw toThrow;
    }

    ///////////////////////////////// HELPERS ////////////////////////////

    /**
     * Gets a value indicating whether the storage operation may be tried again for an occurred exception or not.
     * <p/>
     * Tries again for {@link Category#CATEGORY_TRY_AGAIN} exceptions by default; override of applicable.
     *
     * @param e The exception to check
     * @return <code>true</code> if the operation may be tried again, <code>false</code>, otherwise
     */
    private boolean mayTryAgain(OXException e) {
        return null != e && Category.CATEGORY_TRY_AGAIN.equals(e.getCategory());
    }

    /**
     * Executes the read-only storage operation in a transaction.
     *
     * @return The result
     */
    private T doExecuteQuery() throws OXException {
        Connection readConnection = null;
        try {
            readConnection = databaseService.getReadOnly(contextId);
            onConnection(readConnection);
            return execute(new SimpleDBProvider(readConnection, null), DBTransactionPolicy.NO_TRANSACTIONS);
        } finally {
            onConnection(null);
            if (null != readConnection) {
                databaseService.backReadOnly(contextId, readConnection);
            }
        }
    }

    /**
     * Executes the read/write storage operation in a transaction.
     *
     * @return The result
     */
    private T doExecuteUpdate() throws OXException {
        ConnectionStatus status = ConnectionStatus.INITIALISED;
        Connection writeConnection = null;
        try {
            writeConnection = databaseService.getWritable(contextId);
            writeConnection.setAutoCommit(false);
            status = ConnectionStatus.FAILED;

            onConnection(writeConnection);
            T result = execute(new SimpleDBProvider(writeConnection, writeConnection), DBTransactionPolicy.NO_TRANSACTIONS);

            writeConnection.commit();
            status = ConnectionStatus.SUCCEEDED;
            return result;
        } catch (OXException e) {
            throw e;
        } catch (SQLException e) {
            throw ContactsProviderExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            onConnection(null);
            Databases.backWriteable(databaseService, writeConnection, contextId, status);
        }
    }

    /**
     * Executes the database operation
     *
     * @param dbProvider The database provider
     * @param txPolicy The database transaction policy
     * @return The result
     * @throws OXException if an error is occurred
     */
    private T execute(DBProvider dbProvider, DBTransactionPolicy txPolicy) throws OXException {
        ContactStorages storage = initStorage(dbProvider, txPolicy);
        long start = System.currentTimeMillis();
        try {
            return perform(storage);
        } finally {
            LOG.trace("Contacts storage operation finished after {} ms.", L(System.currentTimeMillis() - start));
        }
    }
}
