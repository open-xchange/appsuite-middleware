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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventStatus;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.DefaultMapper;
import com.openexchange.groupware.tools.mappings.DefaultMapping;
import com.openexchange.groupware.tools.mappings.Mapping;
import com.openexchange.tools.arrays.Arrays;

/**
 * {@link EventMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventMapper extends DefaultMapper<Event, EventField> {

    private static final EventMapper INSTANCE = new EventMapper();

    /** The event fields that are preserved for reference in "tombstone" events */
    private static final EventField[] TOMBSTONE_FIELDS = {
        EventField.CHANGE_EXCEPTION_DATES, EventField.CLASSIFICATION, EventField.CREATED, EventField.CREATED_BY,
        EventField.DELETE_EXCEPTION_DATES, EventField.END_DATE, EventField.ID, EventField.LAST_MODIFIED,
        EventField.MODIFIED_BY, EventField.CALENDAR_USER, EventField.FOLDER_ID, EventField.SERIES_ID, EventField.RECURRENCE_RULE,
        EventField.SEQUENCE, EventField.START_DATE, EventField.TRANSP, EventField.UID,
        EventField.FILENAME, EventField.SEQUENCE
    };

    /**
     * Gets the event mapper instance.
     *
     * @return The event mapper.
     */
    public static EventMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link EventMapper}.
     */
    private EventMapper() {
        super();
    }

    /**
     * Copies some specific or all properties from one event into to another one.
     *
     * @param from The source event
     * @param to The destination event, or <code>null</code> to copy into a newly initialized one
     * @param considerUnset <code>true</code> to also consider not <i>set</i> properties of the source, <code>false</code>, otherwise
     * @param fields The fields to copy, or <code>null</code> to copy all mapped fields
     * @return The destination event
     */
    public Event copy(Event from, Event to, boolean considerUnset, EventField... fields) throws OXException {
        if (null == to) {
            to = new Event();
        }
        if (null == fields) {
            for (Mapping<? extends Object, Event> mapping : getMappings().values()) {
                if (considerUnset || mapping.isSet(from)) {
                    mapping.copy(from, to);
                }
            }
        } else {
            for (EventField field : fields) {
                Mapping<? extends Object, Event> mapping = get(field);
                if (considerUnset || mapping.isSet(from)) {
                    mapping.copy(from, to);
                }
            }
        }
        return to;
    }

    /**
     * Creates a new object and sets all those properties that are <i>set</i> and different in the supplied object to the values from the
     * second one, thus, generating some kind of a 'delta' object.
     *
     * @param original The original object
     * @param update The updated object
     * @param considerUnset <code>true</code> to also consider comparison with not <i>set</i> fields of the original, <code>false</code>, otherwise
     * @param ignoredFields Fields to ignore when determining the differences
     * @return An object containing the properties that are different
     */
    public Event getDifferences(Event original, Event update, boolean considerUnset, EventField... ignoredFields) throws OXException {
        if (null == original) {
            throw new IllegalArgumentException("original");
        }
        if (null == update) {
            throw new IllegalArgumentException("update");
        }
        Event delta = newInstance();
        for (Entry<EventField, ? extends Mapping<? extends Object, Event>> entry : getMappings().entrySet()) {
            if (Arrays.contains(ignoredFields, entry.getKey())) {
                continue;
            }
            Mapping<? extends Object, Event> mapping = entry.getValue();
            if (mapping.isSet(update) && ((considerUnset || mapping.isSet(original)) && false == mapping.equals(original, update))) {
                mapping.copy(update, delta);
            }
        }
        return delta;
    }

    /**
     * Gets a value indicating whether specific properties of one event are equal to those properties of a second event.
     *
     * @param event1 The first event to compare
     * @param event2 The second event to compare
     * @param fields The event fields to compare
     * @return <code>true</code> if all fields are equal, <code>false</code>, otherwise
     * @throws OXException
     */
    public boolean equalsByFields(Event event1, Event event2, EventField... fields) throws OXException {
        for (EventField field : fields) {
            if (false == get(field).equals(event1, event2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Copies data from one event to another. Only <i>set</i> fields of the source event are transferred, unless they're not already
     * <i>set</i> in the target event.
     *
     * @param from The source event
     * @param to The destination event
     * @param fields The fields to copy
     */
    public void copyIfNotSet(Event from, Event to, EventField... fields) throws OXException {
        for (EventField field : fields) {
            Mapping<? extends Object, Event> mapping = get(field);
            if (mapping.isSet(from) && false == mapping.isSet(to)) {
                mapping.copy(from, to);
            }
        }
    }

    /**
     * Generates a tombstone event object based on the supplied event, as used to track the deletion in the storage.
     *
     * @param event The event to create the tombstone for
     * @param lastModified The last modification time to take over
     * @param modifiedBy The identifier of the modifying user to take over
     * @return The tombstone event
     */
    public Event getTombstone(Event event, Date lastModified, int modifiedBy) throws OXException {
        Event tombstone = copy(event, new Event(), true, TOMBSTONE_FIELDS);
        Consistency.setModified(lastModified, tombstone, modifiedBy);
        return tombstone;
    }

    @Override
    public Event newInstance() {
        return new Event();
    }

    @Override
    public EventField[] newArray(int size) {
        return new EventField[size];
    }

    @Override
    protected EnumMap<EventField, ? extends Mapping<? extends Object, Event>> getMappings() {
        EnumMap<EventField, Mapping<? extends Object, Event>> mappings = new
            EnumMap<EventField, Mapping<? extends Object, Event>>(EventField.class);
        mappings.put(EventField.ID, new DefaultMapping<String, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsId();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setId(value);
            }

            @Override
            public String get(Event object) {
                return object.getId();
            }

            @Override
            public void remove(Event object) {
                object.removeId();
            }
        });
        mappings.put(EventField.FOLDER_ID, new DefaultMapping<String, Event>() {

            @Override
            public void set(Event event, String value) {
                event.setFolderId(value);
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsFolderId();
            }

            @Override
            public String get(Event event) {
                return event.getFolderId();
            }

            @Override
            public void remove(Event event) {
                event.removeFolderId();
            }
        });
        mappings.put(EventField.UID, new DefaultMapping<String, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsUid();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setUid(value);
            }

            @Override
            public String get(Event object) {
                return object.getUid();
            }

            @Override
            public void remove(Event object) {
                object.removeUid();
            }
        });
        mappings.put(EventField.FILENAME, new DefaultMapping<String, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsFilename();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setFilename(value);
            }

            @Override
            public String get(Event object) {
                return object.getFilename();
            }

            @Override
            public void remove(Event object) {
                object.removeFilename();
            }
        });
        mappings.put(EventField.SEQUENCE, new DefaultMapping<Integer, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsSequence();
            }

            @Override
            public void set(Event object, Integer value) throws OXException {
                object.setSequence(null == value ? 0 : i(value));
            }

            @Override
            public Integer get(Event object) {
                return I(object.getSequence());
            }

            @Override
            public void remove(Event object) {
                object.removeSequence();
            }
        });
        mappings.put(EventField.CREATED, new DefaultMapping<Date, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsCreated();
            }

            @Override
            public void set(Event object, Date value) throws OXException {
                object.setCreated(value);
            }

            @Override
            public Date get(Event object) {
                return object.getCreated();
            }

            @Override
            public void remove(Event object) {
                object.removeSequence();
            }
        });
        mappings.put(EventField.CREATED_BY, new DefaultMapping<Integer, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsCreatedBy();
            }

            @Override
            public void set(Event object, Integer value) throws OXException {
                object.setCreatedBy(null == value ? 0 : i(value));
            }

            @Override
            public Integer get(Event object) {
                return I(object.getCreatedBy());
            }

            @Override
            public void remove(Event object) {
                object.removeCreatedBy();
            }
        });
        mappings.put(EventField.LAST_MODIFIED, new DefaultMapping<Date, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsLastModified();
            }

            @Override
            public void set(Event object, Date value) throws OXException {
                object.setLastModified(value);
            }

            @Override
            public Date get(Event object) {
                return object.getLastModified();
            }

            @Override
            public void remove(Event object) {
                object.removeLastModified();
            }
        });
        mappings.put(EventField.MODIFIED_BY, new DefaultMapping<Integer, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsModifiedBy();
            }

            @Override
            public void set(Event object, Integer value) throws OXException {
                object.setModifiedBy(null == value ? 0 : i(value));
            }

            @Override
            public Integer get(Event object) {
                return I(object.getModifiedBy());
            }

            @Override
            public void remove(Event object) {
                object.removeModifiedBy();
            }
        });
        mappings.put(EventField.CALENDAR_USER, new DefaultMapping<Integer, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsCalendarUser();
            }

            @Override
            public void set(Event object, Integer value) throws OXException {
                object.setCalendarUser(null == value ? 0 : i(value));
            }

            @Override
            public Integer get(Event object) {
                return I(object.getCalendarUser());
            }

            @Override
            public void remove(Event object) {
                object.removeCalendarUser();
            }
        });
        mappings.put(EventField.SUMMARY, new DefaultMapping<String, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsSummary();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setSummary(value);
            }

            @Override
            public String get(Event object) {
                return object.getSummary();
            }

            @Override
            public void remove(Event object) {
                object.removeSummary();
            }
        });
        mappings.put(EventField.LOCATION, new DefaultMapping<String, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsLocation();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setLocation(value);
            }

            @Override
            public String get(Event object) {
                return object.getLocation();
            }

            @Override
            public void remove(Event object) {
                object.removeLocation();
            }
        });
        mappings.put(EventField.DESCRIPTION, new DefaultMapping<String, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsDescription();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setDescription(value);
            }

            @Override
            public String get(Event object) {
                return object.getDescription();
            }

            @Override
            public void remove(Event object) {
                object.removeDescription();
            }
        });
        mappings.put(EventField.CATEGORIES, new DefaultMapping<List<String>, Event>() {

            @Override
            public void copy(Event from, Event to) throws OXException {
                List<String> value = get(from);
                set(to, null == value ? null : new ArrayList<String>(value));
            }

            @Override
            public boolean isSet(Event object) {
                return object.containsCategories();
            }

            @Override
            public void set(Event object, List<String> value) throws OXException {
                object.setCategories(value);
            }

            @Override
            public List<String> get(Event object) {
                return object.getCategories();
            }

            @Override
            public void remove(Event object) {
                object.removeCategories();
            }
        });
        mappings.put(EventField.CLASSIFICATION, new DefaultMapping<Classification, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsClassification();
            }

            @Override
            public void set(Event object, Classification value) throws OXException {
                object.setClassification(value);
            }

            @Override
            public Classification get(Event object) {
                return object.getClassification();
            }

            @Override
            public void remove(Event object) {
                object.removeClassification();
            }
        });
        mappings.put(EventField.COLOR, new DefaultMapping<String, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsColor();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setColor(value);
            }

            @Override
            public String get(Event object) {
                return object.getColor();
            }

            @Override
            public void remove(Event object) {
                object.removeColor();
            }
        });
        mappings.put(EventField.START_DATE, new DefaultMapping<DateTime, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsStartDate();
            }

            @Override
            public void set(Event object, DateTime value) throws OXException {
                object.setStartDate(value);
            }

            @Override
            public DateTime get(Event object) {
                return object.getStartDate();
            }

            @Override
            public void remove(Event object) {
                object.removeStartDate();
            }

            @Override
            public int compare(Event event1, Event event2, Locale locale, TimeZone timeZone) {
                return CalendarUtils.compare(get(event1), get(event2), timeZone);
            }
        });
        mappings.put(EventField.END_DATE, new DefaultMapping<DateTime, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsEndDate();
            }

            @Override
            public void set(Event object, DateTime value) throws OXException {
                object.setEndDate(value);
            }

            @Override
            public DateTime get(Event object) {
                return object.getEndDate();
            }

            @Override
            public void remove(Event object) {
                object.removeEndDate();
            }

            @Override
            public int compare(Event event1, Event event2, Locale locale, TimeZone timeZone) {
                return CalendarUtils.compare(get(event1), get(event2), timeZone);
            }
        });
        mappings.put(EventField.TRANSP, new DefaultMapping<Transp, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsTransp();
            }

            @Override
            public void set(Event object, Transp value) throws OXException {
                object.setTransp(value);
            }

            @Override
            public Transp get(Event object) {
                return object.getTransp();
            }

            @Override
            public void remove(Event object) {
                object.removeTransp();
            }
        });
        mappings.put(EventField.SERIES_ID, new DefaultMapping<String, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsSeriesId();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setSeriesId(value);
            }

            @Override
            public String get(Event object) {
                return object.getSeriesId();
            }

            @Override
            public void remove(Event object) {
                object.removeSeriesId();
            }
        });
        mappings.put(EventField.RECURRENCE_RULE, new DefaultMapping<String, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsRecurrenceRule();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setRecurrenceRule(value);
            }

            @Override
            public String get(Event object) {
                return object.getRecurrenceRule();
            }

            @Override
            public void remove(Event object) {
                object.removeRecurrenceRule();
            }
        });
        mappings.put(EventField.RECURRENCE_ID, new DefaultMapping<RecurrenceId, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsRecurrenceId();
            }

            @Override
            public void set(Event object, RecurrenceId value) throws OXException {
                object.setRecurrenceId(value);
            }

            @Override
            public RecurrenceId get(Event object) {
                return object.getRecurrenceId();
            }

            @Override
            public void remove(Event object) {
                object.removeRecurrenceId();
            }
        });
        mappings.put(EventField.CHANGE_EXCEPTION_DATES, new DefaultMapping<SortedSet<RecurrenceId>, Event>() {

            @Override
            public void copy(Event from, Event to) throws OXException {
                SortedSet<RecurrenceId> value = get(from);
                set(to, null == value ? null : new TreeSet<RecurrenceId>(value));
            }

            @Override
            public boolean isSet(Event object) {
                return object.containsChangeExceptionDates();
            }

            @Override
            public void set(Event object, SortedSet<RecurrenceId> value) throws OXException {
                object.setChangeExceptionDates(value);
            }

            @Override
            public SortedSet<RecurrenceId> get(Event object) {
                return object.getChangeExceptionDates();
            }

            @Override
            public void remove(Event object) {
                object.removeChangeExceptionDates();
            }
        });
        mappings.put(EventField.DELETE_EXCEPTION_DATES, new DefaultMapping<SortedSet<RecurrenceId>, Event>() {

            @Override
            public void copy(Event from, Event to) throws OXException {
                SortedSet<RecurrenceId> value = get(from);
                set(to, null == value ? null : new TreeSet<RecurrenceId>(value));
            }

            @Override
            public boolean isSet(Event object) {
                return object.containsDeleteExceptionDates();
            }

            @Override
            public void set(Event object, SortedSet<RecurrenceId> value) throws OXException {
                object.setDeleteExceptionDates(value);
            }

            @Override
            public SortedSet<RecurrenceId> get(Event object) {
                return object.getDeleteExceptionDates();
            }

            @Override
            public void remove(Event object) {
                object.removeDeleteExceptionDates();
            }
        });
        mappings.put(EventField.STATUS, new DefaultMapping<EventStatus, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsStatus();
            }

            @Override
            public void set(Event object, EventStatus value) throws OXException {
                object.setStatus(value);
            }

            @Override
            public EventStatus get(Event object) {
                return object.getStatus();
            }

            @Override
            public void remove(Event object) {
                object.removeStatus();
            }
        });
        mappings.put(EventField.ORGANIZER, new DefaultMapping<Organizer, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsOrganizer();
            }

            @Override
            public void set(Event object, Organizer value) throws OXException {
                object.setOrganizer(value);
            }

            @Override
            public Organizer get(Event object) {
                return object.getOrganizer();
            }

            @Override
            public void remove(Event object) {
                object.removeOrganizer();
            }
        });
        mappings.put(EventField.ATTENDEES, new DefaultMapping<List<Attendee>, Event>() {

            @Override
            public void copy(Event from, Event to) throws OXException {
                List<Attendee> value = get(from);
                set(to, null == value ? null : AttendeeMapper.getInstance().copy(value, (AttendeeField[]) null));
            }

            @Override
            public boolean isSet(Event object) {
                return object.containsAttendees();
            }

            @Override
            public void set(Event object, List<Attendee> value) throws OXException {
                object.setAttendees(value);
            }

            @Override
            public List<Attendee> get(Event object) {
                return object.getAttendees();
            }

            @Override
            public void remove(Event object) {
                object.removeAttendees();
            }
        });
        mappings.put(EventField.ATTACHMENTS, new DefaultMapping<List<Attachment>, Event>() {

            @Override
            public void copy(Event from, Event to) throws OXException {
                List<Attachment> value = get(from);
                set(to, null == value ? null : new ArrayList<Attachment>(value)); //TODO deep copy
            }

            @Override
            public boolean isSet(Event object) {
                return object.containsAttachments();
            }

            @Override
            public void set(Event object, List<Attachment> value) throws OXException {
                object.setAttachments(value);
            }

            @Override
            public List<Attachment> get(Event object) {
                return object.getAttachments();
            }

            @Override
            public void remove(Event object) {
                object.removeAttachments();
            }
        });
        mappings.put(EventField.ALARMS, new DefaultMapping<List<Alarm>, Event>() {

            @Override
            public void copy(Event from, Event to) throws OXException {
                List<Alarm> value = get(from);
                set(to, null == value ? null : AlarmMapper.getInstance().copy(value, (AlarmField[]) null));
            }

            @Override
            public boolean isSet(Event object) {
                return object.containsAlarms();
            }

            @Override
            public void set(Event object, List<Alarm> value) throws OXException {
                object.setAlarms(value);
            }

            @Override
            public List<Alarm> get(Event object) {
                return object.getAlarms();
            }

            @Override
            public void remove(Event object) {
                object.removeAlarms();
            }
        });
        mappings.put(EventField.EXTENDED_PROPERTIES, new DefaultMapping<ExtendedProperties, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsExtendedProperties();
            }

            @Override
            public void set(Event object, ExtendedProperties value) throws OXException {
                object.setExtendedProperties(value);
            }

            @Override
            public ExtendedProperties get(Event object) {
                return object.getExtendedProperties();
            }

            @Override
            public void remove(Event object) {
                object.removeStatus();
            }
        });
        return mappings;
    }

}
