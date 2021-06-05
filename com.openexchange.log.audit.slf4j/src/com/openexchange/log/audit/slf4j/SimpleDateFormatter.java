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

package com.openexchange.log.audit.slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * {@link SimpleDateFormatter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class SimpleDateFormatter implements DateFormatter {

    /** The GMT time zone */
    private static final TimeZone TIMEZONE_GMT = TimeZone.getTimeZone("GMT");

    /**
     * Creates a new instance from specified attributes.
     *
     * @param pattern The date pattern
     * @param locale The locale
     * @param timeZone The time zone
     * @return The new formatter instance
     */
    public static SimpleDateFormatter newInstance(String pattern, Locale locale, TimeZone timeZone) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, null == locale ? Locale.US : locale);
        sdf.setTimeZone(null == timeZone ? TIMEZONE_GMT : timeZone);
        return new SimpleDateFormatter(sdf);
    }

    // ------------------------------------------------------------------------------------------------------------------

    private final SimpleDateFormat sdf;

    /**
     * Initializes a new {@link SimpleDateFormatter}.
     */
    private SimpleDateFormatter(SimpleDateFormat sdf) {
        super();
        this.sdf = sdf;
    }

    @Override
    public String format(Date date) {
        synchronized (sdf) {
            return sdf.format(date);
        }
    }

}
