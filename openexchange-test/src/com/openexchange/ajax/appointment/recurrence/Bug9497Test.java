
package com.openexchange.ajax.appointment.recurrence;

import java.util.Calendar;
import java.util.Date;
import org.junit.Test;
import com.openexchange.groupware.container.Appointment;

/**
 *
 * {@link Bug9497Test}
 * 
 * @author Offspring
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - fixes
 *
 */
public class Bug9497Test extends AbstractRecurrenceTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Bug9497Test.class);
    private int objectId = -1;

    public Bug9497Test() {
        super();
    }

    /**
     * This test case checks the calculation for appointments starting before 01.01.1970
     */
    @Test
    public void testBug9497() throws Exception {
        final Date startDate = simpleDateFormatUTC.parse("1969-12-28 00:00:00");
        final Date endDate = simpleDateFormat.parse("1969-12-29 00:00:00");

        final String title = "testBug9497";
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle(title);
        appointmentObj.setStartDate(startDate);
        appointmentObj.setEndDate(endDate);
        appointmentObj.setFullTime(true);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setRecurrenceType(Appointment.YEARLY);
        appointmentObj.setInterval(1);
        appointmentObj.setDayInMonth(28);
        appointmentObj.setMonth(Calendar.DECEMBER);
        appointmentObj.setIgnoreConflicts(true);
        objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);
        catm.get(appointmentFolderId, objectId, 39);
    }
}
