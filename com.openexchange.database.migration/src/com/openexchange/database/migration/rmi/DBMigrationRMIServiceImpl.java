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

package com.openexchange.database.migration.rmi;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.migration.DBMigration;
import com.openexchange.database.migration.DBMigrationConnectionProvider;
import com.openexchange.database.migration.internal.DBMigrationExecutorServiceImpl;
import com.openexchange.exception.OXException;

/**
 * {@link DBMigrationRMIServiceImpl}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class DBMigrationRMIServiceImpl implements DBMigrationRMIService {

    private static final Logger LOG = LoggerFactory.getLogger(DBMigrationRMIServiceImpl.class);

    private final DBMigrationExecutorServiceImpl dbMigrationExecutorService;
    private final ConcurrentMap<String, DBMigration> registeredMigrations;

    /**
     * Initialises a new {@link DBMigrationRMIServiceImpl}.
     * 
     * @param dbMigrationExecutorService A reference to the DB migration service
     * @param migration The DB migration
     */
    public DBMigrationRMIServiceImpl(DBMigrationExecutorServiceImpl dbMigrationExecutorService) {
        super();
        Validate.notNull(dbMigrationExecutorService, "DBMigrationExecuterService must not be null!");
        this.dbMigrationExecutorService = dbMigrationExecutorService;
        this.registeredMigrations = new ConcurrentHashMap<>(4);
    }

    @Override
    public void forceMigration(String schemaName) throws RemoteException {
        try {
            dbMigrationExecutorService.scheduleDBMigration(getMigration(schemaName)).awaitCompletion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException(e.getMessage(), e);
        } catch (ExecutionException e) {
            throw new RemoteException(e.getMessage(), e);
        }

    }

    @Override
    public void rollbackMigration(String schemaName, String changeSetTag) throws RemoteException {
        try {
            dbMigrationExecutorService.scheduleDBRollback(getMigration(schemaName), changeSetTag).awaitCompletion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException(e.getMessage(), e);
        } catch (ExecutionException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void releaseLocks(String schemaName) throws RemoteException {
        DBMigration migration = getMigration(schemaName);
        DBMigrationConnectionProvider connectionProvider = migration.getConnectionProvider();
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = connectionProvider.get();
            stmt = connection.prepareStatement("UPDATE DATABASECHANGELOGLOCK SET LOCKED=0, LOCKGRANTED=null, LOCKEDBY=null where ID=1;");
            stmt.execute();
        } catch (Exception e) {
            LOG.error("Not able to release the lock for table DATABASECHANGELOGLOCK", e);
            String message = e.getMessage();
            throw new RemoteException(message, new Exception(message));
        } finally {
            if (null != stmt) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOG.error("", e);
                }
            }
            if (connection != null) {
                connectionProvider.back(connection);
            }
        }
    }

    @Override
    public String getMigrationStatus(String schemaName) throws RemoteException {
        try {
            return dbMigrationExecutorService.getDBStatus(getMigration(schemaName));
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public String getLockStatus(String schemaName) throws RemoteException {
        try {
            return dbMigrationExecutorService.listDBLocks(getMigration(schemaName));
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    private DBMigration getMigration(String schemaName) {
        DBMigration migration = registeredMigrations.get(schemaName);
        Validate.notNull(migration, "Could not find any DBMigration registered for schema with name  '" + schemaName + "'");
        return migration;
    }

    /**
     * Registers and hooks up the specified migration with a new {@link DBMigrationRMIService}
     * 
     * @param migration The {@link DBMigration} to register
     * @return <code>true</code> if the migration was successfully registered;
     *         <code>false</code> if another migration already exists
     */
    public boolean register(DBMigration migration) {
        Validate.notNull(migration, "DBMigration must not be null!");
        if (null == registeredMigrations.putIfAbsent(migration.getSchemaName(), migration)) {
            return true;
        }
        LOG.error("DBMigration {} already registered.", migration);
        return false;
    }
}
