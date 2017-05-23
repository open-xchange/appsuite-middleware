
package com.openexchange.ajax.appointment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.ajax.appointment.action.ConflictObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.CalendarTestManager;

public class ConflictTest extends AppointmentTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        final Calendar c = Calendar.getInstance();
        c.setTimeZone(timeZone);
        c.set(Calendar.HOUR_OF_DAY, 8);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        startTime = c.getTimeInMillis();
        startTime += timeZone.getOffset(startTime);
        endTime = startTime + 7200000;
    }

    /**
     * Test case for conflict
     * Appointment Start: 8:00
     * Appointment End: 10:00
     *
     * Conflict Start: 8:00
     * Conflict End: 10:00
     */
    @Test
    public void testConflict1() throws Exception {
        final Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis(startTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        final Date rangeStart = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        final Date rangeEnd = calendar.getTime();

        Appointment appointmentObj = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testConflict1 - insert", new Date(startTime), new Date(endTime));
        appointmentObj.setIgnoreConflicts(true);
        int objectId = catm.insert(appointmentObj).getObjectID();

        Appointment conflictObj = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testConflict1 - insert", new Date(startTime), new Date(endTime));
        conflictObj.setIgnoreConflicts(false);

        catm.insert(conflictObj);
        assertTrue(catm.getLastResponse().hasConflicts());
        List<ConflictObject> conflicts = catm.getLastResponse().getConflicts();

        boolean found = false;
        for (ConflictObject conflict : conflicts) {
            if (conflict.getId() == objectId) {
                found = true;
            }
        }
        assertTrue("appointment id " + objectId + " not found in conflicts", found);

        Appointment newAppointment = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testConflict1 - insert", new Date(startTime), new Date(endTime));
        newAppointment.setIgnoreConflicts(true);
        final int secondObjectId = catm.insert(newAppointment).getObjectID();
        
        Appointment loadAppointment = get(secondObjectId, rangeStart, rangeEnd);
        Date modified = new Date(Long.MAX_VALUE);

        loadAppointment.setObjectID(secondObjectId);
        loadAppointment.setIgnoreConflicts(true);
        loadAppointment.setShownAs(Appointment.FREE);
        loadAppointment.setLastModified(modified);
        catm.update(appointmentFolderId, loadAppointment);

        loadAppointment = catm.get(appointmentFolderId, secondObjectId);
        modified = loadAppointment.getLastModified();

        loadAppointment.setIgnoreConflicts(false);
        loadAppointment.setShownAs(Appointment.ABSENT);
        loadAppointment.setTitle("testConflict1 - update");
        loadAppointment.setObjectID(secondObjectId);
        
        catm.update(appointmentFolderId, loadAppointment);
        assertTrue(catm.getLastResponse().hasConflicts());

        List<ConflictObject> updateConflicts = catm.getLastResponse().getConflicts();
        found = false;
        for (ConflictObject conflict : updateConflicts) {
            if (conflict.getId() == objectId) {
                found = true;
            }
        }
        assertTrue("appointment id " + objectId + " not found in conflicts", found);
    }

    private Appointment get(int objectId, Date rangeStart, Date rangeEnd) {
        Appointment[] all = catm.all(appointmentFolderId, rangeStart, rangeEnd);
        for (Appointment element : all) {
            if (element.getObjectID() == objectId) {
                return element;
            }
        }
        return null;
    }

    /**
     * Test case for conflict
     * Appointment Start: 8:00
     * Appointment End: 10:00
     *
     * Conflict Start: 8:00
     * Conflict End: 9:00
     */
    @Test
    public void testConflict2() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testConflict2");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();
        
        Appointment conflictObj = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testConflict2", new Date(startTime), new Date(endTime - 3600000));
        conflictObj.setIgnoreConflicts(false);

        catm.insert(conflictObj);
        assertTrue(catm.getLastResponse().hasConflicts());
        List<ConflictObject> conflicts = catm.getLastResponse().getConflicts();

        boolean found = false;

        for (ConflictObject conflict : conflicts) {
            if (conflict.getId() == objectId) {
                found = true;
            }
        }

        assertTrue("appointment id " + objectId + " not found in conflicts", found);
    }

    /**
     * Test case for conflict
     * Appointment Start: 8:00
     * Appointment End: 10:00
     *
     * Conflict Start: 8:00
     * Conflict End: 11:00
     */
    @Test
    public void testConflict3() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testConflict3");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();

        Appointment conflictObj = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testConflict3", new Date(startTime), new Date(endTime + 3600000));
        conflictObj.setIgnoreConflicts(false);
        
        catm.insert(conflictObj);
        assertTrue(catm.getLastResponse().hasConflicts());
        List<ConflictObject> conflicts = catm.getLastResponse().getConflicts();

        boolean found = false;

        for (ConflictObject conflict : conflicts) {
            if (conflict.getId() == objectId) {
                found = true;
            }
        }

        assertTrue("appointment id " + objectId + " not found in conflicts", found);
    }

    /**
     * Test case for conflict
     * Appointment Start: 8:00
     * Appointment End: 10:00
     *
     * Conflict Start: 7:00
     * Conflict End: 10:00
     */
    @Test
    public void testConflict4() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testConflict4");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();

        Appointment conflictObj = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testConflict4", new Date(startTime - 3600000), new Date(endTime));
        conflictObj.setIgnoreConflicts(false);

        catm.insert(conflictObj);
        assertTrue(catm.getLastResponse().hasConflicts());
        List<ConflictObject> conflicts = catm.getLastResponse().getConflicts();

        boolean found = false;

        for (ConflictObject conflict : conflicts) {
            if (conflict.getId() == objectId) {
                found = true;
            }
        }

        assertTrue("appointment id " + objectId + " not found in conflicts", found);
    }

    /**
     * Test case for conflict
     * Appointment Start: 8:00
     * Appointment End: 10:00
     *
     * Conflict Start: 9:00
     * Conflict End: 10:00
     */
    @Test
    public void testConflict5() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testConflict5");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();

        Appointment conflictObj = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testConflict5", new Date(startTime + 3600000), new Date(endTime));
        conflictObj.setIgnoreConflicts(false);

        catm.insert(conflictObj);
        assertTrue(catm.getLastResponse().hasConflicts());
        List<ConflictObject> conflicts = catm.getLastResponse().getConflicts();

        boolean found = false;

        for (ConflictObject conflict : conflicts) {
            if (conflict.getId() == objectId) {
                found = true;
            }
        }
        assertTrue("appointment id " + objectId + " not found in conflicts", found);
    }

    /**
     * Test case for conflict
     * Appointment Start: 8:00
     * Appointment End: 10:00
     *
     * Conflict Start: 9:00
     * Conflict End: 9:30
     */
    @Test
    public void testConflict6() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testConflict6");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();

        Appointment conflictObj = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testConflict6", new Date(startTime + 3600000), new Date(endTime - 1800000));
        conflictObj.setIgnoreConflicts(false);

        catm.insert(conflictObj);
        assertTrue(catm.getLastResponse().hasConflicts());
        List<ConflictObject> conflicts = catm.getLastResponse().getConflicts();

        boolean found = false;

        for (ConflictObject conflict : conflicts) {
            if (conflict.getId() == objectId) {
                found = true;
            }
        }

        assertTrue("appointment id " + objectId + " not found in conflicts", found);
    }

    /**
     * Test case for conflict
     * Appointment Start: 8:00
     * Appointment End: 10:00
     *
     * Conflict Start: 7:00
     * Conflict End: 11:00
     */
    @Test
    public void testConflict7() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testConflict7");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();

        Appointment conflictObj = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testConflict7", new Date(startTime - 3600000), new Date(endTime + 3600000));
        conflictObj.setIgnoreConflicts(false);

        catm.insert(conflictObj);
        assertTrue(catm.getLastResponse().hasConflicts());
        List<ConflictObject> conflicts = catm.getLastResponse().getConflicts();

        boolean found = false;

        for (ConflictObject conflict : conflicts) {
            if (conflict.getId() == objectId) {
                found = true;
            }
        }

        assertTrue("appointment id " + objectId + " not found in conflicts", found);
    }

    /**
     * Test case for conflict
     * Appointment Start: 8:00
     * Appointment End: 10:00
     *
     * Conflict Start: 7:00
     * Conflict End: 8:00
     */
    @Test
    public void testNonConflict1() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testNonConflict1");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();

        Appointment conflictObj = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testConflict7", new Date(startTime - 3600000), new Date(endTime - 7200000));
        conflictObj.setIgnoreConflicts(false);

        catm.insert(conflictObj);
        assertFalse(catm.getLastResponse().hasConflicts());
        List<ConflictObject> conflicts = catm.getLastResponse().getConflicts();
        assertNull(conflicts);
    }

    /**
     * Test case for conflict
     * Appointment Start: 8:00
     * Appointment End: 10:00
     *
     * Conflict Start: 10:00
     * Conflict End: 11:00
     */
    @Test
    public void testNonConflict2() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testNonConflict2");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();

        Appointment conflictObj = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testNonConflict2", new Date(startTime + 7200000), new Date(endTime + 3600000));
        conflictObj.setIgnoreConflicts(false);

        catm.insert(conflictObj);
        assertFalse(catm.getLastResponse().hasConflicts());
        List<ConflictObject> conflicts = catm.getLastResponse().getConflicts();
        assertNull(conflicts);
    }

    /**
     * Test case for conflict
     * Appointment Start: Today FullTime
     * Appointment End: +24 Std
     *
     * Conflict Start: 8:00
     * Conflict End: 10:00
     */
    @Test
    public void testFullTimeConflict1() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testFullTimeConflict1");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setFullTime(true);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();

        Appointment conflictObj = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testFullTimeConflict1", new Date(startTime), new Date(endTime));
        conflictObj.setFullTime(true);
        conflictObj.setIgnoreConflicts(false);

        catm.insert(conflictObj);
        assertTrue(catm.getLastResponse().hasConflicts());
        List<ConflictObject> conflicts = catm.getLastResponse().getConflicts();

        boolean found = false;

        for (ConflictObject conflict : conflicts) {
            if (conflict.getId() == objectId) {
                found = true;
            }
        }
        assertTrue("appointment id " + objectId + " not found in conflicts", found);
    }

    /**
     * Test case for conflict
     * Appointment Start: Today FullTime
     * Appointment End: +24 Std
     *
     * Conflict Start: Today FullTime
     * Conflict End: +24 Std
     */
    @Test
    public void testFullTimeConflict2() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testFullTimeConflict2");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setFullTime(true);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        final int objectId = catm.insert(appointmentObj).getObjectID();

        Appointment conflictObj = CalendarTestManager.createAppointmentObject(appointmentFolderId, "testFullTimeConflict2", new Date(startTime), new Date(endTime));
        conflictObj.setIgnoreConflicts(false);
        conflictObj.setFullTime(true);

        catm.insert(conflictObj);
        assertTrue(catm.getLastResponse().hasConflicts());
        List<ConflictObject> conflicts = catm.getLastResponse().getConflicts();

        boolean found = false;

        for (ConflictObject conflict : conflicts) {
            if (conflict.getId() == objectId) {
                found = true;
            }
        }
        assertTrue("appointment id " + objectId + " not found in conflicts", found);
    }
}
