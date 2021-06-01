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
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug28490Test}
 *
 * Appointment on mobile shifted one hour
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug28490Test extends CalDAVTest {

    @Test
    public void testTimeZoneCET() throws Exception {
        /*
         * create appointment series on server
         */
        String uid = randomUID();
        Appointment appointment = new Appointment();
        appointment.setUid(uid);
        appointment.setTitle(getClass().getCanonicalName());
        appointment.setIgnoreConflicts(true);
        appointment.setStartDate(TimeTools.D("last january on friday at 16:00", TimeZone.getTimeZone("CET")));
        appointment.setEndDate(TimeTools.D("last january on friday at 16:30", TimeZone.getTimeZone("CET")));
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setDays(Appointment.FRIDAY);
        appointment.setInterval(1);
        appointment.setTimezone("CET");
        super.create(appointment);
        super.rememberForCleanUp(appointment);
        /*
         * verify appointment on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertNotNull("No TZID attribute found in DTSTART property", iCalResource.getVEvent().getProperty("DTSTART").getAttribute("TZID"));
        assertNotNull("No TZID attribute found in DTEND property", iCalResource.getVEvent().getProperty("DTEND").getAttribute("TZID"));
    }

}
