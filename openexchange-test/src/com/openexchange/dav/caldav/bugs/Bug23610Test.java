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
import java.util.Map.Entry;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug23610Test}
 *
 * "Shown as" status changed when confirming/declining appointment in Apple iCal client as participant
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug23610Test extends CalDAVTest {

    @Test
    public void testConfirmAppointment() throws Exception {
        for (int shownAs : new int[] { Appointment.FREE, Appointment.TEMPORARY, Appointment.RESERVED, Appointment.ABSENT }) {
            for (int confirmation : new int[] { Appointment.ACCEPT, Appointment.DECLINE, Appointment.TENTATIVE }) {
                this.confirmAppointment(shownAs, confirmation);
            }
        }
    }

    private void confirmAppointment(int appointmentShownAs, int confirmationStatus) throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken());
        /*
         * create appointment on server
         */
        String uid = randomUID();
        String summary = "Bug23610Test-" + appointmentShownAs + "-" + confirmationStatus;
        String location = "test";
        Date start = TimeTools.D("next saturday at 10:00");
        Date end = TimeTools.D("next saturday at 11:00");
        Appointment appointment = generateAppointment(start, end, uid, summary, location);
        appointment.setOrganizer("otto@example.com");
        appointment.addParticipant(new UserParticipant(super.getAJAXClient().getValues().getUserId()));
        ExternalUserParticipant participant = new ExternalUserParticipant("otto@example.com");
        participant.setConfirm(Appointment.ACCEPT);
        appointment.addParticipant(participant);
        appointment.setShownAs(appointmentShownAs);
        super.rememberForCleanUp(super.create(appointment));
        /*
         * verify appointment on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = super.calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        assertEquals("LOCATION wrong", location, iCalResource.getVEvent().getLocation());
        if (null != iCalResource.getVEvent().getTransp()) {
            assertEquals("TRANSP wrong", Appointment.FREE == appointmentShownAs ? "TRANSPARENT" : "OPAQUE", iCalResource.getVEvent().getTransp());
        }
        /*
         * confirm appointment on client
         */
        String partstat = Appointment.TENTATIVE == confirmationStatus ? "TENTATIVE" : Appointment.DECLINE == confirmationStatus ? "DECLINED" : "ACCEPTED";
        List<Property> attendees = iCalResource.getVEvent().getProperties("ATTENDEE");
        for (Property property : attendees) {
            if (property.getValue().contains(super.getAJAXClient().getValues().getDefaultAddress())) {
                for (Entry<String, String> attribute : property.getAttributes().entrySet()) {
                    if (attribute.getKey().equals("PARTSTAT") && false == partstat.equals(attribute.getValue())) {
                        attribute.setValue(partstat);
                        iCalResource.getVEvent().setTransp(Appointment.DECLINE == confirmationStatus ? "TRANSPARENT" : "OPAQUE");
                    }
                }
                break;
            }
        }
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICalUpdate(iCalResource));
        /*
         * verify appointment on server
         */
        appointment = super.getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        UserParticipant[] users = appointment.getUsers();
        assertNotNull("appointment has no users", users);
        UserParticipant partipant = null;
        for (UserParticipant user : users) {
            if (getAJAXClient().getValues().getUserId() == user.getIdentifier()) {
                partipant = user;
                break;
            }
        }
        assertNotNull("confirming participant not found", partipant);
        assertEquals("confirmation status wrong", confirmationStatus, partipant.getConfirm());
        assertEquals("shown as wrong", appointmentShownAs, appointment.getShownAs());
        /*
         * verify appointment on client
         */
        iCalResource = super.get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        Property attendee = null;
        attendees = iCalResource.getVEvent().getProperties("ATTENDEE");
        for (Property property : attendees) {
            if (property.getValue().contains(super.getAJAXClient().getValues().getDefaultAddress())) {
                attendee = property;
                break;
            }
        }
        assertNotNull("confirming attendee not found", attendee);
        assertEquals("partstat status wrong", partstat, attendee.getAttribute("PARTSTAT"));
        if (null != iCalResource.getVEvent().getTransp()) {
            assertEquals("TRANSP wrong", Appointment.FREE == appointmentShownAs ? "TRANSPARENT" : "OPAQUE", iCalResource.getVEvent().getTransp());
        }
    }

}
