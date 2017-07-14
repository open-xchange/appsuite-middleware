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

package com.openexchange.chronos.json.converter;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.java.Autoboxing.l;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import org.dmfs.rfc5545.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventStatus;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ParticipantRole;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.Trigger.Related;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.json.osgi.ChronosJsonActivator;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.json.DateTimeMapping;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper;
import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapping;
import com.openexchange.groupware.tools.mappings.json.IntegerMapping;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.groupware.tools.mappings.json.ListMapping;
import com.openexchange.groupware.tools.mappings.json.LongMapping;
import com.openexchange.groupware.tools.mappings.json.StringMapping;
import com.openexchange.groupware.tools.mappings.json.TimeMapping;
import com.openexchange.java.Enums;
import com.openexchange.session.Session;

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

    public EventField[] getMappedFields() {
        return mappedFields;
    }

    public EventField[] getAssignedFields(Event event, EventField... mandatoryFields) {
        if (null == event) {
            throw new IllegalArgumentException("event");
        }
        Set<EventField> setFields = new HashSet<EventField>();
        for (Entry<EventField, ? extends JsonMapping<? extends Object, Event>> entry : getMappings().entrySet()) {
            JsonMapping<? extends Object, Event> mapping = entry.getValue();
            if (mapping.isSet(event)) {
                EventField field = entry.getKey();
                setFields.add(field);
            }
        }
        if (null != mandatoryFields) {
            setFields.addAll(Arrays.asList(mandatoryFields));
        }
        return setFields.toArray(newArray(setFields.size()));
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
        mappings.put(EventField.ID, new StringMapping<Event>(ChronosJsonFields.ID, ColumnIDs.ID) {

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
        mappings.put(EventField.FOLDER_ID, new StringMapping<Event>(ChronosJsonFields.FOLDER, ColumnIDs.FOLDER_ID) {

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
        mappings.put(EventField.UID, new StringMapping<Event>(ChronosJsonFields.UID, ColumnIDs.UID) {

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
        mappings.put(EventField.FILENAME, new StringMapping<Event>(ChronosJsonFields.FILENAME, ColumnIDs.FILENAME) {

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
        mappings.put(EventField.SEQUENCE, new IntegerMapping<Event>(ChronosJsonFields.SEQUENCE, ColumnIDs.SEQUENCE) {

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
        mappings.put(EventField.CREATED, new TimeMapping<Event>(ChronosJsonFields.CREATED, ColumnIDs.CREATED) {

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
                object.removeCreated();
            }
        });
        mappings.put(EventField.CREATED_BY, new IntegerMapping<Event>(ChronosJsonFields.CREATED_BY, ColumnIDs.CREATED_BY) {

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

            @Override
            public Object serialize(Event from, TimeZone timeZone, Session session) throws JSONException, OXException {

                CalendarUser user = new CalendarUser();
                Integer id = get(from);
                CalendarUtilities service = ChronosJsonActivator.getServiceLookup().getService(CalendarUtilities.class);
                service.getEntityResolver(session.getContextId()).applyEntityData(user, id);
                return serializeCalendarUser(user);
            }

            @Override
            public void deserialize(JSONObject from, Event to, TimeZone timeZone) throws JSONException, OXException {
                JSONObject organizer = (JSONObject) from.get(ChronosJsonFields.CREATED_BY);
                Organizer deserializeCalendarUser = deserializeCalendarUser(organizer, Organizer.class);
                if(deserializeCalendarUser.getUri()==null && from.has(getAjaxName())){
                    super.deserialize(from, to);
                }
                set(to, deserializeCalendarUser.getEntity());
            }
        });
        mappings.put(EventField.LAST_MODIFIED, new TimeMapping<Event>(ChronosJsonFields.LAST_MODIFIED, ColumnIDs.LAST_MODIFIED) {

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
        mappings.put(EventField.MODIFIED_BY, new IntegerMapping<Event>(ChronosJsonFields.MODIFIED_BY, ColumnIDs.MODIFIED_BY) {

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
        mappings.put(EventField.CALENDAR_USER, new IntegerMapping<Event>(ChronosJsonFields.CALENDAR_USER, ColumnIDs.CALENDAR_USER) {

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
        mappings.put(EventField.SUMMARY, new StringMapping<Event>(ChronosJsonFields.SUMMARY, ColumnIDs.SUMMARY) {

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
        mappings.put(EventField.LOCATION, new StringMapping<Event>(ChronosJsonFields.LOCATION, ColumnIDs.LOCATION) {

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
        mappings.put(EventField.DESCRIPTION, new StringMapping<Event>(ChronosJsonFields.DESCRIPTION, ColumnIDs.DESCRIPTION) {

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
        mappings.put(EventField.CATEGORIES, new ListMapping<String, Event>(ChronosJsonFields.CATEGORIES, ColumnIDs.CATEGORIES) {

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
        mappings.put(EventField.CLASSIFICATION, new StringMapping<Event>(ChronosJsonFields.CLASSIFICATION, ColumnIDs.CLASSIFICATION) {

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
        mappings.put(EventField.COLOR, new StringMapping<Event>(ChronosJsonFields.COLOR, ColumnIDs.COLOR) {

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
        mappings.put(EventField.START_DATE, new DateTimeMapping<Event>(ChronosJsonFields.START_DATE, ColumnIDs.START_DATE) {

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
        mappings.put(EventField.END_DATE, new DateTimeMapping<Event>(ChronosJsonFields.END_DATE, ColumnIDs.END_DATE) {

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
        mappings.put(EventField.TRANSP, new StringMapping<Event>(ChronosJsonFields.TRANSP, ColumnIDs.TRANSP) {

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
                return object.getTransp().getValue();
            }

            @Override
            public void remove(Event object) {
                object.removeTransp();
            }
        });
        mappings.put(EventField.SERIES_ID, new StringMapping<Event>(ChronosJsonFields.SERIES_ID, ColumnIDs.SERIES_ID) {

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
        mappings.put(EventField.RECURRENCE_RULE, new StringMapping<Event>(ChronosJsonFields.RECURRENCE_RULE, ColumnIDs.RECURRENCE_RULE) {

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
        mappings.put(EventField.RECURRENCE_ID, new LongMapping<Event>(ChronosJsonFields.RECURRENCE_ID, ColumnIDs.RECURRENCE_ID) {

            @Override
            public boolean isSet(Event object) {
                return object.containsRecurrenceId();
            }

            @Override
            public void set(Event object, Long value) throws OXException {
                object.setRecurrenceId(null == value ? null : new DefaultRecurrenceId(l(value)));
            }

            @Override
            public Long get(Event object) {
                RecurrenceId value = object.getRecurrenceId();
                return null == value ? null : L(value.getValue());
            }

            @Override
            public void remove(Event object) {
                object.removeRecurrenceId();
            }
        });
        mappings.put(EventField.CHANGE_EXCEPTION_DATES, new ListMapping<Long, Event>(ChronosJsonFields.CHANGE_EXCEPTION_DATES, ColumnIDs.CHANGE_EXCEPTION_DATES) {

            @Override
            public boolean isSet(Event object) {
                return object.containsChangeExceptionDates();
            }

            @Override
            public void set(Event object, List<Long> value) throws OXException {
                if (null == value) {
                    object.setChangeExceptionDates(null);
                } else {
                    SortedSet<RecurrenceId> recurrenceIds = new TreeSet<RecurrenceId>();
                    for (Long timestamp : value) {
                        recurrenceIds.add(new DefaultRecurrenceId(timestamp));
                    }
                    object.setChangeExceptionDates(recurrenceIds);
                }
            }

            @Override
            public List<Long> get(Event object) {
                SortedSet<RecurrenceId> recurrenceIds = object.getChangeExceptionDates();
                if (null == recurrenceIds) {
                    return null;
                }
                List<Long> value = new ArrayList<Long>(recurrenceIds.size());
                for (RecurrenceId recurrenceId : recurrenceIds) {
                    value.add(L(recurrenceId.getValue()));
                }
                return value;
            }

            @Override
            public void remove(Event object) {
                object.removeChangeExceptionDates();
            }

            @Override
            protected Long deserialize(JSONArray array, int index, TimeZone timeZone) throws JSONException, OXException {
                return L(array.getLong(index));
            }
        });
        mappings.put(EventField.DELETE_EXCEPTION_DATES, new ListMapping<Long, Event>(ChronosJsonFields.DELETE_EXCEPTION_DATES, ColumnIDs.DELETE_EXCEPTION_DATES) {

            @Override
            public boolean isSet(Event object) {
                return object.containsDeleteExceptionDates();
            }

            @Override
            public void set(Event object, List<Long> value) throws OXException {
                if (null == value) {
                    object.setDeleteExceptionDates(null);
                } else {
                    SortedSet<RecurrenceId> recurrenceIds = new TreeSet<RecurrenceId>();
                    for (Long timestamp : value) {
                        recurrenceIds.add(new DefaultRecurrenceId(timestamp));
                    }
                    object.setDeleteExceptionDates(recurrenceIds);
                }
            }

            @Override
            public List<Long> get(Event object) {
                SortedSet<RecurrenceId> recurrenceIds = object.getDeleteExceptionDates();
                if (null == recurrenceIds) {
                    return null;
                }
                List<Long> value = new ArrayList<Long>(recurrenceIds.size());
                for (RecurrenceId recurrenceId : recurrenceIds) {
                    value.add(L(recurrenceId.getValue()));
                }
                return value;
            }

            @Override
            public void remove(Event object) {
                object.removeDeleteExceptionDates();
            }

            @Override
            protected Long deserialize(JSONArray array, int index, TimeZone timeZone) throws JSONException, OXException {
                return L(array.getLong(index));
            }
        });
        mappings.put(EventField.STATUS, new StringMapping<Event>(ChronosJsonFields.STATUS, ColumnIDs.STATUS) {

            @Override
            public boolean isSet(Event object) {
                return object.containsStatus();
            }

            @Override
            public void set(Event object, String value) throws OXException {
                object.setStatus(Enums.parse(EventStatus.class, value));
            }

            @Override
            public String get(Event object) {
                EventStatus status = object.getStatus();
                return null == status ? null : status.name();
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
        mappings.put(EventField.ORGANIZER, new DefaultJsonMapping<Organizer, Event>(ChronosJsonFields.ORGANIZER, ColumnIDs.ORGANIZER) {

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

            @Override
            public void deserialize(JSONObject from, Event to) throws JSONException, OXException {
                JSONObject organizer = (JSONObject) from.get(ChronosJsonFields.ORGANIZER);
                set(to, deserializeCalendarUser(organizer, Organizer.class));
            }

            @Override
            public Object serialize(Event from, TimeZone timeZone, Session session) throws JSONException {
                return serializeCalendarUser(from.getOrganizer());
            }
        });
        mappings.put(EventField.GEO, new DefaultJsonMapping<double[], Event>(ChronosJsonFields.GEO, null) {

            @Override
            public boolean isSet(Event object) {
                return object.containsOrganizer();
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
                object.removeOrganizer();
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
        mappings.put(EventField.ATTENDEES, new ListItemMapping<Attendee, Event, JSONObject>(ChronosJsonFields.ATTENDEES, ColumnIDs.ATTENDEES) {

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

            @Override
            protected Attendee deserialize(JSONArray array, int index, TimeZone timeZone) throws JSONException, OXException {
                JSONObject jsonObject = array.getJSONObject(index);
                return deserialize(jsonObject, timeZone);
            }

            @Override
            public Object serialize(Event from, TimeZone timeZone, Session session) throws JSONException {
                List<Attendee> value = get(from);
                if (null == value) {
                    return null;
                }
                JSONArray jsonArray = new JSONArray(value.size());
                for (Attendee attendee : value) {

                    jsonArray.put(serialize(attendee, timeZone));
                }
                return jsonArray;
            }

            @Override
            public Attendee deserialize(JSONObject from, TimeZone timeZone) throws JSONException {
                Attendee attendee = deserializeCalendarUser(from, Attendee.class);
                if (from.has(ChronosJsonFields.Attendee.CU_TYPE)) {
                    attendee.setCuType(new CalendarUserType(from.getString(ChronosJsonFields.Attendee.CU_TYPE)));
                }
                if (from.has(ChronosJsonFields.Attendee.ROLE)) {
                    attendee.setRole(new ParticipantRole(from.getString(ChronosJsonFields.Attendee.ROLE)));
                }
                if (from.has(ChronosJsonFields.Attendee.PARTICIPATION_STATUS)) {
                    attendee.setPartStat(new ParticipationStatus(from.getString(ChronosJsonFields.Attendee.PARTICIPATION_STATUS)));
                }
                if (from.has(ChronosJsonFields.Attendee.COMMENT)) {
                    attendee.setComment(from.getString(ChronosJsonFields.Attendee.COMMENT));
                }
                if (from.has(ChronosJsonFields.Attendee.RSVP)) {
                    attendee.setRsvp(from.getBoolean(ChronosJsonFields.Attendee.RSVP));
                }
                if (from.has(ChronosJsonFields.Attendee.FOLDER)) {
                    attendee.setFolderID(from.getString(ChronosJsonFields.Attendee.FOLDER));
                }
                if (from.has(ChronosJsonFields.Attendee.MEMBER)) {
                    JSONArray array = from.getJSONArray(ChronosJsonFields.Attendee.MEMBER);
                    List<String> list = new ArrayList<>(array.length());
                    for(Object o: array.asList()){
                        list.add(o.toString());
                    }
                    attendee.setMember(list);
                }

                return attendee;
            }

            @Override
            public JSONObject serialize(Attendee from, TimeZone timeZone) throws JSONException {
                JSONObject jsonObject = serializeCalendarUser(from);
                if (null != from.getCuType()) {
                    jsonObject.put(ChronosJsonFields.Attendee.CU_TYPE, from.getCuType().getValue());
                }
                if (null != from.getRole()) {
                    jsonObject.put(ChronosJsonFields.Attendee.ROLE, from.getRole().getValue());
                }
                if (null != from.getPartStat()) {
                    jsonObject.put(ChronosJsonFields.Attendee.PARTICIPATION_STATUS, from.getPartStat().getValue());
                }
                if (null != from.getComment()) {
                    jsonObject.put(ChronosJsonFields.Attendee.COMMENT, from.getComment());
                }
                if (null != from.getRsvp()) {
                    jsonObject.put(ChronosJsonFields.Attendee.RSVP, from.getRsvp());
                }
                if (null != from.getFolderID()) {
                    jsonObject.put(ChronosJsonFields.Attendee.FOLDER, from.getFolderID());
                }
                if (null != from.getMember()) {
                    jsonObject.put(ChronosJsonFields.Attendee.MEMBER, from.getMember());
                }
                return jsonObject;
            }
        });
        mappings.put(EventField.ATTACHMENTS, new ListItemMapping<Attachment, Event, JSONObject>(ChronosJsonFields.ATTACHMENTS, ColumnIDs.ATTACHMENTS) {

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

            @Override
            protected Attachment deserialize(JSONArray array, int index, TimeZone timeZone) throws JSONException, OXException {
                JSONObject jsonObject = array.getJSONObject(index);
                return deserialize(jsonObject, timeZone);

            }

            @Override
            public Object serialize(Event from, TimeZone timeZone, Session session) throws JSONException {
                List<Attachment> value = get(from);
                if (null == value) {
                    return null;
                }
                JSONArray jsonArray = new JSONArray(value.size());
                for (Attachment attachment : value) {

                    jsonArray.put(serialize(attachment, timeZone));
                }
                return jsonArray;
            }

            @Override
            public Attachment deserialize(JSONObject from, TimeZone timeZone) throws JSONException {
                Attachment attachment = new Attachment();

                if (from.has(ChronosJsonFields.Attachment.FILENAME)) {
                    attachment.setFilename(from.getString(ChronosJsonFields.Attachment.FILENAME));
                }
                if (from.has(ChronosJsonFields.Attachment.FORMAT_TYPE)) {
                    attachment.setFormatType(from.getString(ChronosJsonFields.Attachment.FORMAT_TYPE));
                }
                if (from.has(ChronosJsonFields.Attachment.SIZE)) {
                    attachment.setSize(from.getLong(ChronosJsonFields.Attachment.SIZE));
                }
                if (from.has(ChronosJsonFields.Attachment.CREATED)) {
                    long date = from.getLong(ChronosJsonFields.Attachment.CREATED);
                    date -= timeZone.getOffset(date);
                    attachment.setCreated(new Date(date));
                }
                if (from.has(ChronosJsonFields.Attachment.MANAGED_ID)) {
                    attachment.setManagedId(from.getInt(ChronosJsonFields.Attachment.MANAGED_ID));
                }
                return attachment;
            }

            @Override
            public JSONObject serialize(Attachment attachment, TimeZone timeZone) throws JSONException {
                JSONObject jsonObject = new JSONObject();
                if (null != attachment.getFilename()) {
                    jsonObject.put(ChronosJsonFields.Attachment.FILENAME, attachment.getFilename());
                }
                if (null != attachment.getFormatType()) {
                    jsonObject.put(ChronosJsonFields.Attachment.FORMAT_TYPE, attachment.getFormatType());
                }
                if (0 < attachment.getSize()) {
                    jsonObject.put(ChronosJsonFields.Attachment.SIZE, attachment.getSize());
                }
                if (null != attachment.getCreated()) {
                    long date = attachment.getCreated().getTime();
                    date += timeZone.getOffset(date);
                    jsonObject.put(ChronosJsonFields.Attachment.CREATED, date);
                }
                if (0 < attachment.getManagedId()) {
                    jsonObject.put(ChronosJsonFields.Attachment.MANAGED_ID, attachment.getManagedId());
                }
                return jsonObject;
            }
        });
        mappings.put(EventField.ALARMS, new DefaultJsonMapping<List<Alarm>, Event>(ChronosJsonFields.ALARMS, ColumnIDs.ALARMS) {

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

            @SuppressWarnings("unchecked")
            @Override
            public void deserialize(JSONObject from, Event to, TimeZone timezone) throws JSONException, OXException {

                JSONArray arrayOfAlarms = from.getJSONArray(getAjaxName());
                List<Alarm> alarms = new ArrayList<>(arrayOfAlarms.length());
                for (int index = 0; index < arrayOfAlarms.length(); index++) {

                    JSONObject jsonObject = arrayOfAlarms.getJSONObject(index);
                    Alarm alarm = new Alarm();
                    if (jsonObject.has(ChronosJsonFields.Alarm.ACTION)) {
                        String action = jsonObject.getString(ChronosJsonFields.Alarm.ACTION);
                        alarm.setAction(new AlarmAction(action));
                    }

                    if (jsonObject.has(ChronosJsonFields.Alarm.TRIGGER)) {
                        JSONObject triggerJSON = jsonObject.getJSONObject(ChronosJsonFields.Alarm.TRIGGER);
                        Trigger trigger = new Trigger();
                        if (triggerJSON.has(ChronosJsonFields.Alarm.Trigger.RELATED)) {
                            String related = triggerJSON.getString(ChronosJsonFields.Alarm.Trigger.RELATED);
                            trigger.setRelated(Related.valueOf(related.toUpperCase()));
                        }
                        if (triggerJSON.has(ChronosJsonFields.Alarm.Trigger.DURATION)) {
                            String duration = triggerJSON.getString(ChronosJsonFields.Alarm.Trigger.DURATION);
                            trigger.setDuration(duration);
                        }
                        if (triggerJSON.has(ChronosJsonFields.Alarm.Trigger.DATE_TIME)) {
                            long date = triggerJSON.getLong(ChronosJsonFields.Alarm.Trigger.DATE_TIME);
                            date -= timezone.getOffset(date);
                            trigger.setDateTime(new Date(date));
                        }
                        alarm.setTrigger(trigger);
                    }

                    if (jsonObject.has(ChronosJsonFields.Alarm.ATTACHMENTS)) {
                        JSONArray arrayOfAttachments = jsonObject.getJSONArray(ChronosJsonFields.Alarm.ATTACHMENTS);
                        List<Attachment> attachments = new ArrayList<>(arrayOfAttachments.length());
                        for (int x = 0; x < arrayOfAttachments.length(); x++) {
                            JSONObject attachmentJSON = arrayOfAttachments.getJSONObject(x);
                            Attachment attach = ((ListItemMapping<Attachment, Event, JSONObject>) EventMapper.getInstance().getMappings().get(EventField.ATTACHMENTS)).deserialize(attachmentJSON, timezone);
                            attachments.add(attach);
                        }
                        if (!attachments.isEmpty()) {
                            alarm.setAttachments(attachments);
                        }
                    }

                    if (jsonObject.has(ChronosJsonFields.Alarm.ATTENDEES)) {
                        JSONArray arrayOfAttendees = jsonObject.getJSONArray(ChronosJsonFields.Alarm.ATTENDEES);
                        List<Attendee> attendees = new ArrayList<>(arrayOfAttendees.length());
                        for (int x = 0; x < arrayOfAttendees.length(); x++) {
                            JSONObject attendeeJSON = arrayOfAttendees.getJSONObject(x);
                            Attendee attendee = ((ListItemMapping<Attendee, Event, JSONObject>) EventMapper.getInstance().getMappings().get(EventField.ATTENDEES)).deserialize(attendeeJSON, timezone);
                            attendees.add(attendee);
                        }
                        if (!attendees.isEmpty()) {
                            alarm.setAttendees(attendees);
                        }
                    }

                    if (jsonObject.has(ChronosJsonFields.Alarm.SUMMARY)) {
                        String summary = jsonObject.getString(ChronosJsonFields.Alarm.SUMMARY);
                        alarm.setSummary(summary);
                    }

                    if (jsonObject.has(ChronosJsonFields.Alarm.DESCRIPTION)) {
                        String description = jsonObject.getString(ChronosJsonFields.Alarm.DESCRIPTION);
                        alarm.setDescription(description);
                    }

                    if (jsonObject.has(ChronosJsonFields.Alarm.EXTENDED_PROPERTIES)) {
                        ExtendedProperties deserializeExtendedProperties = deserializeExtendedProperties(jsonObject.getJSONArray(ChronosJsonFields.Alarm.EXTENDED_PROPERTIES));
                        if (deserializeExtendedProperties != null) {
                            alarm.setExtendedProperties(deserializeExtendedProperties);
                        }
                    }

                    alarms.add(alarm);
                }

                to.setAlarms(alarms);
            }

            @SuppressWarnings("unchecked")
            @Override
            public Object serialize(Event from, TimeZone timeZone, Session session) throws JSONException {
                List<Alarm> value = get(from);
                if (null == value) {
                    return null;
                }
                JSONArray jsonArray = new JSONArray(value.size());
                for (Alarm alarm : value) {
                    JSONObject jsonObject = new JSONObject();
                    if (0 < alarm.getId()) {
                        jsonObject.put(ChronosJsonFields.Alarm.ID, alarm.getId());
                    }
                    if (null != alarm.getUid()) {
                        jsonObject.put(ChronosJsonFields.Alarm.UID, alarm.getUid());
                    }
                    if (null != alarm.getAction()) {
                        jsonObject.put(ChronosJsonFields.Alarm.ACTION, alarm.getAction().getValue());
                    }
                    if (null != alarm.getAcknowledged()) {
                        long date = alarm.getAcknowledged().getTime();
                        date += timeZone.getOffset(date);
                        jsonObject.put(ChronosJsonFields.Alarm.ACK, date);
                    }
                    if (null != alarm.getTrigger()) {
                        Trigger trigger = alarm.getTrigger();
                        JSONObject triggerJsonObject = new JSONObject();
                        if (null != trigger.getRelated()) {
                            triggerJsonObject.put(ChronosJsonFields.Alarm.Trigger.RELATED, trigger.getRelated().name());
                        }
                        triggerJsonObject.putOpt(ChronosJsonFields.Alarm.Trigger.DURATION, trigger.getDuration());
                        if (null != trigger.getDateTime()) {
                            long date = trigger.getDateTime().getTime();
                            date += timeZone.getOffset(date);
                            jsonObject.put(ChronosJsonFields.Alarm.Trigger.DATE_TIME, date);
                        }
                        jsonObject.put(ChronosJsonFields.Alarm.TRIGGER, triggerJsonObject);
                    }

                    if (null != alarm.getAttachments()) {

                        List<Attachment> attachments = alarm.getAttachments();
                        JSONArray attachmentArray = new JSONArray(attachments.size());
                        for(Attachment attachment: attachments){
                            attachmentArray.put(((ListItemMapping<Attachment, Event, JSONObject>) EventMapper.getInstance().getMappings().get(EventField.ATTACHMENTS)).serialize(attachment, timeZone));
                        }
                        jsonObject.put(ChronosJsonFields.Alarm.ATTACHMENTS, attachmentArray);
                    }

                    if (null != alarm.getAttendees()) {

                        List<Attendee> attendees = alarm.getAttendees();
                        JSONArray attendessArray = new JSONArray(attendees.size());
                        for(Attendee attendee: attendees){
                            attendessArray.put(((ListItemMapping<Attendee, Event, JSONObject>) EventMapper.getInstance().getMappings().get(EventField.ATTENDEES)).serialize(attendee, timeZone));
                        }
                        jsonObject.put(ChronosJsonFields.Alarm.ATTENDEES, attendessArray);
                    }

                    if (null != alarm.getSummary()) {
                        jsonObject.put(ChronosJsonFields.Alarm.SUMMARY, alarm.getSummary());
                    }

                    if (null != alarm.getDescription()) {
                        jsonObject.put(ChronosJsonFields.Alarm.DESCRIPTION, alarm.getDescription());
                    }

                    if (null != alarm.getExtendedProperties()) {
                        jsonObject.put(ChronosJsonFields.Alarm.EXTENDED_PROPERTIES, serializeExtendedProperties(alarm.getExtendedProperties()));
                    }
                    jsonArray.put(jsonObject);
                }
                return jsonArray;
            }

            @Override
            public void deserialize(JSONObject from, Event to) throws JSONException, OXException {
                this.deserialize(from, to, TimeZone.getTimeZone("UTC"));

            }
        });
        mappings.put(EventField.EXTENDED_PROPERTIES, new DefaultJsonMapping<ExtendedProperties, Event>(ChronosJsonFields.EXTENDED_PROPERTIES, ColumnIDs.EXTENDED_PROPERTIES) {

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

            @Override
            public void deserialize(JSONObject from, Event to) throws JSONException, OXException {
                if (from.has(getAjaxName())) {
                    set(to, deserializeExtendedProperties(from.getJSONArray(getAjaxName())));
                }
            }

            @Override
            public Object serialize(Event from, TimeZone timeZone, Session session) throws JSONException {
                return serializeExtendedProperties(from.getExtendedProperties());
            }
        });

        return mappings;
    }

    <T extends CalendarUser> T deserializeCalendarUser(JSONObject jsonObject, Class<T> calendarUserClass) throws JSONException {
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

    static JSONObject serializeCalendarUser(CalendarUser calendarUser) throws JSONException {
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

    static ExtendedProperties deserializeExtendedProperties(JSONArray jsonArray) throws JSONException {
        if (null == jsonArray) {
            return null;
        }
        ExtendedProperties extendedProperties = new ExtendedProperties();
        for (int x = 0; x < jsonArray.length(); x++) {
            extendedProperties.add(deserializeExtendedProperty(jsonArray.getJSONObject(x)));
        }
        return extendedProperties;
    }

    static JSONArray serializeExtendedProperties(ExtendedProperties extendedProperties) throws JSONException {
        if (null == extendedProperties) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(extendedProperties.size());
        for (ExtendedProperty extendedProperty : extendedProperties) {
            jsonArray.put(serializeExtendedProperty(extendedProperty));
        }
        return jsonArray;
    }

    private static JSONObject serializeExtendedProperty(ExtendedProperty extendedProperty) throws JSONException {
        if (null == extendedProperty) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ChronosJsonFields.ExtendedProperty.NAME, extendedProperty.getName());
        jsonObject.put(ChronosJsonFields.ExtendedProperty.VALUE, extendedProperty.getValue());
        List<ExtendedPropertyParameter> parameters = extendedProperty.getParameters();
        if (null == parameters || parameters.isEmpty()) {
            return jsonObject;
        }
        JSONArray jsonParameters = new JSONArray(parameters.size());
        for (int i = 0; i < parameters.size(); i++) {
            ExtendedPropertyParameter parameter = parameters.get(i);
            jsonParameters.add(i, new JSONObject().putOpt(ChronosJsonFields.ExtendedProperty.Parameter.NAME, parameter.getName()).putOpt(ChronosJsonFields.ExtendedProperty.Parameter.VALUE, parameter.getValue()));
        }
        jsonObject.put(ChronosJsonFields.ExtendedProperty.PARAMETERS, jsonParameters);
        return jsonObject;
    }

    private static ExtendedProperty deserializeExtendedProperty(JSONObject extendedProperty) throws JSONException {
        if (null == extendedProperty) {
            return null;
        }

        String name = extendedProperty.getString(ChronosJsonFields.ExtendedProperty.NAME);
        String value = extendedProperty.getString(ChronosJsonFields.ExtendedProperty.VALUE);
        List<ExtendedPropertyParameter> parameters = null;
        if (extendedProperty.has(ChronosJsonFields.ExtendedProperty.PARAMETERS)) {
            JSONArray array = extendedProperty.getJSONArray(ChronosJsonFields.ExtendedProperty.PARAMETERS);
            parameters = new ArrayList<>(array.length());
            for (int x = 0; x < extendedProperty.length(); x++) {
                JSONObject param = array.getJSONObject(x);
                String paraName = param.getString(ChronosJsonFields.ExtendedProperty.Parameter.NAME);
                String paraValue = param.getString(ChronosJsonFields.ExtendedProperty.Parameter.VALUE);
                if (paraName != null && !paraName.isEmpty() && paraValue != null && !paraValue.isEmpty()) {
                    parameters.add(new ExtendedPropertyParameter(name, value));
                }
            }
        }
        if (parameters != null && !parameters.isEmpty()) {
            return new ExtendedProperty(name, value, parameters);
        } else {
            return new ExtendedProperty(name, value);
        }
    }

}
