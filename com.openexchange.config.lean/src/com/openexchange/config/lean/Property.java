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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.config.lean;

import java.util.Map;

/**
 * {@link Property} - Describes a lean property with a fully qualified name and a default value.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 */
public interface Property {

    /**
     * Gets the fully qualified property name as-is.
     *
     * @return The fully qualified property name
     */
    String getFQPropertyName();

    /**
     * Gets the fully qualified property name optionally replacing place-holders in brackets with values from given map (if any given).
     * <p>
     * <b>Example</b>:<br>
     * The fully qualified property name <code>"com.openexchange.test.[replaceMe]"</code> is returned as
     * <code>"com.openexchange.test.success"</code> if the map contains a key called <code>"replaceMe"</code> with the value
     * <code>"success"</code>. If the map does not contain such a key, <code>"com.openexchange.test.[replaceMe]"</code> is returned.
     *
     * @param optionals A map containing values for optional parts in the fully qualified name
     * @return The fully qualified property name with place-holders replaced
     */
    default String getFQPropertyName(Map<String, String> optionals) {
        if (null == optionals || optionals.isEmpty()) {
            // No need to change anything
            return getFQPropertyName();
        }

        // Check for presence of opening bracket '['
        String fqn = getFQPropertyName();
        int bracketStart = fqn.indexOf('[');
        if (bracketStart < 0) {
            // Does not contain an opening bracket '['
            return fqn;
        }

        // Contains optional parameters
        int length = fqn.length();
        StringBuilder builder = null;
        for (int i = bracketStart; i < length; i++) {
            char c = fqn.charAt(i);
            if (c == '[') {
                // Find associated closing bracket
                int pos = fqn.indexOf(']', i);
                if (pos > 0) {
                    String toReplace = fqn.substring(i + 1, pos);
                    String optValue = optionals.get(toReplace);
                    if (null != optValue) {
                        // Replacement available
                        if (null == builder) {
                            builder = new StringBuilder(length);
                            if (i > 0) {
                                builder.append(fqn, 0, i);
                            }
                        }
                        builder.append(optValue);
                        i += toReplace.length() + 1;
                    } else {
                        // Add '[' character as no replacement available in map
                        if (null != builder) {
                            builder.append(c);
                        }
                    }
                } else {
                    // Add '[' character as no closing ']' present
                    if (null != builder) {
                        builder.append(c);
                    }
                }
            } else {
                // Not a '[' character
                if (null != builder) {
                    builder.append(c);
                }
            }
        }
        return null == builder ? fqn : builder.toString();
    }

    /**
     * Gets the default value.
     *
     * @return The default value
     */
    Object getDefaultValue();

    /**
     * Gets the default value.
     *
     * @param clazz The type of the property's value to which it will be casted
     * @return The default value
     * @throws IllegalArgumentException If specified type does not match the one of the default value
     */
    default <T extends Object> T getDefaultValue(Class<T> clazz) throws IllegalArgumentException {
        // Check type
        if (null == clazz) {
            throw new IllegalArgumentException("Type must not be null");
        }

        // Grab default value
        Object defaultValue = getDefaultValue();
        if (null == defaultValue) {
            return null;
        }

        // Check this order to be able to cast to sub-classes, too
        try {
            return clazz.cast(defaultValue);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("The object cannot be converted to the specified type: " + clazz.getCanonicalName(), e);
        }
    }
}
