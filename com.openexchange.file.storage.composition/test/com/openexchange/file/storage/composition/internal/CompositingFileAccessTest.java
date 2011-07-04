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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.file.storage.composition.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.session.Session;
import com.openexchange.session.SimSession;
import com.openexchange.sim.Block;
import com.openexchange.sim.SimBuilder;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link CompositingFileAccessTest}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CompositingFileAccessTest extends CompositingIDBasedFileAccess implements FileStorageService, FileStorageAccountAccess, FileStorageAccountManager {

    private static final InputStream EMPTY_INPUT_STREAM = new ByteArrayInputStream(new byte[0]);

    private String serviceId;

    private FileStorageFileAccess files;

    private SimBuilder fileAccess = new SimBuilder();

    private String accountId;

    private FileID fileId = new FileID("com.openexchange.test", "account 23", "folder", "id");

    private FolderID folderId = new FolderID(fileId.getService(), fileId.getAccountId(), fileId.getFolderId());

    private String serviceId2;

    private String accountId2;

    private FileID fileId2 = new FileID("com.openexchange.test2", "account 12", "folder2", "id2");

    private FolderID folderId2 = new FolderID(fileId2.getService(), fileId2.getAccountId(), fileId2.getFolderId());
    
    private IDSetter setId = new IDSetter(fileId.getFileId());
    
    

    public CompositingFileAccessTest() {
        super(new SimSession());
    }

    @Override
    protected FileStorageService getFileStorageService(String serviceId) {
        if (this.serviceId == null) {
            this.serviceId = serviceId;
        } else {
            this.serviceId2 = serviceId;
        }
        return this;
    }

    @Test
    public void testExists() throws OXException {
        fileAccess.expectCall("exists", fileId.getFolderId(), fileId.getFileId(), 12).andReturn(true);

        assertTrue(exists(fileId.toUniqueID(), 12));
        verifyAccount();

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testGetDeltaWithoutSort() throws OXException {

        fileAccess.expectCall("getDelta", folderId.getFolderId(), 12l, Arrays.asList(
            File.Field.TITLE,
            File.Field.ID,
            File.Field.FOLDER_ID,
            File.Field.LAST_MODIFIED), true);

        getDelta(folderId.toUniqueID(), 12, Arrays.asList(File.Field.TITLE), true);

        verifyAccount();
        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testGetDeltaWithSort() throws OXException {

        fileAccess.expectCall("getDelta", folderId.getFolderId(), 12l, Arrays.asList(
            File.Field.TITLE,
            File.Field.ID,
            File.Field.FOLDER_ID,
            File.Field.LAST_MODIFIED), File.Field.TITLE, SortDirection.DESC, true);

        getDelta(folderId.toUniqueID(), 12, Arrays.asList(File.Field.TITLE), File.Field.TITLE, SortDirection.DESC, true);

        verifyAccount();
        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testGetDocument() throws OXException {
        fileAccess.expectCall("getDocument", fileId.getFolderId(), fileId.getFileId(), 12);

        getDocument(fileId.toUniqueID(), 12);

        verifyAccount();
        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testGetDocuments1() throws OXException {
        fileAccess.expectCall("getDocuments", folderId.getFolderId());

        getDocuments(folderId.toUniqueID());

        verifyAccount();
        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testGetDocuments2() throws OXException {
        fileAccess.expectCall("getDocuments", folderId.getFolderId(), Arrays.asList(
            File.Field.TITLE,
            File.Field.ID,
            File.Field.FOLDER_ID,
            File.Field.LAST_MODIFIED));

        getDocuments(folderId.toUniqueID(), Arrays.asList(File.Field.TITLE));

        verifyAccount();
        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testGetDocuments3() throws OXException {
        fileAccess.expectCall("getDocuments", folderId.getFolderId(), Arrays.asList(
            File.Field.TITLE,
            File.Field.ID,
            File.Field.FOLDER_ID,
            File.Field.LAST_MODIFIED), File.Field.TITLE, SortDirection.DESC);

        getDocuments(folderId.toUniqueID(), Arrays.asList(File.Field.TITLE), File.Field.TITLE, SortDirection.DESC);

        verifyAccount();
        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testGetDocuments4() throws OXException {
        DefaultFile defaultFile = new DefaultFile();
        defaultFile.setLastModified(new Date());
        defaultFile.setId(fileId.getFileId());
        defaultFile.setFolderId(fileId.getFolderId());

        fileAccess.expectCall("exists", fileId.getFolderId(), fileId.getFileId(), FileStorageFileAccess.CURRENT_VERSION).andReturn(
            true);
        fileAccess.expectCall("getFileMetadata", fileId.getFolderId(), fileId.getFileId(), FileStorageFileAccess.CURRENT_VERSION).andReturn(
            defaultFile);
        fileAccess.expectCall("exists", fileId2.getFolderId(), fileId2.getFileId(), FileStorageFileAccess.CURRENT_VERSION).andReturn(
            true);
        fileAccess.expectCall("getFileMetadata", fileId2.getFolderId(), fileId2.getFileId(), FileStorageFileAccess.CURRENT_VERSION).andReturn(
            defaultFile);

        getDocuments(Arrays.asList(fileId.toUniqueID(), fileId2.toUniqueID()), Arrays.asList(File.Field.TITLE));

        verifyAccount();
        verifyAccount2();

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testGetFileMetadata() throws OXException {
        DefaultFile file = new DefaultFile();
        file.setId(fileId.getFileId());
        file.setFolderId(fileId.getFolderId());

        fileAccess.expectCall("getFileMetadata", fileId.getFolderId(), fileId.getFileId(), 12).andReturn(file);

        getFileMetadata(fileId.toUniqueID(), 12);

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testGetVersions1() throws OXException {
        fileAccess.expectCall("getVersions", fileId.getFolderId(), fileId.getFileId());

        getVersions(fileId.toUniqueID());

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testGetVersions2() throws OXException {
        fileAccess.expectCall("getVersions", fileId.getFolderId(), fileId.getFileId(), Arrays.asList(
            File.Field.TITLE,
            File.Field.ID,
            File.Field.FOLDER_ID,
            File.Field.LAST_MODIFIED));

        getVersions(fileId.toUniqueID(), Arrays.asList(File.Field.TITLE));

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testGetVersions3() throws OXException {
        fileAccess.expectCall("getVersions", fileId.getFolderId(), fileId.getFileId(), Arrays.asList(
            File.Field.TITLE,
            File.Field.ID,
            File.Field.FOLDER_ID,
            File.Field.LAST_MODIFIED), File.Field.TITLE, SortDirection.DESC);

        getVersions(fileId.toUniqueID(), Arrays.asList(File.Field.TITLE), File.Field.TITLE, SortDirection.DESC);

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testLock() throws OXException {
        fileAccess.expectCall("lock", fileId.getFolderId(), fileId.getFileId(), 1337l);

        lock(fileId.toUniqueID(), 1337);

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testUnlock() throws OXException {
        fileAccess.expectCall("unlock", fileId.getFolderId(), fileId.getFileId());

        unlock(fileId.toUniqueID());

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testTouch() throws OXException {
        fileAccess.expectCall("lock", fileId.getFolderId(), fileId.getFileId(), 1337l);

        lock(fileId.toUniqueID(), 1337);

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testRemoveDocument() throws OXException {
        fileAccess.expectCall("removeDocument", folderId.getFolderId(), 12l);

        removeDocument(folderId.toUniqueID(), 12);

        fileAccess.assertAllWereCalled();
    }

    // Somewhat brittle test
    @Test
    public void testRemoveDocuments() throws OXException {
        FileStorageFileAccess.IDTuple tuple = new FileStorageFileAccess.IDTuple(fileId.getFolderId(), fileId.getFileId());
        fileAccess.expectCall("hashCode").andReturn(1); // Look if it's there
        fileAccess.expectCall("hashCode").andReturn(1); // Store it
        fileAccess.expectCall("hashCode").andReturn(2); // Look if it's there
        fileAccess.expectCall("hashCode").andReturn(2); // Store it

        fileAccess.expectCall("removeDocument", Arrays.asList(tuple), 12l).andReturn(Arrays.asList(tuple));
        fileAccess.expectCall("getAccountAccess").andReturn(this);

        FileStorageFileAccess.IDTuple tuple2 = new FileStorageFileAccess.IDTuple(fileId2.getFolderId(), fileId2.getFileId());
        fileAccess.expectCall("removeDocument", Arrays.asList(tuple2), 12l).andReturn(Arrays.asList(tuple2));
        fileAccess.expectCall("getAccountAccess").andReturn(this);

        List<String> ids = Arrays.asList(fileId.toUniqueID(), fileId2.toUniqueID());
        List<String> conflicted = removeDocument(ids, 12);

        assertEquals(Arrays.asList(new FileID(getId(), getAccountId(), fileId.getFolderId(), fileId.getFileId()).toUniqueID(), new FileID(
            getId(),
            getAccountId(),
            fileId2.getFolderId(),
            fileId2.getFileId()).toUniqueID()), conflicted);

        verifyAccount();
        verifyAccount2();

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testRemoveVersions() throws OXException {
        int[] versions = new int[] { 1, 2, 3 };

        fileAccess.expectCall("removeVersion", fileId.getFolderId(), fileId.getFileId(), versions);

        removeVersion(fileId.toUniqueID(), versions);

        verifyAccount();
        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testSearch() throws OXException {
        fileAccess.expectCall("search", "query", Arrays.asList(
            File.Field.TITLE,
            File.Field.ID,
            File.Field.FOLDER_ID,
            File.Field.LAST_MODIFIED), folderId.getFolderId(), File.Field.TITLE, SortDirection.DESC, 10, 20);

        search("query", Arrays.asList(File.Field.TITLE), folderId.toUniqueID(), File.Field.TITLE, SortDirection.DESC, 10, 20);

        verifyAccount();

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testSearchInAllFolders() throws OXException {
        fileAccess.expectCall(
            "search",
            "query",
            Arrays.asList(File.Field.TITLE, File.Field.ID, File.Field.FOLDER_ID, File.Field.LAST_MODIFIED),
            FileStorageFileAccess.ALL_FOLDERS,
            File.Field.TITLE,
            SortDirection.DESC,
            10,
            20).andReturn(SearchIteratorAdapter.emptyIterator());
        fileAccess.expectCall("getAccountAccess").andReturn(this);
        fileAccess.expectCall(
            "search",
            "query",
            Arrays.asList(File.Field.TITLE, File.Field.ID, File.Field.FOLDER_ID, File.Field.LAST_MODIFIED),
            FileStorageFileAccess.ALL_FOLDERS,
            File.Field.TITLE,
            SortDirection.DESC,
            10,
            20).andReturn(SearchIteratorAdapter.emptyIterator());
        fileAccess.expectCall("getAccountAccess").andReturn(this);

        search("query", Arrays.asList(File.Field.TITLE), FileStorageFileAccess.ALL_FOLDERS, File.Field.TITLE, SortDirection.DESC, 10, 20);

        fileAccess.assertAllWereCalled();
    }

    // create file
    @Test
    public void testCreateDocument1() throws OXException {
        File file = new DefaultFile();
        file.setId(FileStorageFileAccess.NEW);
        file.setFolderId(folderId.toUniqueID());

        fileAccess.expectCall("saveDocument", file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER).andDo(setId);

        saveDocument(file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);

        verifyAccount();
        fileAccess.assertAllWereCalled();

        assertEquals(file.getId(), fileId.toUniqueID());
    }

    @Test
    public void testCreateDocument2() throws OXException {
        File file = new DefaultFile();
        file.setId(FileStorageFileAccess.NEW);
        file.setFolderId(folderId.toUniqueID());

        fileAccess.expectCall("saveDocument", file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE)).andDo(setId);

        saveDocument(file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE));

        verifyAccount();
        fileAccess.assertAllWereCalled();

        assertEquals(file.getId(), fileId.toUniqueID());
    }

    @Test
    public void testCreateMetadata1() throws OXException {
        File file = new DefaultFile();
        file.setId(FileStorageFileAccess.NEW);
        file.setFolderId(folderId.toUniqueID());

        fileAccess.expectCall("saveFileMetadata", file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE)).andDo(setId);

        saveFileMetadata(file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE));

        verifyAccount();
        fileAccess.assertAllWereCalled();

        assertEquals(file.getId(), fileId.toUniqueID());
    }

    @Test
    public void testCreateMetadata2() throws OXException {
        File file = new DefaultFile();
        file.setId(FileStorageFileAccess.NEW);
        file.setFolderId(folderId.toUniqueID());

        fileAccess.expectCall("saveFileMetadata", file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE)).andDo(setId);

        saveFileMetadata(file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE));

        verifyAccount();
        fileAccess.assertAllWereCalled();

        assertEquals(file.getId(), fileId.toUniqueID());
    }

    // update file
    
    @Test
    public void testUpdateDocument1() throws OXException {
        File file = new DefaultFile();
        file.setId(fileId.toUniqueID());
        file.setFolderId(folderId.toUniqueID());

        fileAccess.expectCall("saveDocument", file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);

        saveDocument(file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);

        verifyAccount();
        fileAccess.assertAllWereCalled();

        assertEquals(file.getId(), fileId.toUniqueID());
    }

    @Test
    public void testUpdateDocument2() throws OXException {
        File file = new DefaultFile();
        file.setId(fileId.toUniqueID());
        file.setFolderId(folderId.toUniqueID());

        fileAccess.expectCall("saveDocument", file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE));

        saveDocument(file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE));

        verifyAccount();
        fileAccess.assertAllWereCalled();

        assertEquals(file.getId(), fileId.toUniqueID());
    }

    @Test
    public void testUpdateMetadata1() throws OXException {
        File file = new DefaultFile();
        file.setId(fileId.toUniqueID());
        file.setFolderId(folderId.toUniqueID());

        fileAccess.expectCall("saveFileMetadata", file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE));

        saveFileMetadata(file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE));

        verifyAccount();
        fileAccess.assertAllWereCalled();

        assertEquals(file.getId(), fileId.toUniqueID());
    }

    @Test
    public void testUpdateMetadata2() throws OXException {
        File file = new DefaultFile();
        file.setId(fileId.toUniqueID());
        file.setFolderId(folderId.toUniqueID());

        fileAccess.expectCall("saveFileMetadata", file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE));

        saveFileMetadata(file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE));

        verifyAccount();
        fileAccess.assertAllWereCalled();

        assertEquals(file.getId(), fileId.toUniqueID());
    }

    // Moving across filestores
    
    @Test
    public void testMoveACompleteFileWithANewUpload() throws OXException {
        File file = new DefaultFile();
        file.setId(fileId2.toUniqueID()); // We start in FileStore 2
        file.setFolderId(folderId.toUniqueID()); // And want to move to FileStore 1
        
        // Firstly the file should be created in the destination as a new file
        File destinationFile = new DefaultFile();
        destinationFile.setId(FileStorageFileAccess.NEW);
        destinationFile.setFolderId(folderId.getFolderId());
        
        fileAccess.expectCall("saveDocument", file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER).andDo(setId);
        
        // Secondly the original must be deleted
        
        fileAccess.expectCall("removeDocument", Arrays.asList(new FileStorageFileAccess.IDTuple(fileId2.getFolderId(), fileId2.getFileId())), 1337l);
                
        move(file, EMPTY_INPUT_STREAM, 1337, null);
        
        verifyAccount(); // Store on destination account then
        verifyAccount2(); // Remove from source account
        
        fileAccess.assertAllWereCalled();
        
        // The document will receive a new ID
        assertEquals(file.getId(), fileId.toUniqueID());
    }
    
    @Test
    public void testPartialMetadataWithANewUpload() throws OXException {
        File file = new DefaultFile();
        file.setId(fileId2.toUniqueID()); // We start in FileStore 2
        file.setFolderId(folderId.toUniqueID()); // And want to move to FileStore 1
        file.setTitle("New Title"); // And we want to set a new title
        
        // Since this is only a partial file, firstly the original document should be loaded
        File storedFile = new DefaultFile();
        storedFile.setTitle("Old Title");
        storedFile.setDescription("Old Description"); // We want to keep the old description
        fileAccess.expectCall("getFileMetadata", fileId2.getFolderId(), fileId2.getFileId(), FileStorageFileAccess.CURRENT_VERSION).andReturn(storedFile);
        
        // Next the file should be created in the destination as a new file
        File destinationFile = new DefaultFile();
        destinationFile.setId(FileStorageFileAccess.NEW);
        destinationFile.setFolderId(folderId.getFolderId());
        
        fileAccess.expectCall("saveDocument", file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER).andDo(setId);
        
        // And lastly the original must be deleted
        
        fileAccess.expectCall("removeDocument", Arrays.asList(new FileStorageFileAccess.IDTuple(fileId2.getFolderId(), fileId2.getFileId())), 1337l);
                
        move(file, EMPTY_INPUT_STREAM, 1337, Arrays.asList(File.Field.TITLE, File.Field.FOLDER_ID)); // Title and FolderID have been changed
        
        fileAccess.assertAllWereCalled();
        
        // The document will receive a new ID
        assertEquals(file.getId(), fileId.toUniqueID());
        
        File fileThatWasCreated = setId.getFile();
        
        assertEquals("New Title", fileThatWasCreated.getTitle());
        assertEquals("Old Description", fileThatWasCreated.getDescription());
    }
    
    @Test
    public void testMoveCompleteFileWithoutUpload() throws OXException {
        File file = new DefaultFile();
        file.setId(fileId2.toUniqueID()); // We start in FileStore 2
        file.setFolderId(folderId.toUniqueID()); // And want to move to FileStore 1
        
        // No Input Stream is provided, so load the file from the source store
        fileAccess.expectCall("getDocument", fileId2.getFolderId(), fileId2.getFileId(), FileStorageFileAccess.CURRENT_VERSION).andReturn(EMPTY_INPUT_STREAM);
        
        // Next the file should be created in the destination as a new file, with the input stream provided above
        File destinationFile = new DefaultFile();
        destinationFile.setId(FileStorageFileAccess.NEW);
        destinationFile.setFolderId(folderId.getFolderId());
        
        fileAccess.expectCall("saveDocument", file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER).andDo(setId);
        
        // Lastly the original must be deleted
        
        fileAccess.expectCall("removeDocument", Arrays.asList(new FileStorageFileAccess.IDTuple(fileId2.getFolderId(), fileId2.getFileId())), 1337l);
                
        move(file, null, 1337, null);
        
        fileAccess.assertAllWereCalled();
        
        // The document will receive a new ID
        assertEquals(file.getId(), fileId.toUniqueID());
    }
    
    @Test
    public void testMovePartialFileWithoutUpload() throws OXException {
        File file = new DefaultFile();
        file.setId(fileId2.toUniqueID()); // We start in FileStore 2
        file.setFolderId(folderId.toUniqueID()); // And want to move to FileStore 1
        file.setTitle("New Title"); // And we want to set a new title
        
        // No Input Stream is provided, so load the file from the source store
        fileAccess.expectCall("getDocument", fileId2.getFolderId(), fileId2.getFileId(), FileStorageFileAccess.CURRENT_VERSION).andReturn(EMPTY_INPUT_STREAM);
        
        // Since this is only a partial file, firstly the original document should be loaded
        File storedFile = new DefaultFile();
        storedFile.setTitle("Old Title");
        storedFile.setDescription("Old Description"); // We want to keep the old description
        fileAccess.expectCall("getFileMetadata", fileId2.getFolderId(), fileId2.getFileId(), FileStorageFileAccess.CURRENT_VERSION).andReturn(storedFile);
        
        // Next the file should be created in the destination as a new file
        File destinationFile = new DefaultFile();
        destinationFile.setId(FileStorageFileAccess.NEW);
        destinationFile.setFolderId(folderId.getFolderId());
        
        fileAccess.expectCall("saveDocument", file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER).andDo(setId);
        
        // And lastly the original must be deleted
        
        fileAccess.expectCall("removeDocument", Arrays.asList(new FileStorageFileAccess.IDTuple(fileId2.getFolderId(), fileId2.getFileId())), 1337l);
                
        move(file, null, 1337, Arrays.asList(File.Field.TITLE, File.Field.FOLDER_ID)); // Title and FolderID have been changed
        
        fileAccess.assertAllWereCalled();
        
        // The document will receive a new ID
        assertEquals(file.getId(), fileId.toUniqueID());
        
        File fileThatWasCreated = setId.getFile();
        
        assertEquals("New Title", fileThatWasCreated.getTitle());
        assertEquals("Old Description", fileThatWasCreated.getDescription());
    }

    private void verifyAccount() {
        assertEquals(fileId.getAccountId(), accountId);
        assertEquals(fileId.getService(), serviceId);
    }

    private void verifyAccount2() {
        assertEquals(fileId2.getAccountId(), accountId2);
        assertEquals(fileId2.getService(), serviceId2);
    }

    public FileStorageAccountAccess getAccountAccess(String accountId, Session session) throws OXException {
        if (this.accountId == null) {
            this.accountId = accountId;
        } else {
            this.accountId2 = accountId;
        }
        assertSame(this.session, session);
        return this;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageService#getAccountManager()
     */
    public FileStorageAccountManager getAccountManager() {
        return this;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageService#getDisplayName()
     */
    public String getDisplayName() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageService#getFormDescription()
     */
    public DynamicFormDescription getFormDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageService#getId()
     */
    public String getId() {
        return "someId";
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageService#getSecretProperties()
     */
    public Set<String> getSecretProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountAccess#getAccountId()
     */
    public String getAccountId() {
        return "someAccount";
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountAccess#getFileAccess()
     */
    public FileStorageFileAccess getFileAccess() throws OXException {
        if (files != null) {
            return files;
        }
        return files = fileAccess.getSim(FileStorageFileAccess.class);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountAccess#getFolderAccess()
     */
    public FileStorageFolderAccess getFolderAccess() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountAccess#getRootFolder()
     */
    public FileStorageFolder getRootFolder() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageResource#cacheable()
     */
    public boolean cacheable() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageResource#close()
     */
    public void close() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageResource#connect()
     */
    public void connect() throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageResource#isConnected()
     */
    public boolean isConnected() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageResource#ping()
     */
    public boolean ping() throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountAccess#getService()
     */
    public FileStorageService getService() {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    protected List<FileStorageService> getAllFileStorageServices() {
        return Arrays.asList((FileStorageService) this, this);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountManager#addAccount(com.openexchange.file.storage.FileStorageAccount,
     * com.openexchange.session.Session)
     */
    public String addAccount(FileStorageAccount account, Session session) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountManager#checkSecretCanDecryptStrings(com.openexchange.session.Session,
     * java.lang.String)
     */
    public boolean checkSecretCanDecryptStrings(Session session, String secret) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountManager#deleteAccount(com.openexchange.file.storage.FileStorageAccount,
     * com.openexchange.session.Session)
     */
    public void deleteAccount(FileStorageAccount account, Session session) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountManager#getAccount(java.lang.String, com.openexchange.session.Session)
     */
    public FileStorageAccount getAccount(String id, Session session) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountManager#getAccounts(com.openexchange.session.Session)
     */
    public List<FileStorageAccount> getAccounts(Session session) throws OXException {
        FileStorageAccount account = new FileStorageAccount() {

                    public Map<String, Object> getConfiguration() {
                // TODO Auto-generated method stub
                return null;
            }

                    public String getDisplayName() {
                // TODO Auto-generated method stub
                return null;
            }

                    public FileStorageService getFileStorageService() {
                // TODO Auto-generated method stub
                return null;
            }

                    public String getId() {
                return "account 23";
            }
        };
        return Arrays.asList(account);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountManager#migrateToNewSecret(java.lang.String, java.lang.String,
     * com.openexchange.session.Session)
     */
    public void migrateToNewSecret(String oldSecret, String newSecret, Session session) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountManager#updateAccount(com.openexchange.file.storage.FileStorageAccount,
     * com.openexchange.session.Session)
     */
    public void updateAccount(FileStorageAccount account, Session session) throws OXException {
        // TODO Auto-generated method stub

    }

    private static final class IDSetter implements Block {

        private String id;
        private File file;

        private IDSetter(String id) {
            super();
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

            public Object perform(Object self, Object... arguments) {
            (this.file = (File) arguments[0]).setId(id);
            return null;
        }
        
        public File getFile() {
            return file;
        }

    }

    public FileStorageAccountManagerProvider getProvider() {
        // TODO Auto-generated method stub
        return null;
    }

}
