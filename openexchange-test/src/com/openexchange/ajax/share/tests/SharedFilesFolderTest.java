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

import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreRequest;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;


/**
 * {@link SharedFilesFolderTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class SharedFilesFolderTest extends ShareTest {

    private static final String SHARED_FOLDER = "10";

    private FolderObject folder;
    private File file;

    /**
     * Initializes a new {@link SharedFilesFolderTest}.
     * @param name
     */
    public SharedFilesFolderTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, client.getValues().getPrivateInfostoreFolder());
        file = insertFile(folder.getObjectID(), randomUID());
    }

    public void testReShareNotPossibleForInternals() throws Exception {
        AJAXClient client2 = new AJAXClient(User.User2);
        AJAXClient client3 = new AJAXClient(User.User3);
        try {
            List<FileStorageObjectPermission> permissions = new ArrayList<FileStorageObjectPermission>(1);
            permissions.add(new DefaultFileStorageObjectPermission(client2.getValues().getUserId(), false, FileStorageObjectPermission.WRITE));
            file.setObjectPermissions(permissions);
            file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });
            String sharedFileId = sharedFileId(file.getId());

            // Check shareable flag
            assertFalse(client2.execute(new GetInfostoreRequest(sharedFileId)).getDocumentMetadata().isShareable());

            // Try to share it anyway
            DefaultFile toUpdate = new DefaultFile();
            toUpdate.setFolderId(SHARED_FOLDER);
            toUpdate.setId(sharedFileId);
            permissions.add(new DefaultFileStorageObjectPermission(client3.getValues().getUserId(), false, FileStorageObjectPermission.WRITE));
            toUpdate.setObjectPermissions(permissions);

            UpdateInfostoreRequest updateInfostoreRequest = new UpdateInfostoreRequest(toUpdate, new Field[] { Field.OBJECT_PERMISSIONS }, file.getLastModified());
            updateInfostoreRequest.setFailOnError(false);
            UpdateInfostoreResponse updateInfostoreResponse = client2.execute(updateInfostoreRequest);
            assertTrue(updateInfostoreResponse.hasError());
            assertTrue(InfostoreExceptionCodes.NO_WRITE_PERMISSION.equals(updateInfostoreResponse.getException()));
        } finally {
            client2.logout();
            client3.logout();
        }
    }

    public void testReShareNotPossibleForInvitedGuests() throws Exception {
        List<FileStorageObjectPermission> permissions = new ArrayList<FileStorageObjectPermission>(1);
        String guestEmail = randomUID() + "@example.com";
        OCLGuestPermission guestPermission = createNamedAuthorPermission(guestEmail, randomUID());
        permissions.add(asObjectPermission(guestPermission));
        file.setObjectPermissions(permissions);
        file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });
        String sharedFileId = sharedFileId(file.getId());

        String invitationLink = discoverInvitationLink(client, guestEmail);
        GuestClient guestClient = resolveShare(invitationLink);
        guestPermission.setEntity(guestClient.getValues().getUserId());
        guestClient.checkFileAccessible(sharedFileId, guestPermission);

        // Check shareable flag
        assertFalse(guestClient.execute(new GetInfostoreRequest(sharedFileId)).getDocumentMetadata().isShareable());

        // Try to share it anyway
        DefaultFile toUpdate = new DefaultFile();
        toUpdate.setFolderId(SHARED_FOLDER);
        toUpdate.setId(sharedFileId);
        permissions.add(asObjectPermission(createNamedAuthorPermission(randomUID() + "example.com", randomUID())));
        toUpdate.setObjectPermissions(permissions);

        UpdateInfostoreRequest updateInfostoreRequest = new UpdateInfostoreRequest(toUpdate, new Field[] { Field.OBJECT_PERMISSIONS }, file.getLastModified());
        updateInfostoreRequest.setFailOnError(false);
        UpdateInfostoreResponse updateInfostoreResponse = guestClient.execute(updateInfostoreRequest);
        assertTrue(updateInfostoreResponse.hasError());
        assertTrue(InfostoreExceptionCodes.NO_WRITE_PERMISSION.equals(updateInfostoreResponse.getException()));
    }

    private static String sharedFileId(String fileId) {
        FileID tmp = new FileID(fileId);
        tmp.setFolderId(SHARED_FOLDER);
        return tmp.toUniqueID();
    }

}
