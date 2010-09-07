
package com.openexchange.ajax.appointment.bugtests;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.API;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertResponse;
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
    
    UserValues secondUserValues;

    public Bug15590Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        

        // Create 2. User
        secondClient = new AJAXClient(User.User2);
        secondUserValues = secondClient.getValues();
        
        cleanUp(0);

        // Create Folder and share it to 2. User        
        testFolder = Create.createPrivateFolder("bug15590folder", FolderObject.CALENDAR, client.getValues().getUserId());
        testFolder.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        
        com.openexchange.ajax.folder.actions.InsertRequest insFolder = new com.openexchange.ajax.folder.actions.InsertRequest(
            API.OX_NEW,
            testFolder);
        InsertResponse folderInsertResponse = client.execute(insFolder);
        testFolder.setObjectID(folderInsertResponse.getId());
        

        FolderTools.shareFolder(
            client,
            API.OX_NEW,
            testFolder.getObjectID(),
            secondUserValues.getUserId(),
            OCLPermission.CREATE_OBJECTS_IN_FOLDER,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION);

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

    public void testBug15590() throws Exception {
        Appointment moveAppointment = new Appointment();
        moveAppointment.setIgnoreConflicts(true);
        moveAppointment.setObjectID(testAppointment.getObjectID());
        moveAppointment.setParentFolderID(testFolder.getObjectID());
        moveAppointment.setLastModified(testAppointment.getLastModified());
        
        final UpdateRequest moveReq = new UpdateRequest(secondUserValues.getPrivateAppointmentFolder(), moveAppointment, TimeZone.getDefault(), true);
        secondClient.execute(moveReq);
        
        com.openexchange.ajax.appointment.action.GetRequest getMovedAppointmentRequest = new com.openexchange.ajax.appointment.action.GetRequest(moveAppointment);
        com.openexchange.ajax.appointment.action.GetResponse getMovedAppointmentResponse = secondClient.execute(getMovedAppointmentRequest);
        Appointment movedAppointment = getMovedAppointmentResponse.getAppointment(TimeZone.getDefault());
        
        assertEquals(movedAppointment.getParentFolderID(), moveAppointment.getParentFolderID());
    }

    @Override
    public void tearDown() throws Exception {
        // Delete testAppointment
        if (testAppointment != null) {
            final com.openexchange.ajax.appointment.action.DeleteRequest delApp = new com.openexchange.ajax.appointment.action.DeleteRequest(
                testAppointment.getObjectID(), testFolder.getObjectID(), new Date(Long.MAX_VALUE));
            secondClient.execute(delApp);
        }

        if (secondClient != null) {
            secondClient.logout();
        }

        // Delete testFolder
        if (testFolder != null) {
            final DeleteRequest delFolder = new DeleteRequest(API.OX_NEW, testFolder.getObjectID(), new Date());
            client.execute(delFolder);
        }

        super.tearDown();

    }
    
    /**
     * Use this to manually delete a created folder e.g. in cause of Exception
     * @throws Exception
     */
    private void cleanUp(int id) throws Exception {
        if (id != 0) {
            GetRequest getReq = new GetRequest(API.OX_OLD, id);
            GetResponse getResp = client.execute(getReq);
            FolderObject deleteFolder = getResp.getFolder();
            if (deleteFolder != null) {
                final DeleteRequest delFolder = new DeleteRequest(API.OX_OLD, deleteFolder.getObjectID(), new Date());
                client.execute(delFolder);
            }
        }
    }

}
