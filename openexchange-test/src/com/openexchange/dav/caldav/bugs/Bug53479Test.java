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

import static com.openexchange.java.Autoboxing.i;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.caldav.Abstract2UserCalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug53479Test}
 *
 * accepting an appointment using a mobile (android) phone deletes the assigned resource
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class Bug53479Test extends Abstract2UserCalDAVTest {

    private CalendarTestManager manager2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        manager2 = new CalendarTestManager(client2);
        manager2.setFailOnError(true);
    }

    @Test
    public void testAcceptWithResource() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(fetchSyncToken());
        /*
         * create appointment on server as user b with resource participant and user a
         */
        int resId = i(testContext.acquireResource());
        String uid = randomUID();
        Appointment appointment = new Appointment();
        appointment.setParentFolderID(manager2.getPrivateFolder());
        appointment.setUid(uid);
        appointment.setTitle("Bug53479Test");
        appointment.setIgnoreConflicts(true);
        appointment.setStartDate(TimeTools.D("next week at 4 pm", TimeZone.getTimeZone("Europe/Berlin")));
        appointment.setEndDate(TimeTools.D("next week at 5 pm", TimeZone.getTimeZone("Europe/Berlin")));
        appointment.addParticipant(new UserParticipant(manager2.getClient().getValues().getUserId()));
        appointment.addParticipant(new UserParticipant(getClient().getValues().getUserId()));
        appointment.addParticipant(new ResourceParticipant(resId));
        appointment = manager2.insert(appointment);
        /*
         * verify appointment on client as user a
         */
        Map<String, String> eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<ICalResource> calendarData = calendarMultiget(eTags.keySet());
        ICalResource iCalResource = assertContains(uid, calendarData);
        assertNotNull(iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress()));
        assertNotNull(iCalResource.getVEvent().getAttendee(manager2.getClient().getValues().getDefaultAddress()));
        Property resourceAttendee = null;
        for (Property property : iCalResource.getVEvent().getProperties("ATTENDEE")) {
            if ("RESOURCE".equals(property.getAttribute("CUTYPE"))) {
                resourceAttendee = property;
                break;
            }
        }
        assertNotNull("No resource attendee found in iCal", resourceAttendee);
        /*
         * accept event as user a
         */
        Property userAttendee = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        userAttendee.getAttributes().put("PARTSTAT", "ACCEPTED");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
        /*
         * verify appointment on server as user b
         */
        appointment = manager2.get(appointment);
        Participant resourceParticipant = null;
        for (Participant participant : appointment.getParticipants()) {
            if (Participant.RESOURCE == participant.getType() && participant.getIdentifier() == resId) {
                resourceParticipant = participant;
                break;
            }
        }
        assertNotNull("Resource participant no longer found in appointment", resourceParticipant);
        /*
         * verify appointment on client as user a
         */
        eTags = syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        calendarData = calendarMultiget(eTags.keySet());
        iCalResource = assertContains(uid, calendarData);
        resourceAttendee = null;
        for (Property property : iCalResource.getVEvent().getProperties("ATTENDEE")) {
            if ("RESOURCE".equals(property.getAttribute("CUTYPE"))) {
                resourceAttendee = property;
                break;
            }
        }
        assertNotNull("Resource attendee no longer found in iCal", resourceAttendee);
    }

}
