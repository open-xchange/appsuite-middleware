
package com.openexchange.groupware;


import com.openexchange.ajax.Resource;
import com.openexchange.api2.ReminderSQLInterface;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.reminder.ReminderHandler;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Properties;

import junit.framework.TestCase;

import com.openexchange.event.EventConfigImpl;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarOperation;
import com.openexchange.groupware.calendar.CalendarRecurringCollection;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.calendar.CalendarSqlImp;
import com.openexchange.groupware.calendar.ConflictHandler;
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
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.DBPool;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAction;
import com.openexchange.tools.oxfolder.OXFolderTools;

public class CalendarTest extends TestCase {
    
    
    private final static int TEST_PASS = 1;
    private final static int TEST_PASS_HOT_SPOT = 1;
    public static final long SUPER_END = 253402210800000L; // 31.12.9999 00:00:00 (GMT)
    
    private static int userid = 11; // bishoph
    public final static int contextid = 1;
    
    private static boolean init = false;
    
    
    int cols[] = new int[] { AppointmentObject.TITLE, AppointmentObject.RECURRENCE_ID, AppointmentObject.RECURRENCE_POSITION, AppointmentObject.OBJECT_ID, AppointmentObject.FOLDER_ID, AppointmentObject.USERS };
    
    protected void setUp() throws Exception {        
        super.setUp();
        EventConfigImpl event = new EventConfigImpl();
        event.setEventQueueEnabled(false);
        userid = getUserId();
        
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
    
    private static int resolveUser(String user) throws Exception {
        UserStorage uStorage = UserStorage.getInstance(getContext());
        return uStorage.getUserId(user);
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
        privatefolder = new Integer(OXFolderTools.getCalendarDefaultFolder(userid, context, readcon)).intValue();
        DBPool.push(context, readcon);
        return privatefolder;
    }
    
    /* ----- test cases -------*/
    
    /*
    
    
    void deleteAllAppointments() throws Exception  {
        Connection readcon = DBPool.pickup(getContext());
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "deleteAllApps");
        CalendarSql csql = new CalendarSql(so);        
        SearchIterator si = csql.getAppointmentsBetween(userid, new Date(0), new Date(SUPER_END), cols, 0,  null);
        while (si.hasNext()) {
            CalendarDataObject cdao = (CalendarDataObject)si.next();
            testDelete(cdao);
        }
        DBPool.push(context, readcon);                
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
        
        readcon = DBPool.pickup(context);
        
        int privatefolder = OXFolderTools.getCalendarDefaultFolder(userid, cdao.getContext(), readcon);
        
        assertFalse("Checking for update", co.prepareUpdateAction(cdao, 1, privatefolder, "Europe/Berlin"));
        long realstart = 1149724800000L;
        assertEquals("Testing start time", cdao.getStartDate().getTime(), realstart);
        assertEquals("Testing end time", cdao.getEndDate().getTime(), realstart+CalendarRecurringCollection.MILLI_DAY);
        DBPool.push(context, readcon);
        
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
        int uid2 = resolveUser(user2);
        p2.setIdentifier(uid2);
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
        SearchIterator fbr = new FreeBusyResults(rs, prep,  getContext(), readcon);   
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
        int fid = OXFolderTools.getDefaultFolder(userid, FolderObject.CALENDAR, context);
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
        int fid = OXFolderTools.getDefaultFolder(userid, FolderObject.CALENDAR, context);
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
        
    }    
    

    public void testInsertAndAlarm() throws Throwable {
        Context context = new ContextImpl(contextid);
        int fid = OXFolderTools.getDefaultFolder(userid, FolderObject.CALENDAR, context);        
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testInsertAndAlarm - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        cdao.setIgnoreConflicts(true);
        
        UserParticipant up = new UserParticipant();
        up.setIdentifier(userid);
        up.setAlarmMinutes(5);
        cdao.setUsers(new UserParticipant[] { up });
        
        
        Participants participants = new Participants();
        Participant p = new UserParticipant();
        p.setIdentifier(userid);
        participants.add(p);
 
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        
        Participant p2 = new UserParticipant();
        p2.setIdentifier(resolveUser(user2));
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
        int public_folder_id = 0;
        try {
            ofa.createFolder(fo, so, true, readcon, writecon, false);
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
            
            
            si = csql.searchAppointments("*.*", public_folder_id, 0, "ASC", cols);
            gotresults = si.hasNext();
            assertTrue("Got some results by searching \"*e*\"", !gotresults);
        
        } finally {
            ofa.deleteFolder(public_folder_id, so, true, SUPER_END);
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
        cdao.setParentFolderID(OXFolderTools.getDefaultFolder(userid, FolderObject.CALENDAR, context));
        
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
            ofa.deleteFolder(public_folder_id, so, true, SUPER_END);
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
        cdao.setParentFolderID(OXFolderTools.getDefaultFolder(userid, FolderObject.CALENDAR, context));
        
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
            
            assertTrue("Got no results out of the public folder", found == false);
            
            // Magic test
        
            si = csql.getAppointmentsBetween(userid, new Date(0), new Date(SUPER_END), cols, 0,  null);
            assertTrue("Got results", !si.hasNext());
            
            
        } finally {
            ofa.deleteFolder(public_folder_id, so, true, SUPER_END);
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
        cdao.setParentFolderID(OXFolderTools.getDefaultFolder(userid, FolderObject.CALENDAR, context));
        
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        
        fillDatesInDao(cdao);
        cdao.setIgnoreConflicts(true);
        CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        int object_id = cdao.getObjectID();
        
        CalendarDataObject conflict_cdao = new CalendarDataObject();

        conflict_cdao.setTitle("testConflict Step 2 - Insert - Must conflict");
        conflict_cdao.setParentFolderID(OXFolderTools.getDefaultFolder(userid, FolderObject.CALENDAR, context));
        
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
        System.out.println("DEBUG: deleted : "+counter);
        si = csql.getAppointmentsBetween(userid, new Date(0), new Date(SUPER_END), cols, 0, null);
        assertTrue("Check that we deleted them all", !si.hasNext());
        DBPool.push(context, readcon);
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
    
    public void testResourceConflictHandling() throws Exception {
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        int folder_id = OXFolderTools.getDefaultFolder(userid, FolderObject.CALENDAR, context);
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone("Europe/Berlin");
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(folder_id);
        
        CalendarTest.fillDatesInDao(cdao);
        
        cdao.setTitle("testResourceConflictHandling - Step 1 - Insert");
        
        
        Participants p = new Participants();
        
        Participant resource = new ResourceParticipant();
        resource.setIdentifier(100);
        
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
        Participant resource2 = new ResourceParticipant();
        resource2.setIdentifier(1001);
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

    
    public void testExternalParticipants() throws Throwable {
        Context context = new ContextImpl(contextid);
        int fid = OXFolderTools.getDefaultFolder(userid, FolderObject.CALENDAR, context);        
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testExternalParticipants - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        cdao.setIgnoreConflicts(true);
        
        UserParticipant up = new UserParticipant();
        up.setIdentifier(userid);
        up.setAlarmMinutes(5);
        cdao.setUsers(new UserParticipant[] { up });
        
        String mail_address = "test@example.org";
        String display_name = "Externer test user";
        
        Participants participants = new Participants();
        Participant p = new ExternalUserParticipant();
        p.setEmailAddress(mail_address);
        p.setDisplayName(display_name);
        participants.add(p);
        
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
                assertEquals("Check display name", display_name, test_participants[a].getDisplayName());
                assertEquals("Check mail address", mail_address, test_participants[a].getEmailAddress());
                found = true;
            }
        }
        
        assertTrue("Got external participant", found);
        
    }
    
    public void testComplexConflictHandling() throws Exception  {
        deleteAllAppointments(); // Clean up !

        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        int fid = OXFolderTools.getDefaultFolder(userid, FolderObject.CALENDAR, context);                
        CalendarSql csql = new CalendarSql(so);                
        
        SearchIterator si = csql.getAppointmentsBetween(userid, new Date(0), new Date(SUPER_END), cols, 0,  null);
        assertTrue("Got results", si.hasNext() == false);
        
        
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
*/    

    public void testConfirmation() throws Throwable {
        Context context = new ContextImpl(contextid);
        int fid = OXFolderTools.getDefaultFolder(userid, FolderObject.CALENDAR, context);        
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTitle("testConfirmation - Step 1 - Insert");
        cdao.setParentFolderID(fid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        cdao.setIgnoreConflicts(true);
        
        UserParticipant userparticipants = new UserParticipant();
        userparticipants.setIdentifier(userid);
        userparticipants.setConfirm(AppointmentObject.ACCEPT);
        String check_confirm_message = "Check this";
        userparticipants.setConfirmMessage(check_confirm_message);
        cdao.setUsers(new UserParticipant[] { userparticipants });
        
        assertEquals("Check id" , userid, userparticipants.getIdentifier());
        assertEquals("Check confirm state" , AppointmentObject.ACCEPT, userparticipants.getConfirm());
        assertEquals("Check confirm message", check_confirm_message, userparticipants.getConfirmMessage());
        
        Participants participants = new Participants();
        
        Participant p2 = new UserParticipant();
        p2.setIdentifier(resolveUser(user2));
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

}
