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

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import liquibase.Liquibase;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.lang.Validate;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.migration.DBMigrationExceptionCodes;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.database.migration.internal.accessors.SimpleClassLoaderResourceAccessor;
import com.openexchange.exception.OXException;

/**
 * Implementation of {@link DBMigrationExecutorService} to execute database migration statements provided by the given file.
 * {@link ResourceAccessor}s are used to access to open the file for the classloader.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class DBMigrationExecutorServiceImpl implements DBMigrationExecutorService {

    private static final String LIQUIBASE_CONTEXT_CONFIGDB = "configdb";

    private DatabaseService databaseService;

    private ConfigurationService configurationService;

    private List<ResourceAccessor> accessors = new CopyOnWriteArrayList<ResourceAccessor>();

    /**
     * Initializes a new {@link DBMigrationExecutorServiceImpl}.
     *
     * @param databaseService - {@link DatabaseService} to be able to execute migration files
     * @param configurationService - {@link ConfigurationService} to be able to retrieve the configuration files
     */
    public DBMigrationExecutorServiceImpl(DatabaseService databaseService, ConfigurationService configurationService) {
        super();

        Validate.notNull(databaseService, "DatabaseService mustn't be null!");
        Validate.notNull(configurationService, "ConfigurationService mustn't be null!");
        this.databaseService = databaseService;
        this.configurationService = configurationService;

        accessors.add(new ClassLoaderResourceAccessor());
        accessors.add(new FileSystemResourceAccessor());
        accessors.add(new SimpleClassLoaderResourceAccessor());
    }

    /**
     * {@inheritDoc}
     *
     * @throws OXException
     */
    @Override
    public void execute(DatabaseChangeLog databaseChangeLog) throws OXException {

        Connection writable = null;
        Liquibase liquibase = null;
        try {
            writable = databaseService.getWritable();

            liquibase = prepareLiquibase(writable, databaseChangeLog.getPhysicalFilePath());

            liquibase.update(LIQUIBASE_CONTEXT_CONFIGDB);
        } catch (Exception exception) {
            handleExceptions(exception);
        } finally {
            cleanUpLiquibase(writable, liquibase);
        }
    }

    /**
     * Handles exceptions occurred while preparing and using liquibase.
     *
     * @param exception
     * @throws OXException
     */
    private void handleExceptions(Exception exception) throws OXException {
        if (exception instanceof ValidationFailedException) {
            throw DBMigrationExceptionCodes.VALIDATION_FAILED_ERROR.create(exception);
        } else if (exception instanceof LiquibaseException) {
            throw DBMigrationExceptionCodes.LIQUIBASE_ERROR.create(exception);
        } else if (exception instanceof OXException) {
            throw DBMigrationExceptionCodes.DBMIGARTION_ERROR.create(exception);
        } else {
            throw DBMigrationExceptionCodes.UNEXPECTED_ERROR.create(exception);
        }
    }

    /**
     * Prepares liquibase to be able to execute statements
     *
     * @param writable
     * @param databaseChangeLog
     * @return
     * @throws LiquibaseException
     */
    private Liquibase prepareLiquibase(Connection writable, String filePath) throws LiquibaseException {
        JdbcConnection jdbcConnection = null;

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
     */
    private void cleanUpLiquibase(Connection writable, Liquibase liquibase) throws OXException {
        if (liquibase != null) {
            try {
                liquibase.forceReleaseLocks();
            } catch (LiquibaseException liquibaseException) {
                throw DBMigrationExceptionCodes.LIQUIBASE_ERROR.create(liquibaseException);
            }
        }
        if (writable != null) {
            databaseService.backWritable(writable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(String fileName) throws OXException {
        File xmlConfigFile = getChangeLogFile(fileName);

        execute(new DatabaseChangeLog(xmlConfigFile.getAbsolutePath()));
    }

    /**
     * @param fileName
     * @return
     * @throws OXException
     */
    private File getChangeLogFile(String fileName) throws OXException {
        File xmlConfigFile = configurationService.getFileByName(fileName);
        if (null == xmlConfigFile) {
            throw DBMigrationExceptionCodes.CHANGELOG_FILE_NOT_FOUND_ERROR.create(new FileNotFoundException(fileName), fileName);
        }
        return xmlConfigFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(String fileName, List<ResourceAccessor> additionalAccessors) throws OXException {
        if (additionalAccessors == null) {
            execute(fileName);
            return;
        }

        for (ResourceAccessor accessor : additionalAccessors) {
            accessors.add(accessor);
        }
        execute(fileName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback(int numberOfChangeSets) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     *
     * @throws OXException
     */
    @Override
    public boolean rollback(String fileName, String changeSetTag) throws OXException {
        boolean rollbackSuccessful = false;

        File xmlConfigFile = getChangeLogFile(fileName);

        Connection writable = null;
        Liquibase liquibase = null;
        try {
            writable = databaseService.getWritable();

            liquibase = prepareLiquibase(writable, xmlConfigFile.getAbsolutePath());
            liquibase.rollback(changeSetTag, LIQUIBASE_CONTEXT_CONFIGDB);

            rollbackSuccessful = true;
        } catch (Exception exception) {
            handleExceptions(exception);
        } finally {
            cleanUpLiquibase(writable, liquibase);
        }

        return rollbackSuccessful;
    }
}
