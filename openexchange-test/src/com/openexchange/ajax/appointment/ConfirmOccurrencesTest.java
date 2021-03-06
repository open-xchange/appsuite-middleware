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

package com.openexchange.ajax.appointment;

import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.common.test.TestClassConfig;

/**
 * {@link ConfirmOccurrencesTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ConfirmOccurrencesTest extends AbstractAJAXSession {

    private AJAXClient client1;

    private AJAXClient client2;

    private CalendarTestManager ctm2;

    private Appointment appointment;

    private int folderId1;

    private int folderId2;

    private int nextYear;

    private final int occurrence = 5;

    private static final int[] COLS = new int[] {
        Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.RECURRENCE_ID, Appointment.RECURRENCE_POSITION, Appointment.TITLE,
        Appointment.CONFIRMATIONS, Appointment.USERS, Appointment.PARTICIPANTS, Appointment.RECURRENCE_POSITION };

    /**
     * Initializes a new {@link ConfirmOccurrencesTest}.
     */
    public ConfirmOccurrencesTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        client1 = getClient();
        client2 = testUser2.getAjaxClient();
        ctm2 = new CalendarTestManager(client2);
        nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;
        folderId1 = client1.getValues().getPrivateAppointmentFolder();
        folderId2 = client2.getValues().getPrivateAppointmentFolder();

        appointment = new Appointment();
        appointment.setTitle("Test for occurrence based confirmations.");
        appointment.setStartDate(D("01.02." + nextYear + " 08:00"));
        appointment.setEndDate(D("01.02." + nextYear + " 09:00"));
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(10);
        appointment.setParentFolderID(folderId1);
        appointment.setIgnoreConflicts(true);
        UserParticipant user1 = new UserParticipant(client1.getValues().getUserId());
        UserParticipant user2 = new UserParticipant(client2.getValues().getUserId());
        ExternalUserParticipant external1 = new ExternalUserParticipant("external1@example.com");
        ExternalUserParticipant external2 = new ExternalUserParticipant("external2@example.com");
        appointment.setParticipants(new Participant[] { user1, user2, external1, external2 });
        catm.insert(appointment);
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testConfirmSeries() throws Exception {
        catm.confirm(folderId1, appointment.getObjectID(), catm.getLastModification(), Appointment.TENTATIVE, "tentative");
        catm.confirmExternal(folderId1, appointment.getObjectID(), catm.getLastModification(), "external1@example.com", Appointment.TENTATIVE, "tentative");
        catm.confirmExternal(folderId1, appointment.getObjectID(), catm.getLastModification(), "external2@example.com", Appointment.TENTATIVE, "tentative");
        ctm2.confirm(folderId2, appointment.getObjectID(), catm.getLastModification(), Appointment.TENTATIVE, "tentative");

        Appointment loadedAppointment = catm.get(appointment);
        checkConfirmations(loadedAppointment, Appointment.TENTATIVE, "tentative", 2);

        Appointment[] apps = catm.all(client1.getValues().getPrivateAppointmentFolder(), D("01.02." + nextYear + " 08:00"), D("11.02." + nextYear + " 09:00"), COLS, false);
        for (Appointment app : apps) {
            if (app.getObjectID() == appointment.getObjectID()) {
                checkConfirmations(app, Appointment.TENTATIVE, "tentative", 2);
            }
        }
    }

    @Test
    public void testException() throws Exception {
        catm.confirm(folderId1, appointment.getObjectID(), catm.getLastModification(), Appointment.TENTATIVE, "tentative");
        catm.confirmExternal(folderId1, appointment.getObjectID(), catm.getLastModification(), "external1@example.com", Appointment.TENTATIVE, "tentative");
        catm.confirmExternal(folderId1, appointment.getObjectID(), catm.getLastModification(), "external2@example.com", Appointment.TENTATIVE, "tentative");
        ctm2.confirm(folderId2, appointment.getObjectID(), catm.getLastModification(), Appointment.TENTATIVE, "tentative");

        Appointment exception = catm.createIdentifyingCopy(appointment);
        exception.setStartDate(D("05.02." + nextYear + " 10:00"));
        exception.setEndDate(D("05.02." + nextYear + " 12:00"));
        exception.setRecurrencePosition(this.occurrence);
        exception.setTitle(appointment.getTitle() + " - Exception");
        exception.setLastModified(new Date(Long.MAX_VALUE));
        exception.setIgnoreConflicts(true);
        catm.update(exception);
        appointment.setLastModified(exception.getLastModified());

        catm.confirm(folderId1, exception.getObjectID(), catm.getLastModification(), Appointment.DECLINE, "decline");
        catm.confirmExternal(folderId1, exception.getObjectID(), catm.getLastModification(), "external1@example.com", Appointment.DECLINE, "decline");
        catm.confirmExternal(folderId1, exception.getObjectID(), catm.getLastModification(), "external2@example.com", Appointment.DECLINE, "decline");
        ctm2.confirm(folderId2, exception.getObjectID(), catm.getLastModification(), Appointment.DECLINE, "decline");

        Appointment loadedAppointment = catm.get(appointment);
        checkConfirmations(loadedAppointment, Appointment.TENTATIVE, "tentative", 2);

        Appointment[] apps = catm.all(client1.getValues().getPrivateAppointmentFolder(), D("01.02." + nextYear + " 08:00"), D("11.02." + nextYear + " 09:00"), COLS, false);
        for (Appointment app : apps) {
            if (app.getObjectID() == appointment.getObjectID()) {
                checkConfirmations(app, Appointment.TENTATIVE, "tentative", 2);
            }
        }

        loadedAppointment = catm.get(exception);
        checkConfirmations(loadedAppointment, Appointment.DECLINE, "decline", 2);
    }

    @Test
    public void testOccurrence() throws Exception {
        catm.confirm(folderId1, appointment.getObjectID(), catm.getLastModification(), Appointment.TENTATIVE, "tentative");
        catm.confirmExternal(folderId1, appointment.getObjectID(), catm.getLastModification(), "external1@example.com", Appointment.TENTATIVE, "tentative");
        catm.confirmExternal(folderId1, appointment.getObjectID(), catm.getLastModification(), "external2@example.com", Appointment.TENTATIVE, "tentative");
        ctm2.confirm(folderId2, appointment.getObjectID(), catm.getLastModification(), Appointment.TENTATIVE, "tentative");

        catm.confirm(folderId1, appointment.getObjectID(), ctm2.getLastModification(), Appointment.DECLINE, "decline", this.occurrence);
        catm.confirmExternal(folderId1, appointment.getObjectID(), catm.getLastModification(), "external1@example.com", Appointment.DECLINE, "decline", this.occurrence);
        catm.confirmExternal(folderId1, appointment.getObjectID(), catm.getLastModification(), "external2@example.com", Appointment.DECLINE, "decline", this.occurrence);
        ctm2.confirm(folderId2, appointment.getObjectID(), catm.getLastModification(), Appointment.DECLINE, "decline", this.occurrence);

        Appointment loadedAppointment = catm.get(appointment);
        checkConfirmations(loadedAppointment, Appointment.TENTATIVE, "tentative", 2);

        Appointment[] apps = catm.all(client1.getValues().getPrivateAppointmentFolder(), D("01.02." + nextYear + " 08:00"), D("11.02." + nextYear + " 09:00"), COLS, false);
        for (Appointment app : apps) {
            if (app.getObjectID() == appointment.getObjectID()) {
                if (app.getRecurrencePosition() == this.occurrence) {
                    checkConfirmations(app, Appointment.DECLINE, "decline", 2);
                } else {
                    checkConfirmations(app, Appointment.TENTATIVE, "tentative", 2);
                }
            }
        }
    }

    @Test
    public void testOccurrenceOnExistingException() throws Exception {
        catm.confirm(folderId1, appointment.getObjectID(), catm.getLastModification(), Appointment.TENTATIVE, "tentative");
        catm.confirmExternal(folderId1, appointment.getObjectID(), catm.getLastModification(), "external1@example.com", Appointment.TENTATIVE, "tentative");
        catm.confirmExternal(folderId1, appointment.getObjectID(), catm.getLastModification(), "external2@example.com", Appointment.TENTATIVE, "tentative");
        ctm2.confirm(folderId2, appointment.getObjectID(), catm.getLastModification(), Appointment.TENTATIVE, "tentative");

        Appointment exception = catm.createIdentifyingCopy(appointment);
        exception.setStartDate(D("05.02." + nextYear + " 10:00"));
        exception.setEndDate(D("05.02." + nextYear + " 12:00"));
        exception.setRecurrencePosition(this.occurrence);
        exception.setTitle(appointment.getTitle() + " - Exception");
        exception.setLastModified(new Date(Long.MAX_VALUE));
        exception.setIgnoreConflicts(true);
        catm.update(exception);
        appointment.setLastModified(exception.getLastModified());

        catm.confirm(folderId1, exception.getObjectID(), catm.getLastModification(), Appointment.DECLINE, "decline");
        catm.confirmExternal(folderId1, exception.getObjectID(), catm.getLastModification(), "external1@example.com", Appointment.DECLINE, "decline");
        catm.confirmExternal(folderId1, exception.getObjectID(), catm.getLastModification(), "external2@example.com", Appointment.DECLINE, "decline");
        ctm2.confirm(folderId2, exception.getObjectID(), catm.getLastModification(), Appointment.DECLINE, "decline");

        Appointment loadedAppointment = catm.get(appointment);
        checkConfirmations(loadedAppointment, Appointment.TENTATIVE, "tentative", 2);

        Appointment[] apps = catm.all(client1.getValues().getPrivateAppointmentFolder(), D("01.02." + nextYear + " 08:00"), D("11.02." + nextYear + " 09:00"), COLS, false);
        for (Appointment app : apps) {
            if (app.getObjectID() == appointment.getObjectID()) {
                checkConfirmations(app, Appointment.TENTATIVE, "tentative", 2);
            }
        }

        loadedAppointment = catm.get(exception);
        checkConfirmations(loadedAppointment, Appointment.DECLINE, "decline", 2);

        catm.confirm(folderId1, appointment.getObjectID(), catm.getLastModification(), Appointment.ACCEPT, "accept", this.occurrence);
        catm.confirmExternal(folderId1, appointment.getObjectID(), catm.getLastModification(), "external1@example.com", Appointment.ACCEPT, "accept", this.occurrence);
        catm.confirmExternal(folderId1, appointment.getObjectID(), catm.getLastModification(), "external2@example.com", Appointment.ACCEPT, "accept", this.occurrence);
        ctm2.confirm(folderId2, appointment.getObjectID(), catm.getLastModification(), Appointment.ACCEPT, "accept", this.occurrence);

        loadedAppointment = catm.get(appointment);
        checkConfirmations(loadedAppointment, Appointment.TENTATIVE, "tentative", 2);

        apps = catm.all(client1.getValues().getPrivateAppointmentFolder(), D("01.02." + nextYear + " 08:00"), D("11.02." + nextYear + " 09:00"), COLS, false);
        for (Appointment app : apps) {
            if (app.getObjectID() == appointment.getObjectID()) {
                checkConfirmations(app, Appointment.TENTATIVE, "tentative", 2);
            }
        }

        loadedAppointment = catm.get(exception);
        checkConfirmations(loadedAppointment, Appointment.ACCEPT, "accept", 2);
    }

    @Test
    public void testConflicts() throws Exception {
        catm.confirm(folderId1, appointment.getObjectID(), catm.getLastModification(), Appointment.ACCEPT, "accept");
        catm.confirmExternal(folderId1, appointment.getObjectID(), catm.getLastModification(), "external1@example.com", Appointment.ACCEPT, "accept");
        catm.confirmExternal(folderId1, appointment.getObjectID(), catm.getLastModification(), "external2@example.com", Appointment.ACCEPT, "accept");
        ctm2.confirm(folderId2, appointment.getObjectID(), catm.getLastModification(), Appointment.ACCEPT, "accept");

        catm.confirm(folderId1, appointment.getObjectID(), ctm2.getLastModification(), Appointment.DECLINE, "decline", this.occurrence);
        catm.confirmExternal(folderId1, appointment.getObjectID(), catm.getLastModification(), "external1@example.com", Appointment.DECLINE, "decline", this.occurrence);
        catm.confirmExternal(folderId1, appointment.getObjectID(), catm.getLastModification(), "external2@example.com", Appointment.DECLINE, "decline", this.occurrence);
        ctm2.confirm(folderId2, appointment.getObjectID(), catm.getLastModification(), Appointment.DECLINE, "decline", this.occurrence);

        Appointment loadedAppointment = catm.get(appointment);
        checkConfirmations(loadedAppointment, Appointment.ACCEPT, "accept", 2);

        Appointment conflict = new Appointment();
        conflict.setTitle("Test for occurrence based confirmations. - CONFLICT");
        conflict.setStartDate(D("05.02." + nextYear + " 08:00"));
        conflict.setEndDate(D("05.02." + nextYear + " 09:00"));
        conflict.setParentFolderID(client1.getValues().getPrivateAppointmentFolder());
        conflict.setIgnoreConflicts(false);

        catm.insert(conflict);
        List<ConflictObject> conflicts = ((AppointmentInsertResponse) catm.getLastResponse()).getConflicts();
        boolean foundBadConflict = false;
        if (conflicts != null) {
            for (ConflictObject co : conflicts) {
                if (co.getId() == appointment.getObjectID()) {
                    foundBadConflict = true;
                    break;
                }
            }
        }
        assertFalse("Found conflict", foundBadConflict);
    }

    private void checkConfirmations(Appointment appointment, int status, String message, int participantAmount) {
        assertEquals("Wrong amount of participants.", participantAmount, appointment.getConfirmations().length);
        assertEquals("Wrong amount of participants.", participantAmount, appointment.getUsers().length);
        for (ConfirmableParticipant p : appointment.getConfirmations()) {
            assertEquals("Wrong confirmation status.", status, p.getConfirm());
            assertEquals("Wrong confirmation message.", message, p.getMessage());
        }
        for (UserParticipant p : appointment.getUsers()) {
            assertEquals("Wrong confirmation status.", status, p.getConfirm());
            assertEquals("Wrong confirmation message.", message, p.getConfirmMessage());
        }
    }
}
