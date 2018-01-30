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
import java.util.SortedSet;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.service.AvailableField;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.database.BigIntMapping;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMultiMapping;
import com.openexchange.groupware.tools.mappings.database.IntegerMapping;
import com.openexchange.groupware.tools.mappings.database.VarCharMapping;
import com.openexchange.java.Strings;

/**
 * {@link AvailableMapper}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AvailableMapper extends DefaultDbMapper<Available, AvailableField> {

    private static final AvailableMapper INSTANCE = new AvailableMapper();

    /**
     * Gets the mapper instance
     *
     * @return The mapper instance
     */
    public static AvailableMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Initialises a new {@link AvailableMapper}
     */
    public AvailableMapper() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.tools.mappings.Factory#newInstance()
     */
    @Override
    public Available newInstance() {
        return new Available();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.tools.mappings.ArrayFactory#newArray(int)
     */
    @Override
    public AvailableField[] newArray(int size) {
        return new AvailableField[size];
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.tools.mappings.database.DefaultDbMapper#createMappings()
     */
    @Override
    protected EnumMap<AvailableField, ? extends DbMapping<? extends Object, Available>> createMappings() {
        EnumMap<AvailableField, DbMapping<? extends Object, Available>> mappings = new EnumMap<AvailableField, DbMapping<? extends Object, Available>>(AvailableField.class);
        mappings.put(AvailableField.id, new IntegerMapping<Available>("id", "Available ID") {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.id);
            }

            @Override
            public void set(Available object, Integer value) throws OXException {
                object.setId(Integer.toString(value));
            }

            @Override
            public Integer get(Available object) {
                return Integer.valueOf(object.getId());
            }

            @Override
            public void remove(Available object) {
                object.removeId();
            }

        });
        mappings.put(AvailableField.user, new IntegerMapping<Available>("user", "Calendar User ID") {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.user);
            }

            @Override
            public void set(Available object, Integer value) throws OXException {
                object.setCalendarUser(value);
            }

            @Override
            public Integer get(Available object) {
                return object.getCalendarUser();
            }

            @Override
            public void remove(Available object) {
                object.removeCalendarUser();
            }
        });
        mappings.put(AvailableField.uid, new VarCharMapping<Available>("uid", "Available UID") {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.uid);
            }

            @Override
            public void set(Available object, String value) throws OXException {
                object.setUid(value);
            }

            @Override
            public String get(Available object) {
                return object.getUid();
            }

            @Override
            public void remove(Available object) {
                object.removeUid();
            }

        });
        mappings.put(AvailableField.dtstart, new DateTimeMapping<Available>("start", "startTimezone", "allDay", "Start DateTime") {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.dtstart);
            }

            @Override
            public void set(Available object, DateTime value) throws OXException {
                object.setStartTime(value);
            }

            @Override
            public DateTime get(Available object) {
                return object.getStartTime();
            }

            @Override
            public void remove(Available object) {
                object.removeStartTime();
            }

        });
        mappings.put(AvailableField.dtend, new DefaultDbMultiMapping<DateTime, Available>(new String[] { "end", "endTimezone" }, "End date") {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.dtend);
            }

            @Override
            public void set(Available object, DateTime value) throws OXException {
                object.setEndTime(value);
            }

            @Override
            public DateTime get(Available object) {
                return object.getEndTime();
            }

            @Override
            public void remove(Available object) {
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
            public int set(PreparedStatement statement, int parameterIndex, Available object) throws SQLException {
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
        mappings.put(AvailableField.created, new BigIntMapping<Available>("created", "Created") {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.created);
            }

            @Override
            public void set(Available object, Long value) throws OXException {
                object.setCreated(value == null ? null : new Date(value.longValue()));
            }

            @Override
            public Long get(Available object) {
                Date created = object.getCreated();
                return created == null ? null : created.getTime();
            }

            @Override
            public void remove(Available object) {
                object.removeCreated();
            }
        });
        mappings.put(AvailableField.lastModified, new BigIntMapping<Available>("modified", "Last Modified") {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.lastModified);
            }

            @Override
            public void set(Available object, Long value) throws OXException {
                object.setLastModified(value == null ? null : new Date(value));
            }

            @Override
            public Long get(Available object) {
                return object.getLastModified().getTime();
            }

            @Override
            public void remove(Available object) {
                object.removeLastModified();
            }
        });
        mappings.put(AvailableField.description, new VarCharMapping<Available>("description", "Description") {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.description);
            }

            @Override
            public void set(Available object, String value) throws OXException {
                object.setDescription(value);
                ;
            }

            @Override
            public String get(Available object) {
                return object.getDescription();
            }

            @Override
            public void remove(Available object) {
                object.removeDescription();
            }
        });
        mappings.put(AvailableField.recurid, new BigIntMapping<Available>("recurrence", "Recurrence ID") {

            @Override
            public void set(Available object, Long value) {
                object.setRecurrenceId(null == value ? null : new DefaultRecurrenceId(new DateTime(value.longValue())));
            }

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.recurid);
            }

            @Override
            public Long get(Available object) {
                RecurrenceId value = object.getRecurrenceId();
                return null == value ? null : L(value.getValue().getTimestamp());
            }

            @Override
            public void remove(Available object) {
                object.removeRecurrenceId();
            }
        });
        mappings.put(AvailableField.exdate, new RecurrenceIdListMapping<Available>("exDate", "Delete exceptions") {

            @Override
            public boolean isSet(Available available) {
                return available.contains(AvailableField.exdate);
            }

            @Override
            public void remove(Available available) {
                available.removeDeleteExceptionDates();
            }

            @Override
            protected SortedSet<RecurrenceId> getRecurrenceIds(Available available) {
                return available.getDeleteExceptionDates();
            }

            @Override
            protected void setRecurrenceIds(Available available, SortedSet<RecurrenceId> value) {
                available.setDeleteExceptionDates(value);
            }
        });
        mappings.put(AvailableField.rrule, new VarCharMapping<Available>("rrule", "Recurrence Rule") {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.rrule);
            }

            @Override
            public void set(Available object, String value) throws OXException {
                object.setRecurrenceRule(value);
            }

            @Override
            public String get(Available object) {
                return object.getRecurrenceRule();
            }

            @Override
            public void remove(Available object) {
                object.removeRecurrenceRule();
            }
        });
        mappings.put(AvailableField.summary, new VarCharMapping<Available>("summary", "Summary") {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.summary);
            }

            @Override
            public void set(Available object, String value) throws OXException {
                object.setSummary(value);
            }

            @Override
            public String get(Available object) {
                return object.getSummary();
            }

            @Override
            public void remove(Available object) {
                object.removeSummary();
            }
        });
        mappings.put(AvailableField.categories, new VarCharMapping<Available>("categories", "Categories") {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.categories);
            }

            @Override
            public void set(Available object, String value) throws OXException {
                String[] split = Strings.splitByCommaNotInQuotes(value);
                object.setCategories(split == null ? null : Arrays.asList(split));
            }

            @Override
            public String get(Available object) {
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
            public void remove(Available object) {
                object.removeCategories();
            }
        });
        mappings.put(AvailableField.comment, new VarCharMapping<Available>("comment", "Comment") {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.comment);
            }

            @Override
            public void set(Available object, String value) throws OXException {
                String[] split = Strings.splitByCommaNotInQuotes(value);
                object.setComments(split == null ? null : Arrays.asList(split));
            }

            @Override
            public String get(Available object) {
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
            public void remove(Available object) {
                object.removeComments();
            }
        });
        mappings.put(AvailableField.extendedProperties, new ExtendedPropertiesMapping<Available>("extendedProperties", "Extended Properties") {

            @Override
            public boolean isSet(Available object) {
                return object.contains(AvailableField.extendedProperties);
            }

            @Override
            public void set(Available object, ExtendedProperties value) throws OXException {
                object.setExtendedProperties(value);
            }

            @Override
            public ExtendedProperties get(Available object) {
                return object.getExtendedProperties();
            }

            @Override
            public void remove(Available object) {
                object.removeExtendedProperties();
            }

        });
        return mappings;
    }

}
