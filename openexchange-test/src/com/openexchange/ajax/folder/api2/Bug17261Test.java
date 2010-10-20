package com.openexchange.ajax.folder.api2;

import com.openexchange.ajax.folder.FolderTools;
import com.openexchange.ajax.folder.actions.API;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.FolderTestManager;


public class Bug17261Test extends AbstractAJAXSession {
    private AJAXClient client;
    private AJAXClient client2;
    private FolderObject folder;
    private FolderObject secondFolder;
    private FolderTestManager ftm1;
    private FolderTestManager ftm2;

    public Bug17261Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        client2 = new AJAXClient(User.User2);
        ftm1 = new FolderTestManager(client);
        folder = ftm1.generateFolder("Bug17621 Folder", FolderObject.CONTACT, 1, new int[] {client.getValues().getUserId()});
        InsertRequest insertFolderReq = new InsertRequest(API.OUTLOOK, folder, false);
        InsertResponse insertFolderResp = client.execute(insertFolderReq);
        
        assertNull("Inserting folder caused exception.", insertFolderResp.getException());
        insertFolderResp.fillObject(folder);   
    }
    
    public void testInsertingFolderWithSameNameFromSecondUser() throws Exception {
        ftm2 = new FolderTestManager(client2);
        secondFolder = ftm2.generateFolder("Bug17621 Folder", FolderObject.CONTACT, 1, new int[] {client2.getValues().getUserId()});
        InsertRequest insertSecondFolderReq = new InsertRequest(API.OUTLOOK, secondFolder, false);
        InsertResponse insertSecondFolderResp = client2.execute(insertSecondFolderReq);
        
        assertNull("Inserting second folder caused exception.", insertSecondFolderResp.getException());
        insertSecondFolderResp.fillObject(secondFolder);
        
        ftm2.deleteFolderOnServer(secondFolder);
    }
    
    public void testMakeFirstFolderVisibleAndTryAgain() throws Exception {
        FolderTools.shareFolder(client, API.OUTLOOK, folder.getObjectID(), client2.getValues().getUserId(), 
            OCLPermission.READ_FOLDER,
            OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS);
        
        ftm2 = new FolderTestManager(client2);
        secondFolder = ftm2.generateFolder("Bug17621 Folder", FolderObject.CONTACT, 1, new int[] {client2.getValues().getUserId()});
        InsertRequest insertSecondFolderReq = new InsertRequest(API.OUTLOOK, secondFolder, false);
        InsertResponse insertSecondFolderResp = client2.execute(insertSecondFolderReq);
        
        assertNotNull("Inserting second folder should cause exception.", insertSecondFolderResp.getException());
        insertSecondFolderResp.fillObject(secondFolder);
        
        ftm2.deleteFolderOnServer(secondFolder);
    }

    @Override
    protected void tearDown() throws Exception {
        ftm1.deleteFolderOnServer(folder);
        client2.logout();
        
        super.tearDown();
    }
    
    

}
