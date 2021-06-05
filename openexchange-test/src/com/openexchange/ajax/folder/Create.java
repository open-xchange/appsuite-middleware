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
import org.json.JSONException;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Create {

    private Create() {
        super();
    }

    /**
     * This method creates a public folder object. Everyone gets full object
     * permissions on this folder.
     *
     * @param name name of the folder.
     * @param type PIM type of the folder.
     * @param admin user identifier of the admin.
     * @return a ready to insert folder.
     */
    public static FolderObject setupPublicFolder(final String name, final int type, final int admin) {
        final FolderObject folder = new FolderObject();
        folder.setFolderName(name);
        folder.setModule(type);
        folder.setType(FolderObject.PUBLIC);
        final OCLPermission perm1 = new OCLPermission();
        perm1.setEntity(admin);
        perm1.setGroupPermission(false);
        perm1.setFolderAdmin(true);
        perm1.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        final OCLPermission perm2 = new OCLPermission();
        perm2.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
        perm2.setGroupPermission(true);
        perm2.setFolderAdmin(false);
        perm2.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);
        folder.setPermissionsAsArray(new OCLPermission[] { perm1, perm2 });
        return folder;
    }

    /**
     * This method creates a public folder
     *
     * @param client
     * @param name
     * @param module the module (e.g. CONTACT) from FolderObject.java
     * @return
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    public static FolderObject createPublicFolder(final AJAXClient client, final String name, final int module) throws OXException, IOException, JSONException {
        return createPublicFolder(client, name, module, true);
    }

    /**
     * This method creates a public folder
     *
     * @param client
     * @param name
     * @param module the module (e.g. CONTACT) from FolderObject.java
     * @param failOnError whether the request will fail if the server returns an error
     * @return
     * @throws OXException
     * @throws IOException
     * @throws JSONException
     */
    public static FolderObject createPublicFolder(final AJAXClient client, final String name, final int module, boolean failOnError) throws OXException, IOException, JSONException {
        final FolderObject folder = setupPublicFolder(name, module, client.getValues().getUserId());
        folder.setParentFolderID(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        final InsertRequest request = new InsertRequest(EnumAPI.OX_OLD, folder, failOnError);
        final InsertResponse response = client.execute(request);
        response.fillObject(folder);
        return folder;
    }

    /**
     * This method creates a private folder object. Admin user gets full access
     * permissions.
     *
     * @param name name of the folder.
     * @param module PIM module of the folder.
     * @param admin user identifier of the admin.
     * @return a ready to insert folder.
     */
    public static FolderObject createPrivateFolder(final String name, final int module, final int admin, OCLPermission... shared) {
        final FolderObject folder = new FolderObject();
        folder.setFolderName(name);
        folder.setModule(module);
        folder.setType(FolderObject.PRIVATE);
        final OCLPermission perm = ocl(admin, false, true, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        folder.addPermission(perm);
        for (OCLPermission permission : shared) {
            folder.addPermission(permission);
        }
        return folder;
    }

    public static OCLPermission ocl(final int entity, final boolean isGroup, final boolean isAdmin, final int fp, final int opr, final int opw, final int opd) {
        final OCLPermission retval = new OCLPermission();
        retval.setEntity(entity);
        retval.setGroupPermission(isGroup);
        retval.setFolderAdmin(isAdmin);
        retval.setAllPermission(fp, opr, opw, opd);
        return retval;
    }

    public static FolderObject folder(final int parent, final String name, final int module, final int type, final OCLPermission... permissions) {
        final FolderObject retval = new FolderObject();
        retval.setParentFolderID(parent);
        retval.setFolderName(name);
        retval.setModule(module);
        retval.setType(type);
        retval.setPermissionsAsArray(permissions);
        return retval;
    }
}
