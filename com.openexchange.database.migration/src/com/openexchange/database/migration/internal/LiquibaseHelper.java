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

import java.sql.Connection;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.migration.DBMigration;
import com.openexchange.database.migration.DBMigrationExceptionCodes;
import com.openexchange.database.migration.resource.accessor.BundleResourceAccessor;
import com.openexchange.exception.OXException;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.ResourceAccessor;

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
     * @param lifeThreadConnection An optional additional database connection used for the life thread
     * @param migration The database migration
     * @return The initialized liquibase instance
     */
    public static Liquibase prepareLiquibase(Connection connection, Connection lifeThreadConnection, DBMigration migration) throws LiquibaseException {
        return prepareLiquibase(connection, lifeThreadConnection, migration.getFileLocation(), migration.getAccessor());
    }

    /**
     * Prepares a new liquibase instance for the given file location. The instance is initialized with a writable non-timeout connection to
     * the underlying database.
     *
     * @param connection The database connection to use
     * @param lifeThreadConnection An optional additional database connection used for the life thread
     * @param fileLocation The file location
     * @param accessor Needed to access the given file
     * @return The initialized liquibase instance
     */
    public static Liquibase prepareLiquibase(Connection connection, Connection lifeThreadConnection, String fileLocation, ResourceAccessor accessor) throws LiquibaseException {
        LifeThreadConnectionAwareMysqlDatabase database = new LifeThreadConnectionAwareMysqlDatabase();
        database.setConnection(new JdbcConnection(connection));
        database.setLifeThreadConnection(lifeThreadConnection);
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
