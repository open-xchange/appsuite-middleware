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

package com.openexchange.chronos.provider.caching.internal.response;

import static com.openexchange.chronos.common.CalendarUtils.getFlags;
import static com.openexchange.chronos.common.CalendarUtils.getSearchTerm;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarAccess;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.operation.OSGiCalendarStorageOperation;
import com.openexchange.exception.OXException;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnFieldOperand;

/**
 * {@link ChangeExceptionsResponseGenerator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ChangeExceptionsResponseGenerator extends ResponseGenerator {

    final String seriesId;

    public ChangeExceptionsResponseGenerator(BasicCachingCalendarAccess cachedCalendarAccess, String seriesId) {
        super(cachedCalendarAccess);
        this.seriesId = seriesId;
    }

    public List<Event> generate() throws OXException {
        return new OSGiCalendarStorageOperation<List<Event>>(Services.getServiceLookup(), this.cachedCalendarAccess.getSession().getContextId(), this.cachedCalendarAccess.getAccount().getAccountId()) {

            @Override
            protected List<Event> call(CalendarStorage storage) throws OXException {
                EventField[] fields = getFields(cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_FIELDS, EventField[].class), EventField.FOLDER_ID);
                Event event = storage.getEventStorage().loadEvent(seriesId, fields);
                if (null == event) {
                    throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(seriesId);
                }
                /*
                 * construct search term to lookup all change exceptions
                 */
                CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND).addSearchTerm(getSearchTerm(EventField.SERIES_ID, SingleOperation.EQUALS, seriesId)).addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.NOT_EQUALS, new ColumnFieldOperand<EventField>(EventField.SERIES_ID)));
                /*
                 * perform search & filter the results based on user's access permissions
                 */
                List<Event> changeExceptions = storage.getEventStorage().searchEvents(searchTerm, null, fields);
                if (null == changeExceptions || 0 == changeExceptions.size()) {
                    return Collections.emptyList();
                }
                changeExceptions = storage.getUtilities().loadAdditionalEventData(cachedCalendarAccess.getAccount().getUserId(), changeExceptions, fields);
                for (Event changeException : changeExceptions) {
                    changeException.setFlags(getFlags(event, cachedCalendarAccess.getAccount().getUserId()));
                }
                return changeExceptions;
            }
        }.executeQuery();
    }

}
