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

package com.openexchange.chronos.provider.caching;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProviders;
import com.openexchange.chronos.provider.caching.internal.CachingCalendarAccessConstants;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.session.Session;

/**
 * {@link CachingCalendarUtils} - provides some useful helper methods
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class CachingCalendarUtils {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CachingCalendarUtils.class);

    /**
     * (Only) Adapts the internal configuration for the given {@link CalendarAccount} account so that the next request might update the persisted data.
     * <p>
     * <b>Make sure that the new account configuration will be persisted.</b>
     *
     * @param calendarAccount The calendar account to invalidate the cache should be invalidated
     * @param newLastUpdateTimestamp The timestamp to set as 'last update'
     */
    public static void invalidateCache(CalendarAccount calendarAccount, long newLastUpdateTimestamp) {
        JSONObject internalConfiguration = calendarAccount.getInternalConfiguration();
        if (internalConfiguration.hasAndNotNull(CachingCalendarAccessConstants.CACHING)) {
            try {
                JSONObject caching = internalConfiguration.getJSONObject(CachingCalendarAccessConstants.CACHING);
                if (caching != null) {
                    caching.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, newLastUpdateTimestamp);
                }
            } catch (JSONException e) {
                LOG.error("Unable to retrieve caching information for calendar account '{}' with provider '{}': {}", calendarAccount.getAccountId(), calendarAccount.getProviderId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Gets a value indicating whether synchronization is enabled or not for accounts of a specific calendar provider.
     *
     * @param providerId The identifier of the provider to check
     * @param session The current user's session
     * @return <code>true</code> if accounts from this provider can be used for synchronization, <code>false</code> otherwise
     */
    public static boolean canBeUsedForSync(String providerId, Session session) {
        DefaultProperty property = DefaultProperty.valueOf(CalendarProviders.getUsedForSyncPropertyName(providerId), Boolean.FALSE);
        LeanConfigurationService leanConfigurationService = Services.getService(LeanConfigurationService.class);
        if (null == leanConfigurationService) {
            LOG.warn("Unable to access configuration service, assuming '{}' for '{}'.", property.getDefaultValue(), property.getFQPropertyName());
            return property.getDefaultValue(Boolean.class).booleanValue();
        }
        return leanConfigurationService.getBooleanProperty(session.getUserId(), session.getContextId(), property);
    }

}
