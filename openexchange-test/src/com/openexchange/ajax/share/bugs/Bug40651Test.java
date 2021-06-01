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
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.ajax.share.actions.GetLinkResponse;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.find.Module;
import com.openexchange.find.drive.DriveFacetType;
import com.openexchange.find.facet.ActiveFacet;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.SimpleFacet;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.share.ShareTarget;
import com.openexchange.test.tryagain.TryAgain;

/**
 * {@link Bug40651Test}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class Bug40651Test extends ShareTest {

    /**
     * Initializes a new {@link Bug40651Test}.
     *
     * @param name
     */
    public Bug40651Test() {
        super();
    }

    @Test
    @TryAgain
    public void testShareFileLinkAndSearchForItAsGuest() throws Exception {
        FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder());
        File file = insertFile(folder.getObjectID(), "Tests.zip");

        ShareTarget target = new ShareTarget(FolderObject.INFOSTORE, Integer.toString(folder.getObjectID()), file.getId());
        GetLinkRequest getLinkRequest = new GetLinkRequest(target, getClient().getValues().getTimeZone());
        GetLinkResponse getLinkResponse = getClient().execute(getLinkRequest);
        String url = getLinkResponse.getShareLink().getShareURL();

        GuestClient guestClient = resolveShare(url);
        List<Facet> facets = AbstractFindTest.autocomplete(guestClient, Module.DRIVE, "tests");
        List<ActiveFacet> activeFacets = new ArrayList<>(2);
        activeFacets.add(AbstractFindTest.createActiveFolderFacet("10"));
        activeFacets.add(AbstractFindTest.createActiveFacet((SimpleFacet) AbstractFindTest.findByType(DriveFacetType.FILE_NAME, facets)));
        List<PropDocument> searchResults = AbstractFindTest.query(guestClient, Module.DRIVE, activeFacets);
        FileID expectedId = new FileID(file.getId());
        expectedId.setFolderId("10");
        PropDocument expectedDoc = AbstractFindTest.findByProperty(searchResults, "id", expectedId.toUniqueID());
        Assert.assertNotNull("Found no document with ID " + expectedId.toUniqueID(), expectedDoc);
    }

    @Test
    @TryAgain
    public void testShareFolderLinkAndSearchForContainedItemAsGuest() throws Exception {
        FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder());
        File file = insertFile(folder.getObjectID(), "Tests.zip");

        ShareTarget target = new ShareTarget(FolderObject.INFOSTORE, Integer.toString(folder.getObjectID()));
        GetLinkRequest getLinkRequest = new GetLinkRequest(target, getClient().getValues().getTimeZone());
        GetLinkResponse getLinkResponse = getClient().execute(getLinkRequest);
        String url = getLinkResponse.getShareLink().getShareURL();

        GuestClient guestClient = resolveShare(url);
        List<Facet> facets = AbstractFindTest.autocomplete(guestClient, Module.DRIVE, "tests");
        List<ActiveFacet> activeFacets = new ArrayList<>(2);
        activeFacets.add(AbstractFindTest.createActiveFolderFacet("10"));
        activeFacets.add(AbstractFindTest.createActiveFacet((SimpleFacet) AbstractFindTest.findByType(DriveFacetType.FILE_NAME, facets)));
        List<PropDocument> searchResults = AbstractFindTest.query(guestClient, Module.DRIVE, activeFacets);
        PropDocument expectedDoc = AbstractFindTest.findByProperty(searchResults, "id", file.getId());
        Assert.assertNotNull("Found no document with ID " + file.getId(), expectedDoc);
    }

}
