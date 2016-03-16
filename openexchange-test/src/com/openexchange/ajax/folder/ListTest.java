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

package com.openexchange.ajax.folder;

import java.util.Iterator;
import org.json.JSONArray;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.Modules;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link ListTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ListTest extends AbstractAJAXSession {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ListTest.class);

    private AJAXClient client;
    private AJAXClient client2;

    /**
     * Initializes a new {@link ListTest}.
     *
     * @param name name of the test.
     */
    public ListTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = getClient();
        client2 = new AJAXClient(User.User2);
    }

    public void testListRoot() throws Throwable {
        // List root's subfolders
        ListRequest request = new ListRequest(EnumAPI.OX_NEW, Integer.toString(FolderObject.SYSTEM_ROOT_FOLDER_ID), new int[] { FolderObject.OBJECT_ID, FolderObject.SUBFOLDERS }, true);
        ListResponse response = client.execute(request);

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
        response = client.execute(request);
        Iterator<FolderObject> iter = response.getFolder();
        FolderObject defaultIMAPFolder = null;
        String primaryMailFolder = MailFolderUtility.prepareFullname(0, MailFolder.DEFAULT_FOLDER_ID);
        while (iter.hasNext()) {
            final FolderObject fo = iter.next();
            if (fo.containsFullName() && primaryMailFolder.equals(fo.getFullName())) {
                defaultIMAPFolder = fo;
                break;
            }
        }
        assertNotNull("Default email folder not found.", defaultIMAPFolder);
        @SuppressWarnings("null")
        boolean subFolders = defaultIMAPFolder.hasSubfolders();
        assertTrue("Default email folder has no subfolders.", subFolders);
        request = new ListRequest(EnumAPI.OX_NEW, defaultIMAPFolder.getFullName());
        response = client.execute(request);
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
        @SuppressWarnings("null")
        GetRequest request2 = new GetRequest(EnumAPI.OX_NEW, inboxFolder.getFullName(), new int[] {
            FolderObject.OBJECT_ID, FolderObject.FOLDER_NAME, FolderObject.OWN_RIGHTS, FolderObject.PERMISSIONS_BITS });
        GetResponse response2 = client.execute(request2);
        assertFalse("Get failed.", response2.hasError());
    }

    public void testListPrivate() throws Throwable {
        // List root's subfolders
        final ListRequest request = new ListRequest(EnumAPI.OX_NEW, String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID));
        final ListResponse response = client.execute(request);

        final JSONArray jsonArray = (JSONArray) response.getResponse().getData();
        final int length = jsonArray.length();
        assertTrue("Subfolders expected below private folder.", length > 0);
    }

    public void testListPrivateWithModules() throws Throwable {
        // List root's subfolders by their type
        final ListRequest request = new ListRequest(EnumAPI.OX_NEW, String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID), new Modules[] { Modules.MAIL });
        final ListResponse response = client.execute(request);
        final JSONArray jsonArray = (JSONArray) response.getResponse().getData();
        final int length = jsonArray.length();
        assertTrue("Subfolders expected below private folder.", length > 0);
        Iterator<FolderObject> iter = response.getFolder();
        while (iter.hasNext()) {
            FolderObject folder = iter.next();
            LOG.info(folder.getFolderName() + ':' + folder.getFullName());
        }
    }

    public void testListPublic() throws Throwable {
        // List root's subfolders
        final ListRequest request = new ListRequest(EnumAPI.OX_NEW, String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID));
        final ListResponse response = client.execute(request);

        final JSONArray jsonArray = (JSONArray) response.getResponse().getData();
        final int length = jsonArray.length();
        assertTrue("Subfolders expected below public folder.", length > 0);
    }

    public void testListShared() throws Throwable {
        client.execute(new ListRequest(EnumAPI.OX_NEW, FolderObject.SYSTEM_ROOT_FOLDER_ID));
        client.execute(new ListRequest(EnumAPI.OX_NEW, FolderObject.SYSTEM_SHARED_FOLDER_ID));
        int folderId = client2.getValues().getPrivateAppointmentFolder();
        int userId = client.getValues().getUserId();
        FolderTools.shareFolder(
            client2,
            EnumAPI.OX_NEW,
            folderId,
            userId,
            OCLPermission.READ_FOLDER,
            OCLPermission.READ_ALL_OBJECTS,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS);
        // List root's subfolders
        ListRequest request1 = new ListRequest(EnumAPI.OX_NEW, FolderObject.SYSTEM_SHARED_FOLDER_ID);
        ListResponse response = client.execute(request1);

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

        @SuppressWarnings("null")
        ListRequest request2 = new ListRequest(EnumAPI.OX_NEW, foundUserShared.getFullName());
        response = client.execute(request2);
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
