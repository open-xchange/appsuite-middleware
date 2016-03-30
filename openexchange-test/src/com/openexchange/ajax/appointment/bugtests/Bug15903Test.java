/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.appointment.bugtests;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.appointment.CalendarTestManagerTest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug15903Test} This tests if a participant is correctly missing, as both participant and user, from the result of the next get when
 * removing him as participant. Also a get for the specific appointment is done twice as the second user. Once when he is in, and therefore
 * may see it. Once when he is out and an error is expected when trying to read it.
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class Bug15903Test extends CalendarTestManagerTest {

    public Bug15903Test(String name) {
        super(name);
    }

    // Tests Bug 15903
    public void testUpdatedParticipants() throws Exception {

        // create appointment with 2 participants
        Appointment appointment = generateAppointment();
        appointment.setIgnoreConflicts(true);
        AJAXClient client2 = new AJAXClient(User.User2);
        int firstUserId = getClient().getValues().getUserId();
        int secondUserId = client2.getValues().getUserId();
        appointment.addParticipant(new UserParticipant(secondUserId));
        appointment = calendarMgr.insert(appointment);

        boolean firstUserParticipantInInitialAppointment = false;
        boolean secondUserParticipantInInitialAppointment = false;
        boolean firstUserUserInInitialAppointment = false;
        boolean secondUserUserInInitialAppointment = false;

        boolean firstUserParticipantInUpdatedAppointment = false;
        boolean secondUserParticipantInUpdatedAppointment = false;
        boolean firstUserUserInUpdatedAppointment = false;
        boolean secondUserUserInUpdatedAppointment = false;

        // Verify that both users are in the initial appointment
        Appointment reload = calendarMgr.get(appointment);
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
        for (ConfirmableParticipant participant : reload.getConfirmations()) {
        }

        assertTrue("First User should be Participant in the initial appointment", firstUserParticipantInInitialAppointment);
        assertTrue("Second should be Participant in the initial appointment", secondUserParticipantInInitialAppointment);
        assertTrue("First User should be User in the initial appointment", firstUserUserInInitialAppointment);
        assertTrue("Second should be User in the initial appointment", secondUserUserInInitialAppointment);

        // Verify the that the second user sees the appointment initally
        CalendarTestManager calendarMgr2 = new CalendarTestManager(client2);
        Appointment appForSeconduser = calendarMgr2.get(client2.getValues().getPrivateAppointmentFolder(), appointment.getObjectID());
        assertEquals("second user should see the appointment initially", appointment.getTitle(), appForSeconduser.getTitle());

        // remove the second user from the appointment
        List<Participant> participants = new ArrayList<Participant>();
        UserParticipant p = new UserParticipant(getClient().getValues().getUserId());
        participants.add(p);
        List<UserParticipant> userParticipants = new ArrayList<UserParticipant>();
        UserParticipant uP = new UserParticipant(firstUserId);
        userParticipants.add(uP);
        reload.setParticipants(participants);
        //reload.setUsers(userParticipants);
        reload.setIgnoreConflicts(true);
        calendarMgr.update(reload);
        Appointment reloadAgain = calendarMgr.get(appointment);

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
        for (ConfirmableParticipant participant : reloadAgain.getConfirmations()) {
        }

        assertTrue("First User should still be Participant in the updated appointment", firstUserParticipantInUpdatedAppointment);
        assertFalse("Second should not be Participant in the updated appointment", secondUserParticipantInUpdatedAppointment);
        assertTrue("First User should still be User in the updated appointment", firstUserUserInUpdatedAppointment);
        assertFalse("Second should not be User in the updated appointment", secondUserUserInUpdatedAppointment);

        // Verify the that the second user does no longer see the appointment
        boolean gotException = false;
        try {
            appForSeconduser = calendarMgr2.get(client2.getValues().getPrivateAppointmentFolder(), appointment.getObjectID());
        } catch (OXException e) {
            gotException = true;
        }

        assertTrue("second user should not see the appointment any longer", gotException);

        client2.logout();

    }
}
