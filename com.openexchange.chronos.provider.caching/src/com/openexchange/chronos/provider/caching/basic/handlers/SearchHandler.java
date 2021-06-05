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

package com.openexchange.chronos.provider.caching.basic.handlers;

import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.Check;
import com.openexchange.chronos.common.SearchUtils;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.operation.OSGiCalendarStorageOperation;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link SearchHandler}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SearchHandler extends AbstractExtensionHandler {

    /**
     * Initialises a new {@link SearchHandler}.
     *
     * @param session The groupware {@link Session}
     * @param account The {@link CalendarAccount}
     * @param calendarParameters The {@link CalendarParameters}
     */
    public SearchHandler(Session session, CalendarAccount account, CalendarParameters parameters) {
        super(session, account, parameters);
    }

    /**
     * Searches for events. The optional event fields will be loaded from the {@link CalendarParameters#PARAMETER_FIELDS}
     *
     * @param filters A {@link List} with the {@link SearchFilter}s
     * @param queries A {@link List} with the queries
     * @return A {@link List} with all matching {@link Event}s
     * @throws OXException if an error is occurred
     */
    public List<Event> searchEvents(List<SearchFilter> filters, List<String> queries) throws OXException {
        Check.minimumSearchPatternLength(queries, ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS));
        return new OSGiCalendarStorageOperation<List<Event>>(Services.getServiceLookup(), getSession().getContextId(), getAccount().getAccountId()) {

            @Override
            protected List<Event> call(CalendarStorage storage) throws OXException {
                List<Event> events = storage.getEventStorage().searchEvents(SearchUtils.buildSearchTerm(queries), filters, getSearchOptions(), getEventFields());
                return postProcess(storage.getUtilities().loadAdditionalEventData(getSession().getUserId(), events, getEventFields()));
            }
        }.executeQuery();
    }
}
