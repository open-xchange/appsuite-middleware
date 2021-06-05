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

package com.openexchange.chronos.provider;

import java.util.List;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link CalendarProviderRegistry}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
@SingletonService
public interface CalendarProviderRegistry {

    /**
     * Gets a specific calendar provider.
     *
     * @param id The identifier of the calendar provider to get
     * @return The calendar provider, or <code>null</code> if not found
     */
    CalendarProvider getCalendarProvider(String id);

    /**
     * Gets all registered calendar providers.
     *
     * @return A list of all registered calendar providers
     */
    List<CalendarProvider> getCalendarProviders();

    /**
     * Gets all registered <i>auto-provisioning</i> calendar providers.
     *
     * @return A list of all registered auto-provisioning calendar providers
     */
    List<AutoProvisioningCalendarProvider> getAutoProvisioningCalendarProviders();

    /**
     * Gets all registered free/busy providers.
     *
     * @return A list of all registered free/busy providers
     */
    List<FreeBusyProvider> getFreeBusyProviders();

}
