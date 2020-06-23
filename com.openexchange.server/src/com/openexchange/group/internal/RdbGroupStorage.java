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

package com.openexchange.group.internal;

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.group.internal.SQLStrings.INSERT_GROUP;
import static com.openexchange.tools.sql.DBUtils.getIN;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupExceptionCodes;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.groupware.ldap.LdapUtility;
import com.openexchange.java.Strings;
import com.openexchange.server.impl.DBPool;
import com.openexchange.tools.sql.DBUtils;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * This class implements the group storage using a relational database.
 */
public class RdbGroupStorage implements GroupStorage {

    /**
     * Default constructor.
     */
    public RdbGroupStorage() {
        super();
    }

    @Override
    public void insertGroup(final Context ctx, final Connection con, final Group group, final StorageType type) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_GROUP.get(type));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, group.getIdentifier());
            stmt.setString(pos++, StorageType.DELETED.equals(type) ? "" : group.getSimpleName());
            stmt.setString(pos++, StorageType.DELETED.equals(type) ? "" : group.getDisplayName());
            stmt.setLong(pos++, group.getLastModified().getTime());
            stmt.setInt(pos++, 65534);
            stmt.execute();
        } catch (SQLException e) {
            throw GroupExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    @Override
    public void updateGroup(final Context ctx, final Connection con, final Group group, final Date lastRead) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE groups SET identifier=?,displayName=?,lastModified=? WHERE cid=? AND id=? AND lastModified<=?");
            int pos = 1;
            stmt.setString(pos++, group.getSimpleName());
            stmt.setString(pos++, group.getDisplayName());
            stmt.setLong(pos++, group.getLastModified().getTime());
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, group.getIdentifier());
            stmt.setLong(pos++, lastRead.getTime());
            final int rows = stmt.executeUpdate();
            if (1 != rows) {
                throw GroupExceptionCodes.MODIFIED.create();
            }
        } catch (SQLException e) {
            throw GroupExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    @Override
    public void insertMember(final Context ctx, final Connection con, final Group group, final int[] members) throws OXException {
        if (0 == members.length) {
            return;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO groups_member (cid,id,member) VALUES (?,?,?)");
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, group.getIdentifier());
            for (final int member : members) {
                stmt.setInt(3, member);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw GroupExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    @Override
    public void deleteMember(final Context ctx, final Connection con, final Group group, final int[] members) throws OXException {
        if (0 == members.length) {
            return;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(getIN("DELETE FROM groups_member WHERE cid=? AND id=? AND member IN (", members.length));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, group.getIdentifier());
            for (final int member : members) {
                stmt.setInt(pos++, member);
            }
            stmt.execute();
        } catch (SQLException e) {
            throw GroupExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    @Override
    public void deleteGroup(final Context ctx, final Connection con, final int groupId, final Date lastRead) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM groups WHERE cid=? AND id=? AND lastModified<=?");
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, groupId);
            stmt.setLong(3, lastRead.getTime());
            final int rows = stmt.executeUpdate();
            if (1 != rows) {
                throw GroupExceptionCodes.MODIFIED.create();
            }
        } catch (SQLException e) {
            throw GroupExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    @Override
    public Group getGroup(final int gid, final Context context) throws OXException {
        return getGroup(gid, true, context);
    }

    @Override
    public Group getGroup(final int gid, boolean loadMembers, final Context context) throws OXException {
        return getGroup(new int[] { gid }, loadMembers, context)[0];
    }

    @Override
    public Group[] getGroup(final int[] groupIds, final Context context) throws OXException {
        return getGroup(groupIds, true, context);
    }

    /**
     * Gets the groups associated with given group identifiers.
     *
     * @param groupIds The identifiers of the groups to return
     * @param loadMembers Whether to load members for each group or not
     * @param context The context
     * @return The groups
     * @throws OXException If groups cannot be loaded
     */
    public Group[] getGroup(final int[] groupIds, boolean loadMembers, final Context context) throws OXException {
        int length = groupIds.length;
        if (0 == length) {
            return new Group[0];
        }

        Connection con = DBPool.pickup(context);
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(DBUtils.getIN("SELECT id,identifier,displayName,lastModified FROM groups WHERE cid=? AND id IN (", length));
            int pos = 1;
            stmt.setLong(pos++, context.getContextId());
            for (final int groupId : groupIds) {
                stmt.setInt(pos++, groupId);
            }
            result = stmt.executeQuery();
            if (!result.next()) {
                throw LdapExceptionCode.GROUP_NOT_FOUND.create(Integer.valueOf(groupIds[0]), Integer.valueOf(context.getContextId())).setPrefix("GRP");
            }

            TIntObjectMap<Group> groups = new TIntObjectHashMap<Group>(length);
            do {
                pos = 1;
                Group group = new Group();
                group.setIdentifier(result.getInt(pos++));
                group.setSimpleName(result.getString(pos++));
                group.setDisplayName(result.getString(pos++));
                group.setLastModified(new Date(result.getLong(pos++)));
                if (loadMembers) {
                    group.setMember(selectMember(con, context, group.getIdentifier()));
                }
                groups.put(group.getIdentifier(), group);
            } while (result.next());
            closeSQLStuff(result, stmt);
            result = null;
            stmt = null;
            DBPool.closeReaderSilent(context, con);
            con = null;

            Group[] retval = new Group[length];
            for (int i = length; i-- > 0;) {
                Group group = groups.get(groupIds[i]);
                if (group == null) {
                    throw LdapExceptionCode.GROUP_NOT_FOUND.create(Integer.valueOf(groupIds[i]), Integer.valueOf(context.getContextId())).setPrefix("GRP");
                }
                retval[i] = group;
            }
            return retval;
        } catch (SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("GRP");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
    }

    @Override
    public Group[] listModifiedGroups(final Date modifiedSince, final Context context) throws OXException {
        return listModifiedOrDeletedGroups(modifiedSince, context, "SELECT id,identifier,displayName,lastModified FROM groups WHERE cid=?");
    }

    @Override
    public Group[] listDeletedGroups(final Date modifiedSince, final Context context) throws OXException {
        return listModifiedOrDeletedGroups(modifiedSince, context, "SELECT id,identifier,displayName,lastModified FROM del_groups WHERE cid=?");
    }

    private Group[] listModifiedOrDeletedGroups(final Date modifiedSince, final Context context, final String statement) throws OXException {
        final Connection con;
        try {
            con = DBPool.pickup(context);
        } catch (Exception e) {
            throw LdapExceptionCode.NO_CONNECTION.create(e).setPrefix("GRP");
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(statement + " AND lastModified>?");
            stmt.setLong(1, context.getContextId());
            stmt.setLong(2, modifiedSince.getTime());
            result = stmt.executeQuery();
            if (!result.next()) {
                return new Group[0];
            }

            List<Group> tmp = new ArrayList<Group>();
            do {
                final Group group = new Group();
                int pos = 1;
                group.setIdentifier(result.getInt(pos++));
                group.setSimpleName(result.getString(pos++));
                group.setDisplayName(result.getString(pos++));
                group.setLastModified(new Date(result.getLong(pos++)));
                group.setMember(selectMember(con, context, group.getIdentifier()));
                tmp.add(group);
            } while (result.next());
            return tmp.toArray(new Group[tmp.size()]);
        } catch (SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("GRP");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
    }

    /**
     * Gets the identifiers of the groups that have been modified since given date in specified context.
     *
     * @param modifiedSince The modified-since date
     * @param context The context
     * @return The identifiers of modified groups
     * @throws OXException If group identifiers cannot be returned
     */
    public int[] listModifiedGroupIds(Date modifiedSince, Context context) throws OXException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con = DBPool.pickup(context);
            stmt = con.prepareStatement("SELECT id FROM groups WHERE cid=? AND lastModified>?");
            stmt.setLong(1, context.getContextId());
            stmt.setLong(2, modifiedSince.getTime());
            result = stmt.executeQuery();
            if (!result.next()) {
                return new int[0];
            }

            TIntList tmp = new TIntArrayList();
            do {
                tmp.add(result.getInt(1));
            } while (result.next());
            return tmp.toArray();
        } catch (SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("GRP");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
    }

    @Override
    public Group[] searchGroups(final String pattern, final boolean loadMembers, final Context context) throws OXException {
        if (Strings.isEmpty(pattern)) {
            return new Group[0];
        }

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con = DBPool.pickup(context);
            stmt = con.prepareStatement("SELECT id,identifier,displayName,lastModified FROM groups WHERE cid=? AND (displayName LIKE ? OR identifier LIKE ?)");
            stmt.setLong(1, context.getContextId());
            final String sqlPattern = LdapUtility.prepareSearchPattern(pattern);
            stmt.setString(2, sqlPattern);
            stmt.setString(3, sqlPattern);
            result = stmt.executeQuery();
            if (!result.next()) {
                return new Group[0];
            }

            List<Group> groups = new ArrayList<Group>();
            do {
                final Group group = new Group();
                int pos = 1;
                group.setIdentifier(result.getInt(pos++));
                group.setSimpleName(result.getString(pos++));
                group.setDisplayName(result.getString(pos++));
                group.setLastModified(new Date(result.getLong(pos++)));
                if (loadMembers) {
                    group.setMember(selectMember(con, context, group.getIdentifier()));
                }
                groups.add(group);
            } while (result.next());
            return groups.toArray(new Group[groups.size()]);
        } catch (SQLException e) {
            throw GroupExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
    }

    /**
     * Gets the identifiers of the groups matching given search pattern.
     *
     * @param pattern The search pattern
     * @param context The context
     * @return The group identifiers
     * @throws OXException If group identifiers cannot be returned
     */
    public int[] searchGroupIds(String pattern, Context context) throws OXException {
        if (Strings.isEmpty(pattern)) {
            return new int[0];
        }

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con = DBPool.pickup(context);
            stmt = con.prepareStatement("SELECT id FROM groups WHERE cid=? AND (displayName LIKE ? OR identifier LIKE ?)");
            stmt.setLong(1, context.getContextId());
            final String sqlPattern = LdapUtility.prepareSearchPattern(pattern);
            stmt.setString(2, sqlPattern);
            stmt.setString(3, sqlPattern);
            result = stmt.executeQuery();
            if (!result.next()) {
                return new int[0];
            }

            TIntList tmp = new TIntArrayList();
            do {
                tmp.add(result.getInt(1));
            } while (result.next());
            return tmp.toArray();
        } catch (SQLException e) {
            throw GroupExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
    }

    @Override
    public Group[] getGroups(final boolean loadMembers, final Context context) throws OXException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con = DBPool.pickup(context);
            stmt = con.prepareStatement("SELECT id,identifier,displayName,lastModified FROM groups WHERE cid=?");
            stmt.setLong(1, context.getContextId());
            result = stmt.executeQuery();
            if (!result.next()) {
                return new Group[0];
            }

            List<Group> tmp = new ArrayList<Group>();
            do {
                final Group group = new Group();
                int pos = 1;
                group.setIdentifier(result.getInt(pos++));
                group.setSimpleName(result.getString(pos++));
                group.setDisplayName(result.getString(pos++));
                group.setLastModified(new Date(result.getLong(pos++)));
                if (loadMembers) {
                    group.setMember(selectMember(con, context, group.getIdentifier()));
                }
                tmp.add(group);
            } while (result.next());
            return tmp.toArray(new Group[tmp.size()]);
        } catch (SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("GRP");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
    }

    /**
     * Gets the identifiers of all groups in given context.
     *
     * @param context The context
     * @return The group identifiers
     * @throws OXException If group identifiers cannot be returned
     */
    public int[] getGroupIds(Context context) throws OXException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con = DBPool.pickup(context);
            stmt = con.prepareStatement("SELECT id FROM groups WHERE cid=?");
            stmt.setLong(1, context.getContextId());
            result = stmt.executeQuery();
            if (!result.next()) {
                return new int[0];
            }

            TIntList tmp = new TIntArrayList();
            do {
                tmp.add(result.getInt(1));
            } while (result.next());
            return tmp.toArray();
        } catch (SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("GRP");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
    }

    private int[] selectMember(final Connection con, final Context ctx, final int groupId) throws SQLException {
        final String getMember = "SELECT member FROM groups_member WHERE cid=? AND id=?";
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(getMember);
            stmt.setLong(1, ctx.getContextId());
            stmt.setInt(2, groupId);
            result = stmt.executeQuery();
            if (!result.next()) {
                return new int[0];
            }

            TIntList tmp = new TIntArrayList();
            do {
                tmp.add(result.getInt(1));
            } while (result.next());
            return tmp.toArray();
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

}
