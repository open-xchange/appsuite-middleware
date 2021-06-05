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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import com.openexchange.exception.OXException;

/**
 * {@link VarCharMapping} - Database mapping for <code>Types.VARCHAR</code>.
 *
 * @param <O> the type of the object *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class VarCharMapping<O> extends DefaultDbMapping<String, O> {

	public VarCharMapping(final String columnName, final String readableName) {
		super(columnName, readableName, Types.VARCHAR);
	}

	@Override
	public String get(final ResultSet resultSet, String columnLabel) throws SQLException {
		return resultSet.getString(columnLabel);
	}

    @Override
    public void validate(O object) throws OXException {
        validateString(get(object));
    }

	@Override
	public boolean truncate(O object, int length) throws OXException {
		final String value = this.get(object);
		if (null != value && length < value.length()) {
			this.set(object, value.substring(0, length));
			return true;
		}
		return false;
	}

    @Override
    public boolean replaceAll(O object, String regex, String replacement) throws OXException {
        String value = get(object);
        if (null != value) {
            String replacedValue = value.replaceAll(regex, replacement);
            if (false == value.equals(replacedValue)) {
                set(object, replacedValue);
                return true;
            }
        }
        return false;
    }

}
