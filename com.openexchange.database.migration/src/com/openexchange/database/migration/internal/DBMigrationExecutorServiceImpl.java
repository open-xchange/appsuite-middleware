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
package com.openexchange.database.migration.internal;

import static com.openexchange.database.migration.internal.LiquibaseHelper.LIQUIBASE_NO_DEFINED_CONTEXT;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.exception.ValidationFailedException;
import com.openexchange.database.migration.DBMigration;
import com.openexchange.database.migration.DBMigrationCallback;
import com.openexchange.database.migration.DBMigrationExceptionCodes;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.database.migration.DBMigrationState;
import com.openexchange.database.migration.mbean.MBeanRegisterer;
import com.openexchange.exception.OXException;

/**
 * Implementation of {@link DBMigrationExecutorService} to execute database migrations via liquibase.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>s
 * @since 7.6.1
 */
public class DBMigrationExecutorServiceImpl implements DBMigrationExecutorService {

    private final DBMigrationExecutor executor;
    private MBeanRegisterer registerer;

    /**
     * Initializes a new {@link DBMigrationExecutorServiceImpl}.
     */
    public DBMigrationExecutorServiceImpl() {
        super();
        executor = new DBMigrationExecutor();
    }

    /**
     * Sets the MBean registerer instance to use.
     *
     * @param registerer The MBean registerer
     */
    public void setRegisterer(MBeanRegisterer registerer) {
        this.registerer = registerer;
    }

    @Override
    public DBMigrationState scheduleDBMigration(DBMigration migration) {
        return scheduleDBMigration(migration, null);
    }

    @Override
    public DBMigrationState scheduleDBMigration(DBMigration migration, DBMigrationCallback callback) {
        return executor.scheduleMigration(migration, callback);
    }

    @Override
    public boolean registerMBean(DBMigration migration) {
        return null != registerer && registerer.register(migration);
    }

    @Override
    public DBMigrationState scheduleDBRollback(DBMigration migration, int numberOfChangeSets) {
        return executor.scheduleRollback(migration, null, numberOfChangeSets);
    }

    @Override
    public DBMigrationState scheduleDBRollback(DBMigration migration, String changeSet) {
        return executor.scheduleRollback(migration, null, changeSet);
    }

    @Override
    public List<ChangeSet> listUnrunDBChangeSets(DBMigration migration) throws OXException {
        Connection connection = null;
        Liquibase liquibase = null;
        try {
            connection = migration.getConnectionProvider().get();
        	liquibase = LiquibaseHelper.prepareLiquibase(connection, migration);
        	return new ArrayList<ChangeSet>(liquibase.listUnrunChangeSets(LIQUIBASE_NO_DEFINED_CONTEXT));
        } catch (Exception exception) {
            if (exception instanceof OXException) {
                throw DBMigrationExceptionCodes.DB_MIGRATION_ERROR.create(exception);
            } else if (exception instanceof ValidationFailedException) {
                throw DBMigrationExceptionCodes.VALIDATION_FAILED_ERROR.create(exception);
            } else if (exception instanceof LiquibaseException) {
                throw DBMigrationExceptionCodes.LIQUIBASE_ERROR.create(exception);
            } else if (exception instanceof SQLException) {
                throw DBMigrationExceptionCodes.SQL_ERROR.create(exception);
            } else if (exception instanceof LockException) {
                throw DBMigrationExceptionCodes.READING_LOCK_ERROR.create(exception);
            } else {
                throw DBMigrationExceptionCodes.UNEXPECTED_ERROR.create(exception);
            }
        } finally {
        	LiquibaseHelper.cleanUpLiquibase(liquibase);
        	if (null != connection) {
        	    migration.getConnectionProvider().back(connection);
        	}
        }
    }

    @Override
    public boolean migrationsRunning() {
        return executor.isActive();
    }

    /**
     * Gets some textual information about the status of a database migration.
     *
     * @param migration The migration to get the status for
     * @return The database migration status
     */
    public String getDBStatus(DBMigration migration) throws OXException {
        Connection connection = null;
        Liquibase liquibase = null;
        try {
            connection = migration.getConnectionProvider().get();
            liquibase = LiquibaseHelper.prepareLiquibase(connection, migration);
            StringWriter sw = new StringWriter();
            liquibase.reportStatus(true, LIQUIBASE_NO_DEFINED_CONTEXT, sw);
            return sw.toString();
        } catch (LiquibaseException e) {
            throw DBMigrationExceptionCodes.LIQUIBASE_ERROR.create(e);
        } finally {
            LiquibaseHelper.cleanUpLiquibase(liquibase);
            if (null != connection) {
                migration.getConnectionProvider().back(connection);
            }
        }
    }

    /**
     * Gets some textual information about any resent locks for a database migration.
     *
     * @param migration The migration to get the locks for
     * @return The database migration locks
     */
    public String listDBLocks(DBMigration migration) throws OXException {
        Connection connection = null;
        Liquibase liquibase = null;
        try {
            connection = migration.getConnectionProvider().get();
            liquibase = LiquibaseHelper.prepareLiquibase(connection, migration);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(os);
            liquibase.reportLocks(ps);
            return os.toString("UTF8");
        } catch (LiquibaseException e) {
            throw DBMigrationExceptionCodes.LIQUIBASE_ERROR.create(e);
        } catch (UnsupportedEncodingException e) {
            throw DBMigrationExceptionCodes.UNEXPECTED_ERROR.create(e);
        } finally {
            LiquibaseHelper.cleanUpLiquibase(liquibase);
            if (null != connection) {
                migration.getConnectionProvider().back(connection);
            }
        }
    }

}
