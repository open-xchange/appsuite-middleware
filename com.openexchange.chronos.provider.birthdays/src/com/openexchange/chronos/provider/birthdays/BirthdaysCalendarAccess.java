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

package com.openexchange.chronos.provider.birthdays;

import static com.openexchange.chronos.provider.birthdays.Services.getService;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultUpdatesResult;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.DefaultCalendarFolder;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link BirthdaysCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class BirthdaysCalendarAccess implements CalendarAccess {

    private final Session session;
    private final CalendarFolder folder;
    private final CalendarAccount account;
    private final CalendarParameters parameters;

    /**
     * Initializes a new {@link BirthdaysCalendarAccess}.
     *
     * @param session The session
     * @param folder The calendar folder
     * @param parameters Additional calendar parameters
     */
    public BirthdaysCalendarAccess(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        super();
        this.session = session;
        this.account = account;
        this.folder = new DefaultCalendarFolder("0", "Birthdays");
        this.parameters = parameters;
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    public List<CalendarFolder> getVisibleFolders() throws OXException {
        return Collections.singletonList(folder);
    }

    @Override
    public CalendarFolder getFolder(String folderId) throws OXException {
        if (false == folder.getId().equals(folderId)) {
            throw OXException.notFound(folderId);
        }
        return folder;
    }

    @Override
    public Event getEvent(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException {
        if (false == folder.getId().equals(folderId)) {
            throw OXException.notFound(folderId);
        }
        int[] decodedId = decodeId(eventId);
        Contact contact = getService(ContactService.class).getContact(session, String.valueOf(decodedId[0]), String.valueOf(decodedId[1]));
        Event birthdaySeries = getBirthdaySeries(contact);
        if (null == recurrenceId) {
            return birthdaySeries;
        }
        RecurrenceIterator<Event> iterator = getService(RecurrenceService.class).iterateEventOccurrences(birthdaySeries, getFrom(), getUntil());
        if (false == iterator.hasNext()) {
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(eventId, recurrenceId);
        }
        return addAge(iterator.next(), contact, iterator.getPosition() - 1);
    }

    @Override
    public List<Event> getEvents(List<EventID> eventIDs) throws OXException {
        List<Event> events = new ArrayList<Event>();
        for (EventID eventID : eventIDs) {
            events.add(getEvent(eventID.getFolderID(), eventID.getObjectID(), eventID.getRecurrenceID()));
        }
        return events;
    }

    @Override
    public List<Event> getEventsInFolder(String folderId) throws OXException {
        if (false == folder.getId().equals(folderId)) {
            throw OXException.notFound(folderId);
        }
        List<Event> events = new ArrayList<Event>();
        CompositeSearchTerm notTerm = new CompositeSearchTerm(CompositeOperation.NOT);
        SingleSearchTerm isNullTerm = new SingleSearchTerm(SingleOperation.ISNULL);
        isNullTerm.addOperand(new ContactFieldOperand(ContactField.BIRTHDAY));
        notTerm.addSearchTerm(isNullTerm);
        SearchIterator<Contact> searchIterator = null;
        try {
            searchIterator = getService(ContactService.class).searchContacts(session, notTerm);
            while (searchIterator.hasNext()) {
                Contact contact = searchIterator.next();
                Event birthdaySeries = getBirthdaySeries(contact);
                RecurrenceIterator<Event> iterator = getService(RecurrenceService.class).iterateEventOccurrences(birthdaySeries, getFrom(), getUntil());
                while (iterator.hasNext()) {
                    events.add(addAge(iterator.next(), contact, iterator.getPosition() - 1));
                }
            }
        } finally {
            SearchIterators.close(searchIterator);
        }
        return events;
    }

    @Override
    public List<Event> getChangeExceptions(String folderId, String seriesId) throws OXException {
        return Collections.emptyList();
    }

    @Override
    public UpdatesResult getUpdatedEventsInFolder(String folderId, Date updatedSince) throws OXException {
        if (false == folder.getId().equals(folderId)) {
            throw OXException.notFound(folderId);
        }
        CompositeSearchTerm notTerm = new CompositeSearchTerm(CompositeOperation.NOT);
        SingleSearchTerm isNullTerm = new SingleSearchTerm(SingleOperation.ISNULL);
        isNullTerm.addOperand(new ContactFieldOperand(ContactField.BIRTHDAY));
        notTerm.addSearchTerm(isNullTerm);
        SingleSearchTerm lastModifiedTerm = new SingleSearchTerm(SingleOperation.GREATER_THAN);
        lastModifiedTerm.addOperand(new ContactFieldOperand(ContactField.LAST_MODIFIED));
        lastModifiedTerm.addOperand(new ConstantOperand<Date>(updatedSince));
        CompositeSearchTerm andTerm = new CompositeSearchTerm(CompositeOperation.AND);
        andTerm.addSearchTerm(notTerm);
        andTerm.addSearchTerm(lastModifiedTerm);
        final List<Event> events = new ArrayList<Event>();
        SearchIterator<Contact> searchIterator = null;
        try {
            searchIterator = getService(ContactService.class).searchContacts(session, andTerm);
            while (searchIterator.hasNext()) {
                Contact contact = searchIterator.next();
                Event birthdaySeries = getBirthdaySeries(contact);
                RecurrenceIterator<Event> iterator = getService(RecurrenceService.class).iterateEventOccurrences(birthdaySeries, getFrom(), getUntil());
                while (iterator.hasNext()) {
                    events.add(addAge(iterator.next(), contact, iterator.getPosition() - 1));
                }
            }
        } finally {
            SearchIterators.close(searchIterator);
        }
        return new DefaultUpdatesResult(events, null);
    }

    private Date getFrom() {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
    }

    private Date getUntil() {
        return parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
    }

    private Event getBirthdaySeries(Contact contact) throws OXException {
        Event event = new Event();
        event.setFolderId(folder.getId());
        event.setId(encodeId(contact));
        event.setSeriesId(event.getId());
        event.setSummary(getSummary(contact, 0));
        //        event.setCreatedBy(contact.getCreatedBy());
        //        event.setCreated(contact.getCreationDate());
        //        event.setModifiedBy(contact.getModifiedBy());
        //        event.setLastModified(contact.getLastModified());
        event.setRecurrenceRule("FREQ=YEARLY");
        event.setTransp(TimeTransparency.TRANSPARENT);
        event.setUid(contact.getUid());
        Attendee attendee = new Attendee();
        attendee.setCuType(CalendarUserType.INDIVIDUAL);
        attendee.setCn(contact.getDisplayName());
        attendee.setUri(CalendarUtils.getURI(contact.getEmail1()));
        if (0 < contact.getInternalUserId()) {
            attendee.setEntity(contact.getInternalUserId());
        }
        attendee.setPartStat(ParticipationStatus.ACCEPTED);
        event.setAttendees(Collections.singletonList(attendee));

        Calendar calendar = CalendarUtils.initCalendar(TimeZones.UTC, contact.getBirthday());
        CalendarUtils.truncateTime(calendar);
        event.setStartDate(new DateTime(calendar.getTimeInMillis()).toAllDay());
        calendar.add(Calendar.DATE, 1);
        event.setEndDate(new DateTime(calendar.getTimeInMillis()).toAllDay());
        return event;
    }

    private Event addAge(Event birthdayOccurrence, final Contact contact, final int age) throws OXException {
        if (0 < age) {
            final String summary = getSummary(contact, age);
            return new DelegatingEvent(birthdayOccurrence) {

                @Override
                public String getSummary() {
                    return summary;
                }
            };
        }
        return birthdayOccurrence;
    }

    private String getSummary(Contact contact, int age) throws OXException {
        Locale locale = ServerSessionAdapter.valueOf(session).getUser().getLocale();
        StringHelper stringHelper = StringHelper.valueOf(locale);
        String name = contact.getDisplayName();
        if (0 < age) {
            return String.format(stringHelper.getString(BirthdaysCalendarStrings.EVENT_SUMMARY_WITH_AGE), name, Autoboxing.I(age));
        } else {
            return String.format(stringHelper.getString(BirthdaysCalendarStrings.EVENT_SUMMARY), name);
        }
    }

    private static int[] decodeId(String eventId) {
        String[] splitted = Strings.splitByDelimNotInQuotes(eventId, '-');
        if (null == splitted || 2 > splitted.length) {
            throw new IllegalArgumentException(eventId);
        }
        int folderId, contactId;
        try {
            folderId = Integer.parseInt(splitted[0]);
            contactId = Integer.parseInt(splitted[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(eventId, e);
        }
        return new int[] { folderId, contactId };
    }

    private static String encodeId(Contact contact) {
        return contact.getParentFolderID() + "-" + contact.getObjectID();
    }

}
