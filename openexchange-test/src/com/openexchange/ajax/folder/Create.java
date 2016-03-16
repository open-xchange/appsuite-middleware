/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.folder;

import java.io.IOException;
import org.json.JSONException;
import org.xml.sax.SAXException;
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
     * @param name name of the folder.
     * @param type PIM type of the folder.
     * @param admin user identifier of the admin.
     * @return a ready to insert folder.
     */
    public static FolderObject setupPublicFolder(final String name,
        final int type, final int admin) {
        final FolderObject folder = new FolderObject();
        folder.setFolderName(name);
        folder.setModule(type);
        folder.setType(FolderObject.PUBLIC);
        final OCLPermission perm1 = new OCLPermission();
        perm1.setEntity(admin);
        perm1.setGroupPermission(false);
        perm1.setFolderAdmin(true);
        perm1.setAllPermission(
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION);
        final OCLPermission perm2 = new OCLPermission();
        perm2.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
        perm2.setGroupPermission(true);
        perm2.setFolderAdmin(false);
        perm2.setAllPermission(
            OCLPermission.CREATE_OBJECTS_IN_FOLDER,
            OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.WRITE_ALL_OBJECTS,
            OCLPermission.DELETE_ALL_OBJECTS);
        folder.setPermissionsAsArray(new OCLPermission[] { perm1, perm2 });
        return folder;
    }

    /**
     * This method creates a public folder
     * @param client
     * @param name
     * @param module the module (e.g. CONTACT) from FolderObject.java
     * @return
     * @throws OXException
     * @throws IOException
     * @throws SAXException
     * @throws JSONException
     */
    public static FolderObject createPublicFolder(final AJAXClient client,
        final String name, final int module) throws OXException, IOException,
        SAXException, JSONException {
        return createPublicFolder(client, name, module, true);
    }

    /**
     * This method creates a public folder
     * @param client
     * @param name
     * @param module the module (e.g. CONTACT) from FolderObject.java
     * @param failOnError whether the request will fail if the server returns an error
     * @return
     * @throws OXException
     * @throws IOException
     * @throws SAXException
     * @throws JSONException
     */
    public static FolderObject createPublicFolder(final AJAXClient client, final String name, final int module, boolean failOnError) throws OXException, IOException, SAXException, JSONException {
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
        final OCLPermission perm = ocl(
            admin,
            false,
            true,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION,
            OCLPermission.ADMIN_PERMISSION);
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
