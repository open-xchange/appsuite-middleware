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
import java.util.List;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarParameters;
import com.openexchange.chronos.CalendarService;
import com.openexchange.chronos.CalendarSession;
import com.openexchange.chronos.EventID;
import com.openexchange.chronos.UserizedEvent;
import com.openexchange.exception.OXException;

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
    public CalendarServiceImpl() {
        super();
    }

    @Override
    public boolean[] hasEventsBetween(CalendarSession session, Date from, Date until) throws OXException {
        return new CalendarReader(session).hasEventsBetween(session.getUser().getId(), from, until);
    }

    @Override
    public List<UserizedEvent> getChangeExceptions(CalendarSession session, int folderID, int objectID) throws OXException {
        return new CalendarReader(session).getChangeExceptions(folderID, objectID);
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
    public List<UserizedEvent> searchEvents(CalendarSession session, int[] folderIDs, String pattern) throws OXException {
        return new CalendarReader(session).searchEvents(folderIDs, pattern);
    }

    @Override
    public UserizedEvent getEvent(CalendarSession session, int folderID, int objectID) throws OXException {
        return new CalendarReader(session).readEvent(folderID, objectID);
    }

    @Override
    public List<UserizedEvent> getEvents(CalendarSession session, List<EventID> eventIDs) throws OXException {
        return new CalendarReader(session).readEvents(eventIDs);
    }

    @Override
    public List<UserizedEvent> getUpdatedEventsInFolder(CalendarSession session, int folderID, Date updatedSince) throws OXException {
        return new CalendarReader(session).readEventsInFolder(folderID, updatedSince);
    }

    @Override
    public List<UserizedEvent> getUpdatedEventsOfUser(CalendarSession session, Date updatedSince) throws OXException {
        return new CalendarReader(session).readEventsOfUser(session.getUser().getId(), updatedSince);
    }

    @Override
    public List<UserizedEvent> getDeletedEventsInFolder(CalendarSession session, int folderID, Date deletedSince) throws OXException {
        return new CalendarReader(session).readEventsInFolder(folderID, deletedSince);
    }

    @Override
    public List<UserizedEvent> getDeletedEventsOfUser(CalendarSession session, Date deletedSince) throws OXException {
        return new CalendarReader(session).readDeletedEventsOfUser(session.getUser().getId(), deletedSince);
    }

    @Override
    public List<UserizedEvent> getEventsInFolder(CalendarSession session, int folderID) throws OXException {
        return new CalendarReader(session).readEventsInFolder(folderID, null);
    }

    @Override
    public List<UserizedEvent> getEventsOfUser(CalendarSession session) throws OXException {
        return new CalendarReader(session).readEventsOfUser(session.getUser().getId(), null);
    }

    @Override
    public UserizedEvent createEvent(CalendarSession session, final UserizedEvent event) throws OXException {
        return new StorageOperation<UserizedEvent>(session) {

            @Override
            protected UserizedEvent execute(CalendarWriter writer) throws OXException {
                return writer.insertEvent(event);
            }
        }.execute();
    }

    @Override
    public UserizedEvent updateEvent(CalendarSession session, final int folderID, final UserizedEvent event) throws OXException {
        Long clientTimestampValue = session.get(CalendarParameters.PARAMETER_TIMESTAMP, Long.class);
        final long clientTimestamp = null != clientTimestampValue ? clientTimestampValue.longValue() : -1L;
        return new StorageOperation<UserizedEvent>(session) {

            @Override
            protected UserizedEvent execute(CalendarWriter writer) throws OXException {
                return writer.updateEvent(folderID, event, clientTimestamp);
            }
        }.execute();
    }

    @Override
    public UserizedEvent updateAttendee(CalendarSession session, final int folderID, final int objectID, final Attendee attendee) throws OXException {
        return new StorageOperation<UserizedEvent>(session) {

            @Override
            protected UserizedEvent execute(CalendarWriter writer) throws OXException {
                return writer.updateAttendee(folderID, objectID, attendee);
            }
        }.execute();
    }

    @Override
    public void deleteEvents(CalendarSession session, final List<EventID> eventIDs) throws OXException {
        Long clientTimestampValue = session.get(CalendarParameters.PARAMETER_TIMESTAMP, Long.class);
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
