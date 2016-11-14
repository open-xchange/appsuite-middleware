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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.impl;

import static com.openexchange.chronos.impl.Utils.getCalendarUser;
import static com.openexchange.chronos.impl.Utils.getFolder;
import static com.openexchange.chronos.impl.Utils.i;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.impl.session.DefaultCalendarSession;
import com.openexchange.chronos.operation.AllOperation;
import com.openexchange.chronos.operation.CalendarResultImpl;
import com.openexchange.chronos.operation.ChangeExceptionsOperation;
import com.openexchange.chronos.operation.CreateOperation;
import com.openexchange.chronos.operation.DeleteOperation;
import com.openexchange.chronos.operation.GetOperation;
import com.openexchange.chronos.operation.HasOperation;
import com.openexchange.chronos.operation.ListOperation;
import com.openexchange.chronos.operation.MoveOperation;
import com.openexchange.chronos.operation.ResolveUidOperation;
import com.openexchange.chronos.operation.SearchOperation;
import com.openexchange.chronos.operation.SequenceNumberOperation;
import com.openexchange.chronos.operation.UpdateAlarmsOperation;
import com.openexchange.chronos.operation.UpdateAttendeeOperation;
import com.openexchange.chronos.operation.UpdateOperation;
import com.openexchange.chronos.operation.UpdatesOperation;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.chronos.service.UserizedEvent;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.session.Session;

/**
 * {@link CalendarService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarServiceImpl implements CalendarService {

    private final ServiceSet<CalendarHandler> calendarHandlers;

    /**
     * Initializes a new {@link CalendarServiceImpl}.
     *
     * @param calendarHandlers The calendar handlers service set
     */
    public CalendarServiceImpl(ServiceSet<CalendarHandler> calendarHandlers) {
        super();
        this.calendarHandlers = calendarHandlers;
    }

    @Override
    public CalendarSession init(Session session) throws OXException {
        return new DefaultCalendarSession(session, this);
    }

    @Override
    public boolean[] hasEventsBetween(final CalendarSession session, final Date from, final Date until) throws OXException {
        return new StorageOperation<boolean[]>(session) {

            @Override
            protected boolean[] execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return HasOperation.prepare(session, storage).perform(session.getUser().getId(), from, until);
            }
        }.executeQuery();
    }

    @Override
    public List<UserizedEvent> getChangeExceptions(CalendarSession session, final int folderID, final int objectID) throws OXException {
        return new StorageOperation<List<UserizedEvent>>(session) {

            @Override
            protected List<UserizedEvent> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return ChangeExceptionsOperation.prepare(session, storage).perform(getFolder(session, folderID), objectID);
            }
        }.executeQuery();
    }

    @Override
    public long getSequenceNumber(CalendarSession session, final int folderID) throws OXException {
        return new StorageOperation<Long>(session) {

            @Override
            protected Long execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return L(SequenceNumberOperation.prepare(session, storage).perform(getFolder(session, folderID)));
            }
        }.executeQuery().longValue();
    }

    @Override
    public int resolveByUID(CalendarSession session, final String uid) throws OXException {
        return new StorageOperation<Integer>(session) {

            @Override
            protected Integer execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return I(ResolveUidOperation.prepare(session, storage).perform(uid));
            }
        }.executeQuery().intValue();
    }

    @Override
    public List<UserizedEvent> searchEvents(CalendarSession session, final int[] folderIDs, final String pattern) throws OXException {
        return new StorageOperation<List<UserizedEvent>>(session) {

            @Override
            protected List<UserizedEvent> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return SearchOperation.prepare(session, storage).perform(folderIDs, pattern);
            }
        }.executeQuery();
    }

    @Override
    public UserizedEvent getEvent(CalendarSession session, final int folderID, final int objectID) throws OXException {
        return new StorageOperation<UserizedEvent>(session) {

            @Override
            protected UserizedEvent execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return GetOperation.prepare(session, storage).perform(getFolder(session, folderID), objectID);
            }
        }.executeQuery();
    }

    @Override
    public List<UserizedEvent> getEvents(CalendarSession session, final List<EventID> eventIDs) throws OXException {
        return new StorageOperation<List<UserizedEvent>>(session) {

            @Override
            protected List<UserizedEvent> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return ListOperation.prepare(session, storage).perform(eventIDs);
            }
        }.executeQuery();
    }

    @Override
    public List<UserizedEvent> getEventsInFolder(CalendarSession session, final int folderID) throws OXException {
        return new StorageOperation<List<UserizedEvent>>(session) {

            @Override
            protected List<UserizedEvent> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return AllOperation.prepare(session, storage).perform(getFolder(session, folderID));
            }
        }.executeQuery();
    }

    @Override
    public List<UserizedEvent> getEventsOfUser(final CalendarSession session) throws OXException {
        return new StorageOperation<List<UserizedEvent>>(session) {

            @Override
            protected List<UserizedEvent> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return AllOperation.prepare(session, storage).perform();
            }
        }.executeQuery();
    }

    @Override
    public UpdatesResult getUpdatedEventsInFolder(CalendarSession session, final int folderID, final Date updatedSince) throws OXException {
        return new StorageOperation<UpdatesResult>(session) {

            @Override
            protected UpdatesResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return UpdatesOperation.prepare(session, storage).perform(getFolder(session, folderID), updatedSince);
            }
        }.executeQuery();
    }

    @Override
    public UpdatesResult getUpdatedEventsOfUser(CalendarSession session, final Date updatedSince) throws OXException {
        return new StorageOperation<UpdatesResult>(session) {

            @Override
            protected UpdatesResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return UpdatesOperation.prepare(session, storage).perform(updatedSince);
            }
        }.executeQuery();
    }

    @Override
    public CalendarResult createEvent(CalendarSession session, final UserizedEvent event) throws OXException {
        /*
         * insert event & notify handlers
         */
        return notifyHandlers(new StorageOperation<CalendarResult>(session) {

            @Override
            protected CalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return CreateOperation.prepare(storage, session, getFolder(session, event.getFolderId())).perform(event.getEvent(), event.getAlarms());
            }
        }.executeUpdate());
    }

    @Override
    public CalendarResult updateEvent(CalendarSession session, final EventID eventID, final UserizedEvent event) throws OXException {
        /*
         * update event & notify handlers
         */
        return notifyHandlers(new StorageOperation<CalendarResult>(session) {

            @Override
            protected CalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                /*
                 * prepare a shared calendar result for the update
                 */
                Long clientTimestampValue = session.get(CalendarParameters.PARAMETER_TIMESTAMP, Long.class);
                long clientTimestamp = null != clientTimestampValue ? clientTimestampValue.longValue() : -1L;
                UserizedFolder folder = getFolder(session, eventID.getFolderID());
                CalendarResultImpl result = new CalendarResultImpl(session, getCalendarUser(folder), i(folder));
                /*
                 * perform a possible move operation beforehand
                 */
                if (event.containsFolderId() && event.getFolderId() != eventID.getFolderID()) {
                    UserizedFolder targetFolder = getFolder(session, event.getFolderId());
                    CalendarResult moveResult = MoveOperation.prepare(storage, session, folder).perform(eventID.getObjectID(), targetFolder, clientTimestamp);
                    result.merge(moveResult);
                    folder = targetFolder;
                    if (null != moveResult.getTimestamp()) {
                        clientTimestamp = moveResult.getTimestamp().getTime();
                    }
                }
                /*
                 * perform the event update
                 */
                if (null != event.getEvent()) {
                    CalendarResult updateResult = UpdateOperation.prepare(storage, session, folder).perform(eventID.getObjectID(), event.getEvent(), clientTimestamp);
                    result.merge(updateResult);
                    if (null != updateResult.getTimestamp()) {
                        clientTimestamp = updateResult.getTimestamp().getTime();
                    }
                }
                /*
                 * update the alarms
                 */
                if (event.containsAlarms()) {
                    CalendarResult alarmResult = UpdateAlarmsOperation.prepare(storage, session, folder).perform(eventID.getObjectID(), event.getAlarms(), clientTimestamp);
                    result.merge(alarmResult);
                    if (null != alarmResult.getTimestamp()) {
                        clientTimestamp = alarmResult.getTimestamp().getTime();
                    }
                }
                return result;
            }

        }.executeUpdate());
    }

    @Override
    public CalendarResult updateAttendee(CalendarSession session, final EventID eventID, final Attendee attendee) throws OXException {
        /*
         * update attendee & notify handlers
         */
        return notifyHandlers(new StorageOperation<CalendarResult>(session) {

            @Override
            protected CalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                Long clientTimestamp = session.get(CalendarParameters.PARAMETER_TIMESTAMP, Long.class);
                return UpdateAttendeeOperation.prepare(storage, session, getFolder(session, eventID.getFolderID())).perform(eventID.getObjectID(), eventID.getRecurrenceID(), attendee, clientTimestamp);

            }
        }.executeUpdate());
    }

    @Override
    public Map<EventID, CalendarResult> deleteEvents(CalendarSession session, final List<EventID> eventIDs) throws OXException {
        /*
         * delete events
         */
        Map<EventID, CalendarResult> results = new StorageOperation<Map<EventID, CalendarResult>>(session) {

            @Override
            protected Map<EventID, CalendarResult> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                Long clientTimestampValue = session.get(CalendarParameters.PARAMETER_TIMESTAMP, Long.class);
                long clientTimestamp = null != clientTimestampValue ? clientTimestampValue.longValue() : -1L;
                Map<EventID, CalendarResult> results = new HashMap<EventID, CalendarResult>(eventIDs.size());
                for (EventID eventID : eventIDs) {
                    results.put(eventID, DeleteOperation.prepare(storage, session, getFolder(session, eventID.getFolderID())).perform(eventID.getObjectID(), eventID.getRecurrenceID(), clientTimestamp));
                }
                return results;
            }
        }.executeUpdate();
        /*
         * notify handlers
         */
        for (CalendarResult result : results.values()) {
            notifyHandlers(result);
        }
        return results;
    }

    private CalendarResult notifyHandlers(CalendarResult result) {
        for (CalendarHandler handler : calendarHandlers) {
            handler.handle(result);
        }
        return result;
    }
}
