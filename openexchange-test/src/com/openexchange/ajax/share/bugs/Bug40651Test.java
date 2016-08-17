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

package com.openexchange.ajax.share.bugs;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
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


/**
 * {@link Bug40651Test}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class Bug40651Test extends ShareTest {

    /**
     * Initializes a new {@link Bug40651Test}.
     * @param name
     */
    public Bug40651Test(String name) {
        super(name);
    }

    public void testShareFileLinkAndSearchForItAsGuest() throws Exception {
        FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, client.getValues().getPrivateInfostoreFolder());
        File file = insertFile(folder.getObjectID(), "Tests.zip");

        ShareTarget target = new ShareTarget(FolderObject.INFOSTORE, Integer.toString(folder.getObjectID()), file.getId());
        GetLinkRequest getLinkRequest = new GetLinkRequest(target, client.getValues().getTimeZone());
        GetLinkResponse getLinkResponse = client.execute(getLinkRequest);
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

    public void testShareFolderLinkAndSearchForContainedItemAsGuest() throws Exception {
        FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, client.getValues().getPrivateInfostoreFolder());
        File file = insertFile(folder.getObjectID(), "Tests.zip");

        ShareTarget target = new ShareTarget(FolderObject.INFOSTORE, Integer.toString(folder.getObjectID()));
        GetLinkRequest getLinkRequest = new GetLinkRequest(target, client.getValues().getTimeZone());
        GetLinkResponse getLinkResponse = client.execute(getLinkRequest);
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
