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

import java.util.UUID;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;


/**
 *  Imagine an ICal file containing a series and a change exception. The appointments contain UIDs.
 *  If you try to import the same file twice, a NullPointerException was thrown formerly, because the
 *  change exception could not refer to its master, whose insertion has been denied because of a UID
 *  conflict.
 *
 *  Currently expected behavior: If inserting a master threw an exception, we simply skip
 *  its change exceptions, as the response already contains an error message for the series master.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug27474Test extends ManagedAppointmentTest {

    public Bug27474Test(String name) {
        super(name);
    }

    public void testRepeatedImportOfASeriesWithChangeException() throws Exception {
        String ical = prepareICal();
        ICalImportResponse icalResponse = getClient().execute(new ICalImportRequest(folder.getObjectID(), ical , false));
        assertFalse("Initial import failed", icalResponse.hasError());
        assertTrue("Should import master and change exception", icalResponse.getImports().length == 2);

        icalResponse = getClient().execute(new ICalImportRequest(folder.getObjectID(), ical , false));
        assertTrue("Expected UID conflict", icalResponse.hasError());
        assertTrue("Should contain only an exception for the master", icalResponse.getImports().length == 1);
    }

    private static String prepareICal() {
        return ICAL.replaceAll("\\$\\{UID\\}", UUID.randomUUID().toString());
    }

    private static final String ICAL = "BEGIN:VCALENDAR\n" +
        "PRODID:Open-Xchange\n" +
        "VERSION:2.0\n" +
        "CALSCALE:GREGORIAN\n" +
        "METHOD:REQUEST\n" +
        "BEGIN:VEVENT\n" +
        "DTSTAMP:20130712T082801Z\n" +
        "SUMMARY:Walk the dog\n" +
        "DTSTART;TZID=Europe/Berlin:20130708T080000\n" +
        "DTEND;TZID=Europe/Berlin:20130708T090000\n" +
        "CLASS:PUBLIC\n" +
        "TRANSP:OPAQUE\n" +
        "RRULE:FREQ=DAILY;INTERVAL=1\n" +
        "UID:${UID}\n" +
        "CREATED:20130712T082735Z\n" +
        "LAST-MODIFIED:20130712T082756Z\n" +
        "ORGANIZER:mailto:steffen.templin@premium\n" +
        "SEQUENCE:1\n" +
        "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL;CN=Steffen Templin:mailto:steffen.templin@premium\n" +
        "END:VEVENT\n" +
        "BEGIN:VTIMEZONE\n" +
        "TZID:Europe/Berlin\n" +
        "TZURL:http://tzurl.org/zoneinfo/Europe/Berlin\n" +
        "X-LIC-LOCATION:Europe/Berlin\n" +
        "BEGIN:DAYLIGHT\n" +
        "TZOFFSETFROM:+0100\n" +
        "TZOFFSETTO:+0200\n" +
        "TZNAME:CEST\n" +
        "DTSTART:19810329T020000\n" +
        "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\n" +
        "END:DAYLIGHT\n" +
        "BEGIN:STANDARD\n" +
        "TZOFFSETFROM:+0200\n" +
        "TZOFFSETTO:+0100\n" +
        "TZNAME:CET\n" +
        "DTSTART:19961027T030000\n" +
        "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\n" +
        "END:STANDARD\n" +
        "BEGIN:STANDARD\n" +
        "TZOFFSETFROM:+005328\n" +
        "TZOFFSETTO:+0100\n" +
        "TZNAME:CET\n" +
        "DTSTART:18930401T000000\n" +
        "RDATE:\n" +
        "END:STANDARD\n" +
        "BEGIN:DAYLIGHT\n" +
        "TZOFFSETFROM:+0100\n" +
        "TZOFFSETTO:+0200\n" +
        "TZNAME:CEST\n" +
        "DTSTART:19160430T230000\n" +
        "END:DAYLIGHT\n" +
        "BEGIN:STANDARD\n" +
        "TZOFFSETFROM:+0200\n" +
        "TZOFFSETTO:+0100\n" +
        "TZNAME:CET\n" +
        "DTSTART:19161001T010000\n" +
        "END:STANDARD\n" +
        "BEGIN:DAYLIGHT\n" +
        "TZOFFSETFROM:+0200\n" +
        "TZOFFSETTO:+0300\n" +
        "TZNAME:CEMT\n" +
        "DTSTART:19450524T020000\n" +
        "END:DAYLIGHT\n" +
        "BEGIN:DAYLIGHT\n" +
        "TZOFFSETFROM:+0300\n" +
        "TZOFFSETTO:+0200\n" +
        "TZNAME:CEST\n" +
        "DTSTART:19450924T030000\n" +
        "END:DAYLIGHT\n" +
        "BEGIN:STANDARD\n" +
        "TZOFFSETFROM:+0100\n" +
        "TZOFFSETTO:+0100\n" +
        "TZNAME:CET\n" +
        "DTSTART:19460101T000000\n" +
        "END:STANDARD\n" +
        "END:VTIMEZONE\n" +
        "BEGIN:VEVENT\n" +
        "DTSTAMP:20130712T082802Z\n" +
        "SUMMARY:Walk the dog\n" +
        "DTSTART;TZID=Europe/Berlin:20130709T100000\n" +
        "DTEND;TZID=Europe/Berlin:20130709T110000\n" +
        "CLASS:PUBLIC\n" +
        "TRANSP:OPAQUE\n" +
        "RECURRENCE-ID:20130709T060000Z\n" +
        "UID:${UID}\n" +
        "CREATED:20130712T082756Z\n" +
        "LAST-MODIFIED:20130712T082756Z\n" +
        "ORGANIZER:mailto:steffen.templin@premium\n" +
        "SEQUENCE:0\n" +
        "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL;CN=Steffen Templin:mailto:steffen.templin@premium\n" +
        "END:VEVENT\n" +
        "END:VCALENDAR";

}
