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

package com.openexchange.groupware.ldap;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.openexchange.groupware.Component;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapException.Code;
import com.openexchange.server.DBPool;

/**
 * This class implements the group storage using a relational database.
 */
public class RdbGroupStorage extends GroupStorage {

    private static final String SELECT_GROUPS = "SELECT " + IDENTIFIER + ','
        + DISPLAYNAME + ',' + LAST_MODIFIED + " FROM groups WHERE cid=?";

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
	public Group getGroup(final int gid, final Context context) throws LdapException {
        Connection con;
        try {
            con = DBPool.pickup(context);
        } catch (Exception e) {
            throw new LdapException(Component.GROUP, Code.NO_CONNECTION, e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        Group group = null;
        try {
            stmt = con.prepareStatement(SELECT_GROUPS + " AND id = ?");
            stmt.setLong(1, context.getContextId());
            stmt.setInt(2, gid);
            result = stmt.executeQuery();
            if (result.next()) {
                group = new Group();
                int pos = 1;
                group.setIdentifier(result.getInt(pos++));
                group.setDisplayName(result.getString(pos++));
                group.setLastModified(new Date(result.getLong(pos++)));
            } else {
                throw new LdapException(Component.GROUP, Code.GROUP_NOT_FOUND,
                    Integer.valueOf(gid), Integer.valueOf(context.getContextId()));
            }
            group.setMember(selectMember(con, context, group.getIdentifier()));
        } catch (SQLException e) {
            throw new LdapException(Component.GROUP, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return group;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public Group[] listModifiedGroups(final Date modifiedSince, final Context context)
        throws LdapException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (Exception e) {
            throw new LdapException(Component.GROUP, Code.NO_CONNECTION, e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        Group[] groups = null;
        try {
            stmt = con.prepareStatement(SELECT_GROUPS + " AND lastModified>=?");
            stmt.setLong(1, context.getContextId());
            stmt.setLong(2, modifiedSince.getTime());
            result = stmt.executeQuery();
            final List<Group> tmp = new ArrayList<Group>();
            while (result.next()) {
                final Group group = new Group();
                int pos = 1;
                group.setIdentifier(result.getInt(pos++));
                group.setDisplayName(result.getString(pos++));
                group.setLastModified(new Date(result.getLong(pos++)));
                group.setMember(selectMember(con, context,
                    group.getIdentifier()));
                tmp.add(group);
            }
            groups = tmp.toArray(new Group[tmp.size()]);
        } catch (SQLException e) {
            throw new LdapException(Component.GROUP, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return groups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public Group[] searchGroups(final String pattern, final Context context) throws LdapException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (Exception e) {
            throw new LdapException(Component.GROUP, Code.NO_CONNECTION, e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        final String sql = SELECT_GROUPS + " AND " + DISPLAYNAME + " LIKE ?";
        final List<Group> groups = new ArrayList<Group>();
        try {
            stmt = con.prepareStatement(sql);
            stmt.setLong(1, context.getContextId());
            stmt.setString(2, LdapUtility.prepareSearchPattern(pattern));
            result = stmt.executeQuery();
            while (result.next()) {
                final Group group = new Group();
                int pos = 1;
                group.setIdentifier(result.getInt(pos++));
                group.setDisplayName(result.getString(pos++));
                group.setLastModified(new Date(result.getLong(pos++)));
                group.setMember(selectMember(con, context,
                    group.getIdentifier()));
                groups.add(group);
            }
        } catch (SQLException e) {
            throw new LdapException(Component.GROUP, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return groups.toArray(new Group[groups.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
	public Group[] getGroups(final Context context) throws LdapException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (Exception e) {
            throw new LdapException(Component.GROUP, Code.NO_CONNECTION, e);
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        Group[] groups = null;
        try {
            stmt = con.prepareStatement(SELECT_GROUPS);
            stmt.setLong(1, context.getContextId());
            result = stmt.executeQuery();
            final List<Group> tmp = new ArrayList<Group>();
            while (result.next()) {
                final Group group = new Group();
                int pos = 1;
                group.setIdentifier(result.getInt(pos++));
                group.setDisplayName(result.getString(pos++));
                group.setLastModified(new Date(result.getLong(pos++)));
                group.setMember(selectMember(con, context,
                    group.getIdentifier()));
                tmp.add(group);
            }
            groups = tmp.toArray(new Group[tmp.size()]);
        } catch (SQLException e) {
            throw new LdapException(Component.GROUP, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return groups;
    }

    private int[] selectMember(final Connection con, final Context ctx,
        final int groupId) throws SQLException {
        final String getMember = "SELECT " + MEMBER
            + " FROM groups_member WHERE cid=? AND id=?";
        PreparedStatement stmt = null;
        ResultSet result = null;
        final List<Integer> tmp = new ArrayList<Integer>();
        try {
            stmt = con.prepareStatement(getMember);
            stmt.setLong(1, ctx.getContextId());
            stmt.setInt(2, groupId);
            result = stmt.executeQuery();
            while (result.next()) {
                tmp.add(Integer.valueOf(result.getInt(1)));
            }
        } finally {
            closeSQLStuff(result, stmt);
        }
        int[] member = new int[tmp.size()];
        for (int i = 0; i < member.length; i++) {
            member[i] = tmp.get(i).intValue();
        }
        return member;
    }
}
