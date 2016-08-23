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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Collections;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.file.storage.FileStorageEventHelper;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.InMemoryFileStorageFileAccess;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.session.SimSession;


/**
 * {@link FileEventTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class FileEventTest {

    private static final String SERVICE = "http://inmemoryfilestorage.ox";

    private static final String ACCOUNT = "5435656";

    private InMemoryAccess fileAccess;


    @Before
    public void setUp() throws Exception {
        fileAccess = new InMemoryAccess();
    }

    @Test
    public void testSave() throws Exception {
        final File file = new DefaultFile();
        file.setTitle("Title...");
        final FolderID folder = new FolderID(SERVICE, ACCOUNT, "dasdb3424");
        file.setFolderId(folder.toUniqueID());
        fileAccess.setEventVerifier(new EventVerifier() {
            @Override
            public void verifyEvent(Event event) throws Exception {
                assertTrue("Wrong topic.", event.getTopic().equals(FileStorageEventConstants.CREATE_TOPIC));
                String folderId = FileStorageEventHelper.extractFolderId(event);
                String objectId = FileStorageEventHelper.extractObjectId(event);
                assertEquals("Wrong folder.", file.getFolderId(), folderId);
                assertEquals("Wrong id.", file.getId(), objectId);
            }
        });
        fileAccess.saveFileMetadata(file, 0);
    }

    @Test
    public void testRemove() throws Exception {
        final File file = new DefaultFile();
        file.setTitle("Title...");
        final FolderID srcfolder = new FolderID(SERVICE, ACCOUNT, "dasdb3424");
        file.setFolderId(srcfolder.toUniqueID());
        fileAccess.saveFileMetadata(file, 0);
        final File updated = new DefaultFile(file);
        final String fullID = fileAccess.saveFileMetadata(updated, 0);
        final String folderID = fullID.substring(0, fullID.lastIndexOf("/"));

        fileAccess.setEventVerifier(new EventVerifier() {
            @Override
            public void verifyEvent(Event event) throws Exception {
                assertTrue("Wrong topic.", event.getTopic().equals(FileStorageEventConstants.DELETE_TOPIC));
                String folderId = FileStorageEventHelper.extractFolderId(event);
                String objectId = FileStorageEventHelper.extractObjectId(event);
                Set<String> versions = FileStorageEventHelper.extractVersions(event);
                assertEquals("Wrong folder.", folderID, folderId);
                assertEquals("Wrong id.", fullID, objectId);
                assertEquals("Too much versions.", 1, versions.size());
                String next = versions.iterator().next();
                assertTrue("Wrong version.", next == updated.getVersion());
            }
        });
        String[] notRemoved = fileAccess.removeVersion(fullID, new String[] { updated.getVersion() });
        assertTrue("Version not removed.", notRemoved.length == 0);

        fileAccess.setEventVerifier(new EventVerifier() {
            @Override
            public void verifyEvent(Event event) throws Exception {
                assertTrue("Wrong topic.", event.getTopic().equals(FileStorageEventConstants.DELETE_TOPIC));
                String folderId = FileStorageEventHelper.extractFolderId(event);
                String objectId = FileStorageEventHelper.extractObjectId(event);
                assertEquals("Wrong folder.", folderID, folderId);
                assertEquals("Wrong id.", fullID, objectId);
            }
        });
        assertTrue("Deletion failed.", fileAccess.removeDocument(Collections.singletonList(fullID), 0).size() == 0);
    }

    @Test
    public void testMove() throws Exception {
        final File file = new DefaultFile();
        file.setTitle("Title...");
        final FolderID srcfolder = new FolderID(SERVICE, ACCOUNT, "dasdb3424");
        file.setFolderId(srcfolder.toUniqueID());
        fileAccess.saveFileMetadata(file, 0);

        final FolderID dstFolder = new FolderID(SERVICE, ACCOUNT, "xsdgd7234");
        final File moved = new DefaultFile(file);
        moved.setFolderId(dstFolder.toUniqueID());

        fileAccess.setEventVerifier(new EventVerifier() {
            private int executionCount = 0;

            private boolean deleted = false;

            private boolean created = false;

            @Override
            public void verifyEvent(Event event) throws Exception {
                if (event.getTopic().equals(FileStorageEventConstants.DELETE_TOPIC)) {
                    String folderId = FileStorageEventHelper.extractFolderId(event);
                    String objectId = FileStorageEventHelper.extractObjectId(event);
                    assertEquals("Wrong folder.", file.getFolderId(), folderId);
                    assertEquals("Wrong id.", file.getId(), objectId);

                    executionCount++;
                    deleted = true;
                } else if (event.getTopic().equals(FileStorageEventConstants.CREATE_TOPIC)) {
                    String folderId = FileStorageEventHelper.extractFolderId(event);
                    String objectId = FileStorageEventHelper.extractObjectId(event);
                    assertEquals("Wrong folder.", moved.getFolderId(), folderId);
                    assertEquals("Wrong id.", moved.getId(), objectId);

                    executionCount++;
                    created = true;
                }

                if (executionCount == 2) {
                    assertTrue("No delete event.", deleted);
                    assertTrue("No create event.", created);
                }
            }
        });
        fileAccess.move(moved, null, 0, null, true);
    }

    @Test
    public void testCopy() throws Exception {
        final File file = new DefaultFile();
        file.setTitle("Title...");
        final FolderID srcfolder = new FolderID(SERVICE, ACCOUNT, "dasdb3424");
        file.setFolderId(srcfolder.toUniqueID());
        fileAccess.saveFileMetadata(file, 0);

        final FolderID dstFolder = new FolderID(SERVICE, ACCOUNT, "xsdgd7234");
        fileAccess.setEventVerifier(new EventVerifier() {
            @Override
            public void verifyEvent(Event event) throws Exception {
                assertTrue("Wrong topic.", event.getTopic().equals(FileStorageEventConstants.CREATE_TOPIC));
                String folderId = FileStorageEventHelper.extractFolderId(event);
                String objectId = FileStorageEventHelper.extractObjectId(event);
                assertEquals("Wrong folder.", dstFolder.toUniqueID(), folderId);
                assertFalse("Wrong id.", file.getId().equals(objectId));
            }
        });
        String copyId = fileAccess.copy(file.getId(), FileStorageFileAccess.CURRENT_VERSION, dstFolder.toUniqueID(), null, null, null);
        File copy = fileAccess.getFileMetadata(copyId, FileStorageFileAccess.CURRENT_VERSION);
        assertNotNull("Copy was null.", copy);
    }

    @Test
    public void testUpdate() throws Exception {
        final File file = new DefaultFile();
        file.setTitle("Title...");
        final FolderID srcfolder = new FolderID(SERVICE, ACCOUNT, "dasdb3424");
        file.setFolderId(srcfolder.toUniqueID());
        String fullID = fileAccess.saveFileMetadata(file, 0);
        final String folderID = fullID.substring(0, fullID.lastIndexOf("/"));

        file.setTitle("Another title...");
        fileAccess.setEventVerifier(new EventVerifier() {
            @Override
            public void verifyEvent(Event event) throws Exception {
                assertTrue("Wrong topic.", event.getTopic().equals(FileStorageEventConstants.UPDATE_TOPIC));
                String folderId = FileStorageEventHelper.extractFolderId(event);
                String objectId = FileStorageEventHelper.extractObjectId(event);
                assertEquals("Wrong folder.", folderID, folderId);
                assertEquals("Wrong id.", folderID + "/" + file.getId(), objectId);
            }
        });
        fileAccess.saveFileMetadata(file, 0);
    }

    @After
    public void tearDown() throws Exception {
        fileAccess = null;
    }

    private static final class InMemoryAccess extends AbstractCompositingIDBasedFileAccess {

        private final FileStorageFileAccess access = new InMemoryFileStorageFileAccess(SERVICE, ACCOUNT);

        private EventVerifier verifier;


        /**
         * Initializes a new {@link InMemoryAccess}.
         * @param session
         */
        public InMemoryAccess() {
            super(new SimSession());
        }

        @Override
        protected EventAdmin getEventAdmin() {
            return null;
        }

        @Override
        protected FileStorageFileAccess getFileAccess(String serviceId, String accountId) throws OXException {
            return access;
        }

        @Override
        protected void postEvent(Event event) {
            try {
                String serviceId = FileStorageEventHelper.extractService(event);
                String accountId = FileStorageEventHelper.extractAccountId(event);
                Session session = FileStorageEventHelper.extractSession(event);
                assertEquals("Wrong service.", SERVICE, serviceId);
                assertEquals("Wrong account.", ACCOUNT, accountId);
                assertEquals("Wrong session.", this.session, session);
            } catch (OXException e) {
                fail(e.getMessage());
            }

            if (verifier != null) {
                try {
                    verifier.verifyEvent(event);
                } catch (Exception e) {
                    fail(e.getMessage());
                }
            }
        }

        public void setEventVerifier(EventVerifier verifier) {
            this.verifier = verifier;
        }

        /* (non-Javadoc)
         * @see com.openexchange.file.storage.composition.IDBasedFileAccess#getDocumentAndMetadata(java.lang.String, java.lang.String)
         */
        @Override
        public Document getDocumentAndMetadata(String id, String version) throws OXException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected FileStorageServiceRegistry getFileStorageServiceRegistry() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private static interface EventVerifier {
        void verifyEvent(Event event) throws Exception;
    }

}
