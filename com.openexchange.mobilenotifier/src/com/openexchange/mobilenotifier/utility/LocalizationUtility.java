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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mobilenotifier.utility;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * {@link LocalizationUtility}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class LocalizationUtility {
    /**
     * Formats specified date in given style with given locale and time zone.
     * 
     * @param date The date to format
     * @param style The style to use
     * @param locale The locale
     * @param timeZone The time zone
     * @return The formatted date
     */
    public static final String getFormattedDate(final Date date, final int style, final Locale locale, final TimeZone timeZone) {
        final DateFormat dateFormat = DateFormat.getDateInstance(style, locale);
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }

    /**
     * Formats specified time in given style with given locale and time zone.
     * 
     * @param date The time to format
     * @param style The style to use
     * @param locale The locale
     * @param timeZone The time zone
     * @return The formatted time
     */
    public static final String getFormattedTime(final Date date, final int style, final Locale locale, final TimeZone timeZone) {
        final DateFormat dateFormat = DateFormat.getTimeInstance(style, locale);
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }
}
