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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.appointment.CalendarTestManagerTest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug15903Test} This tests if a participant is correctly missing, as both participant and user, from the result of the next get when
 * removing him as participant. Also a get for the specific appointment is done twice as the second user. Once when he is in, and therefore
 * may see it. Once when he is out and an error is expected when trying to read it.
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class Bug15903Test extends CalendarTestManagerTest {

    public Bug15903Test() {
        super();
    }

    // Tests Bug 15903    @Test
    @Test
    public void testUpdatedParticipants() throws Exception {

        // create appointment with 2 participants
        Appointment appointment = generateAppointment();
        appointment.setIgnoreConflicts(true);
        AJAXClient client2 = testUser2.getAjaxClient();
        int firstUserId = getClient().getValues().getUserId();
        int secondUserId = client2.getValues().getUserId();
        appointment.addParticipant(new UserParticipant(secondUserId));
        appointment = catm.insert(appointment);

        boolean firstUserParticipantInInitialAppointment = false;
        boolean secondUserParticipantInInitialAppointment = false;
        boolean firstUserUserInInitialAppointment = false;
        boolean secondUserUserInInitialAppointment = false;

        boolean firstUserParticipantInUpdatedAppointment = false;
        boolean secondUserParticipantInUpdatedAppointment = false;
        boolean firstUserUserInUpdatedAppointment = false;
        boolean secondUserUserInUpdatedAppointment = false;

        // Verify that both users are in the initial appointment
        Appointment reload = catm.get(appointment);
        for (Participant participant : reload.getParticipants()) {
            if (participant.getIdentifier() == firstUserId) {
                firstUserParticipantInInitialAppointment = true;
            }
            if (participant.getIdentifier() == secondUserId) {
                secondUserParticipantInInitialAppointment = true;
            }
        }
        for (UserParticipant participant : reload.getUsers()) {
            if (participant.getIdentifier() == firstUserId) {
                firstUserUserInInitialAppointment = true;
            }
            if (participant.getIdentifier() == secondUserId) {
                secondUserUserInInitialAppointment = true;
            }
        }

        assertTrue("First User should be Participant in the initial appointment", firstUserParticipantInInitialAppointment);
        assertTrue("Second should be Participant in the initial appointment", secondUserParticipantInInitialAppointment);
        assertTrue("First User should be User in the initial appointment", firstUserUserInInitialAppointment);
        assertTrue("Second should be User in the initial appointment", secondUserUserInInitialAppointment);

        // Verify the that the second user sees the appointment initally
        CalendarTestManager catm2 = new CalendarTestManager(client2);
        Appointment appForSeconduser = catm2.get(client2.getValues().getPrivateAppointmentFolder(), appointment.getObjectID());
        assertEquals("second user should see the appointment initially", appointment.getTitle(), appForSeconduser.getTitle());

        // remove the second user from the appointment
        List<Participant> participants = new ArrayList<Participant>();
        UserParticipant p = new UserParticipant(getClient().getValues().getUserId());
        participants.add(p);
        List<UserParticipant> userParticipants = new ArrayList<UserParticipant>();
        UserParticipant uP = new UserParticipant(firstUserId);
        userParticipants.add(uP);
        reload.setParticipants(participants);
        reload.setUsers(userParticipants);
        reload.setIgnoreConflicts(true);
        catm.update(reload);
        Appointment reloadAgain = catm.get(appointment);

        // verify that the second user is removed
        for (Participant participant : reloadAgain.getParticipants()) {
            if (participant.getIdentifier() == firstUserId) {
                firstUserParticipantInUpdatedAppointment = true;
            }
            if (participant.getIdentifier() == secondUserId) {
                secondUserParticipantInUpdatedAppointment = true;
            }
        }
        for (UserParticipant participant : reloadAgain.getUsers()) {
            if (participant.getIdentifier() == firstUserId) {
                firstUserUserInUpdatedAppointment = true;
            }
            if (participant.getIdentifier() == secondUserId) {
                secondUserUserInUpdatedAppointment = true;
            }
        }

        assertTrue("First User should still be Participant in the updated appointment", firstUserParticipantInUpdatedAppointment);
        assertFalse("Second should not be Participant in the updated appointment", secondUserParticipantInUpdatedAppointment);
        assertTrue("First User should still be User in the updated appointment", firstUserUserInUpdatedAppointment);
        assertFalse("Second should not be User in the updated appointment", secondUserUserInUpdatedAppointment);

        // Verify the that the second user does no longer see the appointment
        boolean gotException = false;
        try {
            appForSeconduser = catm2.get(client2.getValues().getPrivateAppointmentFolder(), appointment.getObjectID());
        } catch (@SuppressWarnings("unused") OXException e) {
            gotException = true;
        }
        assertTrue(catm2.getLastResponse().hasError());
        assertTrue("second user should not see the appointment any longer", gotException || appForSeconduser == null);

        client2.logout();

    }
}
