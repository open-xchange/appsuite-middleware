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
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.sim.SimBuilder;


/**
 * {@link RefreshITipAnalyzerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RefreshITipAnalyzerTest extends AbstractITipAnalyzerTest {
    @Test
    public void testMethod() {
        final List<ITipMethod> methods = new RefreshITipAnalyzer(null, null).getMethods();
        assertEquals(Arrays.asList(ITipMethod.REFRESH), methods);
    }

    @Test
    public void testRefresh() throws OXException {
        final ITipMessage message = new ITipMessage();
        message.setAppointment(appointment("123-123-123-123"));

        final CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setObjectID(12);

        final SimBuilder integrationBuilder = new SimBuilder();
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(appointment);

        final ITipAnalysis analysis = new RefreshITipAnalyzer(integrationBuilder.getSim(ITipIntegrationUtility.class), null).analyze(message, null, null, session);

        final List<ITipAnnotation> annotations = analysis.getAnnotations();
        assertEquals(1, annotations.size());
        assertEquals(12, annotations.get(0).getAppointment().getObjectID());

        assertActions(analysis, ITipAction.SEND_APPOINTMENT);
    }

    @Test
    public void testRefreshException() throws OXException {
        final ITipMessage message = new ITipMessage();
        final CalendarDataObject requestedAppointment = appointment("123-123-123-123");
        requestedAppointment.setRecurrenceDatePosition(new Date(12345));
        message.addException(requestedAppointment);

        final CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setObjectID(12);

        final CalendarDataObject exception = appointment("123-123-123-123");
        exception.setObjectID(13);
        exception.setRecurrenceDatePosition(new Date(12345));

        final SimBuilder integrationBuilder = new SimBuilder();
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(appointment);
        integrationBuilder.expectCall("getExceptions",appointment, session).andReturn(new ArrayList<CalendarDataObject>(Arrays.asList(exception)));

        final ITipAnalysis analysis = new RefreshITipAnalyzer(integrationBuilder.getSim(ITipIntegrationUtility.class), null).analyze(message, null, null, session);

        final List<ITipAnnotation> annotations = analysis.getAnnotations();
        assertEquals(1, annotations.size());
        assertEquals(13, annotations.get(0).getAppointment().getObjectID());

        assertActions(analysis, ITipAction.SEND_APPOINTMENT);
    }

    @Test
    public void testRefreshUnknown() throws OXException {
        final ITipMessage message = new ITipMessage();
        message.setAppointment(appointment("123-123-123-123"));


        final SimBuilder integrationBuilder = new SimBuilder();
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(null);

        final ITipAnalysis analysis = new RefreshITipAnalyzer(integrationBuilder.getSim(ITipIntegrationUtility.class), null).analyze(message, null, null, session);

        final List<ITipAnnotation> annotations = analysis.getAnnotations();
        assertEquals(1, annotations.size());
        assertEquals("An attendee wants to be brought up to date about an appointment that could not be found. It was probably deleted at some point. Best ignore this message.", annotations.get(0).getMessage());

        assertActions(analysis, ITipAction.IGNORE);
    }

    @Test
    public void testRefreshUnknownException() throws OXException {
        final ITipMessage message = new ITipMessage();
        final CalendarDataObject requestedAppointment = appointment("123-123-123-123");
        requestedAppointment.setRecurrenceDatePosition(new Date(12345));
        message.addException(requestedAppointment);

        final CalendarDataObject appointment = appointment("123-123-123-123");
        appointment.setObjectID(12);

        final CalendarDataObject exception = appointment("123-123-123-123");
        exception.setObjectID(13);
        exception.setRecurrenceDatePosition(new Date(5432100000L));

        final SimBuilder integrationBuilder = new SimBuilder();
        integrationBuilder.expectCall("resolveUid", "123-123-123-123", session).andReturn(appointment);
        integrationBuilder.expectCall("getExceptions",appointment, session).andReturn(new ArrayList<CalendarDataObject>(Arrays.asList(exception)));

        final ITipAnalysis analysis = new RefreshITipAnalyzer(integrationBuilder.getSim(ITipIntegrationUtility.class), null).analyze(message, null, null, session);

        final List<ITipAnnotation> annotations = analysis.getAnnotations();
        assertEquals(1, annotations.size());
        assertEquals("An attendee wants to be brought up to date about an appointment that could not be found. It was probably deleted at some point. Best ignore this message.", annotations.get(0).getMessage());

        assertActions(analysis, ITipAction.IGNORE);
    }


}
