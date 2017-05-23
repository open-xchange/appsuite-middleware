
package com.openexchange.ajax.folder.api2;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

public class ChangePermissionsTest extends AbstractAJAXSession {

    private FolderObject folder;
    

    public void notestChangePermissionsSuccess() throws Exception {
        String folderName = "ChangePermissionsTest Folder" + UUID.randomUUID().toString().replaceAll("-", "");
        folder = ftm.generatePublicFolder(folderName, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), new int[] { getClient().getValues().getUserId() });
        final InsertRequest insertFolderReq = new InsertRequest(EnumAPI.OUTLOOK, folder, false);
        final InsertResponse insertFolderResp = getClient().execute(insertFolderReq);

        assertNull("Inserting folder caused exception.", insertFolderResp.getException());
        insertFolderResp.fillObject(folder);

        {
            ArrayList<OCLPermission> allPermissions = new ArrayList<OCLPermission>();
            {
                OCLPermission permissions = new OCLPermission();
                permissions.setEntity(getClient().getValues().getUserId());
                permissions.setGroupPermission(false);
                permissions.setFolderAdmin(true);
                permissions.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                allPermissions.add(permissions);
            }
            {
                OCLPermission permissions = new OCLPermission();
                permissions.setEntity(getClient2().getValues().getUserId());
                permissions.setGroupPermission(false);
                permissions.setFolderAdmin(false);
                permissions.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
                allPermissions.add(permissions);
            }
            folder.setPermissions(allPermissions);
        }

        folder = ftm.updateFolderOnServer(folder);
        assertTrue("Unexpected number of permissions", 2 == folder.getNonSystemPermissionsAsArray().length);
    }

    @Test
    public void testChangePermissionsFail() throws Exception {
        String folderName = "ChangePermissionsTest Folder" + UUID.randomUUID().toString().replaceAll("-", "");
        folder = ftm.generatePublicFolder(folderName, FolderObject.INFOSTORE, getClient().getValues().getInfostoreTrashFolder(), new int[] { getClient().getValues().getUserId() });
        final InsertRequest insertFolderReq = new InsertRequest(EnumAPI.OUTLOOK, folder, false);
        final InsertResponse insertFolderResp = getClient().execute(insertFolderReq);

        assertNull("Inserting folder caused exception.", insertFolderResp.getException());
        insertFolderResp.fillObject(folder);

        {
            ArrayList<OCLPermission> allPermissions = new ArrayList<OCLPermission>();
            {
                OCLPermission permissions = new OCLPermission();
                permissions.setEntity(getClient().getValues().getUserId());
                permissions.setGroupPermission(false);
                permissions.setFolderAdmin(true);
                permissions.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
                allPermissions.add(permissions);
            }
            {
                OCLPermission permissions = new OCLPermission();
                permissions.setEntity(getClient2().getValues().getUserId());
                permissions.setGroupPermission(false);
                permissions.setFolderAdmin(false);
                permissions.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
                allPermissions.add(permissions);
            }
            folder.setPermissions(allPermissions);
        }

        folder = ftm.updateFolderOnServer(folder, false);
        AbstractAJAXResponse lastResponse = ftm.getLastResponse();
        assertNotNull("Updating trash folder permissions not denied, but should.", lastResponse.getException());
    }
}
