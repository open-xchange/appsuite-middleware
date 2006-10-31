
package com.openexchange.groupware;


import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import junit.framework.TestCase;

import com.openexchange.event.EventConfigImpl;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarRecurringCollection;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.calendar.RecurringResult;
import com.openexchange.groupware.calendar.RecurringResults;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.DBPool;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAction;
import com.openexchange.tools.oxfolder.OXFolderTools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import junit.framework.TestCase;


public class CalendarRecurringTests extends TestCase {
     
    public static final long SUPER_END = 253402210800000L; // 31.12.9999 00:00:00 (GMT)
    
    private static int userid = 11; // bishoph
    public final static int contextid = 1;
    
    private static boolean init = false;
    
    protected void setUp() throws Exception {        
        super.setUp();
        EventConfigImpl event = new EventConfigImpl();
        event.setEventQueueEnabled(false);
        this.userid = getUserId();
    }
    
    protected void tearDown() throws Exception {
        if (init) {
            init = false;
            Init.stopDB();
        }
        super.tearDown();
    }
    
    private static Properties getAJAXProperties() {
        Properties properties = Init.getAJAXProperties();
        return properties;
    }        
    
    private static int resolveUser(String u) throws Exception {
        UserStorage uStorage = UserStorage.getInstance(getContext());
        return uStorage.getUserId(u);
    }
    
    public static int getUserId() throws Exception {
        if (!init) {
            Init.initDB();
            init = true;
        }
        String user = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant2", "");        
        return resolveUser(user);
    }
    
    public static Context getContext() {
        return new ContextImpl(contextid);
    }
    
    public static int getPrivateFolder() throws Exception {
        int privatefolder = 0;
        Context context = getContext();
        Connection readcon = DBPool.pickup(context);
        privatefolder = new Integer(OXFolderTools.getCalendarStandardFolder(userid, context, readcon)).intValue();
        DBPool.push(context, readcon);
        return privatefolder;        
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
        cdao.setRecurrenceType(CalendarObject.DAILY);        
        assertFalse(cdao.calculateRecurrence());
        cdao.setRecurrenceCalculator(1);        
        assertFalse(cdao.calculateRecurrence());
        cdao.setInterval(1);
        assertTrue(cdao.calculateRecurrence());
    }
    
    public void testBasicRecurringWithoutUntil() throws Throwable {
        CalendarDataObject cdao = new CalendarDataObject();      
        assertFalse(cdao.calculateRecurrence());
        cdao.setStartDate(new Date(0));
        cdao.setEndDate(new Date(0));        
        cdao.setTitle("Basic Recurring Test");
        cdao.setRecurrenceID(1);
        assertFalse(cdao.calculateRecurrence());
        cdao.setRecurrenceType(CalendarObject.DAILY);        
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
        String testrecurrence = "t|1|i|1|s|"+s+"|e|"+u+"|";
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u_test));
        cdao.setTitle("Daily Appointment Test");
        cdao.setRecurrence(testrecurrence);
        cdao.setRecurrenceCalculator(1);
        cdao.setRecurrenceID(1);
        long pass_one_start = System.currentTimeMillis();

        CalendarRecurringCollection.fillDAO(cdao);
        
        long pass_one_end = System.currentTimeMillis();
        long pass_one_time = pass_one_end - pass_one_start;
        
        long pass_two_start = System.currentTimeMillis();

        CalendarRecurringCollection.fillDAO(cdao);
        
        long pass_two_end = System.currentTimeMillis();
        long pass_two_time = pass_two_end - pass_two_start;
        
        String check = CalendarRecurringCollection.createDSString(cdao);        
        assertTrue("Checking daily sequence", check.equals(testrecurrence));
        RecurringResults m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Check calculation", 6, m.size());
        m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 1);
        assertEquals("Check calculation", 1, m.size());
        
        double percent = pass_two_time/10;
        percent = pass_one_time * 100 / percent;
        
    }    
    
    public void testDailyByPosition() throws Throwable {
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("Daily Appointment By Position Test");
        CalendarTest.fillDatesInDao(cdao);        
        cdao.setRecurrenceType(CalendarObject.DAILY);
        cdao.setInterval(1);
        CalendarRecurringCollection.fillDAO(cdao);        
        RecurringResults rrs = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
        for (int a = 0; a < rrs.size(); a++) {
            RecurringResults rrs_check = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, rrs.getRecurringResult(a).getPosition());
            RecurringResult rr = rrs_check.getRecurringResult(0);
            assertEquals("Check correct recurrence position", rrs.getRecurringResult(a).getPosition(), rr.getPosition());
            assertEquals("Check correct start time", rrs.getRecurringResult(a).getStart(), rr.getStart());
            assertTrue("Check correct position calculation", a+1 == rr.getPosition());            
        }
    }        
    
    
    public void testDailyRecurringWithDAO() throws Throwable {
        long s = 1149768000000L; // 08.06.2006 12:00 (GMT)
        long e = 1149771600000L; // 08.06.2006 13:00 (GMT)
        long u = 1150156800000L; // 13.06.2006 00:00 (GMT)
        long u_test = 1150106400000L; // 12.06.2006 10:00 (GMT)
        String testrecurrence = "t|1|i|1|s|"+s+"|e|"+u+"|";
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u_test));
        cdao.setTitle("Daily Appointment Test only with DAO");
        cdao.setRecurrenceCalculator(1);
        cdao.setRecurrenceID(1);
        cdao.setRecurrenceType(CalendarObject.DAILY);
        cdao.setInterval(1);
        CalendarRecurringCollection.fillDAO(cdao);
        String check = CalendarRecurringCollection.createDSString(cdao);        
        assertTrue("Checking daily sequence", check.equals(testrecurrence));
        RecurringResults m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Check calculation", 6, m.size()); 
        
        m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 1);
        assertEquals("Check calculation", 1, m.size());
    }       
    
    
    public void testWeeklyRecurrence() throws Throwable {
        
//
//      Juni 2006
//So Mo Di Mi Do Fr Sa 
//             1  2  3
// 4  5  6  7  8  9 10
//11 12 13 14 15 16 17
//18 19 20 21 22 23 24
//25 26 27 28 29 30 
       
        long s = 1149768000000L; // 08.06.2006 12:00 (GMT)
        long e = 1149771600000L; // 08.06.2006 13:00 (GMT)
        long u = 1151100000000L; // 24.06.2006 00:00 (GMT)
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
        RecurringResults m = null;
        CalendarDataObject cdao = null;
        
        // MONDAY
        cdao = new CalendarDataObject();
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u));
        cdao.setTitle("Weekly Appointment Test");
        cdao.setRecurrenceCalculator(1);
        cdao.setRecurrenceID(1);
        cdao.setRecurrenceType(CalendarObject.WEEKLY);
        cdao.setInterval(1);        
        cdao.setDays(AppointmentObject.MONDAY);
        CalendarRecurringCollection.fillDAO(cdao);        
        m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 1);
        c.setTimeInMillis(new Date(m.getRecurringResult(0).getStart()).getTime());
        assertEquals("First day check (MONDAY)", c.get(Calendar.DAY_OF_WEEK), Calendar.MONDAY);
        
        
        // SUNDAY
        cdao = new CalendarDataObject();
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u));
        cdao.setTitle("Weekly Appointment Test");
        cdao.setRecurrenceCalculator(1);
        cdao.setRecurrenceID(1);
        cdao.setRecurrenceType(CalendarObject.WEEKLY);
        cdao.setInterval(1);        
        cdao.setDays(AppointmentObject.SUNDAY);
        CalendarRecurringCollection.fillDAO(cdao);        
        m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 1);
        c.setTimeInMillis(new Date(m.getRecurringResult(0).getStart()).getTime());
        assertEquals("First day check (SUNDAY)", c.get(Calendar.DAY_OF_WEEK), Calendar.SUNDAY);        
        
        // TUESDAY
        cdao = new CalendarDataObject();
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u));
        cdao.setTitle("Weekly Appointment Test");
        cdao.setRecurrenceCalculator(1);
        cdao.setRecurrenceID(1);
        cdao.setRecurrenceType(CalendarObject.WEEKLY);
        cdao.setInterval(1);        
        cdao.setDays(AppointmentObject.TUESDAY);
        CalendarRecurringCollection.fillDAO(cdao);        
        m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 1);
        c.setTimeInMillis(new Date(m.getRecurringResult(0).getStart()).getTime());
        assertEquals("First day check (TUESDAY)", c.get(Calendar.DAY_OF_WEEK), Calendar.TUESDAY);          
        
        
        // WEDNESDAY
        cdao = new CalendarDataObject();
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u));
        cdao.setTitle("Weekly Appointment Test");
        cdao.setRecurrenceCalculator(1);
        cdao.setRecurrenceID(1);
        cdao.setRecurrenceType(CalendarObject.WEEKLY);
        cdao.setInterval(1);        
        cdao.setDays(AppointmentObject.WEDNESDAY);
        CalendarRecurringCollection.fillDAO(cdao);        
        m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 1);
        c.setTimeInMillis(new Date(m.getRecurringResult(0).getStart()).getTime());
        assertEquals("First day check (WEDNESDAY)", c.get(Calendar.DAY_OF_WEEK), Calendar.WEDNESDAY);         
        
        // THURSDAY
        cdao = new CalendarDataObject();
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u));
        cdao.setTitle("Weekly Appointment Test");
        cdao.setRecurrenceCalculator(1);
        cdao.setRecurrenceID(1);
        cdao.setRecurrenceType(CalendarObject.WEEKLY);
        cdao.setInterval(1);        
        cdao.setDays(AppointmentObject.THURSDAY);
        CalendarRecurringCollection.fillDAO(cdao);        
        m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 1);
        c.setTimeInMillis(new Date(m.getRecurringResult(0).getStart()).getTime());
        assertEquals("First day check (THURSDAY)", c.get(Calendar.DAY_OF_WEEK), Calendar.THURSDAY);         
        
        // FRIDAY
        cdao = new CalendarDataObject();
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u));
        cdao.setTitle("Weekly Appointment Test");
        cdao.setRecurrenceCalculator(1);
        cdao.setRecurrenceID(1);
        cdao.setRecurrenceType(CalendarObject.WEEKLY);
        cdao.setInterval(1);        
        cdao.setDays(AppointmentObject.FRIDAY);
        CalendarRecurringCollection.fillDAO(cdao);        
        m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 1);
        c.setTimeInMillis(new Date(m.getRecurringResult(0).getStart()).getTime());
        assertEquals("First day check (FRIDAY)", c.get(Calendar.DAY_OF_WEEK), Calendar.FRIDAY);         
        
        // SATURDAY
        cdao = new CalendarDataObject();
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u));
        cdao.setTitle("Weekly Appointment Test");
        cdao.setRecurrenceCalculator(1);
        cdao.setRecurrenceID(1);
        cdao.setRecurrenceType(CalendarObject.WEEKLY);
        cdao.setInterval(1);        
        cdao.setDays(AppointmentObject.SATURDAY);
        CalendarRecurringCollection.fillDAO(cdao);        
        m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 1);
        c.setTimeInMillis(new Date(m.getRecurringResult(0).getStart()).getTime());
        assertEquals("First day check (SATURDAY)", c.get(Calendar.DAY_OF_WEEK), Calendar.SATURDAY);           
        
        // MONDAY + THURSDAY
        cdao = new CalendarDataObject();
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u));
        cdao.setTitle("Weekly Appointment Test");
        cdao.setRecurrenceCalculator(1);
        cdao.setRecurrenceID(1);
        cdao.setRecurrenceType(CalendarObject.WEEKLY);
        cdao.setInterval(1);        
        cdao.setDays(AppointmentObject.MONDAY + AppointmentObject.THURSDAY);
        CalendarRecurringCollection.fillDAO(cdao);
        m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Check calculation", 5, m.size()); 
        
        c.setTimeInMillis(new Date(m.getRecurringResult(0).getStart()).getTime());
        assertEquals("First day check (THURSDAY)", c.get(Calendar.DAY_OF_WEEK), Calendar.THURSDAY);
        c.setTimeInMillis(new Date(m.getRecurringResult(1).getStart()).getTime());
        assertEquals("First day check (MONDAY)", c.get(Calendar.DAY_OF_WEEK), Calendar.MONDAY);
        
        
        // SUNDAY + MONDAY
        cdao = new CalendarDataObject();
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u));
        cdao.setTitle("Weekly Appointment Test");
        cdao.setRecurrenceCalculator(1);
        cdao.setRecurrenceID(1);
        cdao.setRecurrenceType(CalendarObject.WEEKLY);
        cdao.setInterval(1);        
        cdao.setDays(AppointmentObject.MONDAY + AppointmentObject.SUNDAY);
        CalendarRecurringCollection.fillDAO(cdao);
        m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Check calculation", 5, m.size()); 
        
        c.setTimeInMillis(new Date(m.getRecurringResult(0).getStart()).getTime());
        assertEquals("First day check (SUNDAY)", c.get(Calendar.DAY_OF_WEEK), Calendar.SUNDAY);
        c.setTimeInMillis(new Date(m.getRecurringResult(1).getStart()).getTime());
        assertEquals("First day check (MONDAY)", c.get(Calendar.DAY_OF_WEEK), Calendar.MONDAY);        
        
        
    }       
    
    
    public void testSaveRecurring() throws Throwable {
        Context context = new ContextImpl(contextid);

        CalendarDataObject cdao = new CalendarDataObject();
        
        long s = System.currentTimeMillis();
        long cals = s;
        long calsmod = s%CalendarRecurringCollection.MILLI_DAY;
        cals = cals- calsmod;
        long endcalc = 3600000;
        long mod = s%3600000;
        s = s - mod;
        long saves = s;
        long e = s + endcalc;
        long savee = e;
        long u = s + (CalendarRecurringCollection.MILLI_DAY * 10);
        mod = u%CalendarRecurringCollection.MILLI_DAY;
        u = u - mod;
        
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone(("Europe/Berlin")));
        start.setTimeInMillis(saves);
        Calendar ende = Calendar.getInstance(TimeZone.getTimeZone(("Europe/Berlin")));
        ende.setTimeInMillis(savee);
        
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u));        
        cdao.setTitle("testSaveRecurring");
        cdao.setRecurrenceType(CalendarObject.DAILY);
        cdao.setRecurrenceCalculator(1);
        cdao.setInterval(1);
        cdao.setDays(1);
        
        cdao.setParentFolderID(OXFolderTools.getStandardFolder(userid, FolderObject.CALENDAR, context));
        
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        cdao.setIgnoreConflicts(true);
        CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        
        RecurringResults rss = CalendarRecurringCollection.calculateRecurring(cdao, cals, u, 0);
        assertEquals("Testing size ", rss.size(), 10);
        for (int a = 0; a < rss.size(); a++) {
            RecurringResult rs = rss.getRecurringResult(a);
            assertEquals("Testing start time", rs.getStart(), start.getTimeInMillis());
            assertEquals("Testing end time", rs.getEnd(), ende.getTimeInMillis());
            assertEquals("Testing Position", a+1, rs.getPosition());
            start.add(Calendar.DAY_OF_MONTH, 1);
            ende.add(Calendar.DAY_OF_MONTH, 1);
        }
    }        

    public void testDeleteSingleRecurringAppointment() throws Throwable {
        Context context = new ContextImpl(contextid);
        CalendarDataObject cdao = new CalendarDataObject();
        
        long s = System.currentTimeMillis();
        long cals = s;
        long calsmod = s%CalendarRecurringCollection.MILLI_DAY;
        cals = cals- calsmod;
        long endcalc = 3600000;
        long mod = s%3600000;
        s = s - mod;
        long saves = s;
        long e = s + endcalc;
        long savee = e;
        long u = s + (CalendarRecurringCollection.MILLI_DAY * 10);
        mod = u%CalendarRecurringCollection.MILLI_DAY;
        u = u - mod;
        
        
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone(("Europe/Berlin")));
        start.setTimeInMillis(saves);
        Calendar ende = Calendar.getInstance(TimeZone.getTimeZone(("Europe/Berlin")));
        ende.setTimeInMillis(savee);        
        
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        int uid2 = resolveUser(user2);        
        
        int folder_id = OXFolderTools.getStandardFolder(userid, FolderObject.CALENDAR, context);
        int folder_id2 = OXFolderTools.getStandardFolder(uid2, FolderObject.CALENDAR, context);        
        
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u));        
        cdao.setTitle("testDeleteSingleRecurringAppointment - step 1 - insert ");
        cdao.setRecurrenceType(CalendarObject.DAILY);
        cdao.setRecurrenceCalculator(1);
        cdao.setInterval(1);
        cdao.setDays(1);
        
        cdao.setParentFolderID(folder_id);
        
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");        
        SessionObject so2 = SessionObjectWrapper.createSessionObject(uid2, context.getContextId(), "myTestIdentifier");
        
        Participants p = new Participants();        
        Participant pa = new UserParticipant();
        pa.setIdentifier(userid);
        p.add(pa);

        Participant pa2 = new UserParticipant();
        pa2.setIdentifier(uid2);
        p.add(pa2);
        
        cdao.setParticipants(p.getList());
        
        cdao.setContext(so.getContext());
        cdao.setIgnoreConflicts(true);
        CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        int object_id = cdao.getObjectID();
        Date last = cdao.getLastModified();
        
        RecurringResults rss = CalendarRecurringCollection.calculateRecurring(cdao, cals, u, 0);
        assertEquals("Testing size ", rss.size(), 10);
        for (int a = 0; a < rss.size(); a++) {
            RecurringResult rs = rss.getRecurringResult(a);
            assertEquals("Testing start time", rs.getStart(), start.getTimeInMillis());
            assertEquals("Testing end time", rs.getEnd(), ende.getTimeInMillis());
            assertEquals("Testing Position", a+1, rs.getPosition());
            start.add(Calendar.DAY_OF_MONTH, 1);
            ende.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // Delete (virtually) a single app in the sequence
        CalendarDataObject test_delete = csql.getObjectById(object_id, folder_id);        
        rss = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Testing size ", rss.size(), 11);
        
        CalendarDataObject delete_owner = new CalendarDataObject();
        delete_owner.setContext(so.getContext());
        delete_owner.setRecurrencePosition(3);
        delete_owner.setObjectID(object_id);
        csql.deleteAppointmentObject(delete_owner, folder_id, new Date(SUPER_END));
        
        CalendarDataObject test_object = csql.getObjectById(object_id, folder_id);        
        rss = CalendarRecurringCollection.calculateRecurring(test_object, 0, 0, 0);
        assertEquals("Testing size after deleteing single app from sequence", rss.size(), 10);
        
        rss = CalendarRecurringCollection.calculateRecurring(test_object, 0, 0, 3);
        assertEquals("Testing size after requesting single deleted app from sequence", rss.size(), 0);
        
        // Now we delete a virtual exception and we are not the owner 
        
        CalendarSql csql2 = new CalendarSql(so2);
        CalendarDataObject test_delete_not_owner = new CalendarDataObject();
        test_delete_not_owner.setContext(so.getContext());
        test_delete_not_owner.setObjectID(object_id);
        test_delete_not_owner.setRecurrencePosition(5);
        csql2.deleteAppointmentObject(test_delete_not_owner, folder_id2, new Date(SUPER_END));
        
        CalendarDataObject test_master_object = csql.getObjectById(object_id, folder_id);
        UserParticipant up[] = test_master_object.getUsers();
        assertEquals("Testing participants in master object", up.length, 2);
        
        int cols[] = new int[] { AppointmentObject.TITLE,  AppointmentObject.OBJECT_ID, AppointmentObject.RECURRENCE_ID, AppointmentObject.RECURRENCE_POSITION, AppointmentObject.RECURRENCE_TYPE, AppointmentObject.DELETE_EXCEPTIONS, AppointmentObject.CHANGE_EXCEPTIONS };
        
        SearchIterator si = csql.getModifiedAppointmentsInFolder(folder_id, cols, last);
        
        boolean found_exception = false;
        while (si.hasNext()) {
            CalendarDataObject tcdao = (CalendarDataObject)si.next();
            if (tcdao.getRecurrenceID() == object_id && tcdao.getObjectID() != object_id) {
                // found the single exception we have just created
                found_exception = true;
                Date test_deleted_exceptions[] = tcdao.getDeleteException();
                Date test_changed_exceptions[] = tcdao.getChangeException();
                assertTrue("Test deleted exception is NULL" , test_deleted_exceptions == null);
                assertTrue("Test changed exception is NULL" , test_changed_exceptions == null);
                assertEquals("Check correct recurrence position", 5, tcdao.getRecurrencePosition());
            } else {                
                Date test_deleted_exceptions[] = tcdao.getDeleteException();
                Date test_changed_exceptions[] = tcdao.getChangeException();
                assertTrue("Test deleted exception is NULL" , test_deleted_exceptions == null);
                assertTrue("Test changed exception is ! NULL" , test_changed_exceptions != null);
                assertTrue("Test changed exception is 1" , test_changed_exceptions.length == 1);
                assertEquals("Check master recurrence position", 0, tcdao.getRecurrencePosition());
            }
        }
        
        assertTrue("Found created exception ", found_exception);
        
        // delete whole sequence incl. all exceptions
        CalendarDataObject delete_all = new CalendarDataObject();
        delete_all.setContext(so.getContext());        
        delete_all.setObjectID(object_id);
        csql.deleteAppointmentObject(delete_all, folder_id, new Date(SUPER_END));
        
        si = csql.getModifiedAppointmentsInFolder(folder_id, cols, last);
        while (si.hasNext()) {
            CalendarDataObject tcdao = (CalendarDataObject)si.next();
            assertFalse("Object should not exists anymore ", tcdao.getRecurrenceID() == object_id);
        }
        
    }          
    
    
    
    
    
    
}
