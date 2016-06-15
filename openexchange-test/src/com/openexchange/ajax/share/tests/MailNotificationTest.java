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

package com.openexchange.ajax.share.tests;

import static org.junit.Assert.assertArrayEquals;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.json.JSONException;
import org.jsoup.nodes.Document;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.InsertRequest;
import com.openexchange.ajax.folder.actions.InsertResponse;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreRequest;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.SendLinkRequest;
import com.openexchange.ajax.smtptest.actions.GetMailsRequest;
import com.openexchange.ajax.smtptest.actions.GetMailsResponse.Message;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.notification.NotificationStrings;
import com.openexchange.share.notification.ShareNotifyExceptionCodes;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.test.TestInit;

/**
 * {@link MailNotificationTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class MailNotificationTest extends ShareTest {

    private InfostoreTestManager infostoreTestManager;
    private FolderObject testFolder1;
    private FolderObject publicDriveFolder;
    private File image1, file1;
    private final String IMAGENAME1 = "image1.jpg";
    private final String IMAGETYPE1 = "image/jpeg";
    private final String FILENAME1 = "snippet1.ad";
    private final String FILETYPE1 = "text/plain";
    private Contact clientContact;
    private String clientFullName, clientEmail;
    private final String clientShareMessage = "Hey there, i've got some shares for you!";
    DateFormat dateFormat = null;
    UserValues userValues;

    /**
     * Initializes a new {@link MailNotificationTest}.
     *
     * @param name
     */
    public MailNotificationTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        userValues = client.getValues();
        infostoreTestManager = new InfostoreTestManager(client);
        testFolder1 = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, userValues.getPrivateInfostoreFolder());
        publicDriveFolder = insertPublicFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE);
        image1 = getFile(createFile(testFolder1, IMAGENAME1, IMAGETYPE1).getId());
        file1 = getFile(createFile(testFolder1, FILENAME1, FILETYPE1).getId());
        initUserConfig();
        dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, userValues.getLocale());
    }

    private void initUserConfig() throws OXException, IOException, JSONException {
        com.openexchange.ajax.user.actions.GetRequest request = new GetRequest(client.getValues().getUserId(), client.getValues().getTimeZone());
        com.openexchange.ajax.user.actions.GetResponse response = client.execute(request);
        clientContact = response.getContact();
        clientFullName = String.format("%1$s %2$s", clientContact.getGivenName(), clientContact.getSurName());
        clientEmail = clientContact.getEmail1();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNoMailsButWarningForSharedMailFolderOnCreate() throws Exception {
        /*
         * Create a new mail folder
         */
        FolderObject sharedFolder = Create.createPrivateFolder(randomUID(), FolderObject.MAIL, client.getValues().getUserId());
        sharedFolder.setFullName(client.getValues().getInboxFolder() + "/" + sharedFolder.getFolderName());
        InsertRequest insertFolderReq = new InsertRequest(EnumAPI.OX_NEW, sharedFolder);
        client.execute(insertFolderReq);
        remember(sharedFolder);

        /*
         * Share it to another user and 'accidentally' request a notification message
         */
        AJAXClient client2 = new AJAXClient(User.User2);
        List<OCLPermission> newPermissions = new ArrayList<>(sharedFolder.getPermissions());
        newPermissions.add(Create.ocl(client2.getValues().getUserId(), false, false, OCLPermission.READ_FOLDER,
            OCLPermission.READ_ALL_OBJECTS, OCLPermission.WRITE_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS));
        FolderObject toUpdate = new FolderObject();
        toUpdate.setModule(FolderObject.MAIL);
        toUpdate.setFullName(sharedFolder.getFullName());
        toUpdate.setPermissions(newPermissions);
        toUpdate.setLastModified(new Date());
        UpdateRequest updateReq = new UpdateRequest(EnumAPI.OX_NEW, toUpdate);
        updateReq.setNotifyPermissionEntities(Transport.MAIL, "Look at this!");
        InsertResponse updateResp = client.execute(updateReq);

        /*
         * Assert warning about notification
         */
        assertTrue(updateResp.getResponse().hasWarnings());
        OXException warning = updateResp.getResponse().getWarnings().get(0);
        assertEquals(ShareNotifyExceptionCodes.UNEXPECTED_ERROR.getPrefix(), warning.getPrefix());
        assertEquals(ShareNotifyExceptionCodes.UNEXPECTED_ERROR.getNumber(), warning.getCode());


        /*
         * Assert user 2 can see folder
         */
        GetResponse reloadResponse = client.execute(new com.openexchange.ajax.folder.actions.GetRequest(EnumAPI.OX_NEW, sharedFolder.getFullName()));
        assertFalse(reloadResponse.hasError());
        FolderObject reloadedFolder = reloadResponse.getFolder();
        assertNotNull(reloadedFolder);

        /*
         * Assert no mail was sent
         */
        List<Message> messages = client.execute(new GetMailsRequest()).getMessages();
        assertEquals(0, messages.size());
    }

    //    TODO: Adapt tests and NotifyAction once they are used, e.g. add own com.openexchange.share.notification.ShareNotification.NotificationType
    //          and the matching builder
    //
    //    /**
    //     * Invite a user and expect that he gets his personal link as email, together with the necessary credentials.
    //     * Afterwards share a second folder with the same guest and expect another email without password.
    //     */
    //    public void testCreatedNotificationForGuestWithPassword() throws Exception {
    //        /*
    //         * First invitation
    //         */
    //        OCLGuestPermission guestPermission = createNamedGuestPermission(randomUID() + "@example.com", "TestUser_" + System.currentTimeMillis(), null);
    //        InviteRequest inviteRequest = new InviteRequest();
    //        inviteRequest.addTarget(new ShareTarget(testFolder1.getModule(), Integer.toString(testFolder1.getObjectID())));
    //        inviteRequest.addRecipient(guestPermission.getRecipient());
    //        client.execute(inviteRequest);
    //
    //        List<Message> messages = client.execute(new GetMailsRequest()).getMessages();
    //        assertEquals(1, messages.size());
    //        Message message = messages.get(0);
    //
    //        /*
    //         * assert magic headers and content
    //         */
    //        Map<String, String> headers = message.getHeaders();
    //        assertEquals("share-created", headers.get("X-Open-Xchange-Share-Type"));
    //        String url = headers.get("X-Open-Xchange-Share-URL");
    //        assertNotNull(url);
    //        String access = headers.get("X-Open-Xchange-Share-Access");
    //        assertNotNull(access);
    //        String[] credentials = new String(Base64.decode(access), Charset.forName("UTF-8")).split(":");
    //        assertEquals(2, credentials.length);
    //        String username = credentials[0];
    //        String password = credentials[1];
    //
    //        String plainText = message.getPlainText();
    //        assertNotNull(plainText);
    //        assertTrue(plainText.contains(username));
    //        assertTrue(plainText.contains(password));
    //
    //        /*
    //         * check received link and credentials
    //         */
    //        GuestClient guestClient = new GuestClient(url, username, password);
    //        guestClient.checkFolderAccessible(Integer.toString(testFolder1.getObjectID()), guestPermission);
    //        guestClient.logout();
    //
    //        /*
    //         * second invitation
    //         */
    //        inviteRequest = new InviteRequest();
    //        inviteRequest.addTarget(new ShareTarget(testFolder2.getModule(), Integer.toString(testFolder2.getObjectID())));
    //        inviteRequest.addRecipient(guestPermission.getRecipient());
    //        client.execute(inviteRequest);
    //
    //        messages = client.execute(new GetMailsRequest()).getMessages();
    //        assertEquals(1, messages.size());
    //        message = messages.get(0);
    //
    //        /*
    //         * assert magic headers and content
    //         */
    //        headers = message.getHeaders();
    //        assertEquals("share-created", headers.get("X-Open-Xchange-Share-Type"));
    //        url = headers.get("X-Open-Xchange-Share-URL");
    //        assertNotNull(url);
    //        assertNull(headers.get("X-Open-Xchange-Share-Access"));
    //
    //        /*
    //         * re-login with existing credentials
    //         */
    //        guestClient = new GuestClient(url, username, password);
    //        guestClient.checkFolderAccessible(Integer.toString(testFolder2.getObjectID()), guestPermission);
    //        guestClient.logout();
    //    }
    //
    //    /**
    //     * Get a password-secured link and distribute it via notify action
    //     */
    //    public void testNotifyAnonymousWithPassword() throws Exception {
    //        /*
    //         * get link
    //         */
    //        String password = PasswordUtility.generate();
    //        OCLGuestPermission permission = createAnonymousAuthorPermission(password);
    //        GetLinkRequest getLinkRequest = new GetLinkRequest(Collections.singletonList(new ShareTarget(testFolder1.getModule(), Integer.toString(testFolder1.getObjectID()))));
    //        getLinkRequest.setBits(permission.getPermissionBits());
    //        getLinkRequest.setPassword(password);
    //        GetLinkResponse getLinkResponse = client.execute(getLinkRequest);
    //
    //        /*
    //         * notify
    //         */
    //        String textMessage = randomUID();
    //        NotifyRequest notifyRequest = new NotifyRequest(getLinkResponse.getToken(), textMessage + "@example.com");
    //        notifyRequest.setMessage(textMessage);
    //        client.execute(notifyRequest);
    //        List<Message> messages = client.execute(new GetMailsRequest()).getMessages();
    //        assertEquals(1, messages.size());
    //        Message message = messages.get(0);
    //
    //        /*
    //         * assert magic headers and content
    //         */
    //        Map<String, String> headers = message.getHeaders();
    //        assertEquals("share-created", headers.get("X-Open-Xchange-Share-Type"));
    //        String url = headers.get("X-Open-Xchange-Share-URL");
    //        assertNotNull(url);
    //        String access = headers.get("X-Open-Xchange-Share-Access");
    //        assertNotNull(access);
    //        String receivedPassword = new String(Base64.decode(access), Charset.forName("UTF-8"));
    //        assertEquals(password, receivedPassword);
    //
    //        String plainText = message.getPlainText();
    //        assertNotNull(plainText);
    //        assertTrue(plainText.contains(receivedPassword));
    //        assertTrue(plainText.contains(textMessage));
    //
    //        /*
    //         * check received link and credentials
    //         */
    //        GuestClient guestClient = new GuestClient(url, null, receivedPassword);
    //        guestClient.checkFolderAccessible(Integer.toString(testFolder1.getObjectID()), permission);
    //        guestClient.logout();
    //    }

    //---IMAGES-----------------------------------------------------------------------------------------------------------------------------

    public void testUserGotAnImage() throws Exception {
        testUserGotA(
            testFolder1,
            image1,
            String.format(NotificationStrings.SUBJECT_SHARED_IMAGE, clientFullName, image1.getFileName()),
            String.format(NotificationStrings.HAS_SHARED_IMAGE_NO_MESSAGE, clientFullName, clientEmail, image1.getFileName()),
            NotificationStrings.VIEW_IMAGE,
            null, true);
    }

    public void testUserGotAnImageAndMessage() throws Exception {
        testUserGotA(
        		testFolder1,
        		image1,
        		String.format(NotificationStrings.SUBJECT_SHARED_IMAGE, clientFullName, image1.getFileName()),
        		String.format(NotificationStrings.HAS_SHARED_PHOTO_AND_MESSAGE, clientFullName, clientEmail, image1.getFileName()),
        		NotificationStrings.VIEW_IMAGE,
        		clientShareMessage, true);
    }

    //---FILES------------------------------------------------------------------------------------------------------------------------------

    public void testUserGotAFile() throws Exception {
        testUserGotA(
        		testFolder1,
        		file1,
        		String.format(NotificationStrings.SUBJECT_SHARED_FILE, clientFullName, file1.getFileName()),
        		String.format(NotificationStrings.HAS_SHARED_FILE_NO_MESSAGE, clientFullName, clientEmail, file1.getFileName()),
        		NotificationStrings.VIEW_FILE,
        		null, true);
    }

    public void testUserGotAFileAndMessage() throws Exception {
        testUserGotA(
        		testFolder1,
        		file1,
        		String.format(NotificationStrings.SUBJECT_SHARED_FILE, clientFullName, file1.getFileName()),
        		String.format(NotificationStrings.HAS_SHARED_FILE_AND_MESSAGE, clientFullName, clientEmail, file1.getFileName()),
        		NotificationStrings.VIEW_FILE,
        		clientShareMessage, true);
    }

    //---FOLDER-----------------------------------------------------------------------------------------------------------------------------

    public void testUserGotAFolder() throws Exception {
        testUserGotA(
        		testFolder1,
        		null,
        		String.format(NotificationStrings.SUBJECT_SHARED_FOLDER, clientFullName, testFolder1.getFolderName()),
        		String.format(NotificationStrings.HAS_SHARED_FOLDER_NO_MESSAGE, clientFullName, clientEmail, testFolder1.getFolderName()),
        		NotificationStrings.VIEW_FOLDER,
        		null, true);
    }

    public void testUserGotAFolderAndMessage() throws Exception {
        testUserGotA(
        		testFolder1,
        		null,
        		String.format(NotificationStrings.SUBJECT_SHARED_FOLDER, clientFullName, testFolder1.getFolderName()),
        		String.format(NotificationStrings.HAS_SHARED_FOLDER_AND_MESSAGE, clientFullName, clientEmail, testFolder1.getFolderName()),
        		NotificationStrings.VIEW_FOLDER,
        		clientShareMessage, true);
    }

    public void testAnonymousGotAFolder() throws Exception {
        testAnonymousGotA(
                testFolder1,
                null,
                String.format(NotificationStrings.SUBJECT_SHARED_FOLDER, clientFullName, testFolder1.getFolderName()),
                String.format(NotificationStrings.HAS_SHARED_FOLDER_NO_MESSAGE, clientFullName, clientEmail, testFolder1.getFolderName()),
                NotificationStrings.VIEW_FOLDER,
                null,
                randomUID(),
                new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)));
    }

    public void testDontNotifyInternalsOnPublicFolderShare() throws Exception {
        testUserGotNoNotification(publicDriveFolder, null);
    }

    //---PERMISSION APIs--------------------------------------------------------------------------------------------------------------------

    public void testGuestGetsMessageIfAddedViaFolderPermission() throws Exception {
        OCLGuestPermission guestPermission = createNamedGuestPermission(randomUID() + "@example.com", "TestUser_" + System.currentTimeMillis(), null);
        FolderObject toUpdate = new FolderObject();
        toUpdate.setObjectID(testFolder1.getObjectID());
        toUpdate.setLastModified(testFolder1.getLastModified());
        toUpdate.setFolderName(testFolder1.getFolderName());
        toUpdate.setPermissions(testFolder1.getPermissions());
        toUpdate.addPermission(guestPermission);
        updateFolder(EnumAPI.OX_NEW, toUpdate, new RequestCustomizer<UpdateRequest>() {
            @Override
            public void customize(UpdateRequest request) {
                request.setNotifyPermissionEntities(Transport.MAIL, clientShareMessage);
            }
        });

        Message message = assertAndGetMessage();
        Document document = message.requireHtml();
        String initialSubject = String.format(NotificationStrings.SUBJECT_SHARED_FOLDER, clientFullName, testFolder1.getFolderName());
        String hasSharedString = String.format(NotificationStrings.HAS_SHARED_FOLDER_AND_MESSAGE, clientFullName, clientEmail, testFolder1.getFolderName());
        String viewItemStringString = NotificationStrings.VIEW_FOLDER;

        assertSubject(message.getMimeMessage(), initialSubject);
        assertHasSharedItems(document, hasSharedString);
        assertViewItems(document, viewItemStringString);
        assertShareMessage(document, clientShareMessage);

        assertSignatureText(document, "");
        assertSignatureImage(message);
    }

    public void testGuestGetsMessageIfAddedViaFilePermission() throws Exception {
        FileStorageObjectPermission guestPermission = asObjectPermission(createNamedGuestPermission(randomUID() + "@example.com", "TestUser_" + System.currentTimeMillis(), null));
        File testFile = insertFile(testFolder1.getObjectID());

        DefaultFile toUpdate = new DefaultFile();
        toUpdate.setId(testFile.getId());
        toUpdate.setFolderId(testFile.getFolderId());
        toUpdate.setLastModified(testFile.getLastModified());
        List<FileStorageObjectPermission> newPermissions = new ArrayList<>(2);
        List<FileStorageObjectPermission> oldPermissions = testFile.getObjectPermissions();
        if (oldPermissions != null) {
            newPermissions.addAll(oldPermissions);
        }

        newPermissions.add(guestPermission);
        toUpdate.setObjectPermissions(newPermissions);
        testFile = updateFile(toUpdate, new Field[] { Field.OBJECT_PERMISSIONS }, new RequestCustomizer<UpdateInfostoreRequest>() {
            @Override
            public void customize(UpdateInfostoreRequest request) {
                request.setNotifyPermissionEntities(Transport.MAIL, clientShareMessage);
            }
        });

        Message message = assertAndGetMessage();
        Document document = message.requireHtml();
        String initialSubject = String.format(NotificationStrings.SUBJECT_SHARED_FILE, clientFullName, testFile.getFileName());
        String hasSharedString = String.format(NotificationStrings.HAS_SHARED_FILE_AND_MESSAGE, clientFullName, clientEmail, testFile.getFileName());
        String viewItemStringString = NotificationStrings.VIEW_FILE;

        assertSubject(message.getMimeMessage(), initialSubject);
        assertHasSharedItems(document, hasSharedString);
        assertViewItems(document, viewItemStringString);
        assertShareMessage(document, clientShareMessage);

        assertSignatureText(document, "");
        assertSignatureImage(message);
    }

    //---HELPERS----------------------------------------------------------------------------------------------------------------------------

    public void testUserGotA(FolderObject testFolder, File file, String initialSubject, String hasSharedString, String viewItemString, String shareMessage, boolean notify) throws Exception {
        share(testFolder, file, createNamedGuestPermission(randomUID() + "@example.com", "TestUser_" + System.currentTimeMillis(), null), shareMessage, notify);
        assertGotA(
            initialSubject,
            hasSharedString,
            viewItemString,
            shareMessage,
            null,
            null);
    }

    public void testAnonymousGotA(FolderObject testFolder, File file, String initialSubject, String hasSharedString, String viewItemString, final String shareMessage, String password, Date expiryDate) throws Exception {
        OCLGuestPermission permission = createAnonymousGuestPermission();
        ((AnonymousRecipient) permission.getRecipient()).setPassword(password);
        ((AnonymousRecipient) permission.getRecipient()).setExpiryDate(expiryDate);
        share(testFolder, file, permission, shareMessage, true);
        ShareTarget target = new ShareTarget(
            testFolder.getModule(),
            Integer.toString(testFolder.getObjectID()),
            file == null ? null : file.getId());
        client.execute(new SendLinkRequest(target, randomUID() + "@example.com", shareMessage));
        assertGotA(
            initialSubject,
            hasSharedString,
            viewItemString,
            shareMessage,
            password,
            expiryDate);
    }

    public void assertGotA(String initialSubject, String hasSharedString, String viewItemString, final String shareMessage, String password, Date expiryDate) throws Exception {
        Message message = assertAndGetMessage();
        Document document = message.requireHtml();
        assertSubject(message.getMimeMessage(), initialSubject);
        assertHasSharedItems(document, hasSharedString);
        assertViewItems(document, viewItemString);

        if (shareMessage != null) {
            assertShareMessage(document, shareMessage);
        }

        assertSignatureText(document, "");
        assertSignatureImage(message);

        if (password != null) {
            assertPassword(document, password);
        }

        if (expiryDate != null) {
            assertExpiryDate(document, expiryDate);
        }
    }

    private void share(FolderObject testFolder, File file, OCLPermission guestPermission, final String shareMessage, final boolean notify) throws Exception {
        if (null != file) {
            DefaultFile toUpdate = new DefaultFile();
            toUpdate.setId(file.getId());
            toUpdate.setFolderId(file.getFolderId());
            toUpdate.setLastModified(file.getLastModified());
            List<FileStorageObjectPermission> newPermissions = new ArrayList<>(2);
            List<FileStorageObjectPermission> oldPermissions = file.getObjectPermissions();
            if (oldPermissions != null) {
                newPermissions.addAll(oldPermissions);
            }

            newPermissions.add(asObjectPermission(guestPermission));
            toUpdate.setObjectPermissions(newPermissions);
            file = updateFile(toUpdate, new Field[] { Field.OBJECT_PERMISSIONS }, new RequestCustomizer<UpdateInfostoreRequest>() {
                @Override
                public void customize(UpdateInfostoreRequest request) {
                    if (notify) {
                        request.setNotifyPermissionEntities(Transport.MAIL, shareMessage);
                    }
                }
            });
        } else {
            FolderObject toUpdate = new FolderObject();
            toUpdate.setObjectID(testFolder.getObjectID());
            toUpdate.setLastModified(testFolder.getLastModified());
            toUpdate.setFolderName(testFolder.getFolderName());
            toUpdate.setPermissions(testFolder.getPermissions());
            toUpdate.addPermission(guestPermission);
            updateFolder(EnumAPI.OX_NEW, toUpdate, new RequestCustomizer<UpdateRequest>() {
                @Override
                public void customize(UpdateRequest request) {
                    if (notify) {
                        request.setNotifyPermissionEntities(Transport.MAIL, shareMessage);
                    }
                }
            });
        }
    }

    public void testUserGotNoNotification(FolderObject testFolder, File file) throws Exception {
        AJAXClient secondClient = new AJAXClient(User.User2);
        int internalUserId = secondClient.getValues().getUserId();
        secondClient.logout();
        OCLPermission permission = new OCLPermission();
        permission.setEntity(internalUserId);
        permission.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        share(testFolder, file, permission, null, false);
        List<Message> messages = client.execute(new GetMailsRequest()).getMessages();
        assertEquals(0, messages.size());
    }

    public File createFile(FolderObject folder, String fileName, String mimeType) throws Exception {
        long now = System.currentTimeMillis();
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(folder.getObjectID()));
        file.setTitle(fileName + now);
        file.setFileName(file.getTitle());
        file.setDescription(file.getTitle());
        file.setFileMIMEType(mimeType);
        infostoreTestManager.newAction(file, new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile")));
        return file;
    }

    private Message assertAndGetMessage() throws JSONException, MessagingException, OXException, IOException {
        List<Message> messages = client.execute(new GetMailsRequest()).getMessages();
        assertEquals(1, messages.size());
        return messages.get(0);
    }

    private void assertSubject(MimeMessage mimeMessage, String expected) throws MessagingException {
        String subject = mimeMessage.getSubject();
        assertEquals(expected, subject);
    }

    private void assertHasSharedItems(Document document, String expected) {
        assertEquals(expected, document.getElementById("has_shared_items").ownText());
    }

    private void assertViewItems(Document document, String expected) {
        assertEquals(expected, document.getElementById("view_items").getElementsByTag("span").first().ownText());
    }

    private void assertShareMessage(Document document, String expected) {
        assertEquals(expected, document.getElementById("user_message").text());
    }

    private void assertExpiryDate(Document document, Date expiryDate) {
        assertEquals(String.format(NotificationStrings.LINK_EXPIRE, dateFormat.format(expiryDate)), document.getElementById("will_expire").ownText());
    }

    private void assertPassword(Document document, String password) {
        assertEquals(NotificationStrings.USE_PASSWORD + " " + password, document.getElementById("use_password").ownText());
    }

    private void assertSignatureText(Document document, String expected) {
        assertEquals(expected, document.getElementById("signature_text").ownText());
    }

    private void assertSignatureImage(Message message) throws Exception {
        String src = message.requireHtml().getElementById("signature_image").attr("src");
        BodyPart image = message.getBodyPartByContentID("<" + src.substring(4) + ">");
        assertNotNull(image);
        byte[] expectedBytes = BaseEncoding.base64().decode(SIGNATURE_IMAGE);
        byte[] imageBytes = ByteStreams.toByteArray(image.getInputStream());
        assertArrayEquals(expectedBytes, imageBytes);
    }

    private final String SIGNATURE_IMAGE = "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";

}
