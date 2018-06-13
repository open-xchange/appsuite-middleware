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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link ReturnType} enumeration - Defines all data types for the contact attributes
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public enum ReturnType {
    STRING("String"),
    INTEGER("Integer"),
    LONG("Long"),
    BOOLEAN("Boolean"),
    DATE("Date", "java.util.");

    private static final String PREFIX = "java.lang.";
    private final String name;
    private final String prefix;

    private static final Map<String, ReturnType> RETURN_TYPES;
    static {
        Map<String, ReturnType> returnTypes = new HashMap<>(8);
        for (ReturnType type : ReturnType.values()) {
            returnTypes.put(type.getWithPrefix(), type);
        }
        RETURN_TYPES = Collections.unmodifiableMap(returnTypes);
    }

    /**
     * Initialises a new {@link ReturnType}.
     * 
     * @param name The name of the {@link ReturnType}
     */
    private ReturnType(String name) {
        this(name, PREFIX);
    }

    /**
     * Initialises a new {@link ReturnType}.
     * 
     * @param name The name of the {@link ReturnType}
     * @param prefix The prefix
     */
    private ReturnType(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the name with prefix
     *
     * @return The name with prefix
     */
    public String getWithPrefix() {
        return prefix + getName();
    }

    /**
     * Get the specified {@link ReturnType}
     * 
     * @param type The specified return type
     * @return The {@link ReturnType} or <code>null</code> if none exists
     */
    public static ReturnType getReturnType(String type) {
        return RETURN_TYPES.get(type);
    }
}
