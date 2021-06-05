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

package com.openexchange.mail.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * {@link DateUtils} - Provides some date-related utility constants/methods
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DateUtils {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DateUtils.class);

    private static final DateFormat DATEFORMAT_RFC822 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);

    private static final DateFormat DATEFORMAT_RFC822_RETRY = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);

    private static final Pattern PATTERN_RFC822_FIX = Pattern.compile(",(?= 20[0-9][0-9])");

    /**
     * Gets the corresponding instance of {@link Date} from specified RFC822 date string
     *
     * @param string The RFC822 date string
     * @return The corresponding instance of {@link Date}
     * @throws IllegalArgumentException If specified string cannot be parsed to date
     */
    public static Date getDateRFC822(String string) {
        final String s = PATTERN_RFC822_FIX.matcher(string).replaceFirst("");
        try {
            synchronized (DATEFORMAT_RFC822) {
                return DATEFORMAT_RFC822.parse(s);
            }
        } catch (ParseException e) {
            try {
                synchronized (DATEFORMAT_RFC822_RETRY) {
                    return DATEFORMAT_RFC822_RETRY.parse(s);
                }
            } catch (ParseException e1) {
                LOG.trace("", e1);
            }
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Gets the corresponding RFC822 date string from specified instance of {@link Date}
     *
     * @param d The instance of {@link Date} to convert
     * @return The corresponding RFC822 date string
     */
    public static String toStringRFC822(Date d) {
        return toStringRFC822(d, TimeZone.getDefault());
    }

    /**
     * Gets the corresponding RFC822 date string from specified instance of {@link Date}
     *
     * @param d The instance of {@link Date} to convert
     * @param tz The time zone
     * @return The corresponding RFC822 date string
     */
    public static String toStringRFC822(Date d, TimeZone tz) {
        synchronized (DATEFORMAT_RFC822) {
            DATEFORMAT_RFC822.setTimeZone(tz);
            return DATEFORMAT_RFC822.format(d);
        }
    }

    /**
     * Initializes a new {@link DateUtils}
     */
    private DateUtils() {
        super();
    }
}
