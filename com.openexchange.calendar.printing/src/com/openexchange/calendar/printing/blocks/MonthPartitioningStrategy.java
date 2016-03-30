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

package com.openexchange.calendar.printing.blocks;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.openexchange.calendar.printing.CPAppointment;
import com.openexchange.calendar.printing.CPCalendar;
import com.openexchange.calendar.printing.CPTool;
import com.openexchange.calendar.printing.CPType;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class MonthPartitioningStrategy extends WeekPartitioningStrategy {

    @Override
    public boolean isPackaging(CPType type) {
        return CPType.MONTHLYVIEW == type;
    }

    @Override
    public CPPartition partition(List<CPAppointment> appointments) {
        CPTool tools = new CPTool();
        tools.sort(appointments);

        CPPartition blocks = new CPPartition();
        for (int i = 0, length = appointments.size(); i < length; i++) {
            CPAppointment appointment = appointments.get(i);
            if (i > 0) {
                lastStoredAppointment = appointments.get(i - 1);
            }

            int pointer = blocks.getAppointments().size();

            if (isMissingDaysInbetween(lastStoredAppointment, appointment)) {
                List<Date> days = getMissingDaysInbetween(lastStoredAppointment, appointment);
                for (Date day : days) {
                    if (isOnFirstDayOfMonth(day)) {
                        addMonthBreak(blocks, pointer, day);
                    }
                    if (getCalendar().isOnFirstDayOfWeek(day)) {
                        addWeekBreak(blocks, pointer, day);
                    }
                    addDayBreak(blocks, pointer, day);
                }
            }
            if (isSignalForNewMonth(appointment)) {
                addMonthBreak(blocks, pointer, appointment.getStartDate());
            }

            if (isSignalForNewWeek(appointment)) {
                addWeekBreak(blocks, pointer, appointment.getStartDate());
            }

            if (isSignalForNewDay(appointment)) {
                addDayBreak(blocks, pointer, appointment.getStartDate());
            }

            blocks.addAppointment(appointment);

            if (isOnTwoDays(appointment) || isInTwoWeeks(appointment) || isInTwoMonths(appointment))
             {
                blocks.addAppointment(appointment); // store again for use in second block
            }

            if (i == length - 1) {
                if (!isOnLastDayOfMonth(appointment.getStartDate())) {
                    for (Date day : getMissingDaysInbetween(appointment, null)) {
                        addDayBreak(blocks, ++pointer, day);
                    }
                }
            }
        }
        return blocks;
    }

    private boolean isOnLastDayOfMonth(Date day) {
        CPCalendar cal = getCalendar();
        cal.setTime(day);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.get(Calendar.DAY_OF_MONTH) == 1;
    }

    private boolean isOnFirstDayOfMonth(Date day) {
        // TODO move method to CPCalendar
        CPCalendar cal = getCalendar();
        cal.setTime(day);
        return cal.get(Calendar.DAY_OF_MONTH) == 1;
    }

    @Override
    protected boolean isMissingDaysInbetween(CPAppointment first, CPAppointment second) {
        Date firstDate = null;
        if (first == null) {
            Calendar cal = getCalendar();
            cal.setTime(second.getStartDate());
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            firstDate = cal.getTime();
        } else {
            firstDate = first.getStartDate();
        }
        return isMissingDaysInbetween(firstDate, second.getStartDate());
    }

    @Override
    protected List<Date> getMissingDaysInbetween(CPAppointment first, CPAppointment second) {
        Date firstDate = null, secondDate = null;
        if (first == null) {
            Calendar cal = getCalendar();
            cal.setTime(second.getStartDate());
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.DAY_OF_MONTH, -1); // subtract one because we want to include the first day, too
            firstDate = cal.getTime();
        } else {
            firstDate = first.getStartDate();
        }
        if (second == null) {
            CPCalendar cal = getCalendar();
            cal.setTime(first.getStartDate());
            cal.add(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 1); // add one because we want to include the last day of month
            secondDate = cal.getTime();
        } else {
            secondDate = second.getStartDate();
        }
        return getMissingDaysInbetween(firstDate, secondDate);
    }

}
