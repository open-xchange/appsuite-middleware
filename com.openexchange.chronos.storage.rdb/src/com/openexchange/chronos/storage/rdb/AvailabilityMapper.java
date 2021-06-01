/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.storage.rdb;

import static com.openexchange.java.Autoboxing.I;
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
import com.openexchange.chronos.BusyType;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.service.AvailabilityField;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.database.BigIntMapping;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMultiMapping;
import com.openexchange.groupware.tools.mappings.database.IntegerMapping;
import com.openexchange.groupware.tools.mappings.database.VarCharMapping;
import com.openexchange.java.Strings;

/**
 * {@link AvailabilityMapper}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class AvailabilityMapper extends DefaultDbMapper<Availability, AvailabilityField> {

    private static final AvailabilityMapper INSTANCE = new AvailabilityMapper();

    /**
     * Gets the mapper instance.
     *
     * @return The instance.
     */
    public static AvailabilityMapper getInstance() {
        return INSTANCE;
    }

    /**
     * Initialises a new {@link AvailabilityMapper}.
     */
    private AvailabilityMapper() {
        super();
    }

    @Override
    public Availability newInstance() {
        return new Availability();
    }

    @Override
    public AvailabilityField[] newArray(int size) {
        return new AvailabilityField[size];
    }

    @Override
    protected EnumMap<AvailabilityField, ? extends DbMapping<? extends Object, Availability>> createMappings() {
        EnumMap<AvailabilityField, DbMapping<? extends Object, Availability>> mappings = new EnumMap<AvailabilityField, DbMapping<? extends Object, Availability>>(AvailabilityField.class);
        mappings.put(AvailabilityField.id, new IntegerMapping<Availability>("id", "Availability ID") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.id);
            }

            @Override
            public void set(Availability object, Integer value) throws OXException {
                object.setId(value.toString());
            }

            @Override
            public Integer get(Availability object) {
                return Integer.valueOf(object.getId());
            }

            @Override
            public void remove(Availability object) {
                object.removeId();
            }

        });
        mappings.put(AvailabilityField.uid, new VarCharMapping<Availability>("uid", "Availability UID") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.uid);
            }

            @Override
            public void set(Availability object, String value) throws OXException {
                object.setUid(value);
            }

            @Override
            public String get(Availability object) {
                return object.getUid();
            }

            @Override
            public void remove(Availability object) {
                object.removeUid();
            }

        });
        mappings.put(AvailabilityField.user, new IntegerMapping<Availability>("user", "Calendar User ID") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.user);
            }

            @Override
            public void set(Availability object, Integer value) throws OXException {
                object.setCalendarUser(value.intValue());
            }

            @Override
            public Integer get(Availability object) {
                return I(object.getCalendarUser());
            }

            @Override
            public void remove(Availability object) {
                object.removeCalendarUser();
            }
        });
        mappings.put(AvailabilityField.busytype, new VarCharMapping<Availability>("busyType", "Busy Type") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.busytype);
            }

            @Override
            public void set(Availability object, String value) throws OXException {
                object.setBusyType(BusyType.parseFromString(value));
            }

            @Override
            public String get(Availability object) {
                BusyType busyType = object.getBusyType();
                return busyType == null ? BusyType.BUSY_UNAVAILABLE.getValue() : busyType.getValue();
            }

            @Override
            public void remove(Availability object) {
                object.removeBusyType();
            }
        });
        mappings.put(AvailabilityField.classification, new VarCharMapping<Availability>("class", "Classification") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.classification);
            }

            @Override
            public void set(Availability object, String value) throws OXException {
                object.setClassification(new Classification(value));
            }

            @Override
            public String get(Availability object) {
                Classification classification = object.getClassification();
                return classification == null ? null : classification.getValue();
            }

            @Override
            public void remove(Availability object) {
                object.removeClassification();
            }
        });
        mappings.put(AvailabilityField.created, new BigIntMapping<Availability>("created", "Created") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.created);
            }

            @Override
            public void set(Availability object, Long value) throws OXException {
                object.setCreated(value == null ? null : new Date(value.longValue()));
            }

            @Override
            public Long get(Availability object) {
                Date created = object.getCreated();
                return created == null ? null : L(created.getTime());
            }

            @Override
            public void remove(Availability object) {
                object.removeCreated();
            }
        });
        mappings.put(AvailabilityField.description, new VarCharMapping<Availability>("description", "Description") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.description);
            }

            @Override
            public void set(Availability object, String value) throws OXException {
                object.setDescription(value);
            }

            @Override
            public String get(Availability object) {
                return object.getDescription();
            }

            @Override
            public void remove(Availability object) {
                object.removeDescription();
            }
        });
        mappings.put(AvailabilityField.dtstart, new DateTimeMapping<Availability>("start", "startTimezone", "allDay", "Start DateTime") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.dtstart);
            }

            @Override
            public void set(Availability object, DateTime value) throws OXException {
                object.setStartTime(value);
            }

            @Override
            public DateTime get(Availability object) {
                return object.getStartTime();
            }

            @Override
            public void remove(Availability object) {
                object.removeStartTime();
            }

        });
        mappings.put(AvailabilityField.dtend, new DefaultDbMultiMapping<DateTime, Availability>(new String[] { "end", "endTimezone" }, "End date") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.dtend);
            }

            @Override
            public void set(Availability object, DateTime value) throws OXException {
                object.setEndTime(value);
            }

            @Override
            public DateTime get(Availability object) {
                return object.getEndTime();
            }

            @Override
            public void remove(Availability object) {
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
            public int set(PreparedStatement statement, int parameterIndex, Availability object) throws SQLException {
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
        mappings.put(AvailabilityField.lastModified, new BigIntMapping<Availability>("modified", "Last Modified") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.lastModified);
            }

            @Override
            public void set(Availability object, Long value) throws OXException {
                object.setLastModified(value == null ? null : new Date(value.longValue()));
            }

            @Override
            public Long get(Availability object) {
                return L(object.getLastModified().getTime());
            }

            @Override
            public void remove(Availability object) {
                object.removeLastModified();
            }
        });
        mappings.put(AvailabilityField.location, new VarCharMapping<Availability>("location", "Location") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.location);
            }

            @Override
            public void set(Availability object, String value) throws OXException {
                object.setLocation(value);
            }

            @Override
            public String get(Availability object) {
                return object.getLocation();
            }

            @Override
            public void remove(Availability object) {
                object.removeLocation();
            }
        });
        mappings.put(AvailabilityField.organizer, new VarCharMapping<Availability>("organizer", "Organizer") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.organizer);
            }

            @Override
            public void set(Availability object, String value) throws OXException {
                Organizer organizer = new Organizer();
                organizer.setUri(value);
                object.setOrganizer(organizer);
            }

            @Override
            public String get(Availability object) {
                Organizer organizer = object.getOrganizer();
                return organizer == null ? null : organizer.getUri();
            }

            @Override
            public void remove(Availability object) {
                object.removeOrganizer();
            }
        });
        mappings.put(AvailabilityField.priority, new IntegerMapping<Availability>("priority", "Priority") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.priority);
            }

            @Override
            public void set(Availability object, Integer value) throws OXException {
                object.setPriority(value.intValue());
            }

            @Override
            public Integer get(Availability object) {
                return I(object.getPriority());
            }

            @Override
            public void remove(Availability object) {
                object.removePriority();
            }
        });
        mappings.put(AvailabilityField.seq, new IntegerMapping<Availability>("sequence", "Sequence") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.seq);
            }

            @Override
            public void set(Availability object, Integer value) throws OXException {
                object.setSequence(value == null ? 0 : value.intValue());
            }

            @Override
            public Integer get(Availability object) {
                return I(object.getSequence());
            }

            @Override
            public void remove(Availability object) {
                object.removeSequence();
            }
        });
        mappings.put(AvailabilityField.summary, new VarCharMapping<Availability>("summary", "Summary") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.summary);
            }

            @Override
            public void set(Availability object, String value) throws OXException {
                object.setSummary(value);
            }

            @Override
            public String get(Availability object) {
                return object.getSummary();
            }

            @Override
            public void remove(Availability object) {
                object.removeSummary();
            }
        });
        mappings.put(AvailabilityField.url, new VarCharMapping<Availability>("url", "URL") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.url);
            }

            @Override
            public void set(Availability object, String value) throws OXException {
                object.setUrl(value);
            }

            @Override
            public String get(Availability object) {
                return object.getUrl();
            }

            @Override
            public void remove(Availability object) {
                object.removeUrl();
            }
        });
        mappings.put(AvailabilityField.categories, new VarCharMapping<Availability>("categories", "Categories") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.categories);
            }

            @Override
            public void set(Availability object, String value) throws OXException {
                String[] split = Strings.splitByCommaNotInQuotes(value);
                object.setCategories(split == null ? null : Arrays.asList(split));
            }

            @Override
            public String get(Availability object) {
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
            public void remove(Availability object) {
                object.removeCategories();
            }
        });
        mappings.put(AvailabilityField.comment, new VarCharMapping<Availability>("comment", "Comment") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.comment);
            }

            @Override
            public void set(Availability object, String value) throws OXException {
                String[] split = Strings.splitByCommaNotInQuotes(value);
                object.setComments(split == null ? null : Arrays.asList(split));
            }

            @Override
            public String get(Availability object) {
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
            public void remove(Availability object) {
                object.removeComments();
            }
        });
        mappings.put(AvailabilityField.extendedProperties, new ExtendedPropertiesMapping<Availability>("extendedProperties", "Extended Properties") {

            @Override
            public boolean isSet(Availability object) {
                return object.contains(AvailabilityField.extendedProperties);
            }

            @Override
            public void set(Availability object, ExtendedProperties value) throws OXException {
                object.setExtendedProperties(value);
            }

            @Override
            public ExtendedProperties get(Availability object) {
                return object.getExtendedProperties();
            }

            @Override
            public void remove(Availability object) {
                object.removeExtendedProperties();
            }

        });
        return mappings;
    }

}
