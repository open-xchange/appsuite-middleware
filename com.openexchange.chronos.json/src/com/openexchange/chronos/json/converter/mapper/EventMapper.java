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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.chronos.json.converter.mapper;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import org.dmfs.rfc5545.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.DefaultAttendeePrivileges;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventFlag;
import com.openexchange.chronos.EventStatus;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.json.fields.ChronosJsonFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapping;
import com.openexchange.groupware.tools.mappings.json.IntegerMapping;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.groupware.tools.mappings.json.ListMapping;
import com.openexchange.groupware.tools.mappings.json.LongMapping;
import com.openexchange.groupware.tools.mappings.json.StringMapping;
import com.openexchange.java.Enums;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link EventMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventMapper extends DefaultJsonMapper<Event, EventField> {

    private static final EventMapper INSTANCE = new EventMapper();

    private final EventField[] mappedFields;

    /**
     * Gets the EventMapper instance.
     *
     * @return The EventMapper instance.
     */
    public static EventMapper getInstance() {
        return INSTANCE;
    }


    /**
     * Initializes a new {@link EventMapper}.
     */
    private EventMapper() {
        super();
        this.mappedFields = mappings.keySet().toArray(newArray(mappings.keySet().size()));
    }

    @Override
    public EventField[] getMappedFields() {
        return mappedFields;
    }

    /**
     * Parses event fields from a comma separated string.
     *
     * @param value The comma separated string of field names to parse
     * @return The parsed fields, or <code>null</code> if the passed value was <code>null</code>
     */
    public Set<EventField> parseFields(String value) throws OXException {
        if (null == value) {
            return null;
        }
        String[] splittedValue = Strings.splitByComma(value);
        return parseFields(splittedValue);
    }

    /**
     * Parses event fields from an array of event identifiers.
     *
     * @param value The field identifiers
     * @return The parsed fields, or <code>null</code> if the passed array was <code>null</code>
     */
    public Set<EventField> parseFields(String[] fieldNames) throws OXException {
        if (null == fieldNames) {
            return null;
        }
        Set<EventField> fields = new HashSet<EventField>(fieldNames.length);
        for (int i = 0; i < fieldNames.length; i++) {
            EventField field = getMappedField(fieldNames[i]);
            if (null == field) {
                throw OXException.notFound(fieldNames[i]);
            }
            fields.add(field);
        }
        return fields;
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
    protected EnumMap<EventField, ? extends JsonMapping<? extends Object, Event>> createMappings() {
        EnumMap<EventField, JsonMapping<? extends Object, Event>> mappings = new
            EnumMap<EventField, JsonMapping<? extends Object, Event>>(EventField.class);
        mappings.put(EventField.ID, new StringMapping<Event>(ChronosJsonFields.ID, null) {

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
        mappings.put(EventField.FOLDER_ID, new StringMapping<Event>(ChronosJsonFields.FOLDER, null) {

            @Override
            public boolean isSet(Event object) {
                return object.containsFolderId();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setFolderId(value);
            }

            @Override
            public String get(Event object) {
                return object.getFolderId();
            }

            @Override
            public void remove(Event object) {
                object.removeFolderId();
            }
        });
        mappings.put(EventField.UID, new StringMapping<Event>(ChronosJsonFields.UID, null) {

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
        mappings.put(EventField.FILENAME, new StringMapping<Event>(ChronosJsonFields.FILENAME, null) {

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
        mappings.put(EventField.SEQUENCE, new IntegerMapping<Event>(ChronosJsonFields.SEQUENCE, null) {

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
        mappings.put(EventField.CREATED, new LongMapping<Event>(ChronosJsonFields.CREATED, null) {

            @Override
            public boolean isSet(Event object) {
                return object.containsCreated();
            }

            @Override
            public void set(Event object, Long value) throws OXException {
                object.setCreated(new Date(value));
            }

            @Override
            public Long get(Event object) {
                return object.getCreated() != null ? object.getCreated().getTime() : 0l;
            }

            @Override
            public void remove(Event object) {
                object.removeCreated();
            }
        });
        mappings.put(EventField.TIMESTAMP, new LongMapping<Event>(ChronosJsonFields.TIMESTAMP, null) {

            @Override
            public boolean isSet(Event object) {
                return object.containsTimestamp();
            }

            @Override
            public void set(Event object, Long value) throws OXException {
                object.setTimestamp(value);
            }

            @Override
            public Long get(Event object) {
                return object.getTimestamp();
            }

            @Override
            public void remove(Event object) {
                object.removeTimestamp();
            }
        });
        mappings.put(EventField.CREATED_BY, new CalendarUserMapping<CalendarUser, Event>(ChronosJsonFields.CREATED_BY, null) {

            @Override
            public CalendarUser newInstance() {
                return new CalendarUser();
            }

            @Override
            public boolean isSet(Event object) {
                return object.containsCreatedBy();
            }

            @Override
            public void set(Event object, CalendarUser value) throws OXException {
                object.setCreatedBy(value);
            }

            @Override
            public CalendarUser get(Event object) {
                return object.getCreatedBy();
            }

            @Override
            public void remove(Event object) {
                object.removeCreatedBy();
            }
        });
        mappings.put(EventField.LAST_MODIFIED, new LongMapping<Event>(ChronosJsonFields.LAST_MODIFIED, null) {

            @Override
            public boolean isSet(Event object) {
                return object.containsLastModified();
            }

            @Override
            public void set(Event object, Long value) throws OXException {
                object.setLastModified(new Date(value));
            }

            @Override
            public Long get(Event object) {
                return object.getLastModified() != null ? object.getLastModified().getTime() : 0l;
            }

            @Override
            public void remove(Event object) {
                object.removeLastModified();
            }
        });
        mappings.put(EventField.MODIFIED_BY, new CalendarUserMapping<CalendarUser, Event>(ChronosJsonFields.MODIFIED_BY, null) {

            @Override
            public CalendarUser newInstance() {
                return new CalendarUser();
            }

            @Override
            public boolean isSet(Event object) {
                return object.containsModifiedBy();
            }

            @Override
            public void set(Event object, CalendarUser value) throws OXException {
                object.setModifiedBy(value);
            }

            @Override
            public CalendarUser get(Event object) {
                return object.getModifiedBy();
            }

            @Override
            public void remove(Event object) {
                object.removeModifiedBy();
            }
        });
        mappings.put(EventField.CALENDAR_USER, new CalendarUserMapping<CalendarUser, Event>(ChronosJsonFields.CALENDAR_USER, null) {

            @Override
            public CalendarUser newInstance() {
                return new CalendarUser();
            }

            @Override
            public boolean isSet(Event object) {
                return object.containsCalendarUser();
            }

            @Override
            public void set(Event object, CalendarUser value) throws OXException {
                object.setCalendarUser(value);
            }

            @Override
            public CalendarUser get(Event object) {
                return object.getCalendarUser();
            }

            @Override
            public void remove(Event object) {
                object.removeCalendarUser();
            }
        });
        mappings.put(EventField.SUMMARY, new StringMapping<Event>(ChronosJsonFields.SUMMARY, null) {

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
        mappings.put(EventField.LOCATION, new StringMapping<Event>(ChronosJsonFields.LOCATION, null) {

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
        mappings.put(EventField.DESCRIPTION, new StringMapping<Event>(ChronosJsonFields.DESCRIPTION, null) {

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
        mappings.put(EventField.CATEGORIES, new ListMapping<String, Event>(ChronosJsonFields.CATEGORIES, null) {

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
                object.removeDescription();
            }

            @Override
            protected String deserialize(JSONArray array, int index, TimeZone timeZone) throws JSONException, OXException {
                return array.getString(index);
            }
        });
        mappings.put(EventField.CLASSIFICATION, new StringMapping<Event>(ChronosJsonFields.CLASSIFICATION, null) {

            @Override
            public boolean isSet(Event object) {
                return object.containsClassification();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setClassification(null == value ? null : new Classification(value));
            }

            @Override
            public String get(Event object) {
                Classification value = object.getClassification();
                return null == value ? null : value.getValue();
            }

            @Override
            public void remove(Event object) {
                object.removeClassification();
            }
        });
        mappings.put(EventField.COLOR, new StringMapping<Event>(ChronosJsonFields.COLOR, null) {

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
        mappings.put(EventField.START_DATE, new DateTimeMapping<Event>(ChronosJsonFields.START_DATE, null) {

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
        });
        mappings.put(EventField.END_DATE, new DateTimeMapping<Event>(ChronosJsonFields.END_DATE, null) {

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
        });
        mappings.put(EventField.TRANSP, new StringMapping<Event>(ChronosJsonFields.TRANSP, null) {

            @Override
            public boolean isSet(Event object) {
                return object.containsTransp();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setTransp(Enums.parse(TimeTransparency.class, value, TimeTransparency.OPAQUE));
            }

            @Override
            public String get(Event object) {
                Transp transp = object.getTransp();
                return null == transp ? null : transp.getValue();
            }

            @Override
            public void remove(Event object) {
                object.removeTransp();
            }
        });
        mappings.put(EventField.SERIES_ID, new StringMapping<Event>(ChronosJsonFields.SERIES_ID, null) {

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
        mappings.put(EventField.RECURRENCE_RULE, new StringMapping<Event>(ChronosJsonFields.RECURRENCE_RULE, null) {

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
        mappings.put(EventField.RECURRENCE_ID, new StringMapping<Event>(ChronosJsonFields.RECURRENCE_ID, null) {

            @Override
            public boolean isSet(Event object) {
                return object.containsRecurrenceId();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setRecurrenceId(null == value ? null : new DefaultRecurrenceId(value));
            }

            @Override
            public String get(Event object) {
                RecurrenceId value = object.getRecurrenceId();
                return null == value ? null : value.getValue().toString();
            }

            @Override
            public void remove(Event object) {
                object.removeRecurrenceId();
            }
        });
        mappings.put(EventField.RECURRENCE_DATES, new ListMapping<String, Event>(ChronosJsonFields.RECURRENCE_DATES, null) {

            @Override
            public boolean isSet(Event object) {
                return object.containsRecurrenceDates();
            }

            @Override
            public void set(Event object, List<String> value) throws OXException {
                if (null == value) {
                    object.setRecurrenceDates(null);
                } else {
                    SortedSet<RecurrenceId> recurrenceIds = new TreeSet<RecurrenceId>();
                    for (String dateTimeString : value) {
                        recurrenceIds.add(new DefaultRecurrenceId(dateTimeString));
                    }
                    object.setRecurrenceDates(recurrenceIds);
                }
            }

            @Override
            public List<String> get(Event object) {
                SortedSet<RecurrenceId> recurrenceIds = object.getRecurrenceDates();
                if (null == recurrenceIds) {
                    return null;
                }
                List<String> value = new ArrayList<String>(recurrenceIds.size());
                for (RecurrenceId recurrenceId : recurrenceIds) {
                    value.add(recurrenceId.getValue().toString());
                }
                return value;
            }

            @Override
            public void remove(Event object) {
                object.removeRecurrenceDates();
            }

            @Override
            protected String deserialize(JSONArray array, int index, TimeZone timeZone) throws JSONException, OXException {
                return array.getString(index);
            }
        });
        mappings.put(EventField.CHANGE_EXCEPTION_DATES, new ListMapping<String, Event>(ChronosJsonFields.CHANGE_EXCEPTION_DATES, null) {

            @Override
            public boolean isSet(Event object) {
                return object.containsChangeExceptionDates();
            }

            @Override
            public void set(Event object, List<String> value) throws OXException {
                if (null == value) {
                    object.setChangeExceptionDates(null);
                } else {
                    SortedSet<RecurrenceId> recurrenceIds = new TreeSet<RecurrenceId>();
                    for (String dateTimeString : value) {
                        recurrenceIds.add(new DefaultRecurrenceId(dateTimeString));
                    }
                    object.setChangeExceptionDates(recurrenceIds);
                }
            }

            @Override
            public List<String> get(Event object) {
                SortedSet<RecurrenceId> recurrenceIds = object.getChangeExceptionDates();
                if (null == recurrenceIds) {
                    return null;
                }
                List<String> value = new ArrayList<String>(recurrenceIds.size());
                for (RecurrenceId recurrenceId : recurrenceIds) {
                    value.add(recurrenceId.getValue().toString());
                }
                return value;
            }

            @Override
            public void remove(Event object) {
                object.removeChangeExceptionDates();
            }

            @Override
            protected String deserialize(JSONArray array, int index, TimeZone timeZone) throws JSONException, OXException {
                return array.getString(index);
            }
        });
        mappings.put(EventField.DELETE_EXCEPTION_DATES, new ListMapping<String, Event>(ChronosJsonFields.DELETE_EXCEPTION_DATES, null) {

            @Override
            public boolean isSet(Event object) {
                return object.containsDeleteExceptionDates();
            }

            @Override
            public void set(Event object, List<String> value) throws OXException {
                if (null == value) {
                    object.setDeleteExceptionDates(null);
                } else {
                    SortedSet<RecurrenceId> recurrenceIds = new TreeSet<RecurrenceId>();
                    for (String dateTimeString : value) {
                        recurrenceIds.add(new DefaultRecurrenceId(dateTimeString));
                    }
                    object.setDeleteExceptionDates(recurrenceIds);
                }
            }

            @Override
            public List<String> get(Event object) {
                SortedSet<RecurrenceId> recurrenceIds = object.getDeleteExceptionDates();
                if (null == recurrenceIds) {
                    return null;
                }
                List<String> value = new ArrayList<String>(recurrenceIds.size());
                for (RecurrenceId recurrenceId : recurrenceIds) {
                    value.add(recurrenceId.getValue().toString());
                }
                return value;
            }

            @Override
            public void remove(Event object) {
                object.removeDeleteExceptionDates();
            }

            @Override
            protected String deserialize(JSONArray array, int index, TimeZone timeZone) throws JSONException, OXException {
                return array.getString(index);
            }
        });
        mappings.put(EventField.STATUS, new StringMapping<Event>(ChronosJsonFields.STATUS, null) {

            @Override
            public boolean isSet(Event object) {
                return object.containsStatus();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setStatus(null == value ? null : new EventStatus(value));
            }

            @Override
            public String get(Event object) {
                EventStatus status = object.getStatus();
                return null == status ? null : status.getValue();
            }

            @Override
            public void remove(Event object) {
                object.removeStatus();
            }
        });
        mappings.put(EventField.URL, new StringMapping<Event>(ChronosJsonFields.URL, null) {

            @Override
            public boolean isSet(Event object) {
                return object.containsUrl();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setUrl(value);
            }

            @Override
            public String get(Event object) {
                return object.getUrl();
            }

            @Override
            public void remove(Event object) {
                object.removeUrl();
            }
        });
        mappings.put(EventField.ORGANIZER, new CalendarUserMapping<Organizer, Event>(ChronosJsonFields.ORGANIZER, null) {

            @Override
            public Organizer newInstance() {
                return new Organizer();
            }

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
        mappings.put(EventField.GEO, new DefaultJsonMapping<double[], Event>(ChronosJsonFields.GEO, null) {

            @Override
            public boolean isSet(Event object) {
                return object.containsGeo();
            }

            @Override
            public void set(Event object, double[] value) throws OXException {
                object.setGeo(value);
            }

            @Override
            public double[] get(Event object) {
                return object.getGeo();
            }

            @Override
            public void remove(Event object) {
                object.removeGeo();
            }

            @Override
            public void deserialize(JSONObject from, Event to) throws JSONException, OXException {
                JSONObject geo = (JSONObject) from.get(ChronosJsonFields.GEO);
                double[] geoLocation = new double[2];
                geoLocation[0] = geo.getDouble(ChronosJsonFields.Geo.LATITUDE);
                geoLocation[1] = geo.getDouble(ChronosJsonFields.Geo.LONGITUDE);
                set(to, geoLocation);
            }

            @Override
            public Object serialize(Event from, TimeZone timeZone, Session session) throws JSONException {
                if (from.getGeo() == null || from.getGeo().length != 2) {
                    return null;
                }
                JSONObject geoLocationJson = new JSONObject(2);
                geoLocationJson.put(ChronosJsonFields.Geo.LATITUDE, from.getGeo()[0]);
                geoLocationJson.put(ChronosJsonFields.Geo.LONGITUDE, from.getGeo()[1]);
                return geoLocationJson;
            }
        });
        mappings.put(EventField.ATTENDEE_PRIVILEGES, new StringMapping<Event>(ChronosJsonFields.ATTENDEE_PRIVILEGES, null) {

            @Override
            public boolean isSet(Event object) {
                return object.containsAttendeePrivileges();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setAttendeePrivileges(DefaultAttendeePrivileges.MODIFY.getValue().equalsIgnoreCase(value) ? DefaultAttendeePrivileges.MODIFY : DefaultAttendeePrivileges.DEFAULT);
            }

            @Override
            public String get(Event object) {
                return object.getAttendeePrivileges().getValue();
            }

            @Override
            public void remove(Event object) {
                object.removeAttendeePrivileges();
            }
        });
        mappings.put(EventField.ATTENDEES, new AttendeesMapping<Event>(ChronosJsonFields.ATTENDEES, null) {

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
        mappings.put(EventField.ATTACHMENTS, new AttachmentsMapping<Event>(ChronosJsonFields.ATTACHMENTS, null) {

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
        mappings.put(EventField.ALARMS, new ListMapping<Alarm, Event>(ChronosJsonFields.ALARMS, null) {

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

            @Override
            public Object serialize(Event from, TimeZone timeZone, Session session) throws JSONException {
                List<Alarm> value = get(from);
                if (null == value) {
                    return null;
                }
                JSONArray jsonArray = new JSONArray(value.size());
                try {
                    for (Alarm alarm : value) {
                        jsonArray.put(AlarmMapper.getInstance().serialize(alarm, AlarmMapper.getInstance().getAssignedFields(alarm), timeZone, session));
                    }
                } catch (OXException e) {
                    throw new JSONException(e);
                }
                return jsonArray;
            }

            @Override
            protected Alarm deserialize(JSONArray array, int index, TimeZone timeZone) throws JSONException, OXException {
                JSONObject jsonObject = array.getJSONObject(index);
                if (null == jsonObject) {
                    return null;
                }
                return AlarmMapper.getInstance().deserialize(jsonObject, AlarmMapper.getInstance().getMappedFields(), timeZone);
            }
        });
        mappings.put(EventField.EXTENDED_PROPERTIES, new ExtendedPropertiesMapping<Event>(ChronosJsonFields.EXTENDED_PROPERTIES, null) {

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
                object.removeExtendedProperties();
            }
        });
        mappings.put(EventField.FLAGS, new DefaultJsonMapping<EnumSet<EventFlag>, Event>(ChronosJsonFields.FLAGS, null) {

            @Override
            public boolean isSet(Event object) {
                return object.containsFlags();
            }

            @Override
            public void set(Event object, EnumSet<EventFlag> value) throws OXException {
                object.setFlags(value);
            }

            @Override
            public EnumSet<EventFlag> get(Event object) {
                return object.getFlags();
            }

            @Override
            public void remove(Event object) {
                object.removeFlags();
            }

            @Override
            public void deserialize(JSONObject from, Event to) throws JSONException, OXException {
                JSONArray jsonArray = from.optJSONArray(getAjaxName());
                if (null == jsonArray) {
                    set(to, null);
                } else {
                    EnumSet<EventFlag> flags = EnumSet.noneOf(EventFlag.class);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            flags.add(Enums.parse(EventFlag.class, jsonArray.getString(i)));
                        } catch (IllegalArgumentException e) {
                            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create(e);
                        }
                    }
                    set(to, flags);
                }
            }

            @Override
            public Object serialize(Event from, TimeZone timeZone, Session session) throws JSONException {
                EnumSet<EventFlag> flags = from.getFlags();
                if (null == flags) {
                    return JSONObject.NULL;
                }
                JSONArray jsonArray = new JSONArray(flags.size());
                for (EventFlag flag : flags) {
                    jsonArray.put(flag.name().toLowerCase());
                }
                return jsonArray;
            }
        });
        return mappings;
    }

    static <T extends CalendarUser> T deserializeCalendarUser(JSONObject jsonObject, Class<T> calendarUserClass) throws JSONException {
        if (null == jsonObject) {
            return null;
        }
        T calendarUser;
        try {
            calendarUser = calendarUserClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new JSONException(e);
        }
        if (jsonObject.has(ChronosJsonFields.CalendarUser.URI)) {
            calendarUser.setUri(jsonObject.optString(ChronosJsonFields.CalendarUser.URI, null));
        }
        if (jsonObject.has(ChronosJsonFields.CalendarUser.CN)) {
            calendarUser.setCn(jsonObject.optString(ChronosJsonFields.CalendarUser.CN, null));
        }
        if (jsonObject.has(ChronosJsonFields.CalendarUser.EMAIL)) {
            calendarUser.setEMail(jsonObject.optString(ChronosJsonFields.CalendarUser.EMAIL, null));
        }
        if (jsonObject.has(ChronosJsonFields.CalendarUser.SENT_BY)) {
            calendarUser.setSentBy(deserializeCalendarUser(jsonObject.optJSONObject(ChronosJsonFields.CalendarUser.SENT_BY), CalendarUser.class));
        }
        if (jsonObject.has(ChronosJsonFields.CalendarUser.ENTITY)) {
            calendarUser.setEntity(jsonObject.getInt(ChronosJsonFields.CalendarUser.ENTITY));
        }

        return calendarUser;
    }

    public static JSONObject serializeCalendarUser(CalendarUser calendarUser) throws JSONException {
        if (null == calendarUser) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt(ChronosJsonFields.CalendarUser.URI, calendarUser.getUri());
        jsonObject.putOpt(ChronosJsonFields.CalendarUser.CN, calendarUser.getCn());
        jsonObject.putOpt(ChronosJsonFields.CalendarUser.EMAIL, calendarUser.getEMail());
        jsonObject.putOpt(ChronosJsonFields.CalendarUser.SENT_BY, serializeCalendarUser(calendarUser.getSentBy()));
        if (0 < calendarUser.getEntity()) {
            jsonObject.put(ChronosJsonFields.CalendarUser.ENTITY, calendarUser.getEntity());
        }
        return jsonObject;
    }

}
