
package com.openexchange.ajax.appointment;

import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;

public class FreeBusyTest extends AppointmentTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FreeBusyTest.class);

    @Test
    public void testUserParticipant() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testUserParticipant");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.RESERVED);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);

        final Date start = new Date(System.currentTimeMillis() - (dayInMillis * 2));
        final Date end = new Date(System.currentTimeMillis() + (dayInMillis * 2));
        final Appointment[] appointmentArray = catm.freeBusy(userId, Participant.USER, start, end);

        boolean found = false;

        for (int a = 0; a < appointmentArray.length; a++) {
            if (objectId == appointmentArray[a].getObjectID()) {
                found = true;

                appointmentObj.removeTitle();
                //appointmentObj.removeParentFolderID();
                compareObject(appointmentObj, appointmentArray[a], startTime, endTime);
            }
        }

        assertTrue("appointment with id " + objectId + " not found in free busy response!", found);
    }

    @Test
    public void testFullTimeUserParticipant() throws Exception {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        final long newStartTime = c.getTimeInMillis();
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testFullTimeUserParticipant");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.RESERVED);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);

        final Date start = new Date(System.currentTimeMillis() - (dayInMillis * 2));
        final Date end = new Date(System.currentTimeMillis() + (dayInMillis * 2));
        
        final Appointment[] appointmentArray = catm.freeBusy(userId, Participant.USER, start, end);

        boolean found = false;

        for (int a = 0; a < appointmentArray.length; a++) {
            if (objectId == appointmentArray[a].getObjectID()) {
                found = true;

                appointmentObj.removeTitle();
                //appointmentObj.removeParentFolderID();
                compareObject(appointmentObj, appointmentArray[a], startTime, endTime);
            }
        }

        assertTrue("appointment with id " + objectId + " not found in free busy response!", found);
    }

    @Test
    public void testUserParticipantStatusFree() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testUserParticipantStatusFree");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.FREE);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);

        final Date start = new Date(System.currentTimeMillis() - (dayInMillis * 2));
        final Date end = new Date(System.currentTimeMillis() + (dayInMillis * 2));
        final Appointment[] appointmentArray = catm.freeBusy(userId, Participant.USER, start, end);

        boolean found = false;

        for (int a = 0; a < appointmentArray.length; a++) {
            if (objectId == appointmentArray[a].getObjectID()) {
                found = true;

                appointmentObj.removeTitle();
                //appointmentObj.removeParentFolderID();
                compareObject(appointmentObj, appointmentArray[a], startTime, endTime);
            }
        }

        assertTrue("appointment with id " + objectId + " was found in free busy response!", found);
    }

    @Test
    public void testResourceParticipantStatusFree() throws Exception {
        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testResourceParticipantStatusFree");
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setShownAs(Appointment.FREE);
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int resourceParticipantId = resTm.search(testContext.getResourceParticipants().get(0)).get(0).getIdentifier();

        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
        participants[0] = new UserParticipant(userId);
        participants[1] = new ResourceParticipant(resourceParticipantId);

        appointmentObj.setParticipants(participants);

        final int objectId = catm.insert(appointmentObj).getObjectID();
        appointmentObj.setObjectID(objectId);

        appointmentObj.removeParticipants();

        final Date start = new Date(System.currentTimeMillis() - (dayInMillis * 2));
        final Date end = new Date(System.currentTimeMillis() + (dayInMillis * 2));

        Appointment[] appointmentArray =catm.freeBusy(userId, Participant.USER, start, end);; 

        boolean found = false;

        for (int a = 0; a < appointmentArray.length; a++) {
            if (objectId == appointmentArray[a].getObjectID()) {
                found = true;

                appointmentObj.removeTitle();
                //appointmentObj.removeParentFolderID();
                compareObject(appointmentObj, appointmentArray[a], startTime, endTime);
            }
        }

        assertTrue("appointment with id " + objectId + " was found in free busy response!", found);
        
        appointmentArray = catm.freeBusy(resourceParticipantId, Participant.RESOURCE, start, end);

        found = false;

        for (int a = 0; a < appointmentArray.length; a++) {
            if (objectId == appointmentArray[a].getObjectID()) {
                found = true;

                appointmentObj.removeTitle();
                appointmentObj.removeParentFolderID();
                compareObject(appointmentObj, appointmentArray[a], startTime, endTime);
            }
        }

        assertTrue("appointment with id " + objectId + " was found in free busy response!", found);
    }
}
