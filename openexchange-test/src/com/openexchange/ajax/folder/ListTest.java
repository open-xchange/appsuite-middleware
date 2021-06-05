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

package com.openexchange.ajax.folder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Iterator;
import org.json.JSONArray;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.Modules;
import com.openexchange.ajax.framework.Abstrac2UserAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link ListTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ListTest extends Abstrac2UserAJAXSession {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ListTest.class);

    /**
     * Initializes a new {@link ListTest}.
     *
     * @param name name of the test.
     */
    public ListTest() {
        super();
    }

    @Test
    public void testListRoot() throws Throwable {
        // List root's subfolders
        ListRequest request = new ListRequest(EnumAPI.OX_NEW, Integer.toString(FolderObject.SYSTEM_ROOT_FOLDER_ID), new int[] { FolderObject.OBJECT_ID, FolderObject.SUBFOLDERS }, true);
        ListResponse response = client1.execute(request);

        boolean privateFolder = false;
        boolean publicFolder = false;
        boolean sharedFolder = false;
        boolean infostoreFolder = false;
        final JSONArray jsonArray = (JSONArray) response.getResponse().getData();
        final int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            final JSONArray jsonSubArray = jsonArray.getJSONArray(i);
            int folderId = jsonSubArray.getInt(0);
            if (folderId == FolderObject.SYSTEM_PRIVATE_FOLDER_ID) {
                assertTrue("Subfolders expected below private folder.", jsonSubArray.getBoolean(1));
                privateFolder = true;
            } else if (folderId == FolderObject.SYSTEM_PUBLIC_FOLDER_ID) {
                assertTrue("Subfolders expected below public folder.", jsonSubArray.getBoolean(1));
                publicFolder = true;
            } else if (folderId == FolderObject.SYSTEM_SHARED_FOLDER_ID) {
                sharedFolder = true;
            } else if (folderId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
                assertTrue("Subfolders expected below infostore folder.", jsonSubArray.getBoolean(1));
                infostoreFolder = true;
            }
        }

        assertTrue("Private folder not found", privateFolder);
        assertTrue("Public folder not found", publicFolder);
        assertTrue("Shared folder not found", sharedFolder);
        assertTrue("Infostore folder not found", infostoreFolder);

        request = new ListRequest(EnumAPI.OX_NEW, String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID));
        response = client1.execute(request);
        Iterator<FolderObject> iter = response.getFolder();
        FolderObject defaultIMAPFolder = null;
        String primaryMailFolder = MailFolderUtility.prepareFullname(0, MailFolder.ROOT_FOLDER_ID);
        while (iter.hasNext()) {
            final FolderObject fo = iter.next();
            if (fo.containsFullName() && primaryMailFolder.equals(fo.getFullName())) {
                defaultIMAPFolder = fo;
                break;
            }
        }
        assertNotNull("Default email folder not found.", defaultIMAPFolder);
        boolean subFolders = defaultIMAPFolder.hasSubfolders();
        assertTrue("Default email folder has no subfolders.", subFolders);
        request = new ListRequest(EnumAPI.OX_NEW, defaultIMAPFolder.getFullName());
        response = client1.execute(request);
        iter = response.getFolder();
        FolderObject inboxFolder = null;
        while (iter.hasNext()) {
            final FolderObject fo = iter.next();
            if (fo.getFullName().endsWith("INBOX")) {
                inboxFolder = fo;
                break;
            }
        }
        assertNotNull("Inbox folder for default mail account not found.", inboxFolder);
        GetRequest request2 = new GetRequest(EnumAPI.OX_NEW, inboxFolder.getFullName(), new int[] { FolderObject.OBJECT_ID, FolderObject.FOLDER_NAME, FolderObject.OWN_RIGHTS, FolderObject.PERMISSIONS_BITS });
        GetResponse response2 = client1.execute(request2);
        assertFalse("Get failed.", response2.hasError());
    }

    @Test
    public void testListPrivate() throws Throwable {
        // List root's subfolders
        final ListRequest request = new ListRequest(EnumAPI.OX_NEW, String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID));
        final ListResponse response = client1.execute(request);

        final JSONArray jsonArray = (JSONArray) response.getResponse().getData();
        final int length = jsonArray.length();
        assertTrue("Subfolders expected below private folder.", length > 0);
    }

    @Test
    public void testListPrivateWithModules() throws Throwable {
        // List root's subfolders by their type
        final ListRequest request = new ListRequest(EnumAPI.OX_NEW, String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID), new Modules[] { Modules.MAIL });
        final ListResponse response = client1.execute(request);
        final JSONArray jsonArray = (JSONArray) response.getResponse().getData();
        final int length = jsonArray.length();
        assertTrue("Subfolders expected below private folder.", length > 0);
        Iterator<FolderObject> iter = response.getFolder();
        while (iter.hasNext()) {
            FolderObject folder = iter.next();
            LOG.info(folder.getFolderName() + ':' + folder.getFullName());
        }
    }

    @Test
    public void testListPublic() throws Throwable {
        // List root's subfolders
        final ListRequest request = new ListRequest(EnumAPI.OX_NEW, String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID));
        final ListResponse response = client1.execute(request);

        final JSONArray jsonArray = (JSONArray) response.getResponse().getData();
        final int length = jsonArray.length();
        assertTrue("Subfolders expected below public folder.", length > 0);
    }

    @Test
    public void testListShared() throws Throwable {
        client1.execute(new ListRequest(EnumAPI.OX_NEW, FolderObject.SYSTEM_ROOT_FOLDER_ID));
        client1.execute(new ListRequest(EnumAPI.OX_NEW, FolderObject.SYSTEM_SHARED_FOLDER_ID));
        int folderId = client2.getValues().getPrivateAppointmentFolder();
        int userId = client1.getValues().getUserId();
        FolderTools.shareFolder(client2, EnumAPI.OX_NEW, folderId, userId, OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        // List root's subfolders
        ListRequest request1 = new ListRequest(EnumAPI.OX_NEW, FolderObject.SYSTEM_SHARED_FOLDER_ID);
        ListResponse response = client1.execute(request1);

        String expectedId = FolderObject.SHARED_PREFIX + client2.getValues().getUserId();
        Iterator<FolderObject> iter = response.getFolder();
        FolderObject foundUserShared = null;
        while (iter.hasNext()) {
            FolderObject folder = iter.next();
            if (expectedId.equals(folder.getFullName())) {
                foundUserShared = folder;
            }
        }
        assertNotNull("Expected user named shared folder below root shared folder.", foundUserShared);

        ListRequest request2 = new ListRequest(EnumAPI.OX_NEW, foundUserShared.getFullName());
        response = client1.execute(request2);
        iter = response.getFolder();
        FolderObject foundShared = null;
        while (iter.hasNext()) {
            FolderObject folder = iter.next();
            if (folderId == folder.getObjectID()) {
                foundShared = folder;
            }
        }
        assertNotNull("Shared folder expected below shared parent folder.", foundShared);

        FolderTools.unshareFolder(client2, EnumAPI.OX_NEW, folderId, userId);
    }
}
