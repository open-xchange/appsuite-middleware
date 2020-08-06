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
