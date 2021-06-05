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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.folder.AbstractObjectCountTest;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.infostore.actions.ZipDocumentsRequest;
import com.openexchange.ajax.infostore.actions.ZipDocumentsRequest.IdVersionPair;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.folderstorage.Folder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.test.TestInit;

/**
 * {@link ZipDocumentsTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.2.2
 */
public final class ZipDocumentsTest extends AbstractObjectCountTest {

    /**
     * Initializes a new {@link ZipDocumentsTest}.
     *
     * @param name
     */
    public ZipDocumentsTest() {
        super();
    }

    @Test
    public void testZipDocumentsInInfostoreFolder() throws Exception {
        FolderTestManager folderTestManager = new FolderTestManager(client1);
        InfostoreTestManager infostoreTestManager = new InfostoreTestManager(client1);

        try {
            FolderObject created = createPrivateFolder(client1, folderTestManager, FolderObject.INFOSTORE);
            Folder folder = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            int objectsInFolder = folder.getTotal();
            assertEquals("Wrong object count", 0, objectsInFolder);

            final String id1;
            {
                File expected = new DefaultFile();
                expected.setCreated(new Date());
                expected.setFolderId(folder.getID());
                expected.setTitle("InfostoreCreateDeleteTest File1");
                expected.setLastModified(new Date());
                java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));

                infostoreTestManager.newAction(expected, file);
                assertFalse("Creating an entry should work", infostoreTestManager.getLastResponse().hasError());
                id1 = expected.getId();

                File actual = infostoreTestManager.getAction(expected.getId());
                assertEquals("Name should be the same", expected.getTitle(), actual.getTitle());
            }

            final String id2;
            {
                File expected = new DefaultFile();
                expected.setCreated(new Date());
                expected.setFolderId(folder.getID());
                expected.setTitle("InfostoreCreateDeleteTest File2");
                expected.setLastModified(new Date());
                java.io.File file = new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile"));

                infostoreTestManager.newAction(expected, file);
                assertFalse("Creating an entry should work", infostoreTestManager.getLastResponse().hasError());
                id2 = expected.getId();

                File actual = infostoreTestManager.getAction(expected.getId());
                assertEquals("Name should be the same", expected.getTitle(), actual.getTitle());
            }

            final List<IdVersionPair> pairs = new LinkedList<IdVersionPair>();
            pairs.add(new IdVersionPair(id1, null));
            pairs.add(new IdVersionPair(id2, null));
            ZipDocumentsRequest request = new ZipDocumentsRequest(pairs, folder.getID());
            final WebResponse webResponse = Executor.execute4Download(getSession(), request, AJAXConfig.getProperty(AJAXConfig.Property.PROTOCOL), AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME));
            /*
             * Some assertions
             */
            assertEquals("Unexpected Content-Type.", "application/zip", webResponse.getContentType());

        } finally {
            infostoreTestManager.cleanUp();
            folderTestManager.cleanUp();
        }
    }

}
