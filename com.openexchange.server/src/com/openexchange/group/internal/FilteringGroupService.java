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
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;

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
        delegate.update(context, user, origGroup, lastRead, checkI18nNames);
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
