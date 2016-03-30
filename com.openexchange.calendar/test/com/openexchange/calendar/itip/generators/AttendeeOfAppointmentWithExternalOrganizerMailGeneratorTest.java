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

package com.openexchange.calendar.itip.generators;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import com.openexchange.calendar.itip.ITipRole;
import com.openexchange.calendar.itip.generators.NotificationMail;
import com.openexchange.calendar.itip.generators.NotificationMailGenerator;
import com.openexchange.calendar.itip.generators.NotificationParticipant;
import com.openexchange.calendar.itip.generators.NotificationParticipantResolver;
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import static com.openexchange.time.TimeTools.D;


/**
 * {@link AttendeeOfAppointmentWithExternalOrganizerMailGeneratorTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AttendeeOfAppointmentWithExternalOrganizerMailGeneratorTest extends AbstractMailGeneratorTest{
    // TODO: Check mails for other participants

    public static final class MockParticipantResolver implements NotificationParticipantResolver {

        @Override
        public List<NotificationParticipant> resolveAllRecipients(Appointment original, Appointment appointment, User user, User onBehalfOf, Context ctx) {
            return new ArrayList<NotificationParticipant>(Arrays.asList(
                new NotificationParticipant(ITipRole.ORGANIZER, true, "organizer@otherdomain.ox"),
                new NotificationParticipant(ITipRole.ATTENDEE, false, "internal1@domain.ox", 12),
                new NotificationParticipant(ITipRole.ATTENDEE, false, "internal2@domain.ox", 13),
                new NotificationParticipant(ITipRole.ATTENDEE, true, "external1@otherdomain.ox"),
                new NotificationParticipant(ITipRole.ATTENDEE, true, "external2@otherdomain.ox")
            ));
        }

        public NotificationParticipant resolveOrganizer(Appointment appointment, Context ctx) {
            return new NotificationParticipant(ITipRole.ORGANIZER, true, "organizer@otherdomain.ox");
        }

        public NotificationParticipant resolveUserRole(Appointment appointment, User user, Context ctx) {
            return new NotificationParticipant(ITipRole.ATTENDEE, false, "internal1@domain.ox", 12);
        }

        @Override
        public List<NotificationParticipant> getAllParticipants(List<NotificationParticipant> allRecipients, Appointment appointment, User user, Context ctx) {
            return allRecipients;
        }

        @Override
        public List<NotificationParticipant> getResources(Appointment appointment, Context ctx) throws OXException {
            // Nothing to do
            return null;
        }


    }

    MockParticipantResolver resolver  = new MockParticipantResolver();

    @Test
    public void update() throws OXException {
        Appointment appointment = createTestAppointment();
        appointment.setTitle("New Title");

        Appointment original = createTestAppointment();

        Appointment exc = createTestAppointment();
        exc.setObjectID(13);
        util.setExceptions(Arrays.asList(exc));

        NotificationMailGenerator generator = new NotificationMailGenerator(null, null, resolver, util, original, appointment, user, user, null, session);
        NotificationMail notificationMail = generator.generateUpdateMailFor("organizer@otherdomain.ox");

        assertNotNull(notificationMail);
        assertEquals("organizer@otherdomain.ox", notificationMail.getRecipient().getEmail());
        assertEquals("internal1@domain.ox", notificationMail.getSender().getEmail());
        assertEquals(appointment.getObjectID(), notificationMail.getAppointment().getObjectID());
        assertEquals(original.getTitle(), notificationMail.getOriginal().getTitle());

        ITipMessage message = notificationMail.getMessage();
        assertNotNull(message);
        assertEquals(ITipMethod.COUNTER, message.getMethod());
        assertEquals(appointment.getObjectID(), message.getAppointment().getObjectID());
        assertFalse(message.exceptions().iterator().hasNext());

        assertEquals("notify.appointment.counter.organizer", notificationMail.getTemplateName());

        notificationMail = generator.generateUpdateMailFor("internal2@domain.ox");
        assertNotNull(notificationMail);
        assertTrue(notificationMail.getMessage() == null);
        assertEquals("notify.appointment.counter.participant", notificationMail.getTemplateName());

        notificationMail = generator.generateUpdateMailFor("external1@otherdomain.ox");
        assertTrue(notificationMail == null);

    }

    @Test
    public void changeState() throws OXException {
        Appointment appointment = createTestAppointment();
        confirmFor("internal1@domain.ox", ConfirmStatus.ACCEPT, appointment);

        Appointment original = createTestAppointment();

        Appointment exc = createTestAppointment();
        exc.setObjectID(13);
        util.setExceptions(Arrays.asList(exc));

        NotificationMailGenerator generator = new NotificationMailGenerator(null, null, resolver, util, original, appointment, user, user, null, session);
        NotificationMail notificationMail = generator.generateUpdateMailFor("organizer@otherdomain.ox");

        assertNotNull(notificationMail);
        assertEquals("organizer@otherdomain.ox", notificationMail.getRecipient().getEmail());
        assertEquals("internal1@domain.ox", notificationMail.getSender().getEmail());
        assertEquals(appointment.getObjectID(), notificationMail.getAppointment().getObjectID());


        ITipMessage message = notificationMail.getMessage();
        assertNotNull(message);
        assertEquals(ITipMethod.REPLY, message.getMethod());
        assertEquals(appointment.getUid(), message.getAppointment().getUid());

        Participant[] participants = message.getAppointment().getParticipants();
        assertEquals(1, participants.length);
        assertEquals(session.getUserId(), participants[0].getIdentifier());
        assertEquals(ConfirmStatus.ACCEPT.getId(), ((UserParticipant) participants[0]).getConfirm());

        assertEquals("notify.appointment.accept", notificationMail.getTemplateName());

        notificationMail = generator.generateUpdateMailFor("internal2@domain.ox");
        assertEquals("notify.appointment.accept", notificationMail.getTemplateName());

        notificationMail = generator.generateUpdateMailFor("external1@otherdomain.ox");
        assertTrue(null == notificationMail);

    }

    private void confirmFor(String email, ConfirmStatus status, Appointment appointment) {
        UserParticipant[] users = appointment.getUsers();
        for (UserParticipant userParticipant : users) {
            if (userParticipant.getEmailAddress().equalsIgnoreCase(email)) {
                userParticipant.setConfirm(status.getId());
            }
        }
    }

    @Test
    public void addChangeException() throws OXException {
        Appointment master = createTestAppointment();

        Appointment appointment = createTestAppointment();
        appointment.setTitle("New Title");
        appointment.setRecurrenceID(master.getObjectID());
        appointment.setObjectID(master.getObjectID()+1);
        appointment.setRecurrencePosition(1);
        appointment.setRecurrenceDatePosition(appointment.getStartDate());



        NotificationMailGenerator generator = new NotificationMailGenerator(null, null, resolver, util, master, appointment, user, user, null, session);
        NotificationMail notificationMail = generator.generateCreateExceptionMailFor("organizer@otherdomain.ox");

        assertNotNull(notificationMail);
        assertEquals("organizer@otherdomain.ox", notificationMail.getRecipient().getEmail());
        assertEquals("internal1@domain.ox", notificationMail.getSender().getEmail());
        assertEquals(appointment.getObjectID(), notificationMail.getAppointment().getObjectID());

        ITipMessage message = notificationMail.getMessage();
        assertNotNull(message);
        assertEquals(ITipMethod.COUNTER, message.getMethod());
        assertEquals(appointment.getObjectID(), message.exceptions().iterator().next().getObjectID());


        assertEquals("notify.appointment.createexception", notificationMail.getTemplateName());

        notificationMail = generator.generateCreateExceptionMailFor("internal2@domain.ox");
        assertEquals("notify.appointment.createexception", notificationMail.getTemplateName());
        assertTrue(null == notificationMail.getMessage());

    }

    @Test
    public void addParticipant() throws OXException {
        Appointment appointment = createTestAppointment();
        addParticipant(appointment, "external2@otherdomain.ox", 101);

        Appointment original = createTestAppointment();
        removeExternalParticipant(original, "external2@otherdomain.ox");

        Appointment exc = createTestAppointment();
        exc.setObjectID(13);
        util.setExceptions(Arrays.asList(exc));

        NotificationMailGenerator generator = new NotificationMailGenerator(null, null, resolver, util, original, appointment, user, user, null, session);
        NotificationMail notificationMail = generator.generateUpdateMailFor("organizer@otherdomain.ox");

        ITipMessage message = notificationMail.getMessage();
        assertNotNull(message);
        assertEquals(ITipMethod.COUNTER, message.getMethod());

        assertEquals("notify.appointment.counter.organizer", notificationMail.getTemplateName());

        notificationMail = generator.generateUpdateMailFor("internal2@domain.ox");
        assertNotNull(notificationMail);
        assertTrue(notificationMail.getMessage() == null);
        assertEquals("notify.appointment.counter.participant", notificationMail.getTemplateName());

        notificationMail = generator.generateUpdateMailFor("external1@otherdomain.ox");
        assertTrue(notificationMail == null);
    }


    @Test
    public void removeParticipant() throws OXException {
        Appointment appointment = createTestAppointment();
        removeExternalParticipant(appointment, "external1@otherdomain.ox");

        Appointment original = createTestAppointment();

        Appointment exc = createTestAppointment();
        exc.setObjectID(13);
        util.setExceptions(Arrays.asList(exc));

        NotificationMailGenerator generator = new NotificationMailGenerator(null, null, resolver, util, original, appointment, user, user, null, session);
        NotificationMail notificationMail = generator.generateUpdateMailFor("organizer@otherdomain.ox");

        ITipMessage message = notificationMail.getMessage();
        assertNotNull(message);
        assertEquals(ITipMethod.COUNTER, message.getMethod());

        assertEquals("notify.appointment.counter.organizer", notificationMail.getTemplateName());

        notificationMail = generator.generateUpdateMailFor("internal2@domain.ox");
        assertNotNull(notificationMail);
        assertTrue(notificationMail.getMessage() == null);
        assertEquals("notify.appointment.counter.participant", notificationMail.getTemplateName());

        notificationMail = generator.generateUpdateMailFor("external1@otherdomain.ox");
        assertTrue(notificationMail == null);
    }

    @Test
    public void deleteAppointment() throws OXException {
        Appointment appointment = createTestAppointment();


        NotificationMailGenerator generator = new NotificationMailGenerator(null, null, resolver, util, null, appointment, user, user, null, session);
        NotificationMail notificationMail = generator.generateDeleteMailFor("organizer@otherdomain.ox");
        assertNotNull(notificationMail);
        assertEquals("organizer@otherdomain.ox", notificationMail.getRecipient().getEmail());
        assertEquals("internal1@domain.ox", notificationMail.getSender().getEmail());

        assertEquals(appointment.getObjectID(), notificationMail.getAppointment().getObjectID());


        ITipMessage message = notificationMail.getMessage();
        assertNotNull(message);
        assertEquals(ITipMethod.REPLY, message.getMethod());
        assertEquals(appointment.getUid(), message.getAppointment().getUid());

        Participant[] participants = message.getAppointment().getParticipants();
        assertEquals(1, participants.length);
        assertEquals(session.getUserId(), participants[0].getIdentifier());
        assertEquals(ConfirmStatus.DECLINE.getId(), ((UserParticipant) participants[0]).getConfirm());

        assertEquals("notify.appointment.decline", notificationMail.getTemplateName());

        notificationMail = generator.generateDeleteMailFor("internal2@domain.ox");
        assertEquals("notify.appointment.decline", notificationMail.getTemplateName());

        notificationMail = generator.generateDeleteMailFor("external1@otherdomain.ox");
        assertEquals("notify.appointment.decline", notificationMail.getTemplateName());

    }


    private Appointment createTestAppointment() {
        Appointment appointment = new Appointment();
        appointment.setStartDate(D("8:00 PM"));
        appointment.setEndDate(D("10:00 PM"));

        appointment.setOrganizer("organizer@otherdomain.ox");
        appointment.setObjectID(12);
        appointment.setTitle("title");
        appointment.setUid("the uid");
        appointment.setParticipants(Arrays.asList(
            (Participant) up(12, "internal1@domain.ox"),
            (Participant) up(13, "internal2@domain.ox"),
            (Participant) new ExternalUserParticipant("external1@otherdomain.ox")
        ));
        appointment.setUsers(Arrays.asList(
            up(12, "internal1@domain.ox"),
            up(13, "internal2@domain.ox")
        ));
        return appointment;
    }

}
