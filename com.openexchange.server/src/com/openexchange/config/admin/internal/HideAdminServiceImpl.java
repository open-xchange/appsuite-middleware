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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang.ArrayUtils;
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
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.CustomizableTimedResult;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.CustomizableSearchIterator;
import com.openexchange.tools.iterator.Customizer;
import com.openexchange.tools.iterator.FilteringSearchIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.user.UserService;

/**
 * {@link HideAdminServiceImpl}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class HideAdminServiceImpl implements HideAdminService {

    private final LeanConfigurationService leanConfigurationService;
    private final ContextService contextService;
    private final UserService userService;

    public HideAdminServiceImpl(LeanConfigurationService leanConfigurationService, ContextService contextService, UserService userService) {
        this.leanConfigurationService = leanConfigurationService;
        this.contextService = contextService;
        this.userService = userService;
    }

    @Override
    public boolean showAdmin(int contextId) {
        return contextId > 0 ? leanConfigurationService.getBooleanProperty(-1, contextId, HideAdminProperty.SHOW_ADMIN_ENABLED) : true;
    }

    private int getAdminUserId(int contextId) throws OXException {
        return contextService.getContext(contextId).getMailadmin();
    }

    private int getAdminContactId(int contextId) throws OXException {
        int adminUserId = getAdminUserId(contextId);
        return userService.getUser(adminUserId, contextId).getContactId();
    }

    @Override
    public SearchIterator<Contact> removeAdminFromContacts(int contextId, @Nullable SearchIterator<Contact> searchIterator) throws OXException {
        if (searchIterator == null || searchIterator.size() == 0 || showAdmin(contextId)) {
            return searchIterator;
        }

        int adminContactId = getAdminContactId(contextId);
        return new FilteringSearchIterator<Contact>(searchIterator) {

            @Override
            public boolean accept(Contact thing) {
                return thing.getObjectID() != adminContactId;
            }
        };
    }

    @Override
    public Group[] removeAdminFromGroupMemberList(int contextId, @Nullable Group[] groupList) throws OXException {
        if (groupList == null || groupList.length == 0 || showAdmin(contextId)) {
            return groupList;
        }

        int adminUserId = getAdminUserId(contextId);

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
    public int[] addAdminToGroupMemberList(int contextId, @Nullable int[] originalGroupMember, @Nullable int[] updatedGroupMember) throws OXException {
        if (originalGroupMember == null || originalGroupMember.length == 0 || updatedGroupMember == null || showAdmin(contextId)) {
            return updatedGroupMember;
        }

        int adminUserId = getAdminUserId(contextId);
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
    public UserizedFolder[] removeAdminFromFolderPermissions(int contextId, @Nullable UserizedFolder[] folderList) throws OXException {
        if (folderList == null || folderList.length == 0 || showAdmin(contextId)) {
            return folderList;
        }

        int adminUserId = getAdminUserId(contextId);

        List<PermissionFilterUserizedFolderImpl> copy = Lists.newArrayList();
        for (UserizedFolder folder : folderList) {
            PermissionFilterUserizedFolderImpl filteringUserizedFolder = new PermissionFilterUserizedFolderImpl(adminUserId, folder);
            copy.add(filteringUserizedFolder);
        }

        return copy.stream().toArray(UserizedFolder[]::new);
    }

    @Override
    public Permission[] addAdminToFolderPermissions(int contextId, @Nullable Permission[] originalPermissions, @Nullable Permission[] updatedPermissions) throws OXException {
        if (originalPermissions == null || originalPermissions.length == 0 || updatedPermissions == null || showAdmin(contextId)) {
            return updatedPermissions;
        }

        int adminUserId = getAdminUserId(contextId);
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
    public User[] removeAdminFromUsers(int contextId, @Nullable User[] userList) throws OXException {
        if (userList == null || userList.length == 0 || showAdmin(contextId)) {
            return userList;
        }

        int adminUserId = getAdminUserId(contextId);
        return Arrays.stream(userList).filter(x -> x.getId() != adminUserId).toArray(User[]::new);
    }

    @Override
    public int[] removeAdminFromUserIds(int contextId, @Nullable int[] userIds) throws OXException {
        if (userIds == null || userIds.length == 0 || showAdmin(contextId)) {
            return userIds;
        }

        int adminUserId = getAdminUserId(contextId);
        return Arrays.stream(userIds).filter(x -> x != adminUserId).toArray();
    }

    @Override
    public List<ObjectPermission> removeAdminFromObjectPermissions(int contextId, @Nullable List<ObjectPermission> objectPermissions) throws OXException {
        if (objectPermissions == null || objectPermissions.isEmpty() || showAdmin(contextId)) {
            return objectPermissions;
        }

        int adminUserId = getAdminUserId(contextId);
        return objectPermissions.stream().filter(x -> x.getEntity() != adminUserId).collect(Collectors.toList());
    }

    @Override
    public TimedResult<DocumentMetadata> removeAdminFromObjectPermissions(int contextId, @Nullable final TimedResult<DocumentMetadata> documents) throws OXException {
        if (documents == null || showAdmin(contextId)) {
            return documents;
        }

        final int adminUserId = getAdminUserId(contextId);
        return new CustomizableTimedResult<DocumentMetadata>(documents, new Customizer<DocumentMetadata>() {

            @Override
            public DocumentMetadata customize(DocumentMetadata thing) throws OXException {
                if (thing.getObjectPermissions() != null) {
                    return new PermissionFilterDocumentMetadataImpl(adminUserId, thing);
                }
                return thing;
            }
        });
    }

    @Override
    public SearchIterator<DocumentMetadata> removeAdminFromObjectPermissions(int contextId, @Nullable SearchIterator<DocumentMetadata> searchIterator) throws OXException {
        if (searchIterator == null || searchIterator.size() == 0 || showAdmin(contextId)) {
            return searchIterator;
        }

        final int adminUserId = getAdminUserId(contextId);
        return new CustomizableSearchIterator<>(searchIterator, new Customizer<DocumentMetadata>() {

            @Override
            public DocumentMetadata customize(DocumentMetadata thing) throws OXException {
                if (thing.getObjectPermissions() != null) {
                    return new PermissionFilterDocumentMetadataImpl(adminUserId, thing);
                }
                return thing;
            }
        });
    }

    @Override
    public List<ObjectPermission> addAdminToObjectPermissions(int contextId, @Nullable List<ObjectPermission> originalPermissions, @Nullable List<ObjectPermission> updatedPermissions) throws OXException {
        if (originalPermissions == null || originalPermissions.isEmpty() || updatedPermissions == null || showAdmin(contextId)) {
            return updatedPermissions;
        }

        int adminUserId = getAdminUserId(contextId);
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
