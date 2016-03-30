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

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetInfostoreResponse;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link Bug41184Test}
 *
 * Unable to open a shared document, if its folder was previously shared to the same entity
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug41184Test extends ShareTest {

    private AJAXClient client2;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client2 = new AJAXClient(User.User2);
    }

    @Override
    protected void tearDown() throws Exception {
        if (null != client2) {
            client2.logout();
        }
        super.tearDown();
    }


    /**
     * Initializes a new {@link Bug41184Test}.
     *
     * @param name The test name
     */
    public Bug41184Test(String name) {
        super(name);
    }

    public void testAccessSharedFileInSharedFolder() throws Exception {
        /*
         * create folder shared to other user
         */
        int userId = client2.getValues().getUserId();
        OCLPermission permission = new OCLPermission(userId, 0);
        permission.setAllPermission(OCLPermission.READ_ALL_OBJECTS, OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        FolderObject folder = insertSharedFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(FolderObject.INFOSTORE), permission);
        /*
         * fetch & check internal link from notification mail
         */
        String folderLink = discoverInvitationLink(client, client2.getValues().getDefaultAddress());
        Assert.assertNotNull("Invitation link not found", folderLink);
        String fragmentParams = new URI(folderLink).getRawFragment();
        Matcher folderMatcher = Pattern.compile("folder=([0-9]+)").matcher(fragmentParams);
        Assert.assertTrue("Folder param missing in fragment", folderMatcher.find());
        Assert.assertEquals(String.valueOf(folder.getObjectID()), folderMatcher.group(1));
        /*
         * create file in this folder share it to the same user, too
         */
        DefaultFileStorageObjectPermission objectPermission = new DefaultFileStorageObjectPermission(userId, false, FileStorageObjectPermission.READ);
        File file = insertSharedFile(folder.getObjectID(), objectPermission);
        /*
         * fetch & check internal link from notification mail
         */
        String fileLink = discoverInvitationLink(client, client2.getValues().getDefaultAddress());
        Assert.assertNotNull("Invitation link not found", fileLink);
        fragmentParams = new URI(fileLink).getRawFragment();
        folderMatcher = Pattern.compile("folder=([0-9]+)").matcher(fragmentParams);
        Assert.assertTrue("Folder param missing in fragment", folderMatcher.find());
        Assert.assertEquals("10", folderMatcher.group(1));
        Matcher fileMatcher = Pattern.compile("id=([0-9]+/[0-9]+)").matcher(fragmentParams);
        Assert.assertTrue("ID param missing in fragment", fileMatcher.find());
        FileID fileID = new FileID(file.getId());
        fileID.setFolderId("10");
        Assert.assertEquals(fileID.toUniqueID(), fileMatcher.group(1));
        /*
         * try and access the file in shared files folder
         */
        GetInfostoreRequest getRequest = new GetInfostoreRequest(fileMatcher.group(1));
        GetInfostoreResponse getResponse = client.execute(getRequest);
        File metadata = getResponse.getDocumentMetadata();
        assertNotNull(metadata);
        assertEquals(metadata.getId(), fileMatcher.group(1));
        assertEquals(metadata.getFolderId(), folderMatcher.group(1));
        /*
         * try and access the file in it's physical folder, too
         */
        getRequest = new GetInfostoreRequest(file.getId());
        getResponse = client.execute(getRequest);
        metadata = getResponse.getDocumentMetadata();
        assertNotNull(metadata);
        assertEquals(metadata.getId(), file.getId());
        assertEquals(metadata.getFolderId(), file.getFolderId());
    }

}
