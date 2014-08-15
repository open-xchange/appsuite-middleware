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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.share.tests;

import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link CreateSubfolderTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CreateSubfolderTest extends ShareTest {

    /**
     * Initializes a new {@link CreateSubfolderTest}.
     *
     * @param name The test name
     */
    public CreateSubfolderTest(String name) {
        super(name);
    }

    protected static final int[] TESTED_MODULES = new int[] {
        FolderObject.CONTACT, FolderObject.TASK, FolderObject.CALENDAR
    };

    protected static int randomModule() {
        return TESTED_MODULES[random.nextInt(TESTED_MODULES.length)];
    }

    public void testCreateSubfolderWithoutAdminFlagRandomly() throws Exception {
        testCreateSubfolderWithoutAdminFlag(randomFolderAPI(), randomModule());
    }

    public void testCreateSubfolderWithoutAdminFlagExtensively() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (int module : TESTED_MODULES) {
                testCreateSubfolderWithoutAdminFlag(api, module);
            }
        }
    }

    public void testCreateSubfolderWithAdminFlagRandomly() throws Exception {
        testCreateSubfolderWithAdminFlag(randomFolderAPI(), FolderObject.INFOSTORE);
    }

    public void testCreateSubfolderWithAdminFlagExtensively() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            testCreateSubfolderWithAdminFlag(api, FolderObject.INFOSTORE);
        }
    }

    private void testCreateSubfolderWithAdminFlag(EnumAPI api, int module) throws Exception {
        testCreateSubfolderWithAdminFlag(api, module, getDefaultFolder(module));
    }

    private void testCreateSubfolderWithoutAdminFlag(EnumAPI api, int module) throws Exception {
        testCreateSubfolderWithoutAdminFlag(api, module, getDefaultFolder(module));
    }

    private void testCreateSubfolderWithoutAdminFlag(EnumAPI api, int module, int parent) throws Exception {
        /*
         * create share with guest permissions that allow subfolder creation (yet no update/delete)
         */
        OCLGuestPermission guestPermission = createAnonymousAuthorPermission();
        guestPermission.setFolderPermission(OCLPermission.CREATE_SUB_FOLDERS);
        guestPermission.setFolderAdmin(false);
        /*
         * create folder shared to guest user
         */
        FolderObject folder = insertSharedFolder(api, module, parent, guestPermission);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * discover & check share
         */
        ParsedShare share = discoverShare(folder.getObjectID(), matchingPermission.getEntity());
        checkShare(guestPermission, share);
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(share);
        guestClient.checkShareModuleAvailableExclusively();
        /*
         * create subfolder as guest
         */
        String originalName = randomUID();
        FolderObject subfolder = new FolderObject();
        subfolder.setFolderName(originalName);
        subfolder.setModule(folder.getModule());
        subfolder.setType(folder.getType());
        subfolder.setParentFolderID(folder.getObjectID());
        subfolder.setPermissionsAsArray(folder.getPermissionsAsArray());
        InsertResponse insertResponse = guestClient.execute(new InsertRequest(api, subfolder));
        insertResponse.fillObject(subfolder);
        subfolder.setLastModified(insertResponse.getTimestamp());
        /*
         * verify folder as sharing user
         */
        FolderObject reloadedSubfolder = getFolder(api, subfolder.getObjectID());
        assertNotNull(reloadedSubfolder);
        super.remember(reloadedSubfolder);
        assertEquals("Folder name wrong", originalName, reloadedSubfolder.getFolderName());
        /*
         * try to rename the folder as guest
         */
        subfolder.setFolderName(randomUID());
        InsertResponse updateResponse = guestClient.execute(new UpdateRequest(api, subfolder, false));
        assertTrue("No errors/warnings in response", updateResponse.hasError() || updateResponse.getResponse().hasWarnings());
        /*
         * verify folder is unchanged as sharing user
         */
        reloadedSubfolder = getFolder(api, subfolder.getObjectID());
        assertNotNull(reloadedSubfolder);
        super.remember(reloadedSubfolder);
        assertEquals("Folder name wrong", originalName, reloadedSubfolder.getFolderName());
        /*
         * try to delete the folder as guest
         */
        CommonDeleteResponse deleteResponse = guestClient.execute(new DeleteRequest(api, true, subfolder));
        assertTrue("No errors/warnings in response", deleteResponse.hasError() || deleteResponse.getResponse().hasWarnings());
        /*
         * verify folder is unchanged as sharing user
         */
        reloadedSubfolder = getFolder(api, subfolder.getObjectID());
        assertNotNull(reloadedSubfolder);
        super.remember(reloadedSubfolder);
        assertEquals("Folder name wrong", originalName, reloadedSubfolder.getFolderName());
    }

    private void testCreateSubfolderWithAdminFlag(EnumAPI api, int module, int parent) throws Exception {
        /*
         * create share with guest permissions that allow subfolder creation (yet no update/delete)
         */
        OCLGuestPermission guestPermission = createAnonymousAuthorPermission();
        guestPermission.setFolderPermission(OCLPermission.CREATE_SUB_FOLDERS);
        guestPermission.setFolderAdmin(true);
        /*
         * create folder shared to guest user
         */
        FolderObject folder = insertSharedFolder(api, module, parent, guestPermission);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * discover & check share
         */
        ParsedShare share = discoverShare(folder.getObjectID(), matchingPermission.getEntity());
        checkShare(guestPermission, share);
        /*
         * check access to share
         */
        GuestClient guestClient = resolveShare(share);
        guestClient.checkShareModuleAvailableExclusively();
        /*
         * create subfolder as guest
         */
        String originalName = randomUID();
        FolderObject subfolder = new FolderObject();
        subfolder.setFolderName(originalName);
        subfolder.setModule(folder.getModule());
        subfolder.setType(folder.getType());
        subfolder.setParentFolderID(folder.getObjectID());
        subfolder.setPermissionsAsArray(folder.getPermissionsAsArray());
        InsertResponse insertResponse = guestClient.execute(new InsertRequest(api, subfolder));
        insertResponse.fillObject(subfolder);
        subfolder.setLastModified(insertResponse.getTimestamp());
        /*
         * verify folder as sharing user
         */
        FolderObject reloadedSubfolder = getFolder(api, subfolder.getObjectID());
        assertNotNull(reloadedSubfolder);
        super.remember(reloadedSubfolder);
        assertEquals("Folder name wrong", originalName, reloadedSubfolder.getFolderName());
        /*
         * try to rename the folder as guest
         */
        subfolder.setFolderName(randomUID());
        InsertResponse updateResponse = guestClient.execute(new UpdateRequest(api, subfolder));
        assertFalse("Errors/warnings in response", updateResponse.hasError() || updateResponse.getResponse().hasWarnings());
        /*
         * verify folder is changed as sharing user
         */
        reloadedSubfolder = getFolder(api, subfolder.getObjectID());
        assertNotNull(reloadedSubfolder);
        super.remember(reloadedSubfolder);
        assertEquals("Folder name wrong", subfolder.getFolderName(), reloadedSubfolder.getFolderName());
        /*
         * try to delete the folder as guest
         */
        CommonDeleteResponse deleteResponse = guestClient.execute(new DeleteRequest(api, subfolder));
        assertFalse("Errors/warnings in response", deleteResponse.hasError() || deleteResponse.getResponse().hasWarnings());
    }

}
