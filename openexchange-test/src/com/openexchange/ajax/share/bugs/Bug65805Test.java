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
import com.openexchange.ajax.share.Abstract2UserShareTest;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.tryagain.TryAgain;

/**
 * {@link Bug65805Test}
 *
 * Folder API reveals share_url to other users
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class Bug65805Test extends Abstract2UserShareTest {

    @Test
    @TryAgain
    public void testShareLinkInSharedContactsFolder() throws Exception {
        testShareLinkInSharedFolder(EnumAPI.OX_NEW, FolderObject.CONTACT);
    }

    @Test
    @TryAgain
    public void testShareLinkInSharedTasksFolder() throws Exception {
        testShareLinkInSharedFolder(EnumAPI.OX_NEW, FolderObject.TASK);
    }

    @Test
    @TryAgain
    public void testShareLinkInSharedInfostoreFolder() throws Exception {
        testShareLinkInSharedFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE);
    }

    @Test
    @TryAgain
    public void testShareLinkInSharedCalendarFolder() throws Exception {
        testShareLinkInSharedFolder(EnumAPI.OX_NEW, FolderObject.CALENDAR);
    }

    private void testShareLinkInSharedFolder(EnumAPI api, int module) throws Exception {
        /*
         * create folder, shared to user b and anonymous guest (share link)
         */
        List<OCLPermission> permissions = new ArrayList<OCLPermission>();
        permissions.add(createAnonymousGuestPermission());
        OCLPermission userPermission = new OCLPermission(client2.getValues().getUserId(), 0);
        userPermission.setAllPermission(OCLPermission.READ_ALL_OBJECTS, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        permissions.add(userPermission);
        FolderObject folder = insertSharedFolder(api, module, getDefaultFolder(module), randomUID(), permissions.toArray(new OCLPermission[permissions.size()]));
        /*
         * check that the share url is only visible to user a
         */
        assertNotNull("Share URL not visible", getShareURL(getClient(), api, folder.getObjectID()));
        assertNull("Share URL is visible", getShareURL(client2, api, folder.getObjectID()));
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
