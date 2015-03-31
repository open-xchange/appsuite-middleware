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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.json.JSONException;
import com.openexchange.ajax.drive.action.DeleteLinkRequest;
import com.openexchange.ajax.drive.action.GetLinkRequest;
import com.openexchange.ajax.drive.action.GetLinkResponse;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.drive.DriveShareTarget;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.util.UUIDs;
import com.openexchange.test.TestInit;

/**
 * {@link GetLinkTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class GetLinkTest extends AbstractDriveShareTest {

    private InfostoreTestManager itm;
    private FolderObject rootFolder, folder;
    private DefaultFile file;

    /**
     * Initializes a new {@link GetLinkTest}.
     * 
     * @param name
     */
    public GetLinkTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        itm = new InfostoreTestManager(client);

        UserValues values = client.getValues();
        rootFolder = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), values.getPrivateInfostoreFolder());
        folder = insertPrivateFolder(EnumAPI.OX_NEW, Module.INFOSTORE.getFolderConstant(), rootFolder.getObjectID());

        long now = System.currentTimeMillis();
        file = new DefaultFile();
        file.setFolderId(String.valueOf(folder.getObjectID()));
        file.setTitle("GetLinkTest_" + now);
        file.setFileName(file.getTitle());
        file.setDescription(file.getTitle());
        itm.newAction(file, new File(TestInit.getTestProperty("ajaxPropertiesFile")));
    }

    public void testGetFileLink() throws Exception {
        DriveShareTarget target = new DriveShareTarget();
        target.setPath("/" + folder.getFolderName());
        target.setName(file.getFileName());
        target.setChecksum("123");
        performTest(target);
    }
    
    public void testGetFolderLink() throws Exception {
        DriveShareTarget target = new DriveShareTarget();
        target.setPath("/" + folder.getFolderName());
        target.setChecksum("123");
        performTest(target);
    }

    private void performTest(DriveShareTarget target) throws OXException, IOException, JSONException, Exception {
        int bits = createAnonymousGuestPermission().getPermissionBits();
        String password = UUIDs.getUnformattedString(UUID.randomUUID());
        GetLinkRequest getLinkRequest = new GetLinkRequest(rootFolder.getObjectID(), Collections.singletonList(target), bits, password, true);
        GetLinkResponse getLinkResponse = client.execute(getLinkRequest);
        String token = getLinkResponse.getToken();
        String url = getLinkResponse.getUrl();

        GuestClient guestClient = resolveShare(url, null, password);
        OCLGuestPermission expectedPermission = createAnonymousGuestPermission();
        expectedPermission.setEntity(guestClient.getValues().getUserId());
        guestClient.checkShareAccessible(expectedPermission);

        client.execute(new DeleteLinkRequest(rootFolder.getObjectID(), token));
        assertNull("Share was not deleted", discoverShare(guestClient.getValues().getUserId(), rootFolder.getObjectID(), file.getId()));
        List<FileStorageObjectPermission> objectPermissions = client.execute(new GetInfostoreRequest(file.getId())).getDocumentMetadata().getObjectPermissions();
        assertNull("Permission was not deleted", objectPermissions);
    }

    @Override
    public void tearDown() throws Exception {
        itm.cleanUp();
        super.tearDown();
    }

}
