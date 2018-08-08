/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.contains;
import static com.openexchange.chronos.common.CalendarUtils.getFields;
import static com.openexchange.chronos.common.CalendarUtils.getOccurrence;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import static com.openexchange.chronos.impl.Utils.getFolder;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link GetPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class GetPerformer extends AbstractQueryPerformer {

    /**
     * Initializes a new {@link GetPerformer}.
     *
     * @param session The calendar session
     * @param storage The underlying calendar storage
     */
    public GetPerformer(CalendarSession session, CalendarStorage storage) {
        super(session, storage);
    }

    /**
     * Performs the operation.
     *
     * @param folderId The identifier of the parent folder to read the event in
     * @param eventId The identifier of the event to get
     * @param recurrenceId The recurrence identifier of the occurrence to get, or <code>null</code> if no specific occurrence is targeted
     * @return The loaded event
     */
    public Event perform(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException {
        /*
         * load event data
         */
        CalendarFolder folder = getFolder(session, folderId, false);
        EventField[] fields = getFields(session, EventField.ORGANIZER, EventField.ATTENDEES);
        Event event = storage.getEventStorage().loadEvent(eventId, fields);
        if (null == event) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(eventId);
        }
        if (null != recurrenceId && isSeriesMaster(event) && contains(event.getChangeExceptionDates(), recurrenceId)) {
            Event exceptionEvent = storage.getEventStorage().loadException(eventId, recurrenceId, new EventField[] { EventField.ID });
            if (null == exceptionEvent) {
                throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(eventId, recurrenceId);
            }
            return perform(folderId, exceptionEvent.getId(), recurrenceId);
        }
        event = storage.getUtilities().loadAdditionalEventData(getCalendarUserId(folder), event, fields);
        /*
         * check permissions & userize event
         */
        Check.eventIsVisible(folder, event);
        Check.eventIsInFolder(event, folder);
        event = new EventPostProcessor(session, storage).process(event, folder).getFirstEvent();
        if (null == event) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(eventId);
        }
        /*
         * retrieve targeted event occurrence if specified
         */
        if (null != recurrenceId) {
            if (isSeriesMaster(event)) {
                event = getOccurrence(session.getRecurrenceService(), event, recurrenceId);
            }
            if (null == event || false == recurrenceId.equals(event.getRecurrenceId())) {
                throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(eventId, recurrenceId);
            }
        }
        return event;
    }

}
