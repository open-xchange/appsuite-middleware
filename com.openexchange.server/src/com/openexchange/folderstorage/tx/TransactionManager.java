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

package com.openexchange.folderstorage.tx;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.AfterReadAwareFolderStorage.Mode;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.database.DatabaseFolderStorage.ConnectionMode;
import com.openexchange.folderstorage.database.DatabaseFolderType;
import com.openexchange.folderstorage.database.DatabaseParameterConstants;
import com.openexchange.folderstorage.osgi.FolderStorageServices;


/**
 * The {@link TransactionManager} helps to control transactional {@link FolderStorage} operations
 * for write operations.<br>
 * <br>
 * Usage:
 *
 * <pre>
 * TransactionManager transactionManager = TransactionManager.initTransaction(storageParameters);
 * boolean started = false;
 * try {
 *     FolderStorage folderStorage = getFolderStorage();
 *     started = folderStorage.startTransaction(parameters, true);
 *     // Operate on folderStorage...
 *
 *     if (started) {
 *         folderStorage.commitTransaction(parameters);
 *     }
 *
 *     transactionManager.commit();
 * } catch (Exception e) {
 *     if (started) {
 *         folderStorage.rollback(parameters);
 *     }
 *
 *     transactionManager.rollback();
 *     throw e;
 * }
 * </pre>
 *
 * The {@link TransactionManager} will only function correctly, if every {@link FolderStorage} respects
 * its existence. That means {@link FolderStorage#startTransaction(StorageParameters, boolean)} must be
 * constructed in this way:<br>
 * <pre>
 * // Prepare transaction...
 * TransactionManager.isManagedTransaction(parameters)) {
 *     TransactionManager.getTransactionManager(parameters).transactionStarted(this);
 *     return false;
 * }
 *
 * return true;
 * </pre>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class TransactionManager {

    public static final String PARAMETER_KEY = TransactionManager.class.getName();

    /**
     * Initializes a new {@link TransactionManager} or returns the currently active one if already instantiated.
     * On initialization a writable {@link Connection} is acquired and put into the {@link StorageParameters} for
     * {@link DatabaseFolderType} and {@link DatabaseParameterConstants#PARAM_CONNECTION} as key.
     *
     * @param storageParameters The {@link StorageParameters}, never <code>null</code>.
     * @return The {@link TransactionManager}.
     */
    public static TransactionManager initTransaction(StorageParameters storageParameters) throws OXException {
        TransactionManager transactionManager = getTransactionManager(storageParameters);
        if (transactionManager == null) {
            transactionManager = new TransactionManager(storageParameters);
            storageParameters.putParameter(FolderType.GLOBAL, PARAMETER_KEY, transactionManager);
        }

        ++transactionManager.initCount;
        return transactionManager;
    }

    /**
     * Checks whether the current transaction is managed by a {@link TransactionManager} or not.
     *
     * @param storageParameters The {@link StorageParameters}, never <code>null</code>.
     * @return <code>true</code> if the transaction is managed. Otherwise <code>false</code>.
     */
    public static boolean isManagedTransaction(StorageParameters storageParameters) {
        return getTransactionManager(storageParameters) != null;
    }

    /**
     * Gets the currently active {@link TransactionManager}.
     *
     * @param storageParameters The {@link StorageParameters}, never <code>null</code>.
     * @return The {@link TransactionManager} or <code>null</code> if this transaction is not managed.
     */
    public static TransactionManager getTransactionManager(StorageParameters storageParameters) {
        return storageParameters.getParameter(FolderType.GLOBAL, PARAMETER_KEY);
    }

    // -------------------------------------------------------------------------------------------------------------------------

    private final Collection<FolderStorage> openedStorages = new LinkedHashSet<FolderStorage>(6);

    private final StorageParameters storageParameters;

    private DatabaseService dbService;

    private Connection connection = null;

    private boolean ownsConnection;

    private int initCount = 0;

    /**
     * Initializes a new {@link TransactionManager}.
     * @param storageParameters
     * @throws OXException
     * @throws SQLException
     */
    private TransactionManager(StorageParameters storageParameters) throws OXException {
        super();
        this.storageParameters = storageParameters;
        ConnectionMode connectionMode = storageParameters.getParameter(DatabaseFolderType.getInstance(), DatabaseParameterConstants.PARAM_CONNECTION);
        if (connectionMode == null && !initConnectionViaDecorator()) {
            dbService = FolderStorageServices.requireService(DatabaseService.class);
            connection = dbService.getWritable(storageParameters.getContext());
            connectionMode = new ConnectionMode(new ResilientConnection(connection), Mode.WRITE);
            storageParameters.putParameter(DatabaseFolderType.getInstance(), DatabaseParameterConstants.PARAM_CONNECTION, connectionMode);
            ownsConnection = true;
            try {
                Databases.startTransaction(connection);
            } catch (SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e.getMessage());
            }
        }
    }

    private boolean initConnectionViaDecorator() throws OXException {
        FolderServiceDecorator decorator = storageParameters.getDecorator();
        if (decorator != null) {
            Object connectionProperty = decorator.getProperty(Connection.class.getName());
            try {
                if (connectionProperty != null && connectionProperty instanceof Connection && !((Connection) connectionProperty).isReadOnly()) {
                    connection = (Connection) connectionProperty;
                    ConnectionMode connectionMode = new ConnectionMode(new ResilientConnection((Connection) connectionProperty), Mode.WRITE);
                    storageParameters.putParameter(DatabaseFolderType.getInstance(), DatabaseParameterConstants.PARAM_CONNECTION, connectionMode);
                    ownsConnection = false;
                    return true;
                }
            } catch (SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e.getMessage());
            }
        }

        return false;
    }

    /**
     * Adds a {@link FolderStorage} to the list of storages that need to be committed or rolled-back.
     *
     * @param The {@link FolderStorage}, never <code>null</code>.
     */
    public void transactionStarted(FolderStorage storage) {
        openedStorages.add(storage);
    }

    /**
     * Gets the active database connection, if it is controlled by this {@link TransactionManager}.
     * A connection is not controlled by the manager, if the {@link StorageParameters} contain a
     * {@link ConnectionMode} for type {@link DatabaseFolderType} and key {@link DatabaseParameterConstants#PARAM_CONNECTION}.
     */
    public Connection getConnection() {
        return connection;
    }


    /**
     * Rolls back this transaction via calling rollback on the managed database connection
     * and {@link FolderStorage#rollback(StorageParameters)} on every {@link FolderStorage}
     * that has been submitted via {@link TransactionManager#transactionStarted(FolderStorage)}.
     *
     * The transaction is only rolled back if the caller initialized this {@link TransactionManager} instance.
     * Otherwise the call has no effect.
     */
    public void rollback() {
        if (--initCount <= 0) {
            if (ownsConnection && connection != null) {
                Databases.rollback(connection);
                Databases.autocommit(connection);
                dbService.backWritableAfterReading(storageParameters.getContext(), connection);
            }

            for (FolderStorage fs : openedStorages) {
                fs.rollback(storageParameters);
            }
        }
    }

    /**
     * Commits this transaction via calling commit on the managed database connection
     * and {@link FolderStorage#commitTransaction(StorageParameters)} on every {@link FolderStorage}
     * that has been submitted via {@link TransactionManager#transactionStarted(FolderStorage)}.
     *
     * The transaction is only committed if the caller initialized this {@link TransactionManager} instance.
     * Otherwise the call has no effect.
     *
     * @throws OXException If any underlying commit operation fails.
     *         If so, {@link TransactionManager#rollback()} must be called.
     */
    public void commit() throws OXException {
        if (--initCount == 0) {
            if (ownsConnection && connection != null) {
                try {
                    connection.commit();
                } catch (SQLException e) {
                    throw FolderExceptionErrorMessage.SQL_ERROR.create(e.getMessage());
                }

                Databases.autocommit(connection);
                dbService.backWritable(storageParameters.getContext(), connection);
            }

            for (FolderStorage fs : openedStorages) {
                fs.commitTransaction(storageParameters);
            }
        }
    }

}
