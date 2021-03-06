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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.framework.AbstractUpdatesRequest.Ignore;
import com.openexchange.ajax.infostore.actions.AllInfostoreRequest;
import com.openexchange.ajax.infostore.actions.DetachInfostoreRequest;
import com.openexchange.ajax.infostore.actions.DetachInfostoreResponse;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.infostore.actions.UpdatesInfostoreRequest;
import com.openexchange.ajax.infostore.actions.UpdatesInfostoreResponse;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.util.UUIDs;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.common.test.TestInit;

/**
 * {@link UpdatesTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UpdatesTest extends AbstractAJAXSession {

    protected static final int[] virtualFolders = { FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID, FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID };

    @SuppressWarnings("hiding")
    private FolderTestManager ftm;
    private FolderObject testFolder;
    private File knowledgeDoc;
    private File urlDoc;
    @SuppressWarnings("hiding")
    private InfostoreTestManager itm;

    public UpdatesTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        itm = new InfostoreTestManager(getClient());
        ftm = new FolderTestManager(getClient());
        testFolder = ftm.generatePrivateFolder(UUIDs.getUnformattedString(UUID.randomUUID()), FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), getClient().getValues().getUserId());
        ftm.insertFolderOnServer(testFolder);

        knowledgeDoc = new DefaultFile();
        knowledgeDoc.setFolderId(String.valueOf(testFolder.getObjectID()));
        knowledgeDoc.setTitle("test knowledge");
        knowledgeDoc.setDescription("test knowledge description");
        itm.newAction(knowledgeDoc);

        urlDoc = new DefaultFile();
        urlDoc.setFolderId(String.valueOf(testFolder.getObjectID()));
        urlDoc.setTitle("test url");
        urlDoc.setDescription("test url description");
        urlDoc.setURL("http://www.open-xchange.com");
        itm.newAction(urlDoc);
    }

    @Test
    public void testBasic() throws Exception {
        AllInfostoreRequest allReq = new AllInfostoreRequest(testFolder.getObjectID(), new int[] { Metadata.ID, Metadata.FOLDER_ID }, Metadata.ID, Order.ASCENDING);
        AbstractColumnsResponse allResp = getClient().execute(allReq);
        Date timestamp = new Date(allResp.getTimestamp().getTime() + 2);

        File updateDoc = new DefaultFile();
        updateDoc.setId(knowledgeDoc.getId());
        updateDoc.setTitle("test knowledge updated");
        itm.updateAction(updateDoc, new File.Field[] { File.Field.TITLE }, timestamp);

        UpdatesInfostoreRequest req = new UpdatesInfostoreRequest(testFolder.getObjectID(), new int[] { Metadata.ID, Metadata.TITLE }, Metadata.ID, Order.ASCENDING, Ignore.NONE, timestamp, true);
        UpdatesInfostoreResponse resp = getClient().execute(req);
        JSONArray modifiedValues = resp.getNewAndModified().iterator().next();
        assertEquals("Wrong number of modified documents", 1, resp.getNewAndModified().size());
        assertEquals("Wrong number of deleted documents", 0, resp.getDeleted().size());
        assertEquals("Wrong document id", updateDoc.getId(), modifiedValues.getString(0));
        assertEquals("Wrong document title", updateDoc.getTitle(), modifiedValues.getString(1));

        timestamp = itm.getLastResponse().getTimestamp();
        itm.deleteAction(knowledgeDoc.getId(), String.valueOf(testFolder.getObjectID()), timestamp);
        itm.deleteAction(urlDoc.getId(), String.valueOf(testFolder.getObjectID()), timestamp);

        req = new UpdatesInfostoreRequest(testFolder.getObjectID(), new int[] { Metadata.ID }, Metadata.ID, Order.ASCENDING, Ignore.NONE, timestamp, true);
        resp = getClient().execute(req);
        assertEquals("Wrong number of modified documents", 0, resp.getNewAndModified().size());
        assertEquals("Wrong number of deleted documents", 2, resp.getDeleted().size());
        int found = 0;
        for (String id : resp.getDeleted()) {
            if (id.equals(String.valueOf(knowledgeDoc.getId())) || id.equals(String.valueOf(urlDoc.getId()))) {
                found++;
            }
        }
        assertEquals("Wrong documents have been deleted", 2, found);
    }

    @Test
    public void testRemovedVersionForcesUpdate() throws Exception {
        AllInfostoreRequest allReq = new AllInfostoreRequest(testFolder.getObjectID(), new int[] { Metadata.ID, Metadata.FOLDER_ID }, Metadata.ID, Order.ASCENDING);
        AbstractColumnsResponse allResp = getClient().execute(allReq);
        Date timestamp = new Date(allResp.getTimestamp().getTime() + 2);

        java.io.File upload = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
        DefaultFile updateDoc = new DefaultFile();
        updateDoc.setId(knowledgeDoc.getId());
        updateDoc.setVersionComment("Comment 1");
        itm.updateAction(updateDoc, upload, new File.Field[] { File.Field.VERSION_COMMENT }, timestamp);
        timestamp = itm.getLastResponse().getTimestamp();

        updateDoc.setVersionComment("Comment 2");
        itm.updateAction(updateDoc, upload, new File.Field[] { File.Field.VERSION_COMMENT }, timestamp);
        timestamp = itm.getLastResponse().getTimestamp();

        updateDoc.setVersionComment("Comment 3");
        itm.updateAction(updateDoc, upload, new File.Field[] { File.Field.VERSION_COMMENT }, timestamp);
        timestamp = itm.getLastResponse().getTimestamp();

        DetachInfostoreRequest detachReq = new DetachInfostoreRequest(updateDoc.getId(), testFolder.getObjectID(), Collections.singleton("3"), timestamp);
        DetachInfostoreResponse detachResp = getClient().execute(detachReq);
        assertEquals("Version was not deleted", 0, detachResp.getNotDeleted().length);

        UpdatesInfostoreRequest req = new UpdatesInfostoreRequest(testFolder.getObjectID(), new int[] { Metadata.TITLE, Metadata.DESCRIPTION }, Metadata.ID, Order.ASCENDING, Ignore.NONE, timestamp, true);
        UpdatesInfostoreResponse resp = getClient().execute(req);
        assertEquals("Wrong number of modified documents", 1, resp.getNewAndModified().size());
    }

    //Bug 4269
    @Test
    public void testVirtualFolder() throws Exception {
        for (int folderId : virtualFolders) {
            virtualFolderTest(folderId);
        }
    }

    //Bug 4269
    private void virtualFolderTest(int folderId) throws Exception {
        UpdatesInfostoreRequest req = new UpdatesInfostoreRequest(folderId, new int[] { Metadata.ID }, Metadata.ID, Order.ASCENDING, Ignore.NONE, new Date(0L), true);
        UpdatesInfostoreResponse resp = getClient().execute(req);
        assertEquals("Wrong number of modified documents", 0, resp.getNewAndModified().size() + resp.getDeleted().size());
    }

    // Node 2652    @Test
    @Test
    public void testLastModifiedUTC() throws Exception {
        UpdatesInfostoreRequest req = new UpdatesInfostoreRequest(testFolder.getObjectID(), new int[] { Metadata.LAST_MODIFIED_UTC }, Metadata.LAST_MODIFIED_UTC, Order.ASCENDING, Ignore.NONE, new Date(0L), true);
        UpdatesInfostoreResponse resp = getClient().execute(req);
        assertTrue("Wrong number of modified documents", resp.getNewAndModified().size() + resp.getDeleted().size() > 0);
    }

    // Bug 12427
    @Test
    public void testNumberOfVersions() throws Exception {
        java.io.File upload = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));
        DefaultFile updateDoc = new DefaultFile();
        updateDoc.setId(knowledgeDoc.getId());
        updateDoc.setDescription("New description");
        itm.updateAction(updateDoc, upload, new File.Field[] { File.Field.DESCRIPTION }, new Date(Long.MAX_VALUE));

        UpdatesInfostoreRequest req = new UpdatesInfostoreRequest(testFolder.getObjectID(), new int[] { Metadata.ID, Metadata.NUMBER_OF_VERSIONS }, Metadata.ID, Order.ASCENDING, Ignore.NONE, new Date(0L), true);
        UpdatesInfostoreResponse resp = getClient().execute(req);

        boolean found = false;
        for (JSONArray modified : resp.getNewAndModified()) {
            String id = modified.getString(0);
            int numberOfVersions = modified.getInt(1);
            if (id.equals(updateDoc.getId())) {
                assertEquals(1, numberOfVersions);
                found = true;
            }
        }
        assertTrue(found);
    }

}
