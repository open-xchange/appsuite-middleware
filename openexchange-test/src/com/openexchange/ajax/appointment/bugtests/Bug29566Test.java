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
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link Bug29566Test}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug29566Test extends AbstractAJAXSession {

    private CalendarTestManager catm2;
    private Appointment appointment;

    public Bug29566Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        catm.resetDefaultFolderPermissions();
        catm2 = new CalendarTestManager(testUser2.getAjaxClient());
        catm2.resetDefaultFolderPermissions();

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

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testAddParticipantWithExternalOrganizerAndUid() throws Exception {
        addParticipantWithExternalOrganizerAndUid(false);
    }

    @Test
    public void testAddParticipantWithExternalOrganizerAndUidShared() throws Exception {
        addParticipantWithExternalOrganizerAndUid(true);
    }

    private void addParticipantWithExternalOrganizerAndUid(boolean shared) throws Exception {
        String uid = generateUid();
        appointment.setUid(uid);
        String organizer = "test@extern.example.invalid";
        appointment.setOrganizer(organizer);
        catm.insert(appointment);

        Appointment clone = appointment.clone();

        if (!shared) {
            clone.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        }
        UserParticipant user = new UserParticipant(getClient().getValues().getUserId());
        user.setConfirm(Appointment.NONE);
        UserParticipant user2 = new UserParticipant(testUser2.getAjaxClient().getValues().getUserId());
        user2.setConfirm(Appointment.NONE);
        clone.setParticipants(new Participant[] { user, user2 });
        clone.setUsers(new UserParticipant[] { user, user2 });
        catm2.update(clone);
        AbstractAJAXResponse updateResponse = catm2.getLastResponse();
        assertTrue("Should fail.", updateResponse.hasError());
        String errorCode = updateResponse.getException().getErrorCode();
        if (shared) {
            assertTrue("Wrong error.", "APP-0062".equals(errorCode) || "CAL-4006".equals(errorCode));
        } else {
            assertTrue("Wrong error.", "APP-0059".equals(errorCode) || "CAL-4006".equals(errorCode));
        }
    }

    @Test
    public void testAddParticipantWithoutInfoShared() throws Exception {
        addParticipantWithoutInfo(true);
    }

    @Test
    public void testAddParticipantWithoutInfo() throws Exception {
        addParticipantWithoutInfo(false);
    }

    private void addParticipantWithoutInfo(boolean shared) throws Exception {
        String uid = generateUid();
        appointment.setUid(uid);
        String organizer = "test@extern.example.invalid";
        appointment.setOrganizer(organizer);
        catm.insert(appointment);

        Appointment clone = appointment.clone();
        clone.removeOrganizer();
        clone.removeUid();

        if (!shared) {
            clone.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        }
        UserParticipant user = new UserParticipant(getClient().getValues().getUserId());
        user.setConfirm(Appointment.NONE);
        UserParticipant user2 = new UserParticipant(testUser2.getAjaxClient().getValues().getUserId());
        user2.setConfirm(Appointment.NONE);
        clone.setParticipants(new Participant[] { user, user2 });
        clone.setUsers(new UserParticipant[] { user, user2 });

        catm2.update(clone);
        AbstractAJAXResponse updateResponse = catm2.getLastResponse();
        assertTrue("Should fail.", updateResponse.hasError());
        String errorCode = updateResponse.getException().getErrorCode();
        if (shared) {
            assertTrue("Wrong error.", "APP-0062".equals(errorCode) || "CAL-4006".equals(errorCode));
        } else {
            assertTrue("Wrong error.", "APP-0059".equals(errorCode) || "CAL-4006".equals(errorCode));
        }
    }

    @Test
    public void testAddParticipantWithOnlyExternalOrganizer() throws Exception {
        addParticipantWithOnlyExternalOrganizer(false);
    }

    @Test
    public void testAddParticipantWithOnlyExternalOrganizerShared() throws Exception {
        addParticipantWithOnlyExternalOrganizer(true);
    }

    private void addParticipantWithOnlyExternalOrganizer(boolean shared) throws Exception {
        String uid = generateUid();
        appointment.setUid(uid);
        String organizer = "test@extern.example.invalid";
        appointment.setOrganizer(organizer);
        catm.insert(appointment);

        Appointment clone = appointment.clone();
        clone.removeUid();

        if (!shared) {
            clone.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        }
        UserParticipant user = new UserParticipant(getClient().getValues().getUserId());
        user.setConfirm(Appointment.NONE);
        UserParticipant user2 = new UserParticipant(testUser2.getAjaxClient().getValues().getUserId());
        user2.setConfirm(Appointment.NONE);
        clone.setParticipants(new Participant[] { user, user2 });
        clone.setUsers(new UserParticipant[] { user, user2 });

        catm2.update(clone);
        AbstractAJAXResponse updateResponse = catm2.getLastResponse();
        assertTrue("Should fail.", updateResponse.hasError());
        String errorCode = updateResponse.getException().getErrorCode();
        if (shared) {
            assertTrue("Wrong error.", "APP-0062".equals(errorCode) || "CAL-4006".equals(errorCode));
        } else {
            assertTrue("Wrong error.", "APP-0059".equals(errorCode) || "CAL-4006".equals(errorCode));
        }
    }

    @Test
    public void testAddParticipantWithOnlyUid() throws Exception {
        addParticipantWithOnlyUid(false);
    }

    @Test
    public void testAddParticipantWithOnlyUidShared() throws Exception {
        addParticipantWithOnlyUid(true);
    }

    private void addParticipantWithOnlyUid(boolean shared) throws Exception {
        String uid = generateUid();
        appointment.setUid(uid);
        String organizer = "test@extern.example.invalid";
        appointment.setOrganizer(organizer);
        catm.insert(appointment);

        Appointment clone = appointment.clone();
        clone.removeOrganizer();

        if (!shared) {
            clone.setParentFolderID(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        }
        UserParticipant user = new UserParticipant(getClient().getValues().getUserId());
        user.setConfirm(Appointment.NONE);
        UserParticipant user2 = new UserParticipant(testUser2.getAjaxClient().getValues().getUserId());
        user2.setConfirm(Appointment.NONE);
        clone.setParticipants(new Participant[] { user, user2 });
        clone.setUsers(new UserParticipant[] { user, user2 });

        catm2.update(clone);
        AbstractAJAXResponse updateResponse = catm2.getLastResponse();
        assertTrue("Should fail.", updateResponse.hasError());
        String errorCode = updateResponse.getException().getErrorCode();
        if (shared) {
            assertTrue("Wrong error.", "APP-0062".equals(errorCode) || "CAL-4006".equals(errorCode));
        } else {
            assertTrue("Wrong error.", "APP-0059".equals(errorCode) || "CAL-4006".equals(errorCode));
        }
    }

    private String generateUid() {
        return "UID" + System.currentTimeMillis();
    }

}
