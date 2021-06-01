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

package com.openexchange.chronos.provider.google.config;

import com.openexchange.chronos.provider.google.osgi.Services;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.session.Session;

/**
 * {@link GoogleCalendarConfig}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class GoogleCalendarConfig {

    private static final Long REFRESH_INTERVAL = new Long(10);

    private static final Long RETRY_ON_ERROR_INTERVAL = new Long(1800);

    private static final Property REFRESH_INTERVAL_PROP = DefaultProperty.valueOf("com.openexchange.calendar.provider.google.refreshInterval", REFRESH_INTERVAL);

    private static final Property RETRY_ON_ERROR_INTERVAL_PROP = DefaultProperty.valueOf("com.openexchange.calendar.provider.google.retryOnErrorInterval", RETRY_ON_ERROR_INTERVAL);

    public static long getResfrehInterval(Session session) {

        LeanConfigurationService service = Services.getService(LeanConfigurationService.class);
        if (service == null) {
            return REFRESH_INTERVAL.longValue();
        }
        return service.getLongProperty(session.getUserId(), session.getContextId(), REFRESH_INTERVAL_PROP);

    }

    public static long getRetryOnErrorInterval(Session session) {

        LeanConfigurationService service = Services.getService(LeanConfigurationService.class);
        if (service == null) {
            return RETRY_ON_ERROR_INTERVAL.longValue();
        }
        return service.getLongProperty(session.getUserId(), session.getContextId(), RETRY_ON_ERROR_INTERVAL_PROP);

    }
}
