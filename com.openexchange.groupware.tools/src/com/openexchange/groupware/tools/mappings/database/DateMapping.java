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

package com.openexchange.groupware.tools.mappings.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

/**
 * {@link DateMapping} - Database mapping for <code>Types.DATE</code>.
 *
 * @param <O> the type of the object
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DateMapping<O> extends DefaultDbMapping<Date, O> {

	public DateMapping(final String columnName, final String readableName) {
		super(columnName, readableName, Types.TIMESTAMP);
	}

	@Override
	public Date get(final ResultSet resultSet, String columnLabel) throws SQLException {
	    try {
	        Timestamp timestamp = resultSet.getTimestamp(columnLabel);
	        return null != timestamp ? new Date(timestamp.getTime()) : null;
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
	}

	@Override
    public int set(final PreparedStatement statement, final int parameterIndex, final O object) throws SQLException {
		if (this.isSet(object)) {
			final Date value = this.get(object);
			if (null != value) {
				statement.setTimestamp(parameterIndex, new Timestamp(value.getTime()));
			} else {
				statement.setNull(parameterIndex, this.getSqlType());
			}
		} else {
			statement.setNull(parameterIndex, this.getSqlType());
		}
        return 1;
	}

}
