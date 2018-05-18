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

package com.openexchange.filestore.utils;

import javax.annotation.concurrent.NotThreadSafe;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Tools;

/**
 * {@link PropertyNameBuilder} - A utility class to generate property names having the same prefix.
 * <p>
 * This class is not thread-safe.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
@NotThreadSafe
public class PropertyNameBuilder {

    private final StringBuilder sb;
    private final int reslen;

    /**
     * Initializes a new {@link PropertyNameBuilder}.
     *
     * @param The common prefix for the property names that should be generated; e.g. <code>"com.openexchange.component."</code>
     */
    public PropertyNameBuilder(String prefix) {
        super();
        if (null == prefix) {
            throw new IllegalArgumentException("prefix must not be null");
        }
        if (prefix.length() > 0 && false == prefix.endsWith(".")) {
            throw new IllegalArgumentException("prefix must end with a dot ('.')");
        }
        sb = new StringBuilder(prefix);
        reslen = prefix.length();
    }

    /**
     * Gets the property name for specified suffix.
     *
     * @param suffix The suffix to use to generate the property name
     * @return The property name
     * @throws IllegalArgumentException If given suffix is <code>null</code>
     */
    public String getPropertyNameWith(String suffix) {
        return getPropertyNameWith(null, suffix);
    }

    /**
     * Gets the property name for specified optional infix and suffix.
     *
     * @param suffix The suffix to use to generate the property name
     * @param optInfix The optional infix to use to generate the property name
     * @return The property name
     * @throws IllegalArgumentException If given suffix is <code>null</code>
     */
    public String getPropertyNameWith(String optInfix, String suffix) {
        if (null == suffix) {
            throw new IllegalArgumentException("suffix must not be null");
        }
        if (null != optInfix) {
            sb.append(optInfix).append('.');
        }
        String propName = sb.append(suffix).toString();
        sb.setLength(reslen);
        return propName;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    // ------------------------------------------------------------------------------------------------------------

    /**
     * Requires the specified file storage property.
     *
     * @param filestoreID The file storage identifier
     * @param property The suffix/name of the property
     * @param nameBuilder The name builder to use
     * @param config The configuration service to retrieve the value from
     * @return The property value
     * @throws OXException If no such property exists
     */
    public static String requireProperty(String filestoreID, String property, PropertyNameBuilder nameBuilder, ConfigurationService config) throws OXException {
        String propName = nameBuilder.getPropertyNameWith(filestoreID, property);
        String value = config.getProperty(propName);
        if (Strings.isNotEmpty(value)) {
            return value;
        }

        // Re-check with "default" infix
        String defaultPropName = nameBuilder.getPropertyNameWith("default", property);
        value = config.getProperty(defaultPropName);
        if (Strings.isEmpty(value)) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create("Neither \"" + propName + "\" nor \"" + defaultPropName + "\" is specified");
        }
        return value;
    }

    /**
     * Gets the specified file storage property.
     *
     * @param filestoreID The file storage identifier
     * @param property The suffix/name of the property
     * @param def The default value to return if no such property exists
     * @param nameBuilder The name builder to use
     * @param config The configuration service to retrieve the value from
     * @return The property value or <code>def</code>
     */
    public static String optProperty(String filestoreID, String property, String def, PropertyNameBuilder nameBuilder, ConfigurationService config) {
        String propName = nameBuilder.getPropertyNameWith(filestoreID, property);
        String value = config.getProperty(propName);
        if (Strings.isNotEmpty(value)) {
            return value;
        }

        // Re-check with "default" infix
        propName = nameBuilder.getPropertyNameWith("default", property);
        value = config.getProperty(propName);
        return Strings.isEmpty(value) ? def : value;
    }

    /**
     * Gets the specified file storage integer property.
     *
     * @param filestoreID The file storage identifier
     * @param property The suffix/name of the property
     * @param def The default integer value to return if no such property exists
     * @param nameBuilder The name builder to use
     * @param config The configuration service to retrieve the value from
     * @return The integer property value or <code>def</code>
     */
    public static int optIntProperty(String filestoreID, String property, int def, PropertyNameBuilder nameBuilder, ConfigurationService config) {
        String propName = nameBuilder.getPropertyNameWith(filestoreID, property);
        String value = config.getProperty(propName);
        if (Strings.isNotEmpty(value)) {
            int intVal = Tools.getUnsignedInteger(value.trim());
            if (intVal >= 0) {
                return intVal;
            }
        }

        // Re-check with "default" infix
        propName = nameBuilder.getPropertyNameWith("default", property);
        value = config.getProperty(propName);
        if (Strings.isNotEmpty(value)) {
            int intVal = Tools.getUnsignedInteger(value.trim());
            if (intVal >= 0) {
                return intVal;
            }
        }
        return def;
    }

    /**
     * Gets the specified file storage boolean property.
     *
     * @param filestoreID The file storage identifier
     * @param property The suffix/name of the property
     * @param def The default boolean value to return if no such property exists
     * @param nameBuilder The name builder to use
     * @param config The configuration service to retrieve the value from
     * @return The boolean property value or <code>def</code>
     */
    public static boolean optBoolProperty(String filestoreID, String property, boolean def, PropertyNameBuilder nameBuilder, ConfigurationService config) {
        String propName = nameBuilder.getPropertyNameWith(filestoreID, property);
        String value = config.getProperty(propName);
        if (Strings.isNotEmpty(value)) {
            return Boolean.parseBoolean(value.trim());
        }

        // Re-check with "default" infix
        propName = nameBuilder.getPropertyNameWith("default", property);
        value = config.getProperty(propName);
        if (Strings.isNotEmpty(value)) {
            return Boolean.parseBoolean(value.trim());
        }
        return def;
    }

}
