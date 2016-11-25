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

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.b;
import static com.openexchange.java.Autoboxing.i;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventStatus;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.Transp;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.DefaultMapper;
import com.openexchange.groupware.tools.mappings.DefaultMapping;
import com.openexchange.groupware.tools.mappings.Mapping;

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
        EventField.ALL_DAY, EventField.CHANGE_EXCEPTION_DATES, EventField.CLASSIFICATION, EventField.CREATED, EventField.CREATED_BY,
        EventField.DELETE_EXCEPTION_DATES, EventField.END_DATE, EventField.END_TIMEZONE, EventField.ID, EventField.LAST_MODIFIED,
        EventField.MODIFIED_BY, EventField.PUBLIC_FOLDER_ID, EventField.SERIES_ID, EventField.RECURRENCE_RULE, EventField.SEQUENCE,
        EventField.START_DATE, EventField.START_TIMEZONE, EventField.END_TIMEZONE, EventField.TRANSP, EventField.UID,
        EventField.SEQUENCE
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
     * Copies data from one event to another. Only <i>set</i> fields are transferred.
     *
     * @param from The source event
     * @param to The destination event
     * @param fields The fields to copy
     */
    @Override
    public void copy(Event from, Event to, EventField... fields) throws OXException {
        for (EventField field : fields) {
            Mapping<? extends Object, Event> mapping = get(field);
            if (mapping.isSet(from)) {
                mapping.copy(from, to);
            }
        }
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
        Event tombstone = new Event();
        copy(event, tombstone, TOMBSTONE_FIELDS);
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
        mappings.put(EventField.ID, new DefaultMapping<Integer, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsId();
            }

            @Override
            public void set(Event object, Integer value) throws OXException {
                object.setId(null == value ? 0 : i(value));
            }

            @Override
            public Integer get(Event object) {
                return I(object.getId());
            }

            @Override
            public void remove(Event object) {
                object.removeId();
            }
        });
        mappings.put(EventField.FOLDER_ID, new DefaultMapping<Integer, Event>() {

            @Override
            public void set(Event event, Integer value) {
                event.setFolderId(null == value ? 0 : i(value));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsFolderId();
            }

            @Override
            public Integer get(Event event) {
                return I(event.getFolderId());
            }

            @Override
            public void remove(Event event) {
                event.removeFolderId();
            }
        });
        mappings.put(EventField.PUBLIC_FOLDER_ID, new DefaultMapping<Integer, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsPublicFolderId();
            }

            @Override
            public void set(Event object, Integer value) throws OXException {
                object.setPublicFolderId(null == value ? 0 : i(value));
            }

            @Override
            public Integer get(Event object) {
                return I(object.getPublicFolderId());
            }

            @Override
            public void remove(Event object) {
                object.removePublicFolderId();
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
                return object.containsSequence();
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
        mappings.put(EventField.START_DATE, new DefaultMapping<Date, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsStartDate();
            }

            @Override
            public void set(Event object, Date value) throws OXException {
                object.setStartDate(value);
            }

            @Override
            public Date get(Event object) {
                return object.getStartDate();
            }

            @Override
            public void remove(Event object) {
                object.removeStartDate();
            }
        });
        mappings.put(EventField.START_TIMEZONE, new DefaultMapping<String, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsStartTimeZone();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setStartTimeZone(value);
            }

            @Override
            public String get(Event object) {
                return object.getStartTimeZone();
            }

            @Override
            public void remove(Event object) {
                object.removeStartTimeZone();
            }
        });
        mappings.put(EventField.END_DATE, new DefaultMapping<Date, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsEndDate();
            }

            @Override
            public void set(Event object, Date value) throws OXException {
                object.setEndDate(value);
            }

            @Override
            public Date get(Event object) {
                return object.getEndDate();
            }

            @Override
            public void remove(Event object) {
                object.removeEndDate();
            }
        });
        mappings.put(EventField.END_TIMEZONE, new DefaultMapping<String, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsEndTimeZone();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setEndTimeZone(value);
            }

            @Override
            public String get(Event object) {
                return object.getEndTimeZone();
            }

            @Override
            public void remove(Event object) {
                object.removeEndTimeZone();
            }
        });
        mappings.put(EventField.ALL_DAY, new DefaultMapping<Boolean, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsAllDay();
            }

            @Override
            public void set(Event object, Boolean value) throws OXException {
                object.setAllDay(null == value ? false : b(value));
            }

            @Override
            public Boolean get(Event object) {
                return B(object.getAllDay());
            }

            @Override
            public void remove(Event object) {
                object.removeAllDay();
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
        mappings.put(EventField.SERIES_ID, new DefaultMapping<Integer, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsSeriesId();
            }

            @Override
            public void set(Event object, Integer value) throws OXException {
                object.setSeriesId(null == value ? 0 : i(value));
            }

            @Override
            public Integer get(Event object) {
                return I(object.getSeriesId());
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
        mappings.put(EventField.CHANGE_EXCEPTION_DATES, new DefaultMapping<List<Date>, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsChangeExceptionDates();
            }

            @Override
            public void set(Event object, List<Date> value) throws OXException {
                object.setChangeExceptionDates(value);
            }

            @Override
            public List<Date> get(Event object) {
                return object.getChangeExceptionDates();
            }

            @Override
            public void remove(Event object) {
                object.removeChangeExceptionDates();
            }
        });
        mappings.put(EventField.DELETE_EXCEPTION_DATES, new DefaultMapping<List<Date>, Event>() {

            @Override
            public boolean isSet(Event object) {
                return object.containsDeleteExceptionDates();
            }

            @Override
            public void set(Event object, List<Date> value) throws OXException {
                object.setDeleteExceptionDates(value);
            }

            @Override
            public List<Date> get(Event object) {
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
        return mappings;
    }

}
