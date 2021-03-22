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

package com.openexchange.database.migration.internal;

import static com.openexchange.database.migration.internal.LiquibaseHelper.LIQUIBASE_NO_DEFINED_CONTEXT;
import static com.openexchange.java.Autoboxing.I;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import com.openexchange.database.migration.DBMigration;
import com.openexchange.database.migration.DBMigrationCallback;
import com.openexchange.database.migration.DBMigrationConnectionProvider;
import com.openexchange.database.migration.DBMigrationExceptionCodes;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.database.migration.DBMigrationState;
import com.openexchange.database.migration.rmi.DBMigrationRMIServiceImpl;
import com.openexchange.exception.OXException;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.exception.ValidationFailedException;

/**
 * Implementation of {@link DBMigrationExecutorService} to execute database migrations via liquibase.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>s
 * @since 7.6.1
 */
public class DBMigrationExecutorServiceImpl implements DBMigrationExecutorService {

    private final DBMigrationExecutor executor;
    private DBMigrationRMIServiceImpl rmiService;
    private final Map<Class<? extends Exception>, Function<Exception, OXException>> exceptionSpawners;

    /**
     * Initializes a new {@link DBMigrationExecutorServiceImpl}.
     */
    public DBMigrationExecutorServiceImpl() {
        super();
        executor = new DBMigrationExecutor();
        exceptionSpawners = initialiseExceptionSpawners();
    }

    /**
     * Initialises the {@link OXException} spawners
     *
     * @return An unmodifiable {@link Map} with {@link OXException} spawners
     */
    private Map<Class<? extends Exception>, Function<Exception, OXException>> initialiseExceptionSpawners() {
        Map<Class<? extends Exception>, Function<Exception, OXException>> m = new HashMap<>(8);
        m.put(OXException.class, (x) -> DBMigrationExceptionCodes.DB_MIGRATION_ERROR.create(x));
        m.put(SQLException.class, (x) -> DBMigrationExceptionCodes.SQL_ERROR.create(x));
        m.put(LockException.class, (x) -> DBMigrationExceptionCodes.READING_LOCK_ERROR.create(x));
        m.put(LiquibaseException.class, (x) -> DBMigrationExceptionCodes.LIQUIBASE_ERROR.create(x));
        m.put(ValidationFailedException.class, (x) -> DBMigrationExceptionCodes.VALIDATION_FAILED_ERROR.create(x));
        return Collections.unmodifiableMap(m);
    }

    /**
     * Sets the DBMigrationRMIRegisterer instance to use.
     *
     * @param registerer The {@link DBMigrationRMIRegisterer}
     */
    public void setRegisterer(DBMigrationRMIServiceImpl rmiService) {
        this.rmiService = rmiService;
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
    public boolean register(DBMigration migration) {
        return null != rmiService && rmiService.register(migration);
    }

    @Override
    public DBMigrationState scheduleDBRollback(DBMigration migration, int numberOfChangeSets) {
        return executor.scheduleRollback(migration, null, I(numberOfChangeSets));
    }

    @Override
    public DBMigrationState scheduleDBRollback(DBMigration migration, String changeSet) {
        return executor.scheduleRollback(migration, null, changeSet);
    }

    @Override
    public List<ChangeSet> listUnrunDBChangeSets(DBMigration migration) throws OXException {
        DBMigrationConnectionProvider connectionProvider = migration.getConnectionProvider();
        Connection connection = null;
        Liquibase liquibase = null;
        try {
            connection = connectionProvider.get();
            liquibase = LiquibaseHelper.prepareLiquibase(connection, null, migration);
            return new ArrayList<ChangeSet>(liquibase.listUnrunChangeSets(LIQUIBASE_NO_DEFINED_CONTEXT));
        } catch (Exception exception) {
            Function<Exception, OXException> x = exceptionSpawners.get(exception.getClass());
            if (x == null) {
                throw DBMigrationExceptionCodes.UNEXPECTED_ERROR.create(exception);
            }
            throw x.apply(exception);
        } finally {
            LiquibaseHelper.cleanUpLiquibase(liquibase);
            if (null != connection) {
                connectionProvider.back(connection);
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
        DBMigrationConnectionProvider connectionProvider = migration.getConnectionProvider();
        Connection connection = null;
        Liquibase liquibase = null;
        try {
            connection = connectionProvider.get();
            liquibase = LiquibaseHelper.prepareLiquibase(connection, null, migration);
            StringWriter sw = new StringWriter();
            liquibase.reportStatus(true, LIQUIBASE_NO_DEFINED_CONTEXT, sw);
            return sw.toString();
        } catch (LiquibaseException e) {
            throw DBMigrationExceptionCodes.LIQUIBASE_ERROR.create(e);
        } finally {
            LiquibaseHelper.cleanUpLiquibase(liquibase);
            if (null != connection) {
                connectionProvider.back(connection);
            }
        }
    }

    /**
     * Gets some textual information about any recent locks for a database migration.
     *
     * @param migration The migration to get the locks for
     * @return The database migration locks
     */
    public String listDBLocks(DBMigration migration) throws OXException {
        DBMigrationConnectionProvider connectionProvider = migration.getConnectionProvider();
        Connection connection = null;
        Liquibase liquibase = null;
        try {
            connection = connectionProvider.get();
            liquibase = LiquibaseHelper.prepareLiquibase(connection, null, migration);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(os, false, "UTF8");
            liquibase.reportLocks(ps);
            return os.toString("UTF8");
        } catch (LiquibaseException e) {
            throw DBMigrationExceptionCodes.LIQUIBASE_ERROR.create(e);
        } catch (UnsupportedEncodingException e) {
            throw DBMigrationExceptionCodes.UNEXPECTED_ERROR.create(e);
        } finally {
            LiquibaseHelper.cleanUpLiquibase(liquibase);
            if (null != connection) {
                connectionProvider.back(connection);
            }
        }
    }

}
