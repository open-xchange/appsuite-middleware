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

import java.util.Date;
import java.util.List;
import com.openexchange.calendar.printing.CPAppointment;
import com.openexchange.calendar.printing.CPType;

/**
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

    public abstract boolean isPackaging(CPType type);

    public abstract CPPartition partition(List<CPAppointment> appointments);

    private int lastDay = -1, lastWeek = -1, lastMonth = -1, lastYear = -1;
    
    protected void addDayBreak(CPPartition blocks, int pointer, Date day) {
        CPFormattingInformation dayBreak = new CPFormattingInformation(
            pointer,
            AbstractWeekPartitioningStrategy.DAYBREAK,
            getDayOfYear(day));
        if(lastDay != getWeekOfYear(day).intValue())
            blocks.addFormattingInformation(dayBreak);
        lastDay = getDayOfYear(day).intValue();
    }
    
    protected void addWeekBreak(CPPartition blocks, int pointer, Date day) {
        CPFormattingInformation weekBreak = new CPFormattingInformation(
            pointer,
            AbstractWeekPartitioningStrategy.WEEKBREAK,
            getWeekOfYear(day));
        if(lastWeek != getWeekOfYear(day).intValue())
            blocks.addFormattingInformation(weekBreak);
        lastWeek = getWeekOfYear(day).intValue();
    }

    protected void addMonthBreak(CPPartition blocks, int pointer, Date day) {
        CPFormattingInformation monthBreak = new CPFormattingInformation(
            pointer,
            AbstractWeekPartitioningStrategy.MONTHBREAK,
            day);
        if(lastMonth != getMonthOfYear(day).intValue())
            blocks.addFormattingInformation(monthBreak);
        lastMonth = getMonthOfYear(day).intValue();
    }

    protected void addYearBreak(CPPartition blocks, int pointer, Date date) {
        CPFormattingInformation yearBreak = new CPFormattingInformation(
            pointer,
            AbstractWeekPartitioningStrategy.YEARBREAK,
            date);
        if(lastYear != getYear(date).intValue())
            blocks.addFormattingInformation(yearBreak);
        lastYear = getYear(date).intValue();
    }
    
    protected boolean isSignalForNewDay(CPAppointment appointment) {
        if (lastStoredAppointment == null)
            return true;

        return isOnDifferentDays(lastStoredAppointment.getStartDate(), appointment.getStartDate()) || isOnDifferentDays(
            lastStoredAppointment.getEndDate(),
            appointment.getEndDate());
    }

    protected boolean isSignalForNewWeek(CPAppointment appointment) {
        if (lastStoredAppointment == null)
            return true;

        return isInDifferentWeeks(lastStoredAppointment.getStartDate(), appointment.getStartDate()) || isInDifferentWeeks(
            lastStoredAppointment.getEndDate(),
            appointment.getEndDate());
    }
}
