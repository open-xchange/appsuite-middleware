/*
 * AppointmentAttachmentTest.java
 *
 * Created on 9. September 2006, 12:36
 *
 */

package com.openexchange.groupware;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.DBPool;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;
import com.openexchange.tools.OXFolderTools;
import java.sql.Connection;
import junit.framework.TestCase;

/**
 *
 * @author bishoph
 */
public class AppointmentAttachmentTest extends TestCase {
    
    private int userid = 0;
    private Context context;
    
    protected void setUp() throws Exception {        
        super.setUp();
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
        cdao.setParentFolderID(getPrivateFolder());
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

    private int getPrivateFolder() throws Exception {
        int privatefolder = 0;
        Context context = CalendarTest.getContext();
        Connection readcon = DBPool.pickupWriteable(context);
        privatefolder = new Integer(OXFolderTools.getCalendarStandardFolder(userid, context, readcon)).intValue();
        DBPool.pushWrite(context, readcon);
        return privatefolder;        
    }

    
}
