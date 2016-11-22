
package com.openexchange.webdav.xml.folder;

import org.junit.Test;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.webdav.xml.FolderTest;

public class NewTest extends FolderTest {

    public NewTest() {
        super();
    }

    @Test
    public void testInsertPrivateFolderCalendar() throws Exception {
        final FolderObject folderObj = createFolderObject(userId, "testInsertPrivateFolderCalendar", FolderObject.CALENDAR, false);
        final int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);
        folderObj.setObjectID(objectId);

        final FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password, context);
        compareFolder(folderObj, loadFolder);
    }

    @Test
    public void testInsertPrivateFolderContact() throws Exception {
        final FolderObject folderObj = createFolderObject(userId, "testInsertPrivateFolderContact", FolderObject.CONTACT, false);
        final int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);
        folderObj.setObjectID(objectId);

        final FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password, context);
        compareFolder(folderObj, loadFolder);
    }

    @Test
    public void testInsertPrivateFolderTask() throws Exception {
        final FolderObject folderObj = createFolderObject(userId, "testInsertPrivateFolderTask", FolderObject.TASK, false);
        final int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);
        folderObj.setObjectID(objectId);

        final FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password, context);
        compareFolder(folderObj, loadFolder);
    }

    @Test
    public void testInsertPublicFolderCalendar() throws Exception {
        final FolderObject folderObj = createFolderObject(userId, "testInsertPublicFolderCalendar", FolderObject.CALENDAR, true);
        final int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);
        folderObj.setObjectID(objectId);

        final FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password, context);
        compareFolder(folderObj, loadFolder);
    }

    @Test
    public void testInsertPublicFolderContact() throws Exception {
        final FolderObject folderObj = createFolderObject(userId, "testInsertPublicFolderContact", FolderObject.CONTACT, true);
        final int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);
        folderObj.setObjectID(objectId);

        final FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password, context);
        compareFolder(folderObj, loadFolder);
    }

    @Test
    public void testInsertPublicFolderTask() throws Exception {
        final FolderObject folderObj = createFolderObject(userId, "testInsertPublicFolderTask", FolderObject.TASK, true);
        final int objectId = insertFolder(webCon, folderObj, PROTOCOL + hostName, login, password, context);
        folderObj.setObjectID(objectId);

        final FolderObject loadFolder = loadFolder(webCon, objectId, PROTOCOL + hostName, login, password, context);
        compareFolder(folderObj, loadFolder);
    }
}
