
package com.openexchange.groupware;


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
import com.openexchange.groupware.calendar.RecurringResults;
import com.openexchange.groupware.calendar.RecurringResult;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;
import com.openexchange.tools.OXFolderTools;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Properties;
import junit.framework.TestCase;

public class CalendarTest extends TestCase {
    
    
    private final static int TEST_PASS = 1;
    private final static int TEST_PASS_HOT_SPOT = 1;
    public static final long SUPER_END = 253402210800000L; // 31.12.9999 00:00:00 (GMT)
    
    private static int userid = 11; // bishoph
    public final static int contextid = 1;
    
    private static boolean init = false;
    
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
        userid = uStorage.getUserId(user);
        return userid;
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
        
        int privatefolder = OXFolderTools.getCalendarStandardFolder(userid, cdao.getContext(), readcon);
        
        assertFalse("Checking for update", co.prepareUpdateAction(cdao, 1, privatefolder));
        long realstart = 1149724800000L;
        assertEquals("Testing start time", cdao.getStartDate().getTime(), realstart);
        assertEquals("Testing end time", cdao.getEndDate().getTime(), realstart+CalendarRecurringCollection.MILLI_DAY);
        
        DBPool.push(context, readcon);
        
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
        Connection readcon = DBPool.pickup(getContext());
        CalendarSqlImp calmysql = CalendarSql.getCalendarSqlImplementation();
        PreparedStatement prep = calmysql.getFreeBusy(userid, getContext(), new Date(0), new Date(SUPER_END), readcon);
        ResultSet rs = calmysql.getResultSet(prep);
        SearchIterator fbr = new FreeBusyResults(rs, getContext(), readcon);   
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
        int fid = OXFolderTools.getStandardFolder(userid, FolderObject.CALENDAR, context);
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
    
    
    public void testInsertMoveAndDeleteAppointments() throws Throwable {
        Context context = new ContextImpl(contextid);
        CalendarDataObject cdao = new CalendarDataObject();

        cdao.setTitle("testMove - Step 1 - Insert");
        cdao.setParentFolderID(OXFolderTools.getStandardFolder(userid, FolderObject.CALENDAR, context));
        
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
            update1.setTitle("testMove - Step 2 - Update");
            update1.setIgnoreConflicts(true);
            csql.updateAppointmentObject(update1, private_folder_id, new Date(SUPER_END));

            // TODO: LoadObject by ID and make some tests
            testobject = csql.getObjectById(object_id, public_folder_id);
            UserParticipant up[] = testobject.getUsers();
            for (int a = 0; a < up.length; a++) {
                assertTrue("check that folder id IS NULL", up[a].getPersonalFolderId() == UserParticipant.NO_PFID);
            }

            assertEquals("testMove - Step 2 - Update", update1.getTitle());

            // TODO: Move again to private folder

            CalendarDataObject update2 = csql.getObjectById(object_id, public_folder_id);

            update2.setTitle("testMove - Step 3 - Update");
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

            update3.setTitle("testMove - Step 4 - Update");
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
    
 
    public void testConflictHandling() throws Exception  {
        Context context = new ContextImpl(contextid);
        CalendarDataObject cdao = new CalendarDataObject();

        cdao.setTitle("testConflict Step 1 - Insert - ignore conflicts");
        cdao.setParentFolderID(OXFolderTools.getStandardFolder(userid, FolderObject.CALENDAR, context));
        
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        cdao.setContext(so.getContext());
        
        fillDatesInDao(cdao);
        cdao.setIgnoreConflicts(true);
        CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        
        CalendarDataObject conflict_cdao = new CalendarDataObject();

        conflict_cdao.setTitle("testConflict Step 2 - Insert - Must conflict");
        conflict_cdao.setParentFolderID(OXFolderTools.getStandardFolder(userid, FolderObject.CALENDAR, context));
        
        
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
        }
        
    }
    
    public void testGetAllAppointmentsFromUserInAllFolders() throws Exception {
        Connection readcon = DBPool.pickup(getContext());
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "testGetAllAppointmentsFromUserInAllFolders");
        CalendarSql csql = new CalendarSql(so);
        int cols[] = new int[] { AppointmentObject.TITLE, AppointmentObject.OBJECT_ID, AppointmentObject.USERS };
        SearchIterator si = csql.getAppointmentsBetween(userid, new Date(0), new Date(SUPER_END), cols);
        assertTrue("Test if we got appointments", si.hasNext());
        while (si.hasNext()) {
            CalendarDataObject cdao = (CalendarDataObject)si.next();
            if (cdao.containsRecurrenceID() && cdao.getRecurrencePosition() == 0) {
                testDelete(cdao);
            }
        }
        DBPool.push(context, readcon);
    }
    
    private void testDelete(CalendarDataObject cdao) throws Exception {        
        Connection writecon = DBPool.pickupWriteable(getContext());
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "delete test");
        CalendarSql csql = new CalendarSql(so);
        csql.deleteAppointmentObject(cdao, cdao.getEffectiveFolderId(), new Date(SUPER_END));
        DBPool.pushWrite(context, writecon);
    }
}
