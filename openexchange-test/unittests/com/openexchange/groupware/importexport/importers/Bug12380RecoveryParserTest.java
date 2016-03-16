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

package com.openexchange.groupware.importexport.importers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.SimICalParser;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.server.services.ServerServiceRegistry;
import junit.framework.TestCase;

import static com.openexchange.groupware.calendar.TimeTools.D;


/**
 * {@link Bug12380RecoveryParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class Bug12380RecoveryParserTest extends TestCase {

    private SimICalParser parser;
    private ExtraneousSeriesMasterRecoveryParser bugParser;

    @Override
    public void setUp() {
        parser = new SimICalParser();
        ServerServiceRegistry registry = ServerServiceRegistry.getInstance();
        bugParser = new ExtraneousSeriesMasterRecoveryParser(parser, registry);
        if(null == registry.getService(CalendarCollectionService.class)) {
            registry.addService(CalendarCollectionService.class, new CalendarCollection());
        }
    }

    public void testMultiplexes() throws ConversionError {
        CalendarDataObject appointment = new CalendarDataObject();
        appointment.setTitle("I start on a different date than my series settings suggest");
        appointment.setStartDate(D("Monday at 8 am"));
        appointment.setEndDate(D("Monday at 10 am"));
        appointment.setRecurrenceType(CalendarDataObject.WEEKLY);
        appointment.setInterval(1);
        appointment.setDays(CalendarDataObject.TUESDAY);

        parser.setAppointments(Arrays.asList(appointment));

        List<CalendarDataObject> parsed = bugParser.parseAppointments((String)null, null, null, null, null);

        assertEquals("Expected appointment to be split into two", 2, parsed.size());
        CalendarDataObject app1 = parsed.get(0);
        CalendarDataObject app2 = parsed.get(1);

        assertEquals("Start date was not as expected", appointment.getStartDate(), app1.getStartDate());
        assertEquals("Start date was not as expected", appointment.getStartDate(), app2.getStartDate());

        assertEquals("End date was not as expected", appointment.getEndDate(), app1.getEndDate());
        assertEquals("End date was not as expected", appointment.getEndDate(), app2.getEndDate());

        assertEquals("Title was not as expected", appointment.getTitle(), app1.getTitle());
        assertEquals("Title was not as expected", appointment.getTitle(), app2.getTitle());

        assertTrue("One of app1 and app2 has to be a recurrence, the other one not.", (app1.getRecurrenceType() == CalendarDataObject.NO_RECURRENCE) ^ (app2.getRecurrenceType() == CalendarDataObject.NO_RECURRENCE));

    }

    public void testDoesntMultiplexWhenSeriesMasterIsOnStart() throws ConversionError {
        CalendarDataObject appointment = new CalendarDataObject();
        appointment.setTitle("I start on a date inside my series parameters");
        appointment.setStartDate(D("Tuesday at 8 am"));
        appointment.setEndDate(D("Tuesday at 10 am"));
        appointment.setRecurrenceType(CalendarDataObject.WEEKLY);
        appointment.setInterval(1);
        appointment.setDays(CalendarDataObject.TUESDAY);

        parser.setAppointments(Arrays.asList(appointment));

        List<CalendarDataObject> parsed = bugParser.parseAppointments((String)null, null, null, null, null);

        assertEquals("Expected appointment to be left as one", 1, parsed.size());

    }

    public void testDoesntMultiplexWhenSeriesMasterIsOccurrence() throws ConversionError {
        CalendarDataObject appointment = new CalendarDataObject();
        appointment.setTitle("I start on a date inside my series parameters");
        appointment.setStartDate(new Date(D("Tuesday at 8 am ").getTime()+7*24*3600));
        appointment.setEndDate(new Date(D("Tuesday at 10 am ").getTime()+7*24*3600));
        appointment.setRecurrenceType(CalendarDataObject.WEEKLY);
        appointment.setInterval(1);
        appointment.setDays(CalendarDataObject.TUESDAY);

        parser.setAppointments(Arrays.asList(appointment));

        List<CalendarDataObject> parsed = bugParser.parseAppointments((String)null, null, null, null, null);

        assertEquals("Expected appointment to be left as one", 1, parsed.size());
    }
}
