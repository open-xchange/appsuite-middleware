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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import com.openexchange.calendar.printing.CPTool;
import com.openexchange.calendar.printing.CPType;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class WorkWeekPartitioningStrategy extends AbstractPartitioningStrategy implements CPPartitioningStrategy {

    public static final int DAYBREAK = 0;

    public static final int WEEKBREAK = 1;
    
    public static final int DAYNAME = 10;

    private CPAppointment lastAppointment = null;

    public boolean isPackaging(CPType type) {
        return type == CPType.WORKWEEKVIEW;
    }

    public CPPartition partition(List<CPAppointment> appointments) {
        CPTool tools = new CPTool();
        tools.sort(appointments);

        CPPartition blocks = new CPPartition();
        for (int i = 0, length = appointments.size(); i < length; i++) {
            CPAppointment appointment = appointments.get(i);
            if (i > 0)
                lastAppointment = appointments.get(i - 1);
            
            int appCount = blocks.getAppointments().size();
            
            if (isSignalForNewWeek(appointment))
                blocks.addFormattingInformation(new CPFormattingInformation(appCount, WEEKBREAK, getWeekOfYear(appointment.getStartDate())));

            if (isMissingDaysInbetween(lastAppointment, appointment) )
                for(Date day : getMissingDaysInbetween(lastAppointment, appointment))
                    if(isInWorkWeek(day)){
                        blocks.addFormattingInformation(new CPFormattingInformation(appCount, DAYBREAK,  day));
                        blocks.addFormattingInformation(new CPFormattingInformation(appCount, DAYNAME, getWeekDayNumber(day)));
                    }

            if (isSignalForNewDay(appointment) && isInWorkWeek(appointment.getStartDate())){
                blocks.addFormattingInformation(new CPFormattingInformation(appCount, DAYBREAK, appointment.getStartDate()));
                blocks.addFormattingInformation(new CPFormattingInformation(appCount, DAYNAME, getWeekDayNumber(appointment.getStartDate())));
            }

            if (isWorkWeekAppointment(appointment))
                blocks.addAppointment(appointment);

            if (isWorkWeekAppointment(appointment))
                if (isOnTwoDays(appointment) || isInTwoWeeks(appointment))
                    blocks.addAppointment(appointment); // store again for use in second block
            
            if(i == length -1)
                if(!isOnLastWorkDayOfWeek(appointment.getStartDate()))
                    for(Date day : getMissingDaysInbetween(appointment, null))
                        if(isInWorkWeek(day)){
                            blocks.addFormattingInformation(new CPFormattingInformation(appCount, DAYBREAK,  day));
                            blocks.addFormattingInformation(new CPFormattingInformation(appCount, DAYNAME, getWeekDayNumber(day)));
                        }
        }
        return blocks;
    }


    private Integer getWeekDayNumber(Date day) {
        Calendar cal = getCalendar(); 
        cal.setTime(day);
        return Integer.valueOf( cal.get(Calendar.DAY_OF_WEEK) );
    }

    public Integer getWeekOfYear(Date date) {
        getCalendar().setTime(date);
        return Integer.valueOf( getCalendar().get(Calendar.WEEK_OF_YEAR) );
    }

    private boolean isOnTwoDays(CPAppointment appointment) {
        return isOnDifferentDays(appointment.getStartDate(), appointment.getEndDate());
    }

    private boolean isInTwoWeeks(CPAppointment appointment) {
        return isInDifferentWeeks(appointment.getStartDate(), appointment.getEndDate());
    }

    private boolean isSignalForNewDay(CPAppointment appointment) {
        if (lastAppointment == null)
            return true;

        return isOnDifferentDays(lastAppointment.getStartDate(), appointment.getStartDate()) || isOnDifferentDays(
            lastAppointment.getEndDate(),
            appointment.getEndDate());
    }

    private boolean isSignalForNewWeek(CPAppointment appointment) {
        if (lastAppointment == null)
            return true;

        return isInDifferentWeeks(lastAppointment.getStartDate(), appointment.getStartDate()) || isInDifferentWeeks(
            lastAppointment.getEndDate(),
            appointment.getEndDate());
    }

    protected List<Date> getMissingDaysInbetween(CPAppointment first, CPAppointment second) {
        Date firstDate = null, secondDate = null;
        if(first == null){
            Calendar cal = getCalendar();
            cal.setTime(second.getStartDate());
            cal.set(Calendar.DAY_OF_WEEK, getFirstDayOfWorkWeek());
            cal.add(Calendar.DAY_OF_WEEK, -1); //subtract one because we want to include the first working day
            firstDate = cal.getTime();
        } else {
            firstDate = first.getStartDate();
        }
        if(second == null){
            Calendar cal = getCalendar();
            cal.setTime(first.getStartDate());
            cal.set(Calendar.DAY_OF_WEEK, getLastDayOfWorkWeek());
            cal.add(Calendar.DAY_OF_WEEK, 1); //add one because we want to include the last working day
            secondDate = cal.getTime();
        } else {
            secondDate = second.getStartDate();
        }
        return getMissingDaysInbetween(firstDate, secondDate);
    }

    protected boolean isMissingDaysInbetween(CPAppointment first, CPAppointment second) {
        Date firstDate = null;
        if(first == null){
            Calendar cal = getCalendar();
            cal.setTime(second.getStartDate());
            cal.set(Calendar.DAY_OF_WEEK, getFirstDayOfWorkWeek());
            cal.add(Calendar.DAY_OF_WEEK, -1);
            firstDate = cal.getTime();
        } else 
            firstDate = first.getStartDate();
        return isMissingDaysInbetween(firstDate, second.getStartDate());
    }

    protected boolean isOnLastWorkDayOfWeek(Date day){
        Calendar cal = getCalendar();
        cal.setTime(day);
        return cal.get(Calendar.DAY_OF_WEEK) == getLastDayOfWorkWeek();
    }
}
