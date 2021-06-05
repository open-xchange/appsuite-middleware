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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import com.google.common.collect.Lists;
import com.openexchange.annotation.Nullable;
import com.openexchange.config.admin.HideAdminService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.results.CustomizableDelta;
import com.openexchange.groupware.results.CustomizableTimedResult;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.CustomizableSearchIterator;
import com.openexchange.tools.iterator.FilteringSearchIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link HideAdminServiceImpl}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class HideAdminServiceImpl implements HideAdminService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(HideAdminServiceImpl.class);

    private final LeanConfigurationService leanConfigurationService;
    private final ContextService contextService;
    private final UserService userService;

    /**
     * Initializes a new {@link HideAdminServiceImpl}.
     *
     * @param leanConfigurationService The configuration service to use
     * @param contextService The context service to use
     * @param userService The user service to use
     */
    public HideAdminServiceImpl(LeanConfigurationService leanConfigurationService, ContextService contextService, UserService userService) {
        super();
        this.leanConfigurationService = leanConfigurationService;
        this.contextService = contextService;
        this.userService = userService;
    }

    @Override
    public boolean showAdmin(int contextId) {
        return contextId > 0 ? leanConfigurationService.getBooleanProperty(-1, contextId, HideAdminProperty.SHOW_ADMIN_ENABLED) : true;
    }

    /**
     * Returns the user id from the administrator of the given context or -1 if an error occurred.
     *
     * @param contextId The context id
     * @return int user id of the administrator or -1 in case of an error
     */
    private int getAdminUserId(int contextId) {
        try {
            return contextService.getContext(contextId).getMailadmin();
        } catch (OXException e) {
            LOG.info("Unable to retrieve user id for admin.", e);
            return -1;
        }
    }

    /**
     * Returns the contact id from the administrator of the given context or -1 if an error occurred.
     *
     * @param contextId The context id
     * @return int contact id of the administrator or -1 in case of an error
     */
    private int getAdminContactId(int contextId) {
        int adminUserId = getAdminUserId(contextId);
        if (adminUserId < 0) {
            return adminUserId;
        }
        try {
            return userService.getUser(adminUserId, contextId).getContactId();
        } catch (OXException e) {
            LOG.info("Unable to retrieve contact id for admin.", e);
            return -1;
        }
    }

    @Override
    public SearchIterator<Contact> removeAdminFromContacts(int contextId, @Nullable SearchIterator<Contact> searchIterator) {
        if (searchIterator == null || searchIterator.size() == 0 || showAdmin(contextId)) {
            return searchIterator;
        }

        int adminContactId = getAdminContactId(contextId);
        if (adminContactId < 0) {
            return searchIterator;
        }
        try {
            return new FilteringSearchIterator<Contact>(searchIterator) {

                @Override
                public boolean accept(Contact thing) {
                    return thing.getObjectID() != adminContactId;
                }
            };
        } catch (OXException e) {
            LOG.info("Unable to initialize the SearchIterator.", e);
        }
        return searchIterator;
    }

    @Override
    public Group[] removeAdminFromGroupMemberList(int contextId, @Nullable Group[] groupList) {
        if (groupList == null || groupList.length == 0 || showAdmin(contextId)) {
            return groupList;
        }

        int adminUserId = getAdminUserId(contextId);
        if (adminUserId < 0) {
            return groupList;
        }

        for (int i = 0; i < groupList.length; i++) {
            Group group = groupList[i];
            if (group.isMemberSet() && ArrayUtils.contains(group.getMember(), adminUserId)) {
                Group newGroup = new Group(group);
                newGroup.setMember(ArrayUtils.removeElement(group.getMember(), adminUserId));
                groupList[i] = newGroup;
            }
        }

        return groupList;
    }

    @Override
    public int[] addAdminToGroupMemberList(int contextId, @Nullable int[] originalGroupMember, @Nullable int[] updatedGroupMember) {
        if (originalGroupMember == null || originalGroupMember.length == 0 || updatedGroupMember == null || showAdmin(contextId)) {
            return updatedGroupMember;
        }

        int adminUserId = getAdminUserId(contextId);
        if (adminUserId < 0) {
            return updatedGroupMember;
        }
        int[] newGroupMember = ArrayUtils.clone(updatedGroupMember);
        if (ArrayUtils.contains(newGroupMember, adminUserId)) {
            return newGroupMember;
        }

        if (ArrayUtils.contains(originalGroupMember, adminUserId)) {
            newGroupMember = ArrayUtils.add(newGroupMember, adminUserId);
        }

        return newGroupMember;
    }

    @Override
    public UserizedFolder[] removeAdminFromFolderPermissions(int contextId, @Nullable UserizedFolder[] folderList) {
        if (folderList == null || folderList.length == 0 || showAdmin(contextId)) {
            return folderList;
        }

        int adminUserId = getAdminUserId(contextId);
        if (adminUserId < 0) {
            return folderList;
        }

        List<PermissionFilterUserizedFolderImpl> copy = Lists.newArrayList();
        for (UserizedFolder folder : folderList) {
            PermissionFilterUserizedFolderImpl filteringUserizedFolder = new PermissionFilterUserizedFolderImpl(adminUserId, folder);
            copy.add(filteringUserizedFolder);
        }

        return copy.stream().toArray(UserizedFolder[]::new);
    }

    @Override
    public Permission[] addAdminToFolderPermissions(int contextId, @Nullable Permission[] originalPermissions, @Nullable Permission[] updatedPermissions) {
        if (originalPermissions == null || originalPermissions.length == 0 || updatedPermissions == null || showAdmin(contextId)) {
            return updatedPermissions;
        }

        int adminUserId = getAdminUserId(contextId);
        if (adminUserId < 0) {
            return updatedPermissions;
        }
        List<Permission> newPermissions = new ArrayList<>(Arrays.asList(updatedPermissions));
        Optional<Permission> adminInUpdate = newPermissions.stream().filter(x -> x.getEntity() == adminUserId).findFirst();
        if (adminInUpdate.isPresent()) {
            return newPermissions.stream().toArray(Permission[]::new);
        }

        Optional<Permission> adminInOriginal = Arrays.stream(originalPermissions).filter(x -> x.getEntity() == adminUserId).findFirst();
        if (adminInOriginal.isPresent()) {
            newPermissions.add(adminInOriginal.get());
        }

        return newPermissions.stream().toArray(Permission[]::new);
    }

    @Override
    public User[] removeAdminFromUsers(int contextId, @Nullable User[] userList) {
        if (userList == null || userList.length == 0 || showAdmin(contextId)) {
            return userList;
        }

        int adminUserId = getAdminUserId(contextId);
        if (adminUserId < 0) {
            return userList;
        }
        return Arrays.stream(userList).filter(x -> x.getId() != adminUserId).toArray(User[]::new);
    }

    @Override
    public int[] removeAdminFromUserIds(int contextId, @Nullable int[] userIds) {
        if (userIds == null || userIds.length == 0 || showAdmin(contextId)) {
            return userIds;
        }

        int adminUserId = getAdminUserId(contextId);
        if (adminUserId < 0) {
            return userIds;
        }
        return Arrays.stream(userIds).filter(x -> x != adminUserId).toArray();
    }

    @Override
    public List<ObjectPermission> removeAdminFromObjectPermissions(int contextId, @Nullable List<ObjectPermission> objectPermissions) {
        if (objectPermissions == null || objectPermissions.isEmpty() || showAdmin(contextId)) {
            return objectPermissions;
        }

        int adminUserId = getAdminUserId(contextId);
        if (adminUserId < 0) {
            return objectPermissions;
        }
        return objectPermissions.stream().filter(x -> x.getEntity() != adminUserId).collect(Collectors.toList());
    }

    @Override
    public TimedResult<DocumentMetadata> removeAdminFromObjectPermissions(int contextId, @Nullable final TimedResult<DocumentMetadata> documents) {
        if (documents == null || showAdmin(contextId)) {
            return documents;
        }
        int adminUserId = getAdminUserId(contextId);
        if (adminUserId < 0) {
            return documents;
        }
        return new CustomizableTimedResult<DocumentMetadata>(documents, new PermissionFilterDocumentCustomizer(adminUserId));
    }

    @Override
    public SearchIterator<DocumentMetadata> removeAdminFromObjectPermissions(int contextId, @Nullable SearchIterator<DocumentMetadata> searchIterator) {
        if (searchIterator == null || searchIterator.size() == 0 || showAdmin(contextId)) {
            return searchIterator;
        }
        int adminUserId = getAdminUserId(contextId);
        if (adminUserId < 0) {
            return searchIterator;
        }

        return new CustomizableSearchIterator<>(searchIterator, new PermissionFilterDocumentCustomizer(adminUserId));
    }

    @Override
    public Delta<DocumentMetadata> removeAdminFromObjectPermissions(int contextId, @Nullable Delta<DocumentMetadata> delta) {
        if (delta == null || showAdmin(contextId)) {
            return delta;
        }
        int adminUserId = getAdminUserId(contextId);
        if (adminUserId < 0) {
            return delta;
        }
        return new CustomizableDelta<>(delta, new PermissionFilterDocumentCustomizer(adminUserId));
    }

    @Override
    public List<ObjectPermission> addAdminToObjectPermissions(int contextId, @Nullable List<ObjectPermission> originalPermissions, @Nullable List<ObjectPermission> updatedPermissions) {
        if (originalPermissions == null || originalPermissions.isEmpty() || updatedPermissions == null || showAdmin(contextId)) {
            return updatedPermissions;
        }

        int adminUserId = getAdminUserId(contextId);
        if (adminUserId < 0) {
            return updatedPermissions;
        }

        List<ObjectPermission> newPermissions = new ArrayList<>(updatedPermissions);
        Optional<ObjectPermission> adminInUpdate = newPermissions.stream().filter(x -> x.getEntity() == adminUserId).findFirst();
        if (adminInUpdate.isPresent()) {
            return newPermissions;
        }

        Optional<ObjectPermission> previousAdminPermission = originalPermissions.stream().filter(x -> x.getEntity() == adminUserId).findFirst();
        if (previousAdminPermission.isPresent()) {
            newPermissions.add(previousAdminPermission.get());
        }

        return newPermissions;
    }
}
