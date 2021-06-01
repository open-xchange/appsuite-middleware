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

import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CompositingIDBasedCalendarAccessFactory}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CompositingIDBasedCalendarAccessFactory implements IDBasedCalendarAccessFactory {

    private final CalendarProviderRegistry providerRegistry;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link CompositingIDBasedCalendarAccessFactory}.
     *
     * @param providerRegistry A reference to the calendar provider registry to use
     * @param services A service lookup reference
     */
    public CompositingIDBasedCalendarAccessFactory(CalendarProviderRegistry providerRegistry, ServiceLookup services) {
        super();
        this.providerRegistry = providerRegistry;
        this.services = services;
    }

    @Override
    public IDBasedCalendarAccess createAccess(Session session) throws OXException {
        return new CompositingIDBasedCalendarAccess(session, providerRegistry, services);
    }

}
