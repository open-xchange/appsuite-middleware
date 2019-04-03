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

package com.openexchange.config.admin.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.DatabaseFolder;
import com.openexchange.folderstorage.internal.UserizedFolderImpl;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.user.UserService;

/**
 * {@link HideAdminServiceImplTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(HideAdminServiceImpl.class)
public class HideAdminServiceImplTest {

    private HideAdminServiceImpl serviceSpy;

    @Mock
    private LeanConfigurationService leanConfigurationService;

    @Mock
    private UserService userService;

    @Mock
    private ContextService contextService;

    private int contextId = 11;

    private int adminUserId = 3;

    private int adminContactId = 2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        serviceSpy = PowerMockito.spy(service);

        PowerMockito.doReturn(adminUserId).when(serviceSpy, "getAdminUserId", ArgumentMatchers.anyInt());
        PowerMockito.doReturn(adminContactId).when(serviceSpy, "getAdminContactId", ArgumentMatchers.anyInt());
        PowerMockito.doReturn(false).when(serviceSpy, "showAdmin", ArgumentMatchers.anyInt());
    }

    @Test
    public void testShowAdmin_contextIdZero_returnTrue() {
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);

        boolean showAdmin = service.showAdmin(0);

        assertTrue(showAdmin);
    }

    @Test
    public void testShowAdmin_disabled_returnFalse() {
        Mockito.when(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any())).thenReturn(Boolean.FALSE.booleanValue());
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);

        boolean showAdmin = service.showAdmin(contextId);

        assertFalse(showAdmin);
    }

    @Test
    public void testShowAdmin_enabled_returnTrue() {
        Mockito.when(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any())).thenReturn(Boolean.TRUE.booleanValue());
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);

        boolean showAdmin = service.showAdmin(contextId);

        assertTrue(showAdmin);
    }

    //============================================================================================================//
    //============================================================================================================//

    @Test
    public void testRemoveAdminFromContacts_contactsNull_returnNull() throws OXException {
        try (SearchIterator<Contact> result = serviceSpy.removeAdminFromContacts(contextId, null)) {
            assertNull(result);
        }
    }

    @Test
    public void testRemoveAdminFromContacts_contactsEmpty_returnEmptyIterator() throws OXException {
        try (SearchIterator<Contact> result = serviceSpy.removeAdminFromContacts(contextId, getContacts())) {
            assertEquals(0, result.size());
        }

    }

    @Test
    public void testRemoveAdminFromContacts_featureDisabled_returnAllContacts() throws OXException {
        Mockito.when(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any())).thenReturn(Boolean.TRUE.booleanValue());
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> contactIds = Arrays.asList(adminContactId, 3, 4, 5, 6);

        try (SearchIterator<Contact> org = getContacts(contactIds.stream().mapToInt(i -> i).toArray()); SearchIterator<Contact> result = service.removeAdminFromContacts(contextId, org)) {
            assertTrue(org.equals(result));
        }
    }

    @Test
    public void testRemoveAdminFromContacts_contactsWithoutAdmin_returnAllContacts() throws OXException {
        List<Integer> contactIds = Arrays.asList(3, 4, 5, 6);

        try (SearchIterator<Contact> result = serviceSpy.removeAdminFromContacts(contextId, getContacts(contactIds.stream().mapToInt(i -> i).toArray()))) {
            assertEquals(contactIds.size(), SearchIterators.asList(result).size());
        }
    }

    @Test
    public void testRemoveAdminFromContacts_contactsWithAdmin_returnWithoutContacts() throws OXException {
        List<Integer> contactIds = Arrays.asList(2, 3, 4, 5, 6);

        try (SearchIterator<Contact> result = serviceSpy.removeAdminFromContacts(contextId, getContacts(contactIds.stream().mapToInt(i -> i).toArray()))) {

            assertEquals(contactIds.size() - 1, SearchIterators.asList(result).size());
            List<Contact> returnedContacts = SearchIterators.asList(result);
            for (Contact contact : returnedContacts) {
                assertNotEquals(adminContactId, contact.getObjectID());
            }
        }
    }

    private static SearchIterator<Contact> getContacts(int... ids) {
        List<Contact> retval = new LinkedList<Contact>();
        for (int id : ids) {
            Contact contact = new Contact();
            contact.setObjectID(id);
            retval.add(contact);
        }
        return new SearchIteratorAdapter<Contact>(retval.iterator(), retval.size());
    }

    //============================================================================================================//
    //============================================================================================================//

    @Test
    public void testRemoveAdminFromGroups_groupsNull_returnNull() throws OXException {
        Group[] result = serviceSpy.removeAdminFromGroupMemberList(contextId, null);

        assertNull(result);
    }

    @Test
    public void testRemoveAdminFromGroups_groupsEmpty_returnEmpty() throws OXException {
        Group[] groups = new ArrayList<>().stream().toArray(Group[]::new);
        Group[] result = serviceSpy.removeAdminFromGroupMemberList(contextId, groups);

        assertEquals(0, result.length);
        assertTrue(groups.equals(result));
    }

    private static Group[] getGroups(int... ids) {
        List<Group> groups = new ArrayList<>();
        for (int id : ids) {
            Group group = new Group();
            group.setMember(ids);
            group.setIdentifier(id);
            groups.add(group);
        }
        return groups.stream().toArray(Group[]::new);
    }

    @Test
    public void testRemoveAdminFromGroups_groupsWithoutAdmin_returnGroups() throws OXException {
        List<Integer> groupsAndMembers = Arrays.asList(6, 7, 8, 9);
        Group[] groups = getGroups(groupsAndMembers.stream().mapToInt(i -> i).toArray());

        Group[] result = serviceSpy.removeAdminFromGroupMemberList(contextId, groups);

        assertEquals(groupsAndMembers.size(), result.length);
        for (int j = 0; j < result.length; j++) {
            assertTrue(groupsAndMembers.contains(result[j].getIdentifier()));
            int[] member = result[j].getMember();
            assertEquals(groupsAndMembers.size(), member.length);
            for (int k : member) {
                assertTrue(groupsAndMembers.contains(k));
            }
        }
    }

    @Test
    public void testRemoveAdminFromGroups_groupsContainAdmin_returnGroupsWithRemovedAdmin() throws OXException {
        List<Integer> groupsAndMembers = Arrays.asList(adminUserId, 6, 7, 8, 9);
        Group[] groups = getGroups(groupsAndMembers.stream().mapToInt(i -> i).toArray());

        Group[] result = serviceSpy.removeAdminFromGroupMemberList(contextId, groups);

        assertEquals(groupsAndMembers.size(), result.length);
        for (int j = 0; j < result.length; j++) {
            assertTrue(groupsAndMembers.contains(result[j].getIdentifier()));
            int[] member = result[j].getMember();
            assertEquals(groupsAndMembers.size() - 1, member.length);
            for (int k : member) {
                assertTrue(groupsAndMembers.contains(k));
            }
        }
        assertFalse(Arrays.stream(result).filter(i -> Arrays.stream(i.getMember()).anyMatch(j -> j == adminUserId)).peek(i -> System.out.println(i.getIdentifier())).findFirst().isPresent());
    }

    @Test
    public void testRemoveAdminFromGroups_featureDisabled_returnGroupsWithAdmin() throws OXException {
        Mockito.when(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any())).thenReturn(Boolean.TRUE.booleanValue());
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> groupsAndMembers = Arrays.asList(adminUserId, 6, 7, 8, 9);
        Group[] groups = getGroups(groupsAndMembers.stream().mapToInt(i -> i).toArray());

        Group[] result = service.removeAdminFromGroupMemberList(contextId, groups);

        assertTrue(groups.equals(result));
        assertTrue(Arrays.stream(result).filter(i -> Arrays.stream(i.getMember()).anyMatch(j -> j == adminUserId)).peek(i -> System.out.println(i.getIdentifier())).findFirst().isPresent());
    }

    //============================================================================================================//
    //============================================================================================================//

    @Test
    public void testAddAdminToGroupIds_orginalNull_returnUpdate() throws OXException {
        List<Integer> groupMembers = Arrays.asList(adminUserId, 6, 7, 8, 9);
        int[] updatedGroupMember = groupMembers.stream().mapToInt(i -> i).toArray();

        int[] result = serviceSpy.addAdminToGroupMemberList(contextId, null, updatedGroupMember);

        assertTrue(updatedGroupMember.equals(result));
        assertEquals(updatedGroupMember.length, result.length);
    }

    @Test
    public void testAddAdminToGroupIds_updatedNull_returnUpdate() throws OXException {
        List<Integer> groupMembers = Arrays.asList(adminUserId, 6, 7, 8, 9);
        int[] origGroupMember = groupMembers.stream().mapToInt(i -> i).toArray();

        int[] result = serviceSpy.addAdminToGroupMemberList(contextId, origGroupMember, null);

        assertNull(result);
    }

    @Test
    public void testAddAdminToGroupIds_adminNotInOriginal_returnUpdate() throws OXException {
        List<Integer> groupMembers = Arrays.asList(6, 7, 8, 9);
        int[] origGroupMember = groupMembers.stream().mapToInt(i -> i).toArray();
        List<Integer> updateGroupMembers = Arrays.asList(adminUserId, 11, 22, 33, 9);
        int[] updateGroupMembersArray = updateGroupMembers.stream().mapToInt(i -> i).toArray();

        int[] result = serviceSpy.addAdminToGroupMemberList(contextId, origGroupMember, updateGroupMembersArray);

        assertEquals(updateGroupMembers.size(), result.length);
        for (int j = 0; j < result.length; j++) {
            assertTrue(updateGroupMembers.contains(result[j]));
        }
    }

    @Test
    public void testAddAdminToGroupIds_adminInOriginal_returnAdminInUpdate() throws OXException {
        List<Integer> groupMembers = Arrays.asList(adminUserId, 6, 7, 8, 9);
        int[] origGroupMember = groupMembers.stream().mapToInt(i -> i).toArray();
        List<Integer> updateGroupMembers = Arrays.asList(11, 22, 33, 9);
        int[] updateGroupMembersArray = updateGroupMembers.stream().mapToInt(i -> i).toArray();

        int[] result = serviceSpy.addAdminToGroupMemberList(contextId, origGroupMember, updateGroupMembersArray);

        List<Integer> expected = new ArrayList<>(updateGroupMembers);
        expected.add(adminUserId);
        assertEquals(expected.size(), result.length);
        boolean adminFound = false;
        for (int j = 0; j < result.length; j++) {
            assertTrue(expected.contains(result[j]));
            if (result[j] == adminUserId) {
                adminFound = true;
            }
        }
        assertTrue(adminFound);
    }

    @Test
    public void testAddAdminToGroupIds_adminInOriginalAndUpdate_returnAdminInUpdate() throws OXException {
        List<Integer> groupMembers = Arrays.asList(adminUserId, 6, 7, 8, 9);
        int[] origGroupMember = groupMembers.stream().mapToInt(i -> i).toArray();
        List<Integer> updateGroupMembers = Arrays.asList(11, 22, 33, 9, adminUserId);
        int[] updateGroupMembersArray = updateGroupMembers.stream().mapToInt(i -> i).toArray();

        int[] result = serviceSpy.addAdminToGroupMemberList(contextId, origGroupMember, updateGroupMembersArray);

        assertEquals(updateGroupMembers.size(), result.length);
    }

    @Test
    public void testAddAdminToGroupIds_featureDisabled_returnUpdate() throws OXException {
        Mockito.when(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any())).thenReturn(Boolean.TRUE.booleanValue());
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> groupMembers = Arrays.asList(adminUserId, 6, 7, 8, 9);
        int[] origGroupMember = groupMembers.stream().mapToInt(i -> i).toArray();
        List<Integer> updateGroupMembers = Arrays.asList(11, 22, 33, 9);
        int[] updateGroupMembersArray = updateGroupMembers.stream().mapToInt(i -> i).toArray();

        int[] result = service.addAdminToGroupMemberList(contextId, origGroupMember, updateGroupMembersArray);

        assertTrue(updateGroupMembersArray.equals(result));
        assertFalse(Arrays.stream(result).anyMatch(i -> i == adminUserId));
    }

    private static UserizedFolder[] getUserizedFolders(int... ids) {
        List<UserizedFolder> userizedFolders = new ArrayList<>();
        for (int id : ids) {
            UserizedFolder folder = new UserizedFolderImpl(new DatabaseFolder(new FolderObject(id)), null, null, null);
            Permission[] permissions = getPermissions(ids);
            folder.setPermissions(permissions);
            userizedFolders.add(folder);
        }
        return userizedFolders.stream().toArray(UserizedFolder[]::new);
    }

    private static Permission[] getPermissions(int... ids) {
        List<Permission> permissions = new ArrayList<>();
        for (int id : ids) {
            Permission permission = new BasicPermission();
            permission.setEntity(id);
            permissions.add(permission);
        }
        return permissions.stream().toArray(Permission[]::new);
    }

    //============================================================================================================//
    //============================================================================================================//

    @Test
    public void testRemoveAdminFromFolderPermissions_folderListNull_returnNull() throws OXException {
        UserizedFolder[] result = serviceSpy.removeAdminFromFolderPermissions(contextId, null);

        assertNull(result);
    }

    @Test
    public void testRemoveAdminFromFolderPermissions_folderListEmpty_returnEmptyFolderList() throws OXException {
        UserizedFolder[] folders = getUserizedFolders();

        UserizedFolder[] result = serviceSpy.removeAdminFromFolderPermissions(contextId, folders);

        assertTrue(folders.equals(result));
    }

    @Test
    public void testRemoveAdminFromFolderPermissions_folderDoesNotContainAdminPermission_returnFolderList() throws OXException {
        List<Integer> foldersAndPermissionIdentifiers = Arrays.asList(4, 5, 6, 7);
        UserizedFolder[] folders = getUserizedFolders(foldersAndPermissionIdentifiers.stream().mapToInt(i -> i).toArray());

        UserizedFolder[] result = serviceSpy.removeAdminFromFolderPermissions(contextId, folders);

        assertEquals(foldersAndPermissionIdentifiers.size(), result.length);
        for (UserizedFolder userizedFolder : result) {
            assertTrue(foldersAndPermissionIdentifiers.contains(Integer.parseInt(userizedFolder.getID())));
            Permission[] permissions = userizedFolder.getPermissions();
            for (Permission permission : permissions) {
                assertTrue(foldersAndPermissionIdentifiers.contains(permission.getEntity()));
            }
        }
    }

    @Test
    public void testRemoveAdminFromFolderPermissions_folderDoesContainAdminPermission_returnWithoutFolderPermissionForAdmin() throws OXException {
        List<Integer> foldersAndPermissionIdentifiers = Arrays.asList(4, 5, 6, 7, adminUserId);
        UserizedFolder[] folders = getUserizedFolders(foldersAndPermissionIdentifiers.stream().mapToInt(i -> i).toArray());

        UserizedFolder[] result = serviceSpy.removeAdminFromFolderPermissions(contextId, folders);

        assertEquals(foldersAndPermissionIdentifiers.size(), result.length);
        for (UserizedFolder userizedFolder : result) {
            assertTrue(foldersAndPermissionIdentifiers.contains(Integer.parseInt(userizedFolder.getID())));
            Permission[] permissions = userizedFolder.getPermissions();
            assertEquals(foldersAndPermissionIdentifiers.size() - 1, permissions.length);
            boolean adminFound = false;
            for (Permission permission : permissions) {
                assertTrue(foldersAndPermissionIdentifiers.contains(permission.getEntity()));
                if (permission.getEntity() == adminUserId) {
                    adminFound = true;
                }
            }
            assertFalse(adminFound);
        }
    }

    @Test
    public void testRemoveAdminFromFolderPermissions_featureDisabled_returnWithFolderPermissionForAdmin() throws OXException {
        Mockito.when(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any())).thenReturn(Boolean.TRUE.booleanValue());
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> foldersAndPermissionIdentifiers = Arrays.asList(4, 5, 6, 7, adminUserId);
        UserizedFolder[] folders = getUserizedFolders(foldersAndPermissionIdentifiers.stream().mapToInt(i -> i).toArray());

        UserizedFolder[] result = service.removeAdminFromFolderPermissions(contextId, folders);

        assertTrue(folders.equals(result));
        assertTrue(Arrays.stream(result).allMatch(i -> Arrays.stream(i.getPermissions()).anyMatch(j -> j.getEntity() == adminUserId)));
    }

    //============================================================================================================//
    //============================================================================================================//

    @Test
    public void testAddAdminToFolderPermissions_origNull_returnUpdate() throws OXException {
        List<Integer> foldersAndPermissionIdentifiers = Arrays.asList(4, 5, 6, 7, adminUserId);
        Permission[] updatedPermissions = getPermissions(foldersAndPermissionIdentifiers.stream().mapToInt(i -> i).toArray());

        Permission[] result = serviceSpy.addAdminToFolderPermissions(contextId, null, updatedPermissions);

        assertTrue(updatedPermissions.equals(result));
        for (int i = 0; i < result.length; i++) {
            assertTrue(foldersAndPermissionIdentifiers.contains(result[i].getEntity()));

        }
    }

    @Test
    public void testAddAdminToFolderPermissions_updatedNull_returnNull() throws OXException {
        List<Integer> foldersAndPermissionIdentifiers = Arrays.asList(4, 5, 6, 7, adminUserId);
        Permission[] originalPermissions = getPermissions(foldersAndPermissionIdentifiers.stream().mapToInt(i -> i).toArray());

        Permission[] result = serviceSpy.addAdminToFolderPermissions(contextId, originalPermissions, null);

        assertNull(result);
    }

    @Test
    public void testAddAdminToFolderPermissions_adminNotInOrig_returnUpdate() throws OXException {
        List<Integer> original = Arrays.asList(4, 5, 6, 7);
        Permission[] originalPermissions = getPermissions(original.stream().mapToInt(i -> i).toArray());
        List<Integer> updated = Arrays.asList(4, 5, 6, 7, 8);
        Permission[] updatedPermissions = getPermissions(updated.stream().mapToInt(i -> i).toArray());

        Permission[] result = serviceSpy.addAdminToFolderPermissions(contextId, originalPermissions, updatedPermissions);

        assertEquals(updated.size(), result.length);
        assertTrue(Arrays.stream(result).allMatch(i -> i.getEntity() != adminUserId));
    }

    @Test
    public void testAddAdminToFolderPermissions_adminInUpdate_returnUpdate() throws OXException {
        List<Integer> original = Arrays.asList(4, 5, 6, 7);
        Permission[] originalPermissions = getPermissions(original.stream().mapToInt(i -> i).toArray());
        List<Integer> updated = Arrays.asList(4, 5, 6, 7, 8, adminUserId);
        Permission[] updatedPermissions = getPermissions(updated.stream().mapToInt(i -> i).toArray());

        Permission[] result = serviceSpy.addAdminToFolderPermissions(contextId, originalPermissions, updatedPermissions);

        assertEquals(updated.size(), result.length);
        assertTrue(Arrays.stream(result).anyMatch(i -> i.getEntity() == adminUserId));
    }

    @Test
    public void testAddAdminToFolderPermissions_adminInUpdateAndOriginal_returnUpdate() throws OXException {
        List<Integer> original = Arrays.asList(adminUserId, 44, 55, 66, 77);
        Permission[] originalPermissions = getPermissions(original.stream().mapToInt(i -> i).toArray());
        List<Integer> updated = Arrays.asList(4, 5, 6, 7, 8, adminUserId);
        Permission[] updatedPermissions = getPermissions(updated.stream().mapToInt(i -> i).toArray());

        Permission[] result = serviceSpy.addAdminToFolderPermissions(contextId, originalPermissions, updatedPermissions);

        assertEquals(updated.size(), result.length);
        assertTrue(Arrays.stream(result).anyMatch(i -> i.getEntity() == adminUserId));
    }

    @Test
    public void testAddAdminToFolderPermissions_adminInOrig_returnInUpdate() throws OXException {
        List<Integer> original = Arrays.asList(4, 5, 6, 7, adminUserId);
        Permission[] originalPermissions = getPermissions(original.stream().mapToInt(i -> i).toArray());
        List<Integer> updated = Arrays.asList(4, 5, 6, 7, 8);
        Permission[] updatedPermissions = getPermissions(updated.stream().mapToInt(i -> i).toArray());

        Permission[] result = serviceSpy.addAdminToFolderPermissions(contextId, originalPermissions, updatedPermissions);

        assertEquals(updated.size() + 1, result.length);
        assertTrue(Arrays.stream(result).anyMatch(i -> i.getEntity() == adminUserId));
    }

    @Test
    public void testAddAdminToFolderPermissions_featureDisabled_returnUpdate() throws OXException {
        Mockito.when(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any())).thenReturn(Boolean.TRUE.booleanValue());
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> original = Arrays.asList(4, 5, 6, 7, adminUserId);
        Permission[] originalPermissions = getPermissions(original.stream().mapToInt(i -> i).toArray());
        List<Integer> updated = Arrays.asList(4, 5, 6, 7, 8);
        Permission[] updatedPermissions = getPermissions(updated.stream().mapToInt(i -> i).toArray());

        Permission[] result = service.addAdminToFolderPermissions(contextId, originalPermissions, updatedPermissions);

        assertEquals(updated.size(), result.length);
        assertTrue(updatedPermissions.equals(result));
        assertTrue(Arrays.stream(result).allMatch(i -> i.getEntity() != adminUserId));
    }

    //============================================================================================================//
    //============================================================================================================//

    private static User[] getUsers(int... ids) {
        List<User> users = new ArrayList<>();
        for (int id : ids) {
            UserImpl user = new UserImpl();
            user.setId(id);
            users.add(user);
        }
        return users.stream().toArray(User[]::new);
    }

    @Test
    public void testRemoveAdminFromUsers_userListNull_returnNull() throws OXException {
        User[] result = serviceSpy.removeAdminFromUsers(contextId, null);

        assertNull(result);
    }

    @Test
    public void testRemoveAdminFromUsers_userListEmpty_returnEmpty() throws OXException {
        User[] users = getUsers();

        User[] result = serviceSpy.removeAdminFromUsers(contextId, users);

        assertTrue(users.equals(result));
        assertEquals(users.length, result.length);
    }

    @Test
    public void testRemoveAdminFromUsers_adminNotInUserList_returnUpdate() throws OXException {
        List<Integer> original = Arrays.asList(4, 5, 6, 7);
        User[] users = getUsers(original.stream().mapToInt(i -> i).toArray());

        User[] result = serviceSpy.removeAdminFromUsers(contextId, users);

        assertEquals(users.length, result.length);
        for (int j = 0; j < result.length; j++) {
            User user = result[j];
            assertTrue(original.contains(user.getId()));
        }
    }

    @Test
    public void testRemoveAdminFromUsers_adminInUserList_removeFromUpdate() throws OXException {
        List<Integer> original = Arrays.asList(4, 5, 6, 7, adminUserId);
        User[] users = getUsers(original.stream().mapToInt(i -> i).toArray());

        User[] result = serviceSpy.removeAdminFromUsers(contextId, users);

        assertEquals(users.length - 1, result.length);
        assertFalse(Arrays.stream(result).anyMatch(x -> x.getId() == adminUserId));
    }

    @Test
    public void testRemoveAdminFromUsers_featureDisabeld_leaveAdminInUpdate() throws OXException {
        Mockito.when(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any())).thenReturn(Boolean.TRUE.booleanValue());
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> original = Arrays.asList(4, 5, 6, 7, adminUserId);
        User[] users = getUsers(original.stream().mapToInt(i -> i).toArray());

        User[] removeAdminFromUsers = service.removeAdminFromUsers(contextId, users);

        assertEquals(users.length, removeAdminFromUsers.length);
        assertTrue(users.equals(removeAdminFromUsers));
    }

    //============================================================================================================//
    //============================================================================================================//

    @Test
    public void testRemoveAdminFromUserIds_idsNull_returnNull() throws OXException {
        int[] result = serviceSpy.removeAdminFromUserIds(contextId, null);

        assertNull(result);
    }

    @Test
    public void testRemoveAdminFromUserIds_idsEmpty_returnEmpty() throws OXException {
        List<Integer> original = Arrays.asList();

        int[] result = serviceSpy.removeAdminFromUserIds(contextId, original.stream().mapToInt(i -> i).toArray());

        assertEquals(original.size(), result.length);
    }

    @Test
    public void testRemoveAdminFromUserIds_adminNotInIds_returnIds() throws OXException {
        List<Integer> original = Arrays.asList(6, 7, 8, 9, 11, 9999);

        int[] result = serviceSpy.removeAdminFromUserIds(contextId, original.stream().mapToInt(i -> i).toArray());

        assertEquals(original.size(), result.length);
        assertFalse(Arrays.stream(result).anyMatch(x -> x == adminUserId));
    }

    @Test
    public void testRemoveAdminFromUserIds_adminInIds_returnIdsWithoutAdmin() throws OXException {
        List<Integer> original = Arrays.asList(6, 7, 8, 9, 11, 9999, adminUserId);

        int[] result = serviceSpy.removeAdminFromUserIds(contextId, original.stream().mapToInt(i -> i).toArray());

        assertEquals(original.size() - 1, result.length);
        assertFalse(Arrays.stream(result).anyMatch(x -> x == adminUserId));
    }

    @Test
    public void testRemoveAdminFromUserIds_featureDisabled_returnIdsWithAdmin() throws OXException {
        Mockito.when(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any())).thenReturn(Boolean.TRUE.booleanValue());
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> original = Arrays.asList(6, 7, 8, 9, 11, 9999, adminUserId);

        int[] result = service.removeAdminFromUserIds(contextId, original.stream().mapToInt(i -> i).toArray());

        assertEquals(original.size(), result.length);
        assertTrue(Arrays.stream(result).anyMatch(x -> x == adminUserId));
    }

    //============================================================================================================//
    //============================================================================================================//

    private static List<ObjectPermission> getObjectPermissions(int... ids) throws OXException {
        List<ObjectPermission> retval = new LinkedList<ObjectPermission>();
        for (int id : ids) {
            ObjectPermission objectPermission = new ObjectPermission();
            objectPermission.setEntity(id);
            retval.add(objectPermission);
        }
        return retval;
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntListOfObjectPermission_listEmpty_returnEmptyList() throws OXException {
        List<ObjectPermission> emptyList = getObjectPermissions();

        List<ObjectPermission> result = serviceSpy.removeAdminFromObjectPermissions(contextId, emptyList);

        assertTrue(emptyList.equals(result));
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntListOfObjectPermission_adminNotInObjectPermissions_returnUnchanged() throws OXException {
        List<ObjectPermission> objectPermissions = getObjectPermissions(77, 99, 1111);

        List<ObjectPermission> result = serviceSpy.removeAdminFromObjectPermissions(contextId, objectPermissions);

        assertEquals(objectPermissions.size(), result.size());
        assertFalse(result.stream().anyMatch(x -> x.getEntity() == adminUserId));
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntListOfObjectPermission_adminInObjectPermissions_returnWithoutAdmin() throws OXException {
        List<ObjectPermission> objectPermissions = getObjectPermissions(77, 99, 1111, adminUserId);

        List<ObjectPermission> result = serviceSpy.removeAdminFromObjectPermissions(contextId, objectPermissions);

        assertEquals(objectPermissions.size() - 1, result.size());
        assertFalse(result.stream().anyMatch(x -> x.getEntity() == adminUserId));
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntListOfObjectPermission_featureDisabled_returnUnchanged() throws OXException {
        Mockito.when(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any())).thenReturn(Boolean.TRUE.booleanValue());
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<ObjectPermission> objectPermissions = getObjectPermissions(77, 99, 1111, adminUserId);

        List<ObjectPermission> result = service.removeAdminFromObjectPermissions(contextId, objectPermissions);

        assertEquals(objectPermissions.size(), result.size());
        assertTrue(result.stream().anyMatch(x -> x.getEntity() == adminUserId));
    }

    //============================================================================================================//
    //============================================================================================================//

    private static SearchIterator<DocumentMetadata> getObjectPermissionsSearchIterator(int... ids) throws OXException {
        List<DocumentMetadata> result = new ArrayList<>();
        List<ObjectPermission> permissions = getObjectPermissions(ids);
        for (int id : ids) {
            DocumentMetadataImpl metaData = new DocumentMetadataImpl();
            metaData.setId(id);
            metaData.setObjectPermissions(permissions);
            result.add(metaData);
        }
        return new SearchIteratorAdapter<DocumentMetadata>(result.iterator(), result.size());
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntSearchIteratorOfDocumentMetadata_emptyIterator_returnIterator() throws OXException {
        SearchIterator<DocumentMetadata> documentMetaData = getObjectPermissionsSearchIterator();

        SearchIterator<DocumentMetadata> result = serviceSpy.removeAdminFromObjectPermissions(contextId, documentMetaData);

        assertEquals(documentMetaData.size(), result.size());
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntSearchIteratorOfDocumentMetadata_adminNotInIterator_returnIterator() throws OXException {
        List<Integer> original = Arrays.asList(4, 5, 6, 7);
        SearchIterator<DocumentMetadata> documentMetaData = getObjectPermissionsSearchIterator(original.stream().mapToInt(i -> i).toArray());

        SearchIterator<DocumentMetadata> result = serviceSpy.removeAdminFromObjectPermissions(contextId, documentMetaData);

        assertEquals(documentMetaData.size(), result.size());
        while (result.hasNext()) {
            DocumentMetadata documentMetadata2 = result.next();
            assertTrue(documentMetadata2.getObjectPermissions().stream().allMatch(x -> x.getEntity() != adminUserId));
        }
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntSearchIteratorOfDocumentMetadata_adminInIterator_returnWithRemovedAdmin() throws OXException {
        List<Integer> original = Arrays.asList(4, 5, 6, 7, adminUserId, adminContactId);
        SearchIterator<DocumentMetadata> documentMetaData = getObjectPermissionsSearchIterator(original.stream().mapToInt(i -> i).toArray());

        SearchIterator<DocumentMetadata> result = serviceSpy.removeAdminFromObjectPermissions(contextId, documentMetaData);

        assertEquals(documentMetaData.size(), result.size());
        List<DocumentMetadata> asList = SearchIterators.asList(result);
        for (DocumentMetadata documentMetadata2 : asList) {
            assertTrue(documentMetadata2.getObjectPermissions().stream().allMatch(x -> x.getEntity() != adminUserId));
            assertTrue(documentMetadata2.getObjectPermissions().stream().allMatch(x -> original.contains(x.getEntity())));
        }
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntSearchIteratorOfDocumentMetadata_featureDisabled_returnUnchanged() throws OXException {
        Mockito.when(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any())).thenReturn(Boolean.TRUE.booleanValue());
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> original = Arrays.asList(4, 5, 6, 7, adminUserId, adminContactId);
        SearchIterator<DocumentMetadata> documentMetaData = getObjectPermissionsSearchIterator(original.stream().mapToInt(i -> i).toArray());

        SearchIterator<DocumentMetadata> result = service.removeAdminFromObjectPermissions(contextId, documentMetaData);

        assertTrue(documentMetaData.equals(result));
        List<DocumentMetadata> asList = SearchIterators.asList(result);
        for (DocumentMetadata documentMetadata2 : asList) {
            assertTrue(documentMetadata2.getObjectPermissions().stream().anyMatch(x -> x.getEntity() == adminUserId));
            assertTrue(documentMetadata2.getObjectPermissions().stream().allMatch(x -> original.contains(x.getEntity())));
        }
    }

    //============================================================================================================//
    //============================================================================================================//

    private static TimedResult<DocumentMetadata> getDocumentMetadataTimedResult(int... ids) throws OXException {
        final SearchIterator<DocumentMetadata> results = getObjectPermissionsSearchIterator(ids);

        return new TimedResult<DocumentMetadata>() {

            @Override
            public SearchIterator<DocumentMetadata> results() throws OXException {
                return results;
            }

            @Override
            public long sequenceNumber() throws OXException {
                return System.currentTimeMillis();
            }
        };
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntTimedResultOfDocumentMetadata_resultsNull_returnOrig() throws OXException {
        List<DocumentMetadata> retval = new LinkedList<DocumentMetadata>();
        final SearchIterator<DocumentMetadata> results = getObjectPermissionsSearchIterator();

        TimedResult<DocumentMetadata> original = new TimedResult<DocumentMetadata>() {

            @Override
            public SearchIterator<DocumentMetadata> results() throws OXException {
                return null;
            }

            @Override
            public long sequenceNumber() throws OXException {
                return System.currentTimeMillis();
            }
        };

        TimedResult<DocumentMetadata> result = serviceSpy.removeAdminFromObjectPermissions(contextId, original);

        assertEquals(original.sequenceNumber(), result.sequenceNumber());
        assertNull(original.results());
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntTimedResultOfDocumentMetadata_emptyDocumentMetadata_returnEmpty() throws OXException {
        TimedResult<DocumentMetadata> original = getDocumentMetadataTimedResult();

        TimedResult<DocumentMetadata> result = serviceSpy.removeAdminFromObjectPermissions(contextId, original);

        assertEquals(original.results().size(), result.results().size());
        assertEquals(original.sequenceNumber(), result.sequenceNumber());
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntTimedResultOfDocumentMetadata_adminNotInInDocumentMetadata_returnUnchanged() throws OXException {
        List<Integer> originalIdentifiers = Arrays.asList(4, 44, 99999, adminContactId);
        TimedResult<DocumentMetadata> original = getDocumentMetadataTimedResult(originalIdentifiers.stream().mapToInt(i -> i).toArray());

        TimedResult<DocumentMetadata> result = serviceSpy.removeAdminFromObjectPermissions(contextId, original);

        assertEquals(original.results().size(), result.results().size());
        assertEquals(original.sequenceNumber(), result.sequenceNumber());
        List<DocumentMetadata> resultAsList = SearchIterators.asList(result.results());
        for (DocumentMetadata documentMetadata : resultAsList) {
            assertTrue(documentMetadata.getObjectPermissions().stream().noneMatch(x -> x.getEntity() == adminUserId));
            assertTrue(documentMetadata.getObjectPermissions().stream().allMatch(x -> originalIdentifiers.contains(x.getEntity())));
        }
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntTimedResultOfDocumentMetadata_adminInDocumentMetadata_removeAdmin() throws OXException {
        List<Integer> originalIdentifiers = Arrays.asList(4, 44, 99999, adminContactId, adminUserId);
        TimedResult<DocumentMetadata> original = getDocumentMetadataTimedResult(originalIdentifiers.stream().mapToInt(i -> i).toArray());

        TimedResult<DocumentMetadata> result = serviceSpy.removeAdminFromObjectPermissions(contextId, original);

        assertEquals(original.results().size(), result.results().size());
        assertEquals(original.sequenceNumber(), result.sequenceNumber());
        List<DocumentMetadata> resultAsList = SearchIterators.asList(result.results());
        for (DocumentMetadata documentMetadata : resultAsList) {
            assertTrue(documentMetadata.getObjectPermissions().stream().noneMatch(x -> x.getEntity() == adminUserId));
            assertTrue(documentMetadata.getObjectPermissions().stream().allMatch(x -> originalIdentifiers.contains(x.getEntity())));
        }
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntTimedResultOfDocumentMetadata_featureDisabeld_returnUnchanged() throws OXException {
        Mockito.when(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any())).thenReturn(Boolean.TRUE.booleanValue());
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> originalIdentifiers = Arrays.asList(4, 44, 99999, adminContactId, adminUserId);
        TimedResult<DocumentMetadata> original = getDocumentMetadataTimedResult(originalIdentifiers.stream().mapToInt(i -> i).toArray());

        TimedResult<DocumentMetadata> result = service.removeAdminFromObjectPermissions(contextId, original);

        assertEquals(original.results().size(), result.results().size());
        assertEquals(original.sequenceNumber(), result.sequenceNumber());
        List<DocumentMetadata> resultAsList = SearchIterators.asList(result.results());
        for (DocumentMetadata documentMetadata : resultAsList) {
            assertTrue(documentMetadata.getObjectPermissions().stream().anyMatch(x -> x.getEntity() == adminUserId));
            assertTrue(documentMetadata.getObjectPermissions().stream().allMatch(x -> originalIdentifiers.contains(x.getEntity())));
        }
    }

    //============================================================================================================//
    //============================================================================================================//

    @Test
    public void testAddAdminToObjectPermissions_originalNull_returnUpdated() throws OXException {
        List<ObjectPermission> updatedPermissions = getObjectPermissions(adminUserId, 59, 444, 99);

        List<ObjectPermission> result = serviceSpy.addAdminToObjectPermissions(contextId, null, updatedPermissions);

        assertTrue(updatedPermissions.equals(result));
    }

    @Test
    public void testAddAdminToObjectPermissions_updatelNull_returnNull() throws OXException {
        List<ObjectPermission> originalPermissions = getObjectPermissions(adminUserId, 59, 444, 99);

        List<ObjectPermission> result = serviceSpy.addAdminToObjectPermissions(contextId, originalPermissions, null);

        assertNull(result);
    }

    @Test
    public void testAddAdminToObjectPermissions_originalEmpty_returnUpdated() throws OXException {
        List<ObjectPermission> originalPermissions = getObjectPermissions();
        List<ObjectPermission> updatedPermissions = getObjectPermissions(adminUserId, 59, 444, 99);

        List<ObjectPermission> result = serviceSpy.addAdminToObjectPermissions(contextId, originalPermissions, updatedPermissions);

        assertTrue(updatedPermissions.equals(result));
    }

    @Test
    public void testAddAdminToObjectPermissions_adminNotInOriginal_returnUpdated() throws OXException {
        List<ObjectPermission> originalPermissions = getObjectPermissions(77, 88, 99);
        List<ObjectPermission> updatedPermissions = getObjectPermissions(adminUserId, 59, 444, 99);

        List<ObjectPermission> result = serviceSpy.addAdminToObjectPermissions(contextId, originalPermissions, updatedPermissions);

        assertTrue(result.stream().anyMatch(x -> x.getEntity() == adminUserId));
        assertEquals(updatedPermissions.size(), result.size());
    }

    @Test
    public void testAddAdminToObjectPermissions_adminNotInOriginalAndUpdated_returnUpdated() throws OXException {
        List<ObjectPermission> originalPermissions = getObjectPermissions(77, 88, 99);
        List<ObjectPermission> updatedPermissions = getObjectPermissions(59, 444, 99);

        List<ObjectPermission> result = serviceSpy.addAdminToObjectPermissions(contextId, originalPermissions, updatedPermissions);

        assertTrue(result.stream().noneMatch(x -> x.getEntity() == adminUserId));
        assertEquals(updatedPermissions.size(), result.size());
    }

    @Test
    public void testAddAdminToObjectPermissions_adminInOriginalAndUpdated_returnUpdated() throws OXException {
        List<ObjectPermission> originalPermissions = getObjectPermissions(77, 88, 99, adminUserId);
        List<ObjectPermission> updatedPermissions = getObjectPermissions(59, 444, adminUserId, 99);

        List<ObjectPermission> result = serviceSpy.addAdminToObjectPermissions(contextId, originalPermissions, updatedPermissions);

        assertTrue(result.stream().anyMatch(x -> x.getEntity() == adminUserId));
        assertEquals(updatedPermissions.size(), result.size());
    }

    @Test
    public void testAddAdminToObjectPermissions_adminInOriginal_returnUpdated() throws OXException {
        List<ObjectPermission> originalPermissions = getObjectPermissions(77, 88, 99, adminUserId);
        List<ObjectPermission> updatedPermissions = getObjectPermissions(59, 444, 99);

        List<ObjectPermission> result = serviceSpy.addAdminToObjectPermissions(contextId, originalPermissions, updatedPermissions);

        assertTrue(result.stream().anyMatch(x -> x.getEntity() == adminUserId));
        assertEquals(updatedPermissions.size() + 1, result.size());
    }

    @Test
    public void testAddAdminToObjectPermissions_featureDisabled_returnUpdated() throws OXException {
        Mockito.when(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any())).thenReturn(Boolean.TRUE.booleanValue());
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<ObjectPermission> originalPermissions = getObjectPermissions(77, 88, 99, adminUserId);
        List<ObjectPermission> updatedPermissions = getObjectPermissions(59, 444, 99);

        List<ObjectPermission> result = service.addAdminToObjectPermissions(contextId, originalPermissions, updatedPermissions);

        assertTrue(updatedPermissions.equals(result));
    }
}
