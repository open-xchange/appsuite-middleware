
package com.openexchange.ajax.appointment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.common.test.TestClassConfig;

public class ConfirmTest extends AppointmentTest {

    private CalendarTestManager ctm2;
    private FolderTestManager ftm2;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        ctm2 = new CalendarTestManager(testUser2.getAjaxClient());
        ftm2 = new FolderTestManager(testUser2.getAjaxClient());
    }

    @Override
    public TestClassConfig getTestConfig() {
        return TestClassConfig.builder().createAjaxClient().withUserPerContext(2).build();
    }

    @Test
    public void testConfirm() throws Exception {

        FolderObject user2CalendarFolder = ftm2.getFolderFromServer(testUser2.getAjaxClient().getValues().getPrivateAppointmentFolder());
        final int secondUserId = testUser2.getUserId();

        Appointment appointmentObj = createAppointmentObject("testConfirm");
        final com.openexchange.groupware.container.Participant[] participants = new com.openexchange.groupware.container.Participant[2];
        participants[0] = new UserParticipant(userId);
        participants[1] = new UserParticipant(secondUserId);
        appointmentObj.setParticipants(participants);
        appointmentObj.setIgnoreConflicts(true);

        final int objectId = catm.insert(appointmentObj).getObjectID();

        appointmentObj = ctm2.get(user2CalendarFolder.getObjectID(), objectId);
        ctm2.confirm(appointmentObj, Appointment.ACCEPT, "Yap.");

        final Appointment loadAppointment = catm.get(appointmentFolderId, objectId);

        boolean found = false;

        final UserParticipant[] users = loadAppointment.getUsers();
        for (UserParticipant user : users) {
            if (user.getIdentifier() == secondUserId) {
                found = true;
                assertEquals("wrong confirm status", Appointment.ACCEPT, user.getConfirm());
            }
        }

        assertTrue("user participant with id " + secondUserId + " not found", found);
    }
}
