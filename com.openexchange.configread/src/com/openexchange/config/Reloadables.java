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

package com.openexchange.config;

import java.io.File;
import com.openexchange.java.Strings;

/**
 * {@link Reloadables} - Utility class for {@link Reloadable}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class Reloadables {

    /**
     * Initializes a new {@link Reloadables}.
     */
    private Reloadables() {
        super();
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
