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
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.Mapper;

/**
 * {@link DbMapper} - Generic database mapper definition for field-wise
 * operations on objects
 *
 * @param <O> the type of the object
 * @param <E> the enum type for the fields
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface DbMapper<O, E extends Enum<E>> extends Mapper<O, E> {

	@Override
	DbMapping<? extends Object, O> get(E field) throws OXException;

	/**
	 * Gets the field whose mapping denotes the supplied column label.
	 *
	 * @param columnLabel the column label
	 * @return the field, or <code>null</code> if no such field was found
	 */
	E getMappedField(String columnLabel);

    /**
     * Creates a new object and sets all properties of the supplied fields
     * from the result set.
     *
     * @param resultSet the result set to create the object from
     * @param fields the fields present in the result set
     * @return the object
     * @throws OXException
     * @throws SQLException
     */
    O fromResultSet(ResultSet resultSet, E[] fields) throws OXException, SQLException;

    /**
     * Creates a new object and sets all properties of the supplied fields
     * from the result set.
     *
     * @param resultSet the result set to create the object from
     * @param fields the fields present in the result set
     * @param columnLabelPrefix A prefix to use for the column names when getting the results from the columns in the result set,
     *                          typically something like <code>ab.</code> when getting joined results
     * @return the object
     * @throws OXException
     * @throws SQLException
     */
    O fromResultSet(ResultSet resultSet, E[] fields, String columnLabelPrefix) throws OXException, SQLException;

    /**
     * Creates a list of new objects and sets all properties of the supplied
     * fields from the result set.
     *
     * @param resultSet the result set to create the object from
     * @param fields the fields present in the result set
     * @return the object
     * @throws OXException
     * @throws SQLException
     */
    List<O> listFromResultSet(ResultSet resultSet, E[] fields) throws OXException, SQLException;

	/**
	 * Sets all parameters of the supplied fields in the statement to the
	 * values found in the object.
	 *
	 * @param stmt the statement to set the parameters for
	 * @param object the object to read the values from
	 * @param fields the fields to be set
	 * @throws SQLException
	 * @throws OXException
	 */
	void setParameters(PreparedStatement stmt, O object, E[] fields) throws SQLException, OXException;

    /**
     * Sets all parameters of the supplied fields in the statement to the
     * values found in the object, beginning with a specific parameter index.
     *
     * @param stmt The statement to set the parameters for
     * @param parameterIndex The parameter index to start with
     * @param object The object to read the values from
     * @param fields The fields to be set
     * @return The parameter index, incremented by the number of set parameters
     */
    int setParameters(PreparedStatement stmt, int parameterIndex, O object, E[] fields) throws SQLException, OXException;

	/**
	 * Constructs a string containing parameterized assignments of the supplied
	 * fields for database statements using the mapped column names of the
	 * fields, separated by <code>,</code>-chars.
	 *
	 * @param fields the fields to get the assignments for
	 * @return the assignments string
	 * @throws OXException
	 */
	String getAssignments(E[] fields) throws OXException;

    /**
     * Gets a comma-separated string of the mapped column names for the
     * supplied fields.
     *
     * @param fields the fields
     * @return the columns string
     * @throws OXException
     */
    String getColumns(E[] fields) throws OXException;

    /**
     * Gets a comma-separated string of the mapped column names for the
     * supplied fields.
     *
     * @param fields the fields
     * @param columnLabelPrefix A prefix to use for the column names when getting the results from the columns in the result set,
     *                          typically something like <code>ab.</code> when getting joined results
     * @return the columns string
     * @throws OXException
     */
    String getColumns(E[] fields, String columnLabelPrefix) throws OXException;

}
