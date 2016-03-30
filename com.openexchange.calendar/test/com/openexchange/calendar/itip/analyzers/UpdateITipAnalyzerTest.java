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

import static org.junit.Assert.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.calendar.itip.ITipAction;
import com.openexchange.calendar.itip.ITipAnalysis;
import com.openexchange.calendar.itip.ITipAnnotation;
import com.openexchange.calendar.itip.ITipChange;
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.calendar.itip.analyzers.UpdateITipAnalyzer;
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.sim.SimBuilder;
import static com.openexchange.time.TimeTools.D;

/**
 * {@link UpdateITipAnalyzerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UpdateITipAnalyzerTest extends AbstractITipAnalyzerTest {


    // Happy cases
    @Test
    public void testMethods() {
        List<ITipMethod> methods = new UpdateITipAnalyzer(null, null).getMethods();
        assertEquals(Arrays.asList(ITipMethod.REQUEST, ITipMethod.COUNTER, ITipMethod.PUBLISH), methods);
    }

    // New Appointments

    @Test
    public void testNewAppointment() throws OXException {
        // Simulate ITipIntegration without appointments
        CalendarDataObject appointment = appointment("123-123-123-123");
        SimBuilder integrationBuilder = new SimBuilder();
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(null);
        integrationBuilder.expectCall("getConflicts", appointment, session).andReturn(Collections.emptyList());

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.setMethod(ITipMethod.REQUEST);

        ITipAnalysis analysis =  new UpdateITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.CREATE, change.getType());
        assertEquals("123-123-123-123", change.getNewAppointment().getUid());

        assertActions(analysis, ITipAction.ACCEPT, ITipAction.DECLINE,  ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);

        integrationBuilder.assertAllWereCalled();

    }

    @Test
    public void testNewWithConflictingAppointments() throws OXException {
        CalendarDataObject appointment = appointment("123-123-123-123");
        SimBuilder integrationBuilder = new SimBuilder();
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(null);
        integrationBuilder.expectCall("getConflicts", appointment, session).andReturn(
            Arrays.asList(appointment("1"), appointment("2"), appointment("3")));
        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment("123-123-123-123"));
        msg.setMethod(ITipMethod.REQUEST);
        ITipAnalysis analysis =  new UpdateITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        List<Appointment> conflicts = change.getConflicts();

        assertNotNull(conflicts);
        assertEquals(3, conflicts.size());
        assertEquals("1", conflicts.get(0).getUid());
        assertEquals("2", conflicts.get(1).getUid());
        assertEquals("3", conflicts.get(2).getUid());

        assertActions(analysis, ITipAction.ACCEPT_AND_IGNORE_CONFLICTS, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);


    }

    @Test
    public void testNewSeriesWithExceptions() throws OXException {
        // Simulate ITipIntegration without appointments
        CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setRecurrenceType(CalendarObject.WEEKLY);
        appointment.setInterval(1);
        appointment.setObjectID(1);

        CalendarDataObject ex1 = appointment("123-123-123-123");
        ex1.setObjectID(101);
        CalendarDataObject ex2 = appointment("123-123-123-123");
        ex2.setObjectID(102);
        CalendarDataObject ex3 = appointment("123-123-123-123");
        ex3.setObjectID(103);
        CalendarDataObject ex4 = appointment("123-123-123-123");
        ex4.setObjectID(104);

        SimBuilder integrationBuilder = new SimBuilder();
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(null);
        integrationBuilder.expectCall("getConflicts", appointment, session).andReturn(Collections.emptyList());
        integrationBuilder.expectCall("getConflicts", ex1, session).andReturn(Collections.emptyList());
        integrationBuilder.expectCall("getConflicts", ex2, session).andReturn(Collections.emptyList());
        integrationBuilder.expectCall("getConflicts", ex3, session).andReturn(Collections.emptyList());
        integrationBuilder.expectCall("getConflicts", ex4, session).andReturn(Collections.emptyList());

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.setMethod(ITipMethod.REQUEST);
        msg.addException(ex1);
        msg.addException(ex2);
        msg.addException(ex3);
        msg.addException(ex4);

        ITipAnalysis analysis =  new UpdateITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(5, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.CREATE, change.getType());
        assertEquals("123-123-123-123", change.getNewAppointment().getUid());

        assertEquals(ITipChange.Type.CREATE, changes.get(1).getType());
        assertEquals(ITipChange.Type.CREATE, changes.get(2).getType());
        assertEquals(ITipChange.Type.CREATE, changes.get(3).getType());
        assertEquals(ITipChange.Type.CREATE, changes.get(4).getType());

        assertTrue(changes.get(1).isException());
        assertTrue(changes.get(2).isException());
        assertTrue(changes.get(3).isException());
        assertTrue(changes.get(4).isException());

        assertEquals(appointment, changes.get(1).getMasterAppointment());
        assertEquals(appointment, changes.get(2).getMasterAppointment());
        assertEquals(appointment, changes.get(3).getMasterAppointment());
        assertEquals(appointment, changes.get(4).getMasterAppointment());

        assertEquals(ex1, changes.get(1).getNewAppointment());
        assertEquals(ex2, changes.get(2).getNewAppointment());
        assertEquals(ex3, changes.get(3).getNewAppointment());
        assertEquals(ex4, changes.get(4).getNewAppointment());

        assertActions(analysis, ITipAction.ACCEPT, ITipAction.DECLINE,  ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);

        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testUpdate() throws OXException {
        // Simulate ITipIntegration with a matching appointment
        SimBuilder integrationBuilder = new SimBuilder();

        CalendarDataObject appointment = appointment("123-123-123-123");
        CalendarDataObject original = appointment.clone();
        original.setStartDate(D("tomorrow at 09:00"));
        original.setEndDate(D("tomorrow at 09:30"));
        original.setObjectID(12);

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        integrationBuilder.expectCall("getExceptions", original, session).andReturn(Collections.emptyList());
        integrationBuilder.expectCall("getConflicts", appointment, session).andReturn(Collections.emptyList());

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.setMethod(ITipMethod.REQUEST);

        ITipAnalysis analysis =  new UpdateITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.UPDATE, change.getType());
        assertEquals("123-123-123-123", change.getNewAppointment().getUid());
        assertEquals(12, change.getCurrentAppointment().getObjectID());

        assertActions(analysis, ITipAction.ACCEPT, ITipAction.DECLINE,  ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);


        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testUpdateWithConflicts() throws OXException {
     // Simulate ITipIntegration with a matching appointment
        SimBuilder integrationBuilder = new SimBuilder();

        CalendarDataObject appointment = appointment("123-123-123-123");
        CalendarDataObject original = appointment.clone();
        original.setStartDate(D("tomorrow at 09:00"));
        original.setEndDate(D("tomorrow at 09:30"));
        original.setObjectID(12);

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        integrationBuilder.expectCall("getExceptions", original, session).andReturn(Collections.emptyList());
        integrationBuilder.expectCall("getConflicts", appointment, session).andReturn( Arrays.asList(appointment("1"), appointment("2"), appointment("3")));

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.setMethod(ITipMethod.REQUEST);

        ITipAnalysis analysis =  new UpdateITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.UPDATE, change.getType());
        assertEquals("123-123-123-123", change.getNewAppointment().getUid());
        assertEquals(12, change.getCurrentAppointment().getObjectID());

        assertActions(analysis, ITipAction.ACCEPT_AND_IGNORE_CONFLICTS, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);


        integrationBuilder.assertAllWereCalled();
    }

    public void testUpdateButNotReschedule() throws OXException {
        // Simulate ITipIntegration with a matching appointment
        SimBuilder integrationBuilder = new SimBuilder();

        CalendarDataObject appointment = appointment("123-123-123-123");
        CalendarDataObject original = appointment.clone();
        original.setTitle("The older title");
        original.setObjectID(12);

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        integrationBuilder.expectCall("getExceptions", original, session).andReturn(Collections.emptyList());
        integrationBuilder.expectCall("getConflicts", appointment, session).andReturn(Collections.emptyList());

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.setMethod(ITipMethod.REQUEST);

        ITipAnalysis analysis =  new UpdateITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.UPDATE, change.getType());
        assertEquals("123-123-123-123", change.getNewAppointment().getUid());
        assertEquals(12, change.getCurrentAppointment().getObjectID());

        assertActions(analysis, ITipAction.UPDATE);


        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testDiff() throws OXException {
        // Simulate ITipIntegration with a matching appointment
        SimBuilder integrationBuilder = new SimBuilder();

        CalendarDataObject appointment = appointment("123-123-123-123");
        CalendarDataObject original = appointment.clone();
        original.setStartDate(D("tomorrow at 09:00"));
        original.setEndDate(D("tomorrow at 09:30"));
        original.setObjectID(12);

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        integrationBuilder.expectCall("getExceptions", original, session).andReturn(Collections.emptyList());
        integrationBuilder.expectCall("getConflicts", appointment, session).andReturn(Collections.emptyList());

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.setMethod(ITipMethod.REQUEST);

        ITipAnalysis analysis =  new UpdateITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        AppointmentDiff diff = change.getDiff();
        assertNotNull(diff);
        Set<String> differingFieldNames = diff.getDifferingFieldNames();

        assertEquals(2, differingFieldNames.size());
        assertTrue(differingFieldNames.contains(CalendarFields.START_DATE));
        assertTrue(differingFieldNames.contains(CalendarFields.END_DATE));

        assertActions(analysis, ITipAction.ACCEPT, ITipAction.DECLINE,  ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);

        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testUpdateWithNewException() throws OXException {
        SimBuilder integrationBuilder = new SimBuilder();

        CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setRecurrenceType(CalendarObject.WEEKLY);
        appointment.setInterval(1);

        CalendarDataObject newException = appointment("123-123-123-123");
        newException.setTitle("Exception title");
        newException.setStartDate(D("Tomorrow at 09:00"));
        newException.setRecurrenceDatePosition(new Date(0));

        CalendarDataObject original = appointment.clone();
        original.setObjectID(12);

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        integrationBuilder.expectCall("getExceptions", original, session).andReturn(Collections.emptyList());
        integrationBuilder.expectCall("getConflicts", newException, session).andReturn(Collections.emptyList());

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.addException(newException);
        msg.setMethod(ITipMethod.REQUEST);

        ITipAnalysis analysis =  new UpdateITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.CREATE, change.getType());
        assertTrue(change.isException());
        assertEquals(newException, change.getNewAppointment());
        assertEquals(12, change.getMasterAppointment().getObjectID());
        assertNotNull(change.getDiff());

        assertActions(analysis, ITipAction.ACCEPT, ITipAction.DECLINE,  ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);

        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testUpdateWithNewExceptionThatIsntARescheduling() throws OXException {
        SimBuilder integrationBuilder = new SimBuilder();

        CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setRecurrenceType(CalendarObject.WEEKLY);
        appointment.setInterval(1);

        CalendarDataObject newException = appointment("123-123-123-123");
        newException.setTitle("Exception title");
        newException.setRecurrenceDatePosition(new Date(0));

        CalendarDataObject original = appointment.clone();
        original.setObjectID(12);

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        integrationBuilder.expectCall("getExceptions", original, session).andReturn(Collections.emptyList());
        integrationBuilder.expectCall("getConflicts", newException, session).andReturn(Collections.emptyList());

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.addException(newException);
        msg.setMethod(ITipMethod.REQUEST);

        ITipAnalysis analysis =  new UpdateITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.CREATE, change.getType());
        assertTrue(change.isException());
        assertEquals(newException, change.getNewAppointment());
        assertEquals(12, change.getMasterAppointment().getObjectID());
        assertNotNull(change.getDiff());

        assertActions(analysis, ITipAction.ACCEPT, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);

        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testUpdateWithNewExceptionAndConflicts() throws OXException {
        SimBuilder integrationBuilder = new SimBuilder();

        CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setRecurrenceType(CalendarObject.WEEKLY);
        appointment.setInterval(1);

        CalendarDataObject newException = appointment("123-123-123-123");
        newException.setTitle("Exception title");
        newException.setStartDate(D("Tomorrow at 09:00"));
        newException.setRecurrenceDatePosition(new Date(0));

        CalendarDataObject original = appointment.clone();
        original.setObjectID(12);

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        integrationBuilder.expectCall("getExceptions", original, session).andReturn(Collections.emptyList());
        integrationBuilder.expectCall("getConflicts", newException, session).andReturn( Arrays.asList(appointment("1"), appointment("2"), appointment("3")));

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.addException(newException);
        msg.setMethod(ITipMethod.REQUEST);

        ITipAnalysis analysis =  new UpdateITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.CREATE, change.getType());
        assertTrue(change.isException());
        assertEquals(newException, change.getNewAppointment());
        assertEquals(12, change.getMasterAppointment().getObjectID());
        assertNotNull(change.getDiff());

        assertActions(analysis, ITipAction.ACCEPT_AND_IGNORE_CONFLICTS, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);


        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testUpdateChangingException() throws OXException {
        SimBuilder integrationBuilder = new SimBuilder();

        CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setRecurrenceType(CalendarObject.WEEKLY);
        appointment.setInterval(1);

        CalendarDataObject changedException = appointment("123-123-123-123");
        changedException.setTitle("Exception title");
        changedException.setRecurrenceDatePosition(new Date(12345));

        CalendarDataObject original = appointment.clone();
        original.setObjectID(12);

        CalendarDataObject originalException = changedException.clone();
        originalException.setTitle("Original Exception title");
        originalException.setStartDate(D("Tomorrow at 09:00"));
        originalException.setObjectID(13);

        CalendarDataObject otherException = changedException.clone();
        otherException.setObjectID(14);
        otherException.setRecurrenceDatePosition(new Date(5432210000L)); // Needs to be different

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        integrationBuilder.expectCall("getExceptions", original, session).andReturn(Arrays.asList(otherException, originalException));
        integrationBuilder.expectCall("getConflicts", appointment, session).andReturn(Collections.emptyList());

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.addException(changedException);
        msg.addException(otherException); // This one is unchanged
        msg.setMethod(ITipMethod.REQUEST);

        ITipAnalysis analysis =  new UpdateITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.UPDATE, change.getType());
        assertTrue(change.isException());
        assertEquals(changedException, change.getNewAppointment());
        assertEquals(13, change.getCurrentAppointment().getObjectID());
        assertEquals(12, change.getMasterAppointment().getObjectID());

        assertActions(analysis, ITipAction.ACCEPT, ITipAction.DECLINE,  ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);


        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testUpdateChangingExceptionWithoutRescheduling() throws OXException {
        SimBuilder integrationBuilder = new SimBuilder();

        CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setRecurrenceType(CalendarObject.WEEKLY);
        appointment.setInterval(1);

        CalendarDataObject changedException = appointment("123-123-123-123");
        changedException.setTitle("Exception title");
        changedException.setRecurrenceDatePosition(new Date(12345));

        CalendarDataObject original = appointment.clone();
        original.setObjectID(12);

        CalendarDataObject originalException = changedException.clone();
        originalException.setTitle("Original Exception title");
        originalException.setObjectID(13);

        CalendarDataObject otherException = changedException.clone();
        otherException.setObjectID(14);
        otherException.setRecurrenceDatePosition(new Date(5432210000L)); // Needs to be different

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        integrationBuilder.expectCall("getExceptions", original, session).andReturn(Arrays.asList(otherException, originalException));
        integrationBuilder.expectCall("getConflicts", appointment, session).andReturn(Collections.emptyList());

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.addException(changedException);
        msg.addException(otherException); // This one is unchanged
        msg.setMethod(ITipMethod.REQUEST);

        ITipAnalysis analysis =  new UpdateITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.UPDATE, change.getType());
        assertTrue(change.isException());
        assertEquals(changedException, change.getNewAppointment());
        assertEquals(13, change.getCurrentAppointment().getObjectID());
        assertEquals(12, change.getMasterAppointment().getObjectID());

        assertActions(analysis, ITipAction.ACCEPT, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER, ITipAction.UPDATE);


        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testUpdateChangingExceptionWithConflicts() throws OXException {
        SimBuilder integrationBuilder = new SimBuilder();

        CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setRecurrenceType(CalendarObject.WEEKLY);
        appointment.setInterval(1);

        CalendarDataObject changedException = appointment("123-123-123-123");
        changedException.setTitle("Exception title");
        changedException.setRecurrenceDatePosition(new Date(12345));

        CalendarDataObject original = appointment.clone();
        original.setObjectID(12);

        CalendarDataObject originalException = changedException.clone();
        originalException.setTitle("Original Exception title");
        originalException.setStartDate(D("Tomorrow at 09:00"));
        originalException.setObjectID(13);

        CalendarDataObject otherException = changedException.clone();
        otherException.setObjectID(14);
        otherException.setRecurrenceDatePosition(new Date(5432210000L)); // Needs to be different

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        integrationBuilder.expectCall("getExceptions", original, session).andReturn(Arrays.asList(otherException, originalException));
        integrationBuilder.expectCall("getConflicts", appointment, session).andReturn(Arrays.asList(appointment("1"), appointment("2"), appointment("3")));

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.addException(changedException);
        msg.addException(otherException); // This one is unchanged
        msg.setMethod(ITipMethod.REQUEST);

        ITipAnalysis analysis =  new UpdateITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.UPDATE, change.getType());
        assertTrue(change.isException());
        assertEquals(changedException, change.getNewAppointment());
        assertEquals(13, change.getCurrentAppointment().getObjectID());
        assertEquals(12, change.getMasterAppointment().getObjectID());

        assertActions(analysis, ITipAction.ACCEPT_AND_IGNORE_CONFLICTS, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);


        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testCreateDeleteException() {
        // TODO
    }

    @Test
    public void testDeletingChangeException() throws OXException {
        SimBuilder integrationBuilder = new SimBuilder();

        CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setRecurrenceType(CalendarObject.WEEKLY);
        appointment.setInterval(1);

        CalendarDataObject ex1 = appointment("123-123-123-123");
        ex1.setTitle("Exception title");
        ex1.setRecurrenceDatePosition(new Date(12345));

        CalendarDataObject original = appointment.clone();
        original.setObjectID(12);

        CalendarDataObject originalException = ex1.clone();
        originalException.setObjectID(13);

        CalendarDataObject deletedException = ex1.clone();
        deletedException.setObjectID(14);
        deletedException.setRecurrenceDatePosition(new Date(5432210000L));

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        integrationBuilder.expectCall("getExceptions", original, session).andReturn(Arrays.asList(deletedException, originalException));

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        // Build the message with the update, exception with id 14 is missing
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.addException(ex1);
        msg.setMethod(ITipMethod.REQUEST);

        ITipAnalysis analysis =  new UpdateITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.DELETE, change.getType());
        assertTrue(change.isException());

        assertEquals(deletedException, change.getDeletedAppointment());

        assertActions(analysis, ITipAction.DELETE);

        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testUpdateChangingParticipantState() {
        // Do this when diffing is implemented for participants
    }

    @Test
    public void testDelegation() {
        // TODO
    }



    // Irrelevant Conflicts

    @Test
    public void testAnAppointmentDoesntConflictWithItself() throws OXException {
        ITipAnalysis analysis = new ITipAnalysis();

        ITipChange change = new ITipChange();

        CalendarDataObject appointment = appointment("123-123-123-123");
        CalendarDataObject original = appointment.clone();
        original.setObjectID(12);

        change.setNewAppointment(appointment);
        change.setCurrentAppointment(original);
        change.setConflicts(new ArrayList<Appointment>(Arrays.asList((Appointment) original)));

        analysis.addChange(change);

        new UpdateITipAnalyzer(null, null).purgeConflicts(analysis);

        assertTrue("An appointment should never conflict with itself", change.getConflicts().isEmpty());

    }

    @Test
    public void testAnAppointmentDoesntConflictWithItsMaster() throws OXException {
        ITipAnalysis analysis = new ITipAnalysis();

        ITipChange change = new ITipChange();

        CalendarDataObject appointment = appointment("123-123-123-123");
        CalendarDataObject original = appointment.clone();
        original.setObjectID(12);

        change.setNewAppointment(appointment);
        change.setCurrentAppointment(original);
        change.setMaster(original);
        change.setConflicts(new ArrayList<Appointment>(Arrays.asList((Appointment) original)));

        analysis.addChange(change);

        new UpdateITipAnalyzer(null, null).purgeConflicts(analysis);

        assertTrue("An appointment should never conflict with itself", change.getConflicts().isEmpty());

    }

    @Test
    public void testAnAppointmentDoesntConflictIfItIsChangedToANonConflictingDate() throws OXException {
        ITipAnalysis analysis = new ITipAnalysis();

        ITipChange change = new ITipChange();

        // First the change with the conflict
        CalendarDataObject appointment = appointment("123-123-123-123");
        CalendarDataObject original = appointment.clone();
        original.setObjectID(12);

        CalendarDataObject conflict = appointment("123-123-123-123");
        conflict.setStartDate(appointment.getStartDate());
        conflict.setEndDate(appointment.getEndDate());
        conflict.setObjectID(23);

        change.setNewAppointment(appointment);
        change.setCurrentAppointment(original);
        change.setConflicts(new ArrayList<Appointment>(Arrays.asList((Appointment) conflict)));

        analysis.addChange(change);
        // Now the change to the conflict

        CalendarDataObject updateToConflict = conflict.clone();
        updateToConflict.setStartDate((new Date(1)));
        updateToConflict.setEndDate((new Date(2))); // Doesn't conflict with "appointment"

        change = new ITipChange();
        change.setNewAppointment(updateToConflict);
        change.setCurrentAppointment(conflict);

        analysis.addChange(change);

        new UpdateITipAnalyzer(null, null).purgeConflicts(analysis);

        assertTrue(
            "An appointment should not conflict if it is changed to an agreeable timeframe in the same update",
            analysis.getChanges().get(0).getConflicts().isEmpty());
    }

    @Test
    public void testAnExceptionDoesntConflictIfItIsDeleted() throws OXException {
        ITipAnalysis analysis = new ITipAnalysis();

        ITipChange change = new ITipChange();

        // First the change with the conflict
        CalendarDataObject appointment = appointment("123-123-123-123");
        CalendarDataObject original = appointment.clone();
        original.setObjectID(12);

        CalendarDataObject conflict = appointment("123-123-123-123");
        conflict.setStartDate(appointment.getStartDate());
        conflict.setEndDate(appointment.getEndDate());
        conflict.setObjectID(23);

        change.setNewAppointment(appointment);
        change.setCurrentAppointment(original);
        change.setConflicts(new ArrayList<Appointment>(Arrays.asList((Appointment) conflict)));

        analysis.addChange(change);
        // Now the deletion of the conflict


        change = new ITipChange();
        change.setDeleted(conflict);
        change.setType(ITipChange.Type.DELETE);
        change.setException(true);

        analysis.addChange(change);

        new UpdateITipAnalyzer(null, null).purgeConflicts(analysis);

        assertTrue(
            "An appointment should not conflict if it is deleted in the same update",
            analysis.getChanges().get(0).getConflicts().isEmpty());
    }

    @Test
    public void testAConflictCanStillBeAConflictEvenIfItWasChanged() throws OXException {
        ITipAnalysis analysis = new ITipAnalysis();

        ITipChange change = new ITipChange();

        // First the change with the conflict
        CalendarDataObject appointment = appointment("123-123-123-123");
        CalendarDataObject original = appointment.clone();
        original.setObjectID(12);

        CalendarDataObject conflict = appointment("123-123-123-123");
        conflict.setStartDate(appointment.getStartDate());
        conflict.setEndDate(appointment.getEndDate());
        conflict.setObjectID(23);

        change.setNewAppointment(appointment);
        change.setCurrentAppointment(original);
        change.setConflicts(new ArrayList<Appointment>(Arrays.asList((Appointment) conflict)));

        analysis.addChange(change);
        // Now the change to the conflict

        CalendarDataObject updateToConflict = conflict.clone();
        updateToConflict.setStartDate((new Date(1)));
        updateToConflict.setEndDate(appointment.getEndDate()); // Still conflicts with the "appointment", since the enddate still matches.

        change = new ITipChange();
        change.setNewAppointment(updateToConflict);
        change.setCurrentAppointment(conflict);

        analysis.addChange(change);

        new UpdateITipAnalyzer(null, null).purgeConflicts(analysis);

        assertFalse(
            "An appointment should still conflict if it still overlaps",
            analysis.getChanges().get(0).getConflicts().isEmpty());
    }

    @Test
    public void testCounterSuggestsChangingOrDeclining() throws OXException {
     // Simulate ITipIntegration with a matching appointment
        SimBuilder integrationBuilder = new SimBuilder();

        CalendarDataObject appointment = appointment("123-123-123-123");
        CalendarDataObject original = appointment.clone();
        original.setStartDate(D("tomorrow at 09:00"));
        original.setEndDate(D("tomorrow at 09:30"));
        original.setObjectID(12);

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        integrationBuilder.expectCall("getExceptions", original, session).andReturn(Collections.emptyList());
        integrationBuilder.expectCall("getConflicts", appointment, session).andReturn(Collections.emptyList());

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.setMethod(ITipMethod.COUNTER);

        ITipAnalysis analysis =  new UpdateITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();

        assertEquals(1, changes.size());

        ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.UPDATE, change.getType());
        assertEquals("123-123-123-123", change.getNewAppointment().getUid());
        assertEquals(12, change.getCurrentAppointment().getObjectID());

        assertActions(analysis, ITipAction.UPDATE, ITipAction.DECLINECOUNTER);


        integrationBuilder.assertAllWereCalled();
    }

    public void testPartyCrasherViaCounter() {
        // TODO: Participant Differ
    }

    public void testMultipleUpdatesWithConflictingActions() {
        // TODO
    }

    // Error cases
    @Test
    public void testRejectOlderChange() throws OXException {
        // Simulate ITipIntegration with a matching appointment
        SimBuilder integrationBuilder = new SimBuilder();

        CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setSequence(99);

        CalendarDataObject original = appointment.clone();
        original.setStartDate(D("tomorrow at 09:00"));
        original.setEndDate(D("tomorrow at 09:30"));
        original.setObjectID(12);
        original.setSequence(100);

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        integrationBuilder.expectCall("getExceptions", original, session).andReturn(Collections.emptyList());
        integrationBuilder.expectCall("getConflicts", appointment, session).andReturn(Collections.emptyList());

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.setMethod(ITipMethod.REQUEST);

        ITipAnalysis analysis =  new UpdateITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();
        assertEquals(1, changes.size());

        List<ITipAnnotation> annotations = analysis.getAnnotations();
        assertEquals(1, annotations.size());

        ITipAnnotation error = annotations.get(0);
        assertEquals("This is an update to an appointment that has been changed in the meantime. Best ignore it.", error.getMessage());

        assertActions(analysis, ITipAction.IGNORE);
    }

    @Test
    public void testCounterUnknown() throws OXException {
     // Simulate ITipIntegration with a matching appointment
        SimBuilder integrationBuilder = new SimBuilder();

        CalendarDataObject appointment = appointment("123-123-123-123");


        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(null);

        ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        // Build the message with the update
        ITipMessage msg = new ITipMessage();
        msg.setAppointment(appointment);
        msg.setMethod(ITipMethod.COUNTER);

        ITipAnalysis analysis =  new UpdateITipAnalyzer(utility, null).analyze(msg, null, null, session);

        List<ITipChange> changes = analysis.getChanges();
        assertEquals(0, changes.size());

        List<ITipAnnotation> annotations = analysis.getAnnotations();
        assertEquals(1, annotations.size());

        ITipAnnotation error = annotations.get(0);
        assertEquals("An attendee wants to change an appointment that could not be found. Probably the appointment was deleted. Best ignore it.", error.getMessage());

        assertActions(analysis, ITipAction.IGNORE);

        integrationBuilder.assertAllWereCalled();
    }

}
