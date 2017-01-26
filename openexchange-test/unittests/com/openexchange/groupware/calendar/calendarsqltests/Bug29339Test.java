
package com.openexchange.groupware.calendar.calendarsqltests;

import static com.openexchange.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Participant;

/**
 * {@link Bug29339Test}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Bug29339Test extends CalendarSqlTest {

    private CalendarDataObject appointment;

    private CalendarDataObject appointment2;

    private int resourceId;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        int nextYear = Calendar.getInstance().get(Calendar.YEAR) + 1;

        appointment = appointments.buildAppointmentWithResourceParticipants(resource1);
        appointment.setParentFolderID(appointments.getPrivateFolder());
        appointment.setTitle("Bug 29339 Test");
        appointment.setStartDate(D("26.10." + nextYear + " 00:00"));
        appointment.setEndDate(D("27.10." + nextYear + " 00:00"));
        appointment.setFullTime(true);
        appointment.setIgnoreConflicts(true);
        appointments.save(appointment);
        clean.add(appointment);

        resourceId = getResourceId(appointment);

        appointments.switchUser(secondUser);

        appointment2 = appointments.buildAppointmentWithResourceParticipants(resource1);
        appointment2.setTitle("Bug 29339 Test conflict.");
        appointment2.setParentFolderID(appointments.getPrivateFolder());
        appointment2.setStartDate(D("25.10." + nextYear + " 15:00"));
        appointment2.setEndDate(D("26.10." + nextYear + " 15:00"));
        appointment2.setIgnoreConflicts(false);
    }

    @Test
    public void testBug29339() throws Exception {
        CalendarDataObject[] conflicts = appointments.save(appointment2);
        clean.add(appointment2);

        assertTrue("Missing conflicts.", conflicts != null && conflicts.length > 0);
        boolean found = false;
        for (CalendarDataObject conflict : conflicts) {
            for (Participant participant : conflict.getParticipants()) {
                if (participant.getType() == Participant.RESOURCE && participant.getIdentifier() == resourceId) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue("Missing resource conflict.", found);
    }

    private int getResourceId(CalendarDataObject app) {
        for (Participant p : app.getParticipants()) {
            if (p.getType() == Participant.RESOURCE) {
                return p.getIdentifier();
            }
        }
        return -1;
    }

}
