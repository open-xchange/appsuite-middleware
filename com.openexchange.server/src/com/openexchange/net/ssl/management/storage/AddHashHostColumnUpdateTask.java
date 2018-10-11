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

package com.openexchange.net.ssl.management.storage;

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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskV2#perform(com.openexchange.groupware.update.PerformParameters)
     */
    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        boolean rollback = false;
        try {
            connection.setAutoCommit(false);

            Column hostHash = new Column("host_hash", "VARCHAR(64) CHARACTER SET latin1 COLLATE latin1_general_ci NULL");
            Tools.checkAndAddColumns(connection, TABLE_NAME, hostHash);

            int updated = setHostHashes(connection);
            LOG.info("Calculated hashes for already existing hosts, {} rows affected.", updated);

            Tools.dropPrimaryKey(connection, TABLE_NAME);
            Tools.createPrimaryKey(connection, TABLE_NAME, new String[] { "cid", "userid", "host_hash", "fingerprint" });

            connection.commit();
            rollback = true;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(connection);
            }
            Databases.autocommit(connection);
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskV2#getDependencies()
     */
    @Override
    public String[] getDependencies() {
        return new String[] { CreateSSLCertificateManagementTableTask.class.getName() };
    }
}
