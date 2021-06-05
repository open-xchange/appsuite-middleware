/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.config.admin.internal;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
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
import com.openexchange.folderstorage.UserizedFolderImpl;
import com.openexchange.folderstorage.database.DatabaseFolder;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.user.User;
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

    private final int contextId = 11;

    private final int adminUserId = 3;

    private final int adminContactId = 2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        serviceSpy = PowerMockito.spy(service);

        PowerMockito.doReturn(I(adminUserId)).when(serviceSpy, "getAdminUserId", I(ArgumentMatchers.anyInt()));
        PowerMockito.doReturn(I(adminContactId)).when(serviceSpy, "getAdminContactId", I(ArgumentMatchers.anyInt()));
        PowerMockito.doReturn(Boolean.FALSE).when(serviceSpy, "showAdmin", I(ArgumentMatchers.anyInt()));
    }

    @Test
    public void testShowAdmin_contextIdZero_returnTrue() {
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);

        boolean showAdmin = service.showAdmin(0);

        assertTrue(showAdmin);
    }

    @Test
    public void testShowAdmin_disabled_returnFalse() {
        Mockito.when(B(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any()))).thenReturn(Boolean.FALSE);
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);

        boolean showAdmin = service.showAdmin(contextId);

        assertFalse(showAdmin);
    }

    @Test
    public void testShowAdmin_enabled_returnTrue() {
        Mockito.when(B(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any()))).thenReturn(Boolean.TRUE);
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);

        boolean showAdmin = service.showAdmin(contextId);

        assertTrue(showAdmin);
    }

    //============================================================================================================//
    //============================================================================================================//

    @Test
    public void testRemoveAdminFromContacts_contactsNull_returnNull() {
        try (SearchIterator<Contact> result = serviceSpy.removeAdminFromContacts(contextId, null)) {
            assertNull(result);
        }
    }

    @Test
    public void testRemoveAdminFromContacts_contactsEmpty_returnEmptyIterator() {
        try (SearchIterator<Contact> result = serviceSpy.removeAdminFromContacts(contextId, getContacts())) {
            assertEquals(0, result.size());
        }

    }

    @Test
    public void testRemoveAdminFromContacts_featureDisabled_returnAllContacts() {
        Mockito.when(B(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any()))).thenReturn(Boolean.TRUE);
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> contactIds = Arrays.asList(I(adminContactId), I(3), I(4), I(5), I(6));

        try (SearchIterator<Contact> org = getContacts(contactIds.stream().mapToInt(i -> i.intValue()).toArray()); SearchIterator<Contact> result = service.removeAdminFromContacts(contextId, org)) {
            assertTrue(org.equals(result));
        }
    }

    @Test
    public void testRemoveAdminFromContacts_contactsWithoutAdmin_returnAllContacts() throws OXException {
        List<Integer> contactIds = Arrays.asList(I(3), I(4), I(5), I(6));

        try (SearchIterator<Contact> result = serviceSpy.removeAdminFromContacts(contextId, getContacts(contactIds.stream().mapToInt(i -> i.intValue()).toArray()))) {
            assertEquals(contactIds.size(), SearchIterators.asList(result).size());
        }
    }

    @Test
    public void testRemoveAdminFromContacts_contactsWithAdmin_returnWithoutContacts() throws OXException {
        List<Integer> contactIds = Arrays.asList(I(2), I(3), I(4), I(5), I(6));

        try (SearchIterator<Contact> result = serviceSpy.removeAdminFromContacts(contextId, getContacts(contactIds.stream().mapToInt(i -> i.intValue()).toArray()))) {

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
    public void testRemoveAdminFromGroups_groupsNull_returnNull() {
        Group[] result = serviceSpy.removeAdminFromGroupMemberList(contextId, null);

        assertNull(result);
    }

    @Test
    public void testRemoveAdminFromGroups_groupsEmpty_returnEmpty() {
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
    public void testRemoveAdminFromGroups_groupsWithoutAdmin_returnGroups() {
        List<Integer> groupsAndMembers = Arrays.asList(I(6), I(7), I(8), I(9));
        Group[] groups = getGroups(groupsAndMembers.stream().mapToInt(i -> i.intValue()).toArray());

        Group[] result = serviceSpy.removeAdminFromGroupMemberList(contextId, groups);

        assertEquals(groupsAndMembers.size(), result.length);
        for (int j = 0; j < result.length; j++) {
            assertTrue(groupsAndMembers.contains(I(result[j].getIdentifier())));
            int[] member = result[j].getMember();
            assertEquals(groupsAndMembers.size(), member.length);
            for (int k : member) {
                assertTrue(groupsAndMembers.contains(I(k)));
            }
        }
    }

    @Test
    public void testRemoveAdminFromGroups_groupsContainAdmin_returnGroupsWithRemovedAdmin() {
        List<Integer> groupsAndMembers = Arrays.asList(I(adminUserId), I(6), I(7), I(8), I(9));
        Group[] groups = getGroups(groupsAndMembers.stream().mapToInt(i -> i.intValue()).toArray());

        Group[] result = serviceSpy.removeAdminFromGroupMemberList(contextId, groups);

        assertEquals(groupsAndMembers.size(), result.length);
        for (int j = 0; j < result.length; j++) {
            assertTrue(groupsAndMembers.contains(I(result[j].getIdentifier())));
            int[] member = result[j].getMember();
            assertEquals(groupsAndMembers.size() - 1, member.length);
            for (int k : member) {
                assertTrue(groupsAndMembers.contains(I(k)));
            }
        }
        assertFalse(Arrays.stream(result).filter(i -> Arrays.stream(i.getMember()).anyMatch(j -> j == adminUserId)).peek(i -> System.out.println(i.getIdentifier())).findFirst().isPresent());
    }

    @Test
    public void testRemoveAdminFromGroups_featureDisabled_returnGroupsWithAdmin() {
        Mockito.when(B(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any()))).thenReturn(Boolean.TRUE);
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> groupsAndMembers = Arrays.asList(I(adminUserId), I(6), I(7), I(8), I(9));
        Group[] groups = getGroups(groupsAndMembers.stream().mapToInt(i -> i.intValue()).toArray());

        Group[] result = service.removeAdminFromGroupMemberList(contextId, groups);

        assertTrue(groups.equals(result));
        assertTrue(Arrays.stream(result).filter(i -> Arrays.stream(i.getMember()).anyMatch(j -> j == adminUserId)).peek(i -> System.out.println(i.getIdentifier())).findFirst().isPresent());
    }

    //============================================================================================================//
    //============================================================================================================//

    @Test
    public void testAddAdminToGroupIds_orginalNull_returnUpdate() {
        List<Integer> groupMembers = Arrays.asList(I(adminUserId), I(6), I(7), I(8), I(9));
        int[] updatedGroupMember = groupMembers.stream().mapToInt(i -> i.intValue()).toArray();

        int[] result = serviceSpy.addAdminToGroupMemberList(contextId, null, updatedGroupMember);

        assertTrue(updatedGroupMember.equals(result));
        assertEquals(updatedGroupMember.length, result.length);
    }

    @Test
    public void testAddAdminToGroupIds_updatedNull_returnUpdate() {
        List<Integer> groupMembers = Arrays.asList(I(adminUserId), I(6), I(7), I(8), I(9));
        int[] origGroupMember = groupMembers.stream().mapToInt(i -> i.intValue()).toArray();

        int[] result = serviceSpy.addAdminToGroupMemberList(contextId, origGroupMember, null);

        assertNull(result);
    }

    @Test
    public void testAddAdminToGroupIds_adminNotInOriginal_returnUpdate() {
        List<Integer> groupMembers = Arrays.asList(I(6), I(7), I(8), I(9));
        int[] origGroupMember = groupMembers.stream().mapToInt(i -> i.intValue()).toArray();
        List<Integer> updateGroupMembers = Arrays.asList(I(adminUserId), I(11), I(22), I(33), I(9));
        int[] updateGroupMembersArray = updateGroupMembers.stream().mapToInt(i -> i.intValue()).toArray();

        int[] result = serviceSpy.addAdminToGroupMemberList(contextId, origGroupMember, updateGroupMembersArray);

        assertEquals(updateGroupMembers.size(), result.length);
        for (int j = 0; j < result.length; j++) {
            assertTrue(updateGroupMembers.contains(I(result[j])));
        }
    }

    @Test
    public void testAddAdminToGroupIds_adminInOriginal_returnAdminInUpdate() {
        List<Integer> groupMembers = Arrays.asList(I(adminUserId), I(6), I(7), I(8), I(9));
        int[] origGroupMember = groupMembers.stream().mapToInt(i -> i.intValue()).toArray();
        List<Integer> updateGroupMembers = Arrays.asList(I(11), I(22), I(33), I(9));
        int[] updateGroupMembersArray = updateGroupMembers.stream().mapToInt(i -> i.intValue()).toArray();

        int[] result = serviceSpy.addAdminToGroupMemberList(contextId, origGroupMember, updateGroupMembersArray);

        List<Integer> expected = new ArrayList<>(updateGroupMembers);
        expected.add(I(adminUserId));
        assertEquals(expected.size(), result.length);
        boolean adminFound = false;
        for (int j = 0; j < result.length; j++) {
            assertTrue(expected.contains(I(result[j])));
            if (result[j] == adminUserId) {
                adminFound = true;
            }
        }
        assertTrue(adminFound);
    }

    @Test
    public void testAddAdminToGroupIds_adminInOriginalAndUpdate_returnAdminInUpdate() {
        List<Integer> groupMembers = Arrays.asList(I(adminUserId), I(6), I(7), I(8), I(9));
        int[] origGroupMember = groupMembers.stream().mapToInt(i -> i.intValue()).toArray();
        List<Integer> updateGroupMembers = Arrays.asList(I(11), I(22), I(33), I(9), I(adminUserId));
        int[] updateGroupMembersArray = updateGroupMembers.stream().mapToInt(i -> i.intValue()).toArray();

        int[] result = serviceSpy.addAdminToGroupMemberList(contextId, origGroupMember, updateGroupMembersArray);

        assertEquals(updateGroupMembers.size(), result.length);
    }

    @Test
    public void testAddAdminToGroupIds_featureDisabled_returnUpdate() {
        Mockito.when(B(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any()))).thenReturn(Boolean.TRUE);
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> groupMembers = Arrays.asList(I(adminUserId), I(6), I(7), I(8), I(9));
        int[] origGroupMember = groupMembers.stream().mapToInt(i -> i.intValue()).toArray();
        List<Integer> updateGroupMembers = Arrays.asList(I(11), I(22), I(33), I(9));
        int[] updateGroupMembersArray = updateGroupMembers.stream().mapToInt(i -> i.intValue()).toArray();

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
    public void testRemoveAdminFromFolderPermissions_folderListNull_returnNull() {
        UserizedFolder[] result = serviceSpy.removeAdminFromFolderPermissions(contextId, null);

        assertNull(result);
    }

    @Test
    public void testRemoveAdminFromFolderPermissions_folderListEmpty_returnEmptyFolderList() {
        UserizedFolder[] folders = getUserizedFolders();

        UserizedFolder[] result = serviceSpy.removeAdminFromFolderPermissions(contextId, folders);

        assertTrue(folders.equals(result));
    }

    @Test
    public void testRemoveAdminFromFolderPermissions_folderDoesNotContainAdminPermission_returnFolderList() {
        List<Integer> foldersAndPermissionIdentifiers = Arrays.asList(I(4), I(5), I(6), I(7));
        UserizedFolder[] folders = getUserizedFolders(foldersAndPermissionIdentifiers.stream().mapToInt(i -> i.intValue()).toArray());

        UserizedFolder[] result = serviceSpy.removeAdminFromFolderPermissions(contextId, folders);

        assertEquals(foldersAndPermissionIdentifiers.size(), result.length);
        for (UserizedFolder userizedFolder : result) {
            assertTrue(foldersAndPermissionIdentifiers.contains(Integer.valueOf(userizedFolder.getID())));
            Permission[] permissions = userizedFolder.getPermissions();
            for (Permission permission : permissions) {
                assertTrue(foldersAndPermissionIdentifiers.contains(I(permission.getEntity())));
            }
        }
    }

    @Test
    public void testRemoveAdminFromFolderPermissions_folderDoesContainAdminPermission_returnWithoutFolderPermissionForAdmin() {
        List<Integer> foldersAndPermissionIdentifiers = Arrays.asList(I(4), I(5), I(6), I(7), I(adminUserId));
        UserizedFolder[] folders = getUserizedFolders(foldersAndPermissionIdentifiers.stream().mapToInt(i -> i.intValue()).toArray());

        UserizedFolder[] result = serviceSpy.removeAdminFromFolderPermissions(contextId, folders);

        assertEquals(foldersAndPermissionIdentifiers.size(), result.length);
        for (UserizedFolder userizedFolder : result) {
            assertTrue(foldersAndPermissionIdentifiers.contains(Integer.valueOf(userizedFolder.getID())));
            Permission[] permissions = userizedFolder.getPermissions();
            assertEquals(foldersAndPermissionIdentifiers.size() - 1, permissions.length);
            boolean adminFound = false;
            for (Permission permission : permissions) {
                assertTrue(foldersAndPermissionIdentifiers.contains(I(permission.getEntity())));
                if (permission.getEntity() == adminUserId) {
                    adminFound = true;
                }
            }
            assertFalse(adminFound);
        }
    }

    @Test
    public void testRemoveAdminFromFolderPermissions_featureDisabled_returnWithFolderPermissionForAdmin() {
        Mockito.when(B(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any()))).thenReturn(Boolean.TRUE);
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> foldersAndPermissionIdentifiers = Arrays.asList(I(4), I(5), I(6), I(7), I(adminUserId));
        UserizedFolder[] folders = getUserizedFolders(foldersAndPermissionIdentifiers.stream().mapToInt(i -> i.intValue()).toArray());

        UserizedFolder[] result = service.removeAdminFromFolderPermissions(contextId, folders);

        assertTrue(folders.equals(result));
        assertTrue(Arrays.stream(result).allMatch(i -> Arrays.stream(i.getPermissions()).anyMatch(j -> j.getEntity() == adminUserId)));
    }

    //============================================================================================================//
    //============================================================================================================//

    @Test
    public void testAddAdminToFolderPermissions_origNull_returnUpdate() {
        List<Integer> foldersAndPermissionIdentifiers = Arrays.asList(I(4), I(5), I(6), I(7), I(adminUserId));
        Permission[] updatedPermissions = getPermissions(foldersAndPermissionIdentifiers.stream().mapToInt(i -> i.intValue()).toArray());

        Permission[] result = serviceSpy.addAdminToFolderPermissions(contextId, null, updatedPermissions);

        assertTrue(updatedPermissions.equals(result));
        for (int i = 0; i < result.length; i++) {
            assertTrue(foldersAndPermissionIdentifiers.contains(I(result[i].getEntity())));

        }
    }

    @Test
    public void testAddAdminToFolderPermissions_updatedNull_returnNull()  {
        List<Integer> foldersAndPermissionIdentifiers = Arrays.asList(I(4), I(5), I(6), I(7), I(adminUserId));
        Permission[] originalPermissions = getPermissions(foldersAndPermissionIdentifiers.stream().mapToInt(i -> i.intValue()).toArray());

        Permission[] result = serviceSpy.addAdminToFolderPermissions(contextId, originalPermissions, null);

        assertNull(result);
    }

    @Test
    public void testAddAdminToFolderPermissions_adminNotInOrig_returnUpdate()  {
        List<Integer> original = Arrays.asList(I(4), I(5), I(6), I(7));
        Permission[] originalPermissions = getPermissions(original.stream().mapToInt(i -> i.intValue()).toArray());
        List<Integer> updated = Arrays.asList(I(4), I(5), I(6), I(7), I(8));
        Permission[] updatedPermissions = getPermissions(updated.stream().mapToInt(i -> i.intValue()).toArray());

        Permission[] result = serviceSpy.addAdminToFolderPermissions(contextId, originalPermissions, updatedPermissions);

        assertEquals(updated.size(), result.length);
        assertTrue(Arrays.stream(result).allMatch(i -> i.getEntity() != adminUserId));
    }

    @Test
    public void testAddAdminToFolderPermissions_adminInUpdate_returnUpdate()  {
        List<Integer> original = Arrays.asList(I(4), I(5), I(6), I(7));
        Permission[] originalPermissions = getPermissions(original.stream().mapToInt(i -> i.intValue()).toArray());
        List<Integer> updated = Arrays.asList(I(4), I(5), I(6), I(7), I(8), I(adminUserId));
        Permission[] updatedPermissions = getPermissions(updated.stream().mapToInt(i -> i.intValue()).toArray());

        Permission[] result = serviceSpy.addAdminToFolderPermissions(contextId, originalPermissions, updatedPermissions);

        assertEquals(updated.size(), result.length);
        assertTrue(Arrays.stream(result).anyMatch(i -> i.getEntity() == adminUserId));
    }

    @Test
    public void testAddAdminToFolderPermissions_adminInUpdateAndOriginal_returnUpdate()  {
        List<Integer> original = Arrays.asList(I(adminUserId), I(44), I(55), I(66), I(77));
        Permission[] originalPermissions = getPermissions(original.stream().mapToInt(i -> i.intValue()).toArray());
        List<Integer> updated = Arrays.asList(I(4), I(5), I(6), I(7), I(8), I(adminUserId));
        Permission[] updatedPermissions = getPermissions(updated.stream().mapToInt(i -> i.intValue()).toArray());

        Permission[] result = serviceSpy.addAdminToFolderPermissions(contextId, originalPermissions, updatedPermissions);

        assertEquals(updated.size(), result.length);
        assertTrue(Arrays.stream(result).anyMatch(i -> i.getEntity() == adminUserId));
    }

    @Test
    public void testAddAdminToFolderPermissions_adminInOrig_returnInUpdate()  {
        List<Integer> original = Arrays.asList(I(4), I(5), I(6), I(7), I(adminUserId));
        Permission[] originalPermissions = getPermissions(original.stream().mapToInt(i -> i.intValue()).toArray());
        List<Integer> updated = Arrays.asList(I(4), I(5), I(6), I(7), I(8));
        Permission[] updatedPermissions = getPermissions(updated.stream().mapToInt(i -> i.intValue()).toArray());

        Permission[] result = serviceSpy.addAdminToFolderPermissions(contextId, originalPermissions, updatedPermissions);

        assertEquals(updated.size() + 1, result.length);
        assertTrue(Arrays.stream(result).anyMatch(i -> i.getEntity() == adminUserId));
    }

    @Test
    public void testAddAdminToFolderPermissions_featureDisabled_returnUpdate()  {
        Mockito.when(B(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any()))).thenReturn(Boolean.TRUE);
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> original = Arrays.asList(I(4), I(5), I(6), I(7), I(adminUserId));
        Permission[] originalPermissions = getPermissions(original.stream().mapToInt(i -> i.intValue()).toArray());
        List<Integer> updated = Arrays.asList(I(4), I(5), I(6), I(7), I(8));
        Permission[] updatedPermissions = getPermissions(updated.stream().mapToInt(i -> i.intValue()).toArray());

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
    public void testRemoveAdminFromUsers_userListNull_returnNull()  {
        User[] result = serviceSpy.removeAdminFromUsers(contextId, null);

        assertNull(result);
    }

    @Test
    public void testRemoveAdminFromUsers_userListEmpty_returnEmpty()  {
        User[] users = getUsers();

        User[] result = serviceSpy.removeAdminFromUsers(contextId, users);

        assertTrue(users.equals(result));
        assertEquals(users.length, result.length);
    }

    @Test
    public void testRemoveAdminFromUsers_adminNotInUserList_returnUpdate()  {
        List<Integer> original = Arrays.asList(I(4), I(5), I(6), I(7));
        User[] users = getUsers(original.stream().mapToInt(i -> i.intValue()).toArray());

        User[] result = serviceSpy.removeAdminFromUsers(contextId, users);

        assertEquals(users.length, result.length);
        for (int j = 0; j < result.length; j++) {
            User user = result[j];
            assertTrue(original.contains(I(user.getId())));
        }
    }

    @Test
    public void testRemoveAdminFromUsers_adminInUserList_removeFromUpdate()  {
        List<Integer> original = Arrays.asList(I(4), I(5), I(6), I(7), I(adminUserId));
        User[] users = getUsers(original.stream().mapToInt(i -> i.intValue()).toArray());

        User[] result = serviceSpy.removeAdminFromUsers(contextId, users);

        assertEquals(users.length - 1, result.length);
        assertFalse(Arrays.stream(result).anyMatch(x -> x.getId() == adminUserId));
    }

    @Test
    public void testRemoveAdminFromUsers_featureDisabeld_leaveAdminInUpdate()  {
        Mockito.when(B(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any()))).thenReturn(Boolean.TRUE);
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> original = Arrays.asList(I(4), I(5), I(6), I(7), I(adminUserId));
        User[] users = getUsers(original.stream().mapToInt(i -> i.intValue()).toArray());

        User[] removeAdminFromUsers = service.removeAdminFromUsers(contextId, users);

        assertEquals(users.length, removeAdminFromUsers.length);
        assertTrue(users.equals(removeAdminFromUsers));
    }

    //============================================================================================================//
    //============================================================================================================//

    @Test
    public void testRemoveAdminFromUserIds_idsNull_returnNull()  {
        int[] result = serviceSpy.removeAdminFromUserIds(contextId, null);

        assertNull(result);
    }

    @Test
    public void testRemoveAdminFromUserIds_idsEmpty_returnEmpty()  {
        List<Integer> original = Arrays.asList();

        int[] result = serviceSpy.removeAdminFromUserIds(contextId, original.stream().mapToInt(i -> i.intValue()).toArray());

        assertEquals(original.size(), result.length);
    }

    @Test
    public void testRemoveAdminFromUserIds_adminNotInIds_returnIds()  {
        List<Integer> original = Arrays.asList(I(6), I(7), I(8), I(9), I(11), I(9999));

        int[] result = serviceSpy.removeAdminFromUserIds(contextId, original.stream().mapToInt(i -> i.intValue()).toArray());

        assertEquals(original.size(), result.length);
        assertFalse(Arrays.stream(result).anyMatch(x -> x == adminUserId));
    }

    @Test
    public void testRemoveAdminFromUserIds_adminInIds_returnIdsWithoutAdmin()  {
        List<Integer> original = Arrays.asList(I(6), I(7), I(8), I(9), I(11), I(9999), I(adminUserId));

        int[] result = serviceSpy.removeAdminFromUserIds(contextId, original.stream().mapToInt(i -> i.intValue()).toArray());

        assertEquals(original.size() - 1, result.length);
        assertFalse(Arrays.stream(result).anyMatch(x -> x == adminUserId));
    }

    @Test
    public void testRemoveAdminFromUserIds_featureDisabled_returnIdsWithAdmin()  {
        Mockito.when(B(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any()))).thenReturn(Boolean.TRUE);
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> original = Arrays.asList(I(6), I(7), I(8), I(9), I(11), I(9999), I(adminUserId));

        int[] result = service.removeAdminFromUserIds(contextId, original.stream().mapToInt(i -> i.intValue()).toArray());

        assertEquals(original.size(), result.length);
        assertTrue(Arrays.stream(result).anyMatch(x -> x == adminUserId));
    }

    //============================================================================================================//
    //============================================================================================================//

    private static List<ObjectPermission> getObjectPermissions(int... ids)  {
        List<ObjectPermission> retval = new LinkedList<ObjectPermission>();
        for (int id : ids) {
            ObjectPermission objectPermission = new ObjectPermission();
            objectPermission.setEntity(id);
            retval.add(objectPermission);
        }
        return retval;
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntListOfObjectPermission_listEmpty_returnEmptyList()  {
        List<ObjectPermission> emptyList = getObjectPermissions();

        List<ObjectPermission> result = serviceSpy.removeAdminFromObjectPermissions(contextId, emptyList);

        assertTrue(emptyList.equals(result));
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntListOfObjectPermission_adminNotInObjectPermissions_returnUnchanged()  {
        List<ObjectPermission> objectPermissions = getObjectPermissions(77, 99, 1111);

        List<ObjectPermission> result = serviceSpy.removeAdminFromObjectPermissions(contextId, objectPermissions);

        assertEquals(objectPermissions.size(), result.size());
        assertFalse(result.stream().anyMatch(x -> x.getEntity() == adminUserId));
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntListOfObjectPermission_adminInObjectPermissions_returnWithoutAdmin()  {
        List<ObjectPermission> objectPermissions = getObjectPermissions(77, 99, 1111, adminUserId);

        List<ObjectPermission> result = serviceSpy.removeAdminFromObjectPermissions(contextId, objectPermissions);

        assertEquals(objectPermissions.size() - 1, result.size());
        assertFalse(result.stream().anyMatch(x -> x.getEntity() == adminUserId));
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntListOfObjectPermission_featureDisabled_returnUnchanged()  {
        Mockito.when(B(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any()))).thenReturn(Boolean.TRUE);
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<ObjectPermission> objectPermissions = getObjectPermissions(77, 99, 1111, adminUserId);

        List<ObjectPermission> result = service.removeAdminFromObjectPermissions(contextId, objectPermissions);

        assertEquals(objectPermissions.size(), result.size());
        assertTrue(result.stream().anyMatch(x -> x.getEntity() == adminUserId));
    }

    //============================================================================================================//
    //============================================================================================================//

    private static SearchIterator<DocumentMetadata> getObjectPermissionsSearchIterator(int... ids)  {
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
    public void testRemoveAdminFromObjectPermissionsIntSearchIteratorOfDocumentMetadata_emptyIterator_returnIterator() {
        SearchIterator<DocumentMetadata> documentMetaData = getObjectPermissionsSearchIterator();

        SearchIterator<DocumentMetadata> result = serviceSpy.removeAdminFromObjectPermissions(contextId, documentMetaData);

        assertEquals(documentMetaData.size(), result.size());
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntSearchIteratorOfDocumentMetadata_adminNotInIterator_returnIterator() throws OXException {
        List<Integer> original = Arrays.asList(I(4), I(5), I(6), I(7));
        SearchIterator<DocumentMetadata> documentMetaData = getObjectPermissionsSearchIterator(original.stream().mapToInt(i -> i.intValue()).toArray());

        SearchIterator<DocumentMetadata> result = serviceSpy.removeAdminFromObjectPermissions(contextId, documentMetaData);

        assertEquals(documentMetaData.size(), result.size());
        while (result.hasNext()) {
            DocumentMetadata documentMetadata2 = result.next();
            assertTrue(documentMetadata2.getObjectPermissions().stream().allMatch(x -> x.getEntity() != adminUserId));
        }
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntSearchIteratorOfDocumentMetadata_adminInIterator_returnWithRemovedAdmin() throws OXException {
        List<Integer> original = Arrays.asList(I(4), I(5), I(6), I(7), I(adminUserId), I(adminContactId));
        SearchIterator<DocumentMetadata> documentMetaData = getObjectPermissionsSearchIterator(original.stream().mapToInt(i -> i.intValue()).toArray());

        SearchIterator<DocumentMetadata> result = serviceSpy.removeAdminFromObjectPermissions(contextId, documentMetaData);

        assertEquals(documentMetaData.size(), result.size());
        List<DocumentMetadata> asList = SearchIterators.asList(result);
        for (DocumentMetadata documentMetadata2 : asList) {
            assertTrue(documentMetadata2.getObjectPermissions().stream().allMatch(x -> x.getEntity() != adminUserId));
            assertTrue(documentMetadata2.getObjectPermissions().stream().allMatch(x -> original.contains(I(x.getEntity()))));
        }
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntSearchIteratorOfDocumentMetadata_featureDisabled_returnUnchanged() throws OXException {
        Mockito.when(B(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any()))).thenReturn(Boolean.TRUE);
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> original = Arrays.asList(I(4), I(5), I(6), I(7), I(adminUserId), I(adminContactId));
        SearchIterator<DocumentMetadata> documentMetaData = getObjectPermissionsSearchIterator(original.stream().mapToInt(i -> i.intValue()).toArray());

        SearchIterator<DocumentMetadata> result = service.removeAdminFromObjectPermissions(contextId, documentMetaData);

        assertTrue(documentMetaData.equals(result));
        List<DocumentMetadata> asList = SearchIterators.asList(result);
        for (DocumentMetadata documentMetadata2 : asList) {
            assertTrue(documentMetadata2.getObjectPermissions().stream().anyMatch(x -> x.getEntity() == adminUserId));
            assertTrue(documentMetadata2.getObjectPermissions().stream().allMatch(x -> original.contains(I(x.getEntity()))));
        }
    }

    //============================================================================================================//
    //============================================================================================================//

    private static TimedResult<DocumentMetadata> getDocumentMetadataTimedResult(int... ids) {
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
        getObjectPermissionsSearchIterator();
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
        List<Integer> originalIdentifiers = Arrays.asList(I(4), I(44), I(99999), I(adminContactId));
        TimedResult<DocumentMetadata> original = getDocumentMetadataTimedResult(originalIdentifiers.stream().mapToInt(i -> i.intValue()).toArray());

        TimedResult<DocumentMetadata> result = serviceSpy.removeAdminFromObjectPermissions(contextId, original);

        assertEquals(original.results().size(), result.results().size());
        assertEquals(original.sequenceNumber(), result.sequenceNumber());
        List<DocumentMetadata> resultAsList = SearchIterators.asList(result.results());
        for (DocumentMetadata documentMetadata : resultAsList) {
            assertTrue(documentMetadata.getObjectPermissions().stream().noneMatch(x -> x.getEntity() == adminUserId));
            assertTrue(documentMetadata.getObjectPermissions().stream().allMatch(x -> originalIdentifiers.contains(I(x.getEntity()))));
        }
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntTimedResultOfDocumentMetadata_adminInDocumentMetadata_removeAdmin() throws OXException {
        List<Integer> originalIdentifiers = Arrays.asList(I(4), I(44), I(99999), I(adminContactId), I(adminUserId));
        TimedResult<DocumentMetadata> original = getDocumentMetadataTimedResult(originalIdentifiers.stream().mapToInt(i -> i.intValue()).toArray());

        TimedResult<DocumentMetadata> result = serviceSpy.removeAdminFromObjectPermissions(contextId, original);

        assertEquals(original.results().size(), result.results().size());
        assertEquals(original.sequenceNumber(), result.sequenceNumber());
        List<DocumentMetadata> resultAsList = SearchIterators.asList(result.results());
        for (DocumentMetadata documentMetadata : resultAsList) {
            assertTrue(documentMetadata.getObjectPermissions().stream().noneMatch(x -> x.getEntity() == adminUserId));
            assertTrue(documentMetadata.getObjectPermissions().stream().allMatch(x -> originalIdentifiers.contains(I(x.getEntity()))));
        }
    }

    @Test
    public void testRemoveAdminFromObjectPermissionsIntTimedResultOfDocumentMetadata_featureDisabeld_returnUnchanged() throws OXException {
        Mockito.when(B(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any()))).thenReturn(Boolean.TRUE);
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<Integer> originalIdentifiers = Arrays.asList(I(4), I(44), I(99999), I(adminContactId), I(adminUserId));
        TimedResult<DocumentMetadata> original = getDocumentMetadataTimedResult(originalIdentifiers.stream().mapToInt(i -> i.intValue()).toArray());

        TimedResult<DocumentMetadata> result = service.removeAdminFromObjectPermissions(contextId, original);

        assertEquals(original.results().size(), result.results().size());
        assertEquals(original.sequenceNumber(), result.sequenceNumber());
        List<DocumentMetadata> resultAsList = SearchIterators.asList(result.results());
        for (DocumentMetadata documentMetadata : resultAsList) {
            assertTrue(documentMetadata.getObjectPermissions().stream().anyMatch(x -> x.getEntity() == adminUserId));
            assertTrue(documentMetadata.getObjectPermissions().stream().allMatch(x -> originalIdentifiers.contains(I(x.getEntity()))));
        }
    }

    //============================================================================================================//
    //============================================================================================================//

    @Test
    public void testAddAdminToObjectPermissions_originalNull_returnUpdated()  {
        List<ObjectPermission> updatedPermissions = getObjectPermissions(adminUserId, 59, 444, 99);

        List<ObjectPermission> result = serviceSpy.addAdminToObjectPermissions(contextId, null, updatedPermissions);

        assertTrue(updatedPermissions.equals(result));
    }

    @Test
    public void testAddAdminToObjectPermissions_updatelNull_returnNull()  {
        List<ObjectPermission> originalPermissions = getObjectPermissions(adminUserId, 59, 444, 99);

        List<ObjectPermission> result = serviceSpy.addAdminToObjectPermissions(contextId, originalPermissions, null);

        assertNull(result);
    }

    @Test
    public void testAddAdminToObjectPermissions_originalEmpty_returnUpdated()  {
        List<ObjectPermission> originalPermissions = getObjectPermissions();
        List<ObjectPermission> updatedPermissions = getObjectPermissions(adminUserId, 59, 444, 99);

        List<ObjectPermission> result = serviceSpy.addAdminToObjectPermissions(contextId, originalPermissions, updatedPermissions);

        assertTrue(updatedPermissions.equals(result));
    }

    @Test
    public void testAddAdminToObjectPermissions_adminNotInOriginal_returnUpdated()  {
        List<ObjectPermission> originalPermissions = getObjectPermissions(77, 88, 99);
        List<ObjectPermission> updatedPermissions = getObjectPermissions(adminUserId, 59, 444, 99);

        List<ObjectPermission> result = serviceSpy.addAdminToObjectPermissions(contextId, originalPermissions, updatedPermissions);

        assertTrue(result.stream().anyMatch(x -> x.getEntity() == adminUserId));
        assertEquals(updatedPermissions.size(), result.size());
    }

    @Test
    public void testAddAdminToObjectPermissions_adminNotInOriginalAndUpdated_returnUpdated()  {
        List<ObjectPermission> originalPermissions = getObjectPermissions(77, 88, 99);
        List<ObjectPermission> updatedPermissions = getObjectPermissions(59, 444, 99);

        List<ObjectPermission> result = serviceSpy.addAdminToObjectPermissions(contextId, originalPermissions, updatedPermissions);

        assertTrue(result.stream().noneMatch(x -> x.getEntity() == adminUserId));
        assertEquals(updatedPermissions.size(), result.size());
    }

    @Test
    public void testAddAdminToObjectPermissions_adminInOriginalAndUpdated_returnUpdated()  {
        List<ObjectPermission> originalPermissions = getObjectPermissions(77, 88, 99, adminUserId);
        List<ObjectPermission> updatedPermissions = getObjectPermissions(59, 444, adminUserId, 99);

        List<ObjectPermission> result = serviceSpy.addAdminToObjectPermissions(contextId, originalPermissions, updatedPermissions);

        assertTrue(result.stream().anyMatch(x -> x.getEntity() == adminUserId));
        assertEquals(updatedPermissions.size(), result.size());
    }

    @Test
    public void testAddAdminToObjectPermissions_adminInOriginal_returnUpdated()  {
        List<ObjectPermission> originalPermissions = getObjectPermissions(77, 88, 99, adminUserId);
        List<ObjectPermission> updatedPermissions = getObjectPermissions(59, 444, 99);

        List<ObjectPermission> result = serviceSpy.addAdminToObjectPermissions(contextId, originalPermissions, updatedPermissions);

        assertTrue(result.stream().anyMatch(x -> x.getEntity() == adminUserId));
        assertEquals(updatedPermissions.size() + 1, result.size());
    }

    @Test
    public void testAddAdminToObjectPermissions_featureDisabled_returnUpdated()  {
        Mockito.when(B(leanConfigurationService.getBooleanProperty(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), (Property) ArgumentMatchers.any()))).thenReturn(Boolean.TRUE);
        HideAdminServiceImpl service = new HideAdminServiceImpl(leanConfigurationService, contextService, userService);
        List<ObjectPermission> originalPermissions = getObjectPermissions(77, 88, 99, adminUserId);
        List<ObjectPermission> updatedPermissions = getObjectPermissions(59, 444, 99);

        List<ObjectPermission> result = service.addAdminToObjectPermissions(contextId, originalPermissions, updatedPermissions);

        assertTrue(updatedPermissions.equals(result));
    }
}
