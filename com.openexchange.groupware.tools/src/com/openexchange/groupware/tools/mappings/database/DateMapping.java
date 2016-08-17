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
	        return resultSet.getTimestamp(columnLabel);
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
