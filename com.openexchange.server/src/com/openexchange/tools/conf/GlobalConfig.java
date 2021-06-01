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

package com.openexchange.tools.conf;

import java.util.HashMap;

/**
 * GlobalConfig
 *
 * @author <a href="mailto:martin.kauss@open-xchange.com">Martin Kauss</a>
 * @author <a href="mailto:stefan.preuss@open-xchange.com">Stefan Preuss</a>
 * @author <a href="mailto:ben.pahne@open-xchange.com">Benjamin Frederic Pahne</a>
 * @deprecated Use specialized config classes.
 */
@Deprecated
public class GlobalConfig {

    private static final HashMap datePattern = new HashMap();

    public static String getDatePattern(final String country) {
        try {
            final String returnPattern = datePattern.get(country.toUpperCase()).toString();
            return returnPattern.substring(0, returnPattern.indexOf(' '));
        } catch (NullPointerException npe) {
            final String defaultPattern = datePattern.get("DEFAULT").toString();
            return defaultPattern.substring(0, defaultPattern.indexOf(' '));
        }
    }

    public static String getDateTimePattern(final String country) {
        try {
            return datePattern.get(country.toUpperCase()).toString();
        } catch (NullPointerException npe) {
            return datePattern.get("DEFAULT").toString();
        }
    }
}
