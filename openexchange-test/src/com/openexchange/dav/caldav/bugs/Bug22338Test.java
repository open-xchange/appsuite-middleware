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
import org.junit.Test;
import com.openexchange.contact.internal.Tools;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug22338Test} - moving an appointment in iCal will not move the appointment accordingly in the OX GUI
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug22338Test extends CalDAVTest {

    @Test
    public void testLastModified() throws Exception {
        /*
         * create appointment on client
         */
        String uid = randomUID();
        String summary = "bug 22338";
        String location = "test";
        Date start = TimeTools.D("tomorrow at 2pm");
        Date end = TimeTools.D("tomorrow at 8pm");
        String iCal = generateICal(start, end, uid, summary, location);
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICal(uid, iCal));
        /*
         * verify appointment on server
         */
        Appointment appointment = super.getAppointment(uid);
        super.rememberForCleanUp(appointment);
        assertAppointmentEquals(appointment, start, end, uid, summary, location);
        Date clientLastModified = super.getManager().getLastModification();
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertEquals("SUMMARY wrong", summary, iCalResource.getVEvent().getSummary());
        assertEquals("LOCATION wrong", location, iCalResource.getVEvent().getLocation());
        assertEquals("DTSTART wrong", start, iCalResource.getVEvent().getDTStart());
        assertEquals("DTEND wrong", end, iCalResource.getVEvent().getDTEnd());
        /*
         * change the start- and endtime on the client
         */
        Date updatedStart = TimeTools.D("tomorrow at 4pm");
        Date updatedEnd = TimeTools.D("tomorrow at 10pm");
        iCalResource.getVEvent().setDTStart(updatedStart);
        iCalResource.getVEvent().setDTEnd(updatedEnd);
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putICalUpdate(iCalResource));
        /*
         * verify updated appointments on server
         */
        List<Appointment> updates = super.getManager().updates(Tools.parse(getDefaultFolderID()), clientLastModified, true);
        assertNotNull("appointment not found on server", updates);
        assertTrue("no updated appointments on server", 0 < updates.size());
        Appointment updatedAppointment = null;
        for (Appointment update : updates) {
            if (uid.equals(update.getUid())) {
                updatedAppointment = update;
                break;
            }
        }
        assertNotNull("appointment not listed in updates", updatedAppointment);
        assertAppointmentEquals(updatedAppointment, updatedStart, updatedEnd, uid, summary, location);
        assertTrue("last modified not changed", clientLastModified.before(super.getManager().getLastModification()));
    }

}
