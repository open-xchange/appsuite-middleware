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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Autoboxing;
import com.openexchange.principalusecount.PrincipalUseCountService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class GroupServiceImpl implements GroupService {

    private final GroupStorage storage;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link GroupServiceImpl}.
     */
    public GroupServiceImpl(GroupStorage storage, ServiceLookup services) {
        this.storage = storage;
        this.services = services;
    }

    @Override
    public void create(Context context, User user, Group group, boolean checkI18nNames) throws OXException {
        Create create = new Create(context, user, group, checkI18nNames);
        create.perform();
    }

    @Override
    public void delete(Context context, User user, int groupId, Date lastRead) throws OXException {
        Delete delete = new Delete(context, user, groupId, lastRead);
        delete.perform();
    }

    @Override
    public Group getGroup(Context ctx, int groupId) throws OXException {
        return storage.getGroup(groupId, ctx);
    }

    @Override
    public Group getGroup(Context ctx, int groupId, boolean loadMembers) throws OXException {
        return storage.getGroup(groupId, loadMembers, ctx);
    }

    @Override
    public Group[] search(Context ctx, String pattern, boolean loadMembers) throws OXException {
        return storage.searchGroups(pattern, loadMembers, ctx);
    }

    @Override
    public Group[] getGroups(Context ctx, boolean loadMembers) throws OXException {
        return storage.getGroups(loadMembers, ctx);
    }

    @Override
    public Group[] listModifiedGroups(Context context, Date modifiedSince) throws OXException {
        return storage.listModifiedGroups(modifiedSince, context);
    }

    @Override
    public Group[] listDeletedGroups(Context context, Date deletedSince) throws OXException {
        return storage.listDeletedGroups(deletedSince, context);
    }

    @Override
    public Group[] searchGroups(Session session, String pattern, boolean loadMembers) throws OXException {
        Group[] groups = storage.searchGroups(pattern, loadMembers, ServerSessionAdapter.valueOf(session).getContext());
        return sortGroupsByUseCount(session, removeHiddenGroups(session, groups));
    }

    @Override
    public Group[] getGroups(Session session, boolean loadMembers) throws OXException {
        Group[] groups = storage.getGroups(loadMembers, ServerSessionAdapter.valueOf(session).getContext());
        return sortGroupsByUseCount(session, removeHiddenGroups(session, groups));
    }

    private Group[] sortGroupsByUseCount(Session session, Group[] groups) throws OXException {
        PrincipalUseCountService usecountService = services.getOptionalService(PrincipalUseCountService.class);
        if (usecountService == null) {
            return groups;
        }

        Integer[] principalIds = new Integer[groups.length];
        int x = 0;
        for (Group group : groups) {
            principalIds[x++] = Autoboxing.I(group.getIdentifier());
        }
        Map<Integer, Integer> useCounts = usecountService.get(session, principalIds);
        List<GroupAndUseCount> listToSort = new ArrayList<>(useCounts.size());
        x = 0;
        for (Group group : groups) {
            listToSort.add(new GroupAndUseCount(group, useCounts.get(Autoboxing.I(group.getIdentifier()))));
        }
        Collections.sort(listToSort);

        Group[] result = new Group[listToSort.size()];
        x = 0;
        for (GroupAndUseCount tmp : listToSort) {
            result[x++] = tmp.getGroup();
        }
        return result;
    }

    private static class GroupAndUseCount implements Comparable<GroupAndUseCount> {

        private final Group group;
        private final Integer usecount;

        /**
         * Initializes a new {@link GroupServiceImpl.GroupAndUseCount}.
         */
        public GroupAndUseCount(Group group, Integer usecount) {
            this.group = group;
            this.usecount = usecount;
        }

        /**
         * Gets the group
         *
         * @return The group
         */
        public Group getGroup() {
            return group;
        }

        /**
         * Gets the usecount
         *
         * @return The usecount
         */
        public Integer getUsecount() {
            return usecount;
        }

        @Override
        public int compareTo(GroupAndUseCount o) {
            return -this.usecount.compareTo(o.getUsecount());
        }

    }

    @Override
    public void update(Context context, User user, Group group, Date lastRead, boolean checkI18nNames) throws OXException {
        Update update = new Update(context, user, group, lastRead, checkI18nNames);
        update.perform();
    }

    @Override
    public Group[] listAllGroups(Context context, boolean loadMembers) throws OXException {
        return storage.getGroups(loadMembers, context);
    }

    @Override
    public Group[] listGroups(Context ctx, int[] ids) throws OXException {
        return storage.getGroup(ids, ctx);
    }

    /**
     * Filters all groups that are configured to be hidden from <i>all</i>- and <i>search</i>-responses.
     *
     * @param session The current user's session
     * @param groups The groups to filter
     * @return The (possibly filtered) groups
     */
    private Group[] removeHiddenGroups(Session session, Group[] groups) {
        if (null == groups || 0 == groups.length) {
            return groups;
        }
        LeanConfigurationService configService = services.getOptionalService(LeanConfigurationService.class);
        if (null == configService) {
            org.slf4j.LoggerFactory.getLogger(GroupServiceImpl.class).warn("Configuration not found, unable to filter hidden groups.");
            return groups;
        }
        boolean filtered = false;
        List<Group> filteredGroups = new ArrayList<Group>(groups.length);
        for (Group group : groups) {
            if (GroupStorage.GROUP_ZERO_IDENTIFIER == group.getIdentifier()) {
                if (configService.getBooleanProperty(session.getUserId(), session.getContextId(), GroupProperty.HIDE_ALL_USERS)) {
                    filtered = true;
                    continue;
                }
            } else if (GroupStorage.GUEST_GROUP_IDENTIFIER == group.getIdentifier()) {
                if (configService.getBooleanProperty(session.getUserId(), session.getContextId(), GroupProperty.HIDE_ALL_GUESTS)) {
                    filtered = true;
                    continue;
                }
            } else if (GroupStorage.GROUP_STANDARD_SIMPLE_NAME.equals(group.getSimpleName())) {
                if (configService.getBooleanProperty(session.getUserId(), session.getContextId(), GroupProperty.HIDE_STANDARD_GROUP)) {
                    filtered = true;
                    continue;
                }
            }
            filteredGroups.add(group);
        }
        return filtered ? filteredGroups.toArray(new Group[filteredGroups.size()]) : groups;
    }

}
