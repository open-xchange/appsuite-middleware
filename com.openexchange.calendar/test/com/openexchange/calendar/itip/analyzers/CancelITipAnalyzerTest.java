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
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.sim.SimBuilder;

/**
 * {@link CancelITipAnalyzerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CancelITipAnalyzerTest extends AbstractITipAnalyzerTest{

    @Test
    public void testMethod() {
        final List<ITipMethod> methods = new CancelITipAnalyzer(null, null).getMethods();
        assertEquals(Arrays.asList(ITipMethod.CANCEL), methods);
    }

    @Test
    public void testCancel() throws OXException {
        final CalendarDataObject appointment = appointment("123-123-123-123");

        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.CANCEL);
        message.setAppointment(appointment);

        final SimBuilder integrationBuilder = new SimBuilder();
        final CalendarDataObject original = appointment("123-123-123-123");
        original.setObjectID(12);

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        final ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        final ITipAnalysis analysis = new CancelITipAnalyzer(utility, null).analyze(message, null, null, session);

        final List<ITipChange> changes = analysis.getChanges();
        assertEquals(1, changes.size());

        final ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.DELETE, change.getType());
        assertEquals(12, change.getDeletedAppointment().getObjectID());

        assertActions(analysis, ITipAction.DELETE);

        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testCancelChangeException() throws OXException {
        final CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setRecurrenceDatePosition(new Date(12345));
        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.CANCEL);
        message.setAppointment(appointment);

        final SimBuilder integrationBuilder = new SimBuilder();
        final CalendarDataObject original = appointment("123-123-123-123");
        original.setObjectID(12);

        final CalendarDataObject exception = appointment("123-123-123-123");
        exception.setObjectID(23);
        exception.setRecurrenceDatePosition(new Date(12345));

        final CalendarDataObject otherException = appointment("123-123-123-123");
        otherException.setObjectID(24);
        otherException.setRecurrenceDatePosition(new Date(54321000000L));

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        integrationBuilder.expectCall("getExceptions", original, session).andReturn(new ArrayList(Arrays.asList(otherException, exception)));

        final ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        final ITipAnalysis analysis = new CancelITipAnalyzer(utility, null).analyze(message, null, null, session);

        final List<ITipChange> changes = analysis.getChanges();
        assertEquals(1, changes.size());

        final ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.DELETE, change.getType());
        assertTrue(change.isException());
        assertEquals(23, change.getDeletedAppointment().getObjectID());

        assertActions(analysis, ITipAction.DELETE);


        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testCreateDeleteException() throws OXException {
        final CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setRecurrenceDatePosition(new Date(12345));
        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.CANCEL);
        message.setAppointment(appointment);

        final SimBuilder integrationBuilder = new SimBuilder();
        final CalendarDataObject original = appointment("123-123-123-123");
        original.setObjectID(12);

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(original);
        integrationBuilder.expectCall("getExceptions", original, session).andReturn(Collections.emptyList());

        final ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        final ITipAnalysis analysis = new CancelITipAnalyzer(utility, null).analyze(message, null, null, session);

        final List<ITipChange> changes = analysis.getChanges();
        assertEquals(1, changes.size());

        final ITipChange change = changes.get(0);

        assertEquals(ITipChange.Type.CREATE_DELETE_EXCEPTION, change.getType());
        assertTrue(change.isException());
        assertEquals("123-123-123-123", change.getDeletedAppointment().getUid());
        assertEquals(new Date(12345), change.getDeletedAppointment().getRecurrenceDatePosition());
        assertEquals(12, change.getCurrentAppointment().getObjectID());

        assertActions(analysis, ITipAction.DELETE);


        integrationBuilder.assertAllWereCalled();
    }

    public void testCancelRange() {
        // TODO
    }

    // Error Cases

    @Test
    public void testCancelDeletedAppointment() throws OXException {
        final CalendarDataObject appointment = appointment("123-123-123-123");

        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.CANCEL);
        message.setAppointment(appointment);

        final SimBuilder integrationBuilder = new SimBuilder();

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(null);
        final ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        final ITipAnalysis analysis = new CancelITipAnalyzer(utility, null).analyze(message, null, null, session);

        final List<ITipChange> changes = analysis.getChanges();
        assertEquals(0, changes.size());

        final List<ITipAnnotation> annotations = analysis.getAnnotations();
        assertEquals(1, annotations.size());

        final ITipAnnotation error = annotations.get(0);
        assertEquals("The organizer would like to cancel an appointment that could not be found.", error.getMessage());

        assertActions(analysis, ITipAction.IGNORE);


        integrationBuilder.assertAllWereCalled();
    }

    @Test
    public void testCancelUnknownException() throws OXException {
        final CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setRecurrenceDatePosition(new Date(12345));

        final ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.CANCEL);
        message.setAppointment(appointment);

        final SimBuilder integrationBuilder = new SimBuilder();

        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(null);
        final ITipIntegrationUtility utility = integrationBuilder.getSim(ITipIntegrationUtility.class);

        final ITipAnalysis analysis = new CancelITipAnalyzer(utility, null).analyze(message, null, null, session);

        final List<ITipChange> changes = analysis.getChanges();
        assertEquals(0, changes.size());

        final List<ITipAnnotation> annotations = analysis.getAnnotations();
        assertEquals(1, annotations.size());

        final ITipAnnotation error = annotations.get(0);
        assertEquals("The organizer would like to cancel an appointment that could not be found.", error.getMessage());

        assertActions(analysis, ITipAction.IGNORE);


        integrationBuilder.assertAllWereCalled();

    }

    //TODO: Only cancelled for some attendees

}
