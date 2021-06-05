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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.provider.AutoProvisioningCalendarProvider;
import com.openexchange.chronos.provider.CalendarCapability;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.FreeBusyProvider;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.osgi.ServiceSet;

/**
 * {@link CalendarProviderRegistryImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarProviderRegistryImpl implements CalendarProviderRegistry {

    private final ServiceListing<CalendarProvider> calendarProviders;
    private final ServiceSet<FreeBusyProvider> freeBusyProviders;

    /**
     * Initializes a new {@link CalendarProviderRegistryImpl}.
     *
     * @param calendarProviders The calendar providers service set
     * @param freeBusyProviders The feee/busy providers service set
     */
    public CalendarProviderRegistryImpl(ServiceListing<CalendarProvider> calendarProviders, ServiceSet<FreeBusyProvider> freeBusyProviders) {
        super();
        this.calendarProviders = calendarProviders;
        this.freeBusyProviders = freeBusyProviders;
    }

    @Override
    public CalendarProvider getCalendarProvider(String id) {
        for (CalendarProvider calendarProvider : calendarProviders) {
            if (id.equals(calendarProvider.getId())) {
                return calendarProvider;
            }
        }
        return null;
    }

    @Override
    public List<CalendarProvider> getCalendarProviders() {
        return calendarProviders.getServiceList();
    }

    public List<CalendarProvider> getCalendarProviders(CalendarCapability capabilitiy) {
        List<CalendarProvider> providers = new ArrayList<CalendarProvider>();
        for (CalendarProvider provider : calendarProviders) {
            if (provider.getCapabilities().contains(capabilitiy)) {
                providers.add(provider);
            }
        }
        return Collections.unmodifiableList(providers);
    }

    @Override
    public List<AutoProvisioningCalendarProvider> getAutoProvisioningCalendarProviders() {
        List<AutoProvisioningCalendarProvider> autoProvisioningCalendarProviders = new ArrayList<AutoProvisioningCalendarProvider>();
        for (CalendarProvider calendarProvider : getCalendarProviders()) {
            if (AutoProvisioningCalendarProvider.class.isInstance(calendarProvider)) {
                autoProvisioningCalendarProviders.add((AutoProvisioningCalendarProvider) calendarProvider);
            }
        }
        return autoProvisioningCalendarProviders;
    }

    @Override
    public List<FreeBusyProvider> getFreeBusyProviders() {
        return Collections.unmodifiableList(new ArrayList<FreeBusyProvider>(freeBusyProviders));
    }

}
