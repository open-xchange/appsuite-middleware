/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
     * <b>Example 1</b>:<br>
     * The fully qualified property name <code>com.openexchange.test.[replaceMe]</code> is returned as
     * <code>com.openexchange.test.success</code> if the map contains a key called <code>replaceMe</code> with the value
     * <code>success</code>. If the map does not contain such a key, <code>com.openexchange.test.[replaceMe]</code> is returned.
     * <p>
     * <b>Example 2</b>:<br>
     * The fully qualified property name <code>com.openexchange.test.[replaceMe].enabled</code> is returned as
     * <code>com.openexchange.test.enabled</code> if the map contains a key called <code>replaceMe</code> with the value
     * <code>""</code> (empty String). If the map does not contain such a key, <code>com.openexchange.test.[replaceMe]</code> is returned.
     *
     * 
     * @param optionals A map containing values for optional parts in the fully qualified name. If the value is <code>""</code> the replacement will be removed, removing following dots, too.
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
                        if ("".equals(optValue) && i < length && fqn.charAt(i + 1) == '.') {
                            // Skip next "." when the replacement is empty
                            i++;
                        }
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
     * @param <T> The class to cast to
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
        if (clazz.isAssignableFrom(defaultValue.getClass())) {
            try {
                return clazz.cast(defaultValue);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("The object '" + defaultValue + "' cannot be converted to the specified type '" + clazz.getCanonicalName() + "'", e);
            }
        }
        throw new IllegalArgumentException("The object '" + defaultValue + "' cannot be converted to the specified type '" + clazz.getCanonicalName() + "'");
    }
}
