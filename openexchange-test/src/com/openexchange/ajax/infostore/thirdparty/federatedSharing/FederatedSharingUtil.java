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

package com.openexchange.ajax.infostore.thirdparty.federatedSharing;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.java.Strings;
import com.openexchange.test.common.test.pool.TestUser;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FileAccountCreationResponse;
import com.openexchange.testing.httpclient.models.FileAccountData;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.MailData;
import com.openexchange.testing.httpclient.models.MailListElement;
import com.openexchange.testing.httpclient.models.MailResponse;
import com.openexchange.testing.httpclient.models.MailsCleanUpResponse;
import com.openexchange.testing.httpclient.models.MailsResponse;
import com.openexchange.testing.httpclient.modules.FilestorageApi;
import com.openexchange.testing.httpclient.modules.MailApi;

/**
 * {@link FederatedSharingUtil}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public final class FederatedSharingUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FederatedSharingUtil.class);

    /**
     * Receives the share link from the <code>X-Open-Xchange-Share-URL</code> header
     *
     * @param apiClient The client to use
     * @param fromToMatch The sender of the mail
     * @param subjectToMatch A part of the subject the mail must have
     * @return The share link
     * @throws Exception In case of error
     */
    public static String receiveShareLink(ApiClient apiClient, String fromToMatch, String subjectToMatch) throws Exception {
        MailData mail = receiveShareMail(apiClient, fromToMatch, subjectToMatch);
        @SuppressWarnings("unchecked") Map<String, String> headers = (Map<String, String>) mail.getHeaders();
        for (Iterator<Entry<String, String>> iterator = headers.entrySet().iterator(); iterator.hasNext();) {
            Entry<String, String> entry = iterator.next();
            if ("X-Open-Xchange-Share-URL".equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        throw new AssertionError("No \"X-Open-Xchange-Share-URL\" header in mail");
    }

    /**
     * Receive the share mail from the inbox
     *
     * @param apiClient The {@link ApiClient} to use
     * @param fromToMatch The mail of the originator of the message
     * @param subjectToMatch The summary of the event
     * @return The mail as {@link MailData}
     * @throws Exception If the mail can't be found or something mismatches
     */
    public static MailData receiveShareMail(ApiClient apiClient, String fromToMatch, String subjectToMatch) throws Exception {
        LOGGER.info("Searching for mail with subject \"{}\" from {}", subjectToMatch, fromToMatch);
        for (int i = 0; i < 10; i++) {
            MailData mailData = lookupMail(apiClient, "default0%2FINBOX", fromToMatch, subjectToMatch);
            if (null != mailData) {
                return mailData;
            }
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
        }
        throw new AssertionError("No mail with \"" + subjectToMatch + "\" from " + fromToMatch + " received");
    }

    private static MailData lookupMail(ApiClient apiClient, String folder, String fromToMatch, String subjectToMatch) throws Exception {
        MailApi mailApi = new MailApi(apiClient);
        MailsResponse mailsResponse = mailApi.getAllMails(folder, "600,601,607,610", null, null, null, "610", "desc", null, null, I(10), null);
        checkResponse(mailsResponse.getError(), mailsResponse.getErrorDesc(), mailsResponse.getData());
        for (List<String> mail : mailsResponse.getData()) {
            String subject = mail.get(2);
            if (Strings.isEmpty(subject)) {
                LOGGER.info("Mail with ID {} has no subject", mail.get(0));
                continue;
            }
            if (false == subject.contains(subjectToMatch)) {
                LOGGER.info("\"{}\" doesn't contain expected subject", subject);
                continue;
            }
            MailResponse mailResponse = mailApi.getMail(mail.get(1), mail.get(0), null, null, "noimg", Boolean.FALSE, Boolean.TRUE, null, null, null, null, null, null, null);
            MailData mailData = checkResponse(mailResponse.getError(), mailsResponse.getErrorDesc(), mailResponse.getData());
            if (null == extractMatchingAddress(mailData.getFrom(), fromToMatch)) {
                LOGGER.info("Found potential matching sharing mail but expected sender {} is not in the FROM header {}", fromToMatch, mailData.getFrom());
                continue;
            }
            deleteMail(mailApi, mailData);
            return mailData;
        }
        return null;
    }

    private static List<String> extractMatchingAddress(List<List<String>> addresses, String email) {
        if (null != addresses) {
            for (List<String> address : addresses) {
                assertEquals(2, address.size());
                if (null != address.get(1) && address.get(1).contains(email)) {
                    return address;
                }
            }
        }
        return null;
    }

    private static void deleteMail(MailApi mailApi, MailData data) throws ApiException {
        if (null == mailApi || null == data) {
            return;
        }
        MailListElement elm = new MailListElement();
        elm.setId(data.getId());
        elm.setFolder(data.getFolderId());
        MailsCleanUpResponse deleteResponse = mailApi.deleteMails(Collections.singletonList(elm), null, Boolean.TRUE, Boolean.FALSE);
        List<String> deletedMailIds = checkResponse(deleteResponse.getError(), deleteResponse.getErrorDesc(), deleteResponse.getData());
        assertThat(deletedMailIds, is(empty()));
    }

    /**
     * Checks if a response doesn't contain any errors
     * 
     * @param <T> The type of the data to return
     * @param error The error element of the response
     * @param errorDesc The error description element of the response
     * @param data The data element of the response
     * @return The data
     */
    public static <T> T checkResponse(String error, String errorDesc, T data) {
        assertNull(errorDesc, error);
        assertNotNull(data);
        return data;
    }

    /**
     * Clears the account error of the given file account
     *
     * @param filestorageApi The API to use
     * @param data The account data
     * @throws Exception In case operation fails
     */
    public static void clearAccountError(FilestorageApi filestorageApi, FileAccountData data) throws Exception {
        FileAccountData fileAccountData = new FileAccountData();
        fileAccountData.setId(data.getId());
        fileAccountData.setFilestorageService(data.getFilestorageService());
        fileAccountData.setDisplayName(data.getDisplayName());
        fileAccountData.setConfiguration(new JSONObject());
        FileAccountCreationResponse resp = filestorageApi.updateFileAccount(fileAccountData);
        assertThat("Password still wrong", resp.getError(), notNullValue());
    }

    /**
     * Clears the INBOX by removing all mail in it
     *
     * @param apiClient The API client
     * @throws ApiException In case clearing fails
     */
    public static void cleanInbox(ApiClient apiClient) throws ApiException {
        MailApi mailApi = new MailApi(apiClient);
        mailApi.clearMailFolders(Collections.singletonList("default0/INBOX"), L(System.currentTimeMillis()));
    }

    /**
     * 
     * {@link PermissionLevel} - Permissions levels of a (guest-) user for a specific folder
     * <p>
     * Named after UI settings
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v7.10.5
     */
    public static enum PermissionLevel {

        /** Read only */
        VIEWER(I(257)),
        /** Read and write permissions */
        REVIEWER(I(33025)),
        /** Read, write and delete permissions */
        AUTHOR(I(4227332)),
        /** Administrator permissions */
        ADMINISTRATOR(I(272662788)),
        /** Owner of the folder, can not be assigned to a (guest-) user */
        OWNER(I(403710016));

        private final Integer bits;

        PermissionLevel(Integer bits) {
            this.bits = bits;
        }

        /**
         * Get the bits fitting to the permission level
         * 
         * @return The bits
         */
        public Integer getBits() {
            return bits;
        }
    }

    /**
     * Prepares a guest permission with viewer permissions
     *
     * @param testUser The guest to prepare
     * @return Folder permissions containing the guest
     */
    public static FolderPermission prepareGuest(TestUser testUser) {
        return prepareGuest(testUser, PermissionLevel.VIEWER);
    }

    /**
     * Prepares a guest permission
     *
     * @param testUser The guest to prepare
     * @param level The level of permissions for the guest
     * @return Folder permissions containing the guest
     */
    public static FolderPermission prepareGuest(TestUser testUser, PermissionLevel level) {
        FolderPermission guest = new FolderPermission();
        guest.setBits(level.getBits());
        guest.setEmailAddress(testUser.getLogin());
        guest.setType("guest");
        return guest;
    }

    /**
     * Prepares an internal user permission with viewer permissions
     *
     * @param testUser The guest to prepare
     * @param userId The user to add
     * @return Folder permissions containing the internal user
     */
    public static FolderPermission prepareUser(TestUser testUser, Integer userId) {
        return prepareUser(testUser, userId, PermissionLevel.VIEWER);
    }

    /**
     * Prepares an internal user permission
     *
     * @param testUser The guest to prepare
     * @param userId The user to add
     * @param level The level of permissions for the user
     * @return Folder permissions containing the internal user
     */
    public static FolderPermission prepareUser(TestUser testUser, Integer userId, PermissionLevel level) {
        FolderPermission guest = new FolderPermission();
        guest.setBits(level.getBits());
        guest.setEmailAddress(testUser.getLogin());
        guest.setEntity(userId);
        guest.setType("user");
        return guest;
    }

}
