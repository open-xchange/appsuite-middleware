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
