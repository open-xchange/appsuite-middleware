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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.junit.Test;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.folder.actions.UpdateRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.UserValues;
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
import com.openexchange.share.notification.NotificationStrings;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.recipient.AnonymousRecipient;
import com.openexchange.test.TestInit;

/**
 * {@link MailNotificationTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class MailNotificationTest extends ShareTest {

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

    @Override
    public void setUp() throws Exception {
        super.setUp();
        userValues = getClient().getValues();
        testFolder1 = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, userValues.getPrivateInfostoreFolder());
        publicDriveFolder = insertPublicFolder(FolderObject.INFOSTORE);
        image1 = getFile(createFile(testFolder1, IMAGENAME1, IMAGETYPE1).getId());
        file1 = getFile(createFile(testFolder1, FILENAME1, FILETYPE1).getId());
        initUserConfig();
        dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, userValues.getLocale());
    }

    private void initUserConfig() throws OXException, IOException, JSONException {
        com.openexchange.ajax.user.actions.GetRequest request = new GetRequest(getClient().getValues().getUserId(), getClient().getValues().getTimeZone());
        com.openexchange.ajax.user.actions.GetResponse response = getClient().execute(request);
        clientContact = response.getContact();
        clientFullName = String.format("%1$s %2$s", clientContact.getGivenName(), clientContact.getSurName());
        clientEmail = clientContact.getEmail1();
    }

    //---IMAGES-----------------------------------------------------------------------------------------------------------------------------
    @Test
    public void testUserGotAnImage() throws Exception {
        testUserGotA(   testFolder1, 
                        image1, 
                        String.format(NotificationStrings.SUBJECT_SHARED_IMAGE, clientFullName, image1.getFileName()), 
                        String.format(NotificationStrings.HAS_SHARED_IMAGE_NO_MESSAGE, clientFullName, clientEmail, image1.getFileName()), 
                        NotificationStrings.VIEW_IMAGE, 
                        null, 
                        true);
    }

    @Test
    public void testUserGotAnImageAndMessage() throws Exception {
        testUserGotA(   testFolder1, 
                        image1, 
                        String.format(NotificationStrings.SUBJECT_SHARED_IMAGE, clientFullName, image1.getFileName()), 
                        String.format(NotificationStrings.HAS_SHARED_PHOTO_AND_MESSAGE, clientFullName, clientEmail, image1.getFileName()), 
                        NotificationStrings.VIEW_IMAGE, 
                        clientShareMessage, 
                        true);
    }

    //---FILES------------------------------------------------------------------------------------------------------------------------------
    @Test
    public void testUserGotAFile() throws Exception {
        testUserGotA(   testFolder1, 
                        file1, 
                        String.format(NotificationStrings.SUBJECT_SHARED_FILE, clientFullName, file1.getFileName()), 
                        String.format(NotificationStrings.HAS_SHARED_FILE_NO_MESSAGE, clientFullName, clientEmail, file1.getFileName()), 
                        NotificationStrings.VIEW_FILE, 
                        null, 
                        true);
    }

    @Test
    public void testUserGotAFileAndMessage() throws Exception {
        testUserGotA(   testFolder1, 
                        file1, 
                        String.format(NotificationStrings.SUBJECT_SHARED_FILE, clientFullName, file1.getFileName()), 
                        String.format(NotificationStrings.HAS_SHARED_FILE_AND_MESSAGE, clientFullName, clientEmail, file1.getFileName()), 
                        NotificationStrings.VIEW_FILE, 
                        clientShareMessage, 
                        true);
    }

    //---FOLDER-----------------------------------------------------------------------------------------------------------------------------
    @Test
    public void testUserGotAFolder() throws Exception {
        testUserGotA(   testFolder1, 
                        null, 
                        String.format(NotificationStrings.SUBJECT_SHARED_FOLDER, clientFullName, testFolder1.getFolderName()), 
                        String.format(NotificationStrings.HAS_SHARED_FOLDER_NO_MESSAGE, clientFullName, clientEmail, testFolder1.getFolderName()), 
                        NotificationStrings.VIEW_FOLDER, 
                        null, 
                        true);
    }

    @Test
    public void testUserGotAFolderAndMessage() throws Exception {
        testUserGotA(   testFolder1, 
                        null, 
                        String.format(NotificationStrings.SUBJECT_SHARED_FOLDER, clientFullName, testFolder1.getFolderName()), 
                        String.format(NotificationStrings.HAS_SHARED_FOLDER_AND_MESSAGE, clientFullName, clientEmail, testFolder1.getFolderName()), 
                        NotificationStrings.VIEW_FOLDER, 
                        clientShareMessage, 
                        true);
    }

    @Test
    public void testAnonymousGotAFolder() throws Exception {
        testAnonymousGotA(  testFolder1, 
                            null, 
                            String.format(NotificationStrings.SUBJECT_SHARED_FOLDER, clientFullName, testFolder1.getFolderName()), 
                            String.format(NotificationStrings.HAS_SHARED_FOLDER_NO_MESSAGE, clientFullName, clientEmail, testFolder1.getFolderName()), 
                            NotificationStrings.VIEW_FOLDER, 
                            null, 
                            randomUID(), 
                            new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)));
    }

    @Test
    public void testDontNotifyInternalsOnPublicFolderShare() throws Exception {
        testUserGotNoNotification(publicDriveFolder, null);
    }

    //---PERMISSION APIs--------------------------------------------------------------------------------------------------------------------
    @Test
    public void testGuestGetsMessageIfAddedViaFolderPermission() throws Exception {
        OCLGuestPermission guestPermission = createNamedGuestPermission(randomUID() + "@example.com", "TestUser_" + UUID.randomUUID().toString(), null);
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

    @Test
    public void testGuestGetsMessageIfAddedViaFilePermission() throws Exception {
        FileStorageObjectPermission guestPermission = asObjectPermission(createNamedGuestPermission(randomUID() + "@example.com", "TestUser_" + UUID.randomUUID().toString(), null));
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
    private void testUserGotA(FolderObject testFolder, File file, String initialSubject, String hasSharedString, String viewItemString, String shareMessage, boolean notify) throws Exception {
        share(testFolder, file, createNamedGuestPermission(randomUID() + "@example.com", "TestUser_" + UUID.randomUUID().toString(), null), shareMessage, notify);
        assertGotA(initialSubject, hasSharedString, viewItemString, shareMessage, null, null);
    }

    private void testAnonymousGotA(FolderObject testFolder, File file, String initialSubject, String hasSharedString, String viewItemString, final String shareMessage, String password, Date expiryDate) throws Exception {
        OCLGuestPermission permission = createAnonymousGuestPermission();
        ((AnonymousRecipient) permission.getRecipient()).setPassword(password);
        ((AnonymousRecipient) permission.getRecipient()).setExpiryDate(expiryDate);
        share(testFolder, file, permission, shareMessage, true);
        ShareTarget target = new ShareTarget(testFolder.getModule(), Integer.toString(testFolder.getObjectID()), file == null ? null : file.getId());
        getClient().execute(new SendLinkRequest(target, randomUID() + "@example.com", shareMessage));
        assertGotA(initialSubject, hasSharedString, viewItemString, shareMessage, password, expiryDate);
    }

    private void assertGotA(String initialSubject, String hasSharedString, String viewItemString, final String shareMessage, String password, Date expiryDate) throws Exception {
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
                    } else {
                        request.setNotifyPermissionEntities(null);
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
                    } else {
                        request.setNotifyPermissionEntities(null);
                    }
                }
            });
        }
    }

    private void testUserGotNoNotification(FolderObject testFolder, File file) throws Exception {
        AJAXClient secondClient = getClient2();
        int internalUserId = secondClient.getValues().getUserId();
        secondClient.logout();
        OCLPermission permission = new OCLPermission();
        permission.setEntity(internalUserId);
        permission.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.READ_ALL_OBJECTS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        share(testFolder, file, permission, null, false);
        List<Message> messages = getClient().execute(new GetMailsRequest()).getMessages();
        assertEquals(0, messages.size());
    }

    private File createFile(FolderObject folder, String fileName, String mimeType) throws Exception {
        long now = System.currentTimeMillis();
        File file = new DefaultFile();
        file.setFolderId(String.valueOf(folder.getObjectID()));
        file.setTitle(fileName + now);
        file.setFileName(file.getTitle());
        file.setDescription(file.getTitle());
        file.setFileMIMEType(mimeType);
        itm.newAction(file, new java.io.File(TestInit.getTestProperty("ajaxPropertiesFile")));
        return file;
    }

    private Message assertAndGetMessage() throws JSONException, MessagingException, OXException, IOException {
        List<Message> messages = getClient().execute(new GetMailsRequest()).getMessages();
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
    }

}
