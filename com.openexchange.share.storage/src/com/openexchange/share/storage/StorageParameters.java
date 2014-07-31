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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link StorageParameters}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class StorageParameters {

    /**
     * The constant indicating no special storage parameters.
     */
    public static final StorageParameters NO_PARAMETERS = new StorageParameters(Collections.<String, Object>emptyMap());

    private final Map<String, Object> parameters;

    /**
     * Initializes a new {@link StorageParameters}.
     */
    public StorageParameters() {
        this(new HashMap<String, Object>());
    }

    /**
     * Initializes a new {@link StorageParameters} using the supplied parameters map.
     *
     * @param parameters The parameters to use
     */
    private StorageParameters(Map<String, Object> parameters) {
        super();
        this.parameters = parameters;
    }

    /**
     * Adds a parameter with the supplied key.
     *
     * @param key The key
     * @param value The value
     * @return
     */
    public StorageParameters put(String key, Object value) {
        parameters.put(key, value);
        return this;
    }

//    public Object get(String key) {
//        return parameters.get(key);
//    }
//
//    public <T> T get(String key, Class<T> type) {
//        Object object = parameters.get(key);
//        if (object == null) {
//            return null;
//        }
//
//        if (type.isAssignableFrom(object.getClass())) {
//            return type.cast(object);
//        }
//
//        return null;
//    }

    /**
     * Gets a parameter identified by its key.
     *
     * @param key The key
     * @return The parameter, or <code>null</code> if not defined
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Object object = parameters.get(key);
        if (object == null) {
            return null;
        }

        try {
            return (T) object;
        } catch (ClassCastException e) {
            org.slf4j.LoggerFactory.getLogger(StorageParameters.class).warn(
                "Error casting parameter value for '{}': {}", key, e.getMessage(), e);
            return null;
        }
    }

}
