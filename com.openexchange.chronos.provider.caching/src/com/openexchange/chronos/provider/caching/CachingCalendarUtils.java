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

package com.openexchange.chronos.provider.caching;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
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
                    caching.putSafe(CachingCalendarAccessConstants.LAST_UPDATE, L(newLastUpdateTimestamp));
                }
            } catch (JSONException e) {
                LOG.error("Unable to retrieve caching information for calendar account '{}' with provider '{}': {}", I(calendarAccount.getAccountId()), calendarAccount.getProviderId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Gets a value indicating whether synchronization is enabled or not for accounts of a specific calendar provider.
     *
     * @param providerId The identifier of the provider to check
     * @param session The current user's session
     * @param def The default value
     * @return <code>true</code> if accounts from this provider can be used for synchronization, <code>false</code> otherwise
     */
    public static boolean canBeUsedForSync(String providerId, Session session, boolean def) {
        DefaultProperty property = DefaultProperty.valueOf(CalendarProviders.getUsedForSyncPropertyName(providerId), Boolean.valueOf(def));
        LeanConfigurationService leanConfigurationService = Services.getService(LeanConfigurationService.class);
        if (null == leanConfigurationService) {
            LOG.warn("Unable to access configuration service, assuming '{}' for '{}'.", property.getDefaultValue(), property.getFQPropertyName());
            return property.getDefaultValue(Boolean.class).booleanValue();
        }
        return leanConfigurationService.getBooleanProperty(session.getUserId(), session.getContextId(), property);
    }

    /**
     * Gets a value indicating whether synchronization is enabled or not for accounts of a specific calendar provider.
     *
     * @param providerId The identifier of the provider to check
     * @param session The current user's session
     * @return <code>true</code> if accounts from this provider can be used for synchronization, <code>false</code> otherwise
     */
    public static boolean canBeUsedForSync(String providerId, Session session) {
        return canBeUsedForSync(providerId, session, true);
    }

}
