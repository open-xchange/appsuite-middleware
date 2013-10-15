
package com.openexchange.ajax.appointment.bugtests;

import java.util.Calendar;
import java.util.List;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.ListIDs;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.test.CalendarTestManager;

/**
 * {@link Bug29268Test}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug29268Test extends AbstractAJAXSession {

    private Appointment appointment;

    private CalendarTestManager ctm;

    public Bug29268Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ctm = new CalendarTestManager(client);

        appointment = new Appointment();
        appointment.setTitle("Bug 29268 Test");
        Calendar start = TimeTools.createCalendar(client.getValues().getTimeZone());
        start.add(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 8);
        Calendar end = TimeTools.createCalendar(client.getValues().getTimeZone());
        end.add(Calendar.DAY_OF_MONTH, 1);
        end.set(Calendar.HOUR_OF_DAY, 9);
        appointment.setStartDate(start.getTime());
        appointment.setEndDate(end.getTime());
        appointment.setAlarm(0);
        appointment.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        appointment.setIgnoreConflicts(true);
        ctm.insert(appointment);
    }

    @Test
    public void testBug29268() throws Exception {
        Appointment getAppointment = ctm.get(appointment);
        assertTrue("Missing alarm value for get request.", getAppointment.containsAlarm());
        assertEquals("Wrong alarm value for get request.", 0, getAppointment.getAlarm());

        ListIDs listIDs = new ListIDs(appointment.getParentFolderID(), appointment.getObjectID());
        List<Appointment> listAppointment = ctm.list(listIDs, new int[] { Appointment.ALARM });
        assertTrue("Missing alarm value for list request.", listAppointment.get(0).containsAlarm());
        assertEquals("Wrong alarm value for list request.", 0, listAppointment.get(0).getAlarm());
    }

    @Override
    public void tearDown() throws Exception {
        ctm.cleanUp();
        super.tearDown();
    }

}
