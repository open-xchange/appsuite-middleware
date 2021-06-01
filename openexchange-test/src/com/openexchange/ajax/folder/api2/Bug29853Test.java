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

package com.openexchange.ajax.folder.api2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Iterator;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug29853Test}
 *
 * Folder with the same name on the server
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class Bug29853Test extends AbstractFolderTest {

    private static final int THREAD_COUNT = 20;

    private FolderObject folder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        folder = createSingle(FolderObject.INFOSTORE, UUID.randomUUID().toString());
        folder.setParentFolderID(client.getValues().getPrivateInfostoreFolder());
        InsertRequest insertRequest = new InsertRequest(EnumAPI.OX_NEW, folder);
        InsertResponse insertResponse = client.execute(insertRequest);
        insertResponse.fillObject(folder);
    }

    @Test
    public void testCreateWithSameName() throws Throwable {
        String folderName = "a";
        FolderObject subfolder = createSingle(FolderObject.INFOSTORE, folderName);
        subfolder.setParentFolderID(folder.getObjectID());
        final InsertRequest insertRequest = new InsertRequest(EnumAPI.OX_NEW, subfolder);
        Runnable insertFolderRunnable = new Runnable() {

            @Override
            public void run() {
                client.executeSafe(insertRequest);
            }
        };
        Thread[] insertThreads = new Thread[THREAD_COUNT];
        for (int i = 0; i < insertThreads.length; i++) {
            insertThreads[i] = new Thread(insertFolderRunnable);
            insertThreads[i].start();
        }
        for (int i = 0; i < insertThreads.length; i++) {
            insertThreads[i].join();
        }
        ListRequest listRequest = new ListRequest(EnumAPI.OX_NEW, folder.getObjectID());
        ListResponse listResponse = client.execute(listRequest);
        Iterator<FolderObject> iterator = listResponse.getFolder();
        assertTrue("No folder created", iterator.hasNext());
        FolderObject createdFolder = iterator.next();
        assertNotNull("No folder created", createdFolder);
        assertEquals("Folder name wrong", folderName, createdFolder.getFolderName());
        assertFalse("Folder was created " + listResponse.getArray().length + " times: " + listResponse.getResponse(), iterator.hasNext());
    }

    @Test
    public void testRenameToSameName() throws Throwable {
        FolderObject[] subfolders = new FolderObject[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            subfolders[i] = createSingle(FolderObject.INFOSTORE, "f" + i);
            subfolders[i].setParentFolderID(folder.getObjectID());
            InsertRequest insertRequest = new InsertRequest(EnumAPI.OX_NEW, subfolders[i]);
            InsertResponse insertResponse = client.execute(insertRequest);
            insertResponse.fillObject(subfolders[i]);
        }
        String folderName = "a";
        Thread[] updateThreads = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            subfolders[i].setFolderName(folderName);
            final UpdateRequest updateRequest = new UpdateRequest(EnumAPI.OX_NEW, subfolders[i]);
            Runnable updateRunnable = new Runnable() {

                @Override
                public void run() {
                    client.executeSafe(updateRequest);
                }
            };
            updateThreads[i] = new Thread(updateRunnable);
            updateThreads[i].start();
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            updateThreads[i].join();
        }
        ListRequest listRequest = new ListRequest(EnumAPI.OX_NEW, folder.getObjectID());
        ListResponse listResponse = client.execute(listRequest);
        FolderObject renamedFolder = null;
        Iterator<FolderObject> iterator = listResponse.getFolder();
        while (iterator.hasNext()) {
            FolderObject folder = iterator.next();
            if (folderName.equals(folder.getFolderName())) {
                if (null == renamedFolder) {
                    renamedFolder = folder;
                } else {
                    fail("Folder was renamed more than once: " + listResponse.getResponse());
                }
            }
        }
        assertNotNull("No folder renamed", renamedFolder);
    }

    @Test
    public void testMoveWithSameName() throws Throwable {
        /*
         * Insert subfolders
         */
        FolderObject[] subfolders = new FolderObject[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            subfolders[i] = createSingle(FolderObject.INFOSTORE, "f" + i);
            subfolders[i].setParentFolderID(folder.getObjectID());
            InsertRequest insertRequest = new InsertRequest(EnumAPI.OX_NEW, subfolders[i]);
            InsertResponse insertResponse = client.execute(insertRequest);
            insertResponse.fillObject(subfolders[i]);
        }
        /*
         * Create a folder with the exact same name in each subfolder
         */
        String folderName = "a";
        FolderObject[] subSubfolders = new FolderObject[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            subSubfolders[i] = createSingle(FolderObject.INFOSTORE, folderName);
            subSubfolders[i].setParentFolderID(subfolders[i].getObjectID());
            InsertRequest insertRequest = new InsertRequest(EnumAPI.OX_NEW, subSubfolders[i]);
            InsertResponse insertResponse = client.execute(insertRequest);
            insertResponse.fillObject(subSubfolders[i]);
        }
        /*
         * Try to move each sub-sub-folder into the parent folder
         */
        Thread[] moveThreads = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            subSubfolders[i].setParentFolderID(folder.getObjectID());
            final UpdateRequest updateRequest = new UpdateRequest(EnumAPI.OX_NEW, subSubfolders[i]);
            Runnable moveRunnable = new Runnable() {

                @Override
                public void run() {
                    client.executeSafe(updateRequest);
                }
            };
            moveThreads[i] = new Thread(moveRunnable);
            moveThreads[i].start();
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            moveThreads[i].join();
        }

        /*
         * Check that only one folder was moved successfully
         */
        ListRequest listRequest = new ListRequest(EnumAPI.OX_NEW, folder.getObjectID());
        ListResponse listResponse = client.execute(listRequest);
        FolderObject movedFolder = null;
        Iterator<FolderObject> iterator = listResponse.getFolder();
        while (iterator.hasNext()) {
            FolderObject folder = iterator.next();
            if (folderName.equals(folder.getFolderName())) {
                if (null == movedFolder) {
                    movedFolder = folder;
                } else {
                    fail("Folder was renamed more than once: " + listResponse.getResponse());
                }
            }
        }
        assertNotNull("No folder renamed", movedFolder);
    }

}
