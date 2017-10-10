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

package com.openexchange.chronos.storage.rdb;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONObject;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.storage.CalendarAccountStorage;
import com.openexchange.chronos.storage.rdb.osgi.Services;
import com.openexchange.exception.OXException;


/**
 * {@link CachingCalendarAccountStorage}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10
 */
public class CachingCalendarAccountStorage implements CalendarAccountStorage {

    private static final String REGION_NAME = "CalendarAccount";

    private final RdbCalendarAccountStorage delegate;

    private final int contextId;

    public CachingCalendarAccountStorage(RdbCalendarAccountStorage delegate, int contextId) {
        super();
        this.delegate = delegate;
        this.contextId = contextId;
    }

    @Override
    public int createAccount(String providerId, int userId, JSONObject internalConfig, JSONObject userConfig) throws OXException {
        return delegate.createAccount(providerId, userId, internalConfig, userConfig);
    }

    @Override
    public void updateAccount(int userId, int id, JSONObject internalConfig, JSONObject userConfig, long timestamp) throws OXException {
        delegate.updateAccount(userId, id, internalConfig, userConfig, timestamp);
        invalidateCalendarAccounts(new int[] { id }, userId);
    }

    @Override
    public void deleteAccount(int userId, int id) throws OXException {
        delegate.deleteAccount(userId, id);
        invalidateCalendarAccounts(new int[] { id }, userId);
    }

    @Override
    public CalendarAccount getAccount(int userId, int id) throws OXException {
        final CacheService cacheService = getCacheService();
        if (null == cacheService) {
            return delegate.getAccount(userId, id);
        }
        final Cache cache = cacheService.getCache(REGION_NAME);
        final Object obj = cache.get(newCacheKey(cacheService, id, userId));
        if (obj instanceof CalendarAccount) {
            return (CalendarAccount) obj;
        }

        final CalendarAccount calendarAccount = delegate.getAccount(userId, id);
        if (null != calendarAccount) {
            cache.put(newCacheKey(cacheService, id, userId), calendarAccount, false);
        }

        return calendarAccount;
    }

    @Override
    public List<CalendarAccount> getAccounts(int userId) throws OXException {
        final CacheService cacheService = getCacheService();
        if (null == cacheService) {
            return delegate.getAccounts(userId);
        }
        final Cache cache = cacheService.getCache(REGION_NAME);
        List<CalendarAccount> calendarAccountList = delegate.getAccounts(userId);
        for (CalendarAccount calendarAccount : calendarAccountList) {
            if (null == cache.get(newCacheKey(cacheService, calendarAccount.getAccountId(), userId))) {
                cache.put(newCacheKey(cacheService, calendarAccount.getAccountId(), userId), calendarAccount, false);
            }
        }
        return calendarAccountList;
    }

    @Override
    public List<CalendarAccount> getAccounts(String providerId, int[] userIds) throws OXException {
        final CacheService cacheService = getCacheService();
        if (null == cacheService) {
            return delegate.getAccounts(providerId, userIds);
        }
        final Cache cache = cacheService.getCache(REGION_NAME);
        List<CalendarAccount> calendarAccountList = delegate.getAccounts(providerId, userIds);
        for (CalendarAccount calendarAccount : calendarAccountList) {
            if (null == cache.get(newCacheKey(cacheService, calendarAccount.getAccountId(), calendarAccount.getUserId()))) {
                cache.put(newCacheKey(cacheService, calendarAccount.getAccountId(), calendarAccount.getUserId()), calendarAccount, false);
            }
        }
        return calendarAccountList;
    }

    private void invalidateCalendarAccounts(int[] accIds, int userId) throws OXException {
        CacheService cacheService = getCacheService();
        if (null == cacheService) {
            // Cache not initialized.
            return;
        }
        /*
         * gather cache keys to invalidate
         */
        Cache cache = cacheService.getCache(REGION_NAME);
        List<Serializable> keys = new LinkedList<Serializable>();
        for (int accId : accIds) {
            Integer key = accId;
            keys.add(newCacheKey(cacheService, key, userId));
        }

        cache.remove(keys);
    }

    private CacheKey newCacheKey(final CacheService cacheService, int accId, int userId) {
        return cacheService.newCacheKey(contextId, String.valueOf(accId), String.valueOf(userId));
    }

    private CacheService getCacheService() {
        return Services.getOptionalService(CacheService.class);
    }

}
