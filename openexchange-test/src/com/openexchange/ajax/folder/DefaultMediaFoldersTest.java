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

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GenJSONRequest;
import com.openexchange.ajax.folder.actions.GenJSONResponse;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;

/**
 * {@link DefaultMediaFoldersTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8
 */
public class DefaultMediaFoldersTest extends AbstractAJAXSession {

    private final int[] folders;
    private final int[] types;

    /**
     * Initializes a new {@link DefaultMediaFoldersTest}.
     */
    public DefaultMediaFoldersTest(String name) {
        super(name);
        folders = new int[4];
        types = new int[4];
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ListRequest listRequest = new ListRequest(EnumAPI.OX_NEW, String.valueOf(client.getValues().getPrivateInfostoreFolder()), new int[] { FolderObject.OBJECT_ID, FolderObject.FOLDER_NAME, FolderObject.OWN_RIGHTS, FolderObject.PERMISSIONS_BITS,
            FolderObject.TYPE, FolderObject.STANDARD_FOLDER }, true);
        ListResponse listResponse = client.execute(listRequest);
        Iterator<FolderObject> it = listResponse.getFolder();
        while (it.hasNext()) {
            FolderObject folder = it.next();
            if (folder.getType() == FolderObject.PICTURES) {
                assertEquals("Wrong folder name.", FolderStrings.SYSTEM_USER_PICTURES_FOLDER_NAME, folder.getFolderName());
                assertTrue("Folder is not a default folder.", folder.isDefaultFolder());
                folders[0] = folder.getObjectID();
                types[0] = FolderObject.PICTURES;
            }
            if (folder.getType() == FolderObject.DOCUMENTS) {
                assertEquals("Wrong folder name.", FolderStrings.SYSTEM_USER_DOCUMENTS_FOLDER_NAME, folder.getFolderName());
                assertTrue("Folder is not a default folder.", folder.isDefaultFolder());
                folders[1] = folder.getObjectID();
                types[1] = FolderObject.DOCUMENTS;
            }
            if (folder.getType() == FolderObject.MUSIC) {
                assertEquals("Wrong folder name.", FolderStrings.SYSTEM_USER_MUSIC_FOLDER_NAME, folder.getFolderName());
                assertTrue("Folder is not a default folder.", folder.isDefaultFolder());
                folders[2] = folder.getObjectID();
                types[2] = FolderObject.MUSIC;
            }
            if (folder.getType() == FolderObject.VIDEOS) {
                assertEquals("Wrong folder name.", FolderStrings.SYSTEM_USER_VIDEOS_FOLDER_NAME, folder.getFolderName());
                assertTrue("Folder is not a default folder.", folder.isDefaultFolder());
                folders[3] = folder.getObjectID();
                types[3] = FolderObject.VIDEOS;
            }
//            if (folder.getType() == FolderObject.TEMPLATES) {
//                assertEquals("Wrong folder name.", FolderStrings.SYSTEM_USER_TEMPLATES_FOLDER_NAME, folder.getFolderName());
//                assertTrue("Folder is not a default folder.", folder.isDefaultFolder());
//                folders[4] = folder.getObjectID();
//                types[4] = FolderObject.TEMPLATES;
//            }
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDelete() throws Exception {
        for (int folderId : folders) {
            DeleteRequest deleteRequest = new DeleteRequest(EnumAPI.OX_NEW, folderId, new Date());
            deleteRequest.setHardDelete(true);
            CommonDeleteResponse deleteResponse = client.execute(deleteRequest);
            JSONArray json = (JSONArray) deleteResponse.getData();
            assertEquals("Wrong array size in response.", 1, json.length());
            assertEquals("Wrong folder Id in response.", folderId, json.getInt(0));
            GetRequest getRequest = new GetRequest(EnumAPI.OX_NEW, folderId);
            GetResponse getResponse = client.execute(getRequest);
            assertFalse("Folder " + folderId + " was deleted.", getResponse.hasError());
        }
    }

    public void testRename() throws Exception {
        for (int folderId : folders) {
            GetRequest getRequest = new GetRequest(EnumAPI.OX_NEW, folderId);
            GetResponse getResponse = client.execute(getRequest);
            if (getResponse.hasError()) {
                fail(getResponse.getErrorMessage());
            }
            FolderObject folder = getResponse.getFolder();
            folder.setFolderName("renamed");
            folder.setLastModified(new Date());
            UpdateRequest updateRequest = new UpdateRequest(EnumAPI.OX_NEW, folder, false);
            InsertResponse updateResponse = client.execute(updateRequest);
            assertTrue("Media folder " + folderId + " renamed.", updateResponse.hasError());
            OXException e = updateResponse.getException();
            assertEquals(OXFolderExceptionCode.NO_DEFAULT_FOLDER_RENAME.getNumber(), e.getCode());
        }
    }

    public void testMove() throws Exception {
        int random = new Random(System.currentTimeMillis()).nextInt(4);
        GetRequest getRequest = new GetRequest(EnumAPI.OX_NEW, folders[random]);
        GetResponse getResponse = client.execute(getRequest);
        if (getResponse.hasError()) {
            fail(getResponse.getErrorMessage());
        }
        FolderObject folder = getResponse.getFolder();
        GenJSONRequest updateRequest = new GenJSONRequest(EnumAPI.OX_NEW, false);
        updateRequest.setJSONValue(new JSONObject("{\"folder_id\":\"" + folders[(random + 1) % 4] + "\"}"));
        updateRequest.setParameter("action", "update");
        updateRequest.setParameter("id", String.valueOf(folder.getObjectID()));
        GenJSONResponse updateResponse = client.execute(updateRequest);
        assertTrue("Media folder moved.", updateResponse.hasError());
        OXException e = updateResponse.getException();
        assertEquals(OXFolderExceptionCode.NO_DEFAULT_FOLDER_MOVE.getNumber(), e.getCode());
    }

    public void testSubfoldersInheritType() throws Exception {
        int random = new Random(System.currentTimeMillis()).nextInt(4);
        FolderObject folder = new FolderObject();
        folder.setFolderName("subfolder");
        folder.setParentFolderID(folders[random]);
        folder.setModule(FolderObject.INFOSTORE);
        OCLPermission perm = new OCLPermission(client.getValues().getUserId(), folders[random]);
        perm.setFolderAdmin(true);
        folder.setPermissions(Collections.singletonList(perm));
        int subfolderId = -1;
        try {
            InsertRequest insertRequest = new InsertRequest(EnumAPI.OX_NEW, folder);
            InsertResponse insertResponse = client.execute(insertRequest);
            insertResponse.fillObject(folder);
            folder.setLastModified(new Date());
            subfolderId = folder.getObjectID();
            GetRequest getRequest = new GetRequest(EnumAPI.OX_NEW, subfolderId, new int[] { FolderObject.TYPE });
            GetResponse getResponse = client.execute(getRequest);
            folder = getResponse.getFolder();
            assertEquals("Folder type is not correct.", types[random], folder.getType());
        } finally {
            if (subfolderId > 0) {
                folder.setLastModified(new Date());
                DeleteRequest deleteRequest = new DeleteRequest(EnumAPI.OX_NEW, false, folder);
                deleteRequest.setHardDelete(true);
                client.execute(deleteRequest);
            }
        }
    }

    public void testSubfolderChangeTypeOnMove() throws Exception {
        int random = new Random(System.currentTimeMillis()).nextInt(4);
        FolderObject folder = new FolderObject();
        folder.setFolderName("subfolder");
        folder.setParentFolderID(folders[random]);
        folder.setModule(FolderObject.INFOSTORE);
        OCLPermission perm = new OCLPermission(client.getValues().getUserId(), folders[random]);
        perm.setFolderAdmin(true);
        folder.setPermissions(Collections.singletonList(perm));
        int subfolderId = -1;
        try {
            InsertRequest insertRequest = new InsertRequest(EnumAPI.OX_NEW, folder);
            InsertResponse insertResponse = client.execute(insertRequest);
            insertResponse.fillObject(folder);
            subfolderId = folder.getObjectID();
            GetRequest getRequest = new GetRequest(EnumAPI.OX_NEW, subfolderId);
            GetResponse getResponse = client.execute(getRequest);
            folder = getResponse.getFolder();
            folder.setLastModified(new Date());
            GenJSONRequest updateRequest = new GenJSONRequest(EnumAPI.OX_NEW, false);
            updateRequest.setJSONValue(new JSONObject("{\"folder_id\":\"" + folders[(random + 1) % 4] + "\"}"));
            updateRequest.setParameter("action", "update");
            updateRequest.setParameter("id", String.valueOf(subfolderId));
            GenJSONResponse updateResponse = client.execute(updateRequest);
            assertFalse("Move subfolder failed.", updateResponse.hasError());
            getRequest = new GetRequest(EnumAPI.OX_NEW, subfolderId);
            getResponse = client.execute(getRequest);
            folder = getResponse.getFolder();
            folder.setLastModified(new Date());
            assertEquals("Folder type not changed when moved.", types[(random + 1) % 4], folder.getType());
        } finally {
            if (subfolderId > 0) {
                DeleteRequest deleteRequest = new DeleteRequest(EnumAPI.OX_NEW, false, folder);
                deleteRequest.setHardDelete(true);
                client.execute(deleteRequest);
            }
        }
    }

}
