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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.AfterReadAwareFolderStorage.Mode;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.database.DatabaseFolderStorage.ConnectionMode;
import com.openexchange.folderstorage.database.DatabaseFolderType;
import com.openexchange.folderstorage.database.DatabaseParameterConstants;
import com.openexchange.folderstorage.database.DatabaseServiceRegistry;


/**
 * {@link TransactionManager}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class TransactionManager {

    public static final String PARAMETER_KEY = TransactionManager.class.getName();

    private final List<FolderStorage> openedStorages = new ArrayList<FolderStorage>(4);

    private final StorageParameters storageParameters;

    private DatabaseService dbService;

    private Connection connection = null;

    private int initCount = 0;

    /**
     * Initializes a new {@link TransactionManager}.
     * @param storageParameters
     * @throws OXException
     */
    private TransactionManager(StorageParameters storageParameters) throws OXException {
        super();
        this.storageParameters = storageParameters;
        ConnectionMode connectionMode = storageParameters.getParameter(DatabaseFolderType.getInstance(), DatabaseParameterConstants.PARAM_CONNECTION);
        if (connectionMode == null) {
            dbService = DatabaseServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
            connection = dbService.getWritable(storageParameters.getContext());
            connectionMode = new ConnectionMode(new ResilientConnection(connection), Mode.WRITE);
            storageParameters.putParameter(DatabaseFolderType.getInstance(), DatabaseParameterConstants.PARAM_CONNECTION, connectionMode);
            try {
                Databases.startTransaction(connection);
            } catch (SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e.getMessage());
            }
        }
    }

    public static TransactionManager initTransaction(StorageParameters storageParameters) throws OXException {
        TransactionManager transactionManager = getTransactionManager(storageParameters);
        if (transactionManager == null) {
            transactionManager = new TransactionManager(storageParameters);
            storageParameters.putParameter(FolderType.GLOBAL, PARAMETER_KEY, transactionManager);
        }

        ++transactionManager.initCount;
        return transactionManager;
    }

    public static boolean isManagedTransaction(StorageParameters storageParameters) {
        return getTransactionManager(storageParameters) != null;
    }

    public static TransactionManager getTransactionManager(StorageParameters storageParameters) {
        return storageParameters.getParameter(FolderType.GLOBAL, PARAMETER_KEY);
    }

    public void addOpenedStorage(FolderStorage storage) throws OXException {
        openedStorages.add(storage);
    }

    public void addOpenedStorages(Collection<FolderStorage> storages) throws OXException {
        openedStorages.addAll(storages);
    }

    public Connection getConnection() {
        return connection;
    }

    public void rollback() {
        if (--initCount <= 0) {
            if (connection != null) {
                Databases.rollback(connection);
                Databases.autocommit(connection);
                dbService.backWritable(storageParameters.getContext(), connection);
            }

            for (FolderStorage fs : openedStorages) {
                fs.rollback(storageParameters);
            }
        }
    }

    public void commit() throws OXException {
        if (--initCount == 0) {
            if (connection != null) {
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
