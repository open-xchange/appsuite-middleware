
package com.openexchange.ajax.appointment.bugtests;

import static org.junit.Assert.assertEquals;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 *
 * @author steffen.templin@open-xchange.com
 *
 */
public class Bug15590Test extends AbstractAJAXSession {

    AJAXClient secondClient;

    FolderObject testFolder;

    Appointment testAppointment;
    Appointment movedAppointment;

    UserValues secondUserValues;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Create 2. User
        secondClient = getClient2();
        secondUserValues = secondClient.getValues();

        // Create Folder and share it to 2. User
        testFolder = Create.createPrivateFolder("bug15590folder_" + UUID.randomUUID().toString(), FolderObject.CALENDAR, getClient().getValues().getUserId());
        testFolder.setParentFolderID(getClient().getValues().getPrivateAppointmentFolder());

        com.openexchange.ajax.folder.actions.InsertRequest insFolder = new com.openexchange.ajax.folder.actions.InsertRequest(EnumAPI.OX_NEW, testFolder);
        InsertResponse folderInsertResponse = getClient().execute(insFolder);
        testFolder.setObjectID(folderInsertResponse.getId());
        // Only necessary because new folder API missed the time stamps.
        testFolder.setLastModified(getClient().execute(new GetRequest(EnumAPI.OX_NEW, testFolder.getObjectID())).getTimestamp());

        FolderTools.shareFolder(getClient(), EnumAPI.OX_NEW, testFolder.getObjectID(), secondUserValues.getUserId(), OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);

        // Create an Appointment as User B
        testAppointment = new Appointment();
        testAppointment.setTitle("testBug15590");

        Calendar calendar = TimeTools.createCalendar(secondUserValues.getTimeZone());
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        //calendar.add(Calendar.HOUR, 1);
        testAppointment.setStartDate(calendar.getTime());
        calendar.add(Calendar.HOUR, 1);
        testAppointment.setEndDate(calendar.getTime());
        testAppointment.setShownAs(Appointment.RESERVED);
        testAppointment.setPrivateFlag(false);
        testAppointment.setParentFolderID(secondUserValues.getPrivateAppointmentFolder());
        testAppointment.setRecurrenceType(Appointment.NO_RECURRENCE);
        testAppointment.setIgnoreConflicts(true);
        testAppointment.setAlarm(15);

        final AppointmentInsertResponse insertR = secondClient.execute(new InsertRequest(testAppointment, TimeZone.getDefault()));
        insertR.fillAppointment(testAppointment);
    }

    @Test
    public void testBug15590() throws Exception {
        Appointment moveAppointment = new Appointment();
        moveAppointment.setIgnoreConflicts(true);
        moveAppointment.setObjectID(testAppointment.getObjectID());
        moveAppointment.setParentFolderID(testFolder.getObjectID());
        moveAppointment.setLastModified(testAppointment.getLastModified());

        final UpdateRequest moveReq = new UpdateRequest(secondUserValues.getPrivateAppointmentFolder(), moveAppointment, TimeZone.getDefault(), true);
        UpdateResponse updateResponse = secondClient.execute(moveReq);
        updateResponse.fillObject(testAppointment);

        com.openexchange.ajax.appointment.action.GetRequest getMovedAppointmentRequest = new com.openexchange.ajax.appointment.action.GetRequest(moveAppointment);
        GetResponse getMovedAppointmentResponse = secondClient.execute(getMovedAppointmentRequest);
        movedAppointment = getMovedAppointmentResponse.getAppointment(TimeZone.getDefault());

        assertEquals(movedAppointment.getParentFolderID(), moveAppointment.getParentFolderID());
    }

    @After
    public void tearDown() throws Exception {
        try {
            // Delete testAppointment
            if (movedAppointment != null) {
                movedAppointment.setLastModified(new Date(Long.MAX_VALUE));
                secondClient.execute(new com.openexchange.ajax.appointment.action.DeleteRequest(movedAppointment));
            } else if (testAppointment != null) {
                testAppointment.setLastModified(new Date(Long.MAX_VALUE));
                final com.openexchange.ajax.appointment.action.DeleteRequest delApp = new com.openexchange.ajax.appointment.action.DeleteRequest(testAppointment.getObjectID(), testFolder.getObjectID(), testAppointment.getLastModified());
                secondClient.execute(delApp);
            }

            if (secondClient != null) {
                secondClient.logout();
            }

            // Delete testFolder
            if (testFolder != null) {
                final DeleteRequest delFolder = new DeleteRequest(EnumAPI.OX_NEW, testFolder);
                getClient().execute(delFolder);
            }

        } finally {
            super.tearDown();
        }

    }
}
