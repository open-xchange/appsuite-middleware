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

import static com.openexchange.time.TimeTools.D;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import com.openexchange.calendar.itip.ITipAction;
import com.openexchange.calendar.itip.ITipAnalysis;
import com.openexchange.calendar.itip.ITipAnnotation;
import com.openexchange.calendar.itip.ITipChange;
import com.openexchange.calendar.itip.ITipChange.Type;
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.calendar.itip.Messages;
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.sim.SimBuilder;

/**
 * {@link AddITipAnalyzerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AddITipAnalyzerTest extends AbstractITipAnalyzerTest {
    @Test
    public void testMethod() {
        final List<ITipMethod> methods = new AddITipAnalyzer(null, null).getMethods();
        assertEquals(Arrays.asList(ITipMethod.ADD), methods);
    }

    @Test
    public void testAddChangeException() throws OXException {

        final CalendarDataObject newException = appointment("123-123-123-123");
        newException.setRecurrenceDatePosition(new Date(12345));
        newException.setStartDate(D("Tomorrow at 09:00"));

        final ITipMessage message = new ITipMessage();
        message.addException(newException);

        final CalendarDataObject existingSeries = appointment("123-123-123-123");
        existingSeries.setRecurrenceType(CalendarObject.WEEKLY);
        existingSeries.setInterval(1);
        existingSeries.setObjectID(12);

        final SimBuilder integrationBuilder = new SimBuilder();
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(existingSeries);
        integrationBuilder.expectCall("getExceptions", existingSeries, session).andReturn(Collections.emptyList());
        integrationBuilder.expectCall("getConflicts", newException, session).andReturn(Collections.emptyList());


        final ITipAnalysis analysis = new AddITipAnalyzer(integrationBuilder.getSim(ITipIntegrationUtility.class), null).analyze(message, null, null, session);

        final List<ITipChange> changes = analysis.getChanges();
        assertEquals(1, changes.size());

        final ITipChange change = changes.get(0);

        assertEquals(Type.CREATE, change.getType());
        assertTrue(change.isException());
        assertEquals("123-123-123-123", change.getNewAppointment().getUid());
        assertEquals(new Date(12345), change.getNewAppointment().getRecurrenceDatePosition());
        assertEquals(12, change.getMasterAppointment().getObjectID());
        assertNotNull(change.getDiff());

        assertActions(analysis, ITipAction.ACCEPT, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.COUNTER, ITipAction.DELEGATE);

        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testAddChangeExceptionWithoutRescheduling() throws OXException {
        final CalendarDataObject newException = appointment("123-123-123-123");
        newException.setRecurrenceDatePosition(new Date(12345));

        final ITipMessage message = new ITipMessage();
        message.addException(newException);

        final CalendarDataObject existingSeries = appointment("123-123-123-123");
        existingSeries.setRecurrenceType(CalendarObject.WEEKLY);
        existingSeries.setInterval(1);
        existingSeries.setObjectID(12);

        final SimBuilder integrationBuilder = new SimBuilder();
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(existingSeries);
        integrationBuilder.expectCall("getExceptions", existingSeries, session).andReturn(Collections.emptyList());
        integrationBuilder.expectCall("getConflicts", newException, session).andReturn(Collections.emptyList());


        final ITipAnalysis analysis = new AddITipAnalyzer(integrationBuilder.getSim(ITipIntegrationUtility.class), null).analyze(message, null, null, session);

        final List<ITipChange> changes = analysis.getChanges();
        assertEquals(1, changes.size());

        final ITipChange change = changes.get(0);

        assertEquals(Type.CREATE, change.getType());
        assertTrue(change.isException());
        assertEquals("123-123-123-123", change.getNewAppointment().getUid());
        assertEquals(new Date(12345), change.getNewAppointment().getRecurrenceDatePosition());
        assertEquals(12, change.getMasterAppointment().getObjectID());
        assertNotNull(change.getDiff());

        assertActions(analysis, ITipAction.ACCEPT, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);


        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testAddChangeExceptionWithConflicts() throws OXException {
        final CalendarDataObject newException = appointment("123-123-123-123");
        newException.setRecurrenceDatePosition(new Date(12345));
        newException.setStartDate(D("Tomorrow at 09:00"));

        final ITipMessage message = new ITipMessage();
        message.addException(newException);

        final CalendarDataObject existingSeries = appointment("123-123-123-123");
        existingSeries.setRecurrenceType(CalendarObject.WEEKLY);
        existingSeries.setInterval(1);
        existingSeries.setObjectID(12);

        final SimBuilder integrationBuilder = new SimBuilder();
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(existingSeries);
        integrationBuilder.expectCall("getExceptions", existingSeries, session).andReturn(Collections.emptyList());
        integrationBuilder.expectCall("getConflicts", newException, session).andReturn(Arrays.asList(appointment("1"), appointment("2")));

        final ITipAnalysis analysis = new AddITipAnalyzer(integrationBuilder.getSim(ITipIntegrationUtility.class), null).analyze(message, null, null, session);

        final List<ITipChange> changes = analysis.getChanges();
        assertEquals(1, changes.size());

        final ITipChange change = changes.get(0);

        assertEquals(Type.CREATE, change.getType());
        assertTrue(change.isException());
        assertEquals("123-123-123-123", change.getNewAppointment().getUid());
        assertEquals(new Date(12345), change.getNewAppointment().getRecurrenceDatePosition());
        assertEquals(12, change.getMasterAppointment().getObjectID());

        assertActions(analysis, ITipAction.ACCEPT_AND_IGNORE_CONFLICTS, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.COUNTER, ITipAction.DELEGATE);

        integrationBuilder.assertAllWereCalled();
    }

    // Error Cases
    @Test
    public void testAddChangeExceptionToNonexistingAppointment() throws OXException {
        final CalendarDataObject newException = appointment("123-123-123-123");
        newException.setRecurrenceDatePosition(new Date(12345));

        final ITipMessage message = new ITipMessage();
        message.addException(newException);

        final SimBuilder integrationBuilder = new SimBuilder();
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(null);

        final ITipAnalysis analysis = new AddITipAnalyzer(integrationBuilder.getSim(ITipIntegrationUtility.class), null).analyze(message, null, null, session);

        final List<ITipChange> changes = analysis.getChanges();
        assertEquals(0, changes.size());

        final List<ITipAnnotation> annotations = analysis.getAnnotations();
        assertEquals(1, annotations.size());

        final ITipAnnotation error = annotations.get(0);
        assertEquals(Messages.ADD_TO_UNKNOWN, error.getMessage());

        assertActions(analysis, ITipAction.REFRESH);

        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testAddChangeWhereAlreadyAChangeExists() throws OXException {
        final CalendarDataObject newException = appointment("123-123-123-123");
        newException.setRecurrenceDatePosition(new Date(12345));

        final ITipMessage message = new ITipMessage();
        message.addException(newException);

        final CalendarDataObject existingSeries = appointment("123-123-123-123");
        existingSeries.setRecurrenceType(CalendarObject.WEEKLY);
        existingSeries.setInterval(1);
        existingSeries.setObjectID(12);

        final CalendarDataObject existingException = newException.clone();
        existingException.setObjectID(13);

        final SimBuilder integrationBuilder = new SimBuilder();
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(existingSeries);
        integrationBuilder.expectCall("getExceptions", existingSeries, session).andReturn(new ArrayList<CalendarDataObject>(Arrays.asList(existingException)));
        integrationBuilder.expectCall("getConflicts", newException, session).andReturn(Collections.emptyList());

        final ITipAnalysis analysis = new AddITipAnalyzer(integrationBuilder.getSim(ITipIntegrationUtility.class), null).analyze(message, null, null, session);

        final List<ITipChange> changes = analysis.getChanges();
        assertEquals(1, changes.size());

        assertEquals(13, changes.get(0).getCurrentAppointment().getObjectID());


        assertActions(analysis, ITipAction.IGNORE, ITipAction.ACCEPT_AND_REPLACE);

        integrationBuilder.assertAllWereCalled();
    }
}
