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

import static com.openexchange.database.migration.internal.LiquibaseHelper.LIQUIBASE_NO_DEFINED_CONTEXT;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
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

/**
 * Implementation of {@link DBMigrationExecutorService} to execute database migration statements provided by the given file.
 * {@link ResourceAccessor}s are used to access to open the file for the classloader.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class DBMigrationExecutorServiceImpl implements DBMigrationExecutorService {

    private static final String CONFIGDB_CHANGE_LOG = "/resource/liquibase/configdbChangeLog.xml";

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
     * {@inheritDoc}
     */
    @Override
    public DBMigrationState scheduleMigration(String fileLocation, ResourceAccessor accessor) {
    	return executor.scheduleMigration(fileLocation, accessor);
    }

    /**
     * {@inheritDoc}
     *
     * @throws OXException
     */
    @Override
    public DBMigrationState scheduleRollback(String fileLocation, int numberOfChangeSets, ResourceAccessor accessor) {
        return executor.scheduleRollback(fileLocation, accessor, numberOfChangeSets);
    }

    /**
     * {@inheritDoc}
     *
     * @throws OXException
     */
    @Override
    public DBMigrationState scheduleRollback(String fileLocation, String changeSetTag, ResourceAccessor accessor) {
    	return executor.scheduleRollback(fileLocation, accessor, changeSetTag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChangeSet> listUnexecutedChangeSets(String fileLocation, ResourceAccessor accessor) throws OXException {
        Liquibase liquibase = null;
        try {
        	liquibase = LiquibaseHelper.prepareLiquibase(databaseService, fileLocation, accessor);        	
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
        	LiquibaseHelper.cleanUpLiquibase(databaseService, liquibase);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean migrationsRunning() {
        return !DBMigrationMonitor.getInstance().getScheduledFiles().isEmpty();
    }

    public void runCoreMigrations() throws InterruptedException, ExecutionException {
        ScheduledExecution scheduledExecution = executor.scheduleMigration(CONFIGDB_CHANGE_LOG, localResourceAccessor);
        scheduledExecution.awaitCompletion();
    }

    public void rollbackCoreMigrations(String changeSetTag) throws ExecutionException, InterruptedException {
        ScheduledExecution scheduledExecution = executor.scheduleRollback(CONFIGDB_CHANGE_LOG, localResourceAccessor, changeSetTag);
        scheduledExecution.awaitCompletion();
    }

    public List<ChangeSet> listUnexecutedCoreMigrations() throws OXException {
        return listUnexecutedChangeSets(CONFIGDB_CHANGE_LOG, localResourceAccessor);
    }
}
