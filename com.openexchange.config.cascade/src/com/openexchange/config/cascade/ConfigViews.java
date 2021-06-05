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

package com.openexchange.config.cascade;

import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link ConfigViews} - A utility class for {@link ConfigView}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class ConfigViews {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigViews.class);
    }

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
        return Strings.isEmpty(str) ? null : str;
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
        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (!property.isDefined()) {
            return def;
        }

        String prop = property.get();
        if (Strings.isNotEmpty(prop)) {
            prop = Strings.asciiLowerCase(prop.trim());
            if ("true".equals(prop)) {
                return true;
            }
            if ("false".equals(prop)) {
                return false;
            }

            LoggerHolder.LOG.trace("Failed to parse value of property {} to a boolean: {}", propertyName, prop);
            return def;
        }
        return def;
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
        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (!property.isDefined()) {
            return def;
        }

        String prop = property.get();
        if (Strings.isNotEmpty(prop)) {
            try {
                return Integer.parseInt(prop.trim());
            } catch (NumberFormatException e) {
                LoggerHolder.LOG.trace("Failed to parse value of property {} to an integer: {}", propertyName, prop, e);
            }
        }
        return def;
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
    public static long getDefinedLongPropertyFrom(String propertyName, long def, ConfigView view) throws OXException {
        ComposedConfigProperty<String> property = view.property(propertyName, String.class);
        if (!property.isDefined()) {
            return def;
        }

        String prop = property.get();
        if (Strings.isNotEmpty(prop)) {
            try {
                return Long.parseLong(prop.trim());
            } catch (NumberFormatException e) {
                LoggerHolder.LOG.trace("Failed to parse value of property {} to a long: {}", propertyName, prop, e);
            }
        }
        return def;
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
