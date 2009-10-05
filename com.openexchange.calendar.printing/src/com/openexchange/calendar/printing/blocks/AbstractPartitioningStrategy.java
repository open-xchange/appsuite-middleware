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
import java.util.LinkedList;
import java.util.List;
import com.openexchange.calendar.printing.CPAppointment;

/**
 * {@link AbstractPartitioningStrategy}
 *
 * @author <a href="mailto:firstname.lastname@open-xchange.com">Firstname Lastname</a>
 */
public class AbstractPartitioningStrategy {
    
    private Calendar calendar = Calendar.getInstance();
    
    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public Calendar getCalendar() {
        return calendar;
    }
    
    public AbstractPartitioningStrategy() {
        super();
    }

    public boolean isOnDifferentDays(Date first, Date second) {
        calendar = getCalendar();
    
        calendar.setTime(first);
        int day1 = calendar.get(Calendar.DAY_OF_YEAR);
        int year1 = calendar.get(Calendar.YEAR);
        calendar.setTime(second);
        int day2 = calendar.get(Calendar.DAY_OF_YEAR);
        int year2 = calendar.get(Calendar.YEAR);
    
        return day1 != day2 || year1 != year2;
    }

    public boolean isInDifferentWeeks(Date first, Date second) {
        calendar = getCalendar();
    
        calendar.setTime(first);
        int week1 = calendar.get(Calendar.WEEK_OF_YEAR);
        int year1 = calendar.get(Calendar.YEAR);
        calendar.setTime(second);
        int week2 = calendar.get(Calendar.WEEK_OF_YEAR);
        int year2 = calendar.get(Calendar.YEAR);
    
        return week1 != week2 || year1 != year2;
    }

    /**
     * @return true if start or end date are in work week, false otherwise (also if not set at all)
     */
    public boolean isWorkWeekAppointment(CPAppointment appointment) {
        if (appointment.getStartDate() != null && isInWorkWeek(appointment.getStartDate()))
            return true;
        if (appointment.getStartDate() != null && isInWorkWeek(appointment.getStartDate()))
            return true;
        return false;
    }

    public boolean isInWorkWeek(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        return isInWorkWeek(calendar.get(Calendar.DAY_OF_WEEK));
    }

    public boolean isInWorkWeek(int calendarDayOfWeek) {
        // TODO: Scope of work week might need to be configurable in the future.
        return !(calendarDayOfWeek == Calendar.SATURDAY || calendarDayOfWeek  == Calendar.SUNDAY);
    }
    
    protected List<Date> getMissingDaysInbetween(Date first, Date second) {
        long length = (second.getTime() - first.getTime()) /1000/60/60/24 - 1;
        LinkedList<Date> days = new LinkedList<Date>();
        Calendar cal = getCalendar();
        cal.setTime(first);
        
        for(int i = 0; i < length; i++){
            cal.add(Calendar.DAY_OF_YEAR, 1);
            days.add( cal.getTime() );
        }
        
        return days;
    }
    
    protected boolean isMissingDaysInbetween(Date first, Date second) {
        return ((second.getTime() - first.getTime()) /1000/60/60/24) > 1;
    }

    protected int getFirstDayOfWorkWeek() {
        // TODO make less European
        return Calendar.MONDAY;
    }


    protected int getLastDayOfWorkWeek() {
        // TODO make less European
        return Calendar.FRIDAY;
    }
}
