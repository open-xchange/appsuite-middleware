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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.getFields;
import static com.openexchange.chronos.common.CalendarUtils.getRecurrenceIds;
import static com.openexchange.chronos.common.CalendarUtils.removeInvalid;
import static com.openexchange.chronos.common.SearchUtils.getSearchTerm;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import static com.openexchange.chronos.impl.Utils.getFolder;
import static com.openexchange.chronos.impl.Utils.getFolderIdTerm;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnFieldOperand;

/**
 * {@link ChangeExceptionsPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ChangeExceptionsPerformer extends AbstractQueryPerformer {

    /**
     * Initializes a new {@link ChangeExceptionsPerformer}.
     *
     * @param session The calendar session
     * @param storage The underlying calendar storage
     */
    public ChangeExceptionsPerformer(CalendarSession session, CalendarStorage storage) {
        super(session, storage);
    }

    /**
     * Performs the operation.
     *
     * @param folderId The identifier of the parent folder for the event series
     * @param seriesId The series identifier to get the change exceptions for
     * @return The change exceptions
     */
    public List<Event> perform(String folderId, String seriesId) throws OXException {
        /*
         * construct search term to lookup all change exceptions
         */
        CalendarFolder folder = getFolder(session, folderId);
        EventField[] fields = getFields(session, EventField.ORGANIZER, EventField.ATTENDEES);
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getFolderIdTerm(session, folder))
            .addSearchTerm(getSearchTerm(EventField.SERIES_ID, SingleOperation.EQUALS, seriesId))
            .addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.NOT_EQUALS, new ColumnFieldOperand<EventField>(EventField.SERIES_ID)))
        ;
        /*
         * perform search & filter the results based on user's access permissions
         */
        List<Event> changeExceptions = storage.getEventStorage().searchEvents(searchTerm, null, fields);
        changeExceptions = removeInvalidChangeExceptions(seriesId, changeExceptions);
        if (null == changeExceptions || 0 == changeExceptions.size()) {
            return Collections.emptyList();
        }
        changeExceptions = storage.getUtilities().loadAdditionalEventData(getCalendarUserId(folder), changeExceptions, fields);
        for (Iterator<Event> iterator = changeExceptions.iterator(); iterator.hasNext();) {
            Event changeException = iterator.next();
            if (false == Utils.isInFolder(changeException, folder) || false == Utils.isVisible(folder, changeException)) {
                iterator.remove();
            }
        }
        return postProcessor().process(changeExceptions, folder).getEvents();
    }

    private List<Event> removeInvalidChangeExceptions(String seriesId, List<Event> changeExceptions) {
        if (null == changeExceptions || changeExceptions.isEmpty()) {
            return changeExceptions;
        }
        try {
            EventField[] fields = new EventField[] { EventField.RECURRENCE_RULE, EventField.START_DATE, EventField.RECURRENCE_DATES };
            Event seriesMaster = storage.getEventStorage().loadEvent(seriesId, fields);
            if (null == seriesMaster) {
                return changeExceptions;
            }
            RecurrenceData recurrenceData = new DefaultRecurrenceData(seriesMaster);
            SortedSet<RecurrenceId> validIds = removeInvalid(getRecurrenceIds(changeExceptions), recurrenceData, session.getRecurrenceService());
            for (Iterator<Event> iterator = changeExceptions.iterator(); iterator.hasNext();) {
                Event changeException = iterator.next();
                if (false == validIds.contains(changeException.getRecurrenceId())) {
                    session.addWarning(CalendarExceptionCodes.INVALID_RECURRENCE_ID.create(changeException.getRecurrenceId(), recurrenceData));
                    iterator.remove();
                }
            }
        } catch (OXException e) {
            session.addWarning(CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, "Error checking change exception validity"));
        }
        return changeExceptions;
    }

}
