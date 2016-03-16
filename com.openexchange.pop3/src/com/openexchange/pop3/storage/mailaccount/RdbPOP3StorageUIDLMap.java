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
import java.util.HashMap;
import java.util.Map;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.POP3ExceptionCode;
import com.openexchange.pop3.storage.FullnameUIDPair;
import com.openexchange.pop3.storage.POP3StorageUIDLMap;
import com.openexchange.session.Session;

/**
 * {@link RdbPOP3StorageUIDLMap} - Database-backed implementation of {@link POP3StorageUIDLMap}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RdbPOP3StorageUIDLMap implements POP3StorageUIDLMap {

    private static final String TABLE_NAME = "pop3_storage_ids";

    private final int cid;
    private final int user;
    private final int accountId;

    /**
     * Initializes a new {@link RdbPOP3StorageUIDLMap}.
     */
    public RdbPOP3StorageUIDLMap(final POP3Access pop3Access) {
        super();
        final Session s = pop3Access.getSession();
        cid = s.getContextId();
        user = s.getUserId();
        accountId = pop3Access.getAccountId();
    }

    private static final String SQL_DROP_PROPERTIES = "DELETE FROM " + TABLE_NAME + " WHERE cid = ? AND user = ? AND id = ?";

    /**
     * Drops all ID entries related to specified POP3 account.
     *
     * @param accountId The account ID
     * @param user The user ID
     * @param cid The context ID
     * @param con The connection to use
     * @throws OXException If dropping properties fails
     */
    public static void dropIDs(final int accountId, final int user, final int cid, final Connection con) throws OXException {
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

    private static final String SQL_DELETE_UIDLS = "DELETE FROM " + TABLE_NAME + " WHERE cid = ? AND user = ? AND id = ? AND uidl = ?";

    private static final String SQL_INSERT_UIDLS = "INSERT INTO " + TABLE_NAME + " (cid, user, id, uidl, fullname, uid) VALUES (?, ?, ?, ?, ?, ?)";

    @Override
    public void addMappings(final String[] uidls, final FullnameUIDPair[] fullnameUIDPairs) throws OXException {
        final Connection con = Database.get(cid, true);
        PreparedStatement stmt = null;
        try {
            // Delete possibly existing mappings for specified UIDLs
            stmt = con.prepareStatement(SQL_DELETE_UIDLS);
            int pos;
            for (final String uidl : uidls) {
                if (uidl != null) {
                    pos = 1;
                    stmt.setInt(pos++, cid);
                    stmt.setInt(pos++, user);
                    stmt.setInt(pos++, accountId);
                    stmt.setString(pos, uidl);
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
            closeSQLStuff(null, stmt);
            // Insert new mappings for specified UIDLs
            stmt = con.prepareStatement(SQL_INSERT_UIDLS);
            for (int i = 0; i < uidls.length; i++) {
                final String uidl = uidls[i];
                if (uidl != null) {
                    final FullnameUIDPair pair = fullnameUIDPairs[i];
                    final String fullname = pair.getFullname();
                    final String mailId = pair.getMailId();
                    if (null != fullname && null != mailId) {
                        pos = 1;
                        stmt.setInt(pos++, cid);
                        stmt.setInt(pos++, user);
                        stmt.setInt(pos++, accountId);
                        stmt.setString(pos++, uidl);
                        stmt.setString(pos++, fullname);
                        stmt.setString(pos, mailId);
                        stmt.addBatch();
                    }
                }
            }
            stmt.executeBatch();
        } catch (final java.sql.BatchUpdateException e) {
            closeSQLStuff(null, stmt);
            stmt = null;
            // One-by-one
            for (int i = 0; i < uidls.length; i++) {
                final String uidl = uidls[i];
                if (uidl != null) {
                    if (exists(uidl, con)) {
                        update(uidl, fullnameUIDPairs[i], con);
                    } else {
                        insert(uidl, fullnameUIDPairs[i], con);
                    }
                }
            }
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }

    private void update(final String uidl, final FullnameUIDPair pair, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE " + TABLE_NAME + " SET fullname=?, uid=? WHERE cid = ? AND user = ? AND id = ? AND uidl = ?");
            int pos = 1;
            stmt.setString(pos++, pair.getFullname());
            stmt.setString(pos++, pair.getMailId());
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos, uidl);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    private void insert(final String uidl, final FullnameUIDPair pair, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_INSERT_UIDLS);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, uidl);
            stmt.setString(pos++, pair.getFullname());
            stmt.setString(pos, pair.getMailId());
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    private boolean exists(final String uidl, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT 1 FROM " + TABLE_NAME + " WHERE cid = ? AND user = ? AND id = ? AND uidl = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos, uidl);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static final String SQL_SELECT_PAIR = "SELECT fullname, uid FROM " + TABLE_NAME + " WHERE cid = ? AND user = ? AND id = ? AND uidl = ?";

    @Override
    public FullnameUIDPair getFullnameUIDPair(final String uidl) throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, false);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_PAIR);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos, uidl);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return new FullnameUIDPair(rs.getString(1), rs.getString(2));
            }
            return null;
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
    }

    private static final String SQL_SELECT_UIDL = "SELECT uidl FROM " + TABLE_NAME + " WHERE cid = ? AND user = ? AND id = ? AND fullname = ? AND uid = ?";

    @Override
    public String getUIDL(final FullnameUIDPair fullnameUIDPair) throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, false);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_UIDL);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, fullnameUIDPair.getFullname());
            stmt.setString(pos, fullnameUIDPair.getMailId());
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
            return null;
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
    }

    @Override
    public String[] getUIDLs(final FullnameUIDPair[] fullnameUIDPairs) throws OXException {
        final String[] uidls = new String[fullnameUIDPairs.length];
        for (int i = 0; i < uidls.length; i++) {
            uidls[i] = getUIDL(fullnameUIDPairs[i]);
        }
        return uidls;
    }

    @Override
    public FullnameUIDPair[] getFullnameUIDPairs(final String[] uidls) throws OXException {
        final FullnameUIDPair[] pairs = new FullnameUIDPair[uidls.length];
        for (int i = 0; i < pairs.length; i++) {
            pairs[i] = getFullnameUIDPair(uidls[i]);
        }
        return pairs;
    }

    private static final String SQL_SELECT_ALL_UIDL = "SELECT uidl, fullname, uid FROM " + TABLE_NAME + " WHERE cid = ? AND user = ? AND id = ?";

    @Override
    public Map<String, FullnameUIDPair> getAllUIDLs() throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, false);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_ALL_UIDL);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            rs = stmt.executeQuery();
            final Map<String, FullnameUIDPair> m = new HashMap<String, FullnameUIDPair>();
            while (rs.next()) {
                final FullnameUIDPair pair = new FullnameUIDPair(rs.getString(2), rs.getString(3));
                m.put(rs.getString(1), pair);
            }
            return m;
        } catch (final SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
    }

    private static final String SQL_DELETE_PAIRS = "DELETE FROM " + TABLE_NAME + " WHERE cid = ? AND user = ? AND id = ? AND fullname = ? AND uid = ?";

    @Override
    public void deleteFullnameUIDPairMappings(final FullnameUIDPair[] fullnameUIDPairs) throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, true);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_DELETE_PAIRS);
            for (int i = 0; i < fullnameUIDPairs.length; i++) {
                final FullnameUIDPair pair = fullnameUIDPairs[i];
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, pair.getFullname());
                stmt.setString(pos++, pair.getMailId());
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

    @Override
    public void deleteUIDLMappings(final String[] uidls) throws OXException {
        final Connection con;
        try {
            con = Database.get(cid, true);
        } catch (final OXException e) {
            throw e;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_DELETE_UIDLS);
            for (int i = 0; i < uidls.length; i++) {
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, uidls[i]);
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
