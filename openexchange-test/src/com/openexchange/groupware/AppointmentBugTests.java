
package com.openexchange.groupware;


import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.calendar.CalendarOperation;
import com.openexchange.groupware.container.ResourceParticipant;
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


public class AppointmentBugTests extends TestCase {
     
    int cols[] = new int[] { AppointmentObject.TITLE, AppointmentObject.RECURRENCE_ID, AppointmentObject.RECURRENCE_POSITION, AppointmentObject.OBJECT_ID, AppointmentObject.FOLDER_ID, AppointmentObject.USERS };
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
        privatefolder = new Integer(OXFolderTools.getCalendarDefaultFolder(userid, context, readcon)).intValue();
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
        RecurringResults m = null;
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setTimezone(TIMEZONE);
        CalendarTest.fillDatesInDao(cdao);
        cdao.removeUntil();
        cdao.setOccurrence(10);
        cdao.setTitle("testBug4467");
        cdao.setRecurrenceID(1);
        cdao.setRecurrenceType(CalendarObject.WEEKLY);
        cdao.setInterval(1);        
        cdao.setDays(AppointmentObject.MONDAY + AppointmentObject.FRIDAY);
        CalendarRecurringCollection.fillDAO(cdao);
        m = CalendarRecurringCollection.calculateRecurring(cdao, 0, 0, 0);
        assertEquals("Check calculation", 10, m.size());

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
        cdao.setFullTime(true);
        cdao.setRecurrenceID(1);
        cdao.setRecurrenceType(CalendarObject.DAILY);
        cdao.setInterval(2);        
        RecurringResults rss = null;
        CalendarOperation co = new CalendarOperation();
        co.prepareUpdateAction(cdao, userid, getPrivateFolder(userid), TIMEZONE);
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
     (wöchentlich). FYI: I did not change the defaults, so it was an the same day and
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
     Edit of an appointment in a shared folder failed although it has been previously
     created by the same user:
    */
    public void testBug4557() throws Throwable {
        // mjst be written
        fail();
    }    
    
}
