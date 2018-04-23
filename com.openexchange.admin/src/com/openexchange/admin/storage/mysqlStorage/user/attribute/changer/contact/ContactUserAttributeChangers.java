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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.MethodMetadata;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.ReturnType;
import com.openexchange.admin.storage.sqlStorage.OXUserSQLStorage.Mapper;

/**
 * {@link ContactUserAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ContactUserAttributeChangers implements AttributeChangers {

    private static final Logger LOG = LoggerFactory.getLogger(ContactUserAttributeChangers.class);

    private static final Set<String> RETURN_TYPES;
    static {
        Set<String> returnTypes = new HashSet<>(8);
        for (ReturnType type : ReturnType.values()) {
            returnTypes.add(type.getWithPrefix());
        }
        RETURN_TYPES = Collections.unmodifiableSet(returnTypes);
    }

    private enum MethodPrefix {
        get, is;
    }

    private final Map<ReturnType, Appender> appenders;

    /**
     * Initialises a new {@link ContactUserAttributeChangers}.
     */
    public ContactUserAttributeChangers() {
        super();
        appenders = new HashMap<>();
        appenders.put(ReturnType.STRING, (userData, methodMetadata, query, setMethods) -> {
            String result = (String) methodMetadata.getMethod().invoke(userData, (Object[]) null);
            if (result != null || isAttributeSet(methodMetadata.getMethod(), userData)) {
                appendToQuery(methodMetadata, query, setMethods);
                if ("field01".equals(Mapper.method2field.get(methodMetadata.getName()))) {
                    //TODO: hint for the cache when updating display name?
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
        appenders.put(ReturnType.INTEGER, (userData, methodMetadata, query, setMethods) -> {
            Date result = (Date) methodMetadata.getMethod().invoke(userData, (Object[]) null);
            if (result != null || isAttributeSet(methodMetadata.getMethod(), userData)) {
                appendToQuery(methodMetadata, query, setMethods);
            }
        });
        appenders.put(ReturnType.INTEGER, (userData, methodMetadata, query, setMethods) -> {
            long result = ((Long) methodMetadata.getMethod().invoke(userData, (Object[]) null)).longValue();
            if (result != -1 || isAttributeSet(methodMetadata.getMethod(), userData)) {
                appendToQuery(methodMetadata, query, setMethods);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AttributeChangers#change(com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute, com.openexchange.admin.rmi.dataobjects.User, int, int,
     * java.sql.Connection)
     */
    @Override
    public boolean change(Attribute attribute, User userData, int userId, int contextId, Connection connection) throws SQLException {
        return !change(Collections.singleton(attribute), userData, userId, contextId, connection).isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AttributeChangers#change(java.util.Set, com.openexchange.admin.rmi.dataobjects.User, int, int, java.sql.Connection)
     */
    @Override
    public Set<String> change(Set<Attribute> attributes, User userData, int userId, int contextId, Connection connection) throws SQLException {
        StringBuilder query = new StringBuilder("UPDATE prg_contacts SET ");
        List<MethodMetadata> setMethods = new ArrayList<>();
        // First we have to check which return value we have. We have to distinguish the return types
        for (MethodMetadata methodMetadata : getGetters(userData.getClass().getMethods())) {
            ReturnType returnType = methodMetadata.getReturnType();
            if (returnType == null) {
                continue;
            }
            // TODO: construct query
            Appender appender = appenders.get(returnType);
            if (appender == null) {
                continue;
            }
            try {
                appender.append(userData, methodMetadata, query, setMethods);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // TODO: maybe throw StorageException instead?
                throw new SQLException(e);
            }
        }
        if (!setMethods.isEmpty()) {
            // TODO: prepare statements and execute
        }
        Set<String> changedAttributes = new HashSet<>();
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
                if (!methodName.contains(methodPrefix.name())) {
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
                if (RETURN_TYPES.contains(returnType)) {
                    ReturnType rt = getReturnType(returnType);
                    if (rt == null) {
                        LOG.debug("Unknown return type '{}'. Method '{}' will be ignored", returnType, methodName);
                        continue;
                    }
                    methodMetadata.add(new MethodMetadata(method, methodNameWithoutPrefix, rt));
                }
            }
        }
        return methodMetadata;
    }

    /**
     * Gets the return type of the specified method
     * 
     * @param returnTypeValue The return type as string
     * @return The {@link ReturnType} or <code>null</code> if no such {@link ReturnType} exists
     */
    private ReturnType getReturnType(String returnTypeValue) {
        try {
            return ReturnType.valueOf(returnTypeValue);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 
     * @param method
     * @param userData
     * @return
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

    private void appendToQuery(MethodMetadata methodMetadata, StringBuilder query, List<MethodMetadata> setMethods) {
        query.append(Mapper.method2field.get(methodMetadata.getName()));
        query.append(" = ?, ");
        setMethods.add(methodMetadata);
    }

    private interface Appender {

        void append(User userData, MethodMetadata method, StringBuilder query, List<MethodMetadata> setMethods) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException;
    }
}
