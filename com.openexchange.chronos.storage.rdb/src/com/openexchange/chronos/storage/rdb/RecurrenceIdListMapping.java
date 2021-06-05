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

import static com.openexchange.chronos.common.CalendarUtils.areNormalized;
import static com.openexchange.chronos.common.CalendarUtils.encode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapping;
import com.openexchange.java.Strings;

/**
 * {@link RecurrenceIdListMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class RecurrenceIdListMapping<O> extends DefaultDbMapping<SortedSet<RecurrenceId>, O> {

    /**
     * Initializes a new {@link RecurrenceIdListMapping}.
     *
     * @param columnLabel The label of the column holding the value
     * @param readableName The readable name for the mapped field
     */
    protected RecurrenceIdListMapping(String columnLabel, String readableName) {
        super(columnLabel, readableName, Types.VARCHAR);
    }

    @Override
    public int set(PreparedStatement statement, int parameterIndex, O object) throws SQLException {
        if (false == isSet(object)) {
            statement.setNull(parameterIndex, getSqlType());
        } else {
            statement.setString(parameterIndex, serialize(get(object)));
        }
        return 1;
    }

    @Override
    public SortedSet<RecurrenceId> get(ResultSet resultSet, String columnLabel) throws SQLException {
        try {
            return deserialize(resultSet.getString(columnLabel));
        } catch (IllegalArgumentException e) {
            throw new SQLException(e.getMessage(), e);
        }
    }

    /**
     * Serializes a collection recurrence identifiers into a single string prior storing it in the database.
     * 
     * @param recurrenceIds The recurrence identifiers to serialize, or <code>null</code> for pass-through
     * @return The serialized recurrence identifiers
     */
    protected static String serialize(Collection<RecurrenceId> recurrenceIds) {
        if (null == recurrenceIds || recurrenceIds.isEmpty()) {
            return null;
        }
        Iterator<RecurrenceId> iterator = recurrenceIds.iterator();
        StringBuilder stringBuilder;
        if (areNormalized(recurrenceIds)) {
            /*
             * Europe/Berlin:20191206T102800,20191207T102800,20191208T102800,...
             */
            stringBuilder = new StringBuilder(20 + 20 * recurrenceIds.size());
            stringBuilder.append(encode(iterator.next().getValue()));
            while (iterator.hasNext()) {
                stringBuilder.append(',').append(iterator.next().getValue().toString());
            }
        } else {
            /*
             * Europe/Berlin:20191206T102800,Europe/Moscow:20191207T122800,Europe/Lisbon:20191208T092800,...
             */
            stringBuilder = new StringBuilder(40 * recurrenceIds.size());
            stringBuilder.append(encode(iterator.next().getValue()));
            while (iterator.hasNext()) {
                stringBuilder.append(',').append(encode(iterator.next().getValue()));
            }
        }
        return stringBuilder.toString();
    }

    /**
     * De-serializes a set of recurrence identifiers from its serialized format.
     * 
     * @param value The value to de-serialize, or <code>null</code> to pass-through
     * @return The de-serialized recurrence identifiers
     * @throws IllegalArgumentException If the value cannot be parsed
     */
    protected static SortedSet<RecurrenceId> deserialize(String value) {
        if (null == value) {
            return null;
        }
        DateTime[] dateTimes = decode(Strings.splitByComma(value));
        if (0 == dateTimes.length) {
            return Collections.emptySortedSet();
        }
        SortedSet<RecurrenceId> recurrenceIds = new TreeSet<RecurrenceId>();
        recurrenceIds.add(new DefaultRecurrenceId(dateTimes[0]));
        if (1 == dateTimes.length) {
            return recurrenceIds;
        }
        /*
         * check if first timezone is applicable for all values ("normalized" recurrence ids)
         */
        if (false == dateTimes[0].isAllDay() && null != dateTimes[0].getTimeZone() && false == "UTC".equals(dateTimes[0].getTimeZone().getID())) {
            boolean considerNormalized = true;
            for (int i = 1; i < dateTimes.length; i++) {
                if (null != dateTimes[i].getTimeZone() && false == dateTimes[i].isAllDay()) {
                    considerNormalized = false;
                    break;
                }
            }
            if (considerNormalized) {
                for (int i = 1; i < dateTimes.length; i++) {
                    recurrenceIds.add(new DefaultRecurrenceId(dateTimes[i].swapTimeZone(dateTimes[0].getTimeZone())));
                }
                return recurrenceIds;
            }
        }
        /*
         * return set of individual recurrence ids, otherwise
         */
        for (int i = 1; i < dateTimes.length; i++) {
            recurrenceIds.add(new DefaultRecurrenceId(dateTimes[i]));
        }
        return recurrenceIds;
    }

    private static DateTime[] decode(String[] values) {
        if (null == values) {
            return null;
        }
        DateTime[] dateTimes = new DateTime[values.length];
        for (int i = 0; i < values.length; i++) {
            dateTimes[i] = CalendarUtils.decode(values[i]);
        }
        return dateTimes;
    }

}
