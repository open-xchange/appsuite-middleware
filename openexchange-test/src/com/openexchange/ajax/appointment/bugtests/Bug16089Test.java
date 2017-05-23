
package com.openexchange.ajax.appointment.bugtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.appointment.action.DeleteRequest;
import com.openexchange.ajax.appointment.action.GetRequest;
import com.openexchange.ajax.appointment.action.GetResponse;
import com.openexchange.ajax.appointment.action.InsertRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.FolderTestManager;

public class Bug16089Test extends AbstractAJAXSession {

    private FolderTestManager manager;

    private AJAXClient client;

    FolderObject folderObject1;

    Appointment appointment;

    TimeZone timezone;

    Calendar cal;

    public Bug16089Test() {
        super();

    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        manager = new FolderTestManager(getClient());
        timezone = getClient().getValues().getTimeZone();
        cal = Calendar.getInstance(timezone);

        // create a folder
        folderObject1 = new FolderObject();
        folderObject1.setFolderName("Bug16089Testfolder");
        folderObject1.setType(FolderObject.PUBLIC);
        folderObject1.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        folderObject1.setModule(FolderObject.CALENDAR);
        // create permissions
        final OCLPermission perm1 = new OCLPermission();
        perm1.setEntity(getClient().getValues().getUserId());
        perm1.setGroupPermission(false);
        perm1.setFolderAdmin(true);
        perm1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        folderObject1.setPermissionsAsArray(new OCLPermission[] { perm1 });
        manager.insertFolderOnServer(folderObject1);

        appointment = createAppointment();
    }

    @Test
    public void testConfirmation() throws Exception {
        GetResponse getAppointmentResp = getClient().execute(new GetRequest(appointment));
        Appointment testApp = getAppointmentResp.getAppointment(timezone);

        Participant[] participants = testApp.getParticipants();
        boolean found = false;
        for (Participant p : participants) {
            if (p.getIdentifier() == getClient().getValues().getUserId()) {
                found = true;
                ConfirmableParticipant[] confirmations = testApp.getConfirmations();
                for (ConfirmableParticipant c : confirmations) {
                    if (c.getIdentifier() == getClient().getValues().getUserId()) {
                        int ctx = getContextID(getClient());
                        int publicConfig = ServerUserSetting.getInstance().getDefaultStatusPublic(ctx, getClient().getValues().getUserId());
                        assertEquals("Confirm status isn't equal with user setting.", c.getConfirm(), publicConfig);
                    }
                }
            }
        }

        if (!found) {
            fail("User not found as Participant");
        }
    }

    private int getContextID(AJAXClient client) throws IOException, SAXException {
        String url = "http://" + getClient().getHostname() + "/ajax/config/context_id?session=" + getClient().getSession().getId();
        WebRequest request = new GetMethodWebRequest(url);
        WebResponse response = getClient().getSession().getConversation().getResponse(request);
        String text = response.getText();
        String sub = text.substring(8, text.length() - 1); //TODO: exchange ugly hack for JSON parser
        return Integer.parseInt(sub);
    }

    @After
    public void tearDown() throws Exception {
        try {
            getClient().execute(new DeleteRequest(appointment, false));
            manager.cleanUp();
        } finally {
            super.tearDown();
        }
    }

    private Appointment createAppointment() throws Exception {
        Calendar cal = (Calendar) this.cal.clone();
        cal.add(Calendar.HOUR_OF_DAY, 1);

        Appointment app = new Appointment();
        app.setTitle("Bug16089Appointment");
        app.setIgnoreConflicts(true);
        app.setStartDate(cal.getTime());
        cal.add(Calendar.HOUR_OF_DAY, 1);
        app.setEndDate(cal.getTime());
        app.setParentFolderID(folderObject1.getObjectID());
        app.setRecurrenceType(Appointment.NO_RECURRENCE);

        InsertRequest insApp = new InsertRequest(app, timezone, false);
        AppointmentInsertResponse execute = getClient().execute(insApp);

        execute.fillAppointment(app);

        return app;
    }

}
