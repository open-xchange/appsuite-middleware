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
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.factory.GroupFactory;
import com.openexchange.admin.rmi.factory.UserFactory;

/**
 *
 * @author cutmasta
 * @author d7
 */
public class GroupTest extends UserTest {

    private final String VALID_CHAR_TESTGROUP = " abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+.%$@";

    @Test
    public void testCreateGroup() throws Exception {
        int id = getGroupManager().create(GroupFactory.createGroup(VALID_CHAR_TESTGROUP + System.currentTimeMillis()), context, contextAdminCredentials).getId();
        assertTrue("group id > 0 expected", id > 0);
    }

    @Test
    public void testSearchGroup() throws Exception {
        String grpname = VALID_CHAR_TESTGROUP + System.currentTimeMillis();
        getGroupManager().create(GroupFactory.createGroup(grpname), context, contextAdminCredentials);
        Group[] grps = getGroupManager().search(context, grpname, contextAdminCredentials);
        assertEquals("invalid search result", 1, grps.length);
    }

    @Test
    public void testDeleteGroup() throws Exception {
        Group grp = getGroupManager().create(GroupFactory.createGroup(VALID_CHAR_TESTGROUP + System.currentTimeMillis()), context, contextAdminCredentials);
        getGroupManager().delete(grp, context, contextAdminCredentials);
        // now load the group again, this MUST fail
        try {
            getGroupManager().getData(grp, context, contextAdminCredentials);
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
        Group tmp = GroupFactory.createGroup(VALID_CHAR_TESTGROUP + System.currentTimeMillis());
        Group grp = getGroupManager().create(tmp, context, contextAdminCredentials);
        getGroupManager().delete(grp, context, contextAdminCredentials);
        // now load the group again, this MUST fail
        try {
            getGroupManager().getData(grp, context, contextAdminCredentials);
            fail("group not exists expected");
        } catch (Exception ecp) {
            if (ecp.toString().toLowerCase().indexOf("group does not exist") != -1) {
                // this exception MUST happen, if not, test MUST fail :)
                assertTrue(true);
            }
        }

        // create same group again
        getGroupManager().create(tmp, context, contextAdminCredentials);
    }

    @Test
    public void testDeleteGroupIdentifiedByName() throws Exception {
        // delete group ident by name
        Group grp = getGroupManager().create(GroupFactory.createGroup(VALID_CHAR_TESTGROUP + System.currentTimeMillis()), context, contextAdminCredentials);

        Group del_grp = new Group();
        del_grp.setName(grp.getName());
        getGroupManager().delete(del_grp, context, contextAdminCredentials);
        // now load the group again, this MUST fail
        try {
            getGroupManager().getData(grp, context, contextAdminCredentials);
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
        Credentials cred = contextAdminCredentials;
        Group grp = getGroupManager().create(GroupFactory.createGroup(VALID_CHAR_TESTGROUP + System.currentTimeMillis()), context, cred);

        Group del_grp = new Group(grp.getId());
        getGroupManager().delete(del_grp, context, cred);
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
        Group addgroup = GroupFactory.createGroup(VALID_CHAR_TESTGROUP + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load from server
        Group srv_group = getGroupManager().getData(createdgroup, context, contextAdminCredentials);

        // compare group fields
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_group.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_group.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_group.getName());
    }

    @Test
    public void testLoadGroupIdentifiedByName() throws Exception {
        Group addgroup = GroupFactory.createGroup(VALID_CHAR_TESTGROUP + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load from server
        Group tmp = new Group();
        tmp.setName(createdgroup.getName());
        Group srv_group = getGroupManager().getData(tmp, context, contextAdminCredentials);

        // compare group fields
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_group.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_group.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_group.getName());
    }

    @Test
    public void testLoadGroupIdentifiedByID() throws Exception {
        Group addgroup = GroupFactory.createGroup(VALID_CHAR_TESTGROUP + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load from server
        Group tmp = new Group(createdgroup.getId());
        Group srv_group = getGroupManager().getData(tmp, context, contextAdminCredentials);

        // compare group fields
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_group.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_group.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_group.getName());
    }

    @Test
    public void testAddMemberToGroup() throws Exception {
        Group addgroup = GroupFactory.createGroup("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        User usr = UserFactory.createUser("groupmemberadduser" + System.currentTimeMillis(), "netline", TEST_DOMAIN, context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // add user as groupmember
        getGroupManager().addMember(createdgroup, context, new User[] { createduser }, contextAdminCredentials);

        // now get all members of group, and check if user is member
        User[] remote_members = getGroupManager().getMembers(createdgroup, context, contextAdminCredentials);
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
        Group addgroup = GroupFactory.createGroup("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        User usr = UserFactory.createUser("groupmemberadduser" + System.currentTimeMillis(), "netline", TEST_DOMAIN, context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // add user as groupmember
        Group tmp = new Group();
        tmp.setName(createdgroup.getName());
        getGroupManager().addMember(tmp, context, new User[] { createduser }, contextAdminCredentials);

        // now get all members of group, and check if user is member
        User[] remote_members = getGroupManager().getMembers(createdgroup, context, contextAdminCredentials);
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
        Group addgroup = GroupFactory.createGroup("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        User usr = UserFactory.createUser("groupmemberadduser" + System.currentTimeMillis(), "netline", TEST_DOMAIN, context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // add user as groupmember
        Group tmp = new Group(createdgroup.getId());
        getGroupManager().addMember(tmp, context, new User[] { createduser }, contextAdminCredentials);

        // now get all members of group, and check if user is member
        User[] remote_members = getGroupManager().getMembers(createdgroup, context, contextAdminCredentials);
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
        Group addgroup = GroupFactory.createGroup("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add

        User usr = UserFactory.createUser("groupmemberadduser" + System.currentTimeMillis(), "netline", TEST_DOMAIN, context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // add user as groupmember
        getGroupManager().addMember(createdgroup, context, new User[] { createduser }, contextAdminCredentials);

        // now get all members of group, and check if user is member
        User[] remote_members = getGroupManager().getMembers(createdgroup, context, contextAdminCredentials);
        boolean foundmember = false;
        for (User element : remote_members) {
            if (element.getId() == createduser.getId().intValue()) {
                foundmember = true;
            }
        }
        assertTrue("member not added to group", foundmember);

        // now remove user from group;
        getGroupManager().removeMember(createdgroup, context, new User[] { createduser }, contextAdminCredentials);
        // now get all members of group, and check if user is member
        remote_members = getGroupManager().getMembers(createdgroup, context, contextAdminCredentials);
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
        Group addgroup = GroupFactory.createGroup("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add

        User usr = UserFactory.createUser("groupmemberadduser" + System.currentTimeMillis(), "netline", TEST_DOMAIN, context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // add user as groupmember
        getGroupManager().addMember(createdgroup, context, new User[] { createduser }, contextAdminCredentials);

        // now get all members of group, and check if user is member
        User[] remote_members = getGroupManager().getMembers(createdgroup, context, contextAdminCredentials);
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
        getGroupManager().removeMember(tmp, context, new User[] { createduser }, contextAdminCredentials);

        // now get all members of group, and check if user is member
        remote_members = getGroupManager().getMembers(createdgroup, context, contextAdminCredentials);
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
        Group addgroup = GroupFactory.createGroup("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add

        User usr = UserFactory.createUser("groupmemberadduser" + System.currentTimeMillis(), "netline", TEST_DOMAIN, context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // add user as groupmember
        getGroupManager().addMember(createdgroup, context, new User[] { createduser }, contextAdminCredentials);

        // now get all members of group, and check if user is member
        User[] remote_members = getGroupManager().getMembers(createdgroup, context, contextAdminCredentials);
        boolean foundmember = false;
        for (User element : remote_members) {
            if (element.getId() == createduser.getId().intValue()) {
                foundmember = true;
            }
        }
        assertTrue("member not added to group", foundmember);

        // now remove user from group;
        Group tmp = new Group(createdgroup.getId());
        getGroupManager().removeMember(tmp, context, new User[] { createduser }, contextAdminCredentials);

        // now get all members of group, and check if user is member
        remote_members = getGroupManager().getMembers(createdgroup, context, contextAdminCredentials);
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
        Group addgroup = GroupFactory.createGroup("changed_this_group" + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load group from server
        Group srv_response = getGroupManager().getData(createdgroup, context, contextAdminCredentials);

        // check if group is created on server correct
        assertEquals("displayname not equal", createdgroup.getDisplayname(), srv_response.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_response.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_response.getName());

        // change the data of the group local
        createChangeGroupData(createdgroup);

        // do the changes on the remote server for the group
        getGroupManager().change(createdgroup, context, contextAdminCredentials);

        // load group which was modified
        Group remote_grp = getGroupManager().getData(createdgroup, context, contextAdminCredentials);

        // check if group is created on server correct
        assertEquals("displayname not equal", createdgroup.getDisplayname(), remote_grp.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), remote_grp.getId());
        assertEquals("identifier not equal", createdgroup.getName(), remote_grp.getName());
    }

    @Test
    public void testChangeGroupIdentifiedbyID() throws Exception {
        Group addgroup = GroupFactory.createGroup("changed_this_group" + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load group from server
        Group srv_response = getGroupManager().getData(createdgroup, context, contextAdminCredentials);

        // check if group is created on server correct
        assertEquals("displayname not equal", createdgroup.getDisplayname(), srv_response.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_response.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_response.getName());

        Group tmp_group = new Group(srv_response.getId());
        tmp_group.setDisplayname(srv_response.getDisplayname() + "_changed");

        // do the changes on the remote server for the group
        getGroupManager().change(tmp_group, context, contextAdminCredentials);

        // load group which was modified
        Group remote_grp = getGroupManager().getData(createdgroup, context, contextAdminCredentials);

        // check if group is change correctly on server
        assertEquals("displayname not equal", tmp_group.getDisplayname(), remote_grp.getDisplayname());

    }

    @Test(expected = InvalidDataException.class)
    public void testChangeNull() throws Exception {
        // change group displayname and name to null, this must fail
        Group addgroup = GroupFactory.createGroup("changed_this_group" + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load group from server
        Group srv_response = getGroupManager().getData(createdgroup, context, contextAdminCredentials);

        // check if group is created on server correct
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_response.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_response.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_response.getName());

        // set display name to null and name to null
        Group tmp_group = new Group(srv_response.getId());
        tmp_group.setDisplayname(null);
        tmp_group.setName(null);

        // do the changes on the remote server for the group
        getGroupManager().change(tmp_group, context, contextAdminCredentials);
    }

    @Test
    public void testChangeAllowedNull() throws Exception {
        // change group displayname and name to null, this must fail
        Group addgroup = GroupFactory.createGroup("changed_this_group" + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load group from server
        Group srv_response = getGroupManager().getData(createdgroup, context, contextAdminCredentials);

        // check if group is created on server correct
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_response.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_response.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_response.getName());

        // set display name to null and name to null
        Group tmp_group = new Group(srv_response.getId());
        tmp_group.setMembers(null);

        // do the changes on the remote server for the group
        getGroupManager().change(tmp_group, context, contextAdminCredentials);

        // load group which was modified
        Group remote_grp = getGroupManager().getData(createdgroup, context, contextAdminCredentials);

        createdgroup.setMembers(new Integer[0]);
        assertEquals("Group aren't equal", createdgroup, remote_grp);
    }

    @Test
    public void testChangeGroupIdentifiedbyName() throws Exception {
        Group addgroup = GroupFactory.createGroup("changed_this_group" + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("expected id > 0", createdgroup.getId() > 0);

        // load group from server
        Group srv_response = getGroupManager().getData(createdgroup, context, contextAdminCredentials);

        // check if group is created on server correct
        assertEquals("displayname id not equal", createdgroup.getDisplayname(), srv_response.getDisplayname());
        assertEquals("id not equals", createdgroup.getId(), srv_response.getId());
        assertEquals("identifier not equal", createdgroup.getName(), srv_response.getName());

        Group tmp_group = new Group();
        tmp_group.setDisplayname(srv_response.getDisplayname() + "_changed");
        tmp_group.setName(srv_response.getName());
        // do the changes on the remote server for the group
        getGroupManager().change(tmp_group, context, contextAdminCredentials);

        // load group which was modified
        Group remote_grp = getGroupManager().getData(createdgroup, context, contextAdminCredentials);

        // check if group is changed correctly on server
        assertEquals("displayname id not equal", tmp_group.getDisplayname(), remote_grp.getDisplayname());
    }

    @Test
    public void testGetMembers() throws Exception {
        Group addgroup = GroupFactory.createGroup("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        User usr = UserFactory.createUser("groupmemberadduser" + System.currentTimeMillis(), "netline", TEST_DOMAIN, context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // add user as groupmember
        getGroupManager().addMember(createdgroup, context, new User[] { createduser }, contextAdminCredentials);

        // now get all members of group, and check if user is member
        User[] remote_members = getGroupManager().getMembers(createdgroup, context, contextAdminCredentials);
        assertTrue("members could not be loaded", remote_members.length > 0);
    }

    @Test
    public void testGetMembersByName() throws Exception {
        Group addgroup = GroupFactory.createGroup("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        User usr = UserFactory.createUser("groupmemberadduser" + System.currentTimeMillis(), "netline", TEST_DOMAIN, context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // We only want to resolve by name
        createdgroup.setId(null);
        createduser.setId(null);
        // add user as groupmember
        getGroupManager().addMember(createdgroup, context, new User[] { createduser }, contextAdminCredentials);

        // now get all members of group, and check if user is member
        User[] remote_members = getGroupManager().getMembers(createdgroup, context, contextAdminCredentials);
        assertTrue("members could not be loaded", remote_members.length > 0);
    }

    @Test
    public void testlistGroupsForUser() throws Exception {
        Group addgroup = GroupFactory.createGroup("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        User usr = UserFactory.createUser("groupmemberadduser" + System.currentTimeMillis(), "netline", TEST_DOMAIN, context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // We only want to resolve by name
        createduser.setId(null);
        // add user as groupmember
        getGroupManager().addMember(createdgroup, context, new User[] { createduser }, contextAdminCredentials);

        // We have to fetch the Group because the create on (createdgroup) doesn't contain
        // the members.
        Group newgroup = getGroupManager().getData(createdgroup, context, contextAdminCredentials);
        // now get all members of group, and check if user is member
        Group[] remote_members = getGroupManager().listUserGroups(usr, context, contextAdminCredentials);
        assertTrue("members could not be loaded", remote_members.length > 0);
        // The right group must be the second one in the array, the first one is the all
        // users group
        assertEquals("no right group", newgroup, remote_members[1]);
    }

    @Test
    public void testlistGroupsForUserByName() throws Exception {
        Group addgroup = GroupFactory.createGroup("memberaddgroup" + VALID_CHAR_TESTGROUP + System.currentTimeMillis());
        Group createdgroup = getGroupManager().create(addgroup, context, contextAdminCredentials);
        assertTrue("group id > 0 expected", createdgroup.getId() > 0);

        // create user to add
        User usr = UserFactory.createUser("groupmemberadduser" + System.currentTimeMillis(), "netline", TEST_DOMAIN, context);
        UserModuleAccess access = new UserModuleAccess();
        User createduser = addUser(context, usr, access);

        // We only want to resolve by name
        createduser.setId(null);
        // add user as groupmember
        getGroupManager().addMember(createdgroup, context, new User[] { createduser }, contextAdminCredentials);

        // We have to fetch the Group because the create on (createdgroup) doesn't contain
        // the members.
        Group newgroup = getGroupManager().getData(createdgroup, context, contextAdminCredentials);
        // now get all members of group, and check if user is member
        Group[] remote_members = getGroupManager().listUserGroups(createduser, context, contextAdminCredentials);
        assertTrue("members could not be loaded", remote_members.length > 0);
        // The right group must be the second one in the array, the first one is the all
        // users group
        assertEquals("no right group", newgroup, remote_members[1]);
    }

    private void createChangeGroupData(Group group) {
        if (group.getDisplayname() != null) {
            group.setDisplayname(group.getDisplayname() + change_suffix);
        }
        if (group.getName() != null) {
            group.setName(group.getName() + change_suffix);
        }
    }
}
