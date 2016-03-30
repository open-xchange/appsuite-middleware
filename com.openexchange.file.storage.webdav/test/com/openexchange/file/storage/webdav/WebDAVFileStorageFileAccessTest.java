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

package com.openexchange.file.storage.webdav;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link WebDAVFileStorageFileAccessTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class WebDAVFileStorageFileAccessTest extends AbstractWebDAVFileStorageTest {

    /**
     * Initializes a new {@link WebDAVFileStorageFileAccessTest}.
     */
    public WebDAVFileStorageFileAccessTest() {
        super();
    }

    public void testCreateFile() throws Exception {
        final WebDAVFileStorageAccountAccess accountAccess = getAccountAccess();
        accountAccess.connect();
        try {
            FileStorageFileAccess fileAccess = accountAccess.getFileAccess();

            final DefaultFile file = new DefaultFile();
            file.setFileName("test.txt");
            file.setFileMIMEType("text/plain");
            final String folderId = accountAccess.getRootFolder().getId();
            file.setFolderId(folderId);
            file.setTitle("test.txt");

            fileAccess.saveDocument(file, new ByteArrayInputStream("Some text...".getBytes(com.openexchange.java.Charsets.UTF_8)), FileStorageFileAccess.DISTANT_FUTURE);

            final List<IDTuple> ids = new ArrayList<FileStorageFileAccess.IDTuple>(1);
            ids.add(new IDTuple(folderId, file.getId()));
            fileAccess.removeDocument(ids, FileStorageFileAccess.DISTANT_FUTURE);
        } finally {
            accountAccess.close();
        }
    }

    public void testTouchFile() throws Exception {
        final WebDAVFileStorageAccountAccess accountAccess = getAccountAccess();
        accountAccess.connect();
        try {
            FileStorageFileAccess fileAccess = accountAccess.getFileAccess();

            final DefaultFile file = new DefaultFile();
            file.setFileName("test.txt");
            file.setFileMIMEType("text/plain");
            final String folderId = accountAccess.getRootFolder().getId();
            file.setFolderId(folderId);
            file.setTitle("test.txt");

            fileAccess.saveDocument(file, new ByteArrayInputStream("Some text...".getBytes(com.openexchange.java.Charsets.UTF_8)), FileStorageFileAccess.DISTANT_FUTURE);
            try {
                File storageFile1 = fileAccess.getFileMetadata(folderId, file.getId(), FileStorageFileAccess.CURRENT_VERSION);
                Date lastModified1 = storageFile1.getLastModified();
                Thread.sleep(2000);
                fileAccess.touch(folderId, file.getId());
                File storageFile2 = fileAccess.getFileMetadata(folderId, file.getId(), FileStorageFileAccess.CURRENT_VERSION);
                Date lastModified2 = storageFile2.getLastModified();
                assertTrue("Last-modified time stamp was not updated on WebDAV server.", lastModified2.after(lastModified1));
            } finally {
                final List<IDTuple> ids = new ArrayList<FileStorageFileAccess.IDTuple>(1);
                ids.add(new IDTuple(folderId, file.getId()));
                fileAccess.removeDocument(ids, FileStorageFileAccess.DISTANT_FUTURE);
            }
        } finally {
            accountAccess.close();
        }
    }

    public void testCopyFile() throws Exception {
        final WebDAVFileStorageAccountAccess accountAccess = getAccountAccess();
        accountAccess.connect();
        try {
            FileStorageFileAccess fileAccess = accountAccess.getFileAccess();

            final DefaultFile file = new DefaultFile();
            file.setFileName("test.txt");
            file.setFileMIMEType("text/plain");
            final String folderId = accountAccess.getRootFolder().getId();
            file.setFolderId(folderId);
            file.setTitle("test.txt");

            fileAccess.saveDocument(file, new ByteArrayInputStream("Some text...".getBytes(com.openexchange.java.Charsets.UTF_8)), FileStorageFileAccess.DISTANT_FUTURE);
            try {
                /*
                 * Create a new folder
                 */
                final FileStorageFolderAccess folderAccess = accountAccess.getFolderAccess();

                final DefaultFileStorageFolder folder = new DefaultFileStorageFolder();
                final String name = "TestFolder" + System.currentTimeMillis();
                folder.setName(name);
                final String parentId = folderAccess.getRootFolder().getId();
                folder.setParentId(parentId);
                final DefaultFileStoragePermission p = DefaultFileStoragePermission.newInstance();
                p.setMaxPermissions();
                p.setEntity(accountAccess.getSession().getUserId());
                p.setGroup(false);
                folder.setPermissions(Collections.<FileStoragePermission> singletonList(p));
                folder.setSubscribed(true);
                final String newId = folderAccess.createFolder(folder);
                try {
                    /*
                     * Check its presence
                     */
                    FileStorageFolder[] subfolders = folderAccess.getSubfolders(parentId, true);
                    boolean found = false;
                    for (FileStorageFolder fileStorageFolder : subfolders) {
                        if (newId.equals(fileStorageFolder.getId())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        fail("Newly created folder \"" + newId + "\" not found");
                    }
                    /*
                     * Copy file
                     */
                    final IDTuple tuple = fileAccess.copy(
                        new IDTuple(folderId, file.getId()), FileStorageFileAccess.CURRENT_VERSION, newId, null, null, null);
                    try {
                        /*
                         * Check
                         */
                        found = false;
                        final TimedResult<File> result = fileAccess.getDocuments(newId, null);
                        final String id = tuple.getId();
                        for (final SearchIterator<File> iter = result.results(); iter.hasNext();) {
                            final File next = iter.next();
                            if (next.getId().equals(id)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            fail("Copied file \"" + file.getFileName() + "\" not found in folder \"" + newId + "\"");
                        }
                    } finally {
                        final List<IDTuple> ids = new ArrayList<FileStorageFileAccess.IDTuple>(1);
                        ids.add(tuple);
                        fileAccess.removeDocument(ids, FileStorageFileAccess.DISTANT_FUTURE);
                    }
                } finally {
                    folderAccess.deleteFolder(newId, true);
                }
            } finally {
                final List<IDTuple> ids = new ArrayList<FileStorageFileAccess.IDTuple>(1);
                ids.add(new IDTuple(folderId, file.getId()));
                fileAccess.removeDocument(ids, FileStorageFileAccess.DISTANT_FUTURE);
            }
        } finally {
            accountAccess.close();
        }
    }

}
