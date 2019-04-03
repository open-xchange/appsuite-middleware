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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.storage.rdb;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.java.Autoboxing.l;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.CalendarStrings;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.DefaultAttendeePrivileges;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventStatus;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.RelatedTo;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.compat.ShownAsTransparency;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.database.BigIntMapping;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMultiMapping;
import com.openexchange.groupware.tools.mappings.database.IntegerMapping;
import com.openexchange.groupware.tools.mappings.database.PointMapping;
import com.openexchange.groupware.tools.mappings.database.VarCharMapping;
import com.openexchange.java.Strings;

/**
 * {@link EventMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventMapper extends DefaultDbMapper<Event, EventField> {

    private static final EventMapper INSTANCE = new EventMapper();

    /**
     * Gets the mapper instance.
     *
     * @return The instance.
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
     * Gets all mapped fields.
     *
     * @return The mapped fields
     */
    @Override
    public EventField[] getMappedFields() {
        return getMappedFields(null);
    }

    /**
     * Gets the mapped fields out of the supplied requested fields, ignoring unmapped fields.
     *
     * @param requestedFields The requested fields, or <code>null</code> to get all mapped fields
     * @return The mapped fields
     */
    @Override
    public EventField[] getMappedFields(EventField[] requestedFields) {
        Set<EventField> knownFields = getMappings().keySet();
        Set<EventField> mappedFields;
        if (null == requestedFields) {
            mappedFields = knownFields;
        } else {
            mappedFields = new HashSet<EventField>(requestedFields.length);
            for (EventField field : requestedFields) {
                if (knownFields.contains(field)) {
                    mappedFields.add(field);
                }
            }
        }
        return mappedFields.toArray(new EventField[mappedFields.size()]);
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
	protected EnumMap<EventField, DbMapping<? extends Object, Event>> createMappings() {
		EnumMap<EventField, DbMapping<? extends Object, Event>> mappings = new
			EnumMap<EventField, DbMapping<? extends Object, Event>>(EventField.class);

        mappings.put(EventField.ID, new IntegerMapping<Event>("id", "Object ID") {

            @Override
            public void set(Event event, Integer value) {
                event.setId(null == value ? null : value.toString());
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsId();
            }

            @Override
            public Integer get(Event event) {
                String value = event.getId();
                return null == value ? null : Integer.valueOf(value);
            }

            @Override
            public void remove(Event event) {
                event.removeId();
            }
        });
        mappings.put(EventField.FOLDER_ID, new VarCharMapping<Event>("folder", "Folder ID") {

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
        mappings.put(EventField.SERIES_ID, new IntegerMapping<Event>("series", "Series id") {

            @Override
            public void set(Event event, Integer value) {
                event.setSeriesId(null == value ? null : value.toString());
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsSeriesId();
            }

            @Override
            public Integer get(Event event) {
                String value = event.getSeriesId();
                return null == value ? null : Integer.valueOf(value);
            }

            @Override
            public void remove(Event event) {
                event.removeSeriesId();
            }
        });
        mappings.put(EventField.UID, new VarCharMapping<Event>("uid", "UID") {

            @Override
            public void set(Event event, String value) {
                event.setUid(value);
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsUid();
            }

            @Override
            public String get(Event event) {
                return event.getUid();
            }

            @Override
            public void remove(Event event) {
                event.removeUid();
            }
        });
        mappings.put(EventField.RELATED_TO, new VarCharMapping<Event>("relatedTo", "Related-To") {

            @Override
            public void set(Event event, String value) {
                if (null == value) {
                    event.setRelatedTo(null);
                } else {
                    String[] splitted = Strings.splitByColon(value);
                    event.setRelatedTo(new RelatedTo(splitted[0], splitted[1]));
                }
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsRelatedTo();
            }

            @Override
            public String get(Event event) {
                RelatedTo value = event.getRelatedTo();
                if (null == value) {
                    return null;
                }
                if (null == value.getRelType()) {
                    return ':' + value.getValue();
                }
                return value.getRelType() + ':' + value.getValue();
            }

            @Override
            public void remove(Event event) {
                event.removeRelatedTo();
            }
        });
        mappings.put(EventField.RECURRENCE_RULE, new VarCharMapping<Event>("rrule", CalendarStrings.FIELD_RECURRENCE_RULE) {

            @Override
            public void set(Event event, String value) {
                event.setRecurrenceRule(value);
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsRecurrenceRule();
            }

            @Override
            public String get(Event event) {
                return event.getRecurrenceRule();
            }

            @Override
            public void remove(Event event) {
                event.removeRecurrenceRule();
            }
        });
        mappings.put(EventField.RECURRENCE_ID, new VarCharMapping<Event>("recurrence", "Recurrence ID") {

            @Override
            public void set(Event event, String value) {
                event.setRecurrenceId(null == value ? null : new DefaultRecurrenceId(value));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsRecurrenceId();
            }

            @Override
            public String get(Event event) {
                RecurrenceId value = event.getRecurrenceId();
                return null == value ? null : value.toString();
            }

            @Override
            public void remove(Event event) {
                event.removeRecurrenceId();
            }
        });
        mappings.put(EventField.RECURRENCE_DATES, new RecurrenceIdListMapping<Event>("rDate", "Recurrence dates") {

            @Override
            public boolean isSet(Event event) {
                return event.containsRecurrenceDates();
            }

            @Override
            public void remove(Event event) {
                event.removeRecurrenceDates();
            }

            @Override
            protected SortedSet<RecurrenceId> getRecurrenceIds(Event event) {
                return event.getRecurrenceDates();
            }

            @Override
            protected void setRecurrenceIds(Event event, SortedSet<RecurrenceId> value) {
                event.setRecurrenceDates(value);
            }
        });
        mappings.put(EventField.CHANGE_EXCEPTION_DATES, new RecurrenceIdListMapping<Event>("overriddenDate", "Change exceptions") {

            @Override
            public boolean isSet(Event event) {
                return event.containsChangeExceptionDates();
            }

            @Override
            public void remove(Event event) {
                event.removeChangeExceptionDates();
            }

            @Override
            protected SortedSet<RecurrenceId> getRecurrenceIds(Event event) {
                return event.getChangeExceptionDates();
            }

            @Override
            protected void setRecurrenceIds(Event event, SortedSet<RecurrenceId> value) {
                event.setChangeExceptionDates(value);
            }
        });
        mappings.put(EventField.DELETE_EXCEPTION_DATES, new RecurrenceIdListMapping<Event>("exDate", "Delete exceptions") {

            @Override
            public boolean isSet(Event event) {
                return event.containsDeleteExceptionDates();
            }

            @Override
            public void remove(Event event) {
                event.removeDeleteExceptionDates();
            }

            @Override
            protected SortedSet<RecurrenceId> getRecurrenceIds(Event event) {
                return event.getDeleteExceptionDates();
            }

            @Override
            protected void setRecurrenceIds(Event event, SortedSet<RecurrenceId> value) {
                event.setDeleteExceptionDates(value);
            }
        });
        mappings.put(EventField.CREATED, new BigIntMapping<Event>("created", "Created") {

            @Override
            public void set(Event event, Long value) {
                event.setCreated(null == value ? null : new Date(value.longValue()));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsCreated();
            }

            @Override
            public Long get(Event event) {
                Date value = event.getCreated();
                return null == value ? null : L(value.getTime());
            }

            @Override
            public void remove(Event event) {
                event.removeCreated();
            }
        });
        mappings.put(EventField.CREATED_BY, new IntegerMapping<Event>("createdBy", "Created by") {

            @Override
            public void set(Event event, Integer value) {
                if (null == value || 0 == value.intValue()) {
                    event.setCreatedBy(null);
                } else {
                    CalendarUser calendarUser = new CalendarUser();
                    calendarUser.setEntity(value.intValue());
                    event.setCreatedBy(calendarUser);
                }
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsCreatedBy();
            }

            @Override
            public Integer get(Event event) {
                return null != event.getCreatedBy() && 0 < event.getCreatedBy().getEntity() ? I(event.getCreatedBy().getEntity()) : null;
            }

            @Override
            public void remove(Event event) {
                event.removeCreatedBy();
            }
        });
        mappings.put(EventField.LAST_MODIFIED, new BigIntMapping<Event>("modified", "Last modified") {

            @Override
            public void set(Event event, Long value) {
                event.setLastModified(null == value ? null : new Date(value.longValue()));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsLastModified();
            }

            @Override
            public Long get(Event event) {
                Date value = event.getLastModified();
                return null == value ? null : L(value.getTime());
            }

            @Override
            public void remove(Event event) {
                event.removeLastModified();
            }
        });
        mappings.put(EventField.MODIFIED_BY, new IntegerMapping<Event>("modifiedBy", "Modified by") {

            @Override
            public void set(Event event, Integer value) {
                if (null == value || 0 == value.intValue()) {
                    event.setModifiedBy(null);
                } else {
                    CalendarUser calendarUser = new CalendarUser();
                    calendarUser.setEntity(value.intValue());
                    event.setModifiedBy(calendarUser);
                }
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsModifiedBy();
            }

            @Override
            public Integer get(Event event) {
                return null != event.getModifiedBy() && 0 < event.getModifiedBy().getEntity() ? I(event.getModifiedBy().getEntity()) : null;
            }

            @Override
            public void remove(Event event) {
                event.removeModifiedBy();
            }
        });
        mappings.put(EventField.CALENDAR_USER, new IntegerMapping<Event>("user", "Calendar User") {

            @Override
            public void set(Event event, Integer value) {
                if (null == value || 0 == value.intValue()) {
                    event.setCalendarUser(null);
                } else {
                    CalendarUser calendarUser = new CalendarUser();
                    calendarUser.setEntity(value.intValue());
                    event.setCalendarUser(calendarUser);
                }
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsCalendarUser();
            }

            @Override
            public Integer get(Event event) {
                return null != event.getCalendarUser() && 0 < event.getCalendarUser().getEntity() ? I(event.getCalendarUser().getEntity()) : null;
            }

            @Override
            public void remove(Event event) {
                event.removeCalendarUser();
            }
        });
        mappings.put(EventField.TIMESTAMP, new BigIntMapping<Event>("timestamp", "Timestamp") {

            @Override
            public void set(Event event, Long value) {
                event.setTimestamp(null == value ? 0L : l(value));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsTimestamp();
            }

            @Override
            public Long get(Event event) {
                return L(event.getTimestamp());
            }

            @Override
            public void remove(Event event) {
                event.removeTimestamp();
            }
        });
        mappings.put(EventField.SEQUENCE, new IntegerMapping<Event>("sequence", "Sequence") {

            @Override
            public void set(Event event, Integer value) {
                event.setSequence(null == value ? 0 : i(value));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsSequence();
            }

            @Override
            public Integer get(Event event) {
                return I(event.getSequence());
            }

            @Override
            public void remove(Event event) {
                event.removeSequence();
            }
        });
        mappings.put(EventField.START_DATE, new DateTimeMapping<Event>("start", "startTimezone", "allDay", CalendarStrings.FIELD_START_DATE) {

            @Override
            public boolean isSet(Event event) {
                return event.containsStartDate();
            }

            @Override
            public void set(Event event, DateTime value) throws OXException {
                event.setStartDate(value);
            }

            @Override
            public DateTime get(Event event) {
                return event.getStartDate();
            }

            @Override
            public void remove(Event event) {
                event.removeStartDate();
            }
        });
        mappings.put(EventField.END_DATE, new DefaultDbMultiMapping<DateTime, Event>(new String[] { "end", "endTimezone" }, CalendarStrings.FIELD_END_DATE) {

            @Override
            public int set(PreparedStatement statement, int parameterIndex, Event object) throws SQLException {
                DateTime value = get(object);
                if (null == value) {
                    statement.setNull(parameterIndex, Types.TIMESTAMP);
                    statement.setNull(parameterIndex + 1, Types.VARCHAR);
                } else {
                    statement.setTimestamp(parameterIndex, new Timestamp(value.getTimestamp()));
                    statement.setString(parameterIndex + 1, null == value.getTimeZone() ? null : value.getTimeZone().getID());
                }
                return 2;
            }

            @Override
            public DateTime get(ResultSet resultSet, String[] columnLabels) throws SQLException {
                Timestamp timestamp;
                try {
                    timestamp = resultSet.getTimestamp(columnLabels[0]);
                } catch (SQLException e) {
                    if ("S1009".equals(e.getSQLState())) {
                        /*
                         * http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-configuration-properties.html
                         * DATETIME values that are composed entirely of zeros result in an exception with state S1009
                         */
                        return null;
                    }
                    throw e;
                }
                if (null == timestamp) {
                    return null;
                }
                String timeZoneId = resultSet.getString(columnLabels[1]);
                return new DateTime(CalendarUtils.optTimeZone(timeZoneId, null), timestamp.getTime());
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsEndDate();
            }

            @Override
            public void set(Event event, DateTime value) throws OXException {
                event.setEndDate(value);
            }

            @Override
            public DateTime get(Event event) {
                return event.getEndDate();
            }

            @Override
            public void remove(Event event) {
                event.removeEndDate();
            }
        });
        mappings.put(EventField.TRANSP, new IntegerMapping<Event>("transp", CalendarStrings.FIELD_TRANSP) {
            // 0 - TRANSPARENT, FREE
            // 1 - OPAQUE, RESERVED
            // 2 - OPAQUE, TEMPORARY
            // 3 - OPAQUE, ABSENT

            @Override
            public void set(Event event, Integer value) {
                if (null == value) {
                    event.setTransp(null);
                } else if (0 == value.intValue()) {
                    event.setTransp(TimeTransparency.TRANSPARENT);
                } else if (2 == value.intValue()) {
                    event.setTransp(ShownAsTransparency.TEMPORARY);
                } else if (3 == value.intValue()) {
                    event.setTransp(ShownAsTransparency.ABSENT);
                } else {
                    event.setTransp(TimeTransparency.OPAQUE);
                }
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsTransp();
            }

            @Override
            public Integer get(Event event) {
                Transp value = event.getTransp();
                if (null == value) {
                    return null;
                } else if (Transp.TRANSPARENT.equals(value.getValue())) {
                    return I(0);
                } else if (ShownAsTransparency.TEMPORARY.equals(value)) {
                    return I(2);
                } else if (ShownAsTransparency.ABSENT.equals(value)) {
                    return I(3);
                } else {
                    return I(1);
                }
            }

            @Override
            public void remove(Event event) {
                event.removeTransp();
            }
        });
        mappings.put(EventField.CLASSIFICATION, new VarCharMapping<Event>("class", CalendarStrings.FIELD_CLASSIFICATION) {

            @Override
            public void set(Event event, String value) {
                event.setClassification(new Classification(value));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsClassification();
            }

            @Override
            public String get(Event event) {
                Classification value = event.getClassification();
                return null == value ? null : value.getValue();
            }

            @Override
            public void remove(Event event) {
                event.removeClassification();
            }
        });
        mappings.put(EventField.STATUS, new VarCharMapping<Event>("status", "Status") {

            @Override
            public void set(Event event, String value) {
                event.setStatus(new EventStatus(value));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsStatus();
            }

            @Override
            public String get(Event event) {
                EventStatus value = event.getStatus();
                return null == value ? null : value.getValue();
            }

            @Override
            public void remove(Event event) {
                event.removeStatus();
            }
        });
        mappings.put(EventField.ORGANIZER, new VarCharMapping<Event>("organizer", "Organizer") {

            @Override
            public void set(Event event, String value) {
                Organizer organizer = new Organizer();
                organizer.setUri(value);
                event.setOrganizer(organizer);
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsOrganizer();
            }

            @Override
            public String get(Event event) {
                Organizer value = event.getOrganizer();
                return null == value ? null : value.getUri();
            }

            @Override
            public void remove(Event event) {
                event.removeOrganizer();
            }
        });
        mappings.put(EventField.SUMMARY, new VarCharMapping<Event>("summary", CalendarStrings.FIELD_SUMMARY) {

            @Override
            public void set(Event event, String value) {
                event.setSummary(value);
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsSummary();
            }

            @Override
            public String get(Event event) {
                return event.getSummary();
            }

            @Override
            public void remove(Event event) {
                event.removeSummary();
            }
        });
        mappings.put(EventField.LOCATION, new VarCharMapping<Event>("location", CalendarStrings.FIELD_LOCATION) {

            @Override
            public void set(Event event, String value) {
                event.setLocation(value);
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsLocation();
            }

            @Override
            public String get(Event event) {
                return event.getLocation();
            }

            @Override
            public void remove(Event event) {
                event.removeLocation();
            }
        });
        mappings.put(EventField.DESCRIPTION, new VarCharMapping<Event>("description", CalendarStrings.FIELD_DESCRIPTION) {

            @Override
            public void set(Event event, String value) {
                event.setDescription(value);
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsDescription();
            }

            @Override
            public String get(Event event) {
                return event.getDescription();
            }

            @Override
            public void remove(Event event) {
                event.removeDescription();
            }
        });
        mappings.put(EventField.CATEGORIES, new VarCharListMapping<Event>("categories", "Categories") {

            @Override
            public boolean isSet(Event event) {
                return event.containsCategories();
            }

            @Override
            public void set(Event event, List<String> value) throws OXException {
                event.setCategories(value);
            }

            @Override
            public List<String> get(Event event) {
                return event.getCategories();
            }

            @Override
            public void remove(Event event) {
                event.removeCategories();
            }
        });
        mappings.put(EventField.COLOR, new VarCharMapping<Event>("color", CalendarStrings.FIELD_COLOR) {

            @Override
            public void set(Event event, String value) {
                event.setColor(value);
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsColor();
            }

            @Override
            public String get(Event event) {
                return event.getColor();
            }

            @Override
            public void remove(Event event) {
                event.removeColor();
            }
        });
        mappings.put(EventField.URL, new VarCharMapping<Event>("url", "Url") {

            @Override
            public void set(Event event, String value) {
                event.setUrl(value);
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsUrl();
            }

            @Override
            public String get(Event event) {
                return event.getUrl();
            }

            @Override
            public void remove(Event event) {
                event.removeUrl();
            }
        });
        mappings.put(EventField.GEO, new PointMapping<Event>("geo", "Geo location") {

            @Override
            public boolean isSet(Event event) {
                return event.containsGeo();
            }

            @Override
            public void set(Event event, double[] value) throws OXException {
                event.setGeo(value);
            }

            @Override
            public double[] get(Event event) {
                return event.getGeo();
            }

            @Override
            public void remove(Event event) {
                event.removeGeo();
            }
        });
        mappings.put(EventField.ATTENDEE_PRIVILEGES, new IntegerMapping<Event>("attendeePrivileges", "Attendee Privileges") {

            @Override
            public void set(Event event, Integer value) {
                if (null != value && value.intValue() == 1) {
                    event.setAttendeePrivileges(DefaultAttendeePrivileges.MODIFY);
                    return;
                }
                event.setAttendeePrivileges(DefaultAttendeePrivileges.DEFAULT);
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsAttendeePrivileges();
            }

            @Override
            public Integer get(Event event) {
                if (CalendarUtils.hasAttendeePrivileges(event, DefaultAttendeePrivileges.MODIFY)) {
                    return Integer.valueOf(1);
                }
                return Integer.valueOf(0);
            }

            @Override
            public void remove(Event event) {
                event.removeAttendeePrivileges();
            }
        });
        mappings.put(EventField.FILENAME, new VarCharMapping<Event>("filename", "Filename") {

            @Override
            public void set(Event event, String value) {
                event.setFilename(value);
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsFilename();
            }

            @Override
            public String get(Event event) {
                return event.getFilename();
            }

            @Override
            public void remove(Event event) {
                event.removeFilename();
            }
        });
        mappings.put(EventField.EXTENDED_PROPERTIES, new ExtendedPropertiesMapping<Event>("extendedProperties", "Extended Properties") {

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
        return mappings;
	}

}
