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
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.caldav.ICalResource;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.common.groupware.calendar.TimeTools;

/**
 * {@link Bug28734Test}
 *
 * ical calendar shows WebUI created appointments in incorrect time zone (+1 hour)
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug28734Test extends CalDAVTest {

    @Test
    public void testTimeZoneHongKong() throws Exception {
        /*
         * create appointment in timezone on server
         */
        String uid = randomUID();
        Appointment appointment = new Appointment();
        appointment.setUid(uid);
        appointment.setTitle(getClass().getCanonicalName());
        appointment.setIgnoreConflicts(true);
        appointment.setStartDate(TimeTools.D("september on friday at 20:00", TimeZone.getTimeZone("Asia/Hong_Kong")));
        appointment.setEndDate(TimeTools.D("september on friday at 21:00", TimeZone.getTimeZone("Asia/Hong_Kong")));
        appointment.setTimezone("Asia/Hong_Kong");
        super.create(appointment);
        super.rememberForCleanUp(appointment);
        /*
         * verify appointment timezone on client
         */
        ICalResource iCalResource = get(uid);
        assertNotNull("No VEVENT in iCal found", iCalResource.getVEvent());
        assertEquals("UID wrong", uid, iCalResource.getVEvent().getUID());
        assertTrue("DTSTART wrong", iCalResource.getVEvent().getPropertyValue("DTSTART").endsWith("T200000"));
        assertTrue("DTEND wrong", iCalResource.getVEvent().getPropertyValue("DTEND").endsWith("T210000"));
    }

}
