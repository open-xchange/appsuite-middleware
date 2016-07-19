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
 *    trademarks of the OX Software GmbH. group of companies.
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
