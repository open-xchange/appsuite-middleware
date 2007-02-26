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
package com.openexchange.admin.rmi;

import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;

import java.rmi.Naming;

/**
 * 
 * @author cutmasta
 * @author d7
 */
public class GroupTest extends AbstractTest {

    // list of chars that must be valid
    private static final String VALID_CHAR_TESTGROUP = " abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+.%$@";

    private static final String VALID_CHAR_TESTUSER = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+.%$@";

    public void testAddGroup() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final int id = addGroup(ctx, getTestGroupObject(cred), getRMIHostUrl(), cred);
        assertTrue("group id > 0 expected", id > 0);
    }

    public void testSearchGroup() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        addGroup(ctx, getTestGroupObject(VALID_CHAR_TESTGROUP, ctx, cred), hosturl, cred);
        final Group[] grps = searchGroup(ctx, VALID_CHAR_TESTGROUP, hosturl, cred);
        assertEquals("invalid search result", 1, grps.length);
    }

    public void testDeleteGroup() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final int id = addGroup(ctx, getTestGroupObject(VALID_CHAR_TESTGROUP, ctx, cred), hosturl, cred);
        final Group grp = new Group();
        grp.setId(id);
        deleteGroup(ctx, grp, hosturl, cred);
        // now load the group again, this MUST fail
        try {
            loadGroup(ctx, grp, hosturl, cred);
            fail("group not exists expected");
        } catch (final Exception ecp) {
            if (ecp.toString().toLowerCase().indexOf("group does not exist") != -1) {
                // this exception MUST happen, if not, test MUST fail :)
                assertTrue(true);
            }
        }
    }

    public void testLoadGroup() throws Exception {
        
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final Group addgroup = getTestGroupObject(cred);
        final String hosturl = getRMIHostUrl();
        final int id = addGroup(ctx, addgroup, hosturl, cred);
        assertTrue("expected id > 0", id > 0);
        addgroup.setId(id);
        
        // load from server 
        Group srv_group = loadGroup(ctx, addgroup, hosturl, cred);
        
        // compare group fields
        assertEquals("displayname id not equal", addgroup.getDisplayname(), srv_group.getDisplayname());
        assertEquals("id not equals", addgroup.getId(), srv_group.getId());
        assertEquals("identifier not equal",addgroup.getName(), srv_group.getName());
        assertEquals("email not equal", addgroup.getEmail(), srv_group.getEmail());
    }

    public void testAddMemberToGroup() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP, ctx, cred);
        final int group_id = addGroup(ctx, addgroup, hosturl, cred);
        assertTrue("group id > 0 expected", group_id > 0);
        addgroup.setId(group_id);

        // create user to add
        final User usr = UserTest.getTestUserObject("groupmemberadduser" + VALID_CHAR_TESTUSER, "netline");
        final UserModuleAccess access = new UserModuleAccess();
        final int usr_id = UserTest.addUser(ctx, usr, access);
        usr.setId(usr_id);

        // add user as groupmember
        final int[] members = new int[1];
        members[0] = (int) usr_id;
        addMemberToGroup(ctx, addgroup, members, hosturl, cred);

        // now get all members of group, and check if user is member
        final int[] remote_members = getMembers(ctx, addgroup, hosturl, cred);
        boolean foundmember = false;
        for (int element : remote_members) {
            if (element == (int) usr_id) {
                foundmember = true;
            }
        }
        assertTrue("member not added to group", foundmember);
    }

    public void testRemoveMemberFromGroup() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP, ctx, cred);
        final int group_id = addGroup(ctx, addgroup, hosturl, cred);
        assertTrue("group id > 0 expected", group_id > 0);
        addgroup.setId(group_id);

        // create user to add
        final User usr = UserTest.getTestUserObject("groupmemberadduser" + VALID_CHAR_TESTUSER, "netline");
        final UserModuleAccess access = new UserModuleAccess();
        final int usr_id = UserTest.addUser(ctx, usr, access);
        usr.setId(usr_id);

        // add user as groupmember
        final int[] members = new int[1];
        members[0] = (int) usr_id;
        addMemberToGroup(ctx, addgroup, members, hosturl, cred);

        // now get all members of group, and check if user is member
        int[] remote_members = getMembers(ctx, addgroup, hosturl, cred);
        boolean foundmember = false;
        for (int element : remote_members) {
            if (element == (int) usr_id) {
                foundmember = true;
            }
        }
        assertTrue("member not added to group", foundmember);

        // now remove user from group;
        removeMemberFromGroup(ctx, addgroup, members, hosturl, cred);
        // now get all members of group, and check if user is member
        remote_members = getMembers(ctx, addgroup, hosturl, cred);
        foundmember = false;
        for (int element : remote_members) {
            if (element == (int) usr_id) {
                foundmember = true;
            }
        }
        assertFalse("member not removed from group", foundmember);

    }

    public void testChangeGroup() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("changed_this_group", ctx, cred);
        final int id = addGroup(ctx, addgroup, hosturl, cred);
        assertTrue("expected id > 0", id > 0);
        addgroup.setId(id);

        // load group from server
        Group srv_response = loadGroup(ctx, addgroup, hosturl, cred);
        
        // check if group is created on server correct
        assertEquals("displayname id not equal", addgroup.getDisplayname(), srv_response.getDisplayname());
        assertEquals("id not equals", addgroup.getId(), srv_response.getId());
        assertEquals("identifier not equal", addgroup.getName(), srv_response.getName());
        assertEquals("email not equal", addgroup.getEmail(),srv_response.getEmail());       

        // change the data of the group local
        createChangeGroupData(addgroup);

        // do the changes on the remote server for the group
        changeGroup(ctx, addgroup, hosturl, cred);

        // load group which was modified
        final Group remote_grp = loadGroup(ctx, addgroup, hosturl, cred);

        // check if group is created on server correct
        assertEquals("displayname id not equal", addgroup.getDisplayname(), remote_grp.getDisplayname());
        assertEquals("id not equals", addgroup.getId(), remote_grp.getId());
        assertEquals("identifier not equal", addgroup.getName(), remote_grp.getName());
        assertEquals("email not equal", addgroup.getEmail(),remote_grp.getEmail());  
    }

    public void testGetMembers() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP, ctx, cred);
        final int group_id = addGroup(ctx, addgroup, hosturl, cred);
        assertTrue("group id > 0 expected", group_id > 0);
        addgroup.setId(group_id);

        // create user to add
        final User usr = UserTest.getTestUserObject("groupmemberadduser" + VALID_CHAR_TESTUSER, "netline");
        final UserModuleAccess access = new UserModuleAccess();
        final int usr_id = UserTest.addUser(ctx, usr, access);
        usr.setId(usr_id);

        // add user as groupmember
        final int[] members = new int[1];
        members[0] = (int) usr_id;
        addMemberToGroup(ctx, addgroup, members, hosturl, cred);

        // now get all members of group, and check if user is member
        final int[] remote_members = getMembers(ctx, addgroup, hosturl, cred);
        assertTrue("members could not be loaded", remote_members.length > 0);
    }

    public static void changeGroup(final Context ctx, final Group grp, final String host, final Credentials cred) throws Exception {
        final OXGroupInterface xres = (OXGroupInterface) Naming.lookup(host + OXGroupInterface.RMI_NAME);
        xres.change(ctx, grp, cred);
    }

    public static int[] getMembers(final Context ctx, final Group group, final String host, final Credentials cred) throws Exception {
        final OXGroupInterface xres = (OXGroupInterface) Naming.lookup(host + OXGroupInterface.RMI_NAME);
        return xres.getMembers(ctx, group.getId(), cred);
    }

    public static void addMemberToGroup(final Context ctx, final Group grp, final int[] member_ids, final String host, final Credentials cred) throws Exception {
        final OXGroupInterface xres = (OXGroupInterface) Naming.lookup(host + OXGroupInterface.RMI_NAME);
        xres.addMember(ctx, grp.getId(), member_ids, cred);
    }

    public static void removeMemberFromGroup(final Context ctx, final Group grp, final int[] member_ids, final String host, final Credentials cred) throws Exception {
        final OXGroupInterface xres = (OXGroupInterface) Naming.lookup(host + OXGroupInterface.RMI_NAME);
        xres.removeMember(ctx, grp.getId(), member_ids, cred);
    }

    public static Group loadGroup(final Context ctx, final Group grp, final String host, final Credentials cred) throws Exception {
        final OXGroupInterface xres = (OXGroupInterface) Naming.lookup(host + OXGroupInterface.RMI_NAME);
        return xres.get(ctx, grp.getId(), cred);
    }

    public static int addGroup(final Context ctx, final Group grp, final String host, final Credentials cred) throws Exception {
        final OXGroupInterface xres = (OXGroupInterface) Naming.lookup(host + OXGroupInterface.RMI_NAME);
        return xres.create(ctx, grp, cred);
    }

    public static void deleteGroup(final Context ctx, final Group grp, final String host, final Credentials cred) throws Exception {
        final OXGroupInterface xres = (OXGroupInterface) Naming.lookup(host + OXGroupInterface.RMI_NAME);
        xres.delete(ctx, new int[] { grp.getId() }, cred);
    }

    public static Group[] searchGroup(final Context ctx, final String pattern, final String host, final Credentials cred) throws Exception {
        final OXGroupInterface xres = (OXGroupInterface) Naming.lookup(host + OXGroupInterface.RMI_NAME);
        return xres.list(ctx, pattern, cred);
    }

    private static Group getTestGroupObject(final Credentials cred) throws Exception {
        final Context ctx = new Context(getContextID());
        return getTestGroupObject(VALID_CHAR_TESTGROUP, ctx, cred);
    }

    private static Group getTestGroupObject(final String ident, final Context ctx, final Credentials cred) throws Exception {
        final Group grp = new Group();
        grp.setDisplayname("display name " + ident);
        grp.setName(ident);
        return grp;
    }

    public static int getContextID() throws Exception {
        final Credentials cred = DummyCredentials();
        final Context ctxset = getTestContextObject(10);
        return addContext(ctxset, getRMIHostUrl(), cred);
    }

    private void createChangeGroupData(final Group group) {
        if (group.getDisplayname() != null) {
            group.setDisplayname(group.getDisplayname() + change_suffix);
        }
        if (group.getName() != null) {
            group.setName(group.getName() + change_suffix);
        }
    }
}
