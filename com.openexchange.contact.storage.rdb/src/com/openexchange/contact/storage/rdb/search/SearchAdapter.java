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

package com.openexchange.contact.storage.rdb.search;

import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * {@link SearchAdapter} - Helps constructing the database statement for a search term.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface SearchAdapter {

	/**
	 * Gets the parameter values that were detected in the search term during
	 * parsing to be included in the database statement in the correct order.
	 *
	 * @return the parameters
	 */
	public Object[] getParameters();

	/**
	 * Gets the constructed <code>WHERE</code>-clause for the search term to be
	 * used in the database statement, without the leading <code>WHERE</code>.
	 *
	 * @return the search clause
	 */
	public StringBuilder getClause();

	/**
	 * Sets the detected database parameters in the supplied prepared statement,
	 * beginning at the specified parameter index.
	 *
	 * @param stmt the statement to set the parameters for
	 * @param parameterIndex the start index to set the parameters
	 * @throws SQLException
	 */
	public void setParameters(PreparedStatement stmt, int parameterIndex) throws SQLException;

}
