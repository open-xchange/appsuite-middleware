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
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

/**
 * {@link Bug44144Test}
 *
 * Appointments which have been imported via iCal import aren't synced using CalDAV
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug44144Test extends CalDAVTest {

    @Test
    public void testSyncImportedAppointment() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken());
        /*
         * prepare iCal file to import
         */
        String uid = randomUID();
        Date start = TimeTools.D("tomorrow at 6am");
        Date end = TimeTools.D("tomorrow at 8am");
        Date lastModified = TimeTools.D("Last month");
        Date created = TimeTools.D("Last month");
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(created) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(lastModified) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(lastModified) + "\r\n" +
            "SUMMARY:testimport\r\n" +
            "UID:" + uid + "\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        /*
         * import iCal appointment on server
         */
        int parentFolderID = Integer.parseInt(getDefaultFolderID());
        ICalImportRequest importRequest = new ICalImportRequest(parentFolderID, Streams.newByteArrayInputStream(iCal.getBytes(Charsets.UTF_8)));
        ICalImportResponse importResponse = client.execute(importRequest);
        ImportResult[] importResult = importResponse.getImports();
        assertNotNull("No import result", importResult);
        assertEquals("Unexpected number of import results", 1, importResult.length);
        int objectID = Integer.parseInt(importResult[0].getObjectId());
        /*
         * verify appointment on server
         */
        Appointment appointment = getManager().get(parentFolderID, objectID);
        assertNotNull("Appointment not found", appointment);
        rememberForCleanUp(appointment);
        /*
         * verify appointment on client
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
    }

    @Test
    public void testSyncImportedAppointmentFromClient() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken());
        /*
         * prepare iCal file to import
         */
        String uid = randomUID();
        Date start = TimeTools.D("tomorrow at 6am");
        Date end = TimeTools.D("tomorrow at 8am");
        Date lastModified = TimeTools.D("Last month");
        Date created = TimeTools.D("Last month");
        String iCal =
            "BEGIN:VCALENDAR\r\n" +
            "CALSCALE:GREGORIAN\r\n" +
            "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" +
            "VERSION:2.0\r\n" +
            "BEGIN:VTIMEZONE\r\n" +
            "TZID:Europe/Berlin\r\n" +
            "BEGIN:DAYLIGHT\r\n" +
            "DTSTART:19810329T020000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" +
            "TZNAME:MESZ\r\n" +
            "TZOFFSETFROM:+0100\r\n" +
            "TZOFFSETTO:+0200\r\n" +
            "END:DAYLIGHT\r\n" +
            "BEGIN:STANDARD\r\n" +
            "DTSTART:19961027T030000\r\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" +
            "TZNAME:MEZ\r\n" +
            "TZOFFSETFROM:+0200\r\n" +
            "TZOFFSETTO:+0100\r\n" +
            "END:STANDARD\r\n" +
            "END:VTIMEZONE\r\n" +
            "BEGIN:VEVENT\r\n" +
            "CREATED:" + formatAsUTC(created) + "\r\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" +
            "DTSTAMP:" + formatAsUTC(lastModified) + "\r\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" +
            "LAST-MODIFIED:" + formatAsUTC(lastModified) + "\r\n" +
            "SUMMARY:testimport\r\n" +
            "UID:" + uid + "\r\n" +
            "END:VEVENT\r\n" +
            "END:VCALENDAR\r\n"
        ;
        /*
         * import iCal file from client and sync
         */
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        /*
         * verify appointment on client
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
    }

}
