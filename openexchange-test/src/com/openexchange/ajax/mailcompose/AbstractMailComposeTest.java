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

package com.openexchange.ajax.mailcompose;

import static com.openexchange.java.Autoboxing.I;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.java.Strings;
import com.openexchange.test.TestInit;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.ContactData;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.MailComposeGetResponse;
import com.openexchange.testing.httpclient.models.MailComposeMessageModel;
import com.openexchange.testing.httpclient.models.MailComposeResponse;
import com.openexchange.testing.httpclient.models.MailDestinationData;
import com.openexchange.testing.httpclient.models.MailImportResponse;
import com.openexchange.testing.httpclient.models.MailListElement;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.modules.ContactsApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.MailApi;
import com.openexchange.testing.httpclient.modules.MailComposeApi;

/**
 * {@link AbstractMailComposeTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.2
 */
public abstract class AbstractMailComposeTest extends AbstractAPIClientSession {

    protected MailComposeApi api;
    protected final String DEFAULT_COLUMNS = "from,sender,to,cc,bcc,subject";
    protected final String ALL_COLUMNS = "from,sender,to,cc,bcc,subject,content,contentType,attachments,sharedAttachmentsInfo,meta,requestReadReceipt,priority,security,contentEncrypted";
    protected final List<String> compositionSpaceIds = new ArrayList<>();

    protected MailApi mailApi;
    protected File attachment;
    protected File attachment2;
    protected String testMailDir;
    protected File mailWithAttachmentFile;
    protected final List<MailDestinationData> IMPORTED_EMAILS = new ArrayList<>();
    protected Long timestamp;
    protected FoldersApi foldersApi;
    protected String folderId;
    protected MailDestinationData mailWithAttachment;

    private static final String FOLDER = "default0%2FINBOX";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.api = new MailComposeApi(apiClient);

        mailApi = new MailApi(getApiClient());
        foldersApi = new FoldersApi(getApiClient());
        testMailDir = AJAXConfig.getProperty(AJAXConfig.Property.TEST_DIR);
        mailWithAttachmentFile = new File(testMailDir, "bug29865.eml");

        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setTitle(this.getClass().getSimpleName() + "_" + new UID().toString());
        folder.setModule("mail");
        body.setFolder(folder);
        FolderUpdateResponse createFolder = foldersApi.createFolder(FOLDER, getApiClient().getSession(), body, "1", null, null);
        folderId = createFolder.getData();

        MailImportResponse response = mailApi.importMail(getApiClient().getSession(), folderId, mailWithAttachmentFile, null, Boolean.TRUE);
        List<MailDestinationData> data = response.getData();
        mailWithAttachment = data.get(0);
        IMPORTED_EMAILS.add(mailWithAttachment);
        timestamp = response.getTimestamp();

        attachment = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        attachment2 = new File(TestInit.getTestProperty("provisioningFile"));

        MailComposeGetResponse allSpaces = api.getMailCompose(getSessionId(), null);
        for (MailComposeMessageModel model : allSpaces.getData()) {
            api.deleteMailComposeById(getSessionId(), model.getId());
        }
    }

    @Override
    public void tearDown() throws Exception {
        if (null != compositionSpaceIds && compositionSpaceIds.size() > 0) {
            for (String id : compositionSpaceIds) {
                try {
                    api.deleteMailComposeById(getSessionId(), id);
                } catch (ApiException e) {
                    // Space was already deleted, ignore...
                }
            }
        }

        List<MailListElement> body = new ArrayList<>();
        for (MailDestinationData dest : IMPORTED_EMAILS) {
            MailListElement mailListElement = new MailListElement();
            mailListElement.setFolder(dest.getFolderId());
            mailListElement.setId(dest.getId());
            body.add(mailListElement);
        }
        mailApi.deleteMails(getApiClient().getSession(), body, timestamp);
        foldersApi.deleteFolders(getApiClient().getSession(), Collections.singletonList(folderId), "1", timestamp, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null);

        super.tearDown();
    }

    protected MailComposeMessageModel createNewCompositionSpace() throws Exception {
        MailComposeResponse response = api.postMailCompose(getSessionId(), null, null, null);
        assertTrue(response.getErrorDesc(), Strings.isEmpty(response.getError()));
        MailComposeMessageModel data = response.getData();
        compositionSpaceIds.add(data.getId());
        return data;
    }

    protected String getMailAddress() throws Exception {
        ContactsApi contactsApi = new ContactsApi(apiClient);
        ContactData data = contactsApi.getContactByUser(getSessionId(), apiClient.getUserId()).getData();
        assertNotNull("No contact data for user.", data);
        String mailAddress = data.getEmail1();
        assertFalse("No mail address for user.", Strings.isEmpty(mailAddress));
        return mailAddress;
    }

    protected String getOtherMailAddress() throws Exception {
        ContactsApi contactsApi = new ContactsApi(apiClient);
        ContactData data = contactsApi.getContactByUser(getSessionId(), I(getClient2().getValues().getUserId())).getData();
        assertNotNull("No contact data for other user.", data);
        String mailAddress = data.getEmail1();
        assertFalse("No mail address for other user.", Strings.isEmpty(mailAddress));
        return mailAddress;
    }

    protected List<String> getSender() throws Exception {
        return Arrays.asList(new String[] { testUser.getUser(), getMailAddress() });
    }

    protected List<List<String>> getRecipient() throws Exception {
        return Collections.singletonList(Arrays.asList(new String[] { testUser2.getUser(), getOtherMailAddress() }));
    }

}
