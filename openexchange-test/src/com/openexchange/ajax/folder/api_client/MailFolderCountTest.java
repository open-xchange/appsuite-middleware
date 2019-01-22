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

package com.openexchange.ajax.folder.api_client;

import java.io.File;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.junit.Assert;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderResponse;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.MailDestinationData;
import com.openexchange.testing.httpclient.models.MailImportResponse;
import com.openexchange.testing.httpclient.models.MailListElement;
import com.openexchange.testing.httpclient.models.MailUpdateBody;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.MailApi;

/**
 * {@link MailFolderCountTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class MailFolderCountTest extends AbstractConfigAwareAPIClientSession {

    private static final String FOLDER = "default0%2FINBOX";
    private MailApi api;
    private final Map<Integer, MailDestinationData> IMPORTED_EMAILS = new HashMap<>();
    private Long timestamp = 0l;
    private FoldersApi folderApi;
    private String folderId;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        String testMailDir = AJAXConfig.getProperty(AJAXConfig.Property.TEST_DIR);
        super.setUpConfiguration();

        // Setup client and import mails ------------------------
        api = new MailApi(getApiClient());
        folderApi = new FoldersApi(getApiClient());

        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setTitle(this.getClass().getSimpleName() + "_" + new UID().toString());
        folder.setModule("mail");
        body.setFolder(folder);
        FolderUpdateResponse createFolder = folderApi.createFolder(FOLDER, getApiClient().getSession(), body, "1", null, null);
        Assert.assertNull(createFolder.getError());
        Assert.assertNotNull(createFolder.getData());

        folderId = createFolder.getData();

        for (int x = 0; x < 2; x++) {
            File f = new File(testMailDir, "bug.eml");
            Assert.assertTrue(f.exists());
            MailImportResponse response = api.importMail(getApiClient().getSession(), folderId, f, null, true);
            List<MailDestinationData> data = checkResponse(response);
            // data size should always be 1
            Assert.assertEquals(1, data.size());
            IMPORTED_EMAILS.put(x, data.get(0));
            timestamp = response.getTimestamp();
        }

        // Mark first mail as unread
        MailUpdateBody mailUpdateBody = new MailUpdateBody();
        mailUpdateBody.setClearFlags(32);
        api.updateMail(getApiClient().getSession(), folderId, mailUpdateBody, IMPORTED_EMAILS.get(0).getId(), null);
        // Mark second mail as deleted and unread
        mailUpdateBody = new MailUpdateBody();
        mailUpdateBody.setSetFlags(2l);
        mailUpdateBody.setClearFlags(32);
        api.updateMail(getApiClient().getSession(), folderId, mailUpdateBody, IMPORTED_EMAILS.get(1).getId(), null);

    }

    @Override
    public void tearDown() throws Exception {
        try {
            List<MailListElement> body = new ArrayList<>();
            for (MailDestinationData dest : IMPORTED_EMAILS.values()) {
                MailListElement mailListElement = new MailListElement();
                mailListElement.setFolder(dest.getFolderId());
                mailListElement.setId(dest.getId());
                body.add(mailListElement);
            }
            api.deleteMails(getApiClient().getSession(), body, timestamp);
            folderApi.deleteFolders(getApiClient().getSession(), Collections.singletonList(folderId), "1", timestamp, null, true, false, false, null);
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testFolderCount() throws Exception {
        FolderResponse resp = folderApi.getFolder(getApiClient().getSession(), folderId, "1", null, null);
        FolderData folder = checkResponse(resp);
        Assert.assertEquals(2, folder.getUnread().intValue());

        CONFIG.put("com.openexchange.imap.ignoreDeleted", Boolean.TRUE.toString());
        super.setUpConfiguration();
        resp = folderApi.getFolder(getApiClient().getSession(), folderId, "1", null, null);
        folder = checkResponse(resp);
        Assert.assertEquals(1, folder.getUnread().intValue());
    }

    private FolderData checkResponse(FolderResponse resp) {
        Assert.assertNull(resp.getError());
        Assert.assertNotNull(resp.getData());
        return resp.getData();
    }

    private List<MailDestinationData> checkResponse(MailImportResponse response) {
        Assert.assertNull(response.getError());
        Assert.assertNotNull(response.getData());
        return response.getData();
    }

    // -------------------------   prepare config --------------------------------------

    private static final Map<String, String> CONFIG = new HashMap<>();

    static {
        CONFIG.put("com.openexchange.imap.ignoreDeleted", Boolean.FALSE.toString());
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        return CONFIG;
    }

    @Override
    protected String getScope() {
        return "user";
    }

    @Override
    protected String getReloadables() {
        return "IMAPPropertiesReloader";
    }

}
