///*
// *
// *    OPEN-XCHANGE legal information
// *
// *    All intellectual property rights in the Software are protected by
// *    international copyright laws.
// *
// *
// *    In some countries OX, OX Open-Xchange, open xchange and OXtender
// *    as well as the corresponding Logos OX Open-Xchange and OX are registered
// *    trademarks of the OX Software GmbH. group of companies.
// *    The use of the Logos is not covered by the GNU General Public License.
// *    Instead, you are allowed to use these Logos according to the terms and
// *    conditions of the Creative Commons License, Version 2.5, Attribution,
// *    Non-commercial, ShareAlike, and the interpretation of the term
// *    Non-commercial applicable to the aforementioned license is published
// *    on the web site http://www.open-xchange.com/EN/legal/index.html.
// *
// *    Please make sure that third-party modules and libraries are used
// *    according to their respective licenses.
// *
// *    Any modifications to this package must retain all copyright notices
// *    of the original copyright holder(s) for the original code used.
// *
// *    After any such modifications, the original and derivative code shall remain
// *    under the copyright of the copyright holder(s) and/or original author(s)per
// *    the Attribution and Assignment Agreement that can be located at
// *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
// *    given Attribution for the derivative code and a license granting use.
// *
// *     Copyright (C) 2016-2020 OX Software GmbH
// *     Mail: info@open-xchange.com
// *
// *
// *     This program is free software; you can redistribute it and/or modify it
// *     under the terms of the GNU General Public License, Version 2 as published
// *     by the Free Software Foundation.
// *
// *     This program is distributed in the hope that it will be useful, but
// *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *     for more details.
// *
// *     You should have received a copy of the GNU General Public License along
// *     with this program; if not, write to the Free Software Foundation, Inc., 59
// *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
// *
// */
//
//package com.openexchange.calendar.json.converters;
//
//import static com.openexchange.java.Autoboxing.L;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Date;
//import java.util.EnumMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map.Entry;
//import java.util.Set;
//import java.util.TimeZone;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import com.openexchange.ajax.fields.ParticipantsFields;
//import com.openexchange.calendar.CalendarField;
//import com.openexchange.chronos.Attendee;
//import com.openexchange.chronos.CalendarUserType;
//import com.openexchange.chronos.Event;
//import com.openexchange.chronos.Organizer;
//import com.openexchange.chronos.UserizedEvent;
//import com.openexchange.chronos.compat.Appointment2Event;
//import com.openexchange.chronos.compat.Event2Appointment;
//import com.openexchange.chronos.compat.SeriesPattern;
//import com.openexchange.exception.OXException;
//import com.openexchange.groupware.container.Participant;
//import com.openexchange.groupware.tools.mappings.json.ArrayMapping;
//import com.openexchange.groupware.tools.mappings.json.BooleanMapping;
//import com.openexchange.groupware.tools.mappings.json.DateMapping;
//import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapper;
//import com.openexchange.groupware.tools.mappings.json.DefaultJsonMapping;
//import com.openexchange.groupware.tools.mappings.json.IntegerMapping;
//import com.openexchange.groupware.tools.mappings.json.JsonMapping;
//import com.openexchange.groupware.tools.mappings.json.StringMapping;
//import com.openexchange.groupware.tools.mappings.json.TimeMapping;
//import com.openexchange.session.Session;
//
///**
// * {@link EventMapper}
// *
// * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
// * @since v7.10.0
// */
//public class EventMapper extends DefaultJsonMapper<UserizedEvent, CalendarField> {
//
//    private static final EventMapper INSTANCE = new EventMapper();
//
//    /**
//     * Gets the mapper instance.
//     *
//     * @return The mapper
//     */
//    public static EventMapper getInstance() {
//        return INSTANCE;
//    }
//
//    /**
//     * Initializes a new {@link EventMapper}.
//     */
//    private EventMapper() {
//        super();
//    }
//
//    public CalendarField[] getMappedFields() {
//        Set<CalendarField> fields = getMappings().keySet();
//        return fields.toArray(new CalendarField[fields.size()]);
//    }
//
//    public CalendarField[] getAssignedFields(UserizedEvent event, CalendarField... mandatoryFields) {
//        if (null == event) {
//            throw new IllegalArgumentException("event");
//        }
//        Set<CalendarField> setFields = new HashSet<>();
//        for (Entry<CalendarField, ? extends JsonMapping<? extends Object, UserizedEvent>> entry : getMappings().entrySet()) {
//            JsonMapping<? extends Object, UserizedEvent> mapping = entry.getValue();
//            if (mapping.isSet(event)) {
//                CalendarField field = entry.getKey();
//                setFields.add(field);
//                if (CalendarField.LAST_MODIFIED.equals(field)) {
//                    setFields.add(CalendarField.LAST_MODIFIED_UTC); // assume virtual LAST_MODIFIED_UTC is set, too
//                }
//            }
//        }
//        if (null != mandatoryFields) {
//            setFields.addAll(Arrays.asList(mandatoryFields));
//        }
//        return setFields.toArray(newArray(setFields.size()));
//    }
//
//    @Override
//    public UserizedEvent newInstance() {
//        return new UserizedEvent(new Event());
//    }
//
//    @Override
//    public CalendarField[] newArray(int size) {
//        return new CalendarField[size];
//    }
//
//    @Override
//    protected EnumMap<CalendarField, ? extends JsonMapping<? extends Object, UserizedEvent>> createMappings() {
//        EnumMap<CalendarField, JsonMapping<? extends Object, UserizedEvent>> mappings = new
//            EnumMap<>(CalendarField.class);
//        mappings.put(CalendarField.ID, new IntegerMapping<UserizedEvent>(CalendarField.ID.getJsonName(), CalendarField.ID.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return 0 < object.getId();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                object.setId(null != value ? value.intValue() : 0);
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                return Integer.valueOf(object.getId());
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setId(0);
//            }
//        });
//        mappings.put(CalendarField.CREATED_BY, new IntegerMapping<UserizedEvent>(CalendarField.CREATED_BY.getJsonName(), CalendarField.CREATED_BY.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return 0 < object.getCreatedBy();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                object.setCreatedBy(null != value ? value.intValue() : 0);
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                return Integer.valueOf(object.getCreatedBy());
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setCreatedBy(0);
//            }
//        });
//        mappings.put(CalendarField.MODIFIED_BY, new IntegerMapping<UserizedEvent>(CalendarField.MODIFIED_BY.getJsonName(), CalendarField.MODIFIED_BY.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return 0 < object.getModifiedBy();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                object.setModifiedBy(null != value ? value.intValue() : 0);
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                return Integer.valueOf(object.getModifiedBy());
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setModifiedBy(0);
//            }
//        });
//        mappings.put(CalendarField.CREATION_DATE, new TimeMapping<UserizedEvent>(CalendarField.CREATION_DATE.getJsonName(), CalendarField.CREATION_DATE.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getCreated();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Date value) throws OXException {
//                object.setCreated(value);
//            }
//
//            @Override
//            public Date get(UserizedEvent object) {
//                return object.getCreated();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setCreated(null);
//            }
//        });
//        mappings.put(CalendarField.LAST_MODIFIED, new TimeMapping<UserizedEvent>(CalendarField.LAST_MODIFIED.getJsonName(), CalendarField.LAST_MODIFIED.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getLastModified();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Date value) throws OXException {
//                object.setLastModified(value);
//            }
//
//            @Override
//            public Date get(UserizedEvent object) {
//                return object.getLastModified();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setLastModified(null);
//            }
//        });
//        mappings.put(CalendarField.LAST_MODIFIED_UTC, new DateMapping<UserizedEvent>(CalendarField.LAST_MODIFIED_UTC.getJsonName(), CalendarField.LAST_MODIFIED_UTC.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getLastModified();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Date value) throws OXException {
//                object.setLastModified(value);
//            }
//
//            @Override
//            public Date get(UserizedEvent object) {
//                return object.getLastModified();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setLastModified(null);
//            }
//        });
//        mappings.put(CalendarField.FOLDER_ID, new IntegerMapping<UserizedEvent>(CalendarField.FOLDER_ID.getJsonName(), CalendarField.FOLDER_ID.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return 0 < object.getFolderId();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                object.setFolderId(null != value ? value.intValue() : 0);
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                return Integer.valueOf(object.getFolderId());
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setFolderId(0);
//            }
//        });
//        mappings.put(CalendarField.CATEGORIES, new StringMapping<UserizedEvent>(CalendarField.CATEGORIES.getJsonName(), CalendarField.CATEGORIES.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getCategories();
//            }
//
//            @Override
//            public void set(UserizedEvent object, String value) throws OXException {
//                object.setCategories(Appointment2Event.getCategories(value));
//            }
//
//            @Override
//            public String get(UserizedEvent object) {
//                return Event2Appointment.getCategories(object.getCategories());
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setCategories(null);
//            }
//        });
//        mappings.put(CalendarField.IDPRIVATE_FLAG, new BooleanMapping<UserizedEvent>(CalendarField.IDPRIVATE_FLAG.getJsonName(), CalendarField.IDPRIVATE_FLAG.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getClassification();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Boolean value) throws OXException {
//                object.setClassification(null != value ? Appointment2Event.getClassification(value.booleanValue()) : null);
//            }
//
//            @Override
//            public Boolean get(UserizedEvent object) {
//                return null == object.getClassification() ? null : Event2Appointment.getPrivateFlag(object.getClassification());
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setClassification(null);
//            }
//        });
//        mappings.put(CalendarField.IDCOLOR_LABEL, new IntegerMapping<UserizedEvent>(CalendarField.IDCOLOR_LABEL.getJsonName(), CalendarField.IDCOLOR_LABEL.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getColor();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                object.setColor(null == value ? null : Appointment2Event.getColor(value.intValue()));
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                return Event2Appointment.getColorLabel(object.getColor());
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setColor(null);
//            }
//        });
//        mappings.put(CalendarField.NUMBER_OF_ATTACHMENTS, new IntegerMapping<UserizedEvent>(CalendarField.NUMBER_OF_ATTACHMENTS.getJsonName(), CalendarField.NUMBER_OF_ATTACHMENTS.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return true;
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                // throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                return Integer.valueOf(null == object.getAttachments() ? 0 : object.getAttachments().size());
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                // throw new UnsupportedOperationException();
//            }
//        });
//        mappings.put(CalendarField.LAST_MODIFIED_OF_NEWEST_ATTACHMENT, new TimeMapping<UserizedEvent>(CalendarField.LAST_MODIFIED_OF_NEWEST_ATTACHMENT.getJsonName(), CalendarField.LAST_MODIFIED_OF_NEWEST_ATTACHMENT.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return false;
//            }
//
//            @Override
//            public void set(UserizedEvent object, Date value) throws OXException {
//                throw new UnsupportedOperationException();
//            }
//
//            @Override
//            public Date get(UserizedEvent object) {
//                return null;
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                throw new UnsupportedOperationException();
//            }
//        });
//        mappings.put(CalendarField.TITLE, new StringMapping<UserizedEvent>(CalendarField.TITLE.getJsonName(), CalendarField.TITLE.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getSummary();
//            }
//
//            @Override
//            public void set(UserizedEvent object, String value) throws OXException {
//                object.setSummary(value);
//            }
//
//            @Override
//            public String get(UserizedEvent object) {
//                return object.getSummary();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setSummary(null);
//            }
//        });
//        mappings.put(CalendarField.START_DATE, new TimeMapping<UserizedEvent>(CalendarField.START_DATE.getJsonName(), CalendarField.START_DATE.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getStartDate();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Date value) throws OXException {
//                object.setStartDate(value);
//            }
//
//            @Override
//            public Date get(UserizedEvent object) {
//                return object.getStartDate();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setStartDate(null);
//            }
//        });
//        mappings.put(CalendarField.END_DATE, new TimeMapping<UserizedEvent>(CalendarField.END_DATE.getJsonName(), CalendarField.END_DATE.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getEndDate();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Date value) throws OXException {
//                object.setEndDate(value);
//            }
//
//            @Override
//            public Date get(UserizedEvent object) {
//                return object.getEndDate();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setEndDate(null);
//            }
//        });
//        mappings.put(CalendarField.NOTE, new StringMapping<UserizedEvent>(CalendarField.NOTE.getJsonName(), CalendarField.NOTE.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getDescription();
//            }
//
//            @Override
//            public void set(UserizedEvent object, String value) throws OXException {
//                object.setDescription(value);
//            }
//
//            @Override
//            public String get(UserizedEvent object) {
//                return object.getDescription();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setDescription(null);
//            }
//        });
//        mappings.put(CalendarField.ALARM, new IntegerMapping<UserizedEvent>(CalendarField.ALARM.getJsonName(), CalendarField.ALARM.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getAlarms();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                object.setAlarms(null == value ? null : Collections.singletonList(Appointment2Event.getAlarm(value.intValue())));
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                return Integer.valueOf(null == object.getAlarms() ? 0 : Event2Appointment.getReminder(object.getAlarms()));
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setAlarms(null);
//            }
//        });
//        mappings.put(CalendarField.RECURRENCE_ID, new IntegerMapping<UserizedEvent>(CalendarField.RECURRENCE_ID.getJsonName(), CalendarField.RECURRENCE_ID.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return false;
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                //
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                return null;
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                //
//            }
//        });
//        mappings.put(CalendarField.RECURRENCE_POSITION, new IntegerMapping<UserizedEvent>(CalendarField.RECURRENCE_POSITION.getJsonName(), CalendarField.RECURRENCE_POSITION.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return false;
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                //
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                return null;
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                //
//            }
//        });
//        mappings.put(CalendarField.RECURRENCE_DATE_POSITION, new DateMapping<UserizedEvent>(CalendarField.RECURRENCE_DATE_POSITION.getJsonName(), CalendarField.RECURRENCE_DATE_POSITION.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return false;
//            }
//
//            @Override
//            public void set(UserizedEvent object, Date value) throws OXException {
//                //
//            }
//
//            @Override
//            public Date get(UserizedEvent object) {
//                return null;
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                //
//            }
//        });
//        mappings.put(CalendarField.RECURRENCE_TYPE, new IntegerMapping<UserizedEvent>(CalendarField.RECURRENCE_TYPE.getJsonName(), CalendarField.RECURRENCE_TYPE.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                return null != pattern && null != pattern.getInterval();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                if (null == pattern) {
//                    pattern = new SeriesPattern();
//                }
//                pattern.setType(value);
//                object.setRecurrenceRule(Appointment2Event.getRecurrenceRule(pattern));
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                return null == pattern ? null : pattern.getType();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                if (null != pattern) {
//                    pattern.setType(null);
//                    object.setRecurrenceRule(Appointment2Event.getRecurrenceRule(pattern));
//                }
//            }
//        });
//        mappings.put(CalendarField.RECURRENCE_START, new DateMapping<UserizedEvent>(CalendarField.RECURRENCE_START.getJsonName(), CalendarField.RECURRENCE_START.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                return null != pattern && null != pattern.getSeriesStart();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Date value) throws OXException {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                if (null == pattern) {
//                    pattern = new SeriesPattern();
//                }
//                pattern.setSeriesStart(null != value ? L(value.getTime()) : null);
//                object.setRecurrenceRule(Appointment2Event.getRecurrenceRule(pattern));
//            }
//
//            @Override
//            public Date get(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                return null == pattern || null == pattern.getSeriesStart() ? null : new Date(pattern.getSeriesStart().longValue());
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                //
//            }
//        });
//        mappings.put(CalendarField.CHANGE_EXCEPTIONS, new ArrayMapping<Long, UserizedEvent>(CalendarField.CHANGE_EXCEPTIONS.getJsonName(), CalendarField.CHANGE_EXCEPTIONS.getColumnNumber()) {
//
//            @Override
//            public Long[] newArray(int size) {
//                return new Long[size];
//            }
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return false;
//            }
//
//            @Override
//            public void set(UserizedEvent object, Long[] value) throws OXException {
//                //
//            }
//
//            @Override
//            public Long[] get(UserizedEvent object) {
//                return null;
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                //
//            }
//
//            @Override
//            protected Long deserialize(JSONArray array, int index) throws JSONException, OXException {
//                return null;
//            }
//        });
//        mappings.put(CalendarField.DELETE_EXCEPTIONS, new ArrayMapping<Long, UserizedEvent>(CalendarField.DELETE_EXCEPTIONS.getJsonName(), CalendarField.DELETE_EXCEPTIONS.getColumnNumber()) {
//
//            @Override
//            public Long[] newArray(int size) {
//                return new Long[size];
//            }
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return false;
//            }
//
//            @Override
//            public void set(UserizedEvent object, Long[] value) throws OXException {
//                //
//            }
//
//            @Override
//            public Long[] get(UserizedEvent object) {
//                return null;
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                //
//            }
//
//            @Override
//            protected Long deserialize(JSONArray array, int index) throws JSONException, OXException {
//                return null;
//            }
//        });
//        mappings.put(CalendarField.DAYS, new IntegerMapping<UserizedEvent>(CalendarField.DAYS.getJsonName(), CalendarField.DAYS.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                return null != pattern && null != pattern.getDaysOfWeek();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                if (null == pattern) {
//                    pattern = new SeriesPattern();
//                }
//                pattern.setDaysOfWeek(value);
//                object.setRecurrenceRule(Appointment2Event.getRecurrenceRule(pattern));
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                return null == pattern ? null : pattern.getDaysOfWeek();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                if (null != pattern) {
//                    pattern.setDaysOfWeek(null);
//                    object.setRecurrenceRule(Appointment2Event.getRecurrenceRule(pattern));
//                }
//            }
//        });
//        mappings.put(CalendarField.DAY_IN_MONTH, new IntegerMapping<UserizedEvent>(CalendarField.DAY_IN_MONTH.getJsonName(), CalendarField.DAY_IN_MONTH.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                return null != pattern && null != pattern.getDayOfMonth();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                if (null == pattern) {
//                    pattern = new SeriesPattern();
//                }
//                pattern.setDayOfMonth(value);
//                object.setRecurrenceRule(Appointment2Event.getRecurrenceRule(pattern));
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                return null == pattern ? null : pattern.getDayOfMonth();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                if (null != pattern) {
//                    pattern.setDayOfMonth(null);
//                    object.setRecurrenceRule(Appointment2Event.getRecurrenceRule(pattern));
//                }
//            }
//        });
//        mappings.put(CalendarField.MONTH, new IntegerMapping<UserizedEvent>(CalendarField.MONTH.getJsonName(), CalendarField.MONTH.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                return null != pattern && null != pattern.getMonth();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                if (null == pattern) {
//                    pattern = new SeriesPattern();
//                }
//                pattern.setMonth(value);
//                object.setRecurrenceRule(Appointment2Event.getRecurrenceRule(pattern));
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                return null == pattern ? null : pattern.getMonth();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                if (null != pattern) {
//                    pattern.setMonth(null);
//                    object.setRecurrenceRule(Appointment2Event.getRecurrenceRule(pattern));
//                }
//            }
//        });
//        mappings.put(CalendarField.INTERVAL, new IntegerMapping<UserizedEvent>(CalendarField.INTERVAL.getJsonName(), CalendarField.INTERVAL.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                return null != pattern && null != pattern.getInterval();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                if (null == pattern) {
//                    pattern = new SeriesPattern();
//                }
//                pattern.setInterval(value);
//                object.setRecurrenceRule(Appointment2Event.getRecurrenceRule(pattern));
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                return null == pattern ? null : pattern.getInterval();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                if (null != pattern) {
//                    pattern.setInterval(null);
//                    object.setRecurrenceRule(Appointment2Event.getRecurrenceRule(pattern));
//                }
//            }
//        });
//        mappings.put(CalendarField.UNTIL, new DateMapping<UserizedEvent>(CalendarField.UNTIL.getJsonName(), CalendarField.UNTIL.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                return null != pattern && null != pattern.getSeriesEnd();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Date value) throws OXException {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                if (null == pattern) {
//                    pattern = new SeriesPattern();
//                }
//                pattern.setSeriesEnd(null == value ? null : value.getTime());
//                object.setRecurrenceRule(Appointment2Event.getRecurrenceRule(pattern));
//            }
//
//            @Override
//            public Date get(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                return null == pattern || null == pattern.getSeriesEnd() ? null : new Date(pattern.getSeriesEnd().longValue());
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                if (null != pattern) {
//                    pattern.setSeriesEnd(null);
//                    object.setRecurrenceRule(Appointment2Event.getRecurrenceRule(pattern));
//                }
//            }
//        });
//        mappings.put(CalendarField.RECURRENCE_COUNT, new IntegerMapping<UserizedEvent>(CalendarField.RECURRENCE_COUNT.getJsonName(), CalendarField.RECURRENCE_COUNT.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                return null != pattern && null != pattern.getOccurrences();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                if (null == pattern) {
//                    pattern = new SeriesPattern();
//                }
//                pattern.setOccurrences(value);
//                object.setRecurrenceRule(Appointment2Event.getRecurrenceRule(pattern));
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                return null == pattern ? null : pattern.getOccurrences();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                SeriesPattern pattern = Event2Appointment.getSeriesPattern(object.getRecurrenceRule());
//                if (null != pattern) {
//                    pattern.setOccurrences(null);
//                    object.setRecurrenceRule(Appointment2Event.getRecurrenceRule(pattern));
//                }
//            }
//        });
//        //        mappings.put(CalendarField.NOTIFICATION, new BooleanMapping<UserizedEvent>(CalendarField.NOTIFICATION.getJsonName(), CalendarField.NOTIFICATION.getColumnNumber()) {
//        //
//        //            @Override
//        //            public boolean isSet(UserizedEvent object) {
//        //                return false;
//        //            }
//        //
//        //            @Override
//        //            public void set(UserizedEvent object, Boolean value) throws OXException {
//        //                //
//        //            }
//        //
//        //            @Override
//        //            public Boolean get(UserizedEvent object) {
//        //                return false;
//        //            }
//        //
//        //            @Override
//        //            public void remove(UserizedEvent object) {
//        //                //
//        //            }
//        //        });
//        //        mappings.put(CalendarField.RECURRENCE_CALCULATOR, new DateMapping<UserizedEvent>(CalendarField.RECURRENCE_CALCULATOR.getJsonName(), CalendarField.RECURRENCE_CALCULATOR.getColumnNumber()) {
//        //
//        //            @Override
//        //            public boolean isSet(UserizedEvent object) {
//        //                return false;
//        //            }
//        //
//        //            @Override
//        //            public void set(UserizedEvent object, Date value) throws OXException {
//        //                //
//        //            }
//        //
//        //            @Override
//        //            public Date get(UserizedEvent object) {
//        //                return null;
//        //            }
//        //
//        //            @Override
//        //            public void remove(UserizedEvent object) {
//        //                //
//        //            }
//        //        });
//        mappings.put(CalendarField.PARTICIPANTS, new DefaultJsonMapping<List<Attendee>, UserizedEvent>(CalendarField.PARTICIPANTS.getJsonName(), CalendarField.PARTICIPANTS.getColumnNumber()) {
//
//            @Override
//            public void deserialize(JSONObject from, UserizedEvent to) throws JSONException, OXException {
//                JSONArray jsonArray = from.optJSONArray(getAjaxName());
//                List<Attendee> value = get(to);
//                if (null == value) {
//                    value = new ArrayList<>();
//                    set(to, value);
//                }
//                for (int i = 0; i < jsonArray.length(); i++) {
//                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//                    Attendee attendee = new Attendee();
//                    attendee.setEntity(jsonObject.optInt(ParticipantsFields.ID));
//                    attendee.setCuType(Appointment2Event.getCalendarUserType(jsonObject.getInt(ParticipantsFields.TYPE)));
//                    if (jsonObject.has(ParticipantsFields.MAIL)) {
//                        attendee.setUri("mailto:" + jsonObject.getString(ParticipantsFields.MAIL));
//                    }
//                    attendee.setCommonName(jsonObject.optString(ParticipantsFields.DISPLAY_NAME));
//                    attendee.setPartStat(Appointment2Event.getParticipationStatus(jsonObject.optInt(ParticipantsFields.CONFIRMATION)));
//                    attendee.setComment(jsonObject.optString(ParticipantsFields.MESSAGE));
//                    value.add(attendee);
//                }
//            }
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getAttendees() && 0 < object.getAttendees().size();
//            }
//
//            @Override
//            public void set(UserizedEvent object, List<Attendee> value) throws OXException {
//                object.setAttendees(value);
//            }
//
//            @Override
//            public List<Attendee> get(UserizedEvent object) {
//                return object.getAttendees();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setAttendees(null);
//            }
//
//            @Override
//            public Object serialize(UserizedEvent from, TimeZone timeZone, Session session) throws JSONException {
//                List<Attendee> value = this.get(from);
//                if (null == value) {
//                    return JSONObject.NULL;
//                }
//                JSONArray jsonArray = new JSONArray(value.size());
//                // TODO: group members should not be listed here explicitly?
//                for (Attendee attendee : value) {
//                    if (0 < attendee.getEntity()) {
//                        jsonArray.put(new JSONObject()
//                            .put(ParticipantsFields.ID, attendee.getEntity())
//                            .put(ParticipantsFields.TYPE, Event2Appointment.getParticipantType(attendee.getCuType(), true)));
//                    } else {
//                        jsonArray.put(new JSONObject()
//                            .put(ParticipantsFields.TYPE, Event2Appointment.getParticipantType(attendee.getCuType(), false))
//                            .putOpt(ParticipantsFields.DISPLAY_NAME, attendee.getCommonName())
//                            .putOpt(ParticipantsFields.MAIL, Event2Appointment.getEMailAddress(attendee.getUri())));
//                    }
//                }
//                return jsonArray;
//            }
//        });
//        mappings.put(CalendarField.USERS, new DefaultJsonMapping<List<Attendee>, UserizedEvent>(CalendarField.USERS.getJsonName(), CalendarField.USERS.getColumnNumber()) {
//
//            @Override
//            public void deserialize(JSONObject from, UserizedEvent to) throws JSONException, OXException {
//                // TODO
//            }
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getAttendees() && 0 < object.getAttendees().size();
//            }
//
//            @Override
//            public void set(UserizedEvent object, List<Attendee> value) throws OXException {
//                object.setAttendees(value);
//            }
//
//            @Override
//            public List<Attendee> get(UserizedEvent object) {
//                return object.getAttendees();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setAttendees(null);
//            }
//
//            @Override
//            public Object serialize(UserizedEvent from, TimeZone timeZone, Session session) throws JSONException {
//                List<Attendee> value = this.get(from);
//                if (null == value) {
//                    return JSONObject.NULL;
//                }
//                JSONArray jsonArray = new JSONArray(value.size());
//                for (Attendee attendee : value) {
//                    if (0 < attendee.getEntity() && CalendarUserType.INDIVIDUAL.equals(attendee.getCuType())) {
//                        jsonArray.put(new JSONObject()
//                            .put(ParticipantsFields.ID, attendee.getEntity())
//                            .put(ParticipantsFields.CONFIRMATION, Event2Appointment.getConfirm(attendee.getPartStat())));
//                    }
//                }
//                return jsonArray;
//            }
//        });
//        mappings.put(CalendarField.CONFIRMATIONS, new DefaultJsonMapping<List<Attendee>, UserizedEvent>(CalendarField.CONFIRMATIONS.getJsonName(), CalendarField.CONFIRMATIONS.getColumnNumber()) {
//
//            @Override
//            public void deserialize(JSONObject from, UserizedEvent to) throws JSONException, OXException {
//                // TODO
//            }
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getAttendees() && 0 < object.getAttendees().size();
//            }
//
//            @Override
//            public void set(UserizedEvent object, List<Attendee> value) throws OXException {
//                object.setAttendees(value);
//            }
//
//            @Override
//            public List<Attendee> get(UserizedEvent object) {
//                return object.getAttendees();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setAttendees(null);
//            }
//
//            @Override
//            public Object serialize(UserizedEvent from, TimeZone timeZone, Session session) throws JSONException {
//                List<Attendee> value = this.get(from);
//                if (null == value) {
//                    return JSONObject.NULL;
//                }
//                JSONArray jsonArray = new JSONArray(value.size());
//                for (Attendee attendee : value) {
//                    if (0 == attendee.getEntity()) {
//                        jsonArray.put(new JSONObject()
//                            .put(ParticipantsFields.TYPE, Participant.EXTERNAL_USER)
//                            .putOpt(ParticipantsFields.MAIL, Event2Appointment.getEMailAddress(attendee.getUri()))
//                            .putOpt(ParticipantsFields.DISPLAY_NAME, attendee.getCommonName())
//                            .put(ParticipantsFields.STATUS, Event2Appointment.getConfirm(attendee.getPartStat())));
//                    }
//                }
//                return jsonArray;
//            }
//        });
//        mappings.put(CalendarField.ORGANIZER, new StringMapping<UserizedEvent>(CalendarField.ORGANIZER.getJsonName(), CalendarField.ORGANIZER.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getOrganizer();
//            }
//
//            @Override
//            public void set(UserizedEvent object, String value) throws OXException {
//                Organizer organizer = object.getOrganizer();
//                if (null == value) {
//                    if (null != organizer) {
//                        organizer.setUri(null);
//                    }
//                } else {
//                    if (null == organizer) {
//                        organizer = new Organizer();
//                        object.setOrganizer(organizer);
//                    }
//                    organizer.setUri("mailto:" + value);
//                }
//            }
//
//            @Override
//            public String get(UserizedEvent object) {
//                return null != object.getOrganizer() ? Event2Appointment.getEMailAddress(object.getOrganizer().getEmail()) : null;
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setOrganizer(null);
//            }
//        });
//        mappings.put(CalendarField.ORGANIZER_ID, new IntegerMapping<UserizedEvent>(CalendarField.ORGANIZER_ID.getJsonName(), CalendarField.ORGANIZER_ID.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getOrganizer();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                Organizer organizer = object.getOrganizer();
//                if (null == value || 0 == value.intValue()) {
//                    if (null != organizer) {
//                        organizer.setEntity(0);
//                    }
//                } else {
//                    if (null == organizer) {
//                        organizer = new Organizer();
//                        object.setOrganizer(organizer);
//                    }
//                    organizer.setEntity(value.intValue());
//                }
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                return Integer.valueOf(null == object.getOrganizer() ? 0 : object.getOrganizer().getEntity());
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setOrganizer(null);
//            }
//        });
//        mappings.put(CalendarField.PRINCIPAL, new StringMapping<UserizedEvent>(CalendarField.PRINCIPAL.getJsonName(), CalendarField.PRINCIPAL.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return false;
//            }
//
//            @Override
//            public void set(UserizedEvent object, String value) throws OXException {
//                //
//            }
//
//            @Override
//            public String get(UserizedEvent object) {
//                return null;
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                //
//            }
//        });
//        mappings.put(CalendarField.PRINCIPAL_ID, new IntegerMapping<UserizedEvent>(CalendarField.PRINCIPAL_ID.getJsonName(), CalendarField.PRINCIPAL_ID.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return false;
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                //
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                return Integer.valueOf(0);
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                //
//            }
//        });
//        mappings.put(CalendarField.UID, new StringMapping<UserizedEvent>(CalendarField.UID.getJsonName(), CalendarField.UID.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getUid();
//            }
//
//            @Override
//            public void set(UserizedEvent object, String value) throws OXException {
//                object.setUid(value);
//            }
//
//            @Override
//            public String get(UserizedEvent object) {
//                return object.getUid();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setUid(null);
//            }
//        });
//        mappings.put(CalendarField.SEQUENCE, new IntegerMapping<UserizedEvent>(CalendarField.SEQUENCE.getJsonName(), CalendarField.SEQUENCE.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getSequence();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                object.setSequence(value);
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                return Integer.valueOf(object.getFolderId());
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setFolderId(0);
//            }
//        });
//        mappings.put(CalendarField.LOCATION, new StringMapping<UserizedEvent>(CalendarField.LOCATION.getJsonName(), CalendarField.LOCATION.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getLocation();
//            }
//
//            @Override
//            public void set(UserizedEvent object, String value) throws OXException {
//                object.setLocation(value);
//            }
//
//            @Override
//            public String get(UserizedEvent object) {
//                return object.getLocation();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setLocation(null);
//            }
//        });
//        mappings.put(CalendarField.FULL_TIME, new BooleanMapping<UserizedEvent>(CalendarField.FULL_TIME.getJsonName(), CalendarField.FULL_TIME.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return object.isAllDay();
//            }
//
//            @Override
//            public void set(UserizedEvent object, Boolean value) throws OXException {
//                object.setAllDay(Boolean.TRUE.equals(value));
//            }
//
//            @Override
//            public Boolean get(UserizedEvent object) {
//                return Boolean.valueOf(object.isAllDay());
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setAllDay(false);
//            }
//        });
//        mappings.put(CalendarField.SHOWN_AS, new IntegerMapping<UserizedEvent>(CalendarField.SHOWN_AS.getJsonName(), CalendarField.SHOWN_AS.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return true;
//            }
//
//            @Override
//            public void set(UserizedEvent object, Integer value) throws OXException {
//                object.setStatus(null == value ? null : Appointment2Event.getEventStatus(value.intValue()));
//            }
//
//            @Override
//            public Integer get(UserizedEvent object) {
//                return null == object.getStatus() ? Integer.valueOf(1) : Event2Appointment.getShownAs(object.getStatus());
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setStatus(null);
//            }
//        });
//        mappings.put(CalendarField.TIMEZONE, new StringMapping<UserizedEvent>(CalendarField.TIMEZONE.getJsonName(), CalendarField.TIMEZONE.getColumnNumber()) {
//
//            @Override
//            public boolean isSet(UserizedEvent object) {
//                return null != object.getStartTimezone();
//            }
//
//            @Override
//            public void set(UserizedEvent object, String value) throws OXException {
//                object.setStartTimezone(value);
//            }
//
//            @Override
//            public String get(UserizedEvent object) {
//                return object.getStartTimezone();
//            }
//
//            @Override
//            public void remove(UserizedEvent object) {
//                object.setStartTimezone(null);
//            }
//        });
//        return mappings;
//    }
//
//}
