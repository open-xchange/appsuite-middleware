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
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;

/**
 * {@link Bug29146Test}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug29146Test extends AbstractAJAXSession {

    private Appointment appointment;

    public Bug29146Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        appointment = new Appointment();
        appointment.setStartDate(D("18.11.2013 08:00"));
        appointment.setEndDate(D("18.11.2013 09:00"));
        appointment.setTitle("Test Bug 29146");
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        UserParticipant user = new UserParticipant(getClient().getValues().getUserId());
        user.setConfirm(Appointment.NONE);
        appointment.setParticipants(new Participant[] { user });
        appointment.setUsers(new UserParticipant[] { user });
    }

    @Test
    public void testInitialParticipantStatus() throws Exception {
        catm.insert(appointment);

        Appointment loadedAppointment = catm.get(appointment);
        UserParticipant participant = loadedAppointment.getUsers()[0];
        assertEquals("Wrong confirm status.", Appointment.NONE, participant.getConfirm());
    }
}
