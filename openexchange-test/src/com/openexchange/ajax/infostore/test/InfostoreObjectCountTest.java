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

import java.util.Date;
import org.junit.Test;
import com.openexchange.ajax.folder.AbstractObjectCountTest;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.folderstorage.Folder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
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
    public InfostoreObjectCountTest(String name) {
        super(name);
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

            DocumentMetadata expected = createDocumentMetadata(folder);
            infostoreTestManager.newAction(expected);

            Folder reloaded = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            objectsInFolder = reloaded.getTotal();
            assertEquals("Wrong object count", 1, objectsInFolder);
        } finally {
            infostoreTestManager.cleanUp();
            folderTestManager.cleanUp();
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

            DocumentMetadata expected = createDocumentMetadata(folder);

            infostoreTestManager.newAction(expected);
            infostoreTestManager.newAction(expected);
            infostoreTestManager.newAction(expected);
            infostoreTestManager.newAction(expected);
            infostoreTestManager.newAction(expected);

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

        try {
            FolderObject created = createSharedFolder(client1, FolderObject.INFOSTORE, client2.getValues().getUserId());
            Folder folder = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 0, folder.getTotal());

            DocumentMetadata expected = createDocumentMetadata(folder);
            infostoreTestManager.newAction(expected);
            infostoreTestManager.newAction(expected);
            infostoreTestManager.newAction(expected);
            infostoreTestManager.newAction(expected);
            infostoreTestManager.newAction(expected);

            Folder reloaded = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 5, reloaded.getTotal());

            Folder reloaded2 = getFolder(client2, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Other client is able to see objects", 0, reloaded2.getTotal());
        } finally {
            infostoreTestManager.cleanUp();
        }
    }

    @Test
    public void testCountInSharedInfostoreFolder_AddFiveFromUserWithPermission_CountReturnsFiveToUserWithPermission() throws Exception {
        InfostoreTestManager infostoreTestManager = new InfostoreTestManager(client2);

        try {
            FolderObject created = createSharedFolder(client1, FolderObject.INFOSTORE, client2.getValues().getUserId());
            Folder folder = getFolder(client2, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 0, folder.getTotal());

            DocumentMetadata expected = createDocumentMetadata(folder);
            infostoreTestManager.newAction(expected);
            infostoreTestManager.newAction(expected);
            infostoreTestManager.newAction(expected);
            infostoreTestManager.newAction(expected);
            infostoreTestManager.newAction(expected);

            Folder reloaded = getFolder(client2, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 5, reloaded.getTotal());

            Folder reloaded2 = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 0, reloaded2.getTotal());
        } finally {
            infostoreTestManager.cleanUp();
        }
    }

    /**
     * Creates a {@link DocumentMetadata} for further processing
     * 
     * @param folder - the folder to create the {@link DocumentMetadata} for
     * @return {@link DocumentMetadata} - created object
     */
    private DocumentMetadata createDocumentMetadata(Folder folder) {
        DocumentMetadata expected = new DocumentMetadataImpl();
        expected.setCreationDate(new Date());
        expected.setFolderId(Long.parseLong(folder.getID()));
        expected.setTitle("InfostoreCountTest Item");
        expected.setLastModified(new Date());
        return expected;
    }
}
