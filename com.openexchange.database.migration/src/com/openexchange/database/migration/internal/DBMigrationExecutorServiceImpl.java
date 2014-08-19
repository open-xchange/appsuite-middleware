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
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.exception.OXException;

/**
 * Implementation of {@link DBMigrationExecutorService} to execute database migration statements provided by the given file.
 * {@link ResourceAccessor}s are used to access to open the file for the classloader.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class DBMigrationExecutorServiceImpl implements DBMigrationExecutorService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DBMigrationExecutorServiceImpl.class);

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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(DatabaseChangeLog databaseChangeLog) {
        Connection writable = null;
        Liquibase liquibase = null;
        JdbcConnection jdbcConnection = null;
        try {
            writable = databaseService.getWritable();

            jdbcConnection = new JdbcConnection(writable);
            jdbcConnection.setAutoCommit(true);

            MySQLDatabase databaseConnection = new MySQLDatabase();
            databaseConnection.setConnection(jdbcConnection);
            databaseConnection.setAutoCommit(true);

            liquibase = new Liquibase(databaseChangeLog.getPhysicalFilePath(), new CompositeResourceAccessor(accessors), databaseConnection);
            liquibase.update("");
        } catch (ValidationFailedException validationFailedException) {
            LOG.error("Validation of DatabaseChangeLog failed with the following exception: " + validationFailedException.getLocalizedMessage(), validationFailedException);
        } catch (LiquibaseException liquibaseException) {
            LOG.error("Error using/executing liquibase: " + liquibaseException.getLocalizedMessage(), liquibaseException);
        } catch (OXException oxException) {
            LOG.error("Unable to retrieve database write connection: " + oxException.getLocalizedMessage(), oxException);
        } catch (Exception exception) {
            LOG.error("An unexpected error occurred while executing database migration: " + exception.getLocalizedMessage(), exception);
        } finally {
            if (liquibase != null) {
                try {
                    liquibase.forceReleaseLocks();
                } catch (LiquibaseException liquibaseException) {
                    LOG.error("Unable to release liquibase locks: " + liquibaseException.getLocalizedMessage(), liquibaseException);
                }
            }
            if (writable != null) {
                databaseService.backWritable(writable);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(String fileName) {
        File xmlConfigFile = configurationService.getFileByName(fileName);
        if (null != xmlConfigFile) {
            execute(new DatabaseChangeLog(xmlConfigFile.getAbsolutePath()));
        } else {
            LOG.info("No database migration file with name " + fileName + " found! Execution for that file will be skipped.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(String fileName, List<ResourceAccessor> additionalAccessors) {
        if (additionalAccessors == null) {
            execute(fileName);
            return;
        }

        for (ResourceAccessor accessor : additionalAccessors) {
            accessors.add(accessor);
        }
        execute(fileName);
    }
}
