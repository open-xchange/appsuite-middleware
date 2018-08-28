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

import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.microsoft.graph.api.client.MicrosoftGraphRESTClient;
import com.openexchange.microsoft.graph.api.client.MicrosoftGraphRESTEndPoint;
import com.openexchange.rest.client.exception.RESTExceptionCodes;

/**
 * {@link MicrosoftGraphOneDriveAPI}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class MicrosoftGraphOneDriveAPI extends AbstractMicrosoftGraphAPI {

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
     */
    public JSONObject getDrive(String accessToken) throws OXException {
        return getResource(accessToken, "/me" + MicrosoftGraphRESTEndPoint.drive.getAbsolutePath());
    }

    /**
     * Returns the metadata of the root folder for a user's default Drive
     * 
     * @param accessToken The oauth access token
     * @return A {@link JSONObject} with the metadata of the user's root folder
     *         of the default Drive
     * @throws OXException if an error is occurred
     */
    public JSONObject getRoot(String accessToken) throws OXException {
        return getResource(accessToken, "/me" + MicrosoftGraphRESTEndPoint.drive.getAbsolutePath() + "/root");
    }

    /**
     * Returns the metadata of all children (files and folders) of the specified folder.
     * 
     * @param accessToken The oauth access token
     * @param folderPath The folder's absolute path, e.g. <code>/path/to/folder</code>
     * @return A {@link JSONObject} with the metadata of all children (files and folders) of the specified folder.
     * @throws OXException if an error is occurred
     */
    public JSONObject getRootChildren(String accessToken, int offset, String skipToken) throws OXException {
        return getResource(accessToken, "/me" + MicrosoftGraphRESTEndPoint.drive.getAbsolutePath() + "/root/children", compileRangeParams(offset, skipToken));
    }

    /**
     * Returns the metadata of the folder with the specified identifier for a user's default Drive
     * 
     * @param accessToken The oauth access token
     * @param folderPath The folder's unique identifier
     * @return A {@link JSONObject} with the metadata of the user's specified folder
     *         of the default Drive
     * @throws OXException if an error is occurred
     */
    public JSONObject getFolder(String accessToken, String folderId) throws OXException {
        if (Strings.isEmpty(folderId)) {
            return getRoot(accessToken);
        }
        return getResource(accessToken, "/me" + MicrosoftGraphRESTEndPoint.drive.getAbsolutePath() + "/items/" + folderId);
    }

    /**
     * Returns the metadata of all children (files and folders) of the specified folder.
     * 
     * @param accessToken The oauth access token
     * @param folderPath The folder's unique identifier
     * @return A {@link JSONObject} with the metadata of all children (files and folders) of the specified folder.
     * @throws OXException if an error is occurred
     */
    public JSONObject getChildren(String accessToken, String folderId) throws OXException {
        return getChildren(accessToken, folderId, 0, null);
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
     */
    public JSONObject getChildren(String accessToken, String folderId, int offset, String skipToken) throws OXException {
        if (Strings.isEmpty(folderId)) {
            return getRootChildren(accessToken, offset, skipToken);
        }
        return getResource(accessToken, "/me" + MicrosoftGraphRESTEndPoint.drive.getAbsolutePath() + "/items/" + folderId + "/children", compileRangeParams(offset, skipToken));
    }

    /**
     * Returns the item (being either a file or folder) with the specified identifier
     * 
     * @param accessToken The oauth access token
     * @param itemId The item's identifier
     * @return The item as a {@link JSONObject}
     * @throws OXException if an error is occurred
     */
    public JSONObject getItem(String accessToken, String itemId) throws OXException {
        return getResource(accessToken, "/me" + MicrosoftGraphRESTEndPoint.drive.getAbsolutePath() + "/items/" + itemId);
    }

    /**
     * 
     * @param accessToken
     * @param parentItemId
     * @param autorename
     * @return
     * @throws OXException
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
            return postResource(accessToken, "/me" + MicrosoftGraphRESTEndPoint.drive.getAbsolutePath() + path + "/children", body);
        } catch (JSONException e) {
            throw RESTExceptionCodes.JSON_ERROR.create(e);
        }
    }

    ///////////////////// HELPERS //////////////////////

    /**
     * Compiles a map with the range query parameters
     * 
     * @param offset the start offset (i.e. the '$top' parameter)
     * @param skipToken the skip token provided by the API's response
     * @return A {@link Map} with the range query parameters
     */
    private Map<String, String> compileRangeParams(int offset, String skipToken) {
        Map<String, String> queryParams = new HashMap<>(2);
        if (offset > 0) {
            queryParams.put("$top", Integer.toString(offset));
        }
        if (Strings.isNotEmpty(skipToken)) {
            queryParams.put("$skipToken", skipToken);
        }
        return queryParams;
    }
}
