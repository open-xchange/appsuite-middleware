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
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
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
            stmt = con.prepareStatement("DELETE FROM pop3_storage_ids WHERE cid = ? AND user = ? AND id = ?");
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

    @Override
    public void addMappings(final String[] uidls, final FullnameUIDPair[] fullnameUIDPairs) throws OXException {
        final Connection con = Database.get(cid, true);
        PreparedStatement stmt = null;
        try {
            // Insert or update specified mappings
            stmt = con.prepareStatement("INSERT INTO pop3_storage_ids (cid, user, id, uidl, fullname, uid) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE fullname=?, uid=?");
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            stmt.setInt(3, accountId);
            for (int i = 0; i < uidls.length; i++) {
                String uidl = uidls[i];
                if (uidl != null) {
                    FullnameUIDPair pair = fullnameUIDPairs[i];
                    String fullname = pair.getFullname();
                    String mailId = pair.getMailId();
                    stmt.setString(4, uidl);
                    stmt.setString(5, fullname);
                    stmt.setString(6, mailId);
                    stmt.setString(7, fullname);
                    stmt.setString(8, mailId);
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        } catch (java.sql.BatchUpdateException e) {
            closeSQLStuff(null, stmt);
            stmt = null;
            // One-by-one
            for (int i = 0; i < uidls.length; i++) {
                String uidl = uidls[i];
                if (uidl != null) {
                    insertOnDuplicateUpdate(uidl, fullnameUIDPairs[i], con);
                }
            }
        } catch (SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }

    private void insertOnDuplicateUpdate(final String uidl, final FullnameUIDPair pair, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO pop3_storage_ids (cid, user, id, uidl, fullname, uid) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE fullname=?, uid=?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, uidl);
            stmt.setString(pos++, pair.getFullname());
            stmt.setString(pos++, pair.getMailId());
            stmt.setString(pos++, pair.getFullname());
            stmt.setString(pos, pair.getMailId());
            stmt.executeUpdate();
        } catch (DataTruncation e) {
            throw POP3ExceptionCode.UIDL_TOO_BIG.create(e, uidl);
        } catch (SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    @Override
    public FullnameUIDPair getFullnameUIDPair(final String uidl) throws OXException {
        final Connection con = Database.get(cid, false);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT fullname, uid FROM pop3_storage_ids WHERE cid = ? AND user = ? AND id = ? AND uidl = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos, uidl);
            rs = stmt.executeQuery();
            return rs.next() ? new FullnameUIDPair(rs.getString(1), rs.getString(2)) : null;
        } catch (SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
    }

    @Override
    public String getUIDL(final FullnameUIDPair fullnameUIDPair) throws OXException {
        final Connection con = Database.get(cid, false);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT uidl FROM pop3_storage_ids WHERE cid = ? AND user = ? AND id = ? AND fullname = ? AND uid = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, fullnameUIDPair.getFullname());
            stmt.setString(pos, fullnameUIDPair.getMailId());
            rs = stmt.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        } catch (SQLException e) {
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

    @Override
    public Map<String, FullnameUIDPair> getAllUIDLs() throws OXException {
        final Connection con = Database.get(cid, false);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT uidl, fullname, uid FROM pop3_storage_ids WHERE cid = ? AND user = ? AND id = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyMap();
            }

            Map<String, FullnameUIDPair> m = new HashMap<String, FullnameUIDPair>(32);
            do {
                m.put(rs.getString(1), new FullnameUIDPair(rs.getString(2), rs.getString(3)));
            } while (rs.next());
            return m;
        } catch (SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
    }

    @Override
    public void deleteFullnameUIDPairMappings(final FullnameUIDPair[] fullnameUIDPairs) throws OXException {
        final Connection con = Database.get(cid, true);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM pop3_storage_ids WHERE cid = ? AND user = ? AND id = ? AND fullname = ? AND uid = ?");
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
        } catch (SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }

    @Override
    public void deleteUIDLMappings(final String[] uidls) throws OXException {
        final Connection con = Database.get(cid, true);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM pop3_storage_ids WHERE cid = ? AND user = ? AND id = ? AND uidl = ?");
            for (int i = 0; i < uidls.length; i++) {
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, uidls[i]);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw POP3ExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }
}
