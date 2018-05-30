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

package com.openexchange.admin.storage.mysqlStorage;

import static com.openexchange.admin.storage.mysqlStorage.AdminMySQLStorageUtil.leaseConnectionForContext;
import static com.openexchange.admin.storage.mysqlStorage.AdminMySQLStorageUtil.releaseWriteContextConnection;
import static com.openexchange.admin.storage.mysqlStorage.AdminMySQLStorageUtil.releaseWriteContextConnectionAfterReading;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.sqlStorage.OXResourceSQLStorage;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFinishedListenerRegistry;
import com.openexchange.groupware.delete.DeleteRegistry;
import com.openexchange.groupware.impl.IDGenerator;

/**
 * @author d7
 * @author cutmasta
 */
public class OXResourceMySQLStorage extends OXResourceSQLStorage implements OXMySQLDefaultValues {

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXResourceMySQLStorage.class);

    private final AdminCache cache;

    /**
     * Initialises a new {@link OXResourceMySQLStorage}.
     */
    public OXResourceMySQLStorage() {
        super();
        this.cache = ClientAdminThread.cache;
    }

    @Override
    public void change(Context ctx, Resource res) throws StorageException {
        int contextId = ctx.getId().intValue();
        int resourceId = res.getId().intValue();
        Connection con = null;
        PreparedStatement editres = null;
        boolean rollback = false;
        try {

            con = leaseConnectionForContext(contextId, cache);
            con.setAutoCommit(false);
            rollback = true;

            int edited_the_resource = 0;

            // update status of resource availability
            if (null != res.getAvailable()) {
                editres = con.prepareStatement("UPDATE resource SET available = ? WHERE cid = ? AND id = ?");
                editres.setBoolean(1, res.getAvailable().booleanValue());
                editres.setInt(2, contextId);
                editres.setInt(3, resourceId);
                editres.executeUpdate();
                editres.close();
                edited_the_resource++;
            }

            // update description of resource
            if (null == res.getDescription() && res.isDescriptionset()) {
                editres = con.prepareStatement("UPDATE resource SET description = ? WHERE cid = ? AND id = ?");
                editres.setNull(1, java.sql.Types.VARCHAR);
                editres.setInt(2, contextId);
                editres.setInt(3, resourceId);
                editres.executeUpdate();
                editres.close();
                edited_the_resource++;
            } else if (null != res.getDescription()) {
                editres = con.prepareStatement("UPDATE resource SET description = ? WHERE cid = ? AND id = ?");
                editres.setString(1, res.getDescription());
                editres.setInt(2, contextId);
                editres.setInt(3, resourceId);
                editres.executeUpdate();
                editres.close();
                edited_the_resource++;
            }

            // update mail of resource
            String mail = res.getEmail();
            if (null != mail) {
                editres = con.prepareStatement("UPDATE resource SET mail = ? WHERE cid = ? AND id = ?");
                editres.setString(1, mail);
                editres.setInt(2, contextId);
                editres.setInt(3, resourceId);
                editres.executeUpdate();
                editres.close();
                edited_the_resource++;
            }

            // Update displayName of resource
            String displayname = res.getDisplayname();
            if (null != displayname) {
                editres = con.prepareStatement("UPDATE resource SET displayName = ? WHERE cid = ? AND id = ?");
                editres.setString(1, displayname);
                editres.setInt(2, contextId);
                editres.setInt(3, resourceId);
                editres.executeUpdate();
                editres.close();
                edited_the_resource++;
            }

            // Check for possibly updating the name of resource
            String resourceName = res.getName();
            if (null == resourceName) {
                // Load the name of the resource for logging purpose
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = con.prepareStatement("SELECT identifier FROM resource WHERE cid = ? AND id = ?");
                    stmt.setInt(1, contextId);
                    stmt.setInt(2, resourceId);
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        resourceName = rs.getString(1);
                    }
                } finally {
                    Databases.closeSQLStuff(rs, stmt);
                }
            } else {
                // Change the name of the resource
                editres = con.prepareStatement("UPDATE resource SET identifier = ? WHERE cid = ? AND id = ?");
                editres.setString(1, resourceName);
                editres.setInt(2, contextId);
                editres.setInt(3, resourceId);
                editres.executeUpdate();
                editres.close();
                edited_the_resource++;
            }

            // Update last-modified time stamp if any modification performed
            if (edited_the_resource > 0) {
                changeLastModified(resourceId, ctx, con);
            }

            con.commit();
            rollback = false;

            log.info("Resource {} changed!", resourceName == null ? "" : resourceName);
        } catch (DataTruncation dt) {
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.closeSQLStuff(editres);
            releaseWriteContextConnection(con, ctx, cache);
        }
    }

    @Override
    public int create(Context ctx, Resource res) throws StorageException {
        int contextId = ctx.getId();
        Connection con = null;
        PreparedStatement prep_insert = null;
        boolean rollback = false;
        try {

            con = leaseConnectionForContext(contextId, cache);
            con.setAutoCommit(false);
            rollback = true;

            String identifier = res.getName();
            String displayName = res.getDisplayname();

            int available;
            if (null != res.getAvailable()) {
                available = res.getAvailable() ? 1 : 0;
            } else {
                // This is the default, so if this attribute of the object has never been
                // touched, we set this to true;
                available = 1;
            }

            String description = res.getDescription();
            String mail = res.getEmail();

            int resID = IDGenerator.getId(contextId, com.openexchange.groupware.Types.PRINCIPAL, con);

            prep_insert = con.prepareStatement("INSERT INTO resource (cid,id,identifier,displayName,available,description,lastModified,mail)VALUES (?,?,?,?,?,?,?,?);");
            prep_insert.setInt(1, contextId);
            prep_insert.setInt(2, resID);
            if (identifier != null) {
                prep_insert.setString(3, identifier);
            } else {
                prep_insert.setNull(3, Types.VARCHAR);
            }
            if (displayName != null) {
                prep_insert.setString(4, displayName);
            } else {
                prep_insert.setNull(4, Types.VARCHAR);
            }
            prep_insert.setInt(5, available);
            if (description != null) {
                prep_insert.setString(6, description);
            } else {
                prep_insert.setNull(6, Types.VARCHAR);
            }
            prep_insert.setLong(7, System.currentTimeMillis());
            if (mail != null) {
                prep_insert.setString(8, mail);
            } else {
                prep_insert.setNull(8, Types.VARCHAR);
            }

            prep_insert.executeUpdate();

            con.commit();
            rollback = false;
            log.info("Resource {} created!", resID);
            return resID;
        } catch (DataTruncation dt) {
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.closeSQLStuff(prep_insert);
            releaseWriteContextConnection(con, ctx, cache);
        }
    }

    @Override
    public void delete(Context ctx, int resource_id) throws StorageException {
        int contextId = ctx.getId();
        Connection con = null;
        PreparedStatement prep_del = null;
        boolean rollback = false;
        try {
            con = leaseConnectionForContext(contextId, cache);
            con.setAutoCommit(false);
            rollback = true;

            DeleteEvent delev = DeleteEvent.createDeleteEventForResourceDeletion(this, resource_id, ContextStorage.getInstance().getContext(contextId));
            DeleteRegistry.getInstance().fireDeleteEvent(delev, con, con);

            createRecoveryData(resource_id, ctx, con);

            prep_del = con.prepareStatement("DELETE FROM resource WHERE cid=? AND id=?;");
            prep_del.setInt(1, contextId);
            prep_del.setInt(2, resource_id);
            prep_del.executeUpdate();

            con.commit();
            rollback = false;

            try {
                DeleteFinishedListenerRegistry.getInstance().fireDeleteEvent(delev);
            } catch (Exception e) {
                log.warn("Failed to trigger delete finished listeners", e);
            }

            log.info("Resource {} deleted!", resource_id);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } catch (OXException e) {
            log.error("Internal Error", e);
            throw new StorageException(e.toString());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.closeSQLStuff(prep_del);
            releaseWriteContextConnection(con, ctx, cache);
        }
    }

    @Override
    public void delete(Context ctx, Resource resource) throws StorageException {
        int resourceId = resource.getId().intValue();
        int contextId = ctx.getId();
        Connection con = null;
        PreparedStatement prep_del = null;
        boolean rollback = false;
        try {
            con = leaseConnectionForContext(contextId, cache);
            con.setAutoCommit(false);
            rollback = true;

            DeleteEvent delev = DeleteEvent.createDeleteEventForResourceDeletion(this, resourceId, ContextStorage.getInstance().getContext(contextId));
            DeleteRegistry.getInstance().fireDeleteEvent(delev, con, con);

            createRecoveryData(resourceId, ctx, con);

            prep_del = con.prepareStatement("DELETE FROM resource WHERE cid=? AND id=?;");
            prep_del.setInt(1, contextId);
            prep_del.setInt(2, resourceId);
            prep_del.executeUpdate();

            con.commit();
            rollback = false;

            try {
                DeleteFinishedListenerRegistry.getInstance().fireDeleteEvent(delev);
            } catch (Exception e) {
                log.warn("Failed to trigger delete finished listeners", e);
            }

            log.info("Resource {} deleted!", resourceId);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } catch (OXException e) {
            log.error("Context Error", e);
            throw new StorageException(e.toString());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.closeSQLStuff(prep_del);
            releaseWriteContextConnection(con, ctx, cache);
        }
    }

    @Override
    public Resource getData(Context ctx, Resource resource) throws StorageException {
        int contextId = ctx.getId();
        Connection con = null;
        PreparedStatement prep_list = null;
        try {

            con = leaseConnectionForContext(contextId, cache);

            prep_list = con.prepareStatement("SELECT cid,id,identifier,displayName,available,description,mail FROM resource WHERE resource.cid = ? AND resource.id = ?");
            prep_list.setInt(1, contextId);
            prep_list.setInt(2, resource.getId());
            ResultSet rs = prep_list.executeQuery();

            if (!rs.next()) {
                throw new StorageException("No such resource");
            }
            int id = rs.getInt("id");
            String ident = rs.getString("identifier");
            String mail = rs.getString("mail");
            String disp = rs.getString("displayName");
            Boolean aval = rs.getBoolean("available");
            String desc = rs.getString("description");

            Resource retval = (Resource) resource.clone();

            retval.setId(id);
            if (null != mail) {
                retval.setEmail(mail);
            }
            if (null != disp) {
                retval.setDisplayname(disp);
            }

            if (null != ident) {
                retval.setName(ident);
            }

            if (null != desc) {
                retval.setDescription(desc);
            }

            if (null != aval) {
                retval.setAvailable(aval);
            }
            return retval;

        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } catch (CloneNotSupportedException e) {
            log.error("", e);
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(prep_list);
            releaseWriteContextConnectionAfterReading(con, contextId, cache);
        }
    }

    @Override
    public Resource[] list(Context ctx, String pattern) throws StorageException {
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement prep_list = null;
        String patterntemp = pattern.replace('*', '%');
        int contextId = ctx.getId();
        try {
            ArrayList<Resource> list = new ArrayList<Resource>();
            con = leaseConnectionForContext(contextId, cache);

            prep_list = con.prepareStatement("SELECT resource.mail,resource.cid,resource.id,resource.identifier,resource.displayName,resource.available,resource.description FROM resource WHERE resource.cid = ? AND (resource.identifier like ? OR resource.displayName = ?)");
            prep_list.setInt(1, contextId);
            prep_list.setString(2, patterntemp);
            prep_list.setString(3, patterntemp);
            rs = prep_list.executeQuery();
            while (rs.next()) {
                Resource res = new Resource();

                int id = rs.getInt("id");
                String ident = rs.getString("identifier");
                String mail = rs.getString("mail");
                String disp = rs.getString("displayName");
                Boolean aval = rs.getBoolean("available");
                String desc = rs.getString("description");

                res.setId(id);
                if (null != mail) {
                    res.setEmail(mail);
                }
                if (null != disp) {
                    res.setDisplayname(disp);
                }

                if (null != ident) {
                    res.setName(ident);
                }

                if (null != desc) {
                    res.setDescription(desc);
                }

                if (null != aval) {
                    res.setAvailable(aval);
                }
                list.add(res);
            }

            Resource[] retval = new Resource[list.size()];
            for (int i = 0; i < list.size(); i++) {
                retval[i] = list.get(i);
            }
            return retval;
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            Databases.closeSQLStuff(rs, prep_list);
            releaseWriteContextConnectionAfterReading(con, contextId, cache);
        }
    }

    @Override
    public void changeLastModified(int resource_id, Context ctx, Connection write_ox_con) throws StorageException {
        PreparedStatement prep_edit_user = null;
        try {
            prep_edit_user = write_ox_con.prepareStatement("UPDATE resource SET lastModified=? WHERE cid=? AND id=?");
            prep_edit_user.setLong(1, System.currentTimeMillis());
            prep_edit_user.setInt(2, ctx.getId());
            prep_edit_user.setInt(3, resource_id);
            prep_edit_user.executeUpdate();
            prep_edit_user.close();
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            Databases.closeSQLStuff(prep_edit_user);
        }
    }

    @Override
    public void createRecoveryData(int resource_id, Context ctx, Connection con) throws StorageException {
        // insert into del_resource table
        int context_id = ctx.getId();
        PreparedStatement del_st = null;
        try {
            del_st = con.prepareStatement("INSERT into del_resource (id,cid,lastModified) VALUES (?,?,?)");
            del_st.setInt(1, resource_id);
            del_st.setInt(2, context_id);
            del_st.setLong(3, System.currentTimeMillis());
            del_st.executeUpdate();
        } catch (DataTruncation dt) {
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            Databases.closeSQLStuff(del_st);
        }
    }

    @Override
    public void deleteAllRecoveryData(Context ctx, Connection con) throws StorageException {
        // delete from del_resource table
        int context_id = ctx.getId();
        PreparedStatement del_st = null;
        try {
            del_st = con.prepareStatement("DELETE from del_resource WHERE cid = ?");
            del_st.setInt(1, context_id);
            del_st.executeUpdate();
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            Databases.closeSQLStuff(del_st);
        }
    }

    @Override
    public void deleteRecoveryData(int resource_id, Context ctx, Connection con) throws StorageException {
        // delete from del_resource table
        int context_id = ctx.getId();
        PreparedStatement del_st = null;
        try {
            del_st = con.prepareStatement("DELETE from del_resource WHERE id = ? AND cid = ?");
            del_st.setInt(1, resource_id);
            del_st.setInt(2, context_id);
            del_st.executeUpdate();
        } catch (SQLException e) {
            log.error("SQL Error", e);
            throw new StorageException(e.toString());
        } finally {
            Databases.closeSQLStuff(del_st);
        }
    }
}
