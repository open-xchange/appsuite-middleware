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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.POP3ExceptionCode;
import com.openexchange.pop3.storage.POP3StorageTrashContainer;
import com.openexchange.session.Session;

/**
 * {@link RdbPOP3StorageTrashContainer} - Database-backed implementation of {@link POP3StorageTrashContainer}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RdbPOP3StorageTrashContainer implements POP3StorageTrashContainer {

    private static final String TABLE_NAME = "pop3_storage_deleted";

    private final int cid;

    private final int user;

    private final int accountId;

    /**
     * Initializes a new {@link RdbPOP3StorageTrashContainer}.
     */
    public RdbPOP3StorageTrashContainer(final POP3Access pop3Access) {
        super();
        final Session s = pop3Access.getSession();
        cid = s.getContextId();
        user = s.getUserId();
        accountId = pop3Access.getAccountId();
    }

    private static final String SQL_DROP_PROPERTIES = "DELETE FROM " + TABLE_NAME + " WHERE cid = ? AND user = ? AND id = ?";

    /**
     * Drops all trash entries related to specified POP3 account.
     *
     * @param accountId The account ID
     * @param user The user ID
     * @param cid The context ID
     * @param con The connection to use
     * @throws OXException If dropping properties fails
     */
    public static void dropTrash(final int accountId, final int user, final int cid, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_DROP_PROPERTIES);
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

    private static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " WHERE cid = ? AND user = ? AND id = ? AND uidl = ?";

    private static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " (cid, user, id, uidl) VALUES (?, ?, ?, ?)";

    @Override
    public void addUIDL(final String uidl) throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, true);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_DELETE);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, uidl);
            stmt.executeUpdate();
            closeSQLStuff(null, stmt);
            stmt = con.prepareStatement(SQL_INSERT);
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, uidl);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }

    private static final String SQL_DELETE_ALL = "DELETE FROM " + TABLE_NAME + " WHERE cid = ? AND user = ? AND id = ?";

    @Override
    public void clear() throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, true);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_DELETE_ALL);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }

    private static final String SQL_SELECT_ALL = "SELECT uidl FROM " + TABLE_NAME + " WHERE cid = ? AND user = ? AND id = ?";

    @Override
    public Set<String> getUIDLs() throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, false);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_ALL);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            rs = stmt.executeQuery();
            final Set<String> set = new HashSet<String>();
            while (rs.next()) {
                set.add(rs.getString(1));
            }
            return set;
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, false, con);
        }
    }

    @Override
    public void removeUIDL(final String uidl) throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, true);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_DELETE);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, uidl);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }

    @Override
    public void addAllUIDL(final Collection<? extends String> uidls) throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, true);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_DELETE);
            for (final String uidl : uidls) {
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, uidl);
                stmt.addBatch();
            }
            stmt.executeBatch();
            closeSQLStuff(null, stmt);
            stmt = con.prepareStatement(SQL_INSERT);
            for (final String uidl : uidls) {
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, uidl);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }

}
