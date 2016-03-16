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
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import com.openexchange.ajax.find.AbstractFindTest;
import com.openexchange.ajax.find.PropDocument;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
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

/**
 * [Desktop/Win10] Drive: Searching engine does not work/show results
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class Bug40561Test extends ShareTest {

    public Bug40561Test(String name) {
        super(name);
    }

    public void testShareFileAndSearchForItAsGuest() throws Exception {
        FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, client.getValues().getPrivateInfostoreFolder());
        File file = insertFile(folder.getObjectID(), "Tests.zip");

        String guestEmailAddress = randomUID() + "@example.com";
        FileStorageGuestObjectPermission guestPermission = asObjectPermission(createNamedAuthorPermission(guestEmailAddress, randomUID()));
        file.setObjectPermissions(Collections.<FileStorageObjectPermission> singletonList(guestPermission));
        file = updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });

        String sharedFolderID = "10";
        FileID tmp = new FileID(file.getId());
        tmp.setFolderId(sharedFolderID);
        String sharedFileID = tmp.toUniqueID();

        GuestClient guestClient = resolveShare(discoverInvitationLink(client, guestEmailAddress));
        guestClient.checkFileAccessible(sharedFolderID, sharedFileID, guestPermission);

        List<Facet> facets = AbstractFindTest.autocomplete(guestClient, Module.DRIVE, "tests");
        List<ActiveFacet> activeFacets = new ArrayList<>(2);
        activeFacets.add(AbstractFindTest.createActiveFolderFacet(sharedFolderID));
        activeFacets.add(AbstractFindTest.createActiveFacet((SimpleFacet) AbstractFindTest.findByType(DriveFacetType.FILE_NAME, facets)));
        List<PropDocument> searchResults = AbstractFindTest.query(guestClient, Module.DRIVE, activeFacets);
        Assert.assertNotNull(AbstractFindTest.findByProperty(searchResults, "id", sharedFileID));
    }

    public void testShareFileInternallyAndSearchForIt() throws Exception {
        AJAXClient shareClient = new AJAXClient(User.User2);
        try {
            FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, client.getValues().getPrivateInfostoreFolder());
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
            Assert.assertNotNull(AbstractFindTest.findByProperty(searchResults, "id", sharedFileID));
        } finally {
            shareClient.logout();
        }
	}

}
