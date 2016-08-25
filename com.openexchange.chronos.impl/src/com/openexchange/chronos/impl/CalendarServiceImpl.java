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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.chronos.service.UserizedEvent;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceSet;

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
     * @param calendarHandlers The calendar handlers
     */
    public CalendarServiceImpl(ServiceSet<CalendarHandler> calendarHandlers) {
        super();
        this.calendarHandlers = calendarHandlers;
    }

    @Override
    public boolean[] hasEventsBetween(final CalendarSession session, final Date from, final Date until) throws OXException {
        return new ReadOperation<boolean[]>(session) {

            @Override
            protected boolean[] execute(CalendarReader reader) throws OXException {
                return reader.hasEventsBetween(session.getUser().getId(), from, until);
            }
        }.execute();
    }

    @Override
    public List<UserizedEvent> getChangeExceptions(CalendarSession session, final int folderID, final int objectID) throws OXException {
        return new ReadOperation<List<UserizedEvent>>(session) {

            @Override
            protected List<UserizedEvent> execute(CalendarReader reader) throws OXException {
                return reader.getChangeExceptions(folderID, objectID);
            }
        }.execute();
    }

    @Override
    public long getSequenceNumber(CalendarSession session, int folderID) throws OXException {
        return new CalendarReader(session).getSequenceNumber(folderID);
    }

    @Override
    public int resolveByUID(CalendarSession session, String uid) throws OXException {
        return new CalendarReader(session).resolveUid(uid);
    }

    @Override
    public List<UserizedEvent> searchEvents(CalendarSession session, final int[] folderIDs, final String pattern) throws OXException {
        return new ReadOperation<List<UserizedEvent>>(session) {

            @Override
            protected List<UserizedEvent> execute(CalendarReader reader) throws OXException {
                return reader.searchEvents(folderIDs, pattern);
            }
        }.execute();
    }

    @Override
    public UserizedEvent getEvent(CalendarSession session, final int folderID, final int objectID) throws OXException {
        return new CalendarReader(session).readEvent(folderID, objectID);
    }

    @Override
    public List<UserizedEvent> getEvents(CalendarSession session, final List<EventID> eventIDs) throws OXException {
        return new ReadOperation<List<UserizedEvent>>(session) {

            @Override
            protected List<UserizedEvent> execute(CalendarReader reader) throws OXException {
                return reader.readEvents(eventIDs);
            }
        }.execute();
    }

    @Override
    public List<UserizedEvent> getUpdatedEventsInFolder(CalendarSession session, final int folderID, final Date updatedSince) throws OXException {
        return new ReadOperation<List<UserizedEvent>>(session) {

            @Override
            protected List<UserizedEvent> execute(CalendarReader reader) throws OXException {
                return reader.readEventsInFolder(folderID, updatedSince);
            }
        }.execute();
    }

    @Override
    public List<UserizedEvent> getUpdatedEventsOfUser(final CalendarSession session, final Date updatedSince) throws OXException {
        return new ReadOperation<List<UserizedEvent>>(session) {

            @Override
            protected List<UserizedEvent> execute(CalendarReader reader) throws OXException {
                return reader.readEventsOfUser(session.getUser().getId(), updatedSince);
            }
        }.execute();
    }

    @Override
    public List<UserizedEvent> getDeletedEventsInFolder(CalendarSession session, final int folderID, final Date deletedSince) throws OXException {
        return new ReadOperation<List<UserizedEvent>>(session) {

            @Override
            protected List<UserizedEvent> execute(CalendarReader reader) throws OXException {
                return reader.readDeletedEventsInFolder(folderID, deletedSince);
            }
        }.execute();
    }

    @Override
    public List<UserizedEvent> getDeletedEventsOfUser(final CalendarSession session, final Date deletedSince) throws OXException {
        return new ReadOperation<List<UserizedEvent>>(session) {

            @Override
            protected List<UserizedEvent> execute(CalendarReader reader) throws OXException {
                return reader.readDeletedEventsOfUser(session.getUser().getId(), deletedSince);
            }
        }.execute();
    }

    @Override
    public List<UserizedEvent> getEventsInFolder(CalendarSession session, final int folderID) throws OXException {
        return new ReadOperation<List<UserizedEvent>>(session) {

            @Override
            protected List<UserizedEvent> execute(CalendarReader reader) throws OXException {
                return reader.readEventsInFolder(folderID, null);
            }
        }.execute();
    }

    @Override
    public List<UserizedEvent> getEventsOfUser(final CalendarSession session) throws OXException {
        return new ReadOperation<List<UserizedEvent>>(session) {

            @Override
            protected List<UserizedEvent> execute(CalendarReader reader) throws OXException {
                return reader.readEventsOfUser(session.getUser().getId(), null);
            }
        }.execute();
    }

    @Override
    public CreateResult createEvent(CalendarSession session, final UserizedEvent event) throws OXException {
        /*
         * insert event
         */
        CreateResultImpl result = new WriteOperation<CreateResultImpl>(session) {

            @Override
            protected CreateResultImpl execute(CalendarWriter writer) throws OXException {
                return writer.insertEvent(event);
            }
        }.execute();
        /*
         * notify handlers
         */
        for (CalendarHandler handler : calendarHandlers) {
            handler.eventCreated(result);
        }
        return result;
    }

    @Override
    public UpdateResult updateEvent(CalendarSession session, final EventID eventID, final UserizedEvent event) throws OXException {
        /*
         * update event
         */
        Long clientTimestampValue = session.get(CalendarParameters.PARAMETER_TIMESTAMP, Long.class);
        final long clientTimestamp = null != clientTimestampValue ? clientTimestampValue.longValue() : -1L;
        UpdateResultImpl result = new WriteOperation<UpdateResultImpl>(session) {

            @Override
            protected UpdateResultImpl execute(CalendarWriter writer) throws OXException {
                return writer.updateEvent(eventID, event, clientTimestamp);
            }
        }.execute();
        /*
         * notify handlers
         */
        for (CalendarHandler handler : calendarHandlers) {
            handler.eventUpdated(result);
        }
        return result;
    }

    @Override
    public UpdateResult updateAttendee(CalendarSession session, final int folderID, final int objectID, final Attendee attendee) throws OXException {
        /*
         * update event
         */
        UpdateResultImpl result = new WriteOperation<UpdateResultImpl>(session) {

            @Override
            protected UpdateResultImpl execute(CalendarWriter writer) throws OXException {
                return writer.updateAttendee(folderID, objectID, attendee);
            }
        }.execute();
        /*
         * notify handlers
         */
        for (CalendarHandler handler : calendarHandlers) {
            handler.eventUpdated(result);
        }
        return result;
    }

    @Override
    public Map<EventID, DeleteResult> deleteEvents(CalendarSession session, final List<EventID> eventIDs) throws OXException {
        Long clientTimestampValue = session.get(CalendarParameters.PARAMETER_TIMESTAMP, Long.class);
        final long clientTimestamp = null != clientTimestampValue ? clientTimestampValue.longValue() : -1L;
        Map<EventID, DeleteResult> results = new WriteOperation<Map<EventID, DeleteResult>>(session) {

            @Override
            protected Map<EventID, DeleteResult> execute(CalendarWriter writer) throws OXException {
                Map<EventID, DeleteResult> results = new HashMap<EventID, DeleteResult>(eventIDs.size());
                for (EventID eventID : eventIDs) {
                    results.put(eventID, writer.deleteEvent(eventID.getFolderID(), eventID.getObjectID(), clientTimestamp));
                }
                return results;
            }
        }.execute();
        /*
         * notify handlers
         */
        for (DeleteResult result : results.values()) {
            if (result.wasUpdate()) {
                for (CalendarHandler handler : calendarHandlers) {
                    handler.eventUpdated(result.asUpdate());
                }
            } else {
                for (CalendarHandler handler : calendarHandlers) {
                    handler.eventDeleted(result);
                }
            }
        }
        return results;
    }

}
