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

package com.openexchange.admin.user.copy.rmi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.admin.rmi.AbstractRMITest;
import com.openexchange.admin.rmi.AbstractTest;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.ListRequest;
import com.openexchange.ajax.folder.actions.ListResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.infostore.actions.AllInfostoreRequest;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.session.LoginTools;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.ajax.session.actions.LoginResponse;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.search.Order;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.TestInit;

/**
 * A test for https://bugs.open-xchange.com/show_bug.cgi?id=30245.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MailAttachmentFolderTest extends AbstractRMITest {

    private OXContextInterface ci;

    private OXUserInterface ui;

    private Context srcCtx;

    private Context dstCtx;

    private User srcUser;

    private User admin;

    private DocumentMetadata expectedDocument;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        AJAXConfig.init();
        ci = getContextInterface();
        ui = getUserInterface();

        superAdminCredentials = AbstractTest.DummyMasterCredentials();
        Context[] contexts = ci.list("UserMove*", superAdminCredentials);
        for (Context ctx : contexts) {
            System.out.println("Deleting context " + ctx.getName() + " in schema " + ctx.getReadDatabase().getScheme());
            try {
                ci.delete(ctx, superAdminCredentials);
            } catch (Exception e) {
                System.out.println("Error during context deletion.");
            }
        }

        admin = newUser("oxadmin", "secret", "Admin User", "Admin", "User", "oxadmin@example.com");
        srcCtx = TestTool.createContext(ci, "UserMoveSourceCtx_", admin, "all", superAdminCredentials);
        dstCtx = TestTool.createContext(ci, "UserMoveDestinationCtx_", admin, "all", superAdminCredentials);

        srcUser = newUser("user", "secret", "Test User", "Test", "User", "oxuser@example.com");
        srcUser.setImapServer("example.com");
        srcUser.setImapLogin("oxuser");
        srcUser.setSmtpServer("example.com");
        srcUser = ui.create(srcCtx, srcUser, getCredentials());

        User dummy = newUser("dummy", "secret", "Dummy User", "Dummy", "User", "oxuser2@example.com");
        dummy.setImapServer("example.com");
        dummy.setImapLogin("oxuser");
        dummy.setSmtpServer("example.com");
        dummy = ui.create(dstCtx, dummy, getCredentials());

        AJAXSession dummySession = performLogin(dummy.getName() + '@' + dstCtx.getName(), "secret");
        AJAXClient dummyClient = new AJAXClient(dummySession, false);
        FolderTestManager ftm = new FolderTestManager(dummyClient);
        FolderObject dummyFolders[] = new FolderObject[7];

        // some debug logging for jenkins

        System.out.println("Dummy ContextID: "+ dummyClient.getValues().getContextId());
        System.out.println("Dummy DefaultAddress: "+ dummyClient.getValues().getDefaultAddress());
        System.out.println("Dummy DraftsFolder: "+ dummyClient.getValues().getDraftsFolder());
        System.out.println("Dummy InboxFolder: "+ dummyClient.getValues().getInboxFolder());
        System.out.println("Dummy PrivateAppointmentFolder: "+ dummyClient.getValues().getPrivateAppointmentFolder());
        System.out.println("Dummy PrivateInfoStoreFolder: "+ dummyClient.getValues().getPrivateInfostoreFolder());
        System.out.println("Dummy PrivateContactFolder: "+ dummyClient.getValues().getPrivateContactFolder());
        System.out.println("Dummy PrivateTaskFolder: "+ dummyClient.getValues().getPrivateTaskFolder());
        System.out.println("Dummy SendAdress: "+ dummyClient.getValues().getSendAddress());
        System.out.println("Dummy TrashFolder: "+ dummyClient.getValues().getTrashFolder());
        System.out.println("Dummy UserId: "+ dummyClient.getValues().getUserId());


        for (int i = 0; i < 7; i++) {
            FolderObject pf = ftm.generatePrivateFolder(
                "dummy_folder_" + i,
                FolderObject.INFOSTORE,
                dummyClient.getValues().getPrivateInfostoreFolder(),
                dummyClient.getValues().getUserId());
            dummyFolders[i] = pf;
        }
        ftm.insertFoldersOnServer(dummyFolders);

        AJAXSession session = performLogin(srcUser.getName() + '@' + srcCtx.getName(), "secret");
        AJAXClient client = new AJAXClient(session, false);
        ListRequest request = new ListRequest(EnumAPI.OX_NEW, String.valueOf(client.getValues().getPrivateInfostoreFolder()));

        // Folder is created asynchronously during login...
        int triesLeft = 10;
        FolderObject srcMailAttachmentFolder = null;
        while (srcMailAttachmentFolder == null) {
            if (triesLeft == 0) {
                fail("Mail attachment folder not found!");
            }

            ListResponse response = client.execute(request);
            while (response.getFolder().hasNext()) {
                FolderObject tmp = response.getFolder().next();
                if (tmp.getFolderName().equals(FolderStrings.DEFAULT_EMAIL_ATTACHMENTS_FOLDER_NAME)) {
                    srcMailAttachmentFolder = tmp;
                    break;
                }
            }

            Thread.sleep(500);
            triesLeft--;
        }

        InfostoreTestManager itm = new InfostoreTestManager(client);
        itm.setFailOnError(true);
        expectedDocument = new DocumentMetadataImpl();
        expectedDocument.setTitle("Infostore Item");
        expectedDocument.setDescription("Infostore Item Description");
        expectedDocument.setFileMIMEType("text/plain");
        expectedDocument.setFolderId(srcMailAttachmentFolder.getObjectID());
        File upload = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
        expectedDocument.setFileName(upload.getName());
        itm.newAction(new com.openexchange.file.storage.infostore.InfostoreFile(expectedDocument), upload);
        client.logout();
    }

    @Test
    public void testCopyWrongMailAttachments() throws Exception {
        OXUserCopyInterface umi = getUserCopyClient();
        User dstUser = umi.copyUser(srcUser, srcCtx, dstCtx, superAdminCredentials);
        dstUser = ui.getData(dstCtx, dstUser, getCredentials());
        System.err.println("DstUser: " + dstUser.getId() + "(" + dstCtx.getIdAsString() + ")");

        AJAXSession session = performLogin(dstUser.getName() + '@' + dstCtx.getName(), "secret");
        AJAXClient client = new AJAXClient(session, false);
        try {
            ListRequest request = new ListRequest(EnumAPI.OX_NEW, String.valueOf(client.getValues().getPrivateInfostoreFolder()));
            ListResponse response = client.execute(request);
            FolderObject mailAttachmentFolder = null;
            while (response.getFolder().hasNext()) {
                FolderObject tmp = response.getFolder().next();
                if (tmp.getFolderName().equals(FolderStrings.DEFAULT_EMAIL_ATTACHMENTS_FOLDER_NAME)) {
                    mailAttachmentFolder = tmp;
                    break;
                }
            }

            assertNotNull("Mail attachment folder has not been created.", mailAttachmentFolder);
            int columns[] = new int[] {
                Metadata.ID, Metadata.TITLE, Metadata.DESCRIPTION, Metadata.FILE_MIMETYPE, Metadata.FILENAME };
            AllInfostoreRequest allRequest = new AllInfostoreRequest(mailAttachmentFolder.getObjectID(), columns, 1, Order.ASCENDING);
            AbstractColumnsResponse allResponse = client.execute(allRequest);
            Object[][] documents = allResponse.getArray();
            assertEquals("Wrong number of documents in destination folder!", 1, documents.length);
            Object[] document = documents[0];
            assertEquals("Wrong title", expectedDocument.getTitle(), document[1]);
            assertEquals("Wrong description", expectedDocument.getDescription(), document[2]);
            assertEquals("Wrong mime type", expectedDocument.getFileMIMEType(), document[3]);
            assertEquals("Wrong file name", expectedDocument.getFileName(), document[4]);
        } finally {
            client.logout();
        }
    }

    @Override
    public void tearDown() throws Exception {
        try {
            if (ui != null && srcCtx != null) {
                ui.delete(srcCtx, srcUser, null, getCredentials());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (ci != null && srcCtx != null && superAdminCredentials != null) {
                ci.delete(srcCtx, superAdminCredentials);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (ci != null && dstCtx != null && superAdminCredentials != null) {
                ci.delete(dstCtx, superAdminCredentials);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.tearDown();
    }

    private AJAXSession performLogin(final String login, final String password) throws OXException, IOException, JSONException {
        final AJAXSession session = new AJAXSession();
        final LoginRequest loginRequest = new LoginRequest(login, password, LoginTools.generateAuthId(), "Usermovetest", "6.20");
        final LoginResponse loginResponse = Executor.execute(session, loginRequest);
        session.setId(loginResponse.getSessionId());
        return session;
    }

}
