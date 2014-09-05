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
package com.openexchange.database.migration.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.exception.ValidationFailedException;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.lang.Validate;
import org.osgi.framework.FrameworkUtil;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.migration.DBMigrationExceptionCodes;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.database.migration.DBMigrationState;
import com.openexchange.database.migration.resource.accessor.BundleResourceAccessor;
import com.openexchange.exception.OXException;
import com.openexchange.tools.sql.DBUtils;

/**
 * Implementation of {@link DBMigrationExecutorService} to execute database migration statements provided by the given file.
 * {@link ResourceAccessor}s are used to access to open the file for the classloader.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class DBMigrationExecutorServiceImpl implements DBMigrationExecutorService {


    private static final String CONFIGDB_CHANGE_LOG = "/liquibase/configdbChangeLog.xml";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DBMigrationExecutorServiceImpl.class);

    static final String LIQUIBASE_NO_DEFINED_CONTEXT = "";

    private final DBMigrationExecutor executor;

    private DatabaseService databaseService;

    private final BundleResourceAccessor localResourceAccessor = new BundleResourceAccessor(FrameworkUtil.getBundle(getClass()));


    /**
     * Initializes a new {@link DBMigrationExecutorServiceImpl}.
     *
     * @param databaseService - {@link DatabaseService} to be able to execute migration files
     */
    public DBMigrationExecutorServiceImpl(DatabaseService databaseService) {
        super();

        Validate.notNull(databaseService, "DatabaseService mustn't be null!");

        this.databaseService = databaseService;
        executor = new DBMigrationExecutor(databaseService);
    }

    /**
     * Handles exceptions occurred while preparing and using liquibase.
     *
     * @param exception - the exception to handle
     * @throws OXException - the OXException related to the given one.
     */
    protected void handleExceptions(Exception exception) throws OXException {
        if (exception instanceof OXException) {
            LOG.error(DBMigrationExceptionCodes.DB_MIGRATION_ERROR_MSG, exception);
            throw DBMigrationExceptionCodes.DB_MIGRATION_ERROR.create(exception);
        } else if (exception instanceof ValidationFailedException) {
            LOG.error(DBMigrationExceptionCodes.VALIDATION_FAILED_ERROR_MSG, exception);
            throw DBMigrationExceptionCodes.VALIDATION_FAILED_ERROR.create(exception);
        } else if (exception instanceof LiquibaseException) {
            LOG.error(DBMigrationExceptionCodes.LIQUIBASE_ERROR_MSG, exception);
            throw DBMigrationExceptionCodes.LIQUIBASE_ERROR.create(exception);
        } else if (exception instanceof SQLException) {
            LOG.error(DBMigrationExceptionCodes.SQL_ERROR_MSG, exception);
            throw DBMigrationExceptionCodes.SQL_ERROR.create(exception);
        } else if (exception instanceof LockException) {
            LOG.error(DBMigrationExceptionCodes.READING_LOCK_ERROR_MSG, exception);
            throw DBMigrationExceptionCodes.READING_LOCK_ERROR.create(exception);
        } else {
            LOG.error(DBMigrationExceptionCodes.UNEXPECTED_ERROR_MSG, exception);
            throw DBMigrationExceptionCodes.UNEXPECTED_ERROR.create(exception);
        }
    }

    /**
     * Prepares liquibase to be able to execute statements
     *
     * @param writable
     * @param filePath
     * @return
     * @throws LiquibaseException
     * @throws SQLException
     */
    protected Liquibase prepareLiquibase(Connection writable, String filePath) throws LiquibaseException, SQLException {
        JdbcConnection jdbcConnection = null;

        writable.setAutoCommit(true);
        jdbcConnection = new JdbcConnection(writable);
        jdbcConnection.setAutoCommit(true);

        MySQLDatabase databaseConnection = new MySQLDatabase();
        databaseConnection.setConnection(jdbcConnection);
        databaseConnection.setAutoCommit(true);

        return new Liquibase(filePath, localResourceAccessor, databaseConnection);
    }

    /**
     * Cleans up everything used for executing liquibase changelogs
     *
     * @param writable
     * @param liquibase
     * @throws OXException
     * @throws SQLException
     */
    protected void cleanUpLiquibase(Connection writable, Liquibase liquibase) throws OXException {
        if (liquibase != null) {
            try {
                liquibase.forceReleaseLocks();
            } catch (LiquibaseException liquibaseException) {
                throw DBMigrationExceptionCodes.LIQUIBASE_ERROR.create(liquibaseException);
            }
            finally {
                if (writable != null) {
                    DBUtils.autocommit(writable);
                    databaseService.backWritable(writable);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DBMigrationState execute(String fileLocation, ResourceAccessor accessor) {
        ScheduledExecution scheduledExecution = new ScheduledExecution(fileLocation, accessor);
        executor.schedule(scheduledExecution);
        return scheduledExecution;
    }

    /**
     * {@inheritDoc}
     *
     * @throws OXException
     */
    @Override
    public DBMigrationState rollback(String fileLocation, int numberOfChangeSets, ResourceAccessor accessor) {
        return rollbackChangeSets(fileLocation, numberOfChangeSets, accessor);
    }

    /**
     * {@inheritDoc}
     *
     * @throws OXException
     */
    @Override
    public DBMigrationState rollback(String fileLocation, String changeSetTag, ResourceAccessor accessor) {
        return rollbackChangeSets(fileLocation, changeSetTag, accessor);
    }

    /**
     * General method for a rollback. Provide an Integer as <code>target</code> param for number of changesets to rollback or provide a
     * String as <code>target</code> param for a tag name to rollback.
     *
     * @param fileLocation
     * @param target
     * @return DBMigrationState
     * @throws OXException
     */
    private DBMigrationState rollbackChangeSets(String fileLocation, Object target, ResourceAccessor accessor) {
        ScheduledExecution scheduledExecution = new ScheduledExecution(fileLocation, accessor, target);
        executor.schedule(scheduledExecution);
        return scheduledExecution;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeSet> listUnexecutedChangeSets(String fileLocation, ResourceAccessor accessor) throws OXException {
        List<ChangeSet> unexecutedChangeSets = new ArrayList<ChangeSet>();

        Connection writable = null;
        Liquibase liquibase = null;
        try {
            writable = databaseService.getWritable();

            liquibase = prepareLiquibase(writable, fileLocation);

            unexecutedChangeSets = liquibase.listUnrunChangeSets(LIQUIBASE_NO_DEFINED_CONTEXT);
        } catch (Exception exception) {
            handleExceptions(exception);
        } finally {
            cleanUpLiquibase(writable, liquibase);
        }
        return unexecutedChangeSets;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean migrationsRunning() {
        return !DBMigrationMonitor.getInstance().getScheduledFiles().isEmpty();
    }

    public void runCoreMigrations() throws InterruptedException, ExecutionException {
        ScheduledExecution scheduledExecution = new ScheduledExecution(
            CONFIGDB_CHANGE_LOG,
            localResourceAccessor);
        executor.schedule(scheduledExecution);
        scheduledExecution.await();
    }

    public void rollbackCoreMigrations(String changeSetTag) throws ExecutionException, InterruptedException {
        ScheduledExecution scheduledExecution = new ScheduledExecution(
            CONFIGDB_CHANGE_LOG,
            localResourceAccessor,
            changeSetTag);
        executor.schedule(scheduledExecution);
        scheduledExecution.await();
    }

    public List<ChangeSet> listUnexecutedCoreMigrations() throws OXException {
        return listUnexecutedChangeSets(CONFIGDB_CHANGE_LOG, localResourceAccessor);
    }
}
