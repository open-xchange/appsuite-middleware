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
    public static Date getDateRFC822(final String string) {
        final String s = PATTERN_RFC822_FIX.matcher(string).replaceFirst("");
        try {
            synchronized (DATEFORMAT_RFC822) {
                return DATEFORMAT_RFC822.parse(s);
            }
        } catch (final ParseException e) {
            try {
                synchronized (DATEFORMAT_RFC822_RETRY) {
                    return DATEFORMAT_RFC822_RETRY.parse(s);
                }
            } catch (final ParseException e1) {
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
    public static String toStringRFC822(final Date d) {
        return toStringRFC822(d, TimeZone.getDefault());
    }

    /**
     * Gets the corresponding RFC822 date string from specified instance of {@link Date}
     *
     * @param d The instance of {@link Date} to convert
     * @param tz The time zone
     * @return The corresponding RFC822 date string
     */
    public static String toStringRFC822(final Date d, final TimeZone tz) {
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
