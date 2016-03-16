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

package com.openexchange.file.storage.onedrive.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.lang.StringUtils;


/**
 * {@link ISO8601DateParser}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public class ISO8601DateParser {

    // Use ThreadLocal because SimpleDateFormat is not thread safe. See http://www.javacodegeeks.com/2010/07/java-best-practices-dateformat-in.html.
    private final static ThreadLocal<DateFormat> mThreadLocalSimpleDateFormat = new ThreadLocal<DateFormat>() {

        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
        }

    };

    // 2004-06-14T19:GMT20:30Z
    // 2004-06-20T06:GMT22:01Z

    // http://www.cl.cam.ac.uk/~mgk25/iso-time.html
    //
    // http://www.intertwingly.net/wiki/pie/DateTime
    //
    // http://www.w3.org/TR/NOTE-datetime
    //
    // Different standards may need different levels of granularity in the date and
    // time, so this profile defines six levels. Standards that reference this
    // profile should specify one or more of these granularities. If a given
    // standard allows more than one granularity, it should specify the meaning of
    // the dates and times with reduced precision, for example, the result of
    // comparing two dates with different precisions.

    // The formats are as follows. Exactly the components shown here must be
    // present, with exactly this punctuation. Note that the "T" appears literally
    // in the string, to indicate the beginning of the time element, as specified in
    // ISO 8601.

    // Year:
    // YYYY (eg 1997)
    // Year and month:
    // YYYY-MM (eg 1997-07)
    // Complete date:
    // YYYY-MM-DD (eg 1997-07-16)
    // Complete date plus hours and minutes:
    // YYYY-MM-DDThh:mmTZD (eg 1997-07-16T19:20+01:00)
    // Complete date plus hours, minutes and seconds:
    // YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30+01:00)
    // Complete date plus hours, minutes, seconds and a decimal fraction of a
    // second
    // YYYY-MM-DDThh:mm:ss.sTZD (eg 1997-07-16T19:20:30.45+01:00)

    // where:

    // YYYY = four-digit year
    // MM = two-digit month (01=January, etc.)
    // DD = two-digit day of month (01 through 31)
    // hh = two digits of hour (00 through 23) (am/pm NOT allowed)
    // mm = two digits of minute (00 through 59)
    // ss = two digits of second (00 through 59)
    // s = one or more digits representing a decimal fraction of a second
    // TZD = time zone designator (Z or +hh:mm or -hh:mm)
    public static Date parse(String input) throws java.text.ParseException {

        // NOTE: SimpleDateFormat uses GMT[-+]hh:mm for the TZ which breaks
        // things a bit. Before we go on we have to repair this.
        DateFormat df = mThreadLocalSimpleDateFormat.get();

        // this is zero time so we need to add that TZ indicator for
        if (input.endsWith("Z")) {
            input = input.substring(0, input.length() - 1) + "GMT-00:00";
        }
        else {
            int inset = 6;

            String s0 = input.substring(0, input.length() - inset);
            String s1 = input.substring(input.length() - inset, input.length());

            input = s0 + "GMT" + s1;
        }

        return df.parse(input);

    }

    /**
     * Same as parse method but does not throws. In case input date string cannot be parsed, null is returned.
     */
    public static Date parseSilently(String input) {
        try {
            Date date = StringUtils.isEmpty(input) ? null : parse(input);
            return date;
        }
        catch (ParseException e) {
            return null;
        }
    }

    public static String toString(Date date) {
        DateFormat df = mThreadLocalSimpleDateFormat.get();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        df.setTimeZone(tz);
        String output = df.format(date);
        String result = output.replaceAll("UTC", "+00:00");
        return result;
    }

}
