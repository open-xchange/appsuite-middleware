package com.openexchange.ajax.folder.api2;

import java.util.ArrayList;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.FolderTestManager;


public class ChangePermissionsTest extends AbstractAJAXSession {

    private AJAXClient client2;
    private FolderObject folder;
    private FolderObject secondFolder;
    private FolderTestManager ftm1;
    private FolderTestManager ftm2;
    private String folderName;

    public ChangePermissionsTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        folderName = "ChangePermissionsTest Folder" + System.currentTimeMillis();
        client2 = new AJAXClient(User.User2);
        ftm1 = new FolderTestManager(client);
    }

    public void notestChangePermissionsSuccess() throws Exception {
        folder = ftm1.generatePublicFolder(folderName, FolderObject.INFOSTORE, client.getValues().getPrivateInfostoreFolder(), new int[] {client.getValues().getUserId()});
        final InsertRequest insertFolderReq = new InsertRequest(EnumAPI.OUTLOOK, folder, false);
        final InsertResponse insertFolderResp = client.execute(insertFolderReq);

        assertNull("Inserting folder caused exception.", insertFolderResp.getException());
        insertFolderResp.fillObject(folder);

        {
            ArrayList<OCLPermission> allPermissions = new ArrayList<OCLPermission>();
            {
                OCLPermission permissions = new OCLPermission();
                permissions.setEntity(client.getValues().getUserId());
                permissions.setGroupPermission(false);
                permissions.setFolderAdmin(true);
                permissions.setAllPermission(
                    OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION);
                allPermissions.add(permissions);
            }
            {
                OCLPermission permissions = new OCLPermission();
                permissions.setEntity(client2.getValues().getUserId());
                permissions.setGroupPermission(false);
                permissions.setFolderAdmin(false);
                permissions.setAllPermission(
                    OCLPermission.READ_FOLDER,
                    OCLPermission.READ_ALL_OBJECTS,
                    OCLPermission.NO_PERMISSIONS,
                    OCLPermission.NO_PERMISSIONS);
                allPermissions.add(permissions);
            }
            folder.setPermissions(allPermissions);
        }

        folder = ftm1.updateFolderOnServer(folder);
        assertTrue("Unexpected number of permissions", 2 == folder.getNonSystemPermissionsAsArray().length);
    }

    public void testChangePermissionsFail() throws Exception {
        folder = ftm1.generatePublicFolder(folderName, FolderObject.INFOSTORE, client.getValues().getInfostoreTrashFolder(), new int[] {client.getValues().getUserId()});
        final InsertRequest insertFolderReq = new InsertRequest(EnumAPI.OUTLOOK, folder, false);
        final InsertResponse insertFolderResp = client.execute(insertFolderReq);

        assertNull("Inserting folder caused exception.", insertFolderResp.getException());
        insertFolderResp.fillObject(folder);

        {
            ArrayList<OCLPermission> allPermissions = new ArrayList<OCLPermission>();
            {
                OCLPermission permissions = new OCLPermission();
                permissions.setEntity(client.getValues().getUserId());
                permissions.setGroupPermission(false);
                permissions.setFolderAdmin(true);
                permissions.setAllPermission(
                    OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION,
                    OCLPermission.ADMIN_PERMISSION);
                allPermissions.add(permissions);
            }
            {
                OCLPermission permissions = new OCLPermission();
                permissions.setEntity(client2.getValues().getUserId());
                permissions.setGroupPermission(false);
                permissions.setFolderAdmin(false);
                permissions.setAllPermission(
                    OCLPermission.READ_FOLDER,
                    OCLPermission.READ_ALL_OBJECTS,
                    OCLPermission.NO_PERMISSIONS,
                    OCLPermission.NO_PERMISSIONS);
                allPermissions.add(permissions);
            }
            folder.setPermissions(allPermissions);
        }

        folder = ftm1.updateFolderOnServer(folder, false);
        AbstractAJAXResponse lastResponse = ftm1.getLastResponse();
        assertNotNull("Updating trash folder permissions not denied, but should.", lastResponse.getException());
    }

    @Override
    protected void tearDown() throws Exception {
        ftm1.deleteFolderOnServer(folder);
        client2.logout();

        super.tearDown();
    }

}
