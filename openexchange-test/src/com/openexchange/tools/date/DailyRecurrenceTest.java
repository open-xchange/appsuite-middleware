/*
 * OPEN-XCHANGE - "the communication and information enviroment"
 *
 * All intellectual property rights in the Software are protected by
 * international copyright laws.
 *
 * OPEN-XCHANGE is a trademark of Netline Internet Service GmbH and all other
 * brand and product names are or may be trademarks of, and are used to identify
 * products or services of, their respective owners.
 *
 * Please make sure that third-party modules and libraries are used according to
 * their respective licenses.
 *
 * Any modifications to this package must retain all copyright notices of the
 * original copyright holder(s) for the original code used.
 *
 * After any such modifications, the original code will still remain copyrighted
 * by the copyright holder(s) or original author(s).
 *
 * Copyright (C) 1998 - 2005 Netline Internet Service GmbH
 * mail:                    info@netline-is.de
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.openexchange.tools.date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.TestCase;

/**
 * This class tests the daily recurrence calculation.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class DailyRecurrenceTest extends TestCase {

    /**
     * A day in milli seconds.
     */
    private static final long DAY = 24 * 60 * 60 * 1000;

    /**
     * Test method for 'com.openexchange.tools.date.DailyRecurrence.next(Date)'.
     * @throws Throwable if an error occurs.
     */
    public void testNextDate() throws Throwable {
        final String enddate = "2100-12-31 23:59:59.999";
        final Locale locale = Locale.GERMANY;
        final DateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss.SSS", locale);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        final Date end = format.parse(enddate);
        Date test = new Date(0);
        final AbstractRecurrence recurrence = new DailyRecurrence(1);
        do {
            final Date next = recurrence.next(test);
            assertEquals("Different time than expected.", test.getTime() + DAY,
                next.getTime());
            test = next;
        } while (test.before(end));
    }
}
