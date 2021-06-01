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

package com.openexchange.ajax.appointment.bugtests;

import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;

/**
 * {@link Bug32465Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug32465Test extends AbstractAJAXSession {

    private Appointment appointment;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        appointment = new Appointment();
        appointment.setTitle("Bug 32465 Test");
        appointment.setStartDate(D("01.06.2014 00:00", TimeZone.getTimeZone("UTC")));
        appointment.setEndDate(D("02.06.2014 00:00", TimeZone.getTimeZone("UTC")));
        appointment.setFullTime(true);
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(12);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        catm.insert(appointment);
    }

    @Test
    public void testBug32465() throws Exception {
        appointment.setOccurrence(13);
        catm.update(appointment);

        Appointment loadedAppointment = catm.get(appointment);
        System.out.println(loadedAppointment.getStartDate());
        System.out.println(loadedAppointment.getEndDate());
    }
}
