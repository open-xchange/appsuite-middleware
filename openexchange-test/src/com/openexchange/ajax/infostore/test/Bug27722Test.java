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

package com.openexchange.ajax.infostore.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    public void testDeleteManyFiles() throws Exception {
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
         * execute delete request
         */
        itm.deleteAction(objectIDs, folderIDs, itm.getLastResponse().getTimestamp());
        long duration = itm.getLastResponse().getRequestDuration();
        assertTrue("deletion took " + duration + "ms, which is too long", 10 * DELETED_ITEMS > duration); // allow 10ms per item
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
