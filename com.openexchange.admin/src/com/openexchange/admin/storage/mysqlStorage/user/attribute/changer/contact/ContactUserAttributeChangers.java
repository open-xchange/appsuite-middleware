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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.contact;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractAttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.MethodMetadata;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.ReturnType;
import com.openexchange.admin.storage.sqlStorage.OXUserSQLStorage.Mapper;

/**
 * {@link ContactUserAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ContactUserAttributeChangers extends AbstractAttributeChangers {

    private static final Logger LOG = LoggerFactory.getLogger(ContactUserAttributeChangers.class);

    private enum MethodPrefix {
        get, is;
    }

    private final Map<ReturnType, Appender> appenders;
    private final Map<ReturnType, ValueSetter> valueSetters;

    /**
     * Initialises a new {@link ContactUserAttributeChangers}.
     */
    public ContactUserAttributeChangers() {
        super();
        valueSetters = new HashMap<>();
        valueSetters.put(ReturnType.STRING, (userData, method, preparedStatement, parameterIndex) -> {
            String result = (java.lang.String) method.invoke(userData, (Object[]) null);
            if (null != result) {
                preparedStatement.setString(parameterIndex, result);
            } else if (isAttributeSet(method, userData)) {
                preparedStatement.setNull(parameterIndex, java.sql.Types.VARCHAR);
            }
        });
        valueSetters.put(ReturnType.INTEGER, (userData, method, preparedStatement, parameterIndex) -> {
            int result = ((Integer) method.invoke(userData, (Object[]) null)).intValue();
            if (-1 != result) {
                preparedStatement.setInt(parameterIndex, result);
            } else if (isAttributeSet(method, userData)) {
                preparedStatement.setNull(parameterIndex, java.sql.Types.INTEGER);
            }
        });
        valueSetters.put(ReturnType.BOOLEAN, (userData, method, preparedStatement, parameterIndex) -> {
            boolean result = (Boolean) method.invoke(userData, (Object[]) null);
            preparedStatement.setBoolean(parameterIndex, result);
        });
        valueSetters.put(ReturnType.DATE, (userData, method, preparedStatement, parameterIndex) -> {
            Date result = (Date) method.invoke(userData, (Object[]) null);
            if (null != result) {
                preparedStatement.setTimestamp(parameterIndex, new Timestamp(result.getTime()));
            } else if (isAttributeSet(method, userData)) {
                preparedStatement.setNull(parameterIndex, java.sql.Types.DATE);
            }
        });
        valueSetters.put(ReturnType.LONG, (userData, method, preparedStatement, parameterIndex) -> {
            long result = ((Long) method.invoke(userData, (Object[]) null)).longValue();
            if (-1 != result) {
                preparedStatement.setLong(parameterIndex, result);
            } else if (isAttributeSet(method, userData)) {
                preparedStatement.setNull(parameterIndex, java.sql.Types.BIGINT);
            }
        });

        appenders = new HashMap<>();
        appenders.put(ReturnType.STRING, (userData, methodMetadata, query, setMethods) -> {
            String result = (String) methodMetadata.getMethod().invoke(userData, (Object[]) null);
            if (result != null || isAttributeSet(methodMetadata.getMethod(), userData)) {
                appendToQuery(methodMetadata, query, setMethods);
                if ("field01".equals(Mapper.method2field.get(methodMetadata.getName()))) {
                    query.append("field90");
                    query.append("=?, ");
                    setMethods.add(methodMetadata);
                }
            }
        });
        appenders.put(ReturnType.INTEGER, (userData, methodMetadata, query, setMethods) -> {
            int result = ((Integer) methodMetadata.getMethod().invoke(userData, (Object[]) null)).intValue();
            if (result != -1 || isAttributeSet(methodMetadata.getMethod(), userData)) {
                appendToQuery(methodMetadata, query, setMethods);
            }
        });
        appenders.put(ReturnType.BOOLEAN, (userData, methodMetadata, query, setMethods) -> {
            Boolean result = (Boolean) methodMetadata.getMethod().invoke(userData, (Object[]) null);
            if (result != null || isAttributeSet(methodMetadata.getMethod(), userData)) {
                appendToQuery(methodMetadata, query, setMethods);
            }
        });
        appenders.put(ReturnType.DATE, (userData, methodMetadata, query, setMethods) -> {
            Date result = (Date) methodMetadata.getMethod().invoke(userData, (Object[]) null);
            if (result != null || isAttributeSet(methodMetadata.getMethod(), userData)) {
                appendToQuery(methodMetadata, query, setMethods);
            }
        });
        appenders.put(ReturnType.LONG, (userData, methodMetadata, query, setMethods) -> {
            long result = ((Long) methodMetadata.getMethod().invoke(userData, (Object[]) null)).longValue();
            if (result != -1 || isAttributeSet(methodMetadata.getMethod(), userData)) {
                appendToQuery(methodMetadata, query, setMethods);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AttributeChangers#change(java.util.Set, com.openexchange.admin.rmi.dataobjects.User, int, int, java.sql.Connection)
     */
    @Override
    public Set<String> change(User userData, int userId, int contextId, Connection connection) throws StorageException {
        StringBuilder query = new StringBuilder("UPDATE prg_contacts SET ");

        // First we have to check which return value we have. We have to distinguish the return types
        List<MethodMetadata> methods = collectMethods(userData, query);

        // Prepare statement and execute
        return changeAttributes(userData, userId, contextId, connection, query, methods);
    }

    ///////////////////////////////////// HELPERS /////////////////////////////////

    /**
     * Collect all methods that have set values
     * 
     * @param userData The {@link User} data
     * @param query The SQL query builder
     * @return A {@link List} with all methods that have set values
     * @throws StorageException If an SQL error is occurred
     */
    private List<MethodMetadata> collectMethods(User userData, StringBuilder query) throws StorageException {
        List<MethodMetadata> setMethods = new ArrayList<>();
        for (MethodMetadata methodMetadata : getGetters(userData.getClass().getMethods())) {
            ReturnType returnType = methodMetadata.getReturnType();
            if (returnType == null) {
                continue;
            }
            Appender appender = appenders.get(returnType);
            if (appender == null) {
                continue;
            }
            try {
                appender.append(userData, methodMetadata, query, setMethods);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new StorageException(e);
            }
        }

        return setMethods;
    }

    /**
     * Prepares the statement and executes the update
     * 
     * @param userData The {@link User} data
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param connection The {@link Connection}
     * @param query The SQL query builder
     * @param collectedMethods The collected methods
     * @return A {@link Set} with all changed attributes
     * @throws StorageException If an SQL error is occurred
     */
    private Set<String> changeAttributes(User userData, int userId, int contextId, Connection connection, StringBuilder query, List<MethodMetadata> collectedMethods) throws StorageException {
        if (collectedMethods.isEmpty()) {
            return EMPTY_SET;
        }

        Set<String> changedAttributes = new HashSet<>();
        query.delete(query.length() - 2, query.length() - 1);
        query.append(" WHERE cid = ? AND userid = ?");

        try (PreparedStatement statement = connection.prepareStatement(query.toString())) {

            int parameterIndex = 1;
            for (MethodMetadata methodMetadata : collectedMethods) {
                ValueSetter valueSetter = valueSetters.get(methodMetadata.getReturnType());
                if (valueSetter == null) {
                    continue;
                }
                valueSetter.set(userData, methodMetadata.getMethod(), statement, parameterIndex++);
                changedAttributes.add(methodMetadata.getName());
            }
            statement.setInt(collectedMethods.size() + 1, contextId);
            statement.setInt(collectedMethods.size() + 2, userId);
            statement.executeUpdate();
        } catch (SQLException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new StorageException(e);
        }

        return changedAttributes;
    }

    /**
     * Gets the getters from the specified methods
     * 
     * @param methods The methods from which to retrieve the getters
     * @return A {@link List} with all the getter methods
     */
    private List<MethodMetadata> getGetters(Method[] methods) {
        List<MethodMetadata> methodMetadata = new ArrayList<>();
        for (final Method method : methods) {
            final String methodName = method.getName();
            for (MethodPrefix methodPrefix : MethodPrefix.values()) {
                if (!methodName.startsWith(methodPrefix.name())) {
                    continue;
                }
                String methodNameWithoutPrefix = methodName.substring(methodPrefix.name().length());
                if (Mapper.notallowed.contains(methodNameWithoutPrefix)) {
                    LOG.debug("Method '{}' is not allowed for mapping", methodName); //TODO: too verbose?
                    continue;
                }
                if (null == Mapper.method2field.get(methodNameWithoutPrefix)) {
                    LOG.debug("No mapping found for method '{}'", methodName); //TODO: too verbose?
                    continue;
                }
                final String returnType = method.getReturnType().getName();
                ReturnType rt = ReturnType.getReturnType(returnType);
                if (rt == null) {
                    LOG.debug("Unknown return type '{}'. Method '{}' will be ignored", rt, methodName);
                    continue;
                }
                methodMetadata.add(new MethodMetadata(method, methodNameWithoutPrefix, rt));
            }
        }
        return methodMetadata;
    }

    /**
     * Checks whether an attribute is set by invoking it's respective {@link Method}
     * 
     * @param method The {@link Method} to invoke
     * @param userData The {@link User} data object which contains the {@link Method}
     * @return <code>true</code> if the attribute by the denoted {@link Method} is set; <code>false</code> otherwise
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    private boolean isAttributeSet(Method method, User userData) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String methodName = "is" + method.getName().substring(3) + "set";
        Method retVal = User.class.getMethod(methodName);
        return ((Boolean) retVal.invoke(userData, (Object[]) null)).booleanValue();
    }

    /**
     * Appends to the query and to collected methods the {@link Method} from the specified {@link MethodMetadata}
     * 
     * @param methodMetadata The {@link MethodMetadata}
     * @param query The SQL query builder
     * @param collectedMethods The collected {@link Method}s so far
     */
    private void appendToQuery(MethodMetadata methodMetadata, StringBuilder query, List<MethodMetadata> collectedMethods) {
        query.append(Mapper.method2field.get(methodMetadata.getName()));
        query.append(" = ?, ");
        collectedMethods.add(methodMetadata);
    }

    //////////////////////////////// PRIVATE INTERFACES ///////////////////////////////

    /**
     * {@link Appender}
     */
    private interface Appender {

        /**
         * Appends the specified {@link Method} to the query if it's invocation yields any data
         * 
         * @param userData The {@link User} data
         * @param method The {@link MethodMetadata} containing the {@link Method} to invoke
         * @param query The SQL query builder
         * @param collectedMethods The connected methods so far
         * @throws NoSuchMethodException
         * @throws SecurityException
         * @throws IllegalAccessException
         * @throws IllegalArgumentException
         * @throws InvocationTargetException
         */
        void append(User userData, MethodMetadata method, StringBuilder query, List<MethodMetadata> collectedMethods) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;
    }

    /**
     * {@link ValueSetter}
     */
    private interface ValueSetter {

        /**
         * Sets the value of the attribute denoted by the {@link Method} to the specified {@link PreparedStatement}.
         * The parameter index is NOT increased.
         * 
         * @param userData The {@link User} data
         * @param method The {@link Method} to invoke for fetching the value
         * @param preparedStatement The {@link PreparedStatement}
         * @param parameterIndex The current parameter index
         * @throws IllegalAccessException
         * @throws IllegalArgumentException
         * @throws InvocationTargetException
         * @throws SQLException
         * @throws NoSuchMethodException
         * @throws SecurityException
         */
        void set(User userData, Method method, PreparedStatement preparedStatement, int parameterIndex) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException, NoSuchMethodException, SecurityException;
    }
}
