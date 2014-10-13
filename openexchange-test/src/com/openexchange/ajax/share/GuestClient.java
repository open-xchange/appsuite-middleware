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

package com.openexchange.ajax.share;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
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
import com.openexchange.ajax.infostore.actions.GetInfostoreRequest;
import com.openexchange.ajax.infostore.actions.GetInfostoreResponse;
import com.openexchange.ajax.infostore.actions.NewInfostoreRequest;
import com.openexchange.ajax.infostore.actions.NewInfostoreResponse;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.ajax.session.actions.LoginResponse;
import com.openexchange.ajax.share.actions.ParsedShare;
import com.openexchange.ajax.share.actions.ResolveShareRequest;
import com.openexchange.ajax.share.actions.ResolveShareResponse;
import com.openexchange.ajax.task.actions.AllRequest;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.util.TimeZones;
import com.openexchange.java.util.UUIDs;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.ShareTarget;

/**
 * {@link GuestClient}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GuestClient extends AJAXClient {

    private final ResolveShareResponse shareResponse;
    private ShareTarget target;

    /**
     * Initializes a new {@link GuestClient}, trying to login via resolving the supplied share automatically.
     *
     * @param share The share to access as guest
     * @param password The password, or <code>null</code> if not required
     * @throws Exception
     */
    public GuestClient(ParsedShare share, String password) throws Exception {
        this(share, password, true);
    }

    /**
     * Initializes a new {@link GuestClient}, trying to login via resolving the supplied share automatically.
     *
     * @param share The share to access as guest
     * @param password The password, or <code>null</code> if not required
     * @param failOnNonRedirect <code>true</code> to fail if request is not redirected, <code>false</code>, otherwise
     * @throws Exception
     */
    public GuestClient(ParsedShare share, String password, boolean failOnNonRedirect) throws Exception {
        super(new AJAXSession(), true);
        getHttpClient().getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        this.shareResponse = resolve(share, failOnNonRedirect);
        if (null != shareResponse.getLoginType()) {
            LoginResponse loginResponse = login(shareResponse, password);
            extractShareTarget(loginResponse);
        } else {
            target = extractShareTarget(shareResponse);
        }
    }

    private static ShareTarget extractShareTarget(ResolveShareResponse shareResponse) {
        return new ShareTarget(Module.getModuleInteger(shareResponse.getModule()), shareResponse.getFolder(), shareResponse.getItem());
    }

    private static ShareTarget extractShareTarget(LoginResponse loginResponse) throws JSONException {
        JSONObject data = (JSONObject) loginResponse.getData();
        return new ShareTarget(Module.getModuleInteger(data.getString("module")), data.getString("folder"), data.getString("item"));
    }

    public ResolveShareResponse getShareResolveResponse() {
        return shareResponse;
    }

    public String getModule() {
        return Module.getForFolderConstant(target.getModule()).getName();
    }

    public int getModuleID() {
        return target.getModule();
    }

    public String getFolder() {
        return target.getFolder();
    }

    public int getIntFolder() {
        return Integer.parseInt(getFolder());
    }

    public String getItem() {
        return target.getItem();
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

    public void checkSessionAlive(boolean expectToFail) throws Exception {
        String contentType = getContentType(getModuleID());
        VisibleFoldersResponse response = execute(new VisibleFoldersRequest(
            EnumAPI.OX_NEW, contentType, FolderObject.ALL_COLUMNS, false == expectToFail));
        checkResponse(response, expectToFail);
    }

    private static void checkResponse(AbstractAJAXResponse response, boolean expectToFail) {
        Assert.assertNotNull("No response", response);
        if (expectToFail) {
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

    private Object getItem(FolderObject folder, String id, boolean expectToFail) throws Exception {
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

    /**
     * Resolves the supplied share, i.e. accesses the share link and authenticates using the share's credentials.
     *
     * @param share The share
     * @param failOnNonRedirect <code>true</code> to fail if request is not redirected, <code>false</code>, otherwise
     * @return The share response
     */
    private ResolveShareResponse resolve(ParsedShare share, boolean failOnNonRedirect) throws Exception {
        if (AuthenticationMode.ANONYMOUS == share.getAuthentication()) {
            setCredentials(null);
        } else {
            setCredentials(share.getGuestMailAddress(), share.getGuestPassword());
        }
        ResolveShareResponse response = Executor.execute(this, new ResolveShareRequest(share, failOnNonRedirect));
        getSession().setId(response.getSessionID());
        return response;
    }

    private LoginResponse login(ResolveShareResponse shareResponse, String password) throws Exception {
        String loginType = shareResponse.getLoginType();
        if (null != loginType) {
            LoginRequest loginRequest = null;
            if ("guest".equals(loginType)) {
                loginRequest = LoginRequest.createGuestLoginRequest(shareResponse.getShare(), shareResponse.getLoginName(), password, true);
            } else if ("anonymous".equals(loginType)) {
                loginRequest = LoginRequest.createAnonymousLoginRequest(shareResponse.getShare(), password, true);
            } else {
                Assert.fail("unknown login type: " + loginType);
            }
            LoginResponse response = Executor.execute(this, loginRequest);
            getSession().setId(response.getSessionId());
            return response;
        }
        return null;
    }

    private DefaultHttpClient getHttpClient() {
        return getSession().getHttpClient();
    }

    private void setCredentials(org.apache.http.auth.Credentials credentials) {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        if (null != credentials) {
            credentialsProvider.setCredentials(org.apache.http.auth.AuthScope.ANY, credentials);
        }
        getHttpClient().setCredentialsProvider(credentialsProvider);
    }

    private void setCredentials(String username, String password) {
        setCredentials(new UsernamePasswordCredentials(username, password));
    }

    private static Date getFutureTimestamp() {
        return new Date(System.currentTimeMillis() + 1000000);
    }

}
