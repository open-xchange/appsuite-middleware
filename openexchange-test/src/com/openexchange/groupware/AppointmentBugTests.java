package com.openexchange.groupware;

import com.openexchange.groupware.calendar.OXCalendarException;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import junit.framework.TestCase;

import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXException;
import com.openexchange.event.EventConfigImpl;
import com.openexchange.groupware.calendar.CalendarCommonCollection;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarOperation;
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
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.DBPool;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderManagerImpl;


public class AppointmentBugTests extends TestCase {
     
    int cols[] = new int[] { AppointmentObject.START_DATE, AppointmentObject.END_DATE, AppointmentObject.TITLE, AppointmentObject.RECURRENCE_ID, AppointmentObject.RECURRENCE_POSITION, AppointmentObject.OBJECT_ID, AppointmentObject.FOLDER_ID, AppointmentObject.USERS, AppointmentObject.FULL_TIME };
    public static final long SUPER_END = 253402210800000L; // 31.12.9999 00:00:00 (GMT)
    public static final String TIMEZONE = "Europe/Berlin";
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
        si.close();
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
    
    
    /* ------------------------------------- */
    
    public static int getPrivateFolder(int userid) throws Exception {
        int privatefolder = 0;
        Context context = getContext();
        Connection readcon = DBPool.pickup(context);
        privatefolder = CalendarTest.getCalendarDefaultFolderForUser(userid, context);
        DBPool.push(context, readcon);
        return privatefolder;        
    }
    
    /*
     1.  Create an appointment
     2.  Click on apppointment series/recurring
     3.  Set pattern to montly; every 2 weeks; 6 times
     4.  Check the entries in the month view
     5.  Check entries in OX6 Calendar
    */     
    public void testBug4467() throws Throwable { 
        
        int fid = getPrivateFolder(userid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");                
        
        RecurringResults m = null;
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(so.getContext());
        cdao.setTimezone(TIMEZONE);
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        cdao.removeUntil();
        cdao.setOccurrence(10);
        cdao.setTitle("testBug4467");
        cdao.setRecurrenceType(CalendarObject.WEEKLY);
        cdao.setInterval(1);        
        cdao.setDays(AppointmentObject.MONDAY + AppointmentObject.WEDNESDAY + AppointmentObject.FRIDAY);
        CalendarRecurringCollection.fillDAO(cdao);
        m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
        /*
        // debug
        for (int a = 0; a < m.size(); a++) {
            RecurringResult rs = m.getRecurringResult(a);
            System.out.println(new Date(rs.getStart()));
        }
        */
        assertEquals("Check calculation", 10, m.size());
        CalendarSql csql = new CalendarSql(so);        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();                
        CalendarDataObject check_cdao = csql.getObjectById(object_id, fid);
        assertEquals("Check calculated rec_string and loaded rec_string", cdao.getRecurrence(), check_cdao.getRecurrence());
        assertEquals("Check calculated occurrence and loaded occurrence", cdao.getOccurrence(), check_cdao.getOccurrence());
        assertEquals("Check calculated until and loaded until", cdao.getUntil(), check_cdao.getUntil());
        
    }

    /*
     1. Create a recurring whole-day appointment in Outlook (with Outlook OXtender)
     2. Set the recurrence to "every day, for 2 days" and save the appointment
     3. Verify the appointment in OX6 GUI in the different appointment views
    */
    public void testBug4377() throws Throwable { 
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(getContext());
        cdao.setTimezone(TIMEZONE);
        cdao.setTitle("testBug4377");
        CalendarTest.fillDatesInDao(cdao);
        cdao.setEndDate(cdao.getStartDate());
        cdao.setFullTime(true);
        cdao.setRecurrenceID(1);
        cdao.setRecurrenceType(CalendarObject.DAILY);
        cdao.setInterval(2);        
        RecurringResults rss = null;
        CalendarOperation co = new CalendarOperation();
        co.prepareUpdateAction(cdao, null, userid, getPrivateFolder(userid), TIMEZONE);
        assertEquals("Check that the recurring calculator is 1", 1, cdao.getRecurrenceCalculator());
        rss = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
        for (int a = 0; a < rss.size(); a++) {
            RecurringResult rs = rss.getRecurringResult(a);
            long check_day = rs.getEnd() - rs.getStart();
            assertEquals("Check that we got a 24 hours appointment ", CalendarRecurringCollection.MILLI_DAY, check_day);
        }
    }
    
    /*
     1. Create appointment with User A and add another participant (User B) in a private calendar folder
     2. Login with User B and delete the app in
     3. Check that User B does not have the appointment anymore in his own calendar
     4. Check that original appointment has only one participant left (User A)
    */     
    public void testBug4497() throws Throwable {
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        int userid2 = resolveUser(user2);        
        int fid = getPrivateFolder(userid);
        int fid2 = getPrivateFolder(userid2);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");
        SessionObject so2 = SessionObjectWrapper.createSessionObject(userid2, getContext().getContextId(), "myTestIdentifier");
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug4497");
        cdao.setIgnoreConflicts(true);
        CalendarTest.fillDatesInDao(cdao);
        
        UserParticipant userA = new UserParticipant();
        userA.setIdentifier(userid);
       
        UserParticipant userB = new UserParticipant();
        userB.setIdentifier(userid2);        
        
        cdao.setUsers(new UserParticipant[] { userA, userB });        
        
        CalendarSql csql = new CalendarSql(so);        
        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();
        
        CalendarDataObject delete = new CalendarDataObject();
        delete.setContext(so2.getContext());
        delete.setObjectID(object_id);
        
        CalendarSql csql2 = new CalendarSql(so2);        
        csql2.deleteAppointmentObject(delete, fid2, new Date(SUPER_END));
        
        boolean test_exists = false;
        try {
            CalendarDataObject cdao_test_exists = csql2.getObjectById(object_id, fid2);
        } catch(OXException oxe) {
            test_exists = true;
        }
        
        assertTrue("Check that User B does not have the appointment anymore in his own calendar", test_exists);
        
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        UserParticipant up[] = testobject.getUsers();
        assertEquals("Check that original appointment has only one participant left (User A)", 1, up.length);
        
    }
       
    /*
     Create an appointment (Termin) with another participant (Teilnehmer). Save the
     appointment. Select it and edit (bearbeiten) it. Modify it to occur weekly
     (woechentlich). FYI: I did not change the defaults, so it was an the same day and
     recurred every 4 weeks.
    */
    public void testBug4276() throws Throwable {
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        int userid2 = resolveUser(user2);        
        int fid = getPrivateFolder(userid);
        int fid2 = getPrivateFolder(userid2);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");
        SessionObject so2 = SessionObjectWrapper.createSessionObject(userid2, getContext().getContextId(), "myTestIdentifier");
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug4497");
        cdao.setIgnoreConflicts(true);
        CalendarTest.fillDatesInDao(cdao);
        
        UserParticipant userA = new UserParticipant();
        userA.setIdentifier(userid);
       
        UserParticipant userB = new UserParticipant();
        userB.setIdentifier(userid2);        
        
        cdao.setUsers(new UserParticipant[] { userA, userB });        
        
        CalendarSql csql = new CalendarSql(so);        
        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();
        
        CalendarDataObject update = new CalendarDataObject();
        update.setContext(so.getContext());
        update.setObjectID(object_id);
        update.setRecurrenceType(AppointmentObject.WEEKLY);
        update.setInterval(1);
        update.setDays(AppointmentObject.MONDAY);        
        update.setIgnoreConflicts(true);
        csql.updateAppointmentObject(update, fid, new Date(SUPER_END));
        
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        assertEquals("Test that app is a recurring appointment", AppointmentObject.WEEKLY, testobject.getRecurrenceType());
        
    }
    
    /*
     when i open a multi participant appt and add one resource to the appt, the
     following error is thrown:
    */
    public void testBug4119() throws Throwable {
        
        // Clean up appointments
        deleteAllAppointments();
        
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        int userid2 = resolveUser(user2);        
        int fid = getPrivateFolder(userid);
        int fid2 = getPrivateFolder(userid2);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");
        SessionObject so2 = SessionObjectWrapper.createSessionObject(userid2, getContext().getContextId(), "myTestIdentifier");
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug4497");
        cdao.setIgnoreConflicts(true);
        CalendarTest.fillDatesInDao(cdao);
        
        UserParticipant userA = new UserParticipant();
        userA.setIdentifier(userid);
       
        UserParticipant userB = new UserParticipant();
        userB.setIdentifier(userid2);        
        
        cdao.setUsers(new UserParticipant[] { userA, userB });        
        
        CalendarSql csql = new CalendarSql(so);        
        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();
        
        CalendarDataObject update = new CalendarDataObject();
        update.setContext(so.getContext());
        update.setObjectID(object_id);
        
        Participants p = new Participants();
        Participant resource = new ResourceParticipant();
        resource.setIdentifier(100);
        p.add(resource);
        p.add(userB);
        Participant pu[] = p.getList();
        assertEquals("Check that we send a resource and userB", 2, pu.length); // TODO: Maybe this must be fixed!
        update.setParticipants(pu);
        update.setIgnoreConflicts(true);
        
        CalendarDataObject conflicts[] = csql.updateAppointmentObject(update, fid, new Date(SUPER_END));
        assertTrue("Got no conflicts ", conflicts == null);
        
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        Participant participants[] = testobject.getParticipants();
        
        boolean check_userA = false;
        boolean check_userB = false;
        boolean check_Resource = false;
        for (int a = 0; a < participants.length; a++) {
            if (participants[a].getType() == Participant.USER) {
                if (participants[a].getIdentifier() == userid) {
                    check_userA = true;
                } else if (participants[a].getIdentifier() == userid2) {
                    check_userB = true;
                }
            } else if (participants[a].getType() == Participant.RESOURCE) {
                if (participants[a].getIdentifier() == 100) {
                    check_Resource = true;
                }
            }
        }
        
        assertTrue("Test userA", check_userA);
        assertTrue("Test userB", check_userB); // Remove if above check "Check that we send a resource and userB" runs
        assertTrue("Test Resource", check_Resource);
        
    }
     
    /*
    Steps:
    1. Create an appointment 
       "Send customer invoices", 11:00 - 12:00
    2. Series
       Monthly, Each 30th of the month,
       starting 30.12.2006, ending 30.12.2007
    3. Save the appointment
    4. Check the Calendar, Month view

    Results:
    Appointments initially get displayed, but always on the 1st of the following months     
    */
    public void testBug4473() throws Throwable {
        RecurringResults m = null;
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone(TIMEZONE);
        long test_start_date = 1167472800000L; // 30.12.2006 11:00
        long test_end_date = 1167476400000L; // 30.12.2006 12:00
        long test_until = 1293706800000L; // 30.12.2010 12:00:00 
        int check_day = 30;
        cdao.setStartDate(new Date(test_start_date));
        cdao.setEndDate(new Date(test_end_date));
        cdao.setUntil(new Date(test_until));
        cdao.setTitle("testBug4473");
        cdao.setRecurrenceID(1);
        cdao.setRecurrenceType(CalendarObject.MONTHLY);
        cdao.setDayInMonth(check_day);
        //cdao.setDays(0);
        cdao.setInterval(1);        
        CalendarRecurringCollection.fillDAO(cdao);
        m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
        for (int a = 0; a < m.size(); a++) {
            RecurringResult rs = m.getRecurringResult(a);
            Calendar test = Calendar.getInstance();
            test.setTime(new Date(rs.getStart()));
            assertEquals("Check day of month", check_day, test.get(Calendar.DAY_OF_MONTH));
        }
        
    }     
 
    
    /*
    1. Create a new appointment in the OX6 GUI (whole day, recurring every day for 2
    days)
    2. Save the appointment and verify the calendar views

    -> The appointment is shown on every day starting at the given start date of the
    recurring whole-day appointment. In the correct case this appointment should
    only be shown on two days at all. I suppose this is a GUI bug because most
    recurring whole-day appointments work with Outlook OXtender 6 and are
    communicated correctly by the server.    
    */
    public void testBug4766() throws Throwable {
        RecurringResults m = null;
        int fid = getPrivateFolder(userid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");        
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone(TIMEZONE);
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        cdao.removeUntil();
        cdao.setTitle("testBug4766");
        cdao.setFullTime(true);
        cdao.setRecurrenceType(CalendarDataObject.DAILY);
        cdao.setInterval(1);
        cdao.setOccurrence(2);
        
        CalendarRecurringCollection.fillDAO(cdao);
        m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Check size of calulated results", 2, m.size());        
        
        CalendarSql csql = new CalendarSql(so);                
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();        
        
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        CalendarRecurringCollection.fillDAO(testobject);
        m = CalendarRecurringCollection.calculateRecurring(testobject, 0, 0, 0);
        assertEquals("Check size of calulated results", 2, m.size());
    }   
 
    /*
    User A:
    1. Share the personal calendar folder to User B with Level 6 permissions
    User B:
    2. Access the shared calendar via Outlook (with Outlook OXtender)
    3. Create a new appointment in the shared calendar of User A
    4. Select the appointment in the shared calendar and move it to another timefram
    -> A Exception is shown in Outlook OXtenders debug log and occurs in
    open-xchange.log (see attachment)
     */    
    public void testBug4717() throws Throwable {
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        int uid2 = resolveUser(user2);        
        SessionObject so2 = SessionObjectWrapper.createSessionObject(uid2, context.getContextId(), "myTestIdentifier");
        
        Connection readcon = DBPool.pickup(context);
        Connection writecon = DBPool.pickupWriteable(context);        
        
        int fid = getPrivateFolder(userid);
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
        fo.setFolderName("testSharedFolder4717");
        fo.setParentFolderID(fid);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1, oclp2 });       
        
        int shared_folder_id = 0;
        try {
            //ofa.createFolder(fo, so, true, readcon, writecon, false);
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            shared_folder_id = fo.getObjectID();       
            
            CalendarSql csql2 = new CalendarSql(so2);
            CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(so.getContext());
            cdao.setParentFolderID(shared_folder_id);
            CalendarTest.fillDatesInDao(cdao);
            cdao.setTitle("testBug4717 - created by "+user2);
            cdao.setIgnoreConflicts(true);
            
            csql2.insertAppointmentObject(cdao);        
            int object_id = cdao.getObjectID();
            
            CalendarDataObject original_object = csql2.getObjectById(object_id, shared_folder_id);
            
            CalendarDataObject udao = new CalendarDataObject();
            udao.setContext(so.getContext());
            udao.setObjectID(object_id);
            Date check_start_date = new Date(cdao.getStartDate().getTime()+3600000); // move 1 h
            Date check_end_date = new Date(cdao.getEndDate().getTime()+3600000); // move 1 h
            udao.setStartDate(check_start_date); 
            udao.setEndDate(check_end_date); 
            udao.setTitle("testBug4717 - updated by "+user2);
            
            Participants participants = new Participants();
            Participant p = new UserParticipant();
            p.setIdentifier(userid);
            participants.add(p);            
            
            Participant p2 = new UserParticipant();
            p2.setIdentifier(uid2);
            participants.add(p2);            
            udao.setParticipants(participants.getList());
            udao.setUsers(participants.getUsers());
            udao.setIgnoreConflicts(true);

            csql2.updateAppointmentObject(udao, shared_folder_id, cdao.getLastModified());            
            
            CalendarDataObject testobject = csql2.getObjectById(object_id, shared_folder_id);
            UserParticipant up[] = testobject.getUsers();
            assertTrue("UserParticipant not null", up != null);
            assertEquals("Check that we got two participants ", 2, up.length);
            assertEquals("Check start date" , check_start_date, testobject.getStartDate());
            assertEquals("Check end date" , check_end_date, testobject.getEndDate());
            
            CalendarDataObject second_update = new CalendarDataObject();
            second_update.setContext(so.getContext());
            second_update.setObjectID(object_id);
            second_update.setStartDate(original_object.getStartDate()); // back to origin time
            second_update.setEndDate(original_object.getEndDate()); // back to origin time
            second_update.setTitle("testBug4717 - updated (2) by "+user2);            
            second_update.setIgnoreConflicts(true);            
            
            csql2.updateAppointmentObject(second_update, shared_folder_id, testobject.getLastModified());
            
            CalendarDataObject testobject2 = csql2.getObjectById(object_id, shared_folder_id);            
            assertEquals("Check start date" , cdao.getStartDate(), testobject2.getStartDate());
            assertEquals("Check end date" , cdao.getEndDate(), testobject2.getEndDate());            
            
        } finally {
            try {
                if (shared_folder_id > 0) {
                    //ofa.deleteFolder(shared_folder_id, so, true, SUPER_END);
                    oxma.deleteFolder(new FolderObject(shared_folder_id), true, SUPER_END);
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
    
    /*
    after creating a not ending series appointment every first Monday every month,
    only the first appointment can be seen in the calendar.
     */
    
    public void testBug4838() throws Throwable {
        RecurringResults m = null;
        int fid = getPrivateFolder(userid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");        
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone(TIMEZONE);
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        cdao.removeUntil();
        cdao.setTitle("testBug4838");
        cdao.setRecurrenceType(CalendarDataObject.MONTHLY);
        cdao.setInterval(1);
        cdao.setDays(CalendarDataObject.MONDAY);
        cdao.setDayInMonth(1);
        cdao.setIgnoreConflicts(true);
        
        CalendarSql csql = new CalendarSql(so);                
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();        
        
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        
        m = CalendarRecurringCollection.calculateRecurring(testobject, 0, 0, 0);
        assertTrue("Calculated results are > 0 ", m.size() > 1);
        int last_month = 0;
        for (int a = 0; a < m.size(); a++) {
            RecurringResult rr = m.getRecurringResult(a);
            Calendar test = Calendar.getInstance();
            test.setFirstDayOfWeek(Calendar.MONDAY);
            Date date = new Date(rr.getStart());
            test.setTime(date);
            if (a > 0 && last_month != 11) {
                assertEquals("Compare month", last_month+1, test.get(Calendar.MONTH));
            }
            assertEquals("Check day of month", Calendar.MONDAY, test.get(Calendar.DAY_OF_WEEK));            
            last_month = test.get(Calendar.MONTH);
        }
        
    }
    
    /*
    Changing the recurrence of a weekly appointment to daily appointment removes all
    recurrence information. 
     */
    
    public void testBug5010() throws Throwable {
        RecurringResults m = null;
        int fid = getPrivateFolder(userid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");        
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone(TIMEZONE);
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        cdao.setTitle("testBug5010");
        cdao.setRecurrenceType(CalendarDataObject.WEEKLY);
        cdao.setInterval(1);
        cdao.setDays(CalendarDataObject.MONDAY);
        cdao.setIgnoreConflicts(true);
        
        CalendarSql csql = new CalendarSql(so);                
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();        
        
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        
        assertEquals("Check rec type ", CalendarDataObject.WEEKLY, testobject.getRecurrenceType());
        
        CalendarDataObject update = new CalendarDataObject();
        update.setContext(so.getContext());
        update.setObjectID(object_id);
        update.setRecurrenceType(CalendarDataObject.DAILY);
        update.setInterval(1);
        
        csql.updateAppointmentObject(update, fid, cdao.getLastModified());
        
        CalendarDataObject testobject2 = csql.getObjectById(object_id, fid);
        
        assertTrue("Check that the recurring has been changed ", testobject.getRecurrenceType() != testobject2.getRecurrenceType());
        
        assertEquals("Check rec type ", CalendarDataObject.DAILY, testobject2.getRecurrenceType());
        
    }    

    /*
    Create a new appointment and make it occur every 2 days. Save it. Go to
    "bearbeiten" and change it to every 3 days. Save. See that it does not change.
     */
    
    public void testBug5012() throws Throwable {   
        RecurringResults m = null;
        int fid = getPrivateFolder(userid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");        
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone(TIMEZONE);
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        cdao.setTitle("testBug5012");
        cdao.setRecurrenceType(CalendarDataObject.DAILY);
        cdao.setInterval(1);
        cdao.setIgnoreConflicts(true);
        
        CalendarSql csql = new CalendarSql(so);                
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();
        
        
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        
        
        CalendarDataObject update = new CalendarDataObject();
        update.setContext(so.getContext());
        update.setObjectID(object_id);
        update.setStartDate(cdao.getStartDate());
        update.setEndDate(cdao.getEndDate());
        update.setRecurrenceType(CalendarDataObject.DAILY);
        update.setInterval(2);
        
        csql.updateAppointmentObject(update, fid, cdao.getLastModified());
        
        CalendarDataObject testobject2 = csql.getObjectById(object_id, fid);
        
        assertEquals("Check that the sequence type is identical ", testobject.getRecurrenceType(), testobject2.getRecurrenceType());
        assertTrue("Check that the interval has been changed", testobject.getInterval() != testobject2.getInterval());
        

        CalendarDataObject update2 = new CalendarDataObject();
        update2.setContext(so.getContext());
        update2.setObjectID(object_id);
        update2.setStartDate(cdao.getStartDate());
        update2.setEndDate(cdao.getEndDate());
        update2.setRecurrenceType(CalendarDataObject.DAILY);
        update2.setInterval(1);
        Date check_until_date = new Date(cdao.getUntil().getTime()+CalendarRecurringCollection.MILLI_DAY);
        update2.setUntil(check_until_date);
        
        csql.updateAppointmentObject(update2, fid, testobject2.getLastModified());
        
        CalendarDataObject testobject3 = csql.getObjectById(object_id, fid);
        
        assertEquals("Check that the sequence type is identical ", testobject.getRecurrenceType(), testobject3.getRecurrenceType());
        assertTrue("Check that the interval has been changed", 1 == testobject3.getInterval());
        assertEquals("Check correct until ", check_until_date, testobject3.getUntil());
        
    } 
    
    /*
    1. Login as user A
    2. Share your private calendar folder to user B
    3. Use rights: View Folder, Read all objects, Write all objects, no delete
    permission.
    4. Logout
    5. Login as user B
    6. Go to shared calendar folder of user A
    7. Create appointment
    */
    public void testBug5130() throws Throwable {
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        int uid2 = resolveUser(user2);        
        SessionObject so2 = SessionObjectWrapper.createSessionObject(uid2, context.getContextId(), "myTestIdentifier");
        
        Connection readcon = DBPool.pickup(context);
        Connection writecon = DBPool.pickupWriteable(context);        
        
        int fid = getPrivateFolder(userid);
        //OXFolderAction ofa = new OXFolderAction(so);
        final OXFolderManager oxma = new OXFolderManagerImpl(so, readcon, writecon);
        FolderObject fo = new FolderObject();
        
        OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(userid);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);
        OCLPermission oclp2 = new OCLPermission();
        oclp2.setEntity(uid2);
        oclp2.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.NO_PERMISSIONS);
        fo.setFolderName("testSharedFolder5130");
        fo.setParentFolderID(fid);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1, oclp2 });       
        
        int shared_folder_id = 0;
        try {
            //ofa.createFolder(fo, so, true, readcon, writecon, false);
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            shared_folder_id = fo.getObjectID();       
            
            CalendarSql csql2 = new CalendarSql(so2);
            CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(so.getContext());
            cdao.setParentFolderID(shared_folder_id);
            CalendarTest.fillDatesInDao(cdao);
            cdao.setTitle("testBug4717 - created by "+user2);
            cdao.setIgnoreConflicts(true);
            
            int object_id = 0;
            try {
                csql2.insertAppointmentObject(cdao);        
                object_id = cdao.getObjectID();
            } catch(OXPermissionException ope) {
                // exzellent
                ope.printStackTrace();
            }
            if (object_id > 0) {
                fail("Object could be created !");
            }
        } finally {
            try {
                if (shared_folder_id > 0) {
                    //ofa.deleteFolder(shared_folder_id, so, true, SUPER_END);
                    oxma.deleteFolder(new FolderObject(shared_folder_id), true, SUPER_END);
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
    
    /*
    1. As User A: Create a new appointment and invite User B
    2. As User B: Verify the appointment in the calendar
    -> The appointment is shown to User B, OK
    3. As User B: Delete the appointment from the personal calendar
    4. As User A: Verify the appointment participants.
    */
    public void testBug5144() throws Throwable {
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        int userid2 = resolveUser(user2);        
        int fid = getPrivateFolder(userid);
        int fid2 = getPrivateFolder(userid2);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");
        SessionObject so2 = SessionObjectWrapper.createSessionObject(userid2, getContext().getContextId(), "myTestIdentifier");
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug5144");
        cdao.setIgnoreConflicts(true);
        CalendarTest.fillDatesInDao(cdao);
        
        UserParticipant userA = new UserParticipant();
        userA.setIdentifier(userid);
       
        UserParticipant userB = new UserParticipant();
        userB.setIdentifier(userid2);        
        
        cdao.setUsers(new UserParticipant[] { userA, userB });        
        
        CalendarSql csql = new CalendarSql(so);
        CalendarSql csql2 = new CalendarSql(so2);
        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();        
        
        CalendarDataObject delete = new CalendarDataObject();
        delete.setContext(so2.getContext());
        delete.setObjectID(object_id);
          
        csql2.deleteAppointmentObject(delete, fid2, new Date(CalendarTest.SUPER_END));
        
        try {
            CalendarDataObject testobject = csql2.getObjectById(object_id, fid2);
            fail("User should get an OXPermissionException ");
        } catch(OXPermissionException ope) {
            // Excellent
        }
     
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        UserParticipant up[] = testobject.getUsers();
        Participant p[] = testobject.getParticipants();
        
        assertEquals("Check that only one userparticipant exists", 1, up.length);
        assertEquals("Check that only one participant exists", 1, p.length);
        
     }

    /*
    A yearly recurring event can be set to the "first Tuesday in january".
    If so, it happens on the first Tuesday in february(!) in the year 2009 (!)
    The date is ok in 2008 and 2010.
     */
    public void testBug5202() throws Throwable {
        RecurringResults m = null;
        int fid = getPrivateFolder(userid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");        
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone(TIMEZONE);
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        cdao.removeUntil();
        cdao.setTitle("testBug5202");
        cdao.setRecurrenceType(CalendarDataObject.YEARLY);
        cdao.setInterval(1);
        cdao.setDays(CalendarDataObject.TUESDAY);
        cdao.setMonth(Calendar.JANUARY);
        cdao.setDayInMonth(1);
        cdao.setIgnoreConflicts(true);
        
        CalendarSql csql = new CalendarSql(so);                
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();        
        
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        
        m = CalendarRecurringCollection.calculateRecurring(testobject, 0, 0, 0);
        assertTrue("Calculated results are > 0 ", m.size() > 1);

        for (int a = 0; a < m.size(); a++) {
            RecurringResult rr = m.getRecurringResult(a);
            Calendar test = Calendar.getInstance();
            test.setFirstDayOfWeek(Calendar.MONDAY);
            Date date = new Date(rr.getStart());
            test.setTime(date);
            assertEquals("Test that the day is a TUESDAY", Calendar.TUESDAY, test.get(Calendar.DAY_OF_WEEK));
            assertEquals("Test that the month is JAN", Calendar.JANUARY, test.get(Calendar.MONTH));
        }
        
    }    

    
    /*
    User A:
    1. RMB the personal calendar -> "Properties"
    2. Click "Rights" and add User B
    3. Assign the following rights and save the folder permissions:

    Rechtevergabe: None
    Ordnerrechte: Objekte anlegen
    Leserechte: Alle
    Schreibrechte: Alle
    Loeschrechte: Keine

    4. Create a new appointment with title "foobar" in the personal calendar which
    is now shared to User B

    User B:
    5. Select the shared calendar of User A
    6. Select "foobar" in the shared calendar and click "Appointment" -> "Edit" in
    the panel
    7. Change the title to "foobar2" and save the appointment

    8. Verify the calendar as User A and User B
    -> "foobar" has been deleted from the personal folder of User A and now exists
    in the personal folder of User B. This must not be possible because User B has
    no delete rights to the shared calendar of User A.
     */
    public void testBug5194() throws Throwable {
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        int uid2 = resolveUser(user2);        
        SessionObject so2 = SessionObjectWrapper.createSessionObject(uid2, context.getContextId(), "myTestIdentifier");
        
        Connection readcon = DBPool.pickup(context);
        Connection writecon = DBPool.pickupWriteable(context);        
        
        int fid = getPrivateFolder(userid);
        //OXFolderAction ofa = new OXFolderAction(so);
        final OXFolderManager oxma = new OXFolderManagerImpl(so, readcon, writecon);
        FolderObject fo = new FolderObject();
        
        OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(userid);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);
        OCLPermission oclp2 = new OCLPermission();
        oclp2.setEntity(uid2);
        oclp2.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.NO_PERMISSIONS);
        fo.setFolderName("testSharedFolder5194");
        fo.setParentFolderID(fid);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1, oclp2 });       
        
        int shared_folder_id = 0;
        try {
            //ofa.createFolder(fo, so, true, readcon, writecon, false);
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            shared_folder_id = fo.getObjectID();       
            
            CalendarSql csql = new CalendarSql(so);
            CalendarSql csql2 = new CalendarSql(so2);
            
            CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(so.getContext());
            cdao.setParentFolderID(shared_folder_id);
            CalendarTest.fillDatesInDao(cdao);
            cdao.setTitle("testBug5194 - created by "+user2);
            cdao.setIgnoreConflicts(true);
            csql.insertAppointmentObject(cdao);        
            int object_id = cdao.getObjectID();
            
            CalendarDataObject testobject = csql2.getObjectById(object_id, shared_folder_id);
            
            UserParticipant up[] = testobject.getUsers();
            assertEquals("Check that only user 1 is participant", 1, up.length);
            assertEquals("Check that only user 1 is participant", userid, up[0].getIdentifier());
            
            CalendarDataObject update = new CalendarDataObject();
            update.setContext(so2.getContext());
            update.setObjectID(object_id);
            update.setTitle("testBug5194 - updated by "+user2);
            
            csql2.updateAppointmentObject(update, shared_folder_id, testobject.getLastModified());
            
            
            CalendarDataObject testobject2 = csql.getObjectById(object_id, shared_folder_id);
            assertEquals("Check folder for user 1", shared_folder_id, testobject.getParentFolderID());
            
            
            up = testobject2.getUsers();
            assertEquals("Check that only user 1 is participant", 1, up.length);
            assertEquals("Check that only user 1 is participant", userid, up[0].getIdentifier());
            assertEquals("Check correct folder", shared_folder_id, testobject2.getParentFolderID());
            
            
            CalendarDataObject testobject3 = csql2.getObjectById(object_id, shared_folder_id);
            up = testobject2.getUsers();
            assertEquals("Check that only user 1 is participant", 1, up.length);
            assertEquals("Check that only user 1 is participant", userid, up[0].getIdentifier());
            assertEquals("Check correct folder", shared_folder_id, testobject2.getParentFolderID());            
            
        } finally {
            try {
                if (shared_folder_id > 0) {
                    //ofa.deleteFolder(shared_folder_id, so, true, SUPER_END);
                    oxma.deleteFolder(new FolderObject(shared_folder_id), true, SUPER_END);
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
    
    
    /*
     Whole day apps should be visible in all views
    */
    public void testBug5222AND5171() throws Throwable {    
        deleteAllAppointments();        
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        int fid = getPrivateFolder(userid);
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug5171");
        cdao.setStartDate(new Date(1168426800000L)); // 01.2007 12:00
        cdao.setEndDate(new Date(1170154800000L)); // 30.01.2007 12:00
        cdao.setFullTime(true);
        cdao.setIgnoreConflicts(true);
        
        
        
        CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        int object_id = cdao.getObjectID();
        
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        assertEquals("Check object_id", object_id, testobject.getObjectID());
        
        
        long range_start = 1168815600000L; // 15.01.2007 00:00
        long range_end = 1169420400000L; // 22.01.2007 00:00
        
        SearchIterator si = csql.getAppointmentsBetweenInFolder(fid, cols, new Date(range_start), new Date(range_end), 0, null);
        boolean found = false;
        while (si.hasNext()) {
            CalendarDataObject temp = (CalendarDataObject)si.next();
            if (temp.getObjectID() == object_id) {
                assertTrue("Fulltime is set to true", temp.getFullTime());
                if (CalendarCommonCollection.inBetween(temp.getStartDate().getTime(), temp.getEndDate().getTime(), range_start, range_end)) {
                    found = true;
                }
                
            }
        }
        assertTrue("Found no appointment (testBug5141)", found);   
        
        
        CalendarDataObject cdao2 = new CalendarDataObject();
        cdao2.setContext(so.getContext());
        cdao2.setParentFolderID(fid);
        cdao2.setTitle("testBug5222");
        CalendarTest.fillDatesInDao(cdao2);
        cdao2.removeUntil();
        cdao2.setFullTime(true);
        cdao2.setIgnoreConflicts(true);
        
        
        csql.insertAppointmentObject(cdao2);
        int object_id2 = cdao2.getObjectID();        
        
        testobject = csql.getObjectById(object_id2, fid);
        assertEquals("Check object_id2", object_id2, testobject.getObjectID());        
        
        range_start = CalendarRecurringCollection.normalizeLong(cdao2.getStartDate().getTime());
        range_end = range_start + CalendarRecurringCollection.MILLI_DAY;
        
        si = csql.getAppointmentsBetweenInFolder(fid, cols, new Date(range_start), new Date(range_end), 0, null);
        
        found = false;
        while (si.hasNext()) {
            CalendarDataObject temp = (CalendarDataObject)si.next();
            if (temp.getObjectID() == object_id2) {
                assertTrue("Fulltime is set to true", temp.getFullTime());
                if (CalendarCommonCollection.inBetween(temp.getStartDate().getTime(), temp.getEndDate().getTime(), range_start, range_end)) {
                    found = true;
                }
                
            }
        }
        
        assertTrue("Found no appointment (testBug5222)", found);
        
    }
    
    /*
    Recurring multi-whole-day appointments are shown only on one day
    Create a new appointment, whole-day, spaning 2 days (22.01.2007 - 24.01.2007)
    */
    public void testBug4987() throws Throwable {
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        int fid = getPrivateFolder(userid);
        CalendarSql csql = new CalendarSql(so);
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        long start = cdao.getStartDate().getTime();
        start = CalendarRecurringCollection.normalizeLong(start);
        long end = (start + (CalendarRecurringCollection.MILLI_DAY * 2));
        
        int calculator = (int)((end-start)/CalendarRecurringCollection.MILLI_DAY);
        assertEquals("Check Calculator result ", 2, calculator);
        
        cdao.setStartDate(new Date(start));
        cdao.setEndDate(new Date(end));
        cdao.setFullTime(true);
        cdao.removeUntil();
        cdao.setTitle("testBug4987");        
        cdao.setRecurrenceType(CalendarDataObject.DAILY);
        cdao.setInterval(3);
        
        csql.insertAppointmentObject(cdao);
        int object_id = cdao.getObjectID();
        
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        
        RecurringResults m = CalendarRecurringCollection.calculateRecurring(testobject, 0, 0, 0);
        assertTrue("Calculated results are > 0 ", m.size() > 0);
        assertTrue("Fulltime is set", testobject.getFullTime());
        assertEquals("Check that the ", calculator, testobject.getRecurrenceCalculator());
        
        for (int a = 0; a < m.size(); a++) {        
            RecurringResult rr = m.getRecurringResult(a);
            long check_start = rr.getStart();
            long check_end = rr.getEnd();
            int check_calculator = (int)((check_end-check_start)/CalendarRecurringCollection.MILLI_DAY);
            assertEquals("Check calculated results", calculator, check_calculator);
            //System.out.println(">>>> "+new Date(check_start) + " ---- "+new Date(check_end));
        }
        
    }
     
    public void testBug5306() throws Throwable {
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        int fid = getPrivateFolder(userid);
        CalendarSql csql = new CalendarSql(so);
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        cdao.setRecurrenceType(CalendarObject.DAILY);
        cdao.setInterval(1);                
        cdao.removeUntil();
        int OCCURRENCE_TEST = 6;
        cdao.setOccurrence(OCCURRENCE_TEST);
        CalendarRecurringCollection.fillDAO(cdao);
        RecurringResults m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Calculated results is correct", OCCURRENCE_TEST, m.size());
        
        long super_start_test = CalendarRecurringCollection.normalizeLong(m.getRecurringResult(m.size()-1).getStart());
        long super_end_test = super_start_test + CalendarRecurringCollection.MILLI_DAY;
        
        m = CalendarRecurringCollection.calculateRecurring(cdao, super_start_test, super_end_test, 0);
        
        assertEquals("Calculated results is correct", 1, m.size());        
        assertTrue("Occurrence is set", cdao.containsOccurrence());
        
        super_start_test = super_start_test+CalendarRecurringCollection.MILLI_DAY;
        super_end_test = super_end_test+CalendarRecurringCollection.MILLI_DAY;
        
        Date s = new Date(super_start_test);
        Date e = new Date(super_end_test);
        
        RecurringResults m2 = CalendarRecurringCollection.calculateRecurring(cdao, super_start_test, super_end_test, 0);
        
        assertTrue("Should not got results", m2 == null || m2.getRecurringResult(0) == null);
        
        Calendar calc = Calendar.getInstance();        
        calc.setFirstDayOfWeek(Calendar.MONDAY);
        calc.setTime(cdao.getStartDate());
        calc.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        
        long check_week_start = calc.getTimeInMillis();
        calc.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        long check_week_end = calc.getTimeInMillis();

        RecurringResults m3 = CalendarRecurringCollection.calculateRecurring(cdao, check_week_start, check_week_end, 0);
        int sub_value = m3.size();        
        
        calc.setTimeInMillis(check_week_start);
        calc.add(Calendar.WEEK_OF_YEAR, 1);
        check_week_start = calc.getTimeInMillis();
        calc.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        check_week_end = calc.getTimeInMillis();
        
        RecurringResults m4 = CalendarRecurringCollection.calculateRecurring(cdao, check_week_start, check_week_end, 0);
        int rest_value = m4.size();        
        
        assertEquals("Calculated results is correct", OCCURRENCE_TEST, sub_value+rest_value);
        
    }
    
     /*
     As User A:
     1. Create a new appointment in your default private calendar
     2. Edit the appointment, and change the folder to the shared folder of user B
     3. Verify the shared folder of user B
     4. Verify the private calendar folder of user A
     
     Note: The server should not move sucn an appointment because the current user would be removed
     and the shared folder owner must be added as participant.
     */    
     public void testBug6910() throws Throwable {
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        int uid2 = resolveUser(user2);        
        SessionObject so2 = SessionObjectWrapper.createSessionObject(uid2, context.getContextId(), "myTestIdentifier");
        
        Connection readcon = DBPool.pickup(context);
        Connection writecon = DBPool.pickupWriteable(context);        
        
        int fid = getPrivateFolder(userid);
        int fid2 = getPrivateFolder(uid2);
        
        final OXFolderManager oxma = new OXFolderManagerImpl(so2, readcon, writecon);
        FolderObject fo = new FolderObject();
        
        OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(uid2);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);
        OCLPermission oclp2 = new OCLPermission();
        oclp2.setEntity(userid);
        oclp2.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        fo.setFolderName("testSharedFolder6910");
        fo.setParentFolderID(fid2);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1, oclp2 });       
        
        int shared_folder_id = 0;
        try {
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            shared_folder_id = fo.getObjectID();       
            
            CalendarSql csql = new CalendarSql(so);
            CalendarSql csql2 = new CalendarSql(so2);
            
            CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(so.getContext());
            cdao.setParentFolderID(fid);
            CalendarTest.fillDatesInDao(cdao);
            cdao.setTitle("testBug6910 - Step 1");
            cdao.setIgnoreConflicts(true);
            csql.insertAppointmentObject(cdao);        
            int object_id = cdao.getObjectID();
            
            CalendarDataObject testobject = csql.getObjectById(object_id, fid);
            
            UserParticipant up[] = testobject.getUsers();
            assertTrue("up > 0", up.length > 0);
            
            CalendarDataObject update = new CalendarDataObject();
            update.setContext(so.getContext());
            update.setObjectID(object_id);
            update.setParentFolderID(shared_folder_id);
            update.setIgnoreConflicts(true);
            //update.setUsers(up); // THE GUI DOES NOT SEND THE USERS !!!
            update.setTitle("testBug6910 - Step 2");
            
            csql.updateAppointmentObject(update, fid, cdao.getLastModified());
 
            testobject = csql2.getObjectById(object_id, shared_folder_id);
            UserParticipant user_test[] = testobject.getUsers();
            
            boolean found_user1 = false;
            boolean found_user2 = false;
            
            for (int a = 0; a < user_test.length; a++) {
                if (user_test[a].getIdentifier() == userid) {
                    found_user1 = true;
                }
                if (user_test[a].getIdentifier() == uid2) {
                    found_user2 = true;
                }
            }
            
            assertTrue("User A is not in the participants", found_user1);
            assertTrue("User B is not in the participants", found_user2);
            
            
            
        } finally {
            try {
                if (shared_folder_id > 0) {
                    oxma.deleteFolder(new FolderObject(shared_folder_id), true, SUPER_END);
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
  
     /*

      Precondition:
       - A main-calendar, the users personal calendar folder
       - A sub-calendar, a subfolder of the users calendar

      1. Create a new appointment series in a sub-calendar of the personal calendar

      2. Select one occurrence of the series and click "Edit" in the panel

      3. Select "Single Appointment" in the dialog popup

      4. Select another calendar folder in the edit contact window (e.g. the main
      personal calendar)

      5. Save the changes

      6. Verify the appointment sequence in the sub-calendar and the main-calendar    

      -> The selected occurrence has been kept in the sub-calendar while the other
      part of the occurrence has been moved to the main-calendar. This is exactly the
      opposite of which is expected.
      */
     public void testBug6400() throws Throwable {
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        
        
        Connection readcon = DBPool.pickup(context);
        Connection writecon = DBPool.pickupWriteable(context);        
        
        int fid = getPrivateFolder(userid);
        final OXFolderManager oxma = new OXFolderManagerImpl(so, readcon, writecon);
        FolderObject fo = new FolderObject();
        
        OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(userid);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);

        
        fo.setFolderName("testBug6400");
        fo.setParentFolderID(fid);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1 });       
        
        int subfolder_id = 0;
        try {
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            subfolder_id = fo.getObjectID();       
            
            CalendarSql csql = new CalendarSql(so);

            
            CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(so.getContext());
            cdao.setParentFolderID(subfolder_id);
            CalendarTest.fillDatesInDao(cdao);
            cdao.setTitle("testBug6400 - Step 1");
            cdao.setIgnoreConflicts(true);
            cdao.setRecurrenceType(CalendarObject.DAILY);
            cdao.setInterval(1);              

            csql.insertAppointmentObject(cdao);        
            int object_id = cdao.getObjectID();
            
            CalendarDataObject testobject = csql.getObjectById(object_id, subfolder_id);
            
        
            Date save_start = testobject.getStartDate();
            Date save_end = testobject.getEndDate();
            
        
            CalendarDataObject update = new CalendarDataObject();
            update.setContext(so.getContext());
            update.setTitle("testBug6400 - Step 2");
            update.setObjectID(object_id);
            update.setRecurrencePosition(1);
            update.setIgnoreConflicts(true);
            update.setParentFolderID(fid);        

            try {
                csql.updateAppointmentObject(update, subfolder_id, new Date(SUPER_END));
                int object_id_exception = update.getObjectID();
                fail("Test failed. An exception can not be moved into a different folder.");
            } catch(OXCalendarException e) {
                // Must fail
                assertEquals("Check correct error message", 66, e.getDetailNumber());
            }
            
                        
        } finally {
            int x = 0;
            try {
                if (subfolder_id > 0) {
                    oxma.deleteFolder(new FolderObject(subfolder_id), true, SUPER_END);
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

     /*

     Steps:
     1. In your private calendar "Kalendar" create a new appointment:
     Beschreibung: please don't remind me
     Beginnt am: 9:00 (assuming it's 9:30 currently)
     Endet am: 10:00
     Ort: testort
     Anmerkung: testanmerkung
     Don't set a reminder

     2. LMB-click to select the appointment "please don't remind me"
     3. LMB-click on Panel --> Kalender --> Verschieben
     4. In the popup-window select your calendar "subFol" to move it to your other
     calendar


     Result:
     After some time you see a reminder popup for the appointment "please don't
     remind me"
     */
     public void testBug6214() throws Throwable {
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestSearch");
        
        
        Connection readcon = DBPool.pickup(context);
        Connection writecon = DBPool.pickupWriteable(context);        
        
        int fid = getPrivateFolder(userid);
        final OXFolderManager oxma = new OXFolderManagerImpl(so, readcon, writecon);
        FolderObject fo = new FolderObject();
        
        OCLPermission oclp1 = new OCLPermission();
        oclp1.setEntity(userid);
        oclp1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        oclp1.setFolderAdmin(true);

        
        fo.setFolderName("testBug6214");
        fo.setParentFolderID(fid);
        fo.setModule(FolderObject.CALENDAR);
        fo.setType(FolderObject.PRIVATE);
        fo.setPermissionsAsArray(new OCLPermission[] { oclp1 });       
        
        int subfolder_id = 0;
        try {
            fo = oxma.createFolder(fo, true, System.currentTimeMillis());
            subfolder_id = fo.getObjectID();       
            
            CalendarSql csql = new CalendarSql(so);

            
            CalendarDataObject cdao = new CalendarDataObject();
            cdao.setContext(so.getContext());
            cdao.setParentFolderID(fid);
            CalendarTest.fillDatesInDao(cdao);
            cdao.setTitle("testBug6214 - Step 1");
            cdao.setIgnoreConflicts(true);
            

            csql.insertAppointmentObject(cdao);        
            int object_id = cdao.getObjectID();
            
            CalendarDataObject update = csql.getObjectById(object_id, fid);
        
            UserParticipant up_insert[] = update.getUsers();

            assertTrue("Got a participant", up_insert != null);
        
            assertEquals("Check that no alarm is set (insert)", false, up_insert[0].containsAlarm());
            
            update.setTitle("testBug6214 - Step 2");
            update.setIgnoreConflicts(true);
            update.setParentFolderID(subfolder_id);
      
            csql.updateAppointmentObject(update, fid, cdao.getLastModified());

            CalendarDataObject testobject = csql.getObjectById(object_id, subfolder_id);
            
            UserParticipant up_update[] = testobject.getUsers();
            
            assertTrue("Got a participant", up_update != null);
            
            assertEquals("Check that no alarm is set (update)", false, up_update[0].containsAlarm());
                        
        } finally {
            int x = 0;
            try {
                if (subfolder_id > 0) {
                    oxma.deleteFolder(new FolderObject(subfolder_id), true, SUPER_END);
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
    

     public void testBug6535() throws Throwable {
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        int fid = CalendarTest.getCalendarDefaultFolderForUser(userid, context);
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(fid);
        
        CalendarTest.fillDatesInDao(cdao);
        
        cdao.setTitle("testBug6535 - Step 1");
        cdao.setRecurrenceType(CalendarObject.DAILY);
        cdao.setInterval(1);
       
        cdao.setIgnoreConflicts(true);
        
        UserParticipant userparticipants = new UserParticipant();
        userparticipants.setIdentifier(userid);
        userparticipants.setConfirm(AppointmentObject.ACCEPT);
        cdao.setUsers(new UserParticipant[] { userparticipants });
        
        CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        int object_id = cdao.getObjectID();
        
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        UserParticipant up1[] = testobject.getUsers();
        assertTrue("Check for null object", up1 != null);
        assertEquals("Check confirm status", CalendarObject.ACCEPT, up1[0].getConfirm());
        
        CalendarDataObject update = new CalendarDataObject();
        update.setContext(so.getContext());
        update.setObjectID(object_id);
        update.setParentFolderID(fid);
        update.setRecurrencePosition(1);
        update.setStartDate(new Date(testobject.getStartDate().getTime() + 3600000));
        update.setEndDate(new Date(testobject.getEndDate().getTime() + 3600000));
        update.setIgnoreConflicts(true);
        update.setUsers(up1);
        update.setTitle("testBug6535 - Exception");
        
        csql.updateAppointmentObject(update, fid, cdao.getLastModified());
        
        CalendarDataObject testobject_exception = csql.getObjectById(update.getObjectID(), fid);
        UserParticipant up2[] = testobject_exception.getUsers();
        assertTrue("Check for null object", up2 != null);
        assertEquals("Check confirm status", CalendarObject.ACCEPT, up2[0].getConfirm());
        
        CalendarDataObject testobject_after_update = csql.getObjectById(object_id, fid);
        UserParticipant up3[] = testobject_after_update.getUsers();
        assertTrue("Check for null object", up3 != null);
        assertEquals("Check confirm status", CalendarObject.ACCEPT, up3[0].getConfirm());
        
        
     }
     
     /*
     * Create a recurrence appointment
     * Create an exception
     * Delete this exception
     * The "recurrence_id" xmltag is missing, so there is no way for the OXtender to
       reference the deleted item.
     */
     public void testBug6960() throws Throwable {
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        int folder_id = CalendarTest.getCalendarDefaultFolderForUser(userid, context);
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(folder_id);
        
        CalendarTest.fillDatesInDao(cdao);
        
        cdao.setTitle("testBug6960");
        cdao.setRecurrenceType(CalendarObject.DAILY);
        cdao.setInterval(1);
        cdao.setDays(1);
        
        cdao.setIgnoreConflicts(true);
        
        CalendarSql csql = new CalendarSql(so);
        csql.insertAppointmentObject(cdao);
        int object_id = cdao.getObjectID();
        Date last = cdao.getLastModified();
        
        CalendarDataObject update = new CalendarDataObject();
        update.setContext(so.getContext());
        update.setObjectID(object_id);
        update.setIgnoreConflicts(true);
        
        
        RecurringResults rss = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 3);
        RecurringResult rs = rss.getRecurringResult(0);
        long new_start = rs.getStart()+3600000;
        long new_end = rs.getEnd()+3600000;
        
        Date test_new_start_date = new Date(new_start);
        Date test_new_end_date = new Date(new_end);
        
        update.setStartDate(test_new_start_date);
        update.setEndDate(test_new_end_date);
        
        update.setTitle("testBug6960 - Exception");
        
        Date test_exception_date = new Date(CalendarRecurringCollection.normalizeLong(new_start));
        
        update.setRecurrenceDatePosition(test_exception_date);
        
        
        csql.updateAppointmentObject(update, folder_id, new Date(SUPER_END));
 
        CalendarDataObject testdelete = new CalendarDataObject();
        testdelete.setContext(so.getContext());
        testdelete.setObjectID(update.getObjectID());
        csql.deleteAppointmentObject(testdelete, folder_id, new Date(SUPER_END));
        
        assertTrue("Check that we got the recurrence id back", testdelete.containsRecurrenceID());
        assertEquals("Check that we got the correct recurrence id", object_id, testdelete.getRecurrenceID());
       
        
        
 
     }

     
     
    /*
    1) User A creates a group appointment with User B & User C and a reminder 15 min
    2) User B click on the calendar (outlook) and receives the appointment and a
    reminder
    3) User B clicks on the reminder (close)
     
     
    This bug also test bug #8196
     
    */
    public void testBug7883() throws Throwable {
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");        
        int userid2 = resolveUser(user2);        
        int fid = getPrivateFolder(userid);
        int fid2 = getPrivateFolder(userid2);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, getContext().getContextId(), "myTestIdentifier");
        SessionObject so2 = SessionObjectWrapper.createSessionObject(userid2, getContext().getContextId(), "myTestIdentifier");
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(so.getContext());
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug7883");
        cdao.setIgnoreConflicts(true);
        CalendarTest.fillDatesInDao(cdao);
        
        UserParticipant userA = new UserParticipant();
        userA.setIdentifier(userid);
        userA.setAlarmMinutes(15);
       
        UserParticipant userB = new UserParticipant();
        userB.setIdentifier(userid2);        
        
        cdao.setUsers(new UserParticipant[] { userA, userB });        
        
        CalendarSql csql = new CalendarSql(so);
        CalendarSql csql2 = new CalendarSql(so2);
        
        csql.insertAppointmentObject(cdao);        
        int object_id = cdao.getObjectID();       
                
        CalendarDataObject testobject = csql.getObjectById(object_id, fid);
        
        assertTrue("Check that userA has an alarm set in the cdao", testobject.containsAlarm());
        assertEquals("Check that we got a 15", 15, testobject.getAlarm());          
                
        CalendarDataObject testobject2 = csql2.getObjectById(object_id, fid2);
        assertTrue("Check that userB has no alarm set in the cdao", !testobject2.containsAlarm());
        
        SearchIterator si = csql.getModifiedAppointmentsInFolder(fid, cols, cdao.getLastModified());
        boolean found = false;
        while (si.hasNext()) {
            CalendarDataObject tdao = (CalendarDataObject)si.next();
            if (tdao.getTitle().equals("testBug7883")) {
                found = true;
                assertTrue("Check that userA has an alarm set in the cdao", tdao.containsAlarm());
                assertEquals("Check that we got a 15", 15, tdao.getAlarm());                 
            }
        }
        assertTrue("Found our object (userA)", found);
        
        SearchIterator si2 = csql2.getModifiedAppointmentsInFolder(fid2, cols, cdao.getLastModified());
        found = false;
        while (si2.hasNext()) {
            CalendarDataObject tdao = (CalendarDataObject)si2.next();
            if (tdao.getTitle().equals("testBug7883")) {
                found = true;
                assertTrue("Check that userB has no alarm set in the cdao", !tdao.containsAlarm());             
            }
        }
        assertTrue("Found our object (userB)", found);        
        
        CalendarDataObject cdao_update = new CalendarDataObject();
        cdao_update.setContext(so.getContext());
        cdao_update.setTitle("testBug7883-update");
        cdao_update.setObjectID(object_id);
        userA = new UserParticipant();
        userA.setIdentifier(userid);
        cdao_update.setAlarm(-1);
        cdao_update.setUsers(new UserParticipant[] { userA, userB });        
        cdao_update.setIgnoreConflicts(true);
        
        csql.updateAppointmentObject(cdao_update, fid, cdao.getLastModified());
        
        testobject = csql.getObjectById(object_id, fid);
        
        assertTrue("Check that userA has no alarm set in the cdao", !testobject.containsAlarm());           
                
        testobject2 = csql2.getObjectById(object_id, fid2);
        assertTrue("Check that userB has no alarm set in the cdao", !testobject2.containsAlarm()); 
        
        si = csql.getModifiedAppointmentsInFolder(fid, cols, cdao.getLastModified());
        found = false;
        while (si.hasNext()) {
            CalendarDataObject tdao = (CalendarDataObject)si.next();
            if (tdao.getTitle().equals("testBug7883-update")) {
                found = true;
                assertTrue("Check that userA has no alarm set in the cdao", !tdao.containsAlarm());
                 
            }
        }
        assertTrue("Found our object (userA)", found);         
        
        si2 = csql2.getModifiedAppointmentsInFolder(fid2, cols, cdao.getLastModified());
        found = false;
        while (si2.hasNext()) {
            CalendarDataObject tdao = (CalendarDataObject)si2.next();
            if (tdao.getTitle().equals("testBug7883-update")) {
                found = true;
                assertTrue("Check that userB has no alarm set in the cdao", !tdao.containsAlarm());             
            }
        }
        assertTrue("Found our object (userB)", found);               
        
        
    }
    
    /*
     1) Create an appointment from 2-4pm
     2) Add a resource and save
     3) Create an appointment from 1-2pm and save
     4) Edit the appointment and add resource

     --> Conflict is reported although the appointments do not overlap.
     */    
    public void testBug7646() throws Throwable {
        
        deleteAllAppointments();
        
        Context context = new ContextImpl(contextid);
        
        int fid = getPrivateFolder(userid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        CalendarSql csql = new CalendarSql(so);
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(so.getContext());
        cdao.setTitle("testBug7646 - #1");
        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        cdao.setStartDate(new Date(cdao.getStartDate().getTime()+ 3600000));
        cdao.setEndDate(new Date(cdao.getEndDate().getTime()+ 7200000));        
        Participants p = new Participants();
        Participant resource = new ResourceParticipant();
        resource.setIdentifier(100);
        p.add(resource);
        cdao.setParticipants(p.getList());        
        cdao.setIgnoreConflicts(true);
        csql.insertAppointmentObject(cdao);
        int object_id = cdao.getObjectID();                       
        
        assertTrue("Got an object id", object_id > 0);
        
        CalendarDataObject cdao_conflict_test = new CalendarDataObject();
        cdao_conflict_test.setContext(so.getContext());
        cdao_conflict_test.setTitle("testBug7646 - #2");
        cdao_conflict_test.setParentFolderID(fid);        
        CalendarTest.fillDatesInDao(cdao_conflict_test);
        cdao_conflict_test.setIgnoreConflicts(false);       
        
        CalendarDataObject conflicts[] = csql.insertAppointmentObject(cdao_conflict_test);
        int object_id2 = cdao_conflict_test.getObjectID();        

        assertTrue("Got an object id", object_id2 > 0);
        
        boolean found_object_1 = false;
        if (conflicts != null) {
            for (int a = 0; a < conflicts.length; a++) {
                if (conflicts[a].getObjectID() == object_id) {
                    found_object_1 = true;
                }
            }
        }
        assertTrue("Conflicted with object #1!", !found_object_1);
        
        CalendarDataObject testobject = csql.getObjectById(object_id2, fid);      
        testobject.removeStartDate();
        testobject.removeEndDate();
        testobject.setParticipants(p.getList());   
        testobject.setIgnoreConflicts(false);
        conflicts = csql.updateAppointmentObject(testobject, fid, cdao_conflict_test.getLastModified());
        
        
        found_object_1 = false;
        if (conflicts != null) {
            for (int a = 0; a < conflicts.length; a++) {
                if (conflicts[a].getObjectID() == object_id) {
                    found_object_1 = true;
                }
            }
        }
        assertTrue("Conflicted with object #1!", !found_object_1);
     
        csql.deleteAppointmentObject(cdao, fid, cdao.getLastModified());
        csql.deleteAppointmentObject(testobject, fid, testobject.getLastModified());
    }
    
    /*
     Steps to reproduce (informal):
     1. Create a recurring yearly appointment "on third tuesday in april"
     2. Verify the appointment series

     Expected results:
     - Step 2: The appointment occurs every year on the third tuesday in april

     Result was:
     The appointment occurs:
     - 2008-04-15 (correct)
     - 2009-04-14 (wrong, should be 2009-04-21)
     - 2010-04-13 (wrong, should be 2010-04-20)
     - ...
     */
    public void testBug7064() throws Throwable {
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        
        int fid = CalendarTest.getCalendarDefaultFolderForUser(userid, context);
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(context);
        cdao.setTimezone(TIMEZONE);
        cdao.setParentFolderID(fid);
        cdao.setTitle("testBug7064");
        
        long startdate = 1175421600000L; // 01.04.2007 12:00
        long enddate = 1175425200000L; // 01.04.2007 13:00
        long until = 1272664800000L; // 01.05.2010 00:00
        
        long testmatrix[] = { 1176804000000L, 1208253600000L, 1240308000000L, 1271757600000L };
        
        cdao.setStartDate(new Date(startdate));
        cdao.setEndDate(new Date(enddate));
        
        cdao.setRecurrenceType(CalendarDataObject.YEARLY);
        
        cdao.setDays(CalendarDataObject.TUESDAY);
        cdao.setDayInMonth(3);
        cdao.setMonth(Calendar.APRIL);
        cdao.setInterval(1);
        
        cdao.setOccurrence(testmatrix.length);
        
        CalendarRecurringCollection.fillDAO(cdao);
        
        RecurringResults rrs = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Check result size", testmatrix.length, rrs.size());
        for (int a = 0; a < rrs.size(); a++) {
            RecurringResult rs = rrs.getRecurringResult(a);            
            assertEquals("Check start time ("+a+")", new Date(testmatrix[a]), new Date(rs.getStart()));
        }    
    }
    
    
    /*
    Create the following appointment in your private calendar.
    From 20.08.2007 Till 31.08.2007 
    Wholeday



    Steps to Reproduce:
    1. Login
    2. On start page move to August on mini calendar
    3. Verify that the days 20,21,22...31 are marked bold (correct)
    4. Go to 'September' on minicalendar
    5. Verify bold days

    Actual Results:
    The 1,23,4,5,6,7 are also marked bold.!! But there are no appointments
     */
    public void testBug8290() throws Throwable {         
        deleteAllAppointments();
        
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        
        int fid = CalendarTest.getCalendarDefaultFolderForUser(userid, context);
        
        long teststarttime = 1188165600000L; // 27.08.2007 00:00
        long testendtime = 1191103200000L; // 30.09.2007 00:00
        
        Date teststartdate = new Date(teststarttime);
        Date testenddate = new Date(testendtime);
        
        CalendarSql csql = new CalendarSql(so);
        boolean testarray[] = csql.hasAppointmentsBetween(teststartdate, testenddate);
        
        boolean check_all_false = true;
        for (int a = 0; a < testarray.length; a++) {
            if (testarray[a]) {
                check_all_false = false;
            }
        }
        
        assertTrue("Got results, but this test works only if no results are given at start time!", check_all_false);
        
        long starttime = 1187560800000L; //  20.08.2007 00:00
        long endtime = 1188511200000L; // 31.08.2007 00:00
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(context);
        cdao.setTimezone(TIMEZONE);
        cdao.setParentFolderID(fid);
        cdao.setStartDate(new Date(starttime));
        cdao.setEndDate(new Date(endtime));
        cdao.setFullTime(true);
        cdao.setTitle("testBug8290");
        cdao.setIgnoreConflicts(true);
        
        csql.insertAppointmentObject(cdao);
        
        testarray = csql.hasAppointmentsBetween(teststartdate, testenddate);
        boolean checkarray[] = { true, true, true, true, false, false, false, false, false, false } ;
        
        for (int a = 0; a < checkarray.length; a++) {
            assertEquals("Check array position "+a+" failed!", checkarray[a], testarray[a]);
        }
                
    }
    
    
    /*
     Steps to reproduce:
     1. Go to the Calendar module
     2. Click 'View' -> 'Calendar' in the panel
     3. Click 'Time range' -> 'Month' in the panel
     4. Navigate to the month 06-2007
     5. Double LMB on the calendar cell for 2007-06-01
     6. Enter a "3x mondays" as subject for the new appointment
     7. LMB the 'Series' button
     8. Click the 'Weekly' radio button
     9. Check the checkbox next to 'Monday'
     10. Click the 'after _ appointments' radio buttons and change the value to "3"
     11. Save the appointment
     12. Verify the appoinmtent in the calendar

     Expected Results:
     - Step 12: The appointment occurs on 2007-06-04, 2007-06-11 and 2007-06-18

     Result was:
     - Step 12: The appointment occurs on 2007-06-04 and 2007-06-11 (the last
     occurence is missing)
    */
    public void testBug7134() throws Throwable {         
        deleteAllAppointments();
        
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        
        int fid = CalendarTest.getCalendarDefaultFolderForUser(userid, context);
        
        long starttime = 1180692000000L; // 01.06.2007 12:00
        long endtime = 1180695600000L; //  01.06.2007 13:00 
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(context);
        cdao.setTimezone(TIMEZONE);
        cdao.setParentFolderID(fid);
        cdao.setStartDate(new Date(starttime));
        cdao.setEndDate(new Date(endtime));
        cdao.setFullTime(true);
        cdao.setTitle("testBug7134");
        cdao.setIgnoreConflicts(true);
        cdao.setRecurrenceType(CalendarDataObject.WEEKLY);
        cdao.setInterval(1);
        cdao.setDays(AppointmentObject.MONDAY);
        cdao.setOccurrence(3);
        
        CalendarRecurringCollection.fillDAO(cdao);
        RecurringResults rrs = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Check that we got 3 results", 3, rrs.size());

                
    }
 
    /*
     Steps to Reproduce:
     1. Login
     2. Select 'Calendar' from OX-Module
     3. From panel press 'New'
     4. Create a whole-day appointment for current day 
     5. Press 'New' from panel again to create a second appointment
     6. Select '00:30' for start and '01:00' for end time and save for current day
     */
    public void testBug8317() throws Throwable {
        deleteAllAppointments();
        
        Context context = new ContextImpl(contextid);
        SessionObject so = SessionObjectWrapper.createSessionObject(userid, context.getContextId(), "myTestIdentifier");
        
        int fid = CalendarTest.getCalendarDefaultFolderForUser(userid, context);
        
        CalendarSql csql = new CalendarSql(so);
        
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(context);
        cdao.setParentFolderID(fid);
        cdao.setTimezone(TIMEZONE);
        CalendarTest.fillDatesInDao(cdao);
        cdao.setStartDate(new Date(cdao.getStartDate().getTime()+CalendarRecurringCollection.MILLI_DAY));
        cdao.setEndDate(new Date(cdao.getEndDate().getTime()+CalendarRecurringCollection.MILLI_DAY));
        cdao.setFullTime(true);
        cdao.setTitle("testBug8317");
        cdao.setIgnoreConflicts(true);
        csql.insertAppointmentObject(cdao);
        
        int object_id = cdao.getObjectID();
        assertTrue("Got an object id", object_id > 0);        
        
        CalendarDataObject cdao2 = new CalendarDataObject();
        cdao2.setContext(context);
        cdao2.setParentFolderID(fid);
        cdao2.setTimezone(TIMEZONE);
        CalendarTest.fillDatesInDao(cdao2);
        long starttime = cdao2.getStartDate().getTime();
        long mod = starttime%CalendarRecurringCollection.MILLI_DAY;
        starttime -= mod;
        starttime -= 3600000*2; // UTC shift !?
        starttime = starttime + 1800000;
        long endtime = starttime + 1800000;
        starttime +=CalendarRecurringCollection.MILLI_DAY;
        endtime +=CalendarRecurringCollection.MILLI_DAY;
        Date startdate = new Date(starttime);
        Date enddate = new Date(endtime);
        cdao2.setStartDate(startdate);
        cdao2.setEndDate(enddate);
        cdao2.setTitle("testBug8317 - 2");
        CalendarDataObject conflicts[] = csql.insertAppointmentObject(cdao2);
        
        assertTrue("Got no conflicts", conflicts != null && conflicts.length > 0);
        
        boolean found_object_1 = false;
        if (conflicts != null) {
            for (int a = 0; a < conflicts.length; a++) {
                if (conflicts[a].getObjectID() == object_id) {
                    found_object_1 = true;
                }
            }
        }
        assertTrue("No conflict with object #1!", found_object_1);
        
    }
    
}