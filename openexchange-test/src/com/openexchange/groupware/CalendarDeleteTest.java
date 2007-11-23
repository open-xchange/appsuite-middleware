package com.openexchange.groupware;

import com.openexchange.groupware.calendar.CalendarAdministration;
import com.openexchange.groupware.delete.DeleteEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
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
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderManagerImpl;
import com.openexchange.tools.oxfolder.OXFolderTools;


public class CalendarDeleteTest extends TestCase {
    
    private static final int contextid = 1;
    private static int userid = 11; 
    private static int groupid = 62;
    private static int resourceid = 2;
    private static int deleteuserid = 11;
    private Context context;
    
    private static boolean init = false;
    
    protected void setUp() throws Exception {        
        super.setUp();
        EventConfigImpl event = new EventConfigImpl();
        event.setEventQueueEnabled(false);
        context = new ContextImpl(contextid);
        userid = CalendarTest.getUserId();
        String user2 = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "user_participant3", "");                
        deleteuserid = resolveUser(user2);        
        
        try {
            CalendarTest ct = new CalendarTest();
            ct.dontDelete();
            ct.testWholeDayWithDB();
            ct.testMultiSpanWholeDay();
            ct.testInsertAndLabel();
            ct.testNoAlarm();
            ct.testInsertAndAlarm();
            ct.testInsertMoveAndDeleteAppointments();
            ct.testConfirmation();
            ct.testInsertUpdateAlarm();
            ct.testAlarmAndUpdate();
            
            AppointmentBugTests abt = new AppointmentBugTests();
            abt.testBug4467();
            abt.testBug4497();
            abt.testBug4276();
            abt.testBug4766();
            abt.testBug5010();
            abt.testBug5012();
            abt.testBug5144();
            
        } catch (Throwable ex) {
            throw new Exception(ex.getMessage(), ex);
        }
        
    }
    
    private Properties getAJAXProperties() {
        Properties properties = Init.getAJAXProperties();
        return properties;
    }      
    
    private int resolveUser(String user) throws Exception {
        UserStorage uStorage = UserStorage.getInstance();
        return uStorage.getUserId(user, context);
    }    
    
    protected void tearDown() throws Exception {
        if (init) {
            init = false;
            Init.stopServer();
        }
        super.tearDown();
    }
    
    public void testDeleteUserData() throws Throwable {
        Connection readcon = DBPool.pickup(context);
        Connection writecon = DBPool.pickupWriteable(context);
        SessionObject so = SessionObjectWrapper.createSessionObject(deleteuserid, context.getContextId(), "deleteAllUserApps");
        
        DeleteEvent delEvent = new DeleteEvent(this, so.getUserObject().getId(), DeleteEvent.TYPE_USER, so.getContext());

        CalendarAdministration ca = new CalendarAdministration();
        ca.deletePerformed(delEvent, readcon, writecon);
        
        Statement stmt = readcon.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT pd.intfield01 from prg_dates pd, prg_dates_members pdm WHERE pd.intfield01 = pdm.object_id AND pdm.member_uid = "+deleteuserid+" AND pd.cid = "+contextid);
        assertTrue("Test that no appointment exists for user "+deleteuserid, rs.next() != true);
        rs.close();

        ResultSet rs2 = stmt.executeQuery("SELECT pd.intfield01 from prg_dates pd WHERE pd.created_from = "+deleteuserid+" AND pd.cid = "+contextid);
        assertTrue("Test that no cerated_from exists for user "+deleteuserid, rs2.next() != true);
        rs2.close();

        ResultSet rs3 = stmt.executeQuery("SELECT pd.intfield01 from prg_dates pd WHERE pd.changed_from = "+deleteuserid+" AND pd.cid = "+contextid);
        assertTrue("Test that no changed_from exists for user "+deleteuserid, rs3.next() != true);
        rs3.close();
        
        ResultSet rs4 = stmt.executeQuery("SELECT pd.intfield01 from prg_dates pd, prg_date_rights pdr WHERE pd.intfield01 = pdr.object_id AND pdr.id = "+deleteuserid+" AND pdr.type = "+Participant.USER+" AND pd.cid = "+contextid);
        assertTrue("Test that no user_right entry exists for user "+deleteuserid, rs4.next() != true);
        rs4.close();
        
        stmt.close();
        
        DBPool.push(context, readcon);
        DBPool.pushWrite(context, writecon);
    }

     public void testDeleteGroup() throws Throwable {
        Connection readcon = DBPool.pickup(context);
        Connection writecon = DBPool.pickupWriteable(context);
        SessionObject so = SessionObjectWrapper.createSessionObject(deleteuserid, context.getContextId(), "deleteAllUserApps");
        
        DeleteEvent delEvent = new DeleteEvent(this, groupid, DeleteEvent.TYPE_GROUP, so.getContext());

        CalendarAdministration ca = new CalendarAdministration();
        ca.deletePerformed(delEvent, readcon, writecon);
        
        Statement stmt = readcon.createStatement();        
        ResultSet rs = stmt.executeQuery("SELECT pd.intfield01 from prg_dates pd, prg_date_rights pdr WHERE pd.intfield01 = pdr.object_id AND pdr.id = "+groupid+" AND pdr.type = "+Participant.GROUP+" AND pd.cid = "+contextid);
        assertTrue("Test that no user_right entry exists for group "+deleteuserid, rs.next() != true);
        rs.close();
        
        stmt.close();
        
        DBPool.push(context, readcon);
        DBPool.pushWrite(context, writecon);
    }

    public void testDeleteResource() throws Throwable {
        Connection readcon = DBPool.pickup(context);
        Connection writecon = DBPool.pickupWriteable(context);
        SessionObject so = SessionObjectWrapper.createSessionObject(deleteuserid, context.getContextId(), "deleteAllUserApps");
        
        DeleteEvent delEvent = new DeleteEvent(this, resourceid, DeleteEvent.TYPE_RESOURCE, so.getContext());

        CalendarAdministration ca = new CalendarAdministration();
        ca.deletePerformed(delEvent, readcon, writecon);
        
        Statement stmt = readcon.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT pd.intfield01 from prg_dates pd, prg_date_rights pdr WHERE pd.intfield01 = pdr.object_id AND pdr.id = "+resourceid+" AND pdr.type = "+Participant.RESOURCE+" AND pd.cid = "+contextid);
        assertTrue("Test that no user_right entry exists for resource "+deleteuserid, rs.next() != true);
        rs.close();
        
        stmt.close();
        
        DBPool.push(context, readcon);
        DBPool.pushWrite(context, writecon);
    }     
    
    
}
