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

package com.openexchange.ajax.appointment.recurrence;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Changes;
import com.openexchange.groupware.container.Expectations;

/**
 * There are two ways to limit an appointment series: One is by date, one is by number of
 * occurrences. Currently, removal of an occurrence by creating a delete exception does
 * not reduce the number of occurrences, also called "recurrence_count".
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestsForDeleteExceptionsAndFixedEndsOfSeries extends ManagedAppointmentTest {

    public TestsForDeleteExceptionsAndFixedEndsOfSeries(String name) {
        super(name);
    }


    public void testShouldNotReduceNumberOfOccurrencesWhenDeletingOneInYearlySeries() throws Exception {
        Appointment app = generateYearlyAppointment();
        app.setOccurrence(5);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 5);

        Expectations expectations = new Expectations();
        expectations.put(Appointment.RECURRENCE_COUNT, 5);
        expectations.put(Appointment.UNTIL, null); //tricky decision whether this should be set or not

        positiveAssertionOnDeleteException.check(app, changes, expectations);
    }

    public void testShouldFailWhenDeletingBeyondScopeOfSeriesInYearlySeries() throws Exception {
        Appointment app = generateYearlyAppointment();
        app.setOccurrence(5);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 6);

        try {
            negativeAssertionOnDeleteException.check(app, changes, new OXException(11));
        } catch (AssertionError e) {
            negativeAssertionOnDeleteException.check(app, changes, OXCalendarExceptionCodes.UNKNOWN_RECURRENCE_POSITION.create());
        }
    }

    public void testShouldNotReduceNumberOfOccurrencesWhenDeletingOneInMonthlySeries() throws Exception {
        Appointment app = generateMonthlyAppointment();
        app.setOccurrence(6);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 6);

        Expectations expectations = new Expectations();
        expectations.put(Appointment.RECURRENCE_COUNT, 6);

        positiveAssertionOnDeleteException.check(app, changes, expectations);
    }

    public void testShouldRemoveWholeSeriesIfEverySingleOccurrenceIsDeleted(){
        Appointment app = generateMonthlyAppointment();
        int numberOfOccurences = 3;
        app.setOccurrence(numberOfOccurences );

        calendarManager.insert(app);

        for(int i = 0; i < numberOfOccurences; i++){
            calendarManager.createDeleteException(app, i+1);
            assertFalse("Should not fail while creating delete exception #"+i, calendarManager.hasLastException());
        }
    }

}
