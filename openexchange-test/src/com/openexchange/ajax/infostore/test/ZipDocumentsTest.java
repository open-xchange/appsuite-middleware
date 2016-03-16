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
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.folderstorage.Folder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.TestInit;

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
    public ZipDocumentsTest(String name) {
        super(name);
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
                java.io.File file = new java.io.File(TestInit.getTestProperty("webdavPropertiesFile"));

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
