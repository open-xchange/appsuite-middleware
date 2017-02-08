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

package com.openexchange.ajax.appointment;

import static com.openexchange.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;

/**
 * {@link ConfirmOccurrencesTest}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ConfirmOccurrencesTest extends AbstractAJAXSession {

    private Appointment appointment;

    private int nextYear;

    private int occurrence = 5;

    private static int NOT_EXISTENT = -9999;

    private static final int[] COLS = new int[] { Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.RECURRENCE_ID, Appointment.RECURRENCE_POSITION, Appointment.TITLE, Appointment.CONFIRMATIONS, Appointment.USERS, Appointment.PARTICIPANTS, Appointment.RECURRENCE_POSITION };

    /**
     * Initializes a new {@link ConfirmOccurrencesTest}.
     * 
     * @param name
     */
    public ConfirmOccurrencesTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;

        appointment = new Appointment();
        appointment.setTitle("Test for occurrence based confirmations.");
        appointment.setStartDate(D("01.02." + nextYear + " 08:00"));
        appointment.setEndDate(D("01.02." + nextYear + " 09:00"));
        appointment.setRecurrenceType(Appointment.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(10);
        appointment.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        UserParticipant user1 = new UserParticipant(getClient().getValues().getUserId());
        UserParticipant user2 = new UserParticipant(getClient2().getValues().getUserId());
        ExternalUserParticipant external1 = new ExternalUserParticipant("external1@example.com");
        ExternalUserParticipant external2 = new ExternalUserParticipant("external2@example.com");
        appointment.setParticipants(new Participant[] { user1, user2, external1, external2 });
        catm.insert(appointment);
    }

    @Test
    public void testConfirmSeries() throws Exception {
        catm.setClient(getClient());
        catm.confirm(appointment, Appointment.TENTATIVE, "tentative");
        catm.confirmExternal(appointment, "external1@example.com", Appointment.TENTATIVE, "tentative");
        catm.confirmExternal(appointment, "external2@example.com", Appointment.TENTATIVE, "tentative");
        catm.setClient(getClient2());
        catm.confirm(appointment, Appointment.TENTATIVE, "tentative");
        catm.setClient(getClient());

        Appointment loadedAppointment = catm.get(appointment);
        checkConfirmations(loadedAppointment, Appointment.TENTATIVE, "tentative", 2, 0);

        Appointment[] apps = catm.all(getClient().getValues().getPrivateAppointmentFolder(), D("01.02." + nextYear + " 08:00"), D("11.02." + nextYear + " 09:00"), COLS, false);
        for (Appointment app : apps) {
            if (app.getObjectID() == appointment.getObjectID()) {
                checkConfirmations(app, Appointment.TENTATIVE, "tentative", 2);
            }
        }
    }

    @Test
    public void testException() throws Exception {
        catm.setClient(getClient());
        catm.confirm(appointment, Appointment.TENTATIVE, "tentative");
        catm.confirmExternal(appointment, "external1@example.com", Appointment.TENTATIVE, "tentative");
        catm.confirmExternal(appointment, "external2@example.com", Appointment.TENTATIVE, "tentative");
        catm.setClient(getClient2());
        catm.confirm(appointment, Appointment.TENTATIVE, "tentative");
        catm.setClient(getClient());

        Appointment exception = catm.createIdentifyingCopy(appointment);
        exception.setStartDate(D("05.02." + nextYear + " 10:00"));
        exception.setEndDate(D("05.02." + nextYear + " 12:00"));
        exception.setRecurrencePosition(this.occurrence);
        exception.setTitle(appointment.getTitle() + " - Exception");
        exception.setLastModified(new Date(Long.MAX_VALUE));
        catm.update(exception);
        appointment.setLastModified(exception.getLastModified());

        catm.confirm(exception, Appointment.DECLINE, "decline");
        catm.confirmExternal(exception, "external1@example.com", Appointment.DECLINE, "decline");
        catm.confirmExternal(exception, "external2@example.com", Appointment.DECLINE, "decline");
        catm.setClient(getClient2());
        catm.confirm(exception, Appointment.DECLINE, "decline");
        catm.setClient(getClient());

        Appointment loadedAppointment = catm.get(appointment);
        checkConfirmations(loadedAppointment, Appointment.TENTATIVE, "tentative", 2, 0);

        Appointment[] apps = catm.all(getClient().getValues().getPrivateAppointmentFolder(), D("01.02." + nextYear + " 08:00"), D("11.02." + nextYear + " 09:00"), COLS, false);
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
        catm.setClient(getClient());
        catm.confirm(appointment, Appointment.TENTATIVE, "tentative");
        catm.confirmExternal(appointment, "external1@example.com", Appointment.TENTATIVE, "tentative");
        catm.confirmExternal(appointment, "external2@example.com", Appointment.TENTATIVE, "tentative");
        catm.setClient(getClient2());
        catm.confirm(appointment, Appointment.TENTATIVE, "tentative");
        catm.setClient(getClient());

        catm.confirm(appointment, Appointment.DECLINE, "decline", this.occurrence);
        catm.confirmExternal(appointment, "external1@example.com", Appointment.DECLINE, "decline", this.occurrence);
        catm.confirmExternal(appointment, "external2@example.com", Appointment.DECLINE, "decline", this.occurrence);
        catm.setClient(getClient2());
        catm.confirm(appointment, Appointment.DECLINE, "decline", this.occurrence);
        catm.setClient(getClient());

        Appointment loadedAppointment = catm.get(appointment);
        checkConfirmations(loadedAppointment, Appointment.TENTATIVE, "tentative", 2, 0);

        Appointment[] apps = catm.all(getClient().getValues().getPrivateAppointmentFolder(), D("01.02." + nextYear + " 08:00"), D("11.02." + nextYear + " 09:00"), COLS, false);
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
        catm.setClient(getClient());
        catm.confirm(appointment, Appointment.TENTATIVE, "tentative");
        catm.confirmExternal(appointment, "external1@example.com", Appointment.TENTATIVE, "tentative");
        catm.confirmExternal(appointment, "external2@example.com", Appointment.TENTATIVE, "tentative");
        catm.setClient(getClient2());
        catm.confirm(appointment, Appointment.TENTATIVE, "tentative");
        catm.setClient(getClient());

        Appointment exception = catm.createIdentifyingCopy(appointment);
        exception.setStartDate(D("05.02." + nextYear + " 10:00"));
        exception.setEndDate(D("05.02." + nextYear + " 12:00"));
        exception.setRecurrencePosition(this.occurrence);
        exception.setTitle(appointment.getTitle() + " - Exception");
        exception.setLastModified(new Date(Long.MAX_VALUE));
        catm.update(exception);
        appointment.setLastModified(exception.getLastModified());

        catm.confirm(exception, Appointment.DECLINE, "decline");
        catm.confirmExternal(exception, "external1@example.com", Appointment.DECLINE, "decline");
        catm.confirmExternal(exception, "external2@example.com", Appointment.DECLINE, "decline");
        catm.setClient(getClient2());
        catm.confirm(exception, Appointment.DECLINE, "decline");
        catm.setClient(getClient());

        Appointment loadedAppointment = catm.get(appointment);
        checkConfirmations(loadedAppointment, Appointment.TENTATIVE, "tentative", 2);

        Appointment[] apps = catm.all(getClient().getValues().getPrivateAppointmentFolder(), D("01.02." + nextYear + " 08:00"), D("11.02." + nextYear + " 09:00"), COLS, false);
        for (Appointment app : apps) {
            if (app.getObjectID() == appointment.getObjectID()) {
                checkConfirmations(app, Appointment.TENTATIVE, "tentative", 2);
            }
        }

        loadedAppointment = catm.get(exception);
        checkConfirmations(loadedAppointment, Appointment.DECLINE, "decline", 2, 0);

        catm.confirm(appointment, Appointment.ACCEPT, "accept", this.occurrence);
        catm.confirmExternal(appointment, "external1@example.com", Appointment.ACCEPT, "accept", this.occurrence);
        catm.confirmExternal(appointment, "external2@example.com", Appointment.ACCEPT, "accept", this.occurrence);
        catm.setClient(getClient2());
        catm.confirm(appointment, Appointment.ACCEPT, "accept", this.occurrence);
        catm.setClient(getClient());

        loadedAppointment = catm.get(appointment);
        checkConfirmations(loadedAppointment, Appointment.TENTATIVE, "tentative", 2);

        apps = catm.all(getClient().getValues().getPrivateAppointmentFolder(), D("01.02." + nextYear + " 08:00"), D("11.02." + nextYear + " 09:00"), COLS, false);
        for (Appointment app : apps) {
            if (app.getObjectID() == appointment.getObjectID()) {
                checkConfirmations(app, Appointment.TENTATIVE, "tentative", 2);
            }
        }

        loadedAppointment = catm.get(exception);
        checkConfirmations(loadedAppointment, Appointment.ACCEPT, "accept", 2, 0);
    }

    @Test
    public void testConflicts() throws Exception {
        catm.setClient(getClient());
        catm.confirm(appointment, Appointment.ACCEPT, "accept");
        catm.confirmExternal(appointment, "external1@example.com", Appointment.ACCEPT, "accept");
        catm.confirmExternal(appointment, "external2@example.com", Appointment.ACCEPT, "accept");
        catm.setClient(getClient2());
        catm.confirm(appointment, Appointment.ACCEPT, "accept");
        catm.setClient(getClient());

        catm.confirm(appointment, Appointment.DECLINE, "decline", this.occurrence);
        catm.confirmExternal(appointment, "external1@example.com", Appointment.DECLINE, "decline", this.occurrence);
        catm.confirmExternal(appointment, "external2@example.com", Appointment.DECLINE, "decline", this.occurrence);
        catm.setClient(getClient2());
        catm.confirm(appointment, Appointment.DECLINE, "decline", this.occurrence);
        catm.setClient(getClient());

        Appointment loadedAppointment = catm.get(appointment);
        checkConfirmations(loadedAppointment, Appointment.ACCEPT, "accept", 2, 0);

        Appointment conflict = new Appointment();
        conflict.setTitle("Test for occurrence based confirmations. - CONFLICT");
        conflict.setStartDate(D("05.02." + nextYear + " 08:00"));
        conflict.setEndDate(D("05.02." + nextYear + " 09:00"));
        conflict.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());
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

    // TODO tests for

    private void checkConfirmations(Appointment appointment, int status, String message, int participantAmount) {
        this.checkConfirmations(appointment, status, message, participantAmount, NOT_EXISTENT);
    }

    private void checkConfirmations(Appointment appointment, int status, String message, int participantAmount, int expectedOccurrence) {
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
