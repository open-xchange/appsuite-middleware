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

package com.openexchange.log.audit;

import java.util.Date;
import java.util.TimeZone;
import com.openexchange.java.Strings;

/**
 * {@link DefaultAttribute} - A default attribute yielded from a possibly predefined name.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class DefaultAttribute<V> implements Attribute<V> {

    /**
     * An enumeration of default names.
     */
    public static enum Name {
        /**
         * The user name and login respectively.
         */
        LOGIN("login"),
        /**
         * The remote IP address of the connected client.
         */
        IP_ADDRESS("ip"),
        /**
         * The time stamp of the event.
         */
        TIMESTAMP("timestamp"),
        /**
         * The client identifier.
         */
        CLIENT("client"),
        ;

        private final String id;

        private Name(String id) {
            this.id = id;
        }

        /**
         * Gets the identifier of this name.
         *
         * @return The identifier
         */
        public String getId() {
            return id;
        }
    }

    /**
     * Gets the time stamp attribute for specified date formatted according to configuration.
     *
     * @param time The time stamp's time
     * @return The attribute
     */
    public static DefaultAttribute<Date> timestampFor(Date time) {
        if (null == time) {
            return null;
        }
        return new DefaultAttribute<Date>(Name.TIMESTAMP.getId(), time, true);
    }

    /**
     * Gets the attribute for specified name and value.
     *
     * @param name The name
     * @param value The value
     * @return The attribute
     * @see #timestampFor(Date, boolean, TimeZone)
     */
    public static DefaultAttribute<String> valueFor(Name name, String value) {
        return valueFor(name, value, 0);
    }

    /**
     * Gets the attribute for specified name and abbreviated value.
     *
     * @param name The name
     * @param value The value
     * @param maxValueLength The max. allowed length for specified value
     * @return The attribute
     * @see #timestampFor(Date, boolean, TimeZone)
     */
    public static DefaultAttribute<String> valueFor(Name name, String value, int maxValueLength) {
        if (null == name || null == value) {
            return null;
        }
        return new DefaultAttribute<String>(name.getId(), maxValueLength > 0 ? Strings.abbreviate(value, maxValueLength) : value, false);
    }

    /**
     * Gets the attribute for specified arbitrary name and value.
     *
     * @param name The name
     * @param value The value
     * @param maxValueLength The max. allowed length for specified value
     * @return The attribute
     */
    public static DefaultAttribute<String> arbitraryFor(String name, String value) {
        return arbitraryFor(name, value, 0);
    }

    /**
     * Gets the attribute for specified arbitrary name and value.
     *
     * @param name The name
     * @param value The value
     * @param maxValueLength The max. allowed length for specified value
     * @return The attribute
     */
    public static DefaultAttribute<String> arbitraryFor(String name, String value, int maxValueLength) {
        if (null == name || null == value) {
            return null;
        }
        return new DefaultAttribute<String>(name, maxValueLength > 0 ? Strings.abbreviate(value, maxValueLength) : value, false);
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    private final String name;
    private final V value;
    private final boolean date;

    /**
     * Initializes a new {@link DefaultAttribute}.
     */
    private DefaultAttribute(String name, V value, boolean date) {
        super();
        this.name = name;
        this.value = value;
        this.date = date;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public boolean isDate() {
        return date;
    }

}
