package com.openexchange.ajax.folder.api2;

import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.FolderTestManager;


public class Bug17261Test extends AbstractAJAXSession {

    private AJAXClient client2;
    private FolderObject folder;
    private FolderObject secondFolder;
    private FolderTestManager ftm1;
    private FolderTestManager ftm2;
    private String folderName;

    public Bug17261Test(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        folderName = "Bug17621 Folder" + System.currentTimeMillis();
        client2 = new AJAXClient(User.User2);
        ftm1 = new FolderTestManager(client);
        folder = ftm1.generatePublicFolder(folderName, FolderObject.CONTACT, 1, new int[] {client.getValues().getUserId()});
        final InsertRequest insertFolderReq = new InsertRequest(EnumAPI.OUTLOOK, folder, false);
        final InsertResponse insertFolderResp = client.execute(insertFolderReq);

        assertNull("Inserting folder caused exception.", insertFolderResp.getException());
        insertFolderResp.fillObject(folder);
    }

    public void testInsertingFolderWithSameNameFromSecondUser() throws Exception {
        ftm2 = new FolderTestManager(client2);
        secondFolder = ftm2.generatePublicFolder(folderName, FolderObject.CONTACT, 1, new int[] {client2.getValues().getUserId()});
        final InsertRequest insertSecondFolderReq = new InsertRequest(EnumAPI.OUTLOOK, secondFolder, false);
        final InsertResponse insertSecondFolderResp = client2.execute(insertSecondFolderReq);

        assertNull("Inserting second folder caused exception.", insertSecondFolderResp.getException());
        insertSecondFolderResp.fillObject(secondFolder);

        ftm2.deleteFolderOnServer(secondFolder);
    }

    public void testMakeFirstFolderVisibleAndTryAgain() throws Exception {
        FolderTools.shareFolder(client, EnumAPI.OUTLOOK, folder.getObjectID(), client2.getValues().getUserId(),
            OCLPermission.READ_FOLDER,
            OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS);

        ftm2 = new FolderTestManager(client2);
        secondFolder = ftm2.generatePublicFolder(folderName, FolderObject.CONTACT, 1, new int[] {client2.getValues().getUserId()});
        final InsertRequest insertSecondFolderReq = new InsertRequest(EnumAPI.OUTLOOK, secondFolder, false);
        final InsertResponse insertSecondFolderResp = client2.execute(insertSecondFolderReq);

        assertNull("Inserting second folder should not cause an exception.", insertSecondFolderResp.getException());
        insertSecondFolderResp.fillObject(secondFolder);

        ftm2.deleteFolderOnServer(secondFolder);
    }

    public void testInsertingFolderCauseException() throws Exception {
        secondFolder = ftm1.generatePublicFolder(folderName, FolderObject.CONTACT, 1, new int[] {client.getValues().getUserId()});
        final InsertRequest insertSecondFolderReq = new InsertRequest(EnumAPI.OUTLOOK, secondFolder, false);
        final InsertResponse insertSecondFolderResp = client2.execute(insertSecondFolderReq);

        assertNotNull("Inserting second folder should cause an exception.", insertSecondFolderResp.getException());
        insertSecondFolderResp.fillObject(secondFolder);

        if (secondFolder.getObjectID() > 0) {
            ftm1.deleteFolderOnServer(secondFolder);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        ftm1.deleteFolderOnServer(folder);
        client2.logout();

        super.tearDown();
    }



}
