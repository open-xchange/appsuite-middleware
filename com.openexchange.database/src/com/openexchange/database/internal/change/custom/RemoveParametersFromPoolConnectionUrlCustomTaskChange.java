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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.database.internal.change.custom;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
 * {@link RemoveParametersFromPoolConnectionUrlCustomTaskChange}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.1
 */
public class RemoveParametersFromPoolConnectionUrlCustomTaskChange implements CustomTaskChange {

    private static final Logger LOG = LoggerFactory.getLogger(RemoveParametersFromPoolConnectionUrlCustomTaskChange.class);

    @Override
    public String getConfirmationMessage() {
        return "Successfully removed parameters from all Connection URLs in the db_pool table.";
    }

    @Override
    public void setUp() throws SetupException {
        // nothing
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        // nothing
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        DatabaseConnection databaseConnection = database.getConnection();
        if (!(databaseConnection instanceof JdbcConnection)) {
            throw new CustomChangeException("Cannot get underlying connection because database connection is not of type " + JdbcConnection.class.getName() + ", but of type: " + databaseConnection.getClass().getName());
        }
        Connection configDbCon = ((JdbcConnection) databaseConnection).getUnderlyingConnection();
        boolean rollback = false;
        try {
            Databases.startTransaction(configDbCon);
            rollback = true;

            execute(configDbCon);

            configDbCon.commit();
            rollback = false;
        } catch (SQLException e) {
            LOG.error("Failed to initialize count tables for ConfigDB", e);
            throw new CustomChangeException("SQL error", e);
        } catch (RuntimeException e) {
            LOG.error("Failed to initialize count tables for ConfigDB", e);
            throw new CustomChangeException("Runtime error", e);
        } finally {
            if (rollback) {
                Databases.rollback(configDbCon);
            }
            Databases.autocommit(configDbCon);
        }
    }

    private static final String SELECT = "SELECT db_pool_id, url FROM db_pool";

    private static final String UPDATE = "UPDATE db_pool SET url = ? WHERE db_pool_id = ?";

    private void execute(Connection con) throws SQLException {
        Map<Integer, String> id2Url = new HashMap<>();
        Map<Integer, String> id2NewUrl = new HashMap<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SELECT);
            rs = stmt.executeQuery();
            while (rs.next()) {
                id2Url.put(I(rs.getInt("db_pool_id")), rs.getString("url"));
            }
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            stmt = null;
            rs = null;
        }

        for (Entry<Integer, String> entry : id2Url.entrySet()) {
            String url = id2Url.get(entry.getKey());
            int paramStart = url.indexOf('?');
            if (paramStart != -1) {
                id2NewUrl.put(entry.getKey(), url.substring(0, paramStart));
            }
        }

        if (!id2NewUrl.isEmpty()) {
            try {
                stmt = con.prepareStatement(UPDATE);
                for (Entry<Integer, String> entry : id2Url.entrySet()) {
                    stmt.setString(1, id2NewUrl.get(entry.getKey()));
                    stmt.setInt(2, entry.getKey().intValue());
                    stmt.addBatch();
                    
                    LOG.info("Changed url for db_pool_id {} from '{}' to '{}'", entry.getKey(), id2Url.get(entry.getKey()), id2NewUrl.get(entry.getKey()));
                }
                stmt.executeBatch();
            } finally {
                Databases.closeSQLStuff(stmt);
            }
        }
    }

}
