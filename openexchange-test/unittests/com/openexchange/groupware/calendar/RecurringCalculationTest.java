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
package com.openexchange.groupware.calendar;

import static com.openexchange.groupware.calendar.tools.CommonAppointments.D;
import static com.openexchange.groupware.calendar.tools.CommonAppointments.dateString;
import static com.openexchange.groupware.calendar.tools.CommonAppointments.recalculate;
import java.util.Date;
import java.util.TimeZone;
import junit.framework.TestCase;
import com.openexchange.calendar.recurrence.RecurringCalculation;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class RecurringCalculationTest extends TestCase {

    // Bug 11439
    public void testShouldCalculateInTimeZone() throws OXException {

        final TimeZone utc = TimeZone.getTimeZone("UTC");
        final TimeZone ny = TimeZone.getTimeZone("America/New York");

        final RecurringCalculation calc = new RecurringCalculation(Appointment.YEARLY,1,0);

        final Date start = recalculate(D("07/02/1981 19:00"), utc, ny); // Will be ignored
        final Date end = recalculate(D("09/02/1981 23:00"), utc, ny);   // Will be ignored

        calc.setStartAndEndTime(start.getTime(), end.getTime());
        calc.setOccurrence(3);
        calc.setMonth(1);
        calc.setDayInMonth(5);

        final RecurringResultInterface recurringResult = calc.calculateRecurrence().getRecurringResult(2);

        final Date startExpected = recalculate(D("05/02/1983 19:00"), utc, ny);
        final Date endExpected = recalculate(D("05/02/1983 23:00"), utc, ny);


        assertEquals(dateString(recurringResult.getStart(), ny), startExpected.getTime(), recurringResult.getStart());
        assertEquals(dateString(recurringResult.getEnd(), ny), endExpected.getTime(), recurringResult.getEnd());
    }

    // Bug 10497

    public void testWorkweekRelativeRecurrence() throws OXException {
        final Date start = D("05/12/2007 10:00");
        final Date end = D("05/12/2007 12:00");

        final RecurringCalculation calc = new RecurringCalculation(Appointment.MONTHLY,1,0);
        calc.setStartAndEndTime(start.getTime(), end.getTime());
        calc.setDays(Appointment.WEEKDAY);
        calc.setDayInMonth(3);
        calc.setOccurrence(5);

        final RecurringResultsInterface results = calc.calculateRecurrence();

        final Date[] days = {
                D("05/12/2007 10:00"),
                D("03/01/2008 10:00"),
                D("05/02/2008 10:00"),
                D("05/03/2008 10:00"),
                D("03/04/2008 10:00")
        };

        for(int i = 0; i < 5; i++) {
            final long expected = days[i].getTime();
            final long actual = results.getRecurringResult(i).getStart();
            assertEquals(new Date(expected)+" expected. Was: "+new Date(actual), expected, actual);
        }
    }

    // Bug 11655

    public void testWeekendRelativeRecurrence() throws OXException {
        final Date start = D("02/12/2007 10:00");
        final Date end = D("02/12/2007 12:00");

        final RecurringCalculation calc = new RecurringCalculation(Appointment.MONTHLY,1,0);
        calc.setStartAndEndTime(start.getTime(), end.getTime());
        calc.setDays(Appointment.WEEKENDDAY);
        calc.setDayInMonth(2);
        calc.setOccurrence(5);

        final RecurringResultsInterface results = calc.calculateRecurrence();

        final Date[] days = {
                D("02/12/2007 10:00"),
                D("06/01/2008 10:00"),
                D("03/02/2008 10:00"),
                D("02/03/2008 10:00"),
                D("06/04/2008 10:00")
        };

        for(int i = 0; i < 5; i++) {
            final long expected = days[i].getTime();
            final long actual = results.getRecurringResult(i).getStart();
            assertEquals(new Date(expected)+" expected. Was: "+new Date(actual), expected, actual);
        }
    }

    // Bug 11730
    public void testRecurrencesGoOnUntil99YearsInTheFuture() throws OXException {
        final RecurringCalculation calc = new RecurringCalculation(Appointment.YEARLY,1,0);
        final Date start = D("05/11/1900 10:00");
        calc.setStartAndEndTime(start.getTime(), D("05/11/1900 12:00").getTime());
        calc.setDayInMonth(11);
        calc.setMonth(4);

        final RecurringResultsInterface results = calc.calculateRecurrence();

        final long threshold = start.getTime() + 40 * Constants.MILLI_DAY;
        for(int i = 0, size = results.size(); i < size; i++) {
            if(threshold < results.getRecurringResult(i).getStart()) {
                return;
            }
        }
        fail("Couldn't find future recurrence for unlimited series");

    }

}
