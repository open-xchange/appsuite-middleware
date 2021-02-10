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
