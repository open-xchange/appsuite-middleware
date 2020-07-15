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

import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Implementation of the RDB group storage with caching capability.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public final class CachingGroupStorage implements GroupStorage {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CachingGroupStorage.class);
    }

    /**
     * Underlying group storage.
     */
    private final RdbGroupStorage delegate;

    /**
     * Default constructor.
     *
     * @param delegate The underlying group storage.
     */
    public CachingGroupStorage(final RdbGroupStorage delegate) {
        super();
        this.delegate = delegate;
    }

    private Optional<Cache> optCache() {
        CacheService service = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null == service) {
            return Optional.empty();
        }

        try {
            return Optional.of(service.getCache(CACHE_REGION_NAME));
        } catch (OXException e) {
            LoggerHolder.LOG.error("", e);
            return Optional.empty();
        }
    }

    @Override
    public Group getGroup(final int groupId, final Context ctx) throws OXException {
        return getGroup(groupId, true, ctx);
    }

    @Override
    public Group getGroup(final int groupId, boolean loadMembers, final Context ctx) throws OXException {
        return getGroups(new int[] { groupId }, loadMembers, ctx)[0];
    }

    @Override
    public Group[] getGroup(int[] groupIds, Context context) throws OXException {
        if (groupIds == null || groupIds.length <= 0) {
            return new Group[0];
        }
        return getGroups(groupIds, true, context);
    }

    private Group[] getGroups(int[] groupIds, boolean loadMembers, Context context) throws OXException {
        return getGroups(groupIds, loadMembers, context, optCache());
    }

    private Group[] getGroups(int[] groupIds, boolean loadMembers, Context context, Optional<Cache> optionalCache) throws OXException {
        if (optionalCache.isPresent() == false) {
            // No cache available. Therefore simply delegate the call.
            return delegate.getGroup(groupIds, loadMembers, context);
        }

        // Cache is available
        Cache cache = optionalCache.get();
        int length = groupIds.length;
        TIntObjectMap<Group> groups = new TIntObjectHashMap<>(length);
        TIntList toLoad = null;

        for (int groupId : groupIds) {
            Object object = cache.get(cacheKey(groupId, context, cache));
            if (object instanceof Group) {
                Group group = (Group) object;
                if (loadMembers && !group.isMemberSet()) {
                    if (toLoad == null) {
                        toLoad = new TIntArrayList(length);
                    }
                    toLoad.add(groupId);
                } else {
                    groups.put(groupId, (Group) group.clone()); // Fetch clone from cache
                }
            } else {
                if (toLoad == null) {
                    toLoad = new TIntArrayList(length);
                }
                toLoad.add(groupId);
            }
        }

        if (toLoad != null) {
            for (Group loadedGroup : delegate.getGroup(toLoad.toArray(), context)) {
                int groupId = loadedGroup.getIdentifier();
                groups.put(groupId, loadedGroup);
                cache.put(cacheKey(groupId, context, cache), (Group) loadedGroup.clone(), false); // Put clone into cache
            }
        }

        Group[] retval = new Group[length];
        for (int i = length; i-- > 0;) {
            Group group = groups.get(groupIds[i]);
            if (group == null) {
                throw LdapExceptionCode.GROUP_NOT_FOUND.create(Integer.valueOf(groupIds[i]), Integer.valueOf(context.getContextId())).setPrefix("GRP");
            }
            retval[i] = group;
        }
        return retval;
    }

    @Override
    public Group[] getGroups(final boolean loadMembers, final Context ctx) throws OXException {
        int[] groupIds;

        Optional<Cache> optionalCache = optCache();
        if (optionalCache.isPresent()) {
            Cache cache = optionalCache.get();
            CacheKey cacheKey = cacheKey(SPECIAL_FOR_ALL_GROUP_IDS, ctx, cache);
            Object object = cache.get(cacheKey);
            if (object instanceof int[]) {
                groupIds = (int[]) object;
            } else {
                groupIds = delegate.getGroupIds(ctx);
                cache.put(cacheKey, groupIds, false);
            }
        } else {
            groupIds = delegate.getGroupIds(ctx);
        }

        if (groupIds.length <= 0) {
            return new Group[0];
        }
        return getGroups(groupIds, loadMembers, ctx, optionalCache);
    }

    @Override
    public Group[] listModifiedGroups(final Date modifiedSince, final Context ctx) throws OXException {
        int[] groupIds = delegate.listModifiedGroupIds(modifiedSince, ctx);
        if (groupIds.length <= 0) {
            return new Group[0];
        }
        return getGroups(groupIds, true, ctx);
    }

    @Override
    public Group[] listDeletedGroups(final Date modifiedSince, final Context ctx) throws OXException {
        return delegate.listDeletedGroups(modifiedSince, ctx);
    }

    @Override
    public Group[] searchGroups(final String pattern, final boolean loadMembers, final Context ctx) throws OXException {
        if (referencesAllGroups(pattern)) {
            return getGroups(loadMembers, ctx);
        }

        int[] groupIds = delegate.searchGroupIds(pattern, ctx);
        if (groupIds.length <= 0) {
            return new Group[0];
        }
        return getGroups(groupIds, loadMembers, ctx);
    }

    /**
     * Tests whether the given pattern references all groups or not.
     * <p>
     * E.g. <code>"*"</code>
     *
     * @param pattern The pattern to check
     * @return <code>true</code> if the pattern references all groups, <code>false</code> otherwise
     */
    private static boolean referencesAllGroups(String pattern) {
        if (Strings.isEmpty(pattern)) {
            // Empty pattern is interpreted as "all"
            return true;
        }

        int length = pattern.length();
        for (int i = length; i-- > 0;) {
            char ch = pattern.charAt(i);
            if ('*' != ch && !Strings.isWhitespace(ch)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void insertGroup(final Context ctx, final Connection con, final Group group, final StorageType type) throws OXException {
        delegate.insertGroup(ctx, con, group, type);
        tryInvalidateFromCache(ctx, SPECIAL_FOR_ALL_GROUP_IDS);
    }

    @Override
    public void deleteMember(final Context ctx, final Connection con, final Group group, final int[] members) throws OXException {
        delegate.deleteMember(ctx, con, group, members);
        tryInvalidateFromCache(ctx, group);
    }

    @Override
    public void insertMember(final Context ctx, final Connection con, final Group group, final int[] members) throws OXException {
        delegate.insertMember(ctx, con, group, members);
        tryInvalidateFromCache(ctx, group);
    }

    @Override
    public void updateGroup(final Context ctx, final Connection con, final Group group, final Date lastRead) throws OXException {
        delegate.updateGroup(ctx, con, group, lastRead);
        tryInvalidateFromCache(ctx, group);
    }

    @Override
    public void deleteGroup(final Context ctx, final Connection con, final int groupId, final Date lastRead) throws OXException {
        delegate.deleteGroup(ctx, con, groupId, lastRead);
        tryInvalidateFromCache(ctx, groupId, SPECIAL_FOR_ALL_GROUP_IDS);
    }

    /**
     * Tries to invalidate the group from the cache if the cache exists.
     *
     * @param ctx The context
     * @param group The group to invalidate
     * @return <code>true</code> if the group was successfully invalidated, <code>false</code> otherwise
     */
    private boolean tryInvalidateFromCache(Context ctx, Group group) {
        Optional<Cache> optionalCache = optCache();
        if (optionalCache.isPresent() == false) {
            return false;
        }

        return invalidateFromCache(ctx, optionalCache.get(), group.getIdentifier());
    }

    /**
     * Tries to invalidate the groups from the cache if the cache exists.
     *
     * @param ctx The context
     * @param groupIds The identifiers of the groups to invalidate
     * @return <code>true</code> if the groups were successfully invalidated, <code>false</code> otherwise
     */
    private boolean tryInvalidateFromCache(Context ctx, int... groupIds) {
        Optional<Cache> optionalCache = optCache();
        if (optionalCache.isPresent() == false) {
            return false;
        }

        return invalidateFromCache(ctx, optionalCache.get(), groupIds);
    }

    /**
     * Invalidates the group identifiers from the given cache.
     *
     * @param ctx The context
     * @param cache The cache
     * @param groupIds The group identifiers to invalidate
     * @return <code>true</code> if the groups were successfully invalidated, <code>false</code> otherwise
     */
    private boolean invalidateFromCache(Context ctx, Cache cache, int... groupIds) {
        try {
            int length = groupIds.length;
            if (length == 1) {
                cache.remove(cacheKey(groupIds[0], ctx, cache));
            } else {
                List<Serializable> keys = new ArrayList<>(length);
                for (int groupId : groupIds) {
                    keys.add(cacheKey(groupId, ctx, cache));
                }
                cache.remove(keys);
            }
            return true;
        } catch (OXException e) {
            LoggerHolder.LOG.error("Failed to invalidate from cache", e);
        }
        return false;
    }

    /**
     * Creates a new cache key.
     *
     * @param groupId The group identifier
     * @param ctx The context
     * @param cache The cache
     * @return The newly created cache key
     */
    public static CacheKey cacheKey(int groupId, Context ctx, Cache cache) {
        return cache.newCacheKey(ctx.getContextId(), groupId);
    }

}
