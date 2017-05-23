
package com.openexchange.webdav.xml.task;

import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.TaskTest;
import com.openexchange.webdav.xml.XmlServlet;

public class PermissionTest extends TaskTest {

    @Test
    public void testInsertTaskInPrivateFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testInsertTaskInPrivateFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.TASK);
        folderObj.setType(FolderObject.PRIVATE);
        folderObj.setParentFolderID(1);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS),
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Task taskObj = new Task();
        taskObj.setTitle("testInsertTaskInPrivateFolderWithoutPermission");
        taskObj.setStartDate(startTime);
        taskObj.setEndDate(endTime);
        taskObj.setParentFolderID(parentFolderId);

        try {
            insertTask(getSecondWebConversation(), taskObj, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testInsertTaskInPublicFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testInsertTaskInPublicFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.TASK);
        folderObj.setType(FolderObject.PUBLIC);
        folderObj.setParentFolderID(2);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS)
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Task taskObj = new Task();
        taskObj.setTitle("testInsertTaskInPublicFolderWithoutPermission");
        taskObj.setStartDate(startTime);
        taskObj.setEndDate(endTime);
        taskObj.setParentFolderID(parentFolderId);

        try {
            insertTask(getSecondWebConversation(), taskObj, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testUpdateTaskInPrivateFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testInsertTaskInPrivateFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.TASK);
        folderObj.setType(FolderObject.PRIVATE);
        folderObj.setParentFolderID(1);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS),
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Task taskObj = new Task();
        taskObj.setTitle("testInsertTaskInPrivateFolderWithoutPermission");
        taskObj.setStartDate(startTime);
        taskObj.setEndDate(endTime);
        taskObj.setParentFolderID(parentFolderId);

        final int taskObjectId = insertTask(getWebConversation(), taskObj, getHostURI(), getLogin(), getPassword());
        taskObj.setObjectID(taskObjectId);

        try {
            updateTask(getSecondWebConversation(), taskObj, taskObjectId, parentFolderId, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testUpdateTaskInPublicFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testUpdateTaskInPublicFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.TASK);
        folderObj.setType(FolderObject.PUBLIC);
        folderObj.setParentFolderID(2);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS)
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Task taskObj = new Task();
        taskObj.setTitle("testUpdateTaskInPublicFolderWithoutPermission");
        taskObj.setStartDate(startTime);
        taskObj.setEndDate(endTime);
        taskObj.setParentFolderID(parentFolderId);

        final int taskObjectId = insertTask(getWebConversation(), taskObj, getHostURI(), getLogin(), getPassword());
        taskObj.setObjectID(taskObjectId);

        try {
            updateTask(getSecondWebConversation(), taskObj, taskObjectId, parentFolderId, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testDeleteTaskInPrivateFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testDeleteTaskInPrivateFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.TASK);
        folderObj.setType(FolderObject.PRIVATE);
        folderObj.setParentFolderID(1);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS),
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Task taskObj = new Task();
        taskObj.setTitle("testDeleteTaskInPrivateFolderWithoutPermission");
        taskObj.setStartDate(startTime);
        taskObj.setEndDate(endTime);
        taskObj.setParentFolderID(parentFolderId);

        final int taskObjectId = insertTask(getWebConversation(), taskObj, getHostURI(), getLogin(), getPassword());
        taskObj.setObjectID(taskObjectId);

        try {
            deleteTask(getSecondWebConversation(), taskObjectId, parentFolderId, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testDeleteTaskInPublicFolderWithoutPermission() throws Exception {
        final FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testDeleteTaskInPublicFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.TASK);
        folderObj.setType(FolderObject.PUBLIC);
        folderObj.setParentFolderID(2);

        final OCLPermission[] permission = new OCLPermission[] { FolderTest.createPermission(userId, false, OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.DELETE_OWN_OBJECTS)
        };

        folderObj.setPermissionsAsArray(permission);

        final int parentFolderId = FolderTest.insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());

        final Task taskObj = new Task();
        taskObj.setTitle("testDeleteTaskInPublicFolderWithoutPermission");
        taskObj.setStartDate(startTime);
        taskObj.setEndDate(endTime);
        taskObj.setParentFolderID(parentFolderId);

        final int taskObjectId = insertTask(getWebConversation(), taskObj, getHostURI(), getLogin(), getPassword());
        taskObj.setObjectID(taskObjectId);

        try {
            deleteTask(getSecondWebConversation(), taskObjectId, parentFolderId, getHostURI(), getSecondLogin(), getPassword());
            fail("permission exception expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), XmlServlet.PERMISSION_STATUS);
        }

        FolderTest.deleteFolder(getWebConversation(), new int[] { parentFolderId }, getHostURI(), getLogin(), getPassword());
    }
}
