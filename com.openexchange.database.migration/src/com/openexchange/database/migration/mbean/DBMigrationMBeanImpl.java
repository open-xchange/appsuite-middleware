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
import java.util.ArrayList;
import java.util.List;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import liquibase.changelog.ChangeSet;
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
    public boolean forceDBMigration(String fileName) throws MBeanException {

        if (fileName == null) {
            throw new MBeanException(new IllegalArgumentException("Param fileName might not be null!"));
        }

        try {
            dbMigrationExecutorService.execute(fileName);
        } catch (OXException e) {
            throw new MBeanException(e, e.getMessage());
        }

        return true;
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
    public List<String> listUnexecutedChangeSets(String fileName) throws MBeanException {
        if (fileName == null) {
            throw new MBeanException(new IllegalArgumentException("Param fileName might not be null!"));
        }
        List<String> unexecutedChangeSets = new ArrayList<String>();

        try {
            List<ChangeSet> listUnexecutedChangeSets = dbMigrationExecutorService.listUnexecutedChangeSets(fileName);

            for (ChangeSet changeSet : listUnexecutedChangeSets) {
                unexecutedChangeSets.add("Filename: " + fileName + "; ChangeSet: " + changeSet.toString(true));
            }

        } catch (OXException e) {
            throw new MBeanException(e, e.getMessage());
        }

        return unexecutedChangeSets;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean rollbackDBMigration(String fileName, String changeSetTag) throws MBeanException {
        if (fileName == null) {
            throw new MBeanException(new IllegalArgumentException("Param fileName might not be null!"));
        }

        try {
            dbMigrationExecutorService.rollback(fileName, changeSetTag);
        } catch (OXException e) {
            throw new MBeanException(e, e.getMessage());
        }

        return true;
    }
}
