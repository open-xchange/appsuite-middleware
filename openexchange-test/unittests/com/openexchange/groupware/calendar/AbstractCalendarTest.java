
package com.openexchange.groupware.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.calendar.tools.CommonAppointments;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.setuptools.TestFolderToolkit;
import com.openexchange.tools.events.TestEventAdmin;
import com.openexchange.tools.iterator.SearchIterator;
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
    protected String fourthUser;
    protected int userId;
    protected int secondUserId;
    protected int thirdUserId;
    protected int fourthUserId;
    protected int groupId;
    protected Context ctx;
    protected CommonAppointments appointments;
    protected TestFolderToolkit folders;
    protected Session session;
    protected Session session2;
    protected Session session3;
    protected Session session4;
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
    		Appointment.LOCATION,
    		CalendarObject.START_DATE,
    		CalendarObject.END_DATE,
    		CalendarObject.NOTE,
    		CalendarObject.RECURRENCE_TYPE,
    		CalendarObject.RECURRENCE_CALCULATOR,
    		CalendarObject.RECURRENCE_ID,
    		CalendarObject.RECURRENCE_POSITION,
    		CalendarObject.PARTICIPANTS,
    		CalendarObject.USERS,
    		Appointment.SHOWN_AS,
    		Appointment.DELETE_EXCEPTIONS,
    		Appointment.CHANGE_EXCEPTIONS,
    		Appointment.FULL_TIME,
    		Appointment.COLOR_LABEL,
    		CalendarDataObject.TIMEZONE
    	};

    protected static Date applyTimeZone2Date(final long utcTime, final TimeZone timeZone) {
    	return new Date(utcTime - timeZone.getOffset(utcTime));
    }

    @Override
    public void setUp() throws Exception {
        Init.startServer();

        TestEventAdmin.getInstance().clearEvents();

        final TestConfig config = new TestConfig();

        user = config.getUser();
        secondUser = config.getSecondUser();
        thirdUser = config.getThirdUser();
        fourthUser = config.getFourthUser();

        final TestContextToolkit tools = new TestContextToolkit();
        final String ctxName = config.getContextName();
        ctx = null == ctxName || ctxName.trim().length() == 0 ? tools.getDefaultContext() : tools.getContextByName(ctxName);

        appointments = new CommonAppointments(ctx, user);

        participant1 = config.getParticipant1();
        participant2 = config.getParticipant2();
        participant3 = config.getParticipant3();

        resource1 = config.getResource1();
        resource2 = config.getResource2();
        resource3 = config.getResource3();

        folders = new TestFolderToolkit();

        group = config.getGroup();
        groupId = tools.resolveGroup(group, ctx);
        final Group group = tools.loadGroup(groupId, ctx);
        final int memberid = group.getMember()[0];
        member = tools.loadUser(memberid, ctx).getLoginInfo();

        userId = tools.resolveUser(user, ctx);
        secondUserId = tools.resolveUser(secondUser, ctx);
        thirdUserId = tools.resolveUser(thirdUser, ctx);
        fourthUserId = tools.resolveUser(fourthUser, ctx);

        appointments.deleteAll(ctx);

        session = tools.getSessionForUser(user, ctx);
        session2 = tools.getSessionForUser(secondUser, ctx);
        session3 = tools.getSessionForUser(thirdUser, ctx);
        session4 = tools.getSessionForUser(fourthUser, ctx);
    }

    @Override
    public void tearDown() throws Exception {
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

    protected void assertContains(final SearchIterator iter, final CalendarDataObject cdao) throws OXException {
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

    public static interface Verifyer {
            public void verify(TestCalendarListener listener);
        }

    public static final class TestCalendarListener implements CalendarListener {
            private String called;
            List<Object> args = new ArrayList<Object>();
            private Verifyer verifyer;

            @Override
            public void createdChangeExceptionInRecurringAppointment(final Appointment master, final Appointment changeException,final int inFolder, final ServerSession session) {
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

    protected List<Appointment> read(final SearchIterator<Appointment> si) throws OXException {
        final List<Appointment> appointments = new ArrayList<Appointment>();
        while(si.hasNext()) { appointments.add( si.next() ); }
        return appointments;
    }

    public AbstractCalendarTest() {
        super();
    }

    public AbstractCalendarTest(final String name) {
        super(name);
    }

    protected CalendarCollection getTools() {
        return new CalendarCollection();
    }

}
