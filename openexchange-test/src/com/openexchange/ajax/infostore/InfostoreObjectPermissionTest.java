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

package com.openexchange.ajax.infostore;

import java.io.IOException;
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
import org.json.JSONException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.infostore.actions.AllInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetInfostoreResponse;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.infostore.actions.ListInfostoreRequest;
import com.openexchange.ajax.infostore.actions.ListInfostoreRequest.ListItem;
import com.openexchange.ajax.infostore.actions.ListInfostoreResponse;
import com.openexchange.configuration.MailConfig;
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
import com.openexchange.test.FolderTestManager;


/**
 * {@link InfostoreObjectPermissionTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class InfostoreObjectPermissionTest extends AbstractAJAXSession {

    private AJAXClient client2;
    private InfostoreTestManager itm;
    private Map<String, Boolean> shareStates;
    private Map<String, File> allFiles;
    private FolderTestManager ftm;
    private FolderObject testFolder;

    /**
     * Initializes a new {@link InfostoreObjectPermissionTest}.
     * @param name
     */
    public InfostoreObjectPermissionTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client2 = new AJAXClient(User.User2);
        ftm = new FolderTestManager(client);
        String folderName = "InfostoreObjectPermissionTest_" + System.currentTimeMillis();
        testFolder = ftm.generatePrivateFolder(folderName,
            FolderObject.INFOSTORE,
            client.getValues().getPrivateInfostoreFolder(),
            client.getValues().getUserId());
        testFolder = ftm.insertFolderOnServer(testFolder);

        allFiles = new HashMap<String, File>();
        shareStates = new HashMap<String, Boolean>();

        itm = new InfostoreTestManager(client);
        itm.setFailOnError(true);
        java.io.File upload = new java.io.File(MailConfig.getProperty(MailConfig.Property.TEST_MAIL_DIR), "contact_image.png");
        Random r = new Random();
        for (int i = 0; i < 10; i++) {
            boolean shared = false;
            List<FileStorageObjectPermission> objectPermissions = null;
            if (r.nextBoolean()) {
                objectPermissions = Collections.<FileStorageObjectPermission>singletonList(
                    new DefaultFileStorageObjectPermission(client2.getValues().getUserId(), false, FileStorageObjectPermission.READ));
                shared = true;
            }
            File newDocument = newDocument(testFolder.getObjectID(), objectPermissions);
            itm.newAction(newDocument, upload);
            allFiles.put(newDocument.getId(), newDocument);
            shareStates.put(newDocument.getId(), shared);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        if (itm != null) {
            itm.cleanUp();
        }
        if (ftm != null) {
            ftm.cleanUp();
        }
        if (client2 != null) {
            client2.logout();
        }
        super.tearDown();
    }

    public void testReadPermission() throws Exception {
        /*
         * Must fail because of missing folder permissions
         */
        AbstractColumnsResponse allResp = client2.execute(new AllInfostoreRequest(
            testFolder.getObjectID(),
            Metadata.columns(Metadata.HTTPAPI_VALUES_ARRAY),
            Metadata.ID,
            Order.ASCENDING,
            false));
        OXException exception = allResp.getException();
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

        allResp = client2.execute(new AllInfostoreRequest(
            FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID,
            Metadata.columns(Metadata.HTTPAPI_VALUES_ARRAY),
            Metadata.ID,
            Order.ASCENDING));
        Set<String> foundIds = new HashSet<String>(sharedFiles);
        for (Object[] doc : allResp.getArray()) {
            int docId = Integer.parseInt((String) doc[allResp.getColumnPos(Metadata.ID)]);
            foundIds.remove(docId);
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
        allResp = client.execute(new AllInfostoreRequest(
            FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID,
            Metadata.columns(Metadata.HTTPAPI_VALUES_ARRAY),
            Metadata.ID,
            Order.ASCENDING));
        for (Object[] doc : allResp.getArray()) {
            int docId = Integer.parseInt((String) doc[allResp.getColumnPos(Metadata.ID)]);
            assertFalse(allFiles.containsKey(docId));
        }

        listItems.clear();
        for (String id : allFiles.keySet()) {
            listItems.add(new ListItem(Integer.toString(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID), id.toString()));
        }

        listResp = client.execute(new ListInfostoreRequest(listItems, Metadata.columns(Metadata.HTTPAPI_VALUES_ARRAY)));
        for (Object[] doc : listResp.getArray()) {
            int docId = Integer.parseInt((String) doc[allResp.getColumnPos(Metadata.ID)]);
            assertFalse(allFiles.containsKey(docId));
        }
    }

    private File newDocument(int folderId, List<FileStorageObjectPermission> objectPermissions) throws OXException, IOException, JSONException {
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
