
package com.openexchange.webdav.xml.folder;

import org.junit.Test;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.webdav.xml.FolderTest;

public class DeleteTest extends FolderTest {

    @Test
    public void testDeleteFolder() throws Exception {
        FolderObject folderObj = createFolderObject(userId, "testDeleteFolder1", FolderObject.CALENDAR, false);
        final int objectId1 = insertFolder(webCon, folderObj, getHostURI(), login, password);
        folderObj = createFolderObject(userId, "testDeleteFolder2", FolderObject.CALENDAR, false);
        final int objectId2 = insertFolder(webCon, folderObj, getHostURI(), login, password);

        deleteFolder(webCon, new int[] { objectId1, objectId2 }, getHostURI(), login, password);
    }
}
