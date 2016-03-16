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

package com.openexchange.data.conversion.ical;

import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Assert extends org.junit.Assert {

    /**
     * Prevent instantiation.
     */
    private Assert() {
        super();
    }

    public static void assertStandardAppFields(final ICalFile ical, final Date start, final Date end, TimeZone tz) {
        assertProperty(ical, "DTSTART", "TZID=" + tz.getID(), Tools.formatForICalWithoutTimezone(start));
        assertProperty(ical, "DTEND",   "TZID=" + tz.getID(), Tools.formatForICalWithoutTimezone(end));
    }

    public static void assertStandardTaskFields(final ICalFile ical, final Date start, final Date end, TimeZone tz) {
        assertProperty(ical, "DTSTART", "TZID=" + tz.getID(), Tools.formatForICalWithoutTimezone(start));
        assertProperty(ical, "DUE",     "TZID=" + tz.getID(), Tools.formatForICalWithoutTimezone(end));
    }

    public static void assertStandardAppFields(final ICalFile ical, final Date start, final Date end) {
        assertProperty(ical, "DTSTART", Tools.formatForICal(start));
        assertProperty(ical, "DTEND", Tools.formatForICal(end));
    }

    public static void assertStandardTaskFields(final ICalFile ical, final Date start, final Date end) {
        assertProperty(ical, "DTSTART", Tools.formatForICal(start));
        assertProperty(ical, "DUE", Tools.formatForICal(end));
    }

    public static void assertProperty(final ICalFile ical, final String name, final String value) {
        String prop = name + (null == value ? "" : ":" + value);
        assertTrue("\""+prop+"\" missing in: \n"+ical.toString(), ical.containsPair(name, value));
    }

    public static void assertProperty(ICalFile ical, String name, String parameter, String value) {
        StringBuilder prop = new StringBuilder(24);
        prop.append(name);
        if (null != parameter) {
            prop.append(';').append(parameter);
        }
        if (null != value) {
            prop.append(':').append(value);
        }
        assertTrue("\""+prop.toString()+"\" missing in: \n"+ical.toString(), ical.containsEntry(name, parameter, value));
    }

    public static void assertNoProperty(final ICalFile ical, final String name) {
        assertFalse("Didn't expect to find "+name+" in: \n"+ical.toString(), ical.containsKey(name));
    }

    public static void assertLine(final ICalFile ical, final String line) {
        assertTrue(line+" missing in: \n"+ical.toString(), ical.containsLine(line));
    }
}
