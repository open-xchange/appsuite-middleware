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

package com.openexchange.config.cascade;

import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link ConfigViews} - A utility class for {@link ConfigView}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class ConfigViews {

    /**
     * Initializes a new {@link ConfigViews}.
     */
    private ConfigViews() {
        super();
    }

    /**
     * Gets the non-empty property from given view associated with specified name.
     *
     * @param propertyName The property name
     * @param view The config view to grab from
     * @return The non-empty property value or <code>null</code> (if not defined or empty)
     * @throws OXException If non-empty property cannot be returned
     */
    public static String getNonEmptyPropertyFrom(String propertyName, ConfigView view) throws OXException {
        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (false == property.isDefined()) {
            return null;
        }

        String str = property.get();
        if (Strings.isEmpty(str)) {
            return null;
        }

        return str;
    }

    /**
     * Gets the defined property from given view associated with specified name.
     *
     * @param propertyName The property name
     * @param view The config view to grab from
     * @param clazz The expected type of the property value
     * @return The defined property value or <code>null</code> (if not defined)
     * @throws OXException If defined property cannot be returned
     */
    public static <V> V getDefinedPropertyFrom(String propertyName, ConfigView view, Class<V> clazz) throws OXException {
        ComposedConfigProperty<V> property = view.property(propertyName, clazz);
        return property.isDefined() ? property.get() : null;
    }

    /**
     * Gets the defined property from given view associated with specified name.
     *
     * @param propertyName The property name
     * @param def The default value to return
     * @param view The config view to grab from
     * @return The defined property value or <code>def</code> (if not defined)
     * @throws OXException If defined property cannot be returned
     */
    public static boolean getDefinedBoolPropertyFrom(String propertyName, boolean def, ConfigView view) throws OXException {
        ComposedConfigProperty<Boolean> property = view.property(propertyName, boolean.class);
        return property.isDefined() ? property.get().booleanValue() : def;
    }

    /**
     * Gets the defined property from given view associated with specified name.
     *
     * @param propertyName The property name
     * @param def The default value to return
     * @param view The config view to grab from
     * @return The defined property value or <code>def</code> (if not defined)
     * @throws OXException If defined property cannot be returned
     */
    public static int getDefinedIntPropertyFrom(String propertyName, int def, ConfigView view) throws OXException {
        ComposedConfigProperty<Integer> property = view.property(propertyName, int.class);
        return property.isDefined() ? property.get().intValue() : def;
    }

    /**
     * Gets the defined property from given view associated with specified name.
     *
     * @param propertyName The property name
     * @param def The default value to return
     * @param view The config view to grab from
     * @return The defined property value or <code>def</code> (if not defined)
     * @throws OXException If defined property cannot be returned
     */
    public static String getDefinedStringPropertyFrom(String propertyName, String def, ConfigView view) throws OXException {
        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        return property.isDefined() ? property.get() : def;
    }

}
