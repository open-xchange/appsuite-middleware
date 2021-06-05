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
import static org.junit.Assert.assertFalse;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug32278Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug32278Test extends AbstractAJAXSession {

    private Appointment appointment;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        catm = new CalendarTestManager(getClient());
        appointment = new Appointment();
        appointment.setTitle("Bug 32278 Test");
        appointment.setStartDate(D("01.05.2014 08:00"));
        appointment.setEndDate(D("01.05.2014 09:00"));
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(3);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
    }

    @Test
    public void testBug() {
        catm.insert(appointment);
        appointment.setRecurrenceType(Appointment.NO_RECURRENCE);
        appointment.removeInterval();
        appointment.removeOccurrence();

        catm.update(appointment);

        List<Appointment> list = catm.list(new ListIDs(appointment.getParentFolderID(), appointment.getObjectID()), new int[] { Appointment.RECURRENCE_ID, Appointment.RECURRENCE_POSITION });
        for (Appointment app : list) {
            assertFalse("No recurrence ID expected.", app.containsRecurrenceID());
            assertFalse("No recurrence position expected.", app.containsRecurrencePosition());
        }
    }
}
