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
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.test.common.groupware.calendar.TimeTools;

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
        String iCal = "BEGIN:VCALENDAR\r\n" + "CALSCALE:GREGORIAN\r\n" + "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" + "VERSION:2.0\r\n" + "BEGIN:VTIMEZONE\r\n" + "TZID:Europe/Berlin\r\n" + "BEGIN:DAYLIGHT\r\n" + "DTSTART:19810329T020000\r\n" + "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" + "TZNAME:MESZ\r\n" + "TZOFFSETFROM:+0100\r\n" + "TZOFFSETTO:+0200\r\n" + "END:DAYLIGHT\r\n" + "BEGIN:STANDARD\r\n" + "DTSTART:19961027T030000\r\n" + "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" + "TZNAME:MEZ\r\n" + "TZOFFSETFROM:+0200\r\n" + "TZOFFSETTO:+0100\r\n" + "END:STANDARD\r\n" + "END:VTIMEZONE\r\n" + "BEGIN:VEVENT\r\n" + "CREATED:" + formatAsUTC(created) + "\r\n" + "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" + "DTSTAMP:" + formatAsUTC(lastModified) + "\r\n" + "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" + "LAST-MODIFIED:" + formatAsUTC(lastModified) + "\r\n" + "SUMMARY:testimport\r\n" + "UID:" + uid + "\r\n" + "END:VEVENT\r\n" + "END:VCALENDAR\r\n";
        /*
         * import iCal appointment on server
         */
        int parentFolderID = Integer.parseInt(getDefaultFolderID());
        ICalImportRequest importRequest = new ICalImportRequest(parentFolderID, Streams.newByteArrayInputStream(iCal.getBytes(Charsets.UTF_8)));
        ICalImportResponse importResponse = getClient().execute(importRequest);
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
        String iCal = "BEGIN:VCALENDAR\r\n" + "CALSCALE:GREGORIAN\r\n" + "PRODID:-//Apple Inc.//iOS 9.1//EN\r\n" + "VERSION:2.0\r\n" + "BEGIN:VTIMEZONE\r\n" + "TZID:Europe/Berlin\r\n" + "BEGIN:DAYLIGHT\r\n" + "DTSTART:19810329T020000\r\n" + "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\r\n" + "TZNAME:MESZ\r\n" + "TZOFFSETFROM:+0100\r\n" + "TZOFFSETTO:+0200\r\n" + "END:DAYLIGHT\r\n" + "BEGIN:STANDARD\r\n" + "DTSTART:19961027T030000\r\n" + "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\r\n" + "TZNAME:MEZ\r\n" + "TZOFFSETFROM:+0200\r\n" + "TZOFFSETTO:+0100\r\n" + "END:STANDARD\r\n" + "END:VTIMEZONE\r\n" + "BEGIN:VEVENT\r\n" + "CREATED:" + formatAsUTC(created) + "\r\n" + "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\r\n" + "DTSTAMP:" + formatAsUTC(lastModified) + "\r\n" + "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\r\n" + "LAST-MODIFIED:" + formatAsUTC(lastModified) + "\r\n" + "SUMMARY:testimport\r\n" + "UID:" + uid + "\r\n" + "END:VEVENT\r\n" + "END:VCALENDAR\r\n";
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
