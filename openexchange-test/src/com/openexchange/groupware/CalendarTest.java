
package com.openexchange.groupware;


import com.openexchange.groupware.CalendarRecurringCollection;
import com.openexchange.groupware.calendar.CalendarCommonCollection;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.RdbContextWrapper;
import com.openexchange.groupware.contexts.ContextStorage;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.api.OXCalendar;
import com.openexchange.groupware.calendar.RecurringResults;
import com.openexchange.groupware.calendar.RecurringResult;

import java.sql.Connection;
import java.util.Date;
import java.util.Map;
import junit.framework.TestCase;

public class CalendarTest extends TestCase {
    
    
    private final static int TEST_PASS = 9999;
    private final static int TEST_PASS_HOT_SPOT = 99999;
    
    protected void setUp() throws Exception {        
        super.setUp();
        Init.initDB();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testBasicRecurring() throws Throwable {
        CalendarDataObject cdao = new CalendarDataObject();      
        assertFalse(cdao.calculateRecurrence());
        cdao.setStartDate(new Date(0));
        cdao.setEndDate(new Date(0));
        cdao.setUntil(new Date(0));        
        cdao.setTitle("Basic Recurring Test");
        cdao.setRecurrenceID(1);
        assertFalse(cdao.calculateRecurrence());
        cdao.setRecurrenceType(OXCalendar.DAILY);        
        assertFalse(cdao.calculateRecurrence());
        cdao.setRecurrenceCalculator(1);        
        assertFalse(cdao.calculateRecurrence());
        cdao.setInterval(1);
        assertTrue(cdao.calculateRecurrence());
    }
    
    
    public void testDailyRecurring() throws Throwable {
        long s = 1149768000000L; // 08.06.2006 12:00 (GMT)
        long e = 1149771600000L; // 08.06.2006 13:00 (GMT)
        long u = 1150156800000L; // 13.06.2006 00:00 (GMT)
        long u_test = 1150106400000L; // 12.06.2006 10:00 (GMT)
        String testrecurrence = "ds|daily|daily_value|1|ds_start|"+s+"|ds_ends|"+u+"|";
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u_test));
        cdao.setTitle("Daily Appointment Test");
        cdao.setRecurrence(testrecurrence);
        cdao.setRecurrenceCalculator(1);
        cdao.setRecurrenceID(1);
        long pass_one_start = System.currentTimeMillis();
        for (int a = 0; a < TEST_PASS; a++) {
            CalendarRecurringCollection.fillDAO(cdao);
        }
        long pass_one_end = System.currentTimeMillis();
        long pass_one_time = pass_one_end - pass_one_start;
        
        long pass_two_start = System.currentTimeMillis();
        for (int a = 0; a < TEST_PASS_HOT_SPOT; a++) {
            CalendarRecurringCollection.fillDAO(cdao);
        }
        long pass_two_end = System.currentTimeMillis();
        long pass_two_time = pass_two_end - pass_two_start;
        
        String check = CalendarRecurringCollection.createDSString(cdao);        
        assertTrue("Checking daily sequence", check.equals(testrecurrence));
        RecurringResults m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Check calculation", 5, m.size());
        m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 1);
        assertEquals("Check calculation", 1, m.size());
        
        double percent = pass_two_time/10;
        percent = pass_one_time * 100 / percent;
        
        
        //System.out.println("test_one:test_two in millisecons: "+pass_one_time + ":"+pass_two_time + " faster%:: "+percent);
        
    }
    
    
    public void testDailyRecurringWithDAO() throws Throwable {
        long s = 1149768000000L; // 08.06.2006 12:00 (GMT)
        long e = 1149771600000L; // 08.06.2006 13:00 (GMT)
        long u = 1150156800000L; // 13.06.2006 00:00 (GMT)
        long u_test = 1150106400000L; // 12.06.2006 10:00 (GMT)
        String testrecurrence = "ds|daily|daily_value|1|ds_start|"+s+"|ds_ends|"+u+"|";
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u_test));
        cdao.setTitle("Daily Appointment Test only with DAO");
        cdao.setRecurrenceCalculator(1);
        cdao.setRecurrenceID(1);
        cdao.setRecurrenceType(OXCalendar.DAILY);
        cdao.setInterval(1);
        for (int a = 0; a < TEST_PASS; a++) {
            CalendarRecurringCollection.fillDAO(cdao);
        }
        String check = CalendarRecurringCollection.createDSString(cdao);        
        assertTrue("Checking daily sequence", check.equals(testrecurrence));
        RecurringResults m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Check calculation", 5, m.size());
        m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 1);
        assertEquals("Check calculation", 1, m.size());
    }    
    
    public void no_testWholeDay() throws Throwable { // TODO: Need connection 
        long s = 1149768000000L; // 08.06.2006 12:00 (GMT)
        long e = 1149771600000L; // 08.06.2006 13:00 (GMT)
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(new RdbContextWrapper(1));
        cdao.setObjectID(1);
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setFullTime(true);
        cdao.setTitle("Simple Whole Day Test");
        CalendarOperation co = new CalendarOperation();
        Connection readcon = null;
        
        Context context = ContextStorage.getInstance().getContext("defaultcontext");
        
        readcon = DBPool.pickupWriteable(context);
        assertFalse("Checking for update", co.prepareUpdateAction(cdao, 1, readcon, -1));
        long realstart = 1149724800000L;
        assertEquals("Testing start time", cdao.getStartDate().getTime(), realstart);
        assertEquals("Testing end time", cdao.getEndDate().getTime(), realstart+CalendarRecurringCollection.MILLI_DAY);
        
        DBPool.pushWrite(context, readcon);
        
    }
    
    public void testPerformance() throws Throwable {
        CalendarDataObject cdao = new CalendarDataObject();
        long s = 1149768000000L; // 08.06.2006 12:00 (GMT)
        long e = 1244458800000L; // 08.06.2009 13:00:00 (GMT)
        long u = 253402210800000L; // 31.12.9999 00:00:00 (GMT)
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u));
        cdao.setTitle("Daily Appointment Test");
        cdao.setRecurrenceType(OXCalendar.DAILY);
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
        
        cdao.setRecurrenceType(OXCalendar.WEEKLY);
        cdao.setDays(OXCalendar.MONDAY + OXCalendar.TUESDAY + OXCalendar.WEDNESDAY + OXCalendar.THURSDAY + OXCalendar.FRIDAY + OXCalendar.SATURDAY + OXCalendar.SUNDAY);
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
    
    
}