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

import java.sql.Connection;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.migration.DBMigration;
import com.openexchange.database.migration.DBMigrationExceptionCodes;
import com.openexchange.database.migration.resource.accessor.BundleResourceAccessor;
import com.openexchange.exception.OXException;

/**
 * {@link LiquibaseHelper}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class LiquibaseHelper {

    private static final Logger LOG = LoggerFactory.getLogger(LiquibaseHelper.class);

    public static final String LIQUIBASE_NO_DEFINED_CONTEXT = "";

    /**
     * Prepares a new liquibase instance for the given migration. The instance is initialized with a writable non-timeout connection to
     * the underlying database.
     *
     * @param connection The database connection to use
     * @param migration The database migration
     * @return The initialized liquibase instance
     */
    public static Liquibase prepareLiquibase(Connection connection, DBMigration migration) throws LiquibaseException, OXException {
        return prepareLiquibase(connection, migration.getFileLocation(), migration.getAccessor());
    }

    /**
     * Prepares a new liquibase instance for the given file location. The instance is initialized with a writable non-timeout connection to
     * the underlying database.
     *
     * @param connection The database connection to use
     * @param fileLocation The file location
     * @param accessor Needed to access the given file
     * @return The initialized liquibase instance
     */
    public static Liquibase prepareLiquibase(Connection connection, String fileLocation, ResourceAccessor accessor) throws LiquibaseException, OXException {
        MySQLDatabase database = new MySQLDatabase();
        database.setConnection(new JdbcConnection(connection));
        return new Liquibase(fileLocation, LiquibaseHelper.prepareResourceAccessor(accessor), database);
    }

    /**
     * All liquibase locks are released and the underlying connection is closed.
     *
     * @param liquibase The liquibase instance. If <code>null</code>, calling this method has no effect.
     * @return The underlying database connection, or <code>null</code> if not available
     * @throws OXException If an error occurs while releasing the locks
     */
    public static Connection cleanUpLiquibase(Liquibase liquibase) throws OXException {
        return cleanUpLiquibase(liquibase, true);
    }

    /**
     * Closes the underlying database connection and optionally releases any present liquibase locks.
     *
     * @param liquibase The liquibase instance. If <code>null</code>, calling this method has no effect.
     * @param forciblyReleaseLocks <code>true</code> to forcibly release the liquibase locks, <code>false</code>, otherwise
     * @return The underlying database connection, or <code>null</code> if not available
     * @throws OXException If an error occurs while releasing the locks
     */
    public static Connection cleanUpLiquibase(Liquibase liquibase, boolean forciblyReleaseLocks) throws OXException {
        if (liquibase != null) {
            try {
                if (forciblyReleaseLocks) {
                    liquibase.forceReleaseLocks();
                }
            } catch (LiquibaseException liquibaseException) {
                throw DBMigrationExceptionCodes.LIQUIBASE_ERROR.create(liquibaseException);
            } finally {
                Database database = liquibase.getDatabase();
                if (database != null) {
                    DatabaseConnection connectionWrapper = database.getConnection();
                    if (connectionWrapper != null) {
                        try {
                            return  ((JdbcConnection) connectionWrapper).getUnderlyingConnection();
                        } catch (ClassCastException e) {
                            LOG.warn("An unexpected connection instance was passed, it will be closed manually.", e);
                            try {
                                connectionWrapper.close();
                            } catch (DatabaseException d) {
                                LOG.error("Could not close unknown connection instance!", d);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Prepares a {@link CompositeResourceAccessor} containing the given {@link ResourceAccessor} and one for this bundle.
     *
     * @param provided The {@link ResourceAccessor} provided by service users
     * @return A {@link CompositeResourceAccessor}
     */
    public static ResourceAccessor prepareResourceAccessor(ResourceAccessor provided) {
        return new CompositeResourceAccessor(provided, new BundleResourceAccessor(FrameworkUtil.getBundle(LiquibaseHelper.class)));
    }

}
