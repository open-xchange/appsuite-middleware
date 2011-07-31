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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mail.headercache.properties;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.headercache.services.HeaderCacheServiceRegistry;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RdbHeaderCacheProperties} - Database-backed implementation of {@link HeaderCacheProperties}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RdbHeaderCacheProperties implements HeaderCacheProperties {

    private static final String TABLE_NAME = "user_mail_account_properties";

    private final int cid;

    private final int user;

    private final int accountId;

    /**
     * Initializes a new {@link RdbHeaderCacheProperties}.
     *
     * @param accountId The account ID
     * @param user The user ID
     * @param cid The context ID
     */
    public RdbHeaderCacheProperties(final int accountId, final int user, final int cid) {
        super();
        this.cid = cid;
        this.user = user;
        this.accountId = accountId;
    }

    private static final String SQL_DROP_PROPERTIES = "DELETE FROM " + TABLE_NAME + " WHERE cid = ? AND user = ? AND id = ?";

    /**
     * Drops all properties related to specified account using given connection.
     *
     * @param accountId The account ID
     * @param user The user ID
     * @param cid The context ID
     * @param con The connection to use
     * @throws OXException If dropping properties fails
     */
    public static void dropProperties(final int accountId, final int user, final int cid, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_DROP_PROPERTIES);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " (cid, user, id, name, value) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " WHERE cid = ? AND user = ? AND id = ? AND name = ?";

    public void addProperty(final String propertyName, final String propertyValue) throws OXException {
        final DatabaseService databaseService = getDBService();
        final Connection con;
        try {
            con = databaseService.getWritable(cid);
            con.setAutoCommit(false); // BEGIN;
        } catch (final SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        PreparedStatement stmt = null;
        try {
            // Delete possibly existing mapping
            stmt = con.prepareStatement(SQL_DELETE);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, propertyName);
            stmt.executeUpdate();
            DBUtils.closeSQLStuff(null, stmt);
            // Insert new mapping
            stmt = con.prepareStatement(SQL_INSERT);
            pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, propertyName);
            stmt.setString(pos++, propertyValue);
            stmt.executeUpdate();
            // Commit
            con.commit(); // COMMIT
        } catch (final SQLException e) {
            DBUtils.rollback(con); // ROLL-BACK
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            DBUtils.rollback(con); // ROLL-BACK
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            DBUtils.autocommit(con);
            databaseService.backWritable(cid, con);
        }
    }

    private static final String SQL_SELECT = "SELECT value FROM " + TABLE_NAME + " WHERE cid = ? AND user = ? AND id = ? AND name = ?";

    public String getProperty(final String propertyName) throws OXException {
        final DatabaseService databaseService = getDBService();
        final Connection con = databaseService.getReadOnly(cid);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT);
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
        } catch (final SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(null, stmt);
            databaseService.backReadOnly(cid, con);
        }
    }

    public void removeProperty(final String propertyName) throws OXException {
        final DatabaseService databaseService = getDBService();
        final Connection con;
        try {
            con = databaseService.getWritable(cid);
            con.setAutoCommit(false); // BEGIN;
        } catch (final SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        PreparedStatement stmt = null;
        try {
            // Delete possibly existing mapping
            stmt = con.prepareStatement(SQL_DELETE);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, propertyName);
            stmt.executeUpdate();
            // Commit
            con.commit(); // COMMIT
        } catch (final SQLException e) {
            DBUtils.rollback(con); // ROLL-BACK
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            DBUtils.rollback(con); // ROLL-BACK
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            DBUtils.autocommit(con);
            databaseService.backWritable(cid, con);
        }
    }

    private static DatabaseService getDBService() throws OXException {
        final DatabaseService databaseService;
        try {
            databaseService = HeaderCacheServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
        } catch (final OXException e) {
            throw new OXException(e);
        }
        return databaseService;
    }

}
