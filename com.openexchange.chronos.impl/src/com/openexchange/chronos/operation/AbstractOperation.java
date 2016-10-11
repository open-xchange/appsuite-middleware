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

package com.openexchange.chronos.operation;

import static com.openexchange.chronos.impl.Utils.getCalendarUser;
import static com.openexchange.chronos.impl.Utils.i;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Period;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.impl.EventMapper;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.ldap.User;

/**
 * {@link AbstractOperation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class AbstractOperation {

    protected final CalendarSession session;
    protected final CalendarStorage storage;
    protected final User calendarUser;
    protected final UserizedFolder folder;
    protected final Date timestamp;
    protected final CalendarResultImpl result;

    /**
     * Initializes a new {@link AbstractOperation}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    protected AbstractOperation(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        super();
        this.folder = folder;
        this.calendarUser = getCalendarUser(folder);
        this.session = session;
        this.timestamp = new Date();
        this.storage = storage;
        this.result = new CalendarResultImpl(session, calendarUser, i(folder)).applyTimestamp(timestamp);
    }

    /**
     * Prepares a new change exception for a recurring event series.
     *
     * @param originalMasterEvent The original master event
     * @param recurrenceID The recurrence identifier
     * @return The prepared exception event
     */
    protected Event prepareException(Event originalMasterEvent, Date recurrenceID) throws OXException {
        Event exceptionEvent = new Event();
        EventMapper.getInstance().copy(originalMasterEvent, exceptionEvent, EventField.values());
        exceptionEvent.setId(storage.nextObjectID());
        exceptionEvent.setRecurrenceId(recurrenceID);
        exceptionEvent.setChangeExceptionDates(Collections.singletonList(recurrenceID));
        exceptionEvent.setDeleteExceptionDates(null);
        exceptionEvent.setStartDate(recurrenceID);
        exceptionEvent.setEndDate(new Date(recurrenceID.getTime() + new Period(originalMasterEvent).getDuration()));
        Consistency.setCreated(timestamp, exceptionEvent, calendarUser.getId());
        Consistency.setModified(timestamp, exceptionEvent, session.getUser().getId());
        return exceptionEvent;
    }

    /**
     * <i>Touches</i> an event in the storage by setting it's last modification timestamp and modified-by property to the current
     * timestamp and calendar user.
     *
     * @param id The identifier of the event to <i>touch</i>
     */
    protected void touch(int id) throws OXException {
        Event eventUpdate = new Event();
        eventUpdate.setId(id);
        Consistency.setModified(timestamp, eventUpdate, calendarUser.getId());
        storage.getEventStorage().updateEvent(eventUpdate);
    }

    /**
     * Adds a specific recurrence identifier to the series master's change exception array and updates the series master event in the
     * storage. Also, an appropriate update result is added.
     *
     * @param originalMasterEvent The original series master event
     * @param recurrenceID The recurrence identifier of the occurrence to add
     */
    protected void addChangeExceptionDate(Event originalMasterEvent, Date recurrenceID) throws OXException {
        List<Date> changeExceptionDates = new ArrayList<Date>();
        if (null != originalMasterEvent.getChangeExceptionDates()) {
            changeExceptionDates.addAll(originalMasterEvent.getChangeExceptionDates());
        }
        if (false == changeExceptionDates.add(recurrenceID)) {
            // TODO throw/log?
        }
        Event eventUpdate = new Event();
        eventUpdate.setId(originalMasterEvent.getId());
        eventUpdate.setChangeExceptionDates(changeExceptionDates);
        Consistency.setModified(timestamp, eventUpdate, calendarUser.getId());
        storage.getEventStorage().updateEvent(eventUpdate);
        result.addUpdate(new UpdateResultImpl(originalMasterEvent, i(folder), loadEventData(originalMasterEvent.getId())));
    }

    /**
     * Loads all data for a specific event, including attendees and attachments.
     *
     * @param id The identifier of the event to load
     * @return The event data
     * @throws OXException {@link CalendarExceptionCodes#EVENT_NOT_FOUND}
     */
    protected Event loadEventData(int id) throws OXException {
        Event event = storage.getEventStorage().loadEvent(id, null);
        if (null == event) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(I(id));
        }
        event.setAttendees(storage.getAttendeeStorage().loadAttendees(event.getId()));
        event.setAttachments(storage.getAttachmentStorage().loadAttachments(event.getId()));
        return event;
    }

    protected List<Event> loadExceptionData(int seriesID, List<Date> recurrenceIDs) throws OXException {
        List<Event> exceptions = new ArrayList<Event>();
        if (null != recurrenceIDs && 0 < recurrenceIDs.size()) {
            for (Date recurrenceID : recurrenceIDs) {
                exceptions.add(loadExceptionData(seriesID, recurrenceID));
            }
        }
        return exceptions;
    }

    protected Event loadExceptionData(int seriesID, Date recurrenceID) throws OXException {
        Event excpetion = storage.getEventStorage().loadException(seriesID, recurrenceID, null);
        if (null == excpetion) {
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(I(seriesID), String.valueOf(recurrenceID));
        }
        excpetion.setAttendees(storage.getAttendeeStorage().loadAttendees(excpetion.getId()));
        excpetion.setAttachments(storage.getAttachmentStorage().loadAttachments(excpetion.getId()));
        return excpetion;
    }

    protected UserizedFolder getFolder(int folderID) throws OXException {
        return Services.getService(FolderService.class).getFolder(FolderStorage.REAL_TREE_ID, String.valueOf(folderID), session.getSession(), null);
    }

    /**
     * Gets the identifier of a specific user's default personal calendar folder.
     *
     * @param userID The identifier of the user to retrieve the default calendar identifier for
     * @return The default calendar folder identifier
     */
    protected int getDefaultCalendarID(int userID) throws OXException {
        return session.getEntityResolver().getDefaultCalendarID(userID);
    }

}
