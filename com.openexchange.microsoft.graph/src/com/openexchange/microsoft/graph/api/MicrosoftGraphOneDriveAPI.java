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

package com.openexchange.microsoft.graph.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.microsoft.graph.api.client.MicrosoftGraphRESTClient;
import com.openexchange.microsoft.graph.api.client.MicrosoftGraphRESTEndPoint;
import com.openexchange.microsoft.graph.api.client.MicrosoftGraphRequest;
import com.openexchange.microsoft.graph.api.exception.MicrosoftGraphAPIExceptionCodes;
import com.openexchange.policy.retry.LinearRetryPolicy;
import com.openexchange.policy.retry.RetryPolicy;
import com.openexchange.rest.client.exception.RESTExceptionCodes;
import com.openexchange.rest.client.v2.RESTMethod;
import com.openexchange.rest.client.v2.RESTResponse;
import com.openexchange.rest.client.v2.entity.JSONObjectEntity;

/**
 * {@link MicrosoftGraphOneDriveAPI}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class MicrosoftGraphOneDriveAPI extends AbstractMicrosoftGraphAPI {

    private static final Logger LOG = LoggerFactory.getLogger(MicrosoftGraphOneDriveAPI.class);

    /**
     * The base API URL '/me/drive'
     */
    private static final String BASE_URL = "/me" + MicrosoftGraphRESTEndPoint.drive.getAbsolutePath();

    /**
     * The chunk size has to be a multiple of 320KiB (327.680 bytes). A multiplier
     * can also be used with that base chunk size.
     */
    private static final int CHUNK_SIZE = 327680;

    /**
     * Initialises a new {@link MicrosoftGraphOneDriveAPI}.
     * 
     * @param client The rest client
     */
    public MicrosoftGraphOneDriveAPI(MicrosoftGraphRESTClient client) {
        super(client);
    }

    /**
     * Gets the user's OneDrive
     * 
     * @param accessToken OAuth The access token
     * @return A {@link JSONObject} with the user's one drive metadata
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/beta/api/drive_get">Get Drive</a>
     */
    public JSONObject getDrive(String accessToken) throws OXException {
        return getResource(accessToken, BASE_URL);
    }

    /**
     * Gets the user's OneDrive
     * 
     * @param accessToken OAuth The access token
     * @return A {@link JSONObject} with the user's one drive metadata
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/beta/api/drive_get">Get Drive</a>
     */
    public JSONObject getDrive(String accessToken, MicrosoftGraphQueryParameters queryParameters) throws OXException {
        return getResource(accessToken, BASE_URL, queryParameters.getQueryParametersMap());
    }

    /**
     * Returns the metadata of the root folder for a user's default Drive
     * 
     * @param accessToken The oauth access token
     * @return A {@link JSONObject} with the metadata of the user's root folder
     *         of the default Drive
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/beta/api/driveitem_get">Get a file or folder</a>
     */
    public JSONObject getRoot(String accessToken) throws OXException {
        return getResource(accessToken, BASE_URL + "/root");
    }

    /**
     * Returns the metadata of the root folder for a user's default Drive
     * 
     * @param accessToken The oauth access token
     * @param queryParams The request query parameters
     * @return A {@link JSONObject} with the metadata of the user's root folder
     *         of the default Drive
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/beta/api/driveitem_get">Get a file or folder</a>
     */
    public JSONObject getRoot(String accessToken, MicrosoftGraphQueryParameters queryParameters) throws OXException {
        return getResource(accessToken, BASE_URL + "/root", queryParameters.getQueryParametersMap());
    }

    /**
     * Returns the metadata of all children (files and folders) of the specified folder.
     * 
     * @param accessToken The oauth access token
     * @param queryParams The request query parameters
     * @return A {@link JSONObject} with the metadata of all children (files and folders) of the specified folder.
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/beta/api/driveitem_list_children">List the contents of a folder</a>
     */
    public JSONObject getRootChildren(String accessToken, MicrosoftGraphQueryParameters queryParameters) throws OXException {
        return getResource(accessToken, BASE_URL + "/root/children", queryParameters.getQueryParametersMap());
    }

    /**
     * Returns the metadata of the folder with the specified identifier for a user's default Drive
     * 
     * @param accessToken The oauth access token
     * @param folderPath The folder's unique identifier
     * @return A {@link JSONObject} with the metadata of the user's specified folder
     *         of the default Drive
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/beta/api/driveitem_get">Get a file or folder</a>
     */
    public JSONObject getFolder(String accessToken, String folderId) throws OXException {
        if (Strings.isEmpty(folderId)) {
            return getRoot(accessToken);
        }
        return getResource(accessToken, BASE_URL + "/items/" + folderId);
    }

    /**
     * Returns the metadata of the folder with the specified identifier for a user's default Drive
     * 
     * @param accessToken The oauth access token
     * @param folderPath The folder's unique identifier
     * @param queryParams The request query parameters
     * @return A {@link JSONObject} with the metadata of the user's specified folder
     *         of the default Drive
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/beta/api/driveitem_get">Get a file or folder</a>
     */
    public JSONObject getFolder(String accessToken, String folderId, MicrosoftGraphQueryParameters queryParameters) throws OXException {
        if (Strings.isEmpty(folderId)) {
            return getRoot(accessToken);
        }
        return getResource(accessToken, BASE_URL + "/items/" + folderId, queryParameters.getQueryParametersMap());
    }

    /**
     * Returns the metadata of all children (files and folders) of the specified folder.
     * 
     * @param accessToken The oauth access token
     * @param folderPath The folder's unique identifier
     * @return A {@link JSONObject} with the metadata of all children (files and folders) of the specified folder.
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/beta/api/driveitem_list_children">List the contents of a folder</a>
     */
    public JSONObject getChildren(String accessToken, String folderId) throws OXException {
        return getChildren(accessToken, folderId, new MicrosoftGraphQueryParameters.Builder().build());
    }

    /**
     * Returns the metadata of all children (files and folders) of the specified folder.
     * 
     * @param accessToken The oauth access token
     * @param folderPath The folder's unique identifier
     * @param offset The offset for the request
     * @param skipToken The skip token for the next page (provided by the API's response)
     * @return A {@link JSONObject} with the metadata of all children (files and folders) of the specified folder.
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/beta/api/driveitem_list_children">List the contents of a folder</a>
     */
    public JSONObject getChildren(String accessToken, String folderId, MicrosoftGraphQueryParameters queryParameters) throws OXException {
        if (Strings.isEmpty(folderId)) {
            return getRootChildren(accessToken, new MicrosoftGraphQueryParameters.Builder().build());
        }
        return getResource(accessToken, BASE_URL + "/items/" + folderId + "/children", queryParameters.getQueryParametersMap());
    }

    /**
     * Returns the item (being either a file or folder) with the specified identifier
     * 
     * @param accessToken The oauth access token
     * @param itemId The item's identifier
     * @return The item as a {@link JSONObject}
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/beta/api/driveitem_get">Get a file or folder</a>
     */
    public JSONObject getItem(String accessToken, String itemId) throws OXException {
        return getResource(accessToken, BASE_URL + "/items/" + itemId);
    }

    /**
     * Downloads the contents of a file
     * 
     * @param accessToken the oauth access token
     * @param itemId The item's identifier
     * @return The InputStream with the contents of the file
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/beta/api/driveitem_get_content">Download the contents of a file</a>
     */
    public InputStream getContent(String accessToken, String itemId) throws OXException {
        JSONObject o = getItem(accessToken, itemId);
        String location = o.optString("@microsoft.graph.downloadUrl");
        return getStream(location);
    }

    /**
     * Asynchronously creates a copy of a driveItem (including any children), under a new parent item or with a new name.
     * If another file with the same name already exists it will automatically be renamed by the API
     * 
     * @param accessToken The oauth access token
     * @param itemId The item identifier
     * @param body The body with the copy information such as new parent identifier or name conflict behaviour
     * @return The value of the <code>Location</code> header which provides a URL for a service that will return
     *         the current state of the copy operation.
     * @throws OXException
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/beta/api/driveitem_copy">Copy a file or folder</a>
     */
    public String copyItemAsync(String accessToken, String itemId, JSONObject body) throws OXException {
        MicrosoftGraphRequest request = new MicrosoftGraphRequest(RESTMethod.POST, BASE_URL + "/itames/" + itemId + "/copy");
        request.setAccessToken(accessToken);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.sethBodyEntity(new JSONObjectEntity(body));
        RESTResponse restResponse = client.execute(request);
        if (restResponse.getStatusCode() == 202) {
            return restResponse.getHeader(HttpHeaders.LOCATION);
        }
        throw new OXException(666, "Copy failed: " + restResponse.getStatusCode());
    }

    /**
     * Synchronously creates a copy of a drive item.
     * If another file with the same name already exists it will automatically be renamed by the API
     * 
     * @param accessToken The oauth access token
     * @param itemId The item identifier
     * @param body The body with the copy information such as new parent identifier or name conflict behaviour
     * @return The new identifier of the item
     * @throws OXException if an error is occurred
     */
    public String copyItem(String accessToken, String itemId, JSONObject body) throws OXException {
        MicrosoftGraphRequest request = new MicrosoftGraphRequest(RESTMethod.POST, BASE_URL + "/items/" + itemId + "/copy");
        request.setAccessToken(accessToken);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.sethBodyEntity(new JSONObjectEntity(body));
        RESTResponse restResponse = client.execute(request);
        String location = restResponse.getHeader(HttpHeaders.LOCATION);
        if (Strings.isEmpty(location)) {
            throw MicrosoftGraphAPIExceptionCodes.GENERAL_EXCEPTION.create("Cannot monitor the 'copy' status. Location header is absent.");
        }
        RetryPolicy retryPolicy = new LinearRetryPolicy();
        do {
            JSONObject monitor = monitorAsyncTask(location);
            String status = monitor.optString("status");
            if ("failed".equals(status)) {
                throw MicrosoftGraphAPIExceptionCodes.GENERAL_EXCEPTION.create("Copying item with id '" + itemId + "' has failed and that's all we know.");
            }
            double percentage = monitor.optDouble("percentageComplete");
            if (percentage == 100) {
                return monitor.optString("resourceId");
            }
            LOG.debug("Copying item with id '{}', completed '{}'", itemId, percentage + "%");
        } while (retryPolicy.isRetryAllowed());
        throw MicrosoftGraphAPIExceptionCodes.GENERAL_EXCEPTION.create("Copy failed. Gave up after " + retryPolicy.getMaxTries() + " tries.");
    }

    /**
     * Retrieves the status report from the specified monitor URL
     * 
     * @param accessToken The oauth access token
     * @param location The URL from which to retrieve the status report
     * @return A {@link JSONObject} with the status report
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/long_running_actions_overview#retrieve-a-completed-status-report-from-the-monitor-url">Retrieve a completed status report from the monitor URL</a>
     */
    public JSONObject monitorAsyncTask(String location) throws OXException {
        HttpRequestBase get = new HttpGet(location);
        RESTResponse response = client.execute(get);
        if (response.getResponseBody() instanceof JSONValue) {
            return ((JSONValue) response.getResponseBody()).toObject();
        }
        throw MicrosoftGraphAPIExceptionCodes.GENERAL_EXCEPTION.create("Unable to retrieve monitoring information from '" + location + "'");
    }

    /**
     * Retrieves the thumbnails' metadata of the item with the specified identifier
     * 
     * @param accessToken The oauth access token
     * @param itemId The item's identifier
     * @return A {@link JSONObject} with the thumbnails' metadata
     * @throws OXException if an error is occurred
     */
    public JSONObject getThumbnails(String accessToken, String itemId) throws OXException {
        return getResource(accessToken, BASE_URL + "/items/" + itemId + "/thumbnails");
    }

    /**
     * Retrieves the thumbnail content of the specified item
     * 
     * @param accessToken The oauth access token
     * @param itemId The item's identifier
     * @return An {@link InputStream} with the thumbnail's contents ready to be streamed to the client
     * @throws OXException if an error is occurred
     */
    public InputStream getThumbnailContent(String accessToken, String itemId) throws OXException {
        JSONObject thumbnails = getThumbnails(accessToken, itemId);
        JSONArray array = thumbnails.optJSONArray("value");
        if (array == null || array.isEmpty()) {
            // No thumbnail available
            return null;
        }
        for (int index = 0; index < array.length(); index++) {
            JSONObject thumbnail = array.optJSONObject(index);
            JSONObject thumb = thumbnail.optJSONObject("large");
            if (thumb == null || thumb.isEmpty()) {
                continue;
            }
            String location = thumb.optString("url");
            if (Strings.isEmpty(location)) {
                continue;
            }
            HttpRequestBase get = new HttpGet(location);
            RESTResponse response = client.execute(get);
            if (response.getResponseBody() instanceof byte[]) {
                return new ByteArrayInputStream(byte[].class.cast(response.getResponseBody()));
            }
        }
        // No thumbnail available
        return null;
    }

    /**
     * Deletes the item with the specified identifier
     * 
     * @param accessToken The oauth access token
     * @param itemId The item's identifier
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/beta/api/driveitem_delete">Delete a file or folder</a>
     */
    public void deleteItem(String accessToken, String itemId) throws OXException {
        deleteResource(accessToken, BASE_URL + "/items/" + itemId);
    }

    /**
     * Performs a search and returns the results
     * 
     * @param accessToken The oauth access token
     * @param query the search query
     * @param queryParams The request query parameters
     * @return The results of the search
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/beta/api/driveitem_search">Search for files</a>
     */
    public JSONObject searchItems(String accessToken, String query, MicrosoftGraphQueryParameters queryParams) throws OXException {
        return getResource(accessToken, BASE_URL + "/root/search(q='" + query + "')", queryParams.getQueryParametersMap());
    }

    /**
     * Updates the metadata of the specified item
     * 
     * @param accessToken the oauth access token
     * @param itemId the item's identifier
     * @param body The body with the metadata to update (delta)
     * @return the updated item as a {@link JSONObject}
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/beta/api/driveitem_update">Update a file or folder</a>
     */
    public JSONObject patchItem(String accessToken, String itemId, JSONObject body) throws OXException {
        return patchResource(accessToken, BASE_URL + "/items/" + itemId, body);
    }

    /**
     * Creates a folder under the specified parent folder
     * 
     * @param accessToken The oauth access token
     * @param parentItemId The parent identifier (an empty or <code>null</code> parent identifier implies the root folder)
     * @param autorename <code>true</code> if an autorename should happen in case of name conflicts
     * @return The new item as a {@link JSONObject}
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/v1.0/api/driveitem_post_children">Create a new folder in a drive</a>
     */
    public JSONObject createFolder(String accessToken, String folderName, String parentItemId, boolean autorename) throws OXException {
        try {
            JSONObject body = new JSONObject(3);
            body.put("name", folderName);
            body.put("folder", new JSONObject(0)); // indicate that this is a folder
            if (autorename) {
                body.put("@microsoft.graph.conflictBehavior", "rename");
            }
            String path = Strings.isEmpty(parentItemId) ? "/root" : "/items/" + parentItemId;
            return postResource(accessToken, BASE_URL + path + "/children", body);
        } catch (JSONException e) {
            throw RESTExceptionCodes.JSON_ERROR.create(e);
        }
    }

    /**
     * Performs a resumable upload, i.e. a chunk-wise streaming of the data. This is a blocking operation.
     * If another file is already present with the same filename in the same folder then its contents will be
     * overridden.
     * 
     * @param accessToken The oauth access token
     * @param folderId The folder identifier of the parent folder (if empty or <code>null</code> the root folder will be used)
     * @param filename The file's name
     * @param contentType The content type of the file
     * @param contentLength The file's size
     * @param inputStream A stream with the actual data
     * @return A {@link JSONObject} with the metadata of the newly uploaded file.
     * @throws OXException if an error is occurred
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/v1.0/api/driveitem_createuploadsession">Resumable Upload</a>
     */
    public JSONObject streamingUpload(String accessToken, String folderId, String filename, String contentType, long contentLength, InputStream inputStream) throws OXException {
        String path = BASE_URL + (Strings.isEmpty(folderId) ? "/root" : "/items/" + folderId) + ":/" + filename + ":/createUploadSession";
        try {
            JSONObject body = new JSONObject();
            body.put("name", filename);

            JSONObject sessionBody = postResource(accessToken, path, body);
            String uploadUrl = sessionBody.optString("uploadUrl");
            if (Strings.isEmpty(uploadUrl)) {
                throw new OXException(666, "Upload failed");
            }

            int read = 0;
            String range;
            int offset = 0;
            int length = CHUNK_SIZE;
            long remainingSize = contentLength;
            long transferredBytes = 0;
            byte[] b = new byte[CHUNK_SIZE];
            String uploadId = UUID.randomUUID().toString();
            while ((read = inputStream.read(b, 0, length)) > 0) {
                // Calculate current position
                remainingSize -= read;
                transferredBytes += read;
                length = (int) (remainingSize > CHUNK_SIZE ? CHUNK_SIZE : remainingSize);
                // The end index has to be calculated before the offset, otherwise it will mess up the range header.
                range = "bytes " + offset + "-" + (offset + read - 1) + "/" + contentLength;
                offset += read;

                // Compile new put request
                HttpEntityEnclosingRequestBase put = new HttpPut(uploadUrl);
                put.setHeader(HttpHeaders.CONTENT_RANGE, range);
                put.setEntity(new ByteArrayEntity(b, 0, read));

                // Upload
                RESTResponse response = client.execute(put);
                if (response.getStatusCode() < 200 || response.getStatusCode() > 203) {
                    throw new OXException(666, "Upload failed: " + response.getStatusCode() + " " + response.getResponseBody());
                }
                if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
                    LOG.debug("Upload status for upload with id '{}': Successfully completed.", uploadId);
                    return ((JSONValue) response.getResponseBody()).toObject();
                }
                LOG.debug("Upload status for upload with id '{}': Completed: {}% ", uploadId, String.format("%1.2f", (((double) transferredBytes / contentLength * 100))));
            }
            throw new OXException(666, "Upload failed");
        } catch (JSONException e) {
            throw RESTExceptionCodes.JSON_ERROR.create(e);
        } catch (IOException e) {
            throw RESTExceptionCodes.IO_ERROR.create(e);
        }
    }
}
