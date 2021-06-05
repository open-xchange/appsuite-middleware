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

package com.openexchange.dav.caldav.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.chronos.ResourceId;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.resource.Resource;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link ResourceAttendeeTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.3
 */
public class ResourceAttendeeTest extends CalDAVTest {

    private Resource resource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Integer res = testContext.acquireResource();
        Assert.assertNotNull(res);
        resource = resTm.get(res.intValue());
    }

    @Test
    public void testCreateViaResourceIdWithCorrectCUType() throws Exception {
        int contextId = getClient().getValues().getContextId();
        testCreate(resource.getIdentifier(), ResourceId.forResource(contextId, resource.getIdentifier()), "RESOURCE");
    }

    @Test
    public void testCreateViaMailWithCorrectCUType() throws Exception {
        testCreate(resource.getIdentifier(), "mailto:" + resource.getMail(), "RESOURCE");
    }

    @Test
    public void testCreateViaResourceIdWithoutCUType() throws Exception {
        int contextId = getClient().getValues().getContextId();
        testCreate(resource.getIdentifier(), ResourceId.forResource(contextId, resource.getIdentifier()), null);
    }

    @Test
    public void testCreateViaMailWithoutCUType() throws Exception {
        testCreate(resource.getIdentifier(), "mailto:" + resource.getMail(), null);
    }

    @Test
    public void testCreateViaResourceIdWithIncorrectCUType() throws Exception {
        int contextId = getClient().getValues().getContextId();
        testCreate(resource.getIdentifier(), ResourceId.forResource(contextId, resource.getIdentifier()), "INDIVIDUAL");
    }

    @Test
    public void testCreateViaMailWithIncorrectCUType() throws Exception {
        testCreate(resource.getIdentifier(), "mailto:" + resource.getMail(), "INDIVIDUAL");
    }

    private void testCreate(int resourceId, String uri, String cuType) throws Exception {
        /*
         * create appointment on client
         */
        String uid = randomUID();
        Date start = TimeTools.D("next monday at 12:00");
        Date end = TimeTools.D("next monday at 13:00");
        String iCal = // @formatter:off
            "BEGIN:VCALENDAR" + "\r\n" +
            "VERSION:2.0" + "\r\n" +
            "BEGIN:VEVENT" + "\r\n" +
            "UID:" + uid + "\r\n" +
            "DTSTAMP:" + formatAsUTC(new Date()) + "\r\n" +
            "DTSTART:" + formatAsUTC(start) + "\r\n" +
            "DTEND:" + formatAsUTC(end) + "\r\n" +
            "SUMMARY:ResourceAttendeeTest"  + "\r\n" +
            "ORGANIZER:mailto:" + getClient().getValues().getDefaultAddress() + "\r\n" +
            "ATTENDEE;PARTSTAT=ACCEPTED:mailto:" + getClient().getValues().getDefaultAddress() + "\r\n" +
            "ATTENDEE;PARTSTAT=NEEDS-ACTION" + (null != cuType ? ";CUTYPE=" + cuType : "") + ":" + uri + "\r\n" +
            "END:VEVENT" + "\r\n" +
            "END:VCALENDAR" + "\r\n";
        ; // @formatter:on
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = getAppointment(uid);
        assertNotNull("Appointment not found on server", appointment);
        rememberForCleanUp(appointment);
        Participant resourceParticipant = null;
        for (Participant participant : appointment.getParticipants()) {
            if (participant.getIdentifier() == resourceId) {
                resourceParticipant = participant;
                break;
            }
        }
        assertNotNull("Resource participant not found", resourceParticipant);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());

        Property resoureceAttendee = iCalResource.getVEvent().getAttendee(uri);
        assertNotNull("Resource attendee not found", resourceParticipant);
        assertEquals("CUTYPE wrong", "RESOURCE", resoureceAttendee.getAttribute("CUTYPE"));
        assertEquals("PARTSTAT wrong", "ACCEPTED", resoureceAttendee.getAttribute("PARTSTAT"));
    }

}
