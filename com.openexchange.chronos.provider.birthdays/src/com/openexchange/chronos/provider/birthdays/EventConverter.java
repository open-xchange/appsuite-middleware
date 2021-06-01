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

package com.openexchange.chronos.provider.birthdays;

import static com.openexchange.chronos.common.CalendarUtils.getFlags;
import static com.openexchange.chronos.common.CalendarUtils.getURI;
import static com.openexchange.chronos.common.CalendarUtils.initCalendar;
import static com.openexchange.chronos.common.CalendarUtils.isInRange;
import static com.openexchange.chronos.common.CalendarUtils.truncateTime;
import static com.openexchange.java.Autoboxing.I;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactDisplayNameHelper;
import com.openexchange.groupware.container.Contact;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;
import com.openexchange.regional.RegionalSettingsService;
import com.openexchange.server.ServiceLookup;

/**
 * {@link EventConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.0
 */
public class EventConverter {

    /** The static recurrence rule used for birthday event */
    static final String BIRTHDAYS_RRULE = "FREQ=YEARLY;SKIP=BACKWARD";

    /** The static time transparency used for birthday event */
    static final Transp BIRTHDAYS_TRANSP = TimeTransparency.TRANSPARENT;

    /** The static classification used for birthday event */
    static final Classification BIRTHDAYS_CLASSIFICATION = Classification.PUBLIC;

    private final Locale locale;
    private final ServiceLookup services;
    private final String folderId;
    private final int calendarUserId;

    public EventConverter(ServiceLookup services, Locale locale, int calendarUserId) {
        super();
        this.locale = locale;
        this.services = services;
        this.calendarUserId = calendarUserId;
        this.folderId = BirthdaysCalendarAccess.FOLDER_ID;
    }

    public Event getSeriesMaster(Contact contact) {
        Event event = new Event();
        event.setFolderId(folderId);
        event.setId(getEventId(contact));
        event.setSeriesId(event.getId());
        CalendarUser calendarUser = new CalendarUser();
        calendarUser.setEntity(calendarUserId);
        event.setCalendarUser(calendarUser);
        event.setRecurrenceRule(BIRTHDAYS_RRULE);
        event.setTransp(BIRTHDAYS_TRANSP);
        event.setClassification(BIRTHDAYS_CLASSIFICATION);
        Calendar calendar = truncateTime(initCalendar(TimeZones.UTC, contact.getBirthday()));
        //        event.setStartDate(new DateTime(null, calendar.getTimeInMillis()).toAllDay());
        event.setStartDate(new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE)));
        calendar.add(Calendar.DATE, 1);
        //        event.setEndDate(new DateTime(null, calendar.getTimeInMillis()).toAllDay());
        event.setEndDate(new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE)));
        if (null != contact.getLastModified()) {
            event.setTimestamp(contact.getLastModified().getTime());
        }
        event.setUid(contact.getUid());
        event.setSummary(getSummary(contact));
        event.setDescription(getDescription(contact));
        if (Strings.isNotEmpty(contact.getEmail1())) {
            event.setAttendees(Collections.singletonList(getAttendee(contact)));
        }
        ExtendedProperties extendedProperties = new ExtendedProperties();
        extendedProperties.add(new ExtendedProperty("X-OX-CONTACT-ID", String.valueOf(contact.getObjectID())));
        extendedProperties.add(new ExtendedProperty("X-OX-CONTACT-FOLDER-ID", String.valueOf(contact.getParentFolderID())));
        event.setExtendedProperties(extendedProperties);
        event.setFlags(getFlags(event, calendarUserId));
        return event;
    }

    /**
     * Returns the master series of the specified contacts
     *
     * @param contacts The contacts
     * @param from The from date
     * @param until The until date
     * @param timeZone The timezone
     * @return The master series
     */
    public List<Event> getSeriesMasters(List<Contact> contacts, Date from, Date until, TimeZone timeZone) {
        if (null == contacts || 0 == contacts.size()) {
            return Collections.emptyList();
        }
        List<Event> events = new ArrayList<>(contacts.size());
        for (Contact contact : contacts) {
            Event seriesMaster = getSeriesMaster(contact);
            events.add(seriesMaster);
        }
        return events;
    }

    public List<Event> getOccurrences(Contact contact, Date from, Date until, TimeZone timeZone) throws OXException {
        List<Event> occurrences = new ArrayList<Event>();
        Event seriesEvent = getSeriesMaster(contact);
        RecurrenceIterator<Event> iterator = services.getService(RecurrenceService.class).iterateEventOccurrences(seriesEvent, from, until);
        while (iterator.hasNext()) {
            Event occurrence = nextOccurrence(iterator, contact);
            if (null != occurrence && isInRange(occurrence, from, until, timeZone)) {
                occurrences.add(occurrence);
            }
        }
        return occurrences;
    }

    public List<Event> getOccurrences(List<Contact> contacts, Date from, Date until, TimeZone timeZone) throws OXException {
        if (null == contacts || 0 == contacts.size()) {
            return Collections.emptyList();
        }
        List<Event> occurrences = new ArrayList<>();
        for (Contact contact : contacts) {
            occurrences.addAll(getOccurrences(contact, from, until, timeZone));
        }
        return occurrences;
    }

    public Event getOccurrence(Contact contact, RecurrenceId recurrenceId) throws OXException {
        Event seriesEvent = getSeriesMaster(contact);
        RecurrenceIterator<Event> iterator = services.getService(RecurrenceService.class).iterateEventOccurrences(seriesEvent, new Date(recurrenceId.getValue().getTimestamp()), null);
        if (false == iterator.hasNext()) {
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(seriesEvent.getSeriesId(), recurrenceId);
        }
        return nextOccurrence(iterator, contact);
    }

    public String getEventId(Contact contact) {
        return contact.getParentFolderID() + "-" + contact.getObjectID();
    }

    public int[] decodeEventId(String eventId) {
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

    private Event nextOccurrence(RecurrenceIterator<Event> iterator, Contact contact) {
        if (false == iterator.hasNext()) {
            return null;
        }
        Event occurrence = iterator.next();
        if (hasYear(contact.getBirthday())) {
            final String summary = getSummary(contact, iterator.getPosition() - 1);
            occurrence = new DelegatingEvent(occurrence) {

                @Override
                public String getSummary() {
                    return summary;
                }

                @Override
                public boolean containsSummary() {
                    return true;
                }
            };
        }
        return occurrence;
    }

    private String getDescription(Contact contact) {
        if (hasYear(contact.getBirthday())) {
            RegionalSettingsService regionalSettingsService = services.getService(RegionalSettingsService.class);
            DateFormat dateFormat;
            if (null != locale && null != regionalSettingsService) {
                dateFormat = regionalSettingsService.getDateFormat(contact.getContextId(), calendarUserId, locale, DateFormat.MEDIUM);
            } else if (null != locale) {
                dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
            } else {
                dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
            }
            dateFormat.setTimeZone(TimeZones.UTC);
            String descriptionFormat = StringHelper.valueOf(locale).getString(BirthdaysCalendarStrings.EVENT_DESCRIPTION);
            return String.format(descriptionFormat, dateFormat.format(contact.getBirthday()));
        }
        return null;
    }

    private Attendee getAttendee(Contact contact) {
        Attendee attendee = new Attendee();
        attendee.setCuType(CalendarUserType.INDIVIDUAL);
        attendee.setCn(getDisplayName(contact));
        attendee.setUri(getURI(contact.getEmail1()));
        if (0 < contact.getInternalUserId()) {
            attendee.setEntity(contact.getInternalUserId());
        }
        attendee.setPartStat(ParticipationStatus.ACCEPTED);
        return attendee;
    }

    private String getDisplayName(Contact contact) {
        return ContactDisplayNameHelper.formatDisplayName(services.getService(I18nServiceRegistry.class), contact, locale);
    }

    private String getSummary(Contact contact) {
        return getSummary(contact, 0);
    }

    private String getSummary(Contact contact, int age) {
        String name = getDisplayName(contact);
        StringHelper stringHelper = StringHelper.valueOf(locale);
        if (0 < age) {
            return String.format(stringHelper.getString(BirthdaysCalendarStrings.EVENT_SUMMARY_WITH_AGE), name, I(age));
        }
        return String.format(stringHelper.getString(BirthdaysCalendarStrings.EVENT_SUMMARY), name);
    }

    protected static boolean hasYear(Date birthday) {
        if (birthday == null) {
            return false;
        }
        Calendar calendar = CalendarUtils.initCalendar(TimeZones.UTC, birthday);
        int year = calendar.get(Calendar.YEAR);
        return 0 != year && 1 != year && 1604 != year;
    }

}
