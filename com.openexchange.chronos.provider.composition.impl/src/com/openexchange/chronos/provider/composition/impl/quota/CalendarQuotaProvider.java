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

package com.openexchange.chronos.provider.composition.impl.quota;

import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.composition.impl.CompositingIDBasedCalendarQuotaProvider;
import com.openexchange.exception.OXException;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.AccountQuotas;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CalendarQuotaProvider}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class CalendarQuotaProvider implements QuotaProvider {

    private final CalendarProviderRegistry registry;
    private final ServiceLookup            services;

    /**
     *
     * Initializes a new {@link CalendarQuotaProvider}.
     *
     * @param services The {@link ServiceLookup} to get different services from
     * @param registry The {@link CalendarProviderRegistry} for {@link CompositingIDBasedCalendarQuotaProvider}
     * @throws OXException In case a service is unavailable
     */
    public CalendarQuotaProvider(ServiceLookup services, CalendarProviderRegistry registry) {
        super();
        this.services = services;
        this.registry = registry;
    }

    @Override
    public String getModuleID() {
        return "calendar";
    }

    @Override
    public String getDisplayName() {
        return "Calendar";
    }

    @Override
    public AccountQuota getFor(Session session, String accountID) throws OXException {
        CompositingIDBasedCalendarQuotaProvider provider = new CompositingIDBasedCalendarQuotaProvider(session, registry, services);
        return provider.get(accountID);
    }

    @Override
    public AccountQuotas getFor(Session session) throws OXException {
        CompositingIDBasedCalendarQuotaProvider provider = new CompositingIDBasedCalendarQuotaProvider(session, registry, services);
        return new AccountQuotas(provider.get(), null);
    }
}
