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

/**
 * This abstract class is the super class for all partitioning strategies that use weeks. Weeks are special, because a view on a month might
 * need to include weeks that do not belong to that month, in case the first or last week of a month contains days from neighbouring months.
 * To do these checks, this class keeps track of days, weeks and months internally. It works stateful, unlike its super class, which only
 * contains non-stateful classes.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class AbstractWeekPartitioningStrategy extends WeekAndDayCalculator implements CPPartitioningStrategy {

    public static final int DAYBREAK = 0;

    public static final int WEEKBREAK = 1;

    public static final int MONTHBREAK = 2;

    public static final int YEARBREAK = 3;

    public static final int DAYNAME = 10;

    public static final int FILLDAY = 11; // a day that is only used to fill up the beginning or end of weeks or months

    protected CPAppointment lastStoredAppointment = null;

    protected abstract void cleanup(CPPartition partition);

    private int lastDay = -1, lastWeek = -1, lastMonth = -1, lastYear = -1;

    protected void addDayBreak(CPPartition blocks, int pointer, Date day) {
        if (lastDay == getDayOfYear(day).intValue()) {
            return;
        }
        blocks.addFormattingInformation(new CPFormattingInformation(pointer, AbstractWeekPartitioningStrategy.DAYBREAK, day));
        blocks.addFormattingInformation(new CPFormattingInformation(
            pointer,
            AbstractWeekPartitioningStrategy.DAYNAME,
            getWeekDayNumber(day)));
        lastDay = getDayOfYear(day).intValue();
    }

    protected void addWeekBreak(CPPartition blocks, int pointer, Date day) {
        if (lastWeek == getWeekOfYear(day).intValue()) {
            return;
        }
        CPFormattingInformation weekBreak = new CPFormattingInformation(
            pointer,
            AbstractWeekPartitioningStrategy.WEEKBREAK,
            getWeekOfYear(day));
        blocks.addFormattingInformation(weekBreak);
        fillUpBeginningOfWorkWeek(blocks.getFormattingInformation(), day, pointer);
        lastWeek = getWeekOfYear(day).intValue();
    }

    protected void addMonthBreak(CPPartition blocks, int pointer, Date day) {
        if (lastMonth == getMonthOfYear(day).intValue()) {
            return;
        }
        CPFormattingInformation monthBreak = new CPFormattingInformation(pointer, AbstractWeekPartitioningStrategy.MONTHBREAK, day);
        blocks.addFormattingInformation(monthBreak);
        fillUpBeginningOfMonth(blocks.getFormattingInformation(), day, pointer);

        lastMonth = getMonthOfYear(day).intValue();
    }

    protected void addYearBreak(CPPartition blocks, int pointer, Date date) {
        CPFormattingInformation yearBreak = new CPFormattingInformation(pointer, AbstractWeekPartitioningStrategy.YEARBREAK, date);
        if (lastYear != getYear(date).intValue()) {
            blocks.addFormattingInformation(yearBreak);
        }
        lastYear = getYear(date).intValue();
    }

    protected boolean isSignalForNewDay(CPAppointment appointment) {
        if (lastStoredAppointment == null) {
            return true;
        }

        return isOnDifferentDays(lastStoredAppointment.getStartDate(), appointment.getStartDate()) || isOnDifferentDays(
            lastStoredAppointment.getEndDate(),
            appointment.getEndDate());
    }

    protected boolean isSignalForNewWeek(CPAppointment appointment) {
        if (lastStoredAppointment == null) {
            return true;
        }

        return isInDifferentWeeks(lastStoredAppointment.getStartDate(), appointment.getStartDate()) || isInDifferentWeeks(
            lastStoredAppointment.getEndDate(),
            appointment.getEndDate());
    }

    protected boolean isSignalForNewMonth(CPAppointment appointment) {
        if (lastStoredAppointment == null) {
            return true;
        }

        return isInDifferentMonths(lastStoredAppointment.getStartDate(), appointment.getStartDate()) || isInDifferentMonths(
            lastStoredAppointment.getEndDate(),
            appointment.getEndDate());
    }

    protected void fillUpBeginningOfMonth(List<CPFormattingInformation> info, Date month, int position) {
        CPCalendar cal = getCalendar();
        cal.setTime(month);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDayOfMonth = cal.getTime();
        int firstDayOfMonthInYear = cal.get(Calendar.DAY_OF_YEAR);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        Date firstDayOfWeekInMonth = cal.getTime();
        int firstDayOfWeekInMonthInYear = cal.get(Calendar.DAY_OF_YEAR);

        int fillersToAdd = 0;
        if (isMissingDaysInbetween(firstDayOfWeekInMonth, firstDayOfMonth)) {
            fillersToAdd = getMissingDaysInbetween(firstDayOfWeekInMonth, firstDayOfMonth).size();
        }
        if (firstDayOfMonthInYear != firstDayOfWeekInMonthInYear)
         {
            fillersToAdd++; // takes care of the one day not covered by "inbetween"
        }

        int insertionPoint = info.lastIndexOf(new CPFormattingInformation(position, MONTHBREAK));
        if (insertionPoint == -1) {
            insertionPoint = info.lastIndexOf(new CPFormattingInformation(position, YEARBREAK));
        }
        if (insertionPoint == -1) {
            insertionPoint = 0;
        }
        else {
            insertionPoint++; // insert after the month/year break
        }
        for (int i = 0; i < fillersToAdd; i++) {
            info.add(insertionPoint, new CPFormattingInformation(position, FILLDAY, Integer.valueOf(MONTHBREAK)));
        }
    }

    protected void fillUpBeginningOfWorkWeek(List<CPFormattingInformation> info, Date day, int position) {
        if(!isInWorkWeek(day)) {
            return;
        }
        CPCalendar cal = getCalendar();
        cal.setTime(day);
        int dayInYear = cal.get(Calendar.DAY_OF_YEAR);

        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWorkWeek());
        Date firstDayOfWorkWeek = cal.getTime();
        int firstDayOfWorkWeekInMonthInYear = cal.get(Calendar.DAY_OF_YEAR);

        int fillersToAdd = 0;
        if (isMissingDaysInbetween(firstDayOfWorkWeek, day)) {
            fillersToAdd = getMissingDaysInbetween(firstDayOfWorkWeek, day).size();
        }
        if (dayInYear!= firstDayOfWorkWeekInMonthInYear)
         {
            fillersToAdd++; // takes care of the one day not covered by "inbetween"
        }

        int insertionPoint = info.lastIndexOf(new CPFormattingInformation(position, WEEKBREAK));
        if (insertionPoint == -1) {
            insertionPoint = info.lastIndexOf(new CPFormattingInformation(position, MONTHBREAK));
        }
        if (insertionPoint == -1) {
            insertionPoint = info.lastIndexOf(new CPFormattingInformation(position, YEARBREAK));
        }
        if (insertionPoint == -1) {
            insertionPoint = 0;
        }
        else {
            insertionPoint++; // insert after the week/month/year break
        }

        for (int i = 0; i < fillersToAdd; i++) {
            info.add(insertionPoint, new CPFormattingInformation(position, FILLDAY, Integer.valueOf(WEEKBREAK)));
        }
    }
}
