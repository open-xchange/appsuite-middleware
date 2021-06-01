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

package com.openexchange.ajax.chronos;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.GenericType;
import com.openexchange.test.common.tools.client.EnhancedApiClient;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.invoker.Pair;
import com.openexchange.testing.httpclient.modules.ChronosApi;

/**
 * {@link EnhancedChronosApi} - This enhanced API class overrides the {@link #createEventWithAttachments(String, String, String, File, Boolean, Boolean)}
 * to simply decorate the request with the 'plainJson' parameter. This is only used to get a proper JSON response instead of the regular HTML response
 * and to be able to use the already existing parsers.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class EnhancedChronosApi extends ChronosApi {

    private EnhancedApiClient enhancedApiClient;

    /**
     * Initialises a new {@link EnhancedChronosApi}.
     */
    public EnhancedChronosApi() {
        super();
    }

    /**
     * Initialises a new {@link EnhancedChronosApi}.
     *
     * @param apiClient
     */
    public EnhancedChronosApi(EnhancedApiClient apiClient) {
        super(apiClient);
        enhancedApiClient = apiClient;
    }

    /**
     * Copied from the original {@link #createEventWithAttachments(String, String, String, File, Boolean, Boolean)}. The request is enhanced
     * with the API parameter<code>plainJson</code> to signal the response renderer to return a plain json object instead of the regular
     * HTML response. It also takes multiple files as parameters
     */
    public String createEventWithAttachments(String folder, String json0, List<File> files, Boolean checkConflicts, String scheduling) throws ApiException {
        Object localVarPostBody = null;

        // verify the required parameter 'folder' is set
        if (folder == null) {
            throw new ApiException(400, "Missing the required parameter 'folder' when calling createEventWithAttachments");
        }

        // verify the required parameter 'json0' is set
        if (json0 == null) {
            throw new ApiException(400, "Missing the required parameter 'json0' when calling createEventWithAttachments");
        }

        // verify the required parameter 'file0' is set
        if (files == null) {
            throw new ApiException(400, "Missing the required parameter 'file0' when calling createEventWithAttachments");
        }

        // create path and map variables
        String localVarPath = "/chronos?action=new".replaceAll("\\{format\\}", "json");

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "folder", folder));
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "plainJson", Boolean.TRUE)); //Set this parameter explicitly to 'true' to return a regular json object instead of text/html
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "checkConflicts", checkConflicts));
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "scheduling", scheduling));
        localVarFormParams.put("json_0", json0);

        int index = 0;
        for (File file : files) {
            if (file != null) {
                localVarFormParams.put("file_" + index++, file);
            }
        }

        final String[] localVarAccepts = { "application/json" };
        final String localVarAccept = enhancedApiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = { "multipart/form-data" };
        final String localVarContentType = enhancedApiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "oauth", "session" };

        GenericType<String> localVarReturnType = new GenericType<String>() {
            // empty
        };
        return enhancedApiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Copied from the original {@link #createEventWithAttachments(String, String, String, File, Boolean, Boolean)}. The request is enhanced
     * with the API parameter<code>plainJson</code> to signal the response renderer to return a plain json object instead of the regular
     * HTML response.
     */
    @Override
    public String createEventWithAttachments(String folder, String json0, File file0, Boolean checkConflicts, String scheduling, Boolean extendedEntities, String usedGroups) throws ApiException {
        return createEventWithAttachments(folder, json0, Collections.singletonList(file0), checkConflicts, scheduling);
    }

    /**
     * Copied from the original {@link #updateEventWithAttachments(String, String, String, Long, String, File, String, Boolean, Boolean)}. The request is enhanced
     * with the API parameter<code>plainJson</code> to signal the response renderer to return a plain json object instead of the regular
     * HTML response.
     */
    @Override
    public String updateEventWithAttachments(String folder, String id, Long timestamp, String json0, File file0, String recurrenceId, Boolean checkConflicts, String scheduling, Boolean extendedEntities, String usedGroups) throws ApiException {
        Object localVarPostBody = null;

        // verify the required parameter 'folder' is set
        if (folder == null) {
            throw new ApiException(400, "Missing the required parameter 'folder' when calling updateEventWithAttachments");
        }

        // verify the required parameter 'id' is set
        if (id == null) {
            throw new ApiException(400, "Missing the required parameter 'id' when calling updateEventWithAttachments");
        }

        // verify the required parameter 'timestamp' is set
        if (timestamp == null) {
            throw new ApiException(400, "Missing the required parameter 'timestamp' when calling updateEventWithAttachments");
        }

        // verify the required parameter 'json0' is set
        if (json0 == null) {
            throw new ApiException(400, "Missing the required parameter 'json0' when calling updateEventWithAttachments");
        }

        // verify the required parameter 'file0' is set
        if (file0 == null) {
            throw new ApiException(400, "Missing the required parameter 'file0' when calling updateEventWithAttachments");
        }

        // create path and map variables
        String localVarPath = "/chronos?action=update".replaceAll("\\{format\\}", "json");

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "folder", folder));
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "id", id));
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "plainJson", Boolean.TRUE)); //Set this parameter explicitly to 'true' to return a regular json object instead of text/html
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "recurrenceId", recurrenceId));
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "checkConflicts", checkConflicts));
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "scheduling", scheduling));
        localVarQueryParams.addAll(enhancedApiClient.parameterToPairs("", "timestamp", timestamp));
        localVarFormParams.put("json_0", json0);
        localVarFormParams.put("file_0", file0);

        final String[] localVarAccepts = { "application/json" };
        final String localVarAccept = enhancedApiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = { "multipart/form-data" };
        final String localVarContentType = enhancedApiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "oauth", "session" };

        GenericType<String> localVarReturnType = new GenericType<String>() {
            //empty
        };
        return enhancedApiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
}
