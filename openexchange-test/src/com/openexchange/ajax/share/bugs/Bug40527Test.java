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

import java.net.URI;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.share.Abstract2UserShareTest;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.tryagain.TryAgain;

/**
 * Share links are broken - a link like https://ox.example.com/appsuite/ui#!&app=io.ox/files&folder=10&id=1234/9876
 * was generated. The item parameter is broken in terms of the included folder ID, "10/9876" would be the correct value.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class Bug40527Test extends Abstract2UserShareTest {

    @Test
    @TryAgain
    public void testInternalFileShareLinkOnSharedCreation() throws Exception {
        AJAXClient shareClient = client2;
        try {
            FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder());
            DefaultFileStorageObjectPermission sharePermission = new DefaultFileStorageObjectPermission(shareClient.getValues().getUserId(), false, FileStorageObjectPermission.READ);
            File file = insertSharedFile(folder.getObjectID(), randomUID(), sharePermission);
            String invitationLink = discoverInvitationLink(apiClient2, shareClient.getValues().getDefaultAddress());
            Assert.assertNotNull("Invitation link not found", invitationLink);
            String fragmentParams = new URI(invitationLink).getRawFragment();
            Matcher folderMatcher = Pattern.compile("folder=([0-9]+)").matcher(fragmentParams);
            Assert.assertTrue("Folder param missing in fragment", folderMatcher.find());
            Assert.assertEquals("10", folderMatcher.group(1));
            Matcher fileMatcher = Pattern.compile("id=([0-9]+/[0-9]+)").matcher(fragmentParams);
            Assert.assertTrue("ID param missing in fragment", fileMatcher.find());
            FileID fileID = new FileID(file.getId());
            fileID.setFolderId("10");
            Assert.assertEquals(fileID.toUniqueID(), fileMatcher.group(1));
        } finally {
            shareClient.logout();
        }
    }

    @Test
    @TryAgain
    public void testInternalFileShareLinkOnSubsequentShare() throws Exception {
        AJAXClient shareClient = client2;
        try {
            FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder());
            File file = insertFile(folder.getObjectID());
            file.setObjectPermissions(Collections.singletonList(new DefaultFileStorageObjectPermission(shareClient.getValues().getUserId(), false, FileStorageObjectPermission.READ)));
            updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });

            String invitationLink = discoverInvitationLink(apiClient2, shareClient.getValues().getDefaultAddress());
            Assert.assertNotNull("Invitation link not found", invitationLink);
            String fragmentParams = new URI(invitationLink).getRawFragment();
            Matcher folderMatcher = Pattern.compile("folder=([0-9]+)").matcher(fragmentParams);
            Assert.assertTrue("Folder param missing in fragment", folderMatcher.find());
            Assert.assertEquals("10", folderMatcher.group(1));
            Matcher fileMatcher = Pattern.compile("id=([0-9]+/[0-9]+)").matcher(fragmentParams);
            Assert.assertTrue("ID param missing in fragment", fileMatcher.find());
            FileID fileID = new FileID(file.getId());
            fileID.setFolderId("10");
            Assert.assertEquals(fileID.toUniqueID(), fileMatcher.group(1));
        } finally {
            shareClient.logout();
        }
    }
}
