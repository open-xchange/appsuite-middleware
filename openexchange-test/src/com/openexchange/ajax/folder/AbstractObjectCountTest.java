/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.folder;

import java.io.IOException;
import java.util.UUID;
import org.json.JSONException;
import org.junit.Before;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequestNew;
import com.openexchange.ajax.folder.actions.GetResponseNew;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.Abstrac2UserAJAXSession;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.internal.ContentTypeRegistry;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.FolderTestManager;

/**
 * {@link AbstractObjectCountTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public abstract class AbstractObjectCountTest extends Abstrac2UserAJAXSession {

    protected static final int[] DEFAULT_COLUMNS = new int[] { 1, 2, 3, 4, 5, 6, 20, 300, 301, 302, 309 };

    protected AbstractObjectCountTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ContentTypeRegistry ctr = ContentTypeRegistry.getInstance();
        ServiceRegistry.getInstance().addService(ContentTypeRegistry.class, ctr);
    }

    protected static Folder getFolder(AJAXClient client, int folderId, int[] columns) throws OXException, IOException, JSONException {
        GetRequestNew req = new GetRequestNew(EnumAPI.OX_NEW, String.valueOf(folderId), columns);
        GetResponseNew resp = client.execute(req);
        return resp.getFolder();
    }

    /**
     * Creates a private folder for the given module (see modules section in {@link FolderObject}).
     * Client1 will be the folder owner.
     */
    protected static FolderObject createPrivateFolder(AJAXClient client, FolderTestManager ftm, int module) throws OXException, IOException, JSONException {
        FolderObject folder = ftm.generatePrivateFolder(UUID.randomUUID().toString(), module, getParentFolderForModule(client, module), client.getValues().getUserId());
        return ftm.insertFolderOnServer(folder);
    }

    /**
     * Creates a shared folder for the given module (see modules section in {@link FolderObject}).
     *
     * @param client will be the folder owner and can read all objects.
     * @param module the module under test
     * @param userId2 will be the user the folder is shared to. He can only see all objects.
     * @param ftm TODO
     */
    protected static FolderObject createSharedFolder(AJAXClient client, int module, int userId2, FolderTestManager ftm) throws OXException, IOException, JSONException {
        FolderObject folder = ftm.generateSharedFolder(UUID.randomUUID().toString(), module, getParentFolderForModule(client, module), client.getValues().getUserId());

        OCLPermission permissions = new OCLPermission();
        permissions.setEntity(userId2);
        permissions.setGroupPermission(false);
        permissions.setFolderAdmin(false);
        permissions.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        folder.addPermission(permissions);
        return ftm.insertFolderOnServer(folder);
    }

    /**
     * Creates a shared folder for the given module (see modules section in {@link FolderObject}).
     *
     * @param client will be the folder owner and can read all objects.
     * @param module the module under test
     * @param permission the permissions
     * @param ftm TODO
     */
    protected static FolderObject createSharedFolder(AJAXClient client, int module, FolderTestManager ftm, OCLPermission... permissions) throws OXException, IOException, JSONException {
        FolderObject folder = ftm.generateSharedFolder(UUID.randomUUID().toString(), module, getParentFolderForModule(client, module), client.getValues().getUserId());

        folder.removePermissions();

        for (OCLPermission permission : permissions) {
            folder.addPermission(permission);
        }
        return ftm.insertFolderOnServer(folder);
    }

    /**
     * Creates a public folder for the given module (see modules section in {@link FolderObject}).
     *
     * @param client will be the folder owner and can read all objects.
     * @param module the module under test
     * @param userId2 will be the user the folder can be read by. He can only see his own objects.
     * @param ftm TODO
     */
    protected static FolderObject createPublicFolder(AJAXClient client, int module, int userId2, FolderTestManager ftm) throws OXException, IOException, JSONException {
        FolderObject folder = ftm.generatePublicFolder(UUID.randomUUID().toString(), module, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, client.getValues().getUserId());

        OCLPermission permissions = new OCLPermission();
        permissions.setEntity(userId2);
        permissions.setGroupPermission(false);
        permissions.setFolderAdmin(false);
        permissions.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_OWN_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        folder.addPermission(permissions);
        return ftm.insertFolderOnServer(folder);
    }

    protected static int getParentFolderForModule(AJAXClient client, int module) throws OXException, IOException, JSONException {
        switch (module) {
            case FolderObject.CALENDAR:
                return client.getValues().getPrivateAppointmentFolder();

            case FolderObject.TASK:
                return client.getValues().getPrivateTaskFolder();

            case FolderObject.INFOSTORE:
                return client.getValues().getPrivateInfostoreFolder();

            case FolderObject.CONTACT:
                return client.getValues().getPrivateContactFolder();

            default:
                return -1;
        }
    }
}
