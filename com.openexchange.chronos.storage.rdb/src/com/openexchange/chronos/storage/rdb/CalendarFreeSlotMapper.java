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
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import com.openexchange.chronos.CalendarFreeSlot;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.service.CalendarFreeSlotField;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.database.BigIntMapping;
import com.openexchange.groupware.tools.mappings.database.DateMapping;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapping;
import com.openexchange.groupware.tools.mappings.database.IntegerMapping;
import com.openexchange.groupware.tools.mappings.database.VarCharMapping;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link CalendarFreeSlotMapper}
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CalendarFreeSlotMapper extends DefaultDbMapper<CalendarFreeSlot, CalendarFreeSlotField> {

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
    public CalendarFreeSlotField[] newArray(int size) {
        return new CalendarFreeSlotField[size];
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.tools.mappings.database.DefaultDbMapper#createMappings()
     */
    @Override
    protected EnumMap<CalendarFreeSlotField, ? extends DbMapping<? extends Object, CalendarFreeSlot>> createMappings() {
        EnumMap<CalendarFreeSlotField, DbMapping<? extends Object, CalendarFreeSlot>> mappings = new EnumMap<CalendarFreeSlotField, DbMapping<? extends Object, CalendarFreeSlot>>(CalendarFreeSlotField.class);
        mappings.put(CalendarFreeSlotField.id, new IntegerMapping<CalendarFreeSlot>("id", "Free Slot ID") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(CalendarFreeSlotField.id);
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
        mappings.put(CalendarFreeSlotField.calendarAvailabilityId, new IntegerMapping<CalendarFreeSlot>("calendarAvailability", "Calendar Availability Parent ID") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(CalendarFreeSlotField.calendarAvailabilityId);
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
        mappings.put(CalendarFreeSlotField.user, new IntegerMapping<CalendarFreeSlot>("user", "Calendar User ID") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(CalendarFreeSlotField.user);
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
        mappings.put(CalendarFreeSlotField.uid, new VarCharMapping<CalendarFreeSlot>("uid", "Free Slot UID") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(CalendarFreeSlotField.uid);
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
        mappings.put(CalendarFreeSlotField.dtstart, new DateMapping<CalendarFreeSlot>("start", "Start DateTime") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(CalendarFreeSlotField.dtstart);
            }

            @Override
            public void set(CalendarFreeSlot object, Date value) throws OXException {
                object.setStartTime(value);
            }

            @Override
            public Date get(CalendarFreeSlot object) {
                return object.getStartTime();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeStartTime();
            }

        });
        mappings.put(CalendarFreeSlotField.dtend, new DateMapping<CalendarFreeSlot>("end", "End DateTime") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(CalendarFreeSlotField.dtend);
            }

            @Override
            public void set(CalendarFreeSlot object, Date value) throws OXException {
                object.setEndTime(value);
            }

            @Override
            public Date get(CalendarFreeSlot object) {
                return object.getEndTime();
            }

            @Override
            public void remove(CalendarFreeSlot object) {
                object.removeEndTime();
            }
        });
        mappings.put(CalendarFreeSlotField.created, new BigIntMapping<CalendarFreeSlot>("created", "Created") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(CalendarFreeSlotField.created);
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
        mappings.put(CalendarFreeSlotField.lastModified, new BigIntMapping<CalendarFreeSlot>("modified", "Last Modified") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(CalendarFreeSlotField.lastModified);
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
        mappings.put(CalendarFreeSlotField.description, new VarCharMapping<CalendarFreeSlot>("description", "Description") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(CalendarFreeSlotField.description);
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
        mappings.put(CalendarFreeSlotField.recurid, new BigIntMapping<CalendarFreeSlot>("recurrence", "Recurrence ID") {

            @Override
            public void set(CalendarFreeSlot object, Long value) {
                object.setRecurrenceId(null == value ? null : new DefaultRecurrenceId(value.longValue()));
            }

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(CalendarFreeSlotField.recurid);
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
        mappings.put(CalendarFreeSlotField.rrule, new VarCharMapping<CalendarFreeSlot>("rrule", "Recurrence Rule") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(CalendarFreeSlotField.rrule);
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
        mappings.put(CalendarFreeSlotField.summary, new VarCharMapping<CalendarFreeSlot>("summary", "Summary") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(CalendarFreeSlotField.summary);
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
        mappings.put(CalendarFreeSlotField.categories, new VarCharMapping<CalendarFreeSlot>("categories", "Categories") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(CalendarFreeSlotField.categories);
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
        mappings.put(CalendarFreeSlotField.comment, new VarCharMapping<CalendarFreeSlot>("comment", "Comment") {

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(CalendarFreeSlotField.comment);
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
        mappings.put(CalendarFreeSlotField.extendedProperties, new DefaultDbMapping<ExtendedProperties, CalendarFreeSlot>("extendedProperties", "Extended Properties", Types.BLOB) {

            @Override
            public int set(PreparedStatement statement, int parameterIndex, CalendarFreeSlot object) throws SQLException {
                ExtendedProperties value = get(object);
                if (null == value) {
                    statement.setNull(parameterIndex, getSqlType());
                } else {
                    try {
                        byte[] data = ExtendedPropertiesCodec.encode(value);
                        statement.setBinaryStream(parameterIndex, Streams.newByteArrayInputStream(data), data.length);
                    } catch (IOException e) {
                        throw new SQLException(e);
                    }
                }
                return 1;
            }

            @Override
            public ExtendedProperties get(ResultSet resultSet, String columnLabel) throws SQLException {
                InputStream inputStream = null;
                try {
                    inputStream = resultSet.getBinaryStream(columnLabel);
                    return ExtendedPropertiesCodec.decode(inputStream);
                } catch (IOException e) {
                    throw new SQLException(e);
                } finally {
                    Streams.close(inputStream);
                }
            }

            @Override
            public boolean isSet(CalendarFreeSlot object) {
                return object.contains(CalendarFreeSlotField.extendedProperties);
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
