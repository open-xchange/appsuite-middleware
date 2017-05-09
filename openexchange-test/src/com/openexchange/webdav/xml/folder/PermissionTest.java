
package com.openexchange.webdav.xml.folder;

import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.webdav.xml.FolderTest;
import com.openexchange.webdav.xml.GroupUserTest;

public class PermissionTest extends FolderTest {

    @Test
    public void testInsertPrivateFolderWithoutPermission() throws Exception {
        GroupUserTest.getUserId(getSecondWebConversation(), getHostURI(), getSecondLogin(), getPassword());

        FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testInsertPrivateFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.TASK);
        folderObj.setType(FolderObject.PRIVATE);
        folderObj.setParentFolderID(1);

        final OCLPermission[] permission = new OCLPermission[] { createPermission(userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS)
        };

        folderObj.setPermissionsAsArray(permission);

        final int objectId = insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());
        folderObj = createFolderObject(userId, "testInsertPrivateFolderWithoutPermission", FolderObject.TASK, false);
        folderObj.setParentFolderID(objectId);
        try {
            final int subFolderId1 = insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());
            deleteFolder(getWebConversation(), new int[] { subFolderId1 }, getHostURI(), getLogin(), getPassword());
            fail("conflict permission expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), 1002);
        }

        deleteFolder(getWebConversation(), new int[] { objectId }, getHostURI(), getLogin(), getPassword());
    }

    @Test
    public void testInsertPublicFolderWithoutPermission() throws Exception {
        GroupUserTest.getUserId(getSecondWebConversation(), getHostURI(), getSecondLogin(), getPassword());

        FolderObject folderObj = new FolderObject();
        folderObj.setFolderName("testInsertPublicFolderWithoutPermission" + System.currentTimeMillis());
        folderObj.setModule(FolderObject.TASK);
        folderObj.setType(FolderObject.PUBLIC);
        folderObj.setParentFolderID(2);

        final OCLPermission[] permission = new OCLPermission[] { createPermission(userId, false, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS)
        };

        folderObj.setPermissionsAsArray(permission);

        final int objectId = insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());
        folderObj = createFolderObject(userId, "testInsertPublicFolderWithoutPermission", FolderObject.TASK, true);
        folderObj.setParentFolderID(objectId);
        try {
            final int subFolderId1 = insertFolder(getWebConversation(), folderObj, getHostURI(), getLogin(), getPassword());
            deleteFolder(getWebConversation(), new int[] { subFolderId1 }, getHostURI(), getLogin(), getPassword());
            fail("conflict permission expected!");
        } catch (final OXException exc) {
            assertExceptionMessage(exc.getMessage(), 1002);
        }

        deleteFolder(getWebConversation(), new int[] { objectId }, getHostURI(), getLogin(), getPassword());
    }
}
