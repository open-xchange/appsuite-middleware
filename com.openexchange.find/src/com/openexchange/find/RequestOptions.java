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

package com.openexchange.find;

import java.util.Map;

/**
 * Encapsulates additional options for find requests. Common options are
 * available via concrete methods. Module-specific ones should be queried
 * by their modules via the generic methods.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class RequestOptions {

    public static final String CLIENT_TIMEZONE = "timezone";

    private final Map<String, String> optionMap;

    private final String timeZone;

    public RequestOptions(final Map<String, String> optionMap) {
        super();
        this.optionMap = optionMap;
        this.timeZone = getOption(CLIENT_TIMEZONE);
    }

    /**
     * The clients time zone that should be used to convert date objects
     * shown to the user. Should match a time zone ID like expected in
     * {@link java.util.TimeZone#getTimeZone(String)}, but is not validated
     * and should therefore be checked.
     *
     * @return The time zone or <code>null</code> if not set.
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * Gets an option by its name.
     *
     * @param The options name.
     * @return The options value or <code>null</code> if not specified.
     */
    public String getOption(String name) {
        return optionMap.get(name);
    }

    /**
     * Gets an option by its name.
     *
     * @param The options name.
     * @param defaultValue The default value if the option is missing.
     * @return The options value or <code>defaultValue</code> if not specified.
     */
    public String getOption(String name, String defaultValue) {
        String tmp = optionMap.get(name);
        if (tmp != null) {
            return tmp;
        }

        return defaultValue;
    }

    /**
     * Gets a boolean option by its name.
     *
     * @param The options name.
     * @return The options value or <code>false</code> if not specified.
     */
    public boolean getBoolOption(String name) {
        return getBoolOption(name, false);
    }

    /**
     * Gets a boolean option by its name.
     *
     * @param The options name.
     * @param defaultValue The default value if the option is missing.
     * @return The options value or <code>defaultValue</code> if not specified.
     */
    public boolean getBoolOption(String name, boolean defaultValue) {
        String tmp = optionMap.get(name);
        if (tmp != null) {
            return Boolean.parseBoolean(tmp);
        }

        return defaultValue;
    }

}
