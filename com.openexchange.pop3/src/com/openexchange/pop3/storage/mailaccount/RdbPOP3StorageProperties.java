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

package com.openexchange.pop3.storage.mailaccount;

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.POP3ExceptionCode;
import com.openexchange.pop3.storage.POP3StorageProperties;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbPOP3StorageProperties} - Database-backed implementation of {@link POP3StorageProperties}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RdbPOP3StorageProperties implements POP3StorageProperties {

    private final int cid;
    private final int user;
    private final int accountId;

    /**
     * Initializes a new {@link RdbPOP3StorageProperties}.
     */
    public RdbPOP3StorageProperties(POP3Access pop3Access) {
        super();
        final Session s = pop3Access.getSession();
        cid = s.getContextId();
        user = s.getUserId();
        accountId = pop3Access.getAccountId();
    }

    /**
     * Drops all properties related to specified POP3 account.
     *
     * @param accountId The account ID
     * @param user The user ID
     * @param cid The context ID
     * @param con The connection to use
     * @throws OXException If dropping properties fails
     */
    public static void dropProperties(int accountId, int user, int cid, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM user_mail_account_properties WHERE cid = ? AND user = ? AND id = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    /**
     * Gets the named property related to specified POP3 account.
     *
     * @param accountId The account ID
     * @param user The user ID
     * @param cid The context ID
     * @param propertyName The property name
     * @param con The connection to use
     * @throws OXException If dropping properties fails
     */
    public static String getProperty(int accountId, int user, int cid, String propertyName, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT value FROM user_mail_account_properties WHERE cid = ? AND user = ? AND id = ? AND name = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, propertyName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
            return null;
        } catch (SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    @Override
    public void addProperty(String propertyName, String propertyValue) throws OXException {
        Connection con = Database.get(cid, true);
        try {
            try {
                addProperty(propertyName, propertyValue, false, con);
            } catch (SQLIntegrityConstraintViolationException e) {
                addProperty(propertyName, propertyValue, true, con);
            }
        } catch (SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Database.back(cid, true, con);
        }
    }

    private void addProperty(String propertyName, String propertyValue, boolean disableForeignKeyChecks, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        boolean restoreConstraints = false;
        try {
            // Delete possibly existing mapping
            stmt = con.prepareStatement("DELETE FROM user_mail_account_properties WHERE cid = ? AND user = ? AND id = ? AND name = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, propertyName);
            stmt.executeUpdate();
            closeSQLStuff(null, stmt);

            // Insert new mapping
            if (disableForeignKeyChecks) {
                restoreConstraints = disableForeignKeyChecks(con);
            }
            stmt = con.prepareStatement("INSERT INTO user_mail_account_properties (cid, user, id, name, value) VALUES (?, ?, ?, ?, ?)");
            pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, propertyName);
            stmt.setString(pos++, propertyValue);
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(null, stmt);
            if (restoreConstraints) {
                try {
                    DBUtils.enableMysqlForeignKeyChecks(con);
                } catch (Exception e) {
                    org.slf4j.LoggerFactory.getLogger(RdbPOP3StorageProperties.class).error("Failed to enable foregn key checks", e);
                }
            }
        }
    }

    private boolean disableForeignKeyChecks(Connection con) {
        try {
            DBUtils.disableMysqlForeignKeyChecks(con);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getProperty(String propertyName) throws OXException {
        Connection con = Database.get(cid, false);
        try {
            return getProperty(accountId, user, cid, propertyName, con);
        } finally {
            Database.back(cid, false, con);
        }
    }

    @Override
    public void removeProperty(String propertyName) throws OXException {
        Connection con = Database.get(cid, true);
        try {
            try {
                removeProperty(propertyName, false, con);
            } catch (SQLIntegrityConstraintViolationException e) {
                removeProperty(propertyName, true, con);
            }
        } catch (SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Database.back(cid, true, con);
        }
    }

    private void removeProperty(String propertyName, boolean disableForeignKeyChecks, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        boolean restoreConstraints = false;
        try {
            // Delete possibly existing mapping
            if (disableForeignKeyChecks) {
                restoreConstraints = disableForeignKeyChecks(con);
            }
            stmt = con.prepareStatement("DELETE FROM user_mail_account_properties WHERE cid = ? AND user = ? AND id = ? AND name = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, propertyName);
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(null, stmt);
            if (restoreConstraints) {
                try {
                    DBUtils.enableMysqlForeignKeyChecks(con);
                } catch (Exception e) {
                    org.slf4j.LoggerFactory.getLogger(RdbPOP3StorageProperties.class).error("Failed to enable foregn key checks", e);
                }
            }
        }
    }

}
