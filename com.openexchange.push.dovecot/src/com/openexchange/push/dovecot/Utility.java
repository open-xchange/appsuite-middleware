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

package com.openexchange.push.dovecot;

import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link Utility} - Utility class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class Utility {

    /**
     * Initializes a new {@link Utility}.
     */
    private Utility() {
        super();
    }

    /**
     * Gets the value for specified property; returns default value if such a property does not exist.
     *
     * @param propertyName The property name
     * @param defaultValue The default value to return
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param services The service look-up to obtain required services from
     * @return The value or <code>defaultValue</code>
     * @throws OXException If value cannot be returned
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static String getValueFromProperty(String propertyName, String defaultValue, int userId, int contextId, ServiceLookup services) throws OXException {
        ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
        if (null == viewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = viewFactory.getView(userId, contextId);
        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        String value = property.get();
        return Strings.isEmpty(value) ? defaultValue : value;
    }

    /**
     * Gets the integer value for specified property; returns default value if such a property does not exist.
     *
     * @param propertyName The property name
     * @param defaultValue The default value to return
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param services The service look-up to obtain required services from
     * @return The integer value or <code>defaultValue</code>
     * @throws OXException If value cannot be returned
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static Integer getIntFromProperty(String propertyName, Integer defaultValue, int userId, int contextId, ServiceLookup services) throws OXException {
        ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
        if (null == viewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = viewFactory.getView(userId, contextId);
        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        String value = property.get();
        if (Strings.isEmpty(value)) {
            return defaultValue;
        }

        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets the boolean value for specified property; returns default value if such a property does not exist.
     *
     * @param propertyName The property name
     * @param defaultValue The default value to return
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param services The service look-up to obtain required services from
     * @return The boolean value or <code>defaultValue</code>
     * @throws OXException If value cannot be returned
     * @throws IllegalArgumentException If session is <code>null</code>
     */
    public static Boolean getBoolFromProperty(String propertyName, Boolean defaultValue, int userId, int contextId, ServiceLookup services) throws OXException {
        ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
        if (null == viewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = viewFactory.getView(userId, contextId);
        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (null == property || !property.isDefined()) {
            return defaultValue;
        }

        String value = property.get();
        return Strings.isEmpty(value) ? defaultValue : ("true".equalsIgnoreCase(value.trim()) ? Boolean.TRUE : ("false".equalsIgnoreCase(value.trim()) ? Boolean.FALSE : defaultValue));
    }

}
