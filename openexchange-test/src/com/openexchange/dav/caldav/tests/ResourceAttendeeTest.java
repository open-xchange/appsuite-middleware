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

package com.openexchange.dav.caldav.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Date;
import org.junit.Test;
import com.openexchange.chronos.ResourceId;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.resource.Resource;

/**
 * {@link ResourceAttendeeTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.3
 */
public class ResourceAttendeeTest extends CalDAVTest {

    @Test
    public void testCreateViaResourceIdWithCorrectCUType() throws Exception {
        Resource resource = resTm.search(testContext.getResourceParticipants().get(0)).get(0);
        int contextId = getClient().getValues().getContextId();
        testCreate(resource.getIdentifier(), ResourceId.forResource(contextId, resource.getIdentifier()), "RESOURCE");
    }

    @Test
    public void testCreateViaMailWithCorrectCUType() throws Exception {
        Resource resource = resTm.search(testContext.getResourceParticipants().get(0)).get(0);
        testCreate(resource.getIdentifier(), "mailto:" + resource.getMail(), "RESOURCE");
    }

    @Test
    public void testCreateViaResourceIdWithoutCUType() throws Exception {
        Resource resource = resTm.search(testContext.getResourceParticipants().get(0)).get(0);
        int contextId = getClient().getValues().getContextId();
        testCreate(resource.getIdentifier(), ResourceId.forResource(contextId, resource.getIdentifier()), null);
    }

    @Test
    public void testCreateViaMailWithoutCUType() throws Exception {
        Resource resource = resTm.search(testContext.getResourceParticipants().get(0)).get(0);
        testCreate(resource.getIdentifier(), "mailto:" + resource.getMail(), null);
    }

    @Test
    public void testCreateViaResourceIdWithIncorrectCUType() throws Exception {
        Resource resource = resTm.search(testContext.getResourceParticipants().get(0)).get(0);
        int contextId = getClient().getValues().getContextId();
        testCreate(resource.getIdentifier(), ResourceId.forResource(contextId, resource.getIdentifier()), "INDIVIDUAL");
    }

    @Test
    public void testCreateViaMailWithIncorrectCUType() throws Exception {
        Resource resource = resTm.search(testContext.getResourceParticipants().get(0)).get(0);
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
