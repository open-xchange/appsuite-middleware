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
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link MWB456Test}
 *
 * CalDAV: Appointment does not get synced when a X-ALT-DESC field is present
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class MWB456Test extends CalDAVTest {

    @Test
    public void testXmlEscaping() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken());
        /*
         * create event with CDATA section in property on client
         */
        String uid = randomUID();
        Date start = TimeTools.D("next monday at 12:00");
        Date end = TimeTools.D("next monday at 13:00");
        String altDesc =
            "<html><head><style id=\"css_styles\">/*<![CDATA[*/ blockquote.cite { margin-left: 5px } /*]]>*/</style></head>" +
            "<body>Themen beim naechsten Mal:<br>Umsetzung als elektronische Version</body></html>"
        ;
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR" + "\r\n" +
            "VERSION:2.0" + "\r\n" +
            "BEGIN:VEVENT" + "\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART:" + formatAsUTC(start) + "\r\n" +
            "DTEND:" + formatAsUTC(end) + "\r\n" +
            "SUMMARY:test\r\n" +
            "DESCRIPTION:test\r\n" +
            "X-ALT-DESC;FMTTYPE=text/html:" + altDesc + "\r\n" +
            "END:VEVENT" + "\r\n" +
            "END:VCALENDAR" + "\r\n";
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = getAppointment(uid);
        rememberForCleanUp(appointment);
        assertEquals(uid, appointment.getUid());
        /*
         * verify appointment on client via GET
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("X-ALT-DESC wrong", altDesc, iCalResource.getVEvent().getPropertyValue("X-ALT-DESC"));
        /*
         * verify appointment on client via sync-collection report
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = calendarMultiget(eTags.keySet());
        iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("X-ALT-DESC wrong", altDesc, iCalResource.getVEvent().getPropertyValue("X-ALT-DESC"));
    }

}
