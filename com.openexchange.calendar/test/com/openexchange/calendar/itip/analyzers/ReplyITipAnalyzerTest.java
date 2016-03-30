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

package com.openexchange.calendar.itip.analyzers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import com.openexchange.calendar.itip.ITipAction;
import com.openexchange.calendar.itip.ITipAnalysis;
import com.openexchange.calendar.itip.ITipAnnotation;
import com.openexchange.calendar.itip.ITipChange;
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.calendar.itip.analyzers.ReplyITipAnalyzer;
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.sim.SimBuilder;

/**
 * {@link ReplyITipAnalyzerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ReplyITipAnalyzerTest extends AbstractITipAnalyzerTest {

    @Test
    public void testMethods() {
        List<ITipMethod> methods = new ReplyITipAnalyzer(null, null).getMethods();
        assertEquals(Arrays.asList(ITipMethod.REPLY), methods);
    }

    private void statusTest(ConfirmStatus status) throws OXException {
        // Simulate ITipIntegration with a matching appointment
        SimBuilder integrationBuilder = new SimBuilder();

        // The reply contains an appointment
        CalendarDataObject appointment = appointment("123-123-123-123");

        // With a user that has accepted
        ExternalUserParticipant externalParticipant = new ExternalUserParticipant("external@somewhere.invalid");
        externalParticipant.setStatus(status);
        appointment.setParticipants(new Participant[] { externalParticipant });
        appointment.setConfirmations(new ConfirmableParticipant[] { externalParticipant });

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.setMethod(ITipMethod.REPLY);
        msg.setComment("Yes or no or whatever!");

        // The appointment exists already and contains the creator and the external participant

        CalendarDataObject original = appointment("123-123-123-123");
        original.setObjectID(12);

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        integrationBuilder.expectCall("getExceptions", original, session).andReturn(Collections.emptyList());

        externalParticipant = new ExternalUserParticipant("external@somewhere.invalid");
        externalParticipant.setStatus(ConfirmStatus.NONE);


        original.setParticipants(new Participant[] { theUser, externalParticipant });
        original.setConfirmations(new ConfirmableParticipant[] { externalParticipant });

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        ITipAnalysis analysis = new ReplyITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.UPDATE, change.getType());
        //assertEquals(status, change.getParticipantChange().getConfirmStatusUpdate());

        assertEquals("123-123-123-123", change.getNewAppointment().getUid());
        assertEquals(12, change.getCurrentAppointment().getObjectID());

        assertEquals("Yes or no or whatever!", change.getParticipantChange().getComment());

        // NewAppointment contains all participants plus the changed state of the external participant
        Participant[] participants = change.getNewAppointment().getParticipants();
        assertEquals(2, participants.length);

        for (Participant participant : participants) {
            switch (participant.getType()) {
            case Participant.USER:
                UserParticipant creatorInMergedAppointment = (UserParticipant) participant;
                assertEquals(theUser.getIdentifier(), creatorInMergedAppointment.getIdentifier());
                assertEquals(theUser.getConfirm(), creatorInMergedAppointment.getConfirm());
                break;
            case Participant.EXTERNAL_USER:
                ExternalUserParticipant externalInMergedAppointment = (ExternalUserParticipant) participant;
                assertEquals(externalParticipant.getEmailAddress(), externalInMergedAppointment.getEmailAddress());
                assertEquals(status, externalInMergedAppointment.getStatus());
                break;
            default:
                fail("Did not expect: " + participant);

            }
        }

        assertActions(analysis, ITipAction.UPDATE);

        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testAccept() throws OXException {
        statusTest(ConfirmStatus.ACCEPT);
    }


    @Test
    public void testDecline() throws OXException {
        statusTest(ConfirmStatus.DECLINE);
    }


    @Test
    public void testTentative() throws OXException {
        statusTest(ConfirmStatus.TENTATIVE);
    }


    @Test
    public void testPartyCrasher() throws OXException {
     // Simulate ITipIntegration with a matching appointment
        SimBuilder integrationBuilder = new SimBuilder();

        // The reply contains an appointment
        CalendarDataObject appointment = appointment("123-123-123-123");

        // With a user that has accepted
        ExternalUserParticipant externalParticipant = new ExternalUserParticipant("partycrasher@somewhere.invalid");
        externalParticipant.setStatus(ConfirmStatus.ACCEPT);
        appointment.setParticipants(new Participant[] { externalParticipant });

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.setMethod(ITipMethod.REPLY);
        msg.setComment("Yes or no or whatever!");

        // The appointment exists already and contains the creator and the external participant

        CalendarDataObject original = appointment("123-123-123-123");
        original.setObjectID(12);

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        integrationBuilder.expectCall("getExceptions", original, session).andReturn(Collections.emptyList());

        externalParticipant = new ExternalUserParticipant("external@somewhere.invalid");
        externalParticipant.setStatus(ConfirmStatus.ACCEPT);

        UserParticipant creator = new UserParticipant(12);
        creator.setConfirm(ConfirmStatus.ACCEPT.getId());

        original.setParticipants(new Participant[] { creator, externalParticipant });

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        ITipAnalysis analysis = new ReplyITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.UPDATE, change.getType());

        assertEquals("123-123-123-123", change.getNewAppointment().getUid());
        assertEquals(12, change.getCurrentAppointment().getObjectID());

        assertEquals("Yes or no or whatever!", change.getParticipantChange().getComment());

        // NewAppointment contains all participants plus the changed state of the external participant
        Participant[] participants = change.getNewAppointment().getParticipants();
        assertEquals(3, participants.length);

        boolean foundPartyCrasher = false;
        for (Participant participant : participants) {
            switch (participant.getType()) {
            case Participant.USER:
                UserParticipant creatorInMergedAppointment = (UserParticipant) participant;
                assertEquals(creator.getIdentifier(), creatorInMergedAppointment.getIdentifier());
                assertEquals(creator.getConfirm(), creatorInMergedAppointment.getConfirm());
                break;
            case Participant.EXTERNAL_USER:
                ExternalUserParticipant externalInMergedAppointment = (ExternalUserParticipant) participant;
                if (externalInMergedAppointment.getEmailAddress().contains("partycrasher")) {
                    // Our Party Crasher
                    foundPartyCrasher = true;
                } else {
                    // External Participant
                    assertEquals(externalParticipant.getEmailAddress(), externalInMergedAppointment.getEmailAddress());
                    assertEquals(CalendarObject.ACCEPT, externalInMergedAppointment.getConfirm());
                }
                break;
            default:
                fail("Did not expect: " + participant);

            }
        }
        assertTrue(foundPartyCrasher);
        // TODO: Diff Completed
        // Accept shows up in diff
        // Party Crasher shows up in diff

        assertActions(analysis, ITipAction.ACCEPT_PARTY_CRASHER);

        integrationBuilder.assertAllWereCalled();
    }

    public void testDelegation() {
        // TODO
    }

    public void testOnBehalfOf() {
        // TODO
    }

    // Exceptions

    private void exceptionStatusTest(ConfirmStatus status) throws OXException {
        // Simulate ITipIntegration with a matching appointment
        SimBuilder integrationBuilder = new SimBuilder();

        // The reply contains an appointment

        // And an exception
        CalendarDataObject changedException = appointment("123-123-123-123");
        changedException.setRecurrenceDatePosition(new Date(12345));

        // With a user that has accepted
        ExternalUserParticipant externalParticipant = new ExternalUserParticipant("external@somewhere.invalid");
        externalParticipant.setStatus(status);
        changedException.setParticipants(new Participant[] { externalParticipant });
        changedException.setConfirmations(new ConfirmableParticipant[] { externalParticipant });

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.addException(changedException);
        msg.setMethod(ITipMethod.REPLY);
        msg.setComment("Yes or no or whatever!");

        // The appointment exists already
        CalendarDataObject original = appointment("123-123-123-123");
        original.setObjectID(12);
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);

        // Along with two exceptions
        CalendarDataObject originalException = changedException.clone();
        originalException.setObjectID(23);
        CalendarDataObject otherException = changedException.clone();
        otherException.setRecurrenceDatePosition(new Date(5432100000L));

        integrationBuilder.expectCall("getExceptions", original, session).andReturn(new ArrayList(Arrays.asList(otherException, originalException)));


        // with the original exception containing the creator and the external participant

        externalParticipant = new ExternalUserParticipant("external@somewhere.invalid");
        externalParticipant.setStatus(ConfirmStatus.NONE);


        originalException.setParticipants(new Participant[] { theUser, externalParticipant });
        originalException.setConfirmations(new ConfirmableParticipant[] { externalParticipant });

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        ITipAnalysis analysis = new ReplyITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.UPDATE, change.getType());
        assertTrue(change.isException());
        //assertEquals(status, change.getParticipantChange().getConfirmStatusUpdate());

        assertEquals("123-123-123-123", change.getNewAppointment().getUid());
        assertEquals(23, change.getCurrentAppointment().getObjectID());

        assertEquals("Yes or no or whatever!", change.getParticipantChange().getComment());

        // NewAppointment contains all participants plus the changed state of the external participant
        Participant[] participants = change.getNewAppointment().getParticipants();
        assertEquals(2, participants.length);

        for (Participant participant : participants) {
            switch (participant.getType()) {
            case Participant.USER:
                UserParticipant creatorInMergedAppointment = (UserParticipant) participant;
                assertEquals(theUser.getIdentifier(), creatorInMergedAppointment.getIdentifier());
                assertEquals(theUser.getConfirm(), creatorInMergedAppointment.getConfirm());
                break;
            case Participant.EXTERNAL_USER:
                ExternalUserParticipant externalInMergedAppointment = (ExternalUserParticipant) participant;
                assertEquals(externalParticipant.getEmailAddress(), externalInMergedAppointment.getEmailAddress());
                assertEquals(status, externalInMergedAppointment.getStatus());
                break;
            default:
                fail("Did not expect: " + participant);

            }
        }

        // TODO: Diff Completed
        // Accept shows up in diff
        assertActions(analysis, ITipAction.UPDATE);
        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testAcceptsException() throws OXException {
        exceptionStatusTest(ConfirmStatus.ACCEPT);
    }

    @Test
    public void testDeclinesException() throws OXException {
        exceptionStatusTest(ConfirmStatus.DECLINE);
    }

    @Test
    public void testTentativelyAcceptsException() throws OXException {
        exceptionStatusTest(ConfirmStatus.TENTATIVE);
    }

    @Test
    public void testPartyCrasherForException() throws OXException {
        ConfirmStatus status = ConfirmStatus.ACCEPT;
        // Simulate ITipIntegration with a matching appointment
        SimBuilder integrationBuilder = new SimBuilder();

        // The reply contains an appointment
        CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setRecurrenceType(CalendarObject.WEEKLY);
        appointment.setInterval(1);

        // And an exception
        CalendarDataObject changedException = appointment("123-123-123-123");
        changedException.setRecurrenceDatePosition(new Date(12345));

        // With a user that has accepted
        ExternalUserParticipant externalParticipant = new ExternalUserParticipant("partycrasher@somewhere.invalid");
        externalParticipant.setStatus(status);
        changedException.setParticipants(new Participant[] { externalParticipant });

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.addException(changedException);
        msg.setMethod(ITipMethod.REPLY);
        msg.setComment("Yes or no or whatever!");

        // The appointment exists already
        CalendarDataObject original = appointment("123-123-123-123");
        original.setObjectID(12);
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);

        // Along with two exceptions
        CalendarDataObject originalException = changedException.clone();
        originalException.setObjectID(23);
        CalendarDataObject otherException = changedException.clone();
        otherException.setRecurrenceDatePosition(new Date(5432100000L));

        integrationBuilder.expectCall("getExceptions", original, session).andReturn(new ArrayList(Arrays.asList(otherException, originalException)));


        // with the original exception containing the creator and the external participant

        externalParticipant = new ExternalUserParticipant("external@somewhere.invalid");
        externalParticipant.setStatus(ConfirmStatus.ACCEPT);

        UserParticipant creator = new UserParticipant(12);
        creator.setConfirm(ConfirmStatus.ACCEPT.getId());

        originalException.setParticipants(new Participant[] { creator, externalParticipant });

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        ITipAnalysis analysis = new ReplyITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.UPDATE, change.getType());
        assertTrue(change.isException());

        assertEquals("123-123-123-123", change.getNewAppointment().getUid());
        assertEquals(23, change.getCurrentAppointment().getObjectID());

        assertEquals("Yes or no or whatever!", change.getParticipantChange().getComment());

        // NewAppointment contains all participants plus the changed state of the external participant
        Participant[] participants = change.getNewAppointment().getParticipants();
        assertEquals(3, participants.length);

        boolean foundPartyCrasher = false;
        for (Participant participant : participants) {
            switch (participant.getType()) {
            case Participant.USER:
                UserParticipant creatorInMergedAppointment = (UserParticipant) participant;
                assertEquals(creator.getIdentifier(), creatorInMergedAppointment.getIdentifier());
                assertEquals(creator.getConfirm(), creatorInMergedAppointment.getConfirm());
                break;
            case Participant.EXTERNAL_USER:
                ExternalUserParticipant externalInMergedAppointment = (ExternalUserParticipant) participant;
                if (externalInMergedAppointment.getEmailAddress().contains("partycrasher")) {
                    // Our Party Crasher
                    foundPartyCrasher = true;
                } else {
                    // External Participant
                    assertEquals(externalParticipant.getEmailAddress(), externalInMergedAppointment.getEmailAddress());
                    assertEquals(CalendarObject.ACCEPT, externalInMergedAppointment.getConfirm());
                }
                break;
            default:
                fail("Did not expect: " + participant);
            }
        }

        assertTrue(foundPartyCrasher);

        // TODO: Diff Completed
        // Accept shows up in diff
        assertActions(analysis, ITipAction.ACCEPT_PARTY_CRASHER);
        integrationBuilder.assertAllWereCalled();
    }

    public void testDelegationInException() {
        // TODO
    }

    public void testOnBehalfOfInException() {
        // TODO
    }

    @Test
    public void testContainsOnlyException() throws OXException {
        ConfirmStatus status = ConfirmStatus.ACCEPT;
        SimBuilder integrationBuilder = new SimBuilder();


        // Reply contains only an exception
        CalendarDataObject changedException = appointment("123-123-123-123");
        changedException.setRecurrenceDatePosition(new Date(12345));

        // With a user that has accepted
        ExternalUserParticipant externalParticipant = new ExternalUserParticipant("external@somewhere.invalid");
        externalParticipant.setStatus(status);
        changedException.setParticipants(new Participant[] { externalParticipant });

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.addException(changedException);
        msg.setMethod(ITipMethod.REPLY);
        msg.setComment("Yes or no or whatever!");

        // The appointment exists already
        CalendarDataObject original = appointment("123-123-123-123");
        original.setObjectID(12);
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);

        // Along with two exceptions
        CalendarDataObject originalException = changedException.clone();
        originalException.setObjectID(23);
        CalendarDataObject otherException = changedException.clone();
        otherException.setRecurrenceDatePosition(new Date(5432100000L));

        integrationBuilder.expectCall("getExceptions", original, session).andReturn(new ArrayList(Arrays.asList(otherException, originalException)));


        // with the original exception containing the creator and the external participant

        externalParticipant = new ExternalUserParticipant("external@somewhere.invalid");
        externalParticipant.setStatus(ConfirmStatus.NONE);

        UserParticipant creator = new UserParticipant(12);
        creator.setConfirm(ConfirmStatus.ACCEPT.getId());

        originalException.setParticipants(new Participant[] { creator, externalParticipant });

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        ITipAnalysis analysis = new ReplyITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.UPDATE, change.getType());
        assertTrue(change.isException());
        assertEquals(status, change.getParticipantChange().getConfirmStatusUpdate());

        assertEquals("123-123-123-123", change.getNewAppointment().getUid());
        assertEquals(23, change.getCurrentAppointment().getObjectID());

        assertEquals("Yes or no or whatever!", change.getParticipantChange().getComment());

        // NewAppointment contains all participants plus the changed state of the external participant
        Participant[] participants = change.getNewAppointment().getParticipants();
        assertEquals(2, participants.length);

        for (Participant participant : participants) {
            switch (participant.getType()) {
            case Participant.USER:
                UserParticipant creatorInMergedAppointment = (UserParticipant) participant;
                assertEquals(creator.getIdentifier(), creatorInMergedAppointment.getIdentifier());
                assertEquals(creator.getConfirm(), creatorInMergedAppointment.getConfirm());
                break;
            case Participant.EXTERNAL_USER:
                ExternalUserParticipant externalInMergedAppointment = (ExternalUserParticipant) participant;
                assertEquals(externalParticipant.getEmailAddress(), externalInMergedAppointment.getEmailAddress());
                assertEquals(status, externalInMergedAppointment.getStatus());
                break;
            default:
                fail("Did not expect: " + participant);

            }
        }

        // TODO: Diff Completed
        // Accept shows up in diff

        integrationBuilder.assertAllWereCalled();
    }

    // Error Cases

    @Test
    public void testAppointmentDoesntExistAnymore() throws OXException {
        // Simulate ITipIntegration with a matching appointment
        SimBuilder integrationBuilder = new SimBuilder();

        // The reply contains an appointment
        CalendarDataObject appointment = appointment("123-123-123-123");

        // With a user that has accepted
        ExternalUserParticipant externalParticipant = new ExternalUserParticipant("external@somewhere.invalid");
        externalParticipant.setStatus(ConfirmStatus.ACCEPT);
        appointment.setParticipants(new Participant[] { externalParticipant });

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.setMethod(ITipMethod.REPLY);
        msg.setComment("Yes or no or whatever!");


        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(null);
        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        ITipAnalysis analysis = new ReplyITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();
        assertEquals(0, changes.size());

        List<ITipAnnotation> annotations = analysis.getAnnotations();
        assertEquals(1, annotations.size());

        ITipAnnotation error = annotations.get(0);
        assertEquals("An attendee wanted to change his/her participant state in an appointment that could not be found. Probably the appointment was already canceled.", error.getMessage());


        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testExceptionDoesntExistAnymore() throws OXException {
        ConfirmStatus status = ConfirmStatus.ACCEPT;
        SimBuilder integrationBuilder = new SimBuilder();


        // Reply contains only an exception
        CalendarDataObject changedException = appointment("123-123-123-123");
        changedException.setRecurrenceDatePosition(new Date(12345));

        // With a user that has accepted
        ExternalUserParticipant externalParticipant = new ExternalUserParticipant("external@somewhere.invalid");
        externalParticipant.setStatus(status);
        changedException.setParticipants(new Participant[] { externalParticipant });

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.addException(changedException);
        msg.setMethod(ITipMethod.REPLY);
        msg.setComment("Yes or no or whatever!");

        // The appointment exists already
        CalendarDataObject original = appointment("123-123-123-123");
        original.setObjectID(12);
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);

        // Along with one other non matching exceptions
        CalendarDataObject otherException = changedException.clone();
        otherException.setRecurrenceDatePosition(new Date(5432100000L));

        integrationBuilder.expectCall("getExceptions", original, session).andReturn(new ArrayList(Arrays.asList(otherException)));




        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        ITipAnalysis analysis = new ReplyITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();
        assertEquals(1, changes.size());
        integrationBuilder.assertAllWereCalled();
    }

}
