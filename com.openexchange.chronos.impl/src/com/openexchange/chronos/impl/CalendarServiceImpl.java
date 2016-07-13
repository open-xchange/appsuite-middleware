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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.chronos.CalendarParameters;
import com.openexchange.chronos.CalendarService;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventID;
import com.openexchange.chronos.UserizedEvent;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CalendarService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarServiceImpl implements CalendarService {

    /**
     * Initializes a new {@link CalendarServiceImpl}.
     */
    public CalendarServiceImpl() throws OXException {
        super();
    }

    @Override
    public UserizedEvent getEvent(ServerSession session, int folderID, int objectID, CalendarParameters parameters) throws OXException {
        EventField[] fields = parameters.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        return new CalendarReader(session).readEvent(folderID, objectID, fields);
    }

    @Override
    public List<UserizedEvent> getEvents(ServerSession session, List<EventID> eventIDs, CalendarParameters parameters) throws OXException {
        EventField[] fields = parameters.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        List<UserizedEvent> events = new ArrayList<UserizedEvent>(eventIDs.size());
        CalendarReader reader = new CalendarReader(session);
        for (EventID eventID : eventIDs) {
            events.add(reader.readEvent(eventID, fields));
        }
        return events;
    }

    @Override
    public List<UserizedEvent> getUpdatedEventsInFolder(ServerSession session, int folderID, Date updatedSince, CalendarParameters parameters) throws OXException {
        Date from = parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
        Date until = parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
        EventField[] fields = parameters.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        return new CalendarReader(session).readEventsInFolder(folderID, from, until, updatedSince, fields);
    }

    @Override
    public List<UserizedEvent> getUpdatedEventsOfUser(ServerSession session, Date updatedSince, CalendarParameters parameters) throws OXException {
        Date from = parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
        Date until = parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
        EventField[] fields = parameters.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        return new CalendarReader(session).readEventsOfUser(session.getUserId(), from, until, updatedSince, fields);
    }

    @Override
    public List<UserizedEvent> getDeletedEventsInFolder(ServerSession session, int folderID, Date deletedSince, CalendarParameters parameters) throws OXException {
        Date from = parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
        Date until = parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
        EventField[] fields = parameters.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        return new CalendarReader(session).readEventsInFolder(folderID, from, until, deletedSince, fields);
    }

    @Override
    public List<UserizedEvent> getDeletedEventsOfUser(ServerSession session, Date deletedSince, CalendarParameters parameters) throws OXException {
        Date from = parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
        Date until = parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
        return new CalendarReader(session).readDeletedEventsOfUser(session.getUserId(), from, until, deletedSince);
    }

    @Override
    public List<UserizedEvent> getEventsInFolder(ServerSession session, int folderID, CalendarParameters parameters) throws OXException {
        Date from = parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
        Date until = parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
        EventField[] fields = parameters.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        return new CalendarReader(session).readEventsInFolder(folderID, from, until, null, fields);
    }

    @Override
    public List<UserizedEvent> getEventsOfUser(ServerSession session, CalendarParameters parameters) throws OXException {
        Date from = parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
        Date until = parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
        EventField[] fields = parameters.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        return new CalendarReader(session).readEventsOfUser(session.getUserId(), from, until, null, fields);
    }

    @Override
    public UserizedEvent createEvent(ServerSession session, final UserizedEvent event, CalendarParameters parameters) throws OXException {
        return new StorageOperation<UserizedEvent>(session) {

            @Override
            protected UserizedEvent execute(CalendarWriter writer) throws OXException {
                return writer.insertEvent(event);
            }
        }.execute();
    }

    @Override
    public UserizedEvent updateEvent(ServerSession session, final int folderID, final UserizedEvent event, CalendarParameters parameters) throws OXException {
        Long clientTimestampValue = parameters.get(CalendarParameters.PARAMETER_TIMESTAMP, Long.class);
        final long clientTimestamp = null != clientTimestampValue ? clientTimestampValue.longValue() : -1L;
        return new StorageOperation<UserizedEvent>(session) {

            @Override
            protected UserizedEvent execute(CalendarWriter writer) throws OXException {
                return writer.updateEvent(folderID, event, clientTimestamp);
            }
        }.execute();
    }

    @Override
    public void deleteEvents(ServerSession session, final List<EventID> eventIDs, CalendarParameters parameters) throws OXException {
        Long clientTimestampValue = parameters.get(CalendarParameters.PARAMETER_TIMESTAMP, Long.class);
        final long clientTimestamp = null != clientTimestampValue ? clientTimestampValue.longValue() : -1L;
        new StorageOperation<Void>(session) {

            @Override
            protected Void execute(CalendarWriter writer) throws OXException {
                for (EventID eventID : eventIDs) {
                    writer.deleteEvent(eventID.getFolderID(), eventID.getObjectID(), clientTimestamp);
                }
                return null;
            }
        }.execute();
    }

}
