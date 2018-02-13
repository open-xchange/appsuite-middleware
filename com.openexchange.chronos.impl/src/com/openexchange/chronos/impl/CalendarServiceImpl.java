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

import static com.openexchange.chronos.impl.Utils.getFolder;
import static com.openexchange.chronos.impl.Utils.getFolders;
import static com.openexchange.java.Autoboxing.L;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.UnmodifiableEvent;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.impl.performer.AllPerformer;
import com.openexchange.chronos.impl.performer.ChangeExceptionsPerformer;
import com.openexchange.chronos.impl.performer.ClearPerformer;
import com.openexchange.chronos.impl.performer.CreatePerformer;
import com.openexchange.chronos.impl.performer.DeletePerformer;
import com.openexchange.chronos.impl.performer.GetAttachmentPerformer;
import com.openexchange.chronos.impl.performer.GetPerformer;
import com.openexchange.chronos.impl.performer.ImportPerformer;
import com.openexchange.chronos.impl.performer.ListPerformer;
import com.openexchange.chronos.impl.performer.MovePerformer;
import com.openexchange.chronos.impl.performer.SearchPerformer;
import com.openexchange.chronos.impl.performer.SequenceNumberPerformer;
import com.openexchange.chronos.impl.performer.SplitPerformer;
import com.openexchange.chronos.impl.performer.TouchPerformer;
import com.openexchange.chronos.impl.performer.UpdateAlarmsPerformer;
import com.openexchange.chronos.impl.performer.UpdateAttendeePerformer;
import com.openexchange.chronos.impl.performer.UpdatePerformer;
import com.openexchange.chronos.impl.performer.UpdatesPerformer;
import com.openexchange.chronos.impl.session.DefaultCalendarSession;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarServiceUtilities;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.ImportResult;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.session.Session;

/**
 * {@link CalendarServiceImpl}
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
        return init(session, null);
    }

    @Override
    public CalendarSession init(Session session, CalendarParameters parameters) throws OXException {
        DefaultCalendarSession calendarSession = new DefaultCalendarSession(session, this);
        if (null != parameters) {
            for (Entry<String, Object> entry : parameters.entrySet()) {
                calendarSession.set(entry.getKey(), entry.getValue());
            }
        }
        return calendarSession;
    }

    @Override
    public CalendarServiceUtilities getUtilities() {
        return CalendarServiceUtilitiesImpl.getInstance();
    }

    @Override
    public List<Event> getChangeExceptions(CalendarSession session, final String folderID, final String objectID) throws OXException {
        return new InternalCalendarStorageOperation<List<Event>>(session) {

            @Override
            protected List<Event> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new ChangeExceptionsPerformer(session, storage).perform(getFolder(session, folderID), objectID);
            }
        }.executeQuery();
    }

    @Override
    public long getSequenceNumber(CalendarSession session, final String folderID) throws OXException {
        return new InternalCalendarStorageOperation<Long>(session) {

            @Override
            protected Long execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return L(new SequenceNumberPerformer(session, storage).perform(getFolder(session, folderID)));
            }
        }.executeQuery().longValue();
    }

    @Override
    public List<Event> searchEvents(CalendarSession session, final String[] folderIDs, final String pattern) throws OXException {
        return new InternalCalendarStorageOperation<List<Event>>(session) {

            @Override
            protected List<Event> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new SearchPerformer(session, storage).perform(folderIDs, pattern);
            }
        }.executeQuery();
    }

    @Override
    public List<Event> searchEvents(CalendarSession session, final String[] folderIDs, final List<SearchFilter> filters, final List<String> queries) throws OXException {
        return new InternalCalendarStorageOperation<List<Event>>(session) {

            @Override
            protected List<Event> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new SearchPerformer(session, storage).perform(folderIDs, filters, queries);
            }
        }.executeQuery();
    }

    @Override
    public Event getEvent(CalendarSession session, final String folderID, final EventID eventId) throws OXException {
        return new InternalCalendarStorageOperation<Event>(session) {

            @Override
            protected Event execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new GetPerformer(session, storage).perform(getFolder(session, folderID), eventId.getObjectID(), eventId.getRecurrenceID());
            }
        }.executeQuery();
    }

    @Override
    public List<Event> getEvents(CalendarSession session, final List<EventID> eventIDs) throws OXException {
        return new InternalCalendarStorageOperation<List<Event>>(session) {

            @Override
            protected List<Event> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new ListPerformer(session, storage).perform(eventIDs);
            }
        }.executeQuery();
    }

    @Override
    public List<Event> getEventsInFolder(CalendarSession session, final String folderID) throws OXException {
        return new InternalCalendarStorageOperation<List<Event>>(session) {

            @Override
            protected List<Event> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new AllPerformer(session, storage).perform(getFolder(session, folderID));
            }
        }.executeQuery();
    }

    @Override
    public List<Event> getEventsInFolders(CalendarSession session, List<String> folderIds) throws OXException {
        return new InternalCalendarStorageOperation<List<Event>>(session) {

            @Override
            protected List<Event> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new AllPerformer(session, storage).perform(getFolders(session, folderIds));
            }
        }.executeQuery();
    }

    @Override
    public List<Event> getEventsOfUser(final CalendarSession session) throws OXException {
        return new InternalCalendarStorageOperation<List<Event>>(session) {

            @Override
            protected List<Event> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new AllPerformer(session, storage).perform();
            }
        }.executeQuery();
    }

    @Override
    public List<Event> getEventsOfUser(final CalendarSession session, Boolean rsvp, ParticipationStatus[] partStats) throws OXException {
        return new InternalCalendarStorageOperation<List<Event>>(session) {

            @Override
            protected List<Event> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new AllPerformer(session, storage).perform(rsvp, partStats);
            }
        }.executeQuery();
    }

    @Override
    public UpdatesResult getUpdatedEventsInFolder(CalendarSession session, final String folderID, final long updatedSince) throws OXException {
        return new InternalCalendarStorageOperation<UpdatesResult>(session) {

            @Override
            protected UpdatesResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new UpdatesPerformer(session, storage).perform(getFolder(session, folderID), updatedSince);
            }
        }.executeQuery();
    }

    @Override
    public UpdatesResult getUpdatedEventsOfUser(CalendarSession session, final long updatedSince) throws OXException {
        return new InternalCalendarStorageOperation<UpdatesResult>(session) {

            @Override
            protected UpdatesResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new UpdatesPerformer(session, storage).perform(updatedSince);
            }
        }.executeQuery();
    }

    @Override
    public CalendarResult createEvent(CalendarSession session, final String folderId, final Event event) throws OXException {
        /*
         * insert event & notify handlers
         */
        InternalCalendarResult result = new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new CreatePerformer(storage, session, getFolder(session, folderId)).perform(new UnmodifiableEvent(event));
            }
        }.executeUpdate();
        /*
         * notify handlers & return userized result
         */
        notifyHandlers(result, session);
        return result.getUserizedResult();
    }

    @Override
    public CalendarResult updateEvent(CalendarSession session, final EventID eventID, final Event event, final long clientTimestamp) throws OXException {
        /*
         * update event & notify handlers
         */
        InternalCalendarResult result = new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new UpdatePerformer(storage, session, getFolder(session, eventID.getFolderID())).perform(eventID.getObjectID(), eventID.getRecurrenceID(), new UnmodifiableEvent(event), clientTimestamp);
            }

        }.executeUpdate();
        /*
         * notify handlers & return userized result
         */
        notifyHandlers(result, session);
        return result.getUserizedResult();
    }

    @Override
    public CalendarResult updateEventAsOrganizer(CalendarSession session, EventID eventID, Event event, long clientTimestamp) throws OXException {
        /*
         * update event & notify handlers
         */
        InternalCalendarResult result = new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new UpdatePerformer(storage, session, getFolder(session, eventID.getFolderID()), EnumSet.of(Role.ORGANIZER)).perform(eventID.getObjectID(), eventID.getRecurrenceID(), new UnmodifiableEvent(event), clientTimestamp);
            }

        }.executeUpdate();
        /*
         * notify handlers & return userized result
         */
        notifyHandlers(result, session);
        return result.getUserizedResult();
    }

    @Override
    public CalendarResult touchEvent(CalendarSession session, final EventID eventID) throws OXException {
        /*
         * touch event & notify handlers
         */
        InternalCalendarResult result = new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new TouchPerformer(storage, session, getFolder(session, eventID.getFolderID())).perform(eventID.getObjectID());
            }

        }.executeUpdate();
        /*
         * notify handlers & return userized result
         */
        notifyHandlers(result, session);
        return result.getUserizedResult();
    }

    @Override
    public CalendarResult moveEvent(CalendarSession session, final EventID eventID, final String folderId, final long clientTimestamp) throws OXException {
        /*
         * move event
         */
        InternalCalendarResult result = new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new MovePerformer(storage, session, getFolder(session, eventID.getFolderID())).perform(eventID.getObjectID(), getFolder(session, folderId), clientTimestamp);
            }
        }.executeUpdate();
        /*
         * notify handlers & return userized result
         */
        notifyHandlers(result, session);
        return result.getUserizedResult();
    }

    @Override
    public CalendarResult updateAttendee(CalendarSession session, final EventID eventID, final Attendee attendee, final long clientTimestamp) throws OXException {
        /*
         * update attendee
         */
        InternalCalendarResult result = new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new UpdateAttendeePerformer(storage, session, getFolder(session, eventID.getFolderID())).perform(eventID.getObjectID(), eventID.getRecurrenceID(), attendee, Long.valueOf(clientTimestamp));

            }
        }.executeUpdate();
        /*
         * notify handlers & return userized result
         */
        notifyHandlers(result, session);
        return result.getUserizedResult();
    }

    @Override
    public CalendarResult updateAlarms(CalendarSession session, final EventID eventID, final List<Alarm> alarms, final long clientTimestamp) throws OXException {
        /*
         * update attendee
         */
        InternalCalendarResult result = new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new UpdateAlarmsPerformer(storage, session, getFolder(session, eventID.getFolderID())).perform(eventID.getObjectID(), eventID.getRecurrenceID(), alarms, Long.valueOf(clientTimestamp));

            }
        }.executeUpdate();
        /*
         * notify handlers & return userized result
         */
        notifyHandlers(result, session);
        return result.getUserizedResult();
    }

    @Override
    public CalendarResult deleteEvent(CalendarSession session, final EventID eventID, final long clientTimestamp) throws OXException {
        /*
         * delete event
         */
        InternalCalendarResult result = new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new DeletePerformer(storage, session, getFolder(session, eventID.getFolderID())).perform(eventID.getObjectID(), eventID.getRecurrenceID(), clientTimestamp);

            }
        }.executeUpdate();
        /*
         * notify handlers & return userized result
         */
        notifyHandlers(result, session);
        return result.getUserizedResult();
    }

    @Override
    public CalendarResult splitSeries(CalendarSession session, EventID eventID, DateTime splitPoint, String uid, long clientTimestamp) throws OXException {
        /*
         * split event series, notify handlers & return userized result
         */
        return notifyHandlers(new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new SplitPerformer(storage, session, getFolder(session, eventID.getFolderID())).perform(eventID.getObjectID(), splitPoint, uid, clientTimestamp);

            }
        }.executeUpdate(), session).getUserizedResult();
    }

    @Override
    public CalendarResult clearEvents(CalendarSession session, final String folderId, final long clientTimestamp) throws OXException {
        /*
         * delete event
         */
        InternalCalendarResult result = new InternalCalendarStorageOperation<InternalCalendarResult>(session) {

            @Override
            protected InternalCalendarResult execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new ClearPerformer(storage, session, getFolder(session, folderId)).perform(clientTimestamp);

            }
        }.executeUpdate();
        /*
         * notify handlers & return userized result
         */
        notifyHandlers(result, session);
        return result.getUserizedResult();
    }

    @Override
    public List<ImportResult> importEvents(CalendarSession session, String folderID, List<Event> events) throws OXException {
        Boolean oldSuppressItip = session.get(CalendarParameters.PARAMETER_SUPPRESS_ITIP, Boolean.class);
        try {
            if (null == oldSuppressItip) {
                session.set(CalendarParameters.PARAMETER_SUPPRESS_ITIP, Boolean.TRUE);
            }
            /*
             * import events & notify handlers
             */
            List<InternalImportResult> results = new InternalCalendarStorageOperation<List<InternalImportResult>>(session) {

                @Override
                protected List<InternalImportResult> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                    return new ImportPerformer(storage, session, getFolder(session, folderID)).perform(events);
                }

            }.executeUpdate();
            /*
             * notify handlers & return userized result
             */
            List<ImportResult> importResults = new ArrayList<ImportResult>(results.size());
            for (InternalImportResult result : results) {
                importResults.add(result.getImportResult());
                notifyHandlers(result.getCalendarEvent(session));
            }
            return importResults;
        } finally {
            session.set(CalendarParameters.PARAMETER_SUPPRESS_ITIP, oldSuppressItip);
        }
    }

    @Override
    public List<AlarmTrigger> getAlarmTriggers(CalendarSession session, Set<String> actions) throws OXException {
        List<AlarmTrigger> triggers = new InternalCalendarStorageOperation<List<AlarmTrigger>>(session) {

            @Override
            protected List<AlarmTrigger> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return storage.getAlarmTriggerStorage().loadTriggers(session.getUserId(), session.get(CalendarParameters.PARAMETER_RANGE_END, Date.class));
            }
        }.executeQuery();
        return AlarmUtils.filter(triggers, actions.toArray(new String[actions.size()]));
    }

    @Override
    public IFileHolder getAttachment(CalendarSession session, EventID eventID, int managedId) throws OXException {
        return new InternalCalendarStorageOperation<IFileHolder>(session) {

            @Override
            protected IFileHolder execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new GetAttachmentPerformer(session, storage).performGetAttachment(eventID.getObjectID(), getFolder(session, eventID.getFolderID()), managedId);
            }
        }.executeQuery();
    }

    private InternalCalendarResult notifyHandlers(InternalCalendarResult result, CalendarSession session) {
        notifyHandlers(result.getCalendarEvent(session));
        return result;
    }

    private void notifyHandlers(CalendarEvent event) {
        for (CalendarHandler handler : calendarHandlers) {
            try {
                handler.handle(event);
            } catch (Exception e) {
                getLogger(getClass()).warn("Unexpected error while handling {}: {}", handler, event, e.getMessage(), e);
            }
        }
    }

}
