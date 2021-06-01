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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug42018Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.1
 */
public class Bug42018Test extends AbstractAJAXSession {

    private CalendarTestManager ctm;
    private Appointment appointment;

    boolean works = false;

    public Bug42018Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ctm = new CalendarTestManager(getClient());
        ctm.setFailOnError(true);
        appointment = new Appointment();
        appointment.setTitle("Bug 42018 Test");
        if (works) {
            appointment.setStartDate(D("03.08.2015 08:00"));
            appointment.setEndDate(D("03.08.2015 09:00"));
        } else {
            appointment.setStartDate(D("07.09.2015 08:00"));
            appointment.setEndDate(D("07.09.2015 09:00"));
        }
        appointment.setFullTime(true);
        appointment.setRecurrenceType(Appointment.WEEKLY);
        appointment.setDays(Appointment.MONDAY);
        appointment.setInterval(1);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
    }

    @Test
    public void testBug42018() throws Exception {
        ctm.insert(appointment);
        ctm.createDeleteException(appointment, 3);
        Appointment loadedAppointment = ctm.get(appointment);
        assertNotNull("Expect delete exception.", loadedAppointment.getDeleteException());
        assertEquals("Expect exactly one delete exception", 1, loadedAppointment.getDeleteException().length);
    }

}
