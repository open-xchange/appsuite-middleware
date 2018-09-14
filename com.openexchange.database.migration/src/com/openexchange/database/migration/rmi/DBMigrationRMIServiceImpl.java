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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.database.migration.rmi;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    private final DBMigration migration;

    /**
     * Initialises a new {@link DBMigrationRMIServiceImpl}.
     * 
     * @param dbMigrationExecutorService A reference to the DB migration service
     * @param migration The DB migration
     */
    public DBMigrationRMIServiceImpl(DBMigrationExecutorServiceImpl dbMigrationExecutorService, DBMigration migration) {
        super();
        Validate.notNull(dbMigrationExecutorService, "DBMigrationExecuterService must not be null!");
        Validate.notNull(migration, "DBMigration must not be null!");
        this.dbMigrationExecutorService = dbMigrationExecutorService;
        this.migration = migration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.database.migration.rmi.DBMigrationRMIService#forceMigration()
     */
    @Override
    public void forceMigration() throws RemoteException {
        try {
            dbMigrationExecutorService.scheduleDBMigration(migration).awaitCompletion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException(e.getMessage(), e);
        } catch (ExecutionException e) {
            throw new RemoteException(e.getMessage(), e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.database.migration.rmi.DBMigrationRMIService#rollbackMigration(java.lang.String)
     */
    @Override
    public void rollbackMigration(String changeSetTag) throws RemoteException {
        try {
            dbMigrationExecutorService.scheduleDBRollback(migration, changeSetTag).awaitCompletion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RemoteException(e.getMessage(), e);
        } catch (ExecutionException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.database.migration.rmi.DBMigrationRMIService#releaseLocks()
     */
    @Override
    public void releaseLocks() throws RemoteException {
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.database.migration.rmi.DBMigrationRMIService#getMigrationStatus()
     */
    @Override
    public String getMigrationStatus() throws RemoteException {
        try {
            return dbMigrationExecutorService.getDBStatus(migration);
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.database.migration.rmi.DBMigrationRMIService#getLockStatus()
     */
    @Override
    public String getLockStatus() throws RemoteException {
        try {
            return dbMigrationExecutorService.listDBLocks(migration);
        } catch (OXException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }
}
