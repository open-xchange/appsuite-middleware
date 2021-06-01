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
 * {@link Bug23612Test}
 *
 * "Shown as" status of "absent" or "temporary" lost after updating appointment in iCal client
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug23612Test extends CalDAVTest {

    @Test
    public void testUpdateAppointment() throws Exception {
        for (int shownAs : new int[] { Appointment.FREE, Appointment.TEMPORARY, Appointment.RESERVED, Appointment.ABSENT }) {
            this.updateAppointment(shownAs);
        }
    }

    private void updateAppointment(int appointmentShownAs) throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken());
        /*
         * create appointment on server
         */
        String uid = randomUID();
        String summary = "Bug23612Test-" + appointmentShownAs;
        String location = "ja";
        Date start = TimeTools.D("next monday at 5:00");
        Date end = TimeTools.D("next monday at 5:55");
        Appointment appointment = generateAppointment(start, end, uid, summary, location);
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
         * update appointment on client
         */
        iCalResource.getVEvent().setSummary(appointment.getTitle() + "_edit");
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICalUpdate(iCalResource));
        /*
         * verify appointment on server
         */
        appointment = super.getAppointment(uid);
        assertNotNull("appointment not found on server", appointment);
        assertEquals("shown as wrong", appointmentShownAs, appointment.getShownAs());
    }

}
