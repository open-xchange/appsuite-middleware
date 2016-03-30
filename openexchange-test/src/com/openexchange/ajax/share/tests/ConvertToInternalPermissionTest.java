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

package com.openexchange.ajax.share.tests;

import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link ConvertToInternalPermissionTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ConvertToInternalPermissionTest extends ShareTest {

    /**
     * Initializes a new {@link ConvertToInternalPermissionTest}.
     *
     * @param name The test name
     */
    public ConvertToInternalPermissionTest(String name) {
        super(name);
    }

    public void testConvertToInternalPermissionRandomly() throws Exception {
        testConvertToInternalPermission(randomFolderAPI(), randomModule(), AJAXClient.User.User3);
    }

    public void noTestConvertToInternalPermissionExtensively() throws Exception {
        for (EnumAPI api : TESTED_FOLDER_APIS) {
            for (int module : TESTED_MODULES) {
                testConvertToInternalPermission(api, module, AJAXClient.User.User3);
            }
        }
    }

    public void testConvertToInternalObjectPermissionRandomly() throws Exception {
        testConvertToInternalObjectPermission(randomFolderAPI(), AJAXClient.User.User3);
    }

    private void testConvertToInternalPermission(EnumAPI api, int module, AJAXClient.User user) throws Exception {
        testConvertToInternalPermission(api, module, getDefaultFolder(module), user);
    }

    private void testConvertToInternalPermission(EnumAPI api, int module, int parent, AJAXClient.User user) throws Exception {
        /*
         * prepare guest permission with e-mail address of other other internal user
         */
        AJAXClient userClient = new AJAXClient(user);
        String email = userClient.getValues().getDefaultAddress();
        int userID = userClient.getValues().getUserId();
        OCLGuestPermission guestPermission = createNamedGuestPermission(email, "", AJAXConfig.getProperty(user.getPassword()));
        userClient.logout();
        /*
         * create folder shared to guest user
         */
        FolderObject folder = insertSharedFolder(api, module, parent, guestPermission);
        /*
         * check permissions
         */
        OCLPermission matchingPermission = null;
        for (OCLPermission permission : folder.getPermissions()) {
            if (permission.getEntity() == userID) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created folder found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * check that no share was created for internal user
         */
        ExtendedPermissionEntity entity = discoverGuestEntity(api, module, folder.getObjectID(), matchingPermission.getEntity());
        assertEquals(RecipientType.USER, entity.getType());
    }

    private void testConvertToInternalObjectPermission(EnumAPI api, AJAXClient.User user) throws Exception {
        testConvertToInternalObjectPermission(api, getDefaultFolder(FolderObject.INFOSTORE), user);
    }

    private void testConvertToInternalObjectPermission(EnumAPI api, int parent, AJAXClient.User user) throws Exception {
        /*
         * prepare guest permission with e-mail address of other other internal user
         */
        AJAXClient userClient = new AJAXClient(user);
        String email = userClient.getValues().getDefaultAddress();
        int userID = userClient.getValues().getUserId();
        FileStorageGuestObjectPermission guestPermission = asObjectPermission(createNamedGuestPermission(email, "", AJAXConfig.getProperty(user.getPassword())));
        userClient.logout();
        /*
         * create folder and a shared file inside
         */
        byte[] contents = new byte[64 + random.nextInt(256)];
        random.nextBytes(contents);
        String filename = randomUID();
        FolderObject folder = insertPrivateFolder(api, FolderObject.INFOSTORE, parent);
        File file = insertSharedFile(folder.getObjectID(), filename, guestPermission, contents);
        /*
         * check permissions
         */
        FileStorageObjectPermission matchingPermission = null;
        for (FileStorageObjectPermission permission : file.getObjectPermissions()) {
            if (permission.getEntity() == userID) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created file found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * check that no share was created for internal user
         */
        ExtendedPermissionEntity entity = discoverGuestEntity(file.getFolderId(), file.getId(), matchingPermission.getEntity());
        assertEquals(RecipientType.USER, entity.getType());
    }

}
