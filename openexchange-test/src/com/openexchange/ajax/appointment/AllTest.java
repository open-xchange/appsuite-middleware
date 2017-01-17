
package com.openexchange.ajax.appointment;

import static com.openexchange.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONArray;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.AllRequest;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.java.util.TimeZones;

public class AllTest extends AppointmentTest {

    private static final int[] SIMPLE_COLUMNS = new int[] { Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.TITLE, Appointment.START_DATE, Appointment.END_DATE };

    @Test
    public void testShouldListAppointmentsInPrivateFolder() throws Exception {
        catm.setTimezone(TimeZones.UTC);
        final Appointment appointment = new Appointment();
        appointment.setStartDate(D("24/02/1998 12:00"));
        appointment.setEndDate(D("24/02/1998 14:00"));
        appointment.setTitle("Appointment 1 for All Test");
        appointment.setParentFolderID(appointmentFolderId);
        catm.insert(appointment);

        final Appointment anotherAppointment = new Appointment();
        anotherAppointment.setStartDate(D("03/05/1999 10:00"));
        anotherAppointment.setEndDate(D("03/05/1999 10:30"));
        anotherAppointment.setTitle("Appointment 2 for All Test");
        anotherAppointment.setParentFolderID(appointmentFolderId);
        catm.insert(anotherAppointment);

        Appointment[] all = catm.all(appointmentFolderId, D("01/01/1990 00:00"), D("01/01/2000 00:00"), SIMPLE_COLUMNS);

        // Verify appointments are included in response
        assertInResponse(all, appointment, anotherAppointment);

        final TimeZone respTimeZone = TimeZone.getTimeZone("GMT+08:00");
        Appointment[] all2 = catm.all(appointmentFolderId, D("01/01/1990 00:00"), D("01/01/2000 00:00"), SIMPLE_COLUMNS, true, respTimeZone.getID());

        for (int i = 0, size = all.length; i < size; i++) {
            final Appointment app1 = all[i];

            final int id = app1.getObjectID();
            final int folderId = app1.getParentFolderID();
            final String title = app1.getTitle();
            final long startDate = app1.getStartDate().getTime();
            final long endDate = app1.getEndDate().getTime();

            final Appointment app2 = all2[i];

            final int id2 = app2.getObjectID();
            final int folderId2 = app2.getParentFolderID();
            final String title2 = app2.getTitle();
            final long startDate2 = app2.getStartDate().getTime();
            final long endDate2 = app2.getEndDate().getTime();

            assertEquals("Unexpected ID.", id, id2);
            assertEquals("Unexpected folder ID.", folderId, folderId2);
            assertEquals("Unexpected title.", title, title2);

            final long userTZStartOffset = TimeZones.UTC.getOffset(startDate);
            final long respTZStartOffset = respTimeZone.getOffset(startDate);
            assertEquals("Unexpected time zone is response", ((startDate2 - startDate)), (respTZStartOffset - userTZStartOffset));

            final long userTZEndOffset = TimeZones.UTC.getOffset(endDate);
            final long respTZEndOffset = respTimeZone.getOffset(endDate2);
            assertEquals("Unexpected time zone is response", ((endDate2 - endDate)), (respTZEndOffset - userTZEndOffset));
        }
    }

    @Test
    public void testShouldOnlyListAppointmentsInSpecifiedTimeRange() {
        catm.setTimezone(TimeZones.UTC);
        
        final Appointment appointment = new Appointment();
        appointment.setStartDate(D("24/02/1998 12:00"));
        appointment.setEndDate(D("24/02/1998 14:00"));
        appointment.setTitle("Appointment 1 for All Test");
        appointment.setParentFolderID(appointmentFolderId);
        catm.insert(appointment);

        final Appointment anotherAppointment = new Appointment();
        anotherAppointment.setStartDate(D("03/05/1999 10:00"));
        anotherAppointment.setEndDate(D("03/05/1999 10:30"));
        anotherAppointment.setTitle("Appointment 2 for All Test");
        anotherAppointment.setParentFolderID(appointmentFolderId);
        catm.insert(anotherAppointment);

        Appointment[] all = catm.all(appointmentFolderId, D("01/01/1999 00:00"), D("01/01/2000 00:00"), SIMPLE_COLUMNS);

        // Verify appointments are included in response
        final JSONArray data = (JSONArray) catm.getLastResponse().getData();

        assertNotInResponse(all, appointment);
        assertInResponse(all, anotherAppointment);
    }

    private void assertInResponse(final Appointment[] data, final Appointment... appointments) {
        final Set<Integer> expectedIds = new HashSet<Integer>();
        final Map<Integer, Appointment> id2appointment = new HashMap<Integer, Appointment>();
        for (final Appointment appointment : appointments) {
            expectedIds.add(appointment.getObjectID());
            id2appointment.put(appointment.getObjectID(), appointment);
        }
        for (Appointment appointment : data) {
            final int id = appointment.getObjectID();
            final int folderId = appointment.getParentFolderID();
            final String title = appointment.getTitle();
            final long startDate = appointment.getStartDate().getTime();
            final long endDate = appointment.getEndDate().getTime();

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

    private void assertNotInResponse(final Appointment[] data, final Appointment... appointments) {
        final Set<Integer> ids = new HashSet<Integer>();
        for (final Appointment appointment : appointments) {
            ids.add(appointment.getObjectID());
        }
        for (int i = 0, size = data.length; i < size; i++) {
            Appointment app = data[i];

            assertFalse(ids.contains(app.getObjectID()));
        }
    }

    @Test
    public void testShowAppointmentsBetween() throws Exception {
        final Date start = new Date(System.currentTimeMillis() - (dayInMillis * 7));
        final Date end = new Date(System.currentTimeMillis() + (dayInMillis * 7));

        final int cols[] = new int[] { Appointment.OBJECT_ID };

        catm.all(appointmentFolderId, start, end, cols);
    }

    @Test
    public void testShowAllAppointmentWhereIAmParticipant() throws Exception {
        final Date start = new Date(System.currentTimeMillis() - (dayInMillis * 7));
        final Date end = new Date(System.currentTimeMillis() + (dayInMillis * 7));

        final int cols[] = new int[] { Appointment.OBJECT_ID };

        catm.all(appointmentFolderId, start, end, cols);
    }

    @Test
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
        final int objectId = catm.insert(appointmentObj).getObjectID();

        calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(startTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date start = calendar.getTime();
        Date end = new Date(start.getTime() + dayInMillis);

        Appointment[] appointmentArray = catm.all(appointmentFolderId, start, end, cols);
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
        appointmentArray = catm.all(appointmentFolderId, start, end, cols);
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
        appointmentArray = catm.all(appointmentFolderId, start, end, cols);
        found = false;
        for (int a = 0; a < appointmentArray.length; a++) {
            if (appointmentArray[a].getObjectID() == objectId) {
                found = true;
            }
        }
        assertFalse("appointment found one day after start date in day view", found);
    }

    // Bug 12171
    @Test
    public void testShowOcurrences() throws Exception {
        final int cols[] = new int[] { Appointment.OBJECT_ID, Appointment.RECURRENCE_COUNT };

        final Appointment appointmentObj = createAppointmentObject("testShowOcurrences");
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date(System.currentTimeMillis() + 60 * 60 * 1000));
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setOccurrence(3);
        appointmentObj.setIgnoreConflicts(true);
        int objectId = catm.insert(appointmentObj).getObjectID();
        final Appointment[] appointmentArray = catm.all(appointmentFolderId, new Date(0), new Date(Long.MAX_VALUE), cols);

        for (final Appointment loaded : appointmentArray) {
            if (loaded.getObjectID() == objectId) {
                assertEquals(appointmentObj.getOccurrence(), loaded.getOccurrence());
            }
        }
    }

    // Node 2652
    @Test
    public void testLastModifiedUTC() throws Exception {
        final int cols[] = new int[] { Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.LAST_MODIFIED_UTC };

        final Appointment appointmentObj = createAppointmentObject("testShowLastModifiedUTC");
        appointmentObj.setStartDate(new Date());
        appointmentObj.setEndDate(new Date(System.currentTimeMillis() + 60 * 60 * 1000));
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();
        final AllRequest req = new AllRequest(appointmentFolderId, cols, new Date(0), new Date(Long.MAX_VALUE), TimeZone.getTimeZone("UTC"));

        final CommonAllResponse response = Executor.execute(getClient(), req);
        final JSONArray arr = (JSONArray) response.getResponse().getData();

        assertNotNull(arr);
        final int size = arr.length();
        assertTrue(size > 0);
        for (int i = 0; i < size; i++) {
            final JSONArray objectData = arr.optJSONArray(i);
            assertNotNull(objectData);
            assertNotNull(objectData.opt(2));
        }
    }
}
