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

package com.openexchange.pop3.storage.mailaccount;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
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
        } catch (final SQLException e) {
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
