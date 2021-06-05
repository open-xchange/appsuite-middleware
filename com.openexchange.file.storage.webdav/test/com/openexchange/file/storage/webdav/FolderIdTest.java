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

package com.openexchange.file.storage.webdav;

import static com.openexchange.tools.session.ServerSessionAdapter.valueOf;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.SimFileStorageAccount;
import com.openexchange.webdav.client.WebDAVClient;

/**
 * {@link FolderIdTest}
 *
 * Tests encoding/decoding of folder identifiers to/from WebDAV paths
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.4
 */
public class FolderIdTest {

    private AbstractWebDAVAccess access;

    @Before
    public void setUp() throws Exception {
        AbstractWebDAVAccountAccess accountAccess = new AbstractWebDAVAccountAccess(null, new SimFileStorageAccount(), valueOf(-1, -1)) {

            @Override
            protected AbstractWebDAVFolderAccess initWebDAVFolderAccess(WebDAVClient webdavClient) throws OXException {
                return null;
            }

            @Override
            protected AbstractWebDAVFileAccess initWebDAVFileAccess(WebDAVClient webdavClient) throws OXException {
                return null;
            }
        };
        access = new AbstractWebDAVAccess(null, accountAccess) {

            @Override
            protected @NonNull WebDAVPath getRootPath(FileStorageAccount account) throws OXException {
                return new WebDAVPath("/users/888/files/");
            }
        };
    }

    @Test
    public void testRootEncoding() {
        assertEquals(FileStorageFolder.ROOT_FULLNAME, access.getFolderId(new WebDAVPath("https://dav.example.com/users/888/files/")));
        assertEquals(FileStorageFolder.ROOT_FULLNAME, access.getFolderId(new WebDAVPath("/users/888/files/")));
    }

    @Test
    public void testRootDecoding() throws Exception {
        assertEquals(new WebDAVPath("/users/888/files/"), access.getWebDAVPath(FileStorageFolder.ROOT_FULLNAME));
    }

    @Test
    public void testFolderEncodingDecoding() throws Exception {
        WebDAVPath path = new WebDAVPath("/users/888/files/");
        assertEquals(path, access.getWebDAVPath(access.getFolderId(path)));
        path = new WebDAVPath("/users/888/files/New%20Folder/New%20Folder/");
        assertEquals(path, access.getWebDAVPath(access.getFolderId(path)));
    }

    @Test
    public void testFileEncodingDecoding() throws Exception {
        WebDAVPath path = new WebDAVPath("/users/888/files/test.txt");
        assertEquals(path, access.getWebDAVPath(access.getFileId(path)));
        path = new WebDAVPath("/users/888/files/mütze.txt");
        assertEquals(path, access.getWebDAVPath(access.getFileId(path)));
        path = new WebDAVPath("/users/888/files/kleine%20wurst.txt");
        assertEquals(path, access.getWebDAVPath(access.getFileId(path)));
        path = new WebDAVPath("/users/888/files/Große%20Wurst.txt");
        assertEquals(path, access.getWebDAVPath(access.getFileId(path)));
    }

}
