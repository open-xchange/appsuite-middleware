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

package com.openexchange.ajax.infostore.thirdparty.webdav;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.ajax.infostore.thirdparty.AbstractFileStorageAccountTest;
import com.openexchange.java.Strings;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FileAccountData;
import com.openexchange.testing.httpclient.models.FoldersCleanUpResponse;

/**
 * {@link WebDAVTest} - Tests the integration of an external WebDAV server as FileStorageAccount
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public class WebDAVTest extends AbstractFileStorageAccountTest {

    /**
     * The constant for the WebDAV FileStorageService
     */
    private static final String WEBDAV_FILE_STORAGE_SERVICE = "webdav";

    /**
     * The constant for the display name of the FileStorageService
     */
    private static final String WEBDAV_FILE_STORAGE_SERVICE_DISPLAY_NAME = "WebDAV test storage";

    /**
     * The base WebDAV URL to use
     */
    private static final String WEB_DAV_URL = "http://localhost:8009/servlet/webdav.infostore";

    private FileAccountData testFileAccount;

    public static class WebDAVFileAccountConfiguration {

        private final String url;
        private final String login;
        private final String password;

        /**
         * Initializes a new {@link WebDAVTest.WebDAVFileAccountConfiguration}.
         *
         * @param url The WebDAV root URL to use
         * @param login The login for authentication against the WebDAV system
         * @param password The password for authentication against the WebDAV system
         */
        public WebDAVFileAccountConfiguration(String url, String login, String password) {
            this.url = url;
            this.login = login;
            this.password = password;
        }

        /**
         * The URL of the WebDAV server
         *
         * @return The URL
         */
        public String getUrl() {
            return url;
        }

        /**
         * The login used to access the WebDAV server
         *
         * @return The login
         */
        public String getLogin() {
            return login;
        }

        /**
         * The password used to access the WebDAV server
         *
         * @return
         */
        public String getPassword() {
            return password;
        }
    }

    /**
     *
     * Gets the WebDAV folder ID for a given path
     *
     * @param path The path
     * @return The folderID
     * @throws ApiException
     * @throws UnsupportedEncodingException
     */
    private String getWebDAVFolderId(String path) throws ApiException {
        final String infostoreRootFolderName = getPrivateInfostoreFolderName(getApiClient()).replace(" ", "%20");

        //<service>://<id>/<base64path>
        //for example: webdav://18/VXNlcnN0b3JlL2FudG9uJTIwYW50b24v
        String rootFolder = String.format("Userstore/%s/", infostoreRootFolderName);
        if (Strings.isNotEmpty(path)) {
            rootFolder += path;
        }
        rootFolder = Base64.getUrlEncoder().withoutPadding().encodeToString(rootFolder.getBytes(StandardCharsets.UTF_8));
        return String.format("%s://%s/%s", testFileAccount.getFilestorageService(), testFileAccount.getId(), rootFolder);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        //Register a new WebDAV FileAccount
        WebDAVFileAccountConfiguration configuration = new WebDAVFileAccountConfiguration(WEB_DAV_URL, testUser.getUser() + "@" + testUser.getContext(), testUser.getPassword());
        testFileAccount = createAccount(WEBDAV_FILE_STORAGE_SERVICE, WEBDAV_FILE_STORAGE_SERVICE_DISPLAY_NAME, configuration);
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        HashMap<String, String> configuration = new HashMap<String, String>();
        configuration.put("com.openexchange.capability.filestorage_webdav", Boolean.TRUE.toString());
        configuration.put("com.openexchange.file.storage.webdav.blacklistedHosts", "");
        return configuration;
    }

    /**
     * Deletes a folder
     *
     * @param folderId The folder to deelte
     * @param hardDelete true to perform a hard-delete, false to put the folder into the trash-bin
     * @return A list of folder IDs which could <b>NOT</b> be removed due conflicts or errors
     * @throws ApiException
     */
    protected List<String> deleteFolder(String folderId, boolean hardDelete) throws ApiException {
        final boolean failOnError = true;
        final boolean extendedResponse = false;
        FoldersCleanUpResponse response = foldersApi.deleteFolders(getSessionId(), Collections.singletonList(folderId), null, null, null, B(hardDelete), B(failOnError), B(extendedResponse), null, Boolean.FALSE);
        return checkResponse(response.getError(), response.getErrorDesc(), response.getData());
    }

    @Override
    protected String getReloadables() {
        return "CapabilityReloadable";
    }

    /**
     * Gets the WebDAV root folder
     *
     * @return The root folder
     * @throws ApiException
     * @throws UnsupportedEncodingException
     */
    @Override
    protected String getRootFolderId() throws ApiException, UnsupportedEncodingException {
        return getWebDAVFolderId(null);
    }

    @Override
    protected FileAccountData getAccountData() throws Exception {
        return testFileAccount;
    }

    @Override
    protected Object getWrongFileStorageConfiguration() {
        final String incorrectURL = "http://notExisting.example.org/servlet/webdav.infostore";
        return new WebDAVFileAccountConfiguration(incorrectURL, testUser.getUser(), testUser.getPassword());
    }

    @Override
    protected TestFile createTestFile() throws Exception {
        String folderId = getRootFolderId();
        String fileName = getRandomFileName();
        String newFileId = uploadInfoItem(getRootFolderId(), getRandomFileName(), testContent, "application/text");
        return new TestFile(folderId, newFileId, fileName);
    }
}
