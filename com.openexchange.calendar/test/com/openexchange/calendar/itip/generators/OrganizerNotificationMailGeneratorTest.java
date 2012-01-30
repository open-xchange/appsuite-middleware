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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.calendar.itip.ITipRole;
import com.openexchange.calendar.itip.MockITipIntegrationUtility;
import com.openexchange.calendar.itip.generators.NotificationMail;
import com.openexchange.calendar.itip.generators.NotificationMailGenerator;
import com.openexchange.calendar.itip.generators.NotificationParticipant;
import com.openexchange.calendar.itip.generators.NotificationParticipantResolver;
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ConfirmationChange;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;
import com.openexchange.session.SimSession;
import static org.junit.Assert.*;

/**
 * {@link OrganizerNotificationMailGeneratorTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OrganizerNotificationMailGeneratorTest extends AbstractMailGeneratorTest {
    MockParticipantResolver resolver  = new MockParticipantResolver();
    MockITipIntegrationUtility util = new MockITipIntegrationUtility();
    SimSession session = new SimSession(12, 1337);
    
    @Test
    public void create() throws OXException {
        Appointment appointment = createTestAppointment();
        Appointment exc = createTestAppointment();
        exc.setObjectID(13);
        util.setExceptions(Arrays.asList(exc));
        
        
        NotificationMailGenerator generator = new NotificationMailGenerator(null, resolver, util, null, appointment, user, user, null, session);
        NotificationMail notificationMail = generator.generateCreateMailFor("external1@otherdomain.ox");
        assertNotNull(notificationMail);
        assertEquals("external1@otherdomain.ox", notificationMail.getRecipient().getEmail());
        assertEquals("organizer@domain.ox", notificationMail.getSender().getEmail());
        
        assertEquals(appointment.getObjectID(), notificationMail.getAppointment().getObjectID());
        
        
        ITipMessage message = notificationMail.getMessage();
        assertNotNull(message);
        assertEquals(ITipMethod.REQUEST, message.getMethod());
        assertEquals(appointment.getObjectID(), message.getAppointment().getObjectID());
        
        assertTrue(null == generator.generateCreateMailFor("organizer@domain.ox"));
        
        assertEquals("notify.appointment.create", notificationMail.getTemplateName());
    }

    @Test
    public void update() throws OXException {
        Appointment appointment = createTestAppointment();
        appointment.setTitle("New Title");
        
        Appointment original = createTestAppointment();

        Appointment exc = createTestAppointment();
        exc.setObjectID(13);
        util.setExceptions(Arrays.asList(exc));
    
        NotificationMailGenerator generator = new NotificationMailGenerator(null, resolver, util, original, appointment, user, user, null, session);
        NotificationMail notificationMail = generator.generateUpdateMailFor("external1@otherdomain.ox");

        assertNotNull(notificationMail);
        assertEquals("external1@otherdomain.ox", notificationMail.getRecipient().getEmail());
        assertEquals("organizer@domain.ox", notificationMail.getSender().getEmail());
        assertEquals(appointment.getObjectID(), notificationMail.getAppointment().getObjectID());
        assertEquals(original.getTitle(), notificationMail.getOriginal().getTitle());
        
        ITipMessage message = notificationMail.getMessage();
        assertNotNull(message);
        assertEquals(ITipMethod.REQUEST, message.getMethod());
        assertEquals(appointment.getObjectID(), message.getAppointment().getObjectID());

        assertTrue(null == generator.generateCreateMailFor("organizer@domain.ox"));

        assertEquals("notify.appointment.update", notificationMail.getTemplateName());

    }
    
    @Test
    public void addChangeException() throws OXException {
        Appointment appointment = createTestAppointment();
        appointment.setTitle("New Title");
        appointment.setRecurrenceDatePosition(appointment.getStartDate());

        Appointment master = createTestAppointment();

    
        NotificationMailGenerator generator = new NotificationMailGenerator(null, resolver, util, master, appointment, user, user, null, session);
        NotificationMail notificationMail = generator.generateCreateExceptionMailFor("external1@otherdomain.ox");

        assertNotNull(notificationMail);
        assertEquals("external1@otherdomain.ox", notificationMail.getRecipient().getEmail());
        assertEquals("organizer@domain.ox", notificationMail.getSender().getEmail());
        assertEquals(appointment.getObjectID(), notificationMail.getAppointment().getObjectID());
        
        ITipMessage message = notificationMail.getMessage();
        assertNotNull(message);
        assertEquals(ITipMethod.REQUEST, message.getMethod());
        assertEquals(appointment.getObjectID(), message.exceptions().iterator().next().getObjectID());

        assertTrue(null == generator.generateCreateMailFor("organizer@domain.ox"));

        assertEquals("notify.appointment.createexception", notificationMail.getTemplateName());
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
    
        NotificationMailGenerator generator = new NotificationMailGenerator(null, resolver, util, original, appointment, user, user, null, session);
        NotificationMail notificationMail = generator.generateUpdateMailFor("external1@otherdomain.ox");

        ITipMessage message = notificationMail.getMessage();
        assertNotNull(message);
        assertEquals(ITipMethod.REQUEST, message.getMethod());
        assertEquals("notify.appointment.update", notificationMail.getTemplateName());
        
        notificationMail = generator.generateUpdateMailFor("external2@otherdomain.ox");

        message = notificationMail.getMessage();
        assertNotNull(message);
        assertEquals(ITipMethod.REQUEST, message.getMethod());
        assertEquals("notify.appointment.create", notificationMail.getTemplateName());
        
    }
    
    @Test
    public void removeParticipant() throws OXException {
        Appointment appointment = createTestAppointment();
        removeExternalParticipant(appointment, "external1@otherdomain.ox");
        
        Appointment original = createTestAppointment();

        Appointment exc = createTestAppointment();
        exc.setObjectID(13);
        util.setExceptions(Arrays.asList(exc));
    
        NotificationMailGenerator generator = new NotificationMailGenerator(null, resolver, util, original, appointment, user, user, null, session);
        NotificationMail notificationMail = generator.generateUpdateMailFor("internal1@domain.ox");

        ITipMessage message = notificationMail.getMessage();
        assertNotNull(message);
        assertEquals(ITipMethod.REQUEST, message.getMethod());
        assertEquals("notify.appointment.update", notificationMail.getTemplateName());
        
        notificationMail = generator.generateUpdateMailFor("external1@otherdomain.ox");

        message = notificationMail.getMessage();
        assertNotNull(message);
        assertEquals(ITipMethod.CANCEL, message.getMethod());
        assertEquals(12, message.getAppointment().getObjectID());
        assertEquals("notify.appointment.delete", notificationMail.getTemplateName());
    }
  
    
    @Test
    public void deleteAppointment() throws OXException {
        Appointment appointment = createTestAppointment();
        
        
        NotificationMailGenerator generator = new NotificationMailGenerator(null, resolver, util, null, appointment, user, user, null, session);
        NotificationMail notificationMail = generator.generateDeleteMailFor("external1@otherdomain.ox");
        assertNotNull(notificationMail);
        assertEquals("external1@otherdomain.ox", notificationMail.getRecipient().getEmail());
        assertEquals("organizer@domain.ox", notificationMail.getSender().getEmail());
        
        assertEquals(appointment.getObjectID(), notificationMail.getAppointment().getObjectID());
        
        
        ITipMessage message = notificationMail.getMessage();
        assertNotNull(message);
        assertEquals(ITipMethod.CANCEL, message.getMethod());
        assertEquals(appointment.getObjectID(), message.getAppointment().getObjectID());

        assertEquals("notify.appointment.delete", notificationMail.getTemplateName());
        
        assertTrue(null == generator.generateDeleteMailFor("organizer@domain.ox"));
    }
    
    public void addParticipantInChangeExceptionOnly() {
        // TODO: Find out what others are doing here
    }
    
    public void addParticipantToASecondChangeException() {
        // TODO: Find out what others are doing here
    }
    
    
    private Appointment createTestAppointment() {
        Appointment appointment = new Appointment();
        appointment.setObjectID(12);
        appointment.setTitle("title");
        appointment.setParticipants(Arrays.asList(
            (Participant) new UserParticipant(12),
            (Participant) new UserParticipant(13),
            (Participant) new UserParticipant(14),
            (Participant) ep("external1@otherdomain.ox", 100)
        ));
        appointment.setUsers(Arrays.asList(
            new UserParticipant(12),
            new UserParticipant(13),
            new UserParticipant(14)
        ));
        return appointment;
    }


    public static final class MockParticipantResolver implements NotificationParticipantResolver {

        public List<NotificationParticipant> resolveAllRecipients(Appointment original, Appointment appointment, User user, User onBehalfOf, Context ctx) {
            return new ArrayList<NotificationParticipant>(Arrays.asList(
                new NotificationParticipant(ITipRole.ORGANIZER, false, "organizer@domain.ox", 12),
                new NotificationParticipant(ITipRole.ATTENDEE, false, "internal1@domain.ox", 13),
                new NotificationParticipant(ITipRole.ATTENDEE, false, "internal2@domain.ox", 14),
                new NotificationParticipant(ITipRole.ATTENDEE, true, "external1@otherdomain.ox"),
                new NotificationParticipant(ITipRole.ATTENDEE, true, "external2@otherdomain.ox")
            ));
        }

        public NotificationParticipant resolveOrganizer(Appointment appointment, Context ctx) {
            return new NotificationParticipant(ITipRole.ORGANIZER, false, "organizer@domain.ox");
        }

        public NotificationParticipant resolveUserRole(Appointment appointment, User user, Context ctx) {
            return new NotificationParticipant(ITipRole.ORGANIZER, false, "organizer@domain.ox");
        }

        public List<NotificationParticipant> getAllParticipants(List<NotificationParticipant> allRecipients, Appointment appointment, User user, Context ctx) {
            return allRecipients;
        }

        /* (non-Javadoc)
         * @see com.openexchange.calendar.itip.generators.NotificationParticipantResolver#getResources(com.openexchange.groupware.container.Appointment, com.openexchange.groupware.contexts.Context)
         */
        public List<NotificationParticipant> getResources(Appointment appointment, Context ctx) throws OXException {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
