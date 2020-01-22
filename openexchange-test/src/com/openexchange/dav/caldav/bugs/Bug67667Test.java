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
import java.util.Calendar;
import java.util.TimeZone;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug67667Test}
 * 
 * Appointments tend to lose confirmation status
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class Bug67667Test extends CalDAVTest {

    private CalendarTestManager catm2;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        catm2 = new CalendarTestManager(getClient2());
        catm2.setFailOnError(true);
    }

    @Override
    public void tearDown() throws Exception {
        try {
            if (null != catm2) {
                catm2.cleanUp();
            }
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testPreservePartstat() throws Exception {
        /*
         * as user b, create appointment on server & invite user a
         */
        String uid = randomUID();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("next monday in the morning", TimeZone.getTimeZone("Europe/Berlin")));
        Appointment appointment = new Appointment();
        appointment.setUid(uid);
        appointment.setTitle("Bug67667Test");
        appointment.setIgnoreConflicts(true);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.addParticipant(new UserParticipant(catm2.getClient().getValues().getUserId()));
        appointment.addParticipant(new UserParticipant(getClient().getValues().getUserId()));
        appointment.setParentFolderID(catm2.getPrivateFolder());
        appointment = catm2.insert(appointment);
        /*
         * as user a, get & "accept" the appointment using the web client
         */
        appointment = catm.get(catm.getPrivateFolder(), appointment.getObjectID());
        assertNotNull(appointment);
        catm.confirm(appointment, Appointment.ACCEPT, "ja");
        /*
         * as user a, synchronize & get the event resource
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        Property attendeeProperty = iCalResource.getVEvent().getAttendee(catm.getClient().getValues().getDefaultAddress());
        assertNotNull("No matching attendee in iCal found", attendeeProperty);
        assertEquals("PARTSTAT wrong", "ACCEPTED", attendeeProperty.getAttribute("PARTSTAT"));
        /*
         * as user a, get & "decline" the appointment using the web client
         */
        appointment = catm.get(catm.getPrivateFolder(), appointment.getObjectID());
        assertNotNull(appointment);
        catm.confirm(appointment, Appointment.DECLINE, "doch nicht");
        /*
         * as user a, update the event resource by adding a reminder, w/o updating the client copy
         */
        Component alarmComponent = new Component("VALARM");
        alarmComponent.getProperties().add(new Property("UID:" + randomUID()));
        alarmComponent.getProperties().add(new Property("TRIGGER:-PT5M"));
        alarmComponent.getProperties().add(new Property("DESCRIPTION:Ereignisbenachrichtigung"));
        alarmComponent.getProperties().add(new Property("ACTION:DISPLAY"));
        iCalResource.getVEvent().getComponents().add(alarmComponent);
        assertEquals("response code wrong", HttpServletResponse.SC_PRECONDITION_FAILED, putICalUpdate(getDefaultFolderID(), uid, iCalResource.toString(), null, iCalResource.getScheduleTag()));
        /*
         * verify that attendees partstat was not changed on server
         */
        appointment = catm.get(appointment);
        for (UserParticipant participant : appointment.getUsers()) {
            if (catm.getClient().getValues().getUserId() == participant.getIdentifier()) {
                assertEquals("Wrong participation status", Appointment.DECLINE, participant.getConfirm());
            }
        }
    }

}
