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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Collection;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.extensions.OXCommonExtensionInterface;
import com.openexchange.admin.rmi.manager.ContextManager;
import com.openexchange.admin.rmi.manager.GroupManager;

/**
 *
 * @author cutmasta
 * @author d7
 */
public class GroupTest extends UserTest {

    // list of chars that must be valid
    private final String VALID_CHAR_TESTGROUP = " abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+.%$@";

    private Context context;

    /**
     * Initialises a new {@link GroupTest}.
     */
    public GroupTest() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.AbstractTest#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        ContextManager cm = ContextManager.getInstance(getRMIHostUrl(), getMasterAdminCredentials());
        context = cm.createContext(getContextAdminCredentials());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.AbstractTest#tearDown()
     */
    @Override
    public void tearDown() throws Exception {
        ContextManager.getInstance(getRMIHostUrl(), getMasterAdminCredentials()).cleanUp();
        super.tearDown();
    }

    @Test
    public void testCreateGroup() throws Exception {
        Credentials cred = getContextAdminCredentials();
        int id = getGroupManager().createGroup(getTestGroupObject(cred), context, cred).getId();
        assertTrue("group id > 0 expected", id > 0);
    }

    @Test
    public void testSearchGroup() throws Exception {
        Credentials cred = getContextAdminCredentials();
        String grpname = VALID_CHAR_TESTGROUP + System.currentTimeMillis();
        getGroupManager().createGroup(getTestGroupObject(grpname, context, cred), context, cred);
        Group[] grps = getGroupManager().listGroups(context, grpname, cred);
        assertEquals("invalid search result", 1, grps.length);
    }

    @Test
    public void testDeleteGroup() throws Exception {
        Credentials cred = getContextAdminCredentials();
        Group grp = getGroupManager().createGroup(getTestGroupObject(VALID_CHAR_TESTGROUP + System.currentTimeMillis(), context, cred), context, cred);
        getGroupManager().deleteGroup(grp, context, cred);
        // now load the group again, this MUST fail
        try {
            getGroupManager().getData(grp, context, cred);
            fail("group not exists expected");
        } catch (Exception ecp) {
            if (ecp.toString().toLowerCase().indexOf("group does not exist") != -1) {
                // this exception MUST happen, if not, test MUST fail :)
                assertTrue(true);
            }
        }
    }

    @Test
    public void testCreateDeleteCreate() throws Exception {
        Credentials cred = getContextAdminCredentials();
        Group tmp = getTestGroupObject(VALID_CHAR_TESTGROUP + System.currentTimeMillis(), context, cred);
        Group grp = getGroupManager().createGroup(tmp, context, cred);
        getGroupManager().deleteGroup(grp, context, cred);
        // now load the group again, this MUST fail
        try {
            getGroupManager().getData(grp, context, cred);
            fail("group not exists expected");
        } catch (Exception ecp) {
            if (ecp.toString().toLowerCase().indexOf("group does not exist") != -1) {
                // this exception MUST happen, if not, test MUST fail :)
                assertTrue(true);
            }
        }

        // create same group again
        getGroupManager().createGroup(tmp, context, cred);
    }

    @Test
    public void testDeleteGroupIdentifiedByName() throws Exception {
        // delete group ident by name
        Credentials cred = getContextAdminCredentials();
        Group grp = getGroupManager().createGroup(getTestGroupObject(VALID_CHAR_TESTGROUP + System.currentTimeMillis(), context, cred), context, cred);

        Group del_grp = new Group();
        del_grp.setName(grp.getName());
        getGroupManager().deleteGroup(del_grp, context, cred);
        // now load the group again, this MUST fail
        try {
            getGroupManager().getData(grp, context, cred);
            fail("group not exists expected");
        } catch (Exception ecp) {
            if (ecp.toString().toLowerCase().indexOf("group does not exist") != -1) {
                // this exception MUST happen, if not, test MUST fail :)
                assertTrue(true);
            }
        }
    }

    @Test
    public void testDeleteGroupIdentifiedByID() throws Exception {
        // delete group ident by id
        Credentials cred = getContextAdminCredentials();
        Group grp = getGroupManager().createGroup(getTestGroupObject(VALID_CHAR_TESTGROUP + System.currentTimeMillis(), context, cred), context, cred);

        Group del_grp = new Group(grp.getId());
        getGroupManager().deleteGroup(del_grp, context, cred);
        // now load the group again, this MUST fail
        try {
            getGroupManager().getData(grp, context, cred);
            fail("group not exists expected");
        } catch (Exception ecp) {
            if (ecp.toString().toLowerCase().indexOf("group does not exist") != -1) {
                // this exception MUST happen, if not, test MUST fail :)
                assertTrue(true);
            }
        }
    }

    @Test
    public void testLoadGroup() throws Exception {
        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject(cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load from server
        Group srv_group = getGroupManager().getData(createdgroup, context, cred);

        // compare group fields
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_group.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_group.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_group.getName());
    }

    @Test
    public void testLoadGroupIdentifiedByName() throws Exception {
        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject(cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load from server
        Group tmp = new Group();
        tmp.setName(createdgroup.getName());
        Group srv_group = getGroupManager().getData(tmp, context, cred);

        // compare group fields
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_group.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_group.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_group.getName());
    }

    @Test
    public void testLoadGroupIdentifiedByID() throws Exception {
        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject(cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load from server
        Group tmp = new Group(createdgroup.getId());
        Group srv_group = getGroupManager().getData(tmp, context, cred);

        // compare group fields
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_group.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_group.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_group.getName());
    }

    @Test
    public void testAddMemberToGroup() throws Exception {
        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis(), context, cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // add user as groupmember
        getGroupManager().addMember(createdgroup, context, new User[] { createduser }, cred);

        // now get all members of group, and check if user is member
        User[] remote_members = getGroupManager().getMembers(createdgroup, context, cred);
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
        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis(), context, cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // add user as groupmember
        Group tmp = new Group();
        tmp.setName(createdgroup.getName());
        getGroupManager().addMember(tmp, context, new User[] { createduser }, cred);

        // now get all members of group, and check if user is member
        User[] remote_members = getGroupManager().getMembers(createdgroup, context, cred);
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
        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis(), context, cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // add user as groupmember
        Group tmp = new Group(createdgroup.getId());
        getGroupManager().addMember(tmp, context, new User[] { createduser }, cred);

        // now get all members of group, and check if user is member
        User[] remote_members = getGroupManager().getMembers(createdgroup, context, cred);
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
        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis(), context, cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add

        User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // add user as groupmember
        getGroupManager().addMember(createdgroup, context, new User[] { createduser }, cred);

        // now get all members of group, and check if user is member
        User[] remote_members = getGroupManager().getMembers(createdgroup, context, cred);
        boolean foundmember = false;
        for (User element : remote_members) {
            if (element.getId() == createduser.getId().intValue()) {
                foundmember = true;
            }
        }
        assertTrue("member not added to group", foundmember);

        // now remove user from group;
        getGroupManager().removeMember(createdgroup, context, new User[] { createduser }, cred);
        // now get all members of group, and check if user is member
        remote_members = getGroupManager().getMembers(createdgroup, context, cred);
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
        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis(), context, cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add

        User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // add user as groupmember
        getGroupManager().addMember(createdgroup, context, new User[] { createduser }, cred);

        // now get all members of group, and check if user is member
        User[] remote_members = getGroupManager().getMembers(createdgroup, context, cred);
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
        getGroupManager().removeMember(tmp, context, new User[] { createduser }, cred);

        // now get all members of group, and check if user is member
        remote_members = getGroupManager().getMembers(createdgroup, context, cred);
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
        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis(), context, cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add

        User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // add user as groupmember
        getGroupManager().addMember(createdgroup, context, new User[] { createduser }, cred);

        // now get all members of group, and check if user is member
        User[] remote_members = getGroupManager().getMembers(createdgroup, context, cred);
        boolean foundmember = false;
        for (User element : remote_members) {
            if (element.getId() == createduser.getId().intValue()) {
                foundmember = true;
            }
        }
        assertTrue("member not added to group", foundmember);

        // now remove user from group;
        Group tmp = new Group(createdgroup.getId());
        getGroupManager().removeMember(tmp, context, new User[] { createduser }, cred);

        // now get all members of group, and check if user is member
        remote_members = getGroupManager().getMembers(createdgroup, context, cred);
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
        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject("changed_this_group" + System.currentTimeMillis(), context, cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load group from server
        Group srv_response = getGroupManager().getData(createdgroup, context, cred);

        // check if group is created on server correct
        assertEquals("displayname not equal", createdgroup.getDisplayname(), srv_response.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_response.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_response.getName());

        // change the data of the group local
        createChangeGroupData(createdgroup);

        // do the changes on the remote server for the group
        getGroupManager().changeGroup(createdgroup, context, cred);

        // load group which was modified
        Group remote_grp = getGroupManager().getData(createdgroup, context, cred);

        // check if group is created on server correct
        assertEquals("displayname not equal", createdgroup.getDisplayname(), remote_grp.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), remote_grp.getId());
        assertEquals("identifier not equal", createdgroup.getName(), remote_grp.getName());
    }

    @Test
    public void testChangeGroupIdentifiedbyID() throws Exception {
        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject("changed_this_group" + System.currentTimeMillis(), context, cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load group from server
        Group srv_response = getGroupManager().getData(createdgroup, context, cred);

        // check if group is created on server correct
        assertEquals("displayname not equal", createdgroup.getDisplayname(), srv_response.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_response.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_response.getName());

        Group tmp_group = new Group(srv_response.getId());
        tmp_group.setDisplayname(srv_response.getDisplayname() + "_changed");

        // do the changes on the remote server for the group
        getGroupManager().changeGroup(tmp_group, context, cred);

        // load group which was modified
        Group remote_grp = getGroupManager().getData(createdgroup, context, cred);

        // check if group is change correctly on server
        assertEquals("displayname not equal", tmp_group.getDisplayname(), remote_grp.getDisplayname());

    }

    @Test(expected = InvalidDataException.class)
    public void testChangeNull() throws Exception {

        // change group displayname and name to null, this must fail

        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject("changed_this_group" + System.currentTimeMillis(), context, cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load group from server
        Group srv_response = getGroupManager().getData(createdgroup, context, cred);

        // check if group is created on server correct
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_response.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_response.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_response.getName());

        // set display name to null and name to null
        Group tmp_group = new Group(srv_response.getId());
        tmp_group.setDisplayname(null);
        tmp_group.setName(null);

        // do the changes on the remote server for the group
        getGroupManager().changeGroup(tmp_group, context, cred);
    }

    @Test
    public void testChangeAllowedNull() throws Exception {
        // change group displayname and name to null, this must fail
        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject("changed_this_group" + System.currentTimeMillis(), context, cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load group from server
        Group srv_response = getGroupManager().getData(createdgroup, context, cred);

        // check if group is created on server correct
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_response.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_response.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_response.getName());

        // set display name to null and name to null
        Group tmp_group = new Group(srv_response.getId());
        tmp_group.setMembers(null);

        // do the changes on the remote server for the group
        getGroupManager().changeGroup(tmp_group, context, cred);

        // load group which was modified
        Group remote_grp = getGroupManager().getData(createdgroup, context, cred);

        createdgroup.setMembers(new Integer[0]);
        assertEquals("Group aren't equal", createdgroup, remote_grp);
    }

    @Test
    public void testChangeGroupIdentifiedbyName() throws Exception {
        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject("changed_this_group" + System.currentTimeMillis(), context, cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load group from server
        Group srv_response = getGroupManager().getData(createdgroup, context, cred);

        // check if group is created on server correct
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_response.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_response.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_response.getName());

        Group tmp_group = new Group();
        tmp_group.setDisplayname(srv_response.getDisplayname() + "_changed");
        tmp_group.setName(srv_response.getName());
        // do the changes on the remote server for the group
        getGroupManager().changeGroup(tmp_group, context, cred);

        // load group which was modified
        Group remote_grp = getGroupManager().getData(createdgroup, context, cred);

        // check if group is changed correctly on server
        assertEquals("displayname id not equal", tmp_group.getDisplayname(), remote_grp.getDisplayname());
    }

    @Test
    public void testGetMembers() throws Exception {
        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis(), context, cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // add user as groupmember
        getGroupManager().addMember(createdgroup, context, new User[] { createduser }, cred);

        // now get all members of group, and check if user is member
        User[] remote_members = getGroupManager().getMembers(createdgroup, context, cred);
        assertTrue("members could not be loaded", remote_members.length > 0);
    }

    @Test
    public void testGetMembersByName() throws Exception {
        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis(), context, cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // We only want to resolve by name
        createdgroup.setId(null);
        createduser.setId(null);
        // add user as groupmember
        getGroupManager().addMember(createdgroup, context, new User[] { createduser }, cred);

        // now get all members of group, and check if user is member
        User[] remote_members = getGroupManager().getMembers(createdgroup, context, cred);
        assertTrue("members could not be loaded", remote_members.length > 0);
    }

    @Test
    public void testlistGroupsForUser() throws Exception {
        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis(), context, cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // We only want to resolve by name
        createduser.setId(null);
        // add user as groupmember
        getGroupManager().addMember(createdgroup, context, new User[] { createduser }, cred);

        // We have to fetch the Group because the create on (createdgroup) doesn't contain
        // the members.
        Group newgroup = getGroupManager().getData(createdgroup, context, cred);
        // now get all members of group, and check if user is member
        Group[] remote_members = getGroupManager().listUserGroups(usr, context, cred);
        assertTrue("members could not be loaded", remote_members.length > 0);
        // The right group must be the second one in the array, the first one is the all
        // users group
        assertEquals("no right group", newgroup, remote_members[1]);
    }

    @Test
    public void testlistGroupsForUserByName() throws Exception {
        Credentials cred = getContextAdminCredentials();
        Group addgroup = getTestGroupObject("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis(), context, cred);
        Group createdgroup = getGroupManager().createGroup(addgroup, context, cred);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        User usr = UserTest.getTestUserObject("groupmemberadduser" + System.currentTimeMillis(), "netline", context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // We only want to resolve by name
        createduser.setId(null);
        // add user as groupmember
        getGroupManager().addMember(createdgroup, context, new User[] { createduser }, cred);

        // We have to fetch the Group because the create on (createdgroup) doesn't contain
        // the members.
        Group newgroup = getGroupManager().getData(createdgroup, context, cred);
        // now get all members of group, and check if user is member
        Group[] remote_members = getGroupManager().listUserGroups(createduser, context, cred);
        assertTrue("members could not be loaded", remote_members.length > 0);
        // The right group must be the second one in the array, the first one is the all
        // users group
        assertEquals("no right group", newgroup, remote_members[1]);
    }

    private Group getTestGroupObject(Credentials cred) throws Exception {
        return getTestGroupObject(VALID_CHAR_TESTGROUP + System.currentTimeMillis(), context, cred);
    }

    public static Group getTestGroupObject(String ident, Context context, Credentials cred) throws Exception {
        Group grp = new Group();
        grp.setDisplayname("display name " + ident);
        grp.setName(ident);
        return grp;
    }

    private void createChangeGroupData(Group group) {
        if (group.getDisplayname() != null) {
            group.setDisplayname(group.getDisplayname() + change_suffix);
        }
        if (group.getName() != null) {
            group.setName(group.getName() + change_suffix);
        }
    }

    public static void compareGroup(Group a, Group b) {
        System.out.println("GROUPA" + a.toString());
        System.out.println("GROUPB" + b.toString());

        assertEquals("displayname not equal", a.getDisplayname(), b.getDisplayname());
        assertEquals("name not equal", a.getName(), b.getName());
        assertArrayEquals("members not equal", a.getMembers(), b.getMembers());
        assertEquals("id not equal", a.getId(), b.getId());

        Collection<OXCommonExtensionInterface> aexts = a.getAllExtensionsAsHash().values();
        Collection<OXCommonExtensionInterface> bexts = b.getAllExtensionsAsHash().values();
        if (aexts.size() == bexts.size()) {
            aexts.containsAll(bexts);
        }
    }

    /**
     * Returns the {@link GroupManager} instance
     * 
     * @return the {@link GroupManager} instance
     */
    private GroupManager getGroupManager() {
        return GroupManager.getInstance(getRMIHostUrl(), getMasterAdminCredentials());
    }
}
