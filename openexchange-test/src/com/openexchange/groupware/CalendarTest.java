
package com.openexchange.groupware;


import com.openexchange.api.OXFolder;
import com.openexchange.api2.FolderSQLInterface;
import com.openexchange.api2.RdbFolderSQLInterface;
import com.openexchange.groupware.CalendarRecurringCollection;
import com.openexchange.groupware.calendar.CalendarCommonCollection;
import com.openexchange.groupware.calendar.FreeBusyResults;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.contexts.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.api.OXCalendar;
import com.openexchange.groupware.calendar.RecurringResults;
import com.openexchange.groupware.calendar.RecurringResult;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;
import com.openexchange.tools.OXFolderTools;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAction;
import com.openexchange.tools.oxfolder.OXFolderPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import junit.framework.TestCase;

public class CalendarTest extends TestCase {
    
    
    private final static int TEST_PASS = 9999;
    private final static int TEST_PASS_HOT_SPOT = 99999;
    public static final long SUPER_END = 253402210800000L; // 31.12.9999 00:00:00 (GMT)
    
    private static int userid = 11; // bishoph
    public final static int contextid = 1;
    
    private static boolean init = false;
    
    protected void setUp() throws Exception {        
        super.setUp();
        userid = getUserId();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    private static Properties getAJAXProperties() {
        Properties properties = Init.getAJAXProperties();
        return properties;
    }        
    
    private static int resolveUser(String user) throws Exception {
        UserStorage uStorage = UserStorage.getInstance(getContext());
        userid = uStorage.getUserId(user);
        return userid;
    }
    
    public static int getUserId() throws Exception {
        if (!init) {
            Init.initDB();
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
        Connection readcon = DBPool.pickupWriteable(context);
        privatefolder = new Integer(OXFolderTools.getCalendarStandardFolder(userid, context, readcon)).intValue();
        DBPool.pushWrite(context, readcon);
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
        cdao.setRecurrenceType(OXCalendar.DAILY);        
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
        String testrecurrence = "t|1|i|1|s|"+s+"|e|"+u+"|";
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
    
    public void testWholeDay() throws Throwable { // TODO: Need connection 
        long s = 1149768000000L; // 08.06.2006 12:00 (GMT)
        long e = 1149771600000L; // 08.06.2006 13:00 (GMT)
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(new ContextImpl(contextid));

        cdao.setObjectID(1);
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setFullTime(true);
        cdao.setTitle("Simple Whole Day Test");
        CalendarOperation co = new CalendarOperation();
        Connection readcon = null;
        
        ContextStorage cs = ContextStorage.getInstance();
        Context context = cs.getContext(cs.getContextId("defaultcontext"));
        
        readcon = DBPool.pickupWriteable(context);
        
        int privatefolder = OXFolderTools.getCalendarStandardFolder(userid, cdao.getContext(), readcon);
        
        assertFalse("Checking for update", co.prepareUpdateAction(cdao, 1, readcon, privatefolder));
        long realstart = 1149724800000L;
        assertEquals("Testing start time", cdao.getStartDate().getTime(), realstart);
        assertEquals("Testing end time", cdao.getEndDate().getTime(), realstart+CalendarRecurringCollection.MILLI_DAY);
        
        DBPool.pushWrite(context, readcon);
        
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
        
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u));        
        cdao.setTitle("testSaveRecurring");
        cdao.setRecurrenceType(OXCalendar.DAILY);
        cdao.setRecurrenceCalculator(1);
        cdao.setInterval(1);
        cdao.setDays(1);
        cdao.setRecurrenceID(1);
        
        cdao.setParentFolderID(OXFolderTools.getStandardFolder(userid, OXFolder.CALENDAR, context));
        
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        
        CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        
        RecurringResults rss = CalendarRecurringCollection.calculateRecurring(cdao, cals, u, 0);
        assertEquals("Testing size ", rss.size(), 10);
        for (int a = 0; a < rss.size(); a++) {
            RecurringResult rs = rss.getRecurringResult(a);
            assertEquals("Testing start time", rs.getStart(), saves);
            assertEquals("Testing end time", rs.getEnd(), savee);
            saves += CalendarRecurringCollection.MILLI_DAY;
            savee += CalendarRecurringCollection.MILLI_DAY;
        }
    }    
    
    public static final void fillDatesInDao(CalendarDataObject cdao) {
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
        
        cdao.setStartDate(new Date(s));
        cdao.setEndDate(new Date(e));
        cdao.setUntil(new Date(u));
        
    }
    
    public void testBasicSearch() throws Exception  {
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        CalendarSql csql = new CalendarSql(so);
        int cols[] = new int[1];
        cols[0] = AppointmentObject.TITLE;
        SearchIterator si = csql.searchAppointments("test", getPrivateFolder(), 0, "ASC", cols);
        boolean gotresults = si.hasNext();
        assertTrue("Got real results", gotresults);
        while (si.hasNext()) {
            CalendarDataObject cdao = (CalendarDataObject)si.next();
        }
    }
    
    public void testfillUserParticipantsWithoutGroups() throws Exception  {
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(getContext());
        Participants participants = new Participants();
        Participant p = new UserParticipant();
        p.setIdentifier(userid);
        participants.add(p);
 
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        
        Participant p2 = new UserParticipant();
        p2.setIdentifier(resolveUser(user2));
        participants.add(p2);
        
        cdao.setParticipants(participants.getList());
        CalendarOperation.fillUserParticipants(cdao);
   
        assertEquals("Check participant size", cdao.getParticipants().length, cdao.getUsers().length);
    }
    
    public void testFreeBusy() throws Exception  {
        Connection readcon = DBPool.pickupWriteable(getContext());
        CalendarMySQL calmysql = new CalendarMySQL();
        PreparedStatement prep = calmysql.getFreeBusy(userid, getContext(), new Date(0), new Date(SUPER_END), readcon);
        ResultSet rs = calmysql.getResultSet(prep);
        SearchIterator fbr = new FreeBusyResults(rs);   
        int counter = 0;
        while (fbr.hasNext()) {
            CalendarDataObject cdao = (CalendarDataObject)fbr.next();            
            assertTrue(cdao.containsShownAs());
            assertTrue(cdao.containsStartDate());
            assertTrue(cdao.containsEndDate());
            counter++;
        }        
        DBPool.pushWrite(getContext(), readcon);
        
    }    
    
    
    public void testInsertMoveAndDeleteAppointments() throws Throwable {
        Context context = new ContextImpl(contextid);
        CalendarDataObject cdao = new CalendarDataObject();

        cdao.setTitle("testMove - Step 1 - Insert");
        cdao.setParentFolderID(OXFolderTools.getStandardFolder(userid, OXFolder.CALENDAR, context));
        
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        
        fillDatesInDao(cdao);
        
        CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        int private_folder_id = cdao.getEffectiveFolderId();
        
        int object_id = cdao.getObjectID();
 
        Connection readcon = DBPool.pickup(context);
        Connection writecon = DBPool.pickupWriteable(context);
        
        OXFolderAction ofa = new OXFolderAction(so);
        OCLPermission oclp = new OCLPermission();
        oclp.setEntity(userid);
        oclp.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp.setFolderAdmin(true);
        FolderObject fo = new FolderObject();
        fo.setFolderName("MyTestFolder");
        fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PUBLIC);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp });
        ofa.createFolder(fo, so, true, readcon, writecon, false);
        int public_folder_id = fo.getObjectID();
        
        // TODO: "Move" folder to a public folder
        CalendarDataObject update1 = new CalendarDataObject();
        update1.setContext(so.getContext());
        update1.setObjectID(object_id);
        update1.setParentFolderID(private_folder_id);
        update1.setTitle("testMove - Step 2 - Update");
        csql.updateAppointmentObject(update1, public_folder_id, new Date(SUPER_END));
        
        // TODO: LoadObject by ID and make some tests
        CalendarDataObject testobject = csql.getObjectById(object_id, public_folder_id);
        UserParticipant up[] = testobject.getUsers();
        for (int a = 0; a < up.length; a++) {
            assertTrue("check that folder id IS NULL", up[a].getPersonalFolderId() == UserParticipant.NO_PFID);
        }
        
        // TODO: Move again to private folder

        CalendarDataObject update2 = csql.getObjectById(object_id, public_folder_id);
        
        update2.setTitle("testMove - Step 3 - Update");
        update2.setParentFolderID(public_folder_id);
        csql.updateAppointmentObject(update2, private_folder_id, new Date(SUPER_END));        
        
        // TODO: LoadObject by ID and make some tests

        CalendarDataObject testobject2 = csql.getObjectById(object_id, private_folder_id);
        
        assertEquals("Test folder id ", testobject2.getEffectiveFolderId(), private_folder_id);
        
        UserParticipant up2[] = testobject2.getUsers();
        
        assertEquals("check length ", up2.length, 1);
        
        for (int a = 0; a < up2.length; a++) {
            assertEquals("check that folder id private folder ", up2[a].getPersonalFolderId(), private_folder_id);
        }        
        
        // TODO: Move again to public folder and delete complete folder
        
        CalendarDataObject update3 = csql.getObjectById(object_id, private_folder_id);
        
        update3.setTitle("testMove - Step 4 - Update");
        update3.setParentFolderID(private_folder_id);
        csql.updateAppointmentObject(update3, public_folder_id, new Date(SUPER_END));        
        
        ofa.deleteFolder(public_folder_id, so, true, SUPER_END);

        try {
            DBPool.push(context, readcon);
            DBPool.pushWrite(context, writecon);        
        } catch(Exception ignore) { 
            ignore.printStackTrace();
        }
                
        try {
            testobject = csql.getObjectById(object_id, public_folder_id);
            throw new Exception("Object not deleted! Test failed!");
        } catch (Exception not_exist) {
            // this is normal because the object has been deleted before
        }

    }      
    
    
    
}