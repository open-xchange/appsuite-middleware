/*
 * CalendarPerformanceTests.java
 *
 * Created on 12. September 2006, 16:50
 *
 */

package com.openexchange.groupware;

import com.openexchange.groupware.calendar.CalendarCommonCollection;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarRecurringCollection;
import com.openexchange.groupware.calendar.RecurringResults;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import java.util.Date;
import junit.framework.TestCase;

/**
 *
 * @author bishoph
 */
public class CalendarPerformanceTests extends TestCase { 
    
    private static final int cols[] = new int[] { AppointmentObject.TITLE, AppointmentObject.START_DATE,  AppointmentObject.END_DATE, AppointmentObject.LOCATION, AppointmentObject.SHOWN_AS, AppointmentObject.RECURRENCE_TYPE };
    
    protected void setUp() throws Exception {        
        super.setUp();    
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testRecurringPerformance() throws Throwable {
        CalendarDataObject cdao = new CalendarDataObject();
        long s = 1149768000000L; // 08.06.2006 12:00 (GMT)
        long e = 1244458800000L; // 08.06.2009 13:00:00 (GMT)
        long u = CalendarTest.SUPER_END; 
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u));
        cdao.setTitle("Daily Appointment Test");
        cdao.setRecurrenceType(CalendarObject.DAILY);
        cdao.setRecurrenceCalculator(1);
        cdao.setInterval(1);
        cdao.setDays(1);
        cdao.setRecurrenceID(1);
        RecurringResults rrs = null;
        int c_size = 0;
        long pass_one_start = System.currentTimeMillis();
        for (int a = 0; a < 400; a++) {
            cdao.setInterval(1);
            rrs = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
            c_size += rrs.size();
        }
        long pass_one_end = System.currentTimeMillis();
        System.out.println("Daily runtime: "+(pass_one_end-pass_one_start)+ " ms. for "+ c_size + " entries");
        
        cdao.setRecurrenceType(CalendarObject.WEEKLY);
        cdao.setDays(CalendarObject.MONDAY + CalendarObject.TUESDAY + CalendarObject.WEDNESDAY + CalendarObject.THURSDAY + CalendarObject.FRIDAY + CalendarObject.SATURDAY + CalendarObject.SUNDAY);
        pass_one_start = System.currentTimeMillis();
        c_size = 0;
        for (int a = 0; a < 400; a++) {
            cdao.setInterval(1);
            rrs = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
            c_size += rrs.size();
        }
        pass_one_end = System.currentTimeMillis();
        System.out.println("Weekly runtime: "+(pass_one_end-pass_one_start)+ " ms. for "+ c_size + " entries");          
    }    
    
    public void testcheckAndAlterColsPerformance() throws Throwable {
        long pass_one_start = System.currentTimeMillis();
        final int RUNS = 4000*100;
        
        for (int a = 0; a < RUNS; a++)  {
            int changeable_cols[] = new int[] { AppointmentObject.TITLE, AppointmentObject.START_DATE,  AppointmentObject.END_DATE, AppointmentObject.LOCATION, AppointmentObject.SHOWN_AS, AppointmentObject.RECURRENCE_TYPE }; //cols.clone();        
            int new_cols[] = CalendarCommonCollection.checkAndAlterCols(changeable_cols);
            assertNotSame(new_cols, changeable_cols);
        }
        long pass_one_end = System.currentTimeMillis();
        System.out.println("checkAndAlterColsPerformance runtime ("+RUNS+"): "+(pass_one_end-pass_one_start));        
    }
    
    
}
