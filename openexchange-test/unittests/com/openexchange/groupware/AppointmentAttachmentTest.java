/*
 * AppointmentAttachmentTest.java
 *
 * Created on 9. September 2006, 12:36
 *
 */

package com.openexchange.groupware;

import static org.junit.Assert.assertTrue;
import java.sql.Connection;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.calendar.CalendarSql;
import com.openexchange.event.impl.EventConfigImpl;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPool;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;
import com.openexchange.tools.oxfolder.OXFolderTools;

/**
 *
 * @author bishoph
 */
public class AppointmentAttachmentTest {

    private int userid = 11;
    private Context context;
    private SessionObject session;

    @Before
    public void setUp() throws Exception {
        final EventConfigImpl event = new EventConfigImpl();
        event.setEventQueueEnabled(false);
        userid = CalendarTest.getUserId();
        context = CalendarTest.getContext();
        session = CalendarTest.getSession();
    }

    @Test
    public void testAttachAndDetachToAppointment() throws Exception {
        final CalendarDataObject cdao = new CalendarDataObject();
        cdao.setContext(context);
        cdao.setTitle("testAttachAndDetachToAppointment");

        final Connection readcon = DBPool.pickup(context);
        final int fid = OXFolderTools.getCalendarDefaultFolder(userid, cdao.getContext(), readcon);
        DBPool.push(context, readcon);

        cdao.setParentFolderID(fid);
        CalendarTest.fillDatesInDao(cdao);
        final SessionObject sessionobject = SessionObjectWrapper.createSessionObject(userid, context, "AttachmentTestId");
        final CalendarSql csql = new CalendarSql(sessionobject);
        cdao.setIgnoreConflicts(true);
        final CalendarDataObject conflicts[] = csql.insertAppointmentObject(cdao);
        final int oid = cdao.getObjectID();

        assertTrue("Got no object_id", oid != 0);
        assertTrue("Got no conflicts ", conflicts == null);

        csql.attachmentAction(fid, oid, userid, session, context, 1, null);
        csql.attachmentAction(fid, oid, userid, session, context, 1, null);
        csql.attachmentAction(fid, oid, userid, session, context, 1, null);
        csql.attachmentAction(fid, oid, userid, session, context, 1, null);

        csql.attachmentAction(fid, oid, userid, session, context, -1, null);
        csql.attachmentAction(fid, oid, userid, session, context, -1, null);
        csql.attachmentAction(fid, oid, userid, session, context, -1, null);
        csql.attachmentAction(fid, oid, userid, session, context, -1, null);

        long last_modified = 0;
        try {
            final long check_modified = System.currentTimeMillis();
            last_modified = csql.attachmentAction(fid, oid, userid, session, context, -1, null);
            assertTrue("Check for last_modified ", last_modified > check_modified);
        } catch (final Exception e) {
            return;
        }
        throw new Exception("Test failed because detach should not be possible!");
    }

}
