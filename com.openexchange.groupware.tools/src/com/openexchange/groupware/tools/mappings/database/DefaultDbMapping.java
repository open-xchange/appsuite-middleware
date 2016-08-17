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
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.DefaultMapping;

/**
 * {@link DefaultDbMapping} - Abstract {@link DbMapping} implementation.
 *
 * @param <T> the type of the property
 * @param <O> the type of the object
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DefaultDbMapping<T, O> extends DefaultMapping<T, O> implements DbMapping<T, O> {

	private final String columnLabel;
	private final String readableName;
	private final int sqlType;

	/**
	 * Initializes a new {@link DefaultDbMapping}.
	 *
	 * @param columnLabel the column label
	 * @param sqlType the SQL type
	 */
	public DefaultDbMapping(final String columnLabel, final int sqlType) {
		this(columnLabel, null, sqlType);
	}

	/**
	 * Initializes a new {@link DefaultDbMapping}.
	 *
	 * @param columnLabel the column label
	 * @param readableName the readable label
	 * @param sqlType the SQL type
	 */
	public DefaultDbMapping(final String columnLabel, final String readableName, final int sqlType) {
		super();
		this.sqlType = sqlType;
		this.columnLabel = columnLabel;
		this.readableName = readableName;
	}

	@Override
    public int set(final PreparedStatement statement, final int parameterIndex, final O object) throws SQLException {
		if (this.isSet(object)) {
			final T value = this.get(object);
			if (null != value) {
				statement.setObject(parameterIndex, value, this.getSqlType());
			} else {
				statement.setNull(parameterIndex, this.getSqlType());
			}
		} else {
			statement.setNull(parameterIndex, this.getSqlType());
		}
        return 1;
	}

    @Override
    public void set(final ResultSet resultSet, final O object) throws SQLException, OXException {
        set(resultSet, object, getColumnLabel());
    }

    @Override
    public void set(final ResultSet resultSet, final O object, String columnLabel) throws SQLException, OXException {
        final T value = this.get(resultSet, columnLabel);
        if (false == resultSet.wasNull()) {
            this.set(object, value);
        }
    }

    @Override
    public String getColumnLabel() {
        return this.columnLabel;
    }

    @Override
    public String getColumnLabel(String prefix) {
        return prefix + this.columnLabel;
    }

	@Override
	public String getReadableName(O Object) {
		return this.readableName;
	}

	@Override
	public int getSqlType() {
		return this.sqlType;
	}

	@Override
	public String toString() {
		return String.format("[%s] %s", columnLabel, readableName);
	}

}
