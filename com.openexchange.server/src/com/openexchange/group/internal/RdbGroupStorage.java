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

import static com.openexchange.group.internal.SQLStrings.INSERT_GROUP;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getIN;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
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

/**
 * This class implements the group storage using a relational database.
 */
public class RdbGroupStorage extends GroupStorage {

    private static final String SELECT_GROUPS = "SELECT id,identifier,displayName,lastModified FROM groups WHERE cid=?";
    private static final String SELECT_DELETED_GROUPS = "SELECT id,identifier,displayName,lastModified FROM del_groups WHERE cid=?";

    /**
     * Default constructor.
     */
    public RdbGroupStorage() {
        super();
    }

    /**
     * {@inheritDoc}
     */
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
        } catch (final SQLException e) {
            throw GroupExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGroup(final Context ctx, final Connection con, final Group group, final Date lastRead) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE groups SET identifier=?,"
                + "displayName=?,lastModified=? WHERE cid=? AND id=? AND lastModified<=?");
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
        } catch (final SQLException e) {
            throw GroupExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertMember(final Context ctx, final Connection con, final Group group, final int[] members) throws OXException {
        if (0 == members.length) {
            return;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO groups_member (cid,id,"
                + "member) VALUES (?,?,?)");
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, group.getIdentifier());
            for (final int member : members) {
                stmt.setInt(3, member);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (final SQLException e) {
            throw GroupExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMember(final Context ctx, final Connection con,
        final Group group, final int[] members) throws OXException {
        if (0 == members.length) {
            return;
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(getIN("DELETE FROM groups_member "
                + "WHERE cid=? AND id=? AND member IN (", members.length));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, group.getIdentifier());
            for (final int member : members) {
                stmt.setInt(pos++, member);
            }
            stmt.execute();
        } catch (final SQLException e) {
            throw GroupExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
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
        } catch (final SQLException e) {
            throw GroupExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group getGroup(final int gid, final Context context) throws OXException {
        final Connection con = DBPool.pickup(context);
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_GROUPS + " AND id = ?");
            stmt.setLong(1, context.getContextId());
            stmt.setInt(2, gid);
            result = stmt.executeQuery();
            if (!result.next()) {
                throw LdapExceptionCode.GROUP_NOT_FOUND.create(Integer.valueOf(gid), Integer.valueOf(context.getContextId())).setPrefix("GRP");
            }
            int pos = 1;
            Group group = new Group();
            group.setIdentifier(result.getInt(pos++));
            group.setSimpleName(result.getString(pos++));
            group.setDisplayName(result.getString(pos++));
            group.setLastModified(new Date(result.getLong(pos++)));
            group.setMember(selectMember(con, context, group.getIdentifier()));
            return group;
        } catch (final SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("GRP");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
    }

    @Override
    public Group[] listModifiedGroups(final Date modifiedSince, final Context context) throws OXException {
        return listModifiedOrDeletedGroups(modifiedSince, context, SELECT_GROUPS);
    }

    @Override
    public Group[] listDeletedGroups(final Date modifiedSince, final Context context) throws OXException {
        return listModifiedOrDeletedGroups(modifiedSince, context, SELECT_DELETED_GROUPS);
    }

    private Group[] listModifiedOrDeletedGroups(final Date modifiedSince, final Context context, final String statement) throws OXException {
        final Connection con;
        try {
            con = DBPool.pickup(context);
        } catch (final Exception e) {
            throw LdapExceptionCode.NO_CONNECTION.create(e).setPrefix("GRP");
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(statement + " AND lastModified>?");
            stmt.setLong(1, context.getContextId());
            stmt.setLong(2, modifiedSince.getTime());
            result = stmt.executeQuery();
            List<Group> tmp = new LinkedList<Group>();
            while (result.next()) {
                final Group group = new Group();
                int pos = 1;
                group.setIdentifier(result.getInt(pos++));
                group.setSimpleName(result.getString(pos++));
                group.setDisplayName(result.getString(pos++));
                group.setLastModified(new Date(result.getLong(pos++)));
                group.setMember(selectMember(con, context,
                    group.getIdentifier()));
                tmp.add(group);
            }
            return tmp.toArray(new Group[tmp.size()]);
        } catch (final SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e,
                e.getMessage()).setPrefix("GRP");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Group[] searchGroups(final String pattern, final boolean loadMembers, final Context context) throws OXException {
        if (Strings.isEmpty(pattern)) {
            return new Group[0];
        }
        final Connection con;
        try {
            con = DBPool.pickup(context);
        } catch (final Exception e) {
            throw GroupExceptionCodes.NO_CONNECTION.create(e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT_GROUPS + " AND (displayName LIKE ? OR identifier LIKE ?)");
            stmt.setLong(1, context.getContextId());
            final String sqlPattern = LdapUtility.prepareSearchPattern(pattern);
            stmt.setString(2, sqlPattern);
            stmt.setString(3, sqlPattern);
            result = stmt.executeQuery();
            List<Group> groups = new ArrayList<Group>();
            while (result.next()) {
                final Group group = new Group();
                int pos = 1;
                group.setIdentifier(result.getInt(pos++));
                group.setSimpleName(result.getString(pos++));
                group.setDisplayName(result.getString(pos++));
                group.setLastModified(new Date(result.getLong(pos++)));
                if(loadMembers) {
                    group.setMember(selectMember(con, context, group.getIdentifier()));
                }
                groups.add(group);
            }
            return groups.toArray(new Group[groups.size()]);
        } catch (final SQLException e) {
            throw GroupExceptionCodes.SQL_ERROR.create(e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Group[] getGroups(final boolean loadMembers, final Context context) throws OXException {
        final Connection con;
        try {
            con = DBPool.pickup(context);
        } catch (final Exception e) {
            throw LdapExceptionCode.NO_CONNECTION.create(e).setPrefix("GRP");
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        Group[] groups = null;
        try {
            stmt = con.prepareStatement(SELECT_GROUPS);
            stmt.setLong(1, context.getContextId());
            result = stmt.executeQuery();
            List<Group> tmp = new ArrayList<Group>();
            while (result.next()) {
                final Group group = new Group();
                int pos = 1;
                group.setIdentifier(result.getInt(pos++));
                group.setSimpleName(result.getString(pos++));
                group.setDisplayName(result.getString(pos++));
                group.setLastModified(new Date(result.getLong(pos++)));
                if(loadMembers) {
                    group.setMember(selectMember(con, context, group.getIdentifier()));
                }
                tmp.add(group);
            }
            groups = tmp.toArray(new Group[tmp.size()]);
        } catch (final SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e,
                e.getMessage()).setPrefix("GRP");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return groups;
    }

    private int[] selectMember(final Connection con, final Context ctx, final int groupId) throws SQLException {
        final String getMember = "SELECT member FROM groups_member WHERE cid=? AND id=?";
        PreparedStatement stmt = null;
        ResultSet result = null;
        TIntList tmp = new TIntLinkedList();
        try {
            stmt = con.prepareStatement(getMember);
            stmt.setLong(1, ctx.getContextId());
            stmt.setInt(2, groupId);
            result = stmt.executeQuery();
            while (result.next()) {
                tmp.add(result.getInt(1));
            }
        } finally {
            closeSQLStuff(result, stmt);
        }
        return tmp.toArray();
    }

}
