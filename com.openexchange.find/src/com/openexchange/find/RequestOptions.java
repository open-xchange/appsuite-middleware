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

    public static final String INCLUDE_CONTEXT_ADMIN = "admin";

    public static final String CLIENT_TIMEZONE = "timezone";

    private final Map<String, String> optionMap;

    private final boolean includeContextAdmin;

    private final String timeZone;

    public RequestOptions(final Map<String, String> optionMap) {
        super();
        this.optionMap = optionMap;
        this.includeContextAdmin = getBoolOption(INCLUDE_CONTEXT_ADMIN, true);
        this.timeZone = getOption(CLIENT_TIMEZONE);
    }

    /**
     * Modules that contain contacts in autocomplete or query responses
     * can be instructed to include or exclude the context administrator.
     *
     * @return Whether the context administrator should be included or not.
     */
    public boolean includeContextAdmin() {
        return includeContextAdmin;
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
