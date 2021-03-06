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

package com.openexchange.ajax.folder.api_client;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.io.File;
import java.rmi.server.UID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.junit.Assert;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderResponse;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.MailDestinationData;
import com.openexchange.testing.httpclient.models.MailImportResponse;
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
    private FoldersApi folderApi;
    private String folderId;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        String testMailDir = AJAXConfig.getProperty(AJAXConfig.Property.TEST_MAIL_DIR);
        super.setUpConfiguration();

        // Setup client and import mails ------------------------
        api = new MailApi(getApiClient());
        folderApi = new FoldersApi(getApiClient());

        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setTitle(this.getClass().getSimpleName() + "_" + new UID().toString());
        folder.setModule("mail");
        body.setFolder(folder);
        FolderUpdateResponse createFolder = folderApi.createFolder(FOLDER, body, "1", null, null, null);
        Assert.assertNull(createFolder.getError());
        Assert.assertNotNull(createFolder.getData());

        folderId = createFolder.getData();

        for (int x = 0; x < 2; x++) {
            File f = new File(testMailDir, "bug.eml");
            Assert.assertTrue(f.exists());
            MailImportResponse response = api.importMail(folderId, f, null, Boolean.TRUE);
            List<MailDestinationData> data = checkResponse(response);
            // data size should always be 1
            Assert.assertEquals(1, data.size());
            IMPORTED_EMAILS.put(I(x), data.get(0));
        }

        // Mark first mail as unread
        MailUpdateBody mailUpdateBody = new MailUpdateBody();
        mailUpdateBody.setClearFlags(I(32));
        api.updateMail(folderId, mailUpdateBody, IMPORTED_EMAILS.get(I(0)).getId(), null);
        // Mark second mail as deleted and unread
        mailUpdateBody = new MailUpdateBody();
        mailUpdateBody.setSetFlags(L(2));
        mailUpdateBody.setClearFlags(I(32));
        api.updateMail(folderId, mailUpdateBody, IMPORTED_EMAILS.get(I(1)).getId(), null);

    }

    @Test
    public void testFolderCount() throws Exception {
        FolderResponse resp = folderApi.getFolder(folderId, "1", null, null);
        FolderData folder = checkResponse(resp);
        Assert.assertEquals(2, folder.getUnread().intValue());

        CONFIG.put("com.openexchange.imap.ignoreDeleted", Boolean.TRUE.toString());
        super.setUpConfiguration();
        resp = folderApi.getFolder(folderId, "1", null, null);
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
