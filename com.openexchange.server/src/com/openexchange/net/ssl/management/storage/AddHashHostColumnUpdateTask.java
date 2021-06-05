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

package com.openexchange.net.ssl.management.storage;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link AddHashHostColumnUpdateTask} - Adds (if not exists) the column <code>hash_host</code> in the table
 * <code>user_certificate</code>, calculates for every <code>host</code> entry the SHA-256, stores it in the new
 * column, drops the already existing PK and creates a new one with the <code>hash_host</code> column.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class AddHashHostColumnUpdateTask extends UpdateTaskAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(AddHashHostColumnUpdateTask.class);

    private static final String TABLE_NAME = "user_certificate";
    private static final String SELECT_HOSTS = "SELECT DISTINCT(host) AS h FROM " + TABLE_NAME;
    private static final String SET_HASH = "UPDATE " + TABLE_NAME + " SET host_hash = ? WHERE host = ?";

    /**
     * Initialises a new {@link AddHashHostColumnUpdateTask}.
     */
    public AddHashHostColumnUpdateTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            rollback = 1;

            Column hostHash = new Column("host_hash", "VARCHAR(64) CHARACTER SET latin1 COLLATE latin1_general_ci NULL");
            Tools.checkAndAddColumns(connection, TABLE_NAME, hostHash);

            int updated = setHostHashes(connection);
            LOG.info("Calculated hashes for already existing hosts, {} rows affected.", I(updated));

            Tools.dropPrimaryKey(connection, TABLE_NAME);
            Tools.createPrimaryKey(connection, TABLE_NAME, new String[] { "cid", "userid", "host_hash", "fingerprint" });

            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
        }
    }

    /**
     * Sets the hash for every host in the table
     *
     * @param connection The {@link Connection}
     * @throws SQLException if an SQL error is occurred
     */
    private int setHostHashes(Connection connection) throws SQLException {
        PreparedStatement selectHostStmt = null;
        ResultSet rs = null;
        int updated = 0;
        try {
            selectHostStmt = connection.prepareStatement(SELECT_HOSTS);
            rs = selectHostStmt.executeQuery();
            while (rs.next()) {
                updated += setHostHash(connection, rs.getString("h"));
            }
        } finally {
            Databases.closeSQLStuff(rs, selectHostStmt);
        }
        return updated;
    }

    /**
     * Sets the hash for the specified hostname in the table
     *
     * @param connection the {@link Connection}
     * @param hostname The hostname for which to calculate the SHA-256
     * @throws SQLException if an SQL error is occurred
     */
    private int setHostHash(Connection connection, String hostname) throws SQLException {
        PreparedStatement setHashStmt = null;
        try {
            setHashStmt = connection.prepareStatement(SET_HASH);
            setHashStmt.setString(1, DigestUtils.sha256Hex(hostname));
            setHashStmt.setString(2, hostname);
            return setHashStmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(setHashStmt);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { CreateSSLCertificateManagementTableTask.class.getName() };
    }
}
