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

package com.openexchange.admin.rmi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collection;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.extensions.OXCommonExtension;

/**
 *
 * @author cutmasta
 * @author d7
 */
public class GroupTest extends AbstractTest {

    // list of chars that must be valid
    private static final String VALID_CHAR_TESTGROUP = " abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+.%$@";

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(GroupTest.class);
    }

    @Test
    public void testCreateGroup() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final int id = createGroup(ctx, getTestGroupObject(cred), getRMIHostUrl(), cred).getId();
        assertTrue("group id > 0 expected", id > 0);
    }

    @Test
    public void testSearchGroup() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        String grpname = VALID_CHAR_TESTGROUP+System.currentTimeMillis();
        createGroup(ctx, getTestGroupObject(grpname, ctx, cred), hosturl, cred);
        final Group[] grps = searchGroup(ctx, grpname, hosturl, cred);
        assertEquals("invalid search result", 1, grps.length);
    }

    @Test
    public void testDeleteGroup() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group grp = createGroup(ctx, getTestGroupObject(VALID_CHAR_TESTGROUP+System.currentTimeMillis(), ctx, cred), hosturl, cred);
        deleteGroup(ctx, grp, hosturl, cred);
        // now load the group again, this MUST fail
        try {
            getData(ctx, grp, hosturl, cred);
            fail("group not exists expected");
        } catch (final Exception ecp) {
            if (ecp.toString().toLowerCase().indexOf("group does not exist") != -1) {
                // this exception MUST happen, if not, test MUST fail :)
                assertTrue(true);
            }
        }
    }

    @Test
    public void testCreateDeleteCreate() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        Group tmp =  getTestGroupObject(VALID_CHAR_TESTGROUP+System.currentTimeMillis(), ctx, cred);
        final Group grp = createGroup(ctx,tmp, hosturl, cred);
        deleteGroup(ctx, grp, hosturl, cred);
        // now load the group again, this MUST fail
        try {
            getData(ctx, grp, hosturl, cred);
            fail("group not exists expected");
        } catch (final Exception ecp) {
            if (ecp.toString().toLowerCase().indexOf("group does not exist") != -1) {
                // this exception MUST happen, if not, test MUST fail :)
                assertTrue(true);
            }
        }

        // create same group again
        createGroup(ctx,tmp, hosturl, cred);
    }

    @Test
    public void testDeleteGroupIdentifiedByName() throws Exception {

        // delete group ident by name

        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group grp = createGroup(ctx, getTestGroupObject(VALID_CHAR_TESTGROUP+System.currentTimeMillis(), ctx, cred), hosturl, cred);

        final Group del_grp = new Group();
        del_grp.setName(grp.getName());
        deleteGroup(ctx, del_grp, hosturl, cred);
        // now load the group again, this MUST fail
        try {
            getData(ctx, grp, hosturl, cred);
            fail("group not exists expected");
        } catch (final Exception ecp) {
            if (ecp.toString().toLowerCase().indexOf("group does not exist") != -1) {
                // this exception MUST happen, if not, test MUST fail :)
                assertTrue(true);
            }
        }
    }

    @Test
    public void testDeleteGroupIdentifiedByID() throws Exception {

        // delete group ident by id

        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group grp = createGroup(ctx, getTestGroupObject(VALID_CHAR_TESTGROUP+System.currentTimeMillis(), ctx, cred), hosturl, cred);

        final Group del_grp = new Group(grp.getId());
        deleteGroup(ctx, del_grp, hosturl, cred);
        // now load the group again, this MUST fail
        try {
            getData(ctx, grp, hosturl, cred);
            fail("group not exists expected");
        } catch (final Exception ecp) {
            if (ecp.toString().toLowerCase().indexOf("group does not exist") != -1) {
                // this exception MUST happen, if not, test MUST fail :)
                assertTrue(true);
            }
        }
    }

    @Test
    public void testLoadGroup() throws Exception {

        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final Group addgroup = getTestGroupObject(cred);
        final String hosturl = getRMIHostUrl();
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load from server
        Group srv_group = getData(ctx, createdgroup, hosturl, cred);

        // compare group fields
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_group.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_group.getId());
        assertEquals("identifier not equal",createdgroup.getName(), srv_group.getName());
    }

    @Test
    public void testLoadGroupIdentifiedByName() throws Exception {

        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final Group addgroup = getTestGroupObject(cred);
        final String hosturl = getRMIHostUrl();
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load from server
        Group tmp = new Group();
        tmp.setName(createdgroup.getName());
        Group srv_group = getData(ctx, tmp, hosturl, cred);

        // compare group fields
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_group.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_group.getId());
        assertEquals("identifier not equal",createdgroup.getName(), srv_group.getName());
    }

    @Test
    public void testLoadGroupIdentifiedByID() throws Exception {

        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final Group addgroup = getTestGroupObject(cred);
        final String hosturl = getRMIHostUrl();
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load from server
        Group tmp = new Group(createdgroup.getId());
        Group srv_group = getData(ctx, tmp, hosturl, cred);

        // compare group fields
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_group.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_group.getId());
        assertEquals("identifier not equal",createdgroup.getName(), srv_group.getName());
    }

    @Test
    public void testAddMemberToGroup() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP+System.currentTimeMillis(), ctx, cred);
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        final User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", ctx);
        final UserModuleAccess access = new UserModuleAccess();
        final User createduser = UserTest.addUser(ctx, usr, access);

        // add user as groupmember
        addMemberToGroup(ctx, createdgroup, new User[]{createduser}, hosturl, cred);

        // now get all members of group, and check if user is member
        final User[] remote_members = getMembers(ctx, createdgroup, hosturl, cred);
        boolean foundmember = false;
        for (User element : remote_members) {
            if (element.getId() == createduser.getId().intValue()) {
                foundmember = true;
            }
        }
        assertTrue("member not added to group", foundmember);
    }

    @Test
    public void testAddMemberToGroupIdentifiedByName() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP+System.currentTimeMillis(), ctx, cred);
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        final User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", ctx);
        final UserModuleAccess access = new UserModuleAccess();
        final User createduser = UserTest.addUser(ctx, usr, access);

        // add user as groupmember
        Group tmp = new Group();
        tmp.setName(createdgroup.getName());
        addMemberToGroup(ctx, tmp, new User[]{createduser}, hosturl, cred);

        // now get all members of group, and check if user is member
        final User[] remote_members = getMembers(ctx, createdgroup, hosturl, cred);
        boolean foundmember = false;
        for (User element : remote_members) {
            if (element.getId() == createduser.getId().intValue()) {
                foundmember = true;
            }
        }
        assertTrue("member not added to group", foundmember);
    }

    @Test
    public void testAddMemberToGroupIdentifiedByID() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP+System.currentTimeMillis(), ctx, cred);
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        final User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", ctx);
        final UserModuleAccess access = new UserModuleAccess();
        final User createduser = UserTest.addUser(ctx, usr, access);

        // add user as groupmember
        Group tmp = new Group(createdgroup.getId());
        addMemberToGroup(ctx, tmp, new User[]{createduser}, hosturl, cred);

        // now get all members of group, and check if user is member
        final User[] remote_members = getMembers(ctx, createdgroup, hosturl, cred);
        boolean foundmember = false;
        for (User element : remote_members) {
            if (element.getId() == createduser.getId().intValue()) {
                foundmember = true;
            }
        }
        assertTrue("member not added to group", foundmember);
    }

    @Test
    public void testRemoveMemberFromGroup() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP+System.currentTimeMillis(), ctx, cred);
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add

        final User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", ctx);
        final UserModuleAccess access = new UserModuleAccess();
        final User createduser = UserTest.addUser(ctx, usr, access);

        // add user as groupmember
        addMemberToGroup(ctx, createdgroup, new User[]{createduser}, hosturl, cred);

        // now get all members of group, and check if user is member
        User[] remote_members = getMembers(ctx, createdgroup, hosturl, cred);
        boolean foundmember = false;
        for (User element : remote_members) {
            if (element.getId() == createduser.getId().intValue()) {
                foundmember = true;
            }
        }
        assertTrue("member not added to group", foundmember);

        // now remove user from group;
        removeMemberFromGroup(ctx, createdgroup, new User[]{createduser}, hosturl, cred);
        // now get all members of group, and check if user is member
        remote_members = getMembers(ctx, createdgroup, hosturl, cred);
        foundmember = false;
        for (User element : remote_members) {
            if (element.getId() == createduser.getId().intValue()) {
                foundmember = true;
            }
        }
        assertFalse("member not removed from group", foundmember);

    }

    @Test
    public void testRemoveMemberFromGroupIdentifiedByName() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP+System.currentTimeMillis(), ctx, cred);
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add

        final User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", ctx);
        final UserModuleAccess access = new UserModuleAccess();
        final User createduser = UserTest.addUser(ctx, usr, access);

        // add user as groupmember
        addMemberToGroup(ctx, createdgroup, new User[]{createduser}, hosturl, cred);

        // now get all members of group, and check if user is member
        User[] remote_members = getMembers(ctx, createdgroup, hosturl, cred);
        boolean foundmember = false;
        for (User element : remote_members) {
            if (element.getId() == createduser.getId().intValue()) {
                foundmember = true;
            }
        }
        assertTrue("member not added to group", foundmember);

        // now remove user from group;
        Group tmp = new Group();
        tmp.setName(createdgroup.getName());
        removeMemberFromGroup(ctx, tmp, new User[]{createduser}, hosturl, cred);

        // now get all members of group, and check if user is member
        remote_members = getMembers(ctx, createdgroup, hosturl, cred);
        foundmember = false;
        for (User element : remote_members) {
            if (element.getId() == createduser.getId().intValue()) {
                foundmember = true;
            }
        }
        assertFalse("member not removed from group", foundmember);

    }

    @Test
    public void testRemoveMemberFromGroupIdentifiedByID() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP+System.currentTimeMillis(), ctx, cred);
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add

        final User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", ctx);
        final UserModuleAccess access = new UserModuleAccess();
        final User createduser = UserTest.addUser(ctx, usr, access);

        // add user as groupmember
        addMemberToGroup(ctx, createdgroup, new User[]{createduser}, hosturl, cred);

        // now get all members of group, and check if user is member
        User[] remote_members = getMembers(ctx, createdgroup, hosturl, cred);
        boolean foundmember = false;
        for (User element : remote_members) {
            if (element.getId() == createduser.getId().intValue()) {
                foundmember = true;
            }
        }
        assertTrue("member not added to group", foundmember);

        // now remove user from group;
        Group tmp = new Group(createdgroup.getId());
        removeMemberFromGroup(ctx, tmp, new User[]{createduser}, hosturl, cred);

        // now get all members of group, and check if user is member
        remote_members = getMembers(ctx, createdgroup, hosturl, cred);
        foundmember = false;
        for (User element : remote_members) {
            if (element.getId() == createduser.getId().intValue()) {
                foundmember = true;
            }
        }
        assertFalse("member not removed from group", foundmember);

    }

    @Test
    public void testChangeGroup() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("changed_this_group"+System.currentTimeMillis(), ctx, cred);
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load group from server
        Group srv_response = getData(ctx, createdgroup, hosturl, cred);

        // check if group is created on server correct
        assertEquals("displayname not equal", createdgroup.getDisplayname(), srv_response.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_response.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_response.getName());

        // change the data of the group local
        createChangeGroupData(createdgroup);

        // do the changes on the remote server for the group
        changeGroup(ctx, createdgroup, hosturl, cred);

        // load group which was modified
        final Group remote_grp = getData(ctx, createdgroup, hosturl, cred);

        // check if group is created on server correct
        assertEquals("displayname not equal", createdgroup.getDisplayname(), remote_grp.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), remote_grp.getId());
        assertEquals("identifier not equal", createdgroup.getName(), remote_grp.getName());
    }


    @Test
    public void testChangeGroupIdentifiedbyID() throws Exception {

        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("changed_this_group"+System.currentTimeMillis(), ctx, cred);
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load group from server
        Group srv_response = getData(ctx, createdgroup, hosturl, cred);

        // check if group is created on server correct
        assertEquals("displayname not equal", createdgroup.getDisplayname(), srv_response.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_response.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_response.getName());

        Group tmp_group = new Group(srv_response.getId());
        tmp_group.setDisplayname(srv_response.getDisplayname()+"_changed");

        // do the changes on the remote server for the group
        changeGroup(ctx, tmp_group, hosturl, cred);

        // load group which was modified
        final Group remote_grp = getData(ctx, createdgroup, hosturl, cred);

        // check if group is change correctly on server
        assertEquals("displayname not equal", tmp_group.getDisplayname(), remote_grp.getDisplayname());

    }

    @Test(expected=InvalidDataException.class)
    public void testChangeNull() throws Exception {

        // change group displayname and name to null, this must fail

        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("changed_this_group"+System.currentTimeMillis(), ctx, cred);
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load group from server
        Group srv_response = getData(ctx, createdgroup, hosturl, cred);

        // check if group is created on server correct
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_response.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_response.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_response.getName());

        // set display name to null and name to null
        Group tmp_group = new Group(srv_response.getId());
        tmp_group.setDisplayname(null);
        tmp_group.setName(null);

        // do the changes on the remote server for the group
        changeGroup(ctx, tmp_group, hosturl, cred);
    }

    @Test
    public void testChangeAllowedNull() throws Exception {
        // change group displayname and name to null, this must fail
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("changed_this_group"+System.currentTimeMillis(), ctx, cred);
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load group from server
        Group srv_response = getData(ctx, createdgroup, hosturl, cred);

        // check if group is created on server correct
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_response.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_response.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_response.getName());

        // set display name to null and name to null
        Group tmp_group = new Group(srv_response.getId());
        tmp_group.setMembers(null);

        // do the changes on the remote server for the group
        changeGroup(ctx, tmp_group, hosturl, cred);

        // load group which was modified
        final Group remote_grp = getData(ctx, createdgroup, hosturl, cred);

        createdgroup.setMembers(new Integer[0]);
        assertEquals("Group aren't equal", createdgroup, remote_grp);
    }

    @Test
    public void testChangeGroupIdentifiedbyName() throws Exception {

        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("changed_this_group"+System.currentTimeMillis(), ctx, cred);
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load group from server
        Group srv_response = getData(ctx, createdgroup, hosturl, cred);

        // check if group is created on server correct
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_response.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_response.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_response.getName());

        Group tmp_group = new Group();
        tmp_group.setDisplayname(srv_response.getDisplayname()+"_changed");
        tmp_group.setName(srv_response.getName());
        // do the changes on the remote server for the group
        changeGroup(ctx, tmp_group, hosturl, cred);

        // load group which was modified
        final Group remote_grp = getData(ctx, createdgroup, hosturl, cred);

        // check if group is changed correctly on server
        assertEquals("displayname id not equal", tmp_group.getDisplayname(), remote_grp.getDisplayname());
    }



    @Test
    public void testGetMembers() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP+System.currentTimeMillis(), ctx, cred);
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        final User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", ctx);
        final UserModuleAccess access = new UserModuleAccess();
        final User createduser = UserTest.addUser(ctx, usr, access);

        // add user as groupmember
        addMemberToGroup(ctx, createdgroup, new User[]{createduser}, hosturl, cred);

        // now get all members of group, and check if user is member
        final User[] remote_members = getMembers(ctx, createdgroup, hosturl, cred);
        assertTrue("members could not be loaded", remote_members.length > 0);
    }

    @Test
    public void testGetMembersByName() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP+System.currentTimeMillis(), ctx, cred);
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        final User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", ctx);
        final UserModuleAccess access = new UserModuleAccess();
        final User createduser = UserTest.addUser(ctx, usr, access);

        // We only want to resolve by name
        createdgroup.setId(null);
        createduser.setId(null);
        // add user as groupmember
        addMemberToGroup(ctx, createdgroup, new User[]{createduser}, hosturl, cred);

        // now get all members of group, and check if user is member
        final User[] remote_members = getMembers(ctx, createdgroup, hosturl, cred);
        assertTrue("members could not be loaded", remote_members.length > 0);
    }

    @Test
    public void testlistGroupsForUser() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP+System.currentTimeMillis(), ctx, cred);
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        final User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", ctx);
        final UserModuleAccess access = new UserModuleAccess();
        final User createduser = UserTest.addUser(ctx, usr, access);

        // We only want to resolve by name
        createduser.setId(null);
        // add user as groupmember
        addMemberToGroup(ctx, createdgroup, new User[]{createduser}, hosturl, cred);

        // We have to fetch the Group because the create on (createdgroup) doesn't contain
        // the members.
        final Group newgroup = getData(ctx, createdgroup, hosturl, cred);
        // now get all members of group, and check if user is member
        final Group[] remote_members = listGroupsForUser(ctx, usr, hosturl, cred);
        assertTrue("members could not be loaded", remote_members.length > 0);
        // The right group must be the second one in the array, the first one is the all
        // users group
        assertEquals("no right group", newgroup, remote_members[1]);
    }

    @Test
    public void testlistGroupsForUserByName() throws Exception {
        final int context_id = getContextID();
        final Context ctx = new Context(context_id);
        final Credentials cred = DummyCredentials();
        final String hosturl = getRMIHostUrl();
        final Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP+System.currentTimeMillis(), ctx, cred);
        final Group createdgroup = createGroup(ctx, addgroup, hosturl, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        final User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", ctx);
        final UserModuleAccess access = new UserModuleAccess();
        final User createduser = UserTest.addUser(ctx, usr, access);

        // We only want to resolve by name
        createduser.setId(null);
        // add user as groupmember
        addMemberToGroup(ctx, createdgroup, new User[]{createduser}, hosturl, cred);

        // We have to fetch the Group because the create on (createdgroup) doesn't contain
        // the members.
        final Group newgroup = getData(ctx, createdgroup, hosturl, cred);
        // now get all members of group, and check if user is member
        final Group[] remote_members = listGroupsForUser(ctx, createduser, hosturl, cred);
        assertTrue("members could not be loaded", remote_members.length > 0);
        // The right group must be the second one in the array, the first one is the all
        // users group
        assertEquals("no right group", newgroup, remote_members[1]);
    }

    private void changeGroup(final Context ctx, final Group grp, final String host, final Credentials cred) throws Exception {
        final OXGroupInterface xres = (OXGroupInterface) Naming.lookup(host + OXGroupInterface.RMI_NAME);
        xres.change(ctx, grp, cred);
    }

    private User[] getMembers(final Context ctx, final Group group, final String host, final Credentials cred) throws Exception {
        final OXGroupInterface xres = (OXGroupInterface) Naming.lookup(host + OXGroupInterface.RMI_NAME);
        return xres.getMembers(ctx, group, cred);
    }

    private Group[] listGroupsForUser(final Context ctx, final User usr, final String host, final Credentials cred) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException, DatabaseUpdateException, NoSuchUserException, MalformedURLException, NotBoundException {
        final OXGroupInterface xres = (OXGroupInterface) Naming.lookup(host + OXGroupInterface.RMI_NAME);
        return xres.listGroupsForUser(ctx, usr, cred);
    }

    private void addMemberToGroup(final Context ctx, final Group grp, final User[] members, final String host, final Credentials cred) throws Exception {
        final OXGroupInterface xres = (OXGroupInterface) Naming.lookup(host + OXGroupInterface.RMI_NAME);
        xres.addMember(ctx, grp, members, cred);
    }

    private void removeMemberFromGroup(final Context ctx, final Group grp, final User[] members, final String host, final Credentials cred) throws Exception {
        final OXGroupInterface xres = (OXGroupInterface) Naming.lookup(host + OXGroupInterface.RMI_NAME);
        xres.removeMember(ctx, grp, members, cred);
    }

    private Group getData(final Context ctx, final Group grp, final String host, final Credentials cred) throws Exception {
        final OXGroupInterface xres = (OXGroupInterface) Naming.lookup(host + OXGroupInterface.RMI_NAME);
        return xres.getData(ctx, grp, cred);
    }

    private Group createGroup(final Context ctx, final Group grp, final String host, final Credentials cred) throws Exception {
        final OXGroupInterface xres = (OXGroupInterface) Naming.lookup(host + OXGroupInterface.RMI_NAME);
        return xres.create(ctx, grp, cred);
    }

    private void deleteGroup(final Context ctx, final Group grp, final String host, final Credentials cred) throws Exception {
        final OXGroupInterface xres = (OXGroupInterface) Naming.lookup(host + OXGroupInterface.RMI_NAME);
        xres.delete(ctx, new Group[] { grp }, cred);
    }

    private Group[] searchGroup(final Context ctx, final String pattern, final String host, final Credentials cred) throws Exception {
        final OXGroupInterface xres = (OXGroupInterface) Naming.lookup(host + OXGroupInterface.RMI_NAME);
        return xres.list(ctx, pattern, cred);
    }

    private Group getTestGroupObject(final Credentials cred) throws Exception {
        final Context ctx = new Context(getContextID());
        return getTestGroupObject(VALID_CHAR_TESTGROUP+System.currentTimeMillis(), ctx, cred);
    }

    public static Group getTestGroupObject(final String ident, final Context ctx, final Credentials cred) throws Exception {
        final Group grp = new Group();
        grp.setDisplayname("display name " + ident);
        grp.setName(ident);
        return grp;
    }

    private int getContextID() throws Exception {
        return 1;
    }

    private void createChangeGroupData(final Group group) {
        if (group.getDisplayname() != null) {
            group.setDisplayname(group.getDisplayname() + change_suffix);
        }
        if (group.getName() != null) {
            group.setName(group.getName() + change_suffix);
        }
    }

    public static void compareGroup(final Group a, final Group b) {
        System.out.println("GROUPA" + a.toString());
        System.out.println("GROUPB" + b.toString());

        assertEquals("displayname not equal", a.getDisplayname(), b.getDisplayname());
        assertEquals("name not equal", a.getName(), b.getName());
        assertEquals("members not equal", a.getMembers(), b.getMembers());
        assertEquals("id not equal", a.getId(), b.getId());

        final Collection<OXCommonExtension> aexts = a.getAllExtensionsAsHash().values();
        final Collection<OXCommonExtension> bexts = b.getAllExtensionsAsHash().values();
        if (aexts.size() == bexts.size()) {
            aexts.containsAll(bexts);
//            for (int i = 0; i < aexts.size(); i++) {
//                final OXCommonExtensionInterface aext = aexts.get(i);
//                final OXCommonExtensionInterface bext = bexts.get(i);
//                assertTrue("Extensions not equal: " + aext.toString() + ",\n" + bext.toString(), aext.equals(bext));
//            }
        }
    }
}
