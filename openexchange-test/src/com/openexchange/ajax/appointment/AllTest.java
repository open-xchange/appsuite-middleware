
package com.openexchange.ajax.appointment;

import static com.openexchange.groupware.calendar.TimeTools.D;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.java.util.TimeZones;

public class AllTest extends AppointmentTest {

    private static final Log LOG = LogFactory.getLog(AllTest.class);

    private static final int[] SIMPLE_COLUMNS = new int[] {
        Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.TITLE, Appointment.START_DATE, Appointment.END_DATE };

    public AllTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        clean();
    }

    public void testShouldListAppointmentsInPrivateFolder() throws Exception {
        final Appointment appointment = new Appointment();
        appointment.setStartDate(D("24/02/1998 12:00"));
        appointment.setEndDate(D("24/02/1998 14:00"));
        appointment.setTitle("Appointment 1 for All Test");
        appointment.setParentFolderID(appointmentFolderId);
        create(appointment);

        final Appointment anotherAppointment = new Appointment();
        anotherAppointment.setStartDate(D("03/05/1999 10:00"));
        anotherAppointment.setEndDate(D("03/05/1999 10:30"));
        anotherAppointment.setTitle("Appointment 2 for All Test");
        anotherAppointment.setParentFolderID(appointmentFolderId);
        create(anotherAppointment);

        AllRequest all = new AllRequest(appointmentFolderId, SIMPLE_COLUMNS, D("01/01/1990 00:00"), D("01/01/2000 00:00"), TimeZones.UTC);
        CommonAllResponse allResponse = getClient().execute(all);

        // Verify appointments are included in response
        final JSONArray data = (JSONArray) allResponse.getData();
        assertInResponse(data, appointment, anotherAppointment);

        all = new AllRequest(appointmentFolderId, SIMPLE_COLUMNS, D("01/01/1990 00:00"), D("01/01/2000 00:00"), TimeZones.UTC);
        final TimeZone respTimeZone = TimeZone.getTimeZone("GMT+08:00");
        all.setTimeZoneId(respTimeZone.getID());
        allResponse = getClient().execute(all);

        final JSONArray data2 = (JSONArray) allResponse.getData();
        for (int i = 0, size = data.length(); i < size; i++) {
            final JSONArray row = data.getJSONArray(i);

            final int id = row.getInt(0);
            final int folderId = row.getInt(1);
            final String title = row.getString(2);
            final long startDate = row.getLong(3);
            final long endDate = row.getLong(4);

            final JSONArray row2 = data2.getJSONArray(i);

            final int id2 = row2.getInt(0);
            final int folderId2 = row2.getInt(1);
            final String title2 = row2.getString(2);
            final long startDate2 = row2.getLong(3);
            final long endDate2 = row2.getLong(4);

            assertEquals("Unexpected ID.", id, id2);
            assertEquals("Unexpected folder ID.", folderId, folderId2);
            assertEquals("Unexpected title.", title, title2);

            final long userTZStartOffset = timeZone.getOffset(startDate);
            final long respTZStartOffset = respTimeZone.getOffset(startDate);
            assertEquals("Unexpected time zone is response", ((startDate2 - startDate)), (respTZStartOffset - userTZStartOffset));

            final long userTZEndOffset = timeZone.getOffset(endDate);
            final long respTZEndOffset = respTimeZone.getOffset(endDate2);
            assertEquals("Unexpected time zone is response", ((endDate2 - endDate)), (respTZEndOffset - userTZEndOffset));
        }
    }

    public void testShouldOnlyListAppointmentsInSpecifiedTimeRange() throws JSONException, OXException, IOException, SAXException {
        final Appointment appointment = new Appointment();
        appointment.setStartDate(D("24/02/1998 12:00"));
        appointment.setEndDate(D("24/02/1998 14:00"));
        appointment.setTitle("Appointment 1 for All Test");
        appointment.setParentFolderID(appointmentFolderId);
        create(appointment);

        final Appointment anotherAppointment = new Appointment();
        anotherAppointment.setStartDate(D("03/05/1999 10:00"));
        anotherAppointment.setEndDate(D("03/05/1999 10:30"));
        anotherAppointment.setTitle("Appointment 2 for All Test");
        anotherAppointment.setParentFolderID(appointmentFolderId);
        create(anotherAppointment);

        final AllRequest all = new AllRequest(
            appointmentFolderId,
            SIMPLE_COLUMNS,
            D("01/01/1999 00:00"),
            D("01/01/2000 00:00"),
            TimeZones.UTC);
        final CommonAllResponse allResponse = getClient().execute(all);

        // Verify appointments are included in response
        final JSONArray data = (JSONArray) allResponse.getData();

        assertNotInResponse(data, appointment);
        assertInResponse(data, anotherAppointment);

    }

    private void assertInResponse(final JSONArray data, final Appointment... appointments) throws JSONException {
        final Set<Integer> expectedIds = new HashSet<Integer>();
        final Map<Integer, Appointment> id2appointment = new HashMap<Integer, Appointment>();
        for (final Appointment appointment : appointments) {
            expectedIds.add(appointment.getObjectID());
            id2appointment.put(appointment.getObjectID(), appointment);
        }
        for (int i = 0, size = data.length(); i < size; i++) {
            final JSONArray row = data.getJSONArray(i);

            final int id = row.getInt(0);
            final int folderId = row.getInt(1);
            final String title = row.getString(2);
            final long startDate = row.getLong(3);
            final long endDate = row.getLong(4);

            final Appointment expectedAppointment = id2appointment.get(id);
            expectedIds.remove(id);

            if (expectedAppointment != null) {
                assertEquals(folderId, expectedAppointment.getParentFolderID());
                assertEquals(title, expectedAppointment.getTitle());
                assertEquals(startDate, expectedAppointment.getStartDate().getTime());
                assertEquals(endDate, expectedAppointment.getEndDate().getTime());
            }
        }

        assertTrue("Missing ids: " + expectedIds, expectedIds.isEmpty());
    }

    private void assertNotInResponse(final JSONArray data, final Appointment... appointments) throws JSONException {
        final Set<Integer> ids = new HashSet<Integer>();
        for (final Appointment appointment : appointments) {
            ids.add(appointment.getObjectID());
        }
        for (int i = 0, size = data.length(); i < size; i++) {
            final JSONArray row = data.getJSONArray(i);

            final int id = row.getInt(0);

            assertFalse(ids.contains(id));
        }
    }

    public void testShowAppointmentsBetween() throws Exception {
        final Date start = new Date(System.currentTimeMillis() - (dayInMillis * 7));
        final Date end = new Date(System.currentTimeMillis() + (dayInMillis * 7));

        final int cols[] = new int[] { Appointment.OBJECT_ID };

        listAppointment(
            getWebConversation(),
            appointmentFolderId,
            cols,
            start,
            end,
            timeZone,
            false,
            PROTOCOL + getHostName(),
            getSessionId());
    }

    public void testShowAllAppointmentWhereIAmParticipant() throws Exception {
        final Date start = new Date(System.currentTimeMillis() - (dayInMillis * 7));
        final Date end = new Date(System.currentTimeMillis() + (dayInMillis * 7));

        final int cols[] = new int[] { Appointment.OBJECT_ID };

        listAppointment(
            getWebConversation(),
            appointmentFolderId,
            cols,
            start,
            end,
            timeZone,
            true,
            PROTOCOL + getHostName(),
            getSessionId());
    }

    public void testShowFullTimeAppointments() throws Exception {
        final int cols[] = new int[] { Appointment.OBJECT_ID };

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(startTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        final Date startDate = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 1);

        final Date endDate = calendar.getTime();

        final Appointment appointmentObj = createAppointmentObject("testShowFullTimeAppointments");
        appointmentObj.setStartDate(startDate);
        appointmentObj.setEndDate(endDate);
        appointmentObj.setFullTime(true);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());

        calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(startTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date start = calendar.getTime();
        Date end = new Date(start.getTime() + dayInMillis);

        Appointment[] appointmentArray = listAppointment(
            getWebConversation(),
            appointmentFolderId,
            cols,
            start,
            end,
            timeZone,
            false,
            getHostName(),
            getSessionId());
        boolean found = false;
        for (int a = 0; a < appointmentArray.length; a++) {
            if (appointmentArray[a].getObjectID() == objectId) {
                found = true;
            }
        }
        assertTrue("appointment not found in day view", found);

        // one day less
        start = new Date(calendar.getTimeInMillis() - dayInMillis);
        end = new Date(start.getTime() + dayInMillis);
        appointmentArray = listAppointment(
            getWebConversation(),
            appointmentFolderId,
            cols,
            start,
            end,
            timeZone,
            false,
            getHostName(),
            getSessionId());
        found = false;
        for (int a = 0; a < appointmentArray.length; a++) {
            if (appointmentArray[a].getObjectID() == objectId) {
                found = true;
            }
        }
        assertFalse("appointment found one day before start date in day view", found);

        // one day more
        start = new Date(calendar.getTimeInMillis() + dayInMillis);
        end = new Date(start.getTime() + dayInMillis);
        appointmentArray = listAppointment(
            getWebConversation(),
            appointmentFolderId,
            cols,
            start,
            end,
            timeZone,
            false,
            getHostName(),
            getSessionId());
        found = false;
        for (int a = 0; a < appointmentArray.length; a++) {
            if (appointmentArray[a].getObjectID() == objectId) {
                found = true;
            }
        }
        assertFalse("appointment found one day after start date in day view", found);

        deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId(), false);
    }

    // Bug 12171
    public void testShowOcurrences() throws Exception {
        final int cols[] = new int[] { Appointment.OBJECT_ID, Appointment.RECURRENCE_COUNT };

        final Appointment appointmentObj = createAppointmentObject("testShowOcurrences");
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date(System.currentTimeMillis() + 60 * 60 * 1000));
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setOccurrence(3);
        appointmentObj.setIgnoreConflicts(true);
        int objectId = -1;
        try {
            objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());

            final Appointment[] appointmentArray = listAppointment(getWebConversation(), appointmentFolderId, cols, new Date(0), new Date(
                Long.MAX_VALUE), timeZone, false, getHostName(), getSessionId());

            for (final Appointment loaded : appointmentArray) {
                if (loaded.getObjectID() == objectId) {
                    assertEquals(appointmentObj.getOccurrence(), loaded.getOccurrence());
                }
            }
        } finally {
            if (objectId != -1) {
                deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId(), false);
            }
        }
    }

    // Node 2652
    public void testLastModifiedUTC() throws Exception {
        final AJAXClient client = new AJAXClient(new AJAXSession(getWebConversation(), getHostName(), getSessionId()), false);
        final int cols[] = new int[] { Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.LAST_MODIFIED_UTC };

        final Appointment appointmentObj = createAppointmentObject("testShowLastModifiedUTC");
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date(System.currentTimeMillis() + 60 * 60 * 1000));
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = insertAppointment(getWebConversation(), appointmentObj, timeZone, getHostName(), getSessionId());
        try {
            final AllRequest req = new AllRequest(
                appointmentFolderId,
                cols,
                new Date(0),
                new Date(Long.MAX_VALUE),
                TimeZone.getTimeZone("UTC"));

            final CommonAllResponse response = Executor.execute(client, req);
            final JSONArray arr = (JSONArray) response.getResponse().getData();

            assertNotNull(arr);
            final int size = arr.length();
            assertTrue(size > 0);
            for (int i = 0; i < size; i++) {
                final JSONArray objectData = arr.optJSONArray(i);
                assertNotNull(objectData);
                assertNotNull(objectData.opt(2));
            }
        } finally {
            deleteAppointment(getWebConversation(), objectId, appointmentFolderId, getHostName(), getSessionId(), false);
        }
    }
}
