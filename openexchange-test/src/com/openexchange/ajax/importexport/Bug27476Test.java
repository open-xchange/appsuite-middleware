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
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug27476Test extends ManagedAppointmentTest {

    public Bug27476Test(String name) {
        super(name);
    }

    public void testRepeatedImportOfASeriesWithChangeException() throws Exception {
        String ical = prepareICal();
        ICalImportResponse icalResponse = getClient().execute(new ICalImportRequest(folder.getObjectID(), ical , false));
        assertFalse("Initial import failed", icalResponse.hasError());
        assertTrue("Should import master and change exception", icalResponse.getImports().length == 2);
    }

    private static String prepareICal() {
        String tmp = ICAL.replaceAll("\\$\\{UID\\}", UUID.randomUUID().toString());
        String recurrence = UUID.randomUUID().toString();
        return tmp.replaceAll("\\$\\{UID1\\}", recurrence);
    }

    private static final String ICAL = "BEGIN:VCALENDAR\n" +
        "PRODID:Open-Xchange\n" +
        "VERSION:2.0\n" +
        "CALSCALE:GREGORIAN\n" +
        "METHOD:REQUEST\n" +
        "BEGIN:VEVENT\n" +
        "DTSTAMP:20130709T213302Z\n" +
        "SUMMARY:new test\n" +
        "DTSTART;TZID=America/New_York:20130705T160000\n" +
        "DTEND;TZID=America/New_York:20130705T170000\n" +
        "CLASS:PUBLIC\n" +
        "LOCATION:rwc\n" +
        "TRANSP:OPAQUE\n" +
        "UID:${UID}\n" +
        "CREATED:20130705T213744Z\n" +
        "LAST-MODIFIED:20130705T213744Z\n" +
        "ORGANIZER:mailto:app102@openwave.com\n" +
        "SEQUENCE:0\n" +
        "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:app101@openwave.com\n" +
        "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL;CN=app102:mailto:app102@openwave.com\n" +
        "END:VEVENT\n" +
        "BEGIN:VTIMEZONE\n" +
        "TZID:America/New_York\n" +
        "TZURL:http://tzurl.org/zoneinfo/America/New_York\n" +
        "X-LIC-LOCATION:America/New_York\n" +
        "BEGIN:DAYLIGHT\n" +
        "TZOFFSETFROM:-0500\n" +
        "TZOFFSETTO:-0400\n" +
        "TZNAME:EDT\n" +
        "DTSTART:20070311T020000\n" +
        "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=2SU\n" +
        "END:DAYLIGHT\n" +
        "BEGIN:STANDARD\n" +
        "TZOFFSETFROM:-0400\n" +
        "TZOFFSETTO:-0500\n" +
        "TZNAME:EST\n" +
        "DTSTART:20071104T020000\n" +
        "RRULE:FREQ=YEARLY;BYMONTH=11;BYDAY=1SU\n" +
        "END:STANDARD\n" +
        "BEGIN:STANDARD\n" +
        "TZOFFSETFROM:-045602\n" +
        "TZOFFSETTO:-0500\n" +
        "TZNAME:EST\n" +
        "DTSTART:18831118T120358\n" +
        "RDATE:\n" +
        "END:STANDARD\n" +
        "BEGIN:DAYLIGHT\n" +
        "TZOFFSETFROM:-0500\n" +
        "TZOFFSETTO:-0400\n" +
        "TZNAME:EDT\n" +
        "DTSTART:19180331T020000\n" +
        "END:DAYLIGHT\n" +
        "BEGIN:STANDARD\n" +
        "TZOFFSETFROM:-0400\n" +
        "TZOFFSETTO:-0500\n" +
        "TZNAME:EST\n" +
        "DTSTART:19181027T020000\n" +
        "END:STANDARD\n" +
        "BEGIN:STANDARD\n" +
        "TZOFFSETFROM:-0500\n" +
        "TZOFFSETTO:-0500\n" +
        "TZNAME:EST\n" +
        "DTSTART:19200101T000000\n" +
        "END:STANDARD\n" +
        "BEGIN:DAYLIGHT\n" +
        "TZOFFSETFROM:-0500\n" +
        "TZOFFSETTO:-0400\n" +
        "TZNAME:EWT\n" +
        "DTSTART:19420209T020000\n" +
        "RDATE:\n" +
        "END:DAYLIGHT\n" +
        "BEGIN:DAYLIGHT\n" +
        "TZOFFSETFROM:-0400\n" +
        "TZOFFSETTO:-0400\n" +
        "TZNAME:EPT\n" +
        "DTSTART:19450814T190000\n" +
        "RDATE:\n" +
        "END:DAYLIGHT\n" +
        "END:VTIMEZONE\n" +
        "BEGIN:VEVENT\n" +
        "DTSTAMP:20130709T213358Z\n" +
        "SUMMARY:new test test\n" +
        "DTSTART;TZID=America/New_York:20130705T180000\n" +
        "DTEND;TZID=America/New_York:20130705T190000\n" +
        "CLASS:PUBLIC\n" +
        "LOCATION:rwc\n" +
        "TRANSP:OPAQUE\n" +
        "UID:${UID}\n" +
        "CREATED:20130705T213912Z\n" +
        "LAST-MODIFIED:20130705T213912Z\n" +
        "ORGANIZER:mailto:app102@openwave.com\n" +
        "SEQUENCE:0\n" +
        "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:app101@openwave.com\n" +
        "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL;CN=app102:mailto:app102@openwave.com\n" +
        "END:VEVENT\n" +
        "BEGIN:VEVENT\n" +
        "DTSTAMP:20130709T213358Z\n" +
        "SUMMARY:appt 1\n" +
        "DTSTART;TZID=America/New_York:20130708T090000\n" +
        "DTEND;TZID=America/New_York:20130708T100000\n" +
        "CLASS:PUBLIC\n" +
        "LOCATION:rwc\n" +
        "TRANSP:OPAQUE\n" +
        "UID:${UID}\n" +
        "CREATED:20130705T223643Z\n" +
        "LAST-MODIFIED:20130705T223643Z\n" +
        "ORGANIZER:mailto:app102@openwave.com\n" +
        "SEQUENCE:0\n" +
        "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:app101@openwave.com\n" +
        "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL;CN=app102:mailto:app102@openwave.com\n" +
        "END:VEVENT\n" +
        "BEGIN:VEVENT\n" +
        "DTSTAMP:20130709T213358Z\n" +
        "SUMMARY:appt 2\n" +
        "DTSTART;TZID=America/New_York:20130708T090000\n" +
        "DTEND;TZID=America/New_York:20130708T110000\n" +
        "CLASS:PUBLIC\n" +
        "LOCATION:rwc\n" +
        "TRANSP:OPAQUE\n" +
        "UID:${UID}\n" +
        "CREATED:20130705T223723Z\n" +
        "LAST-MODIFIED:20130705T223825Z\n" +
        "ORGANIZER:mailto:app102@openwave.com\n" +
        "SEQUENCE:1\n" +
        "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:app101@openwave.com\n" +
        "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL;CN=app102:mailto:app102@openwave.com\n" +
        "END:VEVENT\n" +
        "BEGIN:VEVENT\n" +
        "DTSTAMP:20130709T213358Z\n" +
        "SUMMARY:test series\n" +
        "DTSTART;TZID=America/New_York:20130709T190000\n" +
        "DTEND;TZID=America/New_York:20130709T200000\n" +
        "CLASS:PUBLIC\n" +
        "LOCATION:rwc\n" +
        "TRANSP:OPAQUE\n" +
        "RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=TU\n" +
        "UID:${UID1}\n" +
        "CREATED:20130705T225751Z\n" +
        "LAST-MODIFIED:20130705T225854Z\n" +
        "ORGANIZER:mailto:app102@openwave.com\n" +
        "SEQUENCE:1\n" +
        "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:app101@openwave.com\n" +
        "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL;CN=app102:mailto:app102@openwave.com\n" +
        "BEGIN:VALARM\n" +
        "TRIGGER:-PT15M\n" +
        "ACTION:DISPLAY\n" +
        "DESCRIPTION:Open-XChange\n" +
        "END:VALARM\n" +
        "END:VEVENT\n" +
        "BEGIN:VEVENT\n" +
        "DTSTAMP:20130709T213358Z\n" +
        "SUMMARY:test series\n" +
        "DTSTART;TZID=America/New_York:20130709T200000\n" +
        "DTEND;TZID=America/New_York:20130709T230000\n" +
        "CLASS:PUBLIC\n" +
        "LOCATION:rwc\n" +
        "TRANSP:OPAQUE\n" +
        "RECURRENCE-ID:20130709T230000Z\n" +
        "UID:${UID1}\n" +
        "CREATED:20130705T225824Z\n" +
        "LAST-MODIFIED:20130705T225854Z\n" +
        "ORGANIZER:mailto:app102@openwave.com\n" +
        "SEQUENCE:1\n" +
        "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:app101@openwave.com\n" +
        "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL;CN=app102:mailto:app102@openwave.com\n" +
        "BEGIN:VALARM\n" +
        "TRIGGER:-PT15M\n" +
        "ACTION:DISPLAY\n" +
        "DESCRIPTION:Open-XChange\n" +
        "END:VALARM\n" +
        "END:VEVENT\n" +
        "BEGIN:VEVENT\n" +
        "DTSTAMP:20130709T213358Z\n" +
        "SUMMARY:test RSVP\n" +
        "DESCRIPTION:test RSVP\n" +
        "DTSTART;TZID=America/New_York:20130705T180000\n" +
        "DTEND;TZID=America/New_York:20130705T190000\n" +
        "CLASS:PUBLIC\n" +
        "LOCATION:rwc\n" +
        "TRANSP:OPAQUE\n" +
        "RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=FR\n" +
        "UID:${UID}\n" +
        "CREATED:20130708T185749Z\n" +
        "LAST-MODIFIED:20130705T214908Z\n" +
        "ORGANIZER:mailto:app101@openwave.com\n" +
        "SEQUENCE:0\n" +
        "ATTENDEE;CUTYPE=INDIVIDUAL;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT;RSVP=TRUE:mailto:app101@openwave.com\n" +
        "ATTENDEE;ROLE=REQ-PARTICIPANT;CUTYPE=INDIVIDUAL;CN=app102:mailto:app102@openwave.com\n" +
        "END:VEVENT\n" +
        "END:VCALENDAR";

}
