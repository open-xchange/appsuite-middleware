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
import static com.openexchange.chronos.impl.Utils.i;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.operation.CalendarResultImpl;
import com.openexchange.chronos.operation.CreateOperation;
import com.openexchange.chronos.operation.DeleteOperation;
import com.openexchange.chronos.operation.MoveOperation;
import com.openexchange.chronos.operation.UpdateAlarmsOperation;
import com.openexchange.chronos.operation.UpdateAttendeeOperation;
import com.openexchange.chronos.operation.UpdateOperation;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.UserizedEvent;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;

/**
 * {@link CalendarWriter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarWriter extends CalendarReader {

    /**
     * Initializes a new {@link CalendarWriter}.
     *
     * @param session The session
     */
    public CalendarWriter(CalendarSession session) throws OXException {
        this(session, Services.getService(CalendarStorageFactory.class).create(session.getContext(), session.getEntityResolver()));
    }

    /**
     * Initializes a new {@link CalendarWriter}.
     *
     * @param session The session
     * @param storage The storage
     */
    public CalendarWriter(CalendarSession session, CalendarStorage storage) {
        super(session, storage);
    }

    public CalendarResult insertEvent(UserizedEvent event) throws OXException {
        return CreateOperation.prepare(storage, session, getFolder(event.getFolderId())).perform(event.getEvent(), event.getAlarms());
    }

    public CalendarResult updateEvent(EventID eventID, UserizedEvent event, long clientTimestamp) throws OXException {
        /*
         * prepare a shared calendar result
         */
        UserizedFolder folder = getFolder(eventID.getFolderID());
        CalendarResultImpl result = new CalendarResultImpl(session, getCalendarUser(folder), i(folder));
        /*
         * perform a possible move operation beforehand
         */
        if (event.containsFolderId() && event.getFolderId() != eventID.getFolderID()) {
            UserizedFolder targetFolder = getFolder(event.getFolderId());
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

    public CalendarResult updateAttendee(EventID eventID, Attendee attendee, Long clientTimestamp) throws OXException {
        return UpdateAttendeeOperation.prepare(storage, session, getFolder(eventID.getFolderID()))
            .perform(eventID.getObjectID(), eventID.getRecurrenceID(), attendee, clientTimestamp);
    }

    public CalendarResult deleteEvent(EventID eventID, long clientTimestamp) throws OXException {
        return DeleteOperation.prepare(storage, session, getFolder(eventID.getFolderID())).perform(eventID.getObjectID(), eventID.getRecurrenceID(), clientTimestamp);
    }

}
