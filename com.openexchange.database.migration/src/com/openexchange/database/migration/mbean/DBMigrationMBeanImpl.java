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

package com.openexchange.database.migration.mbean;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.migration.DBMigrationExecutorService;
import com.openexchange.exception.OXException;

/**
 * Implementation of {@link DBMigrationMBean} to manage everything around database migration based on liquibase.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class DBMigrationMBeanImpl extends StandardMBean implements DBMigrationMBean {

    private final DBMigrationExecutorService dbMigrationExecutorService;

    private final DatabaseService databaseService;

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DBMigrationMBeanImpl.class);

    /**
     * Initializes a new {@link DBMigrationMBeanImpl}.
     *
     * @param mbeanInterface
     * @throws NotCompliantMBeanException
     */
    public DBMigrationMBeanImpl(Class<? extends DBMigrationMBean> mbeanInterface, DBMigrationExecutorService dbMigrationExecutorService, DatabaseService databaseService) throws NotCompliantMBeanException {
        super(mbeanInterface);
        Validate.notNull(dbMigrationExecutorService, "DBMigrationExecuterService must not be null!");
        Validate.notNull(databaseService, "DatabaseService must not be null!");

        this.dbMigrationExecutorService = dbMigrationExecutorService;
        this.databaseService = databaseService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void forceDBMigration() throws MBeanException {
        try {
            dbMigrationExecutorService.execute("ox.changelog.xml");
        } catch (OXException e) {
            throw new MBeanException(e, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean releaseDBMigrationLock() throws MBeanException {
        boolean lockReleased = false;

        Connection writable = null;
        PreparedStatement stmt = null;
        try {
            writable = databaseService.getWritable();
            stmt = writable.prepareStatement("UPDATE DATABASECHANGELOGLOCK SET LOCKED=0, LOCKGRANTED=null, LOCKEDBY=null where ID=1;");
            stmt.execute();
            lockReleased = true;
        } catch (final Exception e) {
            LOG.error("Not able to release the lock for table DATABASECHANGELOGLOCK", e);
            final String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        } finally {
            closeSQLStuff(stmt);
            if (writable != null) {
                databaseService.backWritable(writable);
            }
        }
        return lockReleased;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void listDBMigrationStatus() throws MBeanException {

        // TODO
        // Connection writable = null;
        // Liquibase liquibase = null;
        // JdbcConnection jdbcConnection = null;
        // try {
        // writable = databaseService.getWritable();
        //
        // jdbcConnection = new JdbcConnection(writable);
        // jdbcConnection.setAutoCommit(true);
        //
        // MySQLDatabase databaseConnection = new MySQLDatabase();
        // databaseConnection.setConnection(jdbcConnection);
        // databaseConnection.setAutoCommit(true);
        //
        // List<ResourceAccessor> accessors = new CopyOnWriteArrayList<ResourceAccessor>();
        // accessors.add(new ClassLoaderResourceAccessor());
        // accessors.add(new FileSystemResourceAccessor());
        //
        // liquibase = new Liquibase("ox.changelog.xml", null, databaseConnection);
        // DatabaseChangeLogLock[] listLocks = liquibase.listLocks();
        // List<ChangeSet> listUnrunChangeSets = liquibase.listUnrunChangeSets("configdb");
        // Collection<RanChangeSet> listUnexpectedChangeSets = liquibase.listUnexpectedChangeSets("configdb");
        // System.out.println(listLocks.length + listUnrunChangeSets.size() + listUnexpectedChangeSets.size());
        // } catch (ValidationFailedException validationFailedException) {
        // LOG.error("Validation of DatabaseChangeLog failed with the following exception: " +
        // validationFailedException.getLocalizedMessage(), validationFailedException);
        // } catch (LiquibaseException liquibaseException) {
        // LOG.error("Error using/executing liquibase: " + liquibaseException.getLocalizedMessage(), liquibaseException);
        // } catch (OXException oxException) {
        // LOG.error("Unable to retrieve database write connection: " + oxException.getLocalizedMessage(), oxException);
        // } catch (Exception exception) {
        // LOG.error("An unexpected error occurred while executing database migration: " + exception.getLocalizedMessage(), exception);
        // } finally {
        // if (liquibase != null) {
        // try {
        // liquibase.forceReleaseLocks();
        // } catch (LiquibaseException liquibaseException) {
        // LOG.error("Unable to release liquibase locks: " + liquibaseException.getLocalizedMessage(), liquibaseException);
        // }
        // }
        // if (writable != null) {
        // databaseService.backWritable(writable);
        // }
        // }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rollbackDBMigration(String fileName, String changeSetTag) throws MBeanException {
        try {
            return dbMigrationExecutorService.rollback(fileName, changeSetTag);
        } catch (OXException e) {
            throw new MBeanException(e, e.getMessage());
        }
    }
}
