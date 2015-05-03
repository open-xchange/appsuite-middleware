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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Ignore;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.manifests.actions.ConfigRequest;
import com.openexchange.ajax.manifests.actions.ConfigResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.GetLinkRequest;
import com.openexchange.ajax.share.actions.GetLinkResponse;
import com.openexchange.ajax.share.actions.GetMailsRequest;
import com.openexchange.ajax.share.actions.GetMailsResponse.Message;
import com.openexchange.ajax.share.actions.InviteRequest;
import com.openexchange.ajax.share.actions.NotifyRequest;
import com.openexchange.ajax.share.actions.StartSMTPRequest;
import com.openexchange.ajax.share.actions.StopSMTPRequest;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.notification.impl.NotificationStrings;
import com.openexchange.share.tools.PasswordUtility;
import com.openexchange.test.TestInit;
import com.openexchange.tools.encoding.Base64;


/**
 * {@link MailNotificationTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class MailNotificationTest extends ShareTest {

    private InfostoreTestManager infostoreTestManager;
    private FolderObject testFolder1;
    private FolderObject testFolder2;
    private File image1, image2, file1, file2;
    private final String IMAGENAME1 = "image1.jpg", IMAGENAME2 = "image2.png";
    private final String IMAGETYPE1 = "image/jpeg", IMAGETYPE2 = "image/png";
    private final String FILENAME1 = "snippet1.ad", FILENAME2 = "snippet2.md";
    private final String FILETYPE1 = "text/plain", FILETYPE2 = "text/plain";
    private String productName;
    private Contact clientContact;
    private String clientFullName, clientEmail;
    private String clientShareMessage = "Hey there, i've got some shares for you!";
    private Date shareExpireDate = null;
    DateFormat dateFormat = null;
    UserValues userValues;
    

    /**
     * Initializes a new {@link MailNotificationTest}.
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
        testFolder2 = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, userValues.getPrivateInfostoreFolder());
        image1 = createFile(testFolder1, IMAGENAME1, IMAGETYPE1);
        image2 = createFile(testFolder1, IMAGENAME2, IMAGETYPE2);
        file1 = createFile(testFolder1, FILENAME1, FILETYPE1);
        file2 = createFile(testFolder1, FILENAME2, FILETYPE2);
        client.execute(new StartSMTPRequest());
        initProductName();
        initUserConfig();
        shareExpireDate = new SimpleDateFormat("yyyy-MM-dd", userValues.getLocale()).parse("2048-12-02");
        dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, userValues.getLocale());
    }
    
    private void initProductName() throws JSONException, OXException, IOException {
        ConfigRequest request = new ConfigRequest();
        ConfigResponse response = client.execute(request);
        productName = response.getConfig().getString("productName");
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
        client.execute(new StopSMTPRequest());
        super.tearDown();
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
            new File[] {image1},
            String.format(NotificationStrings.SUBJECT_WELCOME_INVITE_TO_PRODUCT, clientFullName, productName),
            String.format(NotificationStrings.HAS_SHARED_IMAGE, clientFullName, clientEmail, image1.getFileName()),
            NotificationStrings.VIEW_IMAGE,
            String.format(NotificationStrings.SUBJECT_SHARED_IMAGE, clientFullName, image1.getFileName()),
            null,
            shareExpireDate
            );
    }
    
    public void testUserGotAnImageAndMessage() throws Exception {
        testUserGotA(
            testFolder1,
            new File[] {image1},
            String.format(NotificationStrings.SUBJECT_WELCOME_INVITE_TO_PRODUCT, clientFullName, productName),
            String.format(NotificationStrings.HAS_SHARED_PHOTO_AND_MESSAGE, clientFullName, clientEmail, image1.getFileName()),
            NotificationStrings.VIEW_IMAGE,
            String.format(NotificationStrings.SUBJECT_SHARED_IMAGE, clientFullName, image1.getFileName()),
            clientShareMessage,
            shareExpireDate
            );
    }
    
    public void testUserGotImages() throws Exception {
        File[] files = new File[] {image1, image2};
        testUserGotA(
            testFolder1,
            files,
            String.format(NotificationStrings.SUBJECT_WELCOME_INVITE_TO_PRODUCT, clientFullName, productName),
            String.format(NotificationStrings.HAS_SHARED_IMAGES, clientFullName, clientEmail, files.length),
            NotificationStrings.VIEW_IMAGES,
            String.format(NotificationStrings.SUBJECT_SHARED_IMAGES, clientFullName, files.length),
            null,
            shareExpireDate
            );
    }
    
    public void testUserGotImagesAndMessage() throws Exception {
        File[] files = new File[] {image1, image2};
        testUserGotA(
            testFolder1,
            files,
            String.format(NotificationStrings.SUBJECT_WELCOME_INVITE_TO_PRODUCT, clientFullName, productName),
            String.format(NotificationStrings.HAS_SHARED_IMAGES_AND_MESSAGE, clientFullName, clientEmail, files.length),
            NotificationStrings.VIEW_IMAGES,
            String.format(NotificationStrings.SUBJECT_SHARED_IMAGES, clientFullName, files.length),
            clientShareMessage,
            shareExpireDate
            );
    }
    
    
    //---FILES------------------------------------------------------------------------------------------------------------------------------
    
    public void testUserGotAFile() throws Exception {
        testUserGotA(
            testFolder1,
            new File[] {file1},
            String.format(NotificationStrings.SUBJECT_WELCOME_INVITE_TO_PRODUCT, clientFullName, productName),
            String.format(NotificationStrings.HAS_SHARED_FILE, clientFullName, clientEmail, file1.getFileName()),
            NotificationStrings.VIEW_FILE,
            String.format(NotificationStrings.SUBJECT_SHARED_FILE, clientFullName, file1.getFileName()),
            null,
            shareExpireDate
            );
    }
    
    public void testUserGotAFileAndMessage() throws Exception {
        testUserGotA(
            testFolder1,
            new File[] {file1},
            String.format(NotificationStrings.SUBJECT_WELCOME_INVITE_TO_PRODUCT, clientFullName, productName),
            String.format(NotificationStrings.HAS_SHARED_FILE_AND_MESSAGE, clientFullName, clientEmail, file1.getFileName()),
            NotificationStrings.VIEW_FILE,
            String.format(NotificationStrings.SUBJECT_SHARED_FILE, clientFullName, file1.getFileName()),
            clientShareMessage,
            shareExpireDate
            );
    }
    
    public void testUserGotFiles() throws Exception {
        File[] files = new File[] {file1, file2};
        testUserGotA(
            testFolder1,
            files,
            String.format(NotificationStrings.SUBJECT_WELCOME_INVITE_TO_PRODUCT, clientFullName, productName),
            String.format(NotificationStrings.HAS_SHARED_FILES, clientFullName, clientEmail, files.length),
            NotificationStrings.VIEW_FILES,
            String.format(NotificationStrings.SUBJECT_SHARED_FILES, clientFullName, files.length),
            null,
            shareExpireDate
            );
    }
    
    public void testUserGotFilesAndMessage() throws Exception {
        File[] files = new File[] {file1, file2};
        testUserGotA(
            testFolder1,
            files,
            String.format(NotificationStrings.SUBJECT_WELCOME_INVITE_TO_PRODUCT, clientFullName, productName),
            String.format(NotificationStrings.HAS_SHARED_FILES_AND_MESSAGE, clientFullName, clientEmail, files.length),
            NotificationStrings.VIEW_FILES,
            String.format(NotificationStrings.SUBJECT_SHARED_FILES, clientFullName, files.length),
            clientShareMessage,
            shareExpireDate
            );
    }
    
    
    //---FILE AND IMAGE---------------------------------------------------------------------------------------------------------------------

    public void testUserGotFileAndImage() throws Exception {
        File[] files = new File[] {image1, file2};
        testUserGotA(
            testFolder1,
            files,
            String.format(NotificationStrings.SUBJECT_WELCOME_INVITE_TO_PRODUCT, clientFullName, productName),
            String.format(NotificationStrings.HAS_SHARED_ITEMS, clientFullName, clientEmail, files.length),
            NotificationStrings.VIEW_ITEMS,
            String.format(NotificationStrings.SUBJECT_SHARED_ITEMS, clientFullName, files.length),
            null,
            shareExpireDate
            );
    }
    
    public void testUserGotFileAndImageAndMessage() throws Exception {
        File[] files = new File[] {image1, file2};
        testUserGotA(
            testFolder1,
            files,
            String.format(NotificationStrings.SUBJECT_WELCOME_INVITE_TO_PRODUCT, clientFullName, productName),
            String.format(NotificationStrings.HAS_SHARED_ITEMS_AND_MESSAGE, clientFullName, clientEmail, files.length),
            NotificationStrings.VIEW_ITEMS,
            String.format(NotificationStrings.SUBJECT_SHARED_ITEMS, clientFullName, files.length),
            clientShareMessage,
            shareExpireDate
            );
    }
    
    //---FOLDER-----------------------------------------------------------------------------------------------------------------------------
    
    public void testUserGotAFolder() throws Exception {
        testUserGotA(
            testFolder1,
            new File[] {},
            String.format(NotificationStrings.SUBJECT_WELCOME_INVITE_TO_PRODUCT, clientFullName, productName),
            String.format(NotificationStrings.HAS_SHARED_FOLDER, clientFullName, clientEmail, testFolder1.getFolderName()),
            NotificationStrings.VIEW_FOLDER,
            String.format(NotificationStrings.SUBJECT_SHARED_FOLDER, clientFullName, testFolder1.getFolderName()),
            null,
            shareExpireDate
            );
    }
    
    public void testUserGotAFolderAndMessage() throws Exception {
        testUserGotA(
            testFolder1,
            new File[] {},
            String.format(NotificationStrings.SUBJECT_WELCOME_INVITE_TO_PRODUCT, clientFullName, productName),
            String.format(NotificationStrings.HAS_SHARED_FOLDER_AND_MESSAGE, clientFullName, clientEmail, testFolder1.getFolderName()),
            NotificationStrings.VIEW_FOLDER,
            String.format(NotificationStrings.SUBJECT_SHARED_FOLDER, clientFullName, testFolder1.getFolderName()),
            clientShareMessage,
            shareExpireDate
            );
    }
    
    //---HELPERS----------------------------------------------------------------------------------------------------------------------------
    
    public void testUserGotA(FolderObject testFolder, File[] files, String initialSubject, String hasSharedString, String viewItemString, String knownSubject, String shareMessage, Date expiryDate) throws Exception {
        System.out.println("Expiry date: " + expiryDate + " in Timezone:" + userValues.getTimeZone());
        OCLGuestPermission guestPermission = createNamedGuestPermission(randomUID() + "@example.com", "TestUser_" + System.currentTimeMillis(), null);
        InviteRequest inviteRequest = new InviteRequest();
        for (File file : files) {
            ShareTarget shareTarget = new ShareTarget(testFolder.getModule(), Integer.toString(testFolder.getObjectID()), file.getId());
            shareTarget.setExpiryDate(expiryDate);
            inviteRequest.addTarget(shareTarget);
        }
        if(files.length == 0) {
            ShareTarget shareTarget = new ShareTarget(testFolder.getModule(), Integer.toString(testFolder.getObjectID()));
            shareTarget.setExpiryDate(expiryDate);
            inviteRequest.addTarget(shareTarget);
        }
        inviteRequest.setMessage(shareMessage);
        inviteRequest.addRecipient(guestPermission.getRecipient());
        client.execute(inviteRequest);

        Message message = assertAndGetMessage();
        Document document = message.getHtml();
        assertSubject(message.getMimeMessage(), initialSubject);
        assertHasSharedItems(document, hasSharedString);
        assertViewItems(document, viewItemString);
        
        if(shareMessage != null) {
            assertShareMessage(document, shareMessage);
        }
        if(expiryDate != null) {
            assertExpiryDate(document, expiryDate);
        }
        
        // share again and verify that the subject changed as the guest is already known 
        client.execute(inviteRequest);
        message = assertAndGetMessage();
        document = message.getHtml();
        assertSubject(message.getMimeMessage(), knownSubject);
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
        assertEquals(expected, document.getElementById("user_message").ownText());
    }
    
    private void assertExpiryDate(Document document, Date expiryDate) {
        assertEquals(String.format(NotificationStrings.LINK_EXPIRE, dateFormat.format(expiryDate)),
            document.getElementById("will_expire").ownText());
    }
    
}
