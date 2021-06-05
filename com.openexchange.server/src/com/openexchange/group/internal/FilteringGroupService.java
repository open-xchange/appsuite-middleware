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

package com.openexchange.group.internal;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import com.openexchange.config.admin.HideAdminService;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;

/**
 * {@link FilteringGroupService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class FilteringGroupService implements GroupService {

    private GroupService delegate;
    private ServiceLookup services;

    public FilteringGroupService(GroupService lDelegate, ServiceLookup lServices) {
        this.delegate = lDelegate;
        this.services = lServices;
    }

    private Group[] removeAdminFromGroups(Context context, Group[] groups) throws OXException {
        HideAdminService hideAdminService = services.getOptionalService(HideAdminService.class);
        if (hideAdminService == null) {
            return groups;
        }
        return hideAdminService.removeAdminFromGroupMemberList(context.getContextId(), groups);
    }

    private Group getOriginalGroup(Context context, int groupId) throws OXException {
        return delegate.getGroup(context, groupId);
    }

    @Override
    public void create(Context context, User user, Group group, boolean checkI18nNames) throws OXException {
        delegate.create(context, user, group, checkI18nNames);
    }

    @Override
    public void delete(Context context, User user, int groupId, Date lastModified) throws OXException {
        delegate.delete(context, user, groupId, lastModified);
    }

    @Override
    public Group getGroup(Context context, int groupId) throws OXException {
        Group group = delegate.getGroup(context, groupId);
        Group[] groups = removeAdminFromGroups(context, new Group[] { group });
        return groups[0];
    }

    @Override
    public Group getGroup(Context context, int groupId, boolean loadMembers) throws OXException {
        Group group = delegate.getGroup(context, groupId, loadMembers);
        Group[] groups = removeAdminFromGroups(context, new Group[] { group });
        return groups[0];
    }

    @Override
    public Group[] listGroups(Context context, int[] groupIds) throws OXException {
        Group[] groups = delegate.listGroups(context, groupIds);
        HideAdminService hideAdminService = services.getOptionalService(HideAdminService.class);
        if (hideAdminService == null) {
            return groups;
        }
        Group[] groupsWithoutAdmin = hideAdminService.removeAdminFromGroupMemberList(context.getContextId(), groups);
        for (final int groupId : groupIds) {
            Optional<Group> found = Arrays.stream(groupsWithoutAdmin).filter(x -> x.getIdentifier() == groupId).findFirst();
            if (!found.isPresent()) {
                throw LdapExceptionCode.GROUP_NOT_FOUND.create(Integer.valueOf(groupId), Integer.valueOf(context.getContextId())).setPrefix("GRP");
            }
        }
        return groupsWithoutAdmin;
    }

    @Override
    public Group[] search(Context context, String pattern, boolean loadMembers) throws OXException {
        Group[] groups = delegate.search(context, pattern, loadMembers);
        return removeAdminFromGroups(context, groups);
    }

    @Override
    public Group[] listAllGroups(Context context, boolean loadMembers) throws OXException {
        Group[] groups = delegate.listAllGroups(context, loadMembers);
        return removeAdminFromGroups(context, groups);
    }

    @Override
    public Group[] listModifiedGroups(Context context, Date modifiedSince) throws OXException {
        Group[] groups = delegate.listModifiedGroups(context, modifiedSince);
        return removeAdminFromGroups(context, groups);
    }

    @Override
    public Group[] listDeletedGroups(Context context, Date deletedSince) throws OXException {
        Group[] groups = delegate.listDeletedGroups(context, deletedSince);
        return removeAdminFromGroups(context, groups);
    }

    @Override
    public void update(Context context, User user, Group group, Date lastRead, boolean checkI18nNames) throws OXException {
        HideAdminService hideAdminService = services.getOptionalService(HideAdminService.class);
        if (hideAdminService == null) {
            delegate.update(context, user, group, lastRead, checkI18nNames);
            return;
        }
        Group origGroup = getOriginalGroup(context, group.getIdentifier());
        int[] newGroupMember = hideAdminService.addAdminToGroupMemberList(context.getContextId(), origGroup.getMember(), group.getMember());
        group.setMember(newGroupMember);
        delegate.update(context, user, group, lastRead, checkI18nNames);
    }

    @Override
    public Group[] getGroups(Context ctx, boolean loadMembers) throws OXException {
        return removeAdminFromGroups(ctx, delegate.getGroups(ctx, loadMembers));
    }

    @Override
    public Group[] searchGroups(Session session, String pattern, boolean loadMembers) throws OXException {
        return removeAdminFromGroups(ServerSessionAdapter.valueOf(session).getContext(), delegate.searchGroups(session, pattern, loadMembers));
    }

    @Override
    public Group[] getGroups(Session session, boolean loadMembers) throws OXException {
        return removeAdminFromGroups(ServerSessionAdapter.valueOf(session).getContext(), delegate.getGroups(session, loadMembers));
    }
}
