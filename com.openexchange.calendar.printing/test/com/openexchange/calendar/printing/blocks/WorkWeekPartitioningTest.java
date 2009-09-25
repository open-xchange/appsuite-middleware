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
import java.util.Date;
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
        assertEquals("Two consecutive days, Wednesday and Thursday, should need only one partition", 1, partitions.getFormattingInformation().size());
        assertEquals("Partition should contain two appointments", 2, partitions.getAppointments().size());
    }

    public void testShouldNotShowWeekendAppointmentsAtAll(){
        Date[] dates = getFourDates(SUNDAY());
        CPAppointment weekendAppointment = new CPAppointment();
        weekendAppointment.setTitle("First appointment");
        weekendAppointment.setStartDate(dates[0]);
        weekendAppointment.setEndDate(dates[1]);
        
        CPPartition partitions = strategy.partition(Arrays.asList(new CPAppointment[]{weekendAppointment}));
        assertEquals("Should have no formatting info", 0, partitions.getFormattingInformation().size());
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
}
