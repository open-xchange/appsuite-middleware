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

package com.openexchange.pop3.storage.mailaccount;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.database.Database;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.POP3Exception;
import com.openexchange.pop3.storage.FullnameUIDPair;
import com.openexchange.pop3.storage.POP3StorageUIDLMap;
import com.openexchange.server.impl.DBPoolingException;
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
        this.cid = s.getContextId();
        this.user = s.getUserId();
        this.accountId = pop3Access.getAccountId();
    }

    private static final String SQL_INSERT_UIDLS = "INSERT INTO " + TABLE_NAME + " (cid, user, id, uidl, fullname, uid) VALUES (?, ?, ?, ?, ?, ?)";

    public void addMappings(final String[] uidls, final FullnameUIDPair[] fullnameUIDPairs) throws POP3Exception {
        final Connection con;
        try {
            con = Database.get(cid, true);
        } catch (final DBPoolingException e) {
            throw new POP3Exception(e);
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_INSERT_UIDLS);
            for (int i = 0; i < uidls.length; i++) {
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, user);
                stmt.setInt(pos++, accountId);
                stmt.setString(pos++, uidls[i]);
                final FullnameUIDPair pair = fullnameUIDPairs[i];
                stmt.setString(pos++, pair.getFullname());
                stmt.setString(pos++, pair.getMailId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (final SQLException e) {
            throw new POP3Exception(POP3Exception.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
            Database.back(cid, true, con);
        }
    }

    private static final String SQL_SELECT_PAIR = "SELECT fullname, uid FROM " + TABLE_NAME + " WHERE cid = ? AND user = ? AND id = ? AND uidl = ?";

    public FullnameUIDPair getFullnameUIDPair(final String uidl) throws POP3Exception {
        final Connection con;
        try {
            con = Database.get(cid, false);
        } catch (final DBPoolingException e) {
            throw new POP3Exception(e);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_PAIR);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, user);
            stmt.setInt(pos++, accountId);
            stmt.setString(pos++, uidl);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return new FullnameUIDPair(rs.getString(1), rs.getString(2));
            }
            return null;
        } catch (final SQLException e) {
            throw new POP3Exception(POP3Exception.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
    }

    private static final String SQL_SELECT_UIDL = "SELECT uidl FROM " + TABLE_NAME + " WHERE cid = ? AND user = ? AND id = ? AND fullname = ? AND uid = ?";

    public String getUIDL(final FullnameUIDPair fullnameUIDPair) throws POP3Exception {
        final Connection con;
        try {
            con = Database.get(cid, false);
        } catch (final DBPoolingException e) {
            throw new POP3Exception(e);
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
            stmt.setString(pos++, fullnameUIDPair.getMailId());
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
            return null;
        } catch (final SQLException e) {
            throw new POP3Exception(POP3Exception.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
    }

    public String[] getUIDLs(final FullnameUIDPair[] fullnameUIDPairs) throws POP3Exception {
        final String[] uidls = new String[fullnameUIDPairs.length];
        for (int i = 0; i < uidls.length; i++) {
            uidls[i] = getUIDL(fullnameUIDPairs[i]);
        }
        return uidls;
    }

    public FullnameUIDPair[] getFullnameUIDPairs(final String[] uidls) throws POP3Exception {
        final FullnameUIDPair[] pairs = new FullnameUIDPair[uidls.length];
        for (int i = 0; i < pairs.length; i++) {
            pairs[i] = getFullnameUIDPair(uidls[i]);
        }
        return pairs;
    }

    private static final String SQL_SELECT_ALL_UIDL = "SELECT uidl, fullname, uid FROM " + TABLE_NAME + " WHERE cid = ? AND user = ? AND id = ?";

    public Map<String, FullnameUIDPair> getAllUIDLs() throws POP3Exception {
        final Connection con;
        try {
            con = Database.get(cid, false);
        } catch (final DBPoolingException e) {
            throw new POP3Exception(e);
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
            throw new POP3Exception(POP3Exception.Code.SQL_ERROR, e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(cid, false, con);
        }
    }
}
