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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

import static com.openexchange.java.Autoboxing.L;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.CalendarFreeSlot;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.service.FreeSlotField;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.database.BigIntMapping;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMultiMapping;
import com.openexchange.groupware.tools.mappings.database.IntegerMapping;
import com.openexchange.groupware.tools.mappings.database.VarCharMapping;
import com.openexchange.java.Strings;

/**
 * {@link CalendarFreeSlotMapper}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CalendarFreeSlotMapper extends DefaultDbMapper<CalendarFreeSlot, FreeSlotField> {

    private static final CalendarFreeSlotMapper INSTANCE = new CalendarFreeSlotMapper();

    /**
     * Gets the mapper instance
     *
     * @return The mapper instance
     */
    public static CalendarFreeSlotMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Initialises a new {@link CalendarFreeSlotMapper}
     */
    public CalendarFreeSlotMapper() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.tools.mappings.Factory#newInstance()
     */
    @Override
    public CalendarFreeSlot newInstance() {
        return new CalendarFreeSlot();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.tools.mappings.ArrayFactory#newArray(int)
     */
    @Override
    public FreeSlotField[] newArray(int size) {
        return new FreeSlotField[size];
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.tools.mappings.database.DefaultDbMapper#createMappings()
     */
    @Override
    protected EnumMap<FreeSlotField, ? extends DbMapping<? extends Object, CalendarFreeSlot>> createMappings() {
        EnumMap<FreeSlotField, DbMapping<? extends Object, CalendarFreeSlot>> mappings = new EnumMap<FreeSlotField, DbMapping<? extends Object, CalendarFreeSlot>>(FreeSlotField.class);
        mappings.put(FreeSlotField.id, new IntegerMapping<CalendarFreeSlot>("id", "Free Slot ID") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.id);
            }

            @Override
            public void set(CalendarFreeSlot object, Integer value) throws OXException {
                object.setId(Integer.toString(value));
            }

            @Override
            public Integer get(CalendarFreeSlot object) {
                return Integer.valueOf(object.getId());
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeId();
            }

        });
        mappings.put(FreeSlotField.calendarAvailabilityId, new IntegerMapping<CalendarFreeSlot>("calendarAvailability", "Calendar Availability Parent ID") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.calendarAvailabilityId);
            }

            @Override
            public void set(CalendarFreeSlot object, Integer value) throws OXException {
                object.setCalendarAvailabilityId(Integer.toString(value));
            }

            @Override
            public Integer get(CalendarFreeSlot object) {
                return Integer.valueOf(object.getCalendarAvailabilityId());
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeCalendarAvailabilityId();
            }
        });
        mappings.put(FreeSlotField.user, new IntegerMapping<CalendarFreeSlot>("user", "Calendar User ID") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.user);
            }

            @Override
            public void set(CalendarFreeSlot object, Integer value) throws OXException {
                object.setCalendarUser(value);
            }

            @Override
            public Integer get(CalendarFreeSlot object) {
                return object.getCalendarUser();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeCalendarUser();
            }
        });
        mappings.put(FreeSlotField.uid, new VarCharMapping<CalendarFreeSlot>("uid", "Free Slot UID") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.uid);
            }

            @Override
            public void set(CalendarFreeSlot object, String value) throws OXException {
                object.setUid(value);
            }

            @Override
            public String get(CalendarFreeSlot object) {
                return object.getUid();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeUid();
            }

        });
        mappings.put(FreeSlotField.dtstart, new DateTimeMapping<CalendarFreeSlot>("start", "startTimezone", "allDay", "Start DateTime") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.dtstart);
            }

            @Override
            public void set(CalendarFreeSlot object, DateTime value) throws OXException {
                object.setStartTime(value);
            }

            @Override
            public DateTime get(CalendarFreeSlot object) {
                return object.getStartTime();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeStartTime();
            }

        });
        mappings.put(FreeSlotField.dtend, new DefaultDbMultiMapping<DateTime, CalendarFreeSlot>(new String[] { "end", "endTimezone" }, "End date") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.dtend);
            }

            @Override
            public void set(CalendarFreeSlot object, DateTime value) throws OXException {
                object.setEndTime(value);
            }

            @Override
            public DateTime get(CalendarFreeSlot object) {
                return object.getEndTime();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeEndTime();
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
            public int set(PreparedStatement statement, int parameterIndex, CalendarFreeSlot object) throws SQLException {
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
        });
        mappings.put(FreeSlotField.created, new BigIntMapping<CalendarFreeSlot>("created", "Created") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.created);
            }

            @Override
            public void set(CalendarFreeSlot object, Long value) throws OXException {
                object.setCreated(value == null ? null : new Date(value.longValue()));
            }

            @Override
            public Long get(CalendarFreeSlot object) {
                Date created = object.getCreated();
                return created == null ? null : created.getTime();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeCreated();
            }
        });
        mappings.put(FreeSlotField.lastModified, new BigIntMapping<CalendarFreeSlot>("modified", "Last Modified") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.lastModified);
            }

            @Override
            public void set(CalendarFreeSlot object, Long value) throws OXException {
                object.setLastModified(value == null ? null : new Date(value));
            }

            @Override
            public Long get(CalendarFreeSlot object) {
                return object.getLastModified().getTime();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeLastModified();
            }
        });
        mappings.put(FreeSlotField.description, new VarCharMapping<CalendarFreeSlot>("description", "Description") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.description);
            }

            @Override
            public void set(CalendarFreeSlot object, String value) throws OXException {
                object.setDescription(value);
                ;
            }

            @Override
            public String get(CalendarFreeSlot object) {
                return object.getDescription();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeDescription();
            }
        });
        mappings.put(FreeSlotField.recurid, new BigIntMapping<CalendarFreeSlot>("recurrence", "Recurrence ID") {

            @Override
            public void set(CalendarFreeSlot object, Long value) {
                object.setRecurrenceId(null == value ? null : new DefaultRecurrenceId(value.longValue()));
            }

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.recurid);
            }

            @Override
            public Long get(CalendarFreeSlot object) {
                RecurrenceId value = object.getRecurrenceId();
                return null == value ? null : L(value.getValue());
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeRecurrenceId();
            }
        });
        mappings.put(FreeSlotField.rrule, new VarCharMapping<CalendarFreeSlot>("rrule", "Recurrence Rule") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.rrule);
            }

            @Override
            public void set(CalendarFreeSlot object, String value) throws OXException {
                object.setRecurrenceRule(value);
                ;
            }

            @Override
            public String get(CalendarFreeSlot object) {
                return object.getRecurrenceRule();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeRecurrenceRule();
            }
        });
        mappings.put(FreeSlotField.summary, new VarCharMapping<CalendarFreeSlot>("summary", "Summary") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.summary);
            }

            @Override
            public void set(CalendarFreeSlot object, String value) throws OXException {
                object.setSummary(value);
            }

            @Override
            public String get(CalendarFreeSlot object) {
                return object.getSummary();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeSummary();
            }
        });
        mappings.put(FreeSlotField.categories, new VarCharMapping<CalendarFreeSlot>("categories", "Categories") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.categories);
            }

            @Override
            public void set(CalendarFreeSlot object, String value) throws OXException {
                String[] split = Strings.splitByCommaNotInQuotes(value);
                object.setCategories(split == null ? null : Arrays.asList(split));
            }

            @Override
            public String get(CalendarFreeSlot object) {
                List<String> categories = object.getCategories();
                if (categories == null || categories.size() == 0) {
                    return null;
                }

                StringBuilder stringBuilder = new StringBuilder();
                for (String category : categories) {
                    stringBuilder.append(category).append(",");
                }
                stringBuilder.setLength(stringBuilder.length() - 1);

                return stringBuilder.toString();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeCategories();
            }
        });
        mappings.put(FreeSlotField.comment, new VarCharMapping<CalendarFreeSlot>("comment", "Comment") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.comment);
            }

            @Override
            public void set(CalendarFreeSlot object, String value) throws OXException {
                String[] split = Strings.splitByCommaNotInQuotes(value);
                object.setComments(split == null ? null : Arrays.asList(split));
            }

            @Override
            public String get(CalendarFreeSlot object) {
                List<String> comments = object.getComments();
                if (comments == null || comments.size() == 0) {
                    return null;
                }

                StringBuilder stringBuilder = new StringBuilder();
                for (String comment : comments) {
                    stringBuilder.append(comment).append(",");
                }
                stringBuilder.setLength(stringBuilder.length() - 1);

                return stringBuilder.toString();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeComments();
            }
        });
        mappings.put(FreeSlotField.extendedProperties, new ExtendedPropertiesMapping<CalendarFreeSlot>("extendedProperties", "Extended Properties") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(FreeSlotField.extendedProperties);
            }

            @Override
            public void set(CalendarFreeSlot object, ExtendedProperties value) throws OXException {
                object.setExtendedProperties(value);
            }

            @Override
            public ExtendedProperties get(CalendarFreeSlot object) {
                return object.getExtendedProperties();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeExtendedProperties();
            }

        });
        return mappings;
    }

}
