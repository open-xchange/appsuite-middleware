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
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.Abstract2UserCalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.dav.caldav.ical.SimpleICal.Component;
import com.openexchange.dav.caldav.ical.SimpleICal.Property;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug64937Test} - Participation Status of exceptions are reset to series value
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class Bug64937Test extends Abstract2UserCalDAVTest {

    private CalendarTestManager catm2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        catm2 = new CalendarTestManager(client2);
        catm2.setFailOnError(true);
    }

    @Test
    public void testOrganizersPartstat() throws Exception {
        /*
         * create appointment on server
         */
        String uid = randomUID();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeTools.D("next friday in the morning", TimeZone.getTimeZone("Europe/Berlin")));
        Appointment appointment = new Appointment();
        appointment.setUid(uid);
        appointment.setAlarm(15);
        appointment.setTitle("Bug64937Test");
        appointment.setIgnoreConflicts(true);
        appointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        appointment.setEndDate(calendar.getTime());
        appointment.addParticipant(new UserParticipant(catm2.getClient().getValues().getUserId()));
        appointment.addParticipant(new UserParticipant(getClient().getValues().getUserId()));
        appointment.setParentFolderID(catm.getPrivateFolder());
        appointment = catm.insert(appointment);
        /*
         * decline appointment on server
         */
        catm.confirm(catm.getPrivateFolder(), appointment.getObjectID(), catm.getLastModification(), Appointment.DECLINE, "doch nicht");
        /*
         * get appointment via caldav as user a
         */
        ICalResource iCalResource = get(appointment.getUid());
        assertNotNull("Event not found via CalDAV", iCalResource);
        Component alarmComponent = iCalResource.getVEvent().getVAlarm();
        assertNotNull("Event alarm not found", alarmComponent);
        Property attendeeProperty = iCalResource.getVEvent().getAttendee(getClient().getValues().getDefaultAddress());
        assertNotNull("Attendee property not found", attendeeProperty);
        /*
         * update event via caldav (acknowledge alarm), setting the PARTSTAT property back to ACCEPTED
         */
        alarmComponent.setProperty("ACKNOWLEDGED", formatAsUTC(new Date()));
        attendeeProperty.getAttributes().put("PARTSTAT", "ACCEPTED");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putICalUpdate(iCalResource));
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
