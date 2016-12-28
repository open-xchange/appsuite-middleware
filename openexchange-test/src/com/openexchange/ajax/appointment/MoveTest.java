
package com.openexchange.ajax.appointment;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.AppointmentTest;
import com.openexchange.groupware.configuration.AbstractConfigWrapper;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.FolderTestManager;

public class MoveTest extends AppointmentTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MoveTest.class);
    private String login;
    private String password;
    private String context;
    private int objectId;

    public MoveTest() {
        super();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        login = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "login", "");
        context = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "contextName", "defaultcontext");
        password = AbstractConfigWrapper.parseProperty(getAJAXProperties(), "password", "");
    }

    @Test
    public void testMove2PrivateFolder() throws Exception {
        final Appointment appointmentObj = new Appointment();
        final String date = String.valueOf(System.currentTimeMillis());
        appointmentObj.setTitle("testMove2PrivateFolder" + date);
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setShownAs(Appointment.RESERVED);
        objectId = catm.insert(appointmentObj).getObjectID();

        final FolderObject folderObj = FolderTestManager.createNewFolderObject("testMove2PrivateFolder" + System.currentTimeMillis(), FolderObject.CALENDAR, FolderObject.PRIVATE, userId, 1);
        int targetFolder = ftm.insertFolderOnServer(folderObj).getObjectID();

        appointmentObj.setParentFolderID(targetFolder);
        catm.update(appointmentFolderId, appointmentObj);
        final Appointment loadAppointment = catm.get(targetFolder, objectId);
        appointmentObj.setObjectID(objectId);
        compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
    }

    @Test
    public void testMove2PublicFolder() throws Exception {
        final Appointment appointmentObj = new Appointment();
        final String date = String.valueOf(System.currentTimeMillis());
        appointmentObj.setTitle("testMove2PublicFolder" + date);
        appointmentObj.setStartDate(new Date(startTime));
        appointmentObj.setEndDate(new Date(endTime));
        appointmentObj.setOrganizer(testUser.getUser());
        appointmentObj.setParentFolderID(appointmentFolderId);
        appointmentObj.setIgnoreConflicts(true);
        appointmentObj.setShownAs(Appointment.RESERVED);
        objectId = catm.insert(appointmentObj).getObjectID();

        final FolderObject folderObj = FolderTestManager.createNewFolderObject("testMove2PublicFolder" + System.currentTimeMillis(), FolderObject.CALENDAR, FolderObject.PUBLIC, userId, 2);
        int targetFolder = ftm.insertFolderOnServer(folderObj).getObjectID();

        appointmentObj.setParentFolderID(targetFolder);
        catm.update(appointmentFolderId, appointmentObj);
        final Appointment loadAppointment = catm.get(targetFolder, objectId);
        appointmentObj.setObjectID(objectId);
        compareObject(appointmentObj, loadAppointment, appointmentObj.getStartDate().getTime(), appointmentObj.getEndDate().getTime());
    }
}
