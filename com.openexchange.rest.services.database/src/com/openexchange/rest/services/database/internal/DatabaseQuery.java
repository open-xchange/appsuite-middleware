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

package com.openexchange.rest.services.database.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * {@link DatabaseQuery}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.8.0
 */
public class DatabaseQuery {

    private final String query;
    private final List<Object> values;
    private boolean wantsResultSet = false;
    private boolean wantsGeneratedKeys = false;

    /**
     * Initializes a new {@link DatabaseQuery}.
     * 
     * @param query The SQL query
     * @param values The values for the prepared statement
     * @param wantsResultSet True if the query expects a ResultSet; false otherwise
     * @param wantsGeneratedKeys True if the query expects generated keys; false otherwise
     */
    public DatabaseQuery(String query, List<Object> values, boolean wantsResultSet, boolean wantsGeneratedKeys) {
        this.query = query;
        this.values = values;
        this.wantsResultSet = wantsResultSet;
        this.wantsGeneratedKeys = wantsGeneratedKeys;
    }

    /**
     * Create a {@link PreparedStatement} for the specified {@link Connection}
     * 
     * @param con The connection to prepare the statement for
     * @return The PreparedStatemnt
     * @throws SQLException if the preparation of the statement fails
     */
    public PreparedStatement prepareFor(Connection con) throws SQLException {
        PreparedStatement stmt = wantsGeneratedKeys ? con.prepareStatement(getQuery(), Statement.RETURN_GENERATED_KEYS) : con.prepareStatement(getQuery());
        if (values == null) {
            return stmt;
        }
        for (int i = 0, size = values.size(); i < size; i++) {
            stmt.setObject(i + 1, values.get(i));
        }
        return stmt;
    }

    /**
     * Get the wantsGeneratedKeys
     * 
     * @return the wantsGeneratedKeys
     */
    public boolean wantsGeneratedKeys() {
        return wantsGeneratedKeys;
    }

    /**
     * Get the wantsResultsSet
     * 
     * @return the wantsResultSets
     */
    public boolean wantsResultSet() {
        return wantsResultSet;
    }

    /**
     * Gets the query
     *
     * @return The query
     */
    public String getQuery() {
        return query;
    }
}
