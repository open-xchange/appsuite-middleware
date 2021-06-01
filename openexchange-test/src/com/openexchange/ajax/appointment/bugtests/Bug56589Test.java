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

import static com.openexchange.ajax.folder.Create.ocl;
import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug56589Test}
 *
 * shared private calendar - decline appointment as a secretary (not an invitee) - deletes the appointment for everyone
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Bug56589Test extends AbstractAJAXSession {

    private CalendarTestManager catm3;
    private Appointment appointment;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        /*
         * as user 2, share default calendar to user 1
         */
        FolderObject folder = new FolderObject();
        folder.setObjectID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        folder.setLastModified(new Date(Long.MAX_VALUE));
        folder.setPermissionsAsArray(new OCLPermission[] {
            ocl(testUser2.getAjaxClient().getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
            ocl(getClient().getValues().getUserId(), false, false, OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_OWN_OBJECTS)
        });
        testUser2.getAjaxClient().execute(new com.openexchange.ajax.folder.actions.UpdateRequest(EnumAPI.OX_OLD, folder));
        /*
         * as user 3, prepare & insert appointment with user 2
         */
        catm3 = new CalendarTestManager(testContext.acquireUser().getAjaxClient());
        Appointment appointment = new Appointment();
        appointment.setIgnoreConflicts(true);
        appointment.setTitle("Bug56589Test");
        appointment.setStartDate(D("next friday at 11:30"));
        appointment.setEndDate(D("next friday at 12:30"));
        UserParticipant user1 = new UserParticipant(catm3.getClient().getValues().getUserId());
        UserParticipant user2 = new UserParticipant(testUser2.getAjaxClient().getValues().getUserId());
        appointment.setParticipants(new Participant[] { user1, user2 });
        appointment.setUsers(new UserParticipant[] { user1, user2 });
        appointment.setParentFolderID(catm3.getClient().getValues().getPrivateAppointmentFolder());
        appointment = catm3.insert(appointment);
        /*
         * as user 1, get the appointment in user 2's calendar
         */
        this.appointment = catm.get(folder.getObjectID(), appointment.getObjectID());
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(3).build();
    }

    @Test
    public void testDeclineThroughUpdateWithoutParticipantsAndConfirm() throws Exception {
        /*
         * as user 1, "decline" for user 2 via update, like USM does
         */
        Appointment appointmentUpdate = new Appointment();
        appointmentUpdate.setParentFolderID(appointment.getParentFolderID());
        appointmentUpdate.setObjectID(appointment.getObjectID());
        appointmentUpdate.setLastModified(appointment.getLastModified());
        appointmentUpdate.setIgnoreConflicts(true);
        appointmentUpdate.setModifiedBy(getClient().getValues().getUserId());
        UserParticipant user1 = new UserParticipant(catm3.getClient().getValues().getUserId());
        UserParticipant user2 = new UserParticipant(testUser2.getAjaxClient().getValues().getUserId());
        user2.setConfirmMessage("");
        user2.setDisplayName("");
        user2.setConfirm(Appointment.DECLINE);
        appointmentUpdate.setUsers(new UserParticipant[] { user1, user2 });
        appointmentUpdate.setFullTime(false);
        catm.update(appointmentUpdate);
        /*
         * as user 1, also "decline" for user 2 via confirm, like USM does
         */
        catm.confirm(appointmentUpdate, testUser2.getAjaxClient().getValues().getUserId(), Appointment.DECLINE, "");
        /*
         * as user 1, verify the appointment
         */
        verifyAfterDecline();
    }

    @Test
    public void testDeclineThroughUpdateAndConfirm() throws Exception {
        /*
         * as user 1, "decline" for user 2 via update, like USM does
         */
        Appointment appointmentUpdate = new Appointment();
        appointmentUpdate.setParentFolderID(appointment.getParentFolderID());
        appointmentUpdate.setObjectID(appointment.getObjectID());
        appointmentUpdate.setLastModified(appointment.getLastModified());
        appointmentUpdate.setIgnoreConflicts(true);
        appointmentUpdate.setModifiedBy(getClient().getValues().getUserId());
        UserParticipant user1 = new UserParticipant(catm3.getClient().getValues().getUserId());
        UserParticipant user2 = new UserParticipant(testUser2.getAjaxClient().getValues().getUserId());
        user2.setConfirmMessage("");
        user2.setDisplayName("");
        user2.setConfirm(Appointment.DECLINE);
        appointmentUpdate.setUsers(new UserParticipant[] { user1, user2 });
        appointmentUpdate.setParticipants(new Participant[] { user1, user2 });
        appointmentUpdate.setFullTime(false);
        catm.update(appointmentUpdate);
        /*
         * as user 1, also "decline" for user 2 via confirm, like USM does
         */
        catm.confirm(appointmentUpdate, testUser2.getAjaxClient().getValues().getUserId(), Appointment.DECLINE, "");
        /*
         * as user 1, verify the appointment
         */
        verifyAfterDecline();
    }

    @Test
    public void testDeclineThroughConfirm() throws Exception {
        /*
         * as user 1, "decline" for user 2 via confirm
         */
        catm.confirm(appointment, testUser2.getAjaxClient().getValues().getUserId(), Appointment.DECLINE, "");
        /*
         * as user 1, verify the appointment
         */
        verifyAfterDecline();
    }

    @Test
    public void testDeclineThroughUpdateWithoutParticipants() throws Exception {
        /*
         * as user 1, "decline" for user 2 via update, like USM does
         */
        Appointment appointmentUpdate = new Appointment();
        appointmentUpdate.setParentFolderID(appointment.getParentFolderID());
        appointmentUpdate.setObjectID(appointment.getObjectID());
        appointmentUpdate.setLastModified(appointment.getLastModified());
        appointmentUpdate.setIgnoreConflicts(true);
        appointmentUpdate.setModifiedBy(getClient().getValues().getUserId());
        UserParticipant user1 = new UserParticipant(catm3.getClient().getValues().getUserId());
        UserParticipant user2 = new UserParticipant(testUser2.getAjaxClient().getValues().getUserId());
        user2.setConfirmMessage("");
        user2.setDisplayName("");
        user2.setConfirm(Appointment.DECLINE);
        appointmentUpdate.setUsers(new UserParticipant[] { user1, user2 });
        appointmentUpdate.setFullTime(false);
        catm.update(appointmentUpdate);
        /*
         * as user 1, verify the appointment
         */
        verifyAfterDecline();
    }

    @Test
    public void testDeclineThroughUpdate() throws Exception {
        /*
         * as user 1, "decline" for user 2 via update, like USM does
         */
        Appointment appointmentUpdate = new Appointment();
        appointmentUpdate.setParentFolderID(appointment.getParentFolderID());
        appointmentUpdate.setObjectID(appointment.getObjectID());
        appointmentUpdate.setLastModified(appointment.getLastModified());
        appointmentUpdate.setIgnoreConflicts(true);
        appointmentUpdate.setModifiedBy(getClient().getValues().getUserId());
        UserParticipant user1 = new UserParticipant(catm3.getClient().getValues().getUserId());
        UserParticipant user2 = new UserParticipant(testUser2.getAjaxClient().getValues().getUserId());
        user2.setConfirmMessage("");
        user2.setDisplayName("");
        user2.setConfirm(Appointment.DECLINE);
        appointmentUpdate.setUsers(new UserParticipant[] { user1, user2 });
        appointmentUpdate.setParticipants(new Participant[] { user1, user2 });
        appointmentUpdate.setFullTime(false);
        catm.update(appointmentUpdate);
        /*
         * as user 1, verify the appointment
         */
        verifyAfterDecline();
    }

    private void verifyAfterDecline() throws Exception {
        Appointment updatedAppointment = catm.get(appointment.getParentFolderID(), appointment.getObjectID());
        assertNotNull("no users in appointment", updatedAppointment.getUsers());
        assertNotNull("no participants in appointment", updatedAppointment.getParticipants());
        assertEquals("unexpected number of users in appointment", 2, updatedAppointment.getUsers().length);
        assertEquals("unexpected number of participants in appointment", 2, updatedAppointment.getParticipants().length);
        for (UserParticipant user : updatedAppointment.getUsers()) {
            if (catm3.getClient().getValues().getUserId() == user.getIdentifier()) {
                assertEquals("unexpected confirmation status", Appointment.ACCEPT, user.getConfirm());
            } else if (testUser2.getAjaxClient().getValues().getUserId() == user.getIdentifier()) {
                assertEquals("unexpected confirmation status", Appointment.DECLINE, user.getConfirm());
            } else {
                fail("unexpected user in appointment: " + user.getIdentifier());
            }
        }
    }

}
