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

package com.openexchange.ajax.infostore.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Date;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.folder.AbstractObjectCountTest;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.folderstorage.Folder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.test.FolderTestManager;

/**
 * {@link InfostoreObjectCountTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.2.2
 */
public final class InfostoreObjectCountTest extends AbstractObjectCountTest {

    /**
     * Initializes a new {@link InfostoreObjectCountTest}.
     *
     * @param name
     */
    public InfostoreObjectCountTest() {
        super();
    }

    @Test
    public void testCountInPrivateInfostoreFolder_AddedOne_CountReturnsOne() throws Exception {
        FolderTestManager folderTestManager = new FolderTestManager(client1);
        InfostoreTestManager infostoreTestManager = new InfostoreTestManager(client1);

        try {
            FolderObject created = createPrivateFolder(client1, folderTestManager, FolderObject.INFOSTORE);
            Folder folder = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            int objectsInFolder = folder.getTotal();
            assertEquals("Wrong object count", 0, objectsInFolder);

            File expected = createDocumentMetadata(folder);
            createAndTestSuccess(expected, infostoreTestManager);

            Folder reloaded = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            objectsInFolder = reloaded.getTotal();
            assertEquals("Wrong object count", 1, objectsInFolder);
        } finally {
            infostoreTestManager.cleanUp();
            folderTestManager.cleanUp();
        }
    }

    private void createAndTestSuccess(File fileToCreate, InfostoreTestManager infostoreTestManager) throws OXException, IOException, JSONException {
        infostoreTestManager.newAction(fileToCreate);
        {
            OXException exception = infostoreTestManager.getLastResponse().getException();
            if (null != exception) {
                fail("Creating an entry should work, but failed with an unexpected exception: " + exception.getMessage());
            }
        }
    }

    @Test
    public void testCountInPrivateInfostoreFolder_AddedFive_CountReturnsFive() throws Exception {
        FolderTestManager folderTestManager = new FolderTestManager(client1);
        InfostoreTestManager infostoreTestManager = new InfostoreTestManager(client1);

        try {
            FolderObject created = createPrivateFolder(client1, folderTestManager, FolderObject.INFOSTORE);
            Folder folder = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 0, folder.getTotal());

            File expected = createDocumentMetadata(folder);
            createAndTestSuccess(expected, infostoreTestManager);
            createAndTestSuccess(expected, infostoreTestManager);
            createAndTestSuccess(expected, infostoreTestManager);
            createAndTestSuccess(expected, infostoreTestManager);
            createAndTestSuccess(expected, infostoreTestManager);

            Folder reloaded = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 5, reloaded.getTotal());
        } finally {
            infostoreTestManager.cleanUp();
            folderTestManager.cleanUp();
        }
    }

    @Test
    public void testCountInSharedInfostoreFolder_AddFiveFromOwner_CountReturnsFiveToOwner() throws Exception {
        InfostoreTestManager infostoreTestManager = new InfostoreTestManager(client1);
        FolderTestManager folderTestManager = new FolderTestManager(client1);

        try {
            OCLPermission permissionUser1 = new OCLPermission();
            permissionUser1.setEntity(client1.getValues().getUserId());
            permissionUser1.setGroupPermission(false);
            permissionUser1.setFolderAdmin(true);
            permissionUser1.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);

            OCLPermission permissionUser2 = new OCLPermission();
            permissionUser2.setEntity(client2.getValues().getUserId());
            permissionUser2.setGroupPermission(false);
            permissionUser2.setFolderAdmin(false);
            permissionUser2.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_OWN_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);

            FolderObject created = createSharedFolder(client1, FolderObject.INFOSTORE, folderTestManager, permissionUser1, permissionUser2);
            Folder folder = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 0, folder.getTotal());

            File expected = createDocumentMetadata(folder);
            createAndTestSuccess(expected, infostoreTestManager);
            createAndTestSuccess(expected, infostoreTestManager);
            createAndTestSuccess(expected, infostoreTestManager);
            createAndTestSuccess(expected, infostoreTestManager);
            createAndTestSuccess(expected, infostoreTestManager);

            Folder reloaded = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 5, reloaded.getTotal());

            Folder reloaded2 = getFolder(client2, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Other client is able to see objects", 0, reloaded2.getTotal());
        } finally {
            infostoreTestManager.cleanUp();
            folderTestManager.cleanUp();
        }
    }

    @Test
    public void testCountInSharedInfostoreFolder_AddFiveFromUserWithPermission_CountReturnsFiveToUserWithPermission() throws Exception {
        InfostoreTestManager infostoreTestManager = new InfostoreTestManager(client2);
        FolderTestManager folderTestManager = new FolderTestManager(client1);

        try {
            OCLPermission permissionUser1 = new OCLPermission();
            permissionUser1.setEntity(client1.getValues().getUserId());
            permissionUser1.setGroupPermission(false);
            permissionUser1.setFolderAdmin(true);
            permissionUser1.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_OWN_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);

            OCLPermission permissionUser2 = new OCLPermission();
            permissionUser2.setEntity(client2.getValues().getUserId());
            permissionUser2.setGroupPermission(false);
            permissionUser2.setFolderAdmin(false);
            permissionUser2.setAllPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER, OCLPermission.READ_OWN_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.DELETE_ALL_OBJECTS);

            FolderObject created = createSharedFolder(client1, FolderObject.INFOSTORE, folderTestManager, permissionUser1, permissionUser2);
            Folder folder = getFolder(client2, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 0, folder.getTotal());

            File expected = createDocumentMetadata(folder);
            createAndTestSuccess(expected, infostoreTestManager);
            createAndTestSuccess(expected, infostoreTestManager);
            createAndTestSuccess(expected, infostoreTestManager);
            createAndTestSuccess(expected, infostoreTestManager);
            createAndTestSuccess(expected, infostoreTestManager);

            Folder reloaded = getFolder(client2, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 5, reloaded.getTotal());

            Folder reloaded2 = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 0, reloaded2.getTotal());
        } finally {
            infostoreTestManager.cleanUp();
            folderTestManager.cleanUp();
        }
    }

    /**
     * Creates a {@link DocumentMetadata} for further processing
     *
     * @param folder - the folder to create the {@link DocumentMetadata} for
     * @return {@link DocumentMetadata} - created object
     */
    private File createDocumentMetadata(Folder folder) {
        File expected = new DefaultFile();
        expected.setCreated(new Date());
        expected.setFolderId(folder.getID());
        expected.setTitle("InfostoreCountTest Item");
        expected.setLastModified(new Date());
        return expected;
    }
}
