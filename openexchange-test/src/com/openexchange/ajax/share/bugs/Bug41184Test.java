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

package com.openexchange.ajax.share.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetInfostoreResponse;
import com.openexchange.ajax.share.Abstract2UserShareTest;
import com.openexchange.ajax.smtptest.MailManager;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.tryagain.TryAgain;

/**
 * {@link Bug41184Test}
 *
 * Unable to open a shared document, if its folder was previously shared to the same entity
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug41184Test extends Abstract2UserShareTest {

    @Test
    @TryAgain
    public void testAccessSharedFileInSharedFolder() throws Exception {
        /*
         * create folder shared to other user
         */
        int userId = client2.getValues().getUserId();
        OCLPermission permission = new OCLPermission(userId, 0);
        permission.setAllPermission(OCLPermission.READ_ALL_OBJECTS, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        FolderObject folder = insertSharedFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(FolderObject.INFOSTORE), permission);
        /*
         * fetch & check internal link from notification mail
         */
        String folderLink = discoverInvitationLink(apiClient2, client2.getValues().getDefaultAddress());
        Assert.assertNotNull("Invitation link not found", folderLink);
        String fragmentParams = new URI(folderLink).getRawFragment();
        Matcher folderMatcher = Pattern.compile("folder=([0-9]+)").matcher(fragmentParams);
        Assert.assertTrue("Folder param missing in fragment", folderMatcher.find());
        String folderID = String.valueOf(folder.getObjectID());
        Assert.assertEquals(folderID, folderMatcher.group(1));
        new MailManager(apiClient2).clearMails();
        /*
         * create file in this folder share it to the same user, too
         */
        DefaultFileStorageObjectPermission objectPermission = new DefaultFileStorageObjectPermission(userId, false, FileStorageObjectPermission.READ);
        File file = insertSharedFile(folder.getObjectID(), objectPermission);
        /*
         * fetch & check internal link from notification mail
         */
        String fileLink = discoverInvitationLink(apiClient2, client2.getValues().getDefaultAddress());
        Assert.assertNotNull("Invitation link not found", fileLink);
        fragmentParams = new URI(fileLink).getRawFragment();
        folderMatcher = Pattern.compile("folder=([0-9]+)").matcher(fragmentParams);
        Assert.assertTrue("Folder param missing in fragment", folderMatcher.find());
        Assert.assertEquals(folderID, folderMatcher.group(1));
        Matcher fileMatcher = Pattern.compile("id=([0-9]+/[0-9]+)").matcher(fragmentParams);
        Assert.assertTrue("ID param missing in fragment", fileMatcher.find());
        FileID fileID = new FileID(file.getId());
        fileID.setFolderId(folderID);
        Assert.assertEquals(fileID.toUniqueID(), fileMatcher.group(1));
        /*
         * try and access the file in shared files folder
         */
        GetInfostoreRequest getRequest = new GetInfostoreRequest(fileMatcher.group(1));
        GetInfostoreResponse getResponse = getClient().execute(getRequest);
        File metadata = getResponse.getDocumentMetadata();
        assertNotNull(metadata);
        assertEquals(metadata.getId(), fileMatcher.group(1));
        assertEquals(metadata.getFolderId(), folderMatcher.group(1));
        /*
         * try and access the file in it's physical folder, too
         */
        getRequest = new GetInfostoreRequest(file.getId());
        getResponse = getClient().execute(getRequest);
        metadata = getResponse.getDocumentMetadata();
        assertNotNull(metadata);
        assertEquals(metadata.getId(), file.getId());
        assertEquals(metadata.getFolderId(), file.getFolderId());
    }

}
