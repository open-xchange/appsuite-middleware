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

package com.openexchange.calendar.printing;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.printing.blocks.WeekAndDayCalculator;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.UserParticipant;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CPTool extends WeekAndDayCalculator {

    /**
     * Based on the selected template, this method determines new start and end dates to present exactly the block that the template needs.
     */
    public void calculateNewStartAndEnd(final CPParameters params) {
        if (!isBlockTemplate(params))
         {
            return;
        // TODO this calls for a strategy pattern later on when there is more than one
        }

        final Calendar cal = getCalendar();
        cal.setTime(params.getStart());
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        params.setStart(cal.getTime());

        cal.setTime(params.getEnd());
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.MILLISECOND, 999);
        params.setEnd(cal.getTime());
    }

    /**
     * Checks whether the template given is one that prints a specific timeframe as a block, which might be different from the given start
     * and end date.
     */
    public boolean isBlockTemplate(final CPParameters params) {
        final String basic = "/[^/]+$";
        final String template = (params.hasUserTemplate()) ? params.getUserTemplate() : params.getTemplate();
        final Matcher m1 = Pattern.compile(CPType.WORKWEEKVIEW.getName() + basic).matcher(template);
        final Matcher m2 = Pattern.compile(CPType.WORKWEEKVIEW.getNumber() + basic).matcher(template);
        return m1.find() || m2.find();
    }

    /**
     * Sort a list of appointments by start date.
     */
    public void sort(final List<CPAppointment> appointments) {
        Collections.sort(appointments, new StartDateComparator());
    }

    /**
     * Expands all appointments in a list using their recurrence information for a certain given timeframe
     */
    public List<CPAppointment> expandAppointements(final List<Appointment> compressedAppointments, final Date start, final Date end, final AppointmentSQLInterface appointmentSql, final CalendarCollectionService calendarTools, int userId) throws OXException, SQLException {
        final List<CPAppointment> expandedAppointments = new LinkedList<CPAppointment>();
        for (final Appointment appointment : compressedAppointments) {
            final Appointment temp = appointmentSql.getObjectById(appointment.getObjectID(), appointment.getParentFolderID());
            final List<Appointment> split = splitIntoSingleDays(temp);
            for (final Appointment temp2 : split) {
                expandedAppointments.addAll(expandRecurrence(temp2, start, end, calendarTools, userId));
            }
        }
        return expandedAppointments;
    }

    public List<Appointment> splitIntoSingleDays(final Appointment appointment) {
        final List<Appointment> appointments = new LinkedList<Appointment>();

        if (!isOnDifferentDays(appointment.getStartDate(), appointment.getEndDate())) {
            appointments.add(appointment);
            return appointments;
        }

        final int duration = getMissingDaysInbetween(appointment.getStartDate(), appointment.getEndDate()).size() + 1;

        final Calendar newStartCal = Calendar.getInstance();
        newStartCal.setTime(appointment.getStartDate());
        newStartCal.set(Calendar.HOUR_OF_DAY, 0);
        newStartCal.set(Calendar.MINUTE, 0);
        newStartCal.set(Calendar.SECOND, 0);
        newStartCal.set(Calendar.MILLISECOND, 0);

        final Calendar newEndCal = Calendar.getInstance();
        newEndCal.setTime(appointment.getStartDate());
        newEndCal.set(Calendar.HOUR_OF_DAY, 23);
        newEndCal.set(Calendar.MINUTE, 59);
        newEndCal.set(Calendar.SECOND, 59);
        newEndCal.set(Calendar.MILLISECOND, 999);

        final Appointment first = appointment.clone();
        first.setEndDate(newEndCal.getTime());
        appointments.add(first);

        for (int i = 1; i < duration; i++) {
            final Appointment middle = appointment.clone();
            newStartCal.add(Calendar.DAY_OF_YEAR, 1);
            newEndCal.add(Calendar.DAY_OF_YEAR, 1);
            middle.setStartDate(newStartCal.getTime());
            middle.setEndDate(newEndCal.getTime());
            appointments.add(middle);
        }

        final Appointment last = appointment.clone();
        newStartCal.add(Calendar.DAY_OF_YEAR, 1);
        last.setStartDate(newStartCal.getTime());
        appointments.add(last);

        return appointments;
    }

    /**
     * Takes an appointment and interprets its recurrence information to find all occurrences between start and end date.
     */
    public List<CPAppointment> expandRecurrence(final Appointment appointment, final Date start, final Date end, final CalendarCollectionService calendarTools, int userId) throws OXException {
        if (hasDeclined(appointment, userId)) {
            return Collections.<CPAppointment>emptyList();
        }
        final RecurringResultsInterface recurrences = calendarTools.calculateRecurring(appointment, start.getTime(), end.getTime(), 0);
        final List<CPAppointment> all = new LinkedList<CPAppointment>();
        if (recurrences == null) {
            all.add(new CPAppointment(appointment));
            return all;
        }

        for (int i = 0, length = recurrences.size(); i < length; i++) {
            final CPAppointment temp = new CPAppointment();
            temp.setTitle(appointment.getTitle());
            final RecurringResultInterface recurringResult = recurrences.getRecurringResult(i);
            temp.setStartDate(new Date(recurringResult.getStart()));
            temp.setEndDate(new Date(recurringResult.getEnd()));
            temp.setOriginal(appointment);
            
            all.add(temp);
        }
        return all;
    }

    public static boolean hasDeclined(Appointment appointment, int userId) {
        UserParticipant[] users = appointment.getUsers();
        for (UserParticipant userParticipant : users) {
            if (userParticipant.getIdentifier() == userId && userParticipant.getConfirm() == Appointment.DECLINE) {
                return true;
            }
        }
        return false;
    }
}
