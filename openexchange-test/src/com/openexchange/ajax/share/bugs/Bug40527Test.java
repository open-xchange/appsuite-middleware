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
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.groupware.container.FolderObject;
import edu.emory.mathcs.backport.java.util.Collections;


/**
 * Share links are broken - a link like https://ox.example.com/appsuite/ui#!&app=io.ox/files&folder=10&id=1234/9876
 * was generated. The item parameter is broken in terms of the included folder ID, "10/9876" would be the correct value.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class Bug40527Test extends ShareTest {

    public Bug40527Test(String name) {
        super(name);
    }

    public void testInternalFileShareLinkOnSharedCreation() throws Exception {
        AJAXClient shareClient = new AJAXClient(User.User2);
        try {
            FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, client.getValues().getPrivateInfostoreFolder());
            DefaultFileStorageObjectPermission sharePermission = new DefaultFileStorageObjectPermission(shareClient.getValues().getUserId(), false, FileStorageObjectPermission.READ);
            File file = insertSharedFile(folder.getObjectID(), randomUID(), sharePermission);
            String invitationLink = discoverInvitationLink(client, shareClient.getValues().getDefaultAddress());
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

    public void testInternalFileShareLinkOnSubsequentShare() throws Exception {
        AJAXClient shareClient = new AJAXClient(User.User2);
        try {
            FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, client.getValues().getPrivateInfostoreFolder());
            File file = insertFile(folder.getObjectID());
            file.setObjectPermissions(Collections.singletonList(new DefaultFileStorageObjectPermission(shareClient.getValues().getUserId(), false, FileStorageObjectPermission.READ)));
            updateFile(file, new Field[] { Field.OBJECT_PERMISSIONS });

            String invitationLink = discoverInvitationLink(client, shareClient.getValues().getDefaultAddress());
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
