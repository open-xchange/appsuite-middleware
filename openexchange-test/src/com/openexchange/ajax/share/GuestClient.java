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

package com.openexchange.ajax.share;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.junit.Assert;
import com.openexchange.ajax.appointment.action.AppointmentInsertResponse;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.folder.actions.GetRequest;
import com.openexchange.ajax.folder.actions.GetResponse;
import com.openexchange.ajax.folder.actions.OCLGuestPermission;
import com.openexchange.ajax.folder.actions.VisibleFoldersRequest;
import com.openexchange.ajax.folder.actions.VisibleFoldersResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.AbstractColumnsResponse;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.infostore.actions.AllInfostoreRequest;
import com.openexchange.ajax.infostore.actions.DeleteInfostoreRequest;
import com.openexchange.ajax.infostore.actions.DeleteInfostoreResponse;
import com.openexchange.ajax.infostore.actions.GetDocumentRequest;
import com.openexchange.ajax.infostore.actions.GetDocumentResponse;
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetInfostoreResponse;
import com.openexchange.ajax.infostore.actions.NewInfostoreRequest;
import com.openexchange.ajax.infostore.actions.NewInfostoreResponse;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreRequest;
import com.openexchange.ajax.infostore.actions.UpdateInfostoreResponse;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.ajax.session.actions.LoginRequest.GuestCredentials;
import com.openexchange.ajax.session.actions.LoginResponse;
import com.openexchange.ajax.share.actions.ResolveShareRequest;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.util.TimeZones;
import com.openexchange.java.util.UUIDs;
import com.openexchange.share.recipient.ShareRecipient;

/**
 * {@link GuestClient}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GuestClient extends AJAXClient {

    private final ResolveShareResponse shareResponse;
    private final LoginResponse loginResponse;
    private final String module;
    private final String item;
    private final String folder;

    /**
     * Initializes a new {@link GuestClient}.
     *
     * @param url The share URL to access
     * @param recipient The recipient
     */
    public GuestClient(String url, ShareRecipient recipient) throws Exception {
        this(url, recipient, false);
    }

    /**
     * Initializes a new {@link GuestClient}.
     *
     * @param url The share URL to access
     * @param recipient The recipient
     * @param failOnNonRedirect <code>true</code> to fail if the share resolve request is not being redirected, <code>false</code>, otherwise
     */
    public GuestClient(String url, ShareRecipient recipient, boolean failOnNonRedirect) throws Exception {
        this(new AJAXSession(), url, ShareTest.getUsername(recipient), ShareTest.getPassword(recipient), failOnNonRedirect);
    }

    /**
     * Initializes a new {@link GuestClient}.
     *
     * @param ajaxSession The underlying ajax session to use
     * @param url The share URL to access
     * @param recipient The recipient
     * @param failOnNonRedirect <code>true</code> to fail if the share resolve request is not being redirected, <code>false</code>, otherwise
     */
    public GuestClient(AJAXSession ajaxSession, String url, ShareRecipient recipient, boolean failOnNonRedirect) throws Exception {
        this(ajaxSession, url, recipient, failOnNonRedirect, true);
    }

    /**
     * Initializes a new {@link GuestClient}.
     *
     * @param ajaxSession The underlying ajax session to use
     * @param url The share URL to access
     * @param recipient The recipient
     * @param failOnNonRedirect <code>true</code> to fail if the share resolve request is not being redirected, <code>false</code>, otherwise
     * @param mustLogout <code>true</code> to enforce logging out on finalize, <code>false</code>, otherwise
     */
    public GuestClient(AJAXSession ajaxSession, String url, ShareRecipient recipient, boolean failOnNonRedirect, boolean mustLogout) throws Exception {
        this(ajaxSession, url, ShareTest.getUsername(recipient), ShareTest.getPassword(recipient), failOnNonRedirect, mustLogout);
    }

    /**
     * Initializes a new {@link GuestClient}.
     *
     * @param url The share URL to access
     * @param username The username to use for authentication, or <code>null</code> if not needed
     * @param password The password to use for authentication, or <code>null</code> if not needed
     */
    public GuestClient(String url, String username, String password) throws Exception {
        this(url, username, password, false);
    }

    /**
     * Initializes a new {@link GuestClient}.
     *
     * @param url The share URL to access
     * @param username The username to use for authentication, or <code>null</code> if not needed
     * @param password The password to use for authentication, or <code>null</code> if not needed
     * @param failOnNonRedirect <code>true</code> to fail if the share resolve request is not being redirected, <code>false</code>, otherwise
     * @throws Exception
     */
    public GuestClient(String url, String username, String password, boolean failOnNonRedirect) throws Exception {
        this(new AJAXSession(), url, username, password, failOnNonRedirect);
    }

    /**
     * Initializes a new {@link GuestClient}.
     *
     * @param ajaxSession The underlying ajax session to use
     * @param url The share URL to access
     * @param username The username to use for authentication, or <code>null</code> if not needed
     * @param password The password to use for authentication, or <code>null</code> if not needed
     * @param failOnNonRedirect <code>true</code> to fail if the share resolve request is not being redirected, <code>false</code>, otherwise
     * @param mustLogout <code>true</code> to enforce logging out on finalize, <code>false</code>, otherwise
     * @throws Exception
     */
    public GuestClient(AJAXSession ajaxSession, String url, String username, String password, boolean failOnNonRedirect) throws Exception {
        this(ajaxSession, url, username, password, failOnNonRedirect, true);
    }

    /**
     * Initializes a new {@link GuestClient}.
     *
     * @param ajaxSession The underlying ajax session to use
     * @param url The share URL to access
     * @param username The username to use for authentication, or <code>null</code> if not needed
     * @param password The password to use for authentication, or <code>null</code> if not needed
     * @param failOnNonRedirect <code>true</code> to fail if the share resolve request is not being redirected, <code>false</code>, otherwise
     * @param mustLogout <code>true</code> to enforce logging out on finalize, <code>false</code>, otherwise
     * @throws Exception
     */
    public GuestClient(AJAXSession ajaxSession, String url, String username, String password, boolean failOnNonRedirect, boolean mustLogout) throws Exception {
        this(ajaxSession, url, username, password, null, failOnNonRedirect, mustLogout);
    }

    public GuestClient(AJAXSession ajaxSession, String url, String username, String password, String client, boolean failOnNonRedirect, boolean mustLogout) throws Exception {
        this(new ClientConfig(url).setAJAXSession(ajaxSession).setCredentials(username, password).setClient(client).setFailOnNonRedirect(failOnNonRedirect).setMustLogout(mustLogout));
    }

    public GuestClient(ClientConfig config) throws Exception {
        super(getOrCreateSession(config), config.mustLogout);
        prepareClient(getHttpClient(), config.username, config.password);
        setHostname(new URI(config.url).getHost());
        setProtocol(new URI(config.url).getScheme());
        shareResponse = Executor.execute(this, new ResolveShareRequest(config.url, config.failOnNonRedirect), getProtocol(), getHostname());
        if (null != shareResponse.getLoginType()) {
            loginResponse = login(shareResponse, config);
            getSession().setId(loginResponse.getSessionId());
            if (false == loginResponse.hasError()) {
                JSONObject data = (JSONObject) loginResponse.getData();
                module = data.has("module") ? data.getString("module") : null;
                folder = data.has("folder") ? data.getString("folder") : null;
                item = data.has("item") ? data.getString("item") : null;
            } else {
                module = null;
                folder = null;
                item = null;
            }
        } else {
            loginResponse = null;
            getSession().setId(shareResponse.getSessionID());
            module = shareResponse.getModule();
            folder = shareResponse.getFolder();
            item = shareResponse.getItem();
            shareResponse.getSessionID();
        }
    }

    private static AJAXSession getOrCreateSession(ClientConfig config) {
        if (config.ajaxSession == null) {
            return new AJAXSession();
        }

        return config.ajaxSession;
    }

    public static final class ClientConfig {

        private final String url;

        private String username;

        private String password;

        private boolean failOnNonRedirect;

        private boolean mustLogout;

        private String client;

        private AJAXSession ajaxSession;

        public ClientConfig(String url) {
            super();
            this.url = url;
        }

        public ClientConfig setCredentials(ShareRecipient recipient) {
            this.username = ShareTest.getUsername(recipient);
            this.password = ShareTest.getPassword(recipient);
            return this;
        }

        public ClientConfig setCredentials(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        public ClientConfig setUsername(String username) {
            this.username = username;
            return this;
        }

        public ClientConfig setPassword(String password) {
            this.password = password;
            return this;
        }

        public ClientConfig setFailOnNonRedirect(boolean failOnNonRedirect) {
            this.failOnNonRedirect = failOnNonRedirect;
            return this;
        }

        public ClientConfig setMustLogout(boolean mustLogout) {
            this.mustLogout = mustLogout;
            return this;
        }

        public ClientConfig setAJAXSession(AJAXSession ajaxSession) {
            this.ajaxSession = ajaxSession;
            return this;
        }

        public ClientConfig setClient(String client) {
            this.client = client;
            return this;
        }

    }

    private LoginResponse login(ResolveShareResponse shareResponse, ClientConfig config) throws Exception {
        LoginRequest loginRequest = null;
        if ("guest".equals(shareResponse.getLoginType()) || "guest_password".equals(shareResponse.getLoginType())) {
            GuestCredentials credentials = new GuestCredentials(config.username, config.password);
            loginRequest = LoginRequest.createGuestLoginRequest(shareResponse.getShare(), shareResponse.getTarget(), credentials, config.client, false);
        } else if ("anonymous_password".equals(shareResponse.getLoginType())) {
            loginRequest = LoginRequest.createAnonymousLoginRequest(shareResponse.getShare(), shareResponse.getTarget(), config.password, false);
        } else {
            Assert.fail("unknown login type: " + shareResponse.getLoginType());
        }
        return Executor.execute(this, loginRequest, getProtocol(), getHostname());
    }

    private static void prepareClient(DefaultHttpClient httpClient, String username, String password) {
        httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        if (null != password) {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(null != username ? username : "guest", password);
            credentialsProvider.setCredentials(org.apache.http.auth.AuthScope.ANY, credentials);
            httpClient.setCredentialsProvider(credentialsProvider);
        }
    }

    public ResolveShareResponse getShareResolveResponse() {
        return shareResponse;
    }

    public LoginResponse getLoginResponse() {
        return loginResponse;
    }

    public String getModule() {
        return module;
    }

    public int getModuleID() {
        return Module.getModuleInteger(getModule());
    }

    public String getFolder() {
        return folder;
    }

    public int getIntFolder() {
        return Integer.parseInt(getFolder());
    }

    public String getItem() {
        return item;
    }

    /**
     * Checks that a share is accessible for the guest according to the granted permissions.
     *
     * @param permissions The guest permissions
     * @throws Exception
     */
    public void checkShareAccessible(FileStorageGuestObjectPermission permissions) throws Exception {
        checkFileAccessible(getFolder(), getItem(), permissions);
    }

    /**
     * Checks that a share is accessible for the guest according to the granted permissions.
     *
     * @param permissions The guest permissions
     * @param expectedContents The expected contents of the file
     * @throws Exception
     */
    public void checkShareAccessible(FileStorageGuestObjectPermission permissions, byte[] expectedContents) throws Exception {
        checkFileAccessible(getFolder(), getItem(), permissions, expectedContents);
    }

    /**
     * Checks that a share is accessible for the guest according to the granted permissions.
     *
     * @param permissions The guest permissions
     * @throws Exception
     */
    public void checkShareAccessible(OCLGuestPermission permissions) throws Exception {
        checkFolderAccessible(getFolder(), permissions);
    }

    /**
     * Checks that a folder is accessible for the guest according to the granted permissions.
     *
     * @param folderID The identifier of the folder to check
     * @param permissions The guest permissions in that folder
     * @throws Exception
     */
    public void checkFolderAccessible(String folderID, OCLGuestPermission permissions) throws Exception {
        /*
         * get folder
         */
        GetResponse getResponse = execute(new GetRequest(EnumAPI.OX_NEW, Integer.valueOf(folderID)));
        FolderObject folder = getResponse.getFolder();
        folder.setLastModified(getResponse.getTimestamp());
        /*
         * check item creation
         */
        String id = createItem(folder, false == permissions.canCreateObjects());
        if (null != id) {
            /*
             * check item retrieval
             */
            getItem(folder, id, false == permissions.canReadOwnObjects());
            /*
             * check item deletion
             */
            deleteItem(folder, id, false == permissions.canDeleteAllObjects());
        }
        /*
         * check item listing
         */
        getAll(folder, false == permissions.canReadOwnObjects());
    }

    /**
     * Checks that a file is accessible for the guest according to the granted permissions.
     *
     * @param folderID The folder identifier of the file to check
     * @param fileID The identifier of the file to check
     * @param permissions The guest permissions for that file
     * @throws Exception
     */
    public void checkFileAccessible(String folderID, String fileID, FileStorageGuestObjectPermission permissions) throws Exception {
        checkFileAccessible(folderID, fileID, permissions, null);
    }

    /**
     * Checks that a file is accessible for the guest according to the granted permissions.
     *
     * @param folderID The folder identifier of the file to check
     * @param fileID The identifier of the file to check
     * @param permissions The guest permissions for that file
     * @param expectedContents The expected contents of the file
     * @throws Exception
     */
    public void checkFileAccessible(String folderID, String fileID, FileStorageGuestObjectPermission permissions, byte[] expectedContents) throws Exception {
        /*
         * check item retrieval
         */
        GetInfostoreRequest getInfostoreRequest = new GetInfostoreRequest(fileID);
        getInfostoreRequest.setFailOnError(permissions.canRead());
        GetInfostoreResponse getInfostoreResponse = execute(getInfostoreRequest);
        checkResponse(getInfostoreResponse, false == permissions.canRead());
        DefaultFile file = new DefaultFile(getInfostoreResponse.getDocumentMetadata());
        if (null != file.getFileName() && 0 < file.getFileSize()) {
            GetDocumentRequest getDocumentRequest = new GetDocumentRequest(folderID, fileID);
            getInfostoreRequest.setFailOnError(permissions.canRead());
            GetDocumentResponse getDocumentResponse = execute(getDocumentRequest);
            checkResponse(getDocumentResponse, false == permissions.canRead());
            byte[] contents = getDocumentResponse.getContentAsByteArray();
            if (false == permissions.canRead()) {
                Assert.assertNull("Contents wrong", contents);
            } else {
                if (null == expectedContents) {
                    Assert.assertNotNull("Contents wrong", contents);
                } else {
                    Assert.assertArrayEquals("Contents wrong", expectedContents, contents);
                }
            }
        }

        if (permissions.canRead()) {
            /*
             * check item update
             */
            file.setFileName(file.getFileName() + "_edit");
            UpdateInfostoreRequest updateInfostoreRequest = new UpdateInfostoreRequest(file, new Field[] { Field.FILENAME }, file.getLastModified());
            updateInfostoreRequest.setFailOnError(permissions.canWrite());
            UpdateInfostoreResponse updateInfostoreResponse = execute(updateInfostoreRequest);
            checkResponse(updateInfostoreResponse, false == permissions.canWrite());
            file.setLastModified(updateInfostoreResponse.getTimestamp());
            /*
             * check item delete
             */
            //TODO: throws "not exist" in folder 10
//            DeleteInfostoreRequest deleteInfostoreRequest = new DeleteInfostoreRequest(fileID, new FileID(fileID).getFolderId(), file.getLastModified());
//            deleteInfostoreRequest.setFailOnError(permissions.canDelete());
//            DeleteInfostoreResponse deleteInfostoreResponse = execute(deleteInfostoreRequest);
//            checkResponse(deleteInfostoreResponse, false == permissions.canDelete());
        }
    }

    /**
     * Checks that a file is not accessible for the guest.
     *
     * @param fileID The identifier of the file to check
     */
    public void checkFileNotAccessible(String fileID) throws Exception {
        /*
         * check item retrieval
         */
        GetInfostoreRequest getInfostoreRequest = new GetInfostoreRequest(fileID);
        getInfostoreRequest.setFailOnError(false);
        GetInfostoreResponse getInfostoreResponse = execute(getInfostoreRequest);
        checkResponse(getInfostoreResponse, true);
    }

    /**
     * Checks that a file is accessible for the guest according to the granted permissions.
     * This method checks only for object permissions. If you shared the parent folder you need
     * to check the files accessibility otherwise.
     *
     * @param fileID The identifier of the file to check
     * @param permissions The guest permissions for that file
     * @throws Exception
     */
    public void checkFileAccessible(String id, OCLGuestPermission permissions) throws Exception {
        /*
         * get file
         */
        FileID fileID = new FileID(id);
        fileID.setFolderId(Integer.toString(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID));
        GetInfostoreRequest getFileRequest = new GetInfostoreRequest(fileID.toUniqueID());
        getFileRequest.setFailOnError(true);
        GetInfostoreResponse getFileResponse = execute(getFileRequest);
        File file = getFileResponse.getDocumentMetadata();
        List<FileStorageObjectPermission> objectPermissions = file.getObjectPermissions();
        if (objectPermissions == null) {
            Assert.fail("File contains no object permission for entity " + permissions.getEntity());
        }

        FileStorageObjectPermission permissionForEntity = null;
        for (FileStorageObjectPermission p : objectPermissions) {
            if (p.getEntity() == permissions.getEntity() && p.isGroup() == permissions.isGroupPermission()) {
                permissionForEntity = p;
                break;
            }
        }

        int expected = getObjectPermissionBits(permissions.getPermissionBits());
        if (permissionForEntity == null) {
            Assert.fail("File contains no object permission for entity " + permissions.getEntity());
        }

        Assert.assertEquals("Wrong permission found", expected, permissionForEntity.getPermissions());
    }

    /**
     * Takes a folder permission bit mask and deduces the according object permissions.
     *
     * @param folderPermissionBits The folder permission bit mask
     * @return The object permission bits
     */
    protected static int getObjectPermissionBits(int folderPermissionBits) {
        int objectBits = ObjectPermission.NONE;
        int[] permissionBits = Permissions.parsePermissionBits(folderPermissionBits);
        int rp = permissionBits[1];
        int wp = permissionBits[2];
        int dp = permissionBits[3];
        if (dp >= Permission.DELETE_ALL_OBJECTS) {
            objectBits = ObjectPermission.DELETE;
        } else if (wp >= Permission.WRITE_ALL_OBJECTS) {
            objectBits = ObjectPermission.WRITE;
        } else if (rp >= Permission.READ_ALL_OBJECTS) {
            objectBits = ObjectPermission.READ;
        }

        return objectBits;
    }

    /**
     * Checks that a folder is not accessible for the guest.
     *
     * @param folderID The identifier of the folder to check
     * @throws Exception
     */
    public void checkFolderNotAccessible(String folderID) throws Exception {
        GetResponse getResponse = execute(new GetRequest(EnumAPI.OX_NEW, Integer.valueOf(folderID), false));
        Assert.assertTrue("No errors in response", getResponse.hasError());
        Assert.assertNull("Folder in response", getResponse.getFolder());
    }

    /**
     * Checks that a module is available.
     *
     * @param moduleID The identifier of the module to be available
     */
    public void checkModuleAvailable(int moduleID) throws Exception {
        com.openexchange.ajax.config.actions.GetResponse getResponse =
            execute(new com.openexchange.ajax.config.actions.GetRequest(Tree.AvailableModules));
        String module = getContentType(moduleID);
        Object[] array = getResponse.getArray();
        for (Object object : array) {
            if (module.equals(object)) {
                return;
            }
        }
        Assert.fail("Module " + getContentType(moduleID) + " not found");
    }

    /**
     * Checks that a module is not available.
     *
     * @param moduleID The identifier of the module to be not available
     */
    public void checkModuleNotAvailable(int moduleID) throws Exception {
        com.openexchange.ajax.config.actions.GetResponse getResponse =
            execute(new com.openexchange.ajax.config.actions.GetRequest(Tree.AvailableModules));
        String module = getContentType(moduleID);
        Object[] array = getResponse.getArray();
        for (Object object : array) {
            Assert.assertNotEquals("Module " + getContentType(moduleID) + " found", object, module);
        }
    }

    /**
     * Checks that the share's module is available.
     */
    public void checkShareModuleAvailable() throws Exception {
        checkModuleAvailable(getModuleID());
    }

    /**
     * Checks that the share's module is available, as well as all others modules are not.
     */
    public void checkShareModuleAvailableExclusively() throws Exception {
        com.openexchange.ajax.config.actions.GetResponse getResponse =
            execute(new com.openexchange.ajax.config.actions.GetRequest(Tree.AvailableModules));
        Object[] array = getResponse.getArray();
        for (int moduleID : new int[] { FolderObject.CALENDAR, FolderObject.CONTACT, FolderObject.INFOSTORE, FolderObject.TASK }) {
            String module = getContentType(moduleID);
            boolean found = false;
            for (Object object : array) {
                if (module.equals(object)) {
                    found = true;
                    break;
                }
            }
            if (getModuleID() == moduleID) {
                Assert.assertTrue("Module " + module + " not found", found);
            } else {
                Assert.assertFalse("Module " + module + " was found", found);
            }
        }
    }

    private String getContentType(int module) {
        switch (module) {
        case FolderObject.CONTACT:
            return "contacts";
        case FolderObject.INFOSTORE:
            return "infostore";
        case FolderObject.TASK:
            return "tasks";
        case FolderObject.CALENDAR:
            return "calendar";
        default:
            Assert.fail("no content type for " + getModule() + "");
            return null;
        }
    }

    /**
     * Checks that the guest client's session is "alive" by executing a "get user" request, followed by a "visible folders" request in
     * case the module is different from infostore.
     *
     * @param expectToFail <code>true</code> if the requests are expected to fail, <code>false</code>, otherwise
     */
    public void checkSessionAlive(boolean expectToFail) throws Exception {
        com.openexchange.ajax.user.actions.GetResponse getResponse = execute(
            new com.openexchange.ajax.user.actions.GetRequest(TimeZones.UTC, false == expectToFail));
        checkResponse(getResponse, expectToFail);
        if (FolderObject.INFOSTORE != getModuleID()) {
            String contentType = getContentType(getModuleID());
            VisibleFoldersResponse response = execute(new VisibleFoldersRequest(
                EnumAPI.OX_NEW, contentType, FolderObject.ALL_COLUMNS, false == expectToFail));
            checkResponse(response, expectToFail);
        }
    }

    private static void checkResponse(AbstractAJAXResponse response, boolean expectToFail) {
        Assert.assertNotNull("No response", response);
        if (expectToFail) {
            if (false == response.hasError()) {
                System.out.println( "+++");
            }

            Assert.assertTrue("No errors in response", response.hasError());
        } else {
            Assert.assertFalse("Errors in response", response.hasError());
        }
    }

    private void deleteItem(FolderObject folder, String id, boolean expectToFail) throws Exception {
        int folderID = folder.getObjectID();
        Date timestamp = getFutureTimestamp();
        boolean failOnError = false == expectToFail;
        switch (folder.getModule()) {
        case FolderObject.CONTACT:
            CommonDeleteResponse deleteContactResponse = execute(
                new com.openexchange.ajax.contact.action.DeleteRequest(folderID, Integer.parseInt(id), timestamp, failOnError));
            checkResponse(deleteContactResponse, expectToFail);
            break;
        case FolderObject.INFOSTORE:
            DeleteInfostoreRequest deleteInfostoreRequest = new DeleteInfostoreRequest(id, String.valueOf(folderID), timestamp);
            deleteInfostoreRequest.setFailOnError(failOnError);
            DeleteInfostoreResponse deleteInfostoreResponse = execute(deleteInfostoreRequest);
            checkResponse(deleteInfostoreResponse, expectToFail);
            break;
        case FolderObject.TASK:
            CommonDeleteResponse deleteTaskResponse = execute(
                new com.openexchange.ajax.task.actions.DeleteRequest(folderID, Integer.parseInt(id), timestamp, failOnError));
            checkResponse(deleteTaskResponse, expectToFail);
            break;
        case FolderObject.CALENDAR:
            CommonDeleteResponse deleteAppointmentResponse = execute(
                new com.openexchange.ajax.appointment.action.DeleteRequest(Integer.parseInt(id), folderID, timestamp, failOnError));
            checkResponse(deleteAppointmentResponse, expectToFail);
            break;
        default:
            Assert.fail("no delete item request for " + folder.getModule() + " implemented");
            break;
        }
    }

    public Object getItem(FolderObject folder, String id, boolean expectToFail) throws Exception {
        int folderID = folder.getObjectID();
        boolean failOnError = false == expectToFail;
        TimeZone timeZone = TimeZones.UTC;
        switch (folder.getModule()) {
        case FolderObject.CONTACT:
            com.openexchange.ajax.contact.action.GetResponse contactGetResponse = execute(
                new com.openexchange.ajax.contact.action.GetRequest(folderID, Integer.parseInt(id), timeZone, failOnError));
            checkResponse(contactGetResponse, expectToFail);
            return expectToFail ? null : contactGetResponse.getContact();
        case FolderObject.INFOSTORE:
            GetInfostoreRequest getInfostoreRequest = new GetInfostoreRequest(id);
            getInfostoreRequest.setFailOnError(false == expectToFail);
            GetInfostoreResponse getInfostoreResponse = execute(getInfostoreRequest);
            checkResponse(getInfostoreResponse, expectToFail);
            return expectToFail ? null : getInfostoreResponse.getDocumentMetadata();
        case FolderObject.TASK:
            com.openexchange.ajax.task.actions.GetResponse getTaskResponse = execute(
                new com.openexchange.ajax.task.actions.GetRequest(folderID, Integer.parseInt(id), failOnError));
            checkResponse(getTaskResponse, expectToFail);
            return expectToFail ? null : getTaskResponse.getTask(timeZone);
        case FolderObject.CALENDAR:
            com.openexchange.ajax.appointment.action.GetResponse getAppointmentResponse = execute(
                new com.openexchange.ajax.appointment.action.GetRequest(folderID, Integer.parseInt(id), failOnError));
            checkResponse(getAppointmentResponse, expectToFail);
            return expectToFail ? null : getAppointmentResponse.getAppointment(timeZone);
        default:
            Assert.fail("no get item request for " + folder.getModule() + " implemented");
            return null;
        }
    }

    private String createItem(FolderObject folder, boolean expectToFail) throws Exception {
        boolean failOnError = false == expectToFail;
        int folderID = folder.getObjectID();
        TimeZone timeZone = TimeZones.UTC;
        switch (folder.getModule()) {
        case FolderObject.CONTACT:
            Contact contact = new Contact();
            contact.setParentFolderID(folderID);
            contact.setDisplayName(UUIDs.getUnformattedString(UUID.randomUUID()));
            InsertResponse insertContactResponse = execute(
                new com.openexchange.ajax.contact.action.InsertRequest(contact, failOnError));
            checkResponse(insertContactResponse, expectToFail);
            return expectToFail ? null : String.valueOf(insertContactResponse.getId());
        case FolderObject.INFOSTORE:
            byte[] data = UUIDs.toByteArray(UUID.randomUUID());
            File metadata = new DefaultFile();
            metadata.setFolderId(String.valueOf(folderID));
            metadata.setFileName(UUIDs.getUnformattedString(UUID.randomUUID()) + ".test");
            NewInfostoreRequest newRequest = new NewInfostoreRequest(metadata, new ByteArrayInputStream(data));
            newRequest.setFailOnError(false == expectToFail);
            NewInfostoreResponse newResponse = execute(newRequest);
            checkResponse(newResponse, expectToFail);
            return expectToFail ? null : String.valueOf(newResponse.getID());
        case FolderObject.TASK:
            Task task = new Task();
            task.setParentFolderID(folderID);
            task.setTitle(UUIDs.getUnformattedString(UUID.randomUUID()));
            com.openexchange.ajax.task.actions.InsertResponse insertTaskResponse = execute(
                new com.openexchange.ajax.task.actions.InsertRequest(task, timeZone, failOnError));
            checkResponse(insertTaskResponse, expectToFail);
            return expectToFail ? null : String.valueOf(insertTaskResponse.getId());
        case FolderObject.CALENDAR:
            Appointment appointment = new Appointment();
            appointment.setParentFolderID(folderID);
            appointment.setTitle(UUIDs.getUnformattedString(UUID.randomUUID()));
            appointment.setStartDate(new Date());
            appointment.setEndDate(new Date(appointment.getStartDate().getTime() + 60 * 1000 * 60));
            appointment.setIgnoreConflicts(true);
            AppointmentInsertResponse insertAppointmentResponse = execute(
                new com.openexchange.ajax.appointment.action.InsertRequest(appointment, timeZone, failOnError));
            checkResponse(insertAppointmentResponse, expectToFail);
            return expectToFail ? null : String.valueOf(insertAppointmentResponse.getId());
        default:
            Assert.fail("no create item request for " + folder.getModule() + " implemented");
            return null;
        }
    }

    private AbstractColumnsResponse getAll(FolderObject folder, boolean expectToFail) throws Exception {
        int folderID = folder.getObjectID();
        switch (folder.getModule()) {
        case FolderObject.CONTACT:
            CommonAllResponse allContactResponse = execute(
                new com.openexchange.ajax.contact.action.AllRequest(folderID, Contact.ALL_COLUMNS));
            checkResponse(allContactResponse, expectToFail);
            return allContactResponse;
        case FolderObject.INFOSTORE:
            int[] columns = new int[] { Metadata.ID, Metadata.TITLE, Metadata.DESCRIPTION, Metadata.URL, Metadata.FOLDER_ID };
            AbstractColumnsResponse allInfostoreResponse = execute(
                new AllInfostoreRequest(folderID, columns, Metadata.ID, Order.ASCENDING));
            checkResponse(allInfostoreResponse, expectToFail);
            return allInfostoreResponse;
        case FolderObject.TASK:
            CommonAllResponse allTaskResponse = execute(new AllRequest(folderID, Task.ALL_COLUMNS, Task.OBJECT_ID, Order.ASCENDING));
            checkResponse(allTaskResponse, expectToFail);
            return allTaskResponse;
        case FolderObject.CALENDAR:
            Date start = new Date(System.currentTimeMillis() - 100000000);
            Date end = new Date(System.currentTimeMillis() + 100000000);
            CommonAllResponse allCalendarResponse = execute(
                new com.openexchange.ajax.appointment.action.AllRequest(folderID, CalendarDataObject.ALL_COLUMNS, start, end, TimeZones.UTC));
            checkResponse(allCalendarResponse, expectToFail);
            return allCalendarResponse;
        default:
            Assert.fail("no all request for " + folder.getModule() + " implemented");
            return null;
        }
    }

    private DefaultHttpClient getHttpClient() {
        return getSession().getHttpClient();
    }

    private static Date getFutureTimestamp() {
        return new Date(System.currentTimeMillis() + 1000000);
    }

}
