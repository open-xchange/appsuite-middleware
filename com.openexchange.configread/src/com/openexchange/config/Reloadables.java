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

package com.openexchange.config;

import java.io.File;
import java.util.Collection;
import org.slf4j.Logger;
import com.openexchange.config.internal.ConfigurationImpl;
import com.openexchange.java.Strings;

/**
 * {@link Reloadables} - Utility class for {@link Reloadable}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class Reloadables {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(Reloadables.class);
    }

    /**
     * Initializes a new {@link Reloadables}.
     */
    private Reloadables() {
        super();
    }

    /**
     * Propagates the change for individual properties
     *
     * @param propertyNames The names of the properties that have been changed
     */
    public static void propagatePropertyChange(Collection<String> propertyNames) {
        if (null == propertyNames || propertyNames.isEmpty()) {
            return;
        }

        ConfigurationImpl configurationImpl = ConfigurationImpl.getConfigReference();
        if (null == configurationImpl) {
            LoggerHolder.LOG.warn("Cannot propagate change for properties since configuration instance has not been initialized");
            return;
        }

        configurationImpl.reloadConfigurationFor(propertyNames.toArray(new String[propertyNames.size()]));
    }

    /**
     * Propagates the change for individual properties
     *
     * @param propertyNames The names of the properties that have been changed
     */
    public static void propagatePropertyChange(String... propertyNames) {
        if (null == propertyNames || propertyNames.length <= 0) {
            return;
        }

        ConfigurationImpl configurationImpl = ConfigurationImpl.getConfigReference();
        if (null == configurationImpl) {
            LoggerHolder.LOG.warn("Cannot propagate change for properties since configuration instance has not been initialized");
            return;
        }

        configurationImpl.reloadConfigurationFor(propertyNames);
    }

    /**
     * Validates specified property name.
     *
     * @param propertyName The property name to validate.
     * @throws IllegalArgumentException If the property name is invalid.
     */
    public static void validatePropertyName(String propertyName) {
        if (null == propertyName) {
            throw new IllegalArgumentException("property name is null");
        }

        if ("*".equals(propertyName)) {
            return;
        }

        if (propertyName.endsWith(".*")) {
            propertyName = propertyName.substring(0, propertyName.length() - 2);
        }

        int length = propertyName.length();
        if (length == 0) {
            throw new IllegalArgumentException("empty property name");
        }

        for (int i = 0; i < length; i++) {
            char ch = propertyName.charAt(i);
            if (ch == '.') {
                // Can't start or end with a '.' but anywhere else is okay
                if (i == 0 || (i == length - 1)) {
                    throw new IllegalArgumentException("invalid property name: " + propertyName);
                }
                // Can't have ".." as that implies empty token
                if (propertyName.charAt(i - 1) == '.') {
                    throw new IllegalArgumentException("invalid property name: " + propertyName);
                }
            } else {
                if (('A' <= ch) && (ch <= 'Z')) {
                    continue;
                }
                if (('a' <= ch) && (ch <= 'z')) {
                    continue;
                }
                if (('0' <= ch) && (ch <= '9')) {
                    continue;
                }
                if ((ch == '_') || (ch == '-')) {
                    continue;
                }
                throw new IllegalArgumentException("invalid property name: " + propertyName);
            }
        }
    }

    /**
     * Validates specified file name.
     *
     * @param fileName The file name to validate.
     * @throws IllegalArgumentException If the file name is invalid.
     */
    public static void validateFileName(String fileName) {
        if (null == fileName) {
            throw new IllegalArgumentException("file name is null");
        }

        if (Strings.isEmpty(fileName)) {
            throw new IllegalArgumentException("empty file name");
        }

        if (fileName.indexOf(File.separatorChar) >= 0) {
            // Seems to be a path...
            throw new IllegalArgumentException("invalid file name: " + fileName);
        }

        int length = fileName.length();
        for (int i = 0; i < length; i++) {
            char ch = fileName.charAt(i);
            if (ch == '.') {
                // Can't start or end with a '.' but anywhere else is okay
                if (i == 0 || (i == length - 1)) {
                    throw new IllegalArgumentException("invalid file name: " + fileName);
                }
                // Can't have ".." as that implies path traversal
                if (fileName.charAt(i - 1) == '.') {
                    throw new IllegalArgumentException("invalid file name: " + fileName);
                }
            } else {
                if (('A' <= ch) && (ch <= 'Z')) {
                    continue;
                }
                if (('a' <= ch) && (ch <= 'z')) {
                    continue;
                }
                if (('0' <= ch) && (ch <= '9')) {
                    continue;
                }
                if ((ch == '_') || (ch == '-')) {
                    continue;
                }
                throw new IllegalArgumentException("invalid file name: " + fileName);
            }
        }
    }

    /**
     * Gets the interests for all
     *
     * @return The interests for all
     */
    public static Interests getInterestsForAll() {
        return DefaultInterests.builder().propertiesOfInterest(getInterestedInAllProperties()).build();
    }

    /**
     * Creates an interest for specified property names.
     *
     * @param propertiesOfInterest The properties of interest
     * @return The interest for specified property names
     */
    public static Interests interestsForProperties(String... propertiesOfInterest) {
        return DefaultInterests.builder().propertiesOfInterest(propertiesOfInterest).build();
    }

    /**
     * Creates an interest for specified file names.
     *
     * @param configFileNames The files of interest
     * @return The interest for specified file names
     */
    public static Interests interestsForFiles(String... configFileNames) {
        return DefaultInterests.builder().configFileNames(configFileNames).build();
    }

    /**
     * Gets the value that signals interest for all properties.
     *
     * @return The value that signals interest for all properties
     */
    public static String[] getInterestedInAllProperties() {
        return new String[] { "*" };
    }

}
