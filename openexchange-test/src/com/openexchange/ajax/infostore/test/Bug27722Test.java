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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;

/**
 * {@link Bug27722Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Bug27722Test extends AbstractInfostoreTest {

    private static final int TOTAL_ITEMS = 100/* 00 */;  // don't test that much files in continuous build
    private static final int DELETED_ITEMS = 50/* 00 */; // don't test that much files in continuous build

    private FolderObject testFolder;
    private List<File> items;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testFolder = ftm.generatePrivateFolder(UUID.randomUUID().toString(), FolderObject.INFOSTORE, getClient().getValues().getPrivateInfostoreFolder(), getClient().getValues().getUserId());
        testFolder = ftm.insertFolderOnServer(testFolder);
        items = new ArrayList<File>(TOTAL_ITEMS);
        for (int i = 0; i < TOTAL_ITEMS; i++) {
            java.io.File tempFile = null;
            try {
                FileOutputStream outputStream = null;
                try {
                    tempFile = java.io.File.createTempFile("file_" + i, ".tst");
                    tempFile.deleteOnExit();
                    outputStream = new FileOutputStream(tempFile);
                    outputStream.write(UUIDs.toByteArray(UUID.randomUUID()));
                    outputStream.flush();
                } finally {
                    Streams.close(outputStream);
                }

                File document = new DefaultFile();
                document.setFolderId(String.valueOf(testFolder.getObjectID()));
                document.setTitle(tempFile.getName());
                document.setFileName(tempFile.getName());
                document.setVersion(String.valueOf(1));
                document.setFileSize(tempFile.length());

                itm.newAction(document, tempFile);
                items.add(document);
            } finally {
                if (null != tempFile) {
                    tempFile.delete();
                }
            }
        }
    }

    @Test
    public void testHardDeleteManyFiles() throws Exception {
        /*
         * pick DELETED_ITEMS randomly
         */
        List<String> objectIDs = new ArrayList<String>(DELETED_ITEMS);
        List<String> folderIDs = new ArrayList<String>(DELETED_ITEMS);
        Random random = new Random();
        while (objectIDs.size() < DELETED_ITEMS) {
            File randomDocument = items.get(random.nextInt(TOTAL_ITEMS));
            String objectID = randomDocument.getId();
            if (false == objectIDs.contains(objectID)) {
                objectIDs.add(objectID);
                folderIDs.add(randomDocument.getFolderId());
            }
        }
        /*
         * execute hard delete request
         */
        itm.deleteAction(objectIDs, folderIDs, itm.getLastResponse().getTimestamp(), Boolean.TRUE);
        assertNull(itm.getLastResponse().getException());
        long duration = itm.getLastResponse().getRequestDuration();
        assertTrue("hard deletion took " + duration + "ms, which is too long", 50 * DELETED_ITEMS > duration); // allow 50ms per hard-deleted item
        /*
         * verify deletion
         */
        int[] columns = { CommonObject.OBJECT_ID };
        List<File> all = itm.getAll(testFolder.getObjectID(), columns);

        assertEquals("Unexpected object count", TOTAL_ITEMS - DELETED_ITEMS, all.size());
        for (File file : all) {
            String objectID = file.getId();
            assertFalse("Object not deleted", objectIDs.contains(objectID));
        }
    }

}
