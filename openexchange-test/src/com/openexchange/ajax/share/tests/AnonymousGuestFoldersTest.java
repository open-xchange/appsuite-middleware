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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;
import com.google.common.collect.Lists;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.folder.actions.PathRequest;
import com.openexchange.ajax.folder.actions.PathResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.ajax.share.actions.GetLinkResponse;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.share.ShareTarget;

/**
 * {@link AnonymousGuestFoldersTest}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.1
 */
public class AnonymousGuestFoldersTest extends ShareTest {
    
    private GuestClient guestClient;
    private FolderObject share;
    private FolderObject root;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        /*
         * Create the following folder structure:
         * - root
         *  - invisible
         *    - share
         */
        root = insertPublicFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE);
        root.setDefaultFolder(true);
        root.setType(PublicType.getInstance().getType());
        updateFolder(EnumAPI.OX_NEW, root);
        FolderObject invisible = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, root.getObjectID(), "invisible" + randomUID());
        share = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, invisible.getObjectID(), "share" + randomUID());
        
        /*
         * Get a link for the new drive subfolder
         */
        ShareTarget target = new ShareTarget(FolderObject.INFOSTORE, Integer.toString(share.getObjectID()));
        GetLinkRequest getLinkRequest = new GetLinkRequest(target, getClient().getValues().getTimeZone());
        GetLinkResponse getLinkResponse = getClient().execute(getLinkRequest);
        String url = getLinkResponse.getShareLink().getShareURL();
        /*
         * Resolve the link and check read permission for folder
         */
        guestClient = resolveShare(url, null, null);
        OCLGuestPermission expectedPermission = createAnonymousGuestPermission();
        expectedPermission.setEntity(guestClient.getValues().getUserId());
        guestClient.checkFolderAccessible(Integer.toString(share.getObjectID()), expectedPermission);

    }
    
    /**
     * Checks if the full name of the shared folder is set to the root folder id
     */
    @Test
    public void testGetSharedSubfolder_correctParent() throws Exception {
                
        final GetRequest request = new GetRequest(EnumAPI.OX_NEW, share.getObjectID(), true);
        final GetResponse response = guestClient.execute(request);
        FolderObject folder = response.getFolder();
        assertEquals("Shared folder parent is not public folder id", folder.getFullName(), String.valueOf(root.getParentFolderID()));
    }
    
    /**
     * Checks if a list command on the root folder hides the invisible folder and only shows
     * the shared folder. The parent of the shared folder has also be adjusted to to the root folder
     */
    @Test
    public void testListSharedRootFolder_correctParentChilds() throws Exception {
        ListResponse listResponse = guestClient.execute(new ListRequest(EnumAPI.OX_NEW, String.valueOf(root.getObjectID()), new int[] { 1, 20, 2, 300, 301, 302, 304, 306, 308 }, false));
        List<FolderObject> folderList = Lists.newArrayList(listResponse.getFolder());
        assertTrue("Unexpected amount of folders in ListResponse", folderList.size() == 1);
        assertEquals("List on root folder should return only shared folder", folderList.get(0).getObjectID(), share.getObjectID());
        assertEquals("Shared folder parent is not public folder id", folderList.get(0).getParentFolderID(), root.getObjectID());

    }
    /**
     * Checks if a path request with the shared folder id correctly returns only three folders, hiding
     * the invisible folder. Parent of the shared folder has to be the root folders parent and not the invisible.
     */
    @Test
    public void testPathSharedFolder_correctPath() throws Exception {
        PathResponse pathResponse = guestClient.execute(new PathRequest(EnumAPI.OX_NEW, String.valueOf(share.getObjectID()), new int[] { 1, 20, 2, 300, 301, 302, 304, 306, 308 }, false));
        List<FolderObject> folderList = Lists.newArrayList(pathResponse.getFolder());
        assertTrue("The amount of folders in the path response is wrong", folderList.size() == 3);
        FolderObject[] sharedFolder = folderList.stream().filter(f -> f.getObjectID() == share.getObjectID()).toArray(FolderObject[]::new);
        assertTrue("Shared folder is missing in path", sharedFolder[0] != null);
        assertTrue("Share folder in path has the wrong parent", sharedFolder[0].getParentFolderID() == root.getParentFolderID());
    }
}
