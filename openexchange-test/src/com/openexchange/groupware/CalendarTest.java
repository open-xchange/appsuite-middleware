
package com.openexchange.groupware;


import com.openexchange.groupware.calendar.OXCalendarException;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import junit.framework.TestCase;

import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXException;
import com.openexchange.api2.ReminderSQLInterface;
import com.openexchange.event.EventConfigImpl;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarOperation;
import com.openexchange.groupware.calendar.CalendarRecurringCollection;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.calendar.CalendarSqlImp;
import com.openexchange.groupware.calendar.ConflictHandler;
import com.openexchange.groupware.calendar.FreeBusyResults;
import com.openexchange.groupware.calendar.RecurringResult;
import com.openexchange.groupware.calendar.RecurringResults;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.Participants;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderManagerImpl;

public class CalendarTest extends TestCase {
    
    
    private final static int TEST_PASS = 1;
    private final static int TEST_PASS_HOT_SPOT = 1;
    public static final long SUPER_END = 253402210800000L; // 31.12.9999 00:00:00 (GMT)
    
    private static int userid = 11; // bishoph
    public final static int contextid = 1;
    
    private static boolean init = false;
    
    private static boolean do_not_delete = false;
    
    static int cols[] = new int[] { AppointmentObject.START_DATE, AppointmentObject.END_DATE, AppointmentObject.TITLE, AppointmentObject.RECURRENCE_ID, AppointmentObject.RECURRENCE_POSITION, AppointmentObject.OBJECT_ID, AppointmentObject.FOLDER_ID, AppointmentObject.USERS, AppointmentObject.FULL_TIME };
    
    protected void setUp() throws Exception {        
        super.setUp();
        EventConfigImpl event = new EventConfigImpl();
        event.setEventQueueEnabled(false);
        userid = getUserId();
        ContextStorage.init();
    }
    
    protected void tearDown() throws Exception {
        if (init) {
            init = false;
            Init.stopServer();
        }
        super.tearDown();
    }
    
    private static Properties getAJAXProperties() {
        Properties properties = Init.getAJAXProperties();
        return properties;
    }        
    
    private static int resolveUser(String user) throws Exception {
        UserStorage uStorage = UserStorage.getInstance();
        return uStorage.getUserId(user, getContext());
    }
    
    public static void dontDelete() {
        do_not_delete = true;
    }
    
    public static int getUserId() throws Exception {
        if (!init) {
            Init.startServer();
            init = true;
        }
        String user = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant2", "");        
        return resolveUser(user);
    }
    
    public static Context getContext() {
        return new ContextImpl(contextid);
    }
    
    static final void fillDatesInDao(CalendarDataObject cdao) {
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
    
    public static int getPrivateFolder() throws Exception {
        int privatefolder = 0;
        Context context = getContext();
        Connection readcon = DBPool.pickup(context);
        privatefolder = getCalendarDefaultFolderForUser(userid, context);
        DBPool.push(context, readcon);
        return privatefolder;
    }
    
    public static int getCalendarDefaultFolderForUser(int userid, Context context) throws OXException {
        OXFolderAccess access = new OXFolderAccess(context);
        FolderObject fo = access.getDefaultFolder(userid, FolderObject.CALENDAR);
        return fo.getObjectID();
    }
    
    private void testDelete(CalendarDataObject cdao) throws Exception {        
        Connection writecon = DBPool.pickupWriteable(getContext());
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "delete test");
        CalendarSql csql = new CalendarSql(so);
        CalendarDataObject deleteit = new CalendarDataObject();
        deleteit.setContext(cdao.getContext());
        deleteit.setObjectID(cdao.getObjectID());
        int fid = cdao.getEffectiveFolderId();
        try {
            if (fid == 0) {
                int x = 0;
            }
            csql.deleteAppointmentObject(deleteit, fid, new Date(SUPER_END));
        } catch(Exception e) { 
            e.printStackTrace();
        }
        DBPool.pushWrite(context, writecon);
    }    

    void deleteAllAppointments() throws Exception  {
        if (do_not_delete) {
            return;
        }
        
        Connection readcon = DBPool.pickup(getContext());
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "deleteAllApps");
        CalendarSql csql = new CalendarSql(so);        
        SearchIterator si = csql.getAppointmentsBetween(userid, new Date(0), new Date(SUPER_END), cols, 0,  null);
        while (si.hasNext()) {
            CalendarDataObject cdao = (CalendarDataObject)si.next();
            testDelete(cdao);
        }
        si.close();
        DBPool.push(context, readcon);                
    }
    
    /* ----- test cases -------*/   
    
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
        
        readcon = DBPool.pickup(context);
        
        int privatefolder = getCalendarDefaultFolderForUser(userid, context);
        
        assertFalse("Checking for update", co.prepareUpdateAction(cdao, cdao, 1, privatefolder, "Europe/Berlin"));
        long realstart = 1149724800000L;
        assertEquals("Testing start time", cdao.getStartDate().getTime(), realstart);
        assertEquals("Testing end time", cdao.getEndDate().getTime(), realstart+CalendarRecurringCollection.MILLI_DAY);
        DBPool.push(context, readcon);
        
    }
    
    
    public void testWholeDayWithDB() throws Throwable {
        Context context = new ContextImpl(contextid);
        int fid = getCalendarDefaultFolderForUser(userid, context);
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testWholeDayWithDB - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        cdao.setIgnoreConflicts(true);
        cdao.setFullTime(true);
        cdao.setGlobalFolderID(fid);
        fillDatesInDao(cdao);
        CalendarSql csql = new CalendarSql(so);        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();    
        
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        long check_start = cdao.getStartDate().getTime();
        long check_end = cdao.getEndDate().getTime();
        
        CalendarDataObject update = new CalendarDataObject();
        update.setContext(so.getContext());
        update.setObjectID(object_id);
        update.setTitle("testWholeDayWithDB - Step 1 - Update");
        
        csql.updateAppointmentObject(update, fid, new Date(SUPER_END));
        
        CalendarDataObject testobject_update = csql.getObjectById(object_id, fid);
        assertTrue("Contains fulltime ", testobject_update.getFullTime());
        assertEquals("Check start time ", check_start, testobject_update.getStartDate().getTime());
        assertEquals("Check end time ", check_end, testobject_update.getEndDate().getTime());
        
    }
    
    public void testMultiSpanWholeDay() throws Throwable {
        int wanted_length = 3;
        Context context = new ContextImpl(contextid);
        int fid = getCalendarDefaultFolderForUser(userid, context);
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testMultiSpanWholeDay - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        cdao.setIgnoreConflicts(true);
        cdao.setFullTime(true);
        cdao.setGlobalFolderID(fid);
        fillDatesInDao(cdao);
        cdao.removeUntil();
        long start_date_long = CalendarRecurringCollection.normalizeLong(cdao.getStartDate().getTime());
        long end_date_long = (start_date_long + (CalendarRecurringCollection.MILLI_DAY * wanted_length));
        Date start_date = new Date(start_date_long);
        Date end_date = new Date(end_date_long);
        cdao.setStartDate(start_date);
        cdao.setEndDate(end_date);
        
        CalendarSql csql = new CalendarSql(so);        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();    
        
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        long check_start = cdao.getStartDate().getTime();
        long check_end = cdao.getEndDate().getTime();
        
        int test_length = (int)((check_end-check_start)/1000/60/60/24);
        
        assertEquals("Check app length ", wanted_length, test_length);

        
    }    
    
    public void testfillUserParticipantsWithoutGroups() throws Exception  {
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(getContext());
        Participants participants = new Participants();
        Participant p = new UserParticipant(userid);
        participants.add(p);
 
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        
        int uid2 = resolveUser(user2);
        Participant p2 = new UserParticipant(uid2);
        participants.add(p2);
        
        cdao.setParticipants(participants.getList());
        CalendarOperation.fillUserParticipants(cdao);
           
        assertEquals("Check participant size", cdao.getParticipants().length, cdao.getUsers().length);
    }
    
    public void testFreeBusy() throws Exception  {
        Connection readcon = DBPool.pickup(getContext());
        CalendarSqlImp calmysql = CalendarSql.getCalendarSqlImplementation();
        PreparedStatement prep = calmysql.getFreeBusy(userid, getContext(), new Date(0), new Date(SUPER_END), readcon);
        ResultSet rs = calmysql.getResultSet(prep);
        SearchIterator fbr = new FreeBusyResults(rs, prep,  getContext(), readcon, 0, 0);   
        int counter = 0;
        while (fbr.hasNext()) {
            CalendarDataObject cdao = (CalendarDataObject)fbr.next();            
            assertTrue(cdao.containsShownAs());
            assertTrue(cdao.containsStartDate());
            assertTrue(cdao.containsEndDate());
            counter++;
        }
        fbr.close();
    }    
    
    public void testInsertAndLabel() throws Throwable {
        Context context = new ContextImpl(contextid);
        int fid = getCalendarDefaultFolderForUser(userid, context);
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testInsertAndLabel - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        cdao.setIgnoreConflicts(true);
        fillDatesInDao(cdao);
        
        CalendarSql csql = new CalendarSql(so);        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();
        CalendarDataObject update = new CalendarDataObject();
        update.setContext(so.getContext());
        update.setObjectID(object_id);
        update.setIgnoreConflicts(true);
        cdao.setTitle("testInsertAndLabel - Step 2 - Update (only Label)");
        update.setLabel(3);
        csql.updateAppointmentObject(update, fid, new Date(SUPER_END));
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        assertEquals("Check label", 3, testobject.getLabel());
        
    }
    
    public void testNoAlarm() throws Throwable {
        Context context = new ContextImpl(contextid);
        int fid = getCalendarDefaultFolderForUser(userid, context);
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testNoAlarm - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        cdao.setIgnoreConflicts(true);
        fillDatesInDao(cdao);
        
        CalendarSql csql = new CalendarSql(so);        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();
        
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        assertTrue("Alarm should not be set", !testobject.containsAlarm());
        
        CalendarDataObject update = new CalendarDataObject();
        update.setContext(so.getContext());
        update.setObjectID(object_id);
        update.setTitle("testNoAlarm - Step 2 - Update");
        
        csql.updateAppointmentObject(update, fid, cdao.getLastModified());
        
        CalendarDataObject testobject2 = csql.getObjectById(object_id, fid);
        assertTrue("Alarm should not be set", !testobject2.containsAlarm());        
        
        CalendarDataObject update2 = (CalendarDataObject)testobject2.clone();
        update2.setContext(so.getContext());
        update2.setObjectID(object_id);
        update2.setTitle("testNoAlarm - Step 3 - Update 2");
        
        csql.updateAppointmentObject(update2, fid, cdao.getLastModified());
        
        CalendarDataObject testobject3 = csql.getObjectById(object_id, fid);
        assertTrue("Alarm should not be set", !testobject3.containsAlarm());
        
    }    

    public void testInsertAndAlarm() throws Throwable {
        Context context = new ContextImpl(contextid);
        int fid = getCalendarDefaultFolderForUser(userid, context);
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testInsertAndAlarm - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        cdao.setIgnoreConflicts(true);
        
        UserParticipant up = new UserParticipant(userid);
        up.setAlarmMinutes(5);
        cdao.setUsers(new UserParticipant[] { up });
        
        
        Participants participants = new Participants();
        Participant p = new UserParticipant(userid);
        participants.add(p);
 
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        
        Participant p2 = new UserParticipant(resolveUser(user2));
        participants.add(p2);        
        
        cdao.setParticipants(participants.getList());        
        
        fillDatesInDao(cdao);
        
        CalendarSql csql = new CalendarSql(so);        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        assertEquals("Check Alarm" , 5, testobject.getAlarm());
        
        CalendarDataObject cdao2 = new CalendarDataObject();
        cdao2.setTitle("testInsertAndAlarm(2) - Step 1 - Insert");
        cdao2.setParentFolderID(fid);
        
        cdao2.setContext(so.getContext());
        cdao2.setIgnoreConflicts(true);
        
        cdao2.setAlarm(5);
        
        fillDatesInDao(cdao2);
        
        cdao2.setParticipants(participants.getList());        
        
        csql.insertAppointmentObject(cdao2);        
        object_id = cdao2.getObjectID();
        CalendarDataObject testobject2 = csql.getObjectById(object_id, fid);
        assertEquals("Check Alarm" , 5, testobject2.getAlarm());        
        
        csql.deleteAppointmentObject(testobject2, fid, new Date(SUPER_END));
        
        ReminderSQLInterface rsql = new ReminderHandler(context);
        assertTrue("Check if reminder has been deleted", rsql.existsReminder(object_id, userid, Types.APPOINTMENT) == false);
        
        
    }

    public void testBasicSearch() throws Exception  {
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        
        Connection readcon = DBPool.pickup(context);
        Connection writecon = DBPool.pickupWriteable(context);        
        
        int folder_id = getPrivateFolder();
        
        //OXFolderAction ofa = new OXFolderAction(so);
        final OXFolderManager oxma = new OXFolderManagerImpl(so, readcon, writecon);
        OCLPermission oclp = new OCLPermission();
        oclp.setEntity(userid);
        oclp.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp.setFolderAdmin(true);
        FolderObject fo = new FolderObject();
        fo.setFolderName("MyTestFolder"+System.currentTimeMillis());
        fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PUBLIC);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp });
        int public_folder_id = 0;
        try {
            //ofa.createFolder(fo, so, true, readcon, writecon, false);
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            public_folder_id = fo.getObjectID();
            CalendarSql csql = new CalendarSql(so);            
            SearchIterator si = csql.searchAppointments("test", folder_id, 0, "ASC", cols);
            boolean gotresults = si.hasNext();
            assertTrue("Got real results by searching \"test\"", gotresults);
            while (si.hasNext()) {
                CalendarDataObject tcdao = (CalendarDataObject)si.next();
                assertTrue("Found only results with something like *test*", tcdao.getTitle().toLowerCase().indexOf("test") != -1);
                assertTrue("Got real folder id ", tcdao.getParentFolderID() == folder_id);
            }
            si.close();
            CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(context);
            cdao.setTitle("test Public Folder Search - Step 1 - Insert");
            cdao.setParentFolderID(public_folder_id);
            cdao.setIgnoreConflicts(true);
            
            fillDatesInDao(cdao);
            cdao.setIgnoreConflicts(true);

            csql.insertAppointmentObject(cdao);
            int test_folder_id = cdao.getEffectiveFolderId();
            int object_id = cdao.getObjectID();        

            assertEquals("Test correct folder id", public_folder_id, test_folder_id);

            si = csql.searchAppointments("*", public_folder_id, 0, "ASC", cols);
            gotresults = si.hasNext();
            assertTrue("Got real results by searching \"*\"", gotresults);
            while (si.hasNext()) {
                CalendarDataObject tcdao = (CalendarDataObject)si.next();
                assertTrue("Got real folder id ", tcdao.getParentFolderID() == public_folder_id);
            }              
            si.close();
            
            si = csql.searchAppointments("*.*", public_folder_id, 0, "ASC", cols);
            gotresults = si.hasNext();
            assertTrue("Got some results by searching \"*e*\"", !gotresults);
            si.close();
        } finally {
        	oxma.deleteFolder(new FolderObject(public_folder_id), true, SUPER_END);
            //ofa.deleteFolder(public_folder_id, so, true, SUPER_END);
        }
        
        
        try {
            DBPool.push(context, readcon);
            DBPool.pushWrite(context, writecon);        
        } catch(Exception ignore) { 
            ignore.printStackTrace();
        }
        
    }
    
    
    public void testInsertMoveAndDeleteAppointments() throws Throwable {
        Context context = new ContextImpl(contextid);
        CalendarDataObject cdao = new CalendarDataObject();

        cdao.setTitle("testInsertMoveAndDeleteAppointments - Step 1 - Insert");
        cdao.setParentFolderID(getCalendarDefaultFolderForUser(userid, context));
        
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        
        fillDatesInDao(cdao);
        cdao.setIgnoreConflicts(true);
        CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        int private_folder_id = cdao.getEffectiveFolderId();
        
        int object_id = cdao.getObjectID();
 
        Connection readcon = DBPool.pickup(context);
        Connection writecon = DBPool.pickupWriteable(context);
        
        //OXFolderAction ofa = new OXFolderAction(so);
        final OXFolderManager oxma = new OXFolderManagerImpl(so, readcon, writecon);
        OCLPermission oclp = new OCLPermission();
        oclp.setEntity(userid);
        oclp.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp.setFolderAdmin(true);
        FolderObject fo = new FolderObject();
        fo.setFolderName("MyTestFolder"+System.currentTimeMillis());
        fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PUBLIC);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp });
        //ofa.createFolder(fo, so, true, readcon, writecon, false);
        fo = oxma.createFolder(fo, true, System.currentTimeMillis());
        int public_folder_id = fo.getObjectID();
        CalendarDataObject testobject = null;
        try {
            // TODO: "Move" folder to a public folder
            CalendarDataObject update1 = new CalendarDataObject();
            update1.setContext(so.getContext());
            update1.setObjectID(object_id);
            update1.setParentFolderID(public_folder_id);
            update1.setTitle("testInsertMoveAndDeleteAppointments - Step 2 - Update");
            update1.setIgnoreConflicts(true);
            csql.updateAppointmentObject(update1, private_folder_id, new Date(SUPER_END));

            // TODO: LoadObject by ID and make some tests
            testobject = csql.getObjectById(object_id, public_folder_id);
            UserParticipant up[] = testobject.getUsers();
            for (int a = 0; a < up.length; a++) {
                assertTrue("check that folder id IS NULL", up[a].getPersonalFolderId() == UserParticipant.NO_PFID);
            }

            assertEquals("testInsertMoveAndDeleteAppointments - Step 2 - Update", update1.getTitle());

            // TODO: Move again to private folder

            CalendarDataObject update2 = csql.getObjectById(object_id, public_folder_id);

            update2.setTitle("testInsertMoveAndDeleteAppointments - Step 3 - Update");
            update2.setParentFolderID(private_folder_id);
            update2.setIgnoreConflicts(true);
            csql.updateAppointmentObject(update2, public_folder_id, new Date(SUPER_END));        

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

            update3.setTitle("testInsertMoveAndDeleteAppointments - Step 4 - Update");
            update3.setParentFolderID(public_folder_id);
            update3.setIgnoreConflicts(true);
            csql.updateAppointmentObject(update3, private_folder_id, new Date(SUPER_END));        
        } finally {
        	oxma.deleteFolder(new FolderObject(public_folder_id), true, SUPER_END);
            //ofa.deleteFolder(public_folder_id, so, true, SUPER_END);
        }

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
    

    
    public void testInsertMoveAllDelete() throws Throwable {
        Context context = new ContextImpl(contextid);
        CalendarDataObject cdao = new CalendarDataObject();

        cdao.setTitle("testInsertMoveAllDelete - Step 1 - Insert");
        cdao.setParentFolderID(getCalendarDefaultFolderForUser(userid, context));
        
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        
        fillDatesInDao(cdao);
        cdao.setIgnoreConflicts(true);
        CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        int private_folder_id = cdao.getEffectiveFolderId();
        
        int object_id = cdao.getObjectID();
 
        Connection readcon = DBPool.pickup(context);
        Connection writecon = DBPool.pickupWriteable(context);
        
        //OXFolderAction ofa = new OXFolderAction(so);
        final OXFolderManager oxma = new OXFolderManagerImpl(so, readcon, writecon);
        OCLPermission oclp = new OCLPermission();
        oclp.setEntity(userid);
        oclp.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp.setFolderAdmin(true);
        FolderObject fo = new FolderObject();
        fo.setFolderName("MyTestFolder"+System.currentTimeMillis());
        fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PUBLIC);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp });
        //ofa.createFolder(fo, so, true, readcon, writecon, false);
        fo = oxma.createFolder(fo, true, System.currentTimeMillis());
        
        int public_folder_id = fo.getObjectID();
        CalendarDataObject testobject = null;
        try {
            // TODO: "Move" folder to a public folder
            CalendarDataObject update1 = new CalendarDataObject();
            update1.setContext(so.getContext());
            update1.setObjectID(object_id);
            update1.setParentFolderID(public_folder_id);
            update1.setTitle("testInsertMoveAllDelete - Step 2 - Update");
            update1.setIgnoreConflicts(true);
            csql.updateAppointmentObject(update1, private_folder_id, new Date(SUPER_END));

            // TODO: LoadObject by ID and make some tests
            testobject = csql.getObjectById(object_id, public_folder_id);
            UserParticipant up[] = testobject.getUsers();
            for (int a = 0; a < up.length; a++) {
                assertTrue("check that folder id IS NULL", up[a].getPersonalFolderId() == UserParticipant.NO_PFID);
            }

            assertEquals("testInsertMoveAllDelete - Step 2 - Update", update1.getTitle());

            // TODO: Move again to private folder

            CalendarDataObject update2 = csql.getObjectById(object_id, public_folder_id);

            update2.setTitle("testInsertMoveAllDelete - Step 3 - Update");
            update2.setParentFolderID(private_folder_id);
            update2.setIgnoreConflicts(true);
            csql.updateAppointmentObject(update2, public_folder_id, new Date(SUPER_END));        

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

            update3.setTitle("testInsertMoveAllDelete - Step 4 - Update");
            update3.setParentFolderID(public_folder_id);
            update3.setIgnoreConflicts(true);
            csql.updateAppointmentObject(update3, private_folder_id, new Date(SUPER_END));        
            
            deleteAllAppointments();
                        
            SearchIterator si = csql.getModifiedAppointmentsInFolder(public_folder_id, cols, new Date(0));
            boolean found = false;
            while (si.hasNext()) {
                CalendarDataObject tdao = (CalendarDataObject)si.next();
                System.out.println(">>> "+tdao.getTitle());
                found = true;
            }
            si.close();
            assertTrue("Got no results out of the public folder", found == false);
            
            // Magic test
        
            si = csql.getAppointmentsBetween(userid, new Date(0), new Date(SUPER_END), cols, 0,  null);
            assertTrue("Got results", !si.hasNext());
            
            si.close();
        } finally {
        	oxma.deleteFolder(new FolderObject(public_folder_id), true, SUPER_END);
            //ofa.deleteFolder(public_folder_id, so, true, SUPER_END);
        }

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
 
    public void testConflictHandling() throws Exception  {
        Context context = new ContextImpl(contextid);
        CalendarDataObject cdao = new CalendarDataObject();

        cdao.setTitle("testConflict Step 1 - Insert - ignore conflicts");
        cdao.setParentFolderID(getCalendarDefaultFolderForUser(userid, context));
        
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        
        fillDatesInDao(cdao);
        cdao.setIgnoreConflicts(true);
        CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        int object_id = cdao.getObjectID();
        
        CalendarDataObject conflict_cdao = new CalendarDataObject();

        conflict_cdao.setTitle("testConflict Step 2 - Insert - Must conflict");
        conflict_cdao.setParentFolderID(getCalendarDefaultFolderForUser(userid, context));
        
        conflict_cdao.setContext(so.getContext());
        
        fillDatesInDao(conflict_cdao);
        conflict_cdao.setIgnoreConflicts(false);
        CalendarDataObject conflicts[] = csql.insertAppointmentObject(conflict_cdao);
        
        if (conflicts != null) { // could be that the whole conflict detection is deactivated 
            assertTrue("Check if the conflict has been detected", conflicts.length > 0);

            boolean found_first_appointment = false;
            for (int a  = 0; a < conflicts.length; a++) {
                if (conflicts[a].getObjectID() == cdao.getObjectID()) {
                    found_first_appointment = true;
                    break;
                }
            }

            if (conflicts.length != ConflictHandler.MAX_CONFLICT_RESULTS) {                
                assertTrue("Check for conflict object ", found_first_appointment);
            }        

            assertTrue("Check that we did not create the second appointment", conflict_cdao.containsObjectID() == false);
        } else {
            fail("No conflict!");
        }
        
    }
    
    public void testGetAllAppointmentsFromUserInAllFolders() throws Exception {
        Connection readcon = DBPool.pickup(getContext());
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "testGetAllAppointmentsFromUserInAllFolders");
        CalendarSql csql = new CalendarSql(so);
        
        SearchIterator si = csql.getAppointmentsBetween(userid, new Date(0), new Date(SUPER_END), cols, 0,  null);
        assertTrue("Test if we got appointments", si.hasNext());
        int counter = 0;
        while (si.hasNext()) {
            CalendarDataObject cdao = (CalendarDataObject)si.next();
            assertTrue("Check folder ", cdao.getParentFolderID() != 0);
            testDelete(cdao);
            counter++;
            
        }
        si.close();
        System.out.println("DEBUG: deleted : "+counter);
        si = csql.getAppointmentsBetween(userid, new Date(0), new Date(SUPER_END), cols, 0, null);
        assertTrue("Check that we deleted them all", !si.hasNext());
        DBPool.push(context, readcon);
    }
    
    
    public void testResourceConflictHandling() throws Exception {
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        int folder_id = getCalendarDefaultFolderForUser(userid, context);
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone("Europe/Berlin");
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(folder_id);
        
        CalendarTest.fillDatesInDao(cdao);
        
        cdao.setTitle("testResourceConflictHandling - Step 1 - Insert");
        
        
        Participants p = new Participants();
        
        Participant resource = new ResourceParticipant(100);
        
        p.add(resource);
        
        cdao.setParticipants(p.getList());
        
        CalendarDataObject cdao_conflict = (CalendarDataObject)cdao.clone();
        
        cdao.setIgnoreConflicts(true);
        
        CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        int object_id = cdao.getObjectID();   
        
        assertTrue("Check that the object really exists ", object_id > 0);
        
        Date last = cdao.getLastModified();     
        
        cdao.setIgnoreConflicts(true);
        
        cdao_conflict.setParentFolderID(folder_id);
 
        CalendarDataObject conflicts[] = csql.insertAppointmentObject(cdao_conflict);
        
        assertTrue("Got conflicts ", conflicts != null);
        boolean found = false;
        for (int a = 0; a < conflicts.length; a++) {
            CalendarDataObject tcdao = conflicts[a];
            if (tcdao.getObjectID() == object_id) {
                found = true;
            }
        }
        assertTrue("Conflict object not found", found);
        
        Participants p2 = new Participants();
        Participant resource2 = new ResourceParticipant(1001);
        p2.add(resource2);
        
        cdao.setParticipants(p2.getList());        
        String titel = "testResourceConflictHandling - Step 2 - Update";
        cdao.setTitle(titel);
        
        conflicts = csql.updateAppointmentObject(cdao, folder_id, new Date(SUPER_END));
        
        CalendarDataObject testobject = csql.getObjectById(object_id, folder_id);
        
        Participant test_participants[] = testobject.getParticipants();
        assertEquals("Check size after update ", 2, test_participants.length);
        assertEquals("Check changed titlel", titel, testobject.getTitle());
        found = false;
        for (int a = 0; a < test_participants.length; a++) {
            if (test_participants[a].getIdentifier() == 1001) {
                found = true;
            }
            
        }
        assertTrue("Check for updated (new) resource", found);
        
        csql.deleteAppointmentObject(cdao, folder_id, new Date(SUPER_END));
    }
    
    public void testComplexConflictHandling() throws Exception  {
        deleteAllAppointments(); // Clean up !

        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        int fid = getCalendarDefaultFolderForUser(userid, context);    
        CalendarSql csql = new CalendarSql(so);                
        
        SearchIterator si = csql.getAppointmentsBetween(userid, new Date(0), new Date(SUPER_END), cols, 0,  null);
        assertTrue("Got no results", si.hasNext() == false);
        si.close();
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone("Europe/Berlin");
        cdao.setTitle("testComplexConflictHandling - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        
        cdao.setContext(so.getContext());
        cdao.setShownAs(AppointmentObject.FREE);
        fillDatesInDao(cdao);
        
        CalendarDataObject cdao_conflict = (CalendarDataObject) cdao.clone();
        cdao_conflict.setShownAs(AppointmentObject.RESERVED);
        cdao_conflict.setTitle("testComplexConflictHandling - Step 2 - Insert");
        
        CalendarDataObject conflicts[] = csql.insertAppointmentObject(cdao);
        assertTrue("Found no conflicts", conflicts == null);
        
        conflicts = csql.insertAppointmentObject(cdao_conflict);           
        assertTrue("Found no conflicts (free)", conflicts == null);
        
        assertTrue("ID check", cdao.getObjectID() != cdao_conflict.getObjectID());
        
        cdao.setTitle("testComplexConflictHandling - Step 3 - Update");
        cdao.setShownAs(AppointmentObject.RESERVED);
        cdao.setFullTime(true);
        conflicts = csql.updateAppointmentObject(cdao, fid, new Date(SUPER_END));
        assertTrue("Found conflicts ", conflicts != null);
        assertEquals("Check correct result size", 1, conflicts.length);
        assertEquals("Check conflict results", conflicts[0].getObjectID(), cdao_conflict.getObjectID());
        cdao.setIgnoreConflicts(true);
        conflicts = csql.updateAppointmentObject(cdao, fid, new Date(SUPER_END));
        assertTrue("Found conflicts ", conflicts == null);
        
        cdao_conflict.setTitle("testComplexConflictHandling - Step 4 - Update");
        conflicts = csql.updateAppointmentObject(cdao_conflict, fid, new Date(SUPER_END));
        assertTrue("Found conflicts ", conflicts != null);
        assertEquals("Check correct result size", 1, conflicts.length);
        assertEquals("Check conflict results", conflicts[0].getObjectID(), cdao.getObjectID());        
        
        // TODO: Convert cdao_conflict to daily recurring app and check more conflicts
        
    }
  

    public void testConfirmation() throws Throwable {
        Context context = new ContextImpl(contextid);
        int fid = getCalendarDefaultFolderForUser(userid, context);
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testConfirmation - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        cdao.setIgnoreConflicts(true);
        
        UserParticipant userparticipants = new UserParticipant(userid);
        userparticipants.setConfirm(AppointmentObject.ACCEPT);
        String check_confirm_message = "Check this";
        userparticipants.setConfirmMessage(check_confirm_message);
        cdao.setUsers(new UserParticipant[] { userparticipants });
        
        assertEquals("Check id" , userid, userparticipants.getIdentifier());
        assertEquals("Check confirm state" , AppointmentObject.ACCEPT, userparticipants.getConfirm());
        assertEquals("Check confirm message", check_confirm_message, userparticipants.getConfirmMessage());
        
        Participants participants = new Participants();
        
        Participant p2 = new UserParticipant(resolveUser(user2));
        participants.add(p2);        
        
        cdao.setParticipants(participants.getList());        
        
        fillDatesInDao(cdao);
        
        
        UserParticipant up[] = cdao.getUsers();
        assertTrue("Check participants (1)", up != null);
        assertTrue("Check participants (1)", up.length > 0);
        boolean found = false;
        for (int a = 0; a < up.length; a++) {
            if (up[a].getIdentifier() == userid) {
                assertEquals("Check confirm state (1)" , AppointmentObject.ACCEPT, up[a].getConfirm());
                found = true;
            }
        }        
        assertTrue("Check correct participants", found);
        
        CalendarSql csql = new CalendarSql(so);        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        up = testobject.getUsers();
        assertTrue("Check participants (2)", up != null);
        assertTrue("Check participants (2)", up.length > 0);
        found = false;
        for (int a = 0; a < up.length; a++) {
            if (up[a].getIdentifier() == userid) {
                assertEquals("Check confirm state (2)" , AppointmentObject.ACCEPT, up[a].getConfirm());
                assertEquals("Check confirm message", check_confirm_message, up[a].getConfirmMessage());
                found = true;
            }
        }
        assertTrue("Check correct participants", found);
        
        
        //csql.deleteAppointmentObject(testobject, fid, new Date(SUPER_END));
        
    }
    
    public void testSharedFolder() throws Throwable {
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        int uid2 = resolveUser(user2);        
        SessionObject so2 = SessionObjectWrapper.createSessionObject(uid2, context.getContextId(), "myTestIdentifier");
        
        Connection readcon = DBPool.pickup(context);
        Connection writecon = DBPool.pickupWriteable(context);        
        
        int fid = getPrivateFolder();
        //OXFolderAction ofa = new OXFolderAction(so);
        final OXFolderManager oxma = new OXFolderManagerImpl(so, readcon, writecon);
        FolderObject fo = new FolderObject();
        
        OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(userid);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);
        OCLPermission oclp2 = new OCLPermission();
        oclp2.setEntity(uid2);
        oclp2.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        fo.setFolderName("testSharedFolder");
        fo.setParentFolderID(fid);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1, oclp2 });       
        
        int shared_folder_id = 0;
        try {
            //ofa.createFolder(fo, so, true, readcon, writecon, false);
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            shared_folder_id = fo.getObjectID();       
            
            CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(so.getContext());
            cdao.setParentFolderID(shared_folder_id);
            fillDatesInDao(cdao);
            cdao.setTitle("testSharedFolder");
            cdao.setIgnoreConflicts(true);
            CalendarSql csql = new CalendarSql(so);
            csql.insertAppointmentObject(cdao);        
            int object_id = cdao.getObjectID();    
            
            CalendarSql csql2 = new CalendarSql(so2);
            SearchIterator si = csql2.getModifiedAppointmentsInFolder(shared_folder_id, cols, new Date(0));
            boolean found = false;
            while (si.hasNext()) {
                CalendarDataObject tdao = (CalendarDataObject)si.next();
                if (object_id == cdao.getObjectID()) {
                    found = true;
                }
            }          
            si.close();
            assertTrue("User2 got object in shared folder created by user1 ", found);            
            
            CalendarDataObject ddao = new CalendarDataObject();
            ddao.setContext(so.getContext());
            ddao.setObjectID(object_id);
            csql.deleteAppointmentObject(ddao, shared_folder_id, new Date(SUPER_END));
            boolean found_deleted = false;        
            si = csql2.getDeletedAppointmentsInFolder(shared_folder_id, cols, new Date(0));
            while (si.hasNext()) {
                CalendarDataObject tdao = (CalendarDataObject)si.next();
                if (object_id == cdao.getObjectID()) {
                found_deleted = true;
                }
            }       
            si.close();
            assertTrue("User2 got no object in shared folder created by user1 ", found_deleted);
            
        } finally {
            try {
                if (shared_folder_id > 0) {
                	oxma.deleteFolder(new FolderObject(shared_folder_id), true, SUPER_END);
                    //ofa.deleteFolder(shared_folder_id, so, true, SUPER_END);
                } else {
                    fail("Folder was not created.");
                }
            } catch(Exception e) {
                e.printStackTrace();
                fail("Error deleting folder object.");
            }
        }
        try {
            DBPool.push(context, readcon);
            DBPool.pushWrite(context, writecon);        
        } catch(Exception ignore) { 
            ignore.printStackTrace();
        }            
    }
    
    
    public void testHasAppointmentsBetween() throws Throwable {
        
        deleteAllAppointments();
        
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        int fid = getPrivateFolder();        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(so.getContext());
        cdao.setTitle("testHasAppointmentsBetween - Normal app");
        cdao.setParentFolderID(fid);
        cdao.setIgnoreConflicts(true);
        fillDatesInDao(cdao);
        
        CalendarSql csql = new CalendarSql(so);        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();
        
        RecurringResults m = null;
        CalendarDataObject cdao2 = new CalendarDataObject();
        cdao2.setContext(so.getContext());
        cdao2.setParentFolderID(fid);
        cdao2.setTimezone(CalendarRecurringTests.TIMEZONE);
        fillDatesInDao(cdao2);
        cdao2.removeUntil();        
        cdao2.setTitle("testHasAppointmentsBetween - Rec app");
        cdao2.setRecurrenceType(CalendarDataObject.WEEKLY);
        cdao2.setInterval(1);        
        cdao2.setDays(AppointmentObject.FRIDAY);        
        
        csql.insertAppointmentObject(cdao2);
        int object_id2 = cdao2.getObjectID();
        
        CalendarDataObject calculation = new CalendarDataObject();
        fillDatesInDao(calculation);
        
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone(CalendarRecurringTests.TIMEZONE));
        c.setTimeInMillis(calculation.getStartDate().getTime());
        
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.YEAR, year);
        
        long range_start = CalendarRecurringCollection.normalizeLong(c.getTimeInMillis());
        
        int calc_length = c.getMaximum(Calendar.DAY_OF_MONTH);
        
        c.add(Calendar.DAY_OF_MONTH, calc_length);
        
        long range_end = CalendarRecurringCollection.normalizeLong(c.getTimeInMillis());
        
        boolean check_array[] = new boolean[calc_length];
        
        int pos = (int)((cdao.getStartDate().getTime()-range_start)/CalendarRecurringCollection.MILLI_DAY);
        int len = (int)((cdao.getEndDate().getTime()-cdao.getStartDate().getTime())/CalendarRecurringCollection.MILLI_DAY);
        //System.out.println("pos = "+pos + " len = "+len);
        for (int a = pos; a <= pos+len; a++) {
            check_array[a] = true;
        }
        
        m = CalendarRecurringCollection.calculateRecurring(cdao2, 0, 0, 0);
        for (int a  = 0; a < m.size(); a++) {
            RecurringResult rr = m.getRecurringResult(a);
            pos = (int)((rr.getStart()-range_start)/CalendarRecurringCollection.MILLI_DAY);
            len = (int)((rr.getEnd()-rr.getStart())/CalendarRecurringCollection.MILLI_DAY);
            //System.out.println("pos = "+pos + " len = "+len);
            for (int b = pos; b <= pos+len; b++) {
                if (b < check_array.length) {
                    check_array[b] = true;
                }
            }            
        }
        
        
        boolean test_array[] = csql.hasAppointmentsBetween(new Date(range_start), new Date(range_end));
       
        
        assertEquals("Check arrays (length)", check_array.length, test_array.length);
        
        for (int a = 0; a < check_array.length; a++) {
            assertEquals("Check arrays (position "+a+")", check_array[a], test_array[a]);
        }
        
    }

    
   public void testInsertUpdateAlarm() throws Throwable {
        Context context = new ContextImpl(contextid);
        int fid = getCalendarDefaultFolderForUser(userid, context);
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testInsertUpdateAlarm - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        cdao.setIgnoreConflicts(true);
        cdao.setAlarm(15);
        cdao.setAlarmFlag(true);
        fillDatesInDao(cdao);
        
        CalendarSql csql = new CalendarSql(so);        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        assertTrue("Check alarm", testobject.containsAlarm());
        assertEquals("Check correct alarm value", 15, testobject.getAlarm());
        
        CalendarDataObject update = new CalendarDataObject();
        update.setTitle("testInsertUpdateAlarm - Step 2 - Update");
        update.setObjectID(object_id);
        update.setContext(so.getContext());
        update.setIgnoreConflicts(true);
        csql.updateAppointmentObject(update, fid, testobject.getLastModified());
        
        testobject = csql.getObjectById(object_id, fid);
        assertTrue("Check alarm", testobject.containsAlarm());
        assertEquals("Check correct alarm value", 15, testobject.getAlarm());        
        
        CalendarDataObject update2 = new CalendarDataObject();
        update2.setTitle("testInsertUpdateAlarm - Step 3 - Update");
        update2.setObjectID(object_id);
        update2.setContext(so.getContext());
        update2.setIgnoreConflicts(true);
        
        UserParticipant up = new UserParticipant(userid);
        update2.setUsers(new UserParticipant[] { up });        
        
        csql.updateAppointmentObject(update2, fid, testobject.getLastModified());
        
        testobject = csql.getObjectById(object_id, fid);
        assertTrue("Check alarm", testobject.containsAlarm());
        assertEquals("Check correct alarm value", 15, testobject.getAlarm());                
        
    }  
   
    public void testInsertMoveAndDeleteAppointmentsWithPrivateFlag() throws Throwable {
        Context context = new ContextImpl(contextid);
        CalendarDataObject cdao = new CalendarDataObject();

        cdao.setTitle("testInsertMoveAndDeleteAppointmentsWithPrivateFlag - Step 1 - Insert");
        cdao.setParentFolderID(getCalendarDefaultFolderForUser(userid, context));
        
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        
        fillDatesInDao(cdao);
        cdao.setIgnoreConflicts(true);
        cdao.setPrivateFlag(true);
        CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        int private_folder_id = cdao.getEffectiveFolderId();
        
        int object_id = cdao.getObjectID();
 
        Connection readcon = DBPool.pickup(context);
        Connection writecon = DBPool.pickupWriteable(context);
        
        //OXFolderAction ofa = new OXFolderAction(so);
        final OXFolderManager oxma = new OXFolderManagerImpl(so, readcon, writecon);
        OCLPermission oclp = new OCLPermission();
        oclp.setEntity(userid);
        oclp.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp.setFolderAdmin(true);
        FolderObject fo = new FolderObject();
        fo.setFolderName("testInsertMoveAndDeleteAppointmentsWithPrivateFlagFolder");
        fo.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PUBLIC);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp });
        //ofa.createFolder(fo, so, true, readcon, writecon, false);
        fo = oxma.createFolder(fo, true, System.currentTimeMillis());
        int public_folder_id = fo.getObjectID();
        CalendarDataObject testobject = null;
        try {
            // TODO: "Move" object to a public folder
            CalendarDataObject update1 = new CalendarDataObject();
            update1.setContext(so.getContext());
            update1.setObjectID(object_id);
            update1.setParentFolderID(public_folder_id);
            update1.setTitle("testInsertMoveAndDeleteAppointments - Step 2 - Update");
            update1.setIgnoreConflicts(true);
            try {
                csql.updateAppointmentObject(update1, private_folder_id, new Date(SUPER_END));
                fail("Move from a private folder with private flag should not be possibe!");
            } catch(OXPermissionException e) {
                // Very good if we get an error
            } catch(OXCalendarException oxce) {
                // Very good if we get this kind of error
            } catch(Exception e) {
                fail ("Nooo "+e.getMessage());
            }
            
        } finally {
        	oxma.deleteFolder(new FolderObject(public_folder_id), true, SUPER_END);
            //ofa.deleteFolder(public_folder_id, so, true, SUPER_END);
        }

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
    
    public void testAlarmAndUpdate() throws Throwable {
        Context context = new ContextImpl(contextid);
        int fid = getCalendarDefaultFolderForUser(userid, context);
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testAlarmAndUpdate - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        cdao.setIgnoreConflicts(true);
        cdao.setAlarm(15);
        fillDatesInDao(cdao);
        
        CalendarSql csql = new CalendarSql(so);        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();
        
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        assertTrue("Alarm should be set", testobject.containsAlarm());
        assertEquals("Test correct alarm value", 15, testobject.getAlarm());
        
        CalendarDataObject update = new CalendarDataObject();
        update.setContext(so.getContext());
        update.setObjectID(object_id);
        update.setTitle("testAlarmAndUpdate - Step 2 - Update");
        update.setIgnoreConflicts(true);
         
        Participants participants = new Participants();
        Participant p = new UserParticipant(userid);
        participants.add(p);        
        UserParticipant up = new UserParticipant(userid);
        
        update.setUsers(new UserParticipant[] { up });        
        update.setParticipants(participants.getList());
        
        csql.updateAppointmentObject(update, fid, testobject.getLastModified());
        
        CalendarDataObject testobject2 = csql.getObjectById(object_id, fid);
        assertTrue("Alarm should be set", testobject2.containsAlarm());
        assertEquals("Test correct alarm value", 15, testobject2.getAlarm());
        
        ReminderSQLInterface rsql = new ReminderHandler(so);
        ReminderObject ro = rsql.loadReminder(object_id, userid, Types.APPOINTMENT);
        long check_date = new Date((testobject2.getStartDate().getTime() - (15*60*1000))).getTime();
        assertEquals("Check correct alam in reminder object" , check_date, ro.getDate().getTime());
        
        CalendarDataObject update2 = new CalendarDataObject();
        update2.setContext(so.getContext());
        update2.setObjectID(object_id);
        String title = "testAlarmAndUpdate - Step 3 - Update";
        update2.setTitle(title);
        update2.setAlarm(30);
        update2.setIgnoreConflicts(true);
        
        csql.updateAppointmentObject(update2, fid, testobject2.getLastModified());
        
        CalendarDataObject testobject3 = csql.getObjectById(object_id, fid);
        assertEquals("Check title", title, testobject3.getTitle());
        assertTrue("Alarm should be set", testobject3.containsAlarm());
        assertEquals("Test correct alarm value", 30, testobject3.getAlarm());
        
        
        ro = rsql.loadReminder(object_id, userid, Types.APPOINTMENT);
        check_date = new Date((testobject3.getStartDate().getTime() - (30*60*1000))).getTime();
        assertEquals("Check correct alam in reminder object" , check_date, ro.getDate().getTime());        
        
        
    } 

    public void testDataTuncationException() throws Throwable {
        Context context = new ContextImpl(contextid);
        int fid = getCalendarDefaultFolderForUser(userid, context);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        CalendarDataObject cdao = new CalendarDataObject();
        StringBuilder sb = new StringBuilder("testDataTuncationException ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        sb.append("extrem long test to force an error message and in detail a DataTruncation Exception! ");
        cdao.setTitle(sb.toString());
        cdao.setParentFolderID(fid);
        cdao.setContext(so.getContext());
        cdao.setIgnoreConflicts(true);
        fillDatesInDao(cdao);
        CalendarSql csql = new CalendarSql(so);        
        int object_id = 0;
        try {
            csql.insertAppointmentObject(cdao);
            object_id = cdao.getObjectID();        
        } catch(OXException oxe) {
            int ids[] = oxe.getTruncatedIds();
            if (ids.length == 0) {
                fail("Got no TruncatedIds ");
            }
        } catch(Exception e) {
            fail("Wrong exception!");
        }
        
        if (object_id > 0) {
            fail("Test not executed correctly");
        }
        
    }
    
    public void testExternalParticipants() throws Throwable {
        Context context = new ContextImpl(contextid);
        int fid = getCalendarDefaultFolderForUser(userid, context);
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testExternalParticipants - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        cdao.setIgnoreConflicts(true);
        
        UserParticipant up = new UserParticipant(userid);
        up.setAlarmMinutes(5);
        cdao.setUsers(new UserParticipant[] { up });
        
        String mail_address = "test@example.org";
        String display_name = "Externer test user";
        
        
        String mail_address2 = "test2@example.org";
        String display_name2 = "Externer test user2";
        
        
        Participants participants = new Participants();
        ExternalUserParticipant p1 = new ExternalUserParticipant(mail_address);
        p1.setEmailAddress(mail_address);
        p1.setDisplayName(display_name);
        participants.add(p1);
        
        ExternalUserParticipant p2 = new ExternalUserParticipant(mail_address2);
        p2.setEmailAddress(mail_address2);
        p2.setDisplayName(display_name2);
        participants.add(p2);
        
        cdao.setParticipants(participants.getList());        
        
        fillDatesInDao(cdao);
        
        CalendarSql csql = new CalendarSql(so);        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);    
        
        Participant test_participants[] = testobject.getParticipants();
        assertTrue("Check return is not null" , test_participants != null);
        boolean found = false;
        for (int a = 0; a < test_participants.length; a++) {
            if (test_participants[a].getType() == Participant.EXTERNAL_USER) {
                if (test_participants[a].getEmailAddress().equals(mail_address)) {
                    assertEquals("Check display name", display_name, test_participants[a].getDisplayName());
                    assertEquals("Check mail address", mail_address, test_participants[a].getEmailAddress());
                } else if (test_participants[a].getEmailAddress().equals(mail_address2)) {
                    assertEquals("Check display name", display_name2, test_participants[a].getDisplayName());
                    assertEquals("Check mail address", mail_address2, test_participants[a].getEmailAddress());                        
                }
                found = true;        
            }
        }
        
        assertTrue("Got external participant", found);
        
        CalendarDataObject update = new CalendarDataObject();
        
        String display_name_2_fail = "Externer test user without mail address";
        participants = new Participants();
        ExternalUserParticipant p_fail = new ExternalUserParticipant(null);
        p_fail.setDisplayName(display_name_2_fail);
        participants.add(p_fail);
        
        update.setContext(so.getContext());
        update.setObjectID(object_id);
        update.setParticipants(participants.getList());
        update.setIgnoreConflicts(true);
        boolean check_update = false;
        
        
        try {
            csql.updateAppointmentObject(update, fid, cdao.getLastModified());
            check_update = true;
        } catch(Exception e) {
            // perfect
            int x = 0;
            e.printStackTrace();
        }
        assertFalse("Participant has no mail, update must fail!", check_update);
 
        CalendarDataObject update_participants = new CalendarDataObject();
        
        String update_new_display_1 = "External Update 1";
        String update_new_mail_1 = "abc@de";
        String update_new_display_2 = "External Update 2";
        String update_new_mail_2 = "abc2@de";        
        
        Participants participants_update = new Participants();
        ExternalUserParticipant p3 = new ExternalUserParticipant(update_new_mail_1);
        p3.setDisplayName(update_new_display_1);
        p3.setEmailAddress(update_new_mail_1);
        participants_update.add(p3);
        
        ExternalUserParticipant p4 = new ExternalUserParticipant(update_new_mail_2);
        p4.setDisplayName(update_new_display_2);
        p4.setEmailAddress(update_new_mail_2);
        participants_update.add(p4);
        
        participants_update.add(p1);
        participants_update.add(p2);
        
        update_participants.setContext(so.getContext());
        update_participants.setObjectID(object_id);
        update_participants.setParticipants(participants_update.getList());
        update_participants.setIgnoreConflicts(true);
        
        csql.updateAppointmentObject(update_participants, fid, cdao.getLastModified());

        
        CalendarDataObject testobject_update = csql.getObjectById(object_id, fid);
        
        Participant test_participants_update[] = testobject_update.getParticipants();
        
        int found_external = 0;
        for (int a = 0; a < test_participants_update.length; a++) {
            if (test_participants_update[a].getType() == Participant.EXTERNAL_USER) {
                if (test_participants_update[a].getEmailAddress().equals(mail_address)) {
                    assertEquals("Check display name", display_name, test_participants_update[a].getDisplayName());
                    assertEquals("Check mail address", mail_address, test_participants_update[a].getEmailAddress());
                    found_external++;
                } else if (test_participants_update[a].getEmailAddress().equals(mail_address2)) {
                    assertEquals("Check display name", display_name2, test_participants_update[a].getDisplayName());
                    assertEquals("Check mail address", mail_address2, test_participants_update[a].getEmailAddress());                        
                    found_external++;
                } else if (test_participants_update[a].getEmailAddress().equals(update_new_mail_1)) {
                    assertEquals("Check display name", update_new_display_1, test_participants_update[a].getDisplayName());
                    assertEquals("Check mail address", update_new_mail_1, test_participants_update[a].getEmailAddress());                        
                    found_external++;
                } else if (test_participants_update[a].getEmailAddress().equals(update_new_mail_2)) {
                    assertEquals("Check display name", update_new_display_2, test_participants_update[a].getDisplayName());
                    assertEquals("Check mail address", update_new_mail_2, test_participants_update[a].getEmailAddress());                        
                    found_external++;
                }
            }
        }        
        
        assertEquals("Found all 4 external participants after update", 4, found_external);
        
        CalendarDataObject update_participants2 = new CalendarDataObject();
      
        
        Participants participants_update2 = new Participants();

        participants_update2.add(p1);

        participants_update2.add(p2);
        

        
        update_participants2.setContext(so.getContext());
        update_participants2.setObjectID(object_id);
        update_participants2.setParticipants(participants_update2.getList());
        update_participants2.setIgnoreConflicts(true);
        
        csql.updateAppointmentObject(update_participants2, fid, update_participants.getLastModified());

        
        CalendarDataObject testobject_update2 = csql.getObjectById(object_id, fid);
        
        Participant test_participants_update2[] = testobject_update2.getParticipants();
        
        found_external = 0;
        for (int a = 0; a < test_participants_update2.length; a++) {
            if (test_participants_update2[a].getType() == Participant.EXTERNAL_USER) {
                if (test_participants_update2[a].getEmailAddress().equals(mail_address)) {
                    assertEquals("Check display name", display_name, test_participants_update2[a].getDisplayName());
                    assertEquals("Check mail address", mail_address, test_participants_update2[a].getEmailAddress());
                    found_external++;
                } else if (test_participants_update2[a].getEmailAddress().equals(mail_address2)) {
                    assertEquals("Check display name", display_name2, test_participants_update2[a].getDisplayName());
                    assertEquals("Check mail address", mail_address2, test_participants_update2[a].getEmailAddress());                        
                    found_external++;
                } else if (test_participants_update2[a].getEmailAddress().equals(update_new_mail_1)) {
                    assertEquals("Check display name", update_new_display_1, test_participants_update2[a].getDisplayName());
                    assertEquals("Check mail address", update_new_mail_1, test_participants_update2[a].getEmailAddress());                        
                    found_external++;
                } else if (test_participants_update2[a].getEmailAddress().equals(update_new_mail_2)) {
                    assertEquals("Check display name", update_new_display_2, test_participants_update2[a].getDisplayName());
                    assertEquals("Check mail address", update_new_mail_2, test_participants_update2[a].getEmailAddress());                        
                    found_external++;
                }
            }
        }          
        
        assertEquals("Found last 2 external participants after second update", 2, found_external);
        
    }    
  
    public void testGroupZero() throws Exception  {
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(getContext());
        Participants participants = new Participants();
        Participant p = new GroupParticipant(0);
        participants.add(p);
        cdao.setParticipants(participants.getList());
        cdao.setTitle("testGroupZero");
        CalendarTest.fillDatesInDao(cdao);
        
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        cdao.setIgnoreConflicts(true);        
        
        int fid = getCalendarDefaultFolderForUser(userid, getContext());
        cdao.setGlobalFolderID(fid);
        
        CalendarSql csql = new CalendarSql(so);        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();
   
        
    }
    
    public void testSetAlarmAndConfirmStateWithParticipantsAndChangeTime() throws Exception {
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        int userid2 = resolveUser(user2);        
        int fid = getCalendarDefaultFolderForUser(userid, getContext());
        int fid2 = getCalendarDefaultFolderForUser(userid2, getContext());
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");
        SessionObject so2 = SessionObjectWrapper.createSessionObject(userid2, getContext().getContextId(), "myTestIdentifier");
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(fid);
        cdao.setTitle("testSetAlarmAndConfirmStateWithParticipantsAndChangeTime");
        cdao.setIgnoreConflicts(true);
        CalendarTest.fillDatesInDao(cdao);
        
        UserParticipant userA = new UserParticipant(userid);
        userA.setAlarmMinutes(15);
       
        UserParticipant userB = new UserParticipant(userid2);
        
        cdao.setUsers(new UserParticipant[] { userA, userB });        
        
        CalendarSql csql = new CalendarSql(so);
        CalendarSql csql2 = new CalendarSql(so2);
        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();                
        
        CalendarDataObject update_user2 = csql2.getObjectById(object_id, fid2);
        
        update_user2.setContext(getContext());
        update_user2.setIgnoreConflicts(true);
        update_user2.setAlarm(30);
        
        csql2.updateAppointmentObject(update_user2, fid2, cdao.getLastModified());
        
        
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        
        SearchIterator si = csql.getModifiedAppointmentsInFolder(fid, cols, cdao.getLastModified());
        boolean found = false;
        while (si.hasNext()) {
            CalendarDataObject tdao = (CalendarDataObject)si.next();
            if (tdao.getObjectID() == object_id) {
                found = true;
                assertTrue("Check that userA has an alarm set in the cdao", tdao.containsAlarm());
                assertEquals("Check that we got the correct reminder", 15, tdao.getAlarm());                 
            }
        }
        assertTrue("Found our object (userA)", found);        
        
        SearchIterator si2 = csql2.getModifiedAppointmentsInFolder(fid2, cols, cdao.getLastModified());
        found = false;
        while (si2.hasNext()) {
            CalendarDataObject tdao = (CalendarDataObject)si2.next();
            if (tdao.getObjectID() == object_id) {
                found = true;
                assertTrue("Check that userB has an alarm set in the cdao", tdao.containsAlarm());  
                assertEquals("Check that we got the correct reminder", 30, tdao.getAlarm());
            }
        }
        assertTrue("Found our object (userB)", found); 
        
        
        CalendarDataObject update_with_time_change = (CalendarDataObject) testobject.clone();
        update_with_time_change.setContext(getContext());
        update_with_time_change.setIgnoreConflicts(true);
        update_with_time_change.setObjectID(object_id);
        
        Date test_date = new Date(testobject.getStartDate().getTime()+3600000);
        
        update_with_time_change.setStartDate(test_date);
        update_with_time_change.setEndDate(new Date(testobject.getEndDate().getTime()+3600000));
        
        csql.updateAppointmentObject(update_with_time_change, fid, testobject.getLastModified());        
        
        
        si = csql.getModifiedAppointmentsInFolder(fid, cols, cdao.getLastModified());
        found = false;
        while (si.hasNext()) {
            CalendarDataObject tdao = (CalendarDataObject)si.next();
            if (tdao.getObjectID() == object_id) {
                found = true;
                assertTrue("Check that userA has an alarm set in the cdao", tdao.containsAlarm());
                assertEquals("Check that we got the correct reminder", 15, tdao.getAlarm());                 
            }
        }
        assertTrue("Found our object (userA)", found);        
        
        si2 = csql2.getModifiedAppointmentsInFolder(fid2, cols, cdao.getLastModified());
        found = false;
        while (si2.hasNext()) {
            CalendarDataObject tdao = (CalendarDataObject)si2.next();
            if (tdao.getObjectID() == object_id) {
                found = true;
                assertTrue("Check that userB has an alarm set in the cdao", tdao.containsAlarm());  
                assertEquals("Check that we got the correct reminder", 30, tdao.getAlarm());
                UserParticipant up_test[] = tdao.getUsers();
                for (int a = 0; a < up_test.length; a++) {
                    if (up_test[a].getIdentifier() == userid2) {
                        assertEquals("Check Confirm State", CalendarDataObject.NONE, up_test[a].getConfirm());
                        ReminderHandler rh = new ReminderHandler(getContext());
                        ReminderObject ro = rh.loadReminder(object_id, userid2, Types.APPOINTMENT);
                        Date check_date = ro.getDate();
                        assertEquals("Check correct Alarm", new Date(test_date.getTime()-(30*60000)), check_date);                        
                    } else if (up_test[a].getIdentifier() == userid) {
                        assertEquals("Check Confirm State", CalendarDataObject.ACCEPT, up_test[a].getConfirm());
                        ReminderHandler rh = new ReminderHandler(getContext());
                        ReminderObject ro = rh.loadReminder(object_id, userid, Types.APPOINTMENT);
                        Date check_date = ro.getDate();
                        assertEquals("Check correct Alarm", new Date(test_date.getTime()-(15*60000)), check_date);                       
                    }
                }
                
            }
        }
        assertTrue("Found our object (userB)", found);         
        
        CalendarDataObject test_fid = csql.getObjectById(object_id, fid);
        assertEquals("Check folder for userA", fid, test_fid.getParentFolderID());
        
        CalendarDataObject test_fid2 = csql2.getObjectById(object_id, fid2);
        assertEquals("Check folder for userB", fid2, test_fid2.getParentFolderID());
        
    }
    
    
}
