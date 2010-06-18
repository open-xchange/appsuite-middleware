
package com.openexchange.ajax.appointment.bugtests;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.appointment.action.UpdateRequest;
import com.openexchange.ajax.appointment.action.UpdateResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.API;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.request.ReminderRequest;
import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.servlet.AjaxException;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug16211Test extends AbstractAJAXSession {

    private FolderObject personalAppointmentFolder;

    private FolderObject sharedAppointmentFolder;

    private Appointment appointment;

    private AJAXClient client2;

    private Calendar calendar;

    private TimeZone tz;

    public Bug16211Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        client2 = new AJAXClient(User.User2);
        tz = client.getValues().getTimeZone();
        calendar = TimeTools.createCalendar(tz);

        sharedAppointmentFolder = Create.createPublicFolder(client2, "Bug16211ShareFolder", FolderObject.CALENDAR);
        FolderTools.shareFolder(
            client2,
            API.OX_NEW,
            sharedAppointmentFolder.getObjectID(),
            client.getValues().getUserId(),
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION);

        appointment = createAppointment();

        // Create and insert the personal folder
        personalAppointmentFolder = Create.createPrivateFolder(
            "Bug16211PersonalFolder",
            FolderObject.CALENDAR,
            client.getValues().getUserId());

        personalAppointmentFolder.setParentFolderID(client.getValues().getPrivateAppointmentFolder());
        com.openexchange.ajax.folder.actions.InsertRequest insertFolderReq = new com.openexchange.ajax.folder.actions.InsertRequest(
            API.OX_NEW,
            personalAppointmentFolder,
            false);
        InsertResponse insertFolderResp = client.execute(insertFolderReq);
        insertFolderResp.fillObject(personalAppointmentFolder);
    }

    public void testMoveToPersonalFolder() throws Exception {   
        appointment.setAlarm(15);
        UpdateRequest reminderAppointmentReq = new UpdateRequest(appointment, tz, false);
        UpdateResponse reminderAppointmentResp = client.execute(reminderAppointmentReq);     
        reminderAppointmentResp.fillObject(appointment);
        
        appointment.setParentFolderID(personalAppointmentFolder.getObjectID());
        UpdateRequest moveAppointmentReq = new UpdateRequest(sharedAppointmentFolder.getObjectID(), appointment, tz, false);
        UpdateResponse moveAppointmentResp = client.execute(moveAppointmentReq);
        moveAppointmentResp.fillObject(appointment);

        GetRequest getAppointmentReq = new GetRequest(appointment, false);
        GetResponse getAppointmentResp = client.execute(getAppointmentReq);
        
        JSONObject respObj = (JSONObject) getAppointmentResp.getData();
        
        if (respObj == null) {
            fail("Appointment wasn't found in folder");
        }
    }

    @Override
    public void tearDown() throws Exception {
        // Delete Appointment
        client2.execute(new com.openexchange.ajax.appointment.action.DeleteRequest(
            appointment.getObjectID(),
            client2.getValues().getPrivateAppointmentFolder(),
            new Date()));

        // Delete folders
        client.execute(new DeleteRequest(API.OX_NEW, personalAppointmentFolder.getObjectID(), new Date()));
        client2.execute(new DeleteRequest(API.OX_NEW, sharedAppointmentFolder.getObjectID(), new Date()));
        
        client2.logout();

        super.tearDown();
    }

    private Appointment createAppointment() throws AjaxException, IOException, SAXException, JSONException {
        final Calendar cal = (Calendar) calendar.clone();
        final Appointment appointmentObj = new Appointment();

        appointmentObj.setTitle("testBug16211");
        cal.add(Calendar.DAY_OF_MONTH, 1);
        appointmentObj.setStartDate(cal.getTime());
        cal.add(Calendar.HOUR, 1);
        appointmentObj.setEndDate(cal.getTime());
        appointmentObj.setAlarm(15);

        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(sharedAppointmentFolder.getObjectID());
        appointmentObj.setRecurrenceType(Appointment.NO_RECURRENCE);
        appointmentObj.setIgnoreConflicts(true);

        final AppointmentInsertResponse insResp = client2.execute(new InsertRequest(appointmentObj, tz, false));
        insResp.fillAppointment(appointmentObj);

        return appointmentObj;
    }
}
