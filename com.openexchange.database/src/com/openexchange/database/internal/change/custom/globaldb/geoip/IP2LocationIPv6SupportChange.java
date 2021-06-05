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

package com.openexchange.database.internal.change.custom.globaldb.geoip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.Databases;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * {@link IP2LocationIPv6SupportChange} - Changes the table 'ip2location' for IPv6 support.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public class IP2LocationIPv6SupportChange implements CustomTaskChange {

    private static final Logger LOGGER = LoggerFactory.getLogger(IP2LocationIPv6SupportChange.class);

    private static final String ALTER = "ALTER TABLE `ip2location` MODIFY `ip_from` DECIMAL(39,0) UNSIGNED NULL DEFAULT NULL, MODIFY `ip_to` DECIMAL(39,0) UNSIGNED NOT NULL;";

    /**
     * Initialises a new {@link IP2LocationIPv6SupportChange}.
     */
    public IP2LocationIPv6SupportChange() {
        super();
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        DatabaseConnection databaseConnection = database.getConnection();
        if (!(databaseConnection instanceof JdbcConnection)) {
            throw new CustomChangeException("Cannot get underlying connection because database connection is not of type " + JdbcConnection.class.getName() + ", but of type: " + databaseConnection.getClass().getName());
        }

        int rollback = 0;
        Connection connection = ((JdbcConnection) databaseConnection).getUnderlyingConnection();
        try (PreparedStatement stmt = connection.prepareStatement(ALTER)) {
            Databases.startTransaction(connection);
            rollback = 1;
            stmt.executeUpdate();
            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            LOGGER.error("Failed to change the ip_from and ip_to columns to BIGDECIMAL", e);
            throw new CustomChangeException("SQL error", e);
        } catch (RuntimeException e) {
            LOGGER.error("Failed to change the ip_from and ip_to columns to BIGDECIMAL", e);
            throw new CustomChangeException("Runtime error", e);
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "ip_from and ip_to columns successfully converted to BIGDECIMALs.";
    }

    @Override
    public void setUp() throws SetupException {
        // no-op
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        // no-op
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
}
