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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.infostore.test;

import java.io.InputStream;
import java.util.Date;
import java.util.UUID;
import org.json.JSONArray;
import com.openexchange.ajax.folder.actions.DeleteRequest;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.ajax.infostore.actions.AllInfostoreRequest;
import com.openexchange.ajax.infostore.actions.DeleteInfostoreRequest;
import com.openexchange.ajax.infostore.actions.DeleteInfostoreResponse;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetInfostoreResponse;
import com.openexchange.ajax.infostore.actions.NewInfostoreRequest;
import com.openexchange.ajax.infostore.actions.NewInfostoreResponse;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;


/**
 * {@link TrashTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class TrashTest extends AbstractInfostoreTest {

    private final int[] COLUMNS = new int[] { Metadata.ID, Metadata.FILENAME, Metadata.FOLDER_ID };

    private FolderObject testFolder;
    private int trashFolderID;

    /**
     * Initializes a new {@link TrashTest}.
     *
     * @param name The test name
     */
    public TrashTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testFolder = fMgr.generatePrivateFolder(UUID.randomUUID().toString(), FolderObject.INFOSTORE,
            client.getValues().getPrivateInfostoreFolder(), client.getValues().getUserId());
        testFolder = fMgr.insertFolderOnServer(testFolder);
        trashFolderID = client.getValues().getInfostoreTrashFolder();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testHardDeleteFolder() throws Exception {
        /*
         * hard-delete folder
         */
        FolderObject folder = createRandomFolder(testFolder.getObjectID());
        deleteFolder(folder, Boolean.TRUE);
        /*
         * check source & trash folder contents
         */
        assertFolderNotExistsInFolder(testFolder.getObjectID(), folder.getObjectID());
        assertFolderNotExistsInFolder(trashFolderID, folder.getObjectID());
    }

    public void testSoftDeleteFolder() throws Exception {
        /*
         * soft-delete folder
         */
        FolderObject folder = createRandomFolder(testFolder.getObjectID());
        deleteFolder(folder, Boolean.FALSE);
        /*
         * check source & trash folder contents
         */
        assertFolderNotExistsInFolder(testFolder.getObjectID(), folder.getObjectID());
        assertFolderExistsInFolder(trashFolderID, folder.getObjectID());
    }

    public void testDefaultDeleteFolder() throws Exception {
        /*
         * soft-delete file
         */
        FolderObject folder = createRandomFolder(testFolder.getObjectID());
        deleteFolder(folder, null);
        /*
         * check source & trash folder contents
         */
        assertFolderNotExistsInFolder(testFolder.getObjectID(), folder.getObjectID());
        assertFolderExistsInFolder(trashFolderID, folder.getObjectID());
    }

    public void testDeleteDeletedFolder() throws Exception {
        /*
         * soft-delete folder
         */
        FolderObject folder = createRandomFolder(testFolder.getObjectID());
        deleteFolder(folder, null);
        /*
         * check source & trash folder contents
         */
        assertFolderNotExistsInFolder(testFolder.getObjectID(), folder.getObjectID());
        assertFolderExistsInFolder(trashFolderID, folder.getObjectID());
        /*
         * delete folder again
         */
        deleteFolder(folder, null);
        /*
         * check source & trash folder contents
         */
        assertFolderNotExistsInFolder(testFolder.getObjectID(), folder.getObjectID());
        assertFolderNotExistsInFolder(trashFolderID, folder.getObjectID());
    }

    public void testDeleteFolderWithConflictingName() throws Exception {
        String foldername = UUID.randomUUID().toString();
        /*
         * soft-delete first folder
         */
        FolderObject folder1 = createRandomFolder(testFolder.getObjectID(), foldername);
        deleteFolder(folder1, null);
        /*
         * soft-delete first file
         */
        FolderObject folder2 = createRandomFolder(testFolder.getObjectID(), foldername);
        deleteFolder(folder2, null);
        /*
         * check source & trash folder contents
         */
        assertFolderNotExistsInFolder(testFolder.getObjectID(), folder1.getObjectID());
        assertFolderNotExistsInFolder(testFolder.getObjectID(), folder2.getObjectID());
        assertFolderExistsInFolder(trashFolderID, folder1.getObjectID());
        assertFolderExistsInFolder(trashFolderID, folder2.getObjectID());
    }

    public void testHardDeleteFile() throws Exception {
        /*
         * hard-delete file
         */
        DocumentMetadataImpl file = createRandomFile(testFolder.getObjectID());
        deleteFile(file, Boolean.TRUE);
        /*
         * check source & trash folder contents
         */
        assertFileNotExistsInFolder(testFolder.getObjectID(), file.getId());
        assertFileNotExistsInFolder(trashFolderID, file.getId());
    }

    public void testSoftDeleteFile() throws Exception {
        /*
         * soft-delete file
         */
        DocumentMetadataImpl file = createRandomFile(testFolder.getObjectID());
        deleteFile(file, Boolean.FALSE);
        /*
         * check source & trash folder contents
         */
        assertFileNotExistsInFolder(testFolder.getObjectID(), file.getId());
        assertFileExistsInFolder(trashFolderID, file.getFileName());
    }

    public void testDefaultDeleteFile() throws Exception {
        /*
         * soft-delete file
         */
        DocumentMetadataImpl file = createRandomFile(testFolder.getObjectID());
        deleteFile(file, null);
        /*
         * check source & trash folder contents
         */
        assertFileNotExistsInFolder(testFolder.getObjectID(), file.getId());
        assertFileExistsInFolder(trashFolderID, file.getFileName());
    }

    public void testDeleteDeletedFile() throws Exception {
        /*
         * soft-delete file
         */
        DocumentMetadataImpl file = createRandomFile(testFolder.getObjectID());
        deleteFile(file, null);
        /*
         * check source & trash folder contents
         */
        assertFileNotExistsInFolder(testFolder.getObjectID(), file.getId());
        assertFileExistsInFolder(trashFolderID, file.getFileName());
        /*
         * delete file again
         */
        deleteFile(file, null);
        /*
         * check source & trash folder contents
         */
        assertFileNotExistsInFolder(testFolder.getObjectID(), file.getId());
        assertFileNotExistsInFolder(trashFolderID, file.getId());
    }

    public void testDeleteFileWithConflictingName() throws Exception {
        String filename = UUID.randomUUID().toString();
        /*
         * soft-delete first file
         */
        DocumentMetadataImpl file1 = createRandomFile(testFolder.getObjectID(), filename);
        DocumentMetadata fetchedFile = getFile(file1.getId());
        file1.setLastModified(fetchedFile.getLastModified());
        deleteFile(file1, null);
        /*
         * soft-delete first file
         */
        DocumentMetadataImpl file2 = createRandomFile(testFolder.getObjectID(), filename);
        fetchedFile = getFile(file2.getId());
        file2.setLastModified(fetchedFile.getLastModified());
        deleteFile(file2, null);
        /*
         * check source & trash folder contents
         */
        assertFileNotExistsInFolder(testFolder.getObjectID(), file1.getId());
        assertFileNotExistsInFolder(testFolder.getObjectID(), file2.getId());
        assertFileExistsInFolder(trashFolderID, filename);
        assertFileExistsInFolder(trashFolderID, filename + " (1)");
    }

    private void deleteFolder(FolderObject folder, Boolean hardDelete) throws Exception {
        Date timestamp = null != folder.getLastModified() ? folder.getLastModified() : new Date(Long.MAX_VALUE);
        DeleteRequest deleteRequest = new DeleteRequest(EnumAPI.OX_OLD, folder.getObjectID(), timestamp);
        deleteRequest.setHardDelete(hardDelete);
        CommonDeleteResponse deleteResponse = client.execute(deleteRequest);
        JSONArray json = (JSONArray) deleteResponse.getData();
        assertEquals("folder not deleted", 0, json.length());
        folder.setLastModified(deleteResponse.getTimestamp());
    }

    private void deleteFile(DocumentMetadataImpl file, Boolean hardDelete) throws Exception {
        Date timestamp = null != file.getLastModified() ? file.getLastModified() : new Date(Long.MAX_VALUE);
        DeleteInfostoreRequest deleteRequest = new DeleteInfostoreRequest(file.getId(), testFolder.getObjectID(), timestamp);
        deleteRequest.setHardDelete(hardDelete);
        DeleteInfostoreResponse deleteResponse = client.execute(deleteRequest);
        JSONArray json = (JSONArray) deleteResponse.getData();
        assertNull("An error occurred during delete attempt: " + deleteResponse.getResponse().getException(), deleteResponse.getResponse().getException());
        assertEquals("file not deleted", 0, json.length());
        file.setLastModified(deleteResponse.getTimestamp());
    }

    private void assertFileExistsInFolder(int folderID, int objectID) throws Exception {
        AllInfostoreRequest allRequest = new AllInfostoreRequest(folderID, COLUMNS, Metadata.ID, Order.ASCENDING);
        AbstractColumnsResponse allResponse = client.execute(allRequest);
        for (Object[] object : allResponse) {
            Integer id = Integer.valueOf(object[0].toString());
            if (id.intValue() == objectID) {
                return;
            }
        }
        fail("File " + objectID + " not found in folder: " + folderID);
    }

    private void assertFileExistsInFolder(int folderID, String fileName) throws Exception {
        AllInfostoreRequest allRequest = new AllInfostoreRequest(folderID, COLUMNS, Metadata.ID, Order.ASCENDING);
        AbstractColumnsResponse allResponse = client.execute(allRequest);
        for (Object[] object : allResponse) {
            String fn = object[1].toString();
            if (fileName.equals(fn)) {
                return;
            }
        }
        fail("File " + fileName + " not found in folder: " + folderID);
    }

    private void assertFolderExistsInFolder(int folderID, int objectID) throws Exception {
        FolderObject[] folders = fMgr.listFoldersOnServer(folderID);
        for (FolderObject folder : folders) {
            if (folder.getObjectID() == objectID) {
                return;
            }
        }
        fail("File " + objectID + " not found in folder: " + folderID);
    }

    private void assertFileNotExistsInFolder(int folderID, int objectID) throws Exception {
        AllInfostoreRequest allRequest = new AllInfostoreRequest(folderID, COLUMNS, Metadata.ID, Order.ASCENDING);
        AbstractColumnsResponse allResponse = client.execute(allRequest);
        for (Object[] object : allResponse) {
            Integer id = Integer.valueOf(object[0].toString());
            assertFalse("File " + objectID + " found in folder: " + folderID, objectID ==  id.intValue());
        }
    }

    private void assertFolderNotExistsInFolder(int folderID, int objectID) throws Exception {
        FolderObject[] folders = fMgr.listFoldersOnServer(folderID);
        for (FolderObject folder : folders) {
            assertFalse("Folder " + objectID + " found in folder: " + folderID, objectID ==  folder.getObjectID());
        }
    }

    private DocumentMetadata getFile(int objectID) throws Exception {
        GetInfostoreResponse getResponse = client.execute(new GetInfostoreRequest(objectID));
        return getResponse.getDocumentMetadata();
    }

    private FolderObject createRandomFolder(int folderID) throws Exception {
        return createRandomFolder(folderID, UUID.randomUUID().toString());
    }

    private FolderObject createRandomFolder(int folderID, String foldername) throws Exception {
        FolderObject folder = fMgr.generatePrivateFolder(foldername, FolderObject.INFOSTORE, folderID, client.getValues().getUserId());
        folder = fMgr.insertFolderOnServer(folder);
        return folder;
    }

    private DocumentMetadataImpl createRandomFile(int folderID) throws Exception {
        return createRandomFile(folderID, UUID.randomUUID().toString());
    }

    private DocumentMetadataImpl createRandomFile(int folderID, String filename) throws Exception {
        DocumentMetadataImpl file = new DocumentMetadataImpl();
        file.setFileName(filename);
        file.setFolderId(folderID);
        file.setCreationDate(new Date());
        InputStream data = null;
        try {
            data = Streams.newByteArrayInputStream(UUIDs.toByteArray(UUID.randomUUID()));
            NewInfostoreResponse newResponse = client.execute(new NewInfostoreRequest(file, data));
            file.setId(newResponse.getID());
            file.setLastModified(newResponse.getTimestamp());
            infoMgr.getCreatedEntities().add(file);
        } finally {
            Streams.close(data);
        }
        return file;
    }

}
