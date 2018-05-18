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
