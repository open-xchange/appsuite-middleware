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
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.test.common.groupware.calendar.TimeTools;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.test.common.test.pool.TestUser;

/**
 *
 * {@link MWB713Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.5
 */
public class MWB713Test extends CalDAVTest {

    private TestUser organizer;
    private String uid;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        organizer = testContextList.get(1).acquireUser();
        uid = randomUID();
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().createApiClient().withUserPerContext(1).withContexts(2).useEnhancedApiClients().build();
    }

    @Test
    public void testUpdateAsAttendee() throws Exception {
        Date start = TimeTools.D("next monday at 12:00");
        Date end = TimeTools.D("next monday at 13:00");
        Date start2 = TimeTools.D("next monday at 14:00");
        Date end2 = TimeTools.D("next monday at 15:00");

        String attendeeMail = getClient().getValues().getDefaultAddress();
        String organizerMail = organizer.getLogin();

        // @formatter:off
        String create =
            "BEGIN:VCALENDAR\n" +
            "VERSION:2.0\n" +
            "PRODID:-//Open-Xchange//7.10.5-Rev0//EN\n" +
            "METHOD:REQUEST\n" +
            "BEGIN:VEVENT\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start, "Europe/Berlin") + "\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end, "Europe/Berlin") + "\n" +
            "ATTENDEE;CN=A;PARTSTAT=NEEDS-ACTION;CUTYPE=INDIVIDUAL;EMAIL=" + organizerMail + ":mailto:" + organizerMail + "\n" +
            "ATTENDEE;CN=B;PARTSTAT=ACCEPTED;CUTYPE=INDIVIDUAL;EMAIL=" + attendeeMail + ":mailto:" + attendeeMail + "\n" +
            "CLASS:PUBLIC\n" +
            "CREATED:20201204T091730Z\n" +
            "LAST-MODIFIED:20201204T091730Z\n" +
            "ORGANIZER;CN=A:mailto:" + organizerMail + "\n" +
            "SEQUENCE:0\n" +
            "SUMMARY:MWB713Test\n" +
            "TRANSP:OPAQUE\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\n" +
            "END:VEVENT\n" +
            "END:VCALENDAR\n";

        String update =
            "BEGIN:VCALENDAR\n" +
            "VERSION:2.0\n" +
            "PRODID:-//Open-Xchange//7.10.5-Rev0//EN\n" +
            "METHOD:REQUEST\n" +
            "BEGIN:VEVENT\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\n" +
            "DTSTART;TZID=Europe/Berlin:" + format(start2, "Europe/Berlin") + "\n" +
            "DTEND;TZID=Europe/Berlin:" + format(end2, "Europe/Berlin") + "\n" +
            "ATTENDEE;CN=A;PARTSTAT=NEEDS-ACTION;CUTYPE=INDIVIDUAL;EMAIL=" + organizerMail + ":mailto:" + organizerMail + "\n" +
            "ATTENDEE;CN=B;PARTSTAT=ACCEPTED;CUTYPE=INDIVIDUAL;EMAIL=" + attendeeMail + ":mailto:" + attendeeMail + "\n" +
            "CLASS:PUBLIC\n" +
            "CREATED:20201204T091730Z\n" +
            "LAST-MODIFIED:20201204T091730Z\n" +
            "ORGANIZER;CN=A:mailto:" + organizerMail + "\n" +
            "SEQUENCE:0\n" +
            "SUMMARY:MWB713Test\n" +
            "TRANSP:OPAQUE\n" +
            "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\n" +
            "END:VEVENT\n" +
            "END:VCALENDAR\n";
        // @formatter:on

        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, create));
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals(start.getTime(), iCalResource.getVEvent().getDTStart().getTime());
        assertEquals(end.getTime(), iCalResource.getVEvent().getDTEnd().getTime());

        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(uid, update, iCalResource.getETag()));

        iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals(start2.getTime(), iCalResource.getVEvent().getDTStart().getTime());
        assertEquals(end2.getTime(), iCalResource.getVEvent().getDTEnd().getTime());
    }

}
