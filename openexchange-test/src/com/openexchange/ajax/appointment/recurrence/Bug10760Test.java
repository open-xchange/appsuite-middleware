
package com.openexchange.ajax.appointment.recurrence;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.groupware.container.Appointment;

/**
 *
 * {@link Bug10760Test}
 * 
 * @author Offspring
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - added clean-up
 *
 */
public class Bug10760Test extends AbstractRecurrenceTest {

    private int objectId;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        objectId = -1;
    }

    @Test
    public void testBug10760() throws Exception {
        final String title = "testBug10760";
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle(title);
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(1);
        appointmentObj.setIgnoreConflicts(true);
        objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);

        appointmentObj.setRecurrencePosition(2);
        catm.update(appointmentFolderId, appointmentObj);
        final List<Integer> tmp = new ArrayList<Integer>();
        for (int i = 0; i < APPOINTMENT_FIELDS.length; i++) {
            tmp.add(Integer.valueOf(APPOINTMENT_FIELDS[i]));
        }
        final Integer[] checkedFields = new Integer[] { Integer.valueOf(Appointment.RECURRENCE_ID), Integer.valueOf(Appointment.RECURRENCE_POSITION) };
        for (int i = 0; i < checkedFields.length; i++) {
            if (!tmp.contains(checkedFields[i])) {
                tmp.add(checkedFields[i]);
            }
        }
        final int[] fields = new int[tmp.size()];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = tmp.get(i).intValue();
        }
        
        final Appointment[] appointmentArray = catm.all(appointmentFolderId, new Date(), new Date());
        for (int a = 0; a < appointmentArray.length; a++) {
            if (appointmentArray[a].getObjectID() == objectId) {
                assertEquals("recurrence id is not equals expected", objectId, appointmentArray[a].getRecurrenceID());
                assertEquals("recurrence pos is not equals expected", 2, appointmentArray[a].getRecurrencePosition());
            }
        }
    }

}
