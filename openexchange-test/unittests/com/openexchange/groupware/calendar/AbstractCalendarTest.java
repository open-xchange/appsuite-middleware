
package com.openexchange.groupware.calendar;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.api2.OXException;
import com.openexchange.group.Group;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.calendar.tools.CalendarContextToolkit;
import com.openexchange.groupware.calendar.tools.CalendarFolderToolkit;
import com.openexchange.groupware.calendar.tools.CalendarTestConfig;
import com.openexchange.groupware.calendar.tools.CommonAppointments;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.tools.events.TestEventAdmin;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.session.ServerSession;
import junit.framework.TestCase;

public abstract class AbstractCalendarTest extends TestCase {

    protected final List<CalendarDataObject> clean = new ArrayList<CalendarDataObject>();
    protected final List<FolderObject> cleanFolders = new ArrayList<FolderObject>();
    protected String participant1;
    protected String participant2;
    protected String participant3;
    protected String resource1;
    protected String resource2;
    protected String resource3;
    protected String group;
    protected String member;
    protected String user;
    protected String secondUser;
    protected String thirdUser;
    protected int userId;
    protected int secondUserId;
    protected int thirdUserId;
    protected Context ctx;
    protected CommonAppointments appointments;
    protected CalendarFolderToolkit folders;
    protected Session session;
    protected Session session2;
    protected Session session3;
    protected static final int[] ACTION_ALL_FIELDS = {
        	CalendarObject.OBJECT_ID,
    		CalendarObject.CREATED_BY,
    		CalendarObject.CREATION_DATE,
    		CalendarObject.LAST_MODIFIED,
    		CalendarObject.MODIFIED_BY,
    		CalendarObject.FOLDER_ID,
    		CalendarObject.PRIVATE_FLAG,
    		CalendarObject.CATEGORIES,
    		CalendarObject.TITLE,
    		AppointmentObject.LOCATION,
    		CalendarObject.START_DATE,
    		CalendarObject.END_DATE,
    		CalendarObject.NOTE,
    		CalendarObject.RECURRENCE_TYPE,
    		CalendarObject.RECURRENCE_CALCULATOR,
    		CalendarObject.RECURRENCE_ID,
    		CalendarObject.RECURRENCE_POSITION,
    		CalendarObject.PARTICIPANTS,
    		CalendarObject.USERS,
    		AppointmentObject.SHOWN_AS,
    		AppointmentObject.DELETE_EXCEPTIONS,
    		AppointmentObject.CHANGE_EXCEPTIONS,
    		AppointmentObject.FULL_TIME,
    		AppointmentObject.COLOR_LABEL,
    		CalendarDataObject.TIMEZONE
    	};

    protected static Date applyTimeZone2Date(final long utcTime, final TimeZone timeZone) {
    	return new Date(utcTime - timeZone.getOffset(utcTime));
    }

    @Override
    public void setUp() throws Exception {
        Init.startServer();
    
        TestEventAdmin.getInstance().clearEvents();
    
        final CalendarTestConfig config = new CalendarTestConfig();
    
        user = config.getUser();
        secondUser = config.getSecondUser();
        thirdUser = config.getThirdUser();
    
        final CalendarContextToolkit tools = new CalendarContextToolkit();
        ctx = tools.getDefaultContext();
    
        appointments = new CommonAppointments(ctx, user);
    
        participant1 = config.getParticipant1();
        participant2 = config.getParticipant2();
        participant3 = config.getParticipant3();
    
        resource1 = config.getResource1();
        resource2 = config.getResource2();
        resource3 = config.getResource3();
    
        folders = new CalendarFolderToolkit();
    
        group = config.getGroup();
        final int groupid = tools.resolveGroup(group, ctx);
        final Group group = tools.loadGroup(groupid, ctx);
        final int memberid = group.getMember()[0];
        member = tools.loadUser(memberid, ctx).getLoginInfo();
    
        userId = tools.resolveUser(user, ctx);
        secondUserId = tools.resolveUser(secondUser, ctx);
        thirdUserId = tools.resolveUser(thirdUser, ctx);
    
        appointments.deleteAll(ctx);
    
        session = tools.getSessionForUser(user, ctx);
        session2 = tools.getSessionForUser(secondUser, ctx);
        session3 = tools.getSessionForUser(thirdUser, ctx);
    }

    @Override
    public void tearDown() throws OXException, SQLException {
        appointments.removeAll(user, clean);
        folders.removeAll(session, cleanFolders);
        Init.stopServer();
    }

    protected JSONObject json(final Object...objects) throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        for(int i = 0; i < objects.length; i++) {
            jsonObject.put(objects[i++].toString(), objects[i]);
        }
        return jsonObject;
    }

    protected void assertContains(final SearchIterator iter, final CalendarDataObject cdao) throws OXException, SearchIteratorException {
        boolean found = false;
        while(iter.hasNext()) {
            final CalendarDataObject cdao2 = (CalendarDataObject)iter.next();
            found = found || cdao.getObjectID() == cdao2.getObjectID();
        }
        assertTrue(found);
    }

    protected void assertContains(final JSONArray arr, final CalendarDataObject cdao) throws JSONException {
        for(int i = 0, size = arr.length(); i < size; i++) {
            final JSONArray row = arr.getJSONArray(i);
            if(row.getInt(0) == cdao.getObjectID()) {
                return;
            }
        }
        fail("Could not find appointment in respone: "+arr);
    }

    protected void assertContainsAsJSONObject(final JSONArray arr, final CalendarDataObject cdao) throws JSONException {
        for(int i = 0, size = arr.length(); i < size; i++) {
            final JSONObject row = arr.getJSONObject(i);
            if(row.getInt("id") == cdao.getObjectID()) {
                return;
            }
        }
        fail("Could not find appointment in respone: "+arr);
    }

    protected static int convertCalendarDAY_OF_WEEK2CalendarDataObjectDAY_OF_WEEK(final int calendarDAY_OF_WEEK) {
    	switch (calendarDAY_OF_WEEK) {
    	case Calendar.SUNDAY:
    		return CalendarDataObject.SUNDAY;
    	case Calendar.MONDAY:
    		return CalendarDataObject.MONDAY;
    	case Calendar.TUESDAY:
    		return CalendarDataObject.TUESDAY;
    	case Calendar.WEDNESDAY:
    		return CalendarDataObject.WEDNESDAY;
    	case Calendar.THURSDAY:
    		return CalendarDataObject.THURSDAY;
    	case Calendar.FRIDAY:
    		return CalendarDataObject.FRIDAY;
    	case Calendar.SATURDAY:
    		return CalendarDataObject.SATURDAY;
    	default:
    		return -1;
    	}
    }

    protected static interface Verifyer {
            public void verify(TestCalendarListener listener);
        }

    protected static final class TestCalendarListener extends AbstractCalendarListener {
            private String called;
            List<Object> args = new ArrayList<Object>();
            private Verifyer verifyer;
    
            @Override
    		public void createdChangeExceptionInRecurringAppointment(final CalendarDataObject master, final CalendarDataObject changeException,int inFolder, final ServerSession session) {
                this.called = "createdChangeExceptionInRecurringAppointment";
                this.args.add(master);
                this.args.add(changeException);
                this.args.add(session);
                verifyer.verify(this);
            }
    
            public void clear() {
                called = null;
                args.clear();
            }
    
            public String getCalledMethodName() {
                return called;
            }
    
            public List<Object> getArgs() {
                return args;
            }
    
            public boolean wasCalled() {
                return called != null;
            }
    
            public Object getArg(final int i) {
                return args.get(i);
            }
    
            public Verifyer getVerifyer() {
                return verifyer;
            }
    
            public void setVerifyer(final Verifyer verifyer) {
                this.verifyer = verifyer;
            }
        }

    protected List<CalendarDataObject> read(final SearchIterator<CalendarDataObject> si) throws OXException, SearchIteratorException {
        final List<CalendarDataObject> appointments = new ArrayList<CalendarDataObject>();
        while(si.hasNext()) { appointments.add( si.next() ); }
        return appointments;
    }

    public AbstractCalendarTest() {
        super();
    }

    public AbstractCalendarTest(String name) {
        super(name);
    }

}
