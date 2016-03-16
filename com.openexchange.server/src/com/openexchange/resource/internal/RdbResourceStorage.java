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

package com.openexchange.resource.internal;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.groupware.ldap.LdapUtility;
import com.openexchange.java.Strings;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceExceptionCode;
import com.openexchange.resource.ResourceGroup;
import com.openexchange.resource.storage.ResourceStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.tools.sql.DBUtils;

/**
 * This class implements the resource storage using a relational database.
 */
public class RdbResourceStorage extends ResourceStorage {

    private static final String RPL_TABLE = "#TABLE#";

    private static final String TABLE_ACTIVE = "resource";

    private static final String TABLE_DELETED = "del_resource";

    /**
     * Default constructor.
     *
     * @param context Context.
     */
    RdbResourceStorage() {
        super();
    }

    @Override
    public ResourceGroup getGroup(final int groupId, final Context context) throws OXException {
        final ResourceGroup[] groups = getGroups(new int[] { groupId }, context);
        if (null == groups || groups.length == 0) {
            throw LdapExceptionCode.RESOURCEGROUP_NOT_FOUND.create(Integer.valueOf(groupId)).setPrefix("RES");
        }
        if (groups.length > 1) {
            throw LdapExceptionCode.RESOURCEGROUP_CONFLICT.create(Integer.valueOf(groupId)).setPrefix("RES");
        }
        return groups[0];
    }

    private static final String SQL_SELECT_GROUP = "SELECT id,identifier,displayName,available " + "FROM resource_group WHERE cid = ?";

    @Override
    public ResourceGroup[] getGroups(final Context context) throws OXException {
        final Connection con;
        try {
            con = DBPool.pickup(context);
        } catch (final Exception e) {
            throw LdapExceptionCode.NO_CONNECTION.create(e).setPrefix("RES");
        }
        final List<ResourceGroup> groups = new ArrayList<ResourceGroup>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_GROUP);
            stmt.setInt(1, context.getContextId());
            result = stmt.executeQuery();
            while (result.next()) {
                final ResourceGroup group = new ResourceGroup();
                int pos = 1;
                group.setId(result.getInt(pos++));
                group.setIdentifier(result.getString(pos++));
                group.setDisplayName(result.getString(pos++));
                group.setAvailable(result.getBoolean(pos++));
                group.setMember(getMember(con, group.getId(), context));
                groups.add(group);
            }
        } catch (final SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("RES");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return groups.toArray(new ResourceGroup[groups.size()]);
    }

    private static final String SQL_SELECT_GROUP2 = "SELECT id,identifier,displayName,available FROM resource_group WHERE cid=? AND id IN #IDS#";

    /**
     * Reads multiple resource groups from the database.
     *
     * @param groupId array with unique identifier of the resource groups to read.
     * @return an array with the read resource groups.
     * @throws OXException if an error occurs.
     */
    private ResourceGroup[] getGroups(final int[] groupId, final Context context) throws OXException {
        if (null == groupId || groupId.length == 0) {
            return new ResourceGroup[0];
        }
        final Connection con;
        try {
            con = DBPool.pickup(context);
        } catch (final Exception e) {
            throw LdapExceptionCode.NO_CONNECTION.create(e).setPrefix("RES");
        }
        final StringBuilder ids = new StringBuilder(16);
        ids.append('(').append(groupId[0]);
        for (int i = 1; i < groupId.length; i++) {
            ids.append(',').append(groupId[i]);
        }
        ids.append(')');
        final List<ResourceGroup> groups = new ArrayList<ResourceGroup>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_GROUP2.replaceFirst("#IDS#", ids.toString()));
            stmt.setLong(1, context.getContextId());
            result = stmt.executeQuery();
            while (result.next()) {
                final ResourceGroup group = new ResourceGroup();
                int pos = 1;
                group.setId(result.getInt(pos++));
                group.setIdentifier(result.getString(pos++));
                group.setDisplayName(result.getString(pos++));
                group.setAvailable(result.getBoolean(pos++));
                group.setMember(getMember(con, group.getId(), context));
                groups.add(group);
            }
        } catch (final SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("RES");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return groups.toArray(new ResourceGroup[groups.size()]);
    }

    private static final String SQL_SELECT_GROUP4 = "SELECT member FROM resource_group_member WHERE cid = ? AND id = ?";

    /**
     * Reads the member of a resource group.
     *
     * @param con readable database connection.
     * @param groupId unique identifier of the resource group.
     * @return an array with all unique identifier of resource that are member of the resource group.
     * @throws SQLException if a database error occurs.
     */
    private int[] getMember(final Connection con, final int groupId, final Context context) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        final List<Integer> member = new ArrayList<Integer>();
        try {
            stmt = con.prepareStatement(SQL_SELECT_GROUP4);
            stmt.setLong(1, context.getContextId());
            stmt.setInt(2, groupId);
            result = stmt.executeQuery();
            while (result.next()) {
                member.add(Integer.valueOf(result.getInt(1)));
            }
        } finally {
            closeSQLStuff(result, stmt);
        }
        final int[] retval = new int[member.size()];
        for (int i = 0; i < member.size(); i++) {
            retval[i] = member.get(i).intValue();
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getResource(final int resourceId, final Context context) throws OXException {
        final Resource[] resources = getResources(new int[] { resourceId }, context);
        if (resources.length == 0) {
            throw LdapExceptionCode.RESOURCE_NOT_FOUND.create(Integer.valueOf(resourceId)).setPrefix("RES");
        }
        if (resources.length > 1) {
            throw LdapExceptionCode.RESOURCE_CONFLICT.create(Integer.valueOf(resourceId)).setPrefix("RES");
        }
        return resources[0];
    }

    private static final String SQL_SELECT_RESOURCE3 = "SELECT id,identifier,displayName,mail,available,description,lastModified " + "FROM resource WHERE cid = ? AND id IN #IDS#";

    /**
     * Reads multiple resources from the database.
     *
     * @param resourceId array with unique identifier of the resources to read.
     * @return an array with the read resources.
     * @throws OXException if an error occurs.
     */
    private Resource[] getResources(final int[] resourceId, final Context context) throws OXException {
        if (null == resourceId || resourceId.length == 0) {
            return new Resource[0];
        }
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (final Exception e) {
            throw LdapExceptionCode.NO_CONNECTION.create(e).setPrefix("RES");
        }
        final StringBuilder ids = new StringBuilder(16);
        ids.append('(').append(resourceId[0]);
        for (int i = 1; i < resourceId.length; i++) {
            ids.append(',').append(resourceId[i]);
        }
        ids.append(')');
        final List<Resource> resources = new ArrayList<Resource>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_RESOURCE3.replaceFirst("#IDS#", ids.toString()));
            stmt.setLong(1, context.getContextId()); // cid
            result = stmt.executeQuery();
            while (result.next()) {
                resources.add(createResourceFromEntry(result));
            }
        } catch (final SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("RES");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return resources.toArray(new Resource[resources.size()]);
    }

    private static final String SQL_SELECT_GROUP3 = "SELECT id,identifier,displayName,available " + "FROM resource_group WHERE cid=? AND identifier LIKE ?";

    @Override
    public ResourceGroup[] searchGroups(final String pattern, final Context context) throws OXException {
        final Connection con;
        try {
            con = DBPool.pickup(context);
        } catch (final Exception e) {
            throw LdapExceptionCode.NO_CONNECTION.create(e).setPrefix("RES");
        }
        final List<ResourceGroup> groups = new ArrayList<ResourceGroup>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_GROUP3);
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
                group.setMember(getMember(con, group.getId(), context));
                groups.add(group);
            }
        } catch (final SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("RES");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return groups.toArray(new ResourceGroup[groups.size()]);
    }

    private static final String SQL_SELECT_RESOURCE2 = "SELECT id,identifier,displayName,mail,available,description,lastModified FROM resource WHERE cid = ? AND (identifier LIKE ? OR displayName LIKE ?)";

    @Override
    public Resource[] searchResources(final String pattern, final Context context) throws OXException {
        if (Strings.isEmpty(pattern)) {
            return new Resource[0];
        }
        final Connection con;
        try {
            con = DBPool.pickup(context);
        } catch (final Exception e) {
            throw LdapExceptionCode.NO_CONNECTION.create(e).setPrefix("RES");
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_RESOURCE2);
            stmt.setLong(1, context.getContextId());
            stmt.setString(2, LdapUtility.prepareSearchPattern(pattern));
            stmt.setString(3, LdapUtility.prepareSearchPattern(pattern));
            result = stmt.executeQuery();
            final List<Resource> resources = new ArrayList<Resource>();
            while (result.next()) {
                resources.add(createResourceFromEntry(result));
            }
            return resources.toArray(new Resource[resources.size()]);
        } catch (final SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("RES");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
    }

    private static final String SQL_SELECT_RESOURCE4 = "SELECT id,identifier,displayName,mail,available,description,lastModified FROM resource WHERE cid = ? AND mail LIKE ?";

    @Override
    public Resource[] searchResourcesByMail(final String pattern, final Context context) throws OXException {
        if (Strings.isEmpty(pattern)) {
            return new Resource[0];
        }
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (final Exception e) {
            throw LdapExceptionCode.NO_CONNECTION.create(e).setPrefix("RES");
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_RESOURCE4);
            stmt.setLong(1, context.getContextId());
            stmt.setString(2, LdapUtility.prepareSearchPattern(pattern));
            result = stmt.executeQuery();
            final List<Resource> resources = new ArrayList<Resource>();
            while (result.next()) {
                resources.add(createResourceFromEntry(result));
            }
            return resources.toArray(new Resource[resources.size()]);
        } catch (final SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("RES");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
    }

    private static final String SQL_SELECT_RESOURCE = "SELECT id,identifier,displayName,mail,available,description,lastModified " + "FROM resource WHERE cid = ? AND lastModified > ?";
    private static final String SQL_SELECT_DELETED_RESOURCE = "SELECT id,identifier,displayName,mail,available,description,lastModified " + "FROM del_resource WHERE cid = ? AND lastModified > ?";

    @Override
    public Resource[] listModified(final Date modifiedSince, final Context context) throws OXException {
        return listModifiedOrDeleted(modifiedSince, context, SQL_SELECT_RESOURCE);
    }

    @Override
    public Resource[] listDeleted(final Date modifiedSince, final Context context) throws OXException {
        return listModifiedOrDeleted(modifiedSince, context, SQL_SELECT_DELETED_RESOURCE);
    }

    private Resource[] listModifiedOrDeleted(final Date modifiedSince, final Context context, final String statement) throws OXException {
        Connection con = null;
        try {
            con = DBPool.pickup(context);
        } catch (final Exception e) {
            throw LdapExceptionCode.NO_CONNECTION.create(e).setPrefix("RES");
        }
        final List<Resource> resources = new ArrayList<Resource>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(statement);
            stmt.setLong(1, context.getContextId());
            stmt.setLong(2, modifiedSince.getTime());
            result = stmt.executeQuery();
            while (result.next()) {
                resources.add(createResourceFromEntry(result));
            }
        } catch (final SQLException e) {
            throw LdapExceptionCode.SQL_ERROR.create(e, e.getMessage()).setPrefix("RES");
        } finally {
            closeSQLStuff(result, stmt);
            DBPool.closeReaderSilent(context, con);
        }
        return resources.toArray(new Resource[resources.size()]);
    }

    /**
     * Creates a newly allocated {@link Resource resource} from current result set's entry.
     *
     * @param result The result set with its cursor properly set
     * @return A newly allocated {@link Resource resource} from current result set's entry
     * @throws SQLException If an SQL error occurs
     */
    private Resource createResourceFromEntry(final ResultSet result) throws SQLException {
        final Resource res = new Resource();
        int pos = 1;
        res.setIdentifier(result.getInt(pos++));// id
        res.setSimpleName(result.getString(pos++));// identifier
        res.setDisplayName(result.getString(pos++));// displayName
        {
            final String mail = result.getString(pos++); // mail
            if (result.wasNull()) {
                res.setMail(null);
            } else {
                res.setMail(mail);
            }
        }
        res.setAvailable(result.getBoolean(pos++));// available
        {
            final String desc = result.getString(pos++);// description
            if (result.wasNull()) {
                res.setDescription(null);
            } else {
                res.setDescription(desc);
            }
        }
        res.setLastModified(result.getLong(pos++));// lastModified
        return res;
    }

    private static final String SQL_INSERT_RESOURCE = "INSERT INTO " + RPL_TABLE + " (cid,id,identifier,displayName,mail,available,description,lastModified) " + "VALUES (?,?,?,?,?,?,?,?)";

    @Override
    public void insertResource(final Context ctx, final Connection con, final Resource resource, final StorageType type) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_INSERT_RESOURCE.replaceFirst(
                RPL_TABLE,
                StorageType.ACTIVE.equals(type) ? TABLE_ACTIVE : TABLE_DELETED));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId()); // cid
            stmt.setInt(pos++, resource.getIdentifier()); // id
            stmt.setString(pos++, resource.getSimpleName()); // identifier
            stmt.setString(pos++, resource.getDisplayName()); // displayName
            if (resource.getMail() == null) {
                stmt.setNull(pos++, Types.VARCHAR); // mail
            } else {
                stmt.setString(pos++, resource.getMail()); // mail
            }
            stmt.setBoolean(pos++, resource.isAvailable()); // available
            if (resource.getDescription() == null) {
                stmt.setNull(pos++, Types.VARCHAR); // description
            } else {
                stmt.setString(pos++, resource.getDescription()); // description
            }
            final long lastModified = System.currentTimeMillis();
            stmt.setLong(pos++, lastModified);// lastModified
            stmt.executeUpdate();
            resource.setLastModified(lastModified);
        } catch (final SQLException e) {
            throw ResourceExceptionCode.SQL_ERROR.create(e);
        } finally {
            DBUtils.closeSQLStuff(null, stmt);
        }
    }

    private static final String SQL_UPDATE_RESOURCE = "UPDATE resource SET identifier = ?, displayName = ?, mail = ?, " + "available = ?, description = ?, lastModified = ? WHERE cid = ? AND id = ?";

    @Override
    public void updateResource(final Context ctx, final Connection con, final Resource resource) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_UPDATE_RESOURCE);
            int pos = 1;
            stmt.setString(pos++, resource.getSimpleName()); // identifier
            stmt.setString(pos++, resource.getDisplayName()); // displayName
            if (resource.getMail() == null) {
                stmt.setNull(pos++, Types.VARCHAR); // mail
            } else {
                stmt.setString(pos++, resource.getMail()); // mail
            }
            stmt.setBoolean(pos++, resource.isAvailable()); // available
            if (resource.getDescription() == null) {
                stmt.setNull(pos++, Types.VARCHAR); // description
            } else {
                stmt.setString(pos++, resource.getDescription()); // description
            }
            final long lastModified = System.currentTimeMillis();
            stmt.setLong(pos++, lastModified);// lastModified
            stmt.setInt(pos++, ctx.getContextId()); // cid
            stmt.setInt(pos++, resource.getIdentifier()); // id
            stmt.executeUpdate();
            resource.setLastModified(lastModified);
        } catch (final SQLException e) {
            throw ResourceExceptionCode.SQL_ERROR.create(e);
        } finally {
            DBUtils.closeSQLStuff(null, stmt);
        }
    }

    private static final String SQL_DELETE_RESOURCE = "DELETE FROM resource WHERE cid = ? AND id = ?";

    @Override
    public void deleteResourceById(final Context ctx, final Connection con, final int resourceId) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_DELETE_RESOURCE);
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId()); // cid
            stmt.setInt(pos++, resourceId); // id
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw ResourceExceptionCode.SQL_ERROR.create(e);
        } finally {
            DBUtils.closeSQLStuff(null, stmt);
        }
    }
}
