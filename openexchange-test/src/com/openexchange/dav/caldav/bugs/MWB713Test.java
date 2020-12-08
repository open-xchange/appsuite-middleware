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
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.test.pool.TestContext;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.test.pool.TestUser;

/**
 * 
 * {@link MWB713Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.5
 */
public class MWB713Test extends CalDAVTest {

    private TestContext context2;
    private TestUser organizerUser;
    private AJAXClient organizerClient;
    private String uid;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        context2 = TestContextPool.acquireContext(this.getClass().getCanonicalName());
        organizerUser = context2.acquireUser();
        organizerClient = generateClient(organizerUser);
        uid = randomUID();
    }

    @Test
    public void testUpdateAsAttendee() throws Exception {
        Date start = TimeTools.D("next monday at 12:00");
        Date end = TimeTools.D("next monday at 13:00");
        Date start2 = TimeTools.D("next monday at 14:00");
        Date end2 = TimeTools.D("next monday at 15:00");

        String attendeeMail = getClient().getValues().getDefaultAddress();
        String organizerMail = organizerClient.getValues().getDefaultAddress();

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

    @Override
    public void tearDown() throws Exception {
        delete(getAppointment(uid));
        super.tearDown();
    }
}
