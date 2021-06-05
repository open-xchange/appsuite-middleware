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

package com.openexchange.chronos.impl.availability;

import com.openexchange.chronos.impl.AbstractStorageOperation;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarAvailabilityStorage;
import com.openexchange.chronos.storage.CalendarAvailabilityStorageFactory;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractCalendarAvailabilityStorageOperation}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractCalendarAvailabilityStorageOperation<T> extends AbstractStorageOperation<CalendarAvailabilityStorage, T> {

    /**
     * Initialises a new {@link AbstractCalendarAvailabilityStorageOperation}.
     * 
     * @param session The server session
     * @throws OXException
     */
    public AbstractCalendarAvailabilityStorageOperation(CalendarSession session) throws OXException {
        super(session);
    }

    @Override
    protected CalendarAvailabilityStorage initStorage(DBProvider dbProvider) throws OXException {
        return Services.getService(CalendarAvailabilityStorageFactory.class).create(context, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
    }
}
