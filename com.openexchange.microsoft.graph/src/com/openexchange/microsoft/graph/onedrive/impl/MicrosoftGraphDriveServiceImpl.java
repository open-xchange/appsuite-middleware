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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.microsoft.graph.onedrive.impl;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.java.Strings;
import com.openexchange.microsoft.graph.api.MicrosoftGraphOneDriveAPI;
import com.openexchange.microsoft.graph.api.MicrosoftGraphQueryParameters;
import com.openexchange.microsoft.graph.api.MicrosoftGraphQueryParameters.Builder;
import com.openexchange.microsoft.graph.api.MicrosoftGraphQueryParameters.ParameterName;
import com.openexchange.microsoft.graph.api.exception.ErrorCode;
import com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService;
import com.openexchange.microsoft.graph.onedrive.OneDriveFile;
import com.openexchange.microsoft.graph.onedrive.OneDriveFolder;
import com.openexchange.microsoft.graph.onedrive.parser.OneDriveFileParser;
import com.openexchange.microsoft.graph.onedrive.parser.OneDriveFolderParser;
import com.openexchange.rest.client.exception.RESTExceptionCodes;

/**
 * {@link MicrosoftGraphDriveServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class MicrosoftGraphDriveServiceImpl implements MicrosoftGraphDriveService {

    private static final Logger LOG = LoggerFactory.getLogger(MicrosoftGraphDriveServiceImpl.class);
    /**
     * API limit for simple uploads, a.k.a. one shot uploads
     * 
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/v1.0/api/driveitem_put_content">Upload files</a>
     */
    private static final long ONE_SHOT_UPLOAD_LIMIT = 1024 * 1024 * 4;

    private final MicrosoftGraphOneDriveAPI api;
    private final OneDriveFolderParser folderEntityParser;
    private final OneDriveFileParser fileEntityParser;

    /**
     * Initialises a new {@link MicrosoftGraphDriveServiceImpl}.
     */
    public MicrosoftGraphDriveServiceImpl(MicrosoftGraphOneDriveAPI api) {
        super();
        this.api = api;
        this.folderEntityParser = new OneDriveFolderParser();
        this.fileEntityParser = new OneDriveFileParser();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#existsFolder(java.lang.String, java.lang.String)
     */
    @Override
    public boolean existsFolder(String accessToken, String folderId) throws OXException {
        try {
            JSONObject response = api.getFolder(accessToken, folderId);
            return !containsError(response, ErrorCode.itemNotFound);
        } catch (OXException e) {
            // FIXME: introduce own exception codes
            if (RESTExceptionCodes.PAGE_NOT_FOUND.equals(e)) {
                return false;
            }
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#getRootFolder(java.lang.String)
     */
    @Override
    public OneDriveFolder getRootFolder(int userId, String accessToken) throws OXException {
        return folderEntityParser.parseEntity(userId, hasSubFolders(accessToken, null), api.getRoot(accessToken));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#getFolder(int, java.lang.String, java.lang.String)
     */
    @Override
    public OneDriveFolder getFolder(int userId, String accessToken, String folderId) throws OXException {
        return folderEntityParser.parseEntity(userId, hasSubFolders(accessToken, folderId), api.getFolder(accessToken, folderId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#getSubFolders(java.lang.String, java.lang.String)
     */
    @Override
    public List<OneDriveFolder> getSubFolders(int userId, String accessToken, String folderId) throws OXException {
        int top = 100;
        String skipToken = null;
        List<OneDriveFolder> list = new LinkedList<>();
        do {
            Builder paramBuilder = new Builder();
            paramBuilder.withParameter(ParameterName.TOP, Integer.toString(top)).withParameter(ParameterName.SKIPTOKEN, skipToken);

            JSONObject response = api.getChildren(accessToken, folderId, paramBuilder.build());
            skipToken = extractSkipToken(response);
            list.addAll(parseEntities(userId, accessToken, response.optJSONArray("value")));

        } while (Strings.isNotEmpty(skipToken));
        return list;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#createFolder(int, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public OneDriveFolder createFolder(int userId, String accessToken, String folderName, String parentId, boolean autorename) throws OXException {
        if (autorename) {
            return folderEntityParser.parseEntity(userId, false, api.createFolder(accessToken, folderName, parentId, autorename));
        }
        // The Microsoft Graph API does not return an error when the 'autorename' behaviour is not present and the folder exists;
        // hence we have to manually search for any folders with that name
        checkFolderExistence(accessToken, folderName, parentId);
        return folderEntityParser.parseEntity(userId, false, api.createFolder(accessToken, folderName, parentId, autorename));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#deleteFolder(java.lang.String, java.lang.String)
     */
    @Override
    public void deleteFolder(String accessToken, String folderId) throws OXException {
        api.deleteItem(accessToken, folderId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#clearFolder(java.lang.String, java.lang.String)
     */
    @Override
    public void clearFolder(String accessToken, String folderId) throws OXException {
        JSONObject ids = api.getChildren(accessToken, folderId, new Builder().withParameter(ParameterName.SELECT, "id").build());
        JSONArray idsArray = ids.optJSONArray("value");
        if (idsArray == null || idsArray.isEmpty()) {
            return;
        }
        for (int index = 0; index < idsArray.length(); index++) {
            JSONObject item = idsArray.optJSONObject(index);
            if (item == null || item.isEmpty()) {
                continue;
            }
            String id = item.optString("id");
            if (Strings.isEmpty(id)) {
                continue;
            }
            api.deleteItem(accessToken, id);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#renameFolder(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void renameFolder(String accessToken, String folderId, String newName) throws OXException {
        try {
            JSONObject body = new JSONObject();
            body.put("name", newName);
            api.patchItem(accessToken, folderId, body);
        } catch (JSONException e) {
            throw new OXException(666, "JSON error", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#moveFolder(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String moveFolder(String accessToken, String folderId, String parentId, String newName) throws OXException {
        try {
            JSONObject parentRef = new JSONObject();
            parentRef.put("id", parentId);
            JSONObject body = new JSONObject();
            body.put("name", newName);
            body.put("parentReference", parentRef);
            JSONObject patchItem = api.patchItem(accessToken, folderId, body);
            return patchItem.optString("id");
        } catch (JSONException e) {
            throw new OXException(666, "JSON error", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#moveFolder(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String moveFolder(String accessToken, String folderId, String parentId) throws OXException {
        return moveItem(accessToken, folderId, parentId);
    }

    /////////////////////////////////////// FILES //////////////////////////////////////

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#getFiles(int, java.lang.String, java.lang.String)
     */
    @Override
    public List<OneDriveFile> getFiles(int userId, String accessToken, String folderId) throws OXException {
        int offset = 100;
        String skipToken = null;
        List<OneDriveFile> list = new LinkedList<>();
        do {
            MicrosoftGraphQueryParameters.Builder paramBuilder = new MicrosoftGraphQueryParameters.Builder();
            paramBuilder.withParameter(ParameterName.TOP, Integer.toString(offset)).withParameter(ParameterName.SKIPTOKEN, skipToken);

            JSONObject response = api.getChildren(accessToken, folderId, paramBuilder.build());
            skipToken = extractSkipToken(response);
            list.addAll(fileEntityParser.parseEntities(userId, response.optJSONArray("value")));
        } while (Strings.isNotEmpty(skipToken));
        return list;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#getFile(int, java.lang.String, java.lang.String)
     */
    @Override
    public OneDriveFile getFile(int userId, String accessToken, String itemId) throws OXException {
        return fileEntityParser.parseEntity(userId, api.getItem(accessToken, itemId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#getFiles(int, java.lang.String, java.util.List)
     */
    @Override
    public List<OneDriveFile> getFiles(int userId, String accessToken, List<String> itemIds) throws OXException {
        List<OneDriveFile> files = new LinkedList<>();
        for (String itemId : itemIds) {
            try {
                files.add(fileEntityParser.parseEntity(userId, api.getItem(accessToken, itemId)));
            } catch (OXException e) {
                // FIXME: introduce own exception codes
                if (RESTExceptionCodes.PAGE_NOT_FOUND.equals(e)) {
                    LOG.debug("Item with id '{}' for user with id '{}' was not found in OneDrive", itemId, userId);
                    continue;
                }
                throw e;
            }
        }
        return files;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#deleteFile(java.lang.String, java.lang.String)
     */
    @Override
    public void deleteFile(String accessToken, String fileId) throws OXException {
        api.deleteItem(accessToken, fileId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#moveFile(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String moveFile(String accessToken, String fileId, String parentId) throws OXException {
        return moveItem(accessToken, fileId, parentId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#updateFile(java.lang.String, com.openexchange.file.storage.File, java.lang.String)
     */
    @Override
    public String updateFile(String accessToken, File file, List<Field> modifiedFields, String parentId) throws OXException {
        try {
            JSONObject body = compileUpdateBody(file, modifiedFields, parentId);
            if (body.isEmpty()) {
                return file.getId();
            }
            JSONObject response = api.patchItem(accessToken, file.getId(), body);
            return response.optString("id");
        } catch (JSONException e) {
            throw new OXException(666, "JSON error", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#copyFile(java.lang.String, com.openexchange.file.storage.File, java.lang.String)
     */
    @Override
    public String copyFile(String accessToken, String itemId, File file, List<Field> modifiedFields, String parentId) throws OXException {
        try {
            return api.copyItem(accessToken, itemId, compileUpdateBody(file, modifiedFields, parentId));
        } catch (JSONException e) {
            throw new OXException(666, "JSON error", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#copyFile(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String copyFile(String accessToken, String itemId, String parentId) throws OXException {
        try {
            JSONObject parentRef = new JSONObject();
            parentRef.put("id", parentId);
            //parentRef.put("driveId", getDriveId());
            JSONObject body = new JSONObject();
            body.put("parentReference", parentRef);
            return api.copyItem(accessToken, itemId, body);
        } catch (JSONException e) {
            throw new OXException(666, "JSON error", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#getFile(java.lang.String, java.lang.String)
     */
    @Override
    public InputStream getFile(String accessToken, String fileId) throws OXException {
        return api.getContent(accessToken, fileId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#getThumbnail(java.lang.String, java.lang.String)
     */
    @Override
    public InputStream getThumbnail(String accessToken, String itemId) throws OXException {
        return api.getThumbnailContent(accessToken, itemId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#getQuota(java.lang.String)
     */
    @Override
    public Quota getQuota(String accessToken) throws OXException {
        JSONObject drive = api.getDrive(accessToken, new Builder().withParameter(ParameterName.SELECT, "quota").build());
        JSONObject quota = drive.optJSONObject("quota");
        if (quota == null || quota.isEmpty()) {
            // Should never happen, but just in case...
            return new Quota(-1, -1, Type.STORAGE);
        }
        return new Quota(quota.optInt("total"), quota.optInt("used"), Type.STORAGE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#searchFiles(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public List<OneDriveFile> searchFiles(int userId, String accessToken, String query, String folderId, boolean includeSubfolders) throws OXException {
        int top = 100;
        String skipToken = null;
        List<OneDriveFile> list = new LinkedList<>();
        do {
            Builder b = new MicrosoftGraphQueryParameters.Builder();
            b.withParameter(ParameterName.TOP, Integer.toString(top)).withParameter(ParameterName.SKIPTOKEN, skipToken);

            JSONObject result = api.searchItems(accessToken, query, b.build());
            skipToken = extractSkipToken(result);

            JSONArray array = result.optJSONArray("value");
            if (array == null || array.isEmpty()) {
                return list;
            }

            for (int index = 0; index < array.length(); index++) {
                JSONObject item = array.optJSONObject(index);
                if (item == null || item.isEmpty()) {
                    continue;
                }
                if (!item.hasAndNotNull("file")) {
                    continue;
                }

                JSONObject parentRef = item.optJSONObject("parentReference");
                if (parentRef == null || parentRef.isEmpty()) {
                    continue;
                }
                if (folderId == null || includeSubfolders || folderId.equals(parentRef.optString("id"))) {
                    list.add(fileEntityParser.parseEntity(userId, item));
                }
            }
        } while (Strings.isNotEmpty(skipToken));
        return list;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#upload(java.lang.String, com.openexchange.file.storage.File, java.io.InputStream)
     */
    @Override
    public String upload(String accessToken, File file, InputStream inputStream) {
        // TODO Auto-generated method stub
        return null;
    }

    //////////////////////////////////////// HELPERS /////////////////////////////////////

    /**
     * 
     * @param accessToken
     * @param itemId
     * @param parentId
     * @return
     * @throws OXException
     */
    private String moveItem(String accessToken, String itemId, String parentId) throws OXException {
        try {
            JSONObject parentRef = new JSONObject();
            parentRef.put("id", parentId);

            JSONObject body = new JSONObject();
            body.put("parentReference", parentRef);

            JSONObject patchItem = api.patchItem(accessToken, itemId, body);
            return patchItem.optString("id");
        } catch (JSONException e) {
            throw new OXException(666, "JSON error", e);
        }
    }

    /**
     * Checks whether a folder with the specified name already exists as a sub-folder of the folder with the specified identifier
     * 
     * @param accessToken The oauth access token
     * @param folderName The folder name to check
     * @param parentId The parent folder's identifier
     * @throws OXException if an error is occurred
     */
    private void checkFolderExistence(String accessToken, String folderName, String parentId) throws OXException {
        Builder b = new Builder();
        b.withParameter(ParameterName.SELECT, "id,name,parentReference").withParameter(ParameterName.FILTER, "folder ne null");
        JSONObject children = api.searchItems(accessToken, folderName, b.build());
        JSONArray namesArray = children.optJSONArray("value");
        if (namesArray == null || namesArray.isEmpty()) {
            return;
        }
        for (int index = 0; index < namesArray.length(); index++) {
            JSONObject candidate = namesArray.optJSONObject(index);
            if (candidate == null || candidate.isEmpty()) {
                continue;
            }
            JSONObject parentRef = candidate.optJSONObject("parentReference");
            if (parentRef == null || parentRef.isEmpty()) {
                continue;
            }
            if (parentId.equals(parentRef.optString("id"))) {
                // FIXME: introduce own exception codes
                throw FileStorageExceptionCodes.DUPLICATE_FOLDER.create(folderName, parentRef.optString("name"));
            }
        }
    }

    /**
     * Checks whether the folder with the specified identifier has any sub-folders
     * 
     * @param accessToken The oauth access token
     * @param folderId The folder identifier
     * @return <code>true</code> if the folder has at least one sub-folder;<code>false</code> otherwise
     * @throws OXException if an error is occurred
     */
    private boolean hasSubFolders(String accessToken, String folderId) throws OXException {
        MicrosoftGraphQueryParameters params = new Builder().withParameter(ParameterName.SELECT, "folder").build();
        JSONObject j = Strings.isEmpty(folderId) ? api.getRootChildren(accessToken, params) : api.getChildren(accessToken, folderId, params);
        JSONArray entities = j.optJSONArray("value");
        if (entities == null || entities.isEmpty()) {
            return false;
        }
        for (int index = 0; index < entities.length(); index++) {
            JSONObject entity = entities.optJSONObject(index);
            if (entity == null || entity.isEmpty()) {
                continue;
            }
            if (entity.hasAndNotNull("folder")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parses the specified {@link JSONArray} of entities in to a {@link List}
     * of {@link OneDriveFolder}s
     * 
     * @param userId the user identifier
     * @param entities The {@link JSONArray} with the entities
     * @return A {@link List} with the {@link OneDriveFolder}s
     * @throws OXException
     */
    private List<OneDriveFolder> parseEntities(int userId, String accessToken, JSONArray entities) throws OXException {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        List<OneDriveFolder> folders = new LinkedList<>();
        for (int index = 0; index < entities.length(); index++) {
            JSONObject entity = entities.optJSONObject(index);
            String folderId = entity.optString("id");
            OneDriveFolder folder = folderEntityParser.parseEntity(userId, hasSubFolders(accessToken, folderId), entity);
            if (folder != null) {
                folders.add(folder);
            }
        }
        return folders;
    }

    /**
     * Checks and extracts a 'skipToken' if available.
     * 
     * @param response The {@link JSONObject} response body
     * @return the skipToken or <code>null</code> if none available.
     */
    private String extractSkipToken(JSONObject response) {
        String nextLink = response.optString("@odata.nextLink");
        if (Strings.isEmpty(nextLink)) {
            return null;
        }
        int indexOfKey = nextLink.indexOf("skiptoken");
        if (indexOfKey < 0) {
            return null;
        }
        int indexOfValue = nextLink.indexOf("=", indexOfKey);
        if (indexOfValue < 0) {
            return null;
        }
        int indexOfLast = nextLink.indexOf("=", indexOfValue);
        return ((indexOfLast < 0)) ? nextLink.substring(indexOfValue) : nextLink.substring(indexOfValue, indexOfLast);
    }

    /**
     * Checks the specified response whether it contains the specified error code
     * 
     * @param response The response
     * @param errorCode The error code
     * @return <code>true</code> if the specified error code is contained</code>;
     *         <code>false</code> if the response is null or empty, or if the error code is not contained.
     */
    private boolean containsError(JSONObject response, ErrorCode errorCode) {
        if (response == null || response.isEmpty()) {
            return true;
        }
        if (!response.hasAndNotNull("error")) {
            return false;
        }
        JSONObject error = response.optJSONObject("error");
        return errorCode.name().equals(error.optString("code"));
    }

    /**
     * 
     * @param file
     * @param modifiedFields
     * @param parentId
     * @return
     * @throws JSONException
     */
    private JSONObject compileUpdateBody(File file, List<Field> modifiedFields, String parentId) throws JSONException {
        JSONObject body = new JSONObject();
        if (modifiedFields == null || modifiedFields.contains(Field.FILENAME)) {
            body.put("name", file.getFileName());
        }
        if (modifiedFields == null || modifiedFields.contains(Field.DESCRIPTION)) {
            body.put("description", file.getDescription());
        }
        if (Strings.isNotEmpty(parentId)) {
            JSONObject parentRef = new JSONObject();
            parentRef.put("id", parentId);
            body.put("parentReference", parentRef);
        }
        return body;
    }
}
