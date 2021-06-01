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

package com.openexchange.test.common.data.conversion.ical;

import java.text.SimpleDateFormat;
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        sdf.setTimeZone(tz);
        assertProperty(ical, "DTSTART", "TZID=" + tz.getID(), sdf.format(start));
        assertProperty(ical, "DTEND", "TZID=" + tz.getID(), sdf.format(end));
    }

    public static void assertStandardTaskFields(final ICalFile ical, final Date start, final Date end, TimeZone tz) {
        assertProperty(ical, "DTSTART", "TZID=" + tz.getID(), Tools.formatForICalWithoutTimezone(start));
        assertProperty(ical, "DUE", "TZID=" + tz.getID(), Tools.formatForICalWithoutTimezone(end));
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
        assertTrue("\"" + prop + "\" missing in: \n" + ical.toString(), ical.containsPair(name, value));
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
        assertTrue("\"" + prop.toString() + "\" missing in: \n" + ical.toString(), ical.containsEntry(name, parameter, value));
    }

    public static void assertNoProperty(final ICalFile ical, final String name) {
        assertFalse("Didn't expect to find " + name + " in: \n" + ical.toString(), ical.containsKey(name));
    }

    public static void assertLine(final ICalFile ical, final String line) {
        assertTrue(line + " missing in: \n" + ical.toString(), ical.containsLine(line));
    }
}
