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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.drive.test;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertNotNull;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.FolderTestManager;
import com.openexchange.testing.httpclient.models.DriveDownloadBody;
import com.openexchange.testing.httpclient.modules.DriveApi;

/**
 * {@link MWB1058Test}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v8.0.0
 */
public class MWB1058Test extends AbstractAPIClientSession {

    private final long FILE_SIZE = 1680771L;
    private final String FILE_NAME = "testData/oxlogo.png";
    private final String FILE_MD5 = "ab2c2da86c805c5be7165072626aff92";

    private DriveApi driveApi;
    private FolderTestManager folderTestManager;
    private InfostoreTestManager infostoreTestManager;
    private FolderObject testFolder;
    private File testFile;
    private int userId;
    private int driveRootFolderId;

    @Test
    public void testSyncFileWithDashes() throws Exception {
        DriveDownloadBody body = new DriveDownloadBody();
        java.io.File file = driveApi.downloadFile(getSessionId(), String.valueOf(driveRootFolderId), "/" + testFolder.getFolderName(), testFile.getFileName(), FILE_MD5, I(8), L(0), L(FILE_SIZE), Boolean.TRUE, body);
        assertNotNull(file);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        userId = getClient().getValues().getUserId();
        driveRootFolderId = getClient().getValues().getPrivateInfostoreFolder();
        this.driveApi = new DriveApi(getApiClient());
        folderTestManager = new FolderTestManager(getClient());
        infostoreTestManager = new InfostoreTestManager(getClient());
        testFolder = folderTestManager.generatePrivateFolder("MWB1058Test_" + UUID.randomUUID().toString(), FolderObject.INFOSTORE, driveRootFolderId, userId);
        testFolder = folderTestManager.insertFolderOnServer(testFolder);
        testFile = InfostoreTestManager.createFile(testFolder.getObjectID(), "MWB-1058-Test-File" + UUID.randomUUID().toString(), "text/plain");
        infostoreTestManager.newAction(testFile, new java.io.File(FILE_NAME));
    }

    @Override
    public void tearDown() throws Exception {
        if (null != folderTestManager && null != testFolder) {
            folderTestManager.deleteFolderOnServer(testFolder);
        }
        super.tearDown();
    }

}
