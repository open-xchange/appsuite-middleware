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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.share.Abstract2UserShareTest;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.find.Module;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.drive.DriveFacetType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.SimpleFacet;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.share.recipient.GuestRecipient;

/**
 * [Desktop/Win10] Drive: Searching engine does not work/show results
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class Bug40561Test extends Abstract2UserShareTest {

    @Test
    public void testShareFileAndSearchForItAsGuest() throws Exception {
        FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder());
        File file = insertFile(folder.getObjectID(), "Tests.zip");

        OCLGuestPermission guestPermission = createNamedAuthorPermission(false);
        String guestEmailAddress = ((GuestRecipient) guestPermission.getRecipient()).getEmailAddress();
        FileStorageGuestObjectPermission guestObjectPermission = asObjectPermission(guestPermission);
        file.setObjectPermissions(Collections.<FileStorageObjectPermission> singletonList(guestObjectPermission));
        file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });

        String sharedFolderID = "10";
        FileID tmp = new FileID(file.getId());
        tmp.setFolderId(sharedFolderID);
        String sharedFileID = tmp.toUniqueID();

        GuestClient guestClient = resolveShare(discoverInvitationLink(guestPermission.getApiClient(), guestEmailAddress));
        guestClient.checkFileAccessible(sharedFolderID, sharedFileID, guestObjectPermission);

        List<Facet> facets = AbstractFindTest.autocomplete(guestClient, Module.DRIVE, "tests");
        List<ActiveFacet> activeFacets = new ArrayList<>(2);
        activeFacets.add(AbstractFindTest.createActiveFolderFacet(sharedFolderID));
        activeFacets.add(AbstractFindTest.createActiveFacet((SimpleFacet) AbstractFindTest.findByType(DriveFacetType.FILE_NAME, facets)));
        List<PropDocument> searchResults = AbstractFindTest.query(guestClient, Module.DRIVE, activeFacets);
        Assert.assertNotNull("Unable to find document by property", AbstractFindTest.findByProperty(searchResults, "id", sharedFileID));
    }

    @Test
    public void testShareFileInternallyAndSearchForIt() throws Exception {
        AJAXClient shareClient = client2;
        try {
            FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder());
            DefaultFileStorageObjectPermission sharePermission = new DefaultFileStorageObjectPermission(shareClient.getValues().getUserId(), false, FileStorageObjectPermission.READ);
            File file = insertSharedFile(folder.getObjectID(), "Tests.zip", sharePermission);

            String sharedFolderID = "10";
            FileID tmp = new FileID(file.getId());
            tmp.setFolderId(sharedFolderID);
            String sharedFileID = tmp.toUniqueID();

            List<Facet> facets = AbstractFindTest.autocomplete(shareClient, Module.DRIVE, "tests");
            List<ActiveFacet> activeFacets = new ArrayList<>(2);
            activeFacets.add(AbstractFindTest.createActiveFolderFacet(sharedFolderID));
            activeFacets.add(AbstractFindTest.createActiveFacet((SimpleFacet) AbstractFindTest.findByType(CommonFacetType.GLOBAL, facets)));
            List<PropDocument> searchResults = AbstractFindTest.query(shareClient, Module.DRIVE, activeFacets);
            Assert.assertNotNull("Unable to find document by property", AbstractFindTest.findByProperty(searchResults, "id", sharedFileID));
        } finally {
            shareClient.logout();
        }
    }
}
