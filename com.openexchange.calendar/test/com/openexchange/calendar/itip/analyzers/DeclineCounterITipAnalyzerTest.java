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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import com.openexchange.calendar.itip.ITipAction;
import com.openexchange.calendar.itip.ITipAnalysis;
import com.openexchange.calendar.itip.ITipAnnotation;
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.calendar.itip.analyzers.DeclineCounterITipAnalyzer;
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.sim.SimBuilder;


/**
 * {@link DeclineCounterITipAnalyzerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DeclineCounterITipAnalyzerTest extends AbstractITipAnalyzerTest {
    @Test
    public void testMethod() {
        List<ITipMethod> methods = new DeclineCounterITipAnalyzer(null, null).getMethods();
        assertEquals(Arrays.asList(ITipMethod.DECLINECOUNTER), methods);
    }

    @Test
    public void testDeclineCounter() throws OXException {

        CalendarDataObject appointment = appointment("123-123-123-123");
        CalendarDataObject declinedFor = appointment("123-123-123-123");
        declinedFor.setObjectID(12);

        ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.DECLINECOUNTER);
        message.setAppointment(appointment);

        SimBuilder integrationBuilder = new SimBuilder();
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(declinedFor);

        ITipAnalysis analysis = new DeclineCounterITipAnalyzer(integrationBuilder.getSim(ITipIntegrationUtility.class), null).analyze(message, null, null, session);

        assertEquals(12, analysis.getAnnotations().get(0).getAppointment().getObjectID());
        assertActions(analysis, ITipAction.DECLINE, ITipAction.REFRESH);
    }

    @Test
    public void testDeclineCounterOfAnException() throws OXException {
        CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setRecurrenceDatePosition(new Date(12345));

        CalendarDataObject declinedForMaster = appointment("123-123-123-123");
        declinedForMaster.setObjectID(12);

        CalendarDataObject declinedForException = appointment("123-123-123-123");
        declinedForException.setObjectID(13);
        declinedForException.setRecurrenceDatePosition(new Date(12345));

        ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.DECLINECOUNTER);
        message.addException(appointment);

        SimBuilder integrationBuilder = new SimBuilder();
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(declinedForMaster);
        integrationBuilder.expectCall("getExceptions", declinedForMaster, session).andReturn(new ArrayList<CalendarDataObject>(Arrays.asList(declinedForException)));


        ITipAnalysis analysis = new DeclineCounterITipAnalyzer(integrationBuilder.getSim(ITipIntegrationUtility.class), null).analyze(message, null, null, session);

        assertEquals(13, analysis.getAnnotations().get(0).getAppointment().getObjectID());
        assertActions(analysis, ITipAction.DECLINE, ITipAction.REFRESH);
    }

    // Error Cases
    @Test
    public void testDeclineCounterForNonExistingAppointment() throws OXException {

        CalendarDataObject appointment = appointment("123-123-123-123");

        ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.DECLINECOUNTER);
        message.setAppointment(appointment);

        SimBuilder integrationBuilder = new SimBuilder();
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(null);

        ITipAnalysis analysis = new DeclineCounterITipAnalyzer(integrationBuilder.getSim(ITipIntegrationUtility.class), null).analyze(message, null, null, session);

        List<ITipAnnotation> annotations = analysis.getAnnotations();
        assertEquals(1, annotations.size());
        assertEquals("The organizer declined your counter proposal for an appointment that could not be found. It was probably deleted in the meantime.", annotations.get(0).getMessage());

        assertActions(analysis, ITipAction.IGNORE, ITipAction.REFRESH);
    }

    @Test
    public void testDeclineCounterForNonExistingException() throws OXException {
        CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setRecurrenceDatePosition(new Date(12345));

        CalendarDataObject declinedForMaster = appointment("123-123-123-123");
        declinedForMaster.setObjectID(12);

        CalendarDataObject declinedForException = appointment("123-123-123-123");
        declinedForException.setObjectID(13);
        declinedForException.setRecurrenceDatePosition(new Date(54321000000L));

        ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.DECLINECOUNTER);
        message.addException(appointment);

        SimBuilder integrationBuilder = new SimBuilder();
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(declinedForMaster);
        integrationBuilder.expectCall("getExceptions", declinedForMaster, session).andReturn(new ArrayList<CalendarDataObject>(Arrays.asList(declinedForException)));


        ITipAnalysis analysis = new DeclineCounterITipAnalyzer(integrationBuilder.getSim(ITipIntegrationUtility.class), null).analyze(message, null, null, session);

        List<ITipAnnotation> annotations = analysis.getAnnotations();
        assertEquals(1, annotations.size());
        assertEquals("The organizer declined your counter proposal for an appointment that could not be found. It was probably deleted in the meantime.", annotations.get(0).getMessage());

        assertActions(analysis, ITipAction.IGNORE, ITipAction.REFRESH);
    }

    @Test
    public void testIrrelevantSequenceNumber() throws OXException {
        CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setSequence(1);
        CalendarDataObject declinedFor = appointment("123-123-123-123");
        declinedFor.setSequence(2);

        ITipMessage message = new ITipMessage();
        message.setMethod(ITipMethod.DECLINECOUNTER);
        message.setAppointment(appointment);

        SimBuilder integrationBuilder = new SimBuilder();
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(declinedFor);

        ITipAnalysis analysis = new DeclineCounterITipAnalyzer(integrationBuilder.getSim(ITipIntegrationUtility.class), null).analyze(message, null, null, session);

        List<ITipAnnotation> annotations = analysis.getAnnotations();
        assertEquals(1, annotations.size());
        assertEquals("This is an update to an appointment that has been changed in the meantime. Best ignore it.", annotations.get(0).getMessage());

        assertActions(analysis, ITipAction.IGNORE);
    }


}
