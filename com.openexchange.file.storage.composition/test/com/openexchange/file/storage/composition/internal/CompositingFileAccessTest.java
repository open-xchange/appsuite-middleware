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

package com.openexchange.file.storage.composition.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageLockedFileAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.FileStorageVersionedFileAccess;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.session.SimSession;
import com.openexchange.sim.Block;
import com.openexchange.sim.SimBuilder;
import com.openexchange.threadpool.SimThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link CompositingFileAccessTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ThreadPools.class })
public class CompositingFileAccessTest extends AbstractCompositingIDBasedFileAccess implements FileStorageService, FileStorageAccountAccess, FileStorageAccountManager {

    private static final InputStream EMPTY_INPUT_STREAM = new ByteArrayInputStream(new byte[0]);

    private String serviceId;

    private FileStorageFileAccess files;

    private final SimBuilder fileAccess = new SimBuilder();

    private String accountId;

    private final FileID fileId = new FileID("com.openexchange.test", "account 23", "folder", "id");

    private final FolderID folderId = new FolderID(fileId.getService(), fileId.getAccountId(), fileId.getFolderId());

    private String serviceId2;

    private String accountId2;

    private final FileID fileId2 = new FileID("com.openexchange.test2", "account 12", "folder2", "id2");

    private final FolderID folderId2 = new FolderID(fileId2.getService(), fileId2.getAccountId(), fileId2.getFolderId());

    private final IDSetter setId = new IDSetter(fileId.getFileId());

    public CompositingFileAccessTest() {
        super(new SimSession());
    }

    @Test
    public void testExists() throws OXException {
        fileAccess.expectCall("exists", fileId.getFolderId(), fileId.getFileId(), "12").andReturn(true);

        assertTrue(exists(fileId.toUniqueID(), "12"));
        verifyAccount();

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testGetDeltaWithoutSort() throws OXException {

        fileAccess.expectCall("getDelta", folderId.getFolderId(), 12L, Arrays.asList(
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

        fileAccess.expectCall("getDelta", folderId.getFolderId(), 12L, Arrays.asList(
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
        fileAccess.expectCall("getDocument", fileId.getFolderId(), fileId.getFileId(), "12");

        getDocument(fileId.toUniqueID(), "12");

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
        final DefaultFile defaultFile = new DefaultFile();
        defaultFile.setLastModified(new Date());
        defaultFile.setId(fileId.getFileId());
        defaultFile.setFolderId(fileId.getFolderId());


        final FileStorageFileAccess.IDTuple tuple = new FileStorageFileAccess.IDTuple(fileId.getFolderId(), fileId.getFileId());
        final FileStorageFileAccess.IDTuple tuple2 = new FileStorageFileAccess.IDTuple(fileId2.getFolderId(), fileId2.getFileId());

        fileAccess.expectCall("hashCode").andReturn(1);// Look if it's there
        //      fileAccess.expectCall("hashCode").andReturn(1); // Store it (uncomment this line if running in eclipse.
        // There is an optimization when running on jenkins, as there is no second hash needed
        fileAccess.expectCall("hashCode").andReturn(1);
        fileAccess.expectCall("getDocuments", Arrays.asList(tuple,tuple2), Arrays.asList(
            File.Field.TITLE));
        fileAccess.expectCall("getAccountAccess").andReturn(this);
        fileAccess.expectCall("getAccountAccess").andReturn(this);

        getDocuments(Arrays.asList(fileId.toUniqueID(), fileId2.toUniqueID()), Arrays.asList(File.Field.TITLE));

        verifyAccount();
        verifyAccount2();

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testGetFileMetadata() throws OXException {
        final DefaultFile file = new DefaultFile();
        file.setId(fileId.getFileId());
        file.setFolderId(fileId.getFolderId());

        fileAccess.expectCall("getFileMetadata", fileId.getFolderId(), fileId.getFileId(), "12").andReturn(file);

        getFileMetadata(fileId.toUniqueID(), "12");

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
        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("lock", fileId.getFolderId(), fileId.getFileId(), 1337L);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        lock(fileId.toUniqueID(), 1337);

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testUnlock() throws OXException {
        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("unlock", fileId.getFolderId(), fileId.getFileId());
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        unlock(fileId.toUniqueID());

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testTouch() throws OXException {
        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("touch", fileId.getFolderId(), fileId.getFileId());
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        touch(fileId.toUniqueID());

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testRemoveDocument() throws OXException {
        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("removeDocument", folderId.getFolderId(), 12L);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        removeDocument(folderId.toUniqueID(), 12);

        fileAccess.assertAllWereCalled();
    }

    // Somewhat brittle test
    @Test
    public void testRemoveDocuments() throws OXException {
        final FileStorageFileAccess.IDTuple tuple = new FileStorageFileAccess.IDTuple(fileId.getFolderId(), fileId.getFileId());
        final FileStorageFileAccess.IDTuple tuple2 = new FileStorageFileAccess.IDTuple(fileId2.getFolderId(), fileId2.getFileId());
        fileAccess.expectCall("hashCode").andReturn(1); // Look if it's there
//        fileAccess.expectCall("hashCode").andReturn(1); // Store it (uncomment this line if running in eclipse.
        // There is an optimization when running on jenkins, as there is no second hash needed for storing)
        fileAccess.expectCall("hashCode").andReturn(1);

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("removeDocument", Arrays.asList(tuple, tuple2), 12L, false).andReturn(Arrays.asList(tuple));
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        fileAccess.expectCall("getAccountAccess").andReturn(this);
        fileAccess.expectCall("getAccountAccess").andReturn(this);


        final List<String> ids = Arrays.asList(fileId.toUniqueID(), fileId2.toUniqueID());
        final List<String> conflicted = removeDocument(ids, 12);

        assertEquals(Arrays.asList(new FileID(getId(), getAccountId(), fileId.getFolderId(), fileId.getFileId()).toUniqueID()), conflicted);

        verifyAccount();
        verifyAccount2();

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testRemoveVersions() throws OXException {
        final String[] versions = new String[] { "1", "2", "3" };

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("removeVersion", fileId.getFolderId(), fileId.getFileId(), versions).andReturn(new String[0]);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");
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
            File.Field.LAST_MODIFIED), folderId.getFolderId(), false, File.Field.TITLE, SortDirection.DESC, 10, 20);

        search("query", Arrays.asList(File.Field.TITLE), folderId.toUniqueID(), false, File.Field.TITLE, SortDirection.DESC, 10, 20);

        verifyAccount();

        fileAccess.assertAllWereCalled();
    }

    @Test
    public void testSearchInAllFolders() throws OXException {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(ThreadPools.class);

        SimThreadPoolService testThreadPool = new SimThreadPoolService();
        PowerMockito.when(ThreadPools.getThreadPool()).thenReturn(testThreadPool);

        fileAccess.expectCall("getAccountAccess").andReturn(this);
        fileAccess.expectCall("getAccountAccess").andReturn(this);

        search("query", Arrays.asList(File.Field.TITLE), FileStorageFileAccess.ALL_FOLDERS, File.Field.TITLE, SortDirection.DESC, 10, 20);

        fileAccess.assertAllWereCalled();
    }

    // create file
    @Test
    public void testCreateDocument1() throws OXException {
        final File file = new DefaultFile();
        file.setId(FileStorageFileAccess.NEW);
        file.setFolderId(folderId.toUniqueID());
        IDTuple tuple = new IDTuple(folderId.getFolderId(), fileId.getFileId());

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("saveDocument", file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER).andReturn(tuple);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        saveDocument(file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);

        verifyAccount();
        fileAccess.assertAllWereCalled();

        assertEquals(file.getId(), fileId.toUniqueID());
    }

    @Test
    public void testCreateDocument2() throws OXException {
        final File file = new DefaultFile();
        file.setId(FileStorageFileAccess.NEW);
        file.setFolderId(folderId.toUniqueID());
        IDTuple tuple = new IDTuple(folderId.getFolderId(), fileId.getFileId());

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("saveDocument", file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE)).andReturn(tuple);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        saveDocument(file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE));

        verifyAccount();
        fileAccess.assertAllWereCalled();

        assertEquals(file.getId(), fileId.toUniqueID());
    }

    @Test
    public void testCreateMetadata1() throws OXException {
        final File file = new DefaultFile();
        file.setId(FileStorageFileAccess.NEW);
        file.setFolderId(folderId.toUniqueID());
        IDTuple tuple = new IDTuple(folderId.getFolderId(), fileId.getFileId());

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("saveFileMetadata", file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE)).andReturn(tuple);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        saveFileMetadata(file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE));

        verifyAccount();
        fileAccess.assertAllWereCalled();

        assertEquals(file.getId(), fileId.toUniqueID());
    }

    @Test
    public void testCreateMetadata2() throws OXException {
        final File file = new DefaultFile();
        file.setId(FileStorageFileAccess.NEW);
        file.setFolderId(folderId.toUniqueID());
        IDTuple tuple = new IDTuple(folderId.getFolderId(), fileId.getFileId());

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("saveFileMetadata", file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE)).andReturn(tuple);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        saveFileMetadata(file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE));

        verifyAccount();
        fileAccess.assertAllWereCalled();

        assertEquals(file.getId(), fileId.toUniqueID());
    }

    // update file

    @Test
    public void testUpdateDocument1() throws OXException {
        final File file = new DefaultFile();
        file.setId(fileId.toUniqueID());
        file.setFolderId(folderId.toUniqueID());
        IDTuple tuple = new IDTuple(folderId.getFolderId(), fileId.getFileId());

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("saveDocument", file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Collections.<File.Field>emptyList()).andReturn(tuple);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        saveDocument(file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Collections.<File.Field>emptyList());

        verifyAccount();
        fileAccess.assertAllWereCalled();

        assertEquals(file.getId(), fileId.getFileId());
    }

    @Test
    public void testUpdateDocument2() throws OXException {
        final File file = new DefaultFile();
        file.setId(fileId.toUniqueID());
        file.setFolderId(folderId.toUniqueID());
        IDTuple tuple = new IDTuple(folderId.getFolderId(), fileId.getFileId());

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("saveDocument", file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE)).andReturn(tuple);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        saveDocument(file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE));

        verifyAccount();
        fileAccess.assertAllWereCalled();

        assertEquals(file.getId(), fileId.getFileId());
    }

    @Test
    public void testUpdateMetadata1() throws OXException {
        final File file = new DefaultFile();
        file.setId(fileId.toUniqueID());
        file.setFolderId(folderId.toUniqueID());
        IDTuple tuple = new IDTuple(folderId.getFolderId(), fileId.getFileId());

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("saveFileMetadata", file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE)).andReturn(tuple);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        saveFileMetadata(file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE));

        verifyAccount();
        fileAccess.assertAllWereCalled();

        assertEquals(file.getId(), fileId.getFileId());
    }

    @Test
    public void testUpdateMetadata2() throws OXException {
        final File file = new DefaultFile();
        file.setId(fileId.toUniqueID());
        file.setFolderId(folderId.toUniqueID());
        IDTuple tuple = new IDTuple(folderId.getFolderId(), fileId.getFileId());

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("saveFileMetadata", file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE)).andReturn(tuple);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        saveFileMetadata(file, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, Arrays.asList(File.Field.TITLE));

        verifyAccount();
        fileAccess.assertAllWereCalled();

        assertEquals(file.getId(), fileId.getFileId());
    }

    // Moving across filestores

    //TODO: don't know how to properly include the return value here
    //@Test
    public void testMoveACompleteFileWithANewUpload() throws OXException {
        final File file = new DefaultFile();
        file.setId(fileId2.toUniqueID()); // We start in FileStore 2
        file.setFolderId(folderId.toUniqueID()); // And want to move to FileStore 1

        // Firstly the file should be created in the destination as a new file
        final File destinationFile = new DefaultFile();
        destinationFile.setId(FileStorageFileAccess.NEW);
        destinationFile.setFolderId(folderId.getFolderId());

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("saveDocument", file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER).andDo(setId);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        // Secondly the original must be deleted

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("removeDocument", Arrays.asList(new FileStorageFileAccess.IDTuple(fileId2.getFolderId(), fileId2.getFileId())), 1337L, true);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        move(file, EMPTY_INPUT_STREAM, 1337, null, true);

        verifyAccount(); // Store on destination account then
        verifyAccount2(); // Remove from source account

        fileAccess.assertAllWereCalled();

        // The document will receive a new ID
        assertEquals(file.getId(), fileId.toUniqueID());
    }

    //TODO: don't know how to properly include the return value here
    //@Test
    public void testPartialMetadataWithANewUpload() throws OXException {
        final File file = new DefaultFile();
        file.setId(fileId2.toUniqueID()); // We start in FileStore 2
        file.setFolderId(folderId.toUniqueID()); // And want to move to FileStore 1
        file.setTitle("New Title"); // And we want to set a new title

        // Since this is only a partial file, firstly the original document should be loaded
        final File storedFile = new DefaultFile();
        storedFile.setTitle("Old Title");
        storedFile.setDescription("Old Description"); // We want to keep the old description
        fileAccess.expectCall("getFileMetadata", fileId2.getFolderId(), fileId2.getFileId(), FileStorageFileAccess.CURRENT_VERSION).andReturn(storedFile);

        // Next the file should be created in the destination as a new file
        final File destinationFile = new DefaultFile();
        destinationFile.setId(FileStorageFileAccess.NEW);
        destinationFile.setFolderId(folderId.getFolderId());

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("saveDocument", file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER).andDo(setId);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        // And lastly the original must be deleted

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("removeDocument", Arrays.asList(new FileStorageFileAccess.IDTuple(fileId2.getFolderId(), fileId2.getFileId())), 1337L, true);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        move(file, EMPTY_INPUT_STREAM, 1337, Arrays.asList(File.Field.TITLE, File.Field.FOLDER_ID), true); // Title and FolderID have been changed

        fileAccess.assertAllWereCalled();

        // The document will receive a new ID
        assertEquals(file.getId(), fileId.toUniqueID());

        final File fileThatWasCreated = setId.getFile();

        assertEquals("New Title", fileThatWasCreated.getTitle());
        assertEquals("Old Description", fileThatWasCreated.getDescription());
    }

    //TODO: don't know how to properly include the return value here
    //@Test
    public void testMoveCompleteFileWithoutUpload() throws OXException {
        final File file = new DefaultFile();
        file.setId(fileId2.toUniqueID()); // We start in FileStore 2
        file.setFolderId(folderId.toUniqueID()); // And want to move to FileStore 1

        // No Input Stream is provided, so load the file from the source store
        fileAccess.expectCall("getDocument", fileId2.getFolderId(), fileId2.getFileId(), FileStorageFileAccess.CURRENT_VERSION).andReturn(EMPTY_INPUT_STREAM);

        // Next the file should be created in the destination as a new file, with the input stream provided above
        final File destinationFile = new DefaultFile();
        destinationFile.setId(FileStorageFileAccess.NEW);
        destinationFile.setFolderId(folderId.getFolderId());

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("saveDocument", file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER).andDo(setId);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        // Lastly the original must be deleted

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("removeDocument", Arrays.asList(new FileStorageFileAccess.IDTuple(fileId2.getFolderId(), fileId2.getFileId())), 1337L, true);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        move(file, null, 1337, null, true);

        fileAccess.assertAllWereCalled();

        // The document will receive a new ID
        assertEquals(file.getId(), fileId.toUniqueID());
    }

    //TODO: don't know how to properly include the return value here
    //@Test
    public void testMovePartialFileWithoutUpload() throws OXException {
        final File file = new DefaultFile();
        file.setId(fileId2.toUniqueID()); // We start in FileStore 2
        file.setFolderId(folderId.toUniqueID()); // And want to move to FileStore 1
        file.setTitle("New Title"); // And we want to set a new title

        // No Input Stream is provided, so load the file from the source store
        fileAccess.expectCall("getDocument", fileId2.getFolderId(), fileId2.getFileId(), FileStorageFileAccess.CURRENT_VERSION).andReturn(EMPTY_INPUT_STREAM);

        // Since this is only a partial file, firstly the original document should be loaded
        final File storedFile = new DefaultFile();
        storedFile.setTitle("Old Title");
        storedFile.setDescription("Old Description"); // We want to keep the old description
        fileAccess.expectCall("getFileMetadata", fileId2.getFolderId(), fileId2.getFileId(), FileStorageFileAccess.CURRENT_VERSION).andReturn(storedFile);

        // Next the file should be created in the destination as a new file
        final File destinationFile = new DefaultFile();
        destinationFile.setId(FileStorageFileAccess.NEW);
        destinationFile.setFolderId(folderId.getFolderId());

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("saveDocument", file, EMPTY_INPUT_STREAM, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER).andDo(setId);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        // And lastly the original must be deleted

        fileAccess.expectCall("startTransaction");
        fileAccess.expectCall("removeDocument", Arrays.asList(new FileStorageFileAccess.IDTuple(fileId2.getFolderId(), fileId2.getFileId())), 1337L, true);
        fileAccess.expectCall("commit");
        fileAccess.expectCall("finish");

        move(file, null, 1337, Arrays.asList(File.Field.TITLE, File.Field.FOLDER_ID), true); // Title and FolderID have been changed

        fileAccess.assertAllWereCalled();

        // The document will receive a new ID
        assertEquals(file.getId(), fileId.toUniqueID());

        final File fileThatWasCreated = setId.getFile();

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

    @Override
    public FileStorageAccountAccess getAccountAccess(final String accountId, final Session session) throws OXException {
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
    @Override
    public FileStorageAccountManager getAccountManager() {
        return this;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageService#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        // Nothing to do
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageService#getFormDescription()
     */
    @Override
    public DynamicFormDescription getFormDescription() {
        // Nothing to do
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageService#getId()
     */
    @Override
    public String getId() {
        return "someId";
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageService#getSecretProperties()
     */
    @Override
    public Set<String> getSecretProperties() {
        // Nothing to do
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountAccess#getAccountId()
     */
    @Override
    public String getAccountId() {
        return "someAccount";
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountAccess#getFileAccess()
     */
    @Override
    public FileStorageFileAccess getFileAccess() throws OXException {
        if (files != null) {
            return files;
        }
        return files = fileAccess.getSim(FileStorageFileAccess.class, FileStorageVersionedFileAccess.class, FileStorageLockedFileAccess.class);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountAccess#getFolderAccess()
     */
    @Override
    public FileStorageFolderAccess getFolderAccess() throws OXException {
        // Nothing to do
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountAccess#getRootFolder()
     */
    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        // Nothing to do
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageResource#cacheable()
     */
    @Override
    public boolean cacheable() {
        // Nothing to do
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageResource#close()
     */
    @Override
    public void close() {
        // Nothing to do

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageResource#connect()
     */
    @Override
    public void connect() throws OXException {
        // Nothing to do

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageResource#isConnected()
     */
    @Override
    public boolean isConnected() {
        // Nothing to do
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageResource#ping()
     */
    @Override
    public boolean ping() throws OXException {
        // Nothing to do
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountAccess#getService()
     */
    @Override
    public FileStorageService getService() {
        // Nothing to do
        return this;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountManager#addAccount(com.openexchange.file.storage.FileStorageAccount,
     * com.openexchange.session.Session)
     */
    @Override
    public String addAccount(final FileStorageAccount account, final Session session) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountManager#hasEncryptedItems(com.openexchange.session.Session)
     */
    @Override
    public boolean hasEncryptedItems(final Session session) throws OXException {
        // Nothing to do
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountManager#deleteAccount(com.openexchange.file.storage.FileStorageAccount,
     * com.openexchange.session.Session)
     */
    @Override
    public void deleteAccount(final FileStorageAccount account, final Session session) throws OXException {
        // Nothing to do

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountManager#getAccount(java.lang.String, com.openexchange.session.Session)
     */
    @Override
    public FileStorageAccount getAccount(final String id, final Session session) throws OXException {
        // Nothing to do
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageAccountManager#getAccounts(com.openexchange.session.Session)
     */
    @Override
    public List<FileStorageAccount> getAccounts(final Session session) throws OXException {
        final FileStorageAccount account = new FileStorageAccount() {

                    @Override
                    public Map<String, Object> getConfiguration() {
                // Nothing to do
                return null;
            }

                    @Override
                    public String getDisplayName() {
                // Nothing to do
                return null;
            }

                    @Override
                    public FileStorageService getFileStorageService() {
                // Nothing to do
                return null;
            }

                    @Override
                    public String getId() {
                return "account 23";
            }
        };
        return Arrays.asList(account);
    }

    @Override
    public void migrateToNewSecret(final String oldSecret, final String newSecret, final Session session) throws OXException {
        // Nothing to do

    }

    @Override
    public void cleanUp(String secret, Session session) throws OXException {
        // noop
    }

    @Override
    public void removeUnrecoverableItems(String secret, Session session) throws OXException {
        // noop
    }

    @Override
    public void updateAccount(final FileStorageAccount account, final Session session) throws OXException {
        // Nothing to do

    }

    private static final class IDSetter implements Block {

        private String id;
        private File file;

        private IDSetter(final String id) {
            super();
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }

            @Override
            public Object perform(final Object self, final Object... arguments) {
            (this.file = (File) arguments[0]).setId(id);
            return null;
        }

        public File getFile() {
            return file;
        }

    }

    public FileStorageAccountManagerProvider getProvider() {
        // Nothing to do
        return null;
    }

    @Override
    protected EventAdmin getEventAdmin() {
        return new EventAdmin() {

            @Override
            public void sendEvent(Event arg0) {
                // Nothing to do

            }

            @Override
            public void postEvent(Event arg0) {
                // Nothing to do

            }
        };
    }

    @Override
    protected FileStorageServiceRegistry getFileStorageServiceRegistry() {
        final FileStorageService thisService = this;
        return new FileStorageServiceRegistry() {

            @Override
            public FileStorageService getFileStorageService(String id) throws OXException {
                if (serviceId == null) {
                    serviceId = id;
                } else {
                    serviceId2 = id;
                }
                return thisService;
            }

            @Override
            public List<FileStorageService> getAllServices() throws OXException {
                return Arrays.asList(thisService, thisService);
            }

            @Override
            public boolean containsFileStorageService(String id) {
                return true;
            }
        };
    }

}
