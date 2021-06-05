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
        /**
         * The session identifier.
         */
        SESSION_ID("session"),
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
