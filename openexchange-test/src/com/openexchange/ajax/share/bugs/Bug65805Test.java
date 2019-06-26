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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link Bug65805Test}
 *
 * Folder API reveals share_url to other users
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class Bug65805Test extends ShareTest {

    @Test
    public void testShareLinkInSharedContactsFolder() throws Exception {
        testShareLinkInSharedFolder(EnumAPI.OX_NEW, FolderObject.CONTACT);
    }

    @Test
    public void testShareLinkInSharedTasksFolder() throws Exception {
        testShareLinkInSharedFolder(EnumAPI.OX_NEW, FolderObject.TASK);
    }

    @Test
    public void testShareLinkInSharedInfostoreFolder() throws Exception {
        testShareLinkInSharedFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE);
    }

    @Test
    public void testShareLinkInSharedCalendarFolder() throws Exception {
        testShareLinkInSharedFolder(EnumAPI.OX_NEW, FolderObject.CALENDAR);
    }

    private void testShareLinkInSharedFolder(EnumAPI api, int module) throws Exception {
        /*
         * create folder, shared to user b and anonymous guest (share link)  
         */
        List<OCLPermission> permissions = new ArrayList<OCLPermission>();
        permissions.add(createAnonymousGuestPermission());
        OCLPermission userPermission = new OCLPermission(getClient2().getValues().getUserId(), 0);
        userPermission.setAllPermission(OCLPermission.READ_ALL_OBJECTS, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        permissions.add(userPermission);
        FolderObject folder = insertSharedFolder(api, module, getDefaultFolder(module), randomUID(), permissions.toArray(new OCLPermission[permissions.size()]));
        /*
         * check that the share url is only visible to user a
         */
        assertNotNull("Share URL not visible", getShareURL(getClient(), api, folder.getObjectID()));
        assertNull("Share URL is visible", getShareURL(getClient2(), api, folder.getObjectID()));
    }

    private static String getShareURL(AJAXClient client, EnumAPI api, int folderId) throws Exception {
        GetResponse getResponse = client.execute(new GetRequest(api, folderId));
        assertFalse(getResponse.getErrorMessage(), getResponse.hasError());
        JSONObject folderData = (JSONObject) getResponse.getData();
        JSONArray extendedPermissionArray = folderData.getJSONArray("com.openexchange.share.extendedPermissions");
        assertNotNull(extendedPermissionArray);
        for (int i = 0; i < extendedPermissionArray.length(); i++) {
            String shareUrl = extendedPermissionArray.getJSONObject(i).optString("share_url", null);
            if (null != shareUrl) {
                return shareUrl;
            }
        }
        return null;
    }

}
