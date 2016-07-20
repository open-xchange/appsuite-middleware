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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventStatus;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.database.BigIntMapping;
import com.openexchange.groupware.tools.mappings.database.DateMapping;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
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

    /**
     * Initializes a new {@link EventMapper}.
     */
	public EventMapper() {
		super();
	}

    /**
     * Gets all mapped fields.
     *
     * @return The mapped fields
     */
    public EventField[] getMappedFields() {
        return getMappedFields(null);
    }

    /**
     * Gets the mapped fields out of the supplied requested fields, ignoring unmapped fields.
     *
     * @param requestedFields The requested fields, or <code>null</code> to get all mapped fields
     * @return The mapped fields
     */
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
                event.setId(null == value ? 0 : i(value));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsId();
            }

            @Override
            public Integer get(Event event) {
                return I(event.getId());
            }

            @Override
            public void remove(Event event) {
                event.removeId();
            }
        });
        mappings.put(EventField.PUBLIC_FOLDER_ID, new IntegerMapping<Event>("fid", "Public folder ID") {

            @Override
            public void set(Event event, Integer value) {
                event.setPublicFolderId(null == value ? 0 : i(value));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsPublicFolderId();
            }

            @Override
            public Integer get(Event event) {
                return I(event.getPublicFolderId());
            }

            @Override
            public void remove(Event event) {
                event.removePublicFolderId();
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
                event.setCreatedBy(null == value ? 0 : i(value));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsCreatedBy();
            }

            @Override
            public Integer get(Event event) {
                return I(event.getCreatedBy());
            }

            @Override
            public void remove(Event event) {
                event.removeCreatedBy();
            }
        });
        mappings.put(EventField.LAST_MODIFIED, new BigIntMapping<Event>("changing_date", "Last modified") {

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
        mappings.put(EventField.MODIFIED_BY, new IntegerMapping<Event>("changed_from", "Modified by") {

            @Override
            public void set(Event event, Integer value) {
                event.setModifiedBy(null == value ? 0 : i(value));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsModifiedBy();
            }

            @Override
            public Integer get(Event event) {
                return I(event.getModifiedBy());
            }

            @Override
            public void remove(Event event) {
                event.removeModifiedBy();
            }
        });
        mappings.put(EventField.SUMMARY, new VarCharMapping<Event>("field01", "Summary") {

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
        mappings.put(EventField.LOCATION, new VarCharMapping<Event>("field02", "Location") {

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
        mappings.put(EventField.DESCRIPTION, new VarCharMapping<Event>("field04", "Description") {

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
        mappings.put(EventField.CLASSIFICATION, new IntegerMapping<Event>("pflag", "Classification") {

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
        mappings.put(EventField.COLOR, new IntegerMapping<Event>("intfield03", "Color") {

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
                String value = event.getColor();
                return null == value ? null : Event2Appointment.getColorLabel(value);
            }

            @Override
            public void remove(Event event) {
                event.removeColor();
            }
        });
        mappings.put(EventField.START_DATE, new DateMapping<Event>("timestampfield01", "Start date") {

            @Override
            public void set(Event event, Date value) {
                event.setStartDate(value);
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsStartDate();
            }

            @Override
            public Date get(Event event) {
                return event.getStartDate();
            }

            @Override
            public void remove(Event event) {
                event.removeStartDate();
            }
        });
        mappings.put(EventField.START_TIMEZONE, new VarCharMapping<Event>("timezone", "Start timezone") {

            @Override
            public void set(Event event, String value) {
                event.setStartTimezone(value);
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsStartTimezone();
            }

            @Override
            public String get(Event event) {
                return event.getStartTimezone();
            }

            @Override
            public void remove(Event event) {
                event.removeStartTimezone();
            }
        });
        mappings.put(EventField.END_DATE, new DateMapping<Event>("timestampfield02", "End date") {

            @Override
            public void set(Event event, Date value) {
                event.setEndDate(value);
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsEndDate();
            }

            @Override
            public Date get(Event event) {
                return event.getEndDate();
            }

            @Override
            public void remove(Event event) {
                event.removeEndDate();
            }
        });
        // EventField.END_TIMEZONE
        mappings.put(EventField.ALL_DAY, new IntegerMapping<Event>("intfield07", "All day") {

            @Override
            public void set(Event event, Integer value) {
                event.setAllDay(null != value && 1 == i(value));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsAllDay();
            }

            @Override
            public Integer get(Event event) {
                return I(event.getAllDay() ? 1 : 0);
            }

            @Override
            public void remove(Event event) {
                event.removeAllDay();
            }
        });
        // EventField.TRANSP
        mappings.put(EventField.RECURRENCE_ID, new IntegerMapping<Event>("intfield02", "Recurrence id") {

            @Override
            public void set(Event event, Integer value) {
                event.setRecurrenceId(null == value ? 0 : i(value));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsRecurrenceId();
            }

            @Override
            public Integer get(Event event) {
                return I(event.getRecurrenceId());
            }

            @Override
            public void remove(Event event) {
                event.removeRecurrenceId();
            }
        });
        mappings.put(EventField.RECURRENCE_RULE, new VarCharMapping<Event>("field06", "Recurrence rule") {

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
        mappings.put(EventField.STATUS, new IntegerMapping<Event>("intfield06", "Status") {

            @Override
            public void set(Event event, Integer value) {
                event.setStatus(null == value ? null : Appointment2Event.getEventStatus(i(value)));
            }

            @Override
            public boolean isSet(Event event) {
                return event.containsStatus();
            }

            @Override
            public Integer get(Event event) {
                EventStatus value = event.getStatus();
                return null == value ? null : Event2Appointment.getShownAs(value);
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
        mappings.put(EventField.ORGANIZER, new DefaultDbMultiMapping<Organizer, Event>(
            new String[] { "organizer", "organizerId" }, "Organizer") {

            @Override
            public Organizer get(ResultSet resultSet, String[] columnLabels) throws SQLException {
                String email = resultSet.getString(columnLabels[0]);
                int entity = resultSet.getInt(columnLabels[1]);
                if (resultSet.wasNull()) {
                    if (null == email) {
                        return null;
                    }
                    Organizer organizer = new Organizer();
                    organizer.setUri(Appointment2Event.getURI(email));
                    return organizer;
                } else {
                    Organizer organizer = new Organizer();
                    organizer.setEntity(entity);
                    organizer.setUri(Appointment2Event.getURI(email));
                    return organizer;
                }
            }

            @Override
            public int set(PreparedStatement statement, int parameterIndex, Event event) throws SQLException {
                Organizer value = isSet(event) ? get(event) : null;
                if (null == value) {
                    statement.setNull(parameterIndex, Types.VARCHAR);
                    statement.setNull(1 + parameterIndex, Types.INTEGER);
                } else {
                    statement.setString(parameterIndex, Event2Appointment.getEMailAddress(value.getUri()));
                    statement.setInt(1 + parameterIndex, value.getEntity());
                }
                return 2;
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
        });
        //      EventField.ATTENDEES
        //      EventField.ATTACHMENT
        return mappings;
	}

    private static List<Date> deserializeExceptionDates(String timestamps) throws NumberFormatException {
        if (null == timestamps) {
            return null;
        }
        List<String> splitted = Strings.splitAndTrim(timestamps, ",");
        List<Date> dates = new ArrayList<Date>();
        for (String timestamp : splitted) {
            dates.add(new Date(Long.parseLong(timestamp)));
        }
        return dates;
    }

    private static String serializeExceptionDates(List<Date> dates) {
        if (null == dates) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(15 * dates.size());
        if (0 < dates.size()) {
            stringBuilder.append(dates.get(0).getTime());
        }
        for (int i = 1; i < dates.size(); i++) {
            stringBuilder.append(',').append(dates.get(i).getTime());
        }
        return stringBuilder.toString();
    }

}
