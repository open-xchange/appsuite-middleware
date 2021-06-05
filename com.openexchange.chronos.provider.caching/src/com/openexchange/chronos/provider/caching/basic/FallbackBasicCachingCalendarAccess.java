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

package com.openexchange.chronos.provider.caching.basic;

import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.basic.FallbackBasicCalendarAccess;
import com.openexchange.chronos.provider.caching.AccountConfigHelper;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link FallbackBasicCachingCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class FallbackBasicCachingCalendarAccess extends FallbackBasicCalendarAccess {

    private final Session session;
    private final OXException error;

    /**
     * Initializes a new {@link FallbackBasicCachingCalendarAccess}.
     *
     * @param session The session
     * @param account The underlying calendar account
     * @param error The error to to include in the accesses' calendar settings, or <code>null</code> if not defined
     */
    public FallbackBasicCachingCalendarAccess(Session session, CalendarAccount account, OXException error) {
        super(account);
        this.session = session;
        this.error = error;
    }

    @Override
    public CalendarSettings getSettings() {
        CalendarSettings settings = new AccountConfigHelper(account, session).getCalendarSettings();
        if (null != error) {
            settings.setError(error);
        }
        return settings;
    }

    @Override
    public String toString() {
        return "FallbackBasicCachingCalendarAccess [account=" + account + "]";
    }

}
