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
package com.openexchange.admin;

import com.openexchange.admin.container.Context;
import com.openexchange.admin.container.Group;
import com.openexchange.admin.container.User;
import com.openexchange.admin.dataSource.I_OXGroup;

import java.rmi.Naming;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author cutmasta
 */
public class GroupTest extends AbstractAdminTest{

    // list of chars that must be valid
    private static String VALID_CHAR_TESTGROUP = " abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+.%$@";
    private static String VALID_CHAR_TESTUSER = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+.%$@";
	
    public void testAddGroup() throws Exception {
        int id = addGroup(getTestGroupObject(),getRMIHost());
        assertTrue("group id > 0 expected",id>0);
    }
    
    public void testSearchGroup() throws Exception{
        long context_id = getContextID();
        addGroup(getTestGroupObject(VALID_CHAR_TESTGROUP,context_id),getRMIHost());
        Group [] grps = searchGroup(context_id,VALID_CHAR_TESTGROUP,getRMIHost());
        assertEquals("invalid search result",1,grps.length);
    }
    
    public void testDeleteGroup() throws Exception{
        long context_id = getContextID();
        int id = addGroup(getTestGroupObject(VALID_CHAR_TESTGROUP,context_id),getRMIHost());
        Group grp = new Group();
        grp.setContextId(context_id);
        grp.setId((long)id);
        deleteGroup(grp,getRMIHost());
        // now load the group again, this MUST fail
        try{
            loadGroup(grp,getRMIHost());
            fail("group not exists expected");
        }catch(Exception ecp){
            if(ecp.toString().toLowerCase().indexOf("group does not exist")!=-1){
                // this exception MUST happen, if not, test MUST fail :)
                assertTrue(true);
            }
        }
    }
    
    public void testLoadGroup() throws Exception{
        Group addgroup = getTestGroupObject();
        int id = addGroup(addgroup,getRMIHost());
        assertTrue("expected id > 0",id>0);
        addgroup.setId((long)id);
        compareGroups(addgroup,loadGroup(addgroup,getRMIHost()));
    }
    
    public void testAddMemberToGroup()throws Exception{
        long ctxid = getContextID();
        Group addgroup = getTestGroupObject("memberaddgroup"+VALID_CHAR_TESTGROUP,ctxid);
        int group_id = addGroup(addgroup,getRMIHost());
        assertTrue("group id > 0 expected",group_id>0);
        addgroup.setId(group_id);
        
        // create user to add
        User usr = UserTest.getTestUserObject("groupmemberadduser"+VALID_CHAR_TESTUSER,"netline",ctxid);
        long usr_id = UserTest.addUser(usr,getRMIHost());
        usr.setId(usr_id);
        
        // add user as groupmember
        int[] members = new int[1];
        members[0] = (int)usr_id;
        addMemberToGroup(addgroup,members,getRMIHost());
        
        // now get all members of group, and check if user is member
        int[] remote_members = getMembers(addgroup,getRMIHost());
        boolean foundmember = false;
        for(int a = 0;a<remote_members.length;a++){
            if(remote_members[a]==(int)usr_id){
                foundmember = true;
            }
        }
        assertTrue("member not added to group",foundmember);
    }
    
    public void testRemoveMemberFromGroup()throws Exception{
        long ctxid = getContextID();
        Group addgroup = getTestGroupObject("memberaddgroup"+VALID_CHAR_TESTGROUP,ctxid);
        int group_id = addGroup(addgroup,getRMIHost());
        assertTrue("group id > 0 expected",group_id>0);
        addgroup.setId(group_id);
        
        // create user to add
        User usr = UserTest.getTestUserObject("groupmemberadduser"+VALID_CHAR_TESTUSER,"netline",ctxid);
        long usr_id = UserTest.addUser(usr,getRMIHost());
        usr.setId(usr_id);
        
        // add user as groupmember
        int[] members = new int[1];
        members[0] = (int)usr_id;
        addMemberToGroup(addgroup,members,getRMIHost());
        
        // now get all members of group, and check if user is member
        int[] remote_members = getMembers(addgroup,getRMIHost());
        boolean foundmember = false;
        for(int a = 0;a<remote_members.length;a++){
            if(remote_members[a]==(int)usr_id){
                foundmember = true;
            }
        }
        assertTrue("member not added to group",foundmember);
        
        // now remove user from group;
        removeMemberFromGroup(addgroup,members,getRMIHost());
        // now get all members of group, and check if user is member
        remote_members = getMembers(addgroup,getRMIHost());
        foundmember = false;
        for(int a = 0;a<remote_members.length;a++){
            if(remote_members[a]==(int)usr_id){
                foundmember = true;
            }
        }
        assertFalse("member not removed from group",foundmember);
        
    }
    
    public void testChangeGroup()throws Exception{
        long ctx = getContextID();
        Group addgroup = getTestGroupObject("changed_this_group",ctx);
        int id = addGroup(addgroup,getRMIHost());
        assertTrue("expected id > 0",id>0);
        addgroup.setId((long)id);
        
        // check if group is created on server
        compareGroups(addgroup,loadGroup(addgroup,getRMIHost()));
        
        // change the data of the group local
        createChangeGroupData(addgroup);
        
        // do the changes on the remote server for the group
        changeGroup(addgroup,getRMIHost());
        
        // load group which was modified
        Group remote_grp = loadGroup(addgroup,getRMIHost());
        
        compareGroups(addgroup,remote_grp);
    }   
    
    public void testGetMembers()throws Exception{
        long ctxid = getContextID();
        Group addgroup = getTestGroupObject("memberaddgroup"+VALID_CHAR_TESTGROUP,ctxid);
        int group_id = addGroup(addgroup,getRMIHost());
        assertTrue("group id > 0 expected",group_id>0);
        addgroup.setId(group_id);
        
        // create user to add
        User usr = UserTest.getTestUserObject("groupmemberadduser"+VALID_CHAR_TESTUSER,"netline",ctxid);
        long usr_id = UserTest.addUser(usr,getRMIHost());
        usr.setId(usr_id);
                
        // add user as groupmember
        int[] members = new int[1];
        members[0] = (int)usr_id;
        addMemberToGroup(addgroup,members,getRMIHost());
        
        // now get all members of group, and check if user is member
        int[] remote_members = getMembers(addgroup,getRMIHost());
        assertTrue("members could not be loaded",remote_members.length>0);
    }
    
    public static void changeGroup(Group grp,String host) throws Exception {
        I_OXGroup xres = (I_OXGroup)Naming.lookup(checkHost(host)+I_OXGroup.RMI_NAME);        
        Vector v = xres.changeOXGroup((int)grp.getContextId(),(int)grp.getId(),grp.xForm2data());
        parseResponse(v);
    }
    
    public static int[] getMembers(Group group,String host) throws Exception {
        I_OXGroup xres = (I_OXGroup)Naming.lookup(checkHost(host)+I_OXGroup.RMI_NAME);
        Vector v = xres.getMembers((int)group.getContextId(),(int)group.getId());
        parseResponse(v);
        Vector tmp_ids = (Vector)v.get(1);
        int[] ids = new int[tmp_ids.size()];
        for(int a = 0;a<tmp_ids.size();a++){
            ids[a] = Integer.parseInt(tmp_ids.get(a).toString());
        }
        return ids;
    }
    
    public static void addMemberToGroup(Group grp,int[] member_ids,String host)throws Exception{
        I_OXGroup xres = (I_OXGroup)Naming.lookup(checkHost(host)+I_OXGroup.RMI_NAME);
        Vector v = xres.addMember((int)grp.getContextId(),(int)grp.getId(),member_ids);
        parseResponse(v);
    }
    
    public static void removeMemberFromGroup(Group grp,int[] member_ids,String host)throws Exception{
        I_OXGroup xres = (I_OXGroup)Naming.lookup(checkHost(host)+I_OXGroup.RMI_NAME);
        Vector v = xres.removeMember((int)grp.getContextId(),(int)grp.getId(),member_ids);
        parseResponse(v);
    }
    
    public static Group loadGroup(Group grp,String host) throws Exception{
        I_OXGroup xres = (I_OXGroup)Naming.lookup(checkHost(host)+I_OXGroup.RMI_NAME);
        Vector v = xres.getOXGroupData((int)grp.getContextId(),(int)grp.getId());
        parseResponse(v);
        return buildGroupFromData((Hashtable)v.get(1));
    }
    
    public static int addGroup(Group grp,String host) throws Exception {
        I_OXGroup xres = (I_OXGroup)Naming.lookup(checkHost(host)+I_OXGroup.RMI_NAME);
        Vector v = xres.createOXGroup((int)grp.getContextId(),grp.xForm2data());
        parseResponse(v);
        return Integer.parseInt(v.get(1).toString());
    }
    
    public static void deleteGroup(Group grp,String host) throws Exception {
        I_OXGroup xres = (I_OXGroup)Naming.lookup(checkHost(host)+I_OXGroup.RMI_NAME);
        Vector v = xres.deleteOXGroup((int)grp.getContextId(),(int)grp.getId());
        parseResponse(v);
    }
    
    public static Group[] searchGroup(long contextId,String pattern,String host) throws Exception{
        I_OXGroup xres = (I_OXGroup)Naming.lookup(checkHost(host)+I_OXGroup.RMI_NAME);
        Vector v = xres.listOXGroups(Integer.parseInt(""+contextId),pattern);
        parseResponse(v);
        Vector data = (Vector)v.get(1);
        Group [] reval = new Group[data.size()];
        for(int a = 0;a<data.size();a++){
            reval[a]=buildGroupFromData((Hashtable)data.get(a));
        }
        return reval;
    }
    
    
    
    private static Group getTestGroupObject()throws Exception{
        return getTestGroupObject(VALID_CHAR_TESTGROUP,getContextID());
    }
    
    private static Group getTestGroupObject(String ident,long contextid) throws Exception{
        Group grp = new Group();
        grp.setContextId(contextid);
        grp.setDisplayName("display name "+ident);
        grp.setIdentifier(ident);
        return grp;
    }
    
    
    
    public static Group buildGroupFromData(Hashtable data){
        Group grp = new Group();
        if(data.containsKey(I_OXGroup.CID)){
            grp.setContextId(Long.parseLong(data.get(I_OXGroup.CID).toString()));
        }
        if(data.containsKey(I_OXGroup.DISPLAYNAME)){
            grp.setDisplayName(data.get(I_OXGroup.DISPLAYNAME).toString());
        }
        if(data.containsKey(I_OXGroup.GID_NUMBER)){
            grp.setId(Long.parseLong(data.get(I_OXGroup.GID_NUMBER).toString()));
        }
        if(data.containsKey(I_OXGroup.GID)){
            grp.setIdentifier(data.get(I_OXGroup.GID).toString());
        }
        return grp;
    }
    
    private static String getRMIHost(){
        return "localhost";
    }
    
    private static long getContextID() throws Exception {
        Context ctx = ContextTest.getTestContextObject(ContextTest.createNewContextID(),10);
        long id = ContextTest.addContext(ctx,getRMIHost());
        return id;
    }
    
    private void compareGroups(Group a,Group b){
        assertEquals("context id not equal",a.getContextId(),b.getContextId());
        assertEquals("displayname id not equal",a.getDisplayName(),b.getDisplayName());
        assertEquals("id not equals",a.getId(),b.getId());
        assertEquals("identifier not equal",a.getIdentifier(),b.getIdentifier());
    }

    private void createChangeGroupData(Group group) {
        if(group.getDisplayName()!=null){
            group.setDisplayName(group.getDisplayName()+change_suffix);
        }
        if(group.getIdentifier()!=null){
            group.setIdentifier(group.getIdentifier()+change_suffix);
        }
    }
}
