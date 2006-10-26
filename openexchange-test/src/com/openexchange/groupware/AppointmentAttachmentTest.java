/*
 * AppointmentAttachmentTest.java
 *
 * Created on 9. September 2006, 12:36
 *
 */

package com.openexchange.groupware;

import junit.framework.TestCase;

import com.openexchange.event.EventConfigImpl;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;

/**
 *
 * @author bishoph
 */
public class AppointmentAttachmentTest extends TestCase {
    
    private int userid = 0;
    private Context context;
    
    protected void setUp() throws Exception {        
        super.setUp();
        EventConfigImpl event = new EventConfigImpl();
        event.setEventQueueEnabled(false);        
        userid = CalendarTest.getUserId();
        context = CalendarTest.getContext();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testAttachAndDetachToAppointment() throws Exception {
        CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(context);
        cdao.setTitle("Attachment Test");
        cdao.setParentFolderID(CalendarTest.getPrivateFolder());
        CalendarTest.fillDatesInDao(cdao);
        SessionObject sessionobject = SessionObjectWrapper.createSessionObject(userid, context, "AttachmentTestId");
        CalendarSql csql = new CalendarSql(sessionobject);
        csql.insertAppointmentObject(cdao);
        int oid = cdao.getObjectID();
        
        csql.attachmentAction(oid, userid, context, true);
        csql.attachmentAction(oid, userid, context, true);
        csql.attachmentAction(oid, userid, context, true);
        csql.attachmentAction(oid, userid, context, true);
        
        csql.attachmentAction(oid, userid, context, false);
        csql.attachmentAction(oid, userid, context, false);
        csql.attachmentAction(oid, userid, context, false);
        csql.attachmentAction(oid, userid, context, false);
        
        try {
            csql.attachmentAction(oid, userid, context, false);
        } catch(Exception e) {
            return; 
        }
        throw new Exception("Test failed because detach should not be possible!");
    }
    
}
