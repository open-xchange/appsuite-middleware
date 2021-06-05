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

package com.openexchange.ajax.infostore;

import static com.openexchange.java.Autoboxing.B;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.Abstrac2UserAJAXSession;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetInfostoreResponse;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.infostore.actions.ListInfostoreRequest;
import com.openexchange.ajax.infostore.actions.ListInfostoreRequest.ListItem;
import com.openexchange.ajax.infostore.actions.ListInfostoreResponse;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.util.UUIDs;
import com.openexchange.test.common.configuration.AJAXConfig;

/**
 * {@link InfostoreObjectPermissionTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class InfostoreObjectPermissionTest extends Abstrac2UserAJAXSession {

    @SuppressWarnings("hiding")
    private InfostoreTestManager itm;
    private Map<String, Boolean> shareStates;
    private Map<String, File> allFiles;
    private FolderObject testFolder;

    /**
     * Initializes a new {@link InfostoreObjectPermissionTest}.
     *
     * @param name
     */
    public InfostoreObjectPermissionTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String folderName = "InfostoreObjectPermissionTest_" + System.currentTimeMillis();
        testFolder = ftm.generatePrivateFolder(folderName, FolderObject.INFOSTORE, client1.getValues().getPrivateInfostoreFolder(), client1.getValues().getUserId());
        testFolder = ftm.insertFolderOnServer(testFolder);

        allFiles = new HashMap<String, File>();
        shareStates = new HashMap<String, Boolean>();

        itm = new InfostoreTestManager(client1);
        itm.setFailOnError(true);
        java.io.File upload = new java.io.File(AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR), "contact_image.png");
        Random r = new Random();
        for (int i = 0; i < 10; i++) {
            boolean shared = false;
            List<FileStorageObjectPermission> objectPermissions = null;
            if (r.nextBoolean()) {
                objectPermissions = Collections.<FileStorageObjectPermission> singletonList(new DefaultFileStorageObjectPermission(client2.getValues().getUserId(), false, FileStorageObjectPermission.READ));
                shared = true;
            }
            File newDocument = newDocument(testFolder.getObjectID(), objectPermissions);
            itm.newAction(newDocument, upload);
            allFiles.put(newDocument.getId(), newDocument);
            shareStates.put(newDocument.getId(), B(shared));
        }
    }

    @Test
    public void testReadPermission() throws Exception {
        /*
         * Must fail because of missing folder permissions
         */
        itm.setClient(client2);
        List<File> all = itm.getAll(testFolder.getObjectID(), Metadata.columns(Metadata.HTTPAPI_VALUES_ARRAY), Metadata.ID, Order.ASCENDING);
        OXException exception = itm.getLastResponse().getException();
        assertTrue("Expected exception: InfostoreExceptionCodes.NO_READ_PERMISSION", InfostoreExceptionCodes.NO_READ_PERMISSION.equals(exception));

        List<String> sharedFiles = new ArrayList<String>(10);
        List<String> otherFiles = new ArrayList<String>(10);
        for (Entry<String, Boolean> entry : shareStates.entrySet()) {
            if (entry.getValue().booleanValue()) {
                sharedFiles.add(entry.getKey());
            } else {
                otherFiles.add(entry.getKey());
            }
        }

        List<ListItem> listItems = new ArrayList<ListItem>(sharedFiles.size());
        for (String id : sharedFiles) {
            listItems.add(new ListItem(Integer.toString(testFolder.getObjectID()), id.toString()));
        }

        ListInfostoreResponse listResp = client2.execute(new ListInfostoreRequest(listItems, Metadata.columns(Metadata.HTTPAPI_VALUES_ARRAY), false));
        exception = listResp.getException();
        assertNotNull(exception);
        // TODO: check code

        /*
         * Shared files must be visible for client2
         * - via get requests
         * - via list request (folder 10)
         * - via all request (folder 10)
         */
        listItems.clear();
        for (String id : sharedFiles) {
            GetInfostoreResponse getResp = client2.execute(new GetInfostoreRequest(id));
            File doc = getResp.getDocumentMetadata();
            assertNotNull(doc);
            assertEquals(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, Integer.valueOf(doc.getFolderId()).intValue());
            assertEquals(id, doc.getId());

            listItems.add(new ListItem(Integer.toString(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID), id.toString()));
        }

        listResp = client2.execute(new ListInfostoreRequest(listItems, Metadata.columns(Metadata.HTTPAPI_VALUES_ARRAY)));
        Object[][] array = listResp.getArray();
        for (int i = 0; i < array.length; i++) {
            Object[] doc = array[i];
            assertEquals(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, Integer.parseInt((String) doc[listResp.getColumnPos(Metadata.FOLDER_ID)]));
            assertEquals(listItems.get(i).getId(), doc[listResp.getColumnPos(Metadata.ID)].toString());
        }

        all = itm.getAll(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, Metadata.columns(Metadata.HTTPAPI_VALUES_ARRAY), Metadata.ID, Order.ASCENDING);
        Set<String> foundIds = new HashSet<String>(sharedFiles);
        for (File file : all) {
            int docId = Integer.parseInt(file.getId());
            foundIds.remove(Integer.toString(docId));
        }
        assertTrue("Not all shared documents have been found", foundIds.isEmpty());

        /*
         * Non-shared files must not be visible via get- and list-requests
         */
        listItems.clear();
        for (String id : otherFiles) {
            GetInfostoreRequest req = new GetInfostoreRequest(id);
            req.setFailOnError(false);
            GetInfostoreResponse getResp = client2.execute(req);
            OXException exception2 = getResp.getException();
            assertNotNull(exception2);
            // TODO: check code

            listItems.add(new ListItem(allFiles.get(id)));
        }

        listResp = client2.execute(new ListInfostoreRequest(listItems, Metadata.columns(Metadata.HTTPAPI_VALUES_ARRAY), false));
        exception = listResp.getException();
        assertNotNull(exception);
        // TODO: check code

        /*
         * The file creator must not see the files within folder 10
         */
        all = itm.getAll(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, Metadata.columns(Metadata.HTTPAPI_VALUES_ARRAY), Metadata.ID, Order.ASCENDING);
        for (File file : all) {
            int docId = Integer.parseInt(file.getId());
            assertFalse(allFiles.containsKey(Integer.toString(docId)));
        }

        listItems.clear();
        for (String id : allFiles.keySet()) {
            listItems.add(new ListItem(Integer.toString(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID), id.toString()));
        }
        itm.list(listItems, Metadata.columns(Metadata.HTTPAPI_VALUES_ARRAY));
        assertFalse(itm.getLastResponse().hasError());
    }

    private File newDocument(int folderId, List<FileStorageObjectPermission> objectPermissions) {
        File doc = new DefaultFile();
        doc.setTitle(UUIDs.getUnformattedString(UUID.randomUUID()));
        doc.setDescription("Infostore Item Description");
        doc.setFileMIMEType("image/png");
        doc.setFolderId(String.valueOf(folderId));
        doc.setObjectPermissions(objectPermissions);
        doc.setFileName("contact_image.png");
        return doc;
    }

}
