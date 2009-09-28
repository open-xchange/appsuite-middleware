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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.calendar.printing.AbstractDateTest;
import com.openexchange.calendar.printing.CPAppointment;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class WorkWeekPartitioningTest extends 
AbstractDateTest {
    private WorkWeekPartitioningStrategy strategy;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        strategy = new WorkWeekPartitioningStrategy();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testShouldPartitionConsecutiveDatesInOneWeekIntoOneBlock() {        
        Date[] dates = getFourDates(THURSDAY());

        CPAppointment app1 = new CPAppointment();
        CPAppointment app2 = new CPAppointment();
        app1.setTitle("First appointment");
        app2.setTitle("Second appointment");
        app1.setStartDate(dates[0]);
        app1.setEndDate(dates[1]);
        app2.setStartDate(dates[2]);
        app2.setEndDate(dates[3]);
        
        CPPartition partitions = strategy.partition(Arrays.asList(new CPAppointment[]{app1,app2}));
        boolean foundWeekBreak = false;
        for(CPFormattingInfomation info: partitions.getFormattingInformation()){
            if(info.getType() == WorkWeekPartitioningStrategy.WEEKBREAK)
                foundWeekBreak = true;
        }
        assertTrue("Two consecutive days, Wednesday and Thursday, should not need any week break info", !foundWeekBreak);
        assertEquals("Partition should contain two appointments", 2, partitions.getAppointments().size());
    }

    public void testShouldNotShowWeekendAppointmentsAtAll(){
        Date[] dates = getFourDates(SUNDAY());
        CPAppointment weekendAppointment = new CPAppointment();
        weekendAppointment.setTitle("First appointment");
        weekendAppointment.setStartDate(dates[0]);
        weekendAppointment.setEndDate(dates[1]);
        
        CPPartition partitions = strategy.partition(Arrays.asList(new CPAppointment[]{weekendAppointment}));
        assertEquals("Partition should be empty", 0, partitions.getAppointments().size());
    }
    
    public void testShouldPartitionTwoDatesInTwoWeeksIntoTwoBlocks() {
        Date[] dates = getFourDates(THURSDAY());
        CPAppointment app1 = new CPAppointment();
        app1.setTitle("First appointment");
        app1.setStartDate(dates[0]);
        app1.setEndDate(dates[1]);
        
        dates = getFourDates(WEDNESDAY_NEXT_WEEK());
        CPAppointment app2 = new CPAppointment();
        app2.setTitle("Second appointment");
        app2.setStartDate(dates[0]);
        app2.setEndDate(dates[1]);
        
        CPPartition partitions = strategy.partition(Arrays.asList(new CPAppointment[]{app1, app2}));
        assertEquals("Should contain two elements", 2, partitions.getAppointments().size());
        boolean daybreakFound = false, weekbreakFound = false;
        for(CPFormattingInfomation info: partitions.getFormattingInformation()){
            if(info.getPosition() == 1 && info.getType() == WorkWeekPartitioningStrategy.DAYBREAK)
                daybreakFound = true;
            if(info.getPosition() == 1 && info.getType() == WorkWeekPartitioningStrategy.WEEKBREAK)
                weekbreakFound = true;
        }
        assertTrue("Should contain a day break after the first element", daybreakFound);
        assertTrue("Should contain a week break after the first element", weekbreakFound);
    }
    
    public void testShouldDetermineMissingDays(){
        int[] daysInbetween = strategy.getMissingDaysInbetween(THURSDAY().getTime(), SUNDAY().getTime());
        assertEquals("Should have two days inbetween", 2, daysInbetween.length);
        assertEquals("First day inbetween would be Friday", Calendar.FRIDAY, daysInbetween[0]);
        assertEquals("Second day inbetween would be Saturday", Calendar.SATURDAY, daysInbetween[1]);
    }
    
    public void testShouldDetermineMissingDaysBetweenTwoWeeks(){
        int[] daysInbetween = strategy.getMissingDaysInbetween(THURSDAY().getTime(), WEDNESDAY_NEXT_WEEK().getTime());
        assertEquals("Should have two days inbetween", 5, daysInbetween.length);
        assertEquals("First day inbetween would be Friday", Calendar.FRIDAY, daysInbetween[0]);
        assertEquals("Second day inbetween would be Saturday", Calendar.SATURDAY, daysInbetween[1]);
        assertEquals("Third day inbetween would be Sunday", Calendar.SUNDAY, daysInbetween[2]);
        assertEquals("Fourth day inbetween would be Monday", Calendar.MONDAY, daysInbetween[3]);
        assertEquals("Fifth day inbetween would be Tuesday", Calendar.TUESDAY, daysInbetween[4]);
    }
    
    public void testShouldGiveDayInfo(){
        Date[] dates = getFourDates(THURSDAY());
        CPAppointment app1 = new CPAppointment();
        app1.setTitle("First appointment");
        app1.setStartDate(dates[0]);
        app1.setEndDate(dates[1]);
        
        dates = getFourDates(SUNDAY());
        CPAppointment app2 = new CPAppointment();
        app2.setTitle("Second appointment");
        app2.setStartDate(dates[0]);
        app2.setEndDate(dates[1]);
        
        CPPartition partitions = strategy.partition(Arrays.asList(new CPAppointment[]{app1, app2}));
        List<Integer> days = new LinkedList<Integer>();
        int numberOfDays = 0;
        for(CPFormattingInfomation info: partitions.getFormattingInformation()){
            if(info.getType() == 10){
                days.add (Integer.valueOf (info.getAdditionalInformation() ) );
                numberOfDays++;
            }
        }
        Collections.sort(days);
        assertEquals("Should contain day info for every work day", 5, numberOfDays);
        assertTrue("Should contain Monday, even though there is no appointment", days.contains( Integer.valueOf(Calendar.MONDAY) ));
        assertTrue("Should contain Tuesday, even though there is no appointment", days.contains( Integer.valueOf(Calendar.TUESDAY) ));
        assertTrue("Should contain Wednesday, even though there is no appointment", days.contains( Integer.valueOf(Calendar.WEDNESDAY) ));
        assertTrue("Should contain Thursday", days.contains( Integer.valueOf(Calendar.THURSDAY) ));        
        assertTrue("Should contain Friday, even though there is no appointment", days.contains( Integer.valueOf(Calendar.FRIDAY) ));
        assertTrue("Should not contain Saturday, because that is a weekend day", !days.contains( Integer.valueOf(Calendar.SATURDAY) ));
        assertTrue("Should not contain Sunday, because that is a weekend day, although there is an appointment", !days.contains( Integer.valueOf(Calendar.SUNDAY) ));
    }
}
