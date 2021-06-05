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

package com.openexchange.chronos.provider.composition.impl;

import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.UsedForSync;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.basic.CommonCalendarConfigurationFields;
import com.openexchange.chronos.provider.basic.FallbackBasicCalendarAccess;
import com.openexchange.exception.OXException;

/**
 * {@link FallbackUnknownCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class FallbackUnknownCalendarAccess extends FallbackBasicCalendarAccess {

    private final OXException error;

    /**
     * Initializes a new {@link FallbackUnknownCalendarAccess}.
     *
     * @param account The underlying calendar account
     * @param error The error to include in the accesses' calendar settings, or <code>null</code> if not defined
     */
    public FallbackUnknownCalendarAccess(CalendarAccount account, OXException error) {
        super(account);
        this.error = error;
    }

    @Override
    public CalendarSettings getSettings() {
        CalendarSettings settings = new CalendarSettings();
        settings.setLastModified(account.getLastModified());
        settings.setConfig(account.getUserConfiguration());
        settings.setName(getAccountName(account));
        settings.setSubscribed(true);
        settings.setUsedForSync(UsedForSync.DEACTIVATED);
        settings.setError(error);
        return settings;
    }

    /**
     * Gets a (fallback) for the account's display name, in case the corresponding settings are not available.
     *
     * @param account The account to get the name for
     * @return The account name
     */
    private static String getAccountName(CalendarAccount account) {
        String fallbackName = "Account " + account.getAccountId();
        try {
            JSONObject internalConfig = account.getInternalConfiguration();
            if (null != internalConfig) {
                return internalConfig.optString(CommonCalendarConfigurationFields.NAME, fallbackName);
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(FallbackUnknownCalendarAccess.class).debug(
                "Error getting display name for calendar account \"{}\": {}", account.getProviderId(), e.getMessage());
        }
        return fallbackName;
    }

}
