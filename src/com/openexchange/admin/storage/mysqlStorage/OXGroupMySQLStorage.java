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
package com.openexchange.admin.storage.mysqlStorage;

import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.sqlStorage.OXGroupSQLStorage;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.tools.oxfolder.OXFolderAdminHelper;

/**
 * @author d7
 * 
 */
public class OXGroupMySQLStorage extends OXGroupSQLStorage implements OXMySQLDefaultValues {

    private final static Log log = LogFactory.getLog(OXGroupMySQLStorage.class);

    public OXGroupMySQLStorage() {
    }

    private void changeLastModifiedOnGroup(final int context_id, final int group_id, final Connection write_ox_con) throws SQLException {
        PreparedStatement prep_edit_group = null;
        try {
            prep_edit_group = write_ox_con.prepareStatement("UPDATE groups SET lastModified=? WHERE cid=? AND id=?;");
            prep_edit_group.setLong(1, System.currentTimeMillis());
            prep_edit_group.setInt(2, context_id);
            prep_edit_group.setInt(3, group_id);
            prep_edit_group.executeUpdate();
        } finally {
            closePreparedStatement(prep_edit_group);
        }
    }

    private void closePreparedStatement(final PreparedStatement prep_edit_group) {
        try {
            if (prep_edit_group != null) {
                prep_edit_group.close();
            }
        } catch (final SQLException ee) {
            log.error("SQL Error", ee);
        }
    }

    @Override
    public void addMember(final Context ctx, final int grp_id, final User[] members) throws StorageException {
        Connection con = null;
        PreparedStatement prep_add_member = null;
        final int context_id = ctx.getId().intValue();
        try {
            con = cache.getConnectionForContext(context_id);
            con.setAutoCommit(false);

            for (final User member : members) {
                prep_add_member = con.prepareStatement("INSERT INTO groups_member VALUES (?,?,?);");
                prep_add_member.setInt(1, context_id);
                prep_add_member.setInt(2, grp_id);
                prep_add_member.setInt(3, member.getId());
                prep_add_member.executeUpdate();
                prep_add_member.close();
            }

            // set last modified on group
            changeLastModifiedOnGroup(context_id, grp_id, con);
            OXUserMySQLStorage oxu = new OXUserMySQLStorage();
            for (final User member : members) {
                oxu.changeLastModified(member.getId(), ctx, con);
            }
            
            // let the groupware api know that the group has changed
            OXFolderAdminHelper.propagateGroupModification(grp_id, con, con, context_id); 
            
            con.commit();
        } catch (final DataTruncation dt) {
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException sql) {
            log.error("SQL Error", sql);
            doRollback(con);
            throw new StorageException(sql);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            doRollback(con);
            throw new StorageException(e);
        } finally {
            closePreparedStatement(prep_add_member);
            pushConnectionforContext(con, context_id);
        }
    }

    @Override
    public void removeMember(final Context ctx, final int grp_id, final User[] members) throws StorageException {
        Connection con = null;
        PreparedStatement prep_del_member = null;
        final int context_id = ctx.getId().intValue();
        try {
            con = cache.getConnectionForContext(context_id);
            con.setAutoCommit(false);

            for (final User member : members) {
                prep_del_member = con.prepareStatement("DELETE FROM groups_member WHERE cid=? AND id=? AND member=?;");
                prep_del_member.setInt(1, context_id);
                prep_del_member.setInt(2, grp_id);
                prep_del_member.setInt(3, member.getId());
                prep_del_member.executeUpdate();
                prep_del_member.close();
            }

            // set last modified
            changeLastModifiedOnGroup(context_id, grp_id, con);
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            OXUserMySQLStorage oxu = new OXUserMySQLStorage();
            for (final User member : members) {
                if (tool.existsUser(ctx, member.getId())) {
                    // update last modified on user
                    oxu.changeLastModified(member.getId(), ctx, con);
                }
            }
            
            // let the groupware api know that the group has changed
            OXFolderAdminHelper.propagateGroupModification(grp_id, con, con, context_id); 
            
            con.commit();
        } catch (final SQLException sql) {
            log.error("SQL Error", sql);
            doRollback(con);
            throw new StorageException(sql);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            doRollback(con);
            throw new StorageException(e);
        } finally {
            closePreparedStatement(prep_del_member);
            pushConnectionforContext(con, context_id);
        }
    }

    @Override
    public void change(final Context ctx, final Group grp) throws StorageException {
        Connection con = null;
        PreparedStatement prep_edit_group = null;
        final int context_id = ctx.getId();
        try {
            con = cache.getConnectionForContext(context_id);
            con.setAutoCommit(false);
            final int group_id = grp.getId();
            final String identifier = grp.getName();
            if (null != identifier) {
                prep_edit_group = con.prepareStatement("UPDATE groups SET identifier=? WHERE cid=? AND id = ?");
                prep_edit_group.setString(1, identifier);
                prep_edit_group.setInt(2, context_id);
                prep_edit_group.setInt(3, group_id);
                prep_edit_group.executeUpdate();
                prep_edit_group.close();
            }

            final String displayName = grp.getDisplayname();
            if (null != displayName) {
                prep_edit_group = con.prepareStatement("UPDATE groups SET displayName=? WHERE cid=? AND id = ?");
                prep_edit_group.setString(1, displayName);
                prep_edit_group.setInt(2, context_id);
                prep_edit_group.setInt(3, group_id);
                prep_edit_group.executeUpdate();
                prep_edit_group.close();
            }
            // check for members and add them after deleting old ones, cause we overwrite the members in this method (change)
            final Integer[] members = grp.getMembers();
            if (members != null) {
                // first delete all old members
                prep_edit_group = con.prepareStatement("DELETE FROM groups_member WHERE cid = ? AND id = ?");
                prep_edit_group.setInt(1, context_id);
                prep_edit_group.setInt(2, group_id);
                prep_edit_group.executeUpdate();
                prep_edit_group.close();

                Integer[] as = members;
                for (final Integer member_id : as) {
                    prep_edit_group = con.prepareStatement("INSERT INTO groups_member (cid,id,member) VALUES (?,?,?)");
                    prep_edit_group.setInt(1, context_id);
                    prep_edit_group.setInt(2, group_id);
                    prep_edit_group.setInt(3, member_id);
                    prep_edit_group.executeUpdate();
                    prep_edit_group.close();
                }                
            } else if (null == members && grp.isMembersset()) {
                prep_edit_group = con.prepareStatement("DELETE FROM groups_member WHERE cid = ? AND id = ?");
                prep_edit_group.setInt(1, context_id);
                prep_edit_group.setInt(2, group_id);
                prep_edit_group.executeUpdate();
                prep_edit_group.close();
            }

            // set last modified
            changeLastModifiedOnGroup(context_id, group_id, con);

            con.commit();
        } catch (final DataTruncation dt) {
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException e) {
           log.error("SQL Error", e);
            doRollback(con);
            throw new StorageException(e);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            doRollback(con);
            throw new StorageException(e);
        } finally {
            closePreparedStatement(prep_edit_group);
            pushConnectionforContext(con, context_id);
        }
    }

    private void doRollback(Connection con) {
        try {
            if (con != null) {
                con.rollback();
            }
        } catch (final SQLException ecp) {
            log.error("Error processing rollback of connection!", ecp);
        }
    }

    @Override
    public int create(final Context ctx, final Group grp) throws StorageException {
        int retval = -1;
        Connection con = null;
        PreparedStatement prep_insert = null;
        final int context_id = ctx.getId().intValue();
        try {
            con = cache.getConnectionForContext(context_id);
            con.setAutoCommit(false);
            final String identifier = grp.getName();

            final String displayName = grp.getDisplayname();
            final int groupID = IDGenerator.getId(context_id, com.openexchange.groupware.Types.PRINCIPAL, con);
            con.commit();
            
            int gid_number = -1;
            if(Integer.parseInt(prop.getGroupProp(AdminProperties.Group.GID_NUMBER_START,"-1"))>0){
                gid_number = IDGenerator.getId(context_id, com.openexchange.groupware.Types.GID_NUMBER, con);
                con.commit();
            }
            
            prep_insert = con.prepareStatement("INSERT INTO groups (cid,id,identifier,displayName,lastModified,gidnumber) VALUES (?,?,?,?,?,?);");
            prep_insert.setInt(1, context_id);
            prep_insert.setInt(2, groupID);
            prep_insert.setString(3, identifier);
            prep_insert.setString(4, displayName);
            prep_insert.setLong(5, System.currentTimeMillis());
            if (-1 != gid_number) {
                prep_insert.setInt(6, gid_number);
            } else {
                prep_insert.setInt(6, NOGROUP);
            }
            prep_insert.executeUpdate();
            prep_insert.close();
            
            // check for members and add them
            if (grp.getMembers() != null && grp.getMembers().length > 0) {
                Integer[] as = grp.getMembers();
                for (Integer member_id : as) {
                    prep_insert = con.prepareStatement("INSERT INTO groups_member (cid,id,member) VALUES (?,?,?)");
                    prep_insert.setInt(1, context_id);
                    prep_insert.setInt(2, groupID);
                    prep_insert.setInt(3, member_id);
                    prep_insert.executeUpdate();
                    prep_insert.close();
                }                
            }
            
            con.commit();
            
            retval = groupID;
            if (log.isInfoEnabled()) {
                log.info("Group " + groupID + " created!");
            }
        } catch (final DataTruncation dt) {
            log.error(AdminCache.DATA_TRUNCATION_ERROR_MSG, dt);
            throw AdminCache.parseDataTruncation(dt);
        } catch (final SQLException sql) {
            log.error("SQL Error", sql);
            doRollback(con);
            throw new StorageException(sql);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            doRollback(con);
            throw new StorageException(e);
        } finally {
            closePreparedStatement(prep_insert);
            pushConnectionforContext(con, context_id);
        }

        return retval;
    }

    public void delete(final Context ctx, final Group[] grps) throws StorageException {
        Connection con = null;
        PreparedStatement prep_del_members = null;
        PreparedStatement prep_del_group = null;
        final int context_id = ctx.getId();
        try {
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            
            tool.existsGroup(ctx, grps);

            con = cache.getConnectionForContext(context_id);
            con.setAutoCommit(false);
            for (final Group grp : grps) {
                final int grp_id = grp.getId();
//              let the groupware api know that the group will be deleted
                OXFolderAdminHelper.propagateGroupModification(grp_id, con, con, context_id); 
                
                final DeleteEvent delev = new DeleteEvent(this, grp_id, DeleteEvent.TYPE_GROUP, context_id);
                AdminCache.delreg.fireDeleteEvent(delev, con, con);

                prep_del_members = con.prepareStatement("DELETE FROM groups_member WHERE cid=? AND id=?");
                prep_del_members.setInt(1, context_id);
                prep_del_members.setInt(2, grp_id);
                prep_del_members.executeUpdate();
                prep_del_members.close();

                createRecoveryData(grp_id, context_id, con);

                prep_del_group = con.prepareStatement("DELETE FROM groups WHERE cid=? AND id=?");
                prep_del_group.setInt(1, context_id);
                prep_del_group.setInt(2, grp_id);
                prep_del_group.executeUpdate();
                prep_del_group.close();
            }
            
            con.commit();
        } catch (final SQLException sql) {
            log.error("SQL Error", sql);
            doRollback(con);
            throw new StorageException(sql);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            doRollback(con);
            throw new StorageException(e);
        } catch (final DeleteFailedException e) {
            log.error("Delete Error", e);
            doRollback(con);
            throw new StorageException(e.toString());
        } catch (final ContextException e) {
            log.error("Context Error", e);
            doRollback(con);
            throw new StorageException(e.toString());
        } finally {
            closePreparedStatement(prep_del_members);
            closePreparedStatement(prep_del_group);
            pushConnectionforContext(con, context_id);
        }
    }

    @Override
    public User[] getMembers(final Context ctx, final int grp_id) throws StorageException {
        Connection con = null;
        final int context_id = ctx.getId();
        try {
            con = cache.getConnectionForContext(context_id);
            
            final Integer[] as =  getMembers(ctx, grp_id,con);
            final User[] ret = new User[as.length];
            for (int i = 0; i < as.length; i++) {
                User u = new User(as[i]);
                ret[i] = u;
            }
            return ret;
        } catch (final PoolException e) {
            log.error("Pool Error", e);            
            throw new StorageException(e);
        } finally {
            pushConnectionforContext(con, context_id);
        }
    }
    
    private Integer[] getMembers(final Context ctx, final int grp_id, final Connection con) throws StorageException {        
        PreparedStatement prep_list = null;
        final int context_id = ctx.getId();
        try {
            prep_list = con.prepareStatement("SELECT member FROM groups_member WHERE groups_member.cid = ? AND groups_member.id = ?;");
            prep_list.setInt(1, context_id);
            prep_list.setInt(2, grp_id);
            final ResultSet rs = prep_list.executeQuery();
            final ArrayList<Integer> ids = new ArrayList<Integer>();
            while (rs.next()) {
                ids.add(rs.getInt("member"));
            }
            return ids.toArray(new Integer[ids.size()]);        
        } catch (final SQLException sql) {
            log.error("SQL Error", sql);            
            throw new StorageException(sql);
        } finally {
            closePreparedStatement(prep_list);
        }
    }

    private Group get(final Context ctx, final Group grp, final Connection con) throws StorageException {
        PreparedStatement prep_list = null;
        final int context_ID = ctx.getId();
        try {
            prep_list = con.prepareStatement("SELECT cid,identifier,displayName FROM groups WHERE groups.cid = ? AND groups.id = ?");
            prep_list.setInt(1, context_ID);
            prep_list.setInt(2, grp.getId());
            final ResultSet rs = prep_list.executeQuery();

            while (rs.next()) {
                final String ident = rs.getString("identifier");
                final String disp = rs.getString("displayName");
                grp.setName(ident);
                grp.setDisplayname(disp);
            }
            final Integer []members = getMembers(ctx, grp.getId(), con);
            if (members != null) {
                grp.setMembers(members);
            }
        } catch (final SQLException sql) {
            log.error("SQL Error", sql);            
            throw new StorageException(sql);       
        } finally {
            closePreparedStatement(prep_list);
        }
        return grp;
    }

    @Override
    public Group get(final Context ctx, Group grp) throws StorageException {
        Connection con = null;
        try {
            con = cache.getConnectionForContext(ctx.getId().intValue());

            return get(ctx, grp, con);        
        } catch (final PoolException e) {
            log.error("Pool Error", e);            
            throw new StorageException(e);
        } finally {
            pushConnectionforContext(con, ctx.getId().intValue());
        }
    }

    @Override
    public Group[] list(final Context ctx, final String pattern) throws StorageException {
        Connection con = null;
        PreparedStatement prep_list = null;
        ResultSet rs = null;
        final int context_id = ctx.getId();
        try {
            String pattern_temp = null;
            if (pattern != null) {
                pattern_temp = pattern.replace('*', '%');
            }

            con = cache.getConnectionForContext(context_id);

            prep_list = con.prepareStatement("SELECT cid,id,identifier,displayName FROM groups WHERE groups.cid = ? AND (identifier like ? OR displayName like ?)");
            prep_list.setInt(1, context_id);
            prep_list.setString(2, pattern_temp);
            prep_list.setString(3, pattern_temp);
            rs = prep_list.executeQuery();

            final ArrayList<Group> list = new ArrayList<Group>();
            while (rs.next()) {
                // int cid = rs.getInt("cid");
                final int id = rs.getInt("id");
                final String ident = rs.getString("identifier");
                final String disp = rs.getString("displayName");
                // data.put(I_OXGroup.CID,cid);
                Group retgrp = new Group(id, ident, disp);
                final Integer []members = getMembers(ctx, id, con);
                if (members != null) {
                    retgrp.setMembers(members);
                }
                list.add(retgrp);
            }
            return (Group[])list.toArray(new Group[list.size()]);
        } catch (final SQLException sql) {
            log.error("SQL Error", sql);            
            throw new StorageException(sql);
        } catch (final PoolException e) {
            log.error("Pool Error", e);            
            throw new StorageException(e);
        } finally {
            closeResultSet(rs);
            closePreparedStatement(prep_list);
            pushConnectionforContext(con, context_id);
        }
    }

    private void closeResultSet(ResultSet rs) {
        try {
            if (null != rs) {
                rs.close();
            }
        } catch (final SQLException ex) {
            log.error("Error closing Resultset!", ex);
        }
    }

    private void pushConnectionforContext(Connection con, final int context_id) {
        try {
            if (null != con) {
                cache.pushConnectionForContext(context_id, con);
            }
        } catch (final PoolException e) {
            log.error("Error pushing ox connection to pool!", e);
        }
    }

    @Override
    public void deleteRecoveryData(final Context ctx, final int group_id, final Connection con) throws StorageException {
        // delete from del_groups table
        PreparedStatement del_st = null;
        final int context_id = ctx.getId();
        try {
            del_st = con.prepareStatement("DELETE from del_groups WHERE id = ? AND cid = ?");
            del_st.setInt(1, group_id);
            del_st.setInt(2, context_id);
            del_st.executeUpdate();
        } catch (final SQLException sql) {
            log.error("SQL Error", sql);
            doRollback(con);
            throw new StorageException(sql);
        } finally {
            closePreparedStatement(del_st);
        }
    }

    @Override
    public void deleteAllRecoveryData(final Context ctx, final Connection con) throws StorageException {
        // delete from del_groups table
        PreparedStatement del_st = null;
        final int context_id = ctx.getId();
        try {
            del_st = con.prepareStatement("DELETE from del_groups WHERE cid = ?");
            del_st.setInt(1, context_id);
            del_st.executeUpdate();
        } catch (final SQLException sql) {
            log.error("SQL Error", sql);
            doRollback(con);
            throw new StorageException(sql);
        } finally {
            closePreparedStatement(del_st);
        }
    }

    private void createRecoveryData(final int group_id, final int context_id, final Connection write_ox_con) throws SQLException {
        PreparedStatement del_st = null;
        ResultSet rs = null;
        try {
            del_st = write_ox_con.prepareStatement("SELECT identifier,displayName,gidNumber FROM groups WHERE id = ? AND cid = ?");
            del_st.setInt(1, group_id);
            del_st.setInt(2, context_id);
            rs = del_st.executeQuery();
            String ident = null;
            String disp = null;
            int gidNumber = -1;
            
            if (rs.next()) {
                ident = rs.getString("identifier");
                disp = rs.getString("displayName");
                gidNumber = rs.getInt("gidNumber");
            }
            del_st.close();
           
            del_st = write_ox_con.prepareStatement("INSERT into del_groups (id,cid,lastModified,identifier,displayName,gidNumber) VALUES (?,?,?,?,?,?)");
            del_st.setInt(1, group_id);
            del_st.setInt(2, context_id);
            del_st.setLong(3, System.currentTimeMillis());
            del_st.setString(4, ident);
            del_st.setString(5, disp);
            del_st.setInt(6, gidNumber);
            del_st.executeUpdate();
        } finally {
            closeResultSet(rs);
            closePreparedStatement(del_st);
        }
    }

    @Override
    public Group[] getGroupsForUser(final Context ctx, final User usr) throws StorageException {
        Connection con = null;
        PreparedStatement prep_list = null;
        try {
            con = cache.getConnectionForContext(ctx.getId().intValue());
            // fetch all group ids the user is member of
            prep_list = con.prepareStatement("SELECT id FROM groups_member WHERE cid = ? AND member = ?");
            prep_list.setInt(1, ctx.getId().intValue());
            prep_list.setInt(2, usr.getId().intValue());

            final ResultSet rs = prep_list.executeQuery();
            ArrayList<Group> grplist = new ArrayList<Group>();
            while (rs.next()) {
                grplist.add(get(ctx, new Group(rs.getInt("id")), con));
            }
            return grplist.toArray(new Group[grplist.size()]);
        } catch (final SQLException sql) {
            log.error("SQL Error", sql);
            throw new StorageException(sql);
        } catch (final PoolException e) {
            log.error("Pool Error", e);
            throw new StorageException(e);
        } finally {
            closePreparedStatement(prep_list);
            pushConnectionforContext(con, ctx.getId().intValue());
        }
    }

}
