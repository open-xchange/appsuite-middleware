/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.folder.manager;

import static com.openexchange.exception.OXExceptionConstants.CATEGORY_WARNING;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.b;
import static com.openexchange.java.Autoboxing.i;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import com.openexchange.ajax.framework.CleanableResourceManager;
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
public class FolderManager implements CleanableResourceManager {

    /** The {@value #INFOSTORE} module identifier */
    public final static String INFOSTORE = "infostore";

    private final FoldersApi foldersApi;
    private final List<String> foldersToDelete = new ArrayList<>();
    private Long lastTimestamp;
    private final String tree;

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
        this.lastTimestamp = Long.valueOf(0l);
    }

    /**
     * Initializes a new {@link FolderManager}.
     *
     * @param api The folders API to use
     * @param tree The tree ID to work on. Most likely you want <code>"1"</code>
     */
    public FolderManager(FoldersApi api, String tree) {
        super();
        this.tree = tree;
        this.foldersApi = api;
        this.lastTimestamp = Long.valueOf(0l);
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

    @Override
    public void cleanUp() throws ApiException {
        deleteFolder(foldersToDelete.stream().sorted(Collections.reverseOrder()).collect(Collectors.toList()), true);
    }

    /**
     * Checks if a folder move response doesn't contain any errors
     *
     * @param category The category or null
     * @param error The error element of the response
     * @param errorDesc The error description element of the response
     * @param data The data element of the response
     * @param ignoreWarnings Whether to ignore warnings or not
     * @return The data
     */
    <T> T checkFolderMoveResponse(String category, String error, String errorDesc, T data, Boolean ignoreWarnings) {
        if (ignoreWarnings != null && ignoreWarnings.booleanValue() && category != null && category.equals(CATEGORY_WARNING.getType().getName())) {
            assertNotNull(data);
            return data;
        }

        assertNull(errorDesc, error);
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
        FolderUpdateResponse createFolder = foldersApi.createFolder(parent, body, tree, null, null, null);
        String folderId = checkResponse(createFolder.getError(), createFolder.getErrorDesc(), createFolder.getData());
        rememberFolder(folderId);
        lastTimestamp = createFolder.getTimestamp();
        return folderId;
    }

    /**
     * Creates an infostore folder
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
        FolderUpdateResponse response = foldersApi.createFolder(parentFolder, body, null, null, null, null);
        String folderId = checkResponse(response.getError(), response.getErrorDesc(), response.getData());
        rememberFolder(folderId);
        lastTimestamp = response.getTimestamp();
        return folderId;
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
        FoldersVisibilityResponse visibleFolders = foldersApi.getVisibleFolders(contentType, columns, tree, null, all);
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
        FoldersResponse resp = foldersApi.getSubFolders(parent, columns, I(all == null || b(all) ? 1 : 0), tree, null, null, Boolean.FALSE);
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

        FolderUpdateResponse response = foldersApi.updateFolder(folderId, folderBody, Boolean.FALSE, lastTimestamp, tree, null, Boolean.TRUE, null, Boolean.FALSE, null);
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
        return deleteFolder(foldersToDelete, false);
    }

    private List<String> deleteFolder(List<String> foldersToDelete, boolean ignoreFolderNotFound) throws ApiException {
        FoldersCleanUpResponse deleteFolders = foldersApi.deleteFolders(foldersToDelete, tree, lastTimestamp, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null, null);
        lastTimestamp = deleteFolders.getTimestamp();
        if (ignoreFolderNotFound && ("FLD-0008".equals(deleteFolders.getCode()) || "FILE_STORAGE-0007".equals(deleteFolders.getCode()))) {
            // ignore FLD-0008 / FILE_STORAGE-0007 errors
            return checkResponse(null, null, deleteFolders.getData());
        }
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
        FolderResponse folderResponse = foldersApi.getFolder(id, tree, null, null);
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
        FolderResponse folder = foldersApi.getFolder(folderId, tree, null, null);
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

    public String moveFolder(String folderId, String newParent) throws ApiException {
        return moveFolder(folderId, newParent, null);
    }

    public String moveFolder(String folderId, String newParent, Boolean ignoreWarnings) throws ApiException {
        FolderBody body = new FolderBody();
        FolderData folder = new FolderData();
        folder.setFolderId(newParent);
        folder.setPermissions(null);
        folder.setComOpenexchangeShareExtendedPermissions(null);
        body.setFolder(folder);
        FolderUpdateResponse updateFolder = foldersApi.updateFolder(folderId, body, Boolean.FALSE, lastTimestamp, tree, null, Boolean.TRUE, null, null, ignoreWarnings);
        checkFolderMoveResponse(updateFolder.getCategories(), updateFolder.getError(), updateFolder.getErrorDesc(), updateFolder.getData(), ignoreWarnings);
        lastTimestamp = updateFolder.getTimestamp();
        return updateFolder.getData();
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
