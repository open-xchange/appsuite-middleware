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

package com.openexchange.dav.caldav.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Date;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.java.util.TimeZones;

/**
 * {@link Bug65943Test}
 * 
 * Umlauts not correctly synced via CalDAV with iOS devices
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class Bug65943Test extends CalDAVTest {

    @Test
    public void testStructuredLocation() throws Exception {
        /*
         * create event with umlauts in X-APPLE-STRUCTURED-LOCATION property on client
         */
        String uid = randomUID();
        Date start = TimeTools.D("next saturday at 06:30", TimeZones.UTC);
        Date end = TimeTools.D("next saturday at 06:30", TimeZones.UTC);
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "VERSION:2.0\r\n" +
            "PRODID:-//Apple Inc.//iOS 12.1.2//EN\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "BEGIN:VEVENT\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART:" + format(start, TimeZones.UTC) + "\r\n" +
            "DTEND:" + format(end, TimeZones.UTC) + "\r\n" +
            "CREATED:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "UID:" + uid + "\r\n" +
            "SUMMARY:Bug65943Test\r\n" +
            "X-APPLE-STRUCTURED-LOCATION;VALUE=URI;X-ADDRESS=\"Berlin, Deutschland\";X-APPLE-RADIUS=70.58738906015142;X-APPLE-REFERENCEFRAME=1;X-TITLE=G\u00fcrtelstra\u00dfe 42:geo:52.511249,13.475021\r\n" +
            "X-FOO;PROPERTY=\u00dcml\u00e4\u00fcte:Bl\u00e4bl\u00e4bl\u00e4\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        /*
         * verify appointment and extended properties on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("DTSTART wrong", start, iCalResource.getVEvent().getDTStart());
        assertEquals("DTEND wrong", end, iCalResource.getVEvent().getDTEnd());
        Property structuredLocationProperty = iCalResource.getVEvent().getProperty("X-APPLE-STRUCTURED-LOCATION");
        assertNotNull(structuredLocationProperty);
        assertEquals("URI", structuredLocationProperty.getAttribute("VALUE"));
        assertEquals("Berlin, Deutschland", structuredLocationProperty.getAttribute("X-ADDRESS"));
        assertEquals("70.58738906015142", structuredLocationProperty.getAttribute("X-APPLE-RADIUS"));
        assertEquals("1", structuredLocationProperty.getAttribute("X-APPLE-REFERENCEFRAME"));
        assertEquals("G\u00fcrtelstra\u00dfe 42", structuredLocationProperty.getAttribute("X-TITLE"));
        assertEquals("geo:52.511249,13.475021", structuredLocationProperty.getValue());
        Property fooProperty = iCalResource.getVEvent().getProperty("X-FOO");
        assertNotNull(fooProperty);
        assertEquals("\u00dcml\u00e4\u00fcte", fooProperty.getAttribute("PROPERTY"));
        assertEquals("Bl\u00e4bl\u00e4bl\u00e4", fooProperty.getValue());
    }

}
