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
 * This class implements the resource storage using a relational database.
 */
public class RdbResourceStorage extends ResourceStorage {

    /**
     * Reference to the context.
     */
    private final transient Context context;

    /**
     * Default constructor.
     * @param context Context.
     */
    public RdbResourceStorage(final Context context) {
        super();
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceGroup getGroup(final int groupId) throws LdapException {
        final ResourceGroup[] groups = getGroups(new int[] { groupId });
        if (null == groups || groups.length == 0) {
            throw new LdapException(Component.RESOURCE,
                Code.RESOURCEGROUP_NOT_FOUND, groupId);
        }
        if (groups.length > 1) {
            throw new LdapException(Component.RESOURCE,
                Code.RESOURCEGROUP_CONFLICT, groupId);
        }
        return groups[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceGroup[] getGroups() throws LdapException {
        final Connection con;
        try {
            con = DBPool.pickup(context);
        } catch (Exception e) {
            throw new LdapException(Component.RESOURCE, Code.NO_CONNECTION, e);
        }
        final String sql = "SELECT id,identifier,displayName,available "
            + "FROM resource_group WHERE cid=?";
        final List<ResourceGroup> groups = new ArrayList<ResourceGroup>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, context.getContextId());
            result = stmt.executeQuery();
            while (result.next()) {
                final ResourceGroup group = new ResourceGroup();
                int pos = 1;
                group.setId(result.getInt(pos++));
                group.setIdentifier(result.getString(pos++));
                group.setDisplayName(result.getString(pos++));
                group.setAvailable(result.getBoolean(pos++));
                group.setMember(getMember(con, group.getId()));
                groups.add(group);
            }
        } catch (SQLException e) {
            throw new LdapException(Component.RESOURCE, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return groups.toArray(new ResourceGroup[groups.size()]);
    }

    /**
     * Reads multiple resource groups from the database.
     * @param groupId array with unique identifier of the resource groups to
     * read.
     * @return an array with the read resource groups.
     * @throws LdapException if an error occurs.
     */
    private ResourceGroup[] getGroups(final int[] groupId)
        throws LdapException {
        if (null == groupId || groupId.length == 0) {
            return new ResourceGroup[0];
        }
        final Connection con;
        try {
            con = DBPool.pickup(context);
        } catch (Exception e) {
            throw new LdapException(Component.RESOURCE, Code.NO_CONNECTION, e);
        }
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT id,identifier,displayName,available ");
        sql.append("FROM resource_group WHERE cid=? AND id IN (");
        for (int i = 0; i < groupId.length; i++) {
            sql.append("?,");
        }
        sql.setCharAt(sql.length() - 1, ')');
        final List<ResourceGroup> groups = new ArrayList<ResourceGroup>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(sql.toString());
            int counter = 1;
            stmt.setLong(counter++, context.getContextId());
            for (int i : groupId) {
                stmt.setInt(counter++, i);
            }
            result = stmt.executeQuery();
            while (result.next()) {
                final ResourceGroup group = new ResourceGroup();
                int pos = 1;
                group.setId(result.getInt(pos++));
                group.setIdentifier(result.getString(pos++));
                group.setDisplayName(result.getString(pos++));
                group.setAvailable(result.getBoolean(pos++));
                group.setMember(getMember(con, group.getId()));
                groups.add(group);
            }
        } catch (SQLException e) {
            throw new LdapException(Component.RESOURCE, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return groups.toArray(new ResourceGroup[groups.size()]);
    }

    /**
     * Reads the member of a resource group.
     * @param con readable database connection.
     * @param groupId unique identifier of the resource group.
     * @return an array with all unique identifier of resource that are member
     * of the resource group.
     * @throws SQLException if a database error occurs.
     */
    private int[] getMember(final Connection con, final int groupId)
        throws SQLException {
        final String sql = "SELECT member FROM resource_group_member "
            + "WHERE cid=? AND id=?";
        PreparedStatement stmt = null;
        ResultSet result = null;
        final List<Integer> member = new ArrayList<Integer>();
        try {
            stmt = con.prepareStatement(sql);
            stmt.setLong(1, context.getContextId());
            stmt.setInt(2, groupId);
            result = stmt.executeQuery();
            while (result.next()) {
                member.add(result.getInt(1));
            }
        } finally {
            closeSQLStuff(result, stmt);
        }
        final int[] retval = new int[member.size()];
        for (int i = 0; i < member.size(); i++) {
            retval[i] = member.get(i);
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getResource(final int resourceId) throws LdapException {
        final Resource[] resources = getResources(new int[] { resourceId });
        if (resources.length == 0) {
            throw new LdapException(Component.RESOURCE,
                Code.RESOURCE_NOT_FOUND, resourceId);
        }
        if (resources.length > 1) {
            throw new LdapException(Component.RESOURCE, Code.RESOURCE_CONFLICT,
                resourceId);
        }
        return resources[0];
    }

    /**
     * Reads multiple resources from the database.
     * @param resourceId array with unique identifier of the resources to read.
     * @return an array with the read resources.
     * @throws LdapException if an error occurs.
     */
    private Resource[] getResources(final int[] resourceId)
        throws LdapException {
        if (null == resourceId || resourceId.length == 0) {
            return new Resource[0];
        }
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (Exception e) {
            throw new LdapException(Component.RESOURCE, Code.NO_CONNECTION, e);
        }
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT id,identifier,displayName,mail,available,");
        sql.append("description,lastModified FROM resource WHERE cid=? ");
        sql.append("AND id IN (");
        for (int i = 0; i < resourceId.length; i++) {
            sql.append("?,");
        }
        sql.setCharAt(sql.length() - 1, ')');
        final List<Resource> resources = new ArrayList<Resource>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(sql.toString());
            int counter = 1;
            stmt.setLong(counter++, context.getContextId());
            for (int i : resourceId) {
                stmt.setInt(counter++, i);
            }
            result = stmt.executeQuery();
            while (result.next()) {
                final Resource res = new Resource();
                int pos = 1;
                res.setIdentifier(result.getInt(pos++));
                pos++; // Skip identifier
                res.setDisplayName(result.getString(pos++));
                res.setMail(result.getString(pos++));
                res.setAvailable(result.getBoolean(pos++));
                res.setDescription(result.getString(pos++));
                res.setLastModified(new Date(result.getLong(pos++)));
                resources.add(res);
            }
        } catch (SQLException e) {
            throw new LdapException(Component.RESOURCE, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return resources.toArray(new Resource[resources.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceGroup[] searchGroups(final String pattern)
        throws LdapException {
        final Connection con;
        try {
            con = DBPool.pickup(context);
        } catch (Exception e) {
            throw new LdapException(Component.RESOURCE, Code.NO_CONNECTION, e);
        }
        final String sql = "SELECT id,identifier,displayName,available "
            + "FROM resource_group WHERE cid=? AND identifier LIKE ?";
        final List<ResourceGroup> groups = new ArrayList<ResourceGroup>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setLong(1, context.getContextId());
            stmt.setString(2, pattern.replace('*', '%'));
            result = stmt.executeQuery();
            while (result.next()) {
                final ResourceGroup group = new ResourceGroup();
                int pos = 1;
                group.setId(result.getInt(pos++));
                group.setIdentifier(result.getString(pos++));
                group.setDisplayName(result.getString(pos++));
                group.setAvailable(result.getBoolean(pos++));
                group.setMember(getMember(con, group.getId()));
                groups.add(group);
            }
        } catch (SQLException e) {
            throw new LdapException(Component.RESOURCE, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return groups.toArray(new ResourceGroup[groups.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource[] searchResources(final String pattern)
        throws LdapException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (Exception e) {
            throw new LdapException(Component.RESOURCE, Code.NO_CONNECTION, e);
        }
        final String sql = "SELECT id,identifier,displayName,mail,"
            + "available,description,lastModified FROM resource WHERE cid=? "
            + "AND identifier LIKE ?";
        final List<Resource> resources = new ArrayList<Resource>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setLong(1, context.getContextId());
            stmt.setString(2, LdapUtility.prepareSearchPattern(pattern));
            result = stmt.executeQuery();
            while (result.next()) {
                final Resource res = new Resource();
                int pos = 1;
                res.setIdentifier(result.getInt(pos++));
                pos++; // skip identifier string
                res.setDisplayName(result.getString(pos++));
                res.setMail(result.getString(pos++));
                res.setAvailable(result.getBoolean(pos++));
                res.setDescription(result.getString(pos++));
                res.setLastModified(new Date(result.getLong(pos++)));
                resources.add(res);
            }
        } catch (SQLException e) {
            throw new LdapException(Component.RESOURCE, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return resources.toArray(new Resource[resources.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource[] listModified(final Date modifiedSince)
        throws LdapException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (Exception e) {
            throw new LdapException(Component.RESOURCE, Code.NO_CONNECTION, e);
        }
        final String sql = "SELECT id,identifier,displayName,mail,"
            + "available,description,lastModified FROM resource WHERE cid=? "
            + "AND lastModified>=?";
        final List<Resource> resources = new ArrayList<Resource>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setLong(1, context.getContextId());
            stmt.setLong(2, modifiedSince.getTime());
            result = stmt.executeQuery();
            while (result.next()) {
                final Resource res = new Resource();
                int pos = 1;
                res.setIdentifier(result.getInt(pos++));
                pos++; // skip identifier string
                res.setDisplayName(result.getString(pos++));
                res.setMail(result.getString(pos++));
                res.setAvailable(result.getBoolean(pos++));
                res.setDescription(result.getString(pos++));
                res.setLastModified(new Date(result.getLong(pos++)));
                resources.add(res);
            }
        } catch (SQLException e) {
            throw new LdapException(Component.RESOURCE, Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return resources.toArray(new Resource[resources.size()]);
    }
}
