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

import static com.openexchange.ajax.folder.Create.ocl;
import static com.openexchange.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.CalendarTestManager;

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
        folder.setObjectID(getClient2().getValues().getPrivateAppointmentFolder());
        folder.setLastModified(new Date(Long.MAX_VALUE));
        folder.setPermissionsAsArray(new OCLPermission[] {
            ocl(getClient2().getValues().getUserId(), false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION),
            ocl(getClient().getValues().getUserId(), false, false, OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_OWN_OBJECTS)
        });
        getClient2().execute(new com.openexchange.ajax.folder.actions.UpdateRequest(EnumAPI.OX_OLD, folder));
        /*
         * as user 3, prepare & insert appointment with user 2
         */
        catm3 = new CalendarTestManager(new AJAXClient(testContext.acquireUser()));
        Appointment appointment = new Appointment();
        appointment.setIgnoreConflicts(true);
        appointment.setTitle("Bug56589Test");
        appointment.setStartDate(D("next friday at 11:30"));
        appointment.setEndDate(D("next friday at 12:30"));
        UserParticipant user1 = new UserParticipant(catm3.getClient().getValues().getUserId());
        UserParticipant user2 = new UserParticipant(getClient2().getValues().getUserId());
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
    @After
    public void tearDown() throws Exception {
        if (null != catm3) {
            try {
                catm3.cleanUp();
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(Bug56589Test.class).error("", e);
            }
            try {
                logoutClient(catm3.getClient(), true);
                catm3.cleanUp();
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(Bug56589Test.class).error("", e);
            }
        }
        try {
            new CalendarTestManager(getClient2()).resetDefaultFolderPermissions();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(Bug56589Test.class).error("", e);
        }
        super.tearDown();
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
        UserParticipant user2 = new UserParticipant(getClient2().getValues().getUserId());
        user2.setConfirmMessage("");
        user2.setDisplayName("");
        user2.setConfirm(Appointment.DECLINE);
        appointmentUpdate.setUsers(new UserParticipant[] { user1, user2 });
        appointmentUpdate.setFullTime(false);
        catm.update(appointmentUpdate);
        /*
         * as user 1, also "decline" for user 2 via confirm, like USM does
         */
        catm.confirm(appointmentUpdate, getClient2().getValues().getUserId(), Appointment.DECLINE, "");
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
        UserParticipant user2 = new UserParticipant(getClient2().getValues().getUserId());
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
        catm.confirm(appointmentUpdate, getClient2().getValues().getUserId(), Appointment.DECLINE, "");
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
        catm.confirm(appointment, getClient2().getValues().getUserId(), Appointment.DECLINE, "");
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
        UserParticipant user2 = new UserParticipant(getClient2().getValues().getUserId());
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
        UserParticipant user2 = new UserParticipant(getClient2().getValues().getUserId());
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
            } else if (getClient2().getValues().getUserId() == user.getIdentifier()) {
                assertEquals("unexpected confirmation status", Appointment.DECLINE, user.getConfirm());
            } else {
                fail("unexpected user in appointment: " + user.getIdentifier());
            }
        }
    }

}
