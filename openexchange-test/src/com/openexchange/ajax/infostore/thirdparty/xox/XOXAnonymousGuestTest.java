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

package com.openexchange.ajax.infostore.thirdparty.xox;

import org.junit.After;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.openexchange.groupware.modules.Module;
import com.openexchange.testing.httpclient.models.FileAccountData;
import com.openexchange.testing.httpclient.models.ShareLinkData;
import com.openexchange.testing.httpclient.models.ShareLinkResponse;
import com.openexchange.testing.httpclient.models.ShareTargetData;
import static org.hamcrest.Matchers.*;
import static com.openexchange.java.Autoboxing.*;

/**
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class XOXAnonymousGuestTest extends AbstractXOXTest {

    private String sharedFolderId;
    private ShareLinkData shareLinkData;
    private ShareTargetData shareTargetData;
    private FileAccountData account;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Override
    public void setUp() throws Exception {
        super.setUp();

        //Create a folder which is shared to a guest (anonymous link)
        sharedFolderId = createFolder(getPrivateInfostoreFolderID(getApiClient()), getRandomFolderName()).getId();

        //Share the folder
        shareTargetData = new ShareTargetData();
        shareTargetData.setFolder(sharedFolderId);
        shareTargetData.setModule(Module.INFOSTORE.getName());
        ShareLinkResponse shareLinkResponse = shareApi.getShareLink(getSessionId(), shareTargetData);
        Assert.assertNull(shareLinkResponse.getErrorDesc(), shareLinkResponse.getError());
        Assert.assertNotNull(shareLinkResponse.getData());
        shareLinkData = shareLinkResponse.getData();
        Assert.assertThat(shareLinkData.getIsNew(), is(Boolean.TRUE));

        //Register an XOX account
        XOXFileAccountConfiguration configuration = new XOXFileAccountConfiguration(shareLinkData.getUrl(), shareLinkData.getPassword());
        account = createAccount(XOX_FILE_STORAGE_SERVICE, XOX_FILE_STORAGE_SERVICE_DISPLAY_NAME, configuration);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        shareApi.deleteShareLink(getSessionId(), L(Long.MAX_VALUE), shareTargetData);
        deleteFolder(sharedFolderId, true);
        super.tearDown();
    }

    //private String createStharedTestFolderNamedGuest() {

    //    FolderPermission permission = new FolderPermission();
    //    permission.setType("guest");
    //    permission.setBits(I(4227332));
    //    permission....

    //    sharedFolder = createFolder(getPrivateInfostoreFolderID(), getRandomFolderName());
    //    String fileName = getRandomFileName();
    //    String newFileId = uploadInfoItem(sharedFolder, fileName, testContent, "application/text");

    //    return
    //}

    @Override
    protected String getRootFolderId() throws Exception {
        return toXOXId(account, "10" /* root folder */);
    }

    @Override
    protected FileAccountData getAccountData() throws Exception {
        return account;
    }

    @Override
    protected Object getWrongFileStorageConfiguration() {
        return null;
    }

    @Override
    protected TestFile createTestFile() throws Exception {
        //The user=guest does not have write permissions:
        //So: create a new file as the user who shares the files; not the guest
        //The new file is created in the shared folder; so the guest can access it
        String fileName = getRandomFileName();
        String newFileId = uploadInfoItem(sharedFolderId, fileName, testContent, "application/text");
        return new TestFile(toXOXId(account, sharedFolderId), toXOXId(account, sharedFolderId, newFileId), fileName);
    }

    @Override
    @Test
    public void testCopyFile() throws Exception {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("You do not have sufficient permissions");
        super.testCopyFile();
    }

    @Override
    @Test
    public void testMoveFile() throws Exception {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("You do not have sufficient permissions");
        super.testMoveFile();
    }

    @Override
    public void testCreateDeleteFile() throws Exception {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("You do not have sufficient permissions");
        super.testCreateDeleteFile();
    }

    @Override
    @Test
    public void testCreateDeleteFolder() throws Exception {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("no create-subfolder permission");
        super.testCreateDeleteFolder();
    }

    @Override
    @Test
    public void testMoveFolder() throws Exception {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("no create-subfolder permission");
        super.testMoveFolder();
    }
}
