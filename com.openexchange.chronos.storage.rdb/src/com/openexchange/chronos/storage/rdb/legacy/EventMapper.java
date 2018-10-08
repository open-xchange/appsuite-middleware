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

package com.openexchange.chronos.storage.rdb.legacy;

import static com.openexchange.chronos.compat.Appointment2Event.asString;
import static com.openexchange.chronos.compat.Event2Appointment.asInteger;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.i;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.CalendarStrings;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.storage.rdb.DateTimeMapping;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.database.BigIntMapping;
import com.openexchange.groupware.tools.mappings.database.DateMapping;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMultiMapping;
import com.openexchange.groupware.tools.mappings.database.IntegerMapping;
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
        mappings.put(EventField.ID, new IntegerMapping<Event>("intfield01", "Object ID") {

            @Override
            public void set(Event event, Integer value) {
                event.setId(asString(value, true));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsId();
            }

            @Override
            public Integer get(Event event) {
                return asInteger(event.getId(), true);
            }

            @Override
            public void remove(Event event) {
                event.removeId();
            }
        });
        mappings.put(EventField.FOLDER_ID, new IntegerMapping<Event>("fid", "Folder ID") {

            @Override
            public void set(Event event, Integer value) {
                event.setFolderId(asString(value, true));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsFolderId();
            }

            @Override
            public Integer get(Event event) {
                return asInteger(event.getFolderId(), true);
            }

            @Override
            public void remove(Event event) {
                event.removeFolderId();
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
        mappings.put(EventField.TIMESTAMP, new BigIntMapping<Event>("changing_date", "Timestamp") {

            @Override
            public void set(Event event, Long value) {
                event.setTimestamp(null == value ? 0 : value.longValue());
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
        mappings.put(EventField.CREATED, new DateMapping<Event>("creating_date", "Created") {

            @Override
            public void set(Event event, Date value) {
                event.setCreated(value);
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsCreated();
            }

            @Override
            public Date get(Event event) {
                return event.getCreated();
            }

            @Override
            public void remove(Event event) {
                event.removeCreated();
            }
        });
        mappings.put(EventField.CREATED_BY, new IntegerMapping<Event>("created_from", "Created by") {

            @Override
            public void set(Event event, Integer value) {
                if (null == value || 0 >= value.intValue()) {
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
        mappings.put(EventField.MODIFIED_BY, new IntegerMapping<Event>("changed_from", "Modified by") {

            @Override
            public void set(Event event, Integer value) {
                if (null == value || 0 >= value.intValue()) {
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
        mappings.put(EventField.SUMMARY, new VarCharMapping<Event>("field01", CalendarStrings.FIELD_SUMMARY) {

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
        mappings.put(EventField.LOCATION, new VarCharMapping<Event>("field02", CalendarStrings.FIELD_LOCATION) {

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
        mappings.put(EventField.DESCRIPTION, new VarCharMapping<Event>("field04", CalendarStrings.FIELD_DESCRIPTION) {

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
        mappings.put(EventField.CATEGORIES, new VarCharMapping<Event>("field09", "Categories") {

            @Override
            public void set(Event event, String value) {
                event.setCategories(Appointment2Event.getCategories(value));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsCategories();
            }

            @Override
            public String get(Event event) {
                return Event2Appointment.getCategories(event.getCategories());
            }

            @Override
            public void remove(Event event) {
                event.removeCategories();
            }
        });
        mappings.put(EventField.CLASSIFICATION, new IntegerMapping<Event>("pflag", CalendarStrings.FIELD_CLASSIFICATION) {

            @Override
            public void set(Event event, Integer value) {
                event.setClassification(null == value ? null : Appointment2Event.getClassification(1 == i(value)));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsClassification();
            }

            @Override
            public Integer get(Event event) {
                Classification value = event.getClassification();
                return null == value ? null : Event2Appointment.getPrivateFlag(value) ? I(1) : I(0);
            }

            @Override
            public void remove(Event event) {
                event.removeClassification();
            }

            @Override
            public int set(PreparedStatement statement, int parameterIndex, Event event) throws SQLException {
                // special handling; Column 'pflag' cannot be null
                Integer value = get(event);
                statement.setInt(parameterIndex, null != value ? i(value) : 0);
                return 1;
            }
        });
        mappings.put(EventField.COLOR, new IntegerMapping<Event>("intfield03", CalendarStrings.FIELD_COLOR) {

            @Override
            public void set(Event event, Integer value) {
                event.setColor(null == value ? null : Appointment2Event.getColor(i(value)));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsColor();
            }

            @Override
            public Integer get(Event event) {
                return I(Event2Appointment.getColorLabel(event.getColor()));
            }

            @Override
            public void remove(Event event) {
                event.removeColor();
            }
        });
        mappings.put(EventField.START_DATE, new DateTimeMapping<Event>("timestampfield01", "timezone", "intfield07", CalendarStrings.FIELD_START_DATE) {

            @Override
            public int set(PreparedStatement statement, int parameterIndex, Event event) throws SQLException {
                DateTime value = get(event);
                if (null != value && null == value.getTimeZone()) {
                    statement.setTimestamp(parameterIndex, new Timestamp(value.getTimestamp()));
                    statement.setString(parameterIndex + 1, "");
                    statement.setInt(parameterIndex + 2, value.isAllDay() ? 1 : 0);
                    return 3;
                }
                return super.set(statement, parameterIndex, event);
            }

            @Override
            public void set(Event event, DateTime value) {
                event.setStartDate(value);
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsStartDate();
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
        mappings.put(EventField.END_DATE, new DateMapping<Event>("timestampfield02", CalendarStrings.FIELD_END_DATE) {

            @Override
            public boolean isSet(Event event) {
                return event.containsEndDate();
            }

            @Override
            public void remove(Event event) {
                event.removeEndDate();
            }

            @Override
            public void set(Event event, Date value) throws OXException {
                event.setEndDate(null == value ? null : new DateTime(value.getTime()));
            }

            @Override
            public Date get(Event event) {
                DateTime value = event.getEndDate();
                return null == value ? null : new Date(value.getTimestamp());
            }
        });
        mappings.put(EventField.TRANSP, new IntegerMapping<Event>("intfield06", CalendarStrings.FIELD_TRANSP) {

            @Override
            public void set(Event event, Integer value) {
                event.setTransp(null == value ? null : Appointment2Event.getTransparency(i(value)));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsTransp();
            }

            @Override
            public Integer get(Event event) {
                Transp value = event.getTransp();
                return null == value ? null : I(Event2Appointment.getShownAs(value));
            }

            @Override
            public int set(PreparedStatement statement, int parameterIndex, Event event) throws SQLException {
                // column is NOT NULL, so avoid setting SQL NULL here
                Integer value = get(event);
                statement.setInt(parameterIndex, null == value ? 0 : value.intValue());
                return 1;
            }

            @Override
            public void remove(Event event) {
                event.removeStatus();
            }
        });
        mappings.put(EventField.SERIES_ID, new IntegerMapping<Event>("intfield02", "Series id") {

            @Override
            public void set(Event event, Integer value) {
                event.setSeriesId(asString(value, true));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsSeriesId();
            }

            @Override
            public Integer get(Event event) {
                return asInteger(event.getSeriesId(), false);
            }

            @Override
            public void remove(Event event) {
                event.removeSeriesId();
            }
        });
        mappings.put(EventField.RECURRENCE_ID, new DefaultDbMapping<RecurrenceId, Event>("intfield05", "Recurrence id", Types.INTEGER) {

            @Override
            public RecurrenceId get(ResultSet resultSet, String columnLabel) throws SQLException {
                int recurrencePosition = resultSet.getInt(columnLabel);
                if (resultSet.wasNull() || 0 >= recurrencePosition) {
                    return null;
                }
                return new StoredRecurrenceId(recurrencePosition);
            }

            @Override
            public int set(PreparedStatement statement, int parameterIndex, Event event) throws SQLException {
                RecurrenceId value = isSet(event) ? get(event) : null;
                if (null == value) {
                    statement.setNull(parameterIndex, Types.INTEGER);
                } else if (StoredRecurrenceId.class.isInstance(value)) {
                    statement.setInt(parameterIndex, ((StoredRecurrenceId) value).getRecurrencePosition());
                } else {
                    throw new IllegalArgumentException("Unable to set recurrence id \"" + value + "\" in prepared statement");
                }
                return 1;
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsRecurrenceId();
            }

            @Override
            public void set(Event event, RecurrenceId value) throws OXException {
                event.setRecurrenceId(value);
            }

            @Override
            public RecurrenceId get(Event event) {
                return event.getRecurrenceId();
            }

            @Override
            public void remove(Event event) {
                event.removeRecurrenceId();
            }
        });
        mappings.put(EventField.RECURRENCE_RULE, new DefaultDbMultiMapping<String, Event>(new String[] { "field06", "intfield04" }, CalendarStrings.FIELD_RECURRENCE_RULE) {

            @Override
            public String get(ResultSet resultSet, String[] columnLabels) throws SQLException {
                String value = resultSet.getString(columnLabels[0]);
                if (null == value) {
                    return null;
                }
                int absoluteDuration = resultSet.getInt(columnLabels[1]);
                return absoluteDuration + "~" + value;
            }

            @Override
            public int set(PreparedStatement statement, int parameterIndex, Event event) throws SQLException {
                String value = isSet(event) ? get(event) : null;
                if (null == value) {
                    statement.setNull(parameterIndex, Types.VARCHAR);
                    statement.setNull(1 + parameterIndex, Types.INTEGER);
                } else {
                    int idx = value.indexOf('~');
                    int absoluteDuration = Integer.parseInt(value.substring(0, idx));
                    String pattern = value.substring(idx + 1);
                    statement.setString(parameterIndex, pattern);
                    statement.setInt(1 + parameterIndex, absoluteDuration);
                }
                return 2;
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsRecurrenceRule();
            }

            @Override
            public void set(Event event, String value) throws OXException {
                event.setRecurrenceRule(value);
            }

            @Override
            public String get(Event event) {
                return event.getRecurrenceRule();
            }

            @Override
            public void remove(Event event) {
                event.removeRecurrenceRule();
            }

            @Override
            public String getColumnLabel() {
                return getColumnLabels()[0];
            }

            @Override
            public String getColumnLabel(String prefix) {
                return getColumnLabels(prefix)[0];
            }

            @Override
            public int getSqlType() {
                return Types.VARCHAR;
            }
        });
        mappings.put(EventField.CHANGE_EXCEPTION_DATES, new VarCharMapping<Event>("field08", "Change exception dates") {

            @Override
            public void set(Event event, String value) {
                event.setChangeExceptionDates(deserializeExceptionDates(value));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsChangeExceptionDates();
            }

            @Override
            public String get(Event event) {
                return serializeExceptionDates(event.getChangeExceptionDates());
            }

            @Override
            public void remove(Event event) {
                event.removeChangeExceptionDates();
            }
        });
        mappings.put(EventField.DELETE_EXCEPTION_DATES, new VarCharMapping<Event>("field07", "Delete exception dates") {

            @Override
            public void set(Event event, String value) {
                event.setDeleteExceptionDates(deserializeExceptionDates(value));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsDeleteExceptionDates();
            }

            @Override
            public String get(Event event) {
                return serializeExceptionDates(event.getDeleteExceptionDates());
            }

            @Override
            public void remove(Event event) {
                event.removeDeleteExceptionDates();
            }
        });
        // EventField.STATUS
        mappings.put(EventField.ORGANIZER, new DefaultDbMultiMapping<Organizer, Event>(
            new String[] { "organizer", "organizerId", "principal", "principalId" }, "Organizer") {

            @Override
            public Organizer get(ResultSet resultSet, String[] columnLabels) throws SQLException {
                Organizer organizer = new Organizer();
                String storedOrganizer = resultSet.getString(columnLabels[0]);
                int storedOrganizerId = resultSet.getInt(columnLabels[1]);
                String storedPrincipal = resultSet.getString(columnLabels[2]);
                int storedPrincipalId = resultSet.getInt(columnLabels[3]);
                if (0 < storedOrganizerId && storedOrganizerId == storedPrincipalId ||
                    0 == storedOrganizerId && Strings.isNotEmpty(storedOrganizer) && storedOrganizer.equals(storedPrincipal) ||
                    0 == storedPrincipalId && Strings.isEmpty(storedPrincipal)) {
                    /*
                     * no different "sent-by" user, take over stored values
                     */
                    organizer.setEntity(storedOrganizerId);
                    organizer.setUri(Appointment2Event.getURI(storedOrganizer));
                } else if (null != storedOrganizer || 0 < storedOrganizerId) {
                    /*
                     * organizer with sent-by, use stored principal as organizer and stored organizer as "sent-by":
                     * database/legacy: organizer ~ acting user , principal ~ calendar owner
                     * iCal/chronos:    organizer ~ calendar owner , sent-by ~ user acting on behalf of organizer
                     */
                    organizer.setEntity(storedPrincipalId);
                    organizer.setUri(Appointment2Event.getURI(storedPrincipal));
                    CalendarUser sentBy = new CalendarUser();
                    sentBy.setEntity(storedOrganizerId);
                    sentBy.setUri(Appointment2Event.getURI(storedOrganizer));
                    organizer.setSentBy(sentBy);
                } else {
                    /*
                     * malformed organizer data, take over values from stored principal
                     */
                    organizer.setEntity(storedPrincipalId);
                    organizer.setUri(Appointment2Event.getURI(storedPrincipal));
                }
                return null == organizer.getUri() && 0 == organizer.getEntity() ? null : organizer;
            }

            @Override
            public int set(PreparedStatement statement, int parameterIndex, Event event) throws SQLException {
                Organizer value = isSet(event) ? get(event) : null;
                if (null == value) {
                    statement.setNull(parameterIndex, Types.VARCHAR);
                    statement.setNull(1 + parameterIndex, Types.INTEGER);
                    statement.setNull(2 + parameterIndex, Types.VARCHAR);
                    statement.setNull(3 + parameterIndex, Types.INTEGER);
                } else if (null != value.getSentBy()) {
                    /*
                     * organizer with sent-by, store organizer as principal and "sent-by" in organizer columns
                     * database/legacy: organizer ~ acting user , principal ~ calendar owner
                     * iCal/chronos:    organizer ~ calendar owner , sent-by ~ user acting on behalf of organizer
                     */
                    statement.setString(parameterIndex, Event2Appointment.getEMailAddress(value.getSentBy().getUri()));
                    statement.setInt(1 + parameterIndex, 0 > value.getSentBy().getEntity() ? 0 : value.getSentBy().getEntity());
                    statement.setString(2 + parameterIndex, Event2Appointment.getEMailAddress(value.getUri()));
                    statement.setInt(3 + parameterIndex, 0 > value.getEntity() ? 0 : value.getEntity());
                } else {
                    /*
                     * no different "sent-by" user, store in organizer columns
                     */
                    statement.setString(parameterIndex, Event2Appointment.getEMailAddress(value.getUri()));
                    statement.setInt(1 + parameterIndex, 0 > value.getEntity() ? 0 : value.getEntity());
                    statement.setNull(2 + parameterIndex, Types.VARCHAR);
                    statement.setNull(3 + parameterIndex, Types.INTEGER);
                }
                return 4;
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsOrganizer();
            }

            @Override
            public void set(Event event, Organizer value) throws OXException {
                event.setOrganizer(value);
            }

            @Override
            public Organizer get(Event event) {
                return event.getOrganizer();
            }

            @Override
            public void remove(Event event) {
                event.removeOrganizer();
            }

            @Override
            public int getSqlType() {
                return Types.NULL;
            }
        });
        //      EventField.ATTENDEES
        //      EventField.ATTACHMENT
        return mappings;
	}

    private static SortedSet<RecurrenceId> deserializeExceptionDates(String timestamps) throws NumberFormatException {
        if (null == timestamps) {
            return null;
        }
        List<String> splitted = Strings.splitAndTrim(timestamps, ",");
        SortedSet<RecurrenceId> dates = new TreeSet<RecurrenceId>();
        for (String timestamp : splitted) {
            dates.add(new DefaultRecurrenceId(new DateTime(Long.parseLong(timestamp))));
        }
        return dates;
    }

    private static String serializeExceptionDates(SortedSet<RecurrenceId> dates) {
        if (null == dates || 0 == dates.size()) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(15 * dates.size());
        Iterator<RecurrenceId> iterator = dates.iterator();
        stringBuilder.append(iterator.next().getValue().getTimestamp());
        while (iterator.hasNext()) {
            stringBuilder.append(',').append(iterator.next().getValue().getTimestamp());
        }
        return stringBuilder.toString();
    }

}
