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
import com.openexchange.exception.OXException;


/**
 * {@link DbMultiMapping} - Extends the generic mapping by database specific
 * operations.
 *
 * @param <T> the type of the property
 * @param <O> the type of the object
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface DbMultiMapping<T, O> extends DbMapping<T, O> {

	/**
	 * Gets the value of the mapped property from a result set.
	 *
	 * @param resultSet the result set to get the property from
	 * @return the value
	 * @throws SQLException
	 */
    T get(ResultSet resultSet, String[] columnLabels) throws SQLException;

    /**
     * Gets the column label of the mapped property.
     *
     * @return the column label
     */
    String[] getColumnLabels();

    /**
     * Gets the column label of the mapped property, prefixed with the supplied value.
     *
     * @return the prefixed column label
     */
    String[] getColumnLabels(String prefix);

    /**
     * Sets the value of the mapped property in an object.
     *
     * @param resultSet the result set to read out the value from
     * @param object the object to set the value
     * @param columnLabel the label for the column specified with the SQL AS clause, or, if the SQL AS clause was not specified, the
     *                    label of the column name
     * @throws SQLException
     * @throws OXException
     */
    void set(ResultSet resultSet, O object, String[] columnLabels) throws SQLException, OXException;

}
