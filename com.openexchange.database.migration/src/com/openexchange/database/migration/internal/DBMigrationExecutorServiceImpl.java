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
import java.util.concurrent.CopyOnWriteArrayList;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.lang.Validate;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.migration.DBMigrationExceptionCodes;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.exception.OXException;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.sql.DBUtils;

/**
 * Implementation of {@link DBMigrationExecutorService} to execute database migration statements provided by the given file.
 * {@link ResourceAccessor}s are used to access to open the file for the classloader.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class DBMigrationExecutorServiceImpl implements DBMigrationExecutorService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DBMigrationExecutorServiceImpl.class);

    private static final String LIQUIBASE_NO_DEFINED_CONTEXT = "";

    private DatabaseService databaseService;

    private List<ResourceAccessor> accessors = new CopyOnWriteArrayList<ResourceAccessor>();

    private ThreadPoolService threadPoolService;

    /**
     * Initializes a new {@link DBMigrationExecutorServiceImpl}.
     *
     * @param databaseService - {@link DatabaseService} to be able to execute migration files
     * @param threadPoolService - {@link ThreadPoolService} to add new threads for processing
     */
    public DBMigrationExecutorServiceImpl(DatabaseService databaseService, ThreadPoolService threadPoolService) {
        super();

        Validate.notNull(databaseService, "DatabaseService mustn't be null!");
        Validate.notNull(threadPoolService, "ThreadPoolService mustn't be null!");

        this.databaseService = databaseService;
        this.threadPoolService = threadPoolService;
    }

    /**
     * Handles exceptions occurred while preparing and using liquibase.
     *
     * @param exception
     * @throws OXException
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

        return new Liquibase(filePath, new CompositeResourceAccessor(accessors), databaseConnection);
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
        }
        if (writable != null) {
            DBUtils.autocommit(writable);
            databaseService.backWritable(writable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(String fileLocation, List<ResourceAccessor> additionalAccessors) throws OXException {
        if (additionalAccessors == null) {
            execute(fileLocation);
            return;
        }

        for (ResourceAccessor accessor : additionalAccessors) {
            accessors.add(accessor);
        }
        execute(fileLocation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final String fileLocation) throws OXException {
        LOG.info("Start asynchronously executing database migration for ChangeLog {}", fileLocation);

        threadPoolService.submit(new AbstractTask<Void>() {

            @Override
            public Void call() throws OXException {
                Connection writable = null;
                Liquibase liquibase = null;
                try {
                    writable = databaseService.getWritable();

                    liquibase = prepareLiquibase(writable, fileLocation);

                    liquibase.update(LIQUIBASE_NO_DEFINED_CONTEXT);
                } catch (Exception exception) {
                    handleExceptions(exception);
                } finally {
                    cleanUpLiquibase(writable, liquibase);
                }
                LOG.info("Finished executing database migration for ChangeLog {}", fileLocation);
                return null;
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @throws OXException
     */
    @Override
    public void rollback(String fileLocation, int numberOfChangeSets) throws OXException {
        rollbackChangeSets(fileLocation, numberOfChangeSets);
    }

    /**
     * {@inheritDoc}
     *
     * @throws OXException
     */
    @Override
    public void rollback(String fileLocation, String changeSetTag) throws OXException {
        rollbackChangeSets(fileLocation, changeSetTag);
    }

    /**
     * General method for a rollback. Provide an Integer as <code>target</code> param for number of changesets to rollback or provide a
     * String as <code>target</code> param for a tag name to rollback.
     *
     * @param fileLocation
     * @param target
     * @return
     * @throws OXException
     */
    private void rollbackChangeSets(String fileLocation, Object target) throws OXException {
        LOG.info("Start rollback database migrations for file {}", fileLocation);

        Connection writable = null;
        Liquibase liquibase = null;
        try {
            writable = databaseService.getWritable();

            liquibase = prepareLiquibase(writable, fileLocation);

            if (target instanceof Integer) {
                int numberOfChangeSetsToRollback = (Integer) target;
                LOG.info("Rollback {} numbers of changesets", numberOfChangeSetsToRollback);
                liquibase.rollback(numberOfChangeSetsToRollback, LIQUIBASE_NO_DEFINED_CONTEXT);
            } else if (target instanceof String) {
                String changeSetTag = (String) target;
                LOG.info("Rollback to changeset {}", changeSetTag);
                liquibase.rollback(changeSetTag, LIQUIBASE_NO_DEFINED_CONTEXT);
            } else {
                throw DBMigrationExceptionCodes.WRONG_TYPE_OF_DATA_ROLLBACK_ERROR.create();
            }
        } catch (Exception exception) {
            handleExceptions(exception);
        } finally {
            cleanUpLiquibase(writable, liquibase);
        }
        LOG.info("Finished rollback of database migrations for file {}", fileLocation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeSet> listUnexecutedChangeSets(String fileLocation) throws OXException {
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
}
