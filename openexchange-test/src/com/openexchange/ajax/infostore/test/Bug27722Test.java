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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.infostore.actions.AllInfostoreRequest;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;

/**
 * {@link Bug27722Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Bug27722Test extends AbstractInfostoreTest {

    private static final int TOTAL_ITEMS = 100/*00*/;  // don't test that much files in continuous build
    private static final int DELETED_ITEMS = 50/*00*/; // don't test that much files in continuous build

    private FolderObject testFolder;
    private List<DocumentMetadata> items;

    /**
     * Initializes a new {@link Bug27722Test}.
     *
     * @param name
     */
    public Bug27722Test(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testFolder = fMgr.generatePrivateFolder(UUID.randomUUID().toString(), FolderObject.INFOSTORE,
            client.getValues().getPrivateInfostoreFolder(), client.getValues().getUserId());
        testFolder = fMgr.insertFolderOnServer(testFolder);
        items = new ArrayList<DocumentMetadata>(TOTAL_ITEMS);
        for (int i = 0; i < TOTAL_ITEMS; i++) {
            File tempFile = null;
            try {
                FileOutputStream outputStream = null;
                try {
                    tempFile = File.createTempFile("file_" + i, ".tst");
                    tempFile.deleteOnExit();
                    outputStream = new FileOutputStream(tempFile);
                    outputStream.write(UUIDs.toByteArray(UUID.randomUUID()));
                    outputStream.flush();
                } finally {
                    Streams.close(outputStream);
                }

                DocumentMetadata document = new DocumentMetadataImpl();
                document.setFolderId(testFolder.getObjectID());
                document.setTitle(tempFile.getName());
                document.setFileName(tempFile.getName());
                document.setVersion(1);
                document.setFileSize(tempFile.length());

                infoMgr.newAction(document, tempFile);
                items.add(document);
            } finally {
                if (null != tempFile) {
                    tempFile.delete();
                }
            }
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testDeleteManyFiles() throws Exception {
        /*
         * pick DELETED_ITEMS randomly
         */
        List<Integer> objectIDs = new ArrayList<Integer>(DELETED_ITEMS);
        List<Integer> folderIDs = new ArrayList<Integer>(DELETED_ITEMS);
        Random random = new Random();
        while (objectIDs.size() < DELETED_ITEMS) {
            DocumentMetadata randomDocument = items.get(random.nextInt(TOTAL_ITEMS));
            Integer objectID = Integer.valueOf(randomDocument.getId());
            if (false == objectIDs.contains(objectID)) {
                objectIDs.add(objectID);
                folderIDs.add(Integer.valueOf((int)randomDocument.getFolderId()));
            }
        }
        /*
         * execute delete request
         */
        infoMgr.deleteAction(objectIDs, folderIDs, infoMgr.getLastResponse().getTimestamp());
        long duration = infoMgr.getLastResponse().getRequestDuration();
        assertTrue("deletion took " + duration + "ms, which is too long", 10 * DELETED_ITEMS > duration); // allow 10ms per item
        /*
         * verify deletion
         */
        int[] columns = { CommonObject.OBJECT_ID };
        AllInfostoreRequest allRequest = new AllInfostoreRequest(testFolder.getObjectID(), columns, -1, null);
        AbstractColumnsResponse allResponse = getClient().execute(allRequest);
        assertEquals("Unexpected object count", TOTAL_ITEMS - DELETED_ITEMS, allResponse.getArray().length);
        for (Object[] object : allResponse) {
            Integer objectID = Integer.valueOf(object[0].toString());
            assertFalse("Object not deleted", objectIDs.contains(objectID));
        }
    }
}
