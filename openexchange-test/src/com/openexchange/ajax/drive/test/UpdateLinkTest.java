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

package com.openexchange.ajax.drive.test;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.openexchange.ajax.drive.action.DeleteLinkRequest;
import com.openexchange.ajax.drive.action.GetLinkRequest;
import com.openexchange.ajax.drive.action.GetLinkResponse;
import com.openexchange.ajax.drive.action.UpdateLinkRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.drive.DriveShareTarget;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.util.UUIDs;
import com.openexchange.test.TestInit;

/**
 * {@link UpdateLinkTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class UpdateLinkTest extends AbstractDriveShareTest {

    private InfostoreTestManager itm;
    private FolderObject rootFolder;
    private FolderObject folder;
    private DefaultFile file;
    private FolderObject folder2;

    /**
     * Initializes a new {@link UpdateLinkTest}.
     *
     * @param name
     */
    public UpdateLinkTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        itm = new InfostoreTestManager(client);

        UserValues values = client.getValues();
        rootFolder = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), values.getPrivateInfostoreFolder());
        folder = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), rootFolder.getObjectID());
        folder2 = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), rootFolder.getObjectID());

        long now = System.currentTimeMillis();
        file = new DefaultFile();
        file.setFolderId(String.valueOf(folder2.getObjectID()));
        file.setTitle("GetLinkTest_" + now);
        file.setFileName(file.getTitle());
        file.setDescription(file.getTitle());
        file.setFileMD5Sum(getChecksum(new File(TestInit.getTestProperty("ajaxPropertiesFile"))));
        itm.newAction(file, new File(TestInit.getTestProperty("ajaxPropertiesFile")));
    }

    public void testUpdateFileLink() throws Exception {
        // Create Link
        DriveShareTarget target = new DriveShareTarget();
        target.setDrivePath("/" + folder2.getFolderName());
        target.setName(file.getFileName());
        target.setChecksum(file.getFileMD5Sum());
        GetLinkRequest getLinkRequest = new GetLinkRequest(rootFolder.getObjectID(), target);
        GetLinkResponse getLinkResponse = client.execute(getLinkRequest);
        String url = getLinkResponse.getUrl();

        // Check Link
        GuestClient guestClient = resolveShare(url, null, null);
        OCLGuestPermission expectedPermission = createAnonymousGuestPermission();
        expectedPermission.setEntity(guestClient.getValues().getUserId());
        guestClient.checkShareAccessible(expectedPermission);

        // Update Link
        Map<String, Object> newMeta = Collections.<String, Object>singletonMap("test", Boolean.TRUE);
        String newPassword = UUIDs.getUnformattedString(UUID.randomUUID());
        Date newExpiry = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        UpdateLinkRequest updateLinkRequest = new UpdateLinkRequest(rootFolder.getObjectID(), target, newExpiry.getTime(), newPassword, newMeta, true);
        client.execute(updateLinkRequest);

        // Check updated Link
        GuestClient newClient = resolveShare(url, null, newPassword);
        newClient.checkFileAccessible(file.getId(), expectedPermission);
        int guestID = guestClient.getValues().getUserId();

        ExtendedPermissionEntity guestEntity = discoverGuestEntity(file.getFolderId(), file.getId(), guestID);
        assertNotNull(guestEntity);
        assertEquals(newExpiry, guestEntity.getExpiry());

        // Delete Link
        client.execute(new DeleteLinkRequest(rootFolder.getObjectID(), target));
        guestEntity = discoverGuestEntity(file.getFolderId(), file.getId(), guestID);
        assertNull("Share was not deleted", guestEntity);
        List<FileStorageObjectPermission> objectPermissions = client.execute(new GetInfostoreRequest(file.getId())).getDocumentMetadata().getObjectPermissions();
        assertTrue("Permission was not deleted", objectPermissions.isEmpty());
    }

    public void testUpdateFolderLink() throws Exception {
        // Create Link
        DriveShareTarget target = new DriveShareTarget();
        target.setDrivePath("/" + folder.getFolderName());
        target.setChecksum(DriveConstants.EMPTY_MD5);
        GetLinkRequest getLinkRequest = new GetLinkRequest(rootFolder.getObjectID(), target);
        GetLinkResponse getLinkResponse = client.execute(getLinkRequest);
        String url = getLinkResponse.getUrl();

        // Check Link
        GuestClient guestClient = resolveShare(url, null, null);
        OCLGuestPermission expectedPermission = createAnonymousGuestPermission();
        expectedPermission.setEntity(guestClient.getValues().getUserId());
        guestClient.checkShareAccessible(expectedPermission);

        // Update Link
        Map<String, Object> newMeta = Collections.<String, Object>singletonMap("test", Boolean.TRUE);
        String newPassword = UUIDs.getUnformattedString(UUID.randomUUID());
        Date newExpiry = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
        UpdateLinkRequest updateLinkRequest = new UpdateLinkRequest(rootFolder.getObjectID(), target, newExpiry.getTime(), newPassword, newMeta, true);
        client.execute(updateLinkRequest);

        // Check updated Link
        GuestClient newClient = resolveShare(url, null, newPassword);
        newClient.checkFolderAccessible(Integer.toString(folder.getObjectID()), expectedPermission);
        String folder = guestClient.getFolder();
        String item = guestClient.getItem();
        int guestID = guestClient.getValues().getUserId();

        ExtendedPermissionEntity guestEntity;
        if (target.isFolder()) {
            guestEntity = discoverGuestEntity(EnumAPI.OX_NEW, FolderObject.INFOSTORE, Integer.parseInt(folder), guestID);
        } else {
            guestEntity = discoverGuestEntity(folder, item, guestID);
        }
        assertNotNull(guestEntity);
        assertEquals(newExpiry, guestEntity.getExpiry());

        // Delete Link
        client.execute(new DeleteLinkRequest(rootFolder.getObjectID(), target));
        if (target.isFolder()) {
            guestEntity = discoverGuestEntity(EnumAPI.OX_NEW, FolderObject.INFOSTORE, Integer.parseInt(folder), guestID);
        } else {
            guestEntity = discoverGuestEntity(folder, item, guestID);
        }
        assertNull("Share was not deleted", guestEntity);
        List<FileStorageObjectPermission> objectPermissions = client.execute(new GetInfostoreRequest(file.getId())).getDocumentMetadata().getObjectPermissions();
        assertTrue("Permission was not deleted", objectPermissions.isEmpty());
    }

    @Override
    public void tearDown() throws Exception {
        itm.cleanUp();
        super.tearDown();
    }

}
