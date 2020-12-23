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

package com.openexchange.ajax.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.java.util.UUIDs;

/**
 * {@link MWB805Test}
 *
 * WebEX invitations are displaying the incorrect timezone
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class MWB805Test extends ManagedAppointmentTest {

    private TimeZone originalTimezone;
    private TimeZone userTimezone;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        userTimezone = getClient().getValues().getTimeZone();
        assertNotNull(userTimezone);
        if (false == "America/Denver".equals(userTimezone.getID())) {
            originalTimezone = userTimezone;
            userTimezone = TimeZone.getTimeZone("America/Denver");
            getClient().getValues().setTimeZone(userTimezone);
        }
    }

    @Override
    public void tearDown() throws Exception {
        if (null != originalTimezone) {
            getClient().getValues().setTimeZone(originalTimezone);
        }
        super.tearDown();
    }

    @Test
    public void testWebExTimeZoneEurope() throws Exception {
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//Microsoft Corporation//Outlook 12.0 MIMEDIR//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:20001029T030000\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=10;BYHOUR=3\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "TZNAME:Standard Time\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:20000326T020000\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=-1SU;BYMONTH=3;BYHOUR=2\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "TZNAME:Daylight Savings Time\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "UID:" + UUIDs.getUnformattedStringFromRandom() + "\r\n" +
            "DTSTAMP:20201216T140925Z\r\n" +
            "SUMMARY:MWB805Test\r\n" +
            "DTSTART;TZID=\"Europe\":20201216T183000\r\n" +
            "DTEND;TZID=\"Europe\":20201216T193000\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        importAndCheck(iCal, DateTime.parse("Europe/Berlin", "20201216T183000"), DateTime.parse("Europe/Berlin", "20201216T193000"));
    }

    @Test
    public void testWebExTimeZoneEastern() throws Exception {
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//Microsoft Corporation//Outlook 12.0 MIMEDIR//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Eastern\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:20001105T020000\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=1SU;BYMONTH=11\r\n" +
            "TZOFFSETFROM:-0400\r\n" +
            "TZOFFSETTO:-0500\r\n" +
            "TZNAME:Standard Time\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:20000312T020000\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=2SU;BYMONTH=3\r\n" +
            "TZOFFSETFROM:-0500\r\n" +
            "TZOFFSETTO:-0400\r\n" +
            "TZNAME:Daylight Savings Time\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "UID:" + UUIDs.getUnformattedStringFromRandom() + "\r\n" +
            "DTSTAMP:20171013T193710Z\r\n" +
            "SUMMARY:MWB805Test\r\n" +
            "DTSTART;TZID=\"Eastern\":20171014T160000\r\n" +
            "DTEND;TZID=\"Eastern\":20171014T170000\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        importAndCheck(iCal, DateTime.parse("America/New_York", "20171014T160000"), DateTime.parse("America/New_York", "20171014T170000"));
    }

    @Test
    public void testWebExTimeZonePacific() throws Exception {
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR\r\n" +
            "PRODID:-//Microsoft Corporation//Outlook 12.0 MIMEDIR//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Pacific\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:20001105T020000\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=1SU;BYMONTH=11;BYHOUR=2\r\n" +
            "TZOFFSETFROM:-0700\r\n" +
            "TZOFFSETTO:-0800\r\n" +
            "TZNAME:Standard Time\r\n" +
            "END:STANDARD\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:20000312T020000\r\n" +
            "RRULE:FREQ=YEARLY;INTERVAL=1;BYDAY=2SU;BYMONTH=3;BYHOUR=2\r\n" +
            "TZOFFSETFROM:-0800\r\n" +
            "TZOFFSETTO:-0700\r\n" +
            "TZNAME:Daylight Savings Time\r\n" +
            "END:DAYLIGHT\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CLASS:PUBLIC\r\n" +
            "UID:" + UUIDs.getUnformattedStringFromRandom() + "\r\n" +
            "DTSTAMP:20131029T182028Z\r\n" +
            "SUMMARY:MWB805Test\r\n" +
            "DTSTART;TZID=\"Pacific\":20131030T110000\r\n" +
            "DTEND;TZID=\"Pacific\":20131030T120000\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ; // @formatter:on
        importAndCheck(iCal, DateTime.parse("America/Los_Angeles", "20131030T110000"), DateTime.parse("America/Los_Angeles", "20131030T120000"));
    }

    private void importAndCheck(String iCal, DateTime expectedStart, DateTime expectedEnd) throws Exception {
        /*
         * import iCal & check results
         */
        ICalImportResponse importResponse = getClient().execute(new ICalImportRequest(folder.getObjectID(), iCal, false));
        assertNotNull("Should have processed 1 event", importResponse.getImports());
        assertEquals("Should have processed 1 event", 1, importResponse.getImports().length);
        int objectID = Integer.parseInt(importResponse.getImports()[0].getObjectId());
        /*
         * get & check appointment in client timezone
         */
        GetResponse getResponse = getClient().execute(new GetRequest(folder.getObjectID(), objectID, true));
        Appointment appointment = getResponse.getAppointment(userTimezone);
        assertEquals("Wrong start", expectedStart.getTimestamp(), appointment.getStartDate().getTime());
        assertEquals("Wrong end", expectedEnd.getTimestamp(), appointment.getEndDate().getTime());
    }

}
