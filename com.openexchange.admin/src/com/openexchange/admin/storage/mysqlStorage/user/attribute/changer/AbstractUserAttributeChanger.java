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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.database.Databases;
import com.openexchange.java.Strings;

/**
 * {@link AbstractUserAttributeChanger}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
abstract class AbstractUserAttributeChanger implements UserAttributeChanger {

    static final String TOKEN = "<COLUMN>";
    static final String SQL_STATEMENT_TEMPLATE = "UPDATE user SET " + TOKEN + " = ? WHERE cid = ? AND id = ?";
    private final Map<Class<?>, Setter> setters;
    private final Map<Class<?>, Unsetter> unsetters;

    /**
     * Initialises a new {@link AbstractUserAttributeChanger}.
     */
    public AbstractUserAttributeChanger() {
        super();

        Map<Class<?>, Setter> s = new HashMap<>();
        s.put(String.class, (stmt, x, parameterIndex) -> stmt.setString(parameterIndex, (String) x));
        s.put(Boolean.class, (stmt, x, parameterIndex) -> stmt.setBoolean(parameterIndex, (Boolean) x));
        s.put(Integer.class, (stmt, x, parameterIndex) -> stmt.setInt(parameterIndex, (Integer) x));
        s.put(Long.class, (stmt, x, parameterIndex) -> stmt.setLong(parameterIndex, (Long) x));

        Map<Class<?>, Unsetter> u = new HashMap<>();
        u.put(String.class, (stmt, parameterIndex) -> stmt.setNull(parameterIndex, Types.VARCHAR));
        u.put(Boolean.class, (stmt, parameterIndex) -> stmt.setNull(parameterIndex, Types.BOOLEAN));
        u.put(Integer.class, (stmt, parameterIndex) -> stmt.setNull(parameterIndex, Types.INTEGER));
        u.put(Long.class, (stmt, parameterIndex) -> stmt.setNull(parameterIndex, Types.BIGINT));

        setters = Collections.unmodifiableMap(s);
        unsetters = Collections.unmodifiableMap(u);
    }

    /**
     * Sets the specified {@link UserAttribute}
     * 
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param userAttribute The {@link UserAttribute} to set
     * @param value The value to set
     * @param connection The {@link Connection}
     * @return <code>true</code> if the attribute was successfully set; <code>false</code> if there is no
     *         associated {@link Setter} for the specified {@link UserAttribute}, or if the value is <code>null</code>
     *         or empty.
     * @throws SQLException if an SQL error is occurred
     */
    boolean setAttribute(int userId, int contextId, UserAttribute userAttribute, Object value, Connection connection) throws SQLException {
        if (value == null || (value instanceof String && Strings.isEmpty((String) value))) {
            return false;
        }

        Setter setter = setters.get(userAttribute.getOriginalType());
        if (setter == null) {
            return false;
        }

        PreparedStatement stmt = prepareStatement(userAttribute, connection);
        int parameterIndex = 1;
        setter.set(stmt, value, parameterIndex++);
        appendUserContext(contextId, userId, stmt, parameterIndex);
        return executeUpdate(stmt);
    }

    /**
     * Un-sets the specified {@link UserAttribute}
     * 
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param userAttribute The {@link UserAttribute} to un-set
     * @param connection The {@link Connection}
     * @return <code>true</code> if the attribute was successfully un-set; <code>false</code> otherwise
     * @throws SQLException if an SQL error is occurred
     */
    boolean unsetAttribute(int userId, int contextId, UserAttribute userAttribute, Connection connection) throws SQLException {
        Unsetter unsetter = unsetters.get(userAttribute.getOriginalType());
        if (unsetter == null) {
            return false;
        }
        PreparedStatement stmt = prepareStatement(userAttribute, connection);

        int parameterIndex = 1;
        unsetter.unset(stmt, parameterIndex++);
        appendUserContext(contextId, userId, stmt, parameterIndex);
        return executeUpdate(stmt);
    }

    ///////////////////////////////////// HELPERS ////////////////////////////////

    /**
     * Executes the update
     * 
     * @param stmt The {@link PreparedStatement}
     * @return <code>true</code> if the update was successful; <code>false</code> otherwise
     * @throws SQLException
     */
    private boolean executeUpdate(PreparedStatement stmt) throws SQLException {
        try {
            return stmt.executeUpdate() == 1; // we expect exactly one change
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Creates a new {@link PreparedStatement} for the specified {@link UserAttribute}
     * 
     * @param userAttribute the {@link UserAttribute}
     * @param connection The {@link Connection}
     * @return The {@link PreparedStatement}
     * @throws SQLException if an SQL error is occurred
     */
    private PreparedStatement prepareStatement(UserAttribute userAttribute, Connection connection) throws SQLException {
        String sqlStatement = SQL_STATEMENT_TEMPLATE.replaceAll(TOKEN, userAttribute.getSQLFieldName());
        return connection.prepareStatement(sqlStatement);
    }

    /**
     * Appends the user and context identifiers to the specified {@link PreparedStatement} in the specified positions
     * 
     * @param contextId the context identifier
     * @param userId The user identifier
     * @param stmt The {@link PreparedStatement}
     * @param parameterIndex The position at which the user and context identifiers will be set
     * 
     * @throws SQLException if an SQL error is occurred
     */
    private void appendUserContext(int contextId, int userId, PreparedStatement stmt, int parameterIndex) throws SQLException {
        stmt.setInt(parameterIndex++, contextId);
        stmt.setInt(parameterIndex++, userId);
    }

    //////////////////////////////// PRIVATE INTERFACES ///////////////////////////////

    /**
     * {@link Setter} - Private functional Setter interface
     */
    private interface Setter {

        /**
         * Sets the specified value to the specified {@link PreparedStatement} at the specified position
         * 
         * @param stmt The {@link PreparedStatement}
         * @param value The value to set
         * @param parameterIndex The position at which the value will be set
         * @throws SQLException if an SQL error is occurred
         */
        void set(PreparedStatement stmt, Object value, int parameterIndex) throws SQLException;
    }

    /**
     * {@link Setter} - Private functional Unsetter interface
     */
    private interface Unsetter {

        /**
         * Un-sets the value from the specified {@link PreparedStatement} and the specified position
         * 
         * @param stmt The {@link PreparedStatement}
         * @param parameterIndex The position at which the value will be un-set
         * @throws SQLException if an SQL error is occurred
         */
        void unset(PreparedStatement stmt, int parameterIndex) throws SQLException;
    }
}
