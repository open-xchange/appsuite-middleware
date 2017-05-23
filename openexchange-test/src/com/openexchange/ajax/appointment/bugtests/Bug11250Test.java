
package com.openexchange.ajax.appointment.bugtests;

import static org.junit.Assert.assertEquals;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.FolderTestManager;

public class Bug11250Test extends AppointmentTest {

    private final static int[] _appointmentFields = { DataObject.OBJECT_ID, CalendarObject.TITLE,
    };

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Bug11250Test.class);

    @Test
    public void testBug11250() throws Exception {
        FolderObject folderObj = FolderTestManager.createNewFolderObject("testBug11250_" + UUID.randomUUID().toString(), FolderObject.CALENDAR, FolderObject.PRIVATE, userId, 1);
        
        final int targetFolder = ftm.insertFolderOnServer(folderObj).getObjectID();

        Appointment appointmentObj = createAppointmentObject("testBug11250_1");
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setParentFolderID(targetFolder);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        simpleDateFormat.setTimeZone(timeZone);

        final int year = Calendar.getInstance(timeZone).get(Calendar.YEAR) + 1;

        final Date start = simpleDateFormat.parse(year + "-06-01 00:00:00");
        final Date end = simpleDateFormat.parse(year + "-06-05 00:00:00");

        Date startDate = simpleDateFormat.parse(year + "-06-01 10:00:00");
        Date endDate = simpleDateFormat.parse(year + "-06-01 11:00:00");

        appointmentObj.setStartDate(startDate);
        appointmentObj.setEndDate(endDate);

        final int objectId1 = catm.insert(appointmentObj).getObjectID();

        startDate = simpleDateFormat.parse(year + "-06-03 10:00:00");
        endDate = simpleDateFormat.parse(year + "-06-03 11:00:00");

        appointmentObj.setTitle("testBug11250_3");
        appointmentObj.setStartDate(startDate);
        appointmentObj.setEndDate(endDate);

        final int objectId3 = catm.insert(appointmentObj).getObjectID();

        appointmentObj = createAppointmentObject("testBug11250_2");
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setParentFolderID(targetFolder);

        startDate = simpleDateFormat.parse(year + "-06-02 10:00:00");
        endDate = simpleDateFormat.parse(year + "-06-02 11:00:00");

        appointmentObj.setStartDate(startDate);
        appointmentObj.setEndDate(endDate);

        appointmentObj.setRecurrenceType(Appointment.DAILY);
        appointmentObj.setInterval(2);
        appointmentObj.setRecurrenceCount(2);

        final int objectId2 = catm.insert(appointmentObj).getObjectID();

        final Appointment[] appointmentArray = catm.all(targetFolder, start, end, _appointmentFields);
        assertEquals("appointment array size not equals", 4, appointmentArray.length);

        assertEquals("appointment id at position 1 not equals", objectId1, appointmentArray[0].getObjectID());
        assertEquals("appointment id at position 2 not equals", objectId2, appointmentArray[1].getObjectID());
        assertEquals("appointment id at position 3 not equals", objectId3, appointmentArray[2].getObjectID());
        assertEquals("appointment id at position 4 not equals", objectId2, appointmentArray[3].getObjectID());
    }
}
