
package com.openexchange.webdav.xml.appointment;

import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.AppointmentTest;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.XmlServlet;

public class PermissionTest extends AppointmentTest {

    @Test
    public void testInsertAppointmentInPrivateFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testInsertAppointmentInPrivateFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.CALENDAR);
        folderObj.setType(FolderObject.PRIVATE);
        folderObj.setParentFolderID(1);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS),
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testInsertAppointmentInPrivateFolderWithoutPermission");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(parentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        try {
            insertAppointment(getSecondWebConversation(), appointmentObj, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testInsertAppointmentInPublicFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testInsertAppointmentInPublicFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.CALENDAR);
        folderObj.setType(FolderObject.PUBLIC);
        folderObj.setParentFolderID(2);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS)
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testInsertAppointmentInPublicFolderWithoutPermission");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(parentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        try {
            insertAppointment(getSecondWebConversation(), appointmentObj, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testUpdateAppointmentInPrivateFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testInsertAppointmentInPrivateFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.CALENDAR);
        folderObj.setType(FolderObject.PRIVATE);
        folderObj.setParentFolderID(1);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS),
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testInsertAppointmentInPrivateFolderWithoutPermission");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(parentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int appointmentObjectId = insertAppointment(getWebConversation(), appointmentObj, getHostURI(), getLogin(), getPassword());
        appointmentObj.setObjectID(appointmentObjectId);

        try {
            updateAppointment(getSecondWebConversation(), appointmentObj, appointmentObjectId, parentFolderId, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testUpdateAppointmentInPublicFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testUpdateAppointmentInPublicFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.CALENDAR);
        folderObj.setType(FolderObject.PUBLIC);
        folderObj.setParentFolderID(2);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS)
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testUpdateAppointmentInPublicFolderWithoutPermission");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(parentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int appointmentObjectId = insertAppointment(getWebConversation(), appointmentObj, getHostURI(), getLogin(), getPassword());
        appointmentObj.setObjectID(appointmentObjectId);

        try {
            updateAppointment(getSecondWebConversation(), appointmentObj, appointmentObjectId, parentFolderId, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testDeleteAppointmentInPrivateFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testDeleteAppointmentInPrivateFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.CALENDAR);
        folderObj.setType(FolderObject.PRIVATE);
        folderObj.setParentFolderID(1);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS),
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testDeleteAppointmentInPrivateFolderWithoutPermission");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(parentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int appointmentObjectId = insertAppointment(getWebConversation(), appointmentObj, getHostURI(), getLogin(), getPassword());
        appointmentObj.setObjectID(appointmentObjectId);

        try {
            deleteAppointment(getSecondWebConversation(), appointmentObjectId, parentFolderId, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testDeleteAppointmentInPublicFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testDeleteAppointmentInPublicFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.CALENDAR);
        folderObj.setType(FolderObject.PUBLIC);
        folderObj.setParentFolderID(2);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS)
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Appointment appointmentObj = new Appointment();
        appointmentObj.setTitle("testDeleteAppointmentInPublicFolderWithoutPermission");
        appointmentObj.setStartDate(startTime);
        appointmentObj.setEndDate(endTime);
        appointmentObj.setShownAs(Appointment.ABSENT);
        appointmentObj.setParentFolderID(parentFolderId);
        appointmentObj.setIgnoreConflicts(true);

        final int appointmentObjectId = insertAppointment(getWebConversation(), appointmentObj, getHostURI(), getLogin(), getPassword());
        appointmentObj.setObjectID(appointmentObjectId);

        try {
            deleteAppointment(getSecondWebConversation(), appointmentObjectId, parentFolderId, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }
}
