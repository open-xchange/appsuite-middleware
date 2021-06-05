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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMultiMapping;

/**
 * {@link DateTimeMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class DateTimeMapping<O> extends DefaultDbMultiMapping<DateTime, O> {

    /**
     * Initializes a new {@link DateTimeMapping}.
     *
     * @param labelTimestamp The label of the column holding the date's timestamp
     * @param labelTimezone The label of the column holding the date's timezone identifier
     * @param labelAllDay The label of the column holding the date's <i>all-day</i> flag
     * @param readableName the readable name for the mapped field
     */
    protected DateTimeMapping(String labelTimestamp, String labelTimezone, String labelAllDay, String readableName) {
        super(new String[] { labelTimestamp, labelTimezone, labelAllDay }, readableName);
    }

    @Override
    public int set(PreparedStatement statement, int parameterIndex, O object) throws SQLException {
        DateTime value = get(object);
        if (null == value) {
            statement.setNull(parameterIndex, Types.TIMESTAMP);
            statement.setNull(parameterIndex + 1, Types.VARCHAR);
            statement.setNull(parameterIndex + 2, Types.BOOLEAN);
        } else {
            statement.setTimestamp(parameterIndex, new Timestamp(value.getTimestamp()));
            statement.setString(parameterIndex + 1, null == value.getTimeZone() ? null : value.getTimeZone().getID());
            statement.setBoolean(parameterIndex + 2, value.isAllDay());
        }
        return 3;
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
        if (1 == resultSet.getInt(columnLabels[2])) {
            return new DateTime(timestamp.getTime()).toAllDay();
        }
        String timeZoneId = resultSet.getString(columnLabels[1]);
        return new DateTime(CalendarUtils.optTimeZone(timeZoneId, null), timestamp.getTime());
    }

}
