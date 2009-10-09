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
import com.openexchange.calendar.printing.CPCalendar;
import com.openexchange.calendar.printing.CPType;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public abstract class AbstractPartitioningStrategy implements CPPartitioningStrategy{
    
    private CPCalendar calendar = new CPCalendar();
    
    public void setCalendar(CPCalendar calendar) {
        this.calendar = calendar;
    }

    public CPCalendar getCalendar() {
        return calendar;
    }
    
    public AbstractPartitioningStrategy() {
        super();
    }
    
    public abstract boolean isPackaging(CPType type);
    
    public abstract CPPartition partition(List<CPAppointment> appointments);

    public Integer getWeekOfYear(Date date) {
        getCalendar().setTime(date);
        return Integer.valueOf( getCalendar().get(Calendar.WEEK_OF_YEAR) );
    }
    

    public Integer getMonthOfYear(Date date) {
        getCalendar().setTime(date);
        return Integer.valueOf( getCalendar().get(Calendar.MONTH) );
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

    public boolean isInDifferentMonths(Date first, Date second) {
        calendar = getCalendar();
    
        calendar.setTime(first);
        int month1 = calendar.get(Calendar.MONTH);
        int year1 = calendar.get(Calendar.YEAR);
        calendar.setTime(second);
        int month2 = calendar.get(Calendar.MONTH);
        int year2 = calendar.get(Calendar.YEAR);
    
        return month1 != month2 || year1 != year2;
    }
    
    public List<Date> getMissingDaysInbetween(Date first, Date second) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(first);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date refinedFirst = cal.getTime();
        
        cal.setTime(second);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        Date refinedSecond = cal.getTime();
        
        long length = (refinedSecond.getTime() - refinedFirst.getTime()) /1000/60/60/24 - 1;
        LinkedList<Date> days = new LinkedList<Date>();
        
        cal.setTime(refinedFirst);
        
        for(int i = 0; i < length; i++){
            cal.add(Calendar.DAY_OF_YEAR, 1);
            days.add( cal.getTime() );
        }
        
        return days;
    }
    
    public boolean isMissingDaysInbetween(Date first, Date second) {
        return getMissingDaysInbetween(first, second).size() != 0;
    }
    
    public boolean isOnFirstDayOfWorkWeek(Date day) {
        CPCalendar cal = getCalendar();
        cal.setTime(day);
        return cal.get(Calendar.DAY_OF_WEEK) == cal.getFirstDayOfWorkWeek();
    }

    public Integer getWeekDayNumber(Date day) {
        Calendar cal = getCalendar();
        cal.setTime(day);
        return Integer.valueOf(cal.get(Calendar.DAY_OF_WEEK));
    }

    public boolean isOnTwoDays(CPAppointment appointment) {
        return isOnDifferentDays(appointment.getStartDate(), appointment.getEndDate());
    }

    public boolean isInTwoWeeks(CPAppointment appointment) {
        return isInDifferentWeeks(appointment.getStartDate(), appointment.getEndDate());
    }
    

    public boolean isInTwoMonths(CPAppointment appointment) {
        return isInDifferentMonths(appointment.getStartDate(), appointment.getEndDate());
    }
}