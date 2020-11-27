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

package com.openexchange.ajax.folder.manager;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.b;
import static com.openexchange.java.Autoboxing.i;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.openexchange.groupware.modules.Module;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FolderBody;
import com.openexchange.testing.httpclient.models.FolderBodyNotification;
import com.openexchange.testing.httpclient.models.FolderData;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.FolderResponse;
import com.openexchange.testing.httpclient.models.FolderUpdateResponse;
import com.openexchange.testing.httpclient.models.FoldersCleanUpResponse;
import com.openexchange.testing.httpclient.models.FoldersResponse;
import com.openexchange.testing.httpclient.models.FoldersVisibilityData;
import com.openexchange.testing.httpclient.models.FoldersVisibilityResponse;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;
import com.openexchange.testing.httpclient.modules.FoldersApi;

/**
 * {@link FolderManager}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class FolderManager {

    /** The {@value #INFOSTORE} module identifier */
    public final static String INFOSTORE = "infostore";

    private final FoldersApi foldersApi;
    private final List<String> foldersToDelete = new ArrayList<>();
    private Long lastTimestamp;
    private final String tree;
    private String session;

    /**
     * Initializes a new {@link FolderManager}.
     * 
     * @param api The API to use
     * @param tree The tree ID to work on. Most likely you want <code>"1"</code>
     */
    public FolderManager(FolderApi api, String tree) {
        super();
        this.tree = tree;
        this.foldersApi = api.getFoldersApi();
        this.session = api.getSession();
        this.lastTimestamp = Long.valueOf(0l);
    }

    /**
     * Initializes a new {@link FolderManager}.
     */
    public FolderManager(FoldersApi api, String tree) {
        super();
        this.tree = tree;
        this.foldersApi = api;
        this.session = api.getApiClient().getSession();
        this.lastTimestamp = Long.valueOf(0l);
    }

    public String getSession() {
        return session;
    }

    public void setLastTimestamp(Long timestamp) {
        lastTimestamp = timestamp;
    }

    private void rememberFolder(String folderId) {
        foldersToDelete.add(folderId);
    }

    public void forgetFolder(String folderId) {
        foldersToDelete.remove(folderId);
    }

    public void cleanUp() throws ApiException {
        deleteFolder(foldersToDelete.stream().sorted(Collections.reverseOrder()).collect(Collectors.toList()));
    }

    /**
     * Checks if a folder move response doesn't contain any errors
     *
     * @param error The error element of the response
     * @param errorDesc The error description element of the response
     * @param data The data element of the response
     * @return The data
     */
    <T> T checkFolderMoveResponse(String error, String errorDesc, T data, Boolean ignoreWarnings) {
        if (ignoreWarnings == null || ignoreWarnings.booleanValue() == false) {
            assertNull(errorDesc, error);
        }
        assertNotNull(data);
        return data;
    }

    /**
     * Checks if a response doesn't contain any errors
     *
     * @param error The error element of the response
     * @param errorDesc The error description element of the response
     * @param data The data element of the response
     * @return The data
     */
    <T> T checkResponse(String error, String errorDesc, T data) {
        assertNull(errorDesc, error);
        assertNotNull(data);
        return data;
    }

    public String createFolder(String parent, String name, String module) throws ApiException {
        NewFolderBody body = new NewFolderBody();
        body.setFolder(FolderFactory.getSimpleFolder(name, module));
        FolderUpdateResponse createFolder = foldersApi.createFolder(parent, getSession(), body, tree, null, null, null);
        checkResponse(createFolder.getError(), createFolder.getErrorDesc(), createFolder.getData());
        rememberFolder(createFolder.getData());
        lastTimestamp = createFolder.getTimestamp();
        return createFolder.getData();
    }

    /**
     * Creates a folder
     *
     * @param parentFolder The parent folder of the new folder
     * @param title The title of the new folder
     * @param permissions The permissions of the new folder
     * @return The ID of the new folder
     * @throws ApiException
     */
    public String createFolder(String parentFolder, String title, List<FolderPermission> permissions) throws ApiException {
        NewFolderBody body = new NewFolderBody();
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setModule(Module.INFOSTORE.getName());
        folder.setTitle(title);
        folder.setPermissions(permissions);
        body.setFolder(folder);
        FolderUpdateResponse response = foldersApi.createFolder(parentFolder, session, body, null, null, null, null);
        checkResponse(response.getError(), response.getErrorDesc(), response.getData());
        rememberFolder(response.getData());
        rememberFolder(response.getData());
        lastTimestamp = response.getTimestamp();
        return response.getData();
    }

    /**
     * Gets all visible folder of the given content type
     *
     * @param contentType
     * @param columns
     * @param all
     * @return
     * @throws ApiException
     */
    public FoldersVisibilityData getAllFolders(String contentType, String columns, Boolean all) throws ApiException {
        FoldersVisibilityResponse visibleFolders = foldersApi.getVisibleFolders(getSession(), contentType, columns, tree, null, all);
        return checkResponse(visibleFolders.getError(), visibleFolders.getErrorDesc(), visibleFolders.getData());
    }

    /**
     * Lists all folders under the given folder
     *
     * @param parent The parent folder id
     * @param columns
     * @param all
     * @return
     * @throws ApiException
     */
    public ArrayList<ArrayList<Object>> listFolders(String parent, String columns, Boolean all) throws ApiException {
        FoldersResponse resp = foldersApi.getSubFolders(getSession(), parent, columns, I(all == null || b(all) ? 1 : 0), tree, null, null, Boolean.FALSE);
        return (ArrayList<ArrayList<Object>>) checkResponse(resp.getError(), resp.getErrorDesc(), resp.getData());
    }

    /**
     * Updates a folder
     *
     * @param folderId The ID of the folder to update
     * @param folder The folder data to change
     * @param message The optional message to send to a recipient
     * @return THe ID of the updated folder
     * @throws ApiException In case of error
     */
    public String updateFolder(String folderId, FolderData folder, String message) throws ApiException {
        FolderBody folderBody = new FolderBody();

        FolderBodyNotification notification = new FolderBodyNotification();
        notification.setTransport("mail");
        notification.setMessage(message);

        folderBody.setNotification(notification);
        folderBody.setFolder(folder);

        FolderUpdateResponse response = foldersApi.updateFolder(session, folderId, folderBody, Boolean.FALSE, lastTimestamp, tree, null, Boolean.TRUE, null, Boolean.FALSE, null);
        String updatedFolderId = checkResponse(response.getError(), response.getErrorDesc(), response.getData());
        rememberFolder(updatedFolderId);
        lastTimestamp = response.getTimestamp();

        return updatedFolderId;
    }

    /**
     * Deletes the list of given folder IDs
     *
     * @param foldersToDelete The folders to delete represented by their IDs
     * @return An array with object IDs of folders that could not be processed because of a concurrent modification or something else.
     * @throws ApiException In case deletion fails
     */
    public List<String> deleteFolder(List<String> foldersToDelete) throws ApiException {
        FoldersCleanUpResponse deleteFolders = foldersApi.deleteFolders(session, foldersToDelete, tree, lastTimestamp, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null, null);
        lastTimestamp = deleteFolders.getTimestamp();
        return checkResponse(deleteFolders.getError(), deleteFolders.getErrorDesc(), deleteFolders.getData());
    }

    /**
     * Gets the given folder
     * 
     * @param id The ID of the folder to get
     * @return {@link FolderData} The folder
     * @throws ApiException In case of error
     */
    public FolderData getFolder(String id) throws ApiException {
        FolderResponse folderResponse = foldersApi.getFolder(getSession(), id, tree, null, null);
        checkResponse(folderResponse.getError(), folderResponse.getErrorDesc(), folderResponse.getData());
        if (folderResponse.getTimestamp() != null && lastTimestamp != null && folderResponse.getTimestamp().longValue() > lastTimestamp.longValue()) {
            lastTimestamp = folderResponse.getTimestamp();
        }
        return folderResponse.getData();

    }

    /**
     * 
     * Gets the name of the folder with the given folder id.
     *
     * @param The folder id.
     * @return The name of the folder.
     * @throws ApiException
     */
    public String getFolderName(String folderId) throws ApiException {
        FolderResponse folder = foldersApi.getFolder(session, folderId, tree, null, null);
        return folder.getData().getTitle();
    }

    /**
     * 
     * Gets the parent folder id of the given folder
     *
     * @param id The folder id.
     * @return The parent folder id.
     * @throws ApiException
     */
    public String getParentFolderId(String id) throws ApiException {
        FolderData folderData = getFolder(id);
        return folderData.getFolderId();

    }

    public void moveFolder(String folderId, String newParent) throws ApiException {
        moveFolder(folderId, newParent, null);
    }

    public void moveFolder(String folderId, String newParent, Boolean ignoreWarnings) throws ApiException {
        FolderBody body = new FolderBody();
        FolderData folder = new FolderData();
        folder.setId(folderId);
        folder.setFolderId(newParent);
        folder.setPermissions(null);
        body.setFolder(folder);
        FolderUpdateResponse updateFolder = foldersApi.updateFolder(getSession(), folderId, body, Boolean.FALSE, lastTimestamp, tree, null, Boolean.TRUE, null, null, ignoreWarnings);
        checkFolderMoveResponse(updateFolder.getError(), updateFolder.getErrorDesc(), updateFolder.getData(), ignoreWarnings);
        lastTimestamp = updateFolder.getTimestamp();
    }

    /**
     * Finds the infostore root folder
     * 
     * @return The folder id of the infostore root
     * @throws ApiException In case folder can't be catched
     */
    public String findInfostoreRoot() throws ApiException {
        FoldersVisibilityData allFolders = getAllFolders(INFOSTORE, "1,20,300,301,302", Boolean.TRUE);
        Object folders = allFolders.getPublic();
        assertNotNull(folders);
        @SuppressWarnings("unchecked") List<List<?>> folderArray = (List<List<?>>) folders;
        // find parent
        String parent = null;
        for (List<?> o : folderArray) {
            if (o.get(1).equals("10")) {
                parent = (String) o.get(0);
                break;
            }
        }
        assertNotNull("Unable to find parent folder!", parent);
        return parent;
    }

    /**
     * Retrieves the default folder
     *
     * @param module The module to get the default folder for
     * @return The default folder
     * @throws Exception if the default folder cannot be found
     */
    public String getDefaultFolder(String module) throws Exception {
        FoldersVisibilityData allFolders = getAllFolders(module, "1,308,316", Boolean.TRUE);

        Object privateFolders = allFolders.getPrivate();
        int defType = getStandardFolderType(module);
        @SuppressWarnings("unchecked") ArrayList<ArrayList<?>> privateList = (ArrayList<ArrayList<?>>) privateFolders;
        if (privateList != null) {
            if (privateList.size() == 1) {
                return (String) privateList.get(0).get(0);
            }
            for (ArrayList<?> folder : privateList) {
                if (((Boolean) folder.get(1)).booleanValue() && (defType <= 0 || i((Integer) folder.get(2)) == defType)) {
                    return (String) folder.get(0);
                }
            }
        }
        throw new Exception("Unable to find default folder!");
    }

    private int getStandardFolderType(String module) {
        switch (module) {
            case "mail":
                return 7;
            default:
                return -1;
        }
    }

}
